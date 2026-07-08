# Validation Checklist — 06-secrets-and-iam

본 문서는 [`./runbook.md`](./runbook.md) 의 [확인] / 완료 기준을 4종 라벨 기반 체크리스트로 정리한 최소판이다.
본 spec(06) 1차 적용 환경은 `aws-paper`, region 은 `ap-northeast-2`, 1차 적용 대상은 MarketConnector EC2.

라벨 규칙(본 문서 외 라벨 사용 금지):

- `[O]` 통과 / 충족 확인됨.
- `[X]` 미충족 또는 실패. 운영자 / Kiro 후속 조치 필요.
- `[Kiro 후속 작업 필요]` 본 spec 안에서 Kiro 가 추가로 만들거나 채울 산출물이 있어 보류된 항목.
- `[운영자 확인 필요]` 운영자만 AWS Console / EC2 shell 에서 직접 확인 가능한 항목. Kiro 자동 검증 대상 아님.

보안 / 안전 원칙

- 아래 항목은 `[REDACTED]` 또는 placeholder 만 사용. 본 문서에 평문 기록 금지.
  - secret value / KIS app key / KIS app secret / 계좌번호
  - RDS endpoint hostname / RDS password / account-id
  - 실제 secret ARN / IAM access key id / Slack webhook URL
- `secretsmanager:GetSecretValue` 실호출은 운영자만. Kiro 자동 검증은 `secretsmanager:DescribeSecret` metadata 만.
- 8개 MS 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 미수정.
- 실제 AWS 리소스 미생성 / 미수정 / 미삭제. 본 체크리스트는 점검 / 결과 기록만 한다.

## 1. 문서 생성 상태

- [O] [`./requirements.md`](./requirements.md) 생성
- [O] [`./README.md`](./README.md) 생성
- [O] [`./design.md`](./design.md) 생성
- [O] [`./tasks.md`](./tasks.md) 생성
- [O] [`./runbook.md`](./runbook.md) 생성
- [Kiro 후속 작업 필요] [`./operation-notes.md`](./operation-notes.md) 생성 예정

## 2. Secrets Manager

- [운영자 확인 필요] `/portfolio/paper/marketconnector/kis-app-key` 생성 확인 (Description / Tag 포함)
- [운영자 확인 필요] `/portfolio/paper/marketconnector/kis-app-secret` 생성 확인
- [운영자 확인 필요] `/portfolio/paper/marketconnector/paper-account` 생성 확인 (JSON multi-key: `PAPER_ACNT`, `ACNT_PRDT_CD`)
- [운영자 확인 필요] `/portfolio/paper/rds/marketconnector-app` 생성 확인 (JSON multi-key: `host`, `port`, `dbname`, `username`, `password`)
- [운영자 확인 필요] 기존 `/portfolio/paper/rds/master` 유지 확인 (이름 / KMS / 마지막 수정 시각이 02 spec 시점과 일치)
- [운영자 확인 필요] 위 secret 5건 외 다른 `/portfolio/paper/marketconnector/*` 또는 `/portfolio/paper/rds/*` secret 추가 등록 0건
- [운영자 확인 필요] secret value 본문이 본 문서 / runbook.md / operation-notes.md / 콘솔 캡처 / 운영자 노트에 평문으로 기록되지 않음 확인

## 3. SSM Parameter Store

- [운영자 확인 필요] `/portfolio/paper/marketconnector/kis-base-url` 생성 확인
- [운영자 확인 필요] `/portfolio/paper/marketconnector/connector-host` 생성 확인
- [운영자 확인 필요] `/portfolio/paper/marketconnector/connector-port` 생성 확인
- [운영자 확인 필요] `/portfolio/paper/marketconnector/connector-debug` 생성 확인
- [운영자 확인 필요] `/portfolio/paper/marketconnector/environment` 생성 확인 (Value=`paper`)
- [운영자 확인 필요] `/portfolio/paper/marketconnector/broker-name` 생성 확인
- [운영자 확인 필요] 위 6건 외 다른 `/portfolio/paper/marketconnector/*` parameter 추가 등록 0건
- [운영자 확인 필요] parameter 이름 자체에 실제 endpoint hostname / 계좌번호 / secret value / password / token 미포함 확인

## 4. IAM Role / Policy

- [운영자 확인 필요] IAM Role `portfolio-paper-marketconnector-ec2-role` 존재 / Trust Policy `Service: ec2.amazonaws.com` + `sts:AssumeRole` 확인
- [운영자 확인 필요] Instance Profile `portfolio-paper-marketconnector-ec2-profile` 존재 / Role attach 확인
- [운영자 확인 필요] EC2 instance 에 위 Instance Profile attach 확인 (`describe-iam-instance-profile-associations` 결과의 ARN 일치)
- [운영자 확인 필요] Permission Policy 에 `secretsmanager:GetSecretValue`, `secretsmanager:DescribeSecret` 만 허용. 다른 secretsmanager Action 0건
- [운영자 확인 필요] Permission Policy 에 `ssm:GetParameter`, `ssm:GetParameters`, `ssm:GetParametersByPath` 만 허용. 다른 ssm Action 0건
- [운영자 확인 필요] Permission Policy Resource 에 `"*"` 0건
- [운영자 확인 필요] Permission Policy Resource 에 `Action: "*"` / `secretsmanager:*` / `ssm:*` 0건
- [운영자 확인 필요] Permission Policy Resource 에 다른 service prefix 0건 (아래 5종)
  - `/portfolio/paper/view/*`
  - `/portfolio/paper/crawler/*`
  - `/portfolio/paper/preprocessor/*`
  - `/portfolio/paper/strategy/*`
  - `/portfolio/paper/research/*`
- [운영자 확인 필요] Permission Policy Resource 에 다른 환경 prefix(`/portfolio/live/...`) 0건
- [운영자 확인 필요] Permission Policy Resource 의 secret ARN 4건 / parameter ARN prefix 1건이 design §4.2 매트릭스와 일치
- [운영자 확인 필요] CMK 사용 결정이 없는 경우 Policy 안 `kms:Decrypt` statement 0건. CMK 사용 결정 시에만 해당 KMS Key ARN 만 한정

## 5. EC2 Access Key 미사용

- [운영자 확인 필요] `~/.aws/credentials` 미존재 (`test -f ~/.aws/credentials` → not found)
- [운영자 확인 필요] `~/.aws/config` 안 `aws_access_key_id` / `aws_secret_access_key` 라인 0건
- [운영자 확인 필요] 현재 shell 의 `AWS_ACCESS_KEY_ID` 환경변수 미설정
- [운영자 확인 필요] 현재 shell 의 `AWS_SECRET_ACCESS_KEY` 환경변수 미설정
- [운영자 확인 필요] 현재 shell 의 `AWS_SESSION_TOKEN` 환경변수 미설정 (Instance Role 자격증명은 metadata service 가 자동 처리하므로 export 불필요)
- [운영자 확인 필요] `~/.bashrc`, `~/.profile`, `~/.bash_profile` 안 access key 관련 export 라인 0건
- [운영자 확인 필요] systemd unit 의 `Environment=` / `EnvironmentFile=` 안 access key 0건
- [운영자 확인 필요] application `.env` / config 파일 안 access key 0건
- [운영자 확인 필요] `aws sts get-caller-identity` 결과 `Arn` 이 `arn:aws:sts::<account-id>:assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>` 형태
- [운영자 확인 필요] `aws configure list` 결과 `access_key` Source 컬럼이 `iam-role` 또는 `Ec2InstanceMetadata`

## 6. EC2 조회 검증

- [운영자 확인 필요] EC2 에서 `aws secretsmanager describe-secret --secret-id /portfolio/paper/marketconnector/kis-app-key` 성공
- [운영자 확인 필요] EC2 에서 `aws secretsmanager describe-secret` 가 본 spec 4건 secret 모두 metadata 정상 반환
- [운영자 확인 필요] EC2 에서 `aws ssm get-parameters-by-path --path /portfolio/paper/marketconnector` 가 6건 parameter 모두 반환
- [운영자 확인 필요] EC2 에서 다른 service prefix(`/portfolio/paper/view/*`, `/portfolio/paper/crawler/*`) 조회 시 AccessDenied 또는 NotFound (권한 격리 정상)
- [운영자 확인 필요] EC2 에서 `secretsmanager:GetSecretValue` 자동 검증 호출 0건 (운영자 정상 운영 흐름에서만 호출)
- [운영자 확인 필요] EC2 에서 다른 환경 prefix(`/portfolio/live/...`) 조회 시 AccessDenied (권한 격리 정상)

## 7. Connector smoke test (조회성만)

- [운영자 확인 필요] 임시 export 스크립트 실행 후 아래 DB 환경변수가 현재 shell 에 주입됨 (값 표시 / 로그 기록 금지)
  - `INTEREST_DB_HOST` / `INTEREST_DB_PORT` / `INTEREST_DB_NAME` / `INTEREST_DB_USER` / `INTEREST_DB_PASSWORD`
- [운영자 확인 필요] 임시 export 스크립트 실행 후 아래 KIS / 설정 환경변수 주입됨
  - `APP_KEY` / `APP_SECRET` / `BASE_URL` / `PAPER_ACNT` / `ACNT_PRDT_CD` / `PORT_ENVIRONMENT` / `PORT_BROKER_NAME`
- [운영자 확인 필요] 임시 export 스크립트 / 환경변수 값이 파일 / 로그 / 콘솔 캡처에 평문 저장되지 않음
- [운영자 확인 필요] RDS `marketconnector_app` 접속 성공 (DDL/DML 미실행, 조회만)
- [운영자 확인 필요] KIS token 발급 또는 기존 `access_token.txt` 재사용 정상 동작
- [운영자 확인 필요] `connector_balance.py` 잔고 조회 성공
- [운영자 확인 필요] `connector_order_check.py` 주문 / 체결 조회 성공
- [운영자 확인 필요] Flask 조회성 endpoint smoke test 통과 (잔고 / 보유 / 주문 내역 등)
- [운영자 확인 필요] 본 검증 동안 신규 주문 / 매수 / 매도 / 취소 / 정정 API 호출 0건
  - 미실행 대상: `connector_buy.py`, `connector_sell.py`, `connector_cancel.py`, `connector_modify.py`

## 8. 완료 기준

- [운영자 확인 필요] EC2 안에 IAM access key 파일 / 환경변수 / dotfile / systemd EnvironmentFile / `.env` 0건
- [운영자 확인 필요] Instance Role 자격증명만으로 본 spec 4건 secret + 6건 parameter read 가능
- [운영자 확인 필요] IAM Permission Policy 에 Resource wildcard / Action wildcard / 다른 service prefix / 다른 환경 prefix 0건
- [운영자 확인 필요] RDS `marketconnector_app` 조회 + KIS 조회성 smoke test 통과
- [O] 본 문서 / `requirements.md` / `README.md` / `design.md` / `tasks.md` / `runbook.md` 어디에도 아래 항목 평문 기록 0건
  - secret value / KIS app key / KIS app secret / 계좌번호
  - RDS endpoint hostname / RDS password / account-id
  - 실제 secret ARN / IAM access key id / Slack webhook URL

## 9. 후속 인계

- [Kiro 후속 작업 필요] [`./operation-notes.md`](./operation-notes.md) 에 본 체크리스트 결과(일자별 누적) 기록 템플릿 작성. secret 값 미기록, 성공 / 실패만 기록.
- [Kiro 후속 작업 필요] [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 갱신 (운영자 승인 시)
  - OD-SEC-001 / OD-OBS-004 갱신
  - 신규 OD-SEC-005 / OD-SEC-006 후보 반영
- [Kiro 후속 작업 필요] [`../_common/risk-register.md`](../_common/risk-register.md) 등록 (운영자 승인 시)
  - R-SEC 후보 4건: 권한 과다 / EC2 secret 평문 노출 / EC2 access key 파일 / naming 불일치
  - 다음 가용 ID 로 등록
- [Kiro 후속 작업 필요] [`../_common/followups-overview.md`](../_common/followups-overview.md) 의 06 섹션 갱신 (1차 적용 환경 / 1차 범위 / 범위 밖 / 03 인계).
- [운영자 확인 필요] 본 spec 결정과 본 체크리스트 통과 결과를 03-marketconnector-ec2 spec 진입 입력으로 인계 (Instance Role 정책 / Access Key 미사용 원칙 / Task Role 골격).
