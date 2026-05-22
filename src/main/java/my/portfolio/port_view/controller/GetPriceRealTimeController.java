package my.portfolio.port_view.controller;

import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.dto.GetPriceRealTimeDTO;
import my.portfolio.port_view.service.GetPriceRealTimeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GetPriceRealTimeController {

    private final GetPriceRealTimeService getPriceRealTimeService;

    public GetPriceRealTimeController(GetPriceRealTimeService getPriceRealTimeService) {
        this.getPriceRealTimeService = getPriceRealTimeService;
    }

    @GetMapping("/get-price-realtime")
    public String showRealTimePrice(
            @RequestParam(required = false) String code,
            Model model
    ) {
        String resolvedCode = code == null ? "" : code.trim();

        model.addAttribute("code", resolvedCode);

        if (!resolvedCode.isEmpty()) {
            GetPriceRealTimeDTO priceDto = getPriceRealTimeService.getRealtimePrice(resolvedCode);
            model.addAttribute("quote", priceDto);
        }

        return ViewNames.GET_PRICE_REALTIME;
    }
}