# 06 Secrets and IAM — 운영자 요약

운영자가 매일 빠르게 보는 한 장 요약 문서다. 자세한 내용은 `requirements.md`를 본다. `design.md` / `tasks.md` / `runbook.md` / `validation-checklist.md` / `operation-notes.md`는 후속 phase에서 만든다. 결정값 변경은 `../_common/operator-decisions.md`에서만 한다.

## 1. 현재 상태

- `requirements.md` 생성 완료. `design.md` / `tasks.md` / `runbook.md` / `validation-checklist.md` / `operation-notes.md`는 아직 후속 phase.
- 1차 적용 환경은 `aws-paper`, region은 `ap-northeast-2`, 1차 적용 대상은 MarketConnector EC2.
- 선행 spec `02-aws-network-and-rds`는 1차 적용 완료. VPC / Subnet / SG / VPC Endpoint(Secrets Manager / SSM / CloudWatch Logs / ECR / S3) / RDS PostgreSQL / DB role 7종이 운영자 직접 작업으로 적용되어 있다.
- MarketConnector EC2(Amazon Linux 2023, public subnet, EIP attach)는 이미 생성되어 있다.
- 본 spec 작업 시점 오전에 운영자는 shell 환경변수 기반으로 KIS paper API 조회 / RDS `marketconnector_app` 접속 / `connector_balance.py` / `connector_order_check.py` 실행 / Flask 조회성 endpoint smoke test를 통과시켰다.

## 2. 이 spec의 목적

- MarketConnector EC2 안에 IAM access key id / secret access key를 절대 저장하지 않는다.
- EC2 Instance Role 자격증명만으로 Secrets Manager / SSM Parameter Store에서 필요한 값만 read 한다.
- KIS / RDS 민감정보를 코드와 문서에서 분리한다.
- 03 / 08 / 04 / 05 / 09 spec의 ECS Task Role이 동일 패턴(Task Role 기반 secret / parameter read)을 재사용할 수 있게 골격을 남긴다.

## 3. 이번에 할 것

- KIS app key / app secret / paper 계좌번호 관련 값(`PAPER_ACNT`, `ACNT_PRDT_CD`)의 Secrets Manager 보관 후보 정리.
- RDS `marketconnector_app` 접속정보(host / port / db name / user / password)의 Secrets Manager 보관 후보 정리.
- KIS base URL / Connector Flask host / port / debug / `PORT_ENVIRONMENT` / `PORT_BROKER_NAME` 등 일반 설정의 SSM Parameter Store 보관 기준 정리.
- MarketConnector EC2 Instance Role / Instance Profile 최소 권한 설계(허용 action: `secretsmanager:GetSecretValue`, `secretsmanager:DescribeSecret`, `ssm:GetParameter`, `ssm:GetParameters`, `ssm:GetParametersByPath`. Resource는 정확한 ARN 또는 `/portfolio/paper/marketconnector/*` prefix 만, `Resource: "*"` 금지).
- EC2 내부 Access Key 미사용 원칙 명문화(IMDSv2 + Instance Role 자격증명만 사용, `~/.aws/credentials` 같은 long-lived access key 파일 금지).
- naming 규칙 `/portfolio/{env}/{service}/{item}` 1차 권고(env는 `paper` / `live`, 본 spec 1차 확정 service는 `marketconnector` / `rds`).

## 4. 이번에 안 할 것

- live rotation 자동화(Lambda rotation, scheduled rotation).
- GitHub Actions OIDC / CI/CD Role 설계(07 spec).
- 8개 MS 전체 full IAM 매트릭스(03 / 08 / 04 / 05 / 09 spec에서 패턴 재사용 형태로 진행).
- aws-live IAM 설계(10-cutover-and-validation-runbook 통합).
- 실제 AWS 리소스(Secrets Manager secret, SSM Parameter, IAM Role / Policy / Instance Profile, EC2 attach 변경) 생성 / 수정 / 삭제. 모두 운영자가 직접 수행한다.
- 8개 MS의 README / AGENTS.md / CHANGELOG / docs / worklog / 소스 코드 수정.

## 5. 운영자 결정 후보

- `OD-SEC-001` Secrets 보관 위치 — 본 spec design 단계에서 🔴 미정 → 🟢 확정 또는 🟡 잠정으로 락 예정.
- `OD-OBS-004` Slack webhook 보관 위치 — Secrets Manager(권고) vs SSM SecureString(절감안) 비교 후 본 spec에서 최종 락 예정.
- `OD-SEC-XXX` MarketConnector EC2 / 8개 MS 모두 Access Key 미사용 원칙(신규 후보).
- `OD-SEC-XXX` EC2 / ECS IAM Role 기반 secret / parameter read 원칙, Resource wildcard 금지(신규 후보).

## 6. 다음 작업

- `design.md` 작성 — 분류 기준 / naming 규칙 / Instance Role·Task Role 매트릭스 / OD·R·후속 spec 분담.
- `tasks.md` 작성 — _common 문서 갱신 작업, 03 / 08 입력 인계 작업 분해.
- `runbook.md` 작성 — Secrets / Parameter 등록 → Instance Role / Profile 생성 → attach → EC2 검증 → Connector smoke test 단계, [실행] / [확인] / [준비] / [복구] 라벨.
- `validation-checklist.md` 작성 — `[O]` / `[X]` / `[Kiro 후속 작업 필요]` / `[운영자 확인 필요]` 4종 라벨, Resource wildcard 0건 검증, EC2 access key 파일 미존재 검증, `aws sts get-caller-identity` 결과가 Instance Role assumed-role ARN 인지 검증.
- `operation-notes.md` 작성 — 일자별 누적 기록, 실제 secret value / 계좌번호 / account-id / RDS endpoint hostname / 실제 secret ARN 미기록(모두 `[REDACTED]`), secret 조회 결과는 성공/실패만 기록.

## 7. 한 줄 요약

06의 핵심은 MarketConnector EC2가 Access Key 없이 Instance Role 만으로 Secrets Manager / SSM Parameter Store에서 KIS / RDS secret과 일반 설정값을 최소 권한으로 read 하는 운영 구조를 1차 확정하는 것이다.

## 8. 관련 문서 링크

### 운영자가 자주 보는 문서

- [Requirements](./requirements.md)
- [Operator Decisions](../_common/operator-decisions.md)
- [Followups Overview](../_common/followups-overview.md)
- [Risk Register](../_common/risk-register.md)

### 후속 phase에서 만들 문서

- `design.md`
- `tasks.md`
- `runbook.md`
- `validation-checklist.md`
- `operation-notes.md`

### 선행 spec

- [01 AWS Migration Foundation — Requirements](../01-aws-migration-foundation/requirements.md)
- [02 AWS Network and RDS — 운영자 요약](../02-aws-network-and-rds/README.md)

본 README에는 실제 secret value, KIS app key / app secret, 계좌번호, RDS endpoint hostname, account-id, 실제 secret ARN, IAM access key id를 적지 않는다. 모두 `[REDACTED]` 또는 placeholder로만 표기한다.
