package my.portfolio.port_view.controller;

import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.entity.TradeOrders;
import my.portfolio.port_view.service.TradeOrdersService;
import my.portfolio.port_view.util.AccountNoResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TradeOrdersController {

    private final TradeOrdersService tradeOrdersService;
    private final AccountNoResolver accountNoResolver;

    public TradeOrdersController(
            TradeOrdersService tradeOrdersService,
            AccountNoResolver accountNoResolver
    ) {
        this.tradeOrdersService = tradeOrdersService;
        this.accountNoResolver = accountNoResolver;
    }

    @GetMapping("/trade-orders")
    public String showTradeOrders(
            @RequestParam(required = false) String accountNo,
            Model model
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        List<TradeOrders> list = tradeOrdersService.listByAccount(resolvedAccountNo);

        model.addAttribute("accountNo", resolvedAccountNo);
        model.addAttribute("tradeOrdersList", list);

        return ViewNames.TRADE_ORDERS;
    }
}