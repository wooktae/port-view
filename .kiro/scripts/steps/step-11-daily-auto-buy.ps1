# Step 11 - DAILY_AUTO_BUY
# Runs strategy execution daily auto buy on ECS Fargate.
# No secret values are stored in this file.
# This step may create/update execution-side buy orders.
# It does not directly submit broker/KIS orders.

Write-Host "============================================================"
Write-Host "[STEP 11] DAILY_AUTO_BUY START"
Write-Host "============================================================"

$result = Invoke-DailyAwsPaperEcsTask `
    -StepCode "DAILY_AUTO_BUY" `
    -Cluster $EcsCluster `
    -TaskDefinition $StrategyExecutionTaskDefinition `
    -ContainerName $StrategyExecutionContainerName `
    -Subnets $EcsPublicSubnets `
    -SecurityGroups $StrategyTaskSecurityGroups `
    -AssignPublicIp "ENABLED" `
    -Command @("python", "daily_auto_buy_execute_run.py", "--execute") `
    -Region $DefaultRegion `
    -LogGroupName $StrategyExecutionLogGroup `
    -LogStreamPrefix $StrategyExecutionLogStreamPrefix `
    -PollSeconds 10 `
    -TimeoutSeconds 1800

Write-Host "============================================================"
Write-Host "[STEP 11] DAILY_AUTO_BUY END"
Write-Host "============================================================"
Write-Host ("[STEP 11] taskArn={0}" -f $result.TaskArn)
Write-Host ("[STEP 11] taskId={0}" -f $result.TaskId)
Write-Host ("[STEP 11] exitCode={0}" -f $result.ExitCode)
Write-Host ("[STEP 11] cloudWatchLog={0}" -f $result.CloudWatchLogPath)

return [pscustomobject]@{
    StepCode = "DAILY_AUTO_BUY"
    Status = "SUCCESS"
    Runner = "ECS"
    TaskArn = $result.TaskArn
    TaskId = $result.TaskId
    ExitCode = $result.ExitCode
    CloudWatchLogPath = $result.CloudWatchLogPath
}
