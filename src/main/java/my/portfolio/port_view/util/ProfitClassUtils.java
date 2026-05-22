package my.portfolio.port_view.util;

import java.math.BigDecimal;

public final class ProfitClassUtils {

    private ProfitClassUtils() {
    }

    public static String toProfitClass(BigDecimal value) {
        if (value == null) {
            return "neutral";
        }

        if (value.compareTo(BigDecimal.ZERO) > 0) {
            return "profit";
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return "loss";
        }

        return "neutral";
    }
}