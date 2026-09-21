package my.portfolio.port_view.common;

/**
 * Collection of Thymeleaf view template name constants.
 *
 * Purpose:
 * - Eliminate hard-coded string template names in Controllers
 * - Manage html filename changes in one place
 * - Prevent template resolution errors caused by typos
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

}
