package my.portfolio.port_view.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
    public String latest(
            @RequestParam(name = "version", required = false) String version,
            Model model
    ) {
        List<String> versions = reportService.getStrategyConfigVersions();

        String selectedVersion = version;
        if (selectedVersion == null || selectedVersion.isBlank()) {
            selectedVersion = versions.isEmpty() ? null : versions.get(0);
        } else {
            selectedVersion = selectedVersion.trim();
        }

        ReportStrategyPageDto page = selectedVersion == null
                ? reportService.getLatestReport()
                : reportService.getLatestReportByStrategyConfigVersion(selectedVersion);

        model.addAttribute("page", page);
        model.addAttribute("versions", versions);
        model.addAttribute("selectedVersion", selectedVersion);
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