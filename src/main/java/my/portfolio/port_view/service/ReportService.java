package my.portfolio.port_view.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import my.portfolio.port_view.dto.strategy.ReportBacktestSummaryDto;
import my.portfolio.port_view.dto.strategy.ReportBlockWeakStatDto;
import my.portfolio.port_view.dto.strategy.ReportDailyTradeStatusDto;
import my.portfolio.port_view.dto.strategy.ReportExitReasonStatDto;
import my.portfolio.port_view.dto.strategy.ReportHoldingPeriodStatDto;
import my.portfolio.port_view.dto.strategy.ReportInsightDto;
import my.portfolio.port_view.dto.strategy.ReportStrategyPageDto;
import my.portfolio.port_view.dto.strategy.ReportTradeDetailDto;
import my.portfolio.port_view.repository.ReportRepository;
import my.portfolio.port_view.repository.ReportRepository.BacktestRunRow;
import my.portfolio.port_view.repository.ReportRepository.BlockWeakStatRow;
import my.portfolio.port_view.repository.ReportRepository.DailyTradeStatusRow;
import my.portfolio.port_view.repository.ReportRepository.ExitReasonStatRow;
import my.portfolio.port_view.repository.ReportRepository.HoldingPeriodStatRow;
import my.portfolio.port_view.repository.ReportRepository.TradeDetailRow;
import my.portfolio.port_view.util.ViewMessages;

@Service
public class ReportService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public ReportStrategyPageDto getLatestReport() {
        BacktestRunRow run = reportRepository.findLatestBacktestRun()
                .orElseThrow(() -> new IllegalStateException("최신 백테스트 리포트가 없습니다."));

        return buildReport(run);
    }

    public List<String> getStrategyConfigVersions() {
        return reportRepository.findStrategyConfigVersions();
    }

    public ReportStrategyPageDto getLatestReportByStrategyConfigVersion(String strategyConfigVersion) {
        if (strategyConfigVersion == null || strategyConfigVersion.isBlank()) {
            return getLatestReport();
        }

        String normalizedVersion = strategyConfigVersion.trim();

        BacktestRunRow run = reportRepository
                .findLatestBacktestRunByStrategyConfigVersion(normalizedVersion)
                .orElseThrow(() -> new IllegalArgumentException(
                        "백테스트 리포트를 찾을 수 없습니다. strategyConfigVersion=" + normalizedVersion
                ));

        return buildReport(run);
    }

    public ReportStrategyPageDto getReportByRunId(String runId) {
        BacktestRunRow run = reportRepository.findBacktestRunByRunId(runId)
                .orElseThrow(() -> new IllegalArgumentException("백테스트 리포트를 찾을 수 없습니다. runId=" + runId));

        return buildReport(run);
    }

    public ReportBacktestSummaryDto getLatestBacktestSummaryOrNull() {
        return reportRepository.findLatestBacktestRun()
                .map(this::toBacktestSummaryDto)
                .orElse(null);
    }

    private ReportStrategyPageDto buildReport(BacktestRunRow run) {
        String runId = run.runId();

        List<ReportExitReasonStatDto> exitReasonStats = reportRepository.findExitReasonStats(runId)
                .stream()
                .map(this::toExitReasonStatDto)
                .toList();

        List<ReportHoldingPeriodStatDto> holdingPeriodStats = reportRepository.findHoldingPeriodStats(runId)
                .stream()
                .map(this::toHoldingPeriodStatDto)
                .toList();

        List<ReportBlockWeakStatDto> blockWeakStats = reportRepository.findBlockWeakStats(runId)
                .stream()
                .map(this::toBlockWeakStatDto)
                .toList();

        List<ReportDailyTradeStatusDto> dailyTrades = reportRepository.findDailyTradeStatuses(runId)
                .stream()
                .map(this::toDailyTradeStatusDto)
                .toList();

        List<ReportTradeDetailDto> tradeDetails = reportRepository.findTradeDetails(runId)
                .stream()
                .map(this::toTradeDetailDto)
                .toList();

        return new ReportStrategyPageDto(
                toBacktestSummaryDto(run),
                buildDiagnoses(exitReasonStats, holdingPeriodStats, blockWeakStats),
                buildGoodPoints(exitReasonStats, holdingPeriodStats),
                buildActionItems(),
                exitReasonStats,
                holdingPeriodStats,
                blockWeakStats,
                dailyTrades,
                tradeDetails
        );
    }

    private ReportBacktestSummaryDto toBacktestSummaryDto(BacktestRunRow row) {
        return new ReportBacktestSummaryDto(
                row.id(),
                row.runId(),
                row.strategyName(),
                row.engineVersion(),
                row.startDate(),
                row.endDate(),
                row.cumulativeReturn(),
                row.maxDrawdown(),
                row.sharpeRatio(),
                row.totalTrades(),
                row.createdAt()
        );
    }

    private ReportExitReasonStatDto toExitReasonStatDto(ExitReasonStatRow row) {
        return new ReportExitReasonStatDto(
                row.reason(),
                toExitReasonLabel(row.reason()),
                row.trades(),
                row.avgReturn(),
                row.winRate(),
                toPerformanceStatusLabel(row.avgReturn(), row.winRate()),
                toPerformanceStatusClass(row.avgReturn(), row.winRate())
        );
    }

    private ReportHoldingPeriodStatDto toHoldingPeriodStatDto(HoldingPeriodStatRow row) {
        return new ReportHoldingPeriodStatDto(
                row.holdingPeriod(),
                row.trades(),
                row.avgReturn(),
                row.winRate(),
                toPerformanceStatusLabel(row.avgReturn(), row.winRate()),
                toPerformanceStatusClass(row.avgReturn(), row.winRate())
        );
    }

    private ReportBlockWeakStatDto toBlockWeakStatDto(BlockWeakStatRow row) {
        return new ReportBlockWeakStatDto(
                row.holdingBucket(),
                row.candidateType(),
                toCandidateTypeLabel(row.candidateType()),
                row.flowBucket(),
                row.finalBucket(),
                row.profitBucket(),
                row.trades(),
                row.avgReturn(),
                row.winRate(),
                toPerformanceStatusLabel(row.avgReturn(), row.winRate()),
                toPerformanceStatusClass(row.avgReturn(), row.winRate())
        );
    }

    private ReportDailyTradeStatusDto toDailyTradeStatusDto(DailyTradeStatusRow row) {
        return new ReportDailyTradeStatusDto(
                row.date(),

                row.marketSignal(),
                toMarketSignalLabel(row.marketSignal()),
                toMarketSignalClass(row.marketSignal()),

                row.positionCount(),

                row.dailyReturn(),
                row.cumulativeReturn(),

                row.buyCount(),
                row.sellCount(),
                row.avgSellReturn(),
                row.winSellCount(),

                safeNames(row.buyNames()),
                safeNames(row.sellNames()),

                toDayStatusLabel(row.dailyReturn()),
                toDayStatusClass(row.dailyReturn())
        );
    }

    private ReportTradeDetailDto toTradeDetailDto(TradeDetailRow row) {
        return new ReportTradeDetailDto(
                row.id(),
                row.tickerCode(),
                row.companyName(),
                row.buyDate(),
                row.sellDate(),
                row.holdingPeriod(),
                row.returnPct(),
                toReturnClass(row.returnPct()),

                row.entryMarketSignal(),
                toMarketSignalLabel(row.entryMarketSignal()),
                row.positionSize(),
                row.buyFlowScore(),
                row.buyScore(),
                row.buyInfoScore(),
                row.buyShortPressure(),
                row.buyVolatility(),
                row.buyIntradayRange(),
                row.hasInfoFlag(),

                row.buyOpenPrice(),
                row.buyClosePrice(),
                row.buyLowPrice(),
                row.sellOpenPrice(),
                row.sellClosePrice(),
                row.sellLowPrice(),

                row.sellReason(),
                toExitReasonLabel(row.sellReason()),
                row.sellFlowScore(),
                row.sellFinalScore(),
                row.sellCumReturn(),

                buildBuyReasonLines(row),
                buildSellReasonLines(row)
        );
    }

    private List<ReportInsightDto> buildDiagnoses(
            List<ReportExitReasonStatDto> exitReasonStats,
            List<ReportHoldingPeriodStatDto> holdingPeriodStats,
            List<ReportBlockWeakStatDto> blockWeakStats
    ) {
        ReportExitReasonStatDto blockWeakOther = findExitReason(exitReasonStats, "market_block_weak_other");
        ReportExitReasonStatDto earlyCut = findExitReason(exitReasonStats, "early_cut");
        ReportHoldingPeriodStatDto dayZero = findHoldingPeriod(holdingPeriodStats, 0);
        ReportBlockWeakStatDto worstBlock = findWorstBlockWeak(blockWeakStats);

        return List.of(
                new ReportInsightDto(
                        "DIAGNOSIS",
                        ViewMessages.text("report.diagnosis.blockWeak.title"),
                        blockWeakOther == null
                                ? ViewMessages.text("report.diagnosis.blockWeak.noData")
                                : ViewMessages.text(
                                        "report.diagnosis.blockWeak.description",
                                        blockWeakOther.trades(),
                                        formatPercent(blockWeakOther.avgReturn())
                                ),
                        ViewMessages.text("report.badge.caution"),
                        "badge-warning"
                ),
                new ReportInsightDto(
                        "DIAGNOSIS",
                        ViewMessages.text("report.diagnosis.earlyCut.title"),
                        earlyCut == null
                                ? ViewMessages.text("report.diagnosis.earlyCut.noData")
                                : ViewMessages.text(
                                        "report.diagnosis.earlyCut.description",
                                        earlyCut.trades(),
                                        formatPercent(earlyCut.avgReturn())
                                ),
                        ViewMessages.text("report.badge.risk"),
                        "badge-danger"
                ),
                new ReportInsightDto(
                        "DIAGNOSIS",
                        ViewMessages.text("report.diagnosis.dayZero.title"),
                        dayZero == null
                                ? ViewMessages.text("report.diagnosis.dayZero.noData")
                                : ViewMessages.text(
                                        "report.diagnosis.dayZero.description",
                                        formatPercent(dayZero.avgReturn()),
                                        formatPercent(dayZero.winRate())
                                ),
                        ViewMessages.text("report.badge.caution"),
                        "badge-warning"
                ),
                new ReportInsightDto(
                        "DIAGNOSIS",
                        ViewMessages.text("report.diagnosis.blockBucket.title"),
                        worstBlock == null
                                ? ViewMessages.text("report.diagnosis.blockBucket.noData")
                                : ViewMessages.text(
                                        "report.diagnosis.blockBucket.description",
                                        safe(worstBlock.holdingBucketLabel()),
                                        safe(worstBlock.candidateTypeLabel()),
                                        formatPercent(worstBlock.avgReturn())
                                ),
                        ViewMessages.text("report.badge.analysis"),
                        "badge-neutral"
                )
        );
    }

    private List<ReportInsightDto> buildGoodPoints(
            List<ReportExitReasonStatDto> exitReasonStats,
            List<ReportHoldingPeriodStatDto> holdingPeriodStats
    ) {
        ReportExitReasonStatDto profitProtect = findExitReason(exitReasonStats, "profit_protect");
        ReportExitReasonStatDto maxHolding = findExitReason(exitReasonStats, "max_holding");
        ReportHoldingPeriodStatDto dayFifteen = findHoldingPeriod(holdingPeriodStats, 15);

        return List.of(
                new ReportInsightDto(
                        "GOOD",
                        ViewMessages.text("report.strength.profitProtect.title"),
                        profitProtect == null
                                ? ViewMessages.text("report.strength.profitProtect.noData")
                                : ViewMessages.text(
                                        "report.strength.profitProtect.description",
                                        profitProtect.trades(),
                                        formatPercent(profitProtect.avgReturn())
                                ),
                        ViewMessages.text("report.badge.strength"),
                        "badge-good"
                ),
                new ReportInsightDto(
                        "GOOD",
                        ViewMessages.text("report.strength.maxHolding.title"),
                        maxHolding == null
                                ? ViewMessages.text("report.strength.maxHolding.noData")
                                : ViewMessages.text(
                                        "report.strength.maxHolding.description",
                                        maxHolding.trades(),
                                        formatPercent(maxHolding.avgReturn())
                                ),
                        ViewMessages.text("report.badge.strength"),
                        "badge-good"
                ),
                new ReportInsightDto(
                        "GOOD",
                        ViewMessages.text("report.strength.day15.title"),
                        dayFifteen == null
                                ? ViewMessages.text("report.strength.day15.noData")
                                : ViewMessages.text(
                                        "report.strength.day15.description",
                                        formatPercent(dayFifteen.avgReturn()),
                                        formatPercent(dayFifteen.winRate())
                                ),
                        ViewMessages.text("report.badge.strength"),
                        "badge-good"
                )
        );
    }

    private List<ReportInsightDto> buildActionItems() {
        return List.of(
                new ReportInsightDto(
                        "ACTION",
                        ViewMessages.text("report.action.blockWeak.title"),
                        ViewMessages.text("report.action.blockWeak.description"),
                        ViewMessages.text("report.badge.priority1"),
                        "badge-danger"
                ),
                new ReportInsightDto(
                        "ACTION",
                        ViewMessages.text("report.action.earlyLoss.title"),
                        ViewMessages.text("report.action.earlyLoss.description"),
                        ViewMessages.text("report.badge.priority2"),
                        "badge-warning"
                ),
                new ReportInsightDto(
                        "ACTION",
                        ViewMessages.text("report.action.sectorBias.title"),
                        ViewMessages.text("report.action.sectorBias.description"),
                        ViewMessages.text("report.badge.priority3"),
                        "badge-neutral"
                )
        );
    }

    private ReportExitReasonStatDto findExitReason(List<ReportExitReasonStatDto> stats, String reason) {
        return stats.stream()
                .filter(stat -> reason.equals(stat.reason()))
                .findFirst()
                .orElse(null);
    }

    private ReportHoldingPeriodStatDto findHoldingPeriod(List<ReportHoldingPeriodStatDto> stats, int holdingPeriod) {
        return stats.stream()
                .filter(stat -> stat.holdingPeriod() != null && stat.holdingPeriod() == holdingPeriod)
                .findFirst()
                .orElse(null);
    }

    private ReportBlockWeakStatDto findWorstBlockWeak(List<ReportBlockWeakStatDto> stats) {
        return stats.stream()
                .filter(stat -> stat.avgReturn() != null)
                .min((a, b) -> a.avgReturn().compareTo(b.avgReturn()))
                .orElse(null);
    }

    private String toExitReasonLabel(String reason) {
        if (reason == null) {
            return ViewMessages.text("label.unknown");
        }

        return switch (reason) {
            case "buy_day_intraday_stop" -> ViewMessages.text("report.exitReason.buyDayIntradayStop.label");
            case "early_cut" -> ViewMessages.text("report.exitReason.earlyCut.label");
            case "early_crash_stop", "early_risk_cut" -> ViewMessages.text("report.exitReason.earlyCrash.label");
            case "flow_breakdown" -> ViewMessages.text("report.exitReason.flowBreakdown.label");
            case "intraday_stop" -> ViewMessages.text("report.exitReason.intradayStop.label");

            case "market_block_both_weak_fast" -> ViewMessages.text("report.exitReason.blockBothWeakFast.label");
            case "market_block_flat_early", "market_block_early_flat" -> ViewMessages.text("report.exitReason.blockFlatEarly.label");
            case "market_block_flow_only_weak" -> ViewMessages.text("report.exitReason.blockFlowOnlyWeak.label");
            case "market_block_flat_late", "market_block_late_flat" -> ViewMessages.text("report.exitReason.blockFlatLate.label");
            case "market_block_mid_hold_neither_clear" -> ViewMessages.text("report.exitReason.blockMidHoldNeitherClear.label");
            case "market_block_quality_drop" -> ViewMessages.text("report.exitReason.blockQualityDrop.label");
            case "market_block_weak_other" -> ViewMessages.text("report.exitReason.blockWeakOther.label");

            case "max_holding" -> ViewMessages.text("report.exitReason.maxHolding.label");
            case "profit_protect" -> ViewMessages.text("report.exitReason.profitProtect.label");
            case "score_breakdown" -> ViewMessages.text("report.exitReason.scoreBreakdown.label");
            case "short_pressure_exit", "short_pressure" -> ViewMessages.text("report.exitReason.shortPressure.label");
            case "stale_loser" -> ViewMessages.text("report.exitReason.staleLoser.label");
            case "weak_signal" -> ViewMessages.text("report.exitReason.weakSignal.label");

            default -> reason;
        };
    }

    private String toCandidateTypeLabel(String candidateType) {
        if (candidateType == null) {
            return ViewMessages.text("label.unknown");
        }

        return switch (candidateType) {
            case "quality_drop" -> ViewMessages.text("report.candidate.qualityDrop");
            case "weak_other" -> ViewMessages.text("report.candidate.weakOther");
            case "flat_early" -> ViewMessages.text("report.candidate.flatEarly");
            case "flat_late" -> ViewMessages.text("report.candidate.flatLate");
            case "flow_only_weak" -> ViewMessages.text("report.candidate.flowOnlyWeak");
            case "both_weak_fast" -> ViewMessages.text("report.candidate.bothWeakFast");
            case "mid_hold_neither_clear" -> ViewMessages.text("report.candidate.midHoldNeitherClear");
            default -> candidateType;
        };
    }

    private List<String> buildBuyReasonLines(TradeDetailRow row) {
        List<String> lines = new ArrayList<>();

        if (row.buyFlowScore() != null) {
            if (row.buyFlowScore().compareTo(BigDecimal.valueOf(0.85)) >= 0) {
                lines.add(ViewMessages.text(
                        "report.rationale.buy.flowVeryFavorable",
                        formatScore(row.buyFlowScore())
                ));
            } else if (row.buyFlowScore().compareTo(BigDecimal.valueOf(0.62)) >= 0) {
                lines.add(ViewMessages.text(
                        "report.rationale.buy.flowQualified",
                        formatScore(row.buyFlowScore())
                ));
            } else {
                lines.add(ViewMessages.text(
                        "report.rationale.buy.flowLow",
                        formatScore(row.buyFlowScore())
                ));
            }
        }

        if (row.buyScore() != null) {
            if (row.buyScore().compareTo(BigDecimal.valueOf(0.50)) >= 0) {
                lines.add(ViewMessages.text(
                        "report.rationale.buy.scoreHigh",
                        formatScore(row.buyScore())
                ));
            } else {
                lines.add(ViewMessages.text(
                        "report.rationale.buy.scoreQualified",
                        formatScore(row.buyScore())
                ));
            }
        }

        if (Boolean.TRUE.equals(row.hasInfoFlag())) {
            lines.add(ViewMessages.text(
                    "report.rationale.buy.infoPresent",
                    formatScore(row.buyInfoScore())
            ));
        } else {
            lines.add(ViewMessages.text(
                    "report.rationale.buy.infoNeutral",
                    formatScore(row.buyInfoScore())
            ));
        }

        lines.add(ViewMessages.text("report.rationale.buy.decision"));

        return lines;
    }

    private List<String> buildSellReasonLines(TradeDetailRow row) {
        List<String> lines = new ArrayList<>();

        if (row.sellReason() != null) {
            lines.add(toExitReasonDescription(row.sellReason()));
        }

        if (row.sellFlowScore() != null) {
            lines.add(ViewMessages.text(
                    "report.rationale.sell.flowScore",
                    formatScore(row.sellFlowScore())
            ));
        }

        if (row.sellFinalScore() != null) {
            lines.add(ViewMessages.text(
                    "report.rationale.sell.finalScore",
                    formatScore(row.sellFinalScore())
            ));
        }

        if (row.sellCumReturn() != null) {
            lines.add(ViewMessages.text(
                    "report.rationale.sell.cumulativeReturn",
                    formatPercent(row.sellCumReturn())
            ));
        } else if (row.returnPct() != null) {
            lines.add(ViewMessages.text("report.rationale.sell.finalReturn", row.returnPct()));
        }

        return lines;
    }

    private String toPerformanceStatusLabel(BigDecimal avgReturn, BigDecimal winRate) {
        if (avgReturn == null) {
            return ViewMessages.text("report.performance.unknown");
        }

        if (avgReturn.compareTo(BigDecimal.valueOf(0.05)) >= 0) {
            return ViewMessages.text("report.performance.strength");
        }

        if (avgReturn.compareTo(ZERO) >= 0) {
            return ViewMessages.text("report.performance.good");
        }

        if (winRate != null && winRate.compareTo(BigDecimal.valueOf(0.30)) < 0) {
            return ViewMessages.text("report.performance.risk");
        }

        return ViewMessages.text("report.performance.caution");
    }

    private String toPerformanceStatusClass(BigDecimal avgReturn, BigDecimal winRate) {
        if (avgReturn == null) {
            return "badge-neutral";
        }

        if (avgReturn.compareTo(BigDecimal.valueOf(0.05)) >= 0) {
            return "badge-good";
        }

        if (avgReturn.compareTo(ZERO) >= 0) {
            return "badge-ok";
        }

        if (winRate != null && winRate.compareTo(BigDecimal.valueOf(0.30)) < 0) {
            return "badge-danger";
        }

        return "badge-warning";
    }

    private String formatPercent(BigDecimal value) {
        if (value == null) {
            return "-";
        }

        return value.multiply(BigDecimal.valueOf(100))
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .toPlainString() + "%";
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? ViewMessages.text("label.unknown") : value;
    }

    private String toExitReasonDescription(String reason) {
        if (reason == null) {
            return ViewMessages.text("report.header.sellReason") + ": " + ViewMessages.text("label.unknown");
        }

        return switch (reason) {
            case "buy_day_intraday_stop" -> ViewMessages.text("report.exitReason.buyDayIntradayStop.description");
            case "early_cut" -> ViewMessages.text("report.exitReason.earlyCut.description");
            case "early_crash_stop", "early_risk_cut" -> ViewMessages.text("report.exitReason.earlyCrash.description");
            case "flow_breakdown" -> ViewMessages.text("report.exitReason.flowBreakdown.description");
            case "intraday_stop" -> ViewMessages.text("report.exitReason.intradayStop.description");
            case "market_block_both_weak_fast" -> ViewMessages.text("report.exitReason.blockBothWeakFast.description");
            case "market_block_flat_early", "market_block_early_flat" -> ViewMessages.text("report.exitReason.blockFlatEarly.description");
            case "market_block_flow_only_weak" -> ViewMessages.text("report.exitReason.blockFlowOnlyWeak.description");
            case "market_block_flat_late", "market_block_late_flat" -> ViewMessages.text("report.exitReason.blockFlatLate.description");
            case "market_block_mid_hold_neither_clear" -> ViewMessages.text("report.exitReason.blockMidHoldNeitherClear.description");
            case "market_block_quality_drop" -> ViewMessages.text("report.exitReason.blockQualityDrop.description");
            case "market_block_weak_other" -> ViewMessages.text("report.exitReason.blockWeakOther.description");
            case "max_holding" -> ViewMessages.text("report.exitReason.maxHolding.description");
            case "profit_protect" -> ViewMessages.text("report.exitReason.profitProtect.description");
            case "score_breakdown" -> ViewMessages.text("report.exitReason.scoreBreakdown.description");
            case "short_pressure_exit", "short_pressure" -> ViewMessages.text("report.exitReason.shortPressure.description");
            case "stale_loser" -> ViewMessages.text("report.exitReason.staleLoser.description");
            case "weak_signal" -> ViewMessages.text("report.exitReason.weakSignal.description");
            default -> reason;
        };
    }

    private String toReturnClass(String returnPct) {
        if (returnPct == null || returnPct.isBlank()) {
            return "neutral";
        }

        return returnPct.trim().startsWith("-") ? "negative" : "positive";
    }

    private String formatScore(BigDecimal value) {
        if (value == null) {
            return "-";
        }

        return value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String toMarketSignalLabel(String marketSignal) {
        if (marketSignal == null) {
            return ViewMessages.text("label.unknown");
        }

        return switch (marketSignal) {
            case "AGGRESSIVE" -> ViewMessages.text("report.market.aggressive");
            case "NEUTRAL" -> ViewMessages.text("report.market.neutral");
            case "DEFENSIVE" -> ViewMessages.text("report.market.defensive");
            case "BLOCK" -> ViewMessages.text("report.market.block");
            default -> marketSignal;
        };
    }

    private String toMarketSignalClass(String marketSignal) {
        if (marketSignal == null) {
            return "badge-neutral";
        }

        return switch (marketSignal) {
            case "AGGRESSIVE" -> "badge-good";
            case "NEUTRAL" -> "badge-neutral";
            case "DEFENSIVE" -> "badge-warning";
            case "BLOCK" -> "badge-danger";
            default -> "badge-neutral";
        };
    }

    private String toDayStatusLabel(BigDecimal dailyReturn) {
        if (dailyReturn == null || dailyReturn.compareTo(ZERO) == 0) {
            return ViewMessages.text("report.day.flat");
        }

        if (dailyReturn.compareTo(ZERO) > 0) {
            return ViewMessages.text("report.day.profit");
        }

        return ViewMessages.text("report.day.loss");
    }

    private String toDayStatusClass(BigDecimal dailyReturn) {
        if (dailyReturn == null || dailyReturn.compareTo(ZERO) == 0) {
            return "badge-neutral";
        }

        if (dailyReturn.compareTo(ZERO) > 0) {
            return "badge-good";
        }

        return "badge-danger";
    }

    private String safeNames(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        return value;
    }

}