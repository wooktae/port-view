package my.portfolio.port_view.controller;

import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.entity.BalanceSummary;
import my.portfolio.port_view.service.BalanceService;
import my.portfolio.port_view.util.AccountNoResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import my.portfolio.port_view.service.ConnectorSnapshotRefreshService;

@Controller
public class BalanceController {

    private final BalanceService balanceService;
    private final AccountNoResolver accountNoResolver;
    private final ConnectorSnapshotRefreshService connectorSnapshotRefreshService;

    public BalanceController(
            BalanceService balanceService,
            AccountNoResolver accountNoResolver,
            ConnectorSnapshotRefreshService connectorSnapshotRefreshService
    ) {
        this.balanceService = balanceService;
        this.accountNoResolver = accountNoResolver;
        this.connectorSnapshotRefreshService = connectorSnapshotRefreshService;
    }

    @GetMapping("/balance-summary")
    public String showBalanceSummary(
            @RequestParam(required = false) String accountNo,
            Model model
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        connectorSnapshotRefreshService.refreshNow(resolvedAccountNo);
        
        List<BalanceSummary> list = balanceService.listByAccount(resolvedAccountNo);
        BigDecimal stockEvalAmount = balanceService.getLatestStockEvalAmount(resolvedAccountNo);
        int holdingCount = balanceService.getLatestHoldingCount(resolvedAccountNo);

        BigDecimal initialCapitalAmount = balanceService.getInitialCapitalAmount(resolvedAccountNo);
        BigDecimal cumulativeProfitAmount = balanceService.getCumulativeProfitAmount(resolvedAccountNo);
        BigDecimal cumulativeProfitRate = balanceService.getCumulativeProfitRate(resolvedAccountNo);

        addAccountNo(model, resolvedAccountNo);
        model.addAttribute("balanceSummaryList", list);
        model.addAttribute("stockEvalAmount", stockEvalAmount);
        model.addAttribute("holdingCount", holdingCount);

        model.addAttribute("initialCapitalAmount", initialCapitalAmount);
        model.addAttribute("cumulativeProfitAmount", cumulativeProfitAmount);
        model.addAttribute("cumulativeProfitRate", cumulativeProfitRate);

        return ViewNames.BALANCE_SUMMARY;
    }

    private void addAccountNo(Model model, String resolvedAccountNo) {
        model.addAttribute("accountNo", resolvedAccountNo);
    }
}
