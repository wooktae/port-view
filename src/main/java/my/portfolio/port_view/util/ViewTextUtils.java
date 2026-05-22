package my.portfolio.port_view.util;

public final class ViewTextUtils {

    private ViewTextUtils() {
    }

    public static String nvl(String value) {
        return value == null ? "" : value;
    }

    public static String blankTo(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String upper(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}