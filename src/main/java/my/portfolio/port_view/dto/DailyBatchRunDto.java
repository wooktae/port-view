package my.portfolio.port_view.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * strategy_daily_batch_run 화면 표시용 DTO.
 *
 * Daily Pipeline 실행 1회 전체 상태를 표현한다.
 */
public record DailyBatchRunDto(
        Long id,

        LocalDate batchDate,
        String runType,
        String runStatus,

        String requestedBy,
        String environment,
        String accountNo,

        String currentStepCode,
        String currentStepName,

        Integer totalStepCount,
        Integer successStepCount,
        Integer failedStepCount,
        Integer skippedStepCount,
        Integer noTargetStepCount,

        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        Long durationMs,

        String errorStepCode,
        String errorMessage,

        String requestPayload,
        String resultPayload,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public boolean isRunning() {
        return "RUNNING".equalsIgnoreCase(runStatus);
    }

    public boolean isSuccess() {
        return "SUCCESS".equalsIgnoreCase(runStatus);
    }

    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(runStatus);
    }

    public boolean isFinished() {
        return isSuccess() || isFailed() || "CANCELLED".equalsIgnoreCase(runStatus);
    }

    public int safeTotalStepCount() {
        return totalStepCount == null ? 0 : totalStepCount;
    }

    public int safeSuccessStepCount() {
        return successStepCount == null ? 0 : successStepCount;
    }

    public int safeFailedStepCount() {
        return failedStepCount == null ? 0 : failedStepCount;
    }

    public int safeSkippedStepCount() {
        return skippedStepCount == null ? 0 : skippedStepCount;
    }

    public int safeNoTargetStepCount() {
        return noTargetStepCount == null ? 0 : noTargetStepCount;
    }

    public int completedStepCount() {
        return safeSuccessStepCount()
                + safeFailedStepCount()
                + safeSkippedStepCount()
                + safeNoTargetStepCount();
    }

    public int progressPercent() {
        int total = safeTotalStepCount();

        if (total <= 0) {
            return 0;
        }

        int percent = (int) Math.round((completedStepCount() * 100.0) / total);

        if (percent < 0) {
            return 0;
        }

        return Math.min(percent, 100);
    }

    public String displayTitle() {
        if (id == null) {
            return "Daily Pipeline";
        }

        return "Daily Pipeline #" + id;
    }
}