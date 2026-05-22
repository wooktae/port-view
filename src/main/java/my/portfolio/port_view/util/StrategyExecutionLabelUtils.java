package my.portfolio.port_view.util;

public final class StrategyExecutionLabelUtils {

    private StrategyExecutionLabelUtils() {
    }

    public static String planStatusLabel(String value) {
        return switch (upper(value)) {
            case "CREATED" -> "생성됨";
            case "READY" -> "실행 후보 있음";
            case "VALIDATED" -> "검증됨";
            case "BLOCKED" -> "차단";
            case "PARTIALLY_BLOCKED" -> "부분 차단";
            case "NO_CANDIDATE" -> "실행 후보 없음";
            case "FAILED" -> "실패";
            case "ERROR" -> "오류";
            default -> fallback(value);
        };
    }

    public static String executionStatusLabel(String value) {
        return switch (upper(value)) {
            case "CANDIDATE" -> "후보";
            case "READY" -> "주문 대기";
            case "SUBMITTED" -> "전송 완료";
            case "SENT" -> "전송됨";
            case "ACCEPTED" -> "접수 완료";
            case "FILLED" -> "체결 완료";
            case "PARTIAL_FILLED" -> "부분 체결";
            case "BLOCKED" -> "차단";
            case "SKIPPED" -> "건너뜀";
            case "FAILED" -> "실패";
            case "ERROR" -> "오류";
            case "CANCELED" -> "취소 완료";
            case "CANCEL_ACCEPTED" -> "취소 접수";
            default -> fallback(value);
        };
    }

    public static String positionStatusLabel(String value) {
        return switch (upper(value)) {
            case "OPEN" -> "보유 중";
            case "SELL_READY" -> "매도 준비";
            case "SELL_ORDERED" -> "매도 주문 중";
            case "CLOSED" -> "청산 완료";
            case "SKIPPED" -> "건너뜀";
            default -> fallback(value);
        };
    }

    public static String actionTypeLabel(String value) {
        return switch (upper(value)) {
            case "BUY" -> "매수";
            case "SELL" -> "매도";
            case "HOLD" -> "보유";
            case "SKIP" -> "건너뜀";
            default -> fallback(value);
        };
    }

    public static String signalTypeLabel(String value) {
        return switch (upper(value)) {
            case "BUY" -> "매수 신호";
            case "SELL" -> "매도 신호";
            case "HOLD" -> "보유 신호";
            case "POSITION_SELL" -> "포지션 매도";
            case "TRADE_LOG_BUY" -> "백테스트 매수";
            case "TRADE_LOG_SELL" -> "백테스트 매도";
            case "DAILY_SIGNAL_BUY" -> "오늘 매수 신호";
            case "MANUAL_TEST" -> "수동 테스트";
            case "DAILY_POSITION_SELL" -> "Daily 포지션 매도";
            case "INTRADAY_STOP_SELL" -> "장중 손절 매도";
            default -> fallback(value);
        };
    }

    public static String sourceTypeLabel(String value) {
        return switch (upper(value)) {
            case "TRADE_LOG_BUY" -> "백테스트 매수";
            case "TRADE_LOG_SELL" -> "백테스트 매도";
            case "POSITION_SELL" -> "수동 포지션 매도";
            case "DAILY_SIGNAL_BUY" -> "Daily 매수 신호";
            case "DAILY_POSITION_SELL" -> "Daily 포지션 매도";
            case "INTRADAY_STOP_SELL" -> "장중 손절 매도";
            case "MANUAL_TEST" -> "수동 테스트";
            case "STRATEGY_SIGNAL" -> "전략 신호";
            default -> fallback(value);
        };
    }

    public static String orderMethodLabel(String value) {
        return switch (upper(value)) {
            case "MARKET" -> "시장가";
            case "LIMIT" -> "지정가";
            default -> fallback(value);
        };
    }

    public static String marketSignalLabel(String value) {
        return switch (upper(value)) {
            case "AGGRESSIVE" -> "공격 운용";
            case "NEUTRAL" -> "선별 진입";
            case "DEFENSIVE" -> "방어 운용";
            case "BLOCK" -> "신규 매수 차단";
            case "UNKNOWN" -> "미확인";
            default -> fallback(value);
        };
    }

    public static String riskRegimeLabel(String value) {
        return switch (upper(value)) {
            case "RISK_ON" -> "위험 선호";
            case "RISK_OFF" -> "위험 회피";
            case "NEUTRAL" -> "중립";
            case "UNKNOWN" -> "미확인";
            default -> fallback(value);
        };
    }

    public static String requestTypeLabel(String value) {
        return switch (upper(value)) {
            case "BUY" -> "매수";
            case "SELL" -> "매도";
            case "MODIFY" -> "정정";
            case "CANCEL" -> "취소";
            default -> fallback(value);
        };
    }

    public static String requestStatusLabel(String value) {
        return switch (upper(value)) {
            case "PENDING" -> "대기";
            case "SUBMITTED" -> "전송 완료";
            case "ACCEPTED" -> "접수 완료";
            case "FILLED" -> "체결 완료";
            case "PARTIAL_FILLED" -> "부분 체결";
            case "REJECTED" -> "거절";
            case "FAILED" -> "실패";
            case "CANCELED" -> "취소 완료";
            case "CANCEL_ACCEPTED" -> "취소 접수";
            default -> fallback(value);
        };
    }

    public static String eventTypeLabel(String value) {
        return switch (upper(value)) {
            case "ORDER_ACCEPTED" -> "주문 접수";
            case "FILLED" -> "체결 완료";
            case "PARTIAL_FILLED" -> "부분 체결";
            case "SUMMARY_ONLY_FILLED" -> "요약 기준 체결";
            case "SUMMARY_ONLY_ORDER_ACCEPTED" -> "요약 기준 주문 접수";
            case "CANCELED" -> "취소 완료";
            case "CANCEL_ACCEPTED" -> "취소 접수";
            case "SUMMARY_ONLY_CANCEL_ACCEPTED" -> "요약 기준 취소 접수";
            default -> fallback(value);
        };
    }

    public static String executionModeLabel(String value) {
        return switch (upper(value)) {
            case "DRY_RUN" -> "검증 전용";
            case "MANUAL_TEST" -> "수동 테스트";
            case "PAPER_STRATEGY" -> "모의 전략 실행";
            case "LIVE_STRATEGY" -> "실전 전략 실행";
            default -> fallback(value);
        };
    }

    public static String sellReasonLabel(String value) {
        return switch (upper(value)) {
            case "INTRADAY_ENTRY_HARD_STOP" -> "진입가 대비 장중 손절";
            case "INTRADAY_PREV_CLOSE_HARD_STOP" -> "전일종가 대비 장중 급락 손절";
            case "HOLD_MIN_HOLDING_DAYS" -> "최소 보유일 미충족";
            case "SELL_FILLED" -> "매도 체결 완료";
            case "POSITION_SELL" -> "포지션 매도";
            case "DAILY_POSITION_SELL" -> "Daily 포지션 매도";
            case "INTRADAY_STOP_SELL" -> "장중 손절 매도";
            case "MARKET_BLOCK_WEAK" -> "시장 약세 차단";
            case "QUALITY_DROP" -> "품질 점수 하락";
            case "HARD_STOP" -> "손절 기준 도달";
            case "PROFIT_PROTECT" -> "수익 보호";
            case "MAX_HOLDING_DAYS" -> "최대 보유일 도달";
            default -> fallback(value);
        };
    }

    public static String blockReasonLabel(String value) {
        return switch (upper(value)) {
            case "NO_SELLABLE_QTY" -> "매도 가능 수량 없음";
            case "LATEST_CONNECTOR_POSITION_SNAPSHOT_NOT_FOUND" -> "최신 보유 스냅샷 없음";
            case "LATEST_POSITION_QTY_ZERO" -> "최신 보유 수량 0";
            case "LATEST_SELLABLE_QTY_ZERO" -> "최신 매도 가능 수량 0";
            case "ORDER_QTY_GT_LATEST_SELLABLE_QTY" -> "주문 수량이 최신 매도 가능 수량보다 큼";
            case "STRATEGY_ORDER_QTY_ZERO" -> "전략 주문 수량 0";
            case "TEST_INTRADAY_STOP_SELL_CANDIDATE" -> "테스트 장중 손절 후보 정리";
            case "ALL EXECUTION ORDERS BLOCKED" -> "모든 주문 후보 차단";
            default -> fallback(value);
        };
    }

    public static String upper(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase();
    }

    public static String fallback(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }
}