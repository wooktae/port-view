package my.portfolio.port_view.service;

import lombok.RequiredArgsConstructor;
import my.portfolio.port_view.config.DailyBatchProperties;
import my.portfolio.port_view.entity.ConnectorOrderRequest;
import my.portfolio.port_view.repository.ConnectorOrderRequestRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 주문 내역 화면 진입 시 미체결/부분체결 주문의 최신 체결 상태를 Connector 스크립트로 확인한다.
 * 화면 조회 자체를 막지 않기 위해 외부 프로세스 실패는 로그로만 남긴다.
 * 작업 디렉터리와 Python 실행 파일은 Daily Batch 설정을 공유한다.
 */
@Service
@RequiredArgsConstructor
public class ConnectorOrderRefreshService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter KIS_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final int REFRESH_LOOKBACK_DAYS = 7;
    private static final int REFRESH_LIMIT = 20;
    private static final long TIMEOUT_SECONDS = 90;

    private final ConnectorOrderRequestRepository orderRepository;
    private final DailyBatchProperties properties;

    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 최근 주문 중 갱신 대상만 선별해 순차적으로 Connector 주문 조회 스크립트를 실행한다.
     */
    public void refreshOpenOrders(String accountNo) {
        System.out.println("[ConnectorOrderRefreshService] refreshOpenOrders called. accountNo=" + accountNo);

        if (accountNo == null || accountNo.isBlank()) {
            System.out.println("[ConnectorOrderRefreshService] accountNo blank. skip.");
            return;
        }

        if (!running.compareAndSet(false, true)) {
            System.out.println("[ConnectorOrderRefreshService] refresh already running. skip.");
            return;
        }

        try {
            List<ConnectorOrderRequest> targets =
                    orderRepository.findRefreshTargetOrders(accountNo, REFRESH_LOOKBACK_DAYS, REFRESH_LIMIT);

            System.out.println("[ConnectorOrderRefreshService] refresh target count=" + targets.size());

            for (ConnectorOrderRequest order : targets) {
                refreshOne(order);
            }
        } catch (Exception e) {
            // 화면 진입 자체를 막지 않기 위해 refresh 실패는 로그만 남긴다.
            System.err.println("[ConnectorOrderRefreshService] refresh failed: " + e.getMessage());
        } finally {
            running.set(false);
        }
    }

    /**
     * 단일 주문의 broker 주문번호를 기준으로 외부 Connector 주문 확인 스크립트를 호출한다.
     */
    private void refreshOne(ConnectorOrderRequest order) {
        if (order == null) {
            return;
        }

        String brokerOrderNo = trimToNull(order.getBrokerOrderNo());
        String brokerBranchCode = trimToNull(order.getBrokerBranchCode());
        String tickerCode = trimToNull(order.getTickerCode());

        if (brokerOrderNo == null) {
            System.out.println("[ConnectorOrderRefreshService] brokerOrderNo blank. orderId=" + order.getId());
            return;
        }

        LocalDate startDate = order.getRequestedAt() == null
                ? LocalDate.now(SEOUL_ZONE).minusDays(REFRESH_LOOKBACK_DAYS)
                : order.getRequestedAt().toLocalDate();

        LocalDate endDate = LocalDate.now(SEOUL_ZONE);

        List<String> command = buildCommand(
                startDate,
                endDate,
                tickerCode,
                brokerOrderNo,
                brokerBranchCode
        );

        System.out.println(
                "[ConnectorOrderRefreshService] run order check. "
                        + "orderId=" + order.getId()
                        + ", ticker=" + tickerCode
                        + ", orderNo=" + brokerOrderNo
                        + ", status=" + order.getRequestStatus()
                        + ", command=" + String.join(" ", command)
        );

        Process process = null;

        try {
            String workDir = properties.getMarketconnectorDir();

            if (workDir == null || workDir.isBlank()) {
                throw new IllegalStateException("marketconnectorDir is blank.");
            }

            File directory = new File(workDir);

            if (!directory.exists() || !directory.isDirectory()) {
                throw new IllegalStateException("marketconnectorDir is invalid. dir=" + directory.getAbsolutePath());
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(directory);
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
            processBuilder.environment().put("PYTHONUTF8", "1");

            process = processBuilder.start();

            StreamCollector stdoutCollector = new StreamCollector(process.getInputStream());
            StreamCollector stderrCollector = new StreamCollector(process.getErrorStream());

            Thread stdoutThread = new Thread(stdoutCollector, "order-refresh-stdout-" + order.getId());
            Thread stderrThread = new Thread(stderrCollector, "order-refresh-stderr-" + order.getId());

            stdoutThread.start();
            stderrThread.start();

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                stdoutThread.join(3000);
                stderrThread.join(3000);

                System.err.println("[ConnectorOrderRefreshService] timeout. orderId=" + order.getId());
                return;
            }

            int exitCode = process.exitValue();

            stdoutThread.join(3000);
            stderrThread.join(3000);

            if (exitCode != 0) {
                System.err.println(
                        "[ConnectorOrderRefreshService] order check failed. "
                                + "orderId=" + order.getId()
                                + ", exitCode=" + exitCode
                                + ", stderr=" + tail(stderrCollector.content())
                                + ", stdout=" + tail(stdoutCollector.content())
                );
                return;
            }

            System.out.println(
                    "[ConnectorOrderRefreshService] order check finished. "
                            + "orderId=" + order.getId()
                            + ", stdout=" + tail(stdoutCollector.content())
            );
        } catch (Exception e) {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }

            System.err.println(
                    "[ConnectorOrderRefreshService] order check exception. "
                            + "orderId=" + order.getId()
                            + ", message=" + e.getMessage()
            );
        }
    }

    private List<String> buildCommand(
            LocalDate startDate,
            LocalDate endDate,
            String tickerCode,
            String brokerOrderNo,
            String brokerBranchCode
    ) {
        String python = properties.getPythonExecutable();

        return List.of(
                python,
                "connector_order_check.py",
                "--start",
                startDate.format(KIS_DATE_FORMATTER),
                "--end",
                endDate.format(KIS_DATE_FORMATTER),
                "--code",
                tickerCode == null ? "" : tickerCode,
                "--order-no",
                brokerOrderNo,
                "--branch-code",
                brokerBranchCode == null ? "" : brokerBranchCode
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String tail(String value) {
        if (value == null) {
            return "";
        }

        int maxLength = 1200;

        if (value.length() <= maxLength) {
            return value.replace(System.lineSeparator(), " | ");
        }

        return value.substring(value.length() - maxLength).replace(System.lineSeparator(), " | ");
    }

    private static class StreamCollector implements Runnable {

        private final InputStream inputStream;
        private final StringBuilder content = new StringBuilder();

        private StreamCollector(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try (
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream, StandardCharsets.UTF_8)
                    )
            ) {
                String line;

                while ((line = reader.readLine()) != null) {
                    content.append(line).append(System.lineSeparator());
                }
            } catch (Exception e) {
                content.append("[STREAM_READ_ERROR] ")
                        .append(e.getClass().getSimpleName())
                        .append(": ")
                        .append(e.getMessage())
                        .append(System.lineSeparator());
            }
        }

        private String content() {
            return content.toString();
        }
    }
}
