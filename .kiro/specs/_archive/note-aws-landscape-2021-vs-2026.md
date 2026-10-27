# The AWS Landscape, 2021 → 2026: What Changed and Why ECS Fargate Is the Answer This Time

> A one-off retrospective note. Not a spec deliverable. Independent of this project's decisions, it explains why the 2021 portfolio the operator built by hand and the 2026 recommendation in this spec diverge. All cost figures are approximate; exact unit prices require confirmation in the AWS Pricing Calculator. Secrets use `[REDACTED]` only.

## 1. The Intent of the 2021 Portfolio (Reconfirmed)

Read the attached 2021 configuration as-is and it is **an intentionally diverse catalog**.

| MS | Compute | CI/CD Tooling | Intent |
|----|---------|------------|------|
| API Communication | EC2 (Windows Server 2016) | CodeDeploy + CodePipeline + S3 | Workloads with strong OS binding, like broker / Creon Plus |
| Interest Me | Lambda + API Gateway + Slack | CodeBuild + CodePipeline | One-shot trigger function |
| Interest Agency | Lambda + CloudWatch + BeautifulSoup | CodeCommit + CodeBuild + CodePipeline | cron crawling |
| Interest News | Lambda + Cloud9 + Selenium / BS4 | CloudFormation + CodePipeline | IaC learning |
| Strategy Algorithm | Fargate + ECS Blue/Green + ALB | CodeCommit + ECR + Docker + CodePipeline | Container + zero-downtime deployment |
| Strategy LSTM | EC2 + EKS + Flask | CodeCommit + ECR + CodeBuild + CodePipeline | Kubernetes learning |
| View | Elastic Beanstalk + Spring Boot + Java 8 | CodeCommit + Maven + CodeBuild + EB | PaaS |

The essence of this configuration is that it "showed EC2 / Lambda / Fargate / EKS / Beanstalk plus 5 kinds of CI/CD all within one system." From an interview / portfolio perspective it was a clever choice, and it is genuinely a good catalog for demonstrating AWS certifications and operational experience at the same time.

That said, in terms of operational consistency, debugging consistency, and single-operator burden, the inefficiency was large. A résumé catalog and an operational system are judged by different criteria.

## 2. From 2021 to 2026, What Actually Changed in AWS

The impression that "almost nothing changed" is partly correct. But pinned down concretely, the change has **accumulated at the periphery**. Gathering only the substantial items:

### Compute / Containers

- Lambda: Container image support (late 2020, in real use from 2021), 10GB memory, VPC cold-start relief through ENI reuse. SnapStart (2022, Java only) brought Spring Boot Lambda into the realm of the feasible. However, **the 15-minute timeout is unchanged**.
- ECS Fargate: Spot pricing (2022), ARM Graviton2/3 support, ECS Exec (SSM-based shell), capacity providers, faster task startup.
- EKS: Fargate profiles stabilized, EKS Auto Mode (2024) absorbed some of the node-management burden. But **the control-plane hourly rate and the addon operational burden are essentially unchanged**.
- App Runner (2021): Its introduction itself is a change. A single-container PaaS. VPC connector (2022) enabled RDS access, but its operational flexibility does not keep up with ECS.
- Lambda Function URL (2022) and Lambda Adapter (2022) raised the container → Lambda migration friendliness.

### Orchestration / Eventing

- **EventBridge Scheduler (2022)**: The very service this project uses directly for the Daily Batch trigger. In 2021 there was little suitable beyond using CloudWatch Events rules with cron expressions.
- Step Functions Distributed Map (2022): Can parallelize tens to tens of thousands of steps. Potentially useful for backtest parameter sweeps.
- VPC Lattice (2023): Abstraction of VPC-to-VPC / service-to-service communication. Overkill for a single-operator setup.
- AWS Verified Access (2023): ZTNA without a VPN. Usable for operator console access.

### Security / IAM

- IAM Roles Anywhere (2022): External workloads (for example, an on-premises PC or GitHub Actions) can also use an IAM Role as temporary credentials. As a result, the OIDC flow became standard.
- GitHub Actions OIDC (late 2021): The approach of receiving short-lived tokens instead of embedding an IAM access key in CI became standardized. This project also recommends this approach.
- Expansion of Secrets Manager automatic rotation tooling.

### Network / Data

- IPv6-only VPC (2023), AWS PrivateLink for SaaS, stabilized cross-VPC peering.
- RDS Optimized Reads / Writes, Aurora Serverless v2 (2022) — on a baseline of existing RDS PostgreSQL, the operational burden is almost the same.
- There has been almost no trend of VPC Endpoint price reductions. Still around ~$8/month per AZ.

### Developer Tools

- AWS Q Developer (formerly CodeWhisperer) — IDE-integrated code assistant.
- AWS Copilot CLI stabilized — automation of the standard ECS deployment pattern.
- CDK v2 and AWS SAM stabilized — reduced barrier to entry for IaC.

### Impact on the Korean Market

- New services arrive faster in the Seoul region (ap-northeast-2). Items that were US-region-first in 2021 now launch almost simultaneously.
- Expansion of AWS Outposts / Local Zones — no direct relation to this project.

### In Short

- What changed: operational convenience (SnapStart, ECS Exec, Copilot, Q Developer), new orchestration services (EventBridge Scheduler, Step Functions Distributed Map), a security standard (OIDC + IAM Roles Anywhere), and added PaaS options such as App Runner.
- What did not change: the VPC / Subnet / SG / Route Table / IGW / NAT model, the IAM Role / Policy structure, the operational responsibility boundary of RDS, the Lambda 15-minute timeout, the EKS control-plane burden, and **the three-way compute taxonomy (VM / container / function) itself**.

## 3. So Why Is the Impression of "Almost No Change" So Strong

This is not AWS being lazy; it is a signal that the cloud platform has **entered maturity**. Pinning down the structural reasons:

### 1) The Three-Way Compute Taxonomy Is Close to Physics

- "Long-running stateful instance" (VM/EC2), "packaged workload run one-shot or continuously" (container), "one-shot stateless function" (FaaS). These three have fundamentally different workload patterns, and there is little room for a new category to squeeze in.
- New products like App Runner / Lightsail Containers / Lambda container images are, in the end, **convenience wrappers** around the three above.

### 2) Compatibility and Lock-in Constrain Innovation

- What AWS does best is **not breaking existing APIs**. boto3 / CloudFormation written 5 years ago still runs almost as-is today. This compatibility is the very basis of user trust, and at the same time it makes large structural change difficult.
- After Kubernetes effectively standardized the container orchestration market, AWS also bet on EKS, but it does not turn ECS off. It accepts the cost of maintaining both.

### 3) The Essence of Operational Cost Is People, Not Technology

- Even in 2021, "EKS + Beanstalk + 5 kinds of CI/CD" was a heavy combination for a single operator.
- In 2026 it is still a heavy combination.
- Operational burden is something AWS can reduce, but it does not reach zero. In the end a single operator converges on one or two standards.

### 4) The Basic Model of Network / Security Does Not Change

- VPC / Subnet / Route Table / SG are effectively the oxygen of IaaS. Change these and compatibility breaks.
- IAM is the same. New permission expressions (IAM Identity Center, ABAC) arrived, but the IAM Policy JSON itself is almost unchanged.

### 5) Most "New Products" Are Repackagings of Existing Services

- App Runner = a repackaging of ECS Fargate + ALB + automatic build.
- AWS Copilot = automation of the standard ECS + CloudFormation + ECR pattern.
- Lambda container image = an adapter that runs an ECR image as Lambda.
- EKS Auto Mode = EKS + some node-operations automation.
- In other words, **the catalog grew but the core building blocks are the same**.

These five combine to create the felt sense that "almost nothing changed in 5 years." At the same time, small changes accumulated so that **the operational model became meaningfully simpler** (for example, GitHub Actions OIDC, EventBridge Scheduler, ECS Exec, Q Developer).

## 4. So Why Is ECS Fargate the Recommendation This Time

The reason the 2021 diversity catalog is not the 2026 answer is that **the objective changed**.

| Item | 2021 Portfolio | 2026 This Project |
|------|----------------|-----------------|
| Primary goal | Show breadth of the AWS service catalog | Operational stability of 8 MS + safety of automated trading |
| Operations headcount | Learning / single person | Single person, immigration portfolio + live service at the same time |
| Risk | Learning cost | Live broker automatic-retry incident |
| Validation priority | Experience operating diverse stacks | Stabilization within a short cutover window |
| Evaluation point | One-shot demo | Long-term operation (paper N business days + live) |

The reason ECS Fargate became the first choice for 6 of the 8 MS compute workloads is simple.

- As a container standard it has weak vendor lock-in (no dependence on a Beanstalk runtime / Lambda runtime / App Runner runtime).
- There is no worker-node operational burden (the biggest difference from EKS).
- Per-task billing keeps idle cost small (the biggest difference from EC2 24/7).
- It has first-class integration with Step Functions / EventBridge Scheduler / ECS RunTask — this matches exactly the Daily Batch / intraday monitor of this project.
- IAM Task Role + Secrets Manager + SSM Parameter Store + CloudWatch Logs come attached as a **default set**, so specs 06 / 09 / 10 of this project all resolve with the same pattern.
- ECS Exec (SSM-based) lets you get inside the container to debug — lowering operational burden further.
- The floor at which single-operator operation is possible. At the same time, because it is the most common standard even when moving to another company, career transferability is high.

The reasons the other candidates are not the first choice are laid out in tables in chapters 4 / 7 / 8 / 9 of this project's `ms-aws-service-decision-matrix.md`, so here just one line each:

- Lambda: In Daily Batch measurements, crawler at about 9 minutes 53 seconds / preprocessor at about 5 minutes 54 seconds is in the danger zone of the 15-minute timeout. On top of that, it is hard to enforce the policy of forbidding live BUY/SELL automatic retries at the infrastructure level.
- EKS: The control-plane hourly rate (~$73/month) + addon operational burden is overkill for single-operator operation.
- Elastic Beanstalk: It moves away from the container standard and is not natural with the flow of moving the Daily Batch to Step Functions. It was good 5 years ago but is now a case that ceded its place to ECS Fargate.
- App Runner: Low VPC flexibility, and its external outbound IP control does not fit the broker IP registration policy.
- EC2: The first choice only when there is a stateful constraint like marketconnector's broker IP registration + single token. Other workloads take a 24/7 idle-cost loss.

## 5. Diversity Is Still Alive — the Substance of This Recommendation

It looks like a single "ECS Fargate card," but simply following this spec's operational-stability-first decisions naturally brings in the following categories.

- Compute: **EC2** (marketconnector) + **ECS Fargate Service** (port-view) + **ECS Fargate Task** (crawler / preprocessor / decision / execution) + **AWS Batch** (research)
- Orchestration: **Step Functions** + **EventBridge Scheduler** + **ECS RunTask**
- Auxiliary: **Lambda** (infrastructure alarm fan-out only) + **SNS** + **CloudWatch Logs / Metrics / Alarms**
- Security: **Secrets Manager** + **SSM Parameter Store** + **IAM Role / Policy** + **KMS**
- Data: **RDS for PostgreSQL** + **S3**
- Network: **VPC** + Subnet + IGW + **VPC Endpoint** + Security Group + **Elastic IP** + (optional) **ALB**
- Deployment / registry: **ECR** + **GitHub Actions OIDC**
- Operational access: **SSM Session Manager** (SSH not used)

This is not short of the 2021 catalog, and operational consistency is far higher. EKS can be separated into a follow-up optional track and left as "a separate track if more portfolio appeal is needed" (chapter 7 of this spec).

## 6. One-Line Conclusion

Even after 5 years, the **building blocks** of AWS are almost the same. That is why the change looks small. But the standard for **how you assemble the building blocks** changed. The 2021 answer was "diversity catalog," and the 2026 answer is "put ECS Fargate at the center and mix EC2 / Batch / Step Functions / EventBridge / Lambda (auxiliary) sensibly." The reason the answer differs for the same AWS is not that AWS changed, but that the operator's **objective** shifted from learning to operation.

## 7. Safety Constraints for This Note

- A one-off retrospective note. Not a spec deliverable. No other spec document references or depends on this note.
- No actual AWS resource creation / IaC authoring / modification of the 8 MS code.
- This spec's recommendations (`ms-aws-service-decision-matrix.md` chapter 5 / `operator-decisions.md` chapter 9 OD-MS-001 ~ OD-MS-010) are not changed by this note.
- All cost figures are approximate; exact values require confirmation in the AWS Pricing Calculator.
- All secrets are noted only as `[REDACTED]`.
