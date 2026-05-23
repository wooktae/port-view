package my.portfolio.port_view.dto.strategy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ReportBacktestSummaryDto(
        Long id,
        String runId,
        String strategyName,
        String engineVersion,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal cumulativeReturn,
        BigDecimal maxDrawdown,
        BigDecimal sharpeRatio,
        Integer totalTrades,
        OffsetDateTime createdAt
) {
}