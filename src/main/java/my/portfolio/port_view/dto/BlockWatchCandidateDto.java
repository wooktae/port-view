package my.portfolio.port_view.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record BlockWatchCandidateDto(
        Long id,
        Long dailyRunId,

        LocalDate runDate,
        LocalDate dataDate,

        String tickerCode,
        String stockName,

        String marketSignal,
        String watchStatus,

        BigDecimal flowScore,
        BigDecimal finalScore,
        BigDecimal infoScore,
        BigDecimal volatility20d,
        BigDecimal intradayRange,
        BigDecimal shortPressureScore,

        String watchReason,

        BigDecimal closePrice,

        BigDecimal return1d,
        BigDecimal return3d,
        BigDecimal return5d,
        BigDecimal return10d,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public String displayName() {
        if (stockName != null && !stockName.isBlank()) {
            return stockName;
        }

        return tickerCode == null ? "-" : tickerCode;
    }

    public String watchReasonLabel() {
        if ("BLOCK_STRONG_EXCEPTION_WATCH".equalsIgnoreCase(watchReason)) {
            return "BLOCK 강한 예외 관찰";
        }

        return watchReason == null || watchReason.isBlank() ? "-" : watchReason;
    }

    public boolean isWaitingReturn1d() {
        return return1d == null;
    }

    public boolean isWaitingReturn3d() {
        return return3d == null;
    }

    public boolean isWaitingReturn5d() {
        return return5d == null;
    }

    public boolean isWaitingReturn10d() {
        return return10d == null;
    }
}