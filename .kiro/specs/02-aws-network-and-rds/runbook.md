# Runbook — 02-aws-network-and-rds

This document is an execution procedure that the operator can follow in order on the AWS Console. This spec's (02) first application environment is `aws-paper`, and `aws-live` is integrated in [`../10-cutover-and-validation-runbook`](../10-cutover-and-validation-runbook) (planned).

This runbook does not automatically create actual AWS resources. The operator clicks directly one step at a time, and proceeds to the next Step after passing the `create-then-confirm` of each Step.

Prerequisite inputs

- [`./requirements.md`](./requirements.md), [`./design.md`](./design.md), [`./tasks.md`](./tasks.md), [`./decision-matrix.md`](./decision-matrix.md)
- [`../_common/operator-decisions.md`](../_common/operator-decisions.md), [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md), [`../_common/cost-simulation.md`](../_common/cost-simulation.md), [`../_common/risk-register.md`](../_common/risk-register.md)
- [`./validation-checklist.md`](./validation-checklist.md), [`./traceability-matrix.md`](./traceability-matrix.md)

Security / safety principles

- All secrets use only `[REDACTED]` or a placeholder. Do not write actual values anywhere in this document, AWS console screen captures, or operator notes.
- Do not modify the 8 MS source code / README / AGENTS.md / CHANGELOG / docs / worklog.
- Do not call broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / order / fill / Daily Batch / intraday monitor.
- Actual cutover dump / restore is out of scope for this spec. This runbook proceeds only up to preparation.

## Step labels

Display the following Korean label next to each Step title. This is so the operator can distinguish the execution nature at a glance; the label does not change the procedure or decision values.

- `[실행]` a step that creates or changes a resource on the actual AWS Console.
- `[확인]` a step that only confirms decisions / console state / cost lines without creating a resource.
- `[준비]` a step that only agrees / records the procedure / SQL / commands to be used at the execution time of a follow-up spec (03 / 06 / 10, etc.).
- `[복구]` a rollback step performed only on incident / interruption / validation failure.

## Step 0. Create IAM administrator user portadmin [실행]

### Purpose

- Create an IAM administrator user `portadmin` that can perform all follow-up spec steps in a new AWS account that can only be logged into with the Root account. After this, all work proceeds as `portadmin`, and the Root account is kept for emergencies only.

### Pre-check

- Currently logged in as the Root account (root user / account-id shown in the top-right of the console).
- Confirm whether Root account MFA is active (if inactive, activation is recommended during or right after this Step).
- Cost incurred: 0 (no cost for IAM itself).
- Privilege: only the Root account can perform this.
- Prepare operator notes: record the account-id, console sign-in URL, and MFA registration time in the operator notes. Do not write the portadmin password anywhere in this document / notes (`[REDACTED]` only).

### Step 0-1. Create the portadmin user

1. AWS Console (Root logged-in state) → go to the IAM menu.
2. Users → click Create user.
3. Input values:
   - User name: `portadmin`
4. Options:
   - Provide user access to the AWS Management Console: check.
   - Console password: select `Custom password`, then the operator enters it directly (`[REDACTED]`. Do not write it in this document).
   - User must create a new password at next sign-in: operator decision (can be unchecked for solo operation, check if handover is possible).
5. Click Next.

### Step 0-2. Grant the AdministratorAccess policy

1. Permissions options: select `Attach policies directly`.
2. Enter `AdministratorAccess` in the Permissions policies search box.
3. Check the AWS managed policy `AdministratorAccess`.
4. Click Next.
5. Add Tags (recommended):
   - `env=paper`
   - `kind=admin`
   - `project=portfolio`
6. Review and create.
7. Click Create user.

### Step 0-3. Obtain the console sign-in URL

1. The created `portadmin` user details → Security credentials tab.
2. Record the Console sign-in URL in the operator notes (format: `https://<account-id>.signin.aws.amazon.com/console`).
3. (recommended) IAM → Account settings → IAM users sign-in link → register an Account alias (e.g., `portfolio-paper`). After registering the alias, the sign-in URL is simplified to the form `https://portfolio-paper.signin.aws.amazon.com/console`.

### Step 0-4. Activate portadmin MFA (strongly recommended)

1. `portadmin` user details → Security credentials tab → Multi-factor authentication (MFA) → Assign MFA device.
2. Device type: operator decision such as `Authenticator app` or `Security key`.
3. Complete registration following the on-screen guidance.
4. The operator keeps the MFA serial / backup codes in a safe location (no plaintext recording in this document / notes, `[REDACTED]`).

### Step 0-5. Switch to logging in as portadmin

1. Log out of the Root account.
2. Go to the Console sign-in URL obtained in Step 0-3.
3. Enter IAM user name: `portadmin` + password (`[REDACTED]`).
4. Enter the MFA code.
5. Confirm the user display in the top-right shows `portadmin@<account-id or alias>`.
6. After this, all Steps of this runbook are performed as `portadmin`.

### Step 0-6. (strongly recommended) Harden Root account security

1. If Root MFA is inactive, activate it immediately (Authenticator app or Security key).
2. If a Root access key (an access key/secret access key usable outside the AWS Management Console) is issued, delete it immediately. Root access key use is not recommended.
3. Perform all daily work as `portadmin`. Use Root only for emergencies such as billing / account closure / IAM policy changes.

### Create-then-confirm

- `portadmin` shown in the IAM → Users list.
- `AdministratorAccess` policy applied on the `portadmin` Permissions tab.
- MFA `Assigned` status shown on the `portadmin` Security credentials tab.
- Console sign-in URL recorded in the operator notes.
- `portadmin` login succeeds after Root logout.
- All follow-up Steps after this Step are performed as `portadmin`.

### Action on failure

- Loss of Root password / MFA: halt this runbook and prioritize recovery via the AWS Account recovery procedure (email verification, payment info verification, etc.). Do not proceed with other Steps until recovered.
- Loss of `portadmin` password: re-login as Root → IAM → Users → `portadmin` → Manage console access → Reset password. Denote the new password again with only `[REDACTED]`.
- Missing `AdministratorAccess` policy grant: if an insufficient-privilege error occurs after `portadmin` login, re-login as Root → Users → `portadmin` → Permissions → Add permissions → re-grant `AdministratorAccess`.
- MFA registration failure: retry after confirming Authenticator app time synchronization. Keep backup codes in a safe location.
- Halt criterion: if Step 0-1 / 0-2 / 0-5 do not pass, do not proceed past Step 1 (risk of working as Root in follow-up work while the privilege / login user is not determined).

## Step 1. Confirm AWS Region [확인]

### Purpose

- Fix the region so that all follow-up work proceeds in Seoul `ap-northeast-2`.

### Pre-check

- Step 0 complete. The current user is `portadmin` and is shown in the top-right of the console.
- `portadmin` has VPC / EC2 / RDS / Secrets Manager console access privileges (satisfied by the `AdministratorAccess` of Step 0-2).
- Cost incurred: 0 (no cost for region itself).

### AWS Console work order

1. Confirm the region selector in the top-right of the AWS Console.
2. Switch to `Asia Pacific (Seoul) ap-northeast-2`.
3. Fix the browser bookmark or console URL.

### Create-then-confirm

- `Seoul` is shown in the top-right of all console menus (VPC, EC2, RDS).
- Resource lists of other regions are not visible.

### Action on failure

- If insufficient privilege, re-confirm the `AdministratorAccess` policy grant of Step 0-2.
- If leftover resources from another region are still visible after switching, the region was not switched or there is multi-tab confusion. Re-confirm in a new tab.

## Step 2. Confirm cost / environment decisions [확인]

### Purpose

- The operator confirms the aws-paper first-application decision and the cost profile decision once more at this point.

### Pre-check

- Confirm the core-decision lock state of [`../_common/operator-decisions.md`](../_common/operator-decisions.md).
- Cost incurred: 0 (document review).

### AWS Console work order

This Step is a document review, not console work.

1. In the `At a Glance` section of [`../_common/operator-decisions.md`](../_common/operator-decisions.md), confirm the following items:
   - OD-ENV-003 first environment = aws-paper 🟢
   - OD-NET-001 paper NAT Gateway = unused 🟢
   - OD-NET-005 VPC Endpoint recommended set active 🟢
   - OD-NET-009 use only SSM Session Manager 🟢
   - OD-RDS-001 paper RDS = `db.t4g.small` single-AZ 🟢
   - OD-CUT-001 cutover = pg_dump+pg_restore 🟢
2. In `11. Cost Profile (aws-paper)` of [`./decision-matrix.md`](./decision-matrix.md), confirm the line among low / realistic / stable that the operator will apply.
3. Record the result as one line in the operator notes (e.g., `aws-paper realistic applied`). No actual secret.

### Create-then-confirm

- The above decision values match this runbook.
- A cost profile line decision record exists in the operator notes.

### Action on failure

- If the decision differs from this runbook, do not proceed with this runbook; start by updating [`../_common/operator-decisions.md`](../_common/operator-decisions.md).
- If a decision change seems needed, do not proceed arbitrarily in this runbook; record only the change proposal.

## Step 3. Fix CIDR / AZ [확인]

### Purpose

- Fix the VPC CIDR and the 2 AZs to use before proceeding with this runbook.

### Pre-check

- Confirm no CIDR conflict with other in-house networks.
- Cost incurred: 0.

### AWS Console work order

This Step is a decision record.

1. CIDR: `10.0.0.0/16` (recommended). Operator decision on in-house conflict.
2. 2 AZs: `ap-northeast-2a`, `ap-northeast-2c` (recommended).
3. Record the result in the operator notes.

### Create-then-confirm

- The CIDR / 2 AZs are written in the operator notes.

### Action on failure

- On CIDR conflict, decide an alternative such as `10.1.0.0/16` and then consistently adjust all subnet CIDRs in this runbook.

## Step 4. Create VPC [실행]

### Purpose

- Create a single VPC `portfolio-vpc` that will hold all resources of this spec.

### Pre-check

- Steps 1 ~ 3 complete.
- Privilege: `ec2:CreateVpc` (satisfied by `AdministratorAccess`).
- Cost incurred: 0.

### AWS Console work order

1. AWS Console → go to the VPC menu.
2. Your VPCs → click Create VPC.
3. Resources to create: select `VPC only`.
4. Name tag: `portfolio-vpc`
5. IPv4 CIDR block: `10.0.0.0/16`
6. IPv6 CIDR block: `No IPv6 CIDR block`
7. Tenancy: `Default`
8. Add Tags:
   - `env=paper`
   - `project=portfolio`
9. Click Create VPC.

### Create-then-confirm

- `portfolio-vpc` shown in the VPC list.
- State = `Available`.
- IPv4 CIDR = `10.0.0.0/16`.
- The default Route Table / NACL / DHCP option set are auto-created.

### Action on failure

- CIDR conflict error: re-confirm the CIDR decision of Step 3.
- If VPC creation fails, confirm in the top-right whether the region is wrong.
- rollback: the VPC can be deleted. However, if attached resources (Subnet, IGW) are created, those must be cleaned up first.

## Step 5. Create Subnets [실행]

### Purpose

- Create 6 subnets as public / private (app) / private (data) 3 types × 2 AZ.

### Pre-check

- Step 4 complete (VPC `portfolio-vpc` Available).
- Privilege: `ec2:CreateSubnet`.
- Cost incurred: 0.

### Step 5-1. Create 2 public subnets

1. VPC → Subnets → click Create subnet.
2. VPC ID: select `portfolio-vpc`.
3. Subnet 1: Name `public-a`, AZ `ap-northeast-2a`, IPv4 CIDR `10.0.0.0/24`, Tags `tier=public`, `env=paper`.
4. Click Add new subnet. Subnet 2: Name `public-b`, AZ `ap-northeast-2c`, IPv4 CIDR `10.0.1.0/24`, same Tags.
5. Click Create subnet.

### Step 5-2. Create 2 private app subnets

1. Click Create subnet (same VPC).
2. Subnet 1: Name `app-a`, AZ `ap-northeast-2a`, IPv4 CIDR `10.0.10.0/24`, Tags `tier=app`, `env=paper`.
3. Subnet 2: Name `app-b`, AZ `ap-northeast-2c`, IPv4 CIDR `10.0.11.0/24`, same Tags.
4. Click Create subnet.

### Step 5-3. Create 2 private data subnets

1. Click Create subnet (same VPC).
2. Subnet 1: Name `data-a`, AZ `ap-northeast-2a`, IPv4 CIDR `10.0.20.0/24`, Tags `tier=data`, `env=paper`.
3. Subnet 2: Name `data-b`, AZ `ap-northeast-2c`, IPv4 CIDR `10.0.21.0/24`, same Tags.
4. Click Create subnet.

### Create-then-confirm

- 6 subnets shown in the Subnets list.
- Each subnet's VPC = `portfolio-vpc`, AZ exactly as recommended.
- Auto-assign public IPv4 address: only public-a / public-b can be enabled per operator decision (decided again in Step 13). Keep the default in this Step.

### Action on failure

- CIDR overlap error: re-confirm the CIDR so it does not overlap with another subnet.
- Subnet creation failure in one AZ: re-confirm in the top-right whether the AZ is within the region.
- rollback: delete a wrongly created subnet via Subnets → Actions → Delete subnet (possible immediately if no resource is attached yet).

## Step 6. Create Internet Gateway / attach to VPC [실행]

### Purpose

- Create the external outbound exit for the public subnets.

### Pre-check

- Steps 4, 5 complete.
- Privilege: `ec2:CreateInternetGateway`, `ec2:AttachInternetGateway`.
- Cost incurred: IGW itself 0. Outbound data transfer is separate (`~$0.114/GB`).

### AWS Console work order

1. VPC → Internet Gateways → click Create internet gateway.
2. Name tag: `portfolio-igw`. Tags: `env=paper`.
3. Click Create internet gateway.
4. Select the created IGW → Actions → Attach to VPC.
5. VPC: select `portfolio-vpc` → Attach internet gateway.

### Create-then-confirm

- `portfolio-igw` State = `Attached` in the IGW list.
- The IGW is connected in the VPC `portfolio-vpc` details.

### Action on failure

- To reuse an IGW already attached to another VPC, detach then attach. 1 VPC = 1 IGW.
- rollback: Detach then Delete.

## Step 7. Confirm NAT Gateway / NAT Instance non-use [확인]

### Purpose

- Per this spec's decisions (OD-NET-001 / OD-NET-002 / R-COST-002), explicitly confirm the fact that no NAT resource is created.

### Pre-check

- Cost incurred: 0 (this Step is a non-creation confirmation).

### AWS Console work order

1. VPC → go to the NAT Gateways menu.
2. Confirm the list is empty. If there is a NAT Gateway created by other work, halt this spec and report to the operator.
3. EC2 → go to the Instances menu.
4. Confirm there is no instance with a Name of `nat-*` or `nat-instance-*`.
5. Explicitly record in the operator notes that this spec does not use NAT.

### Create-then-confirm

- NAT Gateways list is empty.
- No EC2 instance in a NAT role.
- The `NatGateway-Hours` line in AWS Billing is 0 (at this time).

### Action on failure

- On finding an unintended NAT resource, delete it immediately per operator decision (R-COST-002).
- If a decision change that NAT is required is needed, halt this runbook and start by updating OD-NET-001/002 in [`../_common/operator-decisions.md`](../_common/operator-decisions.md).

## Step 8. Create Route Tables and associate subnets [실행]

### Purpose

- Create public / app / data 3 types of Route Tables and associate them to each subnet.

### Pre-check

- Steps 4 ~ 7 complete.
- Privilege: `ec2:CreateRouteTable`, `ec2:CreateRoute`, `ec2:AssociateRouteTable`.
- Cost incurred: 0.

### Step 8-1. Create and associate rt-public

1. VPC → Route Tables → click Create route table.
2. Name: `rt-public`, VPC: `portfolio-vpc`. Tags: `tier=public`, `env=paper`. Create.
3. Select the created `rt-public` → Routes tab → Edit routes.
4. Add route: Destination `0.0.0.0/0`, Target: Internet Gateway → `portfolio-igw`. Save changes.
5. Subnet associations tab → Edit subnet associations → check `public-a`, `public-b` → Save associations.

### Step 8-2. Create and associate rt-app

1. Create route table. Name: `rt-app`, VPC: `portfolio-vpc`. Tags: `tier=app`, `env=paper`.
2. Keep only the default VPC local for Routes (do not add a 0.0.0.0/0 route).
3. Subnet associations → check `app-a`, `app-b` → Save associations.

### Step 8-3. Create and associate rt-data

1. Create route table. Name: `rt-data`, VPC: `portfolio-vpc`. Tags: `tier=data`, `env=paper`.
2. Keep only the default VPC local for Routes.
3. Subnet associations → check `data-a`, `data-b` → Save associations.

### Create-then-confirm

- 3 Route Tables `rt-public`, `rt-app`, `rt-data` in the Route Tables list.
- `0.0.0.0/0 → igw-...` exists in the Routes of `rt-public`.
- Only VPC local exists in the Routes of `rt-app`, `rt-data`.
- The associated Route Table of each subnet is connected as intended.

### Action on failure

- If a wrong subnet is associated, edit Subnet associations again.
- Attempting to add a 0.0.0.0/0 route while the IGW is not attached fails. Re-confirm from Step 6.
- rollback: delete the Route or the Route Table. The main route table cannot be deleted.

## Step 9. Create Security Groups [실행]

### Purpose

- Create 8 per-MS SGs and a VPC Endpoint-dedicated SG (start with empty rules → fill the rules in Step 10).

### Pre-check

- Step 4 complete.
- Privilege: `ec2:CreateSecurityGroup`, `ec2:AuthorizeSecurityGroupIngress`, `ec2:AuthorizeSecurityGroupEgress`.
- Cost incurred: 0.

### Step 9-1. Create SGs (name and description only)

Create each SG in the following form. All in VPC `portfolio-vpc`. Tags: `env=paper`.

1. `sg-marketconnector-ec2` — marketconnector EC2 + EIP.
2. `sg-port-view-ecs` — port-view ECS Fargate Service.
3. `sg-strategy-tasks` — decision / execution ECS Task.
4. `sg-crawler-tasks` — crawler ECS Task (public subnet placement candidate).
5. `sg-preprocessor-tasks` — preprocessor ECS Task (public subnet placement candidate).
6. `sg-research-batch` — research AWS Batch / ECS Task.
7. `sg-rds-postgres` — RDS PostgreSQL.
8. `sg-vpc-endpoints` — Interface VPC Endpoint dedicated.

Creation procedure (repeat):

1. VPC → Security Groups → click Create security group.
2. Security group name: one of the list above.
3. Description: briefly record the SG role (e.g., `marketconnector EC2 with KIS broker outbound`).
4. VPC: `portfolio-vpc`.
5. Keep Inbound rules / Outbound rules at defaults (do not fill in this Step).
6. Click Create security group.

### Create-then-confirm

- 8 SGs shown in the Security Groups list.
- All in VPC `portfolio-vpc`.
- SGs with an auto-created default outbound rule (0.0.0.0/0 all) are cleaned up in Step 10.

### Action on failure

- Name duplication error: if a same-name SG already exists, use the existing SG or decide a different name.
- rollback: an SG can be deleted immediately if it has no attached resource.

## Step 10. Fill Security Group rules [실행]

### Purpose

- Fill the inbound / outbound rules exactly as the design.md SG table. RDS SG inbound 0.0.0.0/0 is absolutely prohibited (R-SEC-001).

### Pre-check

- Step 9 complete.
- Privilege: `ec2:AuthorizeSecurityGroupIngress`, `ec2:AuthorizeSecurityGroupEgress`, `ec2:RevokeSecurityGroupEgress`.
- Cost incurred: 0.

### Step 10-1. sg-rds-postgres inbound (safe to fill first)

1. Select `sg-rds-postgres` → Edit inbound rules.
2. Add rule × 6 (allow 5432 with each SG as source):
   - Type PostgreSQL (5432), Source `sg-marketconnector-ec2`.
   - In the same form, `sg-port-view-ecs`, `sg-strategy-tasks`, `sg-crawler-tasks`, `sg-preprocessor-tasks`, `sg-research-batch`.
3. Save rules.
4. Leave Outbound rules empty (delete the default 0.0.0.0/0 outbound in Edit outbound rules).

### Step 10-2. sg-vpc-endpoints inbound

1. Select `sg-vpc-endpoints` → Edit inbound rules.
2. Add rule × 6 (TCP 443, source the 6 SGs above).
3. Save rules.
4. Leave Outbound rules empty.

### Step 10-3. sg-marketconnector-ec2

1. Inbound rules:
   - Type Custom TCP, Port 5000 (or an operator-decided port), Source `sg-port-view-ecs`.
2. Outbound rules:
   - Type HTTPS (443), Destination `0.0.0.0/0` (KIS broker).
   - Type PostgreSQL (5432), Destination `sg-rds-postgres`.
   - Type HTTPS (443), Destination `sg-vpc-endpoints`.
3. Save rules.

### Step 10-4. sg-port-view-ecs

1. Inbound rules: leave empty (comes in from the ALB SG when SSM port forwarding or a future ALB is introduced).
2. Outbound rules:
   - HTTPS (443) → `0.0.0.0/0` (Slack webhook, etc.).
   - Custom TCP (5000) → `sg-marketconnector-ec2`.
   - PostgreSQL (5432) → `sg-rds-postgres`.
   - HTTPS (443) → `sg-vpc-endpoints`.

### Step 10-5. sg-strategy-tasks

1. Inbound rules: leave empty.
2. Outbound rules:
   - Custom TCP (5000) → `sg-marketconnector-ec2`.
   - PostgreSQL (5432) → `sg-rds-postgres`.
   - HTTPS (443) → `sg-vpc-endpoints`.
   - HTTPS (443) → `0.0.0.0/0` (Slack, etc. if needed).

### Step 10-6. sg-crawler-tasks

1. Inbound rules: leave empty (absolutely disallowed).
2. Outbound rules:
   - HTTPS (443) → `0.0.0.0/0` (KRX/Naver/yfinance).
   - PostgreSQL (5432) → `sg-rds-postgres`.
   - HTTPS (443) → `sg-vpc-endpoints`.

### Step 10-7. sg-preprocessor-tasks

1. Inbound rules: leave empty.
2. Outbound rules:
   - HTTPS (443) → `0.0.0.0/0` (holiday API).
   - PostgreSQL (5432) → `sg-rds-postgres`.
   - HTTPS (443) → `sg-vpc-endpoints`.

### Step 10-8. sg-research-batch

1. Inbound rules: leave empty.
2. Outbound rules:
   - PostgreSQL (5432) → `sg-rds-postgres`.
   - HTTPS (443) → `sg-vpc-endpoints`.
   - HTTPS (443) → `0.0.0.0/0` (few actual calls thanks to S3 Gateway use. Keep if needed).

### Create-then-confirm

- The inbound / outbound rules of each SG match the table above.
- No 0.0.0.0/0 in `sg-rds-postgres` inbound. If a 0/0 inbound rule exists, remove it immediately.
- `sg-crawler-tasks`, `sg-preprocessor-tasks` inbound are empty (prevent external exposure on public subnet placement, R-NET-001).

### Action on failure

- SG reference cycle error: it is normal for `sg-port-view-ecs` to be in `sg-marketconnector-ec2` outbound and `sg-marketconnector-ec2` inbound to be `sg-port-view-ecs`. The cycle itself is allowed in SG references.
- If 0.0.0.0/0 was wrongly entered in RDS inbound, Revoke immediately. Audit the calls in CloudTrail.
- rollback: Revoke only the wrongly added rule.

## Step 11. Create VPC Endpoints [실행]

### Purpose

- Enable using the ECR / S3 / Secrets Manager / SSM / CloudWatch Logs APIs via a private path in a NAT-free environment.

### Pre-check

- Steps 8, 9, 10 complete.
- Privilege: `ec2:CreateVpcEndpoint`.
- Cost incurred: Gateway endpoint 0. Interface endpoint ~$8/month per AZ + data processing (see R-NET-003 / R-COST-001). approval required.

### Step 11-1. S3 Gateway Endpoint

1. VPC → Endpoints → Create endpoint.
2. Name tag: `vpce-s3-gw`.
3. Service category: AWS services.
4. Service name: `com.amazonaws.ap-northeast-2.s3` (Type: Gateway).
5. VPC: `portfolio-vpc`.
6. Route tables: check `rt-app`, `rt-data`.
7. Policy: Full access (default).
8. Tags: `env=paper`.
9. Create endpoint.

### Step 11-2. ECR API Interface Endpoint

1. Create endpoint.
2. Name tag: `vpce-ecr-api`.
3. Service name: `com.amazonaws.ap-northeast-2.ecr.api` (Type: Interface).
4. VPC: `portfolio-vpc`.
5. Subnets: check `app-a`, `app-b`.
6. Security groups: select `sg-vpc-endpoints`.
7. Check Enable DNS name (private DNS).
8. Policy: Full access (default).
9. Create endpoint.

### Step 11-3. ECR DKR Interface Endpoint

1. Create endpoint.
2. Name tag: `vpce-ecr-dkr`.
3. Service name: `com.amazonaws.ap-northeast-2.ecr.dkr` (Interface).
4. Same input as Step 11-2 for the rest.

### Step 11-4. Secrets Manager Interface Endpoint

1. Create endpoint.
2. Name tag: `vpce-secretsmanager`.
3. Service name: `com.amazonaws.ap-northeast-2.secretsmanager` (Interface).
4. Same input for the rest.

### Step 11-5. SSM Interface Endpoint

1. Create endpoint.
2. Name tag: `vpce-ssm`.
3. Service name: `com.amazonaws.ap-northeast-2.ssm` (Interface).
4. Same input for the rest.

This Step covers only SSM Session Manager operator access / Parameter Store. Per operator decision, `ssmmessages`, `ec2messages` can be added.

### Step 11-6. CloudWatch Logs Interface Endpoint

1. Create endpoint.
2. Name tag: `vpce-logs`.
3. Service name: `com.amazonaws.ap-northeast-2.logs` (Interface).
4. Same input for the rest.

### Create-then-confirm

- 6 in the Endpoints list (S3 1 + Interface 5).
- Each Interface endpoint State = `Available`, Private DNS = `enabled`, both `app-a` / `app-b` included in AZ.
- The S3 endpoint is added to the Routes of `rt-app`, `rt-data`.

### Action on failure

- `Endpoint did not stabilize` error: the subnet AZ may not be a service-available AZ. Retry with a subnet in a different AZ.
- If an ECS Task fails to ECR pull, first inspect this Step 11-2 / 11-3 and SG inbound 443 (`sg-vpc-endpoints`).
- rollback: delete the Endpoint. Since AWS APIs are blocked without the endpoint in a NAT-free environment, delete during operation only after stopping ECS Tasks.

## Step 12. Create RDS Subnet Group [실행]

### Purpose

- Create the subnet bundle where the RDS instance will be placed.

### Pre-check

- Step 5 complete (`data-a`, `data-b`).
- Privilege: `rds:CreateDBSubnetGroup`.
- Cost incurred: 0.

### AWS Console work order

1. AWS Console → RDS → Subnet groups → Create DB subnet group.
2. Name: `portfolio-paper-subnet-group`.
3. Description: `Portfolio paper RDS subnet group (data-a, data-b)`.
4. VPC: `portfolio-vpc`.
5. Add subnets: AZ `ap-northeast-2a` → `data-a`, AZ `ap-northeast-2c` → `data-b`.
6. Create.

### Create-then-confirm

- `portfolio-paper-subnet-group` shown in the Subnet groups list. Status = `Complete`.
- 2 subnets included in the Subnets column.

### Action on failure

- subnet AZ shortage error: re-confirm in Step 5 that both subnets were created as the `data` tier.
- rollback: the subnet group cannot be deleted if an RDS instance is using it. If unused, delete immediately.

## Step 13. Create RDS Parameter Group [실행]

### Purpose

- Create the aws-paper RDS PostgreSQL 16 parameter group `pg-portfolio-paper` and apply the recommended values.

### Pre-check

- Cost incurred: 0.

### AWS Console work order

1. RDS → Parameter groups → Create parameter group.
2. Parameter group family: `postgres16`.
3. Type: `DB Parameter Group`.
4. Group name: `pg-portfolio-paper`.
5. Description: `Portfolio paper PostgreSQL 16 parameters`.
6. Create.
7. Select the created group → apply the following values via Edit parameters:
   - `rds.force_ssl` = `1`
   - `log_min_duration_statement` = `1000`
   - `log_lock_waits` = `1`
   - `idle_in_transaction_session_timeout` = `60000`
8. Save changes.

### Create-then-confirm

- `pg-portfolio-paper` shown in the Parameter groups list.
- The above 4 parameter values are applied (static parameters are reflected on RDS reboot, dynamic ones immediately).

### Action on failure

- If the parameter family is wrongly selected, delete the group and recreate.
- rollback: the parameter group can be deleted if not referenced by an instance.

## Step 14. Create RDS PostgreSQL instance [실행]

### Purpose

- Create the aws-paper portfolio RDS instance (approval required, large cost impact).

### Pre-check

- Steps 12, 13 complete.
- The DB master password must be pre-registered in Secrets Manager `/portfolio/paper/rds/master` (Step 14-0). At registration, only the operator enters the actual value, and this document has only `[REDACTED]`.
- Privilege: `rds:CreateDBInstance`, `secretsmanager:GetSecretValue` (later application).
- Cost incurred: `db.t4g.small` single-AZ ~$26/month + storage ~$7/month + backup. Check R-COST-001 / R-COST-002.

### Step 14-0. (prerequisite) Register the master password in Secrets Manager

1. AWS Console → Secrets Manager → Store a new secret.
2. Secret type: `Other type of secret`.
3. Plaintext: `[REDACTED]` (entered directly by the operator. Do not write it in this document).
4. Encryption key: `aws/secretsmanager` (KMS default).
5. Secret name: `/portfolio/paper/rds/master`.
6. Tags: `env=paper`, `kind=rds-master`.
7. Automatic rotation: inactive in this spec. Decided in the 06 spec.
8. Store.

This Step overlaps with the 06 spec, so it can be omitted if 06 was done first.

### Step 14-1. Create RDS

1. AWS Console → RDS → Databases → Create database.
2. Choose creation method: `Standard create`.
3. Engine type: `PostgreSQL`. Version: latest `PostgreSQL 16.x` minor.
4. Templates: `Production` or `Dev/Test` (operator decision. Dev/Test is possible since this spec is paper).
5. Settings:
   - DB instance identifier: `portfolio-paper-rds`.
   - Master username: `portfolio_admin`.
   - Master password: enter the Secrets Manager `/portfolio/paper/rds/master` value directly (`[REDACTED]`). Enter it so it is not visible in the Console.
6. Instance configuration: `db.t4g.small`.
7. Storage:
   - Storage type: `gp3`.
   - Allocated storage: `50 GB`.
   - Storage autoscaling: recommended inactive (activate if needed, operator decision).
8. Availability & durability: `Single-AZ DB instance deployment` (paper decision).
9. Connectivity:
   - VPC: `portfolio-vpc`.
   - DB subnet group: `portfolio-paper-subnet-group`.
   - Public access: `No`.
   - VPC security group: select `sg-rds-postgres`.
   - Availability Zone: `ap-northeast-2a` (or operator decision).
   - Database port: `5432`.
10. Database authentication: `Password authentication` (IAM authentication is reviewed in the 06 spec).
11. Monitoring: Performance Insights `Enabled`. Retention 7 days.
12. Additional configuration:
    - Initial database name: leave empty in this step (create the `portfolio` DB in Step 16).
    - DB parameter group: `pg-portfolio-paper`.
    - Backup retention period: `7 days`.
    - Backup window: an off-operation time window (e.g., `19:00-19:30 UTC`).
    - Encryption: Enable encryption (KMS default).
    - Maintenance window: a recommended time window.
    - Deletion protection: `Enable` (prevent accidental deletion).
13. Click Create database.

### Create-then-confirm

- `portfolio-paper-rds` Status = `Available` in the Databases list (takes several minutes).
- Confirm the Endpoint URL and record it in the operator notes (`INTEREST_DB_HOST`).
- The VPC, Subnet group, SG shown values match the intent.
- Confirm Backup window / retention / PITR active.
- Deletion protection On.

### Action on failure

- subnet group AZ shortage error: re-confirm that both data-a, data-b are in the group.
- SG missing error: if `sg-rds-postgres` selection is missed, RDS inbound 5432 is blocked. Change the SG immediately via Modify.
- rollback: Delete the instance. If Deletion protection is on, disable it first, then delete. On deletion, confirm the final snapshot creation option.

## Step 15. Confirm RDS connection security / Parameter / Option [확인]

### Purpose

- Validate that the created RDS's connection security / parameter group / SG connection / publicly accessible state match the intent.

### Pre-check

- Step 14 complete.
- Cost incurred: 0.

### AWS Console work order

1. RDS → Databases → `portfolio-paper-rds` details.
2. Connectivity & security tab:
   - Record Endpoint / Port.
   - VPC = `portfolio-vpc`.
   - Subnet group = `portfolio-paper-subnet-group`.
   - SG = `sg-rds-postgres`.
   - Publicly accessible = `No`.
3. Configuration tab:
   - DB parameter group = `pg-portfolio-paper`.
   - Status = `in-sync` (static parameters are reflected after reboot).
4. Maintenance & backups tab:
   - Automated backups = `Enabled`, retention = 7 days.
   - PITR = `Enabled`.
5. Monitoring tab:
   - Performance Insights = `Enabled`.

### Create-then-confirm

- All the above values are shown as intended.
- `Publicly accessible = No` (R-SEC-001 core).

### Action on failure

- If wrongly created with Publicly accessible = Yes, change it to No immediately via Modify + `Apply immediately`. Audit external connection logs.
- If the parameter group is not applied, change it in Modify then reboot.

## Step 16. Prepare DB / schema / role creation (execution out of scope for this spec) [준비]

### Purpose

- Prepare the SQL to create the `portfolio` DB / 10 schemas / 7 roles. In this spec, SQL execution is performed directly by the operator, and actual execution is integrated with the 03 / 06 specs.

### Pre-check

- Step 14 complete, RDS endpoint obtained.
- An environment where psql connection is possible from an EC2 or ECS Task within the same VPC via SSM Session Manager. Never open SSH 22 (OD-NET-009).
- Cost incurred: SQL execution itself is 0 (only the connecting EC2 / ECS usage).

### AWS Console work order

1. (operator direct) Connect via SSM Session Manager to an operator client within the same VPC (e.g., a temporary EC2 or ECS Task).
2. Connect the `portfolio_admin` account with the psql command (the password is queried directly only by the operator from Secrets Manager).
3. Prepare the following SQL in the operator notes (actual execution is after this runbook is validated, at the 03 / 06 spec integration time):
   - `CREATE DATABASE portfolio;`
   - 10 schemas: `CREATE SCHEMA IF NOT EXISTS reference;` and 9 others (`interest`, `preprocessor`, `research`, `decision`, `execution`, `connector`, `ops`, `legacy`, `public`).
   - 7 roles: `CREATE ROLE marketconnector_app LOGIN PASSWORD '[REDACTED]';` and 6 others.
   - per-role schema privileges: GRANT exactly per the design.md "per-schema privilege matrix".
   - per-role search_path: `ALTER ROLE <role> SET search_path = ...;` exactly per the 8 MS README definitions (R-DATA-001).
4. Keep the SQL file only on the operator's local (repo commit prohibited). The password slot is `[REDACTED]`.

### Create-then-confirm

- The SQL is prepared in the operator notes.
- Actual DB / schema / role creation is delegated to a step outside this runbook.

### Action on failure

- Do not execute actual SQL in this Step. If accidental execution occurs, the operator immediately rolls back the transaction or drops the schema/role and retries.

## Step 17. pg_dump / pg_restore preparation [준비]

### Purpose

- Agree in advance on the dump / restore procedure for the local-dev → aws-paper cutover. Actual cutover is [`../10-cutover-and-validation-runbook`](../10-cutover-and-validation-runbook) (planned).

### Pre-check

- Step 14, 16 SQL preparation complete.
- Cost incurred: dump / restore itself is 0 (only the operator client + data transfer).

### AWS Console work order

This Step is a procedure agreement.

1. Agree on the local-dev PostgreSQL usable time point (a time window when broker order / fill sync / intraday monitor are stopped).
2. Agree on the dump command format in the operator notes:
   - `pg_dump --format=custom --no-owner --no-privileges -h <local-host> -U <local-user> -d portfolio -f portfolio.dump`
   - The password is entered directly by the operator. This document has `[REDACTED]`.
3. Agree on the restore command format:
   - `pg_restore --no-owner --no-privileges --jobs=4 -h <rds-endpoint> -U portfolio_admin -d portfolio portfolio.dump`
4. Decide the dump file storage location (temporary EC2 EBS, S3 bucket). Agree on the dump file deletion policy after work completion.
5. The validation SQL uses the design.md `Validation SQL Candidates` section.
6. The Rollback criterion references the design.md `Rollback Criteria` section + R-DATA-002 mitigation.

### Create-then-confirm

- The dump / restore command format and timing, storage / deletion policy are recorded in the operator notes.
- Actual dump / restore is not executed in this spec.

### Action on failure

- This Step is not an execution stage, so there is no failure scenario. Actual cutover proceeds in a separate spec.

## Step 18. Final validation [준비]

### Purpose

- Check whether the results of Step 4 ~ 15 all pass the criteria of [`./validation-checklist.md`](./validation-checklist.md).

### Pre-check

- Steps 0 ~ 15 complete.
- Cost incurred: 0 (check).

### AWS Console work order

1. Open [`./validation-checklist.md`](./validation-checklist.md) and check each item directly.
2. For items that do not pass, go back to the corresponding Step of this runbook and rework.
3. When all items pass, record `02 spec runbook passed` in the operator notes.

### Create-then-confirm

- All validation-checklist items checked.
- A pass record exists in the operator notes.

### Action on failure

- Rework Step 4 ~ 15 per item that did not pass.
- If cost / security items do not pass, stop this runbook and start by reviewing [`../_common/operator-decisions.md`](../_common/operator-decisions.md).

## Step 19. Rollback order (if needed) [복구]

This spec's standalone rollback procedure. cutover rollback is integrated in [`../10-cutover-and-validation-runbook`](../10-cutover-and-validation-runbook) (planned).

### 19-1. Delete RDS instance

1. RDS → Databases → `portfolio-paper-rds`.
2. Disable Deletion protection in Modify → Apply immediately.
3. Delete.
4. The Final snapshot option is an operator decision. Create a snapshot if preserving evidence.

### 19-2. Delete RDS Parameter Group / Subnet Group

1. Parameter groups → delete `pg-portfolio-paper` (in an instance-unreferenced state).
2. Subnet groups → delete `portfolio-paper-subnet-group`.

### 19-3. Delete VPC Endpoints

1. Delete all 6 endpoints in Endpoints.

### 19-4. Delete Security Groups

1. Delete the 8 SGs in Security Groups (confirm no other resource references them).
2. If RDS / Endpoint references remain, clean them up first.

### 19-5. Delete Route Tables

1. Release Subnet associations.
2. Delete `rt-public`, `rt-app`, `rt-data`.

### 19-6. Internet Gateway detach / delete

1. Internet Gateways → `portfolio-igw` → Detach from VPC.
2. Delete.

### 19-7. Delete Subnets

1. Delete all 6 in Subnets.

### 19-8. Delete VPC

1. Your VPCs → `portfolio-vpc` → Delete VPC.

### Rollback validation

- `portfolio-vpc`, `portfolio-paper-rds`, the 6 subnets, 8 SGs, 6 endpoints all no longer exist in the VPC console.
- The lines related to this spec end in the daily cost graph of AWS Billing.

This Step does not revert the IAM `portadmin` user / policy / Root security hardening. If the portadmin user itself needs to be removed, handle it only by a separate decision.

## Appendix A. RDS Restore Runner & DB Role / privilege application lessons (based on 2026-06-09 results)

This appendix is a short collection of lessons for reusing, in follow-up operations, the actual Local PostgreSQL → aws-paper RDS migration and DB Role / privilege first application that the operator performed on 2026-06-09. It does not change the decision values / existing Step numbers. See [`./operation-notes.md`](./operation-notes.md) and [`./db-roles-and-grants.md`](./db-roles-and-grants.md) for detailed execution records.

### A-1. Avoid PostgreSQL major version mismatch

- Before starting the dump, confirm the dump source major version with `pg_dump --version` and, for the dump file, the first line of `pg_restore --list` or `head -c` metadata.
- If it differs from the RDS engine major version, align the RDS major version to at least the dump source before restore (R-DATA-003).
- 2026-06-09 result: dump source PostgreSQL 18.1 → existing RDS PG 16.14 incompatible → proceeded after recreating RDS as PG 18.4.

### A-2. Private RDS is accessed only via EC2 / SSM

- To maintain the RDS Public access = No policy (R-SEC-001), do not allow the local PC IP directly in the RDS SG.
- The restore runner uses an EC2 placed in the same VPC (e.g., the aws-paper MarketConnector EC2) or a temporary EC2. Operator access prioritizes EC2 Instance Connect or SSM Session Manager (OD-NET-009).
- 2026-06-09 result: used the aws-paper MarketConnector EC2 (Amazon Linux 2023, public subnet, EIP attach) as the restore runner. Transferred the dump file via the local PC → S3 temporary bucket → EC2 → private RDS path.

### A-3. PostgreSQL client / dump archive compatibility

- For pg_restore archive format compatibility, align the client version to at least the dump source.
- 2026-06-09 result: first confirmed an archive header version mismatch on a PG 15.18 client → worked normally after switching to an 18.4 client.

### A-4. Handling dump owner role vs RDS role mismatch

- If the dump's object owner is a role that does not exist in RDS, the restore stops with a `role "X" does not exist` error (R-DATA-004).
- Recommended option: with `pg_restore --no-owner --no-privileges`, restore only the data ignoring owner / privilege information, then reconstruct owner / privileges with the [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §4 SQL.
- 2026-06-09 result: first attempt `role "postgres" does not exist` → succeeded by re-running with `--no-owner --no-privileges` after DB drop / recreate.

### A-5. Decide owner transfer separately at the schema unit and object unit

- Per the [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §4.2 procedure of this spec, create `portfolio_owner` → grant membership to `portfolio_admin` → transfer the owner of the 9 domain schemas to `portfolio_owner` (public is not changed).
- The owner of existing tables / sequences / indexes may not have `REASSIGN OWNED BY portfolio_admin TO portfolio_owner` executed in the first application, by a separate decision (OD-DB-010). In that case, §4.5 default privileges apply automatically only to new objects, and existing objects operate with only the §4.4 explicit GRANT applied.
- 2026-06-09 result: schema owner transfer complete, existing object owner kept as `portfolio_admin`, REASSIGN not executed. Managed separately by a follow-up decision on whether to transfer all at once.

### A-6. Normalize line breaks when comparing for consistency validation

- When diffing the local and RDS row count CSV / object count snapshot, false-positives frequently occur due to CRLF / LF differences.
- A comparison with normalized line breaks such as `diff --strip-trailing-cr` or `git diff --ignore-cr-at-eol` is recommended.
- 2026-06-09 result: schema / table / index / sequence / FK / trigger / row count all diff 0 after normalized comparison.

This appendix's operational results are used as input when writing the 03 / 06 specs and [`../10-cutover-and-validation-runbook`](../10-cutover-and-validation-runbook) (planned).

## This runbook's work safety constraints

- Actual AWS resource creation / change is performed directly by the operator. This document is only procedure guidance and does not execute automatically.
- Do not modify the 8 MS code / README / AGENTS.md / CHANGELOG / docs / worklog.
- Do not call broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / order / fill / Daily Batch / intraday monitor.
- All secrets use only `[REDACTED]` or a placeholder. Do not record the portadmin password / MFA serial / backup codes in plaintext anywhere in this document / notes.
- Do not change decision values arbitrarily. If a change is needed, record only the change proposal in [`../_common/operator-decisions.md`](../_common/operator-decisions.md).
