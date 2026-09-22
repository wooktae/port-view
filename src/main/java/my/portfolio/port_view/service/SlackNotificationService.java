package my.portfolio.port_view.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.portfolio.port_view.config.SlackProperties;
import my.portfolio.port_view.dto.dailybatch.DailyBatchRunDto;
import my.portfolio.port_view.dto.dailybatch.DailyBatchStepLogDto;
import my.portfolio.port_view.dto.strategy.DailyPositionDecisionDto;
import my.portfolio.port_view.dto.strategy.DailyRunDto;
import my.portfolio.port_view.dto.strategy.DailySignalDto;
import my.portfolio.port_view.dto.strategy.StrategyExecutionOrderDto;
import my.portfolio.port_view.dto.strategy.StrategyExecutionPlanDto;
import my.portfolio.port_view.entity.ConnectorBalanceSnapshot;
import my.portfolio.port_view.entity.ConnectorPositionSnapshot;
import my.portfolio.port_view.repository.ConnectorBalanceSnapshotRepository;
import my.portfolio.port_view.repository.ConnectorPositionSnapshotRepository;
import my.portfolio.port_view.repository.DailyBatchRepository;
import my.portfolio.port_view.repository.StrategyDailyViewRepository;
import my.portfolio.port_view.repository.StrategyExecutionQueryRepository;
import my.portfolio.port_view.util.ViewMessages;
import my.portfolio.port_view.util.ViewTextUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import java.text.DecimalFormat;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackNotificationService {

    private final DailyBatchRepository dailyBatchRepository;
    private final StrategyDailyViewRepository strategyDailyViewRepository;
    private final StrategyExecutionQueryRepository strategyExecutionQueryRepository;
    private final ConnectorBalanceSnapshotRepository connectorBalanceSnapshotRepository;
    private final ConnectorPositionSnapshotRepository connectorPositionSnapshotRepository;
    private final SlackClient slackClient;
    private final SlackProperties slackProperties;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");

    // =====================================================
    // Public Methods
    // =====================================================

    public void sendTestMessage() {
        sendText(text("slack.test.title") + "\n\n" + text("slack.test.description"));
    }

    public void sendDailyBatchSummary(Long batchRunId) {
        if (batchRunId == null) {
            return;
        }

        DailyBatchRunDto run = dailyBatchRepository.findRunById(batchRunId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Daily Batch 실행 정보를 찾을 수 없음. batchRunId=" + batchRunId
                ));

        // Key fix:
        // Do not send Slack notifications for non-terminal states such as RUNNING or PENDING.
        if (!run.isSuccess() && !run.isFailed()) {
            log.info(
                    "Slack Daily Batch summary skipped. batchRunId={}, runStatus={}",
                    batchRunId,
                    run.runStatus()
            );
            return;
        }

        List<DailyBatchStepLogDto> steps = dailyBatchRepository.findSteps(batchRunId);

        String message = buildDailyBatchMessage(run, steps);

        sendText(message);
    }

    // =====================================================
    // Message Builder
    // =====================================================

    private String buildDailyBatchMessage(
            DailyBatchRunDto run,
            List<DailyBatchStepLogDto> steps
    ) {
        String statusEmoji = run.isSuccess() ? "✅" : run.isFailed() ? "🚨" : "ℹ️";
        String statusLabel = runStatusLabel(run.runStatus());

        StringBuilder sb = new StringBuilder();

        sb.append(statusEmoji)
                .append(" ")
                .append(text("slack.daily.title", statusLabel))
                .append("\n\n");

        appendBatchSummary(sb, run);
        appendStepSummary(sb, steps);
        appendDailySummary(sb);
        appendExecutionSummary(sb);
        appendBalanceSummary(sb, run.accountNo());
        appendPositionSummary(sb, run.accountNo());

        return sb.toString();
    }

    private void appendBatchSummary(StringBuilder sb, DailyBatchRunDto run) {
        sb.append(text("slack.section.batch")).append("\n");
        sb.append(text("slack.batch.runId", run.id())).append("\n");
        sb.append(text("slack.batch.runDate", run.batchDate())).append("\n");
        sb.append(text("slack.batch.status", runStatusLabel(run.runStatus()))).append("\n");
        sb.append(text("slack.batch.account", nullToDash(run.accountNo()))).append("\n");
        sb.append(text("slack.batch.environment", nullToDash(run.environment()))).append("\n");
        sb.append(text(
                "slack.batch.steps",
                nvl(run.successStepCount()),
                nvl(run.noTargetStepCount()),
                nvl(run.failedStepCount()),
                nvl(run.skippedStepCount())
        )).append("\n");

        if (run.startedAt() != null) {
            sb.append(text("slack.batch.started", formatKstDateTime(run.startedAt()))).append("\n");
        }

        if (run.finishedAt() != null) {
            sb.append(text("slack.batch.finished", formatKstDateTime(run.finishedAt()))).append("\n");
        }

        if (run.durationMs() != null) {
            sb.append(text("slack.batch.duration", formatDuration(run.durationMs()))).append("\n");
        }

        if (run.isFailed()) {
            sb.append("\n").append(text("slack.section.error")).append("\n");
            sb.append(text("slack.error.step", nullToDash(run.errorStepCode()))).append("\n");
            sb.append(text("slack.error.message", shorten(run.errorMessage(), 700))).append("\n");
        }
    }

    private void appendStepSummary(
            StringBuilder sb,
            List<DailyBatchStepLogDto> steps
    ) {
        sb.append("\n").append(text("slack.section.steps")).append("\n");

        if (steps == null || steps.isEmpty()) {
            sb.append(text("slack.none")).append("\n");
            return;
        }

        for (DailyBatchStepLogDto step : steps) {
            String stepNumber = step.displayStepNo();
            String stepLabel = stepCodeShortLabel(step.stepCode());
            String statusLabel = stepStatusLabel(step.stepStatus());

            if (step.durationMs() == null) {
                sb.append(text("slack.step.line", stepNumber, stepLabel, statusLabel));
            } else {
                sb.append(text(
                        "slack.step.lineWithDuration",
                        stepNumber,
                        stepLabel,
                        statusLabel,
                        formatDuration(step.durationMs())
                ));
            }

            sb.append("\n");
        }
    }

    private void appendDailySummary(StringBuilder sb) {
        sb.append("\n").append(text("slack.section.market")).append("\n");

        Optional<DailyRunDto> latestDailyRun = strategyDailyViewRepository.findLatestDailyRun();

        if (latestDailyRun.isEmpty()) {
            sb.append(text("slack.market.noDailyRun")).append("\n");
            return;
        }

        DailyRunDto dailyRun = latestDailyRun.get();

        sb.append(text(
                "slack.market.status",
                marketSignalLabel(dailyRun.marketSignal()),
                nullToDash(dailyRun.marketSignal())
        )).append("\n");

        sb.append(text(
                "slack.market.dataDate",
                dailyRun.dataDate() == null ? "-" : dailyRun.dataDate()
        )).append("\n");

        sb.append(text(
                "slack.market.candidatesSignals",
                nvl(dailyRun.candidateCount()),
                nvl(dailyRun.signalCount())
        )).append("\n");

        sb.append(text(
                "slack.market.exposurePositions",
                formatDecimal(dailyRun.baseExposure(), 2),
                text("slack.position.unit", nvl(dailyRun.maxPositions()))
        )).append("\n");

        sb.append(text("slack.market.reason", marketReasonText(dailyRun))).append("\n");

        appendDailySignalSummary(sb, dailyRun.id());
        appendDailyPositionDecisionSummary(sb, dailyRun.id());
    }

    private void appendDailySignalSummary(StringBuilder sb, Long dailyRunId) {
        if (dailyRunId == null) {
            return;
        }

        List<DailySignalDto> signals = strategyDailyViewRepository.findDailySignals(dailyRunId);

        if (signals == null || signals.isEmpty()) {
            sb.append(text("slack.signal.none")).append("\n");
            return;
        }

        sb.append(text("slack.signal.count", signals.size())).append("\n");

        int limit = Math.min(signals.size(), 3);

        for (int i = 0; i < limit; i++) {
            DailySignalDto signal = signals.get(i);

            sb.append(text(
                    "slack.signal.item",
                    nullToDash(signal.companyName()),
                    nullToDash(signal.tickerCode()),
                    signal.rankNo() == null ? "-" : signal.rankNo(),
                    signalStatusLabel(signal.signalStatus())
            ));

            if (signal.executionOrderId() != null) {
                sb.append(text("slack.signal.order", signal.executionOrderId()));
            }

            sb.append("\n");
        }

        if (signals.size() > limit) {
            sb.append(text("slack.more.items", signals.size() - limit)).append("\n");
        }
    }

    private void appendDailyPositionDecisionSummary(StringBuilder sb, Long dailyRunId) {
        if (dailyRunId == null) {
            return;
        }

        List<DailyPositionDecisionDto> decisions =
                strategyDailyViewRepository.findDailyPositionDecisions(dailyRunId);

        if (decisions == null || decisions.isEmpty()) {
            sb.append(text("slack.positionDecision.none")).append("\n");
            return;
        }

        long sellCount = decisions.stream()
                .filter(DailyPositionDecisionDto::sell)
                .count();

        long holdCount = decisions.stream()
                .filter(DailyPositionDecisionDto::hold)
                .count();

        long skipCount = decisions.stream()
                .filter(DailyPositionDecisionDto::skip)
                .count();

        sb.append(text(
                "slack.positionDecision.summary",
                sellCount,
                holdCount,
                skipCount
        )).append("\n");
    }

    private void appendExecutionSummary(StringBuilder sb) {
        sb.append("\n").append(text("slack.section.execution")).append("\n");

        StrategyExecutionPlanDto latestPlan = strategyExecutionQueryRepository.findPlans()
                .stream()
                .findFirst()
                .orElse(null);

        if (latestPlan == null) {
            sb.append(text("slack.execution.noPlan")).append("\n");
            return;
        }

        List<StrategyExecutionOrderDto> orders =
                strategyExecutionQueryRepository.findOrdersByPlanId(latestPlan.id());

        List<StrategyExecutionOrderDto> buyOrders = orders.stream()
                .filter(order -> "BUY".equalsIgnoreCase(order.actionType()))
                .toList();

        List<StrategyExecutionOrderDto> sellOrders = orders.stream()
                .filter(order -> "SELL".equalsIgnoreCase(order.actionType()))
                .toList();

        sb.append(text(
                "slack.execution.plan",
                latestPlan.id(),
                latestPlan.planDate(),
                planStatusLabel(latestPlan.planStatus())
        )).append("\n");

        sb.append(text(
                "slack.execution.market",
                marketSignalLabel(latestPlan.marketSignal())
        )).append("\n");

        appendOrderList(sb, text("slack.execution.plannedBuys"), buyOrders);
        appendOrderList(sb, text("slack.execution.plannedSells"), sellOrders);
    }

    private void appendOrderList(
            StringBuilder sb,
            String title,
            List<StrategyExecutionOrderDto> orders
    ) {
        sb.append(text("slack.orderList.header", title)).append(" ");

        if (orders == null || orders.isEmpty()) {
            sb.append(text("label.none")).append("\n");
            return;
        }

        sb.append(text("slack.orderList.count", orders.size())).append("\n");

        int limit = Math.min(orders.size(), 5);

        for (int i = 0; i < limit; i++) {
            StrategyExecutionOrderDto order = orders.get(i);

            sb.append(text(
                    "slack.orderList.item",
                    nullToDash(order.stockName()),
                    nullToDash(order.tickerCode()),
                    text("unit.shares", nvl(order.orderQty())),
                    executionStatusLabel(order.executionStatus())
            ));

            if (order.connectorOrderRequestId() != null) {
                sb.append(text("slack.orderList.connector", order.connectorOrderRequestId()));
            }

            sb.append("\n");
        }

        if (orders.size() > limit) {
            sb.append(text("slack.more.items", orders.size() - limit)).append("\n");
        }
    }

    private void appendBalanceSummary(StringBuilder sb, String accountNo) {
        sb.append("\n").append(text("slack.section.balance")).append("\n");

        if (ViewTextUtils.isBlank(accountNo)) {
            sb.append(text("slack.account.missing")).append("\n");
            return;
        }

        Optional<ConnectorBalanceSnapshot> latestBalance =
                connectorBalanceSnapshotRepository.findTopByAccountNoOrderByAsOfDateDescAsOfTsDesc(accountNo);

        if (latestBalance.isEmpty()) {
            sb.append(text("slack.balance.missing")).append("\n");
            return;
        }

        ConnectorBalanceSnapshot balance = latestBalance.get();

        sb.append(text("slack.balance.date", balance.getAsOfDate())).append("\n");
        sb.append(text("slack.balance.total", formatMoney(balance.getTotalEvalAmount()))).append("\n");
        sb.append(text("slack.balance.cash", formatMoney(balance.getCashBalance()))).append("\n");
        sb.append(text("slack.balance.profit", formatMoney(balance.getEvalProfit()))).append("\n");
    }

    private void appendPositionSummary(StringBuilder sb, String accountNo) {
        sb.append("\n").append(text("slack.section.positions")).append("\n");

        if (ViewTextUtils.isBlank(accountNo)) {
            sb.append(text("slack.account.missing")).append("\n");
            return;
        }

        List<ConnectorPositionSnapshot> positions =
                connectorPositionSnapshotRepository.findLatestPositionsByAccountNo(accountNo);

        if (positions == null || positions.isEmpty()) {
            sb.append(text("slack.none")).append("\n");
            return;
        }

        sb.append(text("slack.positions.count", positions.size())).append("\n");

        int limit = Math.min(positions.size(), 5);

        for (int i = 0; i < limit; i++) {
            ConnectorPositionSnapshot position = positions.get(i);

            sb.append(text(
                    "slack.position.item",
                    nullToDash(position.getStockName()),
                    nullToDash(position.getTickerCode()),
                    text("unit.shares", position.getQuantity() == null ? 0 : position.getQuantity()),
                    formatMoney(position.getEvalAmount()),
                    formatMoney(position.getEvalProfit())
            )).append("\n");
        }

        if (positions.size() > limit) {
            sb.append(text("slack.more.positions", positions.size() - limit)).append("\n");
        }
    }

    // =====================================================
    // Slack Sender
    // =====================================================

    private void sendText(String text) {
        slackClient.sendText(text);
    }

    // =====================================================
    // Text Helpers
    // =====================================================

    private String marketSignalLabel(String marketSignal) {
        if (marketSignal == null || marketSignal.isBlank()) {
            return "-";
        }

        return switch (marketSignal.trim().toUpperCase()) {
            case "BLOCK" -> text("slack.market.block");
            case "NEUTRAL" -> text("slack.market.neutral");
            case "AGGRESSIVE" -> text("slack.market.aggressive");
            case "DEFENSIVE" -> text("slack.market.defensive");
            default -> marketSignal;
        };
    }

    private String marketReasonText(DailyRunDto dailyRun) {
        String signal = ViewTextUtils.upper(dailyRun.marketSignal());

        return switch (signal) {
            case "BLOCK" -> text("slack.marketReason.block");
            case "NEUTRAL" -> text("slack.marketReason.neutral");
            case "AGGRESSIVE" -> text("slack.marketReason.aggressive");
            case "DEFENSIVE" -> text("slack.marketReason.defensive");
            default -> text("slack.marketReason.unknown");
        };
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "-";
        }

        BigDecimal rounded = value.setScale(0, RoundingMode.HALF_UP);
        return text("unit.money", MONEY_FORMAT.format(rounded));
    }

    private String formatDecimal(BigDecimal value, int scale) {
        if (value == null) {
            return "-";
        }

        return value.setScale(scale, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatDuration(Long durationMs) {
        if (durationMs == null || durationMs < 0) {
            return "-";
        }

        long totalSeconds = durationMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (minutes <= 0) {
            return text("unit.seconds", seconds);
        }

        return text("unit.minutesSeconds", minutes, seconds);
    }

    private String text(String key, Object... arguments) {
        return ViewMessages.text(slackLocale(), key, arguments);
    }

    private Locale slackLocale() {
        return Locale.KOREAN.getLanguage().equalsIgnoreCase(slackProperties.getLocale())
                ? Locale.KOREAN
                : Locale.ENGLISH;
    }

    private String runStatusLabel(String status) {
        return switch (ViewTextUtils.upper(status)) {
            case "CREATED" -> text("status.created");
            case "RUNNING" -> text("status.running");
            case "SUCCESS" -> text("label.success");
            case "FAILED" -> text("status.failed");
            case "CANCELLED" -> text("status.canceled");
            default -> nullToDash(status);
        };
    }

    private String stepStatusLabel(String status) {
        return switch (ViewTextUtils.upper(status)) {
            case "PENDING" -> text("status.pending");
            case "RUNNING" -> text("status.running");
            case "SUCCESS" -> text("label.success");
            case "FAILED" -> text("status.failed");
            case "SKIPPED" -> text("status.skipped");
            case "NO_TARGET" -> text("slack.status.noTarget");
            default -> nullToDash(status);
        };
    }

    private String signalStatusLabel(String status) {
        return switch (ViewTextUtils.upper(status)) {
            case "READY" -> text("status.readyComplete");
            case "BLOCKED" -> text("status.blocked");
            case "SKIPPED" -> text("status.skipped");
            case "USED" -> text("status.used");
            case "FAILED" -> text("status.failed");
            default -> nullToDash(status);
        };
    }

    private String planStatusLabel(String status) {
        return switch (ViewTextUtils.upper(status)) {
            case "CREATED" -> text("status.created");
            case "READY" -> text("status.executionCandidate");
            case "VALIDATED" -> text("status.validated");
            case "BLOCKED" -> text("status.blocked");
            case "PARTIALLY_BLOCKED" -> text("status.partiallyBlocked");
            case "NO_CANDIDATE" -> text("status.noCandidate");
            case "FAILED" -> text("status.failed");
            case "ERROR" -> text("status.error");
            default -> nullToDash(status);
        };
    }

    private String executionStatusLabel(String status) {
        return switch (ViewTextUtils.upper(status)) {
            case "CANDIDATE" -> text("status.candidate");
            case "READY" -> text("status.orderPending");
            case "SUBMITTED" -> text("status.submitted");
            case "SENT" -> text("status.sent");
            case "ACCEPTED" -> text("status.accepted");
            case "FILLED" -> text("status.filled");
            case "PARTIAL_FILLED" -> text("status.partialFilled");
            case "BLOCKED" -> text("status.blocked");
            case "SKIPPED" -> text("status.skipped");
            case "FAILED" -> text("status.failed");
            case "ERROR" -> text("status.error");
            case "CANCELED" -> text("status.canceled");
            case "CANCEL_ACCEPTED" -> text("status.cancelAccepted");
            default -> nullToDash(status);
        };
    }

    private String stepCodeShortLabel(String stepCode) {
        return switch (ViewTextUtils.upper(stepCode)) {
            case "CONNECTOR_BALANCE", "BALANCE_BEFORE" -> text("slack.step.connectorBalance");
            case "INTEREST_CRAWLER" -> text("slack.step.interestCrawler");
            case "PREPROCESSOR" -> text("slack.step.preprocessor");
            case "BACKTEST_RESEARCH" -> text("slack.step.backtestResearch");
            case "BACKTEST_REPORT" -> text("slack.step.backtestReport");
            case "DAILY_BUY_SIGNAL" -> text("slack.step.dailyBuySignal");
            case "DAILY_POSITION_SIGNAL" -> text("slack.step.dailyPositionSignal");
            case "DAILY_BUY_EXECUTION" -> text("slack.step.dailyBuyExecution");
            case "DAILY_SELL_EXECUTION" -> text("slack.step.dailySellExecution");
            case "DAILY_AUTO_BUY" -> text("slack.step.dailyAutoBuy");
            case "CONNECTOR_ORDER_CHECK" -> text("slack.step.connectorOrderCheck");
            case "SYNC_BUY_FILL" -> text("slack.step.syncBuyFill");
            case "SYNC_BUY_POSITION" -> text("slack.step.syncBuyPosition");
            case "BALANCE_REFRESH", "BALANCE_AFTER" -> text("slack.step.balanceRefresh");
            case "DAILY_AUTO_SELL" -> text("slack.step.dailyAutoSell");
            case "SYNC_SELL_FILL" -> text("slack.step.syncSellFill");
            case "MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE" -> text("slack.step.strategyOrderExecute");
            default -> nullToDash(stepCode);
        };
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private String nullToDash(String value) {
        return ViewTextUtils.blankTo(value, "-");
    }

    private String shorten(String value, int maxLength) {
        if (ViewTextUtils.isBlank(value)) {
            return "-";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength) + "...";
    }

    private String formatKstDateTime(java.time.OffsetDateTime value) {
        if (value == null) {
            return "-";
        }

        return value.atZoneSameInstant(KST).format(DATE_TIME_FORMATTER);
    }
}
