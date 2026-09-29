# Decision Matrix — 02-aws-network-and-rds

This document organizes the key decisions the operator must make, in table units. It has been updated to reflect the new environment model (local-dev / aws-paper / aws-live) and the NAT-free default. All costs are approximations based on Seoul ap-northeast-2 and carry the caveat "needs confirmation in the AWS Pricing Calculator". All secrets are denoted only as `[REDACTED]`.

The source of the detailed unit prices is the root common document [`../_common/cost-simulation.md`](../_common/cost-simulation.md).

## 1. Environment Model

| Environment | Location | Role | Automatic BUY/SELL | Notes |
|------|------|------|---------------|------|
| local-dev | Operator local PostgreSQL | Development / unit validation / source changes | Prohibited | No AWS resources. Cost 0 |
| aws-paper | AWS single VPC | First AWS build, KIS paper-trading rehearsal | Blocked initially → stepwise allowance after validation | Operational stability criteria applied |
| aws-live | AWS single VPC | Follow-up build after paper validation, real account | Automatic ordering prohibited initially, candidate + manual approval | Automatic-retry-prohibited policy |

Recommendation: The first environment this spec applies to is aws-paper. The aws-live cutover is carried out integrally in `10-cutover-and-validation-runbook`.

## 2. Single VPC vs prod / non-prod Separation

| Option | Cost | Operational burden | Recommendation |
|------|------|-----------|------|
| Single VPC + per-environment SG/Subnet tags | Low | Low | Top priority (current decision) |
| prod / non-prod VPC separation | NAT/Endpoint duplication + peering burden | High | Second priority. Re-review on regulatory / audit requirements |

## 3. NAT Options (comparison for reference)

This spec adopted NAT-not-used as the default for both aws-paper / aws-live. This table is reference material for reviewing changes.

| Option | Description | Monthly fixed cost (single AZ) | Security | Operational difficulty | Adopted by this spec |
|------|------|---------------------|------|-------------|--------------|
| A. 1 NAT GW | Single-AZ NAT | ~$43 + data processing | Medium | Low | Not adopted |
| B. 2 NAT GWs (multi-AZ) | NAT per AZ | ~$86 + data processing | Strong | Low | Not adopted |
| C. NAT Instance `t4g.nano` | Self-managed EC2 NAT | ~$3.5 + EBS + data | Weak (SPOF) | High | Auxiliary (if needed) |
| D. NAT not used + Fargate public + EC2 public | Place only internet-outbound workloads as public without NAT | ~$0 (NAT) | Medium (SG validation required) | Low~Medium | **Top priority adopted** |

Saving effect when adopting NAT-free (aws-paper basis): saves ~$43/month for 1 NAT GW + ~$2~$10/month for data processing.

## 4. Internet Outbound Workload Placement Options (NAT-free premise)

| Option | Applied workloads | Cost impact | Security risk | SG control basis | Operational difficulty |
|------|---------------|-----------|------------|--------------|-------------|
| OPT-1: ECS Fargate + public subnet + assignPublicIp | crawler / preprocessor / research | No NAT. Small hourly cost when using IPv4 | If the Task SG is misconfigured, external inbound is possible | Never allow inbound 0.0.0.0/0. Allow outbound only to required domains such as KRX/Naver/yfinance/holiday | Low |
| OPT-2: marketconnector EC2 + EIP | marketconnector | EC2 unit cost only, EIP attach free | If the EC2 SG is wrong, external exposure | inbound only SSM, block port 22/external ports | Medium |
| OPT-3: crawler-dedicated EC2 or ECS on EC2 | Promote the crawler when KRX login / Selenium stability is insufficient | EC2 24/7 cost | Burden of operating EC2 directly | Same as OPT-2 | Medium |
| OPT-4: NAT Instance (`t4g.nano`) | Auxiliary for some workloads where NAT-free is difficult | EC2 ~$3.5 + EBS + data | SPOF, implement multi-AZ HA directly | NAT EC2 SG is outbound only, inbound only from inside the VPC | High |

Recommendation

- marketconnector: OPT-2 top priority.
- crawler / preprocessor / research: OPT-1 top priority. Promote only the crawler to OPT-3 if KRX login / Selenium stability is insufficient.
- OPT-4 is auxiliary (if needed).

## 5. RDS Instance / multi-AZ

| Environment | Instance | AZ | storage | Monthly RDS cost (approx.) | Notes |
|------|---------|----|---------|--------------------|------|
| local-dev | N/A | N/A | N/A | $0 | Existing local PostgreSQL kept |
| aws-paper | db.t4g.small | single-AZ | gp3 50 GB | ~$26 + storage ~$7 | retention 7 days, PITR on |
| aws-live (cost-saving option) | db.t4g.medium | single-AZ | gp3 100 GB | ~$52 + storage ~$13 | Downtime on AZ failure. Recovery only via PITR |
| aws-live (stability-first option) | db.t4g.medium | multi-AZ | gp3 100 GB | ~$104 + storage ~$13 | Automatic failover |
| aws-live (performance-first option) | db.m6g.large | multi-AZ | gp3 200 GB | ~$330 + storage ~$26 | Headroom for transaction load |

Recommendation: aws-paper starts with `db.t4g.small` single-AZ. aws-live can start with the cost-saving option single-AZ; the operational stability-first option is multi-AZ.

## 6. VPC Endpoint Enablement Combinations (NAT-free core)

| Combination | Enabled endpoints | Monthly cost (multi-AZ basis) | Effect |
|------|---------------|------------------------|------|
| Minimal | S3 (Gateway free) + ECR (api+dkr) | ~$32 | Stable container pull |
| Recommended | Above + Secrets + SSM + Logs | ~$80 | Stable AWS service access under NAT-free |
| Extended | Above + KMS + STS + EC2 | ~$112 | CMK / OIDC role assume / some SDK |

Recommendation

- aws-paper: Can start with the recommended combination + single AZ (5 Endpoints × 1 AZ ≈ ~$40/month).
- aws-live: Recommended combination multi-AZ (~$80/month). Add the KMS endpoint when a CMK is adopted.

NAT-free effect: NAT GW not-used saving ~$43~$86/month vs Endpoint cost ~$40~$80/month. The net effect is toward savings, with the availability / performance benefits of Endpoints added.

## 7. Whether to Use an ALB

| Option | Monthly fixed cost | Security | Operational difficulty | Recommended environment |
|------|---------|------|-------------|----------|
| 1 internal ALB | ~$16.5 + LCU | Strong (inside VPC) | Low | aws-live stability-first option |
| No ALB + ECS Service Discovery + SSM port forwarding | $0 | Strong | Medium (operator access procedure needed) | aws-paper initial, aws-live cost-saving option |
| public ALB + auth gate | ~$16.5 + auth cost | Depends on the auth gate | Medium-high | When external operator access is strictly needed |

Recommendation: Not used in early aws-paper. Deferred in the early aws-live cost-saving option. Promote to an internal ALB per operator decision.

## 8. Backup retention / PITR

| Environment | retention | PITR | Additional monthly backup storage cost |
|------|----------|------|-----------------------------|
| local-dev | Operator's own backup | N/A | 0 |
| aws-paper | 7 days | on | Small |
| aws-live (cost-saving option) | 7 days | on | Small |
| aws-live (stability-first option) | 14 days | on | Medium (about 1× the DB size free, ~$0.105/GB-month for the excess) |

Recommendation: In the aws-live stability-first option, 14 days + manual snapshot (quarterly, right before cutover).

## 9. SG Policy

| Item | Options | Recommendation |
|------|------|------|
| RDS inbound | 0.0.0.0/0 vs SG reference only | SG reference only |
| EC2 SSH 22 | 0.0.0.0/0 vs SSM | SSM |
| public subnet ECS Task inbound | not allowed vs allowlist | not allowed (operator allowlist if needed) |
| ALB public scope | public vs internal | internal (when adopted) |

Recommendation: Same policy for all environments. No cost difference, large security difference.

## 10. Data cutover Method

| Option | Procedure | Downtime | Operational difficulty | Recommendation |
|------|------|---------|-------------|------|
| pg_dump + pg_restore | stop → dump → restore → validate → start | Tens of minutes | Low | Top priority |
| AWS DMS | source connection + ongoing replication | A few minutes | Medium-high | Deferred. Re-review when zero-downtime becomes strictly necessary |
| Backup snapshot restore | Requires an RDS source | N/A | N/A | Out of scope for this spec |

Recommendation: Top priority `pg_dump` + `pg_restore`. The cutover window is an operator decision.

## 11. Cost Profile (aws-paper)

Estimated monthly total per aws-paper environment. For detailed unit prices, see the root common document [`../_common/cost-simulation.md`](../_common/cost-simulation.md).

| Item | low | realistic | stable |
|------|-----|-----------|--------|
| marketconnector EC2 (`t4g.small` 24/7) + EIP attach | ~$15 | ~$15 | ~$17 |
| Internet-outbound ECS Fargate (crawler / preprocessor combined, short batch) | ~$5 | ~$8 | ~$12 |
| Internal ECS Fargate (port-view / decision / execution / research) | ~$30 | ~$45 | ~$70 |
| RDS db.t4g.small single-AZ + gp3 50GB | ~$33 | ~$33 | ~$36 |
| RDS backup retention 7 days | ~$2 | ~$3 | ~$5 |
| NAT Gateway | 0 | 0 | 0 |
| ALB | 0 | 0 | ~$17 (when adopted) |
| VPC Endpoint (single AZ, 5 recommended types) | ~$24 | ~$32 | ~$40 |
| CloudWatch Logs (5~15 GB, retention 7 days) | ~$5 | ~$10 | ~$18 |
| CloudWatch Metrics + Alarms (paper basis) | ~$5 | ~$8 | ~$12 |
| Secrets Manager (~10 secrets assumed) | ~$4 | ~$5 | ~$6 |
| ECR / S3 / Step Functions / EventBridge / Lambda | ~$2 | ~$3 | ~$5 |
| **Total** | **about 125** | **about 162** | **about 238** |

Notes

- low: NAT-free + no ALB + lightweight internal ECS + single-AZ Endpoints + 7-day Logs + minimized Secrets.
- realistic: this spec's recommendation as-is.
- stable: Review adopting an ALB for operational retrospectives, expand Logs / Metrics.

Compared with the existing [`../_common/cost-simulation.md`](../_common/cost-simulation.md) (previous-decision-based paper realistic ~$212), it becomes about $50~$80 lower due to the NAT removal saving ~$43~$50, the additional saving from removing dev AWS across the other environments combined, and the ~$17 saving from not using an ALB.

## 12. Cost Profile (aws-live)

Estimated monthly total per aws-live environment.

| Item | low (cost-saving option) | realistic | stable (stability-first option) |
|------|-------------------|-----------|------------------------|
| marketconnector EC2 (`t3.medium` 24/7) + EIP | ~$38 | ~$40 | ~$45 |
| Internet-outbound ECS Fargate | ~$10 | ~$15 | ~$22 |
| Internal ECS Fargate (24/7 services + daily/intraday tasks) | ~$60 | ~$80 | ~$120 |
| RDS db.t4g.medium single-AZ + gp3 100GB | ~$65 | ~$70 | ~$80 |
| RDS db.t4g.medium multi-AZ + gp3 100GB | 0 (cost-saving option) | 0 | ~$130 (replacement) |
| RDS backup retention | ~$5 | ~$8 | ~$15 |
| NAT Gateway | 0 | 0 | 0 |
| ALB internal | 0 | 0 | ~$17 |
| VPC Endpoint (multi-AZ, 5 types) | ~$40 | ~$80 | ~$80 |
| CloudWatch Logs (30~80 GB, retention 14 days) | ~$25 | ~$40 | ~$60 |
| CloudWatch Metrics + Alarms | ~$10 | ~$15 | ~$22 |
| Secrets Manager (~10 secrets) | ~$4 | ~$5 | ~$7 |
| ECR / S3 / Step Functions / EventBridge / Lambda | ~$3 | ~$5 | ~$8 |
| **Total (single-AZ-based cost-saving option)** | **about 260** | **about 358** | n/a |
| **Total (multi-AZ-based stability-first option)** | n/a | n/a | **about 506** |

Notes

- low (cost-saving option): single-AZ RDS + single-AZ Endpoint cost + no ALB. Premised on tolerating downtime on AZ failure.
- realistic: keep single-AZ + Endpoint multi-AZ to partially reinforce availability.
- stable (stability-first option): multi-AZ RDS + Endpoint multi-AZ + internal ALB + 14-day backup. The automatic-retry-prohibited policy applies identically to all options.

Compared with the existing [`../_common/cost-simulation.md`](../_common/cost-simulation.md) (previous-decision-based live realistic ~$492), a total of about $130~$170 can be saved through the NAT-free saving ~$86~$98, the ~$17 for not using an ALB, and the effect of removing dev AWS (combined across separate environments).

## 13. Per-Item Organization of Cost Saving vs Operational Risk

| Saving item | Saving effect (monthly) | Operational risk |
|-----------|----------------|-------------|
| Remove NAT Gateway | ~$43~$98 | Risk of external exposure if a public subnet workload's SG is misconfigured. SG control required |
| Remove AWS dev | dev environment combined ~$60~$120 (previous estimate) | Integrated validation burden concentrates on aws-paper. Need to strengthen paper operational stability |
| Not use an ALB | ~$17 | Operator access needs SSM port forwarding or a shared IP allowlist |
| Start RDS single-AZ | ~$26~$50 (depending on instance class) | Downtime on AZ failure. Reliance on PITR |
| CloudWatch Logs retention 7~14 days | ~$5~$30 | Insufficient incident / audit retrospective. Temporary export needed when a retrospective is required |
| Minimize Secrets items or use SSM SecureString | Proportional to the number of secrets, ~$1~$5 | Varies with the security / rotation decision in 06 |
| Start VPC Endpoint single AZ | ~$40 (vs multi-AZ) | Endpoint failure on a 1 AZ failure. Application-side retry needed |

## 14. Operator Decision Checklist

The decision values of this checklist are recorded in the root common document [`../_common/operator-decisions.md`](../_common/operator-decisions.md).

- [ ] CIDR / AZ decision
- [ ] Internet-outbound workload option mapping (OPT-1 ~ OPT-4)
- [ ] aws-paper RDS instance class
- [ ] aws-live RDS instance / single-AZ vs multi-AZ
- [ ] VPC Endpoint enabled items and AZ policy (single AZ vs multi-AZ)
- [ ] Whether to adopt an ALB (aws-paper / aws-live)
- [ ] CloudWatch Logs retention
- [ ] backup retention (aws-paper / aws-live)
- [ ] cutover schedule / tolerable downtime window
- [ ] Timing of registering the 7 role passwords in Secrets Manager (synced with spec 06)

After this checklist passes, the operator proceeds from `tasks.md` Phase 1.

## Per-MS AWS Service Candidate Comparison Reference

This decision-matrix focuses on infrastructure option decisions such as NAT / RDS / VPC Endpoint / ALB / backup / cutover.

The per-MS compute / orchestration candidate comparison and the portfolio-appeal augmentation plan are organized separately in the root common document [ms-aws-service-decision-matrix.md](../_common/ms-aws-service-decision-matrix.md). Being compared:

- Compute: EC2 / ECS Fargate / ECS on EC2 / AWS Batch / Lambda / EKS / Elastic Beanstalk / App Runner
- Orchestration: Step Functions / EventBridge Scheduler etc.

The 8 MS compute top-priority decisions (OD-MS-001 ~ OD-MS-010) are recorded in a locked state in chapter 9 of the root common [`../_common/operator-decisions.md`](../_common/operator-decisions.md). This decision-matrix does not change those decisions and focuses only on the network / RDS option selection.

## 15. Safety Constraints for This Spec's Work

- Prohibit actual AWS resource creation
- Prohibit modifying the 8 MS code / README / AGENTS.md / CHANGELOG / docs / worklog
- Prohibit actual secret value output (all `[REDACTED]`)
- 06-secrets-and-iam is not written at the time of this work
