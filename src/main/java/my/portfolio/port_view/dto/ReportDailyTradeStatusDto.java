package my.portfolio.port_view.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReportDailyTradeStatusDto(
        LocalDate date,

        String marketSignal,
        String marketSignalLabel,
        String marketSignalClass,

        Integer positionCount,

        BigDecimal dailyReturn,
        BigDecimal cumulativeReturn,

        Integer buyCount,
        Integer sellCount,
        BigDecimal avgSellReturn,
        Integer winSellCount,

        String buyNames,
        String sellNames,

        String dayStatusLabel,
        String dayStatusClass
) {
}