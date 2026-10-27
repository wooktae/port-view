# Operator Decisions — AWS Migration

## Purpose

This document is the single source of truth managing the operator decisions of the PORT-STRATEGY-AI AWS Migration.

A Decision ID, once assigned, is not reused or renumbered.

Each decision is managed in a 2-column table based by default on `Status / Decision / Selected value / Next review / Cost impact / Operational note / Related spec`.

Per-date validation history, executionName, ARN, commandId, revision, SHA256, DB figures, and raw log are not accumulated in this document.

Detailed execution evidence is managed in each spec's `operation-notes.md`, work history in `WORKLOG.md`, and risks in `risk-register.md`.

Sensitive information originals are not recorded; only `[REDACTED]`-family placeholders are used.

## Status Legend

| Item | Value |
| --- | --- |
| 🟢 확정 | Used as the reference value for follow-up specs |
| 🟡 잠정 | Currently adopted but changeable after follow-up validation |
| 🔴 미정 | Operator decision needed |
| 🔵 보류 | Re-review after a separate phase or condition is met |

## Decision Dashboard

### Status summary

| Item | Value |
| --- | --- |
| Total | 100 |
| 🟢 확정 | 55 |
| 🟡 잠정 | 42 |
| 🔴 미정 | 2 |
| 🔵 보류 | 1 |
| Aggregation basis | Unique Decision ID |

### Status by area

| Item | Value |
| --- | --- |
| Environment | 8 |
| Network | 11 |
| RDS | 9 |
| Database | 12 |
| Compute / Service | 40 |
| Security / IAM | 8 |
| Observability | 4 |
| Cutover | 4 |
| Safety | 4 |

## At a Glance

### Key environment·network·RDS

| Item | Value |
| --- | --- |
| OD-ENV-001 | Whether to build an AWS dev environment · not built · 🟢 확정 |
| OD-ENV-003 | AWS first-build environment · aws-paper · 🟢 확정 |
| OD-NET-001 | NAT Gateway use (aws-paper) · not used · 🟢 확정 |
| OD-NET-002 | NAT Gateway use (aws-live) · not used (default plan) · 🟢 확정 |
| OD-NET-005 | VPC Endpoint active items · default set (S3 GW + ECR api+dkr + Secrets + Logs) · SSM optional · 🟢 확정 |
| OD-NET-009 | Operator access method · use SSM Session Manager only · 🟢 확정 |
| OD-RDS-001 | aws-paper RDS instance · db.t4g.small single-AZ · 🟢 확정 |
| OD-RDS-003 | aws-live RDS stability-first plan · multi-AZ · 🟢 확정 |
| OD-CUT-001 | cutover method · pg_dump+pg_restore first priority · 🟢 확정 |

### Key automation safety

| Item | Value |
| --- | --- |
| OD-SAFE-001 | aws-paper automatic BUY/SELL E2E · initially blocked → allowed after validation · 🟢 확정 |
| OD-SAFE-002 | aws-live automatic BUY · candidate+manual approval first, phased after validation · 🟢 확정 |
| OD-SAFE-003 | aws-live automatic SELL · same policy as OD-SAFE-002 · 🟢 확정 |
| OD-SAFE-004 | automatic retry policy · automatic retry only for idempotent steps · 🟢 확정 |

### Key service placement

| Item | Value |
| --- | --- |
| OD-MS-001 | port-marketconnector compute · EC2+EIP · 🟢 확정 |
| OD-MS-002 | port-view compute · ECS Fargate Service (first priority), Elastic Beanstalk (second-priority comparison retained in body) · 🟢 확정 |
| OD-MS-003 | port-interest-crawler compute · ECS Fargate Task NAT-free public (first priority), ECS on EC2 (promoted if Selenium stability is insufficient) · 🟢 확정 |
| OD-MS-004 | port-interest-preprocessor compute · ECS Fargate Task (first priority), Lambda assists only for short steps · 🟢 확정 |
| OD-MS-005 | port_strategy_common deployment · no separate compute. git submodule packaging (first priority), wheel+CodeArtifact (second priority at maturity) · 🟢 확정 |
| OD-MS-006 | port_strategy_decision compute · ECS Fargate Task + EventBridge Scheduler (first priority, Step Functions integration in 04), Lambda not recommended · 🟢 확정 |
| OD-MS-007 | port_strategy_execution compute · ECS Fargate Task + Step Functions + EventBridge Scheduler (first priority), Lambda not recommended · 🟢 확정 |
| OD-MS-008 | port_strategy_research compute · AWS Batch (first priority, Step Functions assist), ECS Fargate Task (second priority), Lambda not recommended · 🟢 확정 |
| OD-MS-009 | Daily Batch orchestration · Step Functions + EventBridge Scheduler + ECS RunTask · 🟢 확정 |

## Decision Inventory

Each Decision ID is shown only once. The validation process and per-date change history of a decision are managed in the related spec `operation-notes.md`.

## Environment
### OD-ENV-001

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | Whether to build an AWS dev environment |
| Selected value | not built |
| Next review | None |
| Cost impact | Large savings (0 separate RDS/ECS/NAT/ALB) |
| Operational note | Integrated validation performed in aws-paper |
| Related spec | 02, 03, 04, 05, 08, 09 all remove the dev item |

### OD-ENV-002

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | local-dev operation location |
| Selected value | existing local PostgreSQL environment retained |
| Next review | None |
| Cost impact | 0 |
| Operational note | Need to manage data differences between local and aws-paper |
| Related spec | 02 cutover, 10 cutover-runbook |

### OD-ENV-003

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | AWS first-build environment |
| Selected value | aws-paper |
| Next review | None |
| Cost impact | Medium |
| Operational note | Automatic orders are validated within paper |
| Related spec | 03, 04, 08 applied first |

### OD-ENV-004

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | aws-live build timing |
| Selected value | follow-up build after paper validation |
| Next review | None |
| Cost impact | live cost on hold |
| Operational note | phased introduction of live auto-trading |
| Related spec | 10 cutover-and-validation-runbook |

### OD-ENV-005

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | VPC separation policy |
| Selected value | single VPC retained |
| Next review | None |
| Cost impact | savings |
| Operational note | isolate between environments via SG / Subnet tags |
| Related spec | 02 |

### OD-ENV-006

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | Paper environment DB source of truth |
| Selected value | AWS Paper RDS single source of truth |
| Next review | follow-up review |
| Cost impact | 0 (RDS cost is separate in OD-RDS-001) |
| Operational note | Merging / synchronizing orders / fills / positions between the local DB and AWS Paper RDS is the largest operator-mistake risk (R-DATA-007 alignment). local is used only for `LOCAL_DEV` fixture / experiment / backup reference. |
| Related spec | 02, 03, 04, 05, 08, 09, 10 |

### OD-ENV-007

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | Local PC → AWS Paper RDS connection method |
| Selected value | use SSM Port Forwarding only |
| Next review | follow-up review |
| Cost impact | 0 (SSM Port Forwarding itself has no cost) |
| Operational note | Allowing RDS Public violates R-SEC-001. Even if the DB host is `localhost`, the actual target may be AWS Paper RDS, so do not identify the environment by host alone. |
| Related spec | 02, 03, 04, 05 |

### OD-ENV-008

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | Synchronization policy between the Local DB and AWS Paper RDS |
| Selected value | not used |
| Next review | follow-up review |
| Cost impact | 0 |
| Operational note | Introducing synchronization risks data consistency / duplicate orders / duplicate fills / position state conflicts (R-DATA-007). |
| Related spec | 02, 03, 04, 10 |

## Network
### OD-NET-001

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | NAT Gateway use (aws-paper) |
| Selected value | not used |
| Next review | None |
| Cost impact | ~$43+ /month savings |
| Operational note | Internet outbound workloads must be explicitly separated |
| Related spec | 02, 03, 08 |

### OD-NET-002

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | NAT Gateway use (aws-live) |
| Selected value | not used (default plan) |
| Next review | None |
| Cost impact | ~$86+ /month savings |
| Operational note | On live AZ failure, outbound depends on the public workload |
| Related spec | 02, 03, 08 |

### OD-NET-003

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | marketconnector outbound IP |
| Selected value | EC2+EIP retained |
| Next review | None |
| Cost impact | EC2 unit price only |
| Operational note | broker IP registration is bound to the EIP. An EIP detach/attach Runbook is needed when replacing the EC2 |
| Related spec | 03 marketconnector-ec2 |

### OD-NET-004

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | crawler/preprocessor outbound method |
| Selected value | public subnet + assignPublicIp (first priority) |
| Next review | follow-up review |
| Cost impact | 0 |
| Operational note | External exposure risk on Task SG mistake. Egress validation needed |
| Related spec | 08 |

### OD-NET-005

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | VPC Endpoint active items |
| Selected value | default retained set = S3 Gateway + ECR api/dkr Interface + Secrets Manager Interface + CloudWatch Logs Interface |
| Selected value supplement | SSM Endpoint is not part of the required set but is selected depending on NAT-free status·public outbound structure·operator access path |
| Selected value supplement | currently aws-paper has the SSM Endpoint removed |
| Next review | None |
| Cost impact | proportional to the number of Interface Endpoints and AZs (decreases when AZs are reduced) |
| Operational note | If the default set is missing, the corresponding AWS API cannot be accessed without NAT |
| Operational note supplement | The presence of an SSM Endpoint is a separate concept from SSM Session Manager use |
| Related spec | 02, 06, 07 |

### OD-NET-006

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | STS / KMS Endpoint activation |
| Selected value | not used for now. Activate if needed |
| Next review | follow-up review |
| Cost impact | ~$8/month additional per AZ |
| Operational note | If not used, KMS calls may fail without NAT |
| Related spec | 02, 06 |

### OD-NET-007

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | ALB use (aws-paper) |
| Selected value | initially not used |
| Next review | follow-up review |
| Cost impact | ~$16.5/month savings |
| Operational note | port-view operator access is via SSM port forwarding or internal IP |
| Related spec | 05 port-view-ecs |

### OD-NET-008

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | ALB use (aws-live) |
| Selected value | initial cost-savings plan on hold |
| Next review | follow-up review |
| Cost impact | ~$16.5/month savings |
| Operational note | re-review per the live operator access policy |
| Related spec | 05 |

### OD-NET-009

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | Operator access method |
| Selected value | use SSM Session Manager only |
| Next review | None |
| Cost impact | 0 |
| Operational note | EC2 SSH 22 inbound 0.0.0.0/0 absolutely prohibited |
| Related spec | 02, 03 |

### OD-NET-010

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | Local-to-AWS Paper RDS SSM Port Forwarding standard waypoint |
| Selected value | `portfolio-paper-marketconnector-ec2` single · local port `15433` → tunnel → AWS Paper RDS |
| Next review | follow-up review |
| Cost impact | 0 (SSM Port Forwarding) |
| Operational note | DB connection is cut when the tunnel closes (R-AUTO-012) |
| Related spec | 02, 03, 04, 05, 06 |

### OD-NET-011

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | Local-to-AWS Paper RDS pgAdmin4 usage principle |
| Selected value | register only `localhost:15433` (SSM tunnel). Direct RDS endpoint registration prohibited |
| Next review | follow-up review |
| Cost impact | 0 |
| Operational note | When the tunnel is not open, the connection does not go outward (R-SEC-001) |
| Related spec | 02, 03, 04, 05, 10 |

## RDS
### OD-RDS-001

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | aws-paper RDS instance |
| Selected value | db.t4g.small single-AZ |
| Next review | None |
| Cost impact | ~$26 + storage |
| Operational note | With dev absent, validation load concentrates on paper |
| Related spec | 02, 04, 05 |

### OD-RDS-002

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | aws-live RDS cost-savings plan |
| Selected value | can start single-AZ |
| Next review | follow-up review |
| Cost impact | about 50% savings vs multi-AZ |
| Operational note | Downtime on AZ failure. Recovery only via PITR |
| Related spec | 02, 10 |

### OD-RDS-003

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | aws-live RDS stability-first plan |
| Selected value | multi-AZ |
| Next review | None |
| Cost impact | about 2x the cost-savings plan |
| Operational note | AZ failover automatic, minimal downtime |
| Related spec | 02, 10 |

### OD-RDS-004

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | PostgreSQL major version |
| Selected value | 16 or higher |
| Next review | None |
| Cost impact | 0 |
| Operational note | minor auto upgrade policy active for paper/live |
| Related spec | 02 |

### OD-RDS-005

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | encryption at rest |
| Selected value | aws-paper KMS default, aws-live CMK recommended |
| Next review | follow-up review |
| Cost impact | KMS key management cost small |
| Operational note | Using CMK adds an IAM permission matrix |
| Related spec | 02, 06 |

### OD-RDS-006

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | backup retention (aws-paper) |
| Selected value | 7 days |
| Next review | None |
| Cost impact | small |
| Operational note | External backup needed for data loss beyond 7 days |
| Related spec | 02 |

### OD-RDS-007

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | backup retention (aws-live) |
| Selected value | 14 days |
| Next review | None |
| Cost impact | Medium |
| Operational note | Can shorten to 7 days for cost savings |
| Related spec | 02, 10 |

### OD-RDS-008

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | PITR |
| Selected value | aws-paper on, aws-live on |
| Next review | None |
| Cost impact | small |
| Operational note | Recovery to a different point in time possible |
| Related spec | 02 |

### OD-RDS-009

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | manual snapshot policy |
| Selected value | just before cutover + quarterly |
| Next review | None |
| Cost impact | very small |
| Operational note | snapshot naming convention (`before-cutover-{date}`) |
| Related spec | 02, 10 |

## Database
### OD-DB-001

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | DB name |
| Selected value | portfolio (same in all environments) |
| Next review | None |
| Cost impact | 0 |
| Operational note | Maintain environment-variable compatibility |
| Related spec | 02, 06 |

### OD-DB-002

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | schema composition |
| Selected value | schema-per-domain 10 retained |
| Next review | None |
| Cost impact | 0 |
| Operational note | Maintain the existing search_path |
| Related spec | 02 |

### OD-DB-003

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | environment-variable key compatibility |
| Selected value | INTEREST_DB_* / PORT_* / PORTFOLIO_DB_NAME all retained |
| Next review | None |
| Cost impact | 0 |
| Operational note | Enforce the no-code-modification policy |
| Related spec | 02, 06 |

### OD-DB-004

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | DB role separation |
| Selected value | 7 roles (marketconnector_app, crawler_app, preprocessor_app, decision_app, execution_app, research_app, view_app) |
| Next review | None |
| Cost impact | 0 |
| Operational note | Permission-matrix operational burden |
| Related spec | 02, 06 |

### OD-DB-005

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | view_app permissions |
| Selected value | all schema READ + ops WRITE by default. execution write re-reviewed in 05 |
| Next review | follow-up review |
| Cost impact | 0 |
| Operational note | If execution write is needed in port-view, change in 05 |
| Related spec | 02, 05 |

### OD-DB-006

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | search_path policy |
| Selected value | keep per-MS README as-is |
| Next review | None |
| Cost impact | 0 |
| Operational note | Apply per-role ALTER ROLE SET search_path |
| Related spec | 02 |

### OD-DB-007

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | app role permissions on the legacy schema |
| Selected value | USAGE / SELECT not granted to any app role (reflects the 2026-06-09 first-application result) |
| Next review | None |
| Cost impact | 0 |
| Operational note | Security hardening vs the 02 design.md matrix (legacy R partially granted). Grant via a separate decision when an MS needing legacy data access is identified |
| Related spec | 02, 05, 06 |

### OD-DB-008

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | marketconnector_app's execution permissions |
| Selected value | reduced to R-only (reflects the 2026-06-09 first application) |
| Next review | None |
| Cost impact | 0 |
| Operational note | execution write is limited to execution_app alone. Permission-separation hardening vs the 02 design.md matrix (R/W) |
| Related spec | 02, 03, 04 |

### OD-DB-009

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | view_app's execution permissions |
| Selected value | keep R-only (write necessity re-reviewed in the 05 spec) |
| Next review | follow-up review |
| Cost impact | 0 |
| Operational note | Split OD-DB-005 into this decision. If execution write becomes essential in View, change in 05 |
| Related spec | 02, 05 |

### OD-DB-010

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | bulk owner reassignment of existing objects at first application (REASSIGN OWNED) |
| Selected value | not executed (existing table / sequence / index owner kept as `portfolio_admin`) |
| Next review | None |
| Cost impact | 0 |
| Operational note | schema owner reassignment to `portfolio_owner` complete. default privileges apply automatically only to new objects. Whether to bulk-reassign existing objects is separated and managed as a follow-up decision |
| Related spec | 02, 06 |

### OD-DB-011

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | `execution_app`'s `decision` schema UPDATE permission (Step 9 SELL execution link) |
| Selected value | grant only limited UPDATE on `decision.strategy_daily_position_decision` |
| Next review | None |
| Cost impact | 0 |
| Operational note | The UPDATE permission is limited to the single table `decision.strategy_daily_position_decision`. |
| Related spec | 02, 04 |

### OD-DB-012

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | `ops_recorder_app` new role (Step Functions execution-history OPS mirror only · 2026-07-03) |
| Selected value | Establish a dedicated least-privilege role · `ops` schema USAGE + `ops.strategy_daily_batch_run` · `ops.strategy_daily_batch_step_log` SELECT · INSERT · UPDATE + related sequence USAGE · SELECT |
| Selected value supplement | DELETE not granted · `view_app` not reused · `execution_app` permissions not expanded · add `AWS_STEPFUNCTIONS` value to `chk_strategy_daily_batch_run_type` (existing `MANUAL` · `SCHEDULED` · `RETRY` |
| Selected value supplement | `MANUAL_PARTIAL` retained) |
| Next review | None |

## Compute / Service
### OD-MS-001

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | port-marketconnector compute |
| Selected value | EC2+EIP |
| Next review | None |
| Cost impact | EC2 unit price + EIP attach free |
| Operational note | External exposure on EC2 SG mistake. SSM + SG control essential |
| Related spec | 03 |

### OD-MS-002

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | port-view compute |
| Selected value | ECS Fargate Service (first priority), Elastic Beanstalk (second-priority comparison retained in body) |
| Next review | None |
| Cost impact | Fargate per-task |
| Operational note | Whether to introduce ALB is OD-NET-007 / 008 |
| P2 scope | View operational security·display enhancement (ALB · HTTPS · Route53 · authentication · Auto Scaling · Blue/Green · UI enhancement · external exposure) not performed · out of scope |
| P2 current status | ECS Fargate first empirical demonstration state maintained · not externally exposed |
| P2 re-review | When external exposure or multi-user operation is needed |
| Related spec | 05 |

### OD-MS-003

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | port-interest-crawler compute |
| Selected value | ECS Fargate Task NAT-free public (first priority), ECS on EC2 (promoted if Selenium stability is insufficient) |
| Perspective | Target-compute selection perspective (re-review ECS integration if KRX headless conversion becomes possible) |
| Perspective supplement | Current actual operation is Hybrid (OD-MS-011) |
| Next review | None |
| Cost impact | Fargate per-task. No NAT |
| Operational note | Selenium / KRX login stateful. SG control essential |
| Related spec | 08 |

### OD-MS-004

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | port-interest-preprocessor compute |
| Selected value | ECS Fargate Task (first priority), Lambda assists only for short steps |
| Next review | None |
| Cost impact | Fargate per-task |
| Operational note | In NAT-free, holiday API outbound needs a public subnet |
| Related spec | 08 |

### OD-MS-005

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | port_strategy_common deployment |
| Selected value | no separate compute. git submodule packaging (first priority), wheel+CodeArtifact (second priority at maturity) |
| Next review | None |
| Cost impact | 0 |
| Operational note | Version synchronization burden at each MS image build time |
| Related spec | 07 |

### OD-MS-006

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | port_strategy_decision compute |
| Selected value | ECS Fargate Task + EventBridge Scheduler (first priority, Step Functions integration in 04), Lambda not recommended |
| Next review | None |
| Cost impact | Fargate per-task |
| Operational note | Multi-schema read·write + common library. Lambda timeout / connection leak risk |
| Related spec | 04 |

### OD-MS-007

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | port_strategy_execution compute |
| Selected value | ECS Fargate Task + Step Functions + EventBridge Scheduler (first priority), Lambda not recommended |
| Next review | None |
| Cost impact | Fargate per-task |
| Operational note | Enforce the live BUY/SELL no-automatic-retry policy at the state machine level |
| Related spec | 04, 10 |

### OD-MS-008

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | port_strategy_research compute |
| Selected value | AWS Batch (first priority, Step Functions assist), ECS Fargate Task (second priority), Lambda not recommended |
| Next review | None |
| Cost impact | usage-based |
| Operational note | Check long-running backtest / RDS connection leak. report S3 retention |
| Related spec | 09 |

### OD-MS-009

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | Daily Batch orchestration |
| Selected value | Step Functions + EventBridge Scheduler + ECS RunTask |
| Next review | None |
| Cost impact | Step Functions transitions ≪ ECS Task cost |
| Operational note | The existing port-view subprocess is not used as-is on AWS |
| Related spec | 04, 05 |

### OD-MS-010

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | infra alarm channel |
| Selected value | Keep domain notifications on SlackNotificationService; infra alarms via SNS → Lambda → Slack webhook fan-out |
| Next review | follow-up review |
| Cost impact | within free tier |
| Operational note | webhook URL in Secrets Manager or SSM SecureString. Final decision in 06 |
| Related spec | 05, 10 |

### OD-MS-011

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | port-interest-crawler runtime separation (Hybrid execution model) |
| Selected value | Hybrid (KRX GUI=Windows EC2 interactive worker · non-GUI=ECS Fargate Task). preprocessor=ECS Fargate Task |
| Perspective | Current actual Hybrid operational model perspective |
| Perspective supplement | The target structure (ECS integration on KRX headless conversion) is OD-MS-003 |
| Perspective supplement | Service-placement conclusion retained · detailed operational method can be validated later |
| Next review | follow-up review |
| Cost impact | EC2 idle + Fargate per-task |
| Operational note | EC2 worker stop procedure · automation not reached |
| Related spec | 08, 04, 05, 09, 10 |

### OD-MS-012

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | KRX GUI-dependent crawler first operational mode |
| Selected value | wrapper-based manual execution (`run_krx_worker_daily.ps1`) |
| Next review | follow-up review |
| Cost impact | 0 (wrapper itself) |
| Operational note | wrapper success does not guarantee DB loading success (R-AUTO-007) |
| Related spec | 08 |

### OD-MS-013

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | port_strategy_decision Task Definition separation policy |
| Selected value | 2 separate Task Definitions for buy-signal · position-signal |
| Next review | follow-up review |
| Cost impact | Fargate per-task |
| Operational note | Enforce the call order at the orchestration level |
| Related spec | 04, 05 |

### OD-MS-014

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | port_strategy_common first deployment method |
| Selected value | vendoring in the first ECS smoke image |
| Next review | follow-up review |
| Cost impact | 0 (vendoring) |
| Operational note | Version mismatch risk when the source is updated concurrently |
| Related spec | 04, 05, 07, 09 |

### OD-MS-015

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | KRX GUI-dependent crawler first automation method |
| Selected value | SSM RunCommand → schtasks → Scheduled Task → Autologon session → wrapper |
| Next review | follow-up review |
| Cost impact | 0 (SSM/Task) |
| Operational note | KRX login fails in the absence of an Autologon session (R-AUTO-008) |
| Related spec | 08 |

### OD-MS-016

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | Strategy Execution / MarketConnector order-execution responsibility separation |
| Selected value | responsibility separation. Execution=READY→REQUESTED · Connector=REQUESTED→SUBMITTED/FAILED |
| Next review | follow-up review |
| Cost impact | 0 |
| Operational note | View Daily Batch Step 12 calls the Connector executor |
| Related spec | 03, 04, 05 |

### OD-MS-017

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | port_strategy_execution Task Definition operational method |
| Selected value | single Task Definition + command override |
| Next review | follow-up review |
| Cost impact | Fewer Task Defs, Fargate cost same |
| Operational note | command override mapping error risk (R-AUTO-014) |
| Related spec | 04, 05, 10 |

### OD-MS-018

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | port_strategy_research Batch image dependency boundary |
| Selected value | Migrate to Research-internal adapters. Batch image=research+common, decision not included |
| Next review | follow-up review |
| Cost impact | low (image size impact) |
| Operational note | Update omission when the common contract changes, due to adapter duplication (R-DATA-008) |
| Related spec | 09, 07, 10 |

### OD-MS-019

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | port_strategy_research AWS Batch porting-target entrypoints + report artifact preservation |
| Selected value | porting targets = `BACKTEST_RESEARCH` + `BACKTEST_REPORT` 2 types only |
| Next review | follow-up review |
| Cost impact | Fargate usage-based |
| Operational note | Operator manual before the View Daily Batch → SubmitJob mapping (R-AUTO-015) |
| Related spec | 09, 04, 05, 06, 07, 10 |

### OD-MS-020

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | port-interest-crawler status expression / completion definition |
| Selected value | Interest Crawler = **hybrid first implementation partial complete** |
| Next review | follow-up review |
| Cost impact | 0 (expression correction) |
| Operational note | Periodic grep of residual expressions + raw latest-date SQL check (R-DATA-009/010) |
| Related spec | 08, 04, 05, 09 |

### OD-MS-021

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | Backend AWS E2E dry-run 17-step order + safety criteria |
| Selected value | Keep the local View Daily Batch 17-step order as-is. 8 safety criteria |
| Next review | follow-up review |
| Cost impact | 0 (paper limited) |
| Operational note | A safety-criteria violation is R-AUTO-001 |
| Related spec | 08, 04, 05, 09, 10 |

### OD-MS-022

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | KRX GUI crawler auto-login-based operational method |
| Selected value | Windows Autologon + Administrator session + Scheduled Task + SSM trigger |
| Next review | follow-up review |
| Cost impact | 0 (Autologon/Task) |
| Operational note | Autologon is a paper Windows worker-limited security exception (R-SEC-009 etc.) |
| Related spec | 08, 05, 10 |

### OD-MS-023

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | Daily AWS wrapper operational policy (operator local PowerShell tool) |
| Selected value | local Windows PowerShell wrapper split-file structure (main + config + functions + 17 steps) |
| Next review | follow-up review |
| Cost impact | 0 (local tool) |
| Operational note | Real order on misuse of `-AllowPaperOrderExecute` (R-AUTO-019) |
| Related spec | 03, 04, 05, 08, 09, 10 |

### OD-MS-024

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | additional-buy allowance policy + position_state merge method |
| Selected value | additional buy allowed + merge. weighted-average merge via `merge_open_position_state()` + idempotency |
| Next review | follow-up review |
| Cost impact | 0 (normal flow) |
| Operational note | execution_order_id duplication if idempotency is not applied (R-DATA-012) |
| Related spec | 04, 05, 10 |

### OD-MS-025

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | MarketConnector `connector_order_check.py` operational mode (Step 13 fill query) |
| Selected value | active-order single sequential query default + broad option isolation + internal branching |
| Next review | follow-up review |
| Cost impact | 0 |
| Operational note | Reduced broad calls reduce the summary fallback exposure surface (R-AUTO-018) |
| Related spec | 03, 04, 05, 10 |

### OD-MS-026

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | Step 2 INTEREST_CRAWLER operational success criterion (wrapper success-judgment reinforcement) |
| Selected value | Task trigger + Running→Ready wait + Last Result 0 + worker log + KRX raw DB validation all satisfied |
| Next review | follow-up review |
| Cost impact | 0 |
| Operational note | Reduced the limitation of handling SUCCESS on Scheduled Task trigger success alone (R-AUTO-020) |
| Related spec | 08, 04, 05, 10 |

### OD-MS-027

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | MarketConnector env bootstrap regeneration operational policy (handling `/tmp/inject-env.sh` volatility) |
| Selected value | wrapper common-function regeneration. Called just before entering Step 1/12/13/17. |
| Next review | 03, 04, 06, 10 |
| Cost impact | 0 — wrapper common-function regeneration itself has no cost. The Secrets Manager `GetSecretValue` call is the same as the existing flow. |
| Operational note | Risk of Step 1 / 12 / 13 / 17 failure due to `/tmp` volatility after MarketConnector EC2 stop / start (R-AUTO-021 new / Mitigated). |

### OD-MS-028

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | Step 12 retry-normalizer built-in policy (handling the next-day automatic re-submission path after market-close REJECTED · `40580000`) |
| Selected value | built into the Step 12 head · when the 6 recovery conditions are satisfied. |
| Next review | 03, 04, 10 |
| Cost impact | 0 — the retry-normalizer itself has no cost-model change. |
| Operational note | If recovery conditions expand broadly, there is an unintended broker duplicate-order risk (R-AUTO-001 / R-BROKER-004 alignment). Mitigated by maintaining the `broker_order_no IS NULL` + no `connector_fill` condition. |

### OD-MS-029

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | Daily AWS Paper Step Functions approval workflow false / true path operational procedure |
| Selected value | true path approval execution after false path pre-validation. |
| Next review | 04, 10 |
| Cost impact | Step Functions transitions cost + same ECS RunTask · AWS Batch · SSM RunCommand usage — cost-model change negligible. |
| Operational note | On entering the true path, an unintended `allowPaperOrderExecute=true` input can lead to actual broker order submission (R-AUTO-019 / R-AUTO-023 alignment). |

### OD-MS-030

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | AWS common Slack notifier Lambda introduction policy (operational-event notification assist layer) |
| Selected value | AWS common notifier based on Lambda `portfolio-event-notifier`. |
| Next review | 04, 05, 10 |
| Cost impact | very low — within the Lambda 1M requests + 400,000 GB-sec free tier. Slack webhook calls themselves have no cost. |
| Operational note | Risk of long-term storage of the webhook URL environment variable (R-AUTO-024 new / Accepted — move to Secrets Manager or SSM SecureString after operational stabilization) + Slack send failure risk (R-AUTO-023 new). |

### OD-MS-031

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | Step Functions / EventBridge first Slack integration scope limited to 3 kinds policy |
| Selected value | limited to 3 kinds (APPROVAL_REQUIRED + DAILY_EXECUTION_SUCCESS/FAILED). |
| Next review | 04, 05, 10 |
| Cost impact | Step Functions transitions cost change negligible / Lambda call cost within free tier / at most 3 Slack sends per operational cycle. |
| Operational note | The core of this decision is not message omission but that the failure notification is not cut off on the Step Functions Catch path (R-AUTO-023 alignment). |

### OD-MS-032

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | EventBridge Scheduler + Dispatcher Lambda-based Daily automation structure |
| Selected value | 2 Schedulers + Dispatcher Lambda + phased activation. |
| Next review | 04, 05, 10 |
| Cost impact | within the EventBridge Scheduler 14M / Lambda 1M requests free tier / Step Functions Standard transitions cost is OD-MS-009 alignment. |
| Operational note | Risk of Scheduler / Dispatcher Lambda / Step Functions connection failure (R-AUTO-025 new) — mitigated by IAM simulate + Lambda dryRun + Scheduler get-schedule status confirmation. |

### OD-MS-033

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | Step 12 retry-normalizer retry expansion + KIS rate-limit backoff + 09:01 ENABLE on hold |
| Selected value | `40580000` + `EGW00201` retry · sleep/backoff · 09:01 auto ENABLE on hold. |
| Next review | 03, 04, 05, 10 |
| Cost impact | Step 12 entrypoint execution-time impact negligible / no operational-cycle cumulative cost-model change. |
| Operational note | On violation of the 09:01 schedule auto-ENABLE-on-hold policy, immediately call `disable-schedule` + operator direct SQL check (R-AUTO-025 / R-AUTO-026 alignment). |

### OD-MS-034

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | EC2 lifecycle automatic execution configuration (07:50 start / Crawler stop on Step 1~11 success / 15:50 stop) |
| Selected value | EventBridge Scheduler + Lambda-based EC2 start/stop. |
| Next review | 03, 04, 05, 08, 10 |
| Cost impact | within the EventBridge Scheduler 14M / Lambda 1M requests free tier + EC2 lifecycle cost-savings effect (EC2 stop outside business hours). |
| Operational note | Risk of Scheduler / EC2 lifecycle Lambda / Step Functions connection failure (R-AUTO-028 new). |

### OD-MS-035

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | Intraday position check 3-stage structure (Snapshot Refresh + Intraday Evaluate + Stop Sell Submit & Refresh) |
| Selected value | 3-stage separation + new files + Submit initially manual·after approval. |
| Next review | 03, 04, 05, 10 |
| Cost impact | EventBridge Scheduler + Lambda dispatcher each within free tier / Step Functions transitions Standard very low / cumulative cost increase very low within the aws-paper-limited scope. |
| Operational note | Stale snapshot (R-DATA-014) / Duplicate order (R-AUTO-029) / sellable_qty (R-DATA-015) / current_price (R-DATA-016) — all 4 new risks Mitigated. |

### OD-MS-036

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | Intraday Stop Sell Submit Workflow (intraday stop-loss order submission-dedicated State Machine + manual approval operation) |
| Selected value | separate state machine `portfolio-paper-intraday-stop-sell-approval` + manual approval. |
| Next review | 03, 04, 05, 10 |
| Cost impact | Step Functions Standard transitions cost very low / SSM RunCommand + ECS RunTask.sync usage-based / cumulative cost increase very low. |
| Operational note | approval gate misconfiguration risk (R-AUTO-031 new) / Daily SELL vs Intraday Stop SELL flow confusion risk (R-AUTO-032 new) / real-order test risk in the no-holdings state (R-BROKER-005 new) — all Mitigated. |

### OD-MS-037

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | View Local AWS Paper read-only first scope finalization + policy of doing batch second validation before entering ECS / Fargate |
| Selected value | read-only first scope finalized + batch second validation done before ECS entry. |
| Next review | 04, 05, 10 |
| Cost impact | SSM Port Forwarding free / within RDS Free Tier (`db.t4g.micro`) / 0 cumulative cost increase. |
| Operational note | View Local execution-type call risk (R-AUTO-033 new) / DB connection cut when the SSM tunnel closes (R-AUTO-012 alignment). |

### OD-MS-038

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | Daily Brief Slack mini workflow operational method (separated from the Daily order-execution path) |
| Selected value | separate mini Step Functions + Builder Lambda + Notifier Lambda + 2 Schedulers. |
| Next review | 04, 05, 10 |
| Cost impact | mini Step Functions transitions + Lambda calls + 2 Scheduler invocations all very small (2 times per weekday = about 44 invocations/month). |
| Operational note | Daily Brief notification omission / duplication risk (R-AUTO-035 new) / Slack webhook URL Lambda environment-variable exposure risk (R-AUTO-024 alignment) / DB password Lambda environment-variable injection risk is mitigated via the Secrets Manager `valueFrom` method. |

### OD-MS-039

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | Step Functions execution-history OPS mirror operational method (Recorder Lambda + dedicated least-privilege role · 2026-07-03) |
| Selected value | Grant the Recorder Lambda `portfolio-daily-batch-ops-recorder` (Python 3.12 · ap-northeast-2 · VPC Lambda · uses Secrets Manager `ops_recorder_app` secret · action `RECORD_START` |
| Selected value supplement | `RECORD_STEP` · `RECORD_SUCCESS` |
| Selected value supplement | `RECORD_FAILURE`) to the Step Functions execution role with `lambda:InvokeFunction` limited, then the State Machine mirrors run-level + representative workflow steps to `ops.strategy_daily_batch_run` + `ops.strategy_daily_batch_step_log` |
| Selected value supplement | Target State Machines are `portfolio-paper-daily-step1-17-approval` + `portfolio-paper-daily-step12-17-approval` (2 kinds) |
| Selected value supplement | The first scope is centered on run-level + representative workflow steps, not full detailed-step mirroring · View retains the reader · controller role |
| Next review | 04, 05, 10 |

### OD-MS-040

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | ECS batch container Asia/Seoul timezone policy (container including Daily market-date calculation · 2026-07-08) |
| Selected value | An ECS batch container that includes Daily market-date calculation specifies `TZ=Asia · Seoul` instead of relying on the UTC default · the short-term action adds `TZ=Asia |
| Selected value supplement | Seoul` to the TaskDefinition env (`portfolio-paper-interest-crawler:8` |
| Selected value supplement | `portfolio-paper-interest-preprocessor:2`) + Step2A_RunInterestCrawlerNongui of State Machine `portfolio-paper-daily-step1-17-approval` |
| Selected value supplement | Step3_RunPreprocessor task revision update · the root improvement is replacing direct `datetime.now()` use in code with a timezone-aware helper to remove the container default TZ dependency (follow-up) |
| Next review | 04, 08 |

## Security / IAM
### OD-SEC-001

| Item | Value |
| --- | --- |
| Status | 🔴 미정 |
| Decision | Secrets storage location |
| Selected value | Final decision in 06. This spec is a placeholder |
| Next review | Operator decision needed |
| Cost impact | proportional to the number of secrets |
| Operational note | rotation policy undecided |
| Related spec | 06 |

### OD-SEC-002

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | RDS master password storage |
| Selected value | Secrets Manager (recommended) |
| Next review | follow-up review |
| Cost impact | $0.40/secret/month |
| Operational note | Can change per the 06 decision |
| Related spec | 06 |

### OD-SEC-003

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | KIS access_token storage |
| Selected value | EC2 local+S3 backup first priority |
| Next review | follow-up review |
| Cost impact | very small |
| Operational note | Token handover Runbook essential when replacing the EC2 |
| Related spec | 03 |

### OD-SEC-004

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | EC2 SSH 22 inbound |
| Selected value | not opened. Use SSM Session Manager only |
| Next review | None |
| Cost impact | 0 |
| Operational note | Same as OD-NET-009 |
| Related spec | 02, 03 |

### OD-SEC-005

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | EC2 / 8 MS Access Key non-use principle |
| Selected value | Prohibit storing access keys in EC2. Use only IMDSv2 + Instance Role or ECS Task Role |
| Next review | follow-up review |
| Cost impact | 0 |
| Operational note | On violation, immediately revoke in the IAM Console + return to IMDSv2 + Role only mode. |
| Related spec | 03, 04, 05, 06, 08, 09 |

### OD-SEC-006

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | EC2 / ECS IAM Role-based secret / parameter read principle |
| Selected value | Least privilege. Resource wildcard prohibited. Action wildcard prohibited |
| Next review | follow-up review |
| Cost impact | 0 |
| Operational note | Policy detach + previous-policy restore / check 0 wildcards via policy static analysis |
| Related spec | 03, 04, 05, 06, 08, 09 |

### OD-SEC-007

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | EC2 operator access = SSM Session Manager-centric |
| Selected value | `AmazonSSMManagedInstanceCore` attach + SSM Session Manager entry-centric |
| Next review | follow-up review |
| Cost impact | 0 |
| Operational note | SSM Session Manager access needs an AWS API outbound path |
| Operational note supplement | That path can be either Public outbound or an SSM Interface Endpoint |
| Operational note supplement | currently aws-paper has the SSM Endpoint removed |
| Related spec | 03, 04, 05, 08, 09 |

### OD-SEC-008

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | KRX login credential storage |
| Selected value | Secrets Manager (`/portfolio/{env}/krx/crawler-login`, JSON `username` / `password`). EC2 worker IAM Role inline poli... |
| Next review | follow-up review |
| Cost impact | $0.40/secret/month + KMS calls negligible |
| Operational note | worker startup fails if the secret read permission is missing. Record a 4-line summary in the operator note on permission changes |
| Related spec | 06, 08 |

## Observability
### OD-OBS-001

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | CloudWatch Logs use |
| Selected value | Use CloudWatch Logs |
| Next review | None |
| Cost impact | proportional to retention |
| Operational note | Start retention short to prevent cost runaway |
| Related spec | 04, 05, 08 |

### OD-OBS-002

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | CloudWatch Logs retention (aws-paper) |
| Selected value | start at 7 days |
| Next review | follow-up review |
| Cost impact | small |
| Operational note | May be insufficient for failure-analysis retrospectives |
| Related spec | 02, 05 |

### OD-OBS-003

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | CloudWatch Logs retention (aws-live) |
| Selected value | start at 14 days, can raise to 30 days after operational stabilization |
| Next review | follow-up review |
| Cost impact | Medium |
| Operational note | 90 days can be considered for live auditing |
| Related spec | 05, 10 |

### OD-OBS-004

| Item | Value |
| --- | --- |
| Status | 🔴 미정 |
| Decision | Slack webhook storage |
| Selected value | Final decision in 06 |
| Next review | Operator decision needed |
| Cost impact | small |
| Operational note | External-send risk if the webhook URL is exposed |
| Related spec | 06 |

## Cutover
### OD-CUT-001

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | cutover method |
| Selected value | pg_dump+pg_restore first priority |
| Next review | None |
| Cost impact | 0 |
| Operational note | Short downtime allowed |
| Related spec | 02, 10 |

### OD-CUT-002

| Item | Value |
| --- | --- |
| Status | 🔵 보류 |
| Decision | AWS DMS introduction |
| Selected value | on hold |
| Next review | re-review in a separate phase |
| Cost impact | DMS instance cost |
| Operational note | Re-review when zero-downtime becomes essential |
| Related spec | 10 |

### OD-CUT-003

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | aws-live cutover timing |
| Selected value | after paper validation |
| Next review | None |
| Cost impact | live cost delayed |
| Operational note | operator decides the N business days of paper validation |
| Related spec | 10 |

### OD-CUT-004

| Item | Value |
| --- | --- |
| Status | 🟡 잠정 |
| Decision | local-dev retention period |
| Selected value | parallel operation (as rollback insurance) |
| Next review | follow-up review |
| Cost impact | local host cost only |
| Operational note | data-divergence management burden |
| Related spec | 10 |

## Safety
### OD-SAFE-001

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | aws-paper automatic BUY/SELL E2E |
| Selected value | initially blocked → allowed after validation |
| Next review | None |
| Cost impact | 0 |
| Operational note | Even in paper trading, fill/position sync errors must be reproduced |
| Paper Daily stabilization | 2026-07-22 normal automatic cycle end-to-end success · first stabilization complete |
| No overstatement | Not aws-live readiness·long-term zero-failure·full AWS Migration completion |
| broker retry policy | Keep automatic retry prohibited (OD-SAFE-004) · preserve existing failed execution history |
| Related spec | 04, 10 |

### OD-SAFE-002

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | aws-live automatic BUY |
| Selected value | candidate+manual approval first, phased after validation |
| Next review | None |
| Cost impact | 0 |
| Operational note | A single wrong automatic retry can lead to a large loss |
| Related spec | 04, 05, 10 |

### OD-SAFE-003

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | aws-live automatic SELL |
| Selected value | same policy as OD-SAFE-002 |
| Next review | None |
| Cost impact | 0 |
| Operational note | same as above |
| Related spec | 04, 05, 10 |

### OD-SAFE-004

| Item | Value |
| --- | --- |
| Status | 🟢 확정 |
| Decision | automatic retry policy |
| Selected value | automatic retry only for idempotent steps |
| Next review | None |
| Cost impact | 0 |
| Operational note | BUY/SELL/fill sync/position change/intraday stop SELL creation are retry-prohibited |
| Related spec | 04, 08, 10 |

## Review Queue

### 미정

| Item | Value |
| --- | --- |
| OD-SEC-001 | Secrets storage location · current selected value: final decision in 06. This spec is a placeholder · next: operator decision needed |
| OD-OBS-004 | Slack webhook storage · current selected value: final decision in 06 · next: operator decision needed |

### 보류

| Item | Value |
| --- | --- |
| OD-CUT-002 | AWS DMS introduction · on hold · re-review in a separate phase |

### 잠정 decisions

| Item | Value |
| --- | --- |
| Target | 42 |
| Management method | Updated to one of 확정·change·보류 after related-spec validation |
| This document | Keeps only the final selected value and status |
| Execution evidence | Recorded in the relevant spec `operation-notes.md` |
| Per-date history | Managed in `WORKLOG.md` and `CHANGELOG.md` |

## Evidence Management

| Item | Value |
| --- | --- |
| This document | Decision ID · status · decision · selected value · next review · related spec |
| Detailed execution evidence | each spec `operation-notes.md` |
| Per-date work | `WORKLOG.md` |
| Document changes | `CHANGELOG.md` |
| Related risks | `risk-register.md` |
| Service comparison | `ms-aws-service-decision-matrix.md` |
| Decision Details | Does not use a separate long-form section |
| Decision Change Log Details | Does not use a separate long-form section |
| executionName · ARN · SHA256 | Not recorded in this document |

## Decision Update Rules

| Item | Value |
| --- | --- |
| New Decision | Assign the ID after the last number of that prefix |
| ID reuse | Prohibited |
| 확정 change | Update the selected value and status after operator approval |
| 잠정 promotion | Can be finalized after related-spec validation completes |
| 미정 | Do not finalize an arbitrary value before operator selection |
| 보류 | Change status only when the re-review condition is met |
| Detailed history | Recorded in operation-notes and WORKLOG |
| No duplication | The same Decision ID is shown only once in the Inventory |
| Table format | standalone tables use 2 columns `Item / Value` |
| Long cells | Split into multiple rows |
| Sensitive information | Use only `[REDACTED]`-family placeholders |

## Security Notes

| Item | Value |
| --- | --- |
| Actual AWS execution | None |
| Application code modification | None |
| AWS resource change | None |
| broker · KIS · DB execution | None |
| Sensitive information originals | Recording prohibited |
| Allowed notation | `[REDACTED]`-family placeholder |
| Document role | AWS Migration common Operator Decisions |
