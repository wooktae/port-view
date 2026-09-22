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
import my.portfolio.port_view.util.ViewMessages;
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
     * Latest Daily Batch execution view.
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
     * Details view for a specific Daily Batch execution.
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
     * Manually executes the complete Daily Pipeline.
     *
     * Disabled by default during the second local execution validation.
     * Complete Steps 1~17 execution is allowed only when portfolio.batch.full-pipeline-execution-enabled=true.
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
                    dailyBatchBlockedMessage(ViewMessages.text("daily.action.fullRun"))
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }

        try {
            Long batchRunId = dailyBatchService.startDailyPipelineRun();

            dailyBatchAsyncService.executeDailyPipelineAsync(batchRunId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    ViewMessages.text(
                            "daily.flash.started",
                            ViewMessages.text("daily.action.fullRun"),
                            batchRunId
                    )
            );

            return "redirect:/daily-batch/" + batchRunId + "?accountNo=" + resolvedAccountNo;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ViewMessages.text(
                            "daily.flash.failed",
                            ViewMessages.text("daily.action.fullRun"),
                            e.getMessage()
                    )
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }
    }

    /**
     * Executes a range of Daily Pipeline steps.
     *
     * Examples:
     * - Step 1 only: CONNECTOR_BALANCE ~ CONNECTOR_BALANCE
     * - Steps 1~11: CONNECTOR_BALANCE ~ DAILY_AUTO_BUY
     *
     * The Service layer rechecks the allowed step range and Paper order gate.
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
                    dailyBatchBlockedMessage(ViewMessages.text("daily.action.rangeRun"))
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }

        try {
            Long batchRunId = dailyBatchService.startDailyPipelineRunRange(fromStepCode, toStepCode);

            dailyBatchAsyncService.executeDailyPipelineAsync(batchRunId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    ViewMessages.text(
                            "daily.flash.rangeStarted",
                            ViewMessages.text("daily.action.rangeRun"),
                            batchRunId,
                            fromStepCode,
                            toStepCode
                    )
            );

            return "redirect:/daily-batch/" + batchRunId + "?accountNo=" + resolvedAccountNo;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ViewMessages.text(
                            "daily.flash.failed",
                            ViewMessages.text("daily.action.rangeRun"),
                            e.getMessage()
                    )
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }
    }


    /**
     * AWS Step Functions Daily Pipeline range StartExecution.
     *
     * Separated from the existing local-file ProcessBuilder execution path.
     * The initial integration uses only the Steps 1~11 safe range.
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
                    ViewMessages.text(
                            "daily.flash.disabled",
                            ViewMessages.text("daily.action.awsSafeRun"),
                            currentModeText()
                    )
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
                    ViewMessages.text(
                            "daily.flash.awsStarted",
                            ViewMessages.text("daily.action.awsSafeRun"),
                            result.executionName(),
                            result.redactedExecutionArn()
                    )
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ViewMessages.text(
                            "daily.flash.failed",
                            ViewMessages.text("daily.action.awsSafeRun"),
                            e.getMessage()
                    )
            );
        }

        return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
    }
    /**
     * AWS Step Functions Daily Pipeline approval range StartExecution.
     *
     * Separated from the existing Steps 1~11 safe trigger.
     * The order-related Steps 12~17 range must pass the paper-order gate and step range gate.
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
                    ViewMessages.text(
                            "daily.flash.disabled",
                            ViewMessages.text("daily.action.awsApprovalRun"),
                            currentModeText()
                    )
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
                    ViewMessages.text(
                            "daily.flash.awsStarted",
                            ViewMessages.text("daily.action.awsApprovalRun"),
                            result.executionName(),
                            result.redactedExecutionArn()
                    )
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ViewMessages.text(
                            "daily.flash.failed",
                            ViewMessages.text("daily.action.awsApprovalRun"),
                            e.getMessage()
                    )
            );
        }

        return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
    }

    /**
     * Sends a Slack test message.
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
                    ViewMessages.text(
                            "daily.flash.disabled",
                            ViewMessages.text("daily.action.slackTest"),
                            currentModeText()
                    )
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }

        try {
            slackNotificationService.sendTestMessage();

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    ViewMessages.text(
                            "daily.flash.completed",
                            ViewMessages.text("daily.action.slackTest")
                    )
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ViewMessages.text(
                            "daily.flash.failed",
                            ViewMessages.text("daily.action.slackTest"),
                            e.getMessage()
                    )
            );
        }

        return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
    }

    /**
     * Resends an existing Daily Batch execution result to Slack.
     * Use this to check Slack message formatting without rerunning the complete Daily Batch.
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
                    ViewMessages.text(
                            "daily.flash.disabled",
                            ViewMessages.text("daily.action.slackSummary"),
                            currentModeText()
                    )
            );

            return "redirect:/daily-batch/" + batchRunId + "?accountNo=" + resolvedAccountNo;
        }

        try {
            slackNotificationService.sendDailyBatchSummary(batchRunId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    ViewMessages.text(
                            "daily.flash.completedWithId",
                            ViewMessages.text("daily.action.slackSummary"),
                            batchRunId
                    )
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ViewMessages.text(
                            "daily.flash.failed",
                            ViewMessages.text("daily.action.slackSummary"),
                            e.getMessage()
                    )
            );
        }

        return "redirect:/daily-batch/" + batchRunId + "?accountNo=" + resolvedAccountNo;
    }

    /**
     * Manually checks Intraday Positions.
     *
     * - Query the current price
     * - Calculate Return relative to the Entry Price
     * - Detect -10% hard stop Candidates
     * - Do not create Order Candidates
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
                    ViewMessages.text(
                            "daily.flash.disabled",
                            ViewMessages.text("daily.action.intradayMonitor"),
                            currentModeText()
                    )
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
                    ViewMessages.text(
                            "daily.flash.failed",
                            ViewMessages.text("daily.action.intradayMonitor"),
                            e.getMessage()
                    )
            );
        }

        return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
    }

    /**
     * Partially reruns the Daily Pipeline from a specific step.
     *
     * Endpoint retained for compatibility with the existing view.
     * Prefer /daily-batch/run-range in the new second validation.
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
                    dailyBatchBlockedMessage(ViewMessages.text("daily.action.partialRun"))
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }

        try {
            Long batchRunId = dailyBatchService.startDailyPipelineRunFromStep(fromStepCode);

            dailyBatchAsyncService.executeDailyPipelineAsync(batchRunId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    ViewMessages.text(
                            "daily.flash.partialStarted",
                            ViewMessages.text("daily.action.partialRun"),
                            batchRunId,
                            fromStepCode
                    )
            );

            return "redirect:/daily-batch/" + batchRunId + "?accountNo=" + resolvedAccountNo;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ViewMessages.text(
                            "daily.flash.failed",
                            ViewMessages.text("daily.action.partialRun"),
                            e.getMessage()
                    )
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }
    }

    /**
     * Reruns a failed Daily Pipeline from its first FAILED step.
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
                    dailyBatchBlockedMessage(ViewMessages.text("daily.action.retryFailed"))
            );

            return "redirect:/daily-batch/" + batchRunId + "?accountNo=" + resolvedAccountNo;
        }

        try {
            Long retryBatchRunId = dailyBatchService.startDailyPipelineRerunFailed(batchRunId);

            dailyBatchAsyncService.executeDailyPipelineAsync(retryBatchRunId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    ViewMessages.text(
                            "daily.flash.retryStarted",
                            ViewMessages.text("daily.action.retryFailed"),
                            retryBatchRunId,
                            batchRunId
                    )
            );

            return "redirect:/daily-batch/" + retryBatchRunId + "?accountNo=" + resolvedAccountNo;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ViewMessages.text(
                            "daily.flash.failed",
                            ViewMessages.text("daily.action.retryFailed"),
                            e.getMessage()
                    )
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
                && batchProperties.getMinExecutableStepOrder() <= 1
                && batchProperties.getMaxExecutableStepOrder() >= 11;
    }

    private boolean canStartAwsStepfunctionsApproval() {
        return batchProperties.canStartAwsStepfunctions()
                && batchProperties.canStartAwsStepfunctionsStep()
                && batchProperties.isPaperOrderEnabled()
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
        return ViewMessages.text("daily.flash.blocked", actionName, currentModeText());
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