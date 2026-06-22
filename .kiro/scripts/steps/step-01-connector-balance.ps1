# Step 1: CONNECTOR_BALANCE
# Runner: SSM
# Risk: READ_ONLY

$commands = @(
    'set -euo pipefail',
    'echo "===== CONNECTOR_BALANCE START ====="',
    'date -u',
    'cd /home/ec2-user/apps/port-marketconnector/src',
    'export PORT_ENVIRONMENT=paper',
    'export PORT_DB_TARGET=aws-paper',
    '__MARKETCONNECTOR_ENV_BOOTSTRAP__',
    'echo "PORT_ENVIRONMENT=${PORT_ENVIRONMENT:-}"',
    'echo "PORT_DB_TARGET=${PORT_DB_TARGET:-}"',
    'test "${PORT_ENVIRONMENT:-}" = "paper"',
    'test "${PORT_DB_TARGET:-}" = "aws-paper"',
    '/home/ec2-user/apps/port-marketconnector/.venv/bin/python connector_balance.py',
    'echo "===== CONNECTOR_BALANCE END ====="'
)

$commands = @(
    $commands | ForEach-Object {
        if ($_ -eq "__MARKETCONNECTOR_ENV_BOOTSTRAP__") {
            Get-DailyAwsPaperMarketConnectorEnvBootstrapCommands
        }
        else {
            $_
        }
    }
)

$result = Invoke-SsmCommandAndWait `
    -StepCode "CONNECTOR_BALANCE" `
    -InstanceId $MarketConnectorInstanceId `
    -Commands $commands

Write-StepResult `
    -Step 1 `
    -Code "CONNECTOR_BALANCE" `
    -Status "COMPLETED" `
    -Message ("SSM command success. commandId={0}" -f $result.CommandId)
