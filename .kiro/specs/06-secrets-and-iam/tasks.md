# Tasks — 06-secrets-and-iam

These tasks are the minimal set that splits the decisions of [`./design.md`](./design.md) and R1~R14 of [`./requirements.md`](./requirements.md) into actual progress-unit tasks. The goal is to close 06 on paper today and move on to the 03-marketconnector-ec2 follow-up organization.

Each task is written in the checkbox form of the 02 spec tasks.md format (`- [ ] N. ...`), but since this spec is mostly direct-operator-work / document-organization tasks, the deliverable / responsibility / input / completion criteria are briefly specified as sub-bullets.

In this phase, only one file `tasks.md` is created.
`runbook.md` / `validation-checklist.md` / `operation-notes.md` are the responsibility of follow-up phases,
and only the creation plan is set in tasks 19 ~ 21 of these tasks.
All deliverables follow the [`./design.md`](./design.md) §11 safety constraints.

- No actual AWS resource creation · modification · deletion
- No modification of the 8 MS code / docs
- No recording of actual secret value / account number / account-id / RDS endpoint hostname / actual secret ARN / access key

## 1. Document Status Check

- [ ] 1. Confirm requirements.md / README.md / design.md creation
  - Deliverable: none (status check only)
  - Responsibility: Kiro
  - Input: [`./requirements.md`](./requirements.md), [`./README.md`](./README.md), [`./design.md`](./design.md)
  - Completion criteria: all three files exist. Confirm the mapping of requirements.md R1~R14 and design.md §1~§11.
  - _Requirements: R7.1, R8.1, R9.1, R14.7_

## 2. Confirm the Secrets / Parameters Structure

- [ ] 2. Lock the KIS / RDS secret items as Secrets Manager candidates
  - Deliverable: adopt the design.md §1.1 / §1.4 / §2.4 tables as-is. No change
  - Input: design.md §1, requirements.md R1.1 / R1.2 / R1.5
  - Completion criteria: KIS app key, KIS app secret, KIS paper account number (`PAPER_ACNT` / `ACNT_PRDT_CD`), RDS `marketconnector_app` connection info, RDS master password (`/portfolio/paper/rds/master` compatibility maintained) are specified as Secrets Manager candidates
  - _Requirements: R1.1, R1.2, R1.5, R2.5_

- [ ] 3. Lock the general setting values as SSM Parameter Store candidates
  - Deliverable: adopt the design.md §1.2 table as-is
  - Input: design.md §1.2, requirements.md R1.3 / R1.6
  - Completion criteria: KIS base URL, Connector Flask host / port / debug, `PORT_ENVIRONMENT`, `PORT_BROKER_NAME`, `PORT_STRATEGY_NAME`, `PORT_STRATEGY_VERSION` are specified as SSM Parameter Store candidates
  - _Requirements: R1.3, R1.6_

- [ ] 4. First-lock the Slack webhook storage location
  - Deliverable: adopt the design.md §1.3 decision value (tentative: Secrets Manager recommended, cost-saving option SSM SecureString) as-is. In this task, only the decision update is handed off as an §7 OD-OBS-004 update candidate
  - Input: design.md §1.3, requirements.md R1.4
  - Completion criteria: the comparison of the two candidates Secrets Manager recommended / SSM SecureString cost-saving option is specified in one place (design §1.3). The final lock proceeds in the OD-OBS-004 update of task 17
  - _Requirements: R1.4_

## 3. Confirm Naming / Environment-Variable Compatibility

- [ ] 5. Adopt the naming `/portfolio/{env}/{service}/{item}` first recommendation
  - Deliverable: adopt design.md §2. This spec's first-confirmed services limited to `marketconnector` / `rds`
  - Input: design.md §2, requirements.md R2
  - Completion criteria: env=`paper`/`live`, 2 first-confirmed services, compatibility maintained (`/portfolio/paper/rds/master`), and the policy of not including endpoint / account number / secret value / password / token in the name are specified in design
  - _Requirements: R2.1, R2.2, R2.3, R2.4, R2.5, R2.6_

- [ ] 6. Confirm the 8 MS environment-variable key compatibility
  - Deliverable: adopt design.md §3. Maintain the no-modification principle for code / README / AGENTS.md
  - Input: design.md §3, requirements.md R3
  - Completion criteria: the 13 keys `INTEREST_DB_*` / `PORTFOLIO_DB_NAME` / `PORT_*` are maintained + the `port-marketconnector` config.py KIS identifier externalization mapping is specified in design §2.4 / §3.2
  - _Requirements: R3.1, R3.2, R3.3, R3.4_

## 4. Confirm the IAM Role Least-Privilege Structure

- [ ] 7. Confirm the MarketConnector EC2 Instance Role / Profile name candidates
  - Deliverable: adopt design.md §4.1. Role: `portfolio-paper-marketconnector-ec2-role`. Profile: `portfolio-paper-marketconnector-ec2-profile`
  - Input: design.md §4.1, requirements.md R4.1
  - Completion criteria: the name candidates are specified in design. The Trust Policy Principal `Service: ec2.amazonaws.com` is specified
  - _Requirements: R4.1_

- [ ] 8. Confirm the Permission Policy matrix
  - Deliverable: adopt design.md §4.2
  - Input: design.md §4.2, requirements.md R4.2 / R4.3 / R4.6
  - Completion criteria: the three statements are specified in design
    - `SecretsManagerRead` — `secretsmanager:GetSecretValue`, `DescribeSecret` + exact secret ARN placeholder
    - `SsmParameterRead` — `ssm:GetParameter` / `GetParameters` / `GetParametersByPath` + `/portfolio/paper/marketconnector/*` prefix
    - `KmsDecrypt` (conditional)
  - _Requirements: R4.2, R4.3, R4.6_

- [ ] 9. Confirm the prohibition policy matrix
  - Deliverable: adopt design.md §4.3
  - Input: design.md §4.3, requirements.md R4.4
  - Completion criteria: `Resource: "*"`, `Action: "*"`, `secretsmanager:*`, `ssm:*`, other-service-prefix Resource, other-environment-prefix Resource are all specified as prohibited
  - _Requirements: R4.4_

- [ ] 10. Confirm the ARN notation rule
  - Deliverable: adopt design.md §4.5
  - Input: design.md §4.5, requirements.md R4.5 / R14.4
  - Completion criteria: use only a placeholder (`arn:aws:iam::<account-id>:role/...`, etc.) or `[REDACTED]` at every ARN position. Confirm the actual account-id / actual secret ARN are not recorded
  - _Requirements: R4.5, R14.4_

## 5. Confirm the Access Key Non-Use Principle

- [ ] 11. Lock the EC2 internal Access Key non-use principle
  - Deliverable: adopt design.md §5
  - Input: design.md §5, requirements.md R5
  - Completion criteria: the following policy is specified in design
    - access key storage prohibited locations: `~/.aws/credentials` / `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` /
      systemd Environment / EnvironmentFile / dotfile / `.env`
    - use only IMDSv2 + Instance Role credentials
  - _Requirements: R5.1, R5.2, R5.5_

- [ ] 12. Confirm the Access Key validation location (actual validation is a follow-up phase)
  - Deliverable: adopt the design.md §5.2 table. The actual validation procedure is the responsibility of the follow-up runbook.md / validation-checklist.md
  - Input: design.md §5.2, requirements.md R5.3
  - Completion criteria: the `aws sts get-caller-identity` / `aws configure list` validation location is specified as runbook.md responsibility. Among the 8 inspection areas of validation-checklist.md, the access key file non-existence / assumed-role ARN match items are specified
  - _Requirements: R5.3_

## 6. Confirm the ECS Task Role Reuse Pattern

- [ ] 13. Lock the Task Execution Role / Task Role separation pattern
  - Deliverable: adopt design.md §6.1
  - Input: design.md §6.1, requirements.md R6.2
  - Completion criteria: the responsibility separation table for Task Execution Role (image pull / Logs write / `secrets` field injection) and Task Role (runtime read) is specified
  - _Requirements: R6.2_

- [ ] 14. Confirm the ECS `secrets` field pattern
  - Deliverable: adopt design.md §6.2. Specify only the JSON multi-key `:json-key::` suffix-form placeholder
  - Input: design.md §6.2, requirements.md R6.3
  - Completion criteria: the `name=<ENV_KEY>, valueFrom=<arn placeholder>` format is specified. The actual ARN suffix / json-key branching is handed off as the responsibility of the 03 / 08 / 04 / 05 / 09 specs
  - _Requirements: R6.3, R6.4, R6.5_

- [ ] 15. Confirm the follow-up spec division matrix
  - Deliverable: adopt design.md §6.3 / §9.1
  - Input: design.md §6.3 / §9.1, requirements.md R6.4 / R13
  - Completion criteria: the per-spec (03 / 08 / 04 / 05 / 09 / 10) matrix of items using this spec's (06) input is specified. The naming / env / wildcard prohibition decisions are specified as items follow-up specs must not change
  - _Requirements: R13.1, R13.2, R13.3, R13.4_

## 7. Hand Off _common Document Update Candidates

Performed only within this spec phase. Actual _common file modification can proceed as a separate task after operator approval.

- [ ] 16. Organize the operator-decisions.md update candidates
  - Deliverable: adopt the design.md §7 table as-is. Actual [`../_common/operator-decisions.md`](../_common/operator-decisions.md) modification proceeds in this task upon operator approval
  - Input: design.md §7, requirements.md R10
  - Completion criteria: the OD-SEC-001 / OD-OBS-004 update candidate values and the new OD-SEC-005 / OD-SEC-006 candidates are specified in the design §7.1 table. OD-SEC-002 / OD-SEC-003 consistency is specified in §7.2. The Status label (CONFIRMED/TENTATIVE/TBD/DEFERRED + 🟢/🟡/🔴/🔵) rule is specified in §7.3
  - _Requirements: R10.1, R10.2, R10.3, R10.4, R10.5, R10.6, R10.7_

- [ ] 17. Organize the risk-register.md update candidates
  - Deliverable: adopt the design.md §8 table. The actual IDs of the 4 R-SEC candidates are assigned the next available number in [`../_common/risk-register.md`](../_common/risk-register.md) and updated in this task upon operator approval
  - Input: design.md §8, requirements.md R12
  - Completion criteria: one line each of mitigation / detection / rollback for the 4 items over-permission / EC2 secret plaintext exposure / EC2 access key file / naming mismatch is specified. Same as the 02 spec column format
  - _Requirements: R12.1, R12.2, R12.3, R12.4, R12.5, R12.6_

- [ ] 18. Organize the followups-overview.md update candidates
  - Deliverable: adopt the design.md §9 table. Actual [`../_common/followups-overview.md`](../_common/followups-overview.md) modification proceeds in this task upon operator approval
  - Input: design.md §9, requirements.md R11
  - Completion criteria: the first application environment / first scope / out-of-scope, the 03 / 08 / 04 / 05 / 09 / 10 spec input items, and the one line for the 06 ↔ 03 order are specified in §9
  - _Requirements: R11.1, R11.2, R11.3, R11.4_

## 8. Follow-Up Phase Deliverable Creation Plan (not created in this phase)

- [ ] 19. runbook.md to be created
  - Deliverable candidate: [`./runbook.md`](./runbook.md) (created in a separate phase)
  - Input: design.md §10, requirements.md R7
  - Completion criteria (follow-up phase): the following stages are decomposed with `[실행]`/`[확인]`/`[준비]`/`[복구]` labels
    - (a) Secrets Manager registration → (b) SSM Parameter registration → (c) Instance Role / Profile creation
    - (d) policy attach → (e) EC2 attach → (f) read validation → (g) Connector smoke test
  - _Requirements: R7.1, R7.2, R7.3, R7.4, R7.5, R7.6_

- [ ] 20. validation-checklist.md to be created
  - Deliverable candidate: [`./validation-checklist.md`](./validation-checklist.md)
  - Input: design.md §10, requirements.md R8
  - Completion criteria (follow-up phase): includes all 4 labels + 8 inspection areas
    - Labels: `[O]` / `[X]` / `[Kiro 후속 작업 필요]` / `[운영자 확인 필요]`
    - Inspection areas: Secrets inventory / Parameter inventory / Role · Profile existence / policy matrix match /
      0 wildcards / access key file non-existence / assumed-role ARN match / Connector smoke test
  - _Requirements: R8.1, R8.2, R8.3, R8.4, R8.5_

- [ ] 21. operation-notes.md to be created
  - Deliverable candidate: [`./operation-notes.md`](./operation-notes.md)
  - Input: design.md §10, requirements.md R9
  - Completion criteria (follow-up phase): per-date accumulative records (`## YYYY-MM-DD ...`), no secret value recorded (success/failure only), IAM change record template (change date/changer/reason/before-after item summary), operator-direct-execution division specified in the body
  - _Requirements: R9.1, R9.2, R9.3, R9.4, R9.5, R9.6_

## 9. Close 06 / Hand Off to 03

- [ ] 22. Close the 06 minimal design on paper
  - Deliverable: confirm the status of the 4 files requirements.md / README.md / design.md / tasks.md in this spec folder
  - Input: tasks 1 ~ 21 of these tasks
  - Completion criteria
    - The 4 files exist. The design.md §11 safety constraints and task 22 match.
    - No actual AWS resource creation / modification / deletion.
    - No modification of the 8 MS code / docs.
    - No actual secret value / account number / account-id / RDS endpoint hostname / actual secret ARN / access key recorded in any deliverable
  - _Requirements: R14.1, R14.2, R14.3, R14.4, R14.5, R14.6, R14.7_

- [ ] 23. Hand off to the 03-marketconnector-ec2 follow-up organization
  - Deliverable: specify handing off this spec's §4 (Instance Role matrix), §5 (Access Key non-use principle), §6 (Task Role skeleton), §7 (OD candidates) as input items for the 03 spec (planned)
  - Input: design.md §6.3 / §9.1 / §9.3
  - Completion criteria: at 03 spec entry, this spec's decisions can be received as input to proceed to the EC2 formal operation (Connector / Flask / KIS API / RDS connection) stage
  - _Requirements: R11.4, R13.1_

## 10. What These tasks.md Do Not Do (safety constraints)

- Actual Secrets Manager secret creation / modification / deletion (performed directly by the operator, follow-up runbook.md)
- Actual SSM Parameter creation / modification / deletion (同)
- Actual IAM Role / Policy / Instance Profile creation / change / deletion (同)
- EC2 instance Instance Profile attach change (同)
- KMS Key creation / change (performed directly by the operator when applicable)
- Modifying the README / AGENTS.md / CHANGELOG / docs / worklog / source code of the following 8 MS
  - `port-view`, `port-marketconnector`
  - `port-interest-crawler`, `port-interest-preprocessor`
  - `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`
- Running the 8 MS entrypoints, broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / order / fill / Daily Batch / intraday monitor calls
- Actual `secretsmanager:GetSecretValue` call (performed only by the operator, Kiro's automatic validation uses only `DescribeSecret` metadata)
- live rotation automation / GitHub Actions OIDC / 8 MS full IAM matrix / aws-live IAM work

## Task Dependency Graph

Since this is a document / decision organization flow, the waves are short. Even tasks that use the same design section as input are placed in the same wave because there is no deliverable conflict.

```text
1 (document status check)
  └─> 2, 3, 4 (Secrets/Parameter/Slack storage location)
        └─> 5, 6 (naming / environment-variable compatibility)
              └─> 7, 8, 9, 10 (Instance Role matrix)
                    └─> 11, 12 (Access Key non-use principle)
                          └─> 13, 14, 15 (Task Role pattern / follow-up spec division)
                                └─> 16, 17, 18 (_common update candidates)
                                      └─> 19, 20, 21 (follow-up phase deliverable creation plan)
                                            └─> 22 (close 06)
                                                  └─> 23 (hand off to 03)
```
