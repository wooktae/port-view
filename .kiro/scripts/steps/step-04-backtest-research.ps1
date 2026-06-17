# Step 04 - BACKTEST_RESEARCH
# Runner: BATCH
# Risk: SAFE
# Runs strategy backtest research on AWS Batch.
# This step does not submit broker/KIS orders.

$jobName = "daily-paper-backtest-research-$RunId"

$result = Invoke-DailyAwsPaperBatchJob `
    -StepCode "BACKTEST_RESEARCH" `
    -JobName $jobName `
    -JobQueue $BatchResearchJobQueue `
    -JobDefinition $BacktestResearchJobDefinition `
    -Region $DefaultRegion `
    -PollSeconds 30 `
    -TimeoutSeconds 7200

Write-StepResult `
    -Step 4 `
    -Code "BACKTEST_RESEARCH" `
    -Status "COMPLETED" `
    -Message ("Batch job success. jobId={0}" -f $result.JobId)
