param(
    [string]$RunDate = (Get-Date).ToString("yyyy-MM-dd"),
    [string]$Region = "ap-northeast-2",
    [ValidateSet("aws-paper")]
    [string]$Environment = "aws-paper",
    [int]$StartStep = 1,
    [int]$EndStep = 17,
    [switch]$DryRun,
    [switch]$AllowPaperOrderExecute
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Force UTF-8 for AWS CLI / Python-based CLI stdout on Windows.
# This prevents cp949 failures when remote logs contain emoji or Korean text.
chcp.com 65001 > $null
[Console]::InputEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$env:PYTHONIOENCODING = "utf-8"
$env:PYTHONUTF8 = "1"

$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

. (Join-Path $ScriptRoot "daily-aws-paper.config.ps1")
. (Join-Path $ScriptRoot "daily-aws-paper.functions.ps1")

if ($StartStep -lt 1 -or $StartStep -gt 17) {
    throw "StartStep must be between 1 and 17. StartStep=$StartStep"
}

if ($EndStep -lt 1 -or $EndStep -gt 17) {
    throw "EndStep must be between 1 and 17. EndStep=$EndStep"
}

if ($StartStep -gt $EndStep) {
    throw "StartStep must be less than or equal to EndStep. StartStep=$StartStep EndStep=$EndStep"
}

$RunId = "daily-aws-paper-{0}" -f (Get-Date -Format "yyyyMMdd-HHmmss")

# ------------------------------------------------------------
# Output directories
# ------------------------------------------------------------
$RunOutputDir = Join-Path $BaseOutputDir $RunId
$LogDir = Join-Path $RunOutputDir "logs"
$OverrideDir = Join-Path $RunOutputDir "overrides"
$SummaryDir = Join-Path $RunOutputDir "summary"

$Global:RunOutputDir = $RunOutputDir
$Global:LogDir = $LogDir
$Global:OverrideDir = $OverrideDir
$Global:SummaryDir = $SummaryDir

New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
New-Item -ItemType Directory -Force -Path $OverrideDir | Out-Null
New-Item -ItemType Directory -Force -Path $SummaryDir | Out-Null

$SummaryPath = Join-Path $SummaryDir "run-summary.txt"
$Global:SummaryPath = $SummaryPath

@(
    "Daily AWS Paper Wrapper Summary",
    "RunDate=$RunDate",
    "Region=$Region",
    "Environment=$Environment",
    "RunId=$RunId",
    "StartStep=$StartStep",
    "EndStep=$EndStep",
    "DryRun=$DryRun",
    "PaperOrder=$AllowPaperOrderExecute",
    "CreatedAt=$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
) | Set-Content -Path $SummaryPath -Encoding utf8

$DailySteps = Get-DailyAwsPaperSteps -ScriptRoot $ScriptRoot

$SelectedSteps = $DailySteps | Where-Object {
    $_.Step -ge $StartStep -and $_.Step -le $EndStep
}

Write-Host "============================================================"
Write-Host "Daily AWS Paper Wrapper"
Write-Host "============================================================"
Write-Host "RunDate     : $RunDate"
Write-Host "Region      : $Region"
Write-Host "Environment : $Environment"
Write-Host "RunId       : $RunId"
Write-Host "StartStep   : $StartStep"
Write-Host "EndStep     : $EndStep"
Write-Host "DryRun      : $DryRun"
Write-Host "PaperOrder  : $AllowPaperOrderExecute"
Write-Host "============================================================"

Write-Host ""
Write-Host "[PLAN] Selected steps"
$SelectedSteps | Select-Object Step, Code, Runner, Risk, StepFileStatus | Format-Table -AutoSize

if ($DryRun) {
    Write-Host ""
    Write-Host "[DRY_RUN] Execution plan only. No AWS command submitted."
    exit 0
}

Write-Host ""

foreach ($step in $SelectedSteps) {
    Write-StepHeader -Step $step.Step -Code $step.Code

    if ($step.Risk -eq "PAPER_ORDER_GATE" -and -not $AllowPaperOrderExecute) {
        throw ("PAPER_ORDER_GATE blocked. Step={0} Code={1}. Re-run with -AllowPaperOrderExecute only when you intentionally want to submit KIS paper orders." -f $step.Step, $step.Code)
    }

    Invoke-DailyStepFile -StepInfo $step
}

Write-Host ""
Write-Host "[DONE] Selected steps completed."
exit 0
