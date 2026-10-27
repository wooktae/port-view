package my.portfolio.port_view.dto.dailybatch;

import java.util.List;

/**
 * Complete model for the Daily Batch view.
 *
 * Displays the latest execution, step logs, recent execution history,
 * and Block Watch Candidates together on the /daily-batch view.
 */
public record DailyBatchPageDto(
        DailyBatchRunDto selectedRun,
        List<DailyBatchStepLogDto> steps,
        List<DailyBatchRunDto> recentRuns,
        List<BlockWatchCandidateDto> blockWatchCandidates,
        boolean hasRunningBatch,
        Long runningBatchRunId
) {

    public boolean isEmpty() {
        return selectedRun == null;
    }

    public boolean hasSelectedRun() {
        return selectedRun != null;
    }

    public boolean hasSteps() {
        return steps != null && !steps.isEmpty();
    }

    public boolean hasRecentRuns() {
        return recentRuns != null && !recentRuns.isEmpty();
    }

    public boolean hasBlockWatchCandidates() {
        return blockWatchCandidates != null && !blockWatchCandidates.isEmpty();
    }

    public boolean canRunNewBatch() {
        return !hasRunningBatch;
    }

    public int stepCount() {
        return steps == null ? 0 : steps.size();
    }

    public int recentRunCount() {
        return recentRuns == null ? 0 : recentRuns.size();
    }

    public int blockWatchCandidateCount() {
        return blockWatchCandidates == null ? 0 : blockWatchCandidates.size();
    }
}