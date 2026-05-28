package my.portfolio.port_view.service;

import my.portfolio.port_view.config.SnapshotRefreshProperties;
import my.portfolio.port_view.entity.ConnectorBalanceSnapshot;
import my.portfolio.port_view.repository.ConnectorBalanceSnapshotRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 잔고/대시보드/보유 종목 화면에서 필요한 최신 Connector 잔고 스냅샷을 갱신한다.
 * 외부 Python 스크립트를 실행하므로 작업 디렉터리, 실행 파일, timeout 설정에 의존한다.
 * 중복 실행은 인스턴스 내부 AtomicBoolean으로 방지한다.
 */
@Service
public class ConnectorSnapshotRefreshService {

    private final ConnectorBalanceSnapshotRepository balanceSnapshotRepository;
    private final SnapshotRefreshProperties properties;

    /**
     * View 최초 접속/화면 이동 시 connector_balance.py 중복 실행 방지용.
     * 단일 port-view 인스턴스 기준으로 충분함.
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    public ConnectorSnapshotRefreshService(
            ConnectorBalanceSnapshotRepository balanceSnapshotRepository,
            SnapshotRefreshProperties properties
    ) {
        this.balanceSnapshotRepository = balanceSnapshotRepository;
        this.properties = properties;
    }

    /**
     * 최신 스냅샷이 stale 기준을 넘은 경우에만 Connector 잔고 스크립트를 실행한다.
     */
    public void refreshIfStale(String accountNo) {
        if (!properties.isEnabled()) {
            return;
        }

        if (accountNo == null || accountNo.isBlank()) {
            return;
        }

        if (!isStale(accountNo)) {
            return;
        }

        if (!running.compareAndSet(false, true)) {
            return;
        }

        try {
            runConnectorBalance();
        } finally {
            running.set(false);
        }
    }

    private boolean isStale(String accountNo) {
        Optional<ConnectorBalanceSnapshot> latestOpt =
                balanceSnapshotRepository.findTopByAccountNoOrderByAsOfDateDescAsOfTsDesc(accountNo);

        if (latestOpt.isEmpty()) {
            return true;
        }

        OffsetDateTime latestAsOfTs = latestOpt
                .map(ConnectorBalanceSnapshot::getAsOfTs)
                .orElse(null);

        if (latestAsOfTs == null) {
            return true;
        }

        long ageMinutes = Duration.between(latestAsOfTs, OffsetDateTime.now()).toMinutes();

        return ageMinutes >= properties.getStaleMinutes();
    }

    /**
     * 화면 진입 시 stale 여부와 무관하게 Connector 잔고 스크립트를 즉시 실행한다.
     */
    public void refreshNow(String accountNo) {
        System.out.println("[ConnectorSnapshotRefreshService] refreshNow called. accountNo=" + accountNo);

        if (!properties.isEnabled()) {
            System.out.println("[ConnectorSnapshotRefreshService] refresh disabled.");
            return;
        }

        if (accountNo == null || accountNo.isBlank()) {
            System.out.println("[ConnectorSnapshotRefreshService] accountNo blank.");
            return;
        }

        if (!running.compareAndSet(false, true)) {
            System.out.println("[ConnectorSnapshotRefreshService] refresh already running.");
            return;
        }

        try {
            System.out.println("[ConnectorSnapshotRefreshService] run connector balance start.");
            runConnectorBalance();

            balanceSnapshotRepository
                    .findTopByAccountNoOrderByAsOfDateDescAsOfTsDesc(accountNo)
                    .ifPresent(snapshot -> System.out.println(
                            "[ConnectorSnapshotRefreshService] latest after refresh. "
                                    + "id=" + snapshot.getId()
                                    + ", accountNo=" + snapshot.getAccountNo()
                                    + ", asOfDate=" + snapshot.getAsOfDate()
                                    + ", asOfTs=" + snapshot.getAsOfTs()
                                    + ", cashBalance=" + snapshot.getCashBalance()
                                    + ", totalEvalAmount=" + snapshot.getTotalEvalAmount()
                                    + ", evalProfit=" + snapshot.getEvalProfit()
                    ));

            System.out.println("[ConnectorSnapshotRefreshService] run connector balance finished.");
        } finally {
            running.set(false);
        }
    }

    private void runConnectorBalance() {
        Process process = null;

        try {
            System.out.println("[ConnectorSnapshotRefreshService] dir=" + properties.getMarketconnectorDir());
            System.out.println("[ConnectorSnapshotRefreshService] python=" + properties.getPythonExecutable());
            System.out.println("[ConnectorSnapshotRefreshService] script=" + properties.getBalanceScriptName());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    properties.getPythonExecutable(),
                    properties.getBalanceScriptName()
            );

            processBuilder.directory(new File(properties.getMarketconnectorDir()));

            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
            processBuilder.environment().put("PYTHONUTF8", "1");

            process = processBuilder.start();

            StreamCollector stdoutCollector = new StreamCollector(process.getInputStream());
            StreamCollector stderrCollector = new StreamCollector(process.getErrorStream());

            Thread stdoutThread = new Thread(stdoutCollector, "snapshot-refresh-stdout");
            Thread stderrThread = new Thread(stderrCollector, "snapshot-refresh-stderr");

            stdoutThread.start();
            stderrThread.start();

            boolean finished = process.waitFor(
                    properties.getTimeoutSeconds(),
                    TimeUnit.SECONDS
            );

            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException(
                        "Connector Balance refresh timeout. timeoutSeconds="
                                + properties.getTimeoutSeconds()
                );
            }

            int exitCode = process.exitValue();

            stdoutThread.join(3000);
            stderrThread.join(3000);

            if (exitCode != 0) {
                throw new IllegalStateException(
                        "Connector Balance refresh failed. exitCode="
                                + exitCode
                                + ", stdout="
                                + tail(stdoutCollector.content())
                                + ", stderr="
                                + tail(stderrCollector.content())
                );
            }
        } catch (Exception e) {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }

            /*
             * 화면 접속 자체를 막지 않기 위해 여기서는 예외를 삼킨다.
             * refresh 실패 시 기존 최신 snapshot 기준으로 화면 표시.
             */
            System.err.println("[ConnectorSnapshotRefreshService] refresh failed: " + e.getMessage());
        }
    }

    private String tail(String value) {
        if (value == null) {
            return "";
        }

        int max = 2000;

        if (value.length() <= max) {
            return value;
        }

        return value.substring(value.length() - max);
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
