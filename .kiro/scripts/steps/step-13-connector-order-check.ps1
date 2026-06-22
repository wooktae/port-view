# Step 13 - CONNECTOR_ORDER_CHECK
# Runner: SSM
# Risk: ORDER_STATUS_UPDATE
# This step checks broker/KIS paper order status.
# It does not submit new broker/KIS orders, but it may update order/fill status in DB.

$commands = @(
    'set -euo pipefail',
    'echo "===== CONNECTOR_ORDER_CHECK START ====="',
    'date -u',
    'cd /home/ec2-user/apps/port-marketconnector/src',
    'export PORT_ENVIRONMENT=paper',
    'export PORT_DB_TARGET=aws-paper',
    '__MARKETCONNECTOR_ENV_BOOTSTRAP__',
    'echo "PORT_ENVIRONMENT=${PORT_ENVIRONMENT:-}"',
    'echo "PORT_DB_TARGET=${PORT_DB_TARGET:-}"',
    'test "${PORT_ENVIRONMENT:-}" = "paper"',
    'test "${PORT_DB_TARGET:-}" = "aws-paper"',
    '/home/ec2-user/apps/port-marketconnector/.venv/bin/python connector_order_check.py',
    'echo "===== CONNECTOR_ORDER_CHECK END ====="'
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
    -StepCode "CONNECTOR_ORDER_CHECK" `
    -InstanceId $MarketConnectorInstanceId `
    -Commands $commands

Write-StepResult `
    -Step 13 `
    -Code "CONNECTOR_ORDER_CHECK" `
    -Status "COMPLETED" `
    -Message ("SSM command success. commandId={0}" -f $result.CommandId)
