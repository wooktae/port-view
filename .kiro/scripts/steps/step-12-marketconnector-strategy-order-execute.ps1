# Step 12 - MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE
# Runner: SSM
# Risk: PAPER_ORDER_GATE
# This step may submit KIS paper orders.
# It is blocked unless the main wrapper is explicitly run with -AllowPaperOrderExecute.

if (-not $AllowPaperOrderExecute) {
    throw "PAPER_ORDER_GATE blocked inside Step 12. Re-run main wrapper with -AllowPaperOrderExecute only when you intentionally want to submit KIS paper orders."
}

$commands = @(
    'set -euo pipefail',
    'echo "===== MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE START ====="',
    'date -u',
    'cd /home/ec2-user/apps/port-marketconnector/src',
    'export PORT_ENVIRONMENT=paper',
    'export PORT_DB_TARGET=aws-paper',
    'if [ -f /tmp/inject-env.sh ]; then source /tmp/inject-env.sh; else echo "[BLOCKER] /tmp/inject-env.sh not found"; exit 20; fi',
    'echo "PORT_ENVIRONMENT=${PORT_ENVIRONMENT:-}"',
    'echo "PORT_DB_TARGET=${PORT_DB_TARGET:-}"',
    'test "${PORT_ENVIRONMENT:-}" = "paper"',
    'test "${PORT_DB_TARGET:-}" = "aws-paper"',
    'echo "[PAPER_ORDER_GATE] About to execute connector_strategy_order_execute.py --execute"',
    '/home/ec2-user/apps/port-marketconnector/.venv/bin/python connector_strategy_order_execute.py --execute',
    'echo "===== MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE END ====="'
)

$result = Invoke-SsmCommandAndWait `
    -StepCode "MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE" `
    -InstanceId $MarketConnectorInstanceId `
    -Commands $commands

Write-StepResult `
    -Step 12 `
    -Code "MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE" `
    -Status "COMPLETED" `
    -Message ("SSM command success. commandId={0}" -f $result.CommandId)
