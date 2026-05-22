package my.portfolio.port_view.dto;

public record DailyBatchStepOptionDto(
        Integer stepOrder,
        String stepCode,
        String stepName
) {

    public String displayLabel() {
        String no = stepOrder == null ? "-" : String.valueOf(stepOrder);
        String name = stepName == null || stepName.isBlank() ? stepCode : stepName;

        return no + ". " + name;
    }
}