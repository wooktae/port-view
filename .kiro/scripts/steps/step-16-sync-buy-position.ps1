# Step 16 - SYNC_BUY_POSITION
# Runs strategy execution buy position sync on ECS Fargate.
# No secret values are stored in this file.
# This step does not submit broker/KIS orders.
# It may update buy position data in DB.

Write-Host "============================================================"
Write-Host "[STEP 16] SYNC_BUY_POSITION START"
Write-Host "============================================================"

$result = Invoke-DailyAwsPaperEcsTask `
    -StepCode "SYNC_BUY_POSITION" `
    -Cluster $EcsCluster `
    -TaskDefinition $StrategyExecutionTaskDefinition `
    -ContainerName $StrategyExecutionContainerName `
    -Subnets $EcsPublicSubnets `
    -SecurityGroups $StrategyTaskSecurityGroups `
    -AssignPublicIp "ENABLED" `
    -Command @("python", "execution_sync_buy_position.py") `
    -Region $DefaultRegion `
    -LogGroupName $StrategyExecutionLogGroup `
    -LogStreamPrefix $StrategyExecutionLogStreamPrefix `
    -PollSeconds 10 `
    -TimeoutSeconds 1800

Write-Host "============================================================"
Write-Host "[STEP 16] SYNC_BUY_POSITION END"
Write-Host "============================================================"
Write-Host ("[STEP 16] taskArn={0}" -f $result.TaskArn)
Write-Host ("[STEP 16] taskId={0}" -f $result.TaskId)
Write-Host ("[STEP 16] exitCode={0}" -f $result.ExitCode)
Write-Host ("[STEP 16] cloudWatchLog={0}" -f $result.CloudWatchLogPath)

return [pscustomobject]@{
    StepCode = "SYNC_BUY_POSITION"
    Status = "SUCCESS"
    Runner = "ECS"
    TaskArn = $result.TaskArn
    TaskId = $result.TaskId
    ExitCode = $result.ExitCode
    CloudWatchLogPath = $result.CloudWatchLogPath
}
