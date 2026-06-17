# Step 02 - INTEREST_CRAWLER
# Runner: ECS+SSM
# Risk: SAFE
# Runs non-GUI interest crawler on ECS Fargate.
# Optionally triggers KRX GUI Windows worker Scheduled Task when the worker is running.
# This step does not submit broker/KIS orders.

Write-Host "============================================================"
Write-Host "[STEP 02] INTEREST_CRAWLER START"
Write-Host "============================================================"

Write-Host "[STEP 02] non-GUI crawler ECS task start"

$ecsResult = Invoke-DailyAwsPaperEcsTask `
    -StepCode "INTEREST_CRAWLER_NONGUI" `
    -Cluster $EcsCluster `
    -TaskDefinition $InterestCrawlerTaskDefinition `
    -ContainerName $InterestCrawlerContainerName `
    -Subnets $EcsPublicSubnets `
    -SecurityGroups $InterestCrawlerSecurityGroups `
    -AssignPublicIp "ENABLED" `
    -Region $DefaultRegion `
    -LogGroupName $InterestCrawlerLogGroup `
    -LogStreamPrefix $InterestCrawlerLogStreamPrefix `
    -PollSeconds 10 `
    -TimeoutSeconds 3600

Write-Host "[STEP 02] non-GUI crawler ECS task completed"
Write-Host ("[STEP 02] ecsTaskArn={0}" -f $ecsResult.TaskArn)
Write-Host ("[STEP 02] ecsTaskId={0}" -f $ecsResult.TaskId)
Write-Host ("[STEP 02] ecsExitCode={0}" -f $ecsResult.ExitCode)
Write-Host ("[STEP 02] ecsCloudWatchLog={0}" -f $ecsResult.CloudWatchLogPath)

Write-Host "[STEP 02] checking Windows crawler worker state"

$workerState = aws ec2 describe-instances `
    --region $DefaultRegion `
    --instance-ids $CrawlerWorkerInstanceId `
    --query "Reservations[0].Instances[0].State.Name" `
    --output text

Write-Host ("[STEP 02] crawlerWorkerInstanceId={0}" -f $CrawlerWorkerInstanceId)
Write-Host ("[STEP 02] crawlerWorkerState={0}" -f $workerState)

$ssmResult = $null

if ($workerState -ne "running") {
    Write-Host "[STEP 02] KRX GUI worker skipped because crawler worker is not running."
}
else {
    Write-Host "[STEP 02] KRX GUI worker Scheduled Task trigger start"

    $commands = @(
        '$ErrorActionPreference = "Stop"',
        'Write-Host "===== KRX GUI WORKER SCHEDULED TASK START ====="',
        'Get-Date -Format o',
        'schtasks /Run /TN "Portfolio-KRX-Worker-Daily"',
        'Write-Host "===== KRX GUI WORKER SCHEDULED TASK TRIGGERED ====="'
    )

    $ssmResult = Invoke-SsmCommandAndWait `
        -StepCode "INTEREST_CRAWLER_KRX_WORKER" `
        -InstanceId $CrawlerWorkerInstanceId `
        -Commands $commands `
        -DocumentName "AWS-RunPowerShellScript"

    Write-Host ("[STEP 02] krxWorkerCommandId={0}" -f $ssmResult.CommandId)
    Write-Host ("[STEP 02] krxWorkerStdout={0}" -f $ssmResult.StdoutPath)
    Write-Host ("[STEP 02] krxWorkerStderr={0}" -f $ssmResult.StderrPath)
}

Write-Host "============================================================"
Write-Host "[STEP 02] INTEREST_CRAWLER END"
Write-Host "============================================================"

return [pscustomobject]@{
    StepCode = "INTEREST_CRAWLER"
    Status = "SUCCESS"
    Runner = "ECS+SSM"
    EcsTaskArn = $ecsResult.TaskArn
    EcsTaskId = $ecsResult.TaskId
    EcsExitCode = $ecsResult.ExitCode
    EcsCloudWatchLogPath = $ecsResult.CloudWatchLogPath
    CrawlerWorkerInstanceId = $CrawlerWorkerInstanceId
    CrawlerWorkerState = $workerState
    KrxWorkerCommandId = if ($null -eq $ssmResult) { $null } else { $ssmResult.CommandId }
}
