# Step 07 - DAILY_POSITION_SIGNAL
# Runs strategy decision daily position signal on ECS Fargate.
# No secret values are stored in this file.
# Uses the default command defined in the ECS task definition.

Write-Host "============================================================"
Write-Host "[STEP 07] DAILY_POSITION_SIGNAL START"
Write-Host "============================================================"

$result = Invoke-DailyAwsPaperEcsTask `
    -StepCode "DAILY_POSITION_SIGNAL" `
    -Cluster $EcsCluster `
    -TaskDefinition $DailyPositionSignalTaskDefinition `
    -ContainerName $DailyPositionSignalContainerName `
    -Subnets $EcsPublicSubnets `
    -SecurityGroups $StrategyTaskSecurityGroups `
    -AssignPublicIp "ENABLED" `
    -Region $DefaultRegion `
    -LogGroupName $DailyPositionSignalLogGroup `
    -LogStreamPrefix $DailyPositionSignalLogStreamPrefix `
    -PollSeconds 10 `
    -TimeoutSeconds 1800

Write-Host "============================================================"
Write-Host "[STEP 07] DAILY_POSITION_SIGNAL END"
Write-Host "============================================================"
Write-Host ("[STEP 07] taskArn={0}" -f $result.TaskArn)
Write-Host ("[STEP 07] taskId={0}" -f $result.TaskId)
Write-Host ("[STEP 07] exitCode={0}" -f $result.ExitCode)
Write-Host ("[STEP 07] cloudWatchLog={0}" -f $result.CloudWatchLogPath)

return [pscustomobject]@{
    StepCode = "DAILY_POSITION_SIGNAL"
    Status = "SUCCESS"
    Runner = "ECS"
    TaskArn = $result.TaskArn
    TaskId = $result.TaskId
    ExitCode = $result.ExitCode
    CloudWatchLogPath = $result.CloudWatchLogPath
}
