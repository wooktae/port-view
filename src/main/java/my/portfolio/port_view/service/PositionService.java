package my.portfolio.port_view.service;

import my.portfolio.port_view.dto.position.PositionAgencyReportDTO;
import my.portfolio.port_view.dto.position.PositionDetailPageDTO;
import my.portfolio.port_view.dto.position.PositionDetailSummaryDTO;
import my.portfolio.port_view.dto.position.PositionNewsDTO;
import my.portfolio.port_view.dto.position.PositionPageDTO;
import my.portfolio.port_view.dto.position.PositionProfitPointDTO;
import my.portfolio.port_view.dto.position.PositionRowDTO;
import my.portfolio.port_view.entity.ConnectorPositionSnapshot;
import my.portfolio.port_view.repository.ConnectorPositionSnapshotRepository;
import my.portfolio.port_view.repository.PositionInsightRepository;
import my.portfolio.port_view.util.ProfitClassUtils;
import my.portfolio.port_view.util.ViewFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 보유 종목 목록/상세 화면의 평가금액, 수익률, 리서치 보조 정보를 조립한다.
 * Connector 포지션 스냅샷과 리서치 insight 조회 결과를 화면 표시용 DTO로 변환한다.
 */
@Service
public class PositionService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private static final int DETAIL_LOOKBACK_DAYS = 7;
    private static final int DETAIL_REPORT_LIMIT = 10;
    private static final int DETAIL_NEWS_LIMIT = 10;

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private static final DateTimeFormatter CHART_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM-dd");

    private final ConnectorPositionSnapshotRepository positionRepository;
    private final PositionInsightRepository insightRepository;

    public PositionService(
            ConnectorPositionSnapshotRepository positionRepository,
            PositionInsightRepository insightRepository
    ) {
        this.positionRepository = positionRepository;
        this.insightRepository = insightRepository;
    }

    @Transactional(readOnly = true)
    public PositionPageDTO getPositionPage(String accountNo) {

        List<ConnectorPositionSnapshot> positions =
                positionRepository.findLatestPositionsByAccountNo(accountNo);
        List<ConnectorPositionSnapshot> history =
                positionRepository.findPositionHistoryByAccountNo(accountNo);

        PositionPageDTO page = new PositionPageDTO();
        page.setAccountNo(accountNo);
        page.setHasPositions(!positions.isEmpty());
        page.setPositionCount(positions.size());

        if (positions.isEmpty()) {
            page.setAsOfDate("-");
            page.setAsOfTs("-");
            page.setTotalBuyAmountText("-");
            page.setTotalEvalAmountText("-");
            page.setTotalEvalProfitText("-");
            page.setTotalProfitRateText("-");
            page.setBestPositionTitleText("최고 수익 종목");
            page.setBestPositionName("-");
            page.setBestPositionRateText("-");
            page.setBestPositionProfitClass("neutral");
            page.setWorstPositionName("-");
            page.setWorstPositionRateText("-");
            page.setWorstPositionProfitClass("neutral");        
            page.setPageProfitClass("neutral");
            page.setRows(new ArrayList<>());
            return page;
        }

        ConnectorPositionSnapshot first = positions.get(0);

        page.setAsOfDate(first.getAsOfDate() == null ? "-" : first.getAsOfDate().toString());
        page.setAsOfTs(ViewFormatUtils.formatDateTime(first.getAsOfTs()));

        BigDecimal totalBuyAmount = positions.stream()
                .map(ConnectorPositionSnapshot::getBuyAmount)
                .map(this::nvl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEvalAmount = positions.stream()
                .map(ConnectorPositionSnapshot::getEvalAmount)
                .map(this::nvl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEvalProfit = positions.stream()
                .map(ConnectorPositionSnapshot::getEvalProfit)
                .map(this::nvl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfitRate = safeRate(totalEvalProfit, totalBuyAmount);

        Map<String, List<PositionProfitPointDTO>> profitPointMap =
                buildProfitPointMap(history);

        List<PositionRowDTO> rows = toRows(positions, totalEvalAmount, profitPointMap);

        PositionRowDTO best = rows.stream()
                .max(Comparator.comparing(PositionRowDTO::getEvalProfitRate))
                .orElse(null);

        PositionRowDTO worst = rows.stream()
                .min(Comparator.comparing(PositionRowDTO::getEvalProfitRate))
                .orElse(null);

        page.setTotalBuyAmount(totalBuyAmount);
        page.setTotalEvalAmount(totalEvalAmount);
        page.setTotalEvalProfit(totalEvalProfit);
        page.setTotalProfitRate(totalProfitRate);

        page.setTotalBuyAmountText(ViewFormatUtils.formatMoney(totalBuyAmount));
        page.setTotalEvalAmountText(ViewFormatUtils.formatMoney(totalEvalAmount));
        page.setTotalEvalProfitText(ViewFormatUtils.formatSignedMoney(totalEvalProfit));
        page.setTotalProfitRateText(ViewFormatUtils.formatSignedPercentAlready(totalProfitRate));
        page.setPageProfitClass(ProfitClassUtils.toProfitClass(totalEvalProfit));

        boolean hasPositivePosition = rows.stream()
                .anyMatch(row -> row.getEvalProfitRate() != null
                        && row.getEvalProfitRate().compareTo(BigDecimal.ZERO) > 0);

        page.setBestPositionTitleText(hasPositivePosition ? "최고 수익 종목" : "최소 손실 종목");

        page.setBestPositionName(best == null ? "-" : ViewFormatUtils.emptyIfNull(best.getStockName()));
        page.setBestPositionRateText(best == null ? "-" : best.getEvalProfitRateText());
        page.setBestPositionProfitClass(best == null ? "neutral" : best.getProfitClass());

        page.setWorstPositionName(worst == null ? "-" : ViewFormatUtils.emptyIfNull(worst.getStockName()));
        page.setWorstPositionRateText(worst == null ? "-" : worst.getEvalProfitRateText());
        page.setWorstPositionProfitClass(worst == null ? "neutral" : worst.getProfitClass());

        page.setRows(rows);

        return page;
    }

    @Transactional(readOnly = true)
    public PositionDetailPageDTO getPositionDetailPage(String accountNo, String tickerCode) {
        String normalizedTickerCode = normalizeTickerCode(tickerCode);

        LocalDate startDate = LocalDate.now(SEOUL_ZONE).minusDays(DETAIL_LOOKBACK_DAYS - 1);

        PositionDetailSummaryDTO summary =
                insightRepository.findSummary(normalizedTickerCode, startDate, DETAIL_LOOKBACK_DAYS);

        List<PositionAgencyReportDTO> reports =
                insightRepository.findRecentReports(normalizedTickerCode, startDate, DETAIL_REPORT_LIMIT);

        List<PositionNewsDTO> news =
                insightRepository.findRecentNews(normalizedTickerCode, startDate, DETAIL_NEWS_LIMIT);
        
        normalizeDetailSummary(summary, reports, news);

        PositionDetailPageDTO page = positionRepository
                .findLatestPositionByAccountNoAndTickerCode(accountNo, normalizedTickerCode)
                .map(position -> toDetailPage(accountNo, position))
                .orElseGet(() -> emptyDetailPage(accountNo, normalizedTickerCode));

        page.setSummary(summary);
        page.setReports(reports);
        page.setNews(news);

        return page;
    }

    private PositionDetailPageDTO toDetailPage(
            String accountNo,
            ConnectorPositionSnapshot position
    ) {
        PositionDetailPageDTO page = new PositionDetailPageDTO();

        BigDecimal evalProfitRate = resolveProfitRate(position);
        BigDecimal evalProfit = nvl(position.getEvalProfit());

        page.setAccountNo(accountNo);
        page.setTickerCode(position.getTickerCode());
        page.setStockName(ViewFormatUtils.emptyIfNull(position.getStockName()));
        page.setMarket(position.getMarket());
        page.setHasPosition(true);

        page.setQuantity(nvlInt(position.getQuantity()));
        page.setSellableQuantity(nvlInt(position.getSellableQuantity()));

        page.setAvgBuyPrice(nvl(position.getAvgBuyPrice()));
        page.setBuyAmount(nvl(position.getBuyAmount()));
        page.setCurrentPrice(nvl(position.getCurrentPrice()));
        page.setEvalAmount(nvl(position.getEvalAmount()));
        page.setEvalProfit(evalProfit);
        page.setEvalProfitRate(evalProfitRate);

        page.setQuantityText(ViewFormatUtils.formatQty(page.getQuantity()));
        page.setSellableQuantityText(ViewFormatUtils.formatQty(page.getSellableQuantity()));

        page.setAvgBuyPriceText(ViewFormatUtils.formatMoney(position.getAvgBuyPrice()));
        page.setBuyAmountText(ViewFormatUtils.formatMoney(position.getBuyAmount()));
        page.setCurrentPriceText(ViewFormatUtils.formatMoney(position.getCurrentPrice()));
        page.setEvalAmountText(ViewFormatUtils.formatMoney(position.getEvalAmount()));
        page.setEvalProfitText(ViewFormatUtils.formatSignedMoney(position.getEvalProfit()));
        page.setEvalProfitRateText(ViewFormatUtils.formatSignedPercentAlready(evalProfitRate));

        page.setProfitClass(ProfitClassUtils.toProfitClass(evalProfit));

        page.setAsOfDate(position.getAsOfDate() == null ? "-" : position.getAsOfDate().toString());
        page.setAsOfTs(ViewFormatUtils.formatDateTime(position.getAsOfTs()));

        return page;
    }

    private PositionDetailPageDTO emptyDetailPage(String accountNo, String tickerCode) {
        PositionDetailPageDTO page = new PositionDetailPageDTO();

        page.setAccountNo(accountNo);
        page.setTickerCode(tickerCode);
        page.setStockName(tickerCode);
        page.setMarket("-");
        page.setHasPosition(false);

        page.setQuantityText("-");
        page.setSellableQuantityText("-");
        page.setAvgBuyPriceText("-");
        page.setBuyAmountText("-");
        page.setCurrentPriceText("-");
        page.setEvalAmountText("-");
        page.setEvalProfitText("-");
        page.setEvalProfitRateText("-");
        page.setProfitClass("neutral");
        page.setAsOfDate("-");
        page.setAsOfTs("-");

        return page;
    }

    private List<PositionRowDTO> toRows(
            List<ConnectorPositionSnapshot> positions,
            BigDecimal totalEvalAmount,
            Map<String, List<PositionProfitPointDTO>> profitPointMap
    ) {
        List<PositionRowDTO> result = new ArrayList<>();

        for (ConnectorPositionSnapshot p : positions) {
            BigDecimal buyAmount = nvl(p.getBuyAmount());
            BigDecimal evalAmount = nvl(p.getEvalAmount());
            BigDecimal evalProfit = nvl(p.getEvalProfit());

            BigDecimal evalProfitRate = resolveProfitRate(p);
            BigDecimal weightRate = safeRate(evalAmount, totalEvalAmount);

            PositionRowDTO row = new PositionRowDTO();

            row.setTickerCode(p.getTickerCode());
            row.setStockName(ViewFormatUtils.emptyIfNull(p.getStockName()));
            row.setMarket(p.getMarket());

            row.setQuantity(nvlInt(p.getQuantity()));
            row.setSellableQuantity(nvlInt(p.getSellableQuantity()));

            row.setAvgBuyPrice(nvl(p.getAvgBuyPrice()));
            row.setBuyAmount(buyAmount);
            row.setCurrentPrice(nvl(p.getCurrentPrice()));
            row.setEvalAmount(evalAmount);
            row.setEvalProfit(evalProfit);
            row.setEvalProfitRate(evalProfitRate);
            row.setWeightRate(weightRate);

            row.setAvgBuyPriceText(ViewFormatUtils.formatMoney(p.getAvgBuyPrice()));
            row.setBuyAmountText(ViewFormatUtils.formatMoney(buyAmount));
            row.setCurrentPriceText(ViewFormatUtils.formatMoney(p.getCurrentPrice()));
            row.setEvalAmountText(ViewFormatUtils.formatMoney(evalAmount));
            row.setEvalProfitText(ViewFormatUtils.formatSignedMoney(evalProfit));
            row.setEvalProfitRateText(ViewFormatUtils.formatSignedPercentAlready(evalProfitRate));
            row.setWeightRateText(ViewFormatUtils.formatPercentAlready(weightRate));

            row.setProfitClass(ProfitClassUtils.toProfitClass(evalProfit));
            row.setWeightBarWidth(toBarWidth(weightRate));
            row.setProfitBarWidth(toBarWidth(evalProfitRate.abs()));

            row.setAsOfDate(p.getAsOfDate() == null ? "-" : p.getAsOfDate().toString());
            row.setAsOfTs(ViewFormatUtils.formatDateTime(p.getAsOfTs()));

            row.setProfitPoints(
                    profitPointMap.getOrDefault(row.getTickerCode(), new ArrayList<>())
            );

            result.add(row);
        }

        return result;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private int nvlInt(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * numerator / denominator * 100
     */
    private BigDecimal safeRate(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return numerator
                .multiply(HUNDRED)
                .divide(denominator, 4, RoundingMode.HALF_UP);
    }

    private int toBarWidth(BigDecimal rate) {
        if (rate == null) {
            return 0;
        }

        BigDecimal normalized = rate.abs();

        if (normalized.compareTo(BigDecimal.valueOf(100)) > 0) {
            return 100;
        }

        return normalized.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private Map<String, List<PositionProfitPointDTO>> buildProfitPointMap(
            List<ConnectorPositionSnapshot> history
    ) {
        Map<String, List<ConnectorPositionSnapshot>> grouped = new LinkedHashMap<>();

        for (ConnectorPositionSnapshot snapshot : history) {
            if (snapshot.getTickerCode() == null) {
                continue;
            }

            if (nvlInt(snapshot.getQuantity()) <= 0) {
                continue;
            }

            grouped.computeIfAbsent(snapshot.getTickerCode(), key -> new ArrayList<>())
                    .add(snapshot);
        }

        Map<String, List<PositionProfitPointDTO>> result = new HashMap<>();

        for (Map.Entry<String, List<ConnectorPositionSnapshot>> entry : grouped.entrySet()) {
            List<ConnectorPositionSnapshot> snapshots = entry.getValue();

            snapshots.sort(
                    Comparator.comparing(
                            ConnectorPositionSnapshot::getAsOfDate,
                            Comparator.nullsLast(Comparator.naturalOrder())
                    )
            );

            BigDecimal maxAbsRate = snapshots.stream()
                    .map(this::resolveProfitRate)
                    .map(BigDecimal::abs)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            List<PositionProfitPointDTO> points = new ArrayList<>();

            for (ConnectorPositionSnapshot snapshot : snapshots) {
                BigDecimal profitRate = resolveProfitRate(snapshot);

                PositionProfitPointDTO point = new PositionProfitPointDTO();
                point.setDate(snapshot.getAsOfDate() == null ? "-" : snapshot.getAsOfDate().toString());
                point.setDateLabel(snapshot.getAsOfDate() == null ? "-" : snapshot.getAsOfDate().format(CHART_DATE_FORMATTER));
                point.setProfitRate(profitRate);
                point.setProfitRateText(ViewFormatUtils.formatSignedPercentAlready(profitRate));
                point.setProfitClass(ProfitClassUtils.toProfitClass(profitRate));
                point.setBarHeight(toChartBarHeight(profitRate, maxAbsRate));

                points.add(point);
            }

            result.put(entry.getKey(), points);
        }

        return result;
    }

    private BigDecimal resolveProfitRate(ConnectorPositionSnapshot snapshot) {
        BigDecimal raw = snapshot.getEvalProfitRate();

        BigDecimal recalculated = safeRate(
                nvl(snapshot.getEvalProfit()),
                nvl(snapshot.getBuyAmount())
        );

        if (raw == null) {
            return recalculated;
        }

        // raw가 0.8395 형태(=83.95%)인지,
        // 이미 83.95 형태인지 자동 판별
        BigDecimal rawAsPercent = raw.multiply(HUNDRED);

        BigDecimal diffRaw = raw.subtract(recalculated).abs();
        BigDecimal diffPercent = rawAsPercent.subtract(recalculated).abs();

        return diffPercent.compareTo(diffRaw) < 0 ? rawAsPercent : raw;
    }

    private int toChartBarHeight(BigDecimal rate, BigDecimal maxAbsRate) {
        if (rate == null || maxAbsRate == null || maxAbsRate.compareTo(BigDecimal.ZERO) == 0) {
            return 8;
        }

        BigDecimal normalized = rate.abs()
                .multiply(BigDecimal.valueOf(100))
                .divide(maxAbsRate, 4, RoundingMode.HALF_UP);

        int height = normalized.setScale(0, RoundingMode.HALF_UP).intValue();

        if (height < 8) {
            return 8;
        }

        if (height > 100) {
            return 100;
        }

        return height;
    }

    private String normalizeTickerCode(String tickerCode) {
        if (tickerCode == null) {
            return "";
        }

        return tickerCode.trim();
    }

    private void normalizeDetailSummary(
            PositionDetailSummaryDTO summary,
            List<PositionAgencyReportDTO> reports,
            List<PositionNewsDTO> news
    ) {
        if (summary == null) {
            return;
        }

        List<PositionAgencyReportDTO> safeReports =
                reports == null ? new ArrayList<>() : reports;

        List<PositionNewsDTO> safeNews =
                news == null ? new ArrayList<>() : news;

        long reportCount = safeReports.size();

        BigDecimal avgRecommendationScore = safeReports.stream()
                .map(PositionAgencyReportDTO::getRecommendationScore)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long recommendationScoreCount = safeReports.stream()
                .map(PositionAgencyReportDTO::getRecommendationScore)
                .filter(value -> value != null)
                .count();

        if (recommendationScoreCount > 0) {
            avgRecommendationScore = avgRecommendationScore.divide(
                    BigDecimal.valueOf(recommendationScoreCount),
                    4,
                    RoundingMode.HALF_UP
            );
        } else {
            avgRecommendationScore = null;
        }

        BigDecimal avgTargetPrice = safeReports.stream()
                .map(PositionAgencyReportDTO::getTargetPrice)
                .filter(value -> value != null)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long targetPriceCount = safeReports.stream()
                .map(PositionAgencyReportDTO::getTargetPrice)
                .filter(value -> value != null)
                .count();

        if (targetPriceCount > 0) {
            avgTargetPrice = avgTargetPrice.divide(
                    BigDecimal.valueOf(targetPriceCount),
                    2,
                    RoundingMode.HALF_UP
            );
        } else {
            avgTargetPrice = null;
        }

        long newsCount = safeNews.size();

        BigDecimal avgSentimentScore = safeNews.stream()
                .map(PositionNewsDTO::getSentimentScore)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long sentimentCount = safeNews.stream()
                .map(PositionNewsDTO::getSentimentScore)
                .filter(value -> value != null)
                .count();

        if (sentimentCount > 0) {
            avgSentimentScore = avgSentimentScore.divide(
                    BigDecimal.valueOf(sentimentCount),
                    4,
                    RoundingMode.HALF_UP
            );
        } else {
            avgSentimentScore = null;
        }

        BigDecimal avgConfidenceScore = safeNews.stream()
                .map(PositionNewsDTO::getConfidenceScore)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long confidenceCount = safeNews.stream()
                .map(PositionNewsDTO::getConfidenceScore)
                .filter(value -> value != null)
                .count();

        if (confidenceCount > 0) {
            avgConfidenceScore = avgConfidenceScore.divide(
                    BigDecimal.valueOf(confidenceCount),
                    4,
                    RoundingMode.HALF_UP
            );
        } else {
            avgConfidenceScore = null;
        }

        long positiveCount = safeNews.stream()
                .filter(item -> "positive".equalsIgnoreCase(item.getSentimentLabel()))
                .count();

        long negativeCount = safeNews.stream()
                .filter(item -> "negative".equalsIgnoreCase(item.getSentimentLabel()))
                .count();

        long neutralCount = Math.max(0, newsCount - positiveCount - negativeCount);

        summary.setReportCount(reportCount);
        summary.setAvgRecommendationScore(avgRecommendationScore);
        summary.setAvgTargetPrice(avgTargetPrice);

        summary.setNewsCount(newsCount);
        summary.setAvgSentimentScore(avgSentimentScore);
        summary.setAvgConfidenceScore(avgConfidenceScore);

        summary.setPositiveNewsCount(positiveCount);
        summary.setNegativeNewsCount(negativeCount);
        summary.setNeutralNewsCount(neutralCount);

        summary.setReportCountText(ViewFormatUtils.formatInteger(reportCount) + "건");
        summary.setAvgRecommendationScoreText(
                avgRecommendationScore == null
                        ? "-"
                        : ViewFormatUtils.formatDecimal2(avgRecommendationScore) + "점"
        );
        summary.setAvgTargetPriceText(
                avgTargetPrice == null
                        ? "-"
                        : ViewFormatUtils.formatMoney(avgTargetPrice)
        );

        summary.setNewsCountText(ViewFormatUtils.formatInteger(newsCount) + "건");
        summary.setAvgSentimentScoreText(
                avgSentimentScore == null
                        ? "-"
                        : ViewFormatUtils.formatDecimal2(avgSentimentScore) + "점"
        );
        summary.setAvgConfidenceScoreText(
                avgConfidenceScore == null
                        ? "-"
                        : ViewFormatUtils.formatDecimal2(avgConfidenceScore)
        );

        summary.setPositiveNewsCountText(ViewFormatUtils.formatInteger(positiveCount) + "건");
        summary.setNegativeNewsCountText(ViewFormatUtils.formatInteger(negativeCount) + "건");
        summary.setNeutralNewsCountText(ViewFormatUtils.formatInteger(neutralCount) + "건");
    }

}
