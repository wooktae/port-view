package my.portfolio.port_view.service;

import lombok.RequiredArgsConstructor;
import my.portfolio.port_view.config.DailyBatchProperties;
import my.portfolio.port_view.dto.dailybatch.DailyBatchStepOptionDto;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest;
import software.amazon.awssdk.services.sfn.model.StartExecutionResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * View -> AWS Step Functions StartExecution adapter.
 *
 * Separated from the existing local-file ProcessBuilder execution path.
 * This Service does not directly execute Python source.
 */
@Service
@RequiredArgsConstructor
public class StepFunctionsDailyBatchExecutionService {

    private static final String SOURCE_PORT_VIEW = "PORT_VIEW";
    private static final String REQUESTED_BY_VIEW_BUTTON = "VIEW_BUTTON";
    private static final String REQUESTED_BY_VIEW_APPROVAL_BUTTON = "VIEW_APPROVAL_BUTTON";
    private static final int BROKER_SUBMIT_STEP_ORDER = 12;
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final DailyBatchProperties properties;
    private final DailyBatchService dailyBatchService;

    public StartExecutionResult startSafeRange(String fromStepCode, String toStepCode, String accountNo) {
        return startRange(fromStepCode, toStepCode, false, REQUESTED_BY_VIEW_BUTTON, accountNo);
    }

    public StartExecutionResult startApprovalRange(String fromStepCode, String toStepCode, String accountNo) {
        return startRange(fromStepCode, toStepCode, true, REQUESTED_BY_VIEW_APPROVAL_BUTTON, accountNo);
    }

    private StartExecutionResult startRange(
            String fromStepCode,
            String toStepCode,
            boolean allowPaperOrderExecute,
            String requestedBy,
            String accountNo
    ) {
        assertStartAllowed(allowPaperOrderExecute);

        BatchStepRange range = resolveRange(fromStepCode, toStepCode);
        assertRangeAllowed(range, allowPaperOrderExecute);

        String executionName = buildExecutionName(range);
        String input = buildInput(range, allowPaperOrderExecute, requestedBy, accountNo);
        String stateMachineArn = resolveStateMachineArn(allowPaperOrderExecute);

        try (SfnClient client = SfnClient.builder()
                .region(Region.of(properties.getAwsStepfunctionsRegion()))
                .build()) {

            StartExecutionResponse response = client.startExecution(StartExecutionRequest.builder()
                    .stateMachineArn(stateMachineArn)
                    .name(executionName)
                    .input(input)
                    .build());

            return new StartExecutionResult(
                    executionName,
                    response.executionArn(),
                    redactArn(response.executionArn()),
                    input
            );
        }
    }

    private void assertStartAllowed(boolean allowPaperOrderExecute) {
        if (!properties.canStartAwsStepfunctions()) {
            throw new IllegalStateException(
                    "AWS Step Functions StartExecution 차단됨. executionEnabled="
                            + properties.isExecutionEnabled()
                            + ", executionMode="
                            + properties.getExecutionMode()
                            + ", awsStepfunctionsStartEnabled="
                            + properties.isAwsStepfunctionsStartEnabled()
            );
        }

        if (isBlank(properties.getAwsStepfunctionsRegion())) {
            throw new IllegalStateException("AWS Step Functions region 설정이 비어 있음.");
        }

        if (allowPaperOrderExecute) {
            if (isBlank(properties.getAwsStepfunctionsApprovalStateMachineArn())) {
                throw new IllegalStateException("AWS Step Functions approval state machine ARN 설정이 비어 있음.");
            }
        } else {
            if (isBlank(properties.getAwsStepfunctionsStateMachineArn())) {
                throw new IllegalStateException("AWS Step Functions state machine ARN 설정이 비어 있음.");
            }
        }
    }

    private String resolveStateMachineArn(boolean allowPaperOrderExecute) {
        if (allowPaperOrderExecute) {
            return properties.getAwsStepfunctionsApprovalStateMachineArn();
        }

        return properties.getAwsStepfunctionsStateMachineArn();
    }

    private BatchStepRange resolveRange(String fromStepCode, String toStepCode) {
        if (isBlank(fromStepCode)) {
            throw new IllegalArgumentException("시작 stepCode가 비어 있음.");
        }

        if (isBlank(toStepCode)) {
            throw new IllegalArgumentException("종료 stepCode가 비어 있음.");
        }

        List<DailyBatchStepOptionDto> steps = dailyBatchService.getAvailableStepOptions();

        DailyBatchStepOptionDto fromStep = findStep(steps, fromStepCode);
        DailyBatchStepOptionDto toStep = findStep(steps, toStepCode);

        if (fromStep.stepOrder() > toStep.stepOrder()) {
            throw new IllegalArgumentException(
                    "시작 step이 종료 step보다 뒤에 있음. fromStepCode="
                            + fromStepCode
                            + ", toStepCode="
                            + toStepCode
            );
        }

        return new BatchStepRange(fromStep, toStep);
    }

    private DailyBatchStepOptionDto findStep(List<DailyBatchStepOptionDto> steps, String stepCode) {
        return steps.stream()
                .filter(step -> step.stepCode().equals(stepCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Daily Batch step definition을 찾을 수 없음. stepCode=" + stepCode
                ));
    }

    private void assertRangeAllowed(BatchStepRange range, boolean allowPaperOrderExecute) {
        int fromOrder = range.fromStep().stepOrder();
        int toOrder = range.toStep().stepOrder();

        if (fromOrder < properties.getMinExecutableStepOrder()
                || toOrder > properties.getMaxExecutableStepOrder()) {
            throw new IllegalStateException(
                    "AWS Step Functions 실행 범위 차단됨. allowedRange="
                            + properties.getMinExecutableStepOrder()
                            + "~"
                            + properties.getMaxExecutableStepOrder()
                            + ", requestedRange="
                            + fromOrder
                            + "~"
                            + toOrder
            );
        }

        if (!allowPaperOrderExecute && toOrder >= BROKER_SUBMIT_STEP_ORDER) {
            throw new IllegalStateException(
                    "Step 12 이상은 승인형 실행에서만 허용됨. requestedRange="
                            + fromOrder
                            + "~"
                            + toOrder
            );
        }

        if (allowPaperOrderExecute && !properties.isPaperOrderEnabled()) {
            throw new IllegalStateException("승인형 Step Functions 실행 차단됨. paperOrderEnabled=false");
        }
    }

    private String buildExecutionName(BatchStepRange range) {
        String prefix = properties.getAwsStepfunctionsExecutionNamePrefix();

        if (isBlank(prefix)) {
            prefix = "port-view-daily";
        }

        String normalizedPrefix = prefix
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9-_]", "-");

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String shortUuid = UUID.randomUUID().toString().substring(0, 8);

        String name = normalizedPrefix
                + "-step"
                + range.fromStep().stepOrder()
                + "-"
                + range.toStep().stepOrder()
                + "-"
                + timestamp
                + "-"
                + shortUuid;

        if (name.length() <= 80) {
            return name;
        }

        return name.substring(name.length() - 80);
    }

    private String buildInput(
            BatchStepRange range,
            boolean allowPaperOrderExecute,
            String requestedBy,
            String accountNo
    ) {
        return "{"
                + "\"environment\":\"" + jsonEscape(properties.getEnvironment()) + "\","
                + "\"dbTarget\":\"" + jsonEscape(properties.getDbTarget()) + "\","
                + "\"executionMode\":\"" + jsonEscape(properties.getExecutionMode()) + "\","
                + "\"source\":\"" + SOURCE_PORT_VIEW + "\","
                + "\"runDate\":\"" + LocalDate.now(KOREA_ZONE_ID) + "\","
                + "\"requestedBy\":\"" + requestedBy + "\","
                + "\"accountNo\":\"" + jsonEscape(accountNo) + "\","
                + "\"fromStepCode\":\"" + jsonEscape(range.fromStep().stepCode()) + "\","
                + "\"fromStepOrder\":" + range.fromStep().stepOrder() + ","
                + "\"toStepCode\":\"" + jsonEscape(range.toStep().stepCode()) + "\","
                + "\"toStepOrder\":" + range.toStep().stepOrder() + ","
                + "\"startStep\":" + range.fromStep().stepOrder() + ","
                + "\"endStep\":" + range.toStep().stepOrder() + ","
                + "\"allowPaperOrderExecute\":" + allowPaperOrderExecute + ","
                + "\"paperOrderEnabled\":" + properties.isPaperOrderEnabled() + ","
                + "\"requestedFrom\":\"port-view\""
                + "}";
    }

    private String redactArn(String arn) {
        if (arn == null || arn.isBlank()) {
            return arn;
        }

        return arn.replaceAll(":\\d{12}:", ":[REDACTED]:");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    public record StartExecutionResult(
            String executionName,
            String executionArn,
            String redactedExecutionArn,
            String input
    ) {
    }

    private record BatchStepRange(
            DailyBatchStepOptionDto fromStep,
            DailyBatchStepOptionDto toStep
    ) {
    }
}