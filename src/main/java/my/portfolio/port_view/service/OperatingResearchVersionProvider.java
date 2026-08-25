package my.portfolio.port_view.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.model.DescribeJobDefinitionsRequest;
import software.amazon.awssdk.services.batch.model.JobDefinition;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.DescribeStateMachineRequest;

import java.util.List;

@Component
public class OperatingResearchVersionProvider {

    private static final String STATE_MACHINE_NAME =
            "portfolio-paper-daily-step1-17-approval";

    private static final String RESEARCH_STEP =
            "Step4_SubmitBacktestResearch";

    private static final String STRATEGY_CONFIG_OPTION =
            "--strategy-config-version";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getOperatingVersion() {
        Region region = Region.AP_NORTHEAST_2;

        try (SfnClient sfnClient = SfnClient.builder()
                .region(region)
                .build();
             BatchClient batchClient = BatchClient.builder()
                     .region(region)
                     .build()) {

            String stateMachineArn = sfnClient.listStateMachines().stateMachines().stream()
                    .filter(sm -> STATE_MACHINE_NAME.equals(sm.name()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Research operating state machine not found"))
                    .stateMachineArn();

            String definition = sfnClient.describeStateMachine(
                    DescribeStateMachineRequest.builder()
                            .stateMachineArn(stateMachineArn)
                            .build()
            ).definition();

            JsonNode root = objectMapper.readTree(definition);

            String jobDefinitionArn = root
                    .path("States")
                    .path(RESEARCH_STEP)
                    .path("Parameters")
                    .path("JobDefinition")
                    .asText();

            if (jobDefinitionArn.isBlank()) {
                throw new IllegalStateException(
                        "Research operating JobDefinition not found");
            }

            List<JobDefinition> jobDefinitions = batchClient.describeJobDefinitions(
                    DescribeJobDefinitionsRequest.builder()
                            .jobDefinitions(jobDefinitionArn)
                            .build()
            ).jobDefinitions();

            if (jobDefinitions.isEmpty()) {
                throw new IllegalStateException(
                        "Research operating JobDefinition cannot be described");
            }

            List<String> command =
                    jobDefinitions.get(0).containerProperties().command();

            int index = command.indexOf(STRATEGY_CONFIG_OPTION);

            if (index < 0 || index + 1 >= command.size()) {
                throw new IllegalStateException(
                        "Research strategy config version not found");
            }

            return command.get(index + 1);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to resolve Research operating strategy version", e);
        }
    }
}