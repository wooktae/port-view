package my.portfolio.port_view.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DashboardMetricDTO {

    private String title;
    private String value;
    private String subtitle;

    /**
     * primary / cash / stock / profit / loss / neutral
     */
    private String type;

    public DashboardMetricDTO(String title, String value, String subtitle, String type) {
        this.title = title;
        this.value = value;
        this.subtitle = subtitle;
        this.type = type;
    }
}