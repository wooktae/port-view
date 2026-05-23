package my.portfolio.port_view.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "connector")
public class ConnectorProperties {

    private String baseUrl = "http://localhost:5000";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = trimTrailingSlash(baseUrl);
    }

    public String buildUrl(String path) {
        if (path == null || path.isBlank()) {
            return baseUrl;
        }

        if (path.startsWith("/")) {
            return baseUrl + path;
        }

        return baseUrl + "/" + path;
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String trimmed = value.trim();

        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        return trimmed;
    }
}
