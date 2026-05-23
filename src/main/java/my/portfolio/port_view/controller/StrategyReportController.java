package my.portfolio.port_view.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.dto.strategy.ReportStrategyPageDto;
import my.portfolio.port_view.service.ReportService;

@Controller
@RequestMapping("/strategy/reports")
public class StrategyReportController {

    private final ReportService reportService;

    public StrategyReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/latest")
    public String latest(Model model) {
        ReportStrategyPageDto page = reportService.getLatestReport();

        model.addAttribute("page", page);
        model.addAttribute("pageTitle", "백테스트 리포트");

        return ViewNames.STRATEGY_REPORT;
    }

    @GetMapping("/{runId}")
    public String detail(@PathVariable String runId, Model model) {
        ReportStrategyPageDto page = reportService.getReportByRunId(runId);

        model.addAttribute("page", page);
        model.addAttribute("pageTitle", "백테스트 리포트");

        return ViewNames.STRATEGY_REPORT;
    }
}