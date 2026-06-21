# Step 02 - INTEREST_CRAWLER
# Runner: ECS+SSM
# Risk: SAFE
# Runs non-GUI interest crawler on ECS Fargate.
# Optionally triggers KRX GUI Windows worker Scheduled Task when the worker is running.
# This step does not submit broker/KIS orders.

Write-Host "============================================================"
Write-Host "[STEP 02] INTEREST_CRAWLER START"
Write-Host "============================================================"

function Get-PreviousWeekdayString {
    param(
        [Parameter(Mandatory = $true)]
        [string]$DateString
    )

    $date = [datetime]::ParseExact($DateString, "yyyy-MM-dd", [System.Globalization.CultureInfo]::InvariantCulture)
    $candidate = $date.AddDays(-1)

    while ($candidate.DayOfWeek -in @([System.DayOfWeek]::Saturday, [System.DayOfWeek]::Sunday)) {
        $candidate = $candidate.AddDays(-1)
    }

    return $candidate.ToString("yyyy-MM-dd")
}

$expectedKrxRawDate = Get-PreviousWeekdayString -DateString $RunDate

Write-Host ("[STEP 02] RunDate={0}" -f $RunDate)
Write-Host ("[STEP 02] ExpectedKrxRawDate={0}" -f $expectedKrxRawDate)

Write-Host "[STEP 02] non-GUI crawler ECS task start"

$ecsResult = Invoke-DailyAwsPaperEcsTask `
    -StepCode "INTEREST_CRAWLER_NONGUI" `
    -Cluster $EcsCluster `
    -TaskDefinition $InterestCrawlerTaskDefinition `
    -ContainerName $InterestCrawlerContainerName `
    -Subnets $EcsPublicSubnets `
    -SecurityGroups $InterestCrawlerSecurityGroups `
    -AssignPublicIp "ENABLED" `
    -EnvironmentVariables @{
        TEMP = "/tmp"
        TMP = "/tmp"
        PYTHONUTF8 = "1"
        PYTHONIOENCODING = "utf-8"
    } `
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
$dbValidationResult = $null

if ($workerState -ne "running") {
    throw ("[STEP 02] Crawler worker must be running for KRX GUI worker and DB validation. instanceId={0} state={1}" -f $CrawlerWorkerInstanceId, $workerState)
}
else {
    Write-Host "[STEP 02] KRX GUI worker Scheduled Task start with reset/wait/result/log validation"

    $commands = @(
        '$ErrorActionPreference = "Stop"',
        '',
        '$TaskName = "Portfolio-KRX-Worker-Daily"',
        '$TimeoutSeconds = 1800',
        '$PollSeconds = 10',
        '',
        'function Get-TaskQueryLines {',
        '    $result = schtasks /Query /TN $TaskName /V /FO LIST 2>&1',
        '',
        '    if ($LASTEXITCODE -ne 0) {',
        '        throw ("schtasks query failed. exitCode={0} output={1}" -f $LASTEXITCODE, ($result -join " | "))',
        '    }',
        '',
        '    return @($result)',
        '}',
        '',
        'function Get-TaskFieldValue {',
        '    param(',
        '        [string[]]$Lines,',
        '        [string]$FieldName',
        '    )',
        '',
        '    $line = $Lines |',
        '        Where-Object { $_ -match ("^\s*" + [regex]::Escape($FieldName) + "\s*:") } |',
        '        Select-Object -First 1',
        '',
        '    if ($null -eq $line) {',
        '        return $null',
        '    }',
        '',
        '    return (($line -replace ("^\s*" + [regex]::Escape($FieldName) + "\s*:\s*"), "").Trim())',
        '}',
        '',
        'Write-Host "===== KRX GUI WORKER SCHEDULED TASK START ====="',
        'Write-Host ("Now={0}" -f (Get-Date -Format o))',
        'Write-Host ("TaskName={0}" -f $TaskName)',
        '',
        'Write-Host "===== BEST-EFFORT CHROME RESET START ====="',
        '',
        'foreach ($processName in @("chrome", "chromedriver")) {',
        '    try {',
        '        $processes = @(Get-Process -Name $processName -ErrorAction SilentlyContinue)',
        '',
        '        if ($processes.Count -eq 0) {',
        '            Write-Host ("No stale process found: {0}" -f $processName)',
        '            continue',
        '        }',
        '',
        '        foreach ($process in $processes) {',
        '            try {',
        '                Write-Host ("Stopping stale process: name={0} id={1}" -f $process.ProcessName, $process.Id)',
        '                Stop-Process -Id $process.Id -Force -ErrorAction Stop',
        '            }',
        '            catch {',
        '                Write-Warning ("Failed to stop process: name={0} id={1} error={2}" -f $process.ProcessName, $process.Id, $_.Exception.Message)',
        '            }',
        '        }',
        '    }',
        '    catch {',
        '        Write-Warning ("Best-effort process reset failed for {0}: {1}" -f $processName, $_.Exception.Message)',
        '    }',
        '}',
        '',
        'Write-Host "===== BEST-EFFORT CHROME RESET DONE ====="',
        '',
        'Write-Host "===== SCHEDULED TASK RUN REQUEST ====="',
        '$runOutput = schtasks /Run /TN $TaskName 2>&1',
        '$runExitCode = $LASTEXITCODE',
        '',
        'Write-Host ($runOutput -join "`n")',
        '',
        'if ($runExitCode -ne 0) {',
        '    throw ("schtasks run failed. exitCode={0}" -f $runExitCode)',
        '}',
        '',
        'Start-Sleep -Seconds 3',
        '',
        'Write-Host "===== SCHEDULED TASK WAIT START ====="',
        '',
        '$startedAt = Get-Date',
        '$lastStatus = $null',
        '$lastResult = $null',
        '$sawRunning = $false',
        '',
        'while ($true) {',
        '    $lines = Get-TaskQueryLines',
        '    $status = Get-TaskFieldValue -Lines $lines -FieldName "Status"',
        '    $lastResult = Get-TaskFieldValue -Lines $lines -FieldName "Last Result"',
        '',
        '    if ([string]::IsNullOrWhiteSpace($status)) {',
        '        Write-Warning "Scheduled Task status field was not found."',
        '    }',
        '    elseif ($status -ne $lastStatus) {',
        '        Write-Host ("TaskStatus={0}" -f $status)',
        '        $lastStatus = $status',
        '    }',
        '',
        '    if ($status -eq "Running") {',
        '        $sawRunning = $true',
        '    }',
        '',
        '    $elapsed = [int]((Get-Date) - $startedAt).TotalSeconds',
        '',
        '    if ($status -eq "Ready") {',
        '        Write-Host ("Task returned to Ready. elapsedSeconds={0} sawRunning={1}" -f $elapsed, $sawRunning)',
        '        break',
        '    }',
        '',
        '    if ($elapsed -ge $TimeoutSeconds) {',
        '        throw ("Scheduled Task wait timeout. timeoutSeconds={0} lastStatus={1}" -f $TimeoutSeconds, $status)',
        '    }',
        '',
        '    Start-Sleep -Seconds $PollSeconds',
        '}',
        '',
        'Write-Host "===== SCHEDULED TASK FINAL STATE ====="',
        '$finalLines = Get-TaskQueryLines',
        '$finalLines | ForEach-Object { Write-Host $_ }',
        '',
        '$finalStatus = Get-TaskFieldValue -Lines $finalLines -FieldName "Status"',
        '$finalLastResult = Get-TaskFieldValue -Lines $finalLines -FieldName "Last Result"',
        '',
        'Write-Host ("FinalStatus={0}" -f $finalStatus)',
        'Write-Host ("FinalLastResult={0}" -f $finalLastResult)',
        '',
        'if ($finalStatus -ne "Ready") {',
        '    throw ("Scheduled Task final status is not Ready. finalStatus={0}" -f $finalStatus)',
        '}',
        '',
        'if ($finalLastResult -notin @("0", "0x0")) {',
        '    throw ("Scheduled Task Last Result is not success. lastResult={0}" -f $finalLastResult)',
        '}',
        '',
        'Write-Host "===== LATEST KRX WORKER LOG ====="',
        '',
        '$logDir = "C:\portfolio\logs"',
        '$latestLog = $null',
        '',
        'if (Test-Path $logDir) {',
        '    $latestLog = Get-ChildItem $logDir -Filter "krx_worker_daily_*.log" -File -ErrorAction SilentlyContinue |',
        '        Sort-Object LastWriteTime -Descending |',
        '        Select-Object -First 1',
        '}',
        '',
        'if ($null -eq $latestLog) {',
        '    Write-Warning ("No latest KRX worker log found under {0}" -f $logDir)',
        '}',
        'else {',
        '    Write-Host ("LatestLogPath={0}" -f $latestLog.FullName)',
        '    Write-Host ("LatestLogLastWriteTime={0}" -f $latestLog.LastWriteTime.ToString("yyyy-MM-dd HH:mm:ss"))',
        '    Write-Host ("LatestLogSize={0}" -f $latestLog.Length)',
        '',
        '    Write-Host "===== LATEST KRX WORKER LOG TAIL START ====="',
        '    Get-Content $latestLog.FullName -Tail 80 -ErrorAction SilentlyContinue',
        '    Write-Host "===== LATEST KRX WORKER LOG TAIL END ====="',
        '}',
        '',
        'Write-Host "===== KRX GUI WORKER SCHEDULED TASK DONE ====="',
        'Write-Host ("Now={0}" -f (Get-Date -Format o))'
    )

    $ssmResult = Invoke-SsmCommandAndWait `
        -StepCode "INTEREST_CRAWLER_KRX_WORKER" `
        -InstanceId $CrawlerWorkerInstanceId `
        -Commands $commands `
        -DocumentName "AWS-RunPowerShellScript" `
        -ExecutionTimeoutSeconds 2400

    Write-Host ("[STEP 02] krxWorkerCommandId={0}" -f $ssmResult.CommandId)
    Write-Host ("[STEP 02] krxWorkerStdout={0}" -f $ssmResult.StdoutPath)
    Write-Host ("[STEP 02] krxWorkerStderr={0}" -f $ssmResult.StderrPath)

    Write-Host "[STEP 02] KRX raw DB validation start"

    $dbValidationCommands = @(
        'Set-StrictMode -Version Latest',
        '$ErrorActionPreference = "Stop"',
        'chcp 65001 | Out-Null',
        '[Console]::OutputEncoding = [System.Text.Encoding]::UTF8',
        '$OutputEncoding = [System.Text.Encoding]::UTF8',
        '$env:PYTHONUTF8 = "1"',
        '$env:PYTHONIOENCODING = "utf-8"',
        '$AppDir = "C:\portfolio\port-interest-crawler"',
        '$VenvActivate = "C:\portfolio\venvs\interest-crawler\Scripts\Activate.ps1"',
        '$DbEnv = "C:\portfolio\load-crawler-db-env.ps1"',
        'if (-not (Test-Path $AppDir)) { throw ("AppDir missing: " + $AppDir) }',
        'if (-not (Test-Path $VenvActivate)) { throw ("VenvActivate missing: " + $VenvActivate) }',
        'if (-not (Test-Path $DbEnv)) { throw ("DbEnv missing: " + $DbEnv) }',
        'Set-Location $AppDir',
        '. $VenvActivate',
        '. $DbEnv',
        'Write-Host "===== KRX RAW DB VALIDATION START ====="',
        ('Write-Host "ExpectedKrxRawDate=' + $expectedKrxRawDate + '"'),
        ('python .\interest_krx_raw_validate_daily.py --expected-date ' + $expectedKrxRawDate),
        'Write-Host ("VALIDATION_LASTEXITCODE=" + $LASTEXITCODE)',
        'if ($LASTEXITCODE -ne 0) { throw ("KRX raw validation failed. exitCode=" + $LASTEXITCODE) }',
        'Write-Host "===== KRX RAW DB VALIDATION DONE ====="'
    )

    $dbValidationResult = Invoke-SsmCommandAndWait `
        -StepCode "INTEREST_CRAWLER_KRX_DB_VALIDATE" `
        -InstanceId $CrawlerWorkerInstanceId `
        -Commands $dbValidationCommands `
        -DocumentName "AWS-RunPowerShellScript" `
        -ExecutionTimeoutSeconds 900

    Write-Host ("[STEP 02] krxDbValidationCommandId={0}" -f $dbValidationResult.CommandId)
    Write-Host ("[STEP 02] krxDbValidationStdout={0}" -f $dbValidationResult.StdoutPath)
    Write-Host ("[STEP 02] krxDbValidationStderr={0}" -f $dbValidationResult.StderrPath)
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
    ExpectedKrxRawDate = $expectedKrxRawDate
    KrxWorkerCommandId = if ($null -eq $ssmResult) { $null } else { $ssmResult.CommandId }
    KrxDbValidationCommandId = if ($null -eq $dbValidationResult) { $null } else { $dbValidationResult.CommandId }
}
