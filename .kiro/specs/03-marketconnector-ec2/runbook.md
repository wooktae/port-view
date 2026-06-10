# Runbook — 03-marketconnector-ec2

운영자가 EC2 shell / AWS Console 에서 직접 따라 할 절차서. 1차 적용 환경: `aws-paper`, region `ap-northeast-2`. 자세한 결정 근거는 [`./design.md`](./design.md) 참조.

라벨

- `[실행]` 실제 명령 / 작업 수행
- `[확인]` 상태 / 결과 점검만
- `[준비]` 후속 작업 전 합의 / 기록만
- `[복구]` 실패 / 장애 시 되돌리기

원칙

- 실제 secret value, KIS app key / app secret, 계좌번호, RDS endpoint hostname, RDS password, account-id, 실제 secret ARN, IAM access key id, instance-id, EIP 본문 평문 기록 금지. 모두 `[REDACTED]` 또는 placeholder.
- `secretsmanager:GetSecretValue` 실호출은 운영자만. Kiro 자동 검증은 `DescribeSecret` / `GetParameter` metadata 만.
- 신규 주문 / 매수 / 매도 / 취소 / 정정 entrypoint 호출 0건 (`connector_buy.py` / `connector_sell.py` / `connector_cancel.py` / `connector_modify.py` 절대 실행 금지).
- 실제 AWS 리소스 생성 / 변경 / 삭제는 운영자 직접 수행.

## 1. 사전 확인 [확인]

1. EC2 region 이 `ap-northeast-2` 인지 확인. EC2 Instances 에서 MarketConnector EC2 가 `running` 상태인지 확인.
2. EC2 → Modify IAM role 화면에서 Instance Profile 이 `portfolio-paper-marketconnector-ec2-profile` 인지 확인.
3. Security Group 매트릭스 확인: `sg-marketconnector-ec2` → `sg-rds-postgres` 5432 inbound 만 허용. RDS Public access = `No`.
4. Secrets Manager 4건 / SSM Parameter 6건이 06 spec naming 그대로 등록되어 있는지 metadata 만 확인.

## 2. env 주입 [실행] [확인]

1. EC2 에 SSH 또는 SSM Session Manager 로 접속.
2. 임시 export 스크립트 작성 — `/tmp/inject-env.sh` 패턴(권한 700, secret 평문 미저장, 메모리 export 만). 자세한 패턴은 [`../06-secrets-and-iam/runbook.md`](../06-secrets-and-iam/runbook.md) §6-1 참조.
3. 환경변수 매핑은 [`./design.md`](./design.md) §8.2 표 그대로:
   - Secrets Manager: `APP_KEY`, `APP_SECRET`, `PAPER_ACNT`, `ACNT_PRDT_CD`, `INTEREST_DB_HOST` / `INTEREST_DB_PORT` / `INTEREST_DB_NAME` / `INTEREST_DB_USER` / `INTEREST_DB_PASSWORD`
   - SSM Parameter: `BASE_URL`, `PORT_ENVIRONMENT`, `PORT_BROKER_NAME`, `CONNECTOR_HOST`, `CONNECTOR_PORT`, `CONNECTOR_DEBUG`
4. `source /tmp/inject-env.sh` 후 환경변수 keys 만 `env | grep -E '^(APP_KEY|APP_SECRET|PAPER_ACNT|ACNT_PRDT_CD|INTEREST_DB_|BASE_URL|PORT_|CONNECTOR_)' | cut -d= -f1` 로 확인 (값은 출력 / 로그에 남기지 않음).
5. 정상 운영 모드(systemd unit / startup script) 전환은 본 spec 후속 task 또는 별도 phase 책임. 본 runbook 범위 밖. [준비]

## 3. RDS 접속 확인 [확인]

1. `psql` major version 18 인지 확인: `psql --version`. `pg_dump --version`, `pg_restore --version` 도 동일.
2. `marketconnector_app` 사용자로 RDS 접속만 확인:
   ```text
   psql "host=$INTEREST_DB_HOST port=$INTEREST_DB_PORT dbname=$INTEREST_DB_NAME user=$INTEREST_DB_USER" -c "SELECT 1;"
   ```
3. 결과 = `1` 이면 통과. DDL/DML 실행 금지. SELECT 외 호출 0건.
4. 실패 시 §8 [복구] 로 이동.

## 4. connector 조회성 실행 [실행] [확인]

1. venv 활성화: `source <venv-path>/bin/activate`.
2. `connector_balance.py` 실행 — 잔고 조회 결과 반환 확인. exit code 0.
3. `connector_order_check.py` 실행 — 주문 / 체결 조회 결과 반환 확인. exit code 0.
4. 두 스크립트 모두 KIS 측 신규 주문 / 매수 / 매도 / 취소 / 정정 호출을 일으키지 않는다는 점 재확인.

## 5. Flask 조회성 smoke test [실행] [확인]

1. Flask 기동(임시 검증 단계): `python connector_app.py`. 코드 기본값 host `127.0.0.1`, port `5000`, debug `False`.
2. 기동 로그에 `CONNECTOR_DEBUG=True` 흔적이 없는지 확인. 정상 운영 모드에서는 반드시 `false` 강제([`./design.md`](./design.md) §8.2).
3. 같은 EC2 안에서 조회성 endpoint 만 호출:
   - `curl -s http://127.0.0.1:5000/api/v1/view/<placeholder-path>`
   - 잔고 / 보유 / 주문 내역 등 조회성 endpoint 응답 200 확인.
4. 신규 주문 endpoint(`/api/v1/buy/...`, `/api/v1/sell/...`, `/api/v1/cancel/...`, `/api/v1/modify/...`) 절대 호출 금지.
5. 운영 가능 후보(`connector_quote_realtime.py` / `connector_quote_closed.py` / `connector_view_service.py`) 검증은 후속 phase 책임. 본 runbook 범위 밖. [준비]

## 6. Instance Role / Access Key 미사용 확인 [확인]

1. `aws sts get-caller-identity` — `Arn` 이 `arn:aws:sts::<account-id>:assumed-role/portfolio-paper-marketconnector-ec2-role/<instance-id>` 형태인지 확인.
2. `aws configure list` — `access_key` Source 가 `iam-role` 또는 `Ec2InstanceMetadata` 인지 확인.
3. `~/.aws/credentials` 미존재 확인:
   ```text
   test -f ~/.aws/credentials && echo "FOUND" || echo "OK"
   ```
4. `~/.aws/config`, dotfile, systemd EnvironmentFile 안 access key 패턴 0건:
   ```text
   grep -rEi "^aws_access_key_id|^aws_secret_access_key|AWS_ACCESS_KEY_ID=|AWS_SECRET_ACCESS_KEY=" ~/.aws/config ~/.bashrc ~/.profile ~/.bash_profile /etc/systemd/system 2>/dev/null
   ```
5. Secrets Manager `describe-secret` 4건 metadata 정상 반환 확인. `GetSecretValue` 자동 호출 0건 (운영자만 수행).
6. SSM `get-parameters-by-path /portfolio/paper/marketconnector` 6건 정상 반환 확인.

## 7. 결과 기록 [준비]

1. [`./operation-notes.md`](./operation-notes.md) 의 일자별 누적 섹션(`## YYYY-MM-DD ...`) 에 §3 ~ §6 결과를 성공 / 실패 두 값으로만 기록.
2. 실제 secret value / 계좌번호 / RDS endpoint / account-id / 실제 ARN / IAM access key id / instance-id / EIP 미기록.
3. [`./validation-checklist.md`](./validation-checklist.md) 의 7개 점검 영역에 4종 라벨 중 하나로 표시.
4. 8건 검증 결과 모두 `[O]` 또는 운영자 직접 확인 결과로 갱신([`./design.md`](./design.md) §10).

## 8. 실패 시 복구 흐름 [복구]

1. RDS 접속 실패 → SG 매트릭스 확인 / Instance Profile attach 확인 / Secret 이름 오타 확인 / VPC Endpoint 상태 확인.
2. Connector 조회성 실패 → KIS API rate limit (R-BROKER) 확인 / token 발급 / 재사용 흐름 점검(token 값 출력 금지) / Connector 로그 확인 (Flask debug flag 평문 secret 노출 패턴 grep 0건 재확인).
3. Instance Role 검증 실패 → access key 발견 시 IAM Console 즉시 폐기 → `~/.aws/credentials` 백업 이동(타임스탬프 suffix) → IMDSv2 + Role only 모드 재검증([`./design.md`](./design.md) §9.6).
4. systemd / startup script 가 본 runbook 범위 밖이므로, 정상 운영 모드 진입 실패는 본 spec 후속 task 또는 별도 phase 에서 처리. 본 runbook 시점 운영은 임시 export 모드로 일시 복귀 가능.
5. 모든 [복구] 결과는 [`./operation-notes.md`](./operation-notes.md) 에 일자 / 사실 / 후속 조치만 누적 기록(원인 stdout 본문 / secret value 미기록).
