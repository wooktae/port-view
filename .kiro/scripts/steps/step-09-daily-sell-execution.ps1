# Step 09 - DAILY_SELL_EXECUTION
# Runs strategy execution daily sell execution on ECS Fargate.
# No secret values are stored in this file.
# This step creates/updates execution orders only.
# It does not submit broker/KIS orders.

Write-Host "============================================================"
Write-Host "[STEP 09] DAILY_SELL_EXECUTION START"
Write-Host "============================================================"

$result = Invoke-DailyAwsPaperEcsTask `
    -StepCode "DAILY_SELL_EXECUTION" `
    -Cluster $EcsCluster `
    -TaskDefinition $StrategyExecutionTaskDefinition `
    -ContainerName $StrategyExecutionContainerName `
    -Subnets $EcsPublicSubnets `
    -SecurityGroups $StrategyTaskSecurityGroups `
    -AssignPublicIp "ENABLED" `
    -Command @("python", "daily_sell_execution_run.py") `
    -Region $DefaultRegion `
    -LogGroupName $StrategyExecutionLogGroup `
    -LogStreamPrefix $StrategyExecutionLogStreamPrefix `
    -PollSeconds 10 `
    -TimeoutSeconds 1800

Write-Host "============================================================"
Write-Host "[STEP 09] DAILY_SELL_EXECUTION END"
Write-Host "============================================================"
Write-Host ("[STEP 09] taskArn={0}" -f $result.TaskArn)
Write-Host ("[STEP 09] taskId={0}" -f $result.TaskId)
Write-Host ("[STEP 09] exitCode={0}" -f $result.ExitCode)
Write-Host ("[STEP 09] cloudWatchLog={0}" -f $result.CloudWatchLogPath)

return [pscustomobject]@{
    StepCode = "DAILY_SELL_EXECUTION"
    Status = "SUCCESS"
    Runner = "ECS"
    TaskArn = $result.TaskArn
    TaskId = $result.TaskId
    ExitCode = $result.ExitCode
    CloudWatchLogPath = $result.CloudWatchLogPath
}
