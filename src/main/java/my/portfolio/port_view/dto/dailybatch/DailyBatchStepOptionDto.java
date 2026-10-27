package my.portfolio.port_view.dto.dailybatch;

import my.portfolio.port_view.util.ViewMessages;

public record DailyBatchStepOptionDto(
        Integer stepOrder,
        String stepCode,
        String stepName
) {

    public String displayLabel() {
        String no = stepOrder == null ? "-" : String.valueOf(stepOrder);

        return no + ". " + localizedStepName();
    }

    private String localizedStepName() {
        String fallback = stepName == null || stepName.isBlank() ? stepCode : stepName;

        return switch (stepCode == null ? "" : stepCode.trim()) {
            case "CONNECTOR_BALANCE" -> ViewMessages.text("daily.step.connectorBalance");
            case "INTEREST_CRAWLER" -> ViewMessages.text("daily.step.interestCrawler");
            case "PREPROCESSOR" -> ViewMessages.text("daily.step.preprocessor");
            case "BACKTEST_RESEARCH" -> ViewMessages.text("daily.step.backtestResearch");
            case "BACKTEST_REPORT" -> ViewMessages.text("daily.step.backtestReport");
            case "DAILY_BUY_SIGNAL" -> ViewMessages.text("daily.step.dailyBuySignal");
            case "DAILY_POSITION_SIGNAL" -> ViewMessages.text("daily.step.dailyPositionSignal");
            case "DAILY_BUY_EXECUTION" -> ViewMessages.text("daily.step.dailyBuyExecution");
            case "DAILY_SELL_EXECUTION" -> ViewMessages.text("daily.step.dailySellExecution");
            case "DAILY_AUTO_SELL" -> ViewMessages.text("daily.step.dailyAutoSell");
            case "DAILY_AUTO_BUY" -> ViewMessages.text("daily.step.dailyAutoBuy");
            case "MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE" ->
                    ViewMessages.text("daily.step.strategyOrderExecution");
            case "CONNECTOR_ORDER_CHECK" -> ViewMessages.text("daily.step.connectorOrderCheck");
            case "SYNC_SELL_FILL" -> ViewMessages.text("daily.step.syncSellFill");
            case "SYNC_BUY_FILL" -> ViewMessages.text("daily.step.syncBuyFill");
            case "SYNC_BUY_POSITION" -> ViewMessages.text("daily.step.syncBuyPosition");
            case "BALANCE_REFRESH" -> ViewMessages.text("daily.step.balanceRefresh");
            default -> fallback;
        };
    }
}
