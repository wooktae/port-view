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

    private void runConnectorBalance() {
        Process process = null;

        try {
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