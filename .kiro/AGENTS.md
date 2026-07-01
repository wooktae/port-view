# Kiro AWS Migration 작업 규칙

이 `.kiro` 작업공간은 PORT-STRATEGY-AI 포트폴리오의 AWS Migration 관련 cross-service spec을 작성하고 관리하기 위한 공간이다.

## 범위

* 기본 작업 디렉터리:

  * `C:\Workspaces\port-view\.kiro`

* AWS Migration spec 저장 위치:

  * `C:\Workspaces\port-view\.kiro\specs`

* 이 디렉터리는 `port-view` 안에 있지만, 여기서 작성하는 spec은 아래 8개 마이크로서비스 전체의 AWS Migration을 다룬다.

  * `port-marketconnector`
  * `port-view`
  * `port-interest-crawler`
  * `port-interest-preprocessor`
  * `port_strategy_common`
  * `port_strategy_decision`
  * `port_strategy_research`
  * `port_strategy_execution`

## 루트 참조 문서

AWS Migration spec을 새로 만들거나 수정하기 전에 `.kiro/specs/_common` 아래의 루트 공통 문서를 먼저 확인한다.

* `_common/operator-decisions.md`
* `_common/ms-aws-service-decision-matrix.md`
* `_common/cost-simulation.md`
* `_common/followups-overview.md`
* `_common/aws-resource-glossary.md`
* `_archive/note-aws-landscape-2021-vs-2026.md` (참고용 아카이브)

위 문서들은 모든 spec에서 공유하는 기준 문서로 사용한다.

## 단일 기준 문서 규칙

* `operator-decisions.md`는 운영자 결정사항의 단일 기준 문서다.
* `ms-aws-service-decision-matrix.md`는 MS별 AWS 서비스 권고와 판단 근거의 단일 기준 문서다.
* `cost-simulation.md`는 비용 가정과 월 예상 비용의 단일 기준 문서다.
* `followups-overview.md`는 후속 spec 진행 순서와 의존성 맵의 단일 기준 문서다.
* `aws-resource-glossary.md`는 AWS 용어 설명의 단일 기준 문서다.
* `01-aws-migration-foundation`은 baseline foundation spec이다. foundation 수준의 결정이 바뀌는 경우가 아니라면 반복해서 내용을 복사하거나 재작성하지 않는다.

## 업데이트 규칙

새 spec에서 운영자 결정이 바뀌면 `operator-decisions.md`를 업데이트한다.

새 spec에서 특정 MS의 AWS 서비스 권고가 바뀌면 `ms-aws-service-decision-matrix.md`를 업데이트하고, 필요한 경우 `operator-decisions.md`도 함께 업데이트한다.

새 spec에서 월 비용 가정, RDS 크기, VPC Endpoint 수, ALB 사용 여부, NAT 사용 여부, EC2 크기, Fargate 사용량 가정이 바뀌면 `cost-simulation.md`를 업데이트한다.

새 spec에서 후속 spec의 순서, 의존성, 범위가 바뀌면 `followups-overview.md`를 업데이트한다.

새 AWS 서비스나 용어가 Migration에서 중요하게 사용되기 시작하면 `aws-resource-glossary.md`를 업데이트한다.

루트 공통 문서를 기계적으로 전부 업데이트하지 않는다. 현재 결정이나 spec에 영향을 받는 문서만 업데이트한다.

## MS별 AGENTS.md 참조 규칙

각 마이크로서비스는 자체 `AGENTS.md`를 가질 수 있다. 해당 파일들은 현재 작업이 그 MS의 수정을 명시적으로 요구하지 않는 한 read-only 참조 문서로 취급한다.

AWS Migration spec 작성 시 관련 MS의 `AGENTS.md`, `README.md`, 기존 문서는 아래 내용을 이해하기 위한 목적으로만 읽는다.

* 런타임 구조
* entrypoint
* 환경변수
* DB schema / search_path
* secret 항목
* 빌드 / 배포 제약
* 운영상 주의사항

현재 작업이 명시적으로 허용하지 않는 한 MS 소스 코드, README, CHANGELOG, worklog, AGENTS.md를 수정하지 않는다.

## Spec 작성 규칙

각 spec은 자기 범위에 집중한다.

루트 공통 문서의 큰 내용을 그대로 복사하지 않는다. 대신 아래 기준을 따른다.

* 현재 spec에 필요한 내용만 요약한다.
* 상세 기준은 루트 공통 문서를 참조한다.
* 결정사항은 `operator-decisions.md`와 일치시킨다.

각 spec 폴더는 일반적으로 아래 파일을 포함한다.

* `requirements.md`
* `design.md`
* `tasks.md`
* 운영자 선택지가 많은 경우 선택적으로 `decision-matrix.md`

## 보안 규칙

실제 secret, password, token, app key, app secret, 계좌번호, webhook URL을 절대 작성하지 않는다.

모든 민감정보 값은 `[REDACTED]`로 표기한다.

요약, 로그, 예시, 표, 생성 문서 어디에도 민감정보 값을 출력하지 않는다.

## 실행 규칙

Spec 작성 작업에서는 아래 행위를 하지 않는다.

* AWS 리소스 생성 금지
* 운영 entrypoint 실행 금지
* 애플리케이션 소스 코드 수정 금지
* broker 주문 스크립트 실행 금지
* live cutover 수행 금지

AWS Console 작업이나 구현 절차가 필요한 경우에는 실제 실행하지 않고 runbook 단계로만 작성한다.

## README / CHANGELOG / WORKLOG 관리 규칙

`.kiro` 작업공간은 각 MS와 달리 날짜별 worklog 파일을 만들지 않는다.

- `.kiro/README.md`는 Kiro AWS Migration 작업공간의 목적, 범위, 폴더 구조, 주요 문서 역할을 설명한다.
- `.kiro/CHANGELOG.md`는 spec 구조 변경, 루트 공통 문서 변경, 신규 spec 생성, 주요 문서 재구성 이력만 간단히 기록한다.
- `.kiro/WORKLOG.md`는 Kiro 작업 세션의 간단 로그를 하나의 파일에 누적한다.
- `.kiro/docs/worklog/YYYY-MM-DD.md` 같은 날짜별 worklog 파일은 만들지 않는다.
- 각 MS의 `docs/worklog/YYYY-MM-DD.md` 형식보다 훨씬 간단하게 작성한다.
- 의미 있는 Kiro 문서 작업을 수행한 경우 `.kiro/WORKLOG.md`를 업데이트한다.
- spec 구조, 루트 공통 문서, 신규 spec 생성 같은 주요 변경이 있으면 `.kiro/CHANGELOG.md`를 업데이트한다.
- 작업공간 목적, 폴더 구조, 주요 문서 목록이 바뀌면 `.kiro/README.md`를 업데이트한다.
- secret, password, token, app key, app secret, 계좌번호, webhook URL 같은 민감정보는 절대 기록하지 않는다.
- 민감정보는 필요한 경우 `[REDACTED]`로만 표기한다.

## 문서 최소화 / 운영자 가독성 규칙

- 새 문서를 만들기 전에 기존 문서에 흡수 가능한지 먼저 판단한다.
- 운영자가 매일 봐야 하는 문서는 각 spec의 README.md / runbook.md / validation-checklist.md / operation-notes.md로 제한한다.
- requirements.md / design.md / tasks.md / traceability-matrix.md는 Kiro/감사용 문서로 취급하고, 운영자용 요약을 반복해서 길게 쓰지 않는다.
- 같은 결정값은 operator-decisions.md에만 상세히 기록하고, 다른 문서에서는 Decision ID와 짧은 요약만 참조한다.
- 작업 완료 후 자동 갱신 대상은 기본적으로 최대 3개 파일로 제한한다.
  - 현재 spec의 operation-notes.md
  - 현재 spec의 validation-checklist.md
  - 필요 시 WORKLOG.md 또는 operator-decisions.md
- CHANGELOG.md는 구조 변경, 신규 spec 생성, 주요 문서 재구성 때만 갱신한다.

## 운영 명령 작성 규칙(추가)

본 절은 2026-06-30 (오후) port-view ECS Fargate 1차 포팅 + ECS View → AWS Step Functions Step 12~17 승인 실행 통과 회차에서 운영자가 직접 식별한 운영 사고 사례를 토대로 추가된다. 본 spec 작업공간이 작성하는 runbook / validation-checklist / operation-notes 문서 안에서 명령 예시를 기술할 때 아래 3종 규칙을 반드시 따른다.

### 규칙 1: AWS CLI 명령은 `list/describe → 변수 추출 → 후속 검증` 패턴

- 운영자가 ARN / task ARN / ENI ID / public IP / LOG_STREAM / image digest 같은 동적 식별자를 매번 직접 치환하지 않도록 작성한다.
- 잘못된 패턴(작성 금지): 운영자가 손으로 ARN / task ARN / ENI ID 를 끼워 넣어야 하는 1줄 예시.
- 정합 패턴(권장): (1) `aws ecs list-tasks` 같은 `list/describe` 호출로 후보 식별자 수집, (2) 결과에서 `--query` / shell 변수 추출, (3) 추출된 변수를 다음 명령에 주입, (4) `describe`/`status` 호출로 후속 검증.
- 예: ECS task 의 public IP 조회 시 `list-tasks → TASK_ARN` → `describe-tasks → ENI_ID(attachments[].details[?name=='networkInterfaceId'].value)` → `describe-network-interfaces → PUBLIC_IP(Association.PublicIp)` → 브라우저 URL `http://$PUBLIC_IP:8080` 출력. public IP 평문 기록 0건 / `[REDACTED_PUBLIC_IP]` placeholder 정합.

### 규칙 2: psql 검증 쿼리는 `information_schema.columns` 사전 확인

- 추정 컬럼명으로 `SELECT` / `JOIN` 작성 금지. 실제 컬럼명을 `information_schema.columns` 로 먼저 확인한 뒤 확인된 컬럼만 사용한다.
- 2026-06-30 오전 사례 — `connector_position_snapshot.balance_snapshot_id` 컬럼이 실제로는 부재했고, 후검증 쿼리가 `account_no` + `as_of_date` 기준으로 재작성되어야 했음. 본 사례를 본 규칙의 1차 evidence 로 사용한다.
- 후검증 쿼리는 `DO` / `EXECUTE` 로 결과를 숨기지 않고 최종 `SELECT` 결과가 화면에 직접 출력되게 작성한다(허위 성공 마커 방지 / 규칙 3 결합).
- 본 규칙은 본 spec 의 신규 후속 후보 위험(추정 컬럼명 오진) 의 1차 mitigation 으로 기록한다.

### 규칙 3: 실패한 SQL / 명령 뒤에 SUCCESS / DONE marker 금지

- `SELECT 1` / `\echo SUCCESS` / `echo DONE` / `RAISE NOTICE 'SUCCESS'` 등의 marker 를 실패한 SQL 이나 실패한 명령 뒤에 자동으로 찍지 않는다.
- 명령이 실패하면 stderr / non-zero exit / 빈 result set 등 실제 실패 신호 자체를 명령 출력에 그대로 남기고, 다음 단계로 진입하지 않는다.
- 자동 wrapper / 운영 노트 작성 시점에 marker 를 일괄 prefix / suffix 로 추가하는 패턴은 금지(스크립트가 SUCCESS 라고 출력하더라도 운영자가 실제 실행 결과를 별도로 점검해야 하는 부담 증가).
- 본 규칙은 R-AUTO-033 / R-AUTO-034 mitigation 운영 가독성 정합 + 운영자 사후 검증 부담 감소 목적.

### 적용 범위

- 본 3종 규칙은 본 spec 작업공간이 작성하는 runbook / validation-checklist / operation-notes / 운영 메모 안의 명령 예시에만 적용된다.
- 8개 MS 의 소스 코드 / 패키징 / docs / worklog / README / AGENTS.md / CHANGELOG 영역에 본 규칙을 강제 적용하지 않는다(본 작업공간 외부 책임 영역).
- 본 규칙의 추가 evidence 가 발생하면 본 절을 그대로 유지하고 영향을 받은 운영 노트 안에 사례를 사실 기록한다.

### 규칙 4: 추정 컬럼명 사용 금지(R-AUTO-035 / R-AUTO-036 mitigation 강화)

- 2026-06-30 오후 운영자 노트 작성 중 `connector_order_request.order_side` 같은 실제 존재하지 않는 컬럼명을 추정해 SQL 작성한 사례 1건 식별. 추정 컬럼명으로 작성된 검증 쿼리는 DB after-check 자체가 실패하거나, 잘못된 SUCCESS marker 와 결합되면 운영자가 검증을 통과한 것으로 오인할 위험.
- `connector_order_request` / `strategy_execution_order` / `strategy_intraday_position_check` / `connector_position_snapshot` 등 본 spec 영역의 모든 DB after-check 쿼리는 `information_schema.columns` 로 실제 컬럼명 사전 확인 후 작성한다(규칙 2 의 강화 적용).
- 본 작업공간이 작성하는 모든 SQL 예시는 컬럼명을 추정해서 쓰지 않으며, 운영자가 직접 수행하는 검증 쿼리도 동일 정책을 따른다.
- 추정 컬럼명 발견 시 즉시 운영 노트에 반성점 기록 + 검증 쿼리를 재작성 + 실패 출력을 그대로 유지(SUCCESS marker 회피 — 규칙 3 결합).

### 규칙 5: PowerShell native command not found / psql 실패 시 `$LASTEXITCODE` 처리 주의

- PowerShell 에서 `psql` 같은 native command 가 not found 이거나 비정상 종료될 경우 `$LASTEXITCODE` 와 `$?` 의 처리 차이를 주의한다 — `$?` 는 마지막 cmdlet 의 성공 여부, `$LASTEXITCODE` 는 마지막 native command 의 exit code 로 의미가 다르다.
- 명령 실패 직후 `if ($?)` 만 보고 SUCCESS marker 를 찍으면 native command 실패를 놓칠 위험이 있다.
- 본 작업공간이 작성하는 PowerShell 예시는 native command 호출 직후 `if ($LASTEXITCODE -ne 0) { ... }` 패턴으로 exit code 를 명시적으로 점검한 뒤 다음 단계로 진입한다.
- 본 규칙은 규칙 3(실패 명령 뒤 SUCCESS marker 금지) 의 PowerShell 측 적용 사례로 결합된다.

### 규칙 6: Windows AWS CLI stdout 의 emoji / 특수문자 cp949 encoding 오류 대응

- Windows PowerShell / cmd 에서 AWS CLI 또는 SSM 응답 본문이 emoji(`🚨` · `🔵` · `🔴` · `⚪`) / 한글 / 특수문자를 포함하는 경우 콘솔 출력이 cp949 encoding 오류로 깨지거나 운영자가 검증 결과를 오해할 수 있다(2026-06-30 오후 장중 손절 Slack 연동 회차 식별 사례 정합).
- 본 작업공간이 작성하는 운영 노트 / runbook / validation-checklist 의 SSM 출력 점검 패턴은 다음 중 하나를 우선한다.
  - EC2 측에서 출력 자체를 sanitize 한 뒤 SSM 으로 받기
  - SSM 응답에서 `status` / exit code / marker 만 추출하는 status-only 조회
  - 한글 / emoji 포함 본문은 운영자 별도 화면(예: CloudWatch Logs / Slack 채널 / 로컬 EC2 SSH)에서 직접 점검
- Windows PowerShell `chcp 65001` / `[Console]::OutputEncoding = [Text.UTF8Encoding]::new()` 같은 임시 우회는 본 spec 영역의 정식 운영 권고가 아니며, 운영자가 임의로 적용해도 본 규칙의 sanitize 또는 status-only 정책을 우회할 수 없다.

### 규칙 7: SSM multiline command 는 UTF-8 No BOM JSON + `--parameters file://...` 패턴

- SSM RunCommand 의 multiline command(특히 한글 / emoji / Python 스크립트 본문 / SQL 본문이 포함된 경우)는 PowerShell `--parameters '{...}'` 인라인 JSON 으로 전달하지 않는다 — Windows PowerShell 측 BOM / cp949 / 따옴표 escape 문제로 명령이 실패하거나 의도와 다르게 실행될 수 있다.
- 본 작업공간이 작성하는 SSM 명령 예시는 UTF-8 No BOM 으로 저장된 JSON 파일을 만들고 `--parameters file://<path>` 패턴으로 전달한다.
- 본 규칙은 02 spec(RDS) 검증 SQL · 03 spec(MarketConnector EC2) 운영 SSM 명령 · 04 spec(Strategy Batch) Step Functions 측 SSM 호출 등 본 작업공간의 모든 SSM 명령 예시에 적용된다.

### 규칙 8: DB password / secret 값 채팅 / 문서 / 로그 / 명령 예시 절대 포함 금지

- DB password / secret value / KIS app key / KIS app secret / token / Slack webhook URL / RDS password / Secrets Manager value / 계좌번호 12자리 원문 / 실제 IAM Role ARN / 실제 secret ARN / 실제 state machine ARN / 실제 public IP / 실제 account-id / broker_order_no 원문 / image digest full sha256 은 본 작업공간의 어떤 문서 / 채팅 / 명령 예시 / runbook / validation-checklist / operation-notes / WORKLOG / CHANGELOG / README / AGENTS.md / 로그 인용에도 절대 포함하지 않는다.
- 필요한 경우 `[REDACTED]` / `[REDACTED_ACCOUNT_NO]` / `[REDACTED_PUBLIC_IP]` / `[REDACTED_ARN]` / `[REDACTED_SECRET_ARN]` / `[REDACTED_BROKER_ORDER_NO]` / `[REDACTED_TASK_ARN]` placeholder 만 사용한다.
- 운영자가 로컬 세션 / 운영 채팅 / 운영자 노트 작성 중 실수로 secret 값을 노출한 사실이 식별되면 본 spec 산출물에는 password 값 없이 "credential rotation / history cleanup 권고" 수준으로만 기록한다(R-SEC-010 / R-DOCS-001 / R-DOCS-002 정합).
- 본 규칙은 본 작업공간의 모든 spec / 모든 일자 / 모든 phase 에 적용된다 — R-DOCS-001 (secret 평문 기록 금지) 의 본 spec 작업공간 측 핵심 적용 규칙이다.
