package my.portfolio.port_view.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds execution options for refreshing Connector Balance Snapshots.
 * Paths and the Python executable for each production environment should be injected through
 * environment variables or local configuration.
 */
@Component
@ConfigurationProperties(prefix = "portfolio.snapshot-refresh")
public class SnapshotRefreshProperties {

    private boolean enabled = true;

    /**
     * Runs connector_balance.py when the latest Snapshot is older than this many minutes.
     */
    private long staleMinutes = 5L;

    /**
     * Timeout for one connector_balance.py execution.
     */
    private long timeoutSeconds = 60L;

    private String pythonExecutable = "python";

    private String marketconnectorDir = "C:/Workspaces/port-marketconnector";

    private String balanceScriptName = "connector_balance.py";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getStaleMinutes() {
        return staleMinutes;
    }

    public void setStaleMinutes(long staleMinutes) {
        this.staleMinutes = staleMinutes;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getPythonExecutable() {
        return pythonExecutable;
    }

    public void setPythonExecutable(String pythonExecutable) {
        this.pythonExecutable = pythonExecutable;
    }

    public String getMarketconnectorDir() {
        return marketconnectorDir;
    }

    public void setMarketconnectorDir(String marketconnectorDir) {
        this.marketconnectorDir = marketconnectorDir;
    }

    public String getBalanceScriptName() {
        return balanceScriptName;
    }

    public void setBalanceScriptName(String balanceScriptName) {
        this.balanceScriptName = balanceScriptName;
    }
}
