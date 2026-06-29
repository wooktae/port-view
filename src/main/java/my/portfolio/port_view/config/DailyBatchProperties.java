package my.portfolio.port_view.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Daily Batch Pipeline 실행 설정.
 *
 * application.properties 의 portfolio.batch.* 값을 바인딩한다.
 *
 * 목적:
 * - Python 실행 명령어 관리
 * - 각 Microservice 작업 디렉토리 관리
 * - timeout / log tail 길이 관리
 * - Daily Batch 실행 모드 / DB target / 실행 gate 관리
 * - Java 코드 내 로컬 workspace 경로 하드코딩 제거
 * - AWS Paper 로컬 실행 검증 시 주문성 step을 별도 gate로 차단
 */
@Component
@ConfigurationProperties(prefix = "portfolio.batch")
public class DailyBatchProperties {

    /**
     * Python 실행 명령.
     *
     * local Windows:
     * - python
     *
     * Linux/AWS:
     * - python3
     */
    private String pythonExecutable = "python";

    /**
     * 전체 workspace root.
     *
     * application-local.properties 또는 환경변수로 지정한다.
     */
    private String workspaceRoot = "";

    /**
     * Market Connector 작업 디렉토리.
     *
     * application-local.properties 또는 환경변수로 지정한다.
     */
    private String marketconnectorDir = "";

    /**
     * Interest Crawler 작업 디렉토리.
     *
     * application-local.properties 또는 환경변수로 지정한다.
     */
    private String interestCrawlerDir = "";

    /**
     * Interest Preprocessor 작업 디렉토리.
     *
     * application-local.properties 또는 환경변수로 지정한다.
     */
    private String preprocessorDir = "";

    /**
     * Strategy Execution 작업 디렉토리.
     *
     * application-local.properties 또는 환경변수로 지정한다.
     */
    private String executionDir = "";

    /**
     * Daily Batch 기본 계좌번호.
     */
    private String defaultAccountNo = "";

    /**
     * 실행 환경.
     *
     * 예:
     * - local
     * - paper
     * - live
     */
    private String environment = "local";

    /**
     * Daily Batch DB target.
     *
     * 예:
     * - local-fixture-db
     * - aws-paper-rds
     * - aws-live-rds
     *
     * 주의:
     * - aws-paper 검증에서는 로컬에서 실행하더라도 DB target은 aws-paper-rds 여야 한다.
     */
    private String dbTarget = "local-fixture-db";

    /**
     * Daily Batch 실행 모드.
     *
     * 예:
     * - local-file
     * - aws-stepfunctions
     *
     * local-file:
     * - View 서버가 로컬 Python 파일을 ProcessBuilder로 실행한다.
     *
     * aws-stepfunctions:
     * - View 서버가 AWS Step Functions 실행 상태를 조회하거나 StartExecution을 호출한다.
     */
    private String executionMode = "local-file";

    /**
     * Daily Batch 실행 전체 gate.
     *
     * false이면 Daily Pipeline 전체 실행 / 부분 실행 / 재실행을 서버단에서 차단한다.
     */
    private boolean executionEnabled = false;

    /**
     * 로컬 파일 실행 gate.
     *
     * false이면 ProcessBuilder 기반 Python 파일 실행을 차단한다.
     */
    private boolean localFileExecutionEnabled = false;

    /**
     * AWS Step Functions 전체 실행 StartExecution gate.
     *
     * false이면 aws-stepfunctions mode에서도 전체 workflow 시작을 차단한다.
     */
    private boolean awsStepfunctionsStartEnabled = false;

    /**
     * AWS Step Functions 개별 step 실행 gate.
     *
     * false이면 step-only workflow 또는 dispatcher 기반 개별 step 실행을 차단한다.
     */
    private boolean awsStepfunctionsStepStartEnabled = false;

    /**
     * AWS Step Functions region.
     */
    private String awsStepfunctionsRegion = "ap-northeast-2";

    /**
     * AWS Step Functions state machine ARN.
     *
     * 실제 값은 properties 파일에 직접 쓰지 않고 환경변수로 주입한다.
     */
    private String awsStepfunctionsStateMachineArn = "";

    /**
     * AWS Step Functions execution name prefix.
     */
    private String awsStepfunctionsExecutionNamePrefix = "port-view-daily";

    /**
     * 장중 포지션 점검 수동 실행 gate.
     *
     * false이면 /daily-batch/intraday-monitor/run 을 서버단에서 차단한다.
     */
    private boolean intradayMonitorEnabled = false;

    /**
     * Slack 테스트 / Slack 요약 재전송 같은 화면 액션 gate.
     *
     * false이면 사용자가 Daily Batch 화면에서 Slack 액션을 직접 호출할 수 없다.
     */
    private boolean slackActionEnabled = false;

    /**
     * Daily Batch 완료 후 Slack summary 자동 전송 gate.
     *
     * false이면 Daily Batch 실행 완료 후 sendDailyBatchSummary를 호출하지 않는다.
     */
    private boolean slackSummaryEnabled = false;

    /**
     * AWS Paper 주문성 실행 gate.
     *
     * false이면 아래 주문성 step을 서버단에서 차단한다.
     *
     * - Step 10 DAILY_AUTO_SELL
     * - Step 11 DAILY_AUTO_BUY
     * - Step 12 MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE
     *
     * Step 13~17은 주문 제출 이후 follow-up 성격이므로,
     * range 실행에서 Step 12 이후를 열 때도 이 gate를 같이 확인한다.
     */
    private boolean paperOrderEnabled = false;

    /**
     * 로컬 파일 실행 허용 최소 step order.
     *
     * AWS Paper 2차 검증 기본값:
     * - 1
     */
    private int minExecutableStepOrder = 1;

    /**
     * 로컬 파일 실행 허용 최대 step order.
     *
     * AWS Paper 2차 검증 기본값:
     * - 11
     *
     * Step 12~17은 paperOrderEnabled=true 확인 후 별도 확장한다.
     */
    private int maxExecutableStepOrder = 11;

    /**
     * 전체 1~17 실행 버튼 gate.
     *
     * false이면 local-file 실행이 켜져 있어도 전체 실행은 서버단에서 차단한다.
     * 2차 검증에서는 range 실행을 먼저 사용한다.
     */
    private boolean fullPipelineExecutionEnabled = false;

    /**
     * Python command 1개당 timeout.
     *
     * 단위: minutes
     */
    private long commandTimeoutMinutes = 60L;

    /**
     * stdout/stderr tail 저장 길이.
     *
     * DB에는 전체 로그를 저장하지 않고 마지막 N자만 저장한다.
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