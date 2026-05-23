package my.portfolio.port_view.dto.dailybatch;

import java.util.List;

/**
 * Daily Batch 화면 전체 모델.
 *
 * /daily-batch 화면에서 최신 실행, 단계별 로그, 최근 실행 히스토리,
 * Block Watch 후보를 한 번에 표시한다.
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