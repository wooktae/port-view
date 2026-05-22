package my.portfolio.port_view.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PositionNewsDTO {

    private Long id;

    private String source;
    private String title;
    private String url;

    private BigDecimal sentimentScore;
    private BigDecimal confidenceScore;
    private String sentimentLabel;
    private String keywordsText;

    private OffsetDateTime publishedAt;

    private String sentimentScoreText;
    private String confidenceScoreText;
    private String sentimentLabelText;
    private String publishedAtText;
}