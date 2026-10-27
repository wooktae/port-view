package my.portfolio.port_view.dto.strategy;

import java.math.BigDecimal;

import my.portfolio.port_view.util.ViewMessages;

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

    public String holdingBucketLabel() {
        if ("3이하".equals(holdingBucket)) {
            return ViewMessages.text("report.bucket.holding.upTo3");
        }
        return holdingBucket;
    }
}
