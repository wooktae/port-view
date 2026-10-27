# AWS Resource Glossary — AWS Migration

## Purpose

This document is a shared glossary for quickly checking the meaning of the AWS services and resources used in the PORT-STRATEGY-AI AWS Migration.

Each term is organized in the same two-column table with the five items below.

| Item | Value |
| --- | --- |
| Description | The basic meaning of the AWS service or resource |
| Role | The purpose it serves in PORT-STRATEGY-AI |
| Cost | Whether it is free and the main billing basis |
| Caution | Key points to verify during operation |
| Related spec | The associated spec number |

Date-by-date work history, execution names, ARNs, Task Definition revisions, smoke results, and Risk·Decision reinforcement details are excluded from the term descriptions. Such operational evidence is managed in `WORKLOG.md`, and in each spec's `operation-notes.md`, `operator-decisions.md`, and `risk-register.md`.

Sensitive information is never recorded verbatim; only `[REDACTED]`-style placeholders are used.

## Operating Summary

| Item | Value |
| --- | --- |
| Baseline environments | `local-dev` · `aws-paper` · `aws-live` |
| AWS Region | Seoul `ap-northeast-2` |
| Network principle | NAT Gateway not used by default in both paper and live |
| External outbound | Public Subnet + Public IP where needed |
| RDS | Placed in Private Subnet · SG-reference-based access |
| MarketConnector | EC2 + EIP |
| port-view | ECS Fargate Service |
| Daily Batch | ECS Fargate Task + Step Functions + EventBridge Scheduler |
| KRX GUI | Windows EC2 interactive worker |
| Research | AWS Batch + S3 |
| Sensitive information | Only `[REDACTED*]` placeholders recorded |
| Actual execution | Out of scope of this document · performed directly by the operator |

## Operational Validation Note (2026-07-22)

The term definitions and the 5-field template are not changed. The following are short operational facts confirmed in a normal automatic Scheduler cycle.

| Item | Value |
| --- | --- |
| EventBridge Scheduler · Step Functions | Normal automatic cycle Step 1~11 · Step 12~17 success confirmed |
| ECS / Fargate | READY Plan → Order · 2 Order Chain validators passed automatically |
| SSM RunCommand | MarketConnector stage completed normally |
| RDS | Order·fill·Position·Balance consistency confirmed by after-check |
| CloudWatch Logs | Slack success path · OPS success record confirmed |
| port-view | ECS Fargate initial validation state retained · P2 (ALB·Route53·Auto Scaling, etc.) out of scope |

## Category TOC

### Network

- [Region](#region)
- [VPC](#vpc-virtual-private-cloud)
- [Subnet](#subnet)
- [Public Subnet](#public-subnet)
- [Private Subnet](#private-subnet)
- [Route Table](#route-table)
- [Internet Gateway](#internet-gateway-igw)
- [NAT Gateway](#nat-gateway)
- [NAT Instance](#nat-instance)
- [VPC Endpoint](#vpc-endpoint)
- [Security Group](#security-group-sg)
- [Elastic IP](#elastic-ip-eip)
- [ALB](#alb-application-load-balancer)

### Compute

- [EC2](#ec2-elastic-compute-cloud)
- [ECS](#ecs-elastic-container-service)
- [Fargate](#fargate-ecs-launch-type)
- [ECR](#ecr-elastic-container-registry)

### Database

- [RDS](#rds-relational-database-service)
- [RDS Subnet Group](#rds-subnet-group)
- [RDS Parameter Group](#rds-parameter-group)

### Security / IAM / Secrets

- [Secrets Manager](#secrets-manager)
- [SSM Parameter Store](#ssm-parameter-store)
- [IAM User](#iam-user)
- [IAM Role](#iam-role)
- [IAM Policy](#iam-policy)
- [Instance Profile](#instance-profile)
- [KMS](#kms-key-management-service)

### Orchestration

- [EventBridge Scheduler](#eventbridge-scheduler)
- [Step Functions](#step-functions)
- [Lambda](#lambda)
- [SSM RunCommand](#ssm-runcommand)
- [Windows Scheduled Task](#windows-scheduled-task)
- [Windows Autologon](#windows-autologon-sysinternals)

### Observability

- [CloudWatch Logs](#cloudwatch-logs)
- [CloudWatch Metrics](#cloudwatch-metrics)
- [CloudWatch Alarm](#cloudwatch-alarm)

### Storage / Artifact

- [S3](#s3-simple-storage-service)
- [AWS Batch](#aws-batch)
- [Cloud Map](#cloud-map--service-discovery)

## Glossary

### Region

| Item | Value |
| --- | --- |
| Description | A geographic location unit where AWS data centers are grouped. |
| Role | This project operates in a single region, Seoul `ap-northeast-2`. Since both the broker (KIS) and users are Korea-based, a single region is recommended from a latency / regulatory standpoint. |
| Cost | Free. Data transfer between different Regions is billed separately. |
| Caution | Moving the region changes RDS / ECR / all endpoint URLs. Region changes are prohibited within this spec. |
| Related spec | 02, 06, 07, 10. |

### VPC (Virtual Private Cloud)

| Item | Value |
| --- | --- |
| Description | A private network container inside AWS. You define the IP range and routing directly, like an on-premises network. |
| Role | A single VPC `portfolio-vpc` to hold the 8 PORT-STRATEGY-AI MS. Environments (aws-paper, aws-live) are separated within the same VPC by SG and subnet tags. |
| Cost | Free. Attached resources such as NAT Gateway and VPC Endpoint are billed. |
| Caution | When deciding the CIDR, it must not overlap with other in-house networks. Once set, the CIDR is hard to change. |
| Related spec | 02 first, all others depend on it. |

### Subnet

| Item | Value |
| --- | --- |
| Description | A network partition that carves a smaller IP range out of a VPC. |
| Role | This spec recommends 6 subnets: 3 types (public / private (app) / private (data)) × 2 AZ. |
| Cost | Free. |
| Caution | A subnet belongs to only one AZ. For multi-AZ, create separate subnets per AZ. |
| Related spec | 02. |

### Public Subnet

| Item | Value |
| --- | --- |
| Description | A subnet with direct internet outbound via the Internet Gateway. |
| Role | The MarketConnector EC2 (EIP) and ECS Fargate Tasks that need internet outbound (NAT-free strategy) are placed in the public subnet. |
| Cost | Free. A separate charge applies when using a Public IPv4. |
| Caution | Carelessly placing a Task or EC2 in a public subnet can expose it directly to the outside. Always block inbound with an SG. |
| Related spec | 02, 03, 08. |

### Private Subnet

| Item | Value |
| --- | --- |
| Description | A subnet without an Internet Gateway that can reach outside only via internal communication or NAT/VPC Endpoint. |
| Role | The placement location for RDS, general ECS Tasks, and workloads with no external outbound. Because this spec uses a NAT-free strategy, workloads that require internet outbound are not placed in the private subnet. |
| Cost | Free. |
| Caution | In a NAT-free environment, workloads that require external outbound will not work if placed in a private subnet. Only AWS services are reachable via VPC Endpoint. |
| Related spec | 02, 04, 05, 09. |

### Route Table

| Item | Value |
| --- | --- |
| Description | A set of rules that determines where subnet traffic flows. |
| Role | The public Route Table routes 0.0.0.0/0 → IGW. In the NAT-free strategy, the private (app) Route Table has no external route and reaches AWS services only via VPC Endpoint. The data Route Table has no external route. |
| Cost | Free. |
| Caution | A wrong Route Table mapping prevents an ECS Task from reaching RDS or cuts off external outbound. Check it before the SG. |
| Related spec | 02. |

### Internet Gateway (IGW)

| Item | Value |
| --- | --- |
| Description | The gateway that connects the VPC to the internet. |
| Role | The external outbound for public subnets and the external exit for EIP-assigned EC2. |
| Cost | Free. Internet data transfer is billed separately. |
| Caution | Attach only one per VPC. Changing it after detach cuts off external communication. |
| Related spec | 02, 03. |

### NAT Gateway

| Item | Value |
| --- | --- |
| Description | An AWS managed NAT that lets private subnet workloads make outbound-only connections to the internet. |
| Role | For cost savings, this project adopted non-use as the default in both aws-paper and aws-live. Workloads that need internet outbound are replaced with a public subnet or EC2+EIP. |
| Cost | When used, ~$43/month/AZ + data processing ~$0.059/GB. One of the largest single fixed costs. |
| Caution | Multi-AZ NAT doubles the cost. Once turned on, it is easy to forget the cost review. |
| Related spec | 02 comparison item, 03/08 NAT-free alternative review. |

### NAT Instance

| Item | Value |
| --- | --- |
| Description | A self-managed EC2 (e.g., `t4g.nano`) that acts as a NAT. |
| Role | A candidate when you want to reduce NAT Gateway cost. In this spec it is only compared, and the recommendation is NAT-free. |
| Cost | EC2 hourly rate (~$3.5/month) + EBS + data processing. Much cheaper than a NAT GW. |
| Caution | A single EC2, so it is a SPOF. Patching/monitoring is manual. Multi-AZ HA must be implemented by the operator directly. This project chose the NAT-free strategy to avoid this operational burden. |
| Related spec | 02 comparison item. |

### VPC Endpoint

| Item | Value |
| --- | --- |
| Description | A private connection to AWS services from within the VPC without NAT. S3 uses a Gateway Endpoint; others use an Interface Endpoint. |
| Role | The default retained set is S3 Gateway + ECR api/dkr Interface + Secrets Manager Interface + CloudWatch Logs Interface. SSM is not required (Public outbound or SSM Interface Endpoint, selectable). |
| Cost | Gateway Endpoint is free. Interface Endpoints scale with the number of endpoints and AZs. |
| Caution | If the default set is missing, the relevant AWS API is unreachable without NAT. Check Interface Endpoint SG inbound 443 first. Whether an SSM Endpoint exists is separate from using SSM Session Manager. |
| Related spec | 02, 06, 07. |

### Security Group (SG)

| Item | Value |
| --- | --- |
| Description | A stateful firewall rule at the AWS resource level. |
| Role | SGs are separated per MS. The RDS SG allows inbound only via other SG references (never 0.0.0.0/0). |
| Cost | Free. |
| Caution | Because an SG is stateful, allowing inbound auto-allows the return outbound. However, outbound must be specified separately. SG rule changes take effect immediately, so a wrong edit can cause an outage during operation. |
| Related spec | 02 first, used by all others. |

### Elastic IP (EIP)

| Item | Value |
| --- | --- |
| Description | A static public IP. Can be assigned to an EC2 or NAT Gateway. |
| Role | Assigned to the MarketConnector EC2 to satisfy the broker (KIS) IP registration policy. The EIP is registered with the broker. |
| Cost | Free while attached to a running instance. ~$3.6/month when detached or stopped. |
| Caution | When replacing the EC2, detach the EIP → attach it to the new EC2. During the unattached time there is idle cost + risk of a broker outbound IP change. |
| Related spec | 03 marketconnector-ec2. |

### ALB (Application Load Balancer)

| Item | Value |
| --- | --- |
| Description | An HTTP/HTTPS traffic-distributing load balancer. |
| Role | Can be placed in front of the port-view operations console (internal ALB). Per this spec's decision, it is not used initially in aws-paper, and is also deferred for aws-live under the cost-savings plan. |
| Cost | ~$16.5/month + LCU usage. |
| Caution | It is safer to narrow the ALB SG inbound to an operator IP allowlist or an authentication gate. |
| Related spec | 05 port-view-ecs-and-runbook. |

### EC2 (Elastic Compute Cloud)

| Item | Value |
| --- | --- |
| Description | An AWS virtual server. |
| Role | Runs the MarketConnector, which needs a static IP, and the Windows GUI-based KRX worker. |
| Cost | Instance hourly rate (e.g., `t4g.small` ~$15/month, `t3.medium` ~$38/month) + EBS + data transfer. **Stopping EC2 outside business hours reduces cumulative instance-hours** (the exact savings are computed after accumulating operating cycles). |
| Caution | Avoid public SSH access and use SSM Session Manager. When replacing an instance, re-verify the EIP·IAM Role·configuration bindings. |
| Related spec | 03, 08 (NAT-free alternative). |

### ECS (Elastic Container Service)

| Item | Value |
| --- | --- |
| Description | An AWS managed container orchestrator. |
| Role | This project runs almost all Python / Java MS on ECS. Clusters are separated per environment (`portfolio-paper`, `portfolio-live`). |
| Cost | The cluster itself is free. Cost depends on the Fargate / EC2 launch type running inside. |
| Caution | Manage Task Definition revisions. Retain them at deployment so you can roll back to a known-good revision. |
| Related spec | 04, 05, 08. |

### Fargate (ECS launch type)

| Item | Value |
| --- | --- |
| Description | An ECS launch type that runs containers only, with no server management. |
| Role | Runs container workloads such as Daily Batch, port-view, crawler, and preprocessor without server management. |
| Cost | ~$0.05056 / vCPU-hour + ~$0.00553 / GB-hour. 0.5 vCPU + 1 GB 24/7 ≈ $22.5/month. |
| Caution | If internet outbound is needed, verify the subnet·public IP·SG configuration. For KST-based work, use `TZ=Asia/Seoul` or timezone-aware code. |
| Related spec | 04, 05, 08, 09. |

### ECR (Elastic Container Registry)

| Item | Value |
| --- | --- |
| Description | An AWS container image registry. |
| Role | Stores images for the container-deployed MS among the 8 MS (`port-view`, `port-interest-crawler`, `port-interest-preprocessor`, `port-strategy-decision`, `port-strategy-execution`, `port-strategy-research`, and optionally `port-marketconnector`). |
| Cost | storage ~$0.10/GB-month. Pulls in the same region are free. In a NAT-free environment, ECR Endpoint cost is added. |
| Caution | Manage image tags (`:{git-sha}` + `:{env}`). Clean up old images with a lifecycle policy. Do not bake secrets into images at build time. |
| Related spec | 07 cicd-pipelines. |

### RDS (Relational Database Service)

| Item | Value |
| --- | --- |
| Description | An AWS managed RDBMS. This project uses RDS for PostgreSQL 16 or later. |
| Role | A single portfolio DB. Maintains 10 schemas in a schema-per-domain layout. Instances are separated per environment for aws-paper and aws-live. |
| Cost | Instance hourly rate + storage + backup. Multi-AZ roughly doubles it. |
| Caution | publicly accessible false. SG inbound only via other SG references. The master password is kept in Secrets Manager. |
| Related spec | 02, 06, 10. |

### RDS Subnet Group

| Item | Value |
| --- | --- |
| Description | The definition of the subnet set where RDS is placed. |
| Role | Groups the two private (data) subnets data-a and data-b so multi-AZ is possible. Even if starting single-AZ, the subnet group is configured in a multi-AZ-capable form. |
| Cost | Free. |
| Caution | A subnet group is hard to change after RDS creation. Configure it with 2 AZ subnets from the start. |
| Related spec | 02. |

### RDS Parameter Group

| Item | Value |
| --- | --- |
| Description | A collection of PostgreSQL configuration values. |
| Role | Applies recommended operating values grouped together, such as `rds.force_ssl=1` (paper/live), `log_min_duration_statement`, `log_lock_waits=1`, and `idle_in_transaction_session_timeout`. |
| Cost | Free. |
| Caution | Some items require a reboot when the parameter group changes. Mind the reboot window when changing during operation. |
| Related spec | 02. |

### Secrets Manager

| Item | Value |
| --- | --- |
| Description | An AWS managed secret store. Supports automatic rotation. |
| Role | Stores sensitive information such as the DB password, KIS app key·secret, account number, and Slack webhook URL. |
| Cost | $0.40 / secret / month + $0.05 / 10,000 API calls. |
| Caution | Do not record secret values in the application; retrieve them at runtime. Restrict IAM permissions to the required secret ARN. |
| Related spec | 06 secrets-and-iam. |

### SSM Parameter Store

| Item | Value |
| --- | --- |
| Description | An AWS store for environment variables / configuration / SecureString. |
| Role | Stores low-sensitivity configuration such as DB connection info, environment distinction, and strategy settings. If needed, some sensitive settings are also managed as SecureString. |
| Cost | Standard tier free (up to 10,000 parameters). Advanced tier $0.05/parameter/month. |
| Caution | Keep key names as-is for environment-variable compatibility. Putting a wrong value in the wrong environment can call the live broker from paper → risk of a live auto-trading incident. |
| Related spec | 06 secrets-and-iam. |

### IAM User

| Item | Value |
| --- | --- |
| Description | The identity a person (or external system) uses to sign in to the console / API within an AWS account. |
| Role | This project creates an IAM administrator user `portadmin` used by a single operator, and performs all daily work as portadmin. The Root account is kept for emergencies only (billing / account closure / IAM policy changes). 02 runbook Step 0 covers creating portadmin / granting AdministratorAccess / obtaining the console sign-in URL / enabling MFA / hardening Root. |
| Cost | Free. |
| Caution | Do not record the operator password, MFA info, or backup codes in plaintext. As a principle, Root access keys are not used. |
| Related spec | 02 (Step 0 portadmin creation), 06 (IAM permission matrix / review of additional users beyond portadmin). |

### IAM Role

| Item | Value |
| --- | --- |
| Description | A set of permissions used when an AWS resource calls another AWS resource. |
| Role | The permission set used when ECS Task, EC2, Lambda, and Step Functions call other AWS resources. |
| Cost | Free. |
| Caution | Separate the responsibilities of the Task Role and the Task Execution Role, and restrict Action and Resource to the required scope only. |
| Related spec | 06. |

### IAM Policy

| Item | Value |
| --- | --- |
| Description | The permission specification (JSON) attached to an IAM Role / User. |
| Role | Defines the AWS API actions and target resources to allow or deny for IAM Users and IAM Roles. |
| Cost | Free. |
| Caution | Do not use broad wildcards in Action or Resource; apply least privilege per environment and resource. |
| Related spec | 06. |

### Instance Profile

| Item | Value |
| --- | --- |
| Description | A wrapper that lets an EC2 use an IAM Role. |
| Role | Attached to the MarketConnector EC2 to use SSM Session Manager, Secrets Manager, CloudWatch Agent, and S3 token backup. |
| Cost | Free. |
| Caution | One per EC2. A change may require application re-authentication. |
| Related spec | 03, 06. |

### CloudWatch Logs

| Item | Value |
| --- | --- |
| Description | An AWS managed log collection/storage service. |
| Role | ECS Task logs (`awslogs` driver), EC2 CloudWatch agent logs, RDS PostgreSQL logs (`/aws/rds/instance/{id}/postgresql`). |
| Cost | ingestion ~$0.76/GB, storage ~$0.04/GB-month. When NAT is not used, Logs Endpoint cost is added. |
| Caution | A long retention accumulates storage cost. This spec recommends starting at 7 days for aws-paper and 14 days for aws-live. |
| Related spec | 04, 05, 08, 09. |

### CloudWatch Metrics

| Item | Value |
| --- | --- |
| Description | Time-series monitoring data. |
| Role | ECS / EC2 / RDS standard metrics + domain custom metrics (broker error rate, Daily Batch step results, intraday heartbeat, etc.). |
| Cost | AWS standard metrics are free. custom metric ~$0.30/metric/month. |
| Caution | Creating custom metrics indiscriminately accumulates cost. Add only metrics used directly in operational decisions. |
| Related spec | 04, 05, 08. |

### CloudWatch Alarm

| Item | Value |
| --- | --- |
| Description | An alarm based on a metric threshold. |
| Role | Detects things like an RDS connection surge, ECS Task failure, a spike in broker error rate, and a dropped intraday monitor heartbeat. |
| Cost | standard alarm ~$0.10/alarm/month. |
| Caution | Too many alarms cause alert fatigue → the Slack channel gets ignored. Keep only the essential alarms. |
| Related spec | 05, 10. |

### EventBridge Scheduler

| Item | Value |
| --- | --- |
| Description | A cron- or rate-based scheduler. Triggers Daily Batch / intraday polling. |
| Role | Starts scheduled automatic executions such as Daily Batch, EC2 start·stop, intraday work, and the Daily Brief. |
| Cost | Small-scale use stays within the free tier. Calls beyond that are billed separately. |
| Caution | Verify the cron, timezone, and enabled state. Weekend·holiday judgment is handled fail-closed by a separate Dispatcher or guard. |
| Related spec | 04 strategy-batch-stepfunctions, 10 cutover-and-validation-runbook. |

### Step Functions

| Item | Value |
| --- | --- |
| Description | AWS managed workflow orchestration. A state machine. |
| Role | Executes ECS Tasks and EC2 commands in order and manages success·failure·approval branches. |
| Cost | Billed by the number of state transitions. |
| Caution | Do not use automatic Retry on steps with duplicate-execution risk, such as order submission. Validate the failure path and the notification path together. |
| Related spec | 04, 09, 10. |

### Lambda

| Item | Value |
| --- | --- |
| Description | Serverless function execution. |
| Role | An auxiliary execution layer for the Scheduler dispatcher, Slack notifier, EC2 lifecycle, and short summary·recording tasks. |
| Cost | Billed by request count and execution time. Small-scale operation stays within the free tier. |
| Caution | Do not use it for tasks that need Selenium·long-running processing·state retention. For secrets, prefer Secrets Manager retrieval over plaintext environment variables. |
| Related spec | 04, 05, 08, 10. |

### S3 (Simple Storage Service)

| Item | Value |
| --- | --- |
| Description | Object storage. |
| Role | Keeps the KIS access_token EC2 local backup, stores port_strategy_research text reports, and holds RDS external dumps. |
| Cost | Standard storage ~$0.025/GB-month. Free outbound is possible via a Gateway endpoint. |
| Caution | Keep bucket public access blocked by default. Separate by per-environment prefix (`portfolio/paper/...`). |
| Related spec | 03, 09. |

### AWS Batch

| Item | Value |
| --- | --- |
| Description | A service for running long-running / variable-resource batch jobs. |
| Role | The top candidate for `port_strategy_research` backtesting. Can be invoked from Step Functions. |
| Cost | The internal compute env (Fargate or EC2) rate as-is. AWS Batch itself has no rate. |
| Caution | Check job queue priority, concurrent execution limits, and RDS connection leaks during long-running executions. |
| Related spec | 09 strategy-research-batch. |

### KMS (Key Management Service)

| Item | Value |
| --- | --- |
| Description | An encryption key management service. |
| Role | Holds the keys for RDS encryption at rest, Secrets Manager, S3 SSE-KMS, and the ECR image encryption option. |
| Cost | CMK $1.0/month/key + API calls. AWS managed keys are free. |
| Caution | aws-paper recommends KMS default. aws-live recommends a CMK, but if KMS permissions are missing from the IAM Policy, encryption/decryption fails. |
| Related spec | 02, 06. |

### Cloud Map / Service Discovery

| Item | Value |
| --- | --- |
| Description | Service discovery that auto-registers internal DNS names by ECS service name. |
| Role | Lets the port-view ECS Service call the marketconnector EC2 or another ECS Service internally without an ALB. The primary alternative for reducing ALB cost. |
| Cost | Very small. Based on registered services / hosted zone. |
| Caution | Keep DNS TTL short. The application must tolerate the time it takes for a new Task's IP change to propagate. |
| Related spec | 05. |

### SSM RunCommand

| Item | Value |
| --- | --- |
| Description | The remote command execution feature of AWS Systems Manager. Runs commands on EC2 / on-prem via documents such as `AWS-RunPowerShellScript` / `AWS-RunShellScript`. |
| Role | Remotely starts one-off tasks on the MarketConnector EC2 and the Scheduled Task on the Windows KRX worker. |
| Cost | The API call itself is effectively free. When using a VPC Endpoint, Endpoint cost applies. |
| Caution | Do not run Windows GUI work directly in SYSTEM Session 0. Verify the command exit code together with the actual result. |
| Related spec | 03, 08. |

### Windows Scheduled Task

| Item | Value |
| --- | --- |
| Description | The Windows OS built-in task scheduler. Runs registered tasks by a time trigger or an external trigger (`schtasks /Run`). |
| Role | Runs the KRX GUI worker in the Administrator interactive session of the Windows EC2. |
| Cost | Free. The Windows EC2 cost is separate. |
| Caution | Check the run-as user, Logon Mode, and the last run result and exit code. |
| Related spec | 08. |

### Windows Autologon (Sysinternals)

| Item | Value |
| --- | --- |
| Description | A Microsoft Sysinternals tool. Enables automatic login as a designated user (usually Administrator) right after Windows boot to auto-create a console interactive session. |
| Role | Automatically creates the interactive session the GUI worker uses after the Windows EC2 boots. |
| Cost | The tool is free. The Windows EC2 cost is separate. |
| Caution | Use it only as a paper-only security exception. To reduce credential-exposure risk, restrict access and stop the EC2 after work. |
| Related spec | 08. |

## Update Rules

| Item | Value |
| --- | --- |
| New term | Add as an H3 item under the relevant category |
| Default format | A 5-row, 2-column table: `Description / Role / Cost / Caution / Related spec` |
| Deduplication | Consolidate the role of the same service into one item |
| Operational history | Do not add to the term body; record in WORKLOG or `operation-notes.md` |
| Sensitive information | Only `[REDACTED]`-style placeholders are used |
| Cost | Approximate figures based on Seoul `ap-northeast-2` · verify exact amounts with the AWS Pricing Calculator |

## Security Notes

| Item | Value |
| --- | --- |
| AWS resource execution | None |
| Application code changes | None |
| Verbatim sensitive information | None |
| Allowed notation | `[REDACTED]`-style placeholders |
| Document role | Shared glossary for the AWS Migration |
