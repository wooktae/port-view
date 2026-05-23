package my.portfolio.port_view.controller;

import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.dto.order.OrderDetailPageDTO;
import my.portfolio.port_view.dto.order.OrderPageDTO;
import my.portfolio.port_view.service.OrderService;
import my.portfolio.port_view.util.AccountNoResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final AccountNoResolver accountNoResolver;

    public OrderController(
            OrderService orderService,
            AccountNoResolver accountNoResolver
    ) {
        this.orderService = orderService;
        this.accountNoResolver = accountNoResolver;
    }

    @GetMapping("/orders")
    public String showOrders(
            @RequestParam(required = false) String accountNo,
            Model model
    ) {
        String resolvedAccountNo = resolveAndAddAccountNo(accountNo, model);

        OrderPageDTO page = orderService.getOrderPage(resolvedAccountNo);

        model.addAttribute("page", page);

        return ViewNames.ORDERS;
    }

    @GetMapping("/orders/{id}")
    public String showOrderDetail(
            @PathVariable("id") Long id,
            @RequestParam(required = false) String accountNo,
            Model model
    ) {
        String resolvedAccountNo = resolveAndAddAccountNo(accountNo, model);

        OrderDetailPageDTO page = orderService.getOrderDetail(id, resolvedAccountNo);

        model.addAttribute("page", page);

        return ViewNames.ORDER_DETAIL;
    }

    private String resolveAndAddAccountNo(String accountNo, Model model) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);
        model.addAttribute("accountNo", resolvedAccountNo);
        return resolvedAccountNo;
    }
}
