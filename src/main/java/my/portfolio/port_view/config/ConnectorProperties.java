package my.portfolio.port_view.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "connector")
public class ConnectorProperties {

    /**
     * Market Connector MS 기본 URL.
     * 예: http://localhost:5000
     */
    private String baseUrl = "http://localhost:5000";

    /**
     * Connector View API 경로 모음.
     */
    private Api api = new Api();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = trimTrailingSlash(baseUrl);
    }

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
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

    public static class Api {

        /**
         * 계좌 요약 조회 API.
         */
        private String balanceLatestPath = "/api/v1/view/balance/latest";

        /**
         * 보유 포지션 최신 조회 API.
         */
        private String positionsLatestPath = "/api/v1/view/positions/latest";

        /**
         * 주문 목록 조회 API.
         */
        private String ordersPath = "/api/v1/view/orders";

        /**
         * 주문 상세 조회 API.
         * 사용 시 /{id} 붙여서 호출.
         */
        private String orderDetailPath = "/api/v1/view/orders";

        /**
         * 실시간 가격 조회 API.
         */
        private String realtimePricePath = "/api/v1/view/quotes/realtime";

        /**
         * EOD 가격 조회 API.
         */
        private String eodPricePath = "/api/v1/view/quotes/eod";

        public String getBalanceLatestPath() {
            return balanceLatestPath;
        }

        public void setBalanceLatestPath(String balanceLatestPath) {
            this.balanceLatestPath = balanceLatestPath;
        }

        public String getPositionsLatestPath() {
            return positionsLatestPath;
        }

        public void setPositionsLatestPath(String positionsLatestPath) {
            this.positionsLatestPath = positionsLatestPath;
        }

        public String getOrdersPath() {
            return ordersPath;
        }

        public void setOrdersPath(String ordersPath) {
            this.ordersPath = ordersPath;
        }

        public String getOrderDetailPath() {
            return orderDetailPath;
        }

        public void setOrderDetailPath(String orderDetailPath) {
            this.orderDetailPath = orderDetailPath;
        }

        public String getRealtimePricePath() {
            return realtimePricePath;
        }

        public void setRealtimePricePath(String realtimePricePath) {
            this.realtimePricePath = realtimePricePath;
        }

        public String getEodPricePath() {
            return eodPricePath;
        }

        public void setEodPricePath(String eodPricePath) {
            this.eodPricePath = eodPricePath;
        }
    }
}
