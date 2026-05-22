package my.portfolio.port_view.controller;

import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.entity.Holdings;
import my.portfolio.port_view.service.HoldingsService;
import my.portfolio.port_view.util.AccountNoResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HoldingsController {

    private final HoldingsService holdingsService;
    private final AccountNoResolver accountNoResolver;

    public HoldingsController(
            HoldingsService holdingsService,
            AccountNoResolver accountNoResolver
    ) {
        this.holdingsService = holdingsService;
        this.accountNoResolver = accountNoResolver;
    }

    @GetMapping("/holdings")
    public String showHoldings(
            @RequestParam(required = false) String accountNo,
            Model model
    ) {
        String resolvedAccountNo = resolveAndAddAccountNo(accountNo, model);

        List<Holdings> list = holdingsService.listByAccount(resolvedAccountNo);

        model.addAttribute("holdingsList", list);

        return ViewNames.HOLDINGS;
    }

    private String resolveAndAddAccountNo(String accountNo, Model model) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);
        model.addAttribute("accountNo", resolvedAccountNo);
        return resolvedAccountNo;
    }
}
