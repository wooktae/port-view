package my.portfolio.port_view.dto.strategy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StrategyMarketBlockReasonDto(
        LocalDate featureDate,
        String marketSignal,
        String planRiskRegime,
        String featureRiskRegime,

        BigDecimal marketRegimeScore,
        BigDecimal flowPressureScore,
        BigDecimal breadthPressureScore,
        BigDecimal marketFlowStrengthScore,
        BigDecimal marketForeignNetRatio5d,
        BigDecimal marketInstitutionNetRatio5d,
        BigDecimal globalRiskScore,
        BigDecimal macroPressureScore,

        String headline,
        String summaryText,
        List<String> reasonLines
) {
    public boolean hasData() {
        return featureDate != null;
    }

    public boolean isBlock() {
        return "BLOCK".equalsIgnoreCase(marketSignal);
    }
}