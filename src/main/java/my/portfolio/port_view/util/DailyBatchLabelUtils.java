package my.portfolio.port_view.util;

/**
 * Daily Batch 화면 표시용 Label Utility.
 *
 * 목적:
 * - DB status/code 값을 화면용 한글 라벨로 변환
 * - Thymeleaf에서 영어 status가 그대로 노출되는 문제 방지
 * - badge/pill CSS class 결정
 */
public final class DailyBatchLabelUtils {

    private DailyBatchLabelUtils() {
        // utility class
    }

    // =====================================================
    // Run Status
    // =====================================================

    public static String runStatusLabel(String status) {
        String normalized = normalize(status);

        return switch (normalized) {
            case "CREATED" -> "생성됨";
            case "RUNNING" -> "실행 중";
            case "SUCCESS" -> "성공";
            case "FAILED" -> "실패";
            case "CANCELLED" -> "취소됨";
            default -> "미확인";
        };
    }

    public static String runStatusClass(String status) {
        String normalized = normalize(status);

        return switch (normalized) {
            case "CREATED" -> "created";
            case "RUNNING" -> "running";
            case "SUCCESS" -> "success";
            case "FAILED" -> "failed";
            case "CANCELLED" -> "cancelled";
            default -> "unknown";
        };
    }

    // =====================================================
    // Step Status
    // =====================================================

    public static String stepStatusLabel(String status) {
        String normalized = normalize(status);

        return switch (normalized) {
            case "PENDING" -> "대기";
            case "RUNNING" -> "실행 중";
            case "SUCCESS" -> "성공";
            case "FAILED" -> "실패";
            case "SKIPPED" -> "건너뜀";
            case "NO_TARGET" -> "대상 없음";
            default -> "미확인";
        };
    }

    public static String stepStatusClass(String status) {
        String normalized = normalize(status);

        return switch (normalized) {
            case "PENDING" -> "pending";
            case "RUNNING" -> "running";
            case "SUCCESS" -> "success";
            case "FAILED" -> "failed";
            case "SKIPPED" -> "skipped";
            case "NO_TARGET" -> "no-target";
            default -> "unknown";
        };
    }

    // =====================================================
    // Run Type
    // =====================================================

    public static String runTypeLabel(String runType) {
        String normalized = normalize(runType);

        return switch (normalized) {
            case "MANUAL" -> "수동 실행";
            case "SCHEDULED" -> "자동 스케줄";
            case "RETRY" -> "재실행";
            default -> "미확인";
        };
    }

    // =====================================================
    // Step Code
    // =====================================================

    public static String stepCodeLabel(String stepCode) {
        String normalized = normalize(stepCode);

        return switch (normalized) {
            case "CONNECTOR_BALANCE", "BALANCE_BEFORE" -> "잔고/보유종목 확인";
            case "INTEREST_CRAWLER" -> "Interest 수집";
            case "PREPROCESSOR" -> "Preprocessor 전처리";
            case "BACKTEST_RESEARCH" -> "백테스트 분석";
            case "BACKTEST_REPORT" -> "백테스트 리포트";
            case "DAILY_BUY_SIGNAL" -> "Daily 매수 신호";
            case "DAILY_POSITION_SIGNAL" -> "Daily 포지션 신호";
            case "DAILY_BUY_EXECUTION" -> "Daily 매수 연결";
            case "DAILY_SELL_EXECUTION" -> "Daily 매도 연결";
            case "DAILY_AUTO_BUY" -> "자동 매수 실행";
            case "CONNECTOR_ORDER_CHECK" -> "체결 조회";
            case "SYNC_BUY_FILL" -> "매수 체결 동기화";
            case "SYNC_BUY_POSITION" -> "매수 포지션 동기화";
            case "BALANCE_REFRESH", "BALANCE_AFTER" -> "잔고/보유종목 최신화";
            case "DAILY_AUTO_SELL" -> "자동 매도 실행";
            case "SYNC_SELL_FILL" -> "매도 체결/포지션 동기화";
            case "MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE" -> "Strategy 주문 실행";
            default -> fallbackLabel(stepCode);
        };
    }

    public static String stepCodeShortLabel(String stepCode) {
        String normalized = normalize(stepCode);

        return switch (normalized) {
            case "CONNECTOR_BALANCE", "BALANCE_BEFORE" -> "잔고확인";
            case "INTEREST_CRAWLER" -> "수집";
            case "PREPROCESSOR" -> "전처리";
            case "BACKTEST_RESEARCH" -> "분석";
            case "BACKTEST_REPORT" -> "리포트";
            case "DAILY_BUY_SIGNAL" -> "매수신호";
            case "DAILY_POSITION_SIGNAL" -> "포지션신호";
            case "DAILY_BUY_EXECUTION" -> "매수연결";
            case "DAILY_SELL_EXECUTION" -> "매도연결";
            case "DAILY_AUTO_BUY" -> "자동매수";
            case "CONNECTOR_ORDER_CHECK" -> "체결조회";
            case "SYNC_BUY_FILL" -> "체결동기화";
            case "SYNC_BUY_POSITION" -> "포지션동기화";
            case "BALANCE_REFRESH", "BALANCE_AFTER" -> "잔고최신화";
            case "DAILY_AUTO_SELL" -> "자동매도";
            case "SYNC_SELL_FILL" -> "매도동기화";
            case "MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE" -> "Strategy 주문 실행";
            default -> fallbackLabel(stepCode);
        };
    }

    // =====================================================
    // Exit Code / Duration
    // =====================================================

    public static String exitCodeLabel(Integer exitCode) {
        if (exitCode == null) {
            return "-";
        }

        if (exitCode == 0) {
            return "정상 종료";
        }

        return "오류 종료(" + exitCode + ")";
    }

    public static String durationLabel(Long durationMs) {
        if (durationMs == null || durationMs < 0) {
            return "-";
        }

        if (durationMs < 1000) {
            return durationMs + "ms";
        }

        long totalSeconds = durationMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (minutes <= 0) {
            return seconds + "초";
        }

        return minutes + "분 " + seconds + "초";
    }

    // =====================================================
    // Helpers
    // =====================================================

    private static String normalize(String value) {
        return ViewTextUtils.upper(value);
    }

    private static String fallbackLabel(String value) {
        if (ViewTextUtils.isBlank(value)) {
            return "미확인";
        }

        return value
                .trim()
                .replace("_", " ")
                .toLowerCase();
    }
}
