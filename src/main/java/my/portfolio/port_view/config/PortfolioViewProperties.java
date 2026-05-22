package my.portfolio.port_view.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "portfolio.view")
public class PortfolioViewProperties {

    private Account account = new Account();
    private Dashboard dashboard = new Dashboard();
    private Orders orders = new Orders();
    private Positions positions = new Positions();
    private StrategyExecution strategyExecution = new StrategyExecution();

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    public Orders getOrders() {
        return orders;
    }

    public void setOrders(Orders orders) {
        this.orders = orders;
    }

    public Positions getPositions() {
        return positions;
    }

    public void setPositions(Positions positions) {
        this.positions = positions;
    }

    public StrategyExecution getStrategyExecution() {
        return strategyExecution;
    }

    public void setStrategyExecution(StrategyExecution strategyExecution) {
        this.strategyExecution = strategyExecution;
    }

    public static class Account {

        /**
         * View 화면에서 사용할 기본 계좌번호.
         * 추후 Controller 하드코딩 제거 시 사용.
         */
        private String defaultAccountNo;

        public String getDefaultAccountNo() {
            return defaultAccountNo;
        }

        public void setDefaultAccountNo(String defaultAccountNo) {
            this.defaultAccountNo = defaultAccountNo;
        }
    }

    public static class Dashboard {

        /**
         * Dashboard 최근 주문 표시 개수.
         */
        private int recentOrderLimit = 5;

        /**
         * Dashboard 최근 체결/거래 표시 개수.
         */
        private int recentTradeLimit = 5;

        public int getRecentOrderLimit() {
            return recentOrderLimit;
        }

        public void setRecentOrderLimit(int recentOrderLimit) {
            this.recentOrderLimit = recentOrderLimit;
        }

        public int getRecentTradeLimit() {
            return recentTradeLimit;
        }

        public void setRecentTradeLimit(int recentTradeLimit) {
            this.recentTradeLimit = recentTradeLimit;
        }
    }

    public static class Orders {

        /**
         * 주문 목록 기본 조회 개수.
         */
        private int defaultLimit = 50;

        public int getDefaultLimit() {
            return defaultLimit;
        }

        public void setDefaultLimit(int defaultLimit) {
            this.defaultLimit = defaultLimit;
        }
    }

    public static class Positions {

        /**
         * 보유 종목 화면 기본 조회 개수.
         */
        private int defaultLimit = 50;

        public int getDefaultLimit() {
            return defaultLimit;
        }

        public void setDefaultLimit(int defaultLimit) {
            this.defaultLimit = defaultLimit;
        }
    }

    public static class StrategyExecution {

        /**
         * 전략 실행 계획 목록 기본 조회 개수.
         */
        private int defaultPlanLimit = 50;

        /**
         * 전략 실행 주문 목록 기본 조회 개수.
         */
        private int defaultOrderLimit = 50;

        public int getDefaultPlanLimit() {
            return defaultPlanLimit;
        }

        public void setDefaultPlanLimit(int defaultPlanLimit) {
            this.defaultPlanLimit = defaultPlanLimit;
        }

        public int getDefaultOrderLimit() {
            return defaultOrderLimit;
        }

        public void setDefaultOrderLimit(int defaultOrderLimit) {
            this.defaultOrderLimit = defaultOrderLimit;
        }
    }
}
