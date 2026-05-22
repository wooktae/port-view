package my.portfolio.port_view.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PositionAgencyReportDTO {

    private Long id;

    private String agencyName;
    private String title;
    private String contentPreview;

    private String recommendation;
    private BigDecimal recommendationScore;

    private Long targetPrice;
    private LocalDate publishDate;

    private String recommendationScoreText;
    private String targetPriceText;
    private String publishDateText;
}