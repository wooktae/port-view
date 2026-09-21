package my.portfolio.port_view.controller;

import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.dto.position.PositionDetailPageDTO;
import my.portfolio.port_view.dto.position.PositionPageDTO;
import my.portfolio.port_view.service.ConnectorSnapshotRefreshService;
import my.portfolio.port_view.service.PositionService;
import my.portfolio.port_view.util.AccountNoResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for Connector-backed Position views.
 *
 * URL:
 * - /positions
 * - /positions?accountNo=...
 * - /positions/{tickerCode}?accountNo=...
 */
@Controller
public class PositionController {

    private final PositionService positionService;
    private final AccountNoResolver accountNoResolver;
    private final ConnectorSnapshotRefreshService connectorSnapshotRefreshService;
    private final boolean snapshotRefreshEnabled;

    public PositionController(
            PositionService positionService,
            AccountNoResolver accountNoResolver,
            ConnectorSnapshotRefreshService connectorSnapshotRefreshService,
            @Value("${portfolio.snapshot-refresh.enabled:false}") boolean snapshotRefreshEnabled
    ) {
        this.positionService = positionService;
        this.accountNoResolver = accountNoResolver;
        this.connectorSnapshotRefreshService = connectorSnapshotRefreshService;
        this.snapshotRefreshEnabled = snapshotRefreshEnabled;
    }

    @GetMapping("/positions")
    public String showPositions(
            @RequestParam(required = false) String accountNo,
            Model model
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        if (snapshotRefreshEnabled) {
            connectorSnapshotRefreshService.refreshNow(resolvedAccountNo);
        }

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

        if (snapshotRefreshEnabled) {
            connectorSnapshotRefreshService.refreshIfStale(resolvedAccountNo);
        }

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