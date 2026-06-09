# 02 AWS Network and RDS — 운영자 요약

운영자가 매일 빠르게 보는 한 장 요약 문서다. 자세한 설계는 `design.md`, 실행 순서는 `runbook.md`, 점검 항목은 `validation-checklist.md`를 본다. 결정값 변경은 `../_common/operator-decisions.md`에서만 한다.

## 1. 현재 상태

- aws-paper 1차 Foundation은 운영자가 AWS Console로 직접 구축 완료했다(VPC / Subnet 6개 / IGW / Route Table 3종 / SG 8개 / VPC Endpoint 6종 / RDS Subnet Group / Parameter Group / RDS instance / Secrets Manager metadata).
- RDS는 `data` private subnet에 배치되어 있고 Publicly accessible = No, deletion protection = Enabled, backup retention = 7일 상태다.
- Kiro ReadOnly 자동 검증으로 Network / SG / VPC Endpoint / RDS 항목은 기대값과 일치(불일치 0건). validation-checklist 라벨은 [O] 78 / [X] 0 / [Kiro 후속] 2 / [운영자] 20.
- 현재 단계는 DB Migration(03 spec)과 MarketConnector AWS 포팅(후속 spec) 진입 전 최종 확인 단계다.

## 2. 운영자가 해야 할 다음 작업

1. 로컬 PostgreSQL 기준으로 도메인별 테이블 row count snapshot을 만들어 cutover 전 비교 기준선을 확보한다.
2. RDS `portfolio-paper-rds`에 `psql`로 접속해 `portfolio` DB / 도메인별 schema / role 7종 생성 SQL 실행 합의(03 / 06 spec 진입)를 마무리한다.
3. MarketConnector EC2 포팅 전에 EIP / Security Group / Secrets Manager / SSM Parameter Store 사용 기준을 확정한다.

## 3. 이미 완료한 AWS 리소스

| 영역 | 완료 항목 | 상태 | 근거 문서 |
|---|---|---|---|
| Region | `ap-northeast-2` 사용 | 완료 | [Operation Notes](./operation-notes.md) |
| VPC | `portfolio-vpc` (10.0.0.0/16) | 완료 | [Operation Notes](./operation-notes.md) |
| Subnet | public / app / data 각 2개 (총 6개) | 완료 | [Validation Checklist §2](./validation-checklist.md) |
| Internet Gateway | `portfolio-igw` attach | 완료 | [Validation Checklist §2](./validation-checklist.md) |
| NAT | NAT Gateway / NAT Instance 미사용 | 완료 | [Validation Checklist §2](./validation-checklist.md) |
| Route Table | `rt-public` / `rt-app` / `rt-data` 3종 | 완료 | [Validation Checklist §2](./validation-checklist.md) |
| Security Group | `sgroup-*` 8종 | 완료 | [Validation Checklist §3](./validation-checklist.md) |
| VPC Endpoint | S3 Gateway + Interface 5종(ECR api/dkr, Secrets Manager, SSM, Logs) | 완료 | [Validation Checklist §4](./validation-checklist.md) |
| RDS Subnet Group | `portfolio-paper-subnet-group` | 완료 | [Validation Checklist §5](./validation-checklist.md) |
| RDS Parameter Group | `pg-portfolio-paper` (postgres16) | 완료 | [Validation Checklist §5](./validation-checklist.md) |
| RDS instance | PostgreSQL 16, `db.t4g.small`, Single-AZ | 완료 | [Validation Checklist §5](./validation-checklist.md) |
| Secrets Manager | `/portfolio/paper/rds/master` (값은 `[REDACTED]`) | metadata 등록 | [Operation Notes](./operation-notes.md) |
| RDS Public access | `Publicly accessible = No` | 완료 | [Validation Checklist §5](./validation-checklist.md) |
| Backup / Deletion | retention 7일, deletion protection on | 완료 | [Validation Checklist §5](./validation-checklist.md) |

본 README에는 RDS endpoint hostname / secret value / account-id / access key id를 적지 않는다. 접속 상세는 `operation-notes.md`와 안전한 로컬 환경변수에서 확인한다.

## 4. 관련 문서 링크

### 운영자가 자주 보는 문서

- [Runbook](./runbook.md)
- [Validation Checklist](./validation-checklist.md)
- [Operation Notes](./operation-notes.md)
- [Operator Decisions](../_common/operator-decisions.md)
- [Cost Simulation](../_common/cost-simulation.md)

### Kiro / 감사용 문서

- [Requirements](./requirements.md)
- [Design](./design.md)
- [Tasks](./tasks.md)
- [Decision Matrix](./decision-matrix.md)
- [Traceability Matrix](./traceability-matrix.md)
- [Risk Register](../_common/risk-register.md)

## 5. 자동 갱신 기준

- AWS 리소스 생성 / 변경 / 검증 결과는 기본적으로 `operation-notes.md`에 기록한다.
- 체크리스트 통과 여부는 `validation-checklist.md`에 기록한다.
- 운영자 결정이 바뀌면 `../_common/operator-decisions.md`를 갱신한다.
- 본 README는 상태 요약이 달라질 때만 짧게 갱신한다.
- 작업 완료 후 자동 갱신 파일은 기본적으로 최대 3개로 제한한다.
