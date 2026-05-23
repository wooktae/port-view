package my.portfolio.port_view.dto.dailybatch;

import my.portfolio.port_view.util.StrategyExecutionLabelUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record IntradayPositionCheckDto(
        Long id,
        OffsetDateTime checkTs,
        Long positionStateId,
        String tickerCode,
        String stockName,
        BigDecimal currentPrice,
        BigDecimal pnlRate,
        BigDecimal hardStopRate,
        Boolean shouldStop,
        String stopReason,
        Long createdExecutionOrderId,
        Integer warningCount
) {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String checkTsText() {
        if (checkTs == null) {
            return "-";
        }

        return checkTs.atZoneSameInstant(KST).format(DATE_TIME_FORMATTER);
    }
    
    public String shouldStopLabel() {
        return Boolean.TRUE.equals(shouldStop) ? "손절 후보" : "정상";
    }

    public String shouldStopClass() {
        return Boolean.TRUE.equals(shouldStop) ? "danger" : "safe";
    }

    public String stopReasonLabel() {
        return StrategyExecutionLabelUtils.sellReasonLabel(stopReason);
    }

    public String warningLabel() {
        int count = warningCount == null ? 0 : warningCount;
        return count <= 0 ? "없음" : count + "건";
    }

    public String executionOrderText() {
        return createdExecutionOrderId == null ? "-" : "#" + createdExecutionOrderId;
    }
}