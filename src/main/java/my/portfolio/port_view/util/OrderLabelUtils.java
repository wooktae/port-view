package my.portfolio.port_view.util;

public final class OrderLabelUtils {

    private OrderLabelUtils() {
    }

    public static String requestTypeLabel(String requestType) {
        String type = upper(requestType);

        return switch (type) {
            case "BUY" -> "매수";
            case "SELL" -> "매도";
            case "MODIFY" -> "정정";
            case "CANCEL" -> "취소";
            default -> blankTo(requestType, "UNKNOWN");
        };
    }

    public static String requestTypeClass(String requestType) {
        String type = upper(requestType);

        return switch (type) {
            case "BUY" -> "buy";
            case "SELL" -> "sell";
            case "MODIFY" -> "modify";
            case "CANCEL" -> "cancel";
            default -> "unknown";
        };
    }

    public static String statusLabel(String status) {
        String s = upper(status);

        return switch (s) {
            case "REQUESTED" -> "요청";
            case "ACCEPTED" -> "접수";
            case "ORDER_ACCEPTED" -> "주문접수";
            case "FILLED" -> "체결";
            case "PARTIALLY_FILLED" -> "부분체결";
            case "CANCELED" -> "취소완료";
            case "CANCEL_ACCEPTED" -> "취소접수";
            case "MODIFY_ACCEPTED" -> "정정접수";
            case "MODIFIED" -> "정정됨";
            case "REJECTED" -> "거절";
            default -> blankTo(status, "UNKNOWN");
        };
    }

    public static String statusClass(String status) {
        String s = upper(status);

        return switch (s) {
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
        String e = upper(eventType);

        return switch (e) {
            case "ORDER_ACCEPTED", "SUMMARY_ONLY_ORDER_ACCEPTED" -> "주문접수";
            case "FILLED", "SUMMARY_ONLY_FILLED" -> "체결";
            case "CANCELED", "SUMMARY_ONLY_CANCELED" -> "취소완료";
            case "CANCEL_ACCEPTED", "SUMMARY_ONLY_CANCEL_ACCEPTED" -> "취소접수";
            case "MODIFY_ACCEPTED", "SUMMARY_ONLY_MODIFY_ACCEPTED" -> "정정접수";
            default -> blankTo(eventType, "EVENT");
        };
    }

    public static String eventTypeClass(String eventType) {
        String e = upper(eventType);

        if (e.contains("FILLED")) {
            return "filled";
        }

        if (e.contains("CANCEL_ACCEPTED")) {
            return "cancel-accepted";
        }

        if (e.contains("CANCELED")) {
            return "canceled";
        }

        if (e.contains("MODIFY")) {
            return "modify";
        }

        if (e.contains("ORDER_ACCEPTED")) {
            return "accepted";
        }

        return "unknown";
    }

    public static String sideLabel(String side) {
        String s = upper(side);

        return switch (s) {
            case "BUY" -> "매수";
            case "SELL" -> "매도";
            case "UNKNOWN" -> "미확인";
            default -> blankTo(side, "-");
        };
    }

    public static String sideClass(String side) {
        String s = upper(side);

        return switch (s) {
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