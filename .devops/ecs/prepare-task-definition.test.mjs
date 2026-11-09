import test from 'node:test';
import assert from 'node:assert/strict';
import { prepareTaskDefinition } from './prepare-task-definition.mjs';

const reference = 'arn:aws:secretsmanager:REGION:ACCOUNT:secret:account-fixture';
const viewName = 'PORTFOLIO_VIEW_ACCOUNT_DEFAULT_ACCOUNT_NO';
const batchName = 'PORTFOLIO_BATCH_DEFAULT_ACCOUNT_NO';

function template() {
  return {
    family: 'view-fixture', revision: 1, status: 'ACTIVE', taskRoleArn: '[REDACTED_ARN]',
    containerDefinitions: [{
      name: 'view', image: 'previous-image',
      environment: [{ name: 'PORTFOLIO_BATCH_EXECUTION_ENABLED', value: 'true' }],
      secrets: [{ name: viewName, valueFrom: reference }, { name: batchName, valueFrom: reference },
        { name: 'SPRING_DATASOURCE_PASSWORD', valueFrom: '[REDACTED_SECRET_ARN]' }],
    }],
  };
}

test('selects PAPER_ACNT for View and existing Batch references without changing other configuration', () => {
  const input = template();
  const result = prepareTaskDefinition(input, 'tested-image');
  assert.equal(result.revision, undefined);
  assert.equal(result.taskRoleArn, input.taskRoleArn);
  assert.deepEqual(result.containerDefinitions[0].environment, input.containerDefinitions[0].environment);
  assert.deepEqual(result.containerDefinitions[0].secrets.map(s => s.valueFrom),
    [reference + ':PAPER_ACNT::', reference + ':PAPER_ACNT::', '[REDACTED_SECRET_ARN]']);
  assert.equal(input.containerDefinitions[0].image, 'previous-image');
  assert.deepEqual(prepareTaskDefinition(result, 'tested-image'), result);
});

test('preserves pinned secret versions and permits an absent Batch override', () => {
  const input = template();
  input.containerDefinitions[0].secrets = [{ name: viewName, valueFrom: reference + ':old-key::version-fixture' }];
  assert.equal(prepareTaskDefinition(input, 'image').containerDefinitions[0].secrets[0].valueFrom,
    reference + ':PAPER_ACNT::version-fixture');
  input.containerDefinitions[0].secrets[0].valueFrom = reference + '::AWSPREVIOUS:';
  assert.equal(prepareTaskDefinition(input, 'image').containerDefinitions[0].secrets[0].valueFrom,
    reference + ':PAPER_ACNT:AWSPREVIOUS:');
});

test('disables Candidate execution and refresh paths while retaining production flags', () => {
  const input = template();
  const flags = prepareTaskDefinition(input, 'image', true).containerDefinitions[0].environment;
  assert.equal(flags.length, 10);
  assert.ok(flags.every(flag => flag.value === 'false'));
  assert.equal(prepareTaskDefinition(input, 'image').containerDefinitions[0].environment[0].value, 'true');
});

test('rejects missing, duplicate, plaintext, and malformed account references', () => {
  for (const mutate of [
    c => { c.secrets = []; },
    c => { c.secrets.push(c.secrets[0]); },
    c => { c.environment.push({ name: viewName, value: 'invalid-private-value' }); },
    c => { c.secrets[0].valueFrom = 'invalid-private-value'; },
  ]) {
    const input = template();
    mutate(input.containerDefinitions[0]);
    assert.throws(() => prepareTaskDefinition(input, 'image'), error => !error.message.includes('invalid-private-value'));
  }
});

test('replaces retired Candidate account values using only operating secret references', () => {
  const candidate = template();
  candidate.containerDefinitions[0].environment.push({ name: viewName, value: 'retired-account-fixture' });
  candidate.containerDefinitions[0].secrets = [];
  const prepared = prepareTaskDefinition(candidate, 'image', true, template()).containerDefinitions[0];
  assert.ok(!prepared.environment.some(entry => entry.name === viewName || entry.name === batchName));
  assert.equal(prepared.secrets.length, 2);
  assert.ok(prepared.secrets.every(entry => entry.valueFrom === reference + ':PAPER_ACNT::'));
  assert.equal(candidate.containerDefinitions[0].environment[1].value, 'retired-account-fixture');
  assert.throws(() => prepareTaskDefinition(candidate, 'image', true, { containerDefinitions: [{}] }));
  const source = template();
  source.containerDefinitions[0].environment.push({ name: viewName, value: 'invalid-private-value' });
  assert.throws(() => prepareTaskDefinition(candidate, 'image', true, source));
});
