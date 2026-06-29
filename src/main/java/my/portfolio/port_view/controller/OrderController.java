package my.portfolio.port_view.controller;

import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.dto.order.OrderDetailPageDTO;
import my.portfolio.port_view.dto.order.OrderPageDTO;
import my.portfolio.port_view.service.ConnectorOrderRefreshService;
import my.portfolio.port_view.service.OrderService;
import my.portfolio.port_view.util.AccountNoResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 주문 목록과 주문 상세 화면을 제공하는 Spring MVC Controller.
 *
 * AWS Paper local verification에서는 조회-only가 기본이다.
 * connector order refresh는 portfolio.order-refresh.enabled=true일 때만 실행한다.
 */
@Controller
public class OrderController {

    private final OrderService orderService;
    private final AccountNoResolver accountNoResolver;
    private final ConnectorOrderRefreshService connectorOrderRefreshService;
    private final boolean orderRefreshEnabled;

    public OrderController(
            OrderService orderService,
            AccountNoResolver accountNoResolver,
            ConnectorOrderRefreshService connectorOrderRefreshService,
            @Value("${portfolio.order-refresh.enabled:false}") boolean orderRefreshEnabled
    ) {
        this.orderService = orderService;
        this.accountNoResolver = accountNoResolver;
        this.connectorOrderRefreshService = connectorOrderRefreshService;
        this.orderRefreshEnabled = orderRefreshEnabled;
    }

    @GetMapping("/orders")
    public String showOrders(
            @RequestParam(required = false) String accountNo,
            Model model
    ) {
        String resolvedAccountNo = resolveAndAddAccountNo(accountNo, model);

        if (orderRefreshEnabled) {
            connectorOrderRefreshService.refreshOpenOrders(resolvedAccountNo);
        }

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

        if (orderRefreshEnabled) {
            connectorOrderRefreshService.refreshOpenOrders(resolvedAccountNo);
        }

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