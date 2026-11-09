import { readFileSync, writeFileSync } from 'node:fs';
import { pathToFileURL } from 'node:url';

const accountVariables = new Set([
  'PORTFOLIO_VIEW_ACCOUNT_DEFAULT_ACCOUNT_NO',
  'PORTFOLIO_BATCH_DEFAULT_ACCOUNT_NO',
]);

const candidateDisabledFlags = [
  'PORTFOLIO_BATCH_EXECUTION_ENABLED',
  'PORTFOLIO_BATCH_LOCAL_FILE_EXECUTION_ENABLED',
  'PORTFOLIO_BATCH_FULL_PIPELINE_EXECUTION_ENABLED',
  'PORTFOLIO_BATCH_PAPER_ORDER_ENABLED',
  'PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_START_ENABLED',
  'PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_STEP_START_ENABLED',
  'PORTFOLIO_STRATEGY_EXECUTION_SUBMIT_ENABLED',
  'PORTFOLIO_SNAPSHOT_REFRESH_ENABLED',
  'PORTFOLIO_DASHBOARD_SNAPSHOT_REFRESH_ENABLED',
  'PORTFOLIO_ORDER_REFRESH_ENABLED',
];

function selectAccountKey(reference) {
  const parts = typeof reference === 'string' ? reference.split(':') : [];
  if (parts.length < 7 || parts.length > 10 || parts[0] !== 'arn'
      || parts[2] !== 'secretsmanager' || parts[5] !== 'secret' || !parts[6]) {
    throw new Error('Account secret must reference AWS Secrets Manager.');
  }
  // Preserve any explicit version stage or version ID supplied by the operator.
  return [...parts.slice(0, 7), 'PAPER_ACNT', parts[8] ?? '', parts[9] ?? ''].join(':');
}

export function prepareTaskDefinition(template, image, candidate = false, accountSource = null) {
  const definition = structuredClone(template);
  const container = definition.containerDefinitions?.[0];
  if (!container || typeof image !== 'string' || !image.trim()) {
    throw new Error('Task definition container and image are required.');
  }
  for (const key of ['taskDefinitionArn', 'revision', 'status', 'requiresAttributes',
    'compatibilities', 'registeredAt', 'registeredBy', 'deregisteredAt']) {
    delete definition[key];
  }
  if (accountSource) {
    if (!candidate) throw new Error('Account source override is only allowed for Candidate preparation.');
    const source = accountSource.containerDefinitions?.[0];
    if (!source || (source.environment ?? []).some(entry => accountVariables.has(entry.name))) {
      throw new Error('Operating account source must use secret references.');
    }
    // Old Candidate templates may contain retired plaintext account defaults.
    container.environment = (container.environment ?? []).filter(entry => !accountVariables.has(entry.name));
    container.secrets = (container.secrets ?? []).filter(entry => !accountVariables.has(entry.name));
    container.secrets.push(...structuredClone((source.secrets ?? []).filter(entry => accountVariables.has(entry.name))));
  }
  if ((container.environment ?? []).some(entry => accountVariables.has(entry.name))) {
    throw new Error('Account defaults must use secret references, not environment values.');
  }
  for (const name of accountVariables) {
    const secrets = (container.secrets ?? []).filter(entry => entry.name === name);
    if (secrets.length > 1 || (name.startsWith('PORTFOLIO_VIEW_') && secrets.length !== 1)) {
      throw new Error('A unique View account secret reference is required.');
    }
    for (const secret of secrets) secret.valueFrom = selectAccountKey(secret.valueFrom);
  }
  container.image = image;
  if (candidate) {
    container.environment = (container.environment ?? [])
      .filter(entry => !candidateDisabledFlags.includes(entry.name));
    container.environment.push(...candidateDisabledFlags.map(name => ({ name, value: 'false' })));
  }
  return definition;
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    const [input, output, image, mode, accountSourcePath] = process.argv.slice(2);
    if (!input || !output || !image || !['candidate', 'production'].includes(mode)
        || (mode === 'candidate' && !accountSourcePath)) {
      throw new Error('Invalid preparation arguments.');
    }
    const source = accountSourcePath ? JSON.parse(readFileSync(accountSourcePath, 'utf8')) : null;
    const definition = prepareTaskDefinition(JSON.parse(readFileSync(input, 'utf8')), image, mode === 'candidate', source);
    writeFileSync(output, JSON.stringify(definition, null, 2) + '\n', { mode: 0o600 });
    console.log('Prepared task definition; no AWS changes performed.');
  } catch {
    // Never echo input JSON, secret references, account values, or image identifiers.
    console.error('Task definition preparation failed; check template structure and account secret references.');
    process.exitCode = 1;
  }
}
