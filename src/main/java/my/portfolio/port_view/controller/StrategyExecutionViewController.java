package my.portfolio.port_view.controller;

import lombok.RequiredArgsConstructor;
import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.service.StrategyExecutionSubmitService;
import my.portfolio.port_view.service.StrategyExecutionViewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class StrategyExecutionViewController {

    private final StrategyExecutionViewService service;
    private final StrategyExecutionSubmitService submitService;

    @GetMapping("/strategy/execution/plans")
    public String plans(Model model) {
        model.addAttribute("page", service.getPlanPage());
        model.addAttribute("positionSummary", service.getCurrentPositionSummary());
        return ViewNames.STRATEGY_PLANS;
    }

    @GetMapping("/strategy/execution/plans/{planId}")
    public String planDetail(@PathVariable Long planId, Model model) {
        
        model.addAttribute("plan", service.getPlan(planId));
        model.addAttribute("marketBlockReason", service.getMarketBlockReason(planId));
        model.addAttribute("orders", service.getOrders(planId));

        model.addAttribute("positionStates", service.getPositionStates(planId));
        model.addAttribute("connectorOrders", service.getConnectorOrders(planId));
        model.addAttribute("connectorEventsAndFills", service.getConnectorEventsAndFills(planId));

        return ViewNames.STRATEGY_DETAIL;
    }

    @PostMapping("/strategy/execution/orders/{orderId}/submit")
    public String submitOrder(
            @PathVariable Long orderId,
            @RequestParam Long planId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Long connectorOrderRequestId = submitService.submit(orderId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Connector 주문 전송 완료: #" + connectorOrderRequestId
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Connector 주문 전송 실패: " + e.getMessage()
            );
        }

        return "redirect:/strategy/execution/plans/" + planId;
    }
}