# Runbook — 02-aws-network-and-rds

본 문서는 운영자가 AWS Console에서 순서대로 따라 할 수 있는 실행 절차서다. 본 spec(02) 1차 적용 환경은 `aws-paper`이며, `aws-live`는 [`../10-cutover-and-validation-runbook`](../10-cutover-and-validation-runbook)(예정)에서 통합한다.

본 runbook은 실제 AWS 리소스를 자동으로 만들지 않는다. 운영자가 한 단계씩 직접 클릭하고, 각 Step의 `생성 후 확인`까지 통과한 다음 다음 Step으로 진행한다.

선행 입력

- [`./requirements.md`](./requirements.md), [`./design.md`](./design.md), [`./tasks.md`](./tasks.md), [`./decision-matrix.md`](./decision-matrix.md)
- [`../_common/operator-decisions.md`](../_common/operator-decisions.md), [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md), [`../_common/cost-simulation.md`](../_common/cost-simulation.md), [`../_common/risk-register.md`](../_common/risk-register.md)
- [`./validation-checklist.md`](./validation-checklist.md), [`./traceability-matrix.md`](./traceability-matrix.md)

보안 / 안전 원칙

- 모든 secret은 `[REDACTED]` 또는 placeholder만 사용. 실제 값은 본 문서, AWS 콘솔 화면 캡처, 운영자 노트 어디에도 적지 않는다.
- 8개 MS의 소스 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 금지.
- broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / 주문 / 체결 / Daily Batch / intraday monitor 호출 금지.
- 실제 cutover dump / restore는 본 spec 범위 밖. 본 runbook은 사전 준비까지만 진행한다.

## Step 라벨

각 Step 제목 옆에 다음 한글 라벨을 표시한다. 운영자가 한눈에 실행 성격을 구분할 수 있도록 하기 위함이며, 라벨이 절차나 결정값을 바꾸지 않는다.

- `[실행]` 실제 AWS Console에서 리소스를 생성하거나 변경하는 단계.
- `[확인]` 리소스 생성 없이 결정 / 콘솔 상태 / 비용 라인을 확인만 하는 단계.
- `[준비]` 후속 spec(03 / 06 / 10 등)의 실행 시점에 사용할 절차 / SQL / 명령을 합의 / 기록만 하는 단계.
- `[복구]` 장애 / 중단 / 검증 실패 시에만 수행하는 rollback 단계.

## Step 0. IAM 관리자 사용자 portadmin 생성 [실행]

### 목적

- Root 계정으로만 로그인 가능한 신규 AWS 계정에 후속 spec 모든 단계를 수행할 수 있는 IAM 관리자 사용자 `portadmin`을 만든다. 이후 모든 작업은 `portadmin`으로 진행하고 Root 계정은 비상용으로만 보관한다.

### 사전 확인

- 현재 Root 계정 로그인 상태(콘솔 우측 상단에 root user / account-id 표시).
- Root 계정 MFA 활성 여부 확인(미활성이면 본 Step 진행 중 또는 직후 활성 권고).
- 비용 발생 여부: 0 (IAM 자체 비용 없음).
- 권한: Root 계정만 수행 가능.
- 운영자 노트 준비: account-id, 콘솔 sign-in URL, MFA 등록 시점은 운영자 노트에 기록한다. portadmin 비밀번호는 본 문서 / 노트 어디에도 적지 않는다(`[REDACTED]`만).

### Step 0-1. portadmin 사용자 생성

1. AWS Console(Root 로그인 상태) → IAM 메뉴로 이동.
2. Users → Create user 클릭.
3. 입력값:
   - User name: `portadmin`
4. 선택값:
   - Provide user access to the AWS Management Console: 체크.
   - Console password: `Custom password` 선택 후 운영자가 직접 입력(`[REDACTED]`. 본 문서에 적지 않는다).
   - User must create a new password at next sign-in: 운영자 결정(혼자 운영이면 해제 가능, 인계 가능성 있으면 체크).
5. Next 클릭.

### Step 0-2. AdministratorAccess 정책 부여

1. Permissions options: `Attach policies directly` 선택.
2. Permissions policies 검색창에 `AdministratorAccess` 입력.
3. AWS managed policy `AdministratorAccess` 체크.
4. Next 클릭.
5. Tags 추가(권고):
   - `env=paper`
   - `kind=admin`
   - `project=portfolio`
6. Review and create.
7. Create user 클릭.

### Step 0-3. 콘솔 sign-in URL 확보

1. 생성된 `portadmin` 사용자 상세 → Security credentials 탭.
2. Console sign-in URL을 운영자 노트에 기록(형식: `https://<account-id>.signin.aws.amazon.com/console`).
3. (권고) IAM → Account settings → IAM users sign-in link → Account alias 등록(예: `portfolio-paper`). 별칭 등록 후 sign-in URL은 `https://portfolio-paper.signin.aws.amazon.com/console` 형태로 단순화된다.

### Step 0-4. portadmin MFA 활성화 (강력 권고)

1. `portadmin` 사용자 상세 → Security credentials 탭 → Multi-factor authentication (MFA) → Assign MFA device.
2. Device type: `Authenticator app` 또는 `Security key` 등 운영자 결정.
3. 화면 안내에 따라 등록 완료.
4. MFA 시리얼 / 백업 코드는 운영자가 안전한 위치에 보관(본 문서 / 노트에 평문 기록 금지, `[REDACTED]`).

### Step 0-5. portadmin으로 로그인 전환

1. Root 계정 로그아웃.
2. Step 0-3에서 확보한 Console sign-in URL로 이동.
3. IAM user name: `portadmin` 입력 + 비밀번호(`[REDACTED]`) 입력.
4. MFA 코드 입력.
5. 우측 상단 사용자 표시가 `portadmin@<account-id 또는 alias>`로 표시되는지 확인.
6. 이후 본 runbook의 모든 Step은 `portadmin`으로 수행한다.

### Step 0-6. (강력 권고) Root 계정 보안 강화

1. Root MFA 미활성이면 즉시 활성화(Authenticator app 또는 Security key).
2. Root access key(AWS Management Console 외부에서 사용 가능한 access key/secret access key)가 발급되어 있다면 즉시 삭제. Root access key 사용은 비권고.
3. 일상 작업은 모두 `portadmin`으로 수행. Root는 청구 / 계정 폐쇄 / IAM 정책 변경 같은 비상용으로만 사용.

### 생성 후 확인

- IAM → Users 목록에 `portadmin` 표시.
- `portadmin` Permissions 탭에 `AdministratorAccess` 정책 적용.
- `portadmin` Security credentials 탭에 MFA `Assigned` 상태 표시.
- 콘솔 sign-in URL이 운영자 노트에 기록.
- Root 로그아웃 후 `portadmin` 로그인 성공.
- 본 Step 이후 모든 후속 Step은 `portadmin`으로 수행한다.

### 실패 시 조치

- Root 비밀번호 / MFA 분실: 본 runbook 진행을 중단하고 AWS Account recovery 절차(이메일 인증, 결제 정보 인증 등)로 복구 우선. 복구 전까지 다른 Step 진행 금지.
- `portadmin` 비밀번호 분실: Root로 재로그인 → IAM → Users → `portadmin` → Manage console access → Reset password. 새 비밀번호는 다시 `[REDACTED]`로만 표기.
- `AdministratorAccess` 정책 부여 누락: `portadmin` 로그인 후 권한 부족 에러 발생 시 Root로 재로그인 → Users → `portadmin` → Permissions → Add permissions → `AdministratorAccess` 재부여.
- MFA 등록 실패: Authenticator app 시간 동기화 확인 후 재시도. 백업 코드를 안전한 위치에 보관.
- 중단 기준: Step 0-1 / 0-2 / 0-5가 통과하지 못하면 Step 1 이후 진행 금지(권한 / 로그인 사용자가 결정되지 않은 상태에서 후속 작업 시 Root로 작업하게 되는 위험).

## Step 1. AWS Region 확인 [확인]

### 목적

- 모든 후속 작업이 서울 `ap-northeast-2`에서 진행되도록 region을 고정한다.

### 사전 확인

- Step 0 완료. 현재 사용자는 `portadmin`이며 콘솔 우측 상단에 표시된다.
- `portadmin`에 VPC / EC2 / RDS / Secrets Manager 콘솔 접근 권한 존재(Step 0-2의 `AdministratorAccess`로 충족).
- 비용 발생 여부: 0 (region 자체 비용 없음).

### AWS Console 작업 순서

1. AWS Console 우측 상단 region 선택기 확인.
2. `Asia Pacific (Seoul) ap-northeast-2`로 전환.
3. 브라우저 즐겨찾기 또는 콘솔 URL 고정.

### 생성 후 확인

- 모든 콘솔 메뉴(VPC, EC2, RDS) 우측 상단에 `Seoul`이 표시.
- 다른 region의 리소스 목록이 보이지 않는다.

### 실패 시 조치

- 권한 부족이면 Step 0-2의 `AdministratorAccess` 정책 부여를 재확인.
- region 전환 후에도 다른 region 잔존 리소스가 보이면 region 미전환이거나 다중 탭 혼선. 새 탭에서 재확인.

## Step 2. 비용 / 환경 결정 확인 [확인]

### 목적

- aws-paper 1차 적용 결정과 비용 프로파일 결정을 운영자가 본 시점에 한 번 더 확인한다.

### 사전 확인

- [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 핵심 결정 락 상태 확인.
- 비용 발생 여부: 0 (문서 점검).

### AWS Console 작업 순서

본 Step은 콘솔 작업이 아닌 문서 검토.

1. [`../_common/operator-decisions.md`](../_common/operator-decisions.md) `At a Glance` 섹션에서 다음 항목 확인:
   - OD-ENV-003 1차 환경 = aws-paper 🟢
   - OD-NET-001 paper NAT Gateway = 미사용 🟢
   - OD-NET-005 VPC Endpoint 권고 세트 활성 🟢
   - OD-NET-009 SSM Session Manager만 사용 🟢
   - OD-RDS-001 paper RDS = `db.t4g.small` single-AZ 🟢
   - OD-CUT-001 cutover = pg_dump+pg_restore 🟢
2. [`./decision-matrix.md`](./decision-matrix.md) `11. 비용 프로파일 (aws-paper)`에서 low / realistic / stable 중 운영자가 적용할 라인을 확정.
3. 결과를 운영자 노트에 한 줄로 기록(예: `aws-paper realistic 적용`). 실제 secret 없음.

### 생성 후 확인

- 위 결정값이 본 runbook과 일치.
- 운영자 노트에 비용 프로파일 라인 결정 기록 존재.

### 실패 시 조치

- 결정과 본 runbook이 다르면 본 runbook을 진행하지 말고 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 갱신부터 진행.
- 결정 변경이 필요해 보이면 본 runbook에서 임의로 진행하지 말고 변경 제안만 기록.

## Step 3. CIDR / AZ 확정 [확인]

### 목적

- VPC CIDR과 사용 AZ 2개를 본 runbook 진행 전에 확정한다.

### 사전 확인

- 사내 다른 네트워크와 CIDR 충돌 없음을 확인.
- 비용 발생 여부: 0.

### AWS Console 작업 순서

본 Step은 결정 기록.

1. CIDR: `10.0.0.0/16` (권고). 사내 충돌 시 운영자 결정.
2. AZ 2개: `ap-northeast-2a`, `ap-northeast-2c` (권고).
3. 결과를 운영자 노트에 기록.

### 생성 후 확인

- CIDR / AZ 2개가 운영자 노트에 적혀 있다.

### 실패 시 조치

- CIDR 충돌 시 `10.1.0.0/16` 등 대안 결정 후 본 runbook의 모든 subnet CIDR을 일관되게 조정.

## Step 4. VPC 생성 [실행]

### 목적

- 본 spec의 모든 리소스가 들어갈 단일 VPC `portfolio-vpc`를 만든다.

### 사전 확인

- Step 1 ~ 3 완료.
- 권한: `ec2:CreateVpc` (`AdministratorAccess`로 충족).
- 비용 발생 여부: 0.

### AWS Console 작업 순서

1. AWS Console → VPC 메뉴로 이동.
2. Your VPCs → Create VPC 클릭.
3. Resources to create: `VPC only` 선택.
4. Name tag: `portfolio-vpc`
5. IPv4 CIDR block: `10.0.0.0/16`
6. IPv6 CIDR block: `No IPv6 CIDR block`
7. Tenancy: `Default`
8. Tags 추가:
   - `env=paper`
   - `project=portfolio`
9. Create VPC 클릭.

### 생성 후 확인

- VPC 목록에 `portfolio-vpc` 표시.
- State = `Available`.
- IPv4 CIDR = `10.0.0.0/16`.
- 기본 Route Table / NACL / DHCP option set 자동 생성됨.

### 실패 시 조치

- CIDR 충돌 에러: Step 3의 CIDR 결정 재확인.
- VPC 생성에 실패하면 region 잘못된 것이 아닌지 우측 상단 확인.
- rollback: VPC 삭제 가능. 단, attach된 리소스(Subnet, IGW)가 생기면 그것을 먼저 정리해야 한다.

## Step 5. Subnet 생성 [실행]

### 목적

- public / private (app) / private (data) 3종 × 2 AZ로 6개 subnet을 만든다.

### 사전 확인

- Step 4 완료(VPC `portfolio-vpc` Available).
- 권한: `ec2:CreateSubnet`.
- 비용 발생 여부: 0.

### Step 5-1. Public subnet 2개 생성

1. VPC → Subnets → Create subnet 클릭.
2. VPC ID: `portfolio-vpc` 선택.
3. Subnet 1: Name `public-a`, AZ `ap-northeast-2a`, IPv4 CIDR `10.0.0.0/24`, Tags `tier=public`, `env=paper`.
4. Add new subnet 클릭. Subnet 2: Name `public-b`, AZ `ap-northeast-2c`, IPv4 CIDR `10.0.1.0/24`, Tags 동일.
5. Create subnet 클릭.

### Step 5-2. Private app subnet 2개 생성

1. Create subnet 클릭(VPC 동일).
2. Subnet 1: Name `app-a`, AZ `ap-northeast-2a`, IPv4 CIDR `10.0.10.0/24`, Tags `tier=app`, `env=paper`.
3. Subnet 2: Name `app-b`, AZ `ap-northeast-2c`, IPv4 CIDR `10.0.11.0/24`, Tags 동일.
4. Create subnet 클릭.

### Step 5-3. Private data subnet 2개 생성

1. Create subnet 클릭(VPC 동일).
2. Subnet 1: Name `data-a`, AZ `ap-northeast-2a`, IPv4 CIDR `10.0.20.0/24`, Tags `tier=data`, `env=paper`.
3. Subnet 2: Name `data-b`, AZ `ap-northeast-2c`, IPv4 CIDR `10.0.21.0/24`, Tags 동일.
4. Create subnet 클릭.

### 생성 후 확인

- Subnets 목록에 6개 subnet 표시.
- 각 subnet의 VPC = `portfolio-vpc`, AZ가 권고 그대로.
- Auto-assign public IPv4 address: public-a / public-b만 운영자 결정에 따라 활성화 가능(Step 13에서 다시 결정). 본 Step에서는 기본값 유지.

### 실패 시 조치

- CIDR 중복 에러: 다른 subnet과 겹치지 않게 CIDR 재확인.
- AZ 한 곳에서 subnet 생성 실패: AZ가 region 안에 있는지 우측 상단 region 재확인.
- rollback: 잘못 만든 subnet은 Subnets → Actions → Delete subnet으로 삭제(아직 리소스가 attach되지 않았으면 즉시 가능).

## Step 6. Internet Gateway 생성 / VPC attach [실행]

### 목적

- public subnet의 외부 outbound 출구를 만든다.

### 사전 확인

- Step 4, 5 완료.
- 권한: `ec2:CreateInternetGateway`, `ec2:AttachInternetGateway`.
- 비용 발생 여부: IGW 자체 0. outbound 데이터 전송은 별도(`~$0.114/GB`).

### AWS Console 작업 순서

1. VPC → Internet Gateways → Create internet gateway 클릭.
2. Name tag: `portfolio-igw`. Tags: `env=paper`.
3. Create internet gateway 클릭.
4. 생성된 IGW 선택 → Actions → Attach to VPC.
5. VPC: `portfolio-vpc` 선택 → Attach internet gateway.

### 생성 후 확인

- IGW 목록에 `portfolio-igw` State = `Attached`.
- VPC `portfolio-vpc` 상세에 IGW가 연결됨.

### 실패 시 조치

- 다른 VPC에 이미 attach된 IGW를 재사용하려면 detach 후 attach. 1 VPC = 1 IGW.
- rollback: Detach 후 Delete.

## Step 7. NAT Gateway / NAT Instance 미사용 확인 [확인]

### 목적

- 본 spec 결정(OD-NET-001 / OD-NET-002 / R-COST-002)에 따라 NAT 리소스를 만들지 않는다는 사실을 명시적으로 확인한다.

### 사전 확인

- 비용 발생 여부: 0 (본 Step은 미생성 확인).

### AWS Console 작업 순서

1. VPC → NAT Gateways 메뉴로 이동.
2. 항목이 비어 있는지 확인. 만약 다른 작업으로 만들어진 NAT Gateway가 있으면 본 spec 진행을 중단하고 운영자에게 보고.
3. EC2 → Instances 메뉴로 이동.
4. Name이 `nat-*` 또는 `nat-instance-*`인 인스턴스가 없는지 확인.
5. 본 spec은 NAT 미사용임을 운영자 노트에 명시 기록.

### 생성 후 확인

- NAT Gateways 목록 비어 있음.
- NAT 역할의 EC2 인스턴스 없음.
- AWS Billing의 `NatGateway-Hours` 라인 0(현 시점).

### 실패 시 조치

- 의도치 않은 NAT 리소스 발견 시 운영자 결정으로 즉시 삭제(R-COST-002).
- NAT가 꼭 필요하다는 결정 변경이 필요하다면 본 runbook 진행을 중단하고 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-NET-001/002 갱신부터.

## Step 8. Route Table 생성 및 Subnet 연결 [실행]

### 목적

- public / app / data 3종 Route Table을 만들고 각 subnet에 연결한다.

### 사전 확인

- Step 4 ~ 7 완료.
- 권한: `ec2:CreateRouteTable`, `ec2:CreateRoute`, `ec2:AssociateRouteTable`.
- 비용 발생 여부: 0.

### Step 8-1. rt-public 생성 및 연결

1. VPC → Route Tables → Create route table 클릭.
2. Name: `rt-public`, VPC: `portfolio-vpc`. Tags: `tier=public`, `env=paper`. Create.
3. 생성된 `rt-public` 선택 → Routes 탭 → Edit routes.
4. Add route: Destination `0.0.0.0/0`, Target: Internet Gateway → `portfolio-igw`. Save changes.
5. Subnet associations 탭 → Edit subnet associations → `public-a`, `public-b` 체크 → Save associations.

### Step 8-2. rt-app 생성 및 연결

1. Create route table. Name: `rt-app`, VPC: `portfolio-vpc`. Tags: `tier=app`, `env=paper`.
2. Routes는 기본 VPC local만 유지(0.0.0.0/0 라우트 추가 금지).
3. Subnet associations → `app-a`, `app-b` 체크 → Save associations.

### Step 8-3. rt-data 생성 및 연결

1. Create route table. Name: `rt-data`, VPC: `portfolio-vpc`. Tags: `tier=data`, `env=paper`.
2. Routes는 기본 VPC local만 유지.
3. Subnet associations → `data-a`, `data-b` 체크 → Save associations.

### 생성 후 확인

- Route Tables 목록에 `rt-public`, `rt-app`, `rt-data` 3개.
- `rt-public`의 Routes에 `0.0.0.0/0 → igw-...` 존재.
- `rt-app`, `rt-data`의 Routes에는 VPC local만 존재.
- 각 subnet의 associated Route Table이 의도대로 연결됨.

### 실패 시 조치

- 잘못된 subnet이 연결되면 Subnet associations 다시 편집.
- IGW가 attach되지 않은 상태에서 0.0.0.0/0 라우트 추가 시도하면 실패. Step 6부터 재확인.
- rollback: Route 삭제 또는 Route Table 삭제. main route table은 삭제 불가.

## Step 9. Security Group 생성 [실행]

### 목적

- MS별 SG 8개와 VPC Endpoint 전용 SG를 만든다(빈 규칙으로 시작 → Step 10에서 규칙 채움).

### 사전 확인

- Step 4 완료.
- 권한: `ec2:CreateSecurityGroup`, `ec2:AuthorizeSecurityGroupIngress`, `ec2:AuthorizeSecurityGroupEgress`.
- 비용 발생 여부: 0.

### Step 9-1. SG 생성(이름과 설명만)

각 SG는 다음 형식으로 생성. 모두 VPC `portfolio-vpc`. Tags: `env=paper`.

1. `sg-marketconnector-ec2` — marketconnector EC2 + EIP.
2. `sg-port-view-ecs` — port-view ECS Fargate Service.
3. `sg-strategy-tasks` — decision / execution ECS Task.
4. `sg-crawler-tasks` — crawler ECS Task(public subnet 배치 후보).
5. `sg-preprocessor-tasks` — preprocessor ECS Task(public subnet 배치 후보).
6. `sg-research-batch` — research AWS Batch / ECS Task.
7. `sg-rds-postgres` — RDS PostgreSQL.
8. `sg-vpc-endpoints` — Interface VPC Endpoint 전용.

생성 절차(반복):

1. VPC → Security Groups → Create security group 클릭.
2. Security group name: 위 목록 중 하나.
3. Description: SG 역할 짧게 기록(예: `marketconnector EC2 with KIS broker outbound`).
4. VPC: `portfolio-vpc`.
5. Inbound rules / Outbound rules는 기본값 유지(이번 Step에서는 채우지 않는다).
6. Create security group 클릭.

### 생성 후 확인

- Security Groups 목록에 8개 SG 표시.
- 모두 VPC `portfolio-vpc`.
- 기본 outbound rule(0.0.0.0/0 all)이 자동 생성된 SG는 Step 10에서 정리.

### 실패 시 조치

- 이름 중복 에러: 동일 이름 SG가 이미 있으면 기존 SG 사용 또는 다른 이름 결정.
- rollback: SG는 attach된 리소스가 없으면 즉시 삭제 가능.

## Step 10. Security Group 규칙 채우기 [실행]

### 목적

- design.md SG 표 그대로 inbound / outbound 규칙을 채운다. RDS SG inbound 0.0.0.0/0은 절대 금지(R-SEC-001).

### 사전 확인

- Step 9 완료.
- 권한: `ec2:AuthorizeSecurityGroupIngress`, `ec2:AuthorizeSecurityGroupEgress`, `ec2:RevokeSecurityGroupEgress`.
- 비용 발생 여부: 0.

### Step 10-1. sg-rds-postgres inbound (먼저 채우면 안전)

1. `sg-rds-postgres` 선택 → Edit inbound rules.
2. Add rule × 6 (각 SG를 source로 하여 5432 허용):
   - Type PostgreSQL (5432), Source `sg-marketconnector-ec2`.
   - 동일 형식으로 `sg-port-view-ecs`, `sg-strategy-tasks`, `sg-crawler-tasks`, `sg-preprocessor-tasks`, `sg-research-batch`.
3. Save rules.
4. Outbound rules는 비워 둔다(기본 0.0.0.0/0 outbound는 Edit outbound rules에서 삭제).

### Step 10-2. sg-vpc-endpoints inbound

1. `sg-vpc-endpoints` 선택 → Edit inbound rules.
2. Add rule × 6 (TCP 443, source 위 6개 SG).
3. Save rules.
4. Outbound rules 비워 둔다.

### Step 10-3. sg-marketconnector-ec2

1. Inbound rules:
   - Type Custom TCP, Port 5000(또는 운영자 결정 포트), Source `sg-port-view-ecs`.
2. Outbound rules:
   - Type HTTPS (443), Destination `0.0.0.0/0` (KIS broker).
   - Type PostgreSQL (5432), Destination `sg-rds-postgres`.
   - Type HTTPS (443), Destination `sg-vpc-endpoints`.
3. Save rules.

### Step 10-4. sg-port-view-ecs

1. Inbound rules: 비워 둔다(SSM 포트포워딩 또는 향후 ALB 도입 시 ALB SG에서 들어옴).
2. Outbound rules:
   - HTTPS (443) → `0.0.0.0/0` (Slack webhook 등).
   - Custom TCP (5000) → `sg-marketconnector-ec2`.
   - PostgreSQL (5432) → `sg-rds-postgres`.
   - HTTPS (443) → `sg-vpc-endpoints`.

### Step 10-5. sg-strategy-tasks

1. Inbound rules: 비워 둔다.
2. Outbound rules:
   - Custom TCP (5000) → `sg-marketconnector-ec2`.
   - PostgreSQL (5432) → `sg-rds-postgres`.
   - HTTPS (443) → `sg-vpc-endpoints`.
   - HTTPS (443) → `0.0.0.0/0` (필요 시 Slack 등).

### Step 10-6. sg-crawler-tasks

1. Inbound rules: 비워 둔다(절대 미허용).
2. Outbound rules:
   - HTTPS (443) → `0.0.0.0/0` (KRX/Naver/yfinance).
   - PostgreSQL (5432) → `sg-rds-postgres`.
   - HTTPS (443) → `sg-vpc-endpoints`.

### Step 10-7. sg-preprocessor-tasks

1. Inbound rules: 비워 둔다.
2. Outbound rules:
   - HTTPS (443) → `0.0.0.0/0` (holiday API).
   - PostgreSQL (5432) → `sg-rds-postgres`.
   - HTTPS (443) → `sg-vpc-endpoints`.

### Step 10-8. sg-research-batch

1. Inbound rules: 비워 둔다.
2. Outbound rules:
   - PostgreSQL (5432) → `sg-rds-postgres`.
   - HTTPS (443) → `sg-vpc-endpoints`.
   - HTTPS (443) → `0.0.0.0/0` (S3 Gateway 사용으로 사실상 호출 적음. 필요 시 유지).

### 생성 후 확인

- 각 SG의 inbound / outbound 규칙이 위 표와 일치.
- `sg-rds-postgres` inbound에 0.0.0.0/0 없음. 0/0 inbound rule이 있으면 즉시 제거.
- `sg-crawler-tasks`, `sg-preprocessor-tasks` inbound 비어 있음(public subnet 배치 시 외부 노출 방지, R-NET-001).

### 실패 시 조치

- SG 참조 cycle 에러: `sg-port-view-ecs`가 `sg-marketconnector-ec2` outbound에 있고 `sg-marketconnector-ec2` inbound가 `sg-port-view-ecs`인 경우 정상. cycle 자체는 SG 참조에서 허용된다.
- RDS inbound에 잘못해서 0.0.0.0/0이 들어갔다면 즉시 Revoke. CloudTrail에서 호출 audit.
- rollback: 잘못 추가된 rule만 Revoke.

## Step 11. VPC Endpoint 생성 [실행]

### 목적

- NAT-free 환경에서 ECR / S3 / Secrets Manager / SSM / CloudWatch Logs API를 사설 경로로 사용 가능하게 한다.

### 사전 확인

- Step 8, 9, 10 완료.
- 권한: `ec2:CreateVpcEndpoint`.
- 비용 발생 여부: Gateway endpoint 0. Interface endpoint AZ당 ~$8/월 + 데이터 처리(R-NET-003 / R-COST-001 참조). approval required.

### Step 11-1. S3 Gateway Endpoint

1. VPC → Endpoints → Create endpoint.
2. Name tag: `vpce-s3-gw`.
3. Service category: AWS services.
4. Service name: `com.amazonaws.ap-northeast-2.s3` (Type: Gateway).
5. VPC: `portfolio-vpc`.
6. Route tables: `rt-app`, `rt-data` 체크.
7. Policy: Full access(기본).
8. Tags: `env=paper`.
9. Create endpoint.

### Step 11-2. ECR API Interface Endpoint

1. Create endpoint.
2. Name tag: `vpce-ecr-api`.
3. Service name: `com.amazonaws.ap-northeast-2.ecr.api` (Type: Interface).
4. VPC: `portfolio-vpc`.
5. Subnets: `app-a`, `app-b` 체크.
6. Security groups: `sg-vpc-endpoints` 선택.
7. Enable DNS name 체크(private DNS).
8. Policy: Full access(기본).
9. Create endpoint.

### Step 11-3. ECR DKR Interface Endpoint

1. Create endpoint.
2. Name tag: `vpce-ecr-dkr`.
3. Service name: `com.amazonaws.ap-northeast-2.ecr.dkr` (Interface).
4. 나머지 입력 Step 11-2와 동일.

### Step 11-4. Secrets Manager Interface Endpoint

1. Create endpoint.
2. Name tag: `vpce-secretsmanager`.
3. Service name: `com.amazonaws.ap-northeast-2.secretsmanager` (Interface).
4. 나머지 입력 동일.

### Step 11-5. SSM Interface Endpoint

1. Create endpoint.
2. Name tag: `vpce-ssm`.
3. Service name: `com.amazonaws.ap-northeast-2.ssm` (Interface).
4. 나머지 입력 동일.

본 Step에서는 SSM Session Manager 운영자 접속 / Parameter Store만 다룬다. 운영자 결정에 따라 `ssmmessages`, `ec2messages` 추가 가능.

### Step 11-6. CloudWatch Logs Interface Endpoint

1. Create endpoint.
2. Name tag: `vpce-logs`.
3. Service name: `com.amazonaws.ap-northeast-2.logs` (Interface).
4. 나머지 입력 동일.

### 생성 후 확인

- Endpoints 목록 6개(S3 1 + Interface 5).
- 각 Interface endpoint State = `Available`, Private DNS = `enabled`, AZ에 `app-a` / `app-b` 모두 포함.
- S3 endpoint는 `rt-app`, `rt-data`의 Routes에 추가됨.

### 실패 시 조치

- `Endpoint did not stabilize` 에러: subnet AZ가 service 가용 AZ가 아닐 수 있다. 다른 AZ subnet으로 재시도.
- ECS Task가 ECR pull 실패면 본 Step 11-2 / 11-3과 SG inbound 443(`sg-vpc-endpoints`)을 우선 점검.
- rollback: Endpoint 삭제. NAT-free에서 endpoint 없이는 AWS API가 막히므로, 운영 중 삭제는 ECS Task 정지 후에만 진행.

## Step 12. RDS Subnet Group 생성 [실행]

### 목적

- RDS instance가 배치될 subnet 묶음을 만든다.

### 사전 확인

- Step 5 완료(`data-a`, `data-b`).
- 권한: `rds:CreateDBSubnetGroup`.
- 비용 발생 여부: 0.

### AWS Console 작업 순서

1. AWS Console → RDS → Subnet groups → Create DB subnet group.
2. Name: `portfolio-paper-subnet-group`.
3. Description: `Portfolio paper RDS subnet group (data-a, data-b)`.
4. VPC: `portfolio-vpc`.
5. Add subnets: AZ `ap-northeast-2a` → `data-a`, AZ `ap-northeast-2c` → `data-b`.
6. Create.

### 생성 후 확인

- Subnet groups 목록에 `portfolio-paper-subnet-group` 표시. Status = `Complete`.
- Subnets 컬럼에 2개 subnet 포함.

### 실패 시 조치

- subnet AZ 부족 에러: subnet 2개 모두 `data` tier로 생성됐는지 Step 5 재확인.
- rollback: subnet group은 RDS instance가 사용 중이면 삭제 불가. 사용 전이면 즉시 삭제.

## Step 13. RDS Parameter Group 생성 [실행]

### 목적

- aws-paper RDS PostgreSQL 16 parameter group `pg-portfolio-paper`를 만들고 권고값을 적용한다.

### 사전 확인

- 비용 발생 여부: 0.

### AWS Console 작업 순서

1. RDS → Parameter groups → Create parameter group.
2. Parameter group family: `postgres16`.
3. Type: `DB Parameter Group`.
4. Group name: `pg-portfolio-paper`.
5. Description: `Portfolio paper PostgreSQL 16 parameters`.
6. Create.
7. 생성된 그룹 선택 → Edit parameters로 다음 값 적용:
   - `rds.force_ssl` = `1`
   - `log_min_duration_statement` = `1000`
   - `log_lock_waits` = `1`
   - `idle_in_transaction_session_timeout` = `60000`
8. Save changes.

### 생성 후 확인

- Parameter groups 목록에 `pg-portfolio-paper` 표시.
- 위 4개 parameter 값이 적용됨(static parameter는 RDS reboot 시 반영, dynamic은 즉시).

### 실패 시 조치

- parameter family 잘못 선택 시 그룹 삭제 후 재생성.
- rollback: parameter group은 instance가 참조 중이 아니면 삭제 가능.

## Step 14. RDS PostgreSQL 인스턴스 생성 [실행]

### 목적

- aws-paper portfolio RDS instance를 만든다(approval required, 비용 영향 큼).

### 사전 확인

- Step 12, 13 완료.
- DB master password가 Secrets Manager `/portfolio/paper/rds/master`에 사전 등록되어 있어야 한다(Step 14-0). 등록 시 실제 값은 운영자만 입력하고 본 문서에는 `[REDACTED]`만.
- 권한: `rds:CreateDBInstance`, `secretsmanager:GetSecretValue`(추후 application).
- 비용 발생 여부: `db.t4g.small` single-AZ ~$26/월 + storage ~$7/월 + backup. R-COST-001 / R-COST-002 점검.

### Step 14-0. (선행) Secrets Manager에 master password 등록

1. AWS Console → Secrets Manager → Store a new secret.
2. Secret type: `Other type of secret`.
3. Plaintext: `[REDACTED]` (운영자 직접 입력. 본 문서에 적지 않는다).
4. Encryption key: `aws/secretsmanager`(KMS default).
5. Secret name: `/portfolio/paper/rds/master`.
6. Tags: `env=paper`, `kind=rds-master`.
7. 자동 rotation: 본 spec에서는 비활성. 06 spec에서 결정.
8. Store.

본 Step은 06 spec과 중복되므로 06이 먼저 진행된 경우 생략 가능.

### Step 14-1. RDS 생성

1. AWS Console → RDS → Databases → Create database.
2. Choose creation method: `Standard create`.
3. Engine type: `PostgreSQL`. Version: `PostgreSQL 16.x` 최신 minor.
4. Templates: `Production` 또는 `Dev/Test`(운영자 결정. 본 spec은 paper이므로 Dev/Test 가능).
5. Settings:
   - DB instance identifier: `portfolio-paper-rds`.
   - Master username: `portfolio_admin`.
   - Master password: Secrets Manager `/portfolio/paper/rds/master` 값 직접 입력(`[REDACTED]`). Console에 보이지 않게 입력.
6. Instance configuration: `db.t4g.small`.
7. Storage:
   - Storage type: `gp3`.
   - Allocated storage: `50 GB`.
   - Storage autoscaling: 권고 비활성(필요 시 활성, 운영자 결정).
8. Availability & durability: `Single-AZ DB instance deployment` (paper 결정).
9. Connectivity:
   - VPC: `portfolio-vpc`.
   - DB subnet group: `portfolio-paper-subnet-group`.
   - Public access: `No`.
   - VPC security group: `sg-rds-postgres` 선택.
   - Availability Zone: `ap-northeast-2a`(또는 운영자 결정).
   - Database port: `5432`.
10. Database authentication: `Password authentication` (IAM 인증은 06 spec에서 검토).
11. Monitoring: Performance Insights `Enabled`. Retention 7일.
12. Additional configuration:
    - Initial database name: 본 단계에서는 비워 둔다(Step 16에서 `portfolio` DB 생성).
    - DB parameter group: `pg-portfolio-paper`.
    - Backup retention period: `7 days`.
    - Backup window: 운영 외 시간대(예: `19:00-19:30 UTC`).
    - Encryption: Enable encryption (KMS default).
    - Maintenance window: 권고 시간대.
    - Deletion protection: `Enable` (실수 삭제 방지).
13. Create database 클릭.

### 생성 후 확인

- Databases 목록에 `portfolio-paper-rds` Status = `Available` (수 분 소요).
- Endpoint URL 확인 후 운영자 노트에 기록(`INTEREST_DB_HOST`).
- VPC, Subnet group, SG 표시값이 의도와 일치.
- Backup window / retention / PITR 활성 확인.
- Deletion protection On.

### 실패 시 조치

- subnet group AZ 부족 에러: data-a, data-b가 모두 group에 있는지 재확인.
- SG 누락 에러: `sg-rds-postgres` 선택 누락 시 RDS inbound 5432가 막힌다. 즉시 Modify로 SG 변경.
- rollback: instance Delete. Deletion protection이 켜져 있으면 먼저 비활성 후 삭제. 삭제 시 final snapshot 생성 옵션 확인.

## Step 15. RDS 접속 보안 / Parameter / Option 확인 [확인]

### 목적

- 생성된 RDS의 접속 보안 / parameter group / SG 연결 / publicly accessible 상태가 의도와 일치하는지 검증한다.

### 사전 확인

- Step 14 완료.
- 비용 발생 여부: 0.

### AWS Console 작업 순서

1. RDS → Databases → `portfolio-paper-rds` 상세.
2. Connectivity & security 탭:
   - Endpoint / Port 기록.
   - VPC = `portfolio-vpc`.
   - Subnet group = `portfolio-paper-subnet-group`.
   - SG = `sg-rds-postgres`.
   - Publicly accessible = `No`.
3. Configuration 탭:
   - DB parameter group = `pg-portfolio-paper`.
   - Status = `in-sync`(static parameter는 reboot 후 반영).
4. Maintenance & backups 탭:
   - Automated backups = `Enabled`, retention = 7일.
   - PITR = `Enabled`.
5. Monitoring 탭:
   - Performance Insights = `Enabled`.

### 생성 후 확인

- 위 모든 값이 의도대로 표시.
- `Publicly accessible = No`(R-SEC-001 핵심).

### 실패 시 조치

- Publicly accessible = Yes로 잘못 생성된 경우 즉시 Modify로 No로 변경 + 적용 시점 `Apply immediately`. 외부 접속 로그 audit.
- parameter group 미적용 시 Modify에서 변경 후 reboot.

## Step 16. DB / schema / role 생성 준비 (실행은 본 spec 범위 밖) [준비]

### 목적

- `portfolio` DB / 10개 schema / 7개 role 생성 SQL을 준비한다. 본 spec에서는 SQL 실행은 운영자 직접 수행이며, 실제 실행은 03 / 06 spec과 통합한다.

### 사전 확인

- Step 14 완료, RDS endpoint 확보.
- SSM Session Manager로 같은 VPC 내 EC2 또는 ECS Task에서 psql 접속 가능한 환경. SSH 22는 절대 열지 않는다(OD-NET-009).
- 비용 발생 여부: SQL 실행 자체는 0(연결 EC2 / ECS 사용량만).

### AWS Console 작업 순서

1. (운영자 직접) SSM Session Manager로 같은 VPC 안의 운영자 client(예: 임시 EC2 또는 ECS Task)에 접속.
2. psql 명령으로 `portfolio_admin` 계정 접속(비밀번호는 Secrets Manager에서 운영자만 직접 조회).
3. 다음 SQL을 운영자 노트에 준비(실제 실행은 본 runbook 검증 후, 03 / 06 spec 통합 시점):
   - `CREATE DATABASE portfolio;`
   - 10개 schema: `CREATE SCHEMA IF NOT EXISTS reference;` 외 9개(`interest`, `preprocessor`, `research`, `decision`, `execution`, `connector`, `ops`, `legacy`, `public`).
   - 7개 role: `CREATE ROLE marketconnector_app LOGIN PASSWORD '[REDACTED]';` 외 6개.
   - role별 schema 권한: design.md "schema별 권한 매트릭스" 그대로 GRANT.
   - role별 search_path: 8개 MS README 정의 그대로 `ALTER ROLE <role> SET search_path = ...;` (R-DATA-001).
4. SQL 파일을 운영자 로컬에서만 보관(repo 커밋 금지). 비밀번호 자리는 `[REDACTED]`.

### 생성 후 확인

- SQL이 운영자 노트에 준비됨.
- 실제 DB / schema / role 생성은 본 runbook 외부 단계로 위임됨.

### 실패 시 조치

- 본 Step에서 실제 SQL을 실행하지 않는다. 실수 실행이 발생하면 운영자가 즉시 트랜잭션 rollback 또는 schema/role drop 후 재시도.

## Step 17. pg_dump / pg_restore 사전 준비 [준비]

### 목적

- local-dev → aws-paper cutover의 dump / restore 절차를 미리 합의한다. 실제 cutover는 [`../10-cutover-and-validation-runbook`](../10-cutover-and-validation-runbook)(예정).

### 사전 확인

- Step 14, 16 SQL 준비 완료.
- 비용 발생 여부: dump / restore 자체는 0(운영자 client + 데이터 전송만).

### AWS Console 작업 순서

본 Step은 절차 합의.

1. local-dev PostgreSQL 사용 가능 시점 합의(broker 주문 / fill sync / intraday monitor 정지 시간대).
2. dump 명령 형식 운영자 노트에 합의:
   - `pg_dump --format=custom --no-owner --no-privileges -h <local-host> -U <local-user> -d portfolio -f portfolio.dump`
   - 비밀번호는 운영자 직접 입력. 본 문서에는 `[REDACTED]`.
3. restore 명령 형식 합의:
   - `pg_restore --no-owner --no-privileges --jobs=4 -h <rds-endpoint> -U portfolio_admin -d portfolio portfolio.dump`
4. dump 파일 보관 위치 결정(임시 EC2 EBS, S3 버킷). 작업 완료 후 dump 파일 삭제 정책 합의.
5. 검증 SQL은 design.md `검증 SQL 후보` 섹션 사용.
6. Rollback 기준은 design.md `Rollback 기준` 섹션 + R-DATA-002 mitigation 참조.

### 생성 후 확인

- 운영자 노트에 dump / restore 명령 형식과 시점, 보관 / 삭제 정책 기록.
- 실제 dump / restore는 본 spec에서 실행하지 않는다.

### 실패 시 조치

- 본 Step은 실행 단계가 아니므로 실패 시나리오는 없다. 실제 cutover는 별도 spec 진행.

## Step 18. 최종 검증 [준비]

### 목적

- Step 4 ~ 15의 결과가 [`./validation-checklist.md`](./validation-checklist.md) 기준을 모두 통과하는지 점검한다.

### 사전 확인

- Step 0 ~ 15 완료.
- 비용 발생 여부: 0(점검).

### AWS Console 작업 순서

1. [`./validation-checklist.md`](./validation-checklist.md)를 열고 각 항목을 직접 점검.
2. 통과하지 못한 항목은 본 runbook의 해당 Step으로 돌아가 재작업.
3. 모든 항목 통과 시 운영자 노트에 `02 spec runbook 통과` 기록.

### 생성 후 확인

- validation-checklist 모든 항목 체크.
- 운영자 노트에 통과 기록 존재.

### 실패 시 조치

- 통과 못 한 항목별로 Step 4 ~ 15 재작업.
- 비용 / 보안 항목 통과 못 하면 본 runbook 진행을 멈추고 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 검토부터.

## Step 19. Rollback 순서 (필요 시) [복구]

본 spec 단독 rollback 절차. cutover rollback은 [`../10-cutover-and-validation-runbook`](../10-cutover-and-validation-runbook)(예정)에서 통합.

### 19-1. RDS instance 삭제

1. RDS → Databases → `portfolio-paper-rds`.
2. Modify에서 Deletion protection 해제 → Apply immediately.
3. Delete.
4. Final snapshot 옵션은 운영자 결정. evidence 보존이면 snapshot 생성.

### 19-2. RDS Parameter Group / Subnet Group 삭제

1. Parameter groups → `pg-portfolio-paper` 삭제(instance 비참조 상태).
2. Subnet groups → `portfolio-paper-subnet-group` 삭제.

### 19-3. VPC Endpoint 삭제

1. Endpoints에서 6개 endpoint 모두 Delete.

### 19-4. Security Group 삭제

1. Security Groups에서 8개 SG 삭제(다른 리소스 비참조 확인).
2. RDS / Endpoint 참조가 남아 있으면 먼저 정리.

### 19-5. Route Table 삭제

1. Subnet associations 해제.
2. `rt-public`, `rt-app`, `rt-data` 삭제.

### 19-6. Internet Gateway detach / delete

1. Internet Gateways → `portfolio-igw` → Detach from VPC.
2. Delete.

### 19-7. Subnet 삭제

1. Subnets에서 6개 모두 삭제.

### 19-8. VPC 삭제

1. Your VPCs → `portfolio-vpc` → Delete VPC.

### Rollback 검증

- VPC 콘솔에 `portfolio-vpc`, `portfolio-paper-rds`, 6개 subnet, 8개 SG, 6개 endpoint 모두 존재하지 않는다.
- AWS Billing의 일별 비용 그래프에서 본 spec 관련 라인이 종료된다.

본 Step은 IAM `portadmin` 사용자 / 정책 / Root 보안 강화는 되돌리지 않는다. portadmin 사용자 자체를 제거할 필요가 있으면 별도 결정으로만 처리한다.

## 부록 A. RDS Restore Runner & DB Role / 권한 적용 교훈 (2026-06-09 실적 기반)

본 부록은 2026-06-09 운영자가 실제 수행한 Local PostgreSQL → aws-paper RDS migration과 DB Role / 권한 1차 적용 결과를 후속 운영에서 재사용하기 위한 짧은 교훈 모음이다. 결정값 / 기존 Step 번호는 변경하지 않는다. 상세 실행 기록은 [`./operation-notes.md`](./operation-notes.md)와 [`./db-roles-and-grants.md`](./db-roles-and-grants.md) 참조.

### A-1. PostgreSQL major version mismatch 회피

- dump 시작 전에 `pg_dump --version` 그리고 dump 파일에 대해 `pg_restore --list` 첫 줄 또는 `head -c` 메타데이터로 dump source major version을 확인한다.
- RDS engine major version과 다르면 restore 전에 RDS major version을 dump source 이상으로 맞춘다(R-DATA-003).
- 2026-06-09 실적: dump source PostgreSQL 18.1 → 기존 RDS PG 16.14 부적합 → RDS를 PG 18.4로 재생성 후 진행.

### A-2. Private RDS는 EC2 / SSM 경유로만 접속

- RDS Public access = No 정책(R-SEC-001) 유지를 위해 로컬 PC IP를 RDS SG에 직접 허용하지 않는다.
- restore runner는 같은 VPC에 배치된 EC2(예: aws-paper MarketConnector EC2) 또는 임시 EC2를 사용한다. 운영자 접속은 EC2 Instance Connect 또는 SSM Session Manager(OD-NET-009)를 우선한다.
- 2026-06-09 실적: aws-paper MarketConnector EC2(Amazon Linux 2023, public subnet, EIP attach)를 restore runner로 사용. 로컬 PC → S3 임시 bucket → EC2 → private RDS 경로로 dump 파일 전송.

### A-3. PostgreSQL client / dump archive 호환

- pg_restore archive format의 호환성을 위해 client 버전을 dump source 이상으로 맞춘다.
- 2026-06-09 실적: 처음 PG 15.18 client에서 archive header version 불일치 확인 → 18.4 client로 전환 후 정상 동작.

### A-4. dump owner role과 RDS role 불일치 처리

- dump의 object owner가 RDS에 존재하지 않는 role이면 `role "X" does not exist` 오류로 restore가 멈춘다(R-DATA-004).
- 권고 옵션: `pg_restore --no-owner --no-privileges`로 owner / 권한 정보를 무시하고 데이터만 복원한 뒤, [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §4 SQL로 owner / 권한을 재구성한다.
- 2026-06-09 실적: 1차 시도 `role "postgres" does not exist` → DB drop / recreate 후 `--no-owner --no-privileges`로 재실행하여 성공.

### A-5. owner 이관은 schema 단위와 객체 단위를 분리해 결정

- 본 spec [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §4.2 절차대로 `portfolio_owner` 생성 → `portfolio_admin`에 멤버십 부여 → 9개 도메인 schema owner를 `portfolio_owner`로 이관한다(public은 변경하지 않음).
- 기존 table / sequence / index의 owner는 별도 결정(OD-DB-010)으로 1차 적용에서 `REASSIGN OWNED BY portfolio_admin TO portfolio_owner`를 실행하지 않을 수 있다. 그 경우 §4.5 default privileges는 새 객체에만 자동 적용되며, 기존 객체에는 §4.4 명시 GRANT만 적용된 상태로 운영한다.
- 2026-06-09 실적: schema owner 이관 완료, 기존 객체 owner는 `portfolio_admin` 유지, REASSIGN 미실행. 후속 결정으로 일괄 이관 여부 분리 관리.

### A-6. 정합성 검증 비교 시 줄바꿈 정규화

- 로컬과 RDS의 row count CSV / object count snapshot을 diff할 때 CRLF / LF 차이로 false-positive가 자주 발생한다.
- `diff --strip-trailing-cr` 또는 `git diff --ignore-cr-at-eol` 같이 줄바꿈을 정규화한 비교를 권장한다.
- 2026-06-09 실적: schema / table / index / sequence / FK / trigger / row count 모두 정규화 비교 후 diff 0.

본 부록의 운영 실적은 03 / 06 spec과 [`../10-cutover-and-validation-runbook`](../10-cutover-and-validation-runbook)(예정) 작성 시 입력으로 활용한다.

## 본 runbook 작업 안전 제약

- 실제 AWS 리소스 생성 / 변경은 운영자 직접 수행. 본 문서는 절차 안내일 뿐 자동 실행하지 않는다.
- 8개 MS의 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 금지.
- broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / 주문 / 체결 / Daily Batch / intraday monitor 호출 금지.
- 모든 secret은 `[REDACTED]` 또는 placeholder만 사용. portadmin 비밀번호 / MFA 시리얼 / 백업 코드는 본 문서 / 노트 어디에도 평문 기록 금지.
- 결정값을 임의로 바꾸지 않는다. 변경이 필요하면 [`../_common/operator-decisions.md`](../_common/operator-decisions.md)에 변경 제안만 기록.
