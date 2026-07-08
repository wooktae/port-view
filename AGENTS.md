# Kiro AWS Migration 작업 규칙

이 `.kiro` 작업공간은 PORT-STRATEGY-AI 포트폴리오의 AWS Migration 관련 cross-service spec을 작성하고 관리하기 위한 공간이다.

## Scope

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

### 루트 참조 문서

AWS Migration spec을 새로 만들거나 수정하기 전에 `.kiro/specs/_common` 아래의 루트 공통 문서를 먼저 확인한다.

* `_common/operator-decisions.md`
* `_common/ms-aws-service-decision-matrix.md`
* `_common/cost-simulation.md`
* `_common/followups-overview.md`
* `_common/aws-resource-glossary.md`
* `_archive/note-aws-landscape-2021-vs-2026.md` (참고용 아카이브)

위 문서들은 모든 spec에서 공유하는 기준 문서로 사용한다.

### MS별 AGENTS.md 참조 규칙

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

## Single Source of Truth

* `operator-decisions.md`는 운영자 결정사항의 단일 기준 문서다.
* `ms-aws-service-decision-matrix.md`는 MS별 AWS 서비스 권고와 판단 근거의 단일 기준 문서다.
* `cost-simulation.md`는 비용 가정과 월 예상 비용의 단일 기준 문서다.
* `followups-overview.md`는 후속 spec 진행 순서와 의존성 맵의 단일 기준 문서다.
* `aws-resource-glossary.md`는 AWS 용어 설명의 단일 기준 문서다.
* `01-aws-migration-foundation`은 baseline foundation spec이다. foundation 수준의 결정이 바뀌는 경우가 아니라면 반복해서 내용을 복사하거나 재작성하지 않는다.

## Documentation Style

### 업데이트 규칙

새 spec에서 운영자 결정이 바뀌면 `operator-decisions.md`를 업데이트한다.

새 spec에서 특정 MS의 AWS 서비스 권고가 바뀌면 `ms-aws-service-decision-matrix.md`를 업데이트하고, 필요한 경우 `operator-decisions.md`도 함께 업데이트한다.

새 spec에서 월 비용 가정, RDS 크기, VPC Endpoint 수, ALB 사용 여부, NAT 사용 여부, EC2 크기, Fargate 사용량 가정이 바뀌면 `cost-simulation.md`를 업데이트한다.

새 spec에서 후속 spec의 순서, 의존성, 범위가 바뀌면 `followups-overview.md`를 업데이트한다.

새 AWS 서비스나 용어가 Migration에서 중요하게 사용되기 시작하면 `aws-resource-glossary.md`를 업데이트한다.

루트 공통 문서를 기계적으로 전부 업데이트하지 않는다. 현재 결정이나 spec에 영향을 받는 문서만 업데이트한다.

### Spec 작성 규칙

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

### README / CHANGELOG / WORKLOG 관리 규칙

`.kiro` 작업공간은 각 MS와 달리 날짜별 worklog 파일을 만들지 않는다. 루트 문서 3종은 역할을 분리해서 관리한다.

| 문서 | 역할 | 갱신 조건 | 작성 깊이 |
| --- | --- | --- | --- |
| `.kiro/README.md` | 작업공간 dashboard / navigation / 현재 상태 요약 | 작업공간 목적, 폴더 구조, 주요 문서 역할, Current Status Dashboard 가 바뀔 때 | 운영자가 1분 안에 읽는 수준 |
| `.kiro/CHANGELOG.md` | spec 문서 변경 이력 | spec 구조 변경, 루트 공통 문서 변경, 신규 spec 생성, 주요 문서 재구성 시 | 변경 유형별 요약 + 링크 |
| `.kiro/WORKLOG.md` | Kiro 작업 세션 누적 로그 | 의미 있는 Kiro 문서 작업 세션 후 | 날짜별 5~10줄 요약 + 핵심 evidence |

- `.kiro/docs/worklog/YYYY-MM-DD.md` 같은 날짜별 worklog 파일은 만들지 않는다.
- 각 MS의 `docs/worklog/YYYY-MM-DD.md` 형식보다 훨씬 간단하게 작성한다.
- README / CHANGELOG / WORKLOG 는 raw log 저장소가 아니다. 긴 실행 로그, DB raw output, CloudWatch log 전문, Step Functions history 전문은 붙이지 않고 operation-notes / validation-checklist / runbook 으로 연결한다.
- secret, password, token, app key, app secret, 계좌번호, webhook URL 같은 민감정보는 절대 기록하지 않는다.
- 민감정보는 필요한 경우 `[REDACTED]` 계열 placeholder 로만 표기한다.

#### README.md 작성 규칙

README 는 `.kiro` 작업공간의 첫 화면이다. 아래 내용을 유지한다.

- Purpose / Scope 는 짧게 유지하고, `.kiro` 가 `port-view` 하위에 있지만 8개 MS 전체 AWS Migration cross-service spec 저장소라는 점을 명확히 쓴다.
- `Current Status Dashboard` 는 표로 관리한다.
  - 영역, 진행 상태, 최근 검증 일자, 관련 spec, 링크를 포함한다.
  - “완료” 표기는 실제 데이터 적재, 최신성 검증, end-to-end 상태 전이 검증까지 확인된 경우에만 사용한다.
  - aws-live BUY / SELL 자동화처럼 금지 또는 미진행 상태는 눈에 띄게 표시한다.
- `Important Safety Notes` 는 블록쿼트로 유지한다.
  - live 자동화 미진행, Kiro 실행 금지, secret 기록 금지, MS 소스/문서 수정 금지 같은 안전 문구를 반복 가능하게 둔다.
- Directory Map 과 Key Documents 는 문서 구조가 바뀔 때만 갱신한다.
- README 에는 세부 실행 evidence 를 길게 쓰지 않는다. 자세한 내용은 각 spec 의 `operation-notes.md`, `_common/followups-overview.md`, `WORKLOG.md` 로 링크한다.

#### CHANGELOG.md 작성 규칙

CHANGELOG 는 `.kiro` 안의 spec 문서 변경 이력만 기록한다. MS 코드 변경 이력이나 상세 작업 로그를 기록하지 않는다.

- 날짜는 한국 기준 작업 일자를 사용한다.
- 최신 날짜 섹션을 파일 상단에 추가한다.
- 섹션 제목은 `## YYYY-MM-DD (짧은 작업명)` 형식을 사용한다.
- 하위 분류는 아래 4개를 기본으로 한다.
  - `### Added`
  - `### Changed`
  - `### Removed`
  - `### Security`
- 해당 분류에 내용이 없으면 생략한다.
- 각 bullet 은 “무엇이 바뀌었는지 + 왜 중요한지 + 어디서 자세히 보는지”를 한 문장으로 쓴다.
- 신규 spec, 신규 runbook, 신규 validation-checklist, 루트 공통 문서 변경, risk / decision / follow-up 구조 변경은 CHANGELOG 에 기록한다.
- 단순 operation-notes append, 숫자 보강, evidence 한 줄 보강처럼 구조 영향이 작은 변경은 WORKLOG 와 해당 operation-notes 에만 기록하고 CHANGELOG 는 생략한다.
- 많은 값이 필요한 경우 표를 사용하되, raw 실행 로그 전문은 금지한다.
- 상태 배지는 의미 있는 항목에만 사용한다.
  - 🟢 완료 / 성공 / ENABLED / Mitigated
  - 🟠 대기 / 관찰 / Open / 미확정
  - 🔴 금지 / live / 고위험
  - 🔵 evidence / 참고

#### WORKLOG.md 작성 규칙

WORKLOG 는 Kiro 작업 세션의 간단 누적 로그다. 작업 일자별 별도 파일을 만들지 않고 단일 파일에 누적한다.

- 최신 날짜 섹션을 파일 상단에 추가한다.
- 기본 구조는 아래 순서를 사용한다.
  - `### 🧭 Summary`
  - `### ✅ Completed`
  - `### 🔵 Evidence`
  - `### ⚠️ Risks`
  - `### 📌 Follow-ups`
  - `### 🔐 Security`
- 작은 작업이면 Risks / Follow-ups / Security 중 불필요한 섹션은 생략 가능하다. 단, secret / 실행 여부가 중요한 작업은 Security 를 남긴다.
- Summary 는 2~4문장으로 “무슨 작업이 어느 범위에서 완료/보류됐는지”만 적는다.
- Completed 는 5~10개 bullet 이내를 원칙으로 한다.
- Evidence 는 operation-notes / validation-checklist / runbook / common 문서 링크 중심으로 쓴다.
- Risks 는 신규 리스크 또는 기존 리스크 mitigation 보강만 표로 간단히 쓴다.
- Follow-ups 는 체크리스트(`- [ ]`)로 남긴다.
- Security 는 Kiro 가 직접 실행했는지, AWS CLI / boto3 / psql / Spring Boot / 외부 API / broker 주문 / aws-live 작업 / secret 기록 여부를 짧게 남긴다.
- Kiro 가 문서만 수정한 경우에는 “문서만 수정, 실행 0건, broker 주문 0건, aws-live 작업 0건, secret 원문 기록 0건”을 명확히 쓴다.

#### Kiro 문서 생성 / 수정 판단 순서

문서를 새로 만들거나 수정하기 전에 아래 순서로 판단한다.

1. 기존 문서에 흡수 가능한가?
   - 가능하면 새 파일을 만들지 않고 기존 spec 의 `operation-notes.md`, `validation-checklist.md`, `runbook.md`, `_common/*.md` 에 흡수한다.
2. 운영자가 매일 볼 문서인가?
   - 맞으면 README / runbook / validation-checklist / operation-notes 중 하나로 둔다.
   - 아니면 requirements / design / tasks / traceability-matrix 같은 Kiro/감사용 문서로 둔다.
3. 결정값이 바뀌는가?
   - 바뀌면 `operator-decisions.md` 를 먼저 갱신하고, 다른 문서에서는 Decision ID 와 짧은 요약만 참조한다.
4. 리스크가 새로 생기거나 mitigation 이 바뀌는가?
   - 바뀌면 `risk-register.md` 와 해당 spec operation-notes 를 갱신한다.
5. 후속 작업 순서나 의존성이 바뀌는가?
   - 바뀌면 `followups-overview.md` 를 갱신한다.
6. 비용 가정이 바뀌는가?
   - 바뀌면 `cost-simulation.md` 를 갱신한다.
7. 새 AWS 서비스 / 용어가 핵심이 되었는가?
   - 맞으면 `aws-resource-glossary.md` 를 갱신한다.

#### 작업 완료 후 갱신 범위 제한

- 작업 완료 후 자동 갱신 대상은 기본적으로 최대 3개 파일로 제한한다.
  - 현재 spec 의 `operation-notes.md`
  - 현재 spec 의 `validation-checklist.md` 또는 `runbook.md`
  - 필요 시 `.kiro/WORKLOG.md` 또는 `_common/operator-decisions.md`
- CHANGELOG 는 구조 변경, 신규 spec 생성, 주요 문서 재구성, 루트 공통 문서 의미 변경이 있을 때만 갱신한다.
- README 는 dashboard / navigation / 문서 구조가 바뀔 때만 갱신한다.
- 루트 공통 문서를 기계적으로 전부 업데이트하지 않는다. 현재 결정이나 spec 에 영향을 받는 문서만 업데이트한다.

#### 문서 품질 기준

- 같은 사실을 여러 문서에 길게 복사하지 않는다. 한 곳에 상세 기록하고 다른 문서는 링크한다.
- 표는 값 비교 / 상태 요약 / execution metadata 처럼 3개 이상 값이 있는 경우에 사용한다.
- 긴 slash chain 대신 표나 짧은 bullet 로 분리한다.
- “완료”라는 표현은 evidence 가 있을 때만 쓴다.
- “추정”, “가능성”, “후속 확인 필요”는 완료와 분리해서 쓴다.
- 실패한 명령 뒤에 SUCCESS / DONE marker 를 붙이지 않는다.
- 운영자가 직접 수행한 작업과 Kiro 가 문서로만 기록한 작업을 구분한다.
- 실제 AWS 리소스 생성 / 수정 / 삭제가 있었다면 “운영자 직접 수행”인지 “Kiro 문서화”인지 명확히 쓴다.

### 문서 최소화 / 운영자 가독성 규칙

- 새 문서를 만들기 전에 기존 문서에 흡수 가능한지 먼저 판단한다.
- 운영자가 매일 봐야 하는 문서는 각 spec의 README.md / runbook.md / validation-checklist.md / operation-notes.md로 제한한다.
- requirements.md / design.md / tasks.md / traceability-matrix.md는 Kiro/감사용 문서로 취급하고, 운영자용 요약을 반복해서 길게 쓰지 않는다.
- 같은 결정값은 operator-decisions.md에만 상세히 기록하고, 다른 문서에서는 Decision ID와 짧은 요약만 참조한다.
- 작업 완료 후 자동 갱신 대상은 기본적으로 최대 3개 파일로 제한한다.
  - 현재 spec의 operation-notes.md
  - 현재 spec의 validation-checklist.md
  - 필요 시 WORKLOG.md 또는 operator-decisions.md
- CHANGELOG.md는 구조 변경, 신규 spec 생성, 주요 문서 재구성 때만 갱신한다.

## Security Rules

실제 secret, password, token, app key, app secret, 계좌번호, webhook URL을 절대 작성하지 않는다.

모든 민감정보 값은 `[REDACTED]`로 표기한다.

요약, 로그, 예시, 표, 생성 문서 어디에도 민감정보 값을 출력하지 않는다.

## Execution Rules

Spec 작성 작업에서는 아래 행위를 하지 않는다.

* AWS 리소스 생성 금지
* 운영 entrypoint 실행 금지
* 애플리케이션 소스 코드 수정 금지
* broker 주문 스크립트 실행 금지
* live cutover 수행 금지
* live cutover 금지

AWS Console 작업이나 구현 절차가 필요한 경우에는 실제 실행하지 않고 runbook 단계로만 작성한다.

### 운영 명령 작성 규칙(추가)

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

## Markdown Readability Rules (NEW)

본 절은 `.kiro` 작업공간 루트 문서(README / WORKLOG / CHANGELOG / AGENTS.md 및 각 spec 루트의 요약 문서)를 dashboard / summary / navigation 용도로 유지하기 위한 서식 규칙이다. spec 하위의 상세 evidence 문서(operation-notes.md · validation-checklist.md · runbook.md · `_common/*.md`)에는 별도의 서식 제약을 강제하지 않는다.

### 원칙

- 루트 문서는 raw evidence 저장소가 아니다.
- 긴 evidence 는 specs/<spec>/operation-notes.md, specs/<spec>/validation-checklist.md, specs/<spec>/runbook.md, specs/_common/*.md 링크로 추적한다.
- 루트 문서의 목적은 dashboard / summary / navigation 이다.

### 상태 배지 (5종 · 우선순위 🔴 > 🟠 > 🟢 > 🔵 > ⚫)

한 줄 안에 여러 상태가 공존하면 위 우선순위가 높은 배지를 앞쪽에 배치한다. 배지 없이 상태를 문자로만 반복 서술하지 않는다.

| 배지 | 의미 |
|---|---|
| 🔴 | 치명 · 금지 · live · 고위험 |
| 🟠 | 대기 · 관찰 · Open · 미확정 |
| 🟢 | 완료 · 성공 · ENABLED · Mitigated |
| 🔵 | 참고 · evidence · 정보 |
| ⚫ | 해당 없음 · N/A (유의미한 경우만) |

### HTML 컬러 팔레트 (4종 · 치명 안전 안내 한정)

HTML 컬러는 GitHub Primer 계열의 아래 4종만 허용한다. 상태 배지로 이미 표현되는 정보에 컬러를 중복 부여하지 않으며, 치명 · 금지 · live · 승인 게이트 같은 안전 안내가 필요한 경우에 한정해 사용한다.

| HEX | 용도 |
|---|---|
| `#D1242F` | Critical |
| `#BF8700` | Warning |
| `#1A7F37` | Done |
| `#0969DA` | Info |

### Bold 사용 케이스

Bold 는 문단 단위 강조가 아닌 단일 값 · 단일 결론 강조에만 쓴다. 아래 5종 이외에는 Bold 를 남발하지 않는다.

- 최종 상태값
- 핵심 안전 문구
- 핵심 판정
- 중요 ID
- 주요 결론

### 금지 서식

- 2줄 이상 bullet
- 슬래시 체인 4개 이상 나열
- 표 없이 3개 이상 값 나열
- 탭 인덴트

### 서식 선택 규칙

- 값 3개 이상 → 표
- 후속 작업 → 체크리스트 (`- [ ]`)
- 안전 / 금지 / 경고 → 블록쿼트 (`>`)


## `_common` 문서 가독성 리팩토링 규칙

본 절은 `.kiro/specs/_common` 하위 6개 단일 기준 문서를 운영자가 빠르게 읽을 수 있도록 정리할 때 적용한다. `_common` 문서는 상세 기준의 source of truth 이므로 내용을 삭제하거나 의미를 축약해 잃어버리면 안 된다. 단, raw log 덩어리와 장문 mitigation 은 운영자 가독성을 해치지 않도록 구조화한다.

### 대상 문서

| 문서 | 역할 | 리팩토링 목표 |
| --- | --- | --- |
| `_common/operator-decisions.md` | 운영자 결정의 단일 기준 | 결정 요약, 핵심 결정, 상세 결정, 변경 이력을 명확히 분리 |
| `_common/risk-register.md` | 리스크 단일 누적 표 | 위험도별 dashboard, 긴 mitigation 분리, 탐색성 개선 |
| `_common/followups-overview.md` | 후속 spec 순서와 의존성 | 현재 남은 일, 완료 이력, spec별 backlog 분리 |
| `_common/ms-aws-service-decision-matrix.md` | MS별 AWS 서비스 선택 기준 | 최종 권고 요약을 앞에 두고 세부 비교는 뒤로 이동 |
| `_common/cost-simulation.md` | 월 비용 가정과 추정 | 비용 dashboard, 환경별 합계, 주요 비용 driver를 먼저 표시 |
| `_common/aws-resource-glossary.md` | AWS 용어집 | 카테고리별 목차와 일관된 항목 template 적용 |

### 공통 리팩토링 원칙

- 의미 보존을 최우선으로 한다. 기존 Decision ID, Risk ID, OD/R 번호, 비용 수치, status, spec 번호, 날짜, evidence 링크는 임의 변경하지 않는다.
- 최신 운영자가 바로 볼 dashboard 를 문서 상단에 둔다.
- 상세 근거, 과거 긴 이력, raw evidence 는 아래쪽 `Details`, `History`, `Appendix`, 또는 관련 spec `operation-notes.md` 링크로 보낸다.
- 같은 사실을 여러 문서에 장문 복사하지 않는다. 단일 기준 문서에는 상세를 두고 다른 문서에서는 ID와 한 줄 요약만 남긴다.
- 표가 너무 넓어지면 핵심 표와 상세 표로 나눈다. 핵심 표는 6~8컬럼 이내를 권장한다.
- 한 cell 안에 3개 이상 문장 또는 120자 이상 설명이 들어가면 `Notes` / `Evidence` / `Mitigation Details` 소제목으로 분리한다.
- 운영자 직접 수행 사실과 Kiro 문서화 사실을 분리한다.
- secret, password, token, app key, app secret, 계좌번호 원문, webhook URL, 실제 public IP, 실제 ARN, 실제 account-id, broker_order_no 원문은 절대 기록하지 않는다. 필요한 경우 `[REDACTED*]` placeholder 만 쓴다.

### `_common/operator-decisions.md` 규칙

- 상단 순서는 `Status Legend` → `Decision Summary` → `At a Glance` → `Detailed Decisions` → `Change Log` 를 유지한다.
- `Decision Summary` 에는 전체 / 확정 / 잠정 / 미정 / 보류 count 와 비용 영향 큰 결정, 후속 spec 영향 큰 결정만 둔다.
- `At a Glance` 는 운영자가 자주 보는 핵심 결정만 10~15개 이내로 유지한다.
- 상세 결정표는 카테고리별로 접기 쉬운 헤딩을 둔다: Environment, Network, RDS, Security, Compute, Orchestration, Safety, Cutover 등.
- 변경 이력은 상세 결정표 안에 장문으로 계속 붙이지 말고, 각 row 안에는 최신 짧은 evidence 요약만 남긴다. 긴 날짜별 설명은 `Change Log` 또는 해당 spec `operation-notes.md` 링크로 이동한다.
- 새 결정 추가 시 Decision ID, 선택값, status, 비용 영향, 운영 리스크, 후속 spec 영향, 근거 링크를 모두 채운다.

### `_common/risk-register.md` 규칙

- 상단에 `Risk Dashboard` 를 추가하거나 유지한다.
  - Open High risks
  - Mitigated High risks
  - Newly added risks
  - Risks needing operator action
- 메인 Risk Table 은 한 줄 요약 중심으로 유지한다.
- `Mitigation`, `Detection`, `Rollback` 이 길어지면 표 cell 에 장문을 넣지 말고 `Risk Details` 섹션에 Risk ID별로 분리한다.
- `Risk Details` 형식은 아래를 따른다.
  - `### R-AUTO-001 — 제목`
  - `Current status`
  - `Mitigation history`
  - `Detection`
  - `Rollback`
  - `Evidence links`
- Risk ID는 절대 재사용하지 않는다.
- status 변경은 `Open` / `Mitigated` / `Accepted` / `Closed` 중 하나로 유지하고, 운영자 표시에는 배지를 붙인다.

### `_common/followups-overview.md` 규칙

- 상단에 `Current Follow-up Dashboard` 를 둔다.
  - Now / Next / Later
  - Blocked
  - Done recently
- 오래된 날짜별 후속 메모가 길어지면 `Historical Notes` 아래로 이동한다.
- 현재 실행 가능한 후속 작업은 체크리스트로 분리한다.
- spec별 backlog 는 `03` ~ `10` 순서로 고정한다.
- 완료된 작업은 삭제하지 말고 `Completed / Evidence` 아래로 이동하고 operation-notes 링크를 붙인다.
- 후속 순서가 바뀌면 이 문서와 함께 필요한 경우 `operator-decisions.md` 또는 `risk-register.md` 도 갱신한다.

### `_common/ms-aws-service-decision-matrix.md` 규칙

- 상단에 `Final Recommendation Summary` 를 둔다.
  - MS
  - 1순위 서비스
  - 2순위/보류 서비스
  - 채택 이유 한 줄
  - 비용/운영 리스크 한 줄
- 상세 비교표는 MS별 또는 서비스별로 나누되, 한 표가 너무 길면 분리한다.
- “왜 Lambda/EKS/Beanstalk/App Runner가 아닌가”는 Appendix 또는 세부 비교 섹션에 둔다.
- 최종 권고가 바뀌지 않은 evidence 보강은 짧게만 쓰고, 장문 실행 결과는 operation-notes 링크로 보낸다.
- MS별 권고가 바뀌면 반드시 `operator-decisions.md` 와 정합성을 확인한다.

### `_common/cost-simulation.md` 규칙

- 상단에 `Cost Dashboard` 를 둔다.
  - paper 월 예상 비용
  - live 월 예상 비용
  - 주요 cost driver Top 5
  - 비용 절감 결정 Top 5
- 단가 가정은 별도 `Unit Price Assumptions` 섹션에 모은다.
- 환경별 합계와 MS별 breakdown 을 분리한다.
- 가격은 근사치임을 유지하고 `AWS Pricing Calculator 확인 필요` 단서를 반복적으로 남긴다.
- 비용 변경이 없는 evidence 보강은 장문으로 쓰지 말고 “신규 상시 컴퓨트 비용 없음”처럼 짧게 기록한다.
- 비용 가정 변경 시 변경 전/후와 영향을 받는 decision/risk/spec 링크를 남긴다.

### `_common/aws-resource-glossary.md` 규칙

- 상단에 카테고리별 목차를 둔다.
  - Network
  - Compute
  - Database
  - Security / IAM / Secrets
  - Orchestration
  - Observability
  - Storage / Artifact
- 각 용어는 동일 template 을 따른다.
  - 한 줄 설명
  - 이 포트폴리오에서의 역할
  - 비용 발생 여부
  - 운영자가 조심해야 할 점
  - 관련 spec
- glossary 에 운영 로그나 검증 결과를 길게 넣지 않는다. 특정 날짜 evidence 는 operation-notes 로 연결한다.
- 같은 AWS 서비스가 여러 spec에서 쓰이면 중복 항목을 만들지 말고 한 항목 안에 “이 포트폴리오에서의 역할”로 정리한다.

### 리팩토링 작업 절차

1. 먼저 6개 문서의 현재 heading 구조와 상단 100줄을 확인한다.
2. 각 문서의 source of truth 역할을 확인하고 삭제하면 안 되는 ID / 수치 / 결정값 / 링크를 목록화한다.
3. dashboard / summary / details / history / appendix 구조로 재배치한다.
4. 기존 ID, status, 날짜, evidence 링크가 보존됐는지 확인한다.
5. 민감정보 placeholder 정책을 재확인한다.
6. 작업 후 `.kiro/WORKLOG.md` 에 짧게 남긴다.
7. `_common` 문서 구조가 의미 있게 바뀌면 `.kiro/CHANGELOG.md` 에도 짧게 남긴다.

### 금지 사항

- source of truth 값을 보기 좋게 만든다는 이유로 삭제하지 않는다.
- Decision ID / Risk ID / status / 비용 수치 / 날짜를 임의 변경하지 않는다.
- 오래된 내용을 무조건 삭제하지 않는다. 필요하면 `Historical Notes` 로 이동한다.
- raw log 전문, SQL 전체 출력, AWS CLI 전체 JSON 응답, CloudWatch log 전문을 `_common` 문서에 붙이지 않는다.
- 실제 운영 명령을 실행하지 않는다. 문서 리팩토링만 수행한다.

## Update Checklist

작업 완료 시 아래 문서 중 실제 변경 영향을 받은 항목만 갱신한다. 루트 공통 문서를 기계적으로 전부 업데이트하지 않는다.

- [ ] `_common/operator-decisions.md` — 운영자 결정이 바뀌면 업데이트
- [ ] `_common/ms-aws-service-decision-matrix.md` — MS 별 AWS 서비스 권고가 바뀌면 업데이트
- [ ] `_common/cost-simulation.md` — 월 비용 가정 · RDS 크기 · VPC Endpoint 수 · ALB / NAT / EC2 / Fargate 사용 가정이 바뀌면 업데이트
- [ ] `_common/followups-overview.md` — 후속 spec 순서 · 의존성 · 범위가 바뀌면 업데이트
- [ ] `_common/aws-resource-glossary.md` — 새 AWS 서비스 / 용어가 Migration 에서 중요해지면 업데이트
- [ ] `.kiro/WORKLOG.md` — 의미 있는 Kiro 문서 작업 세션 후 업데이트
- [ ] `.kiro/CHANGELOG.md` — spec 구조 변경 · 루트 공통 문서 변경 · 신규 spec 생성 · 주요 문서 재구성 시 업데이트
- [ ] `.kiro/README.md` — 작업공간 목적 · 폴더 구조 · 주요 문서 목록이 바뀌면 업데이트
- [ ] 민감정보 노출 여부 점검 — secret / password / token / app key / app secret / 계좌번호 / webhook URL / 실제 ARN / 실제 public IP / broker_order_no / image digest 원문 대신 `[REDACTED*]` placeholder 사용 확인
