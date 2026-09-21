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
}
