package my.portfolio.port_view.dto.strategy;

import my.portfolio.port_view.util.ViewMessages;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportTradeDetailDto(
        Long id,
        String tickerCode,
        String companyName,
        LocalDate buyDate,
        LocalDate sellDate,
        Integer holdingPeriod,
        String returnPct,
        String returnClass,

        String entryMarketSignal,
        String entryMarketSignalLabel,
        BigDecimal positionSize,
        BigDecimal buyFlowScore,
        BigDecimal buyScore,
        BigDecimal buyInfoScore,
        BigDecimal buyShortPressure,
        BigDecimal buyVolatility,
        BigDecimal buyIntradayRange,
        Boolean hasInfoFlag,

        BigDecimal buyOpenPrice,
        BigDecimal buyClosePrice,
        BigDecimal buyLowPrice,
        BigDecimal sellOpenPrice,
        BigDecimal sellClosePrice,
        BigDecimal sellLowPrice,

        String sellReason,
        String sellReasonLabel,
        BigDecimal sellFlowScore,
        BigDecimal sellFinalScore,
        BigDecimal sellCumReturn,

        List<String> buyReasonLines,
        List<String> sellReasonLines
) {

    public String stockDisplayName() {
        return ViewMessages.stockDisplayName(tickerCode, companyName);
    }

    public boolean showSeparateTicker() {
        return ViewMessages.showSeparateTicker(companyName);
    }
}