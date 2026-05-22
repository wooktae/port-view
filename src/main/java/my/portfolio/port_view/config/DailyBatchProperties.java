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
 * - Java 코드 내 로컬 workspace 경로 하드코딩 제거
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
    private String marketconnectorDir;

    /**
     * Interest Crawler 작업 디렉토리.
     *
     * application-local.properties 또는 환경변수로 지정한다.
     */
    private String interestCrawlerDir;

    /**
     * Interest Preprocessor 작업 디렉토리.
     *
     * application-local.properties 또는 환경변수로 지정한다.
     */
    private String preprocessorDir;

    /**
     * Strategy Execution 작업 디렉토리.
     *
     * application-local.properties 또는 환경변수로 지정한다.
     */
    private String executionDir;

    /**
     * Daily Batch 기본 계좌번호.
     */
    private String defaultAccountNo;

    /**
     * 실행 환경.
     *
     * 예:
     * - local
     * - dev
     * - paper
     */
    private String environment = "local";

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
}
