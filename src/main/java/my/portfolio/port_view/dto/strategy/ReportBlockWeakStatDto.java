package my.portfolio.port_view.dto.strategy;

import java.math.BigDecimal;

public record ReportBlockWeakStatDto(
        String holdingBucket,
        String candidateType,
        String candidateTypeLabel,
        String flowBucket,
        String finalBucket,
        String profitBucket,
        Integer trades,
        BigDecimal avgReturn,
        BigDecimal winRate,
        String statusLabel,
        String statusClass
) {
}