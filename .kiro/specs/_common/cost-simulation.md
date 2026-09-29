# AWS Monthly Cost Simulation — PORT-STRATEGY-AI AWS Migration

## Purpose

This document organizes the monthly cost judgment basis for the PORT-STRATEGY-AI AWS Migration.

Cost is classified into the three categories below.

| Item | Value |
| --- | --- |
| Actuals | The actual billed amount confirmed in AWS Billing |
| Operating estimate | The estimated monthly range reflecting the current aws-paper structure and automation level |
| Modeling | Comparison scenarios assuming a future live or high-availability configuration |

All estimated costs are approximations based on Seoul `ap-northeast-2`, and are re-verified with the AWS Pricing Calculator before actual construction·change.

Date-by-date execution notes, Scheduler activation cost, Lambda call counts, executionName, ARN, and detailed smoke results are not accumulated in this document.

Sensitive information and account-identifying details are never recorded; only `[REDACTED]`-style placeholders are used.

## Cost Dashboard

### Current aws-paper actuals basis

| Item | Value |
| --- | --- |
| Actuals target | June 2026 |
| Pre-tax cost | 135.40 USD |
| Tax | 13.55 USD |
| Tax included | about 148.95 USD |
| Key cost driver at the time | VPC Endpoint |
| NAT Gateway | Not used |
| Current operating estimate | about 150~190 USD/month |
| Conservative budget line | about 180 USD/month |
| Exact amount | Re-verify with the next billing cycle and the Pricing Calculator |

### June 2026 actuals by service

| Item | Value |
| --- | --- |
| VPC | 74.24 USD |
| RDS | 32.57 USD |
| EC2 | 25.15 USD |
| Secrets Manager | 2.88 USD |
| ECS · S3 · ECR · Data Transfer | Small combined amount |
| CloudWatch · Lambda · Step Functions · Scheduler | Around 0 USD |
| Pre-tax total | 135.40 USD |
| Total with tax | about 148.95 USD |

### Key cost drivers

| Item | Value |
| --- | --- |
| 1st | The number of Interface VPC Endpoints and AZ count |
| 2nd | The RDS instance and whether it is Multi-AZ |
| 3rd | Always-on EC2 and Fargate Service |
| 4th | NAT Gateway and data processing |
| 5th | ALB and CloudWatch Logs usage |

### Current savings effect

| Item | Value |
| --- | --- |
| SSM Endpoint | Removal complete |
| ECR API Endpoint | 2 AZ → 1 AZ |
| ECR DKR Endpoint | 2 AZ → 1 AZ |
| Logs Endpoint | 2 AZ → 1 AZ |
| Secrets Manager Endpoint | 2 AZ → 1 AZ |
| Estimated savings | about 56.16 USD/month |
| Additional Endpoint removal | Deferred as the benefit is small relative to operational risk |
| Verification method | Compare next month's bill with Endpoint Hours |

## Operating Cost Models

### Estimated monthly cost by operating mode

| Item | Value |
| --- | --- |
| Archive Mode | 5~20 USD |
| DB Retained Mode | 45~70 USD |
| Private AWS API Mode | 110~150 USD |
| Paper Daily Full ON | 150~190 USD |
| Demo / Interview Mode | 170~220 USD |
| Live Trading Ready Mode | 200~280 USD |

### Modeling range by environment

| Item | Value |
| --- | --- |
| local-dev | No separate AWS dev environment · additional fixed cost 0 |
| aws-paper current structure | about 150~190 USD/month |
| aws-paper NAT-included model | about 180~300 USD/month |
| aws-live cost-saving | about 200~350 USD/month |
| aws-live high-availability | about 430~650 USD/month |
| 3 environments always-on simultaneously | Inconsistent with current operating principles · kept only as a reference model |

### Modeling interpretation

| Item | Value |
| --- | --- |
| paper 212 USD | An upper-bound model including NAT Gateway and high Logs·Endpoint assumptions |
| live 492 USD | Includes Multi-AZ RDS · 2 NATs · ALB · Multi-AZ Endpoint |
| 3 environments 800 USD | A comparison scenario running dev · paper · live always-on simultaneously |
| Current priority values | The actuals of 148.95 USD and the paper 150~190 USD range |
| Usage principle | Do not cite actuals and modeling figures as if they had the same meaning |

## Current Architecture Cost Assumptions

### Compute

| Item | Value |
| --- | --- |
| MarketConnector | EC2 + EIP |
| port-view | ECS Fargate Service · desiredCount 1 when needed |
| Crawler | non-GUI ECS Fargate Task · KRX GUI Windows EC2 |
| Preprocessor | ECS Fargate Task |
| Strategy Decision | ECS Fargate Task |
| Strategy Execution | ECS Fargate Task + Step Functions |
| Research | AWS Batch + S3 |
| Strategy Common | No separate compute |

### Database

| Item | Value |
| --- | --- |
| aws-paper | RDS PostgreSQL `db.t4g.small` single-AZ |
| aws-live default candidate | `db.t4g.medium` |
| aws-live high-availability | Multi-AZ |
| paper storage | about 50 GB assumed |
| live storage | about 100~200 GB assumed |
| paper backup | 7 days |
| live backup | 14 days + PITR |

### Network

| Item | Value |
| --- | --- |
| NAT Gateway | Not used by default in paper · live |
| Public workload | Public Subnet + Public IP where needed |
| MarketConnector | EIP fixed |
| RDS | Private Subnet |
| S3 Endpoint | Gateway Endpoint · free |
| Interface Endpoint | Only required services · 1 AZ preferred |
| ALB | Not used initially · reviewed at formal external exposure |

### Observability and Security

| Item | Value |
| --- | --- |
| CloudWatch Logs | Start with short retention |
| CloudWatch Alarm | Set only for key failures and cost spikes |
| Secrets Manager | Store only high-sensitivity secrets |
| SSM Parameter Store | Use Standard tier for low-sensitivity settings |
| Step Functions | Few transitions, so current cost impact is minimal |
| EventBridge Scheduler | Current usage is within the free tier |
| Lambda | Dispatcher·Notifier focused · within the free tier |

## Unit Cost Reference

Verify exact unit prices again in the AWS Pricing Calculator. The values below are comparison approximations.

### Compute

| Item | Value |
| --- | --- |
| EC2 `t4g.small` | about 15 USD/month |
| EC2 `t3.medium` | about 38 USD/month |
| Fargate 0.5 vCPU + 1 GB always-on | about 22~23 USD/month |
| Fargate 0.5 vCPU + 1 GB 30 hours/month | about 1 USD or less |
| AWS Batch on Fargate | Based on Fargate usage time |

### Network

| Item | Value |
| --- | --- |
| Public IPv4 | about 0.005 USD/hour |
| NAT Gateway 1 | about 43 USD/month + data processing |
| NAT Gateway 2 | about 86 USD/month + data processing |
| ALB | about 16~17 USD/month + LCU |
| Interface Endpoint | about 8 USD/month per AZ |
| S3 Gateway Endpoint | Free |
| Internet outbound | Billed separately by usage |

### Database and Storage

| Item | Value |
| --- | --- |
| RDS `db.t4g.small` single-AZ | about 26 USD/month |
| RDS `db.t4g.medium` single-AZ | about 52 USD/month |
| RDS `db.t4g.medium` Multi-AZ | about 104 USD/month |
| RDS gp3 storage | about 0.131 USD/GB-month |
| S3 Standard | about 0.025 USD/GB-month |
| ECR storage | about 0.10 USD/GB-month |

### Security and Observability

| Item | Value |
| --- | --- |
| Secrets Manager | about 0.40 USD/secret-month |
| SSM Parameter Store Standard | Free |
| CloudWatch Logs ingestion | Proportional to usage |
| CloudWatch custom metric | about 0.30 USD/metric-month |
| CloudWatch standard alarm | about 0.10 USD/alarm-month |
| Step Functions Standard | about 0.025 USD/1,000 transitions |
| EventBridge Scheduler | Current call volume is within the free tier |
| Lambda | Current call volume is within the free tier |

## Environment Scenarios

### aws-paper — current recommendation

| Item | Value |
| --- | --- |
| Purpose | Paper-trading automation and operating-procedure validation |
| Monthly estimate | about 150~190 USD |
| Network | NAT not used · minimal Endpoints · Public outbound |
| RDS | `db.t4g.small` single-AZ |
| View | desiredCount 1 when needed |
| Windows EC2 | Run centered on KRX work hours |
| Advantage | Closest to the current structure and actuals |
| Caution | Continuously check the Endpoint count·AZ and Windows EC2 run time |

### aws-paper — upper-bound model

| Item | Value |
| --- | --- |
| Monthly estimate | about 180~300 USD |
| Additional assumptions | NAT Gateway · high Logs · always-on View |
| Use | Reference for the cost upper bound when expanding the structure |
| Currently applied | No |

### aws-live — cost-saving

| Item | Value |
| --- | --- |
| Monthly estimate | about 200~350 USD |
| Network | NAT not used by default |
| RDS | single-AZ or limited Multi-AZ review |
| View | Can run always-on |
| ALB | Introduced only when the need is confirmed |
| Advantage | Fixed-cost savings |
| Caution | Review availability and audit requirements separately |

### aws-live — high-availability

| Item | Value |
| --- | --- |
| Monthly estimate | about 430~650 USD |
| Network | Multi-AZ NAT or an equivalent outbound structure |
| RDS | Multi-AZ |
| View | ALB + always-on Fargate |
| Endpoint | Multi-AZ |
| Observability | Expanded Logs·Metrics·Alarm |
| Advantage | Strengthened availability and operating standards |
| Caution | May be excessive for the current single-operator scale |

## Cost Reduction Priorities

### Priorities

| Item | Value |
| --- | --- |
| 1 | Minimize the number of Interface Endpoints and AZ count |
| 2 | Keep NAT Gateway not used by default |
| 3 | Operate port-view at desiredCount 0/1 |
| 4 | start/stop Windows EC2 centered on business hours |
| 5 | Limit CloudWatch Logs retention and ingestion |
| 6 | Review the RDS instance size and the Multi-AZ entry point |
| 7 | Introduce ALB only when external exposure is needed |
| 8 | Apply S3 lifecycle and ECR lifecycle |

### Principles to keep while reducing cost

| Item | Value |
| --- | --- |
| Order safety | Do not remove order validation or approval gates for cost savings |
| Data stability | Do not aggressively shrink RDS backup and PITR |
| Security | Do not solve cost issues by switching RDS to Public |
| Endpoint | Before deleting, confirm the actual dependent services and outbound alternatives |
| EC2 | Check together the impact on EIP·broker-registered IP·Windows session |
| live | Decide the cost structure after confirming paper stable cycles and availability requirements |

## Budget and Monitoring

| Item | Value |
| --- | --- |
| Monthly budget line | Current paper 180 USD |
| Warning threshold | Reaching 80% of the budget |
| Emergency check | Projected month-end cost exceeds the budget by 20% |
| Weekly check | VPC · RDS · EC2 · CloudWatch cost |
| Monthly check | Compare per-service actuals with the estimated range |
| Endpoint check | Endpoint Hours · AZ count · unused Endpoints |
| Compute check | EC2 running hours · Fargate task hours |
| Storage check | RDS · S3 · ECR growth rate |
| Recalculation point | live design · ALB introduction · Multi-AZ transition · RDS size change |

## Evidence Management

| Item | Value |
| --- | --- |
| This document | Actual cost · estimated range · unit-price reference · savings principles |
| Billing source data | Not included in this document |
| Date-by-date cost analysis | `WORKLOG.md` |
| Resource change history | The relevant spec's `operation-notes.md` |
| Operator decisions | `operator-decisions.md` |
| Cost risk | `risk-register.md` |
| Detailed calculations | AWS Pricing Calculator |
| Historical Notes | Not accumulated in this document |

## Update Rules

| Item | Value |
| --- | --- |
| Actuals update | Replace the Dashboard values after the monthly bill is finalized |
| Estimated range | Update only when the structure or usage time changes |
| Unit-price update | Revise after checking the Pricing Calculator |
| Date-by-date notes | Do not add to the body; record in WORKLOG |
| New service | Reflect when it incurs 1 USD/month or more, or a fixed cost |
| Small automation | Group Lambda·Scheduler·Step Functions together when within the free tier |
| Table format | Standalone tables use the 2 columns `Item / Value` |
| Long cell | Split into multiple rows |
| Sensitive information | Only `[REDACTED]`-style placeholders are used |

## Security Notes

| Item | Value |
| --- | --- |
| Actual AWS execution | None |
| AWS resource changes | None |
| Application code changes | None |
| broker · KIS · DB execution | None |
| Verbatim sensitive information | Recording prohibited |
| Account·Billing identifying info | Recording prohibited |
| Document role | Reference for AWS Migration cost judgment |
