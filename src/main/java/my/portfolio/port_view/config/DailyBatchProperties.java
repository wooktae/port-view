package my.portfolio.port_view.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Daily Batch Pipeline execution configuration.
 *
 * Binds the portfolio.batch.* values from application.properties.
 *
 * Purpose:
 * - Manage Python execution commands
 * - Manage working directories for each Microservice
 * - Manage timeout and log tail length
 * - Manage Daily Batch execution mode, DB target, and execution gates
 * - Eliminate hard-coded local workspace paths from Java code
 * - Block order-related steps behind separate gates during local AWS Paper execution validation
 */
@Component
@ConfigurationProperties(prefix = "portfolio.batch")
public class DailyBatchProperties {

    /**
     * Python execution command.
     *
     * local Windows:
     * - python
     *
     * Linux/AWS:
     * - python3
     */
    private String pythonExecutable = "python";

    /**
     * Root of the complete workspace.
     *
     * Specify through application-local.properties or an environment variable.
     */
    private String workspaceRoot = "";

    /**
     * Market Connector working directory.
     *
     * Specify through application-local.properties or an environment variable.
     */
    private String marketconnectorDir = "";

    /**
     * Interest Crawler working directory.
     *
     * Specify through application-local.properties or an environment variable.
     */
    private String interestCrawlerDir = "";

    /**
     * Interest Preprocessor working directory.
     *
     * Specify through application-local.properties or an environment variable.
     */
    private String preprocessorDir = "";

    /**
     * Strategy Execution working directory.
     *
     * Specify through application-local.properties or an environment variable.
     */
    private String executionDir = "";

    /**
     * Default account number for Daily Batch.
     */
    private String defaultAccountNo = "";

    /**
     * Execution environment.
     *
     * Examples:
     * - local
     * - paper
     * - live
     */
    private String environment = "local";

    /**
     * Daily Batch DB target.
     *
     * Examples:
     * - local-fixture-db
     * - aws-paper-rds
     * - aws-live-rds
     *
     * Caution:
     * - During aws-paper validation, the DB target must be aws-paper-rds even when execution is local.
     */
    private String dbTarget = "local-fixture-db";

    /**
     * Daily Batch execution mode.
     *
     * Examples:
     * - local-file
     * - aws-stepfunctions
     *
     * local-file:
     * - The View server runs local Python files through ProcessBuilder.
     *
     * aws-stepfunctions:
     * - The View server queries AWS Step Functions execution state or calls StartExecution.
     */
    private String executionMode = "local-file";

    /**
     * Master gate for Daily Batch execution.
     *
     * When false, the server blocks complete, partial, and retry Daily Pipeline execution.
     */
    private boolean executionEnabled = false;

    /**
     * Local File execution gate.
     *
     * When false, blocks ProcessBuilder-based Python file execution.
     */
    private boolean localFileExecutionEnabled = false;

    /**
     * StartExecution gate for complete AWS Step Functions execution.
     *
     * When false, blocks starting the complete workflow even in aws-stepfunctions mode.
     */
    private boolean awsStepfunctionsStartEnabled = false;

    /**
     * Gate for executing individual AWS Step Functions steps.
     *
     * When false, blocks individual step execution through a step-only workflow or dispatcher.
     */
    private boolean awsStepfunctionsStepStartEnabled = false;

    /**
     * AWS Step Functions region.
     */
    private String awsStepfunctionsRegion = "ap-northeast-2";

    /**
     * AWS Step Functions state machine ARN.
     *
     * ARN for the Steps 1~11 safe trigger or default Daily workflow.
     * Inject the actual value through an environment variable rather than writing it directly in a properties file.
     */
    private String awsStepfunctionsStateMachineArn = "";

    /**
     * AWS Step Functions approval state machine ARN.
     *
     * Dedicated ARN for the approval-gated Steps 12~17 workflow.
     * Inject the actual value through an environment variable rather than writing it directly in a properties file.
     */
    private String awsStepfunctionsApprovalStateMachineArn = "";

    /**
     * AWS Step Functions execution name prefix.
     */
    private String awsStepfunctionsExecutionNamePrefix = "port-view-daily";

    /**
     * Manual Intraday Position check gate.
     *
     * When false, the server blocks /daily-batch/intraday-monitor/run.
     */
    private boolean intradayMonitorEnabled = false;

    /**
     * Gate for view actions such as Slack test and Slack summary resend.
     *
     * When false, users cannot invoke Slack actions directly from the Daily Batch view.
     */
    private boolean slackActionEnabled = false;

    /**
     * Gate for automatically sending a Slack summary after Daily Batch completes.
     *
     * When false, sendDailyBatchSummary is not called after Daily Batch execution completes.
     */
    private boolean slackSummaryEnabled = false;

    /**
     * AWS Paper order-related execution gate.
     *
     * When false, the server blocks the order-related steps below.
     *
     * - Step 10 DAILY_AUTO_SELL
     * - Step 11 DAILY_AUTO_BUY
     * - Step 12 MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE
     *
     * Steps 13~17 are post-submission follow-up, so this gate is also checked
     * when a range execution enables Step 12 or later.
     */
    private boolean paperOrderEnabled = false;

    /**
     * Minimum step order allowed for Local File execution.
     *
     * Default for the second AWS Paper validation:
     * - 1
     */
    private int minExecutableStepOrder = 1;

    /**
     * Maximum step order allowed for Local File execution.
     *
     * Default for the second AWS Paper validation:
     * - 11
     *
     * Steps 12~17 are enabled separately after confirming paperOrderEnabled=true.
     */
    private int maxExecutableStepOrder = 11;

    /**
     * Gate for the complete Steps 1~17 execution button.
     *
     * When false, the server blocks complete execution even if local-file execution is enabled.
     * Use range execution first during the second validation.
     */
    private boolean fullPipelineExecutionEnabled = false;

    /**
     * Timeout for each Python command.
     *
     * Unit: minutes
     */
    private long commandTimeoutMinutes = 60L;

    /**
     * Stored stdout/stderr tail length.
     *
     * The DB stores only the final N characters rather than the complete log.
     */
    private int logTailLength = 8000;

    public String getPythonExecutable() {
        return pythonExecutable;
    }

    public void setPythonExecutable(String pythonExecutable) {
        this.pythonExecutable = pythonExecutable;
    }

    public String getWorkspaceRoot() {
        return workspaceRoot;
    }

    public void setWorkspaceRoot(String workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    public String getMarketconnectorDir() {
        return marketconnectorDir;
    }

    public void setMarketconnectorDir(String marketconnectorDir) {
        this.marketconnectorDir = marketconnectorDir;
    }

    public String getInterestCrawlerDir() {
        return interestCrawlerDir;
    }

    public void setInterestCrawlerDir(String interestCrawlerDir) {
        this.interestCrawlerDir = interestCrawlerDir;
    }

    public String getPreprocessorDir() {
        return preprocessorDir;
    }

    public void setPreprocessorDir(String preprocessorDir) {
        this.preprocessorDir = preprocessorDir;
    }

    public String getExecutionDir() {
        return executionDir;
    }

    public void setExecutionDir(String executionDir) {
        this.executionDir = executionDir;
    }

    public String getDefaultAccountNo() {
        return defaultAccountNo;
    }

    public void setDefaultAccountNo(String defaultAccountNo) {
        this.defaultAccountNo = defaultAccountNo;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getDbTarget() {
        return dbTarget;
    }

    public void setDbTarget(String dbTarget) {
        this.dbTarget = dbTarget;
    }

    public String getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(String executionMode) {
        this.executionMode = executionMode;
    }

    public boolean isExecutionEnabled() {
        return executionEnabled;
    }

    public void setExecutionEnabled(boolean executionEnabled) {
        this.executionEnabled = executionEnabled;
    }

    public boolean isLocalFileExecutionEnabled() {
        return localFileExecutionEnabled;
    }

    public void setLocalFileExecutionEnabled(boolean localFileExecutionEnabled) {
        this.localFileExecutionEnabled = localFileExecutionEnabled;
    }

    public boolean isAwsStepfunctionsStartEnabled() {
        return awsStepfunctionsStartEnabled;
    }

    public void setAwsStepfunctionsStartEnabled(boolean awsStepfunctionsStartEnabled) {
        this.awsStepfunctionsStartEnabled = awsStepfunctionsStartEnabled;
    }

    public boolean isAwsStepfunctionsStepStartEnabled() {
        return awsStepfunctionsStepStartEnabled;
    }

    public void setAwsStepfunctionsStepStartEnabled(boolean awsStepfunctionsStepStartEnabled) {
        this.awsStepfunctionsStepStartEnabled = awsStepfunctionsStepStartEnabled;
    }


    public String getAwsStepfunctionsRegion() {
        return awsStepfunctionsRegion;
    }

    public void setAwsStepfunctionsRegion(String awsStepfunctionsRegion) {
        this.awsStepfunctionsRegion = awsStepfunctionsRegion;
    }

    public String getAwsStepfunctionsStateMachineArn() {
        return awsStepfunctionsStateMachineArn;
    }

    public void setAwsStepfunctionsStateMachineArn(String awsStepfunctionsStateMachineArn) {
        this.awsStepfunctionsStateMachineArn = awsStepfunctionsStateMachineArn;
    }

    public String getAwsStepfunctionsApprovalStateMachineArn() {
        return awsStepfunctionsApprovalStateMachineArn;
    }

    public void setAwsStepfunctionsApprovalStateMachineArn(String awsStepfunctionsApprovalStateMachineArn) {
        this.awsStepfunctionsApprovalStateMachineArn = awsStepfunctionsApprovalStateMachineArn;
    }

    public String getAwsStepfunctionsExecutionNamePrefix() {
        return awsStepfunctionsExecutionNamePrefix;
    }

    public void setAwsStepfunctionsExecutionNamePrefix(String awsStepfunctionsExecutionNamePrefix) {
        this.awsStepfunctionsExecutionNamePrefix = awsStepfunctionsExecutionNamePrefix;
    }
    public boolean isIntradayMonitorEnabled() {
        return intradayMonitorEnabled;
    }

    public void setIntradayMonitorEnabled(boolean intradayMonitorEnabled) {
        this.intradayMonitorEnabled = intradayMonitorEnabled;
    }

    public boolean isSlackActionEnabled() {
        return slackActionEnabled;
    }

    public void setSlackActionEnabled(boolean slackActionEnabled) {
        this.slackActionEnabled = slackActionEnabled;
    }

    public boolean isSlackSummaryEnabled() {
        return slackSummaryEnabled;
    }

    public void setSlackSummaryEnabled(boolean slackSummaryEnabled) {
        this.slackSummaryEnabled = slackSummaryEnabled;
    }

    public boolean isPaperOrderEnabled() {
        return paperOrderEnabled;
    }

    public void setPaperOrderEnabled(boolean paperOrderEnabled) {
        this.paperOrderEnabled = paperOrderEnabled;
    }

    public int getMinExecutableStepOrder() {
        return minExecutableStepOrder;
    }

    public void setMinExecutableStepOrder(int minExecutableStepOrder) {
        this.minExecutableStepOrder = minExecutableStepOrder;
    }

    public int getMaxExecutableStepOrder() {
        return maxExecutableStepOrder;
    }

    public void setMaxExecutableStepOrder(int maxExecutableStepOrder) {
        this.maxExecutableStepOrder = maxExecutableStepOrder;
    }

    public boolean isFullPipelineExecutionEnabled() {
        return fullPipelineExecutionEnabled;
    }

    public void setFullPipelineExecutionEnabled(boolean fullPipelineExecutionEnabled) {
        this.fullPipelineExecutionEnabled = fullPipelineExecutionEnabled;
    }

    public long getCommandTimeoutMinutes() {
        return commandTimeoutMinutes;
    }

    public void setCommandTimeoutMinutes(long commandTimeoutMinutes) {
        this.commandTimeoutMinutes = commandTimeoutMinutes;
    }

    public int getLogTailLength() {
        return logTailLength;
    }

    public void setLogTailLength(int logTailLength) {
        this.logTailLength = logTailLength;
    }

    public boolean isLocalFileMode() {
        return "local-file".equalsIgnoreCase(executionMode);
    }

    public boolean isAwsStepfunctionsMode() {
        return "aws-stepfunctions".equalsIgnoreCase(executionMode);
    }

    public boolean isAwsPaperDbTarget() {
        return "aws-paper-rds".equalsIgnoreCase(dbTarget);
    }

    public boolean canRunLocalFileBatch() {
        return executionEnabled && isLocalFileMode() && localFileExecutionEnabled;
    }

    public boolean canRunFullLocalFilePipeline() {
        return canRunLocalFileBatch() && fullPipelineExecutionEnabled;
    }

    public boolean canStartAwsStepfunctions() {
        return executionEnabled && isAwsStepfunctionsMode() && awsStepfunctionsStartEnabled;
    }

    public boolean canStartAwsStepfunctionsStep() {
        return executionEnabled && isAwsStepfunctionsMode() && awsStepfunctionsStepStartEnabled;
    }

    public boolean isStepOrderInExecutableRange(int stepOrder) {
        return stepOrder >= minExecutableStepOrder && stepOrder <= maxExecutableStepOrder;
    }

    public boolean isOrderLikeStepOrder(int stepOrder) {
        return stepOrder >= 10;
    }

    public boolean isBrokerSubmitStepOrder(int stepOrder) {
        return stepOrder == 12;
    }

    public boolean isPostOrderFollowUpStepOrder(int stepOrder) {
        return stepOrder >= 13;
    }

    public boolean canRunStepOrder(int stepOrder) {
        if (!canRunLocalFileBatch()) {
            return false;
        }

        if (!isStepOrderInExecutableRange(stepOrder)) {
            return false;
        }

        if (isOrderLikeStepOrder(stepOrder) && !paperOrderEnabled) {
            return false;
        }

        return true;
    }

    public boolean canRunStepRange(int fromStepOrder, int toStepOrder) {
        if (!canRunLocalFileBatch()) {
            return false;
        }

        if (fromStepOrder <= 0 || toStepOrder <= 0) {
            return false;
        }

        if (fromStepOrder > toStepOrder) {
            return false;
        }

        for (int stepOrder = fromStepOrder; stepOrder <= toStepOrder; stepOrder++) {
            if (!canRunStepOrder(stepOrder)) {
                return false;
            }
        }

        return true;
    }
}