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

> **2026-06-22 보강(OD-MS-027 신규 정합 / R-AUTO-021 신규 mitigation 1차 실증)**
>
> Daily AWS Paper Wrapper 운영(`.kiro/scripts/run-daily-aws-paper.ps1`) 에서는 Step 1 / Step 12 / Step 13 / Step 17(MarketConnector EC2 SSM step 4종) 가 진입 직전에 `daily-aws-paper.functions.ps1` 의 공통 **MarketConnector env bootstrap 함수**를 호출해 `/tmp/inject-env.sh` 를 **step 실행 시점에 재생성**한다.
>
> 즉 `/tmp/inject-env.sh` 의 EC2 상 선존재를 가정하지 않는다. EC2 stop / start 이후 `/tmp` 디렉터리는 ephemeral 로 휘발될 수 있다(R-AUTO-021 정합).
>
> 본 절차의 수동 export 스크립트 작성은 `/tmp/inject-env.sh` 가 부재한 상태에서 운영자가 직접 단발 검증을 수행할 때의 수동 fallback 으로만 사용한다. 정식 systemd unit + `EnvironmentFile` 등록은 본 spec 후속 task(7 / 26 / 27) 책임으로 분리 유지.

1. EC2 에 SSH 또는 SSM Session Manager 로 접속.
2. 임시 export 스크립트 작성 — `/tmp/inject-env.sh` 패턴(권한 700, secret 평문 미저장, 메모리 export 만). 자세한 패턴은 [`../06-secrets-and-iam/runbook.md`](../06-secrets-and-iam/runbook.md) §6-1 참조.
3. 환경변수 매핑은 [`./design.md`](./design.md) §8.2 / §8.2.1 / §8.2.2 표 그대로:
   - Secrets Manager: `APP_KEY`, `APP_SECRET`, `PAPER_ACNT`, `ACNT_PRDT_CD`, `INTEREST_DB_HOST` / `INTEREST_DB_PORT` / `INTEREST_DB_NAME` / `INTEREST_DB_USER` / `INTEREST_DB_PASSWORD`
   - SSM Parameter: `BASE_URL`, `PORT_ENVIRONMENT`, `PORT_BROKER_NAME`, `CONNECTOR_HOST`, `CONNECTOR_PORT`, `CONNECTOR_DEBUG`
   - **KIS_* alias (실제 코드 실행 정합)**: `KIS_APP_KEY`, `KIS_APP_SECRET`, `KIS_PAPER_ACNT`, `KIS_ACNT_PRDT_CD`, `KIS_BASE_URL` — 호환 key 와 동일 value 를 동시 export(`./design.md` §8.2.1 정합 / 2026-06-17 보강)
4. **JSON SecretString 처리** — 아래 secret 은 plain string 이 아니라 JSON SecretString 이다.
   - `/portfolio/paper/marketconnector/kis-app-key` — 내부 key `APP_KEY`
   - `/portfolio/paper/marketconnector/kis-app-secret` — 내부 key `APP_SECRET`
   - `/portfolio/paper/marketconnector/paper-account` — 내부 key `PAPER_ACNT` / `ACNT_PRDT_CD`

   다음 절차로 처리한다(`./design.md` §8.2.2 정합).
   - `aws secretsmanager get-secret-value --secret-id <name> --query SecretString --output text` 결과를 `python -c 'import json,sys; d=json.loads(sys.stdin.read()); print(d["APP_KEY"])'` 또는 `jq -r .APP_KEY` 로 내부 key value 만 추출.
   - JSON dict 전체를 환경변수 값으로 export 하지 않는다(2026-06-17 1차 실패 원인).
   - 추출된 value 를 `APP_KEY` 와 `KIS_APP_KEY` 두 환경변수에 동시 export. `APP_SECRET` / `KIS_APP_SECRET`, `PAPER_ACNT` / `KIS_PAPER_ACNT`, `ACNT_PRDT_CD` / `KIS_ACNT_PRDT_CD`, `BASE_URL` / `KIS_BASE_URL` 도 동일.
   - secret value 자체는 stdout / 로그 / 콘솔 캡처 / 운영자 노트에 출력하지 않는다. value length 또는 key presence 만 확인.
5. `source /tmp/inject-env.sh` 후 환경변수 keys 만 확인 (값은 출력 / 로그에 남기지 않음).

   ```bash
   env | grep -E '^(APP_KEY|APP_SECRET|PAPER_ACNT|ACNT_PRDT_CD|KIS_APP_KEY|KIS_APP_SECRET|KIS_PAPER_ACNT|KIS_ACNT_PRDT_CD|KIS_BASE_URL|INTEREST_DB_|BASE_URL|PORT_|CONNECTOR_)' | cut -d= -f1
   ```
6. 정상 운영 모드(systemd unit / startup script) 전환은 본 spec 후속 task 또는 별도 phase 책임. 본 runbook 범위 밖. [준비]

### 2.1 Daily wrapper bootstrap 장애 시 복구 절차 (2026-06-22 보강)

본 절은 Daily AWS Paper Wrapper 운영 중 Step 1 / Step 12 / Step 13 / Step 17 에서 `/tmp/inject-env.sh not found` 또는 동등 env missing 오류가 발생했을 때의 복구 절차다(OD-MS-027 / R-AUTO-021 정합).

1. wrapper bootstrap 함수 반영 여부 확인 [확인]
   1) `daily-aws-paper.functions.ps1` 안 MarketConnector env bootstrap 함수의 존재 여부 확인.
   2) Step 1 / Step 12 / Step 13 / Step 17 step 파일(`.kiro/scripts/steps/`) 에서 해당 bootstrap 함수가 step 진입 직전에 호출되는지 확인.
   3) PowerShell parser validation 통과 여부 확인(`.ps1` 파일 모두 parser error 0건).
2. wrapper 측 patch 가 정합한 경우 → Step 단독 재실행 [실행]
   1) `.\run-daily-aws-paper.ps1 -Environment aws-paper -RunDate <RunDate> -StartStep 1 -EndStep 1`(Step 1 단독) 또는 영향 step 단독 재실행.
   2) `-StartStep` / `-EndStep` 파라미터만 사용(`-FromStep` / `-ToStep` 미정의 — 실수 시 default 1 / 17 진입 위험).
   3) wrapper run summary 의 step result 가 SUCCESS / SSM ResponseCode 0 / step failure exit status 0 인지 확인.
3. wrapper 측 patch 가 미반영 / 운영 직전 우회 필요한 경우 → 운영자 수동 fallback [실행] [확인]
   1) 운영자가 MarketConnector EC2 에 SSM Session Manager 로 접속해 본 §2 단계 그대로 `/tmp/inject-env.sh` 수동 재생성(권한 700 / 메모리 export 한정).
   2) JSON SecretString 내부 key 추출 흐름 유지.
       - `aws secretsmanager get-secret-value --secret-id <name> --query SecretString --output text` 결과를 JSON parse.
       - 내부 key(`APP_KEY` / `APP_SECRET` / `PAPER_ACNT` / `ACNT_PRDT_CD` / `BASE_URL`) 만 추출.
       - `APP_*` 호환 key + `KIS_*` alias 동시 export.
   3) secret value 평문 출력 금지 — stdout / stderr / 콘솔 캡처 / 운영자 노트 / wrapper 로그 평문 기록 0건(R-DOCS-001 정합). 길이 / key presence 만 출력.
   4) 수동 fallback 후 wrapper Step 단독 재실행. 영구 해결은 wrapper 측 bootstrap 함수 정식 적용 / PowerShell parser validation 후속 통과로 이행.
4. Step 12 실제 주문 safety gate 재강조 [확인]
   1) Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` 는 `-AllowPaperOrderExecute` 옵션을 운영자가 명시적으로 입력했을 때만 실제 KIS paper 주문 제출 가능(R-AUTO-019 mitigation 정합).
   2) bootstrap 장애 복구 중에도 Step 12 단독 재실행 시 의도하지 않은 `-AllowPaperOrderExecute` 입력을 피한다 — `PaperOrder: True` 라벨이 wrapper summary 에 출력되는지 사후 검증.
   3) live 환경 BUY / SELL 자동 활성화 금지(OD-SAFE-002 / OD-SAFE-003 / R-AUTO-002 정합) — wrapper 환경 입력은 `aws-paper` 만 허용 / aws-live 분기 코드 레벨 미존재.

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

### 4.1. `connector_balance.py` 검증 SQL 후보 [확인]

1. `connector.connector_api_call_log` 의 BALANCE 카테고리 최신 row 확인.
   - `category = 'BALANCE'` / `api_name = 'inquire-balance'` / 최신 `called_at`
   - `response_status = 200` / `response_code = 0` / `is_success = true`
   - secret value / 계좌번호 / token / response body 평문 미기록 정책 정합(R-DOCS-001).
2. `connector.connector_balance_snapshot` 최신 row 확인.
   - 최신 `as_of_date` / `as_of_ts` / `created_at`
   - `source_api = 'inquire-balance'` / `source_version` 사실 기록
   - 보유종목 0건은 `connector.connector_position_snapshot` 동일 `as_of_date` 기준 row 0건 또는 정상 0건 row 로 확인.
3. SELECT 한정. DDL/DML 0건. 결과는 [`./operation-notes.md`](./operation-notes.md) 일자별 섹션에 사실로만 누적.

### 4.2. `connector_order_check.py` 검증 SQL 후보 [확인]

1. `connector.connector_api_call_log` 의 ORDER 카테고리 최신 row 확인.
   - `category = 'ORDER'` / `api_name = 'inquire-daily-ccld'` / 최신 `called_at`
   - `response_status = 200` / `response_code = 0` / `is_success = true`
   - body 평문 미기록.
2. row count 인벤토리 확인.
   - `connector.connector_order_request` / `connector.connector_order_event` / `connector.connector_fill` 의 누적 row count + 본 실행 시점 신규 row 수 비교.
   - 본 일자 신규 주문 / 체결 0건이면 `connector_order_event` / `connector_fill` 신규 row 0건 = 정상 판단(KIS 신규 주문 / 체결 미발생 정합).
   - row count 증가가 있으면 호출 시점 / 종목 / 수량 사실만 누적 기록(주문번호 / 계좌번호 평문 미기록).
3. SELECT 한정. DDL/DML 0건. 결과는 [`./operation-notes.md`](./operation-notes.md) 일자별 섹션에 사실로만 누적.

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
