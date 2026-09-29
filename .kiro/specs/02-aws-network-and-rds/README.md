# 02 AWS Network and RDS — Operator Summary

This is a one-page summary that operators review quickly on a daily basis. For the detailed design see `design.md`, for the execution order see `runbook.md`, and for the checklist items see `validation-checklist.md`. Decision values are changed only in `../_common/operator-decisions.md`.

## 1. Current Status

- The aws-paper first Foundation was built directly by the operator through the AWS Console (VPC / 6 Subnets / IGW / 3 Route Tables / 8 SGs / 6 VPC Endpoints / RDS Subnet Group / Parameter Group / RDS instance / Secrets Manager metadata).
- RDS is placed in the `data` private subnet with Publicly accessible = No, deletion protection = Enabled, and backup retention = 7 days.
- With Kiro ReadOnly automated Validation, the Network / SG / VPC Endpoint / RDS items match the expected values (0 mismatches). The validation-checklist labels are [O] 78 / [X] 0 / [Kiro follow-up] 2 / [Operator] 20.
- The current stage is the final confirmation before entering DB Migration (03 spec) and MarketConnector AWS porting (follow-up spec).

## 2. Next Tasks for the Operator

1. Build a per-domain table row count snapshot based on the local PostgreSQL to establish a comparison baseline before cutover.
2. Connect to RDS `portfolio-paper-rds` via `psql` and finalize the agreement to run the SQL that creates the `portfolio` DB / the per-domain schemas / the 7 roles (entering the 03 / 06 spec).
3. Before porting the MarketConnector EC2, finalize the usage criteria for EIP / Security Group / Secrets Manager / SSM Parameter Store.

## 3. AWS Resources Already Completed

| Area | Completed Item | Status | Source Document |
|---|---|---|---|
| Region | `ap-northeast-2` used | Complete | [Operation Notes](./operation-notes.md) |
| VPC | `portfolio-vpc` (10.0.0.0/16) | Complete | [Operation Notes](./operation-notes.md) |
| Subnet | public / app / data, 2 each (6 total) | Complete | [Validation Checklist §2](./validation-checklist.md) |
| Internet Gateway | `portfolio-igw` attach | Complete | [Validation Checklist §2](./validation-checklist.md) |
| NAT | NAT Gateway / NAT Instance not used | Complete | [Validation Checklist §2](./validation-checklist.md) |
| Route Table | `rt-public` / `rt-app` / `rt-data`, 3 types | Complete | [Validation Checklist §2](./validation-checklist.md) |
| Security Group | `sgroup-*`, 8 types | Complete | [Validation Checklist §3](./validation-checklist.md) |
| VPC Endpoint | S3 Gateway + 5 Interface (ECR api/dkr, Secrets Manager, SSM, Logs) | Complete | [Validation Checklist §4](./validation-checklist.md) |
| RDS Subnet Group | `portfolio-paper-subnet-group` | Complete | [Validation Checklist §5](./validation-checklist.md) |
| RDS Parameter Group | `pg-portfolio-paper` (postgres16) | Complete | [Validation Checklist §5](./validation-checklist.md) |
| RDS instance | PostgreSQL 16, `db.t4g.small`, Single-AZ | Complete | [Validation Checklist §5](./validation-checklist.md) |
| Secrets Manager | `/portfolio/paper/rds/master` (value is `[REDACTED]`) | metadata registered | [Operation Notes](./operation-notes.md) |
| RDS Public access | `Publicly accessible = No` | Complete | [Validation Checklist §5](./validation-checklist.md) |
| Backup / Deletion | retention 7 days, deletion protection on | Complete | [Validation Checklist §5](./validation-checklist.md) |

This README does not record the RDS endpoint hostname / secret value / account-id / access key id. Connection details are confirmed in `operation-notes.md` and in secure local environment variables.

## 4. Related Document Links

### Documents the Operator Reviews Frequently

- [Runbook](./runbook.md)
- [Validation Checklist](./validation-checklist.md)
- [Operation Notes](./operation-notes.md)
- [Operator Decisions](../_common/operator-decisions.md)
- [Cost Simulation](../_common/cost-simulation.md)

### Documents for Kiro / Audit

- [Requirements](./requirements.md)
- [Design](./design.md)
- [Tasks](./tasks.md)
- [Decision Matrix](./decision-matrix.md)
- [Traceability Matrix](./traceability-matrix.md)
- [Risk Register](../_common/risk-register.md)

## 5. Auto-Update Criteria

- AWS resource creation / change / validation results are recorded in `operation-notes.md` by default.
- Checklist pass/fail results are recorded in `validation-checklist.md`.
- When an operator decision changes, update `../_common/operator-decisions.md`.
- This README is updated only briefly, and only when the status summary changes.
- After a task completes, the auto-update files are limited to at most 3 by default.
