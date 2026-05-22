package my.portfolio.port_view.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.portfolio.port_view.dto.DailyBatchRunDto;
import my.portfolio.port_view.dto.DailyBatchStepLogDto;
import my.portfolio.port_view.dto.DailyPositionDecisionDto;
import my.portfolio.port_view.dto.DailyRunDto;
import my.portfolio.port_view.dto.DailySignalDto;
import my.portfolio.port_view.dto.StrategyExecutionOrderDto;
import my.portfolio.port_view.dto.StrategyExecutionPlanDto;
import my.portfolio.port_view.entity.ConnectorBalanceSnapshot;
import my.portfolio.port_view.entity.ConnectorPositionSnapshot;
import my.portfolio.port_view.repository.ConnectorBalanceSnapshotRepository;
import my.portfolio.port_view.repository.ConnectorPositionSnapshotRepository;
import my.portfolio.port_view.repository.DailyBatchRepository;
import my.portfolio.port_view.repository.StrategyDailyViewRepository;
import my.portfolio.port_view.repository.StrategyExecutionQueryRepository;
import my.portfolio.port_view.util.DailyBatchLabelUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    @Value("${slack.enabled:false}")
    private boolean slackEnabled;

    @Value("${slack.webhook-url:}")
    private String webhookUrl;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");

    // =====================================================
    // Public Methods
    // =====================================================

    public void sendTestMessage() {
        sendText("""
                ✅ Portfolio Slack 테스트 메시지
                
                port-view에서 Slack Incoming Webhook 전송이 정상 동작합니다.
                """);
    }

    public void sendDailyBatchSummary(Long batchRunId) {
        if (batchRunId == null) {
            return;
        }

        DailyBatchRunDto run = dailyBatchRepository.findRunById(batchRunId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Daily Batch 실행 정보를 찾을 수 없음. batchRunId=" + batchRunId
                ));

        // 핵심 수정:
        // RUNNING / PENDING 등 최종 상태가 아닌 경우 Slack 전송하지 않음
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
        String statusLabel = DailyBatchLabelUtils.runStatusLabel(run.runStatus());

        StringBuilder sb = new StringBuilder();

        sb.append(statusEmoji)
                .append(" Portfolio Daily Batch ")
                .append(statusLabel)
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
        sb.append("[Batch]\n");
        sb.append("- 실행 ID: #").append(run.id()).append("\n");
        sb.append("- 실행일: ").append(run.batchDate()).append("\n");
        sb.append("- 상태: ").append(DailyBatchLabelUtils.runStatusLabel(run.runStatus())).append("\n");
        sb.append("- 계좌: ").append(nullToDash(run.accountNo())).append("\n");
        sb.append("- 환경: ").append(nullToDash(run.environment())).append("\n");
        sb.append("- Step: ")
                .append(nvl(run.successStepCount())).append(" 성공 / ")
                .append(nvl(run.noTargetStepCount())).append(" 대상 없음 / ")
                .append(nvl(run.failedStepCount())).append(" 실패 / ")
                .append(nvl(run.skippedStepCount())).append(" 건너뜀")
                .append("\n");

        if (run.startedAt() != null) {
            sb.append("- 시작: ")
                    .append(formatKstDateTime(run.startedAt()))
                    .append("\n");
        }

        if (run.finishedAt() != null) {
            sb.append("- 종료: ")
                    .append(formatKstDateTime(run.finishedAt()))
                    .append("\n");
        }

        if (run.durationMs() != null) {
            sb.append("- 소요시간: ")
                    .append(formatDuration(run.durationMs()))
                    .append("\n");
        }

        if (run.isFailed()) {
            sb.append("\n[오류]\n");
            sb.append("- 실패 Step: ").append(nullToDash(run.errorStepCode())).append("\n");
            sb.append("- 메시지: ").append(shorten(run.errorMessage(), 700)).append("\n");
        }
    }

    private void appendStepSummary(
            StringBuilder sb,
            List<DailyBatchStepLogDto> steps
    ) {
        sb.append("\n[Step 요약]\n");

        if (steps == null || steps.isEmpty()) {
            sb.append("- 없음\n");
            return;
        }

        for (DailyBatchStepLogDto step : steps) {
            sb.append("- ")
                    .append(step.displayStepNo())
                    .append(". ")
                    .append(DailyBatchLabelUtils.stepCodeShortLabel(step.stepCode()))
                    .append(": ")
                    .append(DailyBatchLabelUtils.stepStatusLabel(step.stepStatus()));

            if (step.durationMs() != null) {
                sb.append(" / ").append(formatDuration(step.durationMs()));
            }

            sb.append("\n");
        }
    }

    private void appendDailySummary(StringBuilder sb) {
        sb.append("\n[시장 상태]\n");

        Optional<DailyRunDto> latestDailyRun = strategyDailyViewRepository.findLatestDailyRun();

        if (latestDailyRun.isEmpty()) {
            sb.append("- 최신 Daily Run 없음\n");
            return;
        }

        DailyRunDto dailyRun = latestDailyRun.get();

        sb.append("- 상태: ")
                .append(marketSignalLabel(dailyRun.marketSignal()))
                .append(" (")
                .append(nullToDash(dailyRun.marketSignal()))
                .append(")")
                .append("\n");

        sb.append("- 기준일: ")
                .append(dailyRun.dataDate() == null ? "-" : dailyRun.dataDate())
                .append("\n");

        sb.append("- 후보/신호: ")
                .append(nvl(dailyRun.candidateCount()))
                .append(" / ")
                .append(nvl(dailyRun.signalCount()))
                .append("\n");

        sb.append("- 노출/최대보유: ")
                .append(formatDecimal(dailyRun.baseExposure(), 2))
                .append(" / ")
                .append(nvl(dailyRun.maxPositions()))
                .append("개")
                .append("\n");

        sb.append("- 이유: ")
                .append(marketReasonText(dailyRun))
                .append("\n");

        appendDailySignalSummary(sb, dailyRun.id());
        appendDailyPositionDecisionSummary(sb, dailyRun.id());
    }

    private void appendDailySignalSummary(StringBuilder sb, Long dailyRunId) {
        if (dailyRunId == null) {
            return;
        }

        List<DailySignalDto> signals = strategyDailyViewRepository.findDailySignals(dailyRunId);

        if (signals == null || signals.isEmpty()) {
            sb.append("- Daily 매수 신호: 없음\n");
            return;
        }

        sb.append("- Daily 매수 신호: ")
                .append(signals.size())
                .append("건\n");

        int limit = Math.min(signals.size(), 3);

        for (int i = 0; i < limit; i++) {
            DailySignalDto signal = signals.get(i);

            sb.append("  · ")
                    .append(nullToDash(signal.companyName()))
                    .append("(")
                    .append(nullToDash(signal.tickerCode()))
                    .append(") ")
                    .append("rank ")
                    .append(signal.rankNo() == null ? "-" : signal.rankNo())
                    .append(" / ")
                    .append(signal.signalStatusLabel());

            if (signal.executionOrderId() != null) {
                sb.append(" / 주문 #").append(signal.executionOrderId());
            }

            sb.append("\n");
        }

        if (signals.size() > limit) {
            sb.append("  · 외 ")
                    .append(signals.size() - limit)
                    .append("건\n");
        }
    }

    private void appendDailyPositionDecisionSummary(StringBuilder sb, Long dailyRunId) {
        if (dailyRunId == null) {
            return;
        }

        List<DailyPositionDecisionDto> decisions =
                strategyDailyViewRepository.findDailyPositionDecisions(dailyRunId);

        if (decisions == null || decisions.isEmpty()) {
            sb.append("- Daily 포지션 판단: 없음\n");
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

        sb.append("- Daily 포지션 판단: ")
                .append("SELL ")
                .append(sellCount)
                .append(" / HOLD ")
                .append(holdCount)
                .append(" / SKIP ")
                .append(skipCount)
                .append("\n");
    }

    private void appendExecutionSummary(StringBuilder sb) {
        sb.append("\n[매수/매도 예정]\n");

        StrategyExecutionPlanDto latestPlan = strategyExecutionQueryRepository.findPlans()
                .stream()
                .findFirst()
                .orElse(null);

        if (latestPlan == null) {
            sb.append("- 실행 계획 없음\n");
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

        sb.append("- 실행 계획: #")
                .append(latestPlan.id())
                .append(" / ")
                .append(latestPlan.planDate())
                .append(" / ")
                .append(latestPlan.planStatusLabel())
                .append("\n");

        sb.append("- 시장 상태: ")
                .append(latestPlan.marketSignalLabel())
                .append("\n");

        appendOrderList(sb, "매수 예정", buyOrders);
        appendOrderList(sb, "매도 예정", sellOrders);
    }

    private void appendOrderList(
            StringBuilder sb,
            String title,
            List<StrategyExecutionOrderDto> orders
    ) {
        sb.append("- ").append(title).append(": ");

        if (orders == null || orders.isEmpty()) {
            sb.append("없음\n");
            return;
        }

        sb.append(orders.size()).append("건\n");

        int limit = Math.min(orders.size(), 5);

        for (int i = 0; i < limit; i++) {
            StrategyExecutionOrderDto order = orders.get(i);

            sb.append("  · ")
                    .append(nullToDash(order.stockName()))
                    .append("(")
                    .append(nullToDash(order.tickerCode()))
                    .append(") ")
                    .append(nvl(order.orderQty()))
                    .append("주 / ")
                    .append(order.executionStatusLabel());

            if (order.connectorOrderRequestId() != null) {
                sb.append(" / 커넥터 #").append(order.connectorOrderRequestId());
            }

            sb.append("\n");
        }

        if (orders.size() > limit) {
            sb.append("  · 외 ")
                    .append(orders.size() - limit)
                    .append("건\n");
        }
    }

    private void appendBalanceSummary(StringBuilder sb, String accountNo) {
        sb.append("\n[잔고]\n");

        if (accountNo == null || accountNo.isBlank()) {
            sb.append("- 계좌번호 없음\n");
            return;
        }

        Optional<ConnectorBalanceSnapshot> latestBalance =
                connectorBalanceSnapshotRepository.findTopByAccountNoOrderByAsOfDateDescAsOfTsDesc(accountNo);

        if (latestBalance.isEmpty()) {
            sb.append("- 최신 잔고 없음\n");
            return;
        }

        ConnectorBalanceSnapshot balance = latestBalance.get();

        sb.append("- 기준일: ")
                .append(balance.getAsOfDate())
                .append("\n");

        sb.append("- 총 평가금액: ")
                .append(formatMoney(balance.getTotalEvalAmount()))
                .append("\n");

        sb.append("- 현금: ")
                .append(formatMoney(balance.getCashBalance()))
                .append("\n");

        sb.append("- 평가손익: ")
                .append(formatMoney(balance.getEvalProfit()))
                .append("\n");
    }

    private void appendPositionSummary(StringBuilder sb, String accountNo) {
        sb.append("\n[보유 종목]\n");

        if (accountNo == null || accountNo.isBlank()) {
            sb.append("- 계좌번호 없음\n");
            return;
        }

        List<ConnectorPositionSnapshot> positions =
                connectorPositionSnapshotRepository.findLatestPositionsByAccountNo(accountNo);

        if (positions == null || positions.isEmpty()) {
            sb.append("- 없음\n");
            return;
        }

        sb.append("- 보유: ")
                .append(positions.size())
                .append("종목\n");

        int limit = Math.min(positions.size(), 5);

        for (int i = 0; i < limit; i++) {
            ConnectorPositionSnapshot position = positions.get(i);

            sb.append("  · ")
                    .append(nullToDash(position.getStockName()))
                    .append("(")
                    .append(nullToDash(position.getTickerCode()))
                    .append(") ")
                    .append(position.getQuantity() == null ? 0 : position.getQuantity())
                    .append("주 / 평가 ")
                    .append(formatMoney(position.getEvalAmount()))
                    .append(" / 손익 ")
                    .append(formatMoney(position.getEvalProfit()))
                    .append("\n");
        }

        if (positions.size() > limit) {
            sb.append("  · 외 ")
                    .append(positions.size() - limit)
                    .append("종목\n");
        }
    }

    // =====================================================
    // Slack Sender
    // =====================================================

    private void sendText(String text) {
        if (!slackEnabled) {
            log.info("Slack notification skipped. slack.enabled=false");
            return;
        }

        String normalizedWebhookUrl = normalizeWebhookUrl(webhookUrl);

        if (normalizedWebhookUrl.isBlank()) {
            log.warn("Slack notification skipped. slack.webhook-url is blank");
            return;
        }

        try {
            String payload = "{\"text\":\"" + jsonEscape(text) + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(normalizedWebhookUrl))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn(
                        "Slack notification failed. statusCode={}, body={}",
                        response.statusCode(),
                        response.body()
                );
            } else {
                log.info("Slack notification sent. statusCode={}", response.statusCode());
            }
        } catch (Exception e) {
            log.warn("Slack notification failed.", e);
        }
    }

    // =====================================================
    // Text Helpers
    // =====================================================

    private String marketSignalLabel(String marketSignal) {
        if (marketSignal == null || marketSignal.isBlank()) {
            return "-";
        }

        return switch (marketSignal.trim().toUpperCase()) {
            case "BLOCK" -> "차단";
            case "NEUTRAL" -> "중립";
            case "AGGRESSIVE" -> "공격";
            case "DEFENSIVE" -> "방어";
            default -> marketSignal;
        };
    }

    private String marketReasonText(DailyRunDto dailyRun) {
        String signal = dailyRun.marketSignal() == null ? "" : dailyRun.marketSignal().trim().toUpperCase();

        return switch (signal) {
            case "BLOCK" -> "신규 매수 차단 / 현금 방어 우선";
            case "NEUTRAL" -> "제한적 매수 가능 / 좋은 후보만 선별";
            case "AGGRESSIVE" -> "공격 매수 가능 / 후보 적극 검토";
            case "DEFENSIVE" -> "방어 운용 / 신규 매수 축소";
            default -> "시장 판단 상태 확인 필요";
        };
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "-";
        }

        BigDecimal rounded = value.setScale(0, RoundingMode.HALF_UP);
        return MONEY_FORMAT.format(rounded) + "원";
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
            return seconds + "초";
        }

        return minutes + "분 " + seconds + "초";
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String shorten(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength) + "...";
    }

    private String normalizeWebhookUrl(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim();

        if (normalized.length() >= 2
                && normalized.startsWith("\"")
                && normalized.endsWith("\"")) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }

        return normalized;
    }
    
    private String formatKstDateTime(java.time.OffsetDateTime value) {
        if (value == null) {
            return "-";
        }

        return value.atZoneSameInstant(KST).format(DATE_TIME_FORMATTER);
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }

        return sb.toString();
    }
}