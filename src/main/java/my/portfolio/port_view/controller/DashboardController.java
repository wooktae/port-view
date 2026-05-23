package my.portfolio.port_view.controller;

import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.dto.dashboard.DashboardViewDTO;
import my.portfolio.port_view.dto.strategy.ReportBacktestSummaryDto;
import my.portfolio.port_view.dto.strategy.StrategyExecutionPlanDto;
import my.portfolio.port_view.service.DashboardService;
import my.portfolio.port_view.service.ReportService;
import my.portfolio.port_view.service.StrategyExecutionViewService;
import my.portfolio.port_view.util.AccountNoResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import my.portfolio.port_view.service.ConnectorSnapshotRefreshService;

/**
 * Portfolio 메인 대시보드 Controller
 *
 * - Connector 테이블 기반 계좌/잔고/보유종목 표시
 * - Strategy Position State 요약 표시
 * - 최신 Backtest Report 요약 표시
 * - 최신 Strategy Execution Plan 요약 표시
 */
@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final StrategyExecutionViewService strategyExecutionViewService;
    private final AccountNoResolver accountNoResolver;
    private final ReportService reportService;
    private final ConnectorSnapshotRefreshService connectorSnapshotRefreshService;

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

        connectorSnapshotRefreshService.refreshIfStale(resolvedAccountNo);

        DashboardViewDTO dashboard = dashboardService.getDashboard(resolvedAccountNo);
        ReportBacktestSummaryDto latestBacktestSummary = reportService.getLatestBacktestSummaryOrNull();
        StrategyExecutionPlanDto latestExecutionPlan = strategyExecutionViewService.getLatestPlanOrNull();

        addAccountNo(model, resolvedAccountNo);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("strategyPositionSummary", strategyExecutionViewService.getCurrentPositionSummary());
        model.addAttribute("latestBacktestSummary", latestBacktestSummary);
        model.addAttribute("latestExecutionPlan", latestExecutionPlan);

        return ViewNames.DASHBOARD;
    }

    private void addAccountNo(Model model, String resolvedAccountNo) {
        model.addAttribute("accountNo", resolvedAccountNo);
    }
}
