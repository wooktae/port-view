# Design Document — 02-aws-network-and-rds

## Overview

본 design은 PORT-STRATEGY-AI 8개 MS가 사용할 AWS 네트워크 기반과 RDS for PostgreSQL 단일 portfolio DB 구조를 확정한다. 운영자 결정 변경에 맞춰 환경 모델은 local-dev / aws-paper / aws-live 3개로 정리하고, 네트워크는 NAT-free 전략을 기본으로 한다.

핵심 결정

- 단일 region(서울 ap-northeast-2), 단일 VPC.
- 환경 구분: local-dev(기존 로컬 PostgreSQL, AWS 리소스 없음) / aws-paper(1차 AWS 구축) / aws-live(paper 검증 후 후속 구축).
- public / private (app) / private (data) 3종 subnet, 다중 AZ.
- NAT Gateway는 aws-paper / aws-live 모두 미사용. 인터넷 outbound가 필요한 워크로드는 public subnet + assignPublicIp 또는 EC2 + EIP 또는 NAT Instance(보조) 후보로 분리.
- VPC Endpoint는 ECR (api+dkr), S3 (Gateway), Secrets Manager, SSM, CloudWatch Logs 활성. STS / KMS는 옵션.
- 마켓커넥터는 EC2 + EIP 1순위. broker IP 등록은 EIP 기준.
- aws-paper RDS는 `db.t4g.small` single-AZ 시작. aws-live RDS는 비용 절감안(single-AZ) / 안정성 우선안(multi-AZ) 두 옵션 비교 후 선택.
- portfolio DB와 10개 schema 유지. MS별 search_path / 환경변수 키 호환성 유지.
- cutover는 `pg_dump` + `pg_restore` 1순위. AWS DMS 보류.
- aws-live BUY/SELL/fill sync/position 변경/intraday stop SELL 생성은 자동 재시도 금지. idempotent step만 자동 재시도 허용.

## Architecture

### 환경 모델

| 환경 | 위치 | 역할 | broker 연결 | 자동 BUY/SELL | RDS | NAT | ALB |
|------|------|------|--------------|---------------|-----|-----|-----|
| local-dev | 운영자 로컬 | 개발, 단위 검증, 소스 수정 | 금지 | 금지 | 기존 로컬 PostgreSQL | 해당 없음 | 해당 없음 |
| aws-paper | AWS 단일 VPC | 실전 리허설(KIS 모의투자) | broker paper / 검증 모드만 | 초기 차단 → 검증 후 단계적 허용 | `db.t4g.small` single-AZ, 7일 backup, PITR | 미사용 | 초기 미사용 |
| aws-live | AWS 단일 VPC | 실계좌 운영 | 허용(초기 자동주문 금지) | 후보 생성 + View 수동 승인 → 검증 후 제한적 | 비용 절감안 single-AZ / 안정성 우선안 multi-AZ, 14일 backup, PITR | 미사용 | 초기 비용 절감안 보류, 안정성 우선안에서 internal ALB |

### 단일 VPC vs prod / non-prod 분리

| 옵션 | 비용 | 운영 부담 | 권고 |
|------|------|-----------|------|
| 단일 VPC + 환경별 SG/Subnet 태그 | 낮음 | 낮음 | 1순위(현 결정) |
| prod / non-prod VPC 분리 | 추가 NAT/Endpoint 비용 발생 가능, peering 또는 별도 빌드 부담 | 높음 | 2순위. 규제 / 감사 요구 시점 재검토 |

### VPC 다이어그램 (텍스트)

```
VPC: portfolio-vpc (10.0.0.0/16, 서울 ap-northeast-2, AZ a/b)

Public Subnets
  ├─ public-a 10.0.0.0/24  (AZ a) [IGW] [marketconnector EC2 + EIP]
  └─ public-b 10.0.1.0/24  (AZ b) [(예비)]

Private (app) Subnets — NAT-free, 외부 라우트 없음
  ├─ app-a    10.0.10.0/24 (AZ a) [ECS Fargate Task]
  └─ app-b    10.0.11.0/24 (AZ b) [ECS Fargate Task]

Private (data) Subnets
  ├─ data-a   10.0.20.0/24 (AZ a) [RDS]
  └─ data-b   10.0.21.0/24 (AZ b) [RDS standby (multi-AZ 시)]

VPC Endpoints (Interface, app subnet)
  - com.amazonaws.ap-northeast-2.ecr.api
  - com.amazonaws.ap-northeast-2.ecr.dkr
  - com.amazonaws.ap-northeast-2.secretsmanager
  - com.amazonaws.ap-northeast-2.ssm
  - com.amazonaws.ap-northeast-2.logs
  (옵션) com.amazonaws.ap-northeast-2.sts
  (옵션) com.amazonaws.ap-northeast-2.kms

VPC Endpoints (Gateway)
  - com.amazonaws.ap-northeast-2.s3
```

CIDR은 권고 예시. 운영자가 사내 다른 네트워크와 충돌이 없도록 결정.

### 리소스 배치 정책

- public-a / public-b
  - IGW
  - marketconnector EC2 (EIP 부여)
  - 인터넷 outbound가 필요한 ECS Fargate Task(예: crawler / preprocessor) 또는 ECS on EC2(crawler 전용 EC2)
- app-a / app-b
  - 외부 인터넷이 필요 없는 ECS Fargate Task(port-view, decision, execution, research)
- data-a / data-b
  - RDS instance 와 multi-AZ standby

### Route Table 정책 (NAT-free)

- public Route Table → 0.0.0.0/0 IGW
- app Route Table → outbound 외부 라우트 없음. AWS 서비스는 VPC Endpoint로만 도달.
- data Route Table → outbound 외부 라우트 없음.

## NAT-free 전략과 인터넷 outbound 옵션

NAT Gateway 미사용을 기본안으로 정한 상태에서, 인터넷 outbound가 필요한 워크로드를 어떻게 배치할지가 본 spec의 핵심 선택지다.

### 옵션 비교

| 옵션 | 설명 | 비용 영향 | 보안 리스크 | SG 통제 기준 | 운영 난이도 | 권고 사용처 |
|------|------|----------|------------|--------------|-------------|--------------|
| OPT-1: ECS Fargate + public subnet + assignPublicIp | Task에 public IP 자동 할당 | NAT 없음. 약간의 IPv4 사용 비용 | Task SG 실수 시 외부에서 inbound 가능 | inbound 0.0.0.0/0 절대 미허용. outbound는 필요한 외부 도메인만 허용 | 낮음 | crawler / preprocessor / research |
| OPT-2: marketconnector EC2 + EIP | EC2에 고정 EIP, broker IP 등록 | EC2 단가 + EIP attach 무료 | EC2 SG 잘못 시 SSH/포트 외부 노출 | inbound는 SSM 접속만, port 22/외부 포트 차단 | 중 | marketconnector 1순위 |
| OPT-3: crawler 전용 EC2 또는 ECS on EC2 | crawler를 EC2 또는 ECS on EC2(public subnet)로 운영 | EC2 24/7 비용 | EC2 직접 운영 부담 | OPT-2와 동일 | 중 | KRX 로그인 / Selenium 안정 운영이 ECS Fargate에서 어려운 경우 |
| OPT-4: NAT Instance(`t4g.nano`) | 자체 NAT EC2를 통해 private app subnet outbound | EC2 ~$3.5/월 + EBS + 데이터 처리 | EC2 SPOF. multi-AZ HA 직접 구현 부담 | NAT EC2 SG는 outbound만, inbound는 VPC 내부만 | 높음 | NAT-free가 어려운 일부 워크로드 보조 |

권고

- 마켓커넥터: OPT-2(EC2 + EIP). broker IP 등록 정책에 가장 적합.
- crawler / preprocessor / research: 1순위 OPT-1. KRX 로그인 / Selenium 안정성이 부족하면 OPT-3로 승격.
- aws-paper와 aws-live 모두 OPT-4(NAT Instance)는 보조 옵션으로만 유지. 기본은 OPT-1과 OPT-2 조합.

### VPC Endpoint 권고

| Endpoint | 종류 | 필요 이유 | 활성 환경 |
|----------|------|----------|-----------|
| `s3` | Gateway | ECR layer 다운로드 / S3 backup / research report 저장. Gateway는 무료. | aws-paper, aws-live |
| `ecr.api` | Interface | ECR API 호출(authorization, manifest). | aws-paper, aws-live |
| `ecr.dkr` | Interface | 이미지 layer pull. | aws-paper, aws-live |
| `secretsmanager` | Interface | ECS Task / EC2 startup secret 주입. | aws-paper, aws-live |
| `ssm` | Interface | SSM Session Manager 운영자 접속, Parameter Store 조회. | aws-paper, aws-live |
| `logs` | Interface | ECS / EC2 / RDS 로그 송신(awslogs driver, CW agent). | aws-paper, aws-live |
| `sts` | Interface | OIDC role assume 등 일부 SDK 흐름. | 옵션. NAT-free에서 SDK가 STS public endpoint에 접근 필요할 때 활성. |
| `kms` | Interface | CMK 사용 시 암호화/복호화 호출. | 옵션. CMK 도입 시 활성. |

비용 영향(서울 기준 근사)

- Interface Endpoint AZ당 ~$8/월 + 데이터 처리.
- 권고 5종 × 2 AZ ≈ ~$80/월 (multi-AZ).
- aws-paper에서 단일 AZ로만 활성화하면 ~$40/월로 절감 가능. 단 Endpoint 가용성이 1 AZ 장애에 취약.
- NAT Gateway 미사용으로 ~$43~$86/월 절감되므로 net 효과는 비용 절감 방향.

## Security Group 설계

| SG | inbound | outbound | 비고 |
|----|---------|----------|------|
| sg-marketconnector-ec2 | sg-port-view-ecs (TCP 5000 또는 운영자 결정 포트) | 0.0.0.0/0 (broker), sg-rds-postgres (5432), VPC Endpoints (443) | KIS broker outbound, EIP 기반 IP 등록 |
| sg-port-view-ecs | (외부 미허용. SSM 포트포워딩 또는 internal ALB 도입 시 ALB SG에서 들어옴) | 0.0.0.0/0 (Slack), sg-marketconnector-ec2, sg-rds-postgres, VPC Endpoints | Daily Batch 호출 시 ECS RunTask API |
| sg-strategy-tasks | (없음) | sg-marketconnector-ec2, sg-rds-postgres, VPC Endpoints, 0.0.0.0/0 (Slack 등 필요 시) | Step Functions에서 RunTask로 기동 |
| sg-crawler-tasks | (없음) | 0.0.0.0/0 (KRX/Naver/yfinance), sg-rds-postgres, VPC Endpoints | public subnet 배치 시 inbound 절대 미허용 |
| sg-preprocessor-tasks | (없음) | 0.0.0.0/0 (holiday API), sg-rds-postgres, VPC Endpoints | |
| sg-research-batch | (없음) | sg-rds-postgres, VPC Endpoints, S3 (Gateway) | 인터넷 outbound 거의 없음. private app subnet 배치 가능 |
| sg-rds-postgres | sg-marketconnector-ec2 (5432), sg-port-view-ecs (5432), sg-strategy-tasks (5432), sg-crawler-tasks (5432), sg-preprocessor-tasks (5432), sg-research-batch (5432) | (없음) | 0.0.0.0/0 inbound 절대 금지 |
| sg-vpc-endpoints | 위 app SG들 (TCP 443) | (없음) | Interface Endpoint 전용 |
| sg-port-view-alb (옵션) | 운영자 IP allowlist 또는 인증 게이트 (TCP 443) | sg-port-view-ecs (HTTP) | ALB 도입 시 추가 |

운영자 접근 정책

- EC2 SSH 22 inbound 0.0.0.0/0 금지.
- SSM Session Manager만 사용. EC2 IAM Role에 `AmazonSSMManagedInstanceCore` 부여.
- 운영자가 port-view 콘솔에 접근하려면: (a) ALB 미사용 시 SSM 포트포워딩, (b) ALB 도입 시 internal ALB + 인증 게이트.

## RDS for PostgreSQL 구성

### 환경별 인스턴스

| 환경 | 인스턴스 | AZ | storage | IOPS | encryption | PI | backup retention | PITR | 비고 |
|------|---------|----|---------|------|------------|----|------------------|------|------|
| local-dev | 해당 없음 | 해당 없음 | 해당 없음 | 해당 없음 | 해당 없음 | 해당 없음 | 해당 없음 | 해당 없음 | 기존 로컬 PostgreSQL 유지 |
| aws-paper | db.t4g.small | single-AZ | gp3 50 GB | baseline | on (KMS default) | on | 7일 | on | 1차 구축. 단일 인스턴스 다운타임 허용 |
| aws-live (비용 절감안) | db.t4g.medium | single-AZ | gp3 100 GB | baseline | on (KMS default) | on | 14일 | on | AZ 장애 시 PITR로만 복구 |
| aws-live (안정성 우선안) | db.t4g.medium 또는 db.m6g.large | multi-AZ | gp3 100~200 GB | baseline + 필요 시 provisioned | on (CMK 권고) | on | 14일 | on | 자동 failover, 무중단 minor patch |

PostgreSQL 버전 16 이상 권고. minor version auto upgrade는 aws-paper / aws-live 활성.

### Parameter Group 권고

- `rds.force_ssl = 1` (aws-paper, aws-live)
- `log_min_duration_statement = 1000` (aws-live)
- `log_lock_waits = 1`
- `idle_in_transaction_session_timeout = 60000`
- `statement_timeout`은 application에서 설정. RDS parameter는 0 유지.

### Subnet Group / Network

- DB subnet group: data-a + data-b (multi-AZ 가능 형태로 구성. single-AZ 인스턴스라도 group은 두 subnet으로).
- VPC SG: sg-rds-postgres.
- Publicly accessible: false.

### portfolio DB와 10개 schema 유지

- 데이터베이스 이름: `portfolio` (모든 환경 동일).
- schema: `reference, interest, preprocessor, research, decision, execution, connector, ops, legacy, public`.
- MS별 search_path는 README 정의 그대로 유지. 본 spec에서 변경 금지.

### 환경변수 호환성

- 기존 키 유지: `INTEREST_DB_HOST`, `INTEREST_DB_PORT`, `INTEREST_DB_NAME`, `INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`, `PORTFOLIO_DB_NAME`, `PORT_ACCOUNT_NO`, `PORT_BROKER_NAME`, `PORT_ENVIRONMENT`, `PORT_STRATEGY_NAME`, `PORT_STRATEGY_VERSION`, `PORT_MAX_ORDER_AMOUNT_RATIO`, `PORT_MIN_ORDER_AMOUNT`.
- AWS 측에서는 ECS Task Definition / EC2 user data가 같은 키 이름으로 RDS endpoint와 secret 값을 주입. Secrets / Parameter Store 매핑 최종 결정은 `06-secrets-and-iam`에서.
- 모든 secret은 `[REDACTED]`로만 기록한다.

## DB Role 권한 매트릭스

### Role 정의

| Role | 사용 MS | 주요 책임 |
|------|---------|----------|
| `marketconnector_app` | port-marketconnector | broker / order / connector snapshot 쓰기 |
| `crawler_app` | port-interest-crawler | interest / reference 쓰기 |
| `preprocessor_app` | port-interest-preprocessor | preprocessor 쓰기, interest / reference 읽기 |
| `decision_app` | port_strategy_decision | decision 쓰기, 다수 schema 읽기 |
| `execution_app` | port_strategy_execution | execution 쓰기, connector / decision / research 읽기 |
| `research_app` | port_strategy_research | research 쓰기, 다수 schema 읽기 |
| `view_app` | port-view | 모든 schema READ + ops WRITE 기본. execution write 필요성은 `05-port-view-ecs-and-runbook`에서 재검토 |

### schema별 권한 매트릭스 (R = SELECT, W = INSERT/UPDATE/DELETE)

| schema | marketconnector_app | crawler_app | preprocessor_app | decision_app | execution_app | research_app | view_app |
|--------|---------------------|-------------|------------------|--------------|---------------|--------------|----------|
| reference | R | R/W | R | R | R | R | R |
| interest | - | R/W | R | - | - | R | R |
| preprocessor | - | - | R/W | R | - | R | R |
| research | - | - | - | R | R | R/W | R |
| decision | - | - | - | R/W | R | - | R |
| execution | R/W | - | - | - | R/W | - | R (write는 05에서 재검토) |
| connector | R/W | - | - | R | R | - | R |
| ops | - | - | - | - | - | - | R/W |
| legacy | R | R | - | - | - | - | R |
| public | R/W (호환) | R | R | R | R | R | R |

권한 부여 SQL 패턴(예시, 실제 실행은 본 spec 범위 밖):

- `GRANT USAGE ON SCHEMA <schema> TO <role>;`
- `GRANT SELECT ON ALL TABLES IN SCHEMA <schema> TO <role>;`
- `GRANT INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA <schema> TO <role>;` (write 권한이 있는 schema에만)
- `ALTER DEFAULT PRIVILEGES IN SCHEMA <schema> GRANT SELECT ON TABLES TO <role>;`

각 role 비밀번호는 Secrets Manager 또는 SSM SecureString 후보. 최종 결정은 `06-secrets-and-iam`. 본 문서에는 `[REDACTED]`로만 표기.

`INTEREST_DB_USER`에 들어가는 값은 MS별로 다르다. 환경변수 키 자체는 호환되며 값만 role 단위로 분기.

## 데이터 cutover 절차

### 결정

- 1순위: `pg_dump` + `pg_restore`. 단일 instance, 단일 DB, 짧은 cutover window 허용 가정.
- 보류: AWS DMS. 무중단이 꼭 필요해질 때 별도 spec(`10-cutover-and-validation-runbook`)에서 재검토.

### 단계 개요 (local-dev → aws-paper)

1. 운영자가 모든 MS 정지(특히 broker 주문, fill sync, intraday monitor).
2. local-dev PostgreSQL `portfolio` DB에 대해 `pg_dump --format=custom --no-owner --no-privileges` 수행.
3. aws-paper RDS에 미리 schema 10개와 role 7개를 생성한다.
4. `pg_restore --no-owner --no-privileges --jobs=N`로 데이터 복원.
5. role 권한 매트릭스 SQL 실행.
6. 검증 SQL 실행(다음 절).
7. 환경변수(`INTEREST_DB_HOST`, `INTEREST_DB_PASSWORD` 등)를 RDS endpoint와 Secrets로 교체(코드 수정 없음, 환경 주입만).
8. aws-paper에서 MS 가동 → 검증.
9. paper 검증 N영업일 통과 후 aws-live cutover로 진행(별도 spec 10).

### 검증 SQL 후보

```
-- schema 목록
SELECT schema_name FROM information_schema.schemata WHERE schema_name NOT LIKE 'pg_%';

-- role 목록
SELECT rolname FROM pg_roles WHERE rolname NOT LIKE 'pg_%';

-- schema별 table 수
SELECT table_schema, count(*) FROM information_schema.tables
WHERE table_schema IN ('reference','interest','preprocessor','research','decision','execution','connector','ops','legacy','public')
GROUP BY table_schema ORDER BY table_schema;

-- 핵심 테이블 row count (정합성 점검)
SELECT 'execution.strategy_execution_order'  AS t, count(*) FROM execution.strategy_execution_order  UNION ALL
SELECT 'connector.connector_order_request'   AS t, count(*) FROM connector.connector_order_request   UNION ALL
SELECT 'decision.strategy_daily_signal'      AS t, count(*) FROM decision.strategy_daily_signal      UNION ALL
SELECT 'preprocessor.pre_total_market_daily_feature' AS t, count(*) FROM preprocessor.pre_total_market_daily_feature;

-- search_path 확인 (해당 role로 접속 후)
SHOW search_path;
```

### Rollback 기준

- 검증 SQL의 row count가 local-dev와 ±0이 아닌 차이가 발견되면 즉시 cutover 중단.
- application 가동 후 broker 호출 실패 또는 connector_order_request insert 실패가 감지되면 즉시 환경변수를 local-dev endpoint로 되돌린다.
- aws-paper RDS 인스턴스는 삭제하지 않고 evidence로 보존. 다음 시도에서 재사용 가능.

## RDS backup / snapshot / PITR

| 정책 | local-dev | aws-paper | aws-live |
|------|-----------|-----------|----------|
| automated backup | 해당 없음 | 7일 | 14일 |
| PITR | 해당 없음 | on | on |
| manual snapshot | 해당 없음 | cutover 직전 + 분기 | cutover 직전 + 분기 |
| 복원 절차 | 해당 없음 | snapshot → 새 instance → endpoint 교체 | 동일. multi-AZ 권고 시 안정성 우선안 |

## 안전장치와 자동 재시도 정책

### aws-paper

- 1단계 (주문 차단 모드): MS는 정상 가동하되 connector BUY/SELL 호출은 차단(ENV flag 또는 SG 단계 정책). fill sync / position sync는 동작시켜 흐름 검증.
- 2단계 (자동 BUY/SELL E2E): 모의투자 broker 연결을 켜고 자동 BUY/SELL을 흘려본다. 중복 주문 / fill sync / position sync 오류를 검증.
- 운영자 결정 후 단계 전환. paper에서도 운영 안정성 기준 적용.

### aws-live

- 초기 자동주문 금지. 후보 생성 + View 수동 승인 중심으로 운영.
- 충분한 검증 후 제한적 자동화 검토(예: 일부 종목 / 일부 시간대만).
- BUY / SELL / fill sync 결과 반영 / position 변경 / intraday stop SELL 생성은 자동 재시도 금지.
- idempotent step만 자동 재시도 허용:
  - 시세 조회
  - 전처리 idempotent step(예: upsert)
  - holiday API
  - yfinance / Naver REST 일시 오류

### 환경 분리 안전장치

- aws-paper에서 aws-live broker로 호출이 발생하지 않도록 다음을 분리한다.
  - `PORT_ENVIRONMENT` 환경변수 값을 `paper` / `live`로 명확히 분기.
  - KIS app key / app secret을 환경별 Secret으로 분리(`/portfolio/paper/...` vs `/portfolio/live/...`).
  - SG는 환경 별도 RDS / EC2 / ECS Task에만 inbound 허용.
  - Slack webhook도 환경별로 분리(권고). 결정은 06.

## 비용 영향 요약

세부 단가는 루트 공통 문서 [`../_common/cost-simulation.md`](../_common/cost-simulation.md)와 본 spec의 [`decision-matrix.md`](./decision-matrix.md) 참고.

- NAT Gateway 미사용으로 paper / live 모두 ~$43~$86/월 절감. 그러나 VPC Endpoint AZ당 ~$8/월씩 누적.
- aws-paper RDS single-AZ 시작으로 multi-AZ 대비 약 50% 절감.
- aws-live RDS single-AZ 시작은 비용 절감안. 안정성 우선안은 multi-AZ.
- ALB 미사용으로 ~$16.5/월 절감. SSM 포트포워딩 또는 internal ALB는 운영자 결정.
- CloudWatch Logs retention 단축으로 storage 비용 절감.
- Secrets Manager 항목 수는 `06`에서 결정. SSM SecureString 활용 시 일부 비용 절감 가능.

비용 절감안 vs 안정성 우선안의 구체 표는 `decision-matrix.md`.

## AWS Console 단계별 진행 흐름 (요약)

1. VPC 생성 (`portfolio-vpc`, 10.0.0.0/16, 2 AZ)
2. Subnet 생성 (public / app / data, 각 2개)
3. IGW 생성 + VPC attach
4. NAT 미사용. NAT Gateway / NAT Instance 생성 단계 없음.
5. Route Table 생성 / 연결 (public → IGW, app / data → 외부 라우트 없음)
6. Security Group 생성 + 규칙 채우기
7. VPC Endpoint 생성 (S3 Gateway + ECR api+dkr / Secrets / SSM / Logs Interface)
8. RDS subnet group 생성
9. RDS parameter group 생성
10. (사전) DB master password를 Secrets Manager에 등록 (`06-secrets-and-iam` spec과 협업)
11. RDS instance 생성 (aws-paper)
12. RDS endpoint 확인 / SG 연결 확인
13. schema 10개 / role 7개 생성 SQL 실행 (psql / pgAdmin via SSM)
14. 권한 매트릭스 SQL 실행
15. 검증 SQL 실행
16. cutover 사전 점검(실제 cutover 통합 실행은 `10-cutover-and-validation-runbook`)

각 단계의 세부 task와 검증 / rollback은 `tasks.md` 참고.

## MS별 AWS 서비스 후보 비교

8개 MS 각각의 AWS 컴퓨트 / orchestration / 보조 서비스 후보 비교와 포트폴리오 어필 관점 보강안은 루트 공통 문서 [ms-aws-service-decision-matrix.md](../_common/ms-aws-service-decision-matrix.md)를 참고한다.

비교 대상 서비스:

- 컴퓨트: EC2 / ECS Fargate / ECS on EC2 / AWS Batch / Lambda / EKS / Elastic Beanstalk / App Runner
- Orchestration: Step Functions / EventBridge Scheduler 등

본 design.md는 1차 cutover 운영 안정성 권고만 다룬다. 후보 비교 / Lambda 검토 / EKS 검토 / Beanstalk·App Runner 비교는 해당 문서로 분리한다.

8개 MS 컴퓨트 1순위 결정(OD-MS-001 ~ OD-MS-010)은 루트 공통 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 9장에 락 상태로 기록되어 있다.

Lambda는 모든 핵심 batch 워크로드(decision / execution / research / crawler / preprocessor)에서 비권고이며, 인프라 알람 fan-out / 짧은 후처리 / S3 metadata 같은 보조 용도로만 사용한다.

권고 근거:

- Daily Batch 16단계 실측
  - crawler 약 9분 53초 / preprocessor 약 5분 54초
  - research 약 45초 / decision · execution 단발 step 1초 미만
- live BUY/SELL 자동 재시도 금지 정책

자세한 내용은 `../_common/ms-aws-service-decision-matrix.md` 1.4장과 5장을 참고한다.

## 본 spec의 안전 제약

- 본 spec(02 폴더) 내부 산출물 4개 파일:
  - [`requirements.md`](./requirements.md), [`design.md`](./design.md), [`tasks.md`](./tasks.md), [`decision-matrix.md`](./decision-matrix.md)
- 루트 공통 참조 문서:
  - [`../_common/operator-decisions.md`](../_common/operator-decisions.md), [`../_common/aws-resource-glossary.md`](../_common/aws-resource-glossary.md)
  - [`../_common/ms-aws-service-decision-matrix.md`](../_common/ms-aws-service-decision-matrix.md), [`../_common/cost-simulation.md`](../_common/cost-simulation.md)
- 8개 MS의 README / AGENTS.md / CHANGELOG / docs / worklog와 소스 코드는 수정하지 않는다.
- 실제 AWS 리소스 생성 / 변경은 본 spec에서 수행하지 않는다. 본 문서는 운영자가 Console / IaC로 진행하기 위한 절차서다.
- 8개 MS entrypoint, broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / 주문 / 체결 / Daily Batch / intraday monitor 호출은 본 작업에 포함되지 않는다.
- 모든 secret은 `[REDACTED]`로만 표기한다.
- Secrets / IAM 매트릭스 최종 결정은 `06-secrets-and-iam` spec에서 다룬다. 본 spec은 placeholder만 남긴다.
