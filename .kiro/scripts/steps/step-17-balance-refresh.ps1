# Step 17 - BALANCE_REFRESH
# Runner: SSM
# Risk: BALANCE_REFRESH
# This step refreshes paper account balance and position snapshots.
# It does not submit broker/KIS orders.

$commands = @(
    'set -euo pipefail',
    'echo "===== BALANCE_REFRESH START ====="',
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
    'echo "===== BALANCE_REFRESH END ====="'
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
    -StepCode "BALANCE_REFRESH" `
    -InstanceId $MarketConnectorInstanceId `
    -Commands $commands

Write-StepResult `
    -Step 17 `
    -Code "BALANCE_REFRESH" `
    -Status "COMPLETED" `
    -Message ("SSM command success. commandId={0}" -f $result.CommandId)
