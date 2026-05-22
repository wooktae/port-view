package my.portfolio.port_view.common;

/**
 * Thymeleaf view template name 상수 모음.
 *
 * 목적:
 * - Controller에서 문자열 template name 하드코딩 제거
 * - html 파일명 변경 시 한 곳에서 관리
 * - 오타로 인한 template resolve 오류 방지
 */
public final class ViewNames {

    private ViewNames() {
        // utility class
    }

    // Migrated pages/* templates.
    public static final String DASHBOARD = "pages/dashboard";

    public static final String ORDERS = "pages/orders";
    public static final String ORDER_DETAIL = "pages/order-detail";

    public static final String POSITIONS = "pages/positions";
    public static final String POSITION_DETAIL = "pages/position-detail";

    public static final String STRATEGY_PLANS = "pages/strategy_plans";
    public static final String STRATEGY_DETAIL = "pages/strategy_detail";

    public static final String DAILY_BATCH = "pages/daily_batch";

    public static final String BALANCE_SUMMARY = "pages/balance-summary";
    public static final String STRATEGY_REPORT = "pages/strategy_report";

    // Active root templates. Keep values unchanged until each view is migrated.
    public static final String HOLDINGS = "holdings";
    public static final String TRADE_ORDERS = "trade-orders";
    public static final String GET_PRICE_REALTIME = "get-price-realtime";
    public static final String STRATEGY_DAILY = "strategy_daily";
}
