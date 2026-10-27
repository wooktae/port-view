package my.portfolio.port_view.dto.position;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PositionProfitPointDTO {

    private String date;
    private String dateLabel;

    private BigDecimal profitRate;
    private String profitRateText;

    /**
     * profit / loss / neutral
     */
    private String profitClass;

    /**
     * CSS height value.
     * Example: 42 -> height: 42%
     */
    private int barHeight;
}