package my.portfolio.port_view.controller;

import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.dto.dashboard.DashboardViewDTO;
import my.portfolio.port_view.dto.strategy.ReportBacktestSummaryDto;
import my.portfolio.port_view.dto.strategy.StrategyExecutionPlanDto;
import my.portfolio.port_view.service.DashboardService;
import my.portfolio.port_view.service.ReportService;
import my.portfolio.port_view.service.StrategyExecutionViewService;
import my.portfolio.port_view.service.ConnectorSnapshotRefreshService;
import my.portfolio.port_view.util.AccountNoResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for the main Portfolio Dashboard.
 *
 * AWS Paper read-only principles:
 * - Do not run a Connector/KIS Snapshot Refresh when entering the view.
 * - Dashboard only queries previously stored AWS Paper RDS Snapshot, Strategy, and Report data.
 */
@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final StrategyExecutionViewService strategyExecutionViewService;
    private final AccountNoResolver accountNoResolver;
    private final ReportService reportService;
    private final ConnectorSnapshotRefreshService connectorSnapshotRefreshService;

    @Value("${portfolio.dashboard.snapshot-refresh-enabled:false}")
    private boolean snapshotRefreshEnabled;

    public DashboardController(
            DashboardService dashboardService,
            StrategyExecutionViewService strategyExecutionViewService,
            AccountNoResolver accountNoResolver,
            ReportService reportService,
            ConnectorSnapshotRefreshService connectorSnapshotRefreshService
    ) {
        this.dashboardService = dashboardService;
        this.strategyExecutionViewService = strategyExecutionViewService;
        this.accountNoResolver = accountNoResolver;
        this.reportService = reportService;
        this.connectorSnapshotRefreshService = connectorSnapshotRefreshService;
    }

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(
            @RequestParam(required = false) String accountNo,
            Model model
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);
        if (snapshotRefreshEnabled) {
            connectorSnapshotRefreshService.refreshIfStale(resolvedAccountNo);
        }

        DashboardViewDTO dashboard = dashboardService.getDashboard(resolvedAccountNo);
        ReportBacktestSummaryDto latestBacktestSummary = reportService.getLatestBacktestSummaryOrNull();
        StrategyExecutionPlanDto latestExecutionPlan = strategyExecutionViewService.getLatestPlanOrNull();

        addAccountNo(model, resolvedAccountNo);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("strategyPositionSummary", strategyExecutionViewService.getCurrentPositionSummary());
        model.addAttribute("latestBacktestSummary", latestBacktestSummary);
        model.addAttribute("latestExecutionPlan", latestExecutionPlan);

        model.addAttribute("dashboardReadOnly", !snapshotRefreshEnabled);
        model.addAttribute("dashboardSnapshotRefreshEnabled", snapshotRefreshEnabled);

        return ViewNames.DASHBOARD;
    }

    private void addAccountNo(Model model, String resolvedAccountNo) {
        model.addAttribute("accountNo", resolvedAccountNo);
    }
}