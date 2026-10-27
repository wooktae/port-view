# Implementation Plan

This work plan is the execution unit **limited to this spec (`01-aws-migration-foundation`)**. Every task produces either a document or a static-analysis result; actual AWS resource creation, modification of the 8 MS' code / README / AGENTS.md / CHANGELOG / docs / worklog, and execution of the 8 MS entrypoints are out of scope for this spec. Any task that entails AWS resource creation is separated into a follow-up spec (`02-` onward) and is not included in this spec.

Each task is divided into a small unit so that a single operator can review and approve it in a single session. The indented sub-items in a task body are all acceptance items within the same task.

- [ ] 1. Author the 8-MS inventory table
  - For each of the 8 MS, organize the language / framework / entrypoint type (service/batch/CLI) / external dependencies / DB schema / `search_path` / sensitive-information items into a single table.
  - Artifact location: a separate appendix section within `design.md` (`Appendix A: MS Inventory`).
  - Reference documents: the READMEs and AGENTS.md of the 8 MS (read-only).
  - External calls / code execution prohibited.
  - _Requirements: 1, 2, 3_

- [ ] 2. Finalize the per-MS compute candidate comparison table
  - For each of the 8 MS, finalize at least 2 AWS compute candidates, the recommendation, and the recommendation rationale in a table.
  - Artifact location: reinforce the `Per-MS Compute Candidate Comparison and Recommendation` section of `design.md` in table form.
  - `port-marketconnector` states the broker IP / token singularity rationale and keeps EC2 as one of the top-tier candidates.
  - `port_strategy_common` states no separate compute, only a packaging recommendation.
  - External calls / code execution prohibited.
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8_

- [ ] 3. Finalize the RDS for PostgreSQL migration decisions
  - Finalize the per-environment RDS instance (`portfolio-dev`, `portfolio-paper`, `portfolio-live`) recommendation.
  - State the 10-schema preservation policy and the per-MS `search_path` preservation policy in the body.
  - Finalize data migration as `pg_dump` / `pg_restore` first choice, AWS DMS second.
  - State the backup policy (automated backup retention, manual snapshot, PITR) per environment.
  - Artifact location: reinforce the `RDS for PostgreSQL Migration Design` section of `design.md`.
  - Actual RDS creation prohibited. Actual dump / restore prohibited.
  - _Requirements: 2.1, 2.2, 2.4, 2.5, 2.6, 2.7_

- [ ] 4. Draft the DB Role matrix
  - For per-MS DB roles (e.g., `marketconnector_app`, `crawler_app`, `preprocessor_app`, `decision_app`, `execution_app`, `research_app`, `view_app`), author a per-schema read / write permission matrix in a table.
  - State the policy that the environment variable keys (`INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`) are preserved as-is and only the values branch per role.
  - Artifact location: finalize the `DB Role / Permissions` section of `design.md` in table form.
  - Actual GRANT / REVOKE execution prohibited.
  - _Requirements: 2.3, 3.6_

- [ ] 5. Finalize the sensitive-information externalization mapping table
  - Finalize the Secrets Manager / SSM Parameter Store usage criteria in a table.
  - Items: KIS app key / app secret / base URL / account number / account product code / `access_token.txt` / `INTEREST_DB_PASSWORD` / Slack webhook / Naver API client secret / `INTEREST_DB_HOST`,`PORT`,`NAME` / `PORTFOLIO_DB_NAME` / `INTEREST_DB_USER` / `PORT_ACCOUNT_NO` / `PORT_BROKER_NAME` / `PORT_ENVIRONMENT` / `PORT_STRATEGY_NAME` / `PORT_STRATEGY_VERSION` / `PORT_MAX_ORDER_AMOUNT_RATIO` / `PORT_MIN_ORDER_AMOUNT` / Naver API client id / Chrome path.
  - Write only `[REDACTED]` in every secret position.
  - State environment variable key compatibility in the body.
  - Artifact location: finalize the `Secrets / Environment Variable Design` section of `design.md` in table form.
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

- [ ] 6. Finalize the rationale for the `access_token.txt` storage location
  - Organize the candidate comparison (EC2 local + S3 periodic backup, EFS, Secrets Manager) in a table.
  - Write the rationale for finalizing EC2 local + S3 backup as first choice and EFS as second, under the market connector single-EC2-instance premise.
  - Mark the token backup / restore procedure on EC2 replacement as a Runbook candidate item.
  - Artifact location: reinforce the `access_token.txt Storage Location` section of `design.md` in table form.
  - Actual EC2 / S3 / EFS creation prohibited.
  - _Requirements: 3.8, 6.5_

- [ ] 7. Finalize the network decisions
  - Reinforce the single VPC, multiple AZ, public/private subnet configuration recommendation with a (text) diagram.
  - Market connector outbound: finalize EC2 + EIP first choice, NAT Gateway EIP second.
  - `port-view` → `port-marketconnector` internal call: finalize ECS Service Discovery / Internal ALB first choice.
  - Operator access: prohibit direct SSH exposure, recommend using SSM Session Manager.
  - State the VPC endpoint recommendation items (S3, ECR, Secrets Manager, SSM, CloudWatch Logs).
  - Artifact location: reinforce the `Network Design` section of `design.md`.
  - Actual VPC / SG / EIP creation prohibited.
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7_

- [ ] 8. Finalize the container / ECR / CI/CD standards
  - Finalize the ECR naming convention (`port-{ms}`) and the tag convention (`:{git-sha}` + `:{env}` alias).
  - `port_strategy_common` packaging: git submodule + Dockerfile build stage first choice, CodeArtifact second.
  - CI/CD: GitHub Actions → ECR → ECS / EC2 deployment first choice, CodePipeline+CodeBuild+CodeDeploy second.
  - State the policy of not baking secrets into the image at build time.
  - State the per-environment promotion flow (dev auto, paper / live manual approval).
  - Separately state market connector EC2 deployment as AMI baseline + systemd unit + CodeDeploy first choice.
  - Artifact location: reinforce the `Container Images and CI/CD` section of `design.md`.
  - Actual ECR / pipeline creation prohibited.
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

- [ ] 9. Finalize the observability standards (logs / metrics / Alarm)
  - Finalize the log group naming convention (`/portfolio/{env}/{ms}`).
  - Finalize the per-environment retention recommendation (live 90 / paper 30 / dev 7~14).
  - Finalize standard metrics + domain metrics (broker error rate, Daily Batch step result, intraday heartbeat, fill sync lag).
  - Keep alarm thresholds as placeholders only and mark them as operator decisions.
  - Artifact location: reinforce the log/metric part of the `Observability / Alerting / Runbook` section of `design.md`.
  - CloudWatch resource creation prohibited.
  - _Requirements: 6.1, 6.2_

- [ ] 10. Finalize the Slack alert / Daily Batch integration recommendation
  - State that domain notifications preserve the existing `port-view` `SlackNotificationService`.
  - Infrastructure alarms via CloudWatch Alarm → SNS → Lambda → Slack webhook first choice, AWS Chatbot second.
  - State the Daily Batch recommendation as EventBridge Scheduler → Step Functions → ECS RunTask. The `strategy_daily_batch_run` and `strategy_daily_batch_step_log` tables are preserved.
  - Artifact location: reinforce the `Slack Alerts` and `Daily Batch Integration` parts of `design.md`.
  - Actual SNS / Lambda / Step Functions creation prohibited.
  - _Requirements: 6.3, 6.4_

- [ ] 11. Author the operational Runbook skeleton v0
  - For the 5 scenarios (broker token expiry/reissue, KIS order failure, RDS failover, Daily Batch failure re-execution, intraday monitor interruption), author the symptom / procedure / whether automatic retry is allowed as body items.
  - Author the automatic retry policy table (allowed / prohibited) as a separate item.
  - Artifact location: reinforce the `Runbook Items` section of `design.md`.
  - Actual operational procedure execution prohibited.
  - _Requirements: 6.5, 6.6_

- [ ] 12. Finalize the environment separation / cost estimation / phased cutover roadmap
  - State whether broker live connection is allowed in the dev / paper / live environments.
  - Express the candidate cost-item list as low / medium / high estimate ranges.
  - State entry conditions / exit (rollback) conditions in the 6-stage cutover roadmap.
  - Keep the paper validation N business days as a placeholder.
  - Artifact location: reinforce the `Environment Separation / Cost / Phased Cutover` section of `design.md`.
  - Actual cost settlement prohibited. Actual cutover prohibited.
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ] 13. Finalize the follow-up spec candidate list
  - For the follow-up spec candidates (`02-aws-network-and-rds`, `03-marketconnector-ec2`, `04-strategy-batch-stepfunctions`, `05-port-view-ecs-and-runbook`, `06-secrets-and-iam`, `07-cicd-pipelines`), author a one-line summary and input dependencies in a table.
  - Artifact location: reinforce the `Follow-up Spec Candidates` section of `design.md`.
  - Follow-up specs are not created within the scope of this spec.
  - _Requirements: 7.3_

- [ ] 14. Re-verify this spec's safety constraints
  - Confirm the artifacts are limited to only the 3 files `requirements.md`, `design.md`, and `tasks.md`.
  - Confirm the 8 MS' README / AGENTS.md / CHANGELOG / docs / worklog were not modified using `git status --short` (read-only commands only).
  - Confirm there are no source code changes in the 8 MS (read-only).
  - Confirm this spec's artifacts do not contain actual secrets via grep (search only, no external calls). Confirm there are no password / token / app key / app secret / account number / webhook URL patterns and that everything is `[REDACTED]`.
  - Confirm that no 8-MS entrypoint was executed.
  - Artifact location: all documents within this spec.
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7_

# Items Not Included in This Spec (Separated into Follow-up Specs)

The following items are explicitly out of scope for this spec. They are addressed in follow-up specs.

- Actual VPC / Subnet / SG / NAT Gateway / EIP creation
- Actual RDS for PostgreSQL instance creation, schema creation, data migration
- Actual ECR repository / image build / push
- Actual ECS Cluster / Task Definition / Service / Step Functions / EventBridge Scheduler creation
- Actual Secrets Manager / SSM Parameter Store registration
- IAM Role / Policy authoring and application
- Market connector EC2 + EIP creation and broker-side IP registration
- `port_strategy_common` packaging code changes or build automation authoring
- Changing the external call method of the `port-view` Daily Batch code
- Slack webhook URL registration, SNS Topic creation, AWS Chatbot integration
- CloudWatch Alarm / Metric Filter registration
- Actual operational Runbook testing (in particular, rehearsing the broker order failure scenario)

Each of the above items proceeds separately in a follow-up spec, and the follow-up specs use the decisions of this `01-aws-migration-foundation` spec as input.
