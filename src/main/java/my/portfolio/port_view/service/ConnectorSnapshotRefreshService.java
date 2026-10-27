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
 * Refreshes the latest Connector Balance Snapshot required by the Balance, Dashboard, and Positions views.
 * Because this runs an external Python script, it depends on working-directory, executable, and timeout settings.
 * An instance-local AtomicBoolean prevents duplicate execution.
 */
@Service
public class ConnectorSnapshotRefreshService {

    private final ConnectorBalanceSnapshotRepository balanceSnapshotRepository;
    private final SnapshotRefreshProperties properties;

    /**
     * Prevents duplicate connector_balance.py execution during initial View access and navigation.
     * Sufficient for a single port-view instance.
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
     * Runs the Connector Balance script only when the latest Snapshot exceeds the stale threshold.
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
     * Runs the Connector Balance script immediately on view entry, regardless of stale state.
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
             * Swallow the exception here so it does not block access to the view itself.
             * If Refresh fails, display the view using the existing latest Snapshot.
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
