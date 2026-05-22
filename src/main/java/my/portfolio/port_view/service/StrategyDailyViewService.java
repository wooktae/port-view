package my.portfolio.port_view.service;

import lombok.RequiredArgsConstructor;
import my.portfolio.port_view.dto.DailyPositionDecisionDto;
import my.portfolio.port_view.dto.DailyRunDto;
import my.portfolio.port_view.dto.DailySignalDto;
import my.portfolio.port_view.repository.StrategyDailyViewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StrategyDailyViewService {

    private final StrategyDailyViewRepository repository;

    public DailyRunDto getLatestDailyRun() {
        return repository.findLatestDailyRun()
                .orElseThrow(() -> new IllegalArgumentException("최신 Daily Run을 찾을 수 없음"));
    }

    public DailyRunDto getDailyRun(Long dailyRunId) {
        return repository.findDailyRunById(dailyRunId)
                .orElseThrow(() -> new IllegalArgumentException("Daily Run을 찾을 수 없음. dailyRunId=" + dailyRunId));
    }

    public List<DailyRunDto> getRecentDailyRuns() {
        return repository.findRecentDailyRuns();
    }

    public List<DailySignalDto> getDailySignals(Long dailyRunId) {
        return repository.findDailySignals(dailyRunId);
    }

    public List<DailyPositionDecisionDto> getDailyPositionDecisions(Long dailyRunId) {
        return repository.findDailyPositionDecisions(dailyRunId);
    }

    public Map<String, Object> getDailySummary(Long dailyRunId) {
        return repository.findDailySummary(dailyRunId);
    }

    public DailyPage getLatestDailyPage() {
        DailyRunDto run = getLatestDailyRun();
        return getDailyPage(run.id());
    }

    public DailyPage getDailyPage(Long dailyRunId) {
        DailyRunDto run = getDailyRun(dailyRunId);

        List<DailyRunDto> recentRuns = getRecentDailyRuns();
        List<DailySignalDto> signals = getDailySignals(dailyRunId);
        List<DailyPositionDecisionDto> positionDecisions = getDailyPositionDecisions(dailyRunId);
        Map<String, Object> summary = getDailySummary(dailyRunId);

        return new DailyPage(
                run,
                recentRuns,
                signals,
                positionDecisions,
                summary
        );
    }

    public record DailyPage(
            DailyRunDto run,
            List<DailyRunDto> recentRuns,
            List<DailySignalDto> signals,
            List<DailyPositionDecisionDto> positionDecisions,
            Map<String, Object> summary
    ) {
        public int signalCount() {
            return signals == null ? 0 : signals.size();
        }

        public int positionDecisionCount() {
            return positionDecisions == null ? 0 : positionDecisions.size();
        }

        public Object summaryValue(String key) {
            if (summary == null || key == null) {
                return null;
            }
            return summary.get(key);
        }
    }
}