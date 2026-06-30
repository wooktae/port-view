package my.portfolio.port_view.controller;

import lombok.RequiredArgsConstructor;
import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.config.DailyBatchProperties;
import my.portfolio.port_view.dto.dailybatch.DailyBatchPageDto;
import my.portfolio.port_view.service.DailyBatchAsyncService;
import my.portfolio.port_view.service.DailyBatchService;
import my.portfolio.port_view.service.SlackNotificationService;
import my.portfolio.port_view.service.StepFunctionsDailyBatchExecutionService;
import my.portfolio.port_view.util.AccountNoResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class DailyBatchController {

    private final DailyBatchService dailyBatchService;
    private final DailyBatchAsyncService dailyBatchAsyncService;
    private final StepFunctionsDailyBatchExecutionService stepFunctionsDailyBatchExecutionService;
    private final AccountNoResolver accountNoResolver;
    private final SlackNotificationService slackNotificationService;
    private final DailyBatchProperties batchProperties;

    /**
     * Daily Batch 최신 실행 화면.
     */
    @GetMapping("/daily-batch")
    public String dailyBatch(
            @RequestParam(required = false) String accountNo,
            Model model
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        DailyBatchPageDto page = dailyBatchService.getLatestPage();

        addDailyBatchModel(model, resolvedAccountNo, page);

        return ViewNames.DAILY_BATCH;
    }

    /**
     * 특정 Daily Batch 실행 상세 화면.
     */
    @GetMapping("/daily-batch/{batchRunId}")
    public String dailyBatchDetail(
            @PathVariable Long batchRunId,
            @RequestParam(required = false) String accountNo,
            Model model
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        DailyBatchPageDto page = dailyBatchService.getPage(batchRunId);

        addDailyBatchModel(model, resolvedAccountNo, page);

        return ViewNames.DAILY_BATCH;
    }

    /**
     * Daily Pipeline 전체 수동 실행.
     *
     * 2차 로컬 실행 검증에서는 기본 비활성.
     * 전체 1~17 실행은 portfolio.batch.full-pipeline-execution-enabled=true일 때만 허용한다.
     */
    @PostMapping("/daily-batch/run")
    public String runDailyBatch(
            @RequestParam(required = false) String accountNo,
            RedirectAttributes redirectAttributes
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        if (!canRunFullLocalFilePipeline()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    dailyBatchBlockedMessage("Daily Pipeline 전체 실행")
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }

        try {
            Long batchRunId = dailyBatchService.startDailyPipelineRun();

            dailyBatchAsyncService.executeDailyPipelineAsync(batchRunId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Daily Pipeline 전체 실행 시작: #" + batchRunId
            );

            return "redirect:/daily-batch/" + batchRunId + "?accountNo=" + resolvedAccountNo;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Daily Pipeline 전체 실행 시작 실패: " + e.getMessage()
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }
    }

    /**
     * Daily Pipeline step 범위 실행.
     *
     * 예:
     * - Step 1 단독: CONNECTOR_BALANCE ~ CONNECTOR_BALANCE
     * - Step 1~11: CONNECTOR_BALANCE ~ DAILY_AUTO_BUY
     *
     * Service 레벨에서 허용 step 범위와 Paper 주문 gate를 다시 확인한다.
     */
    @PostMapping("/daily-batch/run-range")
    public String runDailyBatchRange(
            @RequestParam String fromStepCode,
            @RequestParam String toStepCode,
            @RequestParam(required = false) String accountNo,
            RedirectAttributes redirectAttributes
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        if (!canRunLocalFileBatch()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    dailyBatchBlockedMessage("Daily Pipeline 범위 실행")
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }

        try {
            Long batchRunId = dailyBatchService.startDailyPipelineRunRange(fromStepCode, toStepCode);

            dailyBatchAsyncService.executeDailyPipelineAsync(batchRunId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Daily Pipeline 범위 실행 시작: #"
                            + batchRunId
                            + " / 시작 단계="
                            + fromStepCode
                            + " / 종료 단계="
                            + toStepCode
            );

            return "redirect:/daily-batch/" + batchRunId + "?accountNo=" + resolvedAccountNo;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Daily Pipeline 범위 실행 시작 실패: " + e.getMessage()
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }
    }


    /**
     * AWS Step Functions Daily Pipeline range StartExecution.
     *
     * 기존 local-file ProcessBuilder 실행과 분리한다.
     * 1차 연결에서는 Step 1~11 safe range만 사용한다.
     */
    @PostMapping("/daily-batch/aws-stepfunctions/start-range")
    public String startAwsStepfunctionsRange(
            @RequestParam String fromStepCode,
            @RequestParam String toStepCode,
            @RequestParam(required = false) String accountNo,
            RedirectAttributes redirectAttributes
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        if (!batchProperties.canStartAwsStepfunctions()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "AWS Step Functions 실행은 현재 비활성화되어 있습니다. " + currentModeText()
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }

        try {
            StepFunctionsDailyBatchExecutionService.StartExecutionResult result =
                    stepFunctionsDailyBatchExecutionService.startSafeRange(
                            fromStepCode,
                            toStepCode,
                            resolvedAccountNo
                    );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "AWS Step Functions 실행 시작: "
                            + result.executionName()
                            + " / executionArn="
                            + result.redactedExecutionArn()
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "AWS Step Functions 실행 시작 실패: " + e.getMessage()
            );
        }

        return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
    }
    /**
     * AWS Step Functions Daily Pipeline approval range StartExecution.
     *
     * 기존 Step 1~11 safe trigger와 분리한다.
     * Step 12~17 주문성 구간은 paper-order gate와 step range gate를 통과해야 실행한다.
     */
    @PostMapping("/daily-batch/aws-stepfunctions/start-approval-range")
    public String startAwsStepfunctionsApprovalRange(
            @RequestParam String fromStepCode,
            @RequestParam String toStepCode,
            @RequestParam(required = false) String accountNo,
            RedirectAttributes redirectAttributes
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        if (!canStartAwsStepfunctionsApproval()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "AWS Step Functions 승인형 Step 12~17 실행은 현재 비활성화되어 있습니다. " + currentModeText()
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }

        try {
            StepFunctionsDailyBatchExecutionService.StartExecutionResult result =
                    stepFunctionsDailyBatchExecutionService.startApprovalRange(
                            fromStepCode,
                            toStepCode,
                            resolvedAccountNo
                    );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "AWS Step Functions 승인형 Step 12~17 실행 시작: "
                            + result.executionName()
                            + " / executionArn="
                            + result.redactedExecutionArn()
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "AWS Step Functions 승인형 Step 12~17 실행 시작 실패: " + e.getMessage()
            );
        }

        return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
    }

    /**
     * Slack 테스트 메시지 전송.
     */
    @PostMapping("/daily-batch/slack-test")
    public String sendSlackTestMessage(
            @RequestParam(required = false) String accountNo,
            RedirectAttributes redirectAttributes
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        if (!batchProperties.isSlackActionEnabled()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Slack 테스트 메시지 전송은 현재 비활성화되어 있습니다. "
                            + currentModeText()
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }

        try {
            slackNotificationService.sendTestMessage();

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Slack 테스트 메시지 전송 완료"
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Slack 테스트 메시지 전송 실패: " + e.getMessage()
            );
        }

        return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
    }

    /**
     * 기존 Daily Batch 실행 결과를 Slack으로 재전송.
     * 전체 Daily Batch를 다시 돌리지 않고 Slack 메시지 포맷만 확인할 때 사용.
     */
    @PostMapping("/daily-batch/{batchRunId}/slack-summary-test")
    public String sendDailyBatchSlackSummaryTest(
            @PathVariable Long batchRunId,
            @RequestParam(required = false) String accountNo,
            RedirectAttributes redirectAttributes
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        if (!batchProperties.isSlackActionEnabled()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Daily Batch Slack 요약 재전송은 현재 비활성화되어 있습니다. "
                            + currentModeText()
            );

            return "redirect:/daily-batch/" + batchRunId + "?accountNo=" + resolvedAccountNo;
        }

        try {
            slackNotificationService.sendDailyBatchSummary(batchRunId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Daily Batch Slack 요약 메시지 전송 완료: #" + batchRunId
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Daily Batch Slack 요약 메시지 전송 실패: " + e.getMessage()
            );
        }

        return "redirect:/daily-batch/" + batchRunId + "?accountNo=" + resolvedAccountNo;
    }

    /**
     * 장중 포지션 수동 점검.
     *
     * - 현재가 조회
     * - 진입가 대비 손익률 계산
     * - -10% hard stop 후보 감지
     * - 주문 후보 생성은 하지 않음
     */
    @PostMapping("/daily-batch/intraday-monitor/run")
    public String runIntradayPositionMonitor(
            @RequestParam(required = false) String accountNo,
            RedirectAttributes redirectAttributes
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        if (!canRunIntradayMonitor()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "장중 포지션 점검은 현재 비활성화되어 있습니다. "
                            + currentModeText()
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }

        try {
            String message = dailyBatchService.runIntradayPositionMonitor();

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    message
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "장중 포지션 점검 실패: " + e.getMessage()
            );
        }

        return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
    }

    /**
     * Daily Pipeline 특정 step부터 부분 재실행.
     *
     * 기존 화면 호환용 endpoint.
     * 신규 2차 검증에서는 /daily-batch/run-range 사용을 우선한다.
     */
    @PostMapping("/daily-batch/run-from-step")
    public String runDailyBatchFromStep(
            @RequestParam String fromStepCode,
            @RequestParam(required = false) String accountNo,
            RedirectAttributes redirectAttributes
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        if (!canRunLocalFileBatch()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    dailyBatchBlockedMessage("Daily Pipeline 부분 실행")
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }

        try {
            Long batchRunId = dailyBatchService.startDailyPipelineRunFromStep(fromStepCode);

            dailyBatchAsyncService.executeDailyPipelineAsync(batchRunId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Daily Pipeline 부분 실행 시작: #" + batchRunId + " / 시작 단계=" + fromStepCode
            );

            return "redirect:/daily-batch/" + batchRunId + "?accountNo=" + resolvedAccountNo;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Daily Pipeline 부분 실행 시작 실패: " + e.getMessage()
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }
    }

    /**
     * 실패한 Daily Pipeline을 첫 FAILED step부터 재실행.
     */
    @PostMapping("/daily-batch/{batchRunId}/rerun-failed")
    public String rerunFailedDailyBatch(
            @PathVariable Long batchRunId,
            @RequestParam(required = false) String accountNo,
            RedirectAttributes redirectAttributes
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        if (!canRunLocalFileBatch()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    dailyBatchBlockedMessage("실패 단계부터 재실행")
            );

            return "redirect:/daily-batch/" + batchRunId + "?accountNo=" + resolvedAccountNo;
        }

        try {
            Long retryBatchRunId = dailyBatchService.startDailyPipelineRerunFailed(batchRunId);

            dailyBatchAsyncService.executeDailyPipelineAsync(retryBatchRunId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "실패 단계부터 재실행 시작: #" + retryBatchRunId + " / 원본 실행 #" + batchRunId
            );

            return "redirect:/daily-batch/" + retryBatchRunId + "?accountNo=" + resolvedAccountNo;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "실패 단계부터 재실행 시작 실패: " + e.getMessage()
            );

            return "redirect:/daily-batch/" + batchRunId + "?accountNo=" + resolvedAccountNo;
        }
    }

    private void addDailyBatchModel(
            Model model,
            String resolvedAccountNo,
            DailyBatchPageDto page
    ) {
        model.addAttribute("accountNo", resolvedAccountNo);
        model.addAttribute("page", page);

        model.addAttribute("selectedRun", page.selectedRun());
        model.addAttribute("steps", page.steps());
        model.addAttribute("recentRuns", page.recentRuns());
        model.addAttribute("blockWatchCandidates", page.blockWatchCandidates());

        model.addAttribute("hasRunningBatch", page.hasRunningBatch());
        model.addAttribute("runningBatchRunId", page.runningBatchRunId());

        model.addAttribute("availableSteps", dailyBatchService.getAvailableStepOptions());
        model.addAttribute("intradayChecks", dailyBatchService.getRecentIntradayChecks());

        model.addAttribute("batchEnvironment", batchProperties.getEnvironment());
        model.addAttribute("batchDbTarget", batchProperties.getDbTarget());
        model.addAttribute("batchExecutionMode", batchProperties.getExecutionMode());

        model.addAttribute("batchExecutionEnabled", batchProperties.isExecutionEnabled());
        model.addAttribute("batchLocalFileExecutionEnabled", batchProperties.isLocalFileExecutionEnabled());
        model.addAttribute("batchAwsStepfunctionsStartEnabled", batchProperties.isAwsStepfunctionsStartEnabled());
        model.addAttribute("batchAwsStepfunctionsStepStartEnabled", batchProperties.isAwsStepfunctionsStepStartEnabled());
        model.addAttribute("batchIntradayMonitorEnabled", batchProperties.isIntradayMonitorEnabled());
        model.addAttribute("batchSlackActionEnabled", batchProperties.isSlackActionEnabled());
        model.addAttribute("batchSlackSummaryEnabled", batchProperties.isSlackSummaryEnabled());

        model.addAttribute("batchPaperOrderEnabled", batchProperties.isPaperOrderEnabled());
        model.addAttribute("batchFullPipelineExecutionEnabled", batchProperties.isFullPipelineExecutionEnabled());
        model.addAttribute("batchMinExecutableStepOrder", batchProperties.getMinExecutableStepOrder());
        model.addAttribute("batchMaxExecutableStepOrder", batchProperties.getMaxExecutableStepOrder());

        model.addAttribute("canRunLocalFileBatch", canRunLocalFileBatch());
        model.addAttribute("canRunFullLocalFilePipeline", canRunFullLocalFilePipeline());
        model.addAttribute("canStartAwsStepfunctions", batchProperties.canStartAwsStepfunctions());
        model.addAttribute("canStartAwsStepfunctionsStep", batchProperties.canStartAwsStepfunctionsStep());
        model.addAttribute("canStartAwsStepfunctionsSafe", canStartAwsStepfunctionsSafe());
        model.addAttribute("canStartAwsStepfunctionsApproval", canStartAwsStepfunctionsApproval());
        model.addAttribute("canRunIntradayMonitor", canRunIntradayMonitor());
        model.addAttribute("canUseSlackAction", batchProperties.isSlackActionEnabled());

        model.addAttribute("dailyBatchReadOnly", !canRunLocalFileBatch() && !batchProperties.canStartAwsStepfunctions());
        model.addAttribute("batchModeText", currentModeText());
    }

    private boolean canRunLocalFileBatch() {
        return batchProperties.canRunLocalFileBatch();
    }

    private boolean canRunFullLocalFilePipeline() {
        return batchProperties.canRunFullLocalFilePipeline();
    }

    private boolean canStartAwsStepfunctionsSafe() {
        return batchProperties.canStartAwsStepfunctions()
                && !batchProperties.isPaperOrderEnabled()
                && batchProperties.getMinExecutableStepOrder() <= 1
                && batchProperties.getMaxExecutableStepOrder() >= 11;
    }

    private boolean canStartAwsStepfunctionsApproval() {
        return batchProperties.canStartAwsStepfunctions()
                && batchProperties.canStartAwsStepfunctionsStep()
                && batchProperties.isPaperOrderEnabled()
                && !batchProperties.isFullPipelineExecutionEnabled()
                && batchProperties.getMinExecutableStepOrder() <= 12
                && batchProperties.getMaxExecutableStepOrder() >= 17;
    }

    private boolean canRunIntradayMonitor() {
        return batchProperties.isExecutionEnabled()
                && batchProperties.isLocalFileMode()
                && batchProperties.isLocalFileExecutionEnabled()
                && batchProperties.isIntradayMonitorEnabled();
    }

    private String dailyBatchBlockedMessage(String actionName) {
        return actionName + "은 현재 비활성화되어 있습니다. "
                + "2차 AWS Paper 로컬 실행 검증에서는 허용된 step 범위와 Paper 주문 gate를 서버단에서 확인합니다. "
                + currentModeText();
    }

    private String currentModeText() {
        return "environment=" + batchProperties.getEnvironment()
                + ", dbTarget=" + batchProperties.getDbTarget()
                + ", executionMode=" + batchProperties.getExecutionMode()
                + ", executionEnabled=" + batchProperties.isExecutionEnabled()
                + ", localFileExecutionEnabled=" + batchProperties.isLocalFileExecutionEnabled()
                + ", fullPipelineExecutionEnabled=" + batchProperties.isFullPipelineExecutionEnabled()
                + ", paperOrderEnabled=" + batchProperties.isPaperOrderEnabled()
                + ", minExecutableStepOrder=" + batchProperties.getMinExecutableStepOrder()
                + ", maxExecutableStepOrder=" + batchProperties.getMaxExecutableStepOrder()
                + ", awsStepfunctionsStartEnabled=" + batchProperties.isAwsStepfunctionsStartEnabled()
                + ", awsStepfunctionsStepStartEnabled=" + batchProperties.isAwsStepfunctionsStepStartEnabled()
                + ", intradayMonitorEnabled=" + batchProperties.isIntradayMonitorEnabled();
    }
}