# Operation Notes — 06-secrets-and-iam

본 문서는 06-secrets-and-iam 진행 중 운영자 / Kiro 가 실제 수행한 작업 결과를 일자별로 누적 기록하는 운영 노트다. 1차 적용 환경은 `aws-paper`, region 은 `ap-northeast-2`, 1차 적용 대상은 MarketConnector EC2.

기록 형식

- 일자별 `## YYYY-MM-DD <요약>` 헤더로 누적한다(02 spec operation-notes 와 동일).
- 항목 끝 `[운영자 기록]` 자리는 운영자가 실제 작업 후 결과(`성공` / `실패` / `보류` / `해당 없음`)를 짧게 채운다.
- 실패 / 보류 시에는 1줄 사유만 적는다. JSON 본문 / 에러 메시지 전체 인용 금지(보안 / 분량 절감).
- IAM Role / Policy 변경은 변경 일자 / 변경자 / 변경 사유 / 변경 전·후 항목 요약(JSON 본문 전체 인용 금지) 4줄로 요약한다.

안전 원칙

- 실제 secret value, KIS app key, KIS app secret, 계좌번호, RDS endpoint hostname, RDS password, token, Slack webhook URL, IAM access key id, account-id, 실제 secret ARN, 실제 KMS Key ARN 은 본 문서에 평문 기록 금지. 모두 `[REDACTED]` 또는 placeholder.
- secret 조회 결과 자체(value)는 기록 금지. `성공 / 실패` 와 마지막 갱신 시각(필요 시 ISO 8601 `YYYY-MM-DDTHH:MM:SS+09:00`)만 기록.
- 실제 AWS 리소스 생성 / 수정 / 삭제는 운영자가 직접 수행한다. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행한다.
- 8개 MS(`port-view`, `port-marketconnector`, `port-interest-crawler`, `port-interest-preprocessor`, `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`) 의 README / AGENTS.md / CHANGELOG / docs / worklog / 소스 코드 미수정.
- `secretsmanager:GetSecretValue` 실호출은 운영자만. Kiro 자동 검증은 `secretsmanager:DescribeSecret` metadata 만.

## 2026-06-10 06-secrets-and-iam 문서화 진행

- [`./requirements.md`](./requirements.md) 생성 완료
- [`./README.md`](./README.md) 생성 완료
- [`./design.md`](./design.md) 생성 완료
- [`./tasks.md`](./tasks.md) 생성 완료
- [`./runbook.md`](./runbook.md) 생성 완료
- [`./validation-checklist.md`](./validation-checklist.md) 생성 완료
- [`./operation-notes.md`](./operation-notes.md) 생성 완료

본 일자에는 AWS 리소스 생성 / 수정 / 삭제 0건. 8개 MS 코드 / 문서 변경 0건. 외부 API 호출 0건. 본 spec 폴더 안 7개 문서만 신규 생성.

## 2026-06-10 실제 AWS 작업 기록 템플릿

운영자가 [`./runbook.md`](./runbook.md) 단계를 실제 수행한 뒤 본 섹션을 채운다. 각 항목 끝의 `[운영자 기록]` 자리에 결과만 적는다(secret value / 계좌번호 / endpoint hostname / account-id / 실제 ARN 미기록).

### 1. Secrets Manager

- `/portfolio/paper/marketconnector/kis-app-key` 생성 여부: [운영자 기록]
- `/portfolio/paper/marketconnector/kis-app-secret` 생성 여부: [운영자 기록]
- `/portfolio/paper/marketconnector/paper-account` 생성 여부 (JSON multi-key `PAPER_ACNT` / `ACNT_PRDT_CD`): [운영자 기록]
- `/portfolio/paper/rds/marketconnector-app` 생성 여부 (JSON multi-key `host` / `port` / `dbname` / `username` / `password`): [운영자 기록]
- 기존 `/portfolio/paper/rds/master` 유지 여부 (이름 / KMS / 마지막 수정 시각이 02 spec 시점과 일치): [운영자 기록]
- 위 5건 외 신규 secret 추가 등록 여부 (있다면 0건이 정상): [운영자 기록]
- 실제 secret value 본 문서 / 콘솔 캡처 / 운영자 노트 평문 기록 0건 확인: [운영자 기록]

### 2. SSM Parameter Store

- `/portfolio/paper/marketconnector/kis-base-url` 생성 여부: [운영자 기록]
- `/portfolio/paper/marketconnector/connector-host` 생성 여부: [운영자 기록]
- `/portfolio/paper/marketconnector/connector-port` 생성 여부: [운영자 기록]
- `/portfolio/paper/marketconnector/connector-debug` 생성 여부: [운영자 기록]
- `/portfolio/paper/marketconnector/environment` 생성 여부 (Value=`paper`): [운영자 기록]
- `/portfolio/paper/marketconnector/broker-name` 생성 여부: [운영자 기록]
- parameter 이름 자체에 endpoint hostname / 계좌번호 / secret value / password / token 미포함 확인: [운영자 기록]

### 3. IAM Role / Policy / Instance Profile

- IAM Role `portfolio-paper-marketconnector-ec2-role` 생성 또는 확인 여부 (Trust Policy `Service: ec2.amazonaws.com`): [운영자 기록]
- Instance Profile `portfolio-paper-marketconnector-ec2-profile` 생성 또는 확인 여부 (Role attach 포함): [운영자 기록]
- 최소 read policy `portfolio-paper-marketconnector-ec2-readonly` 작성 / attach 여부: [운영자 기록]
- EC2 instance 에 Instance Profile attach 여부 (`describe-iam-instance-profile-associations` 결과 일치): [운영자 기록]
- Resource wildcard `"*"` 사용 0건 확인: [운영자 기록]
- Action wildcard (`secretsmanager:*` / `ssm:*` / `*`) 0건 확인: [운영자 기록]
- 다른 service prefix (`/portfolio/paper/view/*`, `/portfolio/paper/crawler/*` 등) Resource 0건 확인: [운영자 기록]
- 다른 환경 prefix (`/portfolio/live/...`) Resource 0건 확인: [운영자 기록]
- KMS Decrypt statement 적용 여부 (CMK 미사용 시 0건이 정상): [운영자 기록]

### 4. EC2 검증

- `aws sts get-caller-identity` 결과 `Arn` 이 `assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>` 형태 확인 여부 (실제 account-id / instance-id 본 문서 기록 금지): [운영자 기록]
- `aws configure list` 결과 access_key Source 가 `iam-role` 또는 `Ec2InstanceMetadata` 확인 여부: [운영자 기록]
- `~/.aws/credentials` 미존재 확인 여부: [운영자 기록]
- `~/.aws/config` 안 `aws_access_key_id` / `aws_secret_access_key` 라인 0건 확인 여부: [운영자 기록]
- 현재 shell 의 `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` / `AWS_SESSION_TOKEN` 환경변수 미설정 확인 여부: [운영자 기록]
- dotfile (`~/.bashrc` / `~/.profile` / `~/.bash_profile`) 안 access key export 0건 확인 여부: [운영자 기록]
- Secrets Manager `describe-secret` 4건 metadata 정상 반환 여부 (value 미조회): [운영자 기록]
- SSM `get-parameters-by-path /portfolio/paper/marketconnector` 6건 정상 반환 여부: [운영자 기록]
- 다른 service prefix 조회 시 AccessDenied / NotFound (권한 격리 정상) 확인 여부: [운영자 기록]
- 다른 환경 prefix (`/portfolio/live/...`) 조회 시 AccessDenied 확인 여부: [운영자 기록]

### 5. Connector smoke test (조회성만)

- 임시 export 스크립트 실행 후 `INTEREST_DB_*` / `APP_KEY` / `APP_SECRET` / `BASE_URL` / `PAPER_ACNT` / `ACNT_PRDT_CD` / `PORT_ENVIRONMENT` / `PORT_BROKER_NAME` 환경변수 주입 확인 여부 (값 표시 금지): [운영자 기록]
- 임시 export 스크립트 / 환경변수 값 파일 / 로그 / 콘솔 캡처 평문 저장 0건 확인 여부: [운영자 기록]
- RDS `marketconnector_app` 접속 성공 여부 (DDL/DML 미실행, 조회만): [운영자 기록]
- KIS token 발급 또는 기존 `access_token.txt` 재사용 확인 여부 (token 값 표시 금지): [운영자 기록]
- `connector_balance.py` 잔고 조회 성공 여부: [운영자 기록]
- `connector_order_check.py` 주문 / 체결 조회 성공 여부: [운영자 기록]
- Flask 조회성 endpoint smoke test 통과 여부 (잔고 / 보유 / 주문 내역 등): [운영자 기록]
- 신규 주문 / 매수 / 매도 / 취소 / 정정 API 호출 0건 확인 여부 (`connector_buy.py`, `connector_sell.py`, `connector_cancel.py`, `connector_modify.py` 미실행): [운영자 기록]

### 6. 미완료 / 이월

- 미완료 항목: [운영자 기록]
- 이월 항목 (다음 일자로 넘김): [운영자 기록]
- 03-marketconnector-ec2 spec 으로 넘길 항목 (예: `AmazonSSMManagedInstanceCore` attach, CloudWatch Logs write 권한, systemd 정상 운영 모드 전환): [운영자 기록]
- `_common` 갱신 후보 (operator-decisions.md OD-SEC-001 / OD-OBS-004 / OD-SEC-005 / OD-SEC-006, risk-register.md R-SEC 후보 4건, followups-overview.md 06 섹션) 진행 여부: [운영자 기록]

## IAM 변경 기록 템플릿 (필요 시 일자별 추가)

본 spec 의 Permission Policy / Trust Policy / Resource ARN 목록이 변경되는 경우 본 섹션에 누적 기록한다. 변경 1건 = 4줄 요약 형식.

```
## YYYY-MM-DD IAM 변경
- 변경 일자: YYYY-MM-DDTHH:MM:SS+09:00
- 변경자: [운영자 식별자(닉네임 / 직무)]  # 실제 IAM user / email 평문 금지
- 변경 사유: [한 줄 요약]
- 변경 전 / 후 항목 요약: [Resource 추가 / 삭제 항목 수, Action 추가 / 삭제 항목 수, KMS statement 추가 여부 등 — JSON 본문 전체 인용 금지]
```

## 후속 인계

- 본 일자에 06 spec 의 7개 문서(`requirements.md`, `README.md`, `design.md`, `tasks.md`, `runbook.md`, `validation-checklist.md`, `operation-notes.md`) 가 모두 생성됨. 본 spec 의 문서상 닫힘 조건([`./tasks.md`](./tasks.md) task 22) 충족 — 단, 실제 AWS 리소스 작업과 검증 통과는 운영자 후속 작업.
- 03-marketconnector-ec2 spec 진입 시 본 spec 의 §4 Instance Role 매트릭스, §5 Access Key 미사용 원칙, §6 ECS Task Role 골격, §7 OD 후보를 입력으로 받는다([`./tasks.md`](./tasks.md) task 23).
- [`../_common/operator-decisions.md`](../_common/operator-decisions.md), [`../_common/risk-register.md`](../_common/risk-register.md), [`../_common/followups-overview.md`](../_common/followups-overview.md) 갱신은 운영자 승인 시 [`./tasks.md`](./tasks.md) task 16 / 17 / 18 에서 별도 진행.


## 2026-06-17 Daily AWS 17-step E2E 흐름 중 Secrets / IAM / DB Role 권한 사실 기록

운영자가 같은 일자 두 번째 세션(Daily AWS 17-step E2E 완료) 진행 중 본 spec 범위(MarketConnector EC2 Instance Role + Secrets Manager + SSM Parameter Store + DB role 보정 연결)의 사실을 누적 기록한다. 본 spec 자체의 추가 결정 0건 / 본문 변경 0건. 결정 정합 검증과 02 spec 의 DB role / grants 보정 결과 연결만 사실 기록. 자세한 17 step 전체 진행 상태는 03 / 04 / 02 / 08 / 09 spec operation-notes 의 2026-06-17 섹션 참조. Kiro 는 문서 작성 / 절차 정리만 수행. 실제 IAM / Secrets Manager / SSM Parameter Store / GRANT 작업은 운영자 직접 진행. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건. secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 ARN / IAM access key id 본 노트 평문 기록 0건.

### 1. MarketConnector EC2 Instance Role 기반 Secrets Manager / SSM Parameter Store 1차 실증

1. EC2 Instance Role read 결과: 성공
   1) MarketConnector EC2 Instance Role 기반 `secretsmanager:GetSecretValue` 호출 성공(Step 1 `CONNECTOR_BALANCE` / Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` / Step 13 `CONNECTOR_ORDER_CHECK` / Step 17 `BALANCE_REFRESH` 흐름에서 재사용).
   2) JSON SecretString 내부 key 추출 정책 정합(03 spec design.md §8.2.2 정합) — `kis-app-key` / `kis-app-secret` / `paper-account` 등의 SecretString 은 plain string 이 아닌 JSON / 내부 key `APP_KEY` / `APP_SECRET` / `PAPER_ACNT` / `ACNT_PRDT_CD` 추출 후 export.
   3) Access Key 미사용 원칙(OD-SEC-005) 정합 — IMDSv2 + Instance Role only / static credential 0건.
2. value 평문 출력 0건 / key presence 검증: 완료
   1) 운영자가 환경변수 export 시 `length` / `key presence` / `alias presence` 만 확인.
   2) value 평문 stdout / 로그 / 콘솔 캡처 / 운영자 노트 0건(R-DOCS-001 [2026-06-17 보강] / [2026-06-17 보강(17-step E2E)] 정합).
   3) `KIS_*` alias 동시 export 정책(03 spec runbook §2 정합) — `KIS_APP_KEY` / `KIS_APP_SECRET` / `KIS_PAPER_ACNT` / `KIS_ACNT_PRDT_CD` / `KIS_BASE_URL` 5종.
3. OD-SEC-006 1차 실증 메모 보강: 완료
   1) MarketConnector EC2 Instance Role 의 secret read 권한이 KIS Secrets / DB Secrets 한정으로 적용되어 있고 본 일자 17-step 전 구간에서 정상 동작.
   2) MS 별 Secret 접근 분리 정책 정합(2026-06-15 §6 정합 — preprocessor secret 접근은 여전히 `AccessDeniedException` / 정상 동작).
   3) JSON SecretString 내부 key parsing 정책은 OD-SEC-006 본문 변경 없이 1차 실증 메모만 보강(2026-06-17 첫 세션 결정 정합).

### 2. 02 spec DB Role 권한 보정과 본 spec 연결

본 일자 17-step 흐름에서 발견된 DB role 권한 보정 2건은 02 spec 책임 영역이지만 본 spec 의 IAM / Secrets / Role 정책과 연관된다. 사실 정합 검증과 후속 정식 매트릭스 갱신만 사실 기록.

1. `execution_app` 의 `interest` schema 권한 보정(Step 8 영향): 사실 기록
   1) 02 spec operation-notes 2026-06-17 §1 정합 — 운영자 직접 GRANT 보정으로 `DAILY_BUY_EXECUTION` 통과.
   2) 본 spec 의 `decision-app` / `execution-app` Secrets Manager 정책 정합 검증 — Secret 접근 권한은 본 일자 정합 / DB role 의 schema · table · sequence GRANT 부족이 1차 실패 원인이었음.
   3) 02 spec db-roles-and-grants 정식 매트릭스 갱신은 후속 phase.
2. `marketconnector_app` 의 `legacy` schema · `legacy.holdings` · search_path 보정(Step 17 영향): 사실 기록
   1) 02 spec operation-notes 2026-06-17 §2 정합 — 운영자 직접 search_path 보정 + USAGE / DML / sequence GRANT + default privileges 보정으로 `BALANCE_REFRESH` 통과.
   2) 본 spec 의 `marketconnector-ec2-role` Secrets Manager 정책 정합 검증 — Secret 접근 권한은 본 일자 정합 / DB role 의 legacy schema USAGE / DML / sequence / database search_path 부족이 1차 실패 원인이었음.
   3) OD-DB-007(legacy schema 모든 app role 미부여) 의 marketconnector_app 한정 1건 예외 사실은 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) Change Log 2026-06-17 두 번째 항목 / [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-17 §2 / [`../_common/risk-register.md`](../_common/risk-register.md) R-DATA-011 신규 정합. 02 spec 본문 결정값 변경은 후속 분리.
3. 후속 정식 매트릭스 / default privileges / sequence 권한 검증 후보:
   1) 02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) §4 GRANT / §5 검증 SQL 의 `execution_app` / `marketconnector_app` 행 정식 갱신.
   2) future default privileges(`ALTER DEFAULT PRIVILEGES IN SCHEMA <name> GRANT ...`) 정식 정리.
   3) sequence 권한 정식 정리.

### 3. 안전 / 보안 점검 결과

1. 본 일자 작업으로 8개 MS README / AGENTS.md / CHANGELOG / docs / worklog / 소스 / 패키징 변경 0건. 06 spec 본문 결정값 변경 0건 / 운영자 직접 IAM / Secrets / GRANT 변경분은 본 노트에 사실로만 기록.
2. 실제 secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id 본 노트 평문 기록 0건. 모두 `[REDACTED]` 또는 placeholder. 운영 식별자(secret name path `/portfolio/paper/kis/marketconnector` / `/portfolio/paper/rds/marketconnector-app` / `/portfolio/paper/rds/execution-app` / SSM Parameter name path `/portfolio/paper/kis/...` / 환경변수 key 이름 `APP_KEY` / `KIS_APP_KEY` / `PORT_ENVIRONMENT` / `PORT_DB_TARGET` / role 이름 `execution_app` / `marketconnector_app` / schema 이름 `interest` / `legacy`) 만 사실 기록.
3. AWS / IAM / Secrets Manager / SSM Parameter Store / RDS / GRANT 작업은 모두 운영자 직접 수행. Kiro 는 문서 작성 / 절차 정리 / 검증 항목 정리만 수행. AWS CLI / boto3 실행 0건(Kiro 측). `secretsmanager:GetSecretValue` 결과값 평문 기록 0건. CloudWatch Logs 본문 / SSM 응답 본문 / KIS API response body / 운영자 PowerShell stdout 전문 본 노트 평문 인용 0건.
4. JSON SecretString 내부 key parsing 사실은 mapping 사실로만 기록(value 평문 0건). raw SecretString export 금지 정책 1차 실증(R-DOCS-001 [2026-06-17 보강] / [2026-06-17 보강(17-step E2E)] 정합).
5. broker / KIS 호출은 KIS paper BUY 4건(03 spec Step 12) + balance / order check 조회성 한정. SELL / 취소 / 정정 / 추가 `--execute` 호출 0건. live 자동 BUY / SELL E2E 검증은 OD-SAFE-002 / OD-SAFE-003 정책에 따라 후속 검증 / 승인 전까지 여전히 금지. 본 일자는 `aws-paper` 한정 / aws-live 작업 0건.
6. Daily AWS 17-step E2E paper 1차 통과로 본 spec 의 IAM / Secrets / SSM Parameter / DB role 보정 정책이 backend AWS E2E 흐름에서 정상 동작함이 1차 실증 — OD-SEC-005 / OD-SEC-006 / OD-SEC-007 mitigation 정합 / Status 기존 값 그대로 유지(🟡 잠정).


## 2026-06-29 (2) — Fargate port-view Task Role / env 주입 후속 추가

본 일자 port-view 측 commit `e72de6f`(`feat(view): add Step Functions daily batch trigger`) 의 결과로 Fargate 진입 시점에 06 spec 측 추가로 락해야 할 IAM / Secrets / env 주입 정책을 후속으로 남긴다. 본 노트는 port-view 측 코드 본문 / IAM Policy / ASL / 응답 본문 평문 인용 0건(R-DOCS-001 정합).

§1. Fargate port-view Task Role 후속
 1) `states:StartExecution` 최소 권한
   (1) Resource 패턴 한정
       - Fargate port-view Task Role 의 `states:StartExecution` 권한 scope 는 `portfolio-paper-daily-step1-17-approval` state machine ARN 한정 권장
       - Resource · Action wildcard 0건 유지(OD-SEC-005 / OD-SEC-006 / R-AUTO-034 신규 mitigation 정합)
       - Step 12~17 승인형 state machine 이 별도 추가될 경우 동일 패턴으로 한정
   (2) 본 spec 본문 평문 기록 0건
       - 실제 IAM Role ARN / IAM Policy 전체 본문 / state machine ARN 의 account-id 부분(`[REDACTED]` 처리)
 2) 기타 Task Role 권한
   (1) ECR pull / Secrets Manager(specific ARN) / SSM Parameter Store(specific ARN) / CloudWatch Logs `CreateLogStream` + `PutLogEvents` 최소 권한 부여
   (2) Resource 패턴은 환경별 prefix(`/portfolio/paper/...`) 로 좁힘
   (3) ECS Task Definition `executionRoleArn` 과 `taskRoleArn` 분리 유지

§2. Secrets 주입 정책
 1) RDS 접속정보
   (1) image / properties 직접 기록 금지
       - Dockerfile / `application.properties` / `application-aws-paper.properties` 에 RDS host · port · user · password 평문 기록 금지
       - Secrets Manager(또는 SSM SecureString) 주입 / ECS Task Definition `secrets` 블록 또는 startup hook 으로 환경변수 주입
       - `INTEREST_DB_*` 환경변수 패턴 그대로 유지(port-view README 정합)
   (2) Fargate Task 안에서만 환경변수 노출 / 외부 audit log / 운영자 노트 / 콘솔 캡처에 평문 노출 금지(R-DOCS-001 / R-DOCS-002 / R-SEC-010 정합)
 2) KIS secret / Slack webhook
   (1) Fargate View 측은 KIS broker 직접 호출이 없으므로 KIS secret 주입은 본 spec 1차 적용 범위 밖
       - KIS secret 은 03 spec MarketConnector EC2 측 책임으로 유지(OD-SEC-003 / OD-MS-027 정합)
   (2) Slack webhook URL 은 `portfolio-event-notifier` Lambda 측 환경변수로 1차 운영 / Secrets Manager 또는 SSM SecureString 이전은 후속(R-AUTO-024 정합)
       - port-view 측 Slack webhook 사용 정책 변경 없음

§3. Step Functions 식별자 env 주입
 1) `stateMachineArn` / region / `executionNamePrefix`
   (1) `application.properties` 직접 고정 금지
       - port-view `aws-stepfunctions` mode 의 `portfolio.batch.aws-stepfunctions-state-machine-arn` / `portfolio.batch.aws-stepfunctions-region` / `portfolio.batch.aws-stepfunctions-execution-name-prefix` 는 env 또는 config 주입
       - ECS Task Definition `environment` 또는 `secrets` 블록으로 주입
       - 본 spec 본문 평문 기록 0건(`[REDACTED]` 또는 placeholder)
 2) 안전 기본값
   (1) `portfolio.batch.local-file-execution-enabled=false`
   (2) `portfolio.batch.paper-order-enabled=false`
   (3) `portfolio.batch.full-pipeline-execution-enabled=false`
   (4) `portfolio.batch.max-executable-step-order=11`(Step 12 이상은 별도 승인형 phase 도입 후 ENABLE)
   (5) R-AUTO-034 신규 mitigation 정합 / 05 spec 후속 phase 책임

§4. 결정 / 리스크 매핑
 1) 결정 본문 변경 없음
   (1) OD-SEC-005 / OD-SEC-006 / OD-SEC-007 / OD-MS-002 / OD-MS-009 / OD-MS-037 본문 변경 없이 1차 실증 메모 보강
       - 자세한 결정 변경은 `../_common/operator-decisions.md` Change Log 2026-06-29 (2) 항목 참조
 2) 리스크 매핑
   (1) R-AUTO-034 신규 — Fargate View `states:StartExecution` 권한 과다 + Step 12 gate 우회 위험 / Status `Open` / 06 spec 후속 phase 책임
   (2) R-DOCS-001 / R-DOCS-002 / R-SEC-010 정합 — secret value / DB password / Slack webhook URL / Administrator password 평문 기록 금지 정책 그대로 유지

§5. 후속 (06 spec 후속 phase 책임)
 1) Fargate port-view Task Role 정식 정의 + IAM Policy 작성
   (1) `states:StartExecution` Resource 한정 + ECR pull + Secrets Manager(`/portfolio/paper/...`) + CloudWatch Logs 최소 권한
   (2) `simulate-principal-policy` 검증 + Resource · Action wildcard 0건 audit
 2) ECS Task Definition `secrets` / `environment` 블록 정식 작성
   (1) RDS 접속정보 Secrets Manager 주입 / `INTEREST_DB_*` 환경변수 매핑
   (2) `portfolio.batch.aws-stepfunctions-*` env 주입 매핑
 3) DB password rotate(R-SEC-010 신규) 진입 시점에 Fargate port-view 측 secret 재검증
 4) Slack webhook URL Secrets Manager 또는 SSM Parameter Store 이전(R-AUTO-024) 진입 시 port-view 측 secret loader 영향 cross-spec audit

§6. 본 일자 사실 기록 범위
 1) Kiro 작업 = 06 spec `operation-notes.md` 본 섹션 누적만 수행
 2) 운영자 직접 commit `e72de6f` 코드 변경분은 port-view MS 영역(06 spec 영역 변경 0건)
 3) AWS CLI / boto3 / psql / Spring Boot 실행 / 외부 API 호출 본 일자 변경 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 0건
 4) IAM Policy 전체 본문 / `simulate-principal-policy` 응답 본문 / Secrets Manager `GetSecretValue` 응답 본문 / SSM Parameter Store 응답 본문 / Spring Boot application log 전문 평문 인용 0건
 5) 민감정보(secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / 실제 state machine ARN / Slack webhook URL / DB password / Administrator password) 본 노트 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder
 6) 운영 식별자(commit hash `e72de6f` / Class 이름 `StepFunctionsDailyBatchExecutionService` / Spring properties key 라벨 / 환경변수 패턴 `INTEREST_DB_*` / Spring profile `aws-paper`) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님

## 2026-06-30 (오후) — port-view ECS Fargate Task Role / Task Execution Role 분리 검증 1차 실증

본 일자 오후 운영자가 직접 수행한 port-view ECS Fargate 1차 포팅 통과 + ECS View → AWS Step Functions Step 12~17 승인 실행 1차 실증 결과 중 06 spec(secrets / IAM) 범위에 해당하는 ECS Task Role / Task Execution Role 분리 검증 사실을 누적 기록한다. 본 노트는 port-view 측 코드 본문 / IAM Policy 전체 본문 / `simulate-principal-policy` 응답 본문 / Secrets Manager `GetSecretValue` 응답 본문 / Task Definition JSON 전체 본문 / Step Functions execution history 본문 / Slack 메시지 본문 / CloudWatch Logs 전문 평문 인용 0건(R-DOCS-001 정합).

§1. Task Role / Task Execution Role 분리 1차 실증
 1) Task Role: 완료
   (1) 이름 `portfolio-paper-view-task-role`(실제 ARN `[REDACTED_ARN]` placeholder / 본 노트 평문 기록 0건)
   (2) 책임 = application 측 권한
       - `states:StartExecution` 권한이 Step 12~17 approval state machine ARN(`portfolio-paper-daily-step12-17-approval`) 한정으로 부여
       - 일반 workflow ARN(`portfolio-paper-daily-step1-17-approval`) 한정 부여는 06 spec 후속 phase 책임 그대로 유지
       - Action / Resource wildcard 0건
   (3) 1차 실증 evidence
       - ECS View → Step Functions Step 12~17 승인 실행 통과(executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` / status `SUCCEEDED`)
       - R-AUTO-034 mitigation 의 (a) Task Role `states:StartExecution` 최소 권한 1차 실증 evidence
 2) Task Execution Role: 완료
   (1) 이름 `portfolio-paper-ecs-task-execution-role`(실제 ARN `[REDACTED_ARN]` placeholder / 본 노트 평문 기록 0건)
   (2) 책임 = ECS task 기동 시점 권한
       - ECR repository `portfolio-view` pull
       - CloudWatch Logs group `/ecs/portfolio-view` `PutLogEvents`
       - Secrets Manager `GetSecretValue`(Resource = `/portfolio/paper/...` prefix 한정 부여 정책 그대로 유지 / 06 spec 후속 phase 책임)
   (3) 1차 실증 evidence
       - ECR pull 성공 + ECS task RUNNING + CloudWatch Logs 에 Spring Boot started 확인(log group `/ecs/portfolio-view` / raw 본문 평문 인용 0건)
       - HikariPool RDS connection 성공 + default schema `ops` 확인(RDS 접속정보 Secrets Manager 주입 1차 실증 / 실제 secret ARN `[REDACTED_SECRET_ARN]` placeholder)
 3) 분리 정책 1차 실증: 완료
   (1) Task Role 과 Task Execution Role 이 같은 IAM Role 로 합쳐지지 않도록 분리 부여 1차 실증
       - Task Role 은 application 책임(예: `states:StartExecution`)
       - Task Execution Role 은 기동 시점 책임(예: ECR pull / CloudWatch Logs / Secrets Manager)
       - OD-SEC-005 / OD-SEC-006 / R-AUTO-034 mitigation 정합

§2. Fargate task definition `environment` / `secrets` 블록 1차 실증
 1) `environment` 블록: 완료
   (1) Spring profile `aws-paper` 주입
   (2) `portfolio.batch.local-file-execution-enabled=false`(Fargate 안전 기본값 / R-AUTO-034 mitigation 정합)
   (3) `paperOrderEnabled` · `fullPipelineExecutionEnabled` · `maxExecutableStepOrder` 등 gate 값 default 보다 완화 회귀 0건
   (4) 기본 계좌번호 env 보정 — `PORTFOLIO_BATCH_DEFAULT_ACCOUNT_NO` + `PORTFOLIO_VIEW_ACCOUNT_DEFAULT_ACCOUNT_NO`(revision 1 → 2 보정 한정 / 계좌번호 12자리 원문 본 노트 평문 기록 0건 / `[REDACTED_ACCOUNT_NO]` placeholder)
   (5) `portfolio.batch.aws-stepfunctions-approval-state-machine-arn` env 주입 — 실제 ARN `[REDACTED_ARN]` placeholder / 본 노트 평문 기록 0건
 2) `secrets` 블록: 완료
   (1) RDS 접속정보 Secrets Manager 주입 1차 실증
   (2) Resource ARN 패턴은 `/portfolio/paper/...` prefix 한정 정책 그대로 유지(R-SEC-005 / R-SEC-008 / R-DOCS-001 정합)
   (3) KIS app key · app secret · 계좌번호 · DB password · Slack webhook URL 평문 기록 0건 / 모두 `[REDACTED]` 또는 placeholder

§3. 결정 / 리스크 매핑
 1) 결정 본문 변경 없음
   (1) OD-SEC-005 / OD-SEC-006 / OD-SEC-007 / OD-MS-002 / OD-MS-009 / OD-MS-037 본문 변경 없이 1차 실증 메모 보강
       - 자세한 결정 변경은 `../_common/operator-decisions.md` Change Log 2026-06-30 (오후) 항목 참조
 2) 리스크 매핑
   (1) R-AUTO-034 [2026-06-30 오후 보강] — Fargate Task Role `states:StartExecution` 권한이 Step 12~17 approval state machine ARN 한정 부여 1차 실증 / Task Role + Task Execution Role 분리 1차 실증 / Task Definition `environment` 안 gate 회귀 0건 / Status `Open` 유지(일반 + approval ARN 2종 모두 한정 부여 + Fargate 외부 노출 시점 default ENABLE 회귀 audit 통과 시점에 `Mitigated` 승격 후보)
   (2) R-AUTO-033 [2026-06-30 오후 보강] — 운영자 IP/32 SG inbound + Public IP direct access + ECS View → Step 12~17 approval 3차 실증 통과 / Status `Mitigated` 유지(05 spec 책임)
   (3) R-DOCS-001 / R-DOCS-002 / R-SEC-010 정합 — secret value / DB password / Slack webhook URL / Administrator password / 실제 ARN / image digest full sha256 / public IP / task ARN / ENI ID / broker_order_no 원문 평문 기록 금지 정책 그대로 유지

§4. 후속 (06 spec 후속 phase 책임)
 1) Fargate port-view Task Role 의 `states:StartExecution` Resource 패턴을 일반 workflow ARN + approval workflow ARN 2종 모두 한정 부여
   (1) `simulate-principal-policy` 검증 + Resource · Action wildcard 0건 audit
   (2) IAM Policy 전체 본문 평문 기록 0건(R-DOCS-001 정합)
 2) Fargate task definition `secrets` 블록 정식 작성 — RDS 접속정보 + Slack webhook URL(R-AUTO-024 정합) + KIS app key · app secret 분리
 3) ALB / HTTPS / Route53 / Cloudflare Tunnel 도입 시점에 ACM / Route53 IAM 권한 추가 cross-spec audit
 4) DB password rotate(R-SEC-010 신규) 진입 시점에 Fargate port-view 측 secret 재검증
 5) CloudWatch Logs alarms / DLQ / retry / failure Slack 연동 점검 시 06 spec 측 IAM 권한 cross-spec audit

§5. 본 일자 사실 기록 범위
 1) Kiro 작업 = 06 spec `operation-notes.md` 본 섹션 누적만 수행
 2) 운영자 직접 수행한 ECS task role / execution role 생성 · IAM Policy 작성 · ECR pull · CloudWatch Logs 연결 · Secrets Manager `GetSecretValue` 작업은 운영자 영역으로 cross-service AWS Migration spec 본 일자 작업으로 인한 변경 0건(spec 영역)
 3) AWS CLI / boto3 / psql / Spring Boot 실행 / 외부 API 호출 본 일자 Kiro 측 변경 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 본 일자 Kiro 측 변경 0건
 4) IAM Policy 전체 본문 / `simulate-principal-policy` 응답 본문 / Secrets Manager `GetSecretValue` 응답 본문 / SSM Parameter Store 응답 본문 / Task Definition JSON 전체 본문 / Step Functions execution history 본문 / Slack 메시지 본문 / CloudWatch Logs 전문 / Spring Boot application log 전문 평문 인용 0건
 5) 민감정보(secret value / KIS app key / KIS app secret / 계좌번호 12자리 원문 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / 실제 state machine ARN / Slack webhook URL / DB password / Administrator password / image digest full sha256 / public IP / task ARN / ENI ID / broker_order_no 원문) 본 노트 평문 기록 0건 — 모두 `[REDACTED]` / `[REDACTED_ACCOUNT_NO]` / `[REDACTED_PUBLIC_IP]` / `[REDACTED_ARN]` / `[REDACTED_TASK_ARN]` / `[REDACTED_SECRET_ARN]` / `[REDACTED_BROKER_ORDER_NO]` placeholder
 6) 운영 식별자(Task Role 이름 `portfolio-paper-view-task-role` / Task Execution Role 이름 `portfolio-paper-ecs-task-execution-role` / ECR `portfolio-view` / CloudWatch Logs `/ecs/portfolio-view` / Security Group `sgroup-port-view-ecs` / ECS cluster `portfolio-paper-cluster` / ECS service `portfolio-view-service` / task definition `portfolio-view:2` / Spring profile `aws-paper` / state machine `portfolio-paper-daily-step12-17-approval` / executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` / status `SUCCEEDED` / Spring properties env 라벨 / Secrets Manager prefix `/portfolio/paper/...`) 는 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님

## 2026-06-30 (오후) Slack — Daily Brief Builder Lambda Secrets Manager `valueFrom` + 알림 전용 IAM Role 2종 분리 검증

본 일자 오후의 port-view ECS Fargate Task Role / Task Execution Role 분리 검증(앞 섹션) 과 별도로, 같은 일자 오후에 운영자가 직접 수행한 Daily Brief Slack 자동화 측 IAM Role 신규 분리 + Builder Lambda 의 DB password 주입 방식 사실을 06 spec 범위에서 누적 기록한다. 본 노트는 Lambda 코드 본문 / IAM Policy 전체 본문 / `simulate-principal-policy` 응답 본문 / Secrets Manager `GetSecretValue` 응답 본문 / Step Functions ASL 본문 / Slack 메시지 본문 평문 인용 0건(R-DOCS-001 정합).

§1. 알림 전용 IAM Role 2종 신규 분리 1차 실증
 1) `portfolio-daily-brief-sfn-role`: 완료
   (1) 책임 = Daily Brief mini Step Functions 안 Builder Lambda + Notifier Lambda invoke 권한
       - Resource = Builder Lambda `portfolio-approval-slack-summary-builder` 와 별도 / Daily Brief Builder `portfolio-daily-brief-slack-summary-builder` + Notifier `portfolio-event-notifier` 한정
       - Action = `lambda:InvokeFunction` 한정
       - Action / Resource wildcard 0건
   (2) 실제 ARN 평문 기록 0건(`[REDACTED_ARN]` placeholder)
   (3) Daily 본 실행 IAM Role(`portfolio-paper-stepfunctions-execution-role`) 와 분리하여 알림 측 권한 확장이 본 실행 권한에 영향을 주지 않도록 격리(OD-SEC-005 / OD-SEC-006 정합)
 2) `portfolio-daily-brief-scheduler-role`: 완료
   (1) 책임 = Daily Brief Scheduler 2개의 mini state machine StartExecution 권한
       - Resource = `portfolio-daily-brief-slack-notification` state machine ARN 한정
       - Action = `states:StartExecution` 한정
       - Action / Resource wildcard 0건
   (2) 실제 ARN 평문 기록 0건(`[REDACTED_ARN]` placeholder)
   (3) Daily 본 실행 Scheduler IAM Role(`portfolio-paper-eventbridge-scheduler-role`) / EC2 lifecycle Scheduler IAM Role 과 분리

§2. Builder Lambda DB password 주입 방식 1차 실증
 1) `portfolio-daily-brief-slack-summary-builder` Lambda 구성: 완료
   (1) Lambda runtime
       - Python 3.12
       - DB driver `pg8000`(`psycopg2` 미사용)
   (2) DB password 주입 방식
       - Lambda 환경변수 `DB_PASSWORD_SECRET_VALUE_FROM` 에 Secrets Manager `valueFrom` 매핑
       - Lambda 코드 안에서 `secretsmanager:GetSecretValue` runtime 조회
       - Lambda 환경변수 / Secrets Manager `valueFrom` 매핑 / `GetSecretValue` 응답 본문 평문 기록 0건
       - 실제 secret ARN `[REDACTED_SECRET_ARN]` placeholder
       - secret value 자체는 RDS 접속 시점 이후 메모리 안에서만 사용 / Lambda CloudWatch Logs 평문 기록 0건
       - OD-SEC-002 / R-DOCS-001 / R-SEC-010 정합
 2) RDS read 책임
   (1) Lambda 가 SSM Port Forwarding 경유지 의존 없이 직접 RDS endpoint 로 접속(VPC 안 Lambda)
       - 본 spec 본 노트 평문 RDS endpoint hostname 기록 0건
       - `connector_balance_snapshot` + `connector_position_snapshot` SELECT 한정
       - DDL / DML 0건 / `executemany` 0건
   (2) SELECT 결과 평문 인용 0건 — 운영자 노트에는 summary 결과만 사실 기록

§3. Builder Lambda 측 IAM Policy (책임 분리)
 1) `portfolio-daily-brief-slack-summary-builder` Lambda 의 Execution Role(별도 Lambda 자체 Execution Role / IAM Role 이름 본 노트 평문 기록 0건)
   (1) `secretsmanager:GetSecretValue` Resource = Daily Brief 용 RDS app role secret ARN 한정
   (2) RDS connection 권한 = VPC 안에서 RDS endpoint TCP 5432 outbound 한정
   (3) `logs:CreateLogGroup` / `logs:CreateLogStream` / `logs:PutLogEvents` Resource = `/aws/lambda/portfolio-daily-brief-slack-summary-builder` 한정
   (4) Action / Resource wildcard 0건 / 06 spec 후속 phase 책임으로 정식 매트릭스 갱신
 2) 실제 IAM Policy 본문 / `simulate-principal-policy` 응답 본문 평문 기록 0건

§4. 결정 / 리스크 매핑
 1) 결정 본문 변경 없음
   (1) OD-SEC-002(RDS master password Secrets Manager 보관) / OD-SEC-005(EC2 / 8개 MS Access Key 미사용 원칙) / OD-SEC-006(MS 별 Secret 접근 분리) / OD-MS-030(AWS 공통 Slack notifier) 본문 변경 없이 1차 실증 메모 보강
       - 자세한 결정 변경은 `../_common/operator-decisions.md` Change Log `2026-06-30 (오후) Slack` 항목 + OD-MS-038 신규 참조
 2) 리스크 매핑
   (1) R-AUTO-035 신규 — Daily Brief Slack 자동 발송 실패 또는 중복 발송 위험 / Status `Mitigated` / mini Step Functions 분리 + Scheduler 2개 ENABLED + 수동 smoke `SUCCEEDED` + 알림 전용 IAM Role 분리(권한 광역화 0건)
   (2) R-DOCS-001 / R-SEC-010 정합 — Lambda 코드 본문 / Secrets Manager secret value / DB password / 실제 ARN 평문 기록 금지 / 본 일자 추가분 0건
   (3) R-AUTO-024(Slack webhook URL 평문 노출 위험 / `Accepted`) mitigation 그대로 유지 / Notifier Lambda `portfolio-event-notifier` 환경변수 `SLACK_WEBHOOK_URL` 평문 유지 / 운영 안정화 후 Secrets Manager 또는 SSM SecureString 이전 / 06 spec 후속 phase 책임

§5. 후속 (06 spec 후속 phase 책임)
 1) Daily Brief Builder Lambda 의 IAM Policy 정식 정의 + `simulate-principal-policy` 검증 + Resource · Action wildcard 0건 audit
 2) Slack webhook URL 을 Lambda 환경변수에서 Secrets Manager 또는 SSM SecureString 으로 이전(R-AUTO-024 정합)
 3) `portfolio-daily-brief-sfn-role` / `portfolio-daily-brief-scheduler-role` 의 Resource 패턴이 Daily Brief 전용 ARN 한정인지 정기 audit
 4) Daily Brief Builder Lambda 의 RDS connection 풀링 / 재사용 정책 + Lambda cold start 시점의 Secrets Manager `GetSecretValue` 호출 횟수 audit(평일 2회 = 월 약 44회 / `GetSecretValue` 단가 매우 작음 그대로 유지)
 5) DB password rotate(R-SEC-010) 진입 시점에 Daily Brief Builder Lambda 측 Secrets Manager secret 재검증

§6. 본 일자 사실 기록 범위
 1) Kiro 작업 = 06 spec `operation-notes.md` 본 섹션 누적만 수행
 2) 운영자 직접 수행 영역 = Builder Lambda 2개 신규 생성 + mini state machine 1개 신규 생성 + Scheduler 2개 ENABLED + IAM Role 2종 신규 생성 + IAM Policy 작성 + Notifier formatter 개선 + `portfolio-paper-daily-step1-17-approval` ASL update
 3) AWS CLI / boto3 / psql / Spring Boot 실행 / 외부 API 호출 본 일자 Kiro 측 변경 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 본 일자 Kiro 측 변경 0건
 4) IAM Policy 전체 본문 / `simulate-principal-policy` 응답 본문 / Secrets Manager `GetSecretValue` 응답 본문 / Step Functions ASL 본문 / Lambda 코드 본문 / Slack 메시지 본문 / Builder output 전문 / Notifier input 전문 / CloudWatch Logs 전문 평문 인용 0건
 5) 민감정보(secret value / KIS app key / KIS app secret / 계좌번호 12자리 원문 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / 실제 state machine ARN / Slack webhook URL / DB password / Administrator password) 본 노트 평문 기록 0건 — 모두 `[REDACTED]` / `[REDACTED_ARN]` / `[REDACTED_SECRET_ARN]` / `[REDACTED_ACCOUNT_NO]` placeholder
 6) 운영 식별자(IAM Role 이름 `portfolio-daily-brief-sfn-role` · `portfolio-daily-brief-scheduler-role` / Builder Lambda 이름 `portfolio-approval-slack-summary-builder` · `portfolio-daily-brief-slack-summary-builder` / Notifier Lambda 이름 `portfolio-event-notifier` / state machine 이름 `portfolio-daily-brief-slack-notification` / Scheduler 이름 2종 / Lambda runtime `Python 3.12` / DB driver `pg8000` / DB password 주입 방식 `DB_PASSWORD_SECRET_VALUE_FROM` / Slack webhook 환경변수명 `SLACK_WEBHOOK_URL` / Lambda log group `/aws/lambda/portfolio-daily-brief-slack-summary-builder`) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님

## 2026-06-30 (오후) 장중 손절 Slack — MarketConnector EC2 Instance Role inline policy(`lambda:InvokeFunction` Resource 한정) 분리 검증

본 일자 오후 추가 작업분으로 운영자가 직접 수행한 장중 손절 Slack 실 연동 중 06 spec 범위에 해당하는 MarketConnector EC2 Instance Role inline policy 부여 사실을 누적 기록한다. 본 노트는 IAM Policy 전체 본문 / `simulate-principal-policy` 응답 본문 / Lambda 코드 본문 / `connector_intraday_position_evaluate.py` 본문 / runner ps1 본문 / SSM 응답 본문 / Slack 메시지 본문 / CloudWatch Logs 전문 평문 인용 0건(R-DOCS-001 정합 / 운영 식별자만 사실 기록).

§1. MarketConnector EC2 Instance Role inline policy 분리 1차 실증
 1) Instance Role
   (1) 이름 `portfolio-paper-marketconnector-ec2-role`
       - 03 spec 의 정식 운영 EC2(MarketConnector EC2) Instance Role
       - 실제 ARN 평문 기록 0건(`[REDACTED_ARN]` placeholder)
   (2) 본 inline policy 부여 전 기존 권한 범위
       - SSM Session Manager / `AmazonSSMManagedInstanceCore` managed policy
       - KIS Secrets Manager / SSM Parameter Store read
       - RDS 접속(`marketconnector_app` DB role 기준)
       - CloudWatch Logs write
       - S3 access_token 백업
       - 기존 권한은 본 일자 변경 없음
 2) 신규 inline policy 부여
   (1) policy 이름 `portfolio-paper-marketconnector-event-notifier-invoke`
   (2) action
       - `lambda:InvokeFunction`
   (3) Resource
       - `portfolio-event-notifier` Lambda 한정
       - 실제 Lambda ARN 평문 기록 0건(`[REDACTED_ARN]` placeholder)
       - Resource · Action wildcard 0건
       - OD-SEC-005 / OD-SEC-006 정합
   (4) 본 inline policy 는 Daily Batch state machine / Step Functions execution role / EventBridge Scheduler role / Daily Brief 알림 전용 IAM Role 2종(`portfolio-daily-brief-sfn-role` · `portfolio-daily-brief-scheduler-role`) 와 모두 분리
 3) EC2 invoke smoke 통과
   (1) EC2 측에서 Notifier Lambda invoke smoke 성공
   (2) Lambda 응답 본문 평문 인용 0건
   (3) 본 smoke 는 `connector_intraday_position_evaluate.py --notify-slack` runtime 호출 경로의 IAM 권한 사전 검증 목적

§2. MarketConnector evaluate 의 `DB_PASSWORD_SECRET_VALUE_FROM` 정책 정합
 1) DB password 주입 방식
   (1) 본 일자 추가 분에서도 MarketConnector EC2 측 DB password 주입 방식 변경 없음
   (2) MarketConnector evaluate 는 기존 EC2 Instance Role 기반 Secrets Manager `GetSecretValue` runtime 조회 정책 그대로 유지(OD-SEC-002 / R-DOCS-001 / R-SEC-010 정합)
   (3) Daily Brief Builder Lambda 의 `DB_PASSWORD_SECRET_VALUE_FROM` 환경변수 valueFrom 방식과 별도 — MarketConnector evaluate 는 Lambda 가 아니라 EC2 venv Python 으로 실행되므로 환경변수 주입 방식이 EC2 측 운영자 셋업에 따라 다름 / 본 일자 추가 분에서 변경 없음
 2) Slack webhook URL 측 정책 정합
   (1) Notifier Lambda `portfolio-event-notifier` 의 환경변수 `SLACK_WEBHOOK_URL` 평문 유지(R-AUTO-024 `Accepted`)
   (2) MarketConnector evaluate 는 Slack webhook URL 을 직접 알지 않음 — Notifier Lambda invoke 만 수행 / webhook URL 평문 노출 위험은 Notifier Lambda 측 책임으로 분리
   (3) Slack webhook URL Secrets Manager 또는 SSM SecureString 이전 후속(R-AUTO-024 / 06 spec 후속 phase 책임 그대로 유지)

§3. 결정 / 리스크 매핑
 1) 결정 본문 변경 없음
   (1) OD-SEC-002 / OD-SEC-005 / OD-SEC-006 / OD-SEC-007 / OD-MS-001 / OD-MS-016 / OD-MS-030 / OD-MS-035 / OD-MS-036 / OD-MS-038 본문 변경 없이 evidence 보강
   (2) 자세한 결정 변경은 `../_common/operator-decisions.md` Change Log `2026-06-30 (오후) 장중 손절 Slack` 항목 참조
   (3) 신규 결정 없음 / Decision Summary 카운트 변경 없음(전체 97 / 확정 52 / 잠정 42 유지)
 2) 리스크 매핑
   (1) R-AUTO-036 신규 — 장중 손절 READY 생성 후 Slack 발송 실패 시 rollback 없는 정책의 부작용 위험 / Status `Mitigated` / mitigation 핵심 항목 중 IAM 권한 한정 부여 + EC2 invoke smoke + DB after-check 보강은 본 일자 1차 통과
   (2) R-AUTO-035 [2026-06-30 오후 추가 보강] — Notifier Lambda 호출 진입점이 MarketConnector EC2 runner 까지 확대 / Status `Mitigated` 그대로 유지
   (3) R-AUTO-024(Slack webhook URL 평문 노출 위험 / `Accepted`) mitigation 그대로 유지 / 운영 안정화 후 Secrets Manager 또는 SSM SecureString 이전 / 06 spec 후속 phase 책임
   (4) R-SEC-010(DB password 평문 노출 후속 rotation / `Open`) mitigation 그대로 유지 / 운영자 로컬 세션에서 우연히 노출된 사실은 본 노트에 password 값 없이 "credential rotation / history cleanup 권고" 수준으로만 기록(R-DOCS-001 / R-DOCS-002 정합)
   (5) R-DOCS-001 / R-DOCS-002 정합 — IAM Policy 전체 본문 / Lambda 코드 본문 / `connector_intraday_position_evaluate.py` 본문 / runner ps1 본문 / SSM 응답 본문 / `simulate-principal-policy` 응답 본문 평문 기록 0건

§4. 후속 (06 spec 후속 phase 책임)
 1) MarketConnector EC2 Instance Role 의 inline policy 매트릭스 정식 정의 + `simulate-principal-policy` 검증 + Resource · Action wildcard 0건 audit(Daily Brief Builder Lambda Execution Role + Daily Brief mini Step Functions IAM Role 2종 + MarketConnector EC2 inline policy 통합 매트릭스 정합 audit 포함)
 2) Slack webhook URL 을 Notifier Lambda 환경변수에서 Secrets Manager 또는 SSM SecureString 으로 이전(R-AUTO-024 정합)
 3) DB password rotate(R-SEC-010) 진입 시점에 MarketConnector EC2 측 Secrets Manager secret 재검증
 4) R-AUTO-036 운영 detection 자동화(Lambda CloudWatch Logs metric filter / Slack 발송 실패 alarm / DB after-check ↔ Slack 수신 cross-reference 자동화) — 05 · 06 spec 후속 phase 책임
 5) Daily Brief Builder Lambda Execution Role + Daily Brief mini Step Functions IAM Role 2종 + MarketConnector EC2 inline policy 가 모두 `portfolio-event-notifier` Lambda 만 invoke 하도록 cross-spec audit(권한 광역 회귀 0건 유지)

§5. 본 일자 사실 기록 범위
 1) Kiro 작업 = 06 spec `operation-notes.md` 본 섹션 누적만 수행
 2) 운영자 직접 수행 영역 = MarketConnector EC2 Instance Role 에 inline policy 신규 부여 + EC2 측 Lambda invoke smoke + 1회 실 runner 안전 검증
 3) AWS CLI / boto3 / psql / Spring Boot 실행 / 외부 API 호출 / Lambda 실행 / Step Functions 실행 / SSM RunCommand 본 일자 Kiro 측 변경 0건 / AWS 리소스 신규 생성 · 수정 · 삭제 본 일자 Kiro 측 변경 0건
 4) IAM Policy 전체 본문 / `simulate-principal-policy` 응답 본문 / Secrets Manager `GetSecretValue` 응답 본문 / Lambda 코드 본문 / `connector_intraday_position_evaluate.py` 본문 / runner ps1 본문 / SSM 응답 본문 / Slack 메시지 본문 / CloudWatch Logs 전문 평문 인용 0건
 5) 민감정보(secret value / KIS app key / KIS app secret / 계좌번호 12자리 원문 / token / RDS password / RDS endpoint hostname / account-id 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / IAM access key id / 실제 state machine ARN / Slack webhook URL / DB password / Administrator password / EIP / public IP / broker_order_no 원문) 본 노트 평문 기록 0건 — 모두 `[REDACTED]` 또는 placeholder
 6) 운영 식별자(IAM Role 이름 `portfolio-paper-marketconnector-ec2-role` / inline policy 이름 `portfolio-paper-marketconnector-event-notifier-invoke` / Lambda 이름 `portfolio-event-notifier` / state machine 이름 `portfolio-paper-intraday-stop-sell-approval` / SSM commandId `5b19d5da-5e2e-4b35-821b-c3cf2b36d131` / MarketConnector evaluate 파일 경로 / 배포 SHA256 / 배포 버전 / runner 파일 경로 / runner SHA256 / source_version) 만 사용자 명시 정책 정합으로 사실 기록 — secret 가 아님
