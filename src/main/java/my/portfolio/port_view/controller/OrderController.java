package my.portfolio.port_view.controller;

import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.dto.order.OrderDetailPageDTO;
import my.portfolio.port_view.dto.order.OrderPageDTO;
import my.portfolio.port_view.service.OrderService;
import my.portfolio.port_view.util.AccountNoResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import my.portfolio.port_view.service.ConnectorOrderRefreshService;


/**
 * 주문 목록과 주문 상세 화면을 제공하는 Spring MVC Controller.
 * 화면 조회 전 Connector 주문 갱신 서비스를 호출해 진행 중 주문의 체결 상태를 보정한다.
 */
@Controller
public class OrderController {

    private final OrderService orderService;
    private final AccountNoResolver accountNoResolver;
    private final ConnectorOrderRefreshService connectorOrderRefreshService;

    public OrderController(
            OrderService orderService,
            AccountNoResolver accountNoResolver,
            ConnectorOrderRefreshService connectorOrderRefreshService
    ) {
        this.orderService = orderService;
        this.accountNoResolver = accountNoResolver;
        this.connectorOrderRefreshService = connectorOrderRefreshService;
    }

    @GetMapping("/orders")
    public String showOrders(
            @RequestParam(required = false) String accountNo,
            Model model
    ) {
        String resolvedAccountNo = resolveAndAddAccountNo(accountNo, model);

        connectorOrderRefreshService.refreshOpenOrders(resolvedAccountNo);

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

        connectorOrderRefreshService.refreshOpenOrders(resolvedAccountNo);

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
