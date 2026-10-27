package my.portfolio.port_view.util;

public final class StrategyExecutionLabelUtils {

    private StrategyExecutionLabelUtils() {
    }

    public static String planStatusLabel(String value) {
        return switch (upper(value)) {
            case "CREATED" -> text("status.created");
            case "READY" -> text("status.executionCandidate");
            case "VALIDATED" -> text("status.validated");
            case "BLOCKED" -> text("status.blocked");
            case "PARTIALLY_BLOCKED" -> text("status.partiallyBlocked");
            case "NO_CANDIDATE" -> text("status.noCandidate");
            case "FAILED" -> text("status.failed");
            case "ERROR" -> text("status.error");
            default -> fallback(value);
        };
    }

    public static String executionStatusLabel(String value) {
        return switch (upper(value)) {
            case "CANDIDATE" -> text("status.candidate");
            case "READY" -> text("status.orderPending");
            case "SUBMITTED" -> text("status.submitted");
            case "SENT" -> text("status.sent");
            case "ACCEPTED" -> text("status.accepted");
            case "FILLED" -> text("status.filled");
            case "PARTIAL_FILLED" -> text("status.partialFilled");
            case "BLOCKED" -> text("status.blocked");
            case "SKIPPED" -> text("status.skipped");
            case "FAILED" -> text("status.failed");
            case "ERROR" -> text("status.error");
            case "CANCELED" -> text("status.canceled");
            case "CANCEL_ACCEPTED" -> text("status.cancelAccepted");
            default -> fallback(value);
        };
    }

    public static String positionStatusLabel(String value) {
        return switch (upper(value)) {
            case "OPEN" -> text("status.openHolding");
            case "SELL_READY" -> text("status.sellReady");
            case "SELL_ORDERED" -> text("status.sellOrdered");
            case "CLOSED" -> text("status.closed");
            case "SKIPPED" -> text("status.skipped");
            default -> fallback(value);
        };
    }

    public static String actionTypeLabel(String value) {
        return switch (upper(value)) {
            case "BUY" -> text("action.buy");
            case "SELL" -> text("action.sell");
            case "HOLD" -> text("action.hold");
            case "SKIP" -> text("action.skip");
            default -> fallback(value);
        };
    }

    public static String signalTypeLabel(String value) {
        return switch (upper(value)) {
            case "BUY" -> text("signal.buy");
            case "SELL" -> text("signal.sell");
            case "HOLD" -> text("signal.hold");
            case "POSITION_SELL" -> text("signal.positionSell");
            case "TRADE_LOG_BUY" -> text("signal.backtestBuy");
            case "TRADE_LOG_SELL" -> text("signal.backtestSell");
            case "DAILY_SIGNAL_BUY" -> text("signal.dailyBuy");
            case "MANUAL_TEST" -> text("signal.manualTest");
            case "DAILY_POSITION_SELL" -> text("signal.dailyPositionSell");
            case "INTRADAY_STOP_SELL" -> text("signal.intradayStopSell");
            default -> fallback(value);
        };
    }

    public static String sourceTypeLabel(String value) {
        return switch (upper(value)) {
            case "TRADE_LOG_BUY" -> text("signal.backtestBuy");
            case "TRADE_LOG_SELL" -> text("signal.backtestSell");
            case "POSITION_SELL" -> text("source.manualPositionSell");
            case "DAILY_SIGNAL_BUY" -> text("signal.buy");
            case "DAILY_POSITION_SELL" -> text("signal.dailyPositionSell");
            case "INTRADAY_STOP_SELL" -> text("signal.intradayStopSell");
            case "MANUAL_TEST" -> text("signal.manualTest");
            case "STRATEGY_SIGNAL" -> text("source.strategySignal");
            default -> fallback(value);
        };
    }

    public static String orderMethodLabel(String value) {
        return switch (upper(value)) {
            case "MARKET" -> text("order.method.market");
            case "LIMIT" -> text("order.method.limit");
            default -> fallback(value);
        };
    }

    public static String marketSignalLabel(String value) {
        return switch (upper(value)) {
            case "AGGRESSIVE" -> text("market.aggressive");
            case "NEUTRAL" -> text("market.neutral");
            case "DEFENSIVE" -> text("market.defensive");
            case "BLOCK" -> text("market.block");
            case "UNKNOWN" -> text("label.unknown");
            default -> fallback(value);
        };
    }

    public static String riskRegimeLabel(String value) {
        return switch (upper(value)) {
            case "RISK_ON" -> text("risk.on");
            case "RISK_OFF" -> text("risk.off");
            case "NEUTRAL" -> text("risk.neutral");
            case "UNKNOWN" -> text("label.unknown");
            default -> fallback(value);
        };
    }

    public static String requestTypeLabel(String value) {
        return switch (upper(value)) {
            case "BUY" -> text("action.buy");
            case "SELL" -> text("action.sell");
            case "MODIFY" -> text("action.modify");
            case "CANCEL" -> text("action.cancel");
            default -> fallback(value);
        };
    }

    public static String requestStatusLabel(String value) {
        return switch (upper(value)) {
            case "PENDING" -> text("status.pending");
            case "SUBMITTED" -> text("status.submitted");
            case "ACCEPTED" -> text("status.accepted");
            case "FILLED" -> text("status.filled");
            case "PARTIAL_FILLED" -> text("status.partialFilled");
            case "REJECTED" -> text("status.rejected");
            case "FAILED" -> text("status.failed");
            case "CANCELED" -> text("status.canceled");
            case "CANCEL_ACCEPTED" -> text("status.cancelAccepted");
            default -> fallback(value);
        };
    }

    public static String eventTypeLabel(String value) {
        return switch (upper(value)) {
            case "ORDER_ACCEPTED" -> text("status.orderAccepted");
            case "FILLED" -> text("status.filled");
            case "PARTIAL_FILLED" -> text("status.partialFilled");
            case "SUMMARY_ONLY_FILLED" -> text("status.summaryFilled");
            case "SUMMARY_ONLY_ORDER_ACCEPTED" -> text("status.summaryOrderAccepted");
            case "CANCELED" -> text("status.canceled");
            case "CANCEL_ACCEPTED" -> text("status.cancelAccepted");
            case "SUMMARY_ONLY_CANCEL_ACCEPTED" -> text("status.summaryCancelAccepted");
            default -> fallback(value);
        };
    }

    public static String executionModeLabel(String value) {
        return switch (upper(value)) {
            case "DRY_RUN" -> text("execution.mode.dryRun");
            case "MANUAL_TEST" -> text("execution.mode.manualTest");
            case "PAPER_STRATEGY" -> text("execution.mode.paper");
            case "LIVE_STRATEGY" -> text("execution.mode.live");
            default -> fallback(value);
        };
    }

    public static String sellReasonLabel(String value) {
        return switch (upper(value)) {
            case "INTRADAY_ENTRY_HARD_STOP" -> text("reason.intradayEntryHardStop");
            case "INTRADAY_PREV_CLOSE_HARD_STOP" -> text("reason.intradayPrevCloseHardStop");
            case "HOLD_MIN_HOLDING_DAYS" -> text("reason.minHoldingDays");
            case "SELL_FILLED" -> text("reason.sellFilled");
            case "POSITION_SELL" -> text("reason.positionSell");
            case "DAILY_POSITION_SELL" -> text("signal.dailyPositionSell");
            case "INTRADAY_STOP_SELL" -> text("signal.intradayStopSell");
            case "MARKET_BLOCK_WEAK" -> text("reason.marketBlockWeak");
            case "QUALITY_DROP" -> text("reason.qualityDrop");
            case "HARD_STOP" -> text("reason.hardStop");
            case "PROFIT_PROTECT" -> text("reason.profitProtect");
            case "MAX_HOLDING_DAYS" -> text("reason.maxHoldingDays");
            default -> fallback(value);
        };
    }

    public static String blockReasonLabel(String value) {
        return switch (upper(value)) {
            case "NO_SELLABLE_QTY" -> text("reason.noSellableQty");
            case "LATEST_CONNECTOR_POSITION_SNAPSHOT_NOT_FOUND" -> text("reason.latestPositionMissing");
            case "LATEST_POSITION_QTY_ZERO" -> text("reason.latestPositionQtyZero");
            case "LATEST_SELLABLE_QTY_ZERO" -> text("reason.latestSellableQtyZero");
            case "ORDER_QTY_GT_LATEST_SELLABLE_QTY" -> text("reason.orderQtyExceedsSellable");
            case "STRATEGY_ORDER_QTY_ZERO" -> text("reason.strategyOrderQtyZero");
            case "TEST_INTRADAY_STOP_SELL_CANDIDATE" -> text("reason.testIntradayStopCandidate");
            case "ALL EXECUTION ORDERS BLOCKED" -> text("reason.allOrdersBlocked");
            default -> fallback(value);
        };
    }

    public static String upper(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    public static String fallback(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static String text(String key) {
        return ViewMessages.text(key);
    }
}
