package my.portfolio.port_view.dto;

import java.math.BigDecimal;

public record ReportExitReasonStatDto(
        String reason,
        String reasonLabel,
        Integer trades,
        BigDecimal avgReturn,
        BigDecimal winRate,
        String statusLabel,
        String statusClass
) {
}