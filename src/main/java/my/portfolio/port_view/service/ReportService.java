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
                        "BLOCK 손실군 관리 필요",
                        blockWeakOther == null
                                ? "BLOCK 기타 약세형 데이터가 아직 없습니다."
                                : "BLOCK 기타 약세형 청산은 " + blockWeakOther.trades()
                                        + "건이며 평균 수익률은 " + formatPercent(blockWeakOther.avgReturn()) + "입니다.",
                        "주의",
                        "badge-warning"
                ),
                new ReportInsightDto(
                        "DIAGNOSIS",
                        "초기 손실 구간 점검 필요",
                        earlyCut == null
                                ? "초기 기대 미달 정리 데이터가 아직 없습니다."
                                : "초기 기대 미달 정리는 " + earlyCut.trades()
                                        + "건이며 평균 수익률은 " + formatPercent(earlyCut.avgReturn()) + "입니다.",
                        "위험",
                        "badge-danger"
                ),
                new ReportInsightDto(
                        "DIAGNOSIS",
                        "진입 당일 리스크 확인",
                        dayZero == null
                                ? "0일 보유 통계가 아직 없습니다."
                                : "0일 보유 구간의 평균 수익률은 " + formatPercent(dayZero.avgReturn())
                                        + ", 승률은 " + formatPercent(dayZero.winRate()) + "입니다.",
                        "주의",
                        "badge-warning"
                ),
                new ReportInsightDto(
                        "DIAGNOSIS",
                        "BLOCK 세부 버킷 확인",
                        worstBlock == null
                                ? "BLOCK 세부 버킷 데이터가 아직 없습니다."
                                : "가장 약한 BLOCK 버킷은 " + safe(worstBlock.holdingBucket())
                                        + " / " + safe(worstBlock.candidateTypeLabel())
                                        + "이며 평균 수익률은 " + formatPercent(worstBlock.avgReturn()) + "입니다.",
                        "분석",
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
                        "이익 보전 로직 작동",
                        profitProtect == null
                                ? "이익 보전 청산 데이터가 아직 없습니다."
                                : "이익 보전 청산은 " + profitProtect.trades()
                                        + "건이며 평균 수익률은 " + formatPercent(profitProtect.avgReturn()) + "입니다.",
                        "강점",
                        "badge-good"
                ),
                new ReportInsightDto(
                        "GOOD",
                        "승자 장기 보유 구조 유지",
                        maxHolding == null
                                ? "최대 보유기간 도달 데이터가 아직 없습니다."
                                : "최대 보유기간 도달 청산은 " + maxHolding.trades()
                                        + "건이며 평균 수익률은 " + formatPercent(maxHolding.avgReturn()) + "입니다.",
                        "강점",
                        "badge-good"
                ),
                new ReportInsightDto(
                        "GOOD",
                        "15일 보유 구간 성과 양호",
                        dayFifteen == null
                                ? "15일 보유 통계가 아직 없습니다."
                                : "15일 보유 구간의 평균 수익률은 " + formatPercent(dayFifteen.avgReturn())
                                        + ", 승률은 " + formatPercent(dayFifteen.winRate()) + "입니다.",
                        "강점",
                        "badge-good"
                )
        );
    }

    private List<ReportInsightDto> buildActionItems() {
        return List.of(
                new ReportInsightDto(
                        "ACTION",
                        "BLOCK 손실군 조건 재점검",
                        "전면 완화보다 질 저하형·기타 약세형 축소 중심으로 튜닝하는 것이 좋습니다.",
                        "1순위",
                        "badge-danger"
                ),
                new ReportInsightDto(
                        "ACTION",
                        "초기 0~2일 손실 방어",
                        "진입 직후 손실이 큰 종목을 줄일지, 좋은 종목을 너무 빨리 버리는지 분리해서 확인합니다.",
                        "2순위",
                        "badge-warning"
                ),
                new ReportInsightDto(
                        "ACTION",
                        "섹터 편향 점검",
                        "금융/은행처럼 특정 섹터가 성과를 깎는지 별도 리포트로 확인합니다.",
                        "3순위",
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
            return "미확인";
        }

        return switch (reason) {
            case "buy_day_intraday_stop" -> "진입 당일 급락 방어 청산";
            case "early_cut" -> "초기 기대 미달 정리";
            case "early_crash_stop" -> "초기 급락 방어 청산";
            case "early_risk_cut" -> "초기 급락 방어 청산";
            case "flow_breakdown" -> "수급 약화 정리";
            case "intraday_stop" -> "장중 손절";

            case "market_block_both_weak_fast" -> "BLOCK 수급·점수 동시 약세형 청산";
            case "market_block_flat_early" -> "BLOCK 초기 평탄형 청산";
            case "market_block_early_flat" -> "BLOCK 초기 평탄형 청산";
            case "market_block_flow_only_weak" -> "BLOCK 수급 약세형 청산";
            case "market_block_flat_late" -> "BLOCK 후기 평탄형 청산";
            case "market_block_late_flat" -> "BLOCK 후기 평탄형 청산";
            case "market_block_mid_hold_neither_clear" -> "BLOCK 중기 혼합·불명확형 청산";
            case "market_block_quality_drop" -> "BLOCK 질 저하형 청산";
            case "market_block_weak_other" -> "BLOCK 기타 약세형 청산";

            case "max_holding" -> "최대 보유기간 도달";
            case "profit_protect" -> "이익 보전 청산";
            case "score_breakdown" -> "종합 점수 약화 정리";
            case "short_pressure_exit" -> "공매도 압력 정리";
            case "short_pressure" -> "공매도 압력 정리";
            case "stale_loser" -> "장기 부진 정리";
            case "weak_signal" -> "약한 신호 정리";

            default -> reason;
        };
    }

    private String toCandidateTypeLabel(String candidateType) {
        if (candidateType == null) {
            return "미확인";
        }

        return switch (candidateType) {
            case "quality_drop" -> "질 저하형";
            case "weak_other" -> "기타 약세형";
            case "flat_early" -> "초기 평탄형";
            case "flat_late" -> "후기 평탄형";
            case "flow_only_weak" -> "수급 약세형";
            case "both_weak_fast" -> "수급·점수 동시 약세형";
            case "mid_hold_neither_clear" -> "중기 혼합·불명확형";
            default -> candidateType;
        };
    }

    private List<String> buildBuyReasonLines(TradeDetailRow row) {
        List<String> lines = new ArrayList<>();

        if (row.buyFlowScore() != null) {
            if (row.buyFlowScore().compareTo(BigDecimal.valueOf(0.85)) >= 0) {
                lines.add("수급 지표가 매우 우호적이었음 (수급 점수 " + formatScore(row.buyFlowScore()) + ")");
            } else if (row.buyFlowScore().compareTo(BigDecimal.valueOf(0.62)) >= 0) {
                lines.add("수급 기준을 충족했음 (수급 점수 " + formatScore(row.buyFlowScore()) + ")");
            } else {
                lines.add("수급 점수는 낮은 편이었음 (수급 점수 " + formatScore(row.buyFlowScore()) + ")");
            }
        }

        if (row.buyScore() != null) {
            if (row.buyScore().compareTo(BigDecimal.valueOf(0.50)) >= 0) {
                lines.add("종합 점수가 상위 구간이었음 (종합 점수 " + formatScore(row.buyScore()) + ")");
            } else {
                lines.add("종합 점수 조건을 충족했음 (종합 점수 " + formatScore(row.buyScore()) + ")");
            }
        }

        if (Boolean.TRUE.equals(row.hasInfoFlag())) {
            lines.add("정보 신호가 존재했음 (정보 점수 " + formatScore(row.buyInfoScore()) + ")");
        } else {
            lines.add("정보 신호는 중립이었음 (정보 점수 " + formatScore(row.buyInfoScore()) + ")");
        }

        lines.add("매수 판단: 수급 조건과 점수 조건 기반 진입");

        return lines;
    }

    private List<String> buildSellReasonLines(TradeDetailRow row) {
        List<String> lines = new ArrayList<>();

        if (row.sellReason() != null) {
            lines.add(toExitReasonDescription(row.sellReason()));
        }

        if (row.sellFlowScore() != null) {
            lines.add("청산 시점 수급 점수 " + formatScore(row.sellFlowScore()));
        }

        if (row.sellFinalScore() != null) {
            lines.add("청산 시점 종합 점수 " + formatScore(row.sellFinalScore()));
        }

        if (row.sellCumReturn() != null) {
            lines.add("누적 수익률 " + formatPercent(row.sellCumReturn()));
        } else if (row.returnPct() != null) {
            lines.add("최종 수익률 " + row.returnPct());
        }

        return lines;
    }

    private String toPerformanceStatusLabel(BigDecimal avgReturn, BigDecimal winRate) {
        if (avgReturn == null) {
            return "미확인";
        }

        if (avgReturn.compareTo(BigDecimal.valueOf(0.05)) >= 0) {
            return "강점";
        }

        if (avgReturn.compareTo(ZERO) >= 0) {
            return "양호";
        }

        if (winRate != null && winRate.compareTo(BigDecimal.valueOf(0.30)) < 0) {
            return "위험";
        }

        return "주의";
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
        return value == null || value.isBlank() ? "미확인" : value;
    }

    private String toExitReasonDescription(String reason) {
        if (reason == null) {
            return "매도 사유 미확인";
        }

        return switch (reason) {
            case "buy_day_intraday_stop" -> "진입 당일 변동성이 손절 기준을 하회해 정리함";
            case "early_cut" -> "초기 반응이 기대에 못 미쳐 정리함";
            case "early_crash_stop", "early_risk_cut" -> "초기 급락 방어 기준에 따라 정리함";
            case "flow_breakdown" -> "수급 우위가 약화되어 정리함";
            case "intraday_stop" -> "장중 변동이 손절 기준을 하회해 정리함";
            case "market_block_both_weak_fast" -> "BLOCK 구간에서 수급과 점수가 동시에 약해져 정리함";
            case "market_block_flat_early", "market_block_early_flat" -> "BLOCK 초기 평탄형 조건에 따라 정리함";
            case "market_block_flow_only_weak" -> "BLOCK 구간에서 수급 약세가 확인되어 정리함";
            case "market_block_flat_late", "market_block_late_flat" -> "BLOCK 후기 평탄형 조건에 따라 정리함";
            case "market_block_mid_hold_neither_clear" -> "BLOCK 중기 혼합·불명확형 조건에 따라 정리함";
            case "market_block_quality_drop" -> "BLOCK 구간에서 종목 질 저하가 확인되어 정리함";
            case "market_block_weak_other" -> "BLOCK 기타 약세형 조건에 따라 정리함";
            case "max_holding" -> "최대 보유 기간에 도달해 정리함";
            case "profit_protect" -> "이익 보전 조건에 따라 수익을 확정함";
            case "score_breakdown" -> "종합 점수가 약화되어 정리함";
            case "short_pressure_exit", "short_pressure" -> "공매도 압력이 높아져 정리함";
            case "stale_loser" -> "보유 기간 대비 반등 강도가 제한적이어서 정리함";
            case "weak_signal" -> "전략 신호가 약해져 정리함";
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
            return "미확인";
        }

        return switch (marketSignal) {
            case "AGGRESSIVE" -> "공격";
            case "NEUTRAL" -> "중립";
            case "DEFENSIVE" -> "방어";
            case "BLOCK" -> "차단";
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
            return "보합";
        }

        if (dailyReturn.compareTo(ZERO) > 0) {
            return "수익";
        }

        return "손실";
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