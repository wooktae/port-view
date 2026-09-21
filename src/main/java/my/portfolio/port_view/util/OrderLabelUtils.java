package my.portfolio.port_view.util;

public final class OrderLabelUtils {

    private OrderLabelUtils() {
    }

    public static String requestTypeLabel(String requestType) {
        return switch (upper(requestType)) {
            case "BUY" -> ViewMessages.text("action.buy");
            case "SELL" -> ViewMessages.text("action.sell");
            case "MODIFY" -> ViewMessages.text("action.modify");
            case "CANCEL" -> ViewMessages.text("action.cancel");
            default -> blankTo(requestType, "UNKNOWN");
        };
    }

    public static String requestTypeClass(String requestType) {
        return switch (upper(requestType)) {
            case "BUY" -> "buy";
            case "SELL" -> "sell";
            case "MODIFY" -> "modify";
            case "CANCEL" -> "cancel";
            default -> "unknown";
        };
    }

    public static String statusLabel(String status) {
        return switch (upper(status)) {
            case "REQUESTED" -> ViewMessages.text("status.requested");
            case "ACCEPTED" -> ViewMessages.text("order.simple.accepted");
            case "ORDER_ACCEPTED" -> ViewMessages.text("order.simple.orderAccepted");
            case "FILLED" -> ViewMessages.text("order.simple.filled");
            case "PARTIALLY_FILLED" -> ViewMessages.text("order.simple.partialFilled");
            case "CANCELED" -> ViewMessages.text("order.simple.canceled");
            case "CANCEL_ACCEPTED" -> ViewMessages.text("order.simple.cancelAccepted");
            case "MODIFY_ACCEPTED" -> ViewMessages.text("order.simple.modifyAccepted");
            case "MODIFIED" -> ViewMessages.text("order.simple.modified");
            case "REJECTED" -> ViewMessages.text("order.simple.rejected");
            default -> blankTo(status, "UNKNOWN");
        };
    }

    public static String statusClass(String status) {
        return switch (upper(status)) {
            case "FILLED", "PARTIALLY_FILLED" -> "filled";
            case "ACCEPTED", "ORDER_ACCEPTED", "REQUESTED" -> "accepted";
            case "MODIFY_ACCEPTED" -> "modify";
            case "MODIFIED" -> "modified";
            case "CANCEL_ACCEPTED" -> "cancel-accepted";
            case "CANCELED" -> "canceled";
            case "REJECTED" -> "rejected";
            default -> "unknown";
        };
    }

    public static String eventTypeLabel(String eventType) {
        return switch (upper(eventType)) {
            case "ORDER_ACCEPTED", "SUMMARY_ONLY_ORDER_ACCEPTED" -> ViewMessages.text("order.simple.orderAccepted");
            case "FILLED", "SUMMARY_ONLY_FILLED" -> ViewMessages.text("order.simple.filled");
            case "CANCELED", "SUMMARY_ONLY_CANCELED" -> ViewMessages.text("order.simple.canceled");
            case "CANCEL_ACCEPTED", "SUMMARY_ONLY_CANCEL_ACCEPTED" -> ViewMessages.text("order.simple.cancelAccepted");
            case "MODIFY_ACCEPTED", "SUMMARY_ONLY_MODIFY_ACCEPTED" -> ViewMessages.text("order.simple.modifyAccepted");
            default -> blankTo(eventType, "EVENT");
        };
    }

    public static String eventTypeClass(String eventType) {
        String event = upper(eventType);
        if (event.contains("FILLED")) return "filled";
        if (event.contains("CANCEL_ACCEPTED")) return "cancel-accepted";
        if (event.contains("CANCELED")) return "canceled";
        if (event.contains("MODIFY")) return "modify";
        if (event.contains("ORDER_ACCEPTED")) return "accepted";
        return "unknown";
    }

    public static String sideLabel(String side) {
        return switch (upper(side)) {
            case "BUY" -> ViewMessages.text("action.buy");
            case "SELL" -> ViewMessages.text("action.sell");
            case "UNKNOWN" -> ViewMessages.text("label.unknown");
            default -> blankTo(side, "-");
        };
    }

    public static String sideClass(String side) {
        return switch (upper(side)) {
            case "BUY" -> "buy";
            case "SELL" -> "sell";
            default -> "unknown";
        };
    }

    private static String blankTo(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String upper(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
