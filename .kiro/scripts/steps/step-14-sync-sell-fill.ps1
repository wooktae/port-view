# Step 14 - SYNC_SELL_FILL
# Runs strategy execution sell fill sync on ECS Fargate.
# No secret values are stored in this file.
# This step does not submit broker/KIS orders.
# It may update sell fill/status data in DB.

Write-Host "============================================================"
Write-Host "[STEP 14] SYNC_SELL_FILL START"
Write-Host "============================================================"

$result = Invoke-DailyAwsPaperEcsTask `
    -StepCode "SYNC_SELL_FILL" `
    -Cluster $EcsCluster `
    -TaskDefinition $StrategyExecutionTaskDefinition `
    -ContainerName $StrategyExecutionContainerName `
    -Subnets $EcsPublicSubnets `
    -SecurityGroups $StrategyTaskSecurityGroups `
    -AssignPublicIp "ENABLED" `
    -Command @("python", "execution_sync_sell_fill.py") `
    -Region $DefaultRegion `
    -LogGroupName $StrategyExecutionLogGroup `
    -LogStreamPrefix $StrategyExecutionLogStreamPrefix `
    -PollSeconds 10 `
    -TimeoutSeconds 1800

Write-Host "============================================================"
Write-Host "[STEP 14] SYNC_SELL_FILL END"
Write-Host "============================================================"
Write-Host ("[STEP 14] taskArn={0}" -f $result.TaskArn)
Write-Host ("[STEP 14] taskId={0}" -f $result.TaskId)
Write-Host ("[STEP 14] exitCode={0}" -f $result.ExitCode)
Write-Host ("[STEP 14] cloudWatchLog={0}" -f $result.CloudWatchLogPath)

return [pscustomobject]@{
    StepCode = "SYNC_SELL_FILL"
    Status = "SUCCESS"
    Runner = "ECS"
    TaskArn = $result.TaskArn
    TaskId = $result.TaskId
    ExitCode = $result.ExitCode
    CloudWatchLogPath = $result.CloudWatchLogPath
}
