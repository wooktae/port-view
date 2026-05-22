package my.portfolio.port_view.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyBatchAsyncService {

    private final DailyBatchService dailyBatchService;

    @Async("dailyBatchTaskExecutor")
    public void executeDailyPipelineAsync(Long batchRunId) {
        try {
            log.info("[DailyBatchAsync] Daily Pipeline async start. batchRunId={}", batchRunId);

            dailyBatchService.executeDailyPipeline(batchRunId);

            log.info("[DailyBatchAsync] Daily Pipeline async finished. batchRunId={}", batchRunId);
        } catch (Exception e) {
            log.error("[DailyBatchAsync] Daily Pipeline async failed. batchRunId={}", batchRunId, e);

            dailyBatchService.markRunFailedByAsyncException(batchRunId, e);
        }
    }
}