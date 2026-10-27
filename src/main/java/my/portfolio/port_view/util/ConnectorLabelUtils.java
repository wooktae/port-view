package my.portfolio.port_view.util;

public final class ConnectorLabelUtils {

    private ConnectorLabelUtils() {
    }

    public static String orderMethodLabel(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return ViewMessages.text("label.unknown");
        }
        return switch (normalized) {
            case "MARKET" -> ViewMessages.text("order.method.market");
            case "LIMIT" -> ViewMessages.text("order.method.limit");
            case "BEST" -> ViewMessages.text("order.method.best");
            case "MOC" -> ViewMessages.text("order.method.moc");
            case "LOC" -> ViewMessages.text("order.method.loc");
            default -> String.valueOf(value);
        };
    }

    public static String requestTypeLabel(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return ViewMessages.text("label.unknown");
        }
        return switch (normalized) {
            case "BUY" -> ViewMessages.text("action.buy");
            case "SELL" -> ViewMessages.text("action.sell");
            case "MODIFY" -> ViewMessages.text("action.modify");
            case "CANCEL" -> ViewMessages.text("action.cancel");
            default -> String.valueOf(value);
        };
    }

    public static String sideLabel(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return ViewMessages.text("label.unknown");
        }
        return switch (normalized) {
            case "BUY", "매수" -> ViewMessages.text("action.buy");
            case "SELL", "매도" -> ViewMessages.text("action.sell");
            case "UNKNOWN" -> ViewMessages.text("label.unknown");
            default -> String.valueOf(value);
        };
    }

    public static String requestStatusLabel(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return ViewMessages.text("label.unknown");
        }
        return switch (normalized) {
            case "PENDING" -> ViewMessages.text("status.pending");
            case "READY" -> ViewMessages.text("status.ready");
            case "SUBMITTED", "SENT" -> ViewMessages.text("status.submitted");
            case "ACCEPTED" -> ViewMessages.text("status.accepted");
            case "ORDER_ACCEPTED" -> ViewMessages.text("status.orderAccepted");
            case "PARTIAL_FILLED" -> ViewMessages.text("status.partialFilled");
            case "FILLED" -> ViewMessages.text("status.filled");
            case "REJECTED" -> ViewMessages.text("status.rejected");
            case "CANCELED", "CANCELLED" -> ViewMessages.text("status.canceled");
            case "CANCEL_ACCEPTED" -> ViewMessages.text("status.cancelAccepted");
            case "MODIFIED" -> ViewMessages.text("status.modified");
            case "MODIFY_ACCEPTED" -> ViewMessages.text("status.modifyAccepted");
            default -> String.valueOf(value);
        };
    }

    public static String eventTypeLabel(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return ViewMessages.text("label.unknown");
        }
        return switch (normalized) {
            case "ORDER_ACCEPTED" -> ViewMessages.text("status.orderAccepted");
            case "SUMMARY_ONLY_ORDER_ACCEPTED" -> ViewMessages.text("status.summaryOrderAccepted");
            case "FILLED" -> ViewMessages.text("status.filled");
            case "SUMMARY_ONLY_FILLED" -> ViewMessages.text("status.summaryFilled");
            case "PARTIAL_FILLED" -> ViewMessages.text("status.partialFilled");
            case "SUMMARY_ONLY_PARTIAL_FILLED" -> ViewMessages.text("status.summaryPartialFilled");
            case "CANCELED", "CANCELLED" -> ViewMessages.text("status.canceled");
            case "CANCEL_ACCEPTED" -> ViewMessages.text("status.cancelAccepted");
            case "SUMMARY_ONLY_CANCEL_ACCEPTED" -> ViewMessages.text("status.summaryCancelAccepted");
            case "MODIFIED" -> ViewMessages.text("status.modified");
            case "MODIFY_ACCEPTED" -> ViewMessages.text("status.modifyAccepted");
            default -> String.valueOf(value);
        };
    }

    public static String executionStatusLabel(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return ViewMessages.text("label.unknown");
        }
        return switch (normalized) {
            case "CANDIDATE" -> ViewMessages.text("status.candidate");
            case "READY" -> ViewMessages.text("status.orderPending");
            case "SUBMITTED", "SENT" -> ViewMessages.text("status.submitted");
            case "ACCEPTED" -> ViewMessages.text("status.accepted");
            case "FILLED" -> ViewMessages.text("status.filled");
            case "BLOCKED" -> ViewMessages.text("status.blocked");
            case "SKIPPED" -> ViewMessages.text("status.skipped");
            case "FAILED" -> ViewMessages.text("status.failed");
            default -> String.valueOf(value);
        };
    }

    public static String positionStatusLabel(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return ViewMessages.text("label.unknown");
        }
        return switch (normalized) {
            case "OPEN" -> ViewMessages.text("status.openHolding");
            case "SELL_READY" -> ViewMessages.text("status.sellReady");
            case "SELL_ORDERED" -> ViewMessages.text("status.sellOrdered");
            case "CLOSED" -> ViewMessages.text("status.closed");
            default -> String.valueOf(value);
        };
    }

    public static String sourceTypeLabel(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return ViewMessages.text("label.unknown");
        }
        return switch (normalized) {
            case "TRADE_LOG_BUY" -> ViewMessages.text("source.strategyBuyCandidate");
            case "TRADE_LOG_SELL" -> ViewMessages.text("source.strategySellCandidate");
            case "POSITION_SELL" -> ViewMessages.text("source.manualPositionSell");
            case "DAILY_SIGNAL_BUY" -> ViewMessages.text("source.dailyBuy");
            case "DAILY_POSITION_SELL" -> ViewMessages.text("signal.dailyPositionSell");
            case "INTRADAY_STOP_SELL" -> ViewMessages.text("signal.intradayStopSell");
            case "MANUAL_TEST" -> ViewMessages.text("signal.manualTest");
            case "STRATEGY_SIGNAL" -> ViewMessages.text("source.strategySignal");
            default -> String.valueOf(value);
        };
    }

    private static String normalize(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        if (text.isBlank() || "-".equals(text)) {
            return null;
        }
        return text.toUpperCase();
    }
}
