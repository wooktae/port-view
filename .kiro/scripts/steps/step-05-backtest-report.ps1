# Step 05 - BACKTEST_REPORT
# Runner: BATCH
# Risk: SAFE
# Runs strategy backtest report generation/upload on AWS Batch.
# This step does not submit broker/KIS orders.

$jobName = "daily-paper-backtest-report-$RunId"

$result = Invoke-DailyAwsPaperBatchJob `
    -StepCode "BACKTEST_REPORT" `
    -JobName $jobName `
    -JobQueue $BatchResearchJobQueue `
    -JobDefinition $BacktestReportJobDefinition `
    -Region $DefaultRegion `
    -PollSeconds 30 `
    -TimeoutSeconds 7200

Write-StepResult `
    -Step 5 `
    -Code "BACKTEST_REPORT" `
    -Status "COMPLETED" `
    -Message ("Batch job success. jobId={0}" -f $result.JobId)
