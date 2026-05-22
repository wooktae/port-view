package my.portfolio.port_view.controller;

import lombok.RequiredArgsConstructor;
import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.service.StrategyDailyViewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class StrategyDailyViewController {

    private final StrategyDailyViewService strategyDailyViewService;

    @GetMapping("/strategy/daily/latest")
    public String latestDaily(Model model) {
        StrategyDailyViewService.DailyPage page =
                strategyDailyViewService.getLatestDailyPage();

        addDailyModel(model, page);

        return ViewNames.STRATEGY_DAILY;
    }

    @GetMapping("/strategy/daily/{dailyRunId}")
    public String dailyDetail(
            @PathVariable Long dailyRunId,
            Model model
    ) {
        StrategyDailyViewService.DailyPage page =
                strategyDailyViewService.getDailyPage(dailyRunId);

        addDailyModel(model, page);

        return ViewNames.STRATEGY_DAILY;
    }

    private void addDailyModel(
            Model model,
            StrategyDailyViewService.DailyPage page
    ) {
        model.addAttribute("page", page);
        model.addAttribute("run", page.run());
        model.addAttribute("recentRuns", page.recentRuns());
        model.addAttribute("signals", page.signals());
        model.addAttribute("positionDecisions", page.positionDecisions());
        model.addAttribute("summary", page.summary());
    }
}
