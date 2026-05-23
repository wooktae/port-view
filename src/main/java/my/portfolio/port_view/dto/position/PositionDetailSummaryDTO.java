package my.portfolio.port_view.dto.position;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PositionDetailSummaryDTO {

    private int lookbackDays;

    private long reportCount;
    private BigDecimal avgRecommendationScore;
    private BigDecimal avgTargetPrice;

    private String reportCountText;
    private String avgRecommendationScoreText;
    private String avgTargetPriceText;

    private long newsCount;
    private BigDecimal avgSentimentScore;
    private BigDecimal avgConfidenceScore;

    private long positiveNewsCount;
    private long negativeNewsCount;
    private long neutralNewsCount;

    private String newsCountText;
    private String avgSentimentScoreText;
    private String avgConfidenceScoreText;

    private String positiveNewsCountText;
    private String negativeNewsCountText;
    private String neutralNewsCountText;
}