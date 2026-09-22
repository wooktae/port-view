package my.portfolio.port_view.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.context.i18n.LocaleContextHolder;

public final class ViewMessages {

    private static final String BUNDLE_NAME = "messages";

    private ViewMessages() {
    }

    public static String text(String key, Object... arguments) {
        Locale requestLocale = LocaleContextHolder.getLocale();
        Locale bundleLocale = Locale.KOREAN.getLanguage().equals(requestLocale.getLanguage())
                ? Locale.KOREAN
                : Locale.ROOT;
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, bundleLocale);
        return MessageFormat.format(bundle.getString(key), arguments);
    }

    /**
     * Formats a source-owned Stock name for the current View locale without translating or mutating it.
     * English uses the ticker and original name together; Korean keeps the original name.
     */
    public static String stockDisplayName(String tickerCode, String originalName) {
        boolean hasTicker = hasDisplayValue(tickerCode);
        boolean hasOriginalName = hasOriginalStockName(originalName);

        if (Locale.KOREAN.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
            if (hasOriginalName) {
                return originalName;
            }
            return hasTicker ? tickerCode : "-";
        }

        if (hasTicker && hasOriginalName) {
            return tickerCode + " · " + originalName;
        }
        if (hasTicker) {
            return tickerCode;
        }
        return hasOriginalName ? originalName : "-";
    }

    public static boolean hasOriginalStockName(String originalName) {
        return hasDisplayValue(originalName);
    }

    private static boolean hasDisplayValue(String value) {
        return value != null && !value.trim().isEmpty() && !"-".equals(value.trim());
    }
}
