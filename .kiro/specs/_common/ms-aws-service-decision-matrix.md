# MS × AWS Service Decision Matrix — AWS Migration

## Purpose

This document is the single source of truth for quickly checking the AWS service selection conclusions for the 8 PORT-STRATEGY-AI MS.

Each MS is organized in a `Item / Value` two-column table with the items below.

| Item | Value |
| --- | --- |
| 1st choice | The finally adopted service |
| 2nd choice | A conditional alternative or follow-up candidate |
| Adoption reason | Workload characteristics and operating criteria |
| Cost | The main cost drivers |
| Operational risk | The key risks the operator must manage |
| Related spec | The spec responsible for design·implementation·operation |

The service selection conclusions are not changed arbitrarily. Date-by-date execution history, executionName, Task Definition revision, ARN, smoke results, DB after-check, and Risk·Decision reinforcement details are not managed in this document.

Detailed operational evidence is managed in each spec's `operation-notes.md`, decision history in `operator-decisions.md`, and risks in `risk-register.md`.

Sensitive information is never recorded verbatim; only `[REDACTED]`-style placeholders are used.

## Operating Principles

| Item | Value |
| --- | --- |
| Environment | `local-dev` · `aws-paper` · `aws-live` |
| Region | Seoul `ap-northeast-2` |
| Operating staff | Single-operator basis |
| Network | NAT Gateway not used by default |
| Database | RDS PostgreSQL · single portfolio DB · schema-per-domain |
| Container | ECS Fargate centered |
| orchestration | Step Functions + EventBridge Scheduler |
| Automatic retry | Only idempotent steps allowed |
| Order-related steps | No automatic retry for BUY · SELL · Fill Sync · Position change |
| Operating access | SSM Session Manager preferred |
| Exact cost | Verified separately in the AWS Pricing Calculator |

## Final Recommendation

### `port-marketconnector`

| Item | Value |
| --- | --- |
| 1st choice | `EC2 + EIP` |
| 2nd choice | ECS Fargate · if the broker IP policy changes |
| Adoption reason | Fixed broker-registered IP · single access token · single session retention |
| Operating method | Attach an EIP to the EC2 and operate via SSM |
| Cost | EC2 instance-hour · EBS · Public IPv4 |
| Operational risk | EIP re-attach on EC2 replacement · broker-registered IP consistency · token/session management |
| Not recommended | Lambda · Elastic Beanstalk · App Runner · EKS |
| Related spec | 03 · 06 · 10 |
| Detail | [03 operation-notes](../03-marketconnector-ec2/operation-notes.md) |

### `port-view`

| Item | Value |
| --- | --- |
| 1st choice | `ECS Fargate Service` |
| 2nd choice | Elastic Beanstalk |
| Adoption reason | Spring Boot always-on service · container standard · Step Functions integration |
| Operating method | View has a display·control role · the actual Batch runs on Step Functions |
| Cost | Always-on Fargate vCPU·Memory · Public IPv4 · additional cost if ALB is introduced |
| Operational risk | Task Role least privilege · external-access control · authentication·HTTPS follow-up |
| Current status | Retain the ECS Fargate Service initial validation state |
| P2 not performed | ALB · HTTPS · Route53 · authentication · Auto Scaling · Blue/Green (currently out of scope) |
| Re-review | When external exposure or multi-user operation is needed |
| Not recommended | App Runner · EC2 standalone · EKS · Lambda |
| Related spec | 05 · 06 · 07 · 10 |
| Detail | [05 operation-notes](../05-port-view-ecs-and-runbook/operation-notes.md) |

### `port-interest-crawler`

| Item | Value |
| --- | --- |
| 1st choice | `ECS Fargate Task + Windows EC2 worker` |
| Current operation | Hybrid (non-GUI=ECS Fargate Task · KRX GUI=Windows EC2 interactive worker) |
| Long-term goal | Re-review ECS consolidation when KRX headless conversion becomes possible |
| Fargate role | Naver · yfinance · non-GUI collection |
| Windows role | Collection requiring KRX GUI login·download |
| 2nd choice | ECS on EC2 · if Selenium stability is insufficient |
| Auxiliary candidate | AWS Batch · bulk history backfill |
| Adoption reason | non-GUI and GUI workloads have different execution conditions |
| Cost | Fargate execution time · Windows EC2 execution time · Public IPv4 |
| Operational risk | Windows interactive session · Autologon security exception · exit-code judgment |
| Timezone | `TZ=Asia/Seoul` or timezone-aware code required |
| Not recommended | Lambda · App Runner · Elastic Beanstalk |
| Related spec | 08 · 04 · 10 |
| Detail | [08 operation-notes](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) |

### `port-interest-preprocessor`

| Item | Value |
| --- | --- |
| 1st choice | `ECS Fargate Task` |
| 2nd choice | Lambda · limited to short steps |
| Auxiliary candidate | AWS Batch · bulk backfill |
| Adoption reason | Long-running upsert · idempotent batch · terminates after execution |
| Cost | Fargate vCPU·Memory execution time |
| Operational risk | DB permissions · input data freshness · public subnet outbound |
| Timezone | KST-based batch specifies the timezone |
| Not recommended | Always-on EC2 · EKS · Elastic Beanstalk · App Runner |
| Related spec | 08 · 04 · 10 |
| Detail | [08 operation-notes](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) |

### `port_strategy_common`

| Item | Value |
| --- | --- |
| 1st choice | `No separate compute · git submodule packaging` |
| 2nd choice | wheel + CodeArtifact |
| Adoption reason | A pure Python shared library · not a standalone execution workload |
| Cost | The git submodule approach incurs no additional AWS cost |
| Operational risk | Per-MS image build timing sync · version drift |
| Not recommended | ECS · EC2 · Lambda · EKS |
| Related spec | 07 |
| Detail | [07 spec folder](../07-cicd-pipelines/) |

### `port_strategy_decision`

| Item | Value |
| --- | --- |
| 1st choice | `ECS Fargate Task + EventBridge Scheduler + Step Functions` |
| 2nd choice | AWS Batch · reprocessing multiple dates |
| Adoption reason | daily idempotent batch · terminates after execution · needs order control |
| Structure | Separate Buy Signal · Position Signal Task Definitions |
| Cost | Fargate execution time · Step Functions transition |
| Operational risk | schema permissions · input data freshness · KST reference date |
| Not recommended | Lambda as main execution · always-on EC2 · EKS · App Runner |
| Related spec | 04 · 06 · 10 |
| Detail | [04 operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) |

### `port_strategy_execution`

| Item | Value |
| --- | --- |
| 1st choice | `ECS Fargate Task + Step Functions + EventBridge Scheduler` |
| 2nd choice | ECS Fargate Service · if intraday always-on processing is needed |
| Adoption reason | Separate the order stage and enforce the no-automatic-retry policy in the workflow |
| Structure | Single Task Definition + command override |
| Safety principle | No automatic Retry on order submission · Fill Sync · Position change steps |
| Cost | Fargate execution time · Step Functions transition |
| Operational risk | Retry misconfiguration · duplicate orders · order chain mismatch |
| 2026-07-22 validation | Normal Scheduler automatic cycle success |
| validator validation | READY Plan → Order · Order Chain passed automatically |
| consistency validation | Order · Fill · Position consistency confirmed |
| Stabilization | Paper Daily 1st stabilization complete |
| Not recommended | Lambda · always-on EC2 · EKS · Elastic Beanstalk · App Runner |
| Related spec | 03 · 04 · 05 · 10 |
| Detail | [04 operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) |

### `port_strategy_research`

| Item | Value |
| --- | --- |
| 1st choice | `AWS Batch + S3 + Step Functions auxiliary` |
| 2nd choice | ECS Fargate Task |
| Adoption reason | Long-running backtest · variable vCPU/Memory · concurrent execution · report output |
| Artifact | Store reports in S3 |
| Cost | Batch compute usage · S3 storage |
| Operational risk | heavy job misexecution · concurrent execution count · RDS connection leak |
| Not recommended | Lambda · always-on EC2 · EKS · Elastic Beanstalk · App Runner |
| Related spec | 09 · 06 · 10 |
| Detail | [09 operation-notes](../09-strategy-research-batch/operation-notes.md) |

## Shared AWS Services

### Orchestration

| Item | Value |
| --- | --- |
| Step Functions | Daily Batch · approval branching · failure handling · order control |
| EventBridge Scheduler | Start scheduled executions |
| ECS RunTask | Run Batch containers |
| Lambda | Dispatcher · Notifier · Builder · lifecycle auxiliary |
| Operating principle | Do not use Lambda as the main compute for the 8 MS |

### Security

| Item | Value |
| --- | --- |
| Secrets Manager | High-sensitivity info such as DB password · broker secret · webhook |
| SSM Parameter Store | Low-sensitivity settings · per-environment configuration |
| IAM Role | Permission separation for ECS · EC2 · Lambda · Step Functions |
| IAM Policy | Restrict Action and Resource to the required scope |
| KMS | Encryption option for RDS · S3 · Secrets |
| Operating principle | Role-based access instead of Access Keys |

### Observability

| Item | Value |
| --- | --- |
| CloudWatch Logs | ECS · EC2 · Lambda · RDS logs |
| CloudWatch Metrics | Infrastructure and business metrics |
| CloudWatch Alarm | Detect failures·errors·heartbeat |
| SNS | Alarm fan-out |
| Slack | Final delivery of operational notifications |
| Operating principle | Keep only key alarms to prevent alert fatigue |

### Network and Data

| Item | Value |
| --- | --- |
| VPC | Shared network for paper/live workloads |
| Public Subnet | ECS Tasks and EC2 that need internet outbound |
| Private Subnet | RDS and internal workloads |
| VPC Endpoint (S3) | Gateway Endpoint |
| VPC Endpoint (ECR · Secrets · Logs) | Interface Endpoint |
| VPC Endpoint (SSM) | Public outbound or an optional Interface Endpoint |
| RDS PostgreSQL | The source of truth for portfolio data |
| S3 | research artifact · backup · long-term retention |
| ECR | container image storage |
| Operating principle | NAT Gateway not used by default |

## Rejected or Deferred Services

### Lambda

| Item | Value |
| --- | --- |
| Judgment | Not recommended as core MS compute · suitable as an auxiliary layer |
| Reason not recommended | 15-minute limit · unsuitable for Selenium · difficult stateful session |
| Order processing | Not used as the main execution environment because automatic-retry control is difficult |
| Suitable use | Dispatcher · Notifier · short validation · Alarm relay |
| Re-review condition | Short, stateless, and idempotent standalone tasks |

### EKS

| Item | Value |
| --- | --- |
| Judgment | Currently not recommended · a follow-up optional track |
| Reason not recommended | Control-plane and cluster operations are excessively complex for single-operator |
| Cost | control plane · worker · addon · observability cost |
| Current alternative | ECS Fargate + Step Functions |
| Advantage | Kubernetes · GitOps · Helm · Argo CD experience |
| Re-review condition | Need for multi-team · large-scale workload · Kubernetes standardization |
| Application candidate | port-view + strategy batch bundle |

### Elastic Beanstalk

| Item | Value |
| --- | --- |
| Judgment | port-view 2nd choice |
| Advantage | Simple Spring Boot deployment |
| Reason not recommended | The Step Functions·ECS RunTask integration structure is more awkward than ECS |
| Cost | Base resource cost such as EC2 · ALB |
| Re-review condition | When standalone PaaS operation of View becomes more important |

### App Runner

| Item | Value |
| --- | --- |
| Judgment | Currently not recommended |
| Advantage | Simple deployment for a single-container web service |
| Reason not recommended | Low flexibility for VPC-internal calls · IAM · Batch orchestration |
| Applicability | When the role is reduced to a simple public web service |

### ECS on EC2

| Item | Value |
| --- | --- |
| Judgment | Conditionally deferred |
| Advantage | Host-level control · Selenium·Chrome environment tuning |
| Reason not recommended | EC2 patch · scaling · capacity operational burden |
| Current alternative | non-GUI on Fargate · KRX GUI on a Windows EC2 worker |
| Re-review condition | When container runtime stability cannot be secured on Fargate |

### NAT Gateway

| Item | Value |
| --- | --- |
| Judgment | Not used by default in paper · live |
| Advantage | Simplifies general internet outbound for Private Subnets |
| Reason not recommended | Fixed cost and data processing cost |
| Current alternative | Public Subnet + Public IP · VPC Endpoint · EC2 + EIP |
| Re-review condition | When Private Subnet outbound demand increases significantly |

## Portfolio Appeal

### Service composition

| Item | Value |
| --- | --- |
| Compute | EC2 · ECS Fargate Service · ECS Fargate Task · AWS Batch |
| Orchestration | Step Functions · EventBridge Scheduler |
| Container | ECR · ECS · Fargate |
| Database | RDS PostgreSQL |
| Storage | S3 |
| Security | IAM · Secrets Manager · SSM Parameter Store · KMS |
| Network | VPC · Subnet · Route Table · SG · IGW · Endpoint · EIP |
| Observability | CloudWatch Logs · Metrics · Alarm · SNS |
| Operations | SSM Session Manager · RunCommand |
| CI/CD | GitHub Actions OIDC · ECR promotion |

### Application principles

| Item | Value |
| --- | --- |
| Priority | Operational stability > cost > portfolio variety |
| Service addition | Introduce only when there is an actual operational purpose |
| Fixed-cost services | ALB · NAT Gateway · EKS · EFS are deferred by default |
| live safety | Do not relax automatic-order safeguards for portfolio appeal |
| optional track | EKS · GitOps · CodePipeline reviewed after the 1st cutover |

## Decision Summary

| Item | Value |
| --- | --- |
| MarketConnector | EC2 + EIP |
| View | ECS Fargate Service |
| Crawler | ECS Fargate Task + Windows EC2 worker |
| Preprocessor | ECS Fargate Task |
| Strategy Common | No separate compute |
| Strategy Decision | ECS Fargate Task + EventBridge Scheduler + Step Functions |
| Strategy Execution | ECS Fargate Task + Step Functions + EventBridge Scheduler |
| Strategy Research | AWS Batch + S3 |
| Key criteria | NAT-free · single-operator · paper-first validation · no automatic order Retry |
| Conclusion change | None |

## Evidence Management

| Item | Value |
| --- | --- |
| This document | Keeps only the service selection conclusions and judgment criteria |
| Date-by-date execution history | `WORKLOG.md` |
| Detailed operational evidence | Each spec's `operation-notes.md` |
| Decision history | `operator-decisions.md` |
| Risks and mitigation | `risk-register.md` |
| Cost detail | `cost-simulation.md` |
| Evidence Details | Not accumulated in this document |
| executionName · ARN · SHA256 | Not recorded in this document |

## Update Rules

| Item | Value |
| --- | --- |
| Decision change | Revise starting from Final Recommendation after operator approval |
| 1st-choice string | Change prohibited without approval |
| New service candidate | Add first to Rejected or Deferred Services |
| Operational validation | Do not add date-by-date notes to the body; link to operation-notes |
| No duplication | No repeated explanation beyond Final Recommendation and Decision Summary |
| Table format | New standalone tables use the 2 columns `Item / Value` |
| Long cell | Split into multiple rows |
| Cost | Seoul Region approximation · verify exact amounts with the Pricing Calculator |
| Sensitive information | Only `[REDACTED]`-style placeholders are used |

## Security Notes

| Item | Value |
| --- | --- |
| Actual AWS execution | None |
| Application code changes | None |
| AWS resource creation·modification·deletion | None |
| broker · KIS · DB execution | None |
| aws-live automatic orders | Prohibited until separate approval |
| Verbatim sensitive information | Recording prohibited |
| Allowed notation | `[REDACTED]`-style placeholders |
| Document role | Single source of truth for AWS service selection |
