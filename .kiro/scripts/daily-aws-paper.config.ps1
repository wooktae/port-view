# Daily AWS Paper config
# This file contains shared constants only.
# Do not store secret values here.

$WorkspaceRoot = "C:\Workspaces\port-view\.kiro"

$BaseOutputDir = "C:\Temp\portfolio-daily-aws-paper"

$DefaultRegion = "ap-northeast-2"
$DefaultEnvironment = "aws-paper"

$EcsCluster = "portfolio-paper-cluster"

# ECS network config for aws-paper Fargate tasks.
# Public subnets are used with assignPublicIp=ENABLED.
# Do not store secret values, passwords, tokens, account numbers, endpoints, or ARNs here.
$EcsPublicSubnets = @(
    "subnet-0b4ab98b1f0ee2d9c",
    "subnet-0cf35c54a9933a25d"
)

$PreprocessorSecurityGroups = @(
    "sg-0e3857a190f522c3e"
)

$PreprocessorTaskDefinition = "portfolio-paper-interest-preprocessor:1"
$PreprocessorContainerName = "interest-preprocessor"
$PreprocessorLogGroup = "/portfolio/paper/preprocessor"
$PreprocessorLogStreamPrefix = "ecs"

$InterestCrawlerSecurityGroups = @(
    "sg-05e3ba206c34e72d1"
)

$InterestCrawlerTaskDefinition = "portfolio-paper-interest-crawler:7"
$InterestCrawlerContainerName = "interest-crawler"
$InterestCrawlerLogGroup = "/portfolio/paper/crawler"
$InterestCrawlerLogStreamPrefix = "ecs-crawler-nongui-daily"


$StrategyTaskSecurityGroups = @(
    "sg-0a05b70f33b6a88be"
)

$DailyBuySignalTaskDefinition = "portfolio-paper-strategy-decision-buy-signal:1"
$DailyBuySignalContainerName = "strategy-decision-buy-signal"
$DailyBuySignalLogGroup = "/portfolio/paper/strategy-decision"
$DailyBuySignalLogStreamPrefix = "buy-signal"

$DailyPositionSignalTaskDefinition = "portfolio-paper-strategy-decision-position-signal:1"
$DailyPositionSignalContainerName = "strategy-decision-position-signal"
$DailyPositionSignalLogGroup = "/portfolio/paper/strategy-decision"
$DailyPositionSignalLogStreamPrefix = "position-signal"

$StrategyExecutionTaskDefinition = "portfolio-paper-strategy-execution:1"
$StrategyExecutionContainerName = "strategy-execution"
$StrategyExecutionLogGroup = "/portfolio/paper/strategy-execution"
$StrategyExecutionLogStreamPrefix = "strategy-execution"

$BatchResearchJobQueue = "portfolio-paper-strategy-research-queue"
$BacktestResearchJobDefinition = "portfolio-paper-strategy-research:5"
$BacktestReportJobDefinition = "portfolio-paper-strategy-report:3"
$BatchResearchLogGroup = "/portfolio/paper/strategy-research"
$BatchResearchLogStreamPrefix = "strategy-research"






# Resource identifiers currently used by the operator-run aws-paper wrapper.
# Do not add secret values, passwords, tokens, account numbers, endpoints, or ARNs here.
$MarketConnectorInstanceId = "i-0fce77927b7397b88"
$CrawlerWorkerInstanceId = "i-0ff768ea639a91355"

$StepDirectoryName = "steps"
