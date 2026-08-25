package my.portfolio.port_view.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.dto.strategy.ReportStrategyPageDto;
import my.portfolio.port_view.service.OperatingResearchVersionProvider;
import my.portfolio.port_view.service.ReportService;
import my.portfolio.port_view.service.ResearchChampionPromotionService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/strategy/reports")
public class StrategyReportController {

    private final ReportService reportService;
    private final OperatingResearchVersionProvider operatingResearchVersionProvider;
    private final ResearchChampionPromotionService researchChampionPromotionService;

    public StrategyReportController(
            ReportService reportService,
            OperatingResearchVersionProvider operatingResearchVersionProvider,
            ResearchChampionPromotionService researchChampionPromotionService
    ) {
        this.reportService = reportService;
        this.operatingResearchVersionProvider = operatingResearchVersionProvider;
        this.researchChampionPromotionService = researchChampionPromotionService;
    }

    @GetMapping("/latest")
    public String latest(
            @RequestParam(name = "version", required = false) String version,
            Model model
    ) {
        List<String> versions = reportService.getStrategyConfigVersions();
        String operatingVersion = operatingResearchVersionProvider.getOperatingVersion();

        String selectedVersion = version;
        if (selectedVersion == null || selectedVersion.isBlank()) {
            selectedVersion = operatingVersion != null && versions.contains(operatingVersion)
                    ? operatingVersion
                    : (versions.isEmpty() ? null : versions.get(0));
        } else {
            selectedVersion = selectedVersion.trim();
        }

        ReportStrategyPageDto page = selectedVersion == null
                ? reportService.getLatestReport()
                : reportService.getLatestReportByStrategyConfigVersion(selectedVersion);

        model.addAttribute("page", page);
        model.addAttribute("versions", versions);
        model.addAttribute("selectedVersion", selectedVersion);
        model.addAttribute("operatingVersion", operatingVersion);
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

    @PostMapping("/promote")
    public String promote(
            @RequestParam("version") String version,
            RedirectAttributes redirectAttributes
    ) {
        ResearchChampionPromotionService.PromotionResult result =
                researchChampionPromotionService.promoteChampion(version);

        System.out.println(
                "RESEARCH_CHAMPION_PREVIOUS="
                        + result.previousVersion()
        );

        System.out.println(
                "RESEARCH_CHAMPION_OPERATING="
                        + result.operatingVersion()
        );

        System.out.println(
                "RESEARCH_CHAMPION_OPERATING_REVISION="
                        + result.operatingRevision()
        );

        System.out.println(
                "RESEARCH_CHAMPION_PROMOTION=SUCCESS"
        );

        redirectAttributes.addAttribute(
                "version",
                result.operatingVersion()
        );

        return "redirect:/strategy/reports/latest";
    }
}
