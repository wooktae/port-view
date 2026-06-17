# Step 03 - PREPROCESSOR
# Runs interest preprocessor on ECS Fargate.
# No secret values are stored in this file.

Write-Host "============================================================"
Write-Host "[STEP 03] PREPROCESSOR START"
Write-Host "============================================================"

$result = Invoke-DailyAwsPaperEcsTask `
    -StepCode "PREPROCESSOR" `
    -Cluster $EcsCluster `
    -TaskDefinition $PreprocessorTaskDefinition `
    -ContainerName $PreprocessorContainerName `
    -Subnets $EcsPublicSubnets `
    -SecurityGroups $PreprocessorSecurityGroups `
    -AssignPublicIp "ENABLED" `
    -Region $DefaultRegion `
    -LogGroupName $PreprocessorLogGroup `
    -LogStreamPrefix $PreprocessorLogStreamPrefix `
    -PollSeconds 10 `
    -TimeoutSeconds 1800

Write-Host "============================================================"
Write-Host "[STEP 03] PREPROCESSOR END"
Write-Host "============================================================"
Write-Host ("[STEP 03] taskArn={0}" -f $result.TaskArn)
Write-Host ("[STEP 03] taskId={0}" -f $result.TaskId)
Write-Host ("[STEP 03] exitCode={0}" -f $result.ExitCode)
Write-Host ("[STEP 03] cloudWatchLog={0}" -f $result.CloudWatchLogPath)

return [pscustomobject]@{
    StepCode = "PREPROCESSOR"
    Status = "SUCCESS"
    Runner = "ECS"
    TaskArn = $result.TaskArn
    TaskId = $result.TaskId
    ExitCode = $result.ExitCode
    CloudWatchLogPath = $result.CloudWatchLogPath
}
