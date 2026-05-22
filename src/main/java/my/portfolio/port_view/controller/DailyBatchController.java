package my.portfolio.port_view.controller;

import lombok.RequiredArgsConstructor;
import my.portfolio.port_view.common.ViewNames;
import my.portfolio.port_view.dto.DailyBatchPageDto;
import my.portfolio.port_view.service.DailyBatchAsyncService;
import my.portfolio.port_view.service.DailyBatchService;
import my.portfolio.port_view.service.SlackNotificationService;
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
    private final AccountNoResolver accountNoResolver;
    private final SlackNotificationService slackNotificationService;

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
     */
    @PostMapping("/daily-batch/run")
    public String runDailyBatch(
            @RequestParam(required = false) String accountNo,
            RedirectAttributes redirectAttributes
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

        try {
            Long batchRunId = dailyBatchService.startDailyPipelineRun();

            dailyBatchAsyncService.executeDailyPipelineAsync(batchRunId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Daily Pipeline 실행 시작: #" + batchRunId
            );

            return "redirect:/daily-batch/" + batchRunId + "?accountNo=" + resolvedAccountNo;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Daily Pipeline 실행 시작 실패: " + e.getMessage()
            );

            return "redirect:/daily-batch?accountNo=" + resolvedAccountNo;
        }
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
     */
    @PostMapping("/daily-batch/run-from-step")
    public String runDailyBatchFromStep(
            @RequestParam String fromStepCode,
            @RequestParam(required = false) String accountNo,
            RedirectAttributes redirectAttributes
    ) {
        String resolvedAccountNo = accountNoResolver.resolve(accountNo);

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
    }
}