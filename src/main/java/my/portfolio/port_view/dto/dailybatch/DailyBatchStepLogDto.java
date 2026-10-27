package my.portfolio.port_view.dto.dailybatch;

import java.time.OffsetDateTime;

/**
 * DTO for displaying strategy_daily_batch_step_log in the view.
 *
 * Represents the result of one step within a Daily Pipeline execution.
 */
public record DailyBatchStepLogDto(
        Long id,
        Long batchRunId,

        Integer stepOrder,
        String stepCode,
        String stepName,
        String stepStatus,

        String workDir,
        String commandText,

        Integer exitCode,

        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        Long durationMs,

        String stdoutTail,
        String stderrTail,
        String errorMessage,

        String resultPayload,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public boolean isRunning() {
        return "RUNNING".equalsIgnoreCase(stepStatus);
    }

    public boolean isSuccess() {
        return "SUCCESS".equalsIgnoreCase(stepStatus);
    }

    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(stepStatus);
    }

    public boolean isSkipped() {
        return "SKIPPED".equalsIgnoreCase(stepStatus);
    }

    public boolean isNoTarget() {
        return "NO_TARGET".equalsIgnoreCase(stepStatus);
    }

    public boolean hasLog() {
        return hasText(stdoutTail) || hasText(stderrTail) || hasText(errorMessage);
    }

    public boolean hasStdout() {
        return hasText(stdoutTail);
    }

    public boolean hasStderr() {
        return hasText(stderrTail);
    }

    public boolean hasErrorMessage() {
        return hasText(errorMessage);
    }

    public String displayStepNo() {
        return stepOrder == null ? "-" : String.valueOf(stepOrder);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}