package my.portfolio.port_view.dto;

public record ReportInsightDto(
        String type,
        String title,
        String description,
        String badgeLabel,
        String badgeClass
) {
}