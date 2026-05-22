package my.portfolio.port_view.controller;

import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.dto.PositionDetailPageDTO;
import my.portfolio.port_view.dto.PositionPageDTO;
import my.portfolio.port_view.service.PositionService;
import my.portfolio.port_view.util.AccountNoResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import my.portfolio.port_view.service.ConnectorSnapshotRefreshService;

/**
 * Connector 기반 보유종목 화면 Controller
 *
 * URL:
 * - /positions
 * - /positions?accountNo=50187518
 * - /positions/{tickerCode}?accountNo=50187518
 */
@Controller
public class PositionController {

    private final PositionService positionService;
    private final AccountNoResolver accountNoResolver;

    private final ConnectorSnapshotRefreshService connectorSnapshotRefreshService;

    public PositionController(
            PositionService positionService,
            AccountNoResolver accountNoResolver,
            ConnectorSnapshotRefreshService connectorSnapshotRefreshService
    ) {
        this.positionService = positionService;
        this.accountNoResolver = accountNoResolver;
        this.connectorSnapshotRefreshService = connectorSnapshotRefreshService;
    }

    @GetMapping("/positions")
    public String showPositions(
            @RequestParam(required = false) String accountNo,
            Model model
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        connectorSnapshotRefreshService.refreshIfStale(resolvedAccountNo);

        PositionPageDTO page = positionService.getPositionPage(resolvedAccountNo);

        addAccountNo(model, resolvedAccountNo);
        model.addAttribute("page", page);

        return ViewNames.POSITIONS;
    }

    @GetMapping("/positions/{tickerCode}")
    public String showPositionDetail(
            @PathVariable String tickerCode,
            @RequestParam(required = false) String accountNo,
            Model model
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        connectorSnapshotRefreshService.refreshIfStale(resolvedAccountNo);
        
        PositionDetailPageDTO page =
                positionService.getPositionDetailPage(resolvedAccountNo, tickerCode);

        addAccountNo(model, resolvedAccountNo);
        model.addAttribute("tickerCode", tickerCode);
        model.addAttribute("page", page);

        return ViewNames.POSITION_DETAIL;
    }

    private void addAccountNo(Model model, String resolvedAccountNo) {
        model.addAttribute("accountNo", resolvedAccountNo);
    }
}
