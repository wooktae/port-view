package my.portfolio.port_view.repository;

import my.portfolio.port_view.dto.position.PositionAgencyReportDTO;
import my.portfolio.port_view.dto.position.PositionDetailSummaryDTO;
import my.portfolio.port_view.dto.position.PositionNewsDTO;
import my.portfolio.port_view.util.ViewFormatUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Repository
public class PositionInsightRepository {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final JdbcTemplate jdbcTemplate;

    public PositionInsightRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PositionDetailSummaryDTO findSummary(String tickerCode, LocalDate startDate, int lookbackDays) {
        PositionDetailSummaryDTO summary = new PositionDetailSummaryDTO();
        summary.setLookbackDays(lookbackDays);

        fillReportSummary(summary, tickerCode, startDate);
        fillNewsSummary(summary, tickerCode, startDate);

        return summary;
    }

    public List<PositionAgencyReportDTO> findRecentReports(
            String tickerCode,
            LocalDate startDate,
            int limit
    ) {
        String sql = """
                SELECT
                    id,
                    agency_name,
                    title,
                    content,
                    recommendation,
                    target_price,
                    publish_date,
                    CASE
                        WHEN recommendation IS NULL OR btrim(recommendation) = '' THEN NULL
                        WHEN lower(recommendation) LIKE '%buy%' OR recommendation LIKE '%매수%' THEN 5.0
                        WHEN lower(recommendation) LIKE '%outperform%' OR lower(recommendation) LIKE '%trading%' THEN 4.0
                        WHEN lower(recommendation) LIKE '%hold%' OR lower(recommendation) LIKE '%neutral%' OR recommendation LIKE '%중립%' THEN 3.0
                        WHEN lower(recommendation) LIKE '%reduce%' THEN 2.0
                        WHEN lower(recommendation) LIKE '%sell%' OR recommendation LIKE '%매도%' THEN 1.0
                        ELSE NULL
                    END AS recommendation_score
                FROM interest_agency_raw
                WHERE ticker_code = ?
                  AND publish_date >= ?
                ORDER BY publish_date DESC NULLS LAST, id DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapAgencyReport(rs),
                tickerCode,
                startDate,
                limit
        );
    }

    public List<PositionNewsDTO> findRecentNews(
            String tickerCode,
            LocalDate startDate,
            int limit
    ) {
        String sql = """
                SELECT
                    n.id,
                    n.source,
                    n.title,
                    n.url,
                    n.published_at,
                    a.sentiment_score,
                    a.confidence_score,
                    a.sentiment_label,
                    a.keywords::text AS keywords_text
                FROM pre_news_analysis a
                JOIN interest_news_raw n
                  ON n.id = a.news_id
                WHERE a.ticker_code = ?
                  AND COALESCE(a.published_date, DATE(a.published_at AT TIME ZONE 'Asia/Seoul')) >= ?
                ORDER BY COALESCE(a.published_at, n.published_at) DESC NULLS LAST, n.id DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapNews(rs),
                tickerCode,
                startDate,
                limit
        );
    }

    private void fillReportSummary(
            PositionDetailSummaryDTO summary,
            String tickerCode,
            LocalDate startDate
    ) {
        String sql = """
                SELECT
                    COUNT(*) AS report_count,
                    AVG(target_price)::numeric AS avg_target_price,
                    AVG(recommendation_score)::numeric AS avg_recommendation_score
                FROM (
                    SELECT
                        target_price,
                        CASE
                            WHEN recommendation IS NULL OR btrim(recommendation) = '' THEN NULL
                            WHEN lower(recommendation) LIKE '%buy%' OR recommendation LIKE '%매수%' THEN 5.0
                            WHEN lower(recommendation) LIKE '%outperform%' OR lower(recommendation) LIKE '%trading%' THEN 4.0
                            WHEN lower(recommendation) LIKE '%hold%' OR lower(recommendation) LIKE '%neutral%' OR recommendation LIKE '%중립%' THEN 3.0
                            WHEN lower(recommendation) LIKE '%reduce%' THEN 2.0
                            WHEN lower(recommendation) LIKE '%sell%' OR recommendation LIKE '%매도%' THEN 1.0
                            ELSE NULL
                        END AS recommendation_score
                    FROM interest_agency_raw
                    WHERE ticker_code = ?
                      AND publish_date >= ?
                ) x
                """;

        jdbcTemplate.query(
                sql,
                rs -> {
                    if (rs.next()) {
                        BigDecimal avgScore = rs.getBigDecimal("avg_recommendation_score");
                        BigDecimal avgTargetPrice = rs.getBigDecimal("avg_target_price");

                        summary.setReportCount(rs.getLong("report_count"));
                        summary.setAvgRecommendationScore(avgScore);
                        summary.setAvgTargetPrice(avgTargetPrice);

                        summary.setReportCountText(ViewFormatUtils.formatInteger(summary.getReportCount()) + "건");
                        summary.setAvgRecommendationScoreText(avgScore == null ? "-" : ViewFormatUtils.formatDecimal2(avgScore) + "점");
                        summary.setAvgTargetPriceText(avgTargetPrice == null ? "-" : ViewFormatUtils.formatMoney(avgTargetPrice));
                    }
                },
                tickerCode,
                startDate
        );
    }

    private void fillNewsSummary(
            PositionDetailSummaryDTO summary,
            String tickerCode,
            LocalDate startDate
    ) {
        String sql = """
                SELECT
                    COUNT(*) AS news_count,
                    AVG(sentiment_score)::numeric AS avg_sentiment_score,
                    AVG(confidence_score)::numeric AS avg_confidence_score,
                    SUM(CASE WHEN lower(COALESCE(sentiment_label, '')) = 'positive' THEN 1 ELSE 0 END) AS positive_count,
                    SUM(CASE WHEN lower(COALESCE(sentiment_label, '')) = 'negative' THEN 1 ELSE 0 END) AS negative_count,
                    SUM(CASE
                            WHEN sentiment_label IS NULL
                              OR lower(sentiment_label) NOT IN ('positive', 'negative')
                            THEN 1 ELSE 0
                        END) AS neutral_count
                FROM pre_news_analysis
                WHERE ticker_code = ?
                  AND COALESCE(published_date, DATE(published_at AT TIME ZONE 'Asia/Seoul')) >= ?
                """;

        jdbcTemplate.query(
                sql,
                rs -> {
                    if (rs.next()) {
                        BigDecimal avgSentiment = rs.getBigDecimal("avg_sentiment_score");
                        BigDecimal avgConfidence = rs.getBigDecimal("avg_confidence_score");

                        summary.setNewsCount(rs.getLong("news_count"));
                        summary.setAvgSentimentScore(avgSentiment);
                        summary.setAvgConfidenceScore(avgConfidence);
                        summary.setPositiveNewsCount(rs.getLong("positive_count"));
                        summary.setNegativeNewsCount(rs.getLong("negative_count"));
                        summary.setNeutralNewsCount(rs.getLong("neutral_count"));

                        summary.setNewsCountText(ViewFormatUtils.formatInteger(summary.getNewsCount()) + "건");
                        summary.setAvgSentimentScoreText(avgSentiment == null ? "-" : ViewFormatUtils.formatDecimal2(avgSentiment) + "점");
                        summary.setAvgConfidenceScoreText(avgConfidence == null ? "-" : ViewFormatUtils.formatDecimal2(avgConfidence));
                        summary.setPositiveNewsCountText(ViewFormatUtils.formatInteger(summary.getPositiveNewsCount()) + "건");
                        summary.setNegativeNewsCountText(ViewFormatUtils.formatInteger(summary.getNegativeNewsCount()) + "건");
                        summary.setNeutralNewsCountText(ViewFormatUtils.formatInteger(summary.getNeutralNewsCount()) + "건");
                    }
                },
                tickerCode,
                startDate
        );
    }

    private PositionAgencyReportDTO mapAgencyReport(ResultSet rs) throws java.sql.SQLException {
        PositionAgencyReportDTO dto = new PositionAgencyReportDTO();

        dto.setId(rs.getLong("id"));
        dto.setAgencyName(ViewFormatUtils.emptyIfNull(rs.getString("agency_name")));
        dto.setTitle(ViewFormatUtils.emptyIfNull(rs.getString("title")));
        dto.setContentPreview(toPreview(rs.getString("content"), 150));
        dto.setRecommendation(ViewFormatUtils.emptyIfNull(rs.getString("recommendation")));
        dto.setRecommendationScore(rs.getBigDecimal("recommendation_score"));

        long targetPrice = rs.getLong("target_price");
        dto.setTargetPrice(rs.wasNull() ? null : targetPrice);

        dto.setPublishDate(rs.getObject("publish_date", LocalDate.class));

        dto.setRecommendationScoreText(
                dto.getRecommendationScore() == null
                        ? "-"
                        : ViewFormatUtils.formatDecimal2(dto.getRecommendationScore()) + "점"
        );
        dto.setTargetPriceText(dto.getTargetPrice() == null ? "-" : ViewFormatUtils.formatMoney(dto.getTargetPrice()));
        dto.setPublishDateText(ViewFormatUtils.formatDate(dto.getPublishDate()));

        return dto;
    }

    private PositionNewsDTO mapNews(ResultSet rs) throws java.sql.SQLException {
        PositionNewsDTO dto = new PositionNewsDTO();

        dto.setId(rs.getLong("id"));
        dto.setSource(ViewFormatUtils.emptyIfNull(rs.getString("source")));
        dto.setTitle(ViewFormatUtils.emptyIfNull(rs.getString("title")));
        dto.setUrl(rs.getString("url"));

        dto.setSentimentScore(rs.getBigDecimal("sentiment_score"));
        dto.setConfidenceScore(rs.getBigDecimal("confidence_score"));
        dto.setSentimentLabel(ViewFormatUtils.emptyIfNull(rs.getString("sentiment_label")));
        dto.setKeywordsText(cleanKeywords(rs.getString("keywords_text")));

        Timestamp publishedAt = rs.getTimestamp("published_at");
        if (publishedAt != null) {
            dto.setPublishedAt(OffsetDateTime.ofInstant(publishedAt.toInstant(), SEOUL_ZONE));
        }

        dto.setSentimentScoreText(
                dto.getSentimentScore() == null
                        ? "-"
                        : ViewFormatUtils.formatDecimal2(dto.getSentimentScore()) + "점"
        );
        dto.setConfidenceScoreText(
                dto.getConfidenceScore() == null
                        ? "-"
                        : ViewFormatUtils.formatDecimal2(dto.getConfidenceScore())
        );
        dto.setSentimentLabelText(toSentimentLabelKo(dto.getSentimentLabel()));
        dto.setPublishedAtText(ViewFormatUtils.formatDateTime(dto.getPublishedAt()));

        return dto;
    }

    private String toPreview(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "-";
        }

        String normalized = text
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("\t", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (normalized.length() <= maxLength) {
            return normalized;
        }

        return normalized.substring(0, maxLength) + "...";
    }

    private String cleanKeywords(String raw) {
        if (raw == null || raw.isBlank()) {
            return "-";
        }

        return raw
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .replace("'", "")
                .trim();
    }

    private String toSentimentLabelKo(String label) {
        if (label == null || label.isBlank() || "-".equals(label)) {
            return "중립";
        }

        return switch (label.toLowerCase()) {
            case "positive" -> "긍정";
            case "negative" -> "부정";
            case "neutral" -> "중립";
            default -> label;
        };
    }
}