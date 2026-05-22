package my.portfolio.port_view.dto;

import java.math.BigDecimal;

public record ReportHoldingPeriodStatDto(
        Integer holdingPeriod,
        Integer trades,
        BigDecimal avgReturn,
        BigDecimal winRate,
        String statusLabel,
        String statusClass
) {
}