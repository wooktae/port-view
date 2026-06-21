# Daily AWS Paper common functions
# Keep shared orchestration helpers here.

function Get-DailyAwsPaperSteps {
    param(
        [string]$ScriptRoot
    )

    $stepsRoot = Join-Path $ScriptRoot "steps"

    $steps = @(
        [pscustomobject]@{ Step = 1;  Code = "CONNECTOR_BALANCE";                      Runner = "SSM";     Risk = "READ_ONLY";            StepFile = "step-01-connector-balance.ps1" },
        [pscustomobject]@{ Step = 2;  Code = "INTEREST_CRAWLER";                       Runner = "ECS+SSM"; Risk = "SAFE";                 StepFile = "step-02-interest-crawler.ps1" },
        [pscustomobject]@{ Step = 3;  Code = "PREPROCESSOR";                           Runner = "ECS";     Risk = "SAFE";                 StepFile = "step-03-preprocessor.ps1" },
        [pscustomobject]@{ Step = 4;  Code = "BACKTEST_RESEARCH";                      Runner = "BATCH";   Risk = "SAFE";                 StepFile = "step-04-backtest-research.ps1" },
        [pscustomobject]@{ Step = 5;  Code = "BACKTEST_REPORT";                        Runner = "BATCH";   Risk = "SAFE";                 StepFile = "step-05-backtest-report.ps1" },
        [pscustomobject]@{ Step = 6;  Code = "DAILY_BUY_SIGNAL";                       Runner = "ECS";     Risk = "SAFE";                 StepFile = "step-06-daily-buy-signal.ps1" },
        [pscustomobject]@{ Step = 7;  Code = "DAILY_POSITION_SIGNAL";                  Runner = "ECS";     Risk = "SAFE";                 StepFile = "step-07-daily-position-signal.ps1" },
        [pscustomobject]@{ Step = 8;  Code = "DAILY_BUY_EXECUTION";                    Runner = "ECS";     Risk = "NO_BROKER_ORDER";      StepFile = "step-08-daily-buy-execution.ps1" },
        [pscustomobject]@{ Step = 9;  Code = "DAILY_SELL_EXECUTION";                   Runner = "ECS";     Risk = "NO_BROKER_ORDER";      StepFile = "step-09-daily-sell-execution.ps1" },
        [pscustomobject]@{ Step = 10; Code = "DAILY_AUTO_SELL";                        Runner = "ECS";     Risk = "NO_BROKER_ORDER";      StepFile = "step-10-daily-auto-sell.ps1" },
        [pscustomobject]@{ Step = 11; Code = "DAILY_AUTO_BUY";                         Runner = "ECS";     Risk = "NO_BROKER_ORDER";      StepFile = "step-11-daily-auto-buy.ps1" },
        [pscustomobject]@{ Step = 12; Code = "MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE"; Runner = "SSM";     Risk = "PAPER_ORDER_GATE";     StepFile = "step-12-marketconnector-strategy-order-execute.ps1" },
        [pscustomobject]@{ Step = 13; Code = "CONNECTOR_ORDER_CHECK";                  Runner = "SSM";     Risk = "ORDER_STATUS_UPDATE";  StepFile = "step-13-connector-order-check.ps1" },
        [pscustomobject]@{ Step = 14; Code = "SYNC_SELL_FILL";                         Runner = "ECS";     Risk = "SYNC";                 StepFile = "step-14-sync-sell-fill.ps1" },
        [pscustomobject]@{ Step = 15; Code = "SYNC_BUY_FILL";                          Runner = "ECS";     Risk = "SYNC";                 StepFile = "step-15-sync-buy-fill.ps1" },
        [pscustomobject]@{ Step = 16; Code = "SYNC_BUY_POSITION";                      Runner = "ECS";     Risk = "POSITION_UPDATE";      StepFile = "step-16-sync-buy-position.ps1" },
        [pscustomobject]@{ Step = 17; Code = "BALANCE_REFRESH";                        Runner = "SSM";     Risk = "BALANCE_REFRESH";      StepFile = "step-17-balance-refresh.ps1" }
    )

    foreach ($step in $steps) {
        $fullPath = Join-Path $stepsRoot $step.StepFile
        $status = if (Test-Path $fullPath) { "FOUND" } else { "MISSING" }

        $step | Add-Member -NotePropertyName StepFilePath -NotePropertyValue $fullPath -Force
        $step | Add-Member -NotePropertyName StepFileStatus -NotePropertyValue $status -Force
    }

    return $steps
}
function Write-StepHeader {
    param(
        [int]$Step,
        [string]$Code
    )

    Write-Host ""
    Write-Host "------------------------------------------------------------"
    Write-Host ("[STEP {0}] {1}" -f $Step, $Code)
    Write-Host "------------------------------------------------------------"
}

function Write-StepResult {
    param(
        [int]$Step,
        [string]$Code,
        [string]$Status,
        [string]$Message
    )

    $line = "[STEP {0}] {1} => {2} - {3}" -f $Step, $Code, $Status, $Message
    Write-Host $line

    if (Get-Variable -Name SummaryPath -Scope Global -ErrorAction SilentlyContinue) {
        Add-Content -Path $Global:SummaryPath -Value $line -Encoding utf8
    }
}

function Invoke-DailyStepFile {
    param(
        [pscustomobject]$StepInfo
    )

    if (-not (Test-Path $StepInfo.StepFilePath)) {
        Write-StepResult `
            -Step $StepInfo.Step `
            -Code $StepInfo.Code `
            -Status "BLOCKED" `
            -Message ("Step file missing. path={0}" -f $StepInfo.StepFilePath)

        throw ("Step file missing. step={0} code={1} path={2}" -f $StepInfo.Step, $StepInfo.Code, $StepInfo.StepFilePath)
    }

    & $StepInfo.StepFilePath
}
function New-SsmParameterFile {
    param(
        [string]$StepCode,
        [string[]]$Commands,
        [int]$ExecutionTimeoutSeconds = 900
    )

    $safeStepCode = $StepCode.ToLower().Replace("_", "-")
    $paramPath = Join-Path $Global:LogDir ("ssm-{0}-parameters.json" -f $safeStepCode)

    $payload = @{
        commands = $Commands
        executionTimeout = @([string]$ExecutionTimeoutSeconds)
    }

    $json = $payload | ConvertTo-Json -Depth 10
    [System.IO.File]::WriteAllText($paramPath, $json, [System.Text.UTF8Encoding]::new($false))

    return $paramPath
}

function Invoke-SsmCommandAndWait {
    param(
        [string]$StepCode,
        [string]$InstanceId,
        [string[]]$Commands,
        [string]$DocumentName = "AWS-RunShellScript",
        [int]$ExecutionTimeoutSeconds = 900
    )

    $paramPath = New-SsmParameterFile `
        -StepCode $StepCode `
        -Commands $Commands `
        -ExecutionTimeoutSeconds $ExecutionTimeoutSeconds

    # AWS CLI on Windows expects local param files as file://C:\path\file.json.
    # Do not convert to file:///C:/... because it may be interpreted as /C:/...
    $paramUri = "file://" + $paramPath

    Write-Host ("[SSM] SendCommand target={0} step={1}" -f $InstanceId, $StepCode)
    Write-Host ("[SSM] documentName={0}" -f $DocumentName)
    Write-Host ("[SSM] parameters={0}" -f $paramUri)

    $commandOutput = aws ssm send-command `
        --region $Region `
        --instance-ids $InstanceId `
        --document-name $DocumentName `
        --parameters $paramUri `
        --query "Command.CommandId" `
        --output text 2>&1

    if ($LASTEXITCODE -ne 0) {
        throw ("SSM send-command failed before commandId. step={0} output={1}" -f $StepCode, ($commandOutput -join "`n"))
    }

    $commandId = $commandOutput

    if ([string]::IsNullOrWhiteSpace($commandId)) {
        throw "SSM commandId is empty. step=$StepCode"
    }

    Write-Host ("[SSM] commandId={0}" -f $commandId)

    $status = $null
    $responseCode = $null

    for ($i = 0; $i -lt 90; $i++) {
        Start-Sleep -Seconds 5

        $raw = aws ssm get-command-invocation `
            --region $Region `
            --command-id $commandId `
            --instance-id $InstanceId `
            --query "{Status:Status,ResponseCode:ResponseCode}" `
            --output json

        $state = $raw | ConvertFrom-Json
        $status = $state.Status
        $responseCode = $state.ResponseCode

        Write-Host ("[SSM] status={0} responseCode={1}" -f $status, $responseCode)

        if ($status -in @("Success", "Failed", "Cancelled", "TimedOut", "Cancelling")) {
            break
        }
    }

    $safeStepCode = $StepCode.ToLower().Replace("_", "-")
    $stdoutPath = Join-Path $Global:LogDir ("ssm-{0}-{1}-stdout.txt" -f $safeStepCode, $commandId)
    $stderrPath = Join-Path $Global:LogDir ("ssm-{0}-{1}-stderr.txt" -f $safeStepCode, $commandId)

    aws ssm get-command-invocation `
        --region $Region `
        --command-id $commandId `
        --instance-id $InstanceId `
        --query "StandardOutputContent" `
        --output text | Set-Content -Path $stdoutPath -Encoding utf8

    aws ssm get-command-invocation `
        --region $Region `
        --command-id $commandId `
        --instance-id $InstanceId `
        --query "StandardErrorContent" `
        --output text | Set-Content -Path $stderrPath -Encoding utf8

    if ($status -ne "Success" -or $responseCode -ne 0) {
        throw "SSM command failed. step=$StepCode commandId=$commandId status=$status responseCode=$responseCode stdout=$stdoutPath stderr=$stderrPath"
    }

    return [pscustomobject]@{
        CommandId = $commandId
        Status = $status
        ResponseCode = $responseCode
        StdoutPath = $stdoutPath
        StderrPath = $stderrPath
    }
}


function Invoke-DailyAwsPaperEcsTask {
    param(
        [Parameter(Mandatory = $true)]
        [string] $StepCode,

        [Parameter(Mandatory = $true)]
        [string] $Cluster,

        [Parameter(Mandatory = $true)]
        [string] $TaskDefinition,

        [Parameter(Mandatory = $true)]
        [string] $ContainerName,

        [Parameter(Mandatory = $true)]
        [string[]] $Subnets,

        [Parameter(Mandatory = $true)]
        [string[]] $SecurityGroups,

        [Parameter(Mandatory = $false)]
        [ValidateSet("ENABLED", "DISABLED")]
        [string] $AssignPublicIp = "ENABLED",

        [Parameter(Mandatory = $false)]
        [string[]] $Command = @(),

        [Parameter(Mandatory = $false)]
        [hashtable] $EnvironmentVariables = @{},

        [Parameter(Mandatory = $false)]
        [string] $Region = $Global:AwsRegion,

        [Parameter(Mandatory = $false)]
        [string] $LogGroupName = "",

        [Parameter(Mandatory = $false)]
        [string] $LogStreamPrefix = "",

        [Parameter(Mandatory = $false)]
        [int] $PollSeconds = 10,

        [Parameter(Mandatory = $false)]
        [int] $TimeoutSeconds = 1800
    )

    if ([string]::IsNullOrWhiteSpace($Region)) {
        $Region = "ap-northeast-2"
    }

    if (-not $Global:OverrideDir) {
        throw "Global OverrideDir is not initialized. step=$StepCode"
    }

    if (-not $Global:LogDir) {
        throw "Global LogDir is not initialized. step=$StepCode"
    }

    $safeStepCode = $StepCode.ToLower().Replace("_", "-")
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $overridePath = Join-Path $Global:OverrideDir ("ecs-{0}-{1}-overrides.json" -f $safeStepCode, $timestamp)

    $containerOverride = @{
        name = $ContainerName
    }

    if ($Command -and $Command.Count -gt 0) {
        $containerOverride.command = $Command
    }

    if ($EnvironmentVariables -and $EnvironmentVariables.Count -gt 0) {
        $environment = @()

        foreach ($key in ($EnvironmentVariables.Keys | Sort-Object)) {
            $environment += @{
                name = [string]$key
                value = [string]$EnvironmentVariables[$key]
            }
        }

        $containerOverride.environment = $environment
    }

    $overrides = @{
        containerOverrides = @($containerOverride)
    }

    $overrideJson = $overrides | ConvertTo-Json -Depth 20
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($overridePath, $overrideJson, $utf8NoBom)

    Write-Host ("[ECS] RunTask step={0}" -f $StepCode)
    Write-Host ("[ECS] cluster={0}" -f $Cluster)
    Write-Host ("[ECS] taskDefinition={0}" -f $TaskDefinition)
    Write-Host ("[ECS] containerName={0}" -f $ContainerName)
    Write-Host ("[ECS] overrides={0}" -f $overridePath)

    $subnetText = $Subnets -join ","
    $securityGroupText = $SecurityGroups -join ","
    $networkConfiguration = "awsvpcConfiguration={subnets=[$subnetText],securityGroups=[$securityGroupText],assignPublicIp=$AssignPublicIp}"

    $runTaskOutput = aws ecs run-task `
        --region $Region `
        --cluster $Cluster `
        --launch-type FARGATE `
        --task-definition $TaskDefinition `
        --network-configuration $networkConfiguration `
        --overrides ("file://{0}" -f $overridePath) `
        --output json 2>&1

    if ($LASTEXITCODE -ne 0) {
        throw ("ECS run-task failed before taskArn. step={0} output={1}" -f $StepCode, ($runTaskOutput -join "`n"))
    }

    $runTask = $runTaskOutput | ConvertFrom-Json

    if ($runTask.failures -and $runTask.failures.Count -gt 0) {
        $failureText = $runTask.failures | ConvertTo-Json -Compress -Depth 10
        throw ("ECS run-task returned failures. step={0} failures={1}" -f $StepCode, $failureText)
    }

    if (-not $runTask.tasks -or $runTask.tasks.Count -lt 1) {
        throw "ECS run-task returned no task. step=$StepCode"
    }

    $taskArn = [string] $runTask.tasks[0].taskArn
    if ([string]::IsNullOrWhiteSpace($taskArn)) {
        throw "ECS taskArn is empty. step=$StepCode"
    }

    $taskId = ($taskArn -split "/")[-1]
    Write-Host ("[ECS] taskArn={0}" -f $taskArn)
    Write-Host ("[ECS] taskId={0}" -f $taskId)

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $lastStatus = $null
    $desiredStatus = $null
    $task = $null

    while ($true) {
        Start-Sleep -Seconds $PollSeconds

        $describeOutput = aws ecs describe-tasks `
            --region $Region `
            --cluster $Cluster `
            --tasks $taskArn `
            --output json 2>&1

        if ($LASTEXITCODE -ne 0) {
            throw ("ECS describe-tasks failed. step={0} taskArn={1} output={2}" -f $StepCode, $taskArn, ($describeOutput -join "`n"))
        }

        $describe = $describeOutput | ConvertFrom-Json

        if (-not $describe.tasks -or $describe.tasks.Count -lt 1) {
            throw "ECS describe-tasks returned no task. step=$StepCode taskArn=$taskArn"
        }

        $task = $describe.tasks[0]
        $lastStatus = [string] $task.lastStatus
        $desiredStatus = [string] $task.desiredStatus

        Write-Host ("[ECS] status={0} desired={1}" -f $lastStatus, $desiredStatus)

        if ($lastStatus -eq "STOPPED") {
            break
        }

        if ((Get-Date) -gt $deadline) {
            throw "ECS task timeout. step=$StepCode taskArn=$taskArn timeoutSeconds=$TimeoutSeconds"
        }
    }

    $container = $task.containers | Where-Object { $_.name -eq $ContainerName } | Select-Object -First 1
    if (-not $container) {
        throw "ECS container not found. step=$StepCode containerName=$ContainerName taskArn=$taskArn"
    }

    $exitCode = $container.exitCode

    $containerReason = ""
    if ($container.PSObject.Properties.Name -contains "reason") {
        $containerReason = [string] $container.reason
    }

    $stoppedReason = ""
    if ($task.PSObject.Properties.Name -contains "stoppedReason") {
        $stoppedReason = [string] $task.stoppedReason
    }

    Write-Host ("[ECS] stoppedReason={0}" -f $stoppedReason)
    Write-Host ("[ECS] containerReason={0}" -f $containerReason)
    Write-Host ("[ECS] exitCode={0}" -f $exitCode)

    if ([string]::IsNullOrWhiteSpace($LogGroupName) -or [string]::IsNullOrWhiteSpace($LogStreamPrefix)) {
        $taskDefOutput = aws ecs describe-task-definition `
            --region $Region `
            --task-definition $TaskDefinition `
            --output json 2>&1

        if ($LASTEXITCODE -eq 0) {
            $taskDef = $taskDefOutput | ConvertFrom-Json
            $containerDef = $taskDef.taskDefinition.containerDefinitions |
                Where-Object { $_.name -eq $ContainerName } |
                Select-Object -First 1

            if ($containerDef -and $containerDef.logConfiguration -and $containerDef.logConfiguration.options) {
                if ([string]::IsNullOrWhiteSpace($LogGroupName)) {
                    $LogGroupName = [string] $containerDef.logConfiguration.options."awslogs-group"
                }

                if ([string]::IsNullOrWhiteSpace($LogStreamPrefix)) {
                    $LogStreamPrefix = [string] $containerDef.logConfiguration.options."awslogs-stream-prefix"
                }
            }
        }
    }

    $logStreamName = ""
    $cloudWatchLogPath = ""

    if (-not [string]::IsNullOrWhiteSpace($LogGroupName) -and -not [string]::IsNullOrWhiteSpace($LogStreamPrefix)) {
        $logStreamName = "{0}/{1}/{2}" -f $LogStreamPrefix, $ContainerName, $taskId
        $cloudWatchLogPath = Join-Path $Global:LogDir ("ecs-{0}-{1}-cloudwatch.txt" -f $safeStepCode, $taskId)

        Write-Host ("[ECS] logGroup={0}" -f $LogGroupName)
        Write-Host ("[ECS] logStream={0}" -f $logStreamName)

        $streamCheck = aws logs describe-log-streams `
            --region $Region `
            --log-group-name $LogGroupName `
            --log-stream-name-prefix $logStreamName `
            --max-items 1 `
            --output json 2>&1

        if ($LASTEXITCODE -eq 0) {
            $logEvents = aws logs get-log-events `
                --region $Region `
                --log-group-name $LogGroupName `
                --log-stream-name $logStreamName `
                --start-from-head `
                --query "events[].message" `
                --output text 2>&1

            if ($LASTEXITCODE -eq 0) {
                $logEvents | Set-Content -Path $cloudWatchLogPath -Encoding utf8
                Write-Host ("[ECS] cloudWatchLog={0}" -f $cloudWatchLogPath)
            } else {
                Write-Host ("[ECS] CloudWatch get-log-events skipped or failed. step={0}" -f $StepCode)
            }
        } else {
            Write-Host ("[ECS] CloudWatch log stream not found yet. step={0}" -f $StepCode)
        }
    } else {
        Write-Host ("[ECS] CloudWatch log stream check skipped. step={0}" -f $StepCode)
    }

    if ($null -eq $exitCode) {
        throw "ECS container exitCode is empty. step=$StepCode taskArn=$taskArn"
    }

    if ([int] $exitCode -ne 0) {
        throw "ECS task failed. step=$StepCode taskArn=$taskArn exitCode=$exitCode log=$cloudWatchLogPath"
    }

    return [pscustomobject]@{
        TaskArn = $taskArn
        TaskId = $taskId
        LastStatus = $lastStatus
        DesiredStatus = $desiredStatus
        ExitCode = [int] $exitCode
        StoppedReason = $stoppedReason
        ContainerReason = $containerReason
        OverridePath = $overridePath
        LogGroupName = $LogGroupName
        LogStreamName = $logStreamName
        CloudWatchLogPath = $cloudWatchLogPath
    }
}

function Invoke-DailyAwsPaperBatchJob {
    param(
        [string]$StepCode,
        [string]$JobName,
        [string]$JobQueue,
        [string]$JobDefinition,
        [string]$Region = $DefaultRegion,
        [int]$PollSeconds = 30,
        [int]$TimeoutSeconds = 7200
    )

    $safeStepCode = $StepCode.ToLower().Replace("_", "-")

    Write-Host ("[BATCH] SubmitJob step={0}" -f $StepCode)
    Write-Host ("[BATCH] jobName={0}" -f $JobName)
    Write-Host ("[BATCH] jobQueue={0}" -f $JobQueue)
    Write-Host ("[BATCH] jobDefinition={0}" -f $JobDefinition)

    $jobId = aws batch submit-job `
        --region $Region `
        --job-name $JobName `
        --job-queue $JobQueue `
        --job-definition $JobDefinition `
        --query "jobId" `
        --output text 2>&1

    if ($LASTEXITCODE -ne 0) {
        throw ("Batch submit-job failed. step={0} output={1}" -f $StepCode, ($jobId -join "`n"))
    }

    if ([string]::IsNullOrWhiteSpace($jobId)) {
        throw "Batch jobId is empty. step=$StepCode"
    }

    Write-Host ("[BATCH] jobId={0}" -f $jobId)

    $status = $null
    $statusReason = $null
    $logStreamName = $null
    $exitCode = $null
    $startedAt = $null
    $stoppedAt = $null

    $maxPolls = [Math]::Ceiling($TimeoutSeconds / $PollSeconds)

    for ($i = 0; $i -lt $maxPolls; $i++) {
        Start-Sleep -Seconds $PollSeconds

        $raw = aws batch describe-jobs `
            --region $Region `
            --jobs $jobId `
            --query "jobs[0].{Status:status,StatusReason:statusReason,LogStreamName:container.logStreamName,ExitCode:container.exitCode,StartedAt:startedAt,StoppedAt:stoppedAt}" `
            --output json

        $state = $raw | ConvertFrom-Json
        $status = $state.Status
        $statusReason = $state.StatusReason
        $logStreamName = $state.LogStreamName
        $exitCode = $state.ExitCode
        $startedAt = $state.StartedAt
        $stoppedAt = $state.StoppedAt

        Write-Host ("[BATCH] status={0} exitCode={1}" -f $status, $exitCode)

        if ($status -in @("SUCCEEDED", "FAILED")) {
            break
        }
    }

    if ($status -notin @("SUCCEEDED", "FAILED")) {
        throw "Batch job timed out. step=$StepCode jobId=$jobId lastStatus=$status"
    }

    $cloudWatchLogPath = $null

    if (-not [string]::IsNullOrWhiteSpace($logStreamName)) {
        $cloudWatchLogPath = Join-Path $Global:LogDir ("batch-{0}-{1}-cloudwatch.txt" -f $safeStepCode, $jobId)

        Write-Host ("[BATCH] logGroup={0}" -f $BatchResearchLogGroup)
        Write-Host ("[BATCH] logStream={0}" -f $logStreamName)
        Write-Host ("[BATCH] cloudWatchLog={0}" -f $cloudWatchLogPath)

        aws logs get-log-events `
            --region $Region `
            --log-group-name $BatchResearchLogGroup `
            --log-stream-name $logStreamName `
            --start-from-head `
            --query "events[].message" `
            --output text | Set-Content -Path $cloudWatchLogPath -Encoding utf8
    }
    else {
        Write-Host "[BATCH] logStream is empty. CloudWatch log download skipped."
    }

    if ($status -ne "SUCCEEDED") {
        throw "Batch job failed. step=$StepCode jobId=$jobId status=$status statusReason=$statusReason exitCode=$exitCode cloudWatchLog=$cloudWatchLogPath"
    }

    return [pscustomobject]@{
        JobId = $jobId
        JobName = $JobName
        JobQueue = $JobQueue
        JobDefinition = $JobDefinition
        Status = $status
        StatusReason = $statusReason
        ExitCode = $exitCode
        StartedAt = $startedAt
        StoppedAt = $stoppedAt
        LogStreamName = $logStreamName
        CloudWatchLogPath = $cloudWatchLogPath
    }
}

