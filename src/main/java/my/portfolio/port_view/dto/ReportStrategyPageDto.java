package my.portfolio.port_view.dto;

import java.util.List;

public record ReportStrategyPageDto(
        ReportBacktestSummaryDto summary,
        List<ReportInsightDto> diagnoses,
        List<ReportInsightDto> goodPoints,
        List<ReportInsightDto> actionItems,
        List<ReportExitReasonStatDto> exitReasonStats,
        List<ReportHoldingPeriodStatDto> holdingPeriodStats,
        List<ReportBlockWeakStatDto> blockWeakStats,
        List<ReportDailyTradeStatusDto> dailyTrades,
        List<ReportTradeDetailDto> tradeDetails
) {
}