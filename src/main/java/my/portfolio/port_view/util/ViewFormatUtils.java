package my.portfolio.port_view.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

public final class ViewFormatUtils {

    private static final String EMPTY_TEXT = "-";

    private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("#,##0");
    private static final DecimalFormat DECIMAL_2_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat PERCENT_2_FORMAT = new DecimalFormat("#,##0.00");
    private static final String SEOUL_ZONE = "Asia/Seoul";

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter DATETIME_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter TIME_FORMAT =
        DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final DateTimeFormatter MONTH_DAY_TIME_FORMAT =
        DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");

    private ViewFormatUtils() {
    }

    public static String emptyIfNull(Object value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        String text = String.valueOf(value);
        if (text.isBlank()) {
            return EMPTY_TEXT;
        }

        return text;
    }

    public static String formatInteger(Number value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        return INTEGER_FORMAT.format(value);
    }

    public static String formatDecimal2(Number value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        return DECIMAL_2_FORMAT.format(value);
    }

    public static String formatMoney(Number value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        return INTEGER_FORMAT.format(value) + "원";
    }

    public static String formatMoneyWithoutUnit(Number value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        return INTEGER_FORMAT.format(value);
    }

    public static String formatQty(Number value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        return INTEGER_FORMAT.format(value) + "주";
    }

    public static String formatQtyWithoutUnit(Number value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        return INTEGER_FORMAT.format(value);
    }

    public static String formatPercent(Number value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        BigDecimal percent = toBigDecimal(value).multiply(BigDecimal.valueOf(100));
        return PERCENT_2_FORMAT.format(percent) + "%";
    }

    public static String formatPercentAlready(Number value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        return PERCENT_2_FORMAT.format(value) + "%";
    }

    public static String formatSignedPercent(Number value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        BigDecimal percent = toBigDecimal(value).multiply(BigDecimal.valueOf(100));
        String formatted = PERCENT_2_FORMAT.format(percent.abs()) + "%";

        if (percent.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + formatted;
        }

        if (percent.compareTo(BigDecimal.ZERO) < 0) {
            return "-" + formatted;
        }

        return "0.00%";
    }

    public static String formatSignedPercentAlready(Number value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        BigDecimal percent = toBigDecimal(value);
        String formatted = PERCENT_2_FORMAT.format(percent.abs()) + "%";

        if (percent.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + formatted;
        }

        if (percent.compareTo(BigDecimal.ZERO) < 0) {
            return "-" + formatted;
        }

        return "0.00%";
    }

    public static String formatSignedMoney(Number value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        BigDecimal amount = toBigDecimal(value);
        String formatted = INTEGER_FORMAT.format(amount.abs()) + "원";

        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + formatted;
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return "-" + formatted;
        }

        return "0원";
    }

    public static String formatDate(LocalDate value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        return value.format(DATE_FORMAT);
    }

    public static String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        return value.format(DATETIME_FORMAT);
    }

    public static String formatDateTime(OffsetDateTime value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        return value
                .atZoneSameInstant(ZoneId.of(SEOUL_ZONE))
                .format(DATETIME_FORMAT);
    }

    public static String formatTime(OffsetDateTime value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        return value
                .atZoneSameInstant(ZoneId.of(SEOUL_ZONE))
                .format(TIME_FORMAT);
    }

    public static String formatMonthDayTime(OffsetDateTime value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        return value
                .atZoneSameInstant(ZoneId.of(SEOUL_ZONE))
                .format(MONTH_DAY_TIME_FORMAT);
    }

    public static String formatBooleanYn(Boolean value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        return value ? "Y" : "N";
    }

    public static String formatBooleanKo(Boolean value) {
        if (value == null) {
            return EMPTY_TEXT;
        }

        return value ? "예" : "아니오";
    }

    public static BigDecimal round2(Number value) {
        if (value == null) {
            return null;
        }

        return toBigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal toBigDecimal(Number value) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }

        return new BigDecimal(value.toString());
    }
}