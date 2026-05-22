package my.portfolio.port_view.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "portfolio.snapshot-refresh")
public class SnapshotRefreshProperties {

    private boolean enabled = true;

    /**
     * 최신 snapshot이 이 분 수보다 오래됐으면 connector_balance.py 실행.
     */
    private long staleMinutes = 5L;

    /**
     * connector_balance.py 1회 실행 timeout.
     */
    private long timeoutSeconds = 60L;

    private String pythonExecutable = "python";

    private String marketconnectorDir = "";

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
