# Step 06 - DAILY_BUY_SIGNAL
# Runs strategy decision daily buy signal on ECS Fargate.
# No secret values are stored in this file.
# Uses the default command defined in the ECS task definition.

Write-Host "============================================================"
Write-Host "[STEP 06] DAILY_BUY_SIGNAL START"
Write-Host "============================================================"

$result = Invoke-DailyAwsPaperEcsTask `
    -StepCode "DAILY_BUY_SIGNAL" `
    -Cluster $EcsCluster `
    -TaskDefinition $DailyBuySignalTaskDefinition `
    -ContainerName $DailyBuySignalContainerName `
    -Subnets $EcsPublicSubnets `
    -SecurityGroups $StrategyTaskSecurityGroups `
    -AssignPublicIp "ENABLED" `
    -Region $DefaultRegion `
    -LogGroupName $DailyBuySignalLogGroup `
    -LogStreamPrefix $DailyBuySignalLogStreamPrefix `
    -PollSeconds 10 `
    -TimeoutSeconds 1800

Write-Host "============================================================"
Write-Host "[STEP 06] DAILY_BUY_SIGNAL END"
Write-Host "============================================================"
Write-Host ("[STEP 06] taskArn={0}" -f $result.TaskArn)
Write-Host ("[STEP 06] taskId={0}" -f $result.TaskId)
Write-Host ("[STEP 06] exitCode={0}" -f $result.ExitCode)
Write-Host ("[STEP 06] cloudWatchLog={0}" -f $result.CloudWatchLogPath)

return [pscustomobject]@{
    StepCode = "DAILY_BUY_SIGNAL"
    Status = "SUCCESS"
    Runner = "ECS"
    TaskArn = $result.TaskArn
    TaskId = $result.TaskId
    ExitCode = $result.ExitCode
    CloudWatchLogPath = $result.CloudWatchLogPath
}
