package my.portfolio.port_view.util;

public final class ConnectorLabelUtils {

    private ConnectorLabelUtils() {
    }

    public static String orderMethodLabel(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return "미확인";
        }

        return switch (normalized) {
            case "MARKET" -> "시장가";
            case "LIMIT" -> "지정가";
            case "BEST" -> "최유리";
            case "MOC" -> "장마감 시장가";
            case "LOC" -> "장마감 지정가";
            default -> String.valueOf(value);
        };
    }

    public static String requestTypeLabel(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return "미확인";
        }

        return switch (normalized) {
            case "BUY" -> "매수";
            case "SELL" -> "매도";
            case "MODIFY" -> "정정";
            case "CANCEL" -> "취소";
            default -> String.valueOf(value);
        };
    }

    public static String sideLabel(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return "미확인";
        }

        return switch (normalized) {
            case "BUY", "매수" -> "매수";
            case "SELL", "매도" -> "매도";
            case "UNKNOWN" -> "미확인";
            default -> String.valueOf(value);
        };
    }

    public static String requestStatusLabel(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return "미확인";
        }

        return switch (normalized) {
            case "PENDING" -> "대기";
            case "READY" -> "준비";
            case "SUBMITTED" -> "전송 완료";
            case "SENT" -> "전송 완료";
            case "ACCEPTED" -> "접수 완료";
            case "ORDER_ACCEPTED" -> "주문 접수";
            case "PARTIAL_FILLED" -> "부분 체결";
            case "FILLED" -> "체결 완료";
            case "REJECTED" -> "거절";
            case "CANCELED", "CANCELLED" -> "취소 완료";
            case "CANCEL_ACCEPTED" -> "취소 접수";
            case "MODIFIED" -> "정정 완료";
            case "MODIFY_ACCEPTED" -> "정정 접수";
            default -> String.valueOf(value);
        };
    }

    public static String eventTypeLabel(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return "미확인";
        }

        return switch (normalized) {
            case "ORDER_ACCEPTED" -> "주문 접수";
            case "SUMMARY_ONLY_ORDER_ACCEPTED" -> "요약 기준 주문 접수";
            case "FILLED" -> "체결 완료";
            case "SUMMARY_ONLY_FILLED" -> "요약 기준 체결 완료";
            case "PARTIAL_FILLED" -> "부분 체결";
            case "SUMMARY_ONLY_PARTIAL_FILLED" -> "요약 기준 부분 체결";
            case "CANCELED", "CANCELLED" -> "취소 완료";
            case "CANCEL_ACCEPTED" -> "취소 접수";
            case "SUMMARY_ONLY_CANCEL_ACCEPTED" -> "요약 기준 취소 접수";
            case "MODIFIED" -> "정정 완료";
            case "MODIFY_ACCEPTED" -> "정정 접수";
            default -> String.valueOf(value);
        };
    }

    public static String executionStatusLabel(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return "미확인";
        }

        return switch (normalized) {
            case "CANDIDATE" -> "후보";
            case "READY" -> "주문 대기";
            case "SUBMITTED" -> "전송 완료";
            case "SENT" -> "전송 완료";
            case "ACCEPTED" -> "접수 완료";
            case "FILLED" -> "체결 완료";
            case "BLOCKED" -> "차단";
            case "SKIPPED" -> "제외";
            case "FAILED" -> "실패";
            default -> String.valueOf(value);
        };
    }

    public static String positionStatusLabel(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return "미확인";
        }

        return switch (normalized) {
            case "OPEN" -> "보유 중";
            case "SELL_READY" -> "매도 준비";
            case "SELL_ORDERED" -> "매도 주문 중";
            case "CLOSED" -> "청산 완료";
            default -> String.valueOf(value);
        };
    }

    public static String sourceTypeLabel(Object value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return "미확인";
        }

        return switch (normalized) {
            case "TRADE_LOG_BUY" -> "전략 매수 후보";
            case "TRADE_LOG_SELL" -> "전략 매도 후보";
            case "POSITION_SELL" -> "수동 포지션 매도";
            case "DAILY_SIGNAL_BUY" -> "Daily 매수 신호";
            case "DAILY_POSITION_SELL" -> "Daily 포지션 매도";
            case "INTRADAY_STOP_SELL" -> "장중 손절 매도";
            case "MANUAL_TEST" -> "수동 테스트";
            case "STRATEGY_SIGNAL" -> "전략 신호";
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
