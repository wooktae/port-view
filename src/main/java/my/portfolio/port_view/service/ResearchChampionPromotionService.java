package my.portfolio.port_view.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.model.ContainerProperties;
import software.amazon.awssdk.services.batch.model.DescribeJobDefinitionsRequest;
import software.amazon.awssdk.services.batch.model.JobDefinition;
import software.amazon.awssdk.services.batch.model.RegisterJobDefinitionRequest;
import software.amazon.awssdk.services.batch.model.RegisterJobDefinitionResponse;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.DescribeStateMachineRequest;
import software.amazon.awssdk.services.sfn.model.ExecutionStatus;
import software.amazon.awssdk.services.sfn.model.ListExecutionsRequest;
import software.amazon.awssdk.services.sfn.model.UpdateStateMachineRequest;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResearchChampionPromotionService {

    private static final Region REGION = Region.AP_NORTHEAST_2;

    private static final String PRODUCTION_STATE_MACHINE =
            "portfolio-paper-daily-step1-17-approval";

    private static final String STEP4_ONLY_STATE_MACHINE =
            "portfolio-paper-daily-step4-only";

    private static final String RESEARCH_STEP =
            "Step4_SubmitBacktestResearch";

    private static final String STRATEGY_CONFIG_OPTION =
            "--strategy-config-version";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PromotionPlan buildPromotionPlan(String targetVersion) {
        if (targetVersion == null || targetVersion.isBlank()) {
            throw new IllegalArgumentException(
                    "Target Strategy Config Version is required");
        }

        String normalizedTarget = targetVersion.trim();

        try (SfnClient sfnClient = SfnClient.builder()
                .region(REGION)
                .build();
             BatchClient batchClient = BatchClient.builder()
                     .region(REGION)
                     .build()) {

            String stateMachineArn =
                    resolveStateMachineArn(
                            sfnClient,
                            PRODUCTION_STATE_MACHINE
                    );

            String definition =
                    describeDefinition(
                            sfnClient,
                            stateMachineArn
                    );

            String currentJobDefinitionArn =
                    extractJobDefinitionArn(definition);

            if (currentJobDefinitionArn.isBlank()) {
                throw new IllegalStateException(
                        "Research operating JobDefinition not found");
            }

            List<JobDefinition> jobDefinitions =
                    batchClient.describeJobDefinitions(
                            DescribeJobDefinitionsRequest.builder()
                                    .jobDefinitions(currentJobDefinitionArn)
                                    .build()
                    ).jobDefinitions();

            if (jobDefinitions.size() != 1) {
                throw new IllegalStateException(
                        "Expected exactly one operating JobDefinition");
            }

            JobDefinition currentJob = jobDefinitions.get(0);

            List<String> currentCommand =
                    currentJob.containerProperties().command();

            int optionIndex =
                    currentCommand.indexOf(STRATEGY_CONFIG_OPTION);

            if (optionIndex < 0 ||
                    optionIndex + 1 >= currentCommand.size()) {
                throw new IllegalStateException(
                        "Operating Strategy Config Version not found");
            }

            String currentVersion =
                    currentCommand.get(optionIndex + 1);

            if (normalizedTarget.equals(currentVersion)) {
                throw new IllegalStateException(
                        "Target version is already operating: "
                                + normalizedTarget);
            }

            List<String> targetCommand =
                    new ArrayList<>(currentCommand);

            targetCommand.set(
                    optionIndex + 1,
                    normalizedTarget
            );

            return new PromotionPlan(
                    currentJobDefinitionArn,
                    currentVersion,
                    normalizedTarget,
                    List.copyOf(currentCommand),
                    List.copyOf(targetCommand)
            );

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to build Research champion promotion plan",
                    e
            );
        }
    }

    public RegisteredPromotion registerTargetRevision(String targetVersion) {
        PromotionPlan plan =
                buildPromotionPlan(targetVersion);

        return registerTargetRevision(plan);
    }

    public PromotionResult promoteChampion(String targetVersion) {
        PromotionPlan plan =
                buildPromotionPlan(targetVersion);

        try (SfnClient sfnClient = SfnClient.builder()
                .region(REGION)
                .build()) {

            String productionArn =
                    resolveStateMachineArn(
                            sfnClient,
                            PRODUCTION_STATE_MACHINE
                    );

            String step4OnlyArn =
                    resolveStateMachineArn(
                            sfnClient,
                            STEP4_ONLY_STATE_MACHINE
                    );

            verifyNoRunningExecutions(
                    sfnClient,
                    productionArn,
                    PRODUCTION_STATE_MACHINE
            );

            verifyNoRunningExecutions(
                    sfnClient,
                    step4OnlyArn,
                    STEP4_ONLY_STATE_MACHINE
            );

            String productionDefinition =
                    describeDefinition(
                            sfnClient,
                            productionArn
                    );

            String step4OnlyDefinition =
                    describeDefinition(
                            sfnClient,
                            step4OnlyArn
                    );

            verifyExpectedJobDefinition(
                    productionDefinition,
                    plan.currentJobDefinitionArn(),
                    PRODUCTION_STATE_MACHINE
            );

            verifyExpectedJobDefinition(
                    step4OnlyDefinition,
                    plan.currentJobDefinitionArn(),
                    STEP4_ONLY_STATE_MACHINE
            );

            RegisteredPromotion registered =
                    registerTargetRevision(plan);

            String newProductionDefinition =
                    replaceJobDefinition(
                            productionDefinition,
                            registered.newJobDefinitionArn()
                    );

            String newStep4OnlyDefinition =
                    replaceJobDefinition(
                            step4OnlyDefinition,
                            registered.newJobDefinitionArn()
                    );

            boolean productionUpdated = false;
            boolean step4OnlyUpdated = false;

            try {
                sfnClient.updateStateMachine(
                        UpdateStateMachineRequest.builder()
                                .stateMachineArn(productionArn)
                                .definition(newProductionDefinition)
                                .build()
                );

                productionUpdated = true;

                sfnClient.updateStateMachine(
                        UpdateStateMachineRequest.builder()
                                .stateMachineArn(step4OnlyArn)
                                .definition(newStep4OnlyDefinition)
                                .build()
                );

                step4OnlyUpdated = true;

                verifyExpectedJobDefinition(
                        describeDefinition(
                                sfnClient,
                                productionArn
                        ),
                        registered.newJobDefinitionArn(),
                        PRODUCTION_STATE_MACHINE
                );

                verifyExpectedJobDefinition(
                        describeDefinition(
                                sfnClient,
                                step4OnlyArn
                        ),
                        registered.newJobDefinitionArn(),
                        STEP4_ONLY_STATE_MACHINE
                );

            } catch (RuntimeException promotionFailure) {

                RuntimeException rollbackFailure = null;

                if (step4OnlyUpdated) {
                    try {
                        sfnClient.updateStateMachine(
                                UpdateStateMachineRequest.builder()
                                        .stateMachineArn(step4OnlyArn)
                                        .definition(step4OnlyDefinition)
                                        .build()
                        );
                    } catch (RuntimeException e) {
                        rollbackFailure = e;
                    }
                }

                if (productionUpdated) {
                    try {
                        sfnClient.updateStateMachine(
                                UpdateStateMachineRequest.builder()
                                        .stateMachineArn(productionArn)
                                        .definition(productionDefinition)
                                        .build()
                        );
                    } catch (RuntimeException e) {
                        if (rollbackFailure == null) {
                            rollbackFailure = e;
                        } else {
                            rollbackFailure.addSuppressed(e);
                        }
                    }
                }

                if (rollbackFailure != null) {
                    promotionFailure.addSuppressed(
                            rollbackFailure
                    );
                }

                throw promotionFailure;
            }

            return new PromotionResult(
                    registered.currentVersion(),
                    registered.targetVersion(),
                    registered.previousJobDefinitionArn(),
                    registered.newJobDefinitionArn(),
                    registered.newRevision()
            );
        }
    }

    private RegisteredPromotion registerTargetRevision(
            PromotionPlan plan
    ) {
        try (BatchClient batchClient = BatchClient.builder()
                .region(REGION)
                .build()) {

            List<JobDefinition> currentJobs =
                    batchClient.describeJobDefinitions(
                            DescribeJobDefinitionsRequest.builder()
                                    .jobDefinitions(
                                            plan.currentJobDefinitionArn()
                                    )
                                    .build()
                    ).jobDefinitions();

            if (currentJobs.size() != 1) {
                throw new IllegalStateException(
                        "Expected exactly one operating JobDefinition");
            }

            JobDefinition currentJob =
                    currentJobs.get(0);

            ContainerProperties currentContainer =
                    currentJob.containerProperties();

            ContainerProperties targetContainer =
                    currentContainer.toBuilder()
                            .command(plan.targetCommand())
                            .build();

            RegisterJobDefinitionRequest.Builder request =
                    RegisterJobDefinitionRequest.builder()
                            .jobDefinitionName(
                                    currentJob.jobDefinitionName()
                            )
                            .type(currentJob.type())
                            .containerProperties(targetContainer)
                            .platformCapabilities(
                                    currentJob.platformCapabilities()
                            );

            if (currentJob.parameters() != null &&
                    !currentJob.parameters().isEmpty()) {
                request.parameters(
                        currentJob.parameters()
                );
            }

            if (currentJob.timeout() != null) {
                request.timeout(
                        currentJob.timeout()
                );
            }

            if (currentJob.retryStrategy() != null) {
                request.retryStrategy(
                        currentJob.retryStrategy()
                );
            }

            if (currentJob.propagateTags() != null) {
                request.propagateTags(
                        currentJob.propagateTags()
                );
            }

            if (currentJob.schedulingPriority() != null) {
                request.schedulingPriority(
                        currentJob.schedulingPriority()
                );
            }

            RegisterJobDefinitionResponse response =
                    batchClient.registerJobDefinition(
                            request.build()
                    );

            if (response.jobDefinitionArn() == null ||
                    response.jobDefinitionArn().isBlank()) {
                throw new IllegalStateException(
                        "Registered JobDefinition ARN is empty");
            }

            return new RegisteredPromotion(
                    plan.currentVersion(),
                    plan.targetVersion(),
                    plan.currentJobDefinitionArn(),
                    response.jobDefinitionArn(),
                    response.revision()
            );
        }
    }

    private String resolveStateMachineArn(
            SfnClient client,
            String stateMachineName
    ) {
        return client.listStateMachines()
                .stateMachines()
                .stream()
                .filter(sm ->
                        stateMachineName.equals(sm.name()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "State machine not found: "
                                        + stateMachineName))
                .stateMachineArn();
    }

    private String describeDefinition(
            SfnClient client,
            String stateMachineArn
    ) {
        return client.describeStateMachine(
                DescribeStateMachineRequest.builder()
                        .stateMachineArn(stateMachineArn)
                        .build()
        ).definition();
    }

    private void verifyNoRunningExecutions(
            SfnClient client,
            String stateMachineArn,
            String stateMachineName
    ) {
        boolean running =
                !client.listExecutions(
                        ListExecutionsRequest.builder()
                                .stateMachineArn(stateMachineArn)
                                .statusFilter(
                                        ExecutionStatus.RUNNING
                                )
                                .maxResults(1)
                                .build()
                ).executions().isEmpty();

        if (running) {
            throw new IllegalStateException(
                    "Running execution exists: "
                            + stateMachineName);
        }
    }

    private String extractJobDefinitionArn(
            String definition
    ) {
        try {
            JsonNode root =
                    objectMapper.readTree(definition);

            return root.path("States")
                    .path(RESEARCH_STEP)
                    .path("Parameters")
                    .path("JobDefinition")
                    .asText();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to read Research JobDefinition",
                    e
            );
        }
    }

    private void verifyExpectedJobDefinition(
            String definition,
            String expectedJobDefinitionArn,
            String stateMachineName
    ) {
        String actual =
                extractJobDefinitionArn(definition);

        if (!expectedJobDefinitionArn.equals(actual)) {
            throw new IllegalStateException(
                    "Unexpected Research JobDefinition. "
                            + "stateMachine="
                            + stateMachineName
                            + ", expected="
                            + expectedJobDefinitionArn
                            + ", actual="
                            + actual);
        }
    }

    private String replaceJobDefinition(
            String definition,
            String newJobDefinitionArn
    ) {
        try {
            JsonNode root =
                    objectMapper.readTree(definition);

            JsonNode step =
                    root.path("States")
                            .path(RESEARCH_STEP);

            if (!step.isObject()) {
                throw new IllegalStateException(
                        "Research Step4 not found");
            }

            JsonNode parameters =
                    step.path("Parameters");

            if (!parameters.isObject()) {
                throw new IllegalStateException(
                        "Research Step4 Parameters not found");
            }

            ((ObjectNode) parameters)
                    .put(
                            "JobDefinition",
                            newJobDefinitionArn
                    );

            return objectMapper.writeValueAsString(root);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to replace Research JobDefinition",
                    e
            );
        }
    }

    public record PromotionPlan(
            String currentJobDefinitionArn,
            String currentVersion,
            String targetVersion,
            List<String> currentCommand,
            List<String> targetCommand
    ) {
    }

    public record RegisteredPromotion(
            String currentVersion,
            String targetVersion,
            String previousJobDefinitionArn,
            String newJobDefinitionArn,
            Integer newRevision
    ) {
    }

    public record PromotionResult(
            String previousVersion,
            String operatingVersion,
            String previousJobDefinitionArn,
            String operatingJobDefinitionArn,
            Integer operatingRevision
    ) {
    }
}