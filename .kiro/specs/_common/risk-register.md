# Risk Register — AWS Migration

## Purpose

본 문서는 PORT-STRATEGY-AI AWS Migration 전체(`01-aws-migration-foundation` · `02-aws-network-and-rds` · 후속 03 ~ 10) 에 걸친 운영 / 보안 / 비용 리스크를 단일 표로 누적 관리하는 단일 진실원(source of truth) 이다. spec 진행 중 새 리스크가 식별되면 본 표 아래에 같은 형식으로 추가한다. 한 번 부여한 Risk ID(`R-*`) 는 재사용하거나 재번호 부여하지 않는다.

문서 흐름: **Purpose → Risk Dashboard → Immediate Action Risks → Risk Index → Risk Details → Accepted/Closed Risks → Risk Update Rules → Security Notes**. 표(Risk Index) 는 짧은 인덱스 역할만 담고, 긴 `Mitigation` · `Detection` · `Rollback` · `Evidence` 는 하단 `Risk Details` 로 이동한다. Risk ID 별 상세 필드 순서: `Current → Mitigation → Detection → Rollback → Evidence`.

원문 그대로 보존되는 사실 값은 아래와 같다.

- Risk ID(`R-*`).
- Status 문자열(Open · Mitigated · Accepted · Closed).
- Impact · Probability · Affected Spec.
- 날짜(YYYY-MM-DD) · 비용 수치.
- Scheduler / State Machine / Lambda / IAM Role 이름 · executionName · spec 번호.
- evidence 관계.

민감정보 원문은 본 문서 어디에도 기록하지 않고 `[REDACTED*]` placeholder 계열만 사용한다.

- secret · password · token · KIS app key · KIS app secret · Slack webhook URL.
- 계좌번호 · account-id · broker_order_no · image digest full sha256.
- 실제 ARN · public IP · EIP · ENI ID · task ARN.
- placeholder 예: `[REDACTED]` · `[REDACTED_ACCOUNT_NO]` · `[REDACTED_PUBLIC_IP]` · `[REDACTED_ARN]` · `[REDACTED_TASK_ARN]` · `[REDACTED_SECRET_ARN]` · `[REDACTED_BROKER_ORDER_NO]`.

raw log 전문 · SQL 전체 출력 · AWS CLI 전체 JSON 응답 · CloudWatch log 전문 · Step Functions execution history 전문 · Slack builder payload · full message body · DB after-check raw output · SHA256 raw hex · full executionName 은 본 문서 본문에 붙여넣지 않고 spec `operation-notes.md` 상대경로 링크로 대체한다.

## Risk Dashboard

운영자가 매일 조회하는 리스크 요약이다. 집계 시점 2026-07-01 기준. 상세 근거와 mitigation 이력은 아래 `Risk Table` 및 `Risk Details` 섹션에서 확인한다. 상태 배지 우선순위 🔴 Open High · 🟠 Open · 🟢 Mitigated · 🔵 Accepted · ⚫ Closed.

### 상태 요약 (전체 75건 기준)

| 상태 | 건수 | 배지 |
|---|---|---|
| Open | 31 | 🟠 (High 21건은 🔴) |
| Mitigated | 42 | 🟢 |
| Accepted | 2 | 🔵 |
| Closed | 0 | ⚫ |

Area count: Automation 37 · Data 17 · Security 7 · Broker 5 · Network 4 · Cost 3 · Docs 2. Impact count: High 46 · Medium 27 · Low 2.

### 그룹 1 — 🔴 Open High risks (21건 · 즉시 관심 대상)

Impact High + Status Open. 상세 mitigation / detection / rollback / evidence 는 아래 `Risk Table` 및 `Risk Details` 참조.

| Risk ID | Area | 한 줄 위험 요약 | Affected Spec |
|---|---|---|---|
| R-NET-001 | Network | Public subnet ECS Task SG inbound 잘못 열림 → 외부 노출 | 02, 08 |
| R-NET-003 | Network | VPC Endpoint 5종 누락 → ECR pull / secret 주입 / 로그 송신 실패 | 02, 06, 07 |
| R-SEC-001 | Security | RDS SG inbound 광범위 CIDR → RDS 외부 노출 | 02 |
| R-SEC-002 | Security | Root 자격(비밀번호 / MFA) 분실 또는 노출 → 비상 복구 불가 | 02 |
| R-SEC-003 | Security | portadmin 자격 분실 → 후속 spec 진행 중단 | 02, 06 |
| R-SEC-004 | Security | Flask debug 모드 운영 진입 → stack trace 외부 노출 | 03, 05 |
| R-SEC-010 | Security | app role DB password 평문 노출 이력 → rotate 후속 필요 | 02, 03, 04, 05, 06, 08, 09, 10 |
| R-DATA-001 | Data | RDS role search_path 미설정 → SQL 조회 실패 · 잘못된 schema 참조 | 02 |
| R-DATA-002 | Data | pg_dump / restore cutover 정합성 깨짐 | 02, 10 |
| R-DATA-007 | Data | local vs AWS Paper RDS 병합 → source of truth 붕괴 | 02, 03, 04, 05, 10 |
| R-DATA-010 | Data | non-GUI raw 최신성 부족 → downstream stale data | 08, 04, 09 |
| R-DATA-017 | Data | worker exit 0 ≠ KRX raw 최신성 보장 → stale data 전파 | 04, 08, 09, 10 |
| R-BROKER-001 | Broker | EIP detach / EC2 교체 → broker 등록 IP 불일치 | 03 |
| R-BROKER-002 | Broker | 단일 access_token 다중 갱신 충돌 | 03 |
| R-AUTO-001 | Automation | Step Functions Retry 잘못 활성화 → 중복 broker 주문 | 04, 10 |
| R-AUTO-002 | Automation | aws-live 자동 BUY/SELL 승인 없이 조기 활성화 | 04, 05, 10 |
| R-AUTO-014 | Automation | 단일 Task Definition + command override 오매핑 | 04, 05, 10 |
| R-AUTO-016 | Automation | Administrator interactive session 부재 → KRX GUI 수집 실패 | 08 |
| R-AUTO-030 | Automation | Intraday Stop Sell Submit 자동 ENABLE 승인 없이 진입 위험 | 03, 04, 10 |
| R-AUTO-034 | Automation | Fargate Task Role `states:StartExecution` 광역 부여 · Step 12 gate 우회 | 04, 05, 06, 10 |
| R-DOCS-001 | Docs | spec 산출물에 secret / token / 계좌번호 / webhook 평문 기록 | 모든 spec |

### 그룹 2 — 🟢 Mitigated High risks (총 25건)

Impact High + Status Mitigated. 정기 audit + detection 계속 유지. 대표 항목만 요약 (상세는 `Risk Table` 참조).

| Risk ID | Area | 한 줄 mitigation 요약 | Affected Spec |
|---|---|---|---|
| R-DATA-003 | Data | pg_dump/pg_restore major version 정합 | 02, 03, 10 |
| R-DATA-004 | Data | `--no-owner --no-privileges` + 사후 GRANT 재구성 | 02, 10 |
| R-DATA-005 | Data | GRANT 매트릭스 정식 정리 · 17-step E2E 검증 통과 | 02, 04, 05, 06, 08 |
| R-DATA-006 | Data | Secrets Manager JSON multi-key 재생성 | 02, 08 |
| R-DATA-011 | Data | `marketconnector_app` legacy schema search_path 보정 | 02, 03, 06 |
| R-DATA-012 | Data | 추가매수 case unique constraint merge patch | 04, 10 |
| R-DATA-013 | Data | `execution_app` decision UPDATE 권한 정식 부여 | 02, 04 |
| R-DATA-014 ~ 016 | Data | Intraday 안전장치 4종 (stale · duplicate · sellable_qty · current_price) | 03, 04, 10 |
| R-AUTO-005 · 007 · 017 · 020 | Automation | Crawler ECS + KRX worker hybrid + Step 2 fail-closed | 08, 02, 04, 05, 09 |
| R-AUTO-009 · 010 · 011 | Automation | Strategy Execution `--execute` end-to-end 검증 통과 | 03, 04, 05 |
| R-AUTO-015 | Automation | Research heavy job smoke/full prefix + attempt=1 | 09, 10 |
| R-AUTO-018 | Automation | KIS `output1 empty` summary fallback guard patch | 03, 04 |
| R-AUTO-019 | Automation | wrapper PAPER_ORDER_GATE + `-AllowPaperOrderExecute` 명시 요구 | 03, 04, 10 |
| R-AUTO-021 | Automation | MarketConnector `/tmp/inject-env.sh` bootstrap 함수 | 03, 04, 06 |
| R-AUTO-022 | Automation | Step 12 retry-normalizer 내장 (`40580000`/`EGW00201`) | 03, 04, 10 |
| R-AUTO-023 | Automation | Step Functions Catch `DAILY_EXECUTION_FAILED` Slack | 04, 05, 10 |
| R-AUTO-025 · 026 · 028 | Automation | Scheduler · Dispatcher · Step Functions 사슬 검증 | 03, 04, 05, 08, 10 |
| R-AUTO-029 | Automation | Duplicate `INTRADAY_STOP_SELL` READY order 사전 점검 | 03, 04, 10 |
| R-AUTO-031 · 032 | Automation | Intraday approval gate blocked/true-path · signal-type 필터 | 03, 04, 05, 10 |
| R-AUTO-033 | Automation | port-view Local Spring Boot read-only 콘솔 default false | 04, 05, 10 |
| R-AUTO-035 · 036 · 037 | Automation | Daily Brief Slack mini SF · Intraday Slack · Step 12~17 자동 ENABLE | 03, 04, 05, 06, 10 |
| R-BROKER-004 | Broker | KIS paper timeout 재실행 전 broker_order_no 사전 점검 | 03, 04, 10 |
| R-BROKER-005 | Broker | 없는 포지션 매도 사전 점검 4종 통과 후 진입 | 03, 04, 10 |
| R-DOCS-002 | Docs | wrapper 로그 secret grep 정기 점검 | 06, 08 |

### 그룹 3 — 🟠 Newly added risks (2026-06-24 ~ 2026-07-01, 8건)

| Risk ID | Area | 추가 일자 | Status |
|---|---|---|---|
| R-AUTO-034 | Automation | 2026-06-29 | 🔴 Open |
| R-AUTO-035 | Automation | 2026-06-30 | 🟢 Mitigated |
| R-AUTO-036 | Automation | 2026-06-30 | 🟢 Mitigated |
| R-AUTO-037 | Automation | 2026-07-01 | 🟢 Mitigated |
| R-SEC-010 | Security | 2026-06-29 | 🔴 Open |
| R-DATA-017 | Data | 2026-06-28 | 🔴 Open |
| R-AUTO-033 | Automation | 2026-06-27 | 🟢 Mitigated |
| R-AUTO-028 | Automation | 2026-06-24 | 🟢 Mitigated |

### 그룹 4 — 🟠 운영자 조치 필요 리스크

| Risk ID | Area | 필요 조치 요약 | Affected Spec |
|---|---|---|---|
| R-SEC-010 | Security | 노출된 app role password rotate + Secrets Manager 갱신 + secret loader 재검증 | 06 후속 |
| R-AUTO-002 | Automation | aws-live 자동 BUY/SELL 별도 Decision + paper N영업일 통과 후 진입 | 10 후속 |
| R-AUTO-030 | Automation | Intraday Stop Sell 자동 ENABLE 진입 시 운영자 별도 승인 | 03, 04, 10 후속 |
| R-AUTO-034 | Automation | Fargate Task Role `states:StartExecution` Resource 를 approval + safe 두 ARN 한정 재부여 | 06 후속 |
| R-DATA-017 | Data | Step2B 성공 기준 강화 + Slack KRX 기준일 표시 + feature lag 정책 명문화 | 04, 08, 09 후속 |
| R-AUTO-014 | Automation | Step Functions state name ↔ command override 매핑 표 정식 정리 | 04 후속 |
| R-DATA-010 | Data | non-GUI raw 최신성 회복 후 preprocessor 재실행 | 08 후속 |
| R-COST-001 · R-COST-002 · R-COST-003 | Cost | 월 비용 spike / NAT 미사용 재확인 / S3 lifecycle 정책 도입 | 06, 10 후속 |

### 컬럼 정의

- Risk ID: `R-{Area}-NNN` 형식. 한 번 부여 후 안정 유지.
- Area: 리스크 영역. `Network` / `Security` / `Data` / `Broker` / `Automation` / `Cost` / `Docs`.
- Risk: 발생 가능한 사건의 한 줄 요약.
- Impact: 발생 시 영향. `High` / `Medium` / `Low`.
- Probability: 현재 구조에서 발생할 가능성. `High` / `Medium` / `Low`.
- Mitigation: 사전 차단 / 완화 조치.
- Detection: 발생을 감지하는 방법(메트릭 / 로그 / 검증 SQL / 운영 점검).
- Rollback: 사후 대응 / 되돌리기 방법.
- Affected Spec: 본 리스크가 다뤄지는 spec 번호.
- Status: `Open` / `Mitigated` / `Accepted` / `Closed`.

### 상태 정의

- Open: 식별되었으나 mitigation이 적용되지 않았다.
- Mitigated: mitigation이 적용되어 영향이 작다. 그러나 모니터링은 계속한다.
- Accepted: 의도적으로 수용한다(예: aws-paper single-AZ 다운타임 허용).
- Closed: 구조 변경으로 더 이상 해당하지 않는다.

## Risk Index

| Risk ID | Area | Status | Impact | Risk | Next Action |
|---|---|---|---|---|---|
| R-NET-001 | Network | 🔴 Open | High | Public subnet ECS Task SG inbound 잘못 열어 외부 노출 가능 | Task SG inbound 0.0.0.0/0 금지 · outbound 필요 도메인만 허용. [See details: R-NET-001 Details](#r-net-001-details) |
| R-NET-002 | Network | 🟠 Open | Medium | NAT-free 구조에서 외부 outbound workload 를 private subnet 에 잘못 배치해 외부 호출 실패 | 외부 outbound workload 는 public subnet + assignPublicIp=ENABLED 배치. [See details: R-NET-002 Details](#r-net-002-details) |
| R-NET-003 | Network | 🔴 Open | High | VPC Endpoint 5종 누락 시 ECR pull · Secrets · SSM · CloudWatch Logs 실패 | 02 runbook 의 VPC Endpoint 5종 활성 · sg-vpc-endpoints inbound 443 확인. [See details: R-NET-003 Details](#r-net-003-details) |
| R-SEC-001 | Security | 🔴 Open | High | RDS SG inbound 광범위 CIDR → RDS 외부 노출 | sg-rds-postgres inbound 는 다른 SG 참조만 허용 · publicly accessible false. [See details: R-SEC-001 Details](#r-sec-001-details) |
| R-DATA-001 | Data | 🔴 Open | High | RDS role search_path 미설정 → 잘못된 schema 조회 | 7 role 에 ALTER ROLE ... SET search_path 적용 · role 별 SHOW search_path 검증. [See details: R-DATA-001 Details](#r-data-001-details) |
| R-DATA-002 | Data | 🔴 Open | High | pg_dump / pg_restore cutover 정합성 깨짐 · sequence · default privilege 누락 | dump 옵션 고정 + restore 후 검증 SQL · row count local-dev ±0 비교. [See details: R-DATA-002 Details](#r-data-002-details) |
| R-BROKER-001 | Broker | 🔴 Open | High | EIP detach / EC2 교체로 broker 등록 IP 불일치 → broker 호출 실패 | EC2+EIP 1순위 유지 · EIP running attach 유지 · 교체 시 detach·attach 절차. [See details: R-BROKER-001 Details](#r-broker-001-details) |
| R-BROKER-002 | Broker | 🔴 Open | High | 단일 access_token 다중 갱신 충돌 (EC2 다중화 · Fargate 다중 Task) | 단일 instance 운영 · Fargate 옵션 시 desiredCount=1 · 토큰 백업 정책. [See details: R-BROKER-002 Details](#r-broker-002-details) |
| R-AUTO-001 | Automation | 🔴 Open | High | Step Functions Retry 잘못 활성화 → BUY / SELL / fill sync / position 중복 주문 | SF state 정의부 Retry 정책 review · BUY / SELL / fill sync / position 재시도 금지. [See details: R-AUTO-001 Details](#r-auto-001-details) |
| R-AUTO-002 | Automation | 🔴 Open | High | aws-live 자동 BUY/SELL 승인 없이 조기 활성화 → 실계좌 잘못된 주문 위험 | aws-live BUY/SELL 별도 Decision + paper N영업일 통과 후 진입. [See details: R-AUTO-002 Details](#r-auto-002-details) |
| R-DOCS-001 | Docs | 🔴 Open | High | spec 산출물에 secret / token / 계좌번호 / webhook 평문 기록 위험 | [REDACTED*] placeholder 유지 · git history secret scan · 매 회차 사후 grep. [See details: R-DOCS-001 Details](#r-docs-001-details) |
| R-COST-001 | Cost | 🟠 Open | Medium | ALB / NAT / VPC Endpoint multi-AZ / RDS multi-AZ 동시 활성화로 월 비용 급증 | NAT 미사용 · ALB 초기 미사용 · paper single-AZ 유지 · Cost Explorer 점검. [See details: R-COST-001 Details](#r-cost-001-details) |
| R-COST-002 | Cost | 🟠 Open | Medium | IaC / Console 실수로 NAT Gateway 생성 → 시간당 ~$0.06 + 데이터 처리 비용 | 02 runbook NAT 미사용 확인 · validation-checklist 항목 포함. [See details: R-COST-002 Details](#r-cost-002-details) |
| R-SEC-002 | Security | 🔴 Open | High | Root 자격(비밀번호 / MFA) 분실 · 노출 → 비상 복구 불가 · 외부 침해 위험 | Root MFA · 백업 코드 안전 보관 · 일상 작업은 portadmin 사용. [See details: R-SEC-002 Details](#r-sec-002-details) |
| R-SEC-003 | Security | 🔴 Open | High | portadmin 자격 분실 → AWS Console / IAM 작업 중단 | portadmin MFA · 백업 코드 안전 보관 · Root 로 재설정 경로 유지. [See details: R-SEC-003 Details](#r-sec-003-details) |
| R-DATA-003 | Data | 🟢 Mitigated | High | dump source · RDS engine major version 불일치 → pg_restore 실패 | client / RDS 를 dump source 이상 major 로 유지 · 다운그레이드 금지. [See details: R-DATA-003 Details](#r-data-003-details) |
| R-DATA-004 | Data | 🟢 Mitigated | High | dump 안 owner role 부재로 restore 실패 · 권한 부정확 설정 | pg_restore --no-owner --no-privileges + 사후 GRANT 재구성. [See details: R-DATA-004 Details](#r-data-004-details) |
| R-NET-004 | Network | 🟢 Mitigated | Medium | RDS Public No → 로컬 PC 직접 접속 불가 · restore / 검증 SQL 수행 불가 | 같은 VPC EC2 를 restore runner 로 · SSM Port Forwarding tunnel 사용. [See details: R-NET-004 Details](#r-net-004-details) |
| R-DATA-005 | Data | 🟢 Mitigated | Medium | DB Role 최소 권한 적용 시 시나리오별 권한 부족 · 과다 가능 | 02 db-roles-and-grants §5 검증 SQL 정기 점검 · 개별 GRANT 보정 누적. [See details: R-DATA-005 Details](#r-data-005-details) |
| R-AUTO-003 | Automation | 🟠 Open | Medium | EC2 EBS 부족 → venv / 로그 / access_token / Connector 산출물 저장 실패 | EBS 사용률 알람 · 로그 rotate · access_token 백업 정책. [See details: R-AUTO-003 Details](#r-auto-003-details) |
| R-BROKER-003 | Broker | 🟠 Open | Medium | KIS API rate limit 초과로 잔고 / 주문 조회 / view API 실패 | 임시 검증 단계 호출 빈도 최소화 · token 재사용 · Restart 캡. [See details: R-BROKER-003 Details](#r-broker-003-details) |
| R-SEC-004 | Security | 🔴 Open | High | Flask debug 모드가 운영 환경 진입 시 stack trace · env · 내부 경로 외부 노출 | CONNECTOR_DEBUG 운영 강제 false · 검증 이후 즉시 unset · systemd env 관리. [See details: R-SEC-004 Details](#r-sec-004-details) |
| R-AUTO-004 | Automation | 🟠 Open | Medium | systemd unit / startup 미설정 → EC2 reboot 후 Connector · Flask 자동 재기동 실패 | systemd unit + Restart=on-failure 도입 · 임시 단계는 수동 재기동 노트. [See details: R-AUTO-004 Details](#r-auto-004-details) |
| R-DATA-006 | Data | 🟢 Mitigated | High | Secrets Manager JSON multi-key 누락 → ECS Task 잘못된 DB host 접속 실패 | JSON 5종 key 포함 · Task Definition secrets 1:1 매핑 사전 점검. [See details: R-DATA-006 Details](#r-data-006-details) |
| R-AUTO-005 | Automation | 🟢 Mitigated | Medium | crawler ECS Task Selenium / Chromium / KRX outbound 검증 없이 운영 진입 위험 | ECS smoke RunTask 통과 · KRX GUI 는 Windows EC2 worker 분리(OD-MS-011/012). [See details: R-AUTO-005 Details](#r-auto-005-details) |
| R-AUTO-006 | Automation | 🟢 Mitigated | Medium | Windows worker 다운로드 경로 가정 vs 실제 Chrome 경로 불일치로 CSV 인식 실패 | C:\Users\USER\Downloads → Administrator\Downloads junction 유지 + 매 실행 점검. [See details: R-AUTO-006 Details](#r-auto-006-details) |
| R-SEC-005 | Security | 🟢 Mitigated | Medium | worker IAM Role 의 KRX 로그인 secret read 권한 누락 → AccessDenied · 기동 실패 | worker IAM Role inline policy 에 secret ARN 한정 GetSecretValue 부여. [See details: R-SEC-005 Details](#r-sec-005-details) |
| R-DOCS-002 | Docs | 🟢 Mitigated | High | Windows worker KRX / RDS password 가 PowerShell · wrapper · 로그 파일 평문 노출 위험 | wrapper password Write-Host 금지 · 로그 secret grep 정기 점검. [See details: R-DOCS-002 Details](#r-docs-002-details) |
| R-AUTO-007 | Automation | 🟢 Mitigated | Medium | EC2 worker wrapper exit 0 이 실제 DB 적재 성공을 보장 못함 → stale data 전파 | wrapper 안 KRX raw DB validation SSM step 자동 호출 · 실패 시 Step 2 fail. [See details: R-AUTO-007 Details](#r-auto-007-details) |
| R-AUTO-008 | Automation | 🟢 Mitigated | Medium | SSM SYSTEM SessionId 0 컨텍스트 wrapper 실행 시 KRX GUI 로그인 실패 위험 | SSM 은 schtasks /Run 만 트리거 · wrapper 실행은 Administrator interactive Task. [See details: R-AUTO-008 Details](#r-auto-008-details) |
| R-DATA-007 | Data | 🔴 Open | High | local PostgreSQL ↔ AWS Paper RDS 병합 → 중복 주문 · 잘못된 fill · source of truth 붕괴 | OD-ENV-006/007/008 명문화 · PORT_ENVIRONMENT + PORT_DB_TARGET guard. [See details: R-DATA-007 Details](#r-data-007-details) |
| R-AUTO-009 | Automation | 🟢 Mitigated | High | MarketConnector executor --execute 실 REQUESTED 처리 미검증 상태로 운영 진입 위험 | 17-step E2E 로 KIS paper BUY 4건 SUBMITTED 통과 · aws-live 진입 전 추가 검증. [See details: R-AUTO-009 Details](#r-auto-009-details) |
| R-AUTO-010 | Automation | 🟢 Mitigated | High | executor --execute guard 우회 · 환경변수 누락 시 잘못된 DB / broker 로 흘러갈 위험 | PORT_ENVIRONMENT=paper + PORT_DB_TARGET=aws-paper 조합 강제 · env 누락 시 진입 거부. [See details: R-AUTO-010 Details](#r-auto-010-details) |
| R-AUTO-011 | Automation | 🟢 Mitigated | Medium | View Daily Batch 12~17 구간 paper end-to-end 미검증 채로 운영 진입 위험 | 17-step E2E 로 12~17 paper 통과 · live cutover 전 평일 / 안전 데이터 추가 검증. [See details: R-AUTO-011 Details](#r-auto-011-details) |
| R-AUTO-012 | Automation | 🟢 Mitigated | Medium | SSM Port Forwarding tunnel 단절 시 로컬 paper 검증 · app role 접속 즉시 불가 | OD-NET-010 표준 경유지 유지 · tunnel 창 종료 금지 · 실패 시 재기동 절차. [See details: R-AUTO-012 Details](#r-auto-012-details) |
| R-AUTO-013 | Automation | 🟢 Mitigated | Low | 로컬 PC psql client 미설치 / PATH 미등록으로 SSM tunnel 위 SQL 점검 불가 | PostgreSQL 18 client 설치 + PATH 등록 또는 full path · psycopg2 우회. [See details: R-AUTO-013 Details](#r-auto-013-details) |
| R-AUTO-014 | Automation | 🔴 Open | High | 단일 Task Definition + command override 오매핑 → 잘못된 entrypoint 실행 위험 | SF state name ↔ command override 매핑 표 정식 정리 · review checklist. [See details: R-AUTO-014 Details](#r-auto-014-details) |
| R-AUTO-015 | Automation | 🟢 Mitigated | Medium | Research heavy backtest / report full 실행 실수 → Batch 비용 · vCPU · RDS 부하 | smoke / full prefix + attempt=1 · SubmitJob 수동 유지. [See details: R-AUTO-015 Details](#r-auto-015-details) |
| R-DATA-008 | Data | 🟠 Open | Medium | Research 내부 adapter 3종이 port_strategy_common 계약 변경 미추종 → 결과 불일치 | py_compile / import smoke + Daily Decision vs Research sample 비교. [See details: R-DATA-008 Details](#r-data-008-details) |
| R-COST-003 | Cost | 🟠 Open | Medium | S3 리포트 lifecycle 미설정 → storage 비용 누적 · public read 부주의 노출 위험 | Job Role PutObject prefix 한정 · S3 lifecycle 정책 도입 후속. [See details: R-COST-003 Details](#r-cost-003-details) |
| R-DATA-009 | Data | 🟠 Open | Medium | smoke 통과 표현이 raw 최신성 완료로 오해되어 stale data 기반 운영 진입 위험 | 표현을 "부분 완료" 로 통일 · non-GUI raw 7종 MAX(trade_date) SQL 점검. [See details: R-DATA-009 Details](#r-data-009-details) |
| R-DATA-010 | Data | 🔴 Open | High | non-GUI raw 최신성 미회복 → preprocessor · Research · Decision stale raw 전파 위험 | raw 최신성 회복 후 preprocessor 재실행 · 신규 feature date 검증. [See details: R-DATA-010 Details](#r-data-010-details) |
| R-SEC-009 | Security | 🟠 Open | Medium | Windows worker Autologon 자격 · RDP 노출로 KRX worker + EC2 OS 동시 침해 위험 | Autologon 은 paper worker 한정 · RDP 운영자 IP 한정 · SSM 우선. [See details: R-SEC-009 Details](#r-sec-009-details) |
| R-AUTO-016 | Automation | 🔴 Open | High | Windows worker Administrator interactive session 부재 → KRX GUI 수집 실패 위험 | Autologon bootstrap · query user Active pre-check · wrapper 로그 login 모니터. [See details: R-AUTO-016 Details](#r-auto-016-details) |
| R-AUTO-017 | Automation | 🟢 Mitigated | Low | wrapper 종료 후 Chrome process 잔존 → 메모리 누수 · 다음 Selenium attach 충돌 | step-02 wrapper 가 KRX 실행 직전 chrome/chromedriver best-effort reset. [See details: R-AUTO-017 Details](#r-auto-017-details) |
| R-AUTO-018 | Automation | 🟢 Mitigated | High | KIS output1 empty + summary fallback 오매핑 → order/fill 오염 · 잘못 전파 위험 | summary fallback guard 패치 + 단건 direct-only 기본화(OD-MS-025). [See details: R-AUTO-018 Details](#r-auto-018-details) |
| R-DATA-011 | Data | 🟢 Mitigated | Medium | marketconnector_app legacy schema USAGE/DML/search_path 누락 → BALANCE_REFRESH 실패 | legacy schema USAGE/DML + search_path 보정 + default privileges 갱신. [See details: R-DATA-011 Details](#r-data-011-details) |
| R-AUTO-019 | Automation | 🟢 Mitigated | High | Step 12 wrapper 가 의도 없이 -AllowPaperOrderExecute 로 실행 → KIS 실주문 위험 | wrapper 중앙 + 내부 이중 gate · default OFF · summary PaperOrder 필드 기록. [See details: R-AUTO-019 Details](#r-auto-019-details) |
| R-AUTO-020 | Automation | 🟢 Mitigated | High | Scheduled Task trigger 성공만으로 Step 2 SUCCESS 시 stale raw · KRX login 실패 미탐 | chrome reset + Running→Ready wait + Last Result 0 + KRX raw DB validation. [See details: R-AUTO-020 Details](#r-auto-020-details) |
| R-BROKER-004 | Broker | 🟢 Mitigated | High | KIS paper timeout 후 단순 재실행 → 같은 주문 broker 중복 접수 위험 | 재실행 전 broker_order_no 사전 점검 · 부재 시만 REQUESTED 통제 복구. [See details: R-BROKER-004 Details](#r-broker-004-details) |
| R-DATA-012 | Data | 🟢 Mitigated | High | 추가매수 case unique 제약 충돌 → position_state 갱신 실패 · 정합성 위험 | execution_sync_buy_position INSERT/UPDATE merge patch. [See details: R-DATA-012 Details](#r-data-012-details) |
| R-AUTO-021 | Automation | 🟢 Mitigated | High | EC2 stop/start 후 /tmp/inject-env.sh 휘발 → Daily wrapper Step 1/12/13/17 실패 | wrapper bootstrap 함수가 Step 1/12/13/17 직전 env 재생성. [See details: R-AUTO-021 Details](#r-auto-021-details) |
| R-DATA-013 | Data | 🟢 Mitigated | High | execution_app 의 decision.strategy_daily_position_decision UPDATE 권한 누락 | GRANT USAGE ON SCHEMA decision + UPDATE ON TABLE 한정 부여. [See details: R-DATA-013 Details](#r-data-013-details) |
| R-AUTO-022 | Automation | 🟢 Mitigated | Medium | REJECTED rejection_code=40580000 시 Step 12 기본 필터 미포함 → 자동 재제출 단절 | Step 12 retry-normalizer 내장 · 6종 조건 만족 시만 REQUESTED 복구. [See details: R-AUTO-022 Details](#r-auto-022-details) |
| R-AUTO-023 | Automation | 🟢 Mitigated | High | SF Catch/Fail state 에 Slack notifier 누락 시 실패 인지 지연 · 정합성 위험 | SF Catch → DAILY_EXECUTION_FAILED Slack notifier · 3종 이벤트 라벨. [See details: R-AUTO-023 Details](#r-auto-023-details) |
| R-AUTO-024 | Automation | 🔵 Accepted | Medium | Slack notifier Lambda 의 webhook URL 환경변수 장기 보관 시 secret 노출 위험 | 운영 안정화 후 Secrets Manager / SSM SecureString 이전 재검토. [See details: R-AUTO-024 Details](#r-auto-024-details) |
| R-AUTO-025 | Automation | 🟢 Mitigated | High | Scheduler → Dispatcher → SF 사슬 실패 시 Daily 자동 검증 진입 누락 | Scheduler IAM 최소 권한 + Dispatcher dryRun 검증 + 단계적 활성화 + Catch Slack. [See details: R-AUTO-025 Details](#r-auto-025-details) |
| R-AUTO-028 | Automation | 🟢 Mitigated | Medium | EC2 lifecycle Scheduler 오류로 07:50 start · 15:50 stop · Crawler stop 실행 실패 | 07:50 · 15:50 Scheduler 2건 ENABLED + Lambda dryRun 통과 + 주말 skip. [See details: R-AUTO-028 Details](#r-auto-028-details) |
| R-DATA-014 | Data | 🟢 Mitigated | High | 장중 Snapshot Refresh 실패/지연 시 stale snapshot 상태에서 Evaluate 진입 위험 | OD-MS-035 안전장치 4종 · snapshot as_of_ts 검증 · fail-closed. [See details: R-DATA-014 Details](#r-data-014-details) |
| R-AUTO-029 | Automation | 🟢 Mitigated | High | Intraday Evaluate 반복 호출로 duplicate READY 생성 · signal_type 필터 누락 | evaluate 사전 duplicate 점검 · Submit & Refresh 의 signal_type 전용 필터. [See details: R-AUTO-029 Details](#r-auto-029-details) |
| R-DATA-015 | Data | 🟢 Mitigated | High | sellable_qty 0 / 부족 상태에서 손절 후보 인식 · Daily SELL 중복 손절 위험 | evaluate 단계 sellable_qty 사전 비교 · 기존 SELL 진행 시 회피. [See details: R-DATA-015 Details](#r-data-015-details) |
| R-DATA-016 | Data | 🟢 Mitigated | High | Snapshot current_price null / 0 / stale 상태에서 Evaluate → 잘못된 손절 판단 | current_price null·0·과거·범위 초과 시 evaluate 진입 차단. [See details: R-DATA-016 Details](#r-data-016-details) |
| R-AUTO-030 | Automation | 🔴 Open | High | 3단계 Intraday Stop Sell 자동 ENABLE 조기 진입 → 승인 gate 없이 broker 손절 위험 | 초기 DISABLED 유지 · 운영자 별도 승인 후 진입 · Slack 안전 gate. [See details: R-AUTO-030 Details](#r-auto-030-details) |
| R-AUTO-031 | Automation | 🟢 Mitigated | High | Intraday Stop Sell approval gate 오구성으로 false 상태에서도 true-path 진입 위험 | blocked gate 안전 테스트 · Daily Step 12 와 분리 · signal_type 필터. [See details: R-AUTO-031 Details](#r-auto-031-details) |
| R-AUTO-032 | Automation | 🟢 Mitigated | High | Daily SELL · Intraday 이 동일 executor 호출 시 signal_type 필터 부재로 중복 broker 호출 | --intraday-stop-only 옵션 · signal_type=INTRADAY_STOP_SELL 만 처리. [See details: R-AUTO-032 Details](#r-auto-032-details) |
| R-BROKER-005 | Broker | 🟢 Mitigated | High | 보유 없음 상태 실주문 테스트 시 REJECTED · OPEN 포지션 없음으로 잘못된 row 생성 | output1_count + strategy_position_state OPEN + sellable_qty 4종 사전 점검. [See details: R-BROKER-005 Details](#r-broker-005-details) |
| R-AUTO-026 | Automation | 🟢 Mitigated | High | KIS EGW00201 REJECTED 자동 재시도 미지원 → 주문 이월 · 매수/매도 기회 상실 | Step 12 retry-normalizer EGW00201 확장 · 자동 재시도 4건 통과. [See details: R-AUTO-026 Details](#r-auto-026-details) |
| R-AUTO-027 | Automation | 🔵 Accepted | Medium | APPROVAL_REQUIRED Slack 이 0/0 표시 → 운영자 오해 · 잘못된 다음 단계 결정 위험 | Slack payload builder 개선 후 Mitigated 승격 후보. [See details: R-AUTO-027 Details](#r-auto-027-details) |
| R-DATA-017 | Data | 🔴 Open | High | KRX worker exit 0 만으로 Step2B SUCCESS 판정 시 raw N영업일 lag stale 전파 위험 | Step2B 성공 기준 강화 · Slack KRX 기준일 표시 · feature lag 정책 명문화. [See details: R-DATA-017 Details](#r-data-017-details) |
| R-AUTO-033 | Automation | 🟢 Mitigated | High | port-view Local Spring Boot 실행성 호출 default ENABLE 회귀 · POST 우회 위험 | feature flag 4종 default false · view_app read-only · Fargate 진입 후속 audit. [See details: R-AUTO-033 Details](#r-auto-033-details) |
| R-SEC-010 | Security | 🔴 Open | High | app role DB password 평문 노출 이력 후 rotate 없이 운영 시 무단 접근 위험 | 노출 role rotate + Secrets Manager 갱신 + secret loader / env 재검증. [See details: R-SEC-010 Details](#r-sec-010-details) |
| R-AUTO-034 | Automation | 🔴 Open | High | Fargate Task Role states:StartExecution 광역 부여 · gate default ENABLE 회귀 위험 | Task Role 을 approval + safe 두 ARN 한정 재부여 · gate default false. [See details: R-AUTO-034 Details](#r-auto-034-details) |
| R-AUTO-035 | Automation | 🟢 Mitigated | Medium | Daily Brief Slack 자동 발송 실패 · 중복 발송으로 운영자 인지 누락 · 오해 위험 | mini SF + Scheduler 2개 + Builder / Notifier / IAM 분리 정합. [See details: R-AUTO-035 Details](#r-auto-035-details) |
| R-AUTO-036 | Automation | 🟢 Mitigated | Medium | Intraday hard stop 후 Slack 실패 시 READY rollback 없이 warning 만 출력 위험 | state machine approval gate 통과 후에만 broker 제출 · Notifier IAM 한정. [See details: R-AUTO-036 Details](#r-auto-036-details) |
| R-AUTO-037 | Automation | 🟢 Mitigated | High | Step 12~17 자동 실행이 Step 1~11 실패 · 데이터 미준비 상태에서 실행될 위험 | Dispatcher 휴장일 fail-closed + SF Choice paperOrderEnabled + retry-normalizer. [See details: R-AUTO-037 Details](#r-auto-037-details) |

## Risk Details

본 섹션은 위 `Risk Table` 의 장문 mitigation / detection / rollback 이력이 표 안에서 가독성을 해쳐 별도 분리한 상세 근거 모음이다. Risk ID / status / 날짜 / 비용 수치 / spec 번호 / evidence 링크는 원문 그대로 보존한다. 상세 이력의 raw 실행 로그 / 응답 본문 / secret value / broker_order_no 원문 / 실제 ARN / public IP / image digest 는 계속 인용 금지(`[REDACTED*]` placeholder 또는 spec `operation-notes.md` 링크로 대체).

### R-AUTO-001 Details

- Current status (R-AUTO-001): `Open` — mitigation 은 04 spec 후속 phase 의 Step Functions state machine 정의 / RunTask 호출부 review checklist 로 확정. 본 일자 RunTask 단건 실행은 수동 운영자 명령이며 자동 재시도 진입 0건.
- R-AUTO-001 · Impact High · Probability Medium · Affected Spec 04, 10.
- Mitigation history:
  - [2026-06-13 보강]
    - Strategy Execution ECS / Fargate 1차 포팅 검증(OD-MS-017) 으로 command override 대상 step 7종 확정(`daily_buy_execution_run` / `daily_sell_execution_run` / `daily_auto_buy_execute_run`
    - `daily_auto_sell_execute_run` / `execution_sync_buy_fill` / `execution_sync_sell_fill` / `execution_sync_buy_position`).
    - Review checklist 에 (a) state 별 Retry 정책 비활성 확인 (b) command override script 명 allowlist 확인 (c) BUY / SELL / fill sync / position 변경 계열 Retry block 부재 확인 추가.
  - [2026-07-01 보강] paper Daily 자동화 1차 풀 ON 도달 (`portfolio-paper-daily-step12-17-order-0901-kst` DISABLED → ENABLED).
    - BUY / SELL / fill sync / position 변경 step 자동 재시도 금지 정책 유지 재확인.
    - Step 12~17 approval state machine 안 Step 12 / 14 / 15 / 16 계열 state Retry block 부재 audit 는 04 spec 후속 phase.
    - 본 일자 Step 12~17 수동 실행 회차(executionName `port-manual-daily-step12-17-20260701-043747` / SUCCEEDED / NO_TARGET 안전 종료) 는 broker 주문 제출 0건 / `connector_order_request` 신규 0건 / 자동 재시도 진입 0건.
- Detection: `connector_order_request` 단기간 중복 insert 감지 + broker 측 동일 종목 · 동일 가격 중복 주문 알림. [2026-06-13 보강] Step Functions execution history 의 retry attempt 기록 / state name ↔ command override 매핑 audit / `connector_order_request` 의 `idempotency_key` 또는 `client_order_id` 중복 row 점검.
- Rollback: 해당 Step Functions execution 즉시 stop → broker 측 취소 또는 수동 정리 → retry 비활성화로 state machine 재배포.
- Evidence links: [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md), [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md).

### R-DOCS-001 Details

- Current status (R-DOCS-001): `Open` — 모든 spec / runbook / decision 문서에 `[REDACTED]` 정책 유지. 각 실 회차마다 secret 평문 기록 0건 재확인.
- R-DOCS-001 · Impact High · Probability Medium · Affected Spec 모든 spec.
- Mitigation history:
  - [2026-06-10 보강] 08 spec 1차 적용 결과 반영 — secret value 가 작업 채팅 / 명령 출력 / 콘솔 캡처 / CloudWatch Logs / 운영자 노트에 평문 노출되지 않았는지 작업 종료 시점 점검을 detection 으로 승격.
  - [2026-06-17 보강] 03 spec MarketConnector 조회성 dry-run 재검증 결과 반영 — JSON SecretString 은 `--query SecretString --output text` 로 조회 후 JSON parse → 내부 key value 만 export.
    - JSON dict 전체를 환경변수로 export 금지.
    - KIS API response body / `connector_api_call_log` body 평문 인용 금지 — metadata(response_status / response_code / is_success / category / api_name) 만 기록.
  - [2026-06-17 보강 · 17-step E2E] KIS paper BUY 4건 broker 호출에도 secret value / KIS app key / KIS app secret / 계좌번호 / token / RDS password / RDS endpoint hostname / account-id / 실제 ARN / IAM access key id / EIP / image digest 평문 기록 0건.
    - KIS paper 4건의 broker_order_no(`0000035906` / `0000035912` / `0000035918` / `0000035932`) 는 broker 응답값 / 실계좌 아님.
  - [2026-06-17 wrapper 보강] wrapper(`.kiro/scripts/run-daily-aws-paper.ps1`) 의 summary / overrides JSON / SSM stdout · stderr 파일에 secret 평문 출력 0건 정책. `/tmp/inject-env.sh` v5 env injection 만 사용. wrapper run id 별 `C:\Temp\portfolio-daily-aws-paper\<run-id>\` 하위 파일도 동일 정책.
    - 운영자가 종료 시점에 grep 으로 secret 패턴 잔존 여부 사후 검증.
- Detection: git history secret scan(예: `truffleHog`, `gitleaks`) + 운영자 점검.
  - [2026-06-17 보강] 03 spec runbook §4.1 / §4.2 검증 SQL — `connector_api_call_log` BALANCE / ORDER metadata 만 점검(body 평문 0건 확인) / `connector_balance_snapshot` 최신 row metadata 만 점검 / `connector_order_request` · `connector_order_event` · `connector_fill` row count 인벤토리만 비교.
  - JSON SecretString export 시점에 환경변수 length / key presence 만 확인. wrapper run id 별 파일에 secret 패턴 grep 점검(`(eyJ|AKIA|ASIA|password|secret|token|app_key|app_secret|account_no|webhook)` 등 키워드 매치 시 wrapper 사용 중단 + rotate).
- Rollback: 즉시 secret rotate(KIS app secret / DB master password / Slack webhook 등) + git history 정리 검토 + 노출 token broker 측 만료 처리.
- Evidence links: [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md), [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md).

### R-DATA-005 Details

- Current status (R-DATA-005): `Mitigated` — [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) §5 검증 SQL 정기 점검 유지. 개별 GRANT 보정 사례는 spec `operation-notes.md` 에 누적.
- R-DATA-005 · Impact Medium · Probability Medium · Affected Spec 02, 04, 05, 06, 08.
- Mitigation history:
  - [2026-06-10 보강] 08 spec — `public` schema 잔존 sequence 2건의 `preprocessor_app` USAGE / SELECT 부족 → 운영자 직접 GRANT 로 해소.
  - [2026-06-13 보강] 04 spec — `decision_app` 의 research / execution / decision schema · table · sequence 권한 부족으로 buy-signal / position-signal 2건 1차 실패. 운영자 직접 GRANT 로 보정. 정식 매트릭스 갱신은 02 spec db-roles-and-grants 후속.
  - [2026-06-17 보강] 17-step E2E — (a) `execution_app` interest schema 누락 → Step 8 1차 실패 후 보정 (b) `marketconnector_app` legacy schema · database search_path 누락 → Step 17 1차 실패 후 보정 (c) `execution` table UPDATE 누락(OD-DB-008 R-only) → Step 12 1차 실패 후 보정. 정식 매트릭스 갱신은 02 spec db-roles-and-grants 후속.
- Detection:
  - application 로그의 `permission denied for ...` 패턴 + `pg_stat_activity` 권한 오류 빈도 + CloudWatch Logs 의 `permission denied for sequence` / `permission denied for relation`
  - `relation "..." does not exist` 패턴. 17-step 진입 직전 app role 별 `current_setting('search_path')` / `has_schema_privilege` / `has_table_privilege` / `has_sequence_privilege` 결과 비교.
- Rollback: 권한 누락 발견 시 운영자 직접 GRANT(최소 권한 한정) + ECS RunTask 재실행 + 보정 결과를 해당 spec `operation-notes.md` 에 누적.
- Evidence links: [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md), [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md).

### R-DOCS-002 Details

- Current status (R-DOCS-002): `Mitigated` — wrapper 안 password `Write-Host` 미출력 정책 표준화 유지. 2026-06-29 노출 이력은 R-SEC-010 신규로 별도 분리.
- R-DOCS-002 · Impact High · Probability Low · Affected Spec 06, 08.
- Mitigation history:
  - [2026-06-29 보강] 운영자 대화 중 DB password 가 평문으로 노출된 이력 식별.
    - 영향받은 app role(`view_app` · `marketconnector_app` · `crawler_app` · `preprocessor_app` · `research_app` · `decision_app` · `execution_app` 중 노출된 role) 의 password rotate 후속 필요.
    - 본 위험 mitigation 자체의 `[REDACTED]` 정책은 그대로 유지.
    - 실 노출 사실은 R-SEC-010 신규.
- Detection: wrapper 로그 파일 password 패턴 grep + RDP 진입 시 `Get-ChildItem env:` 출력 캡처 시 민감 변수 노출 여부 + SSM Session Manager 로그 비밀 값 패턴.
- Rollback: 노출 secret 즉시 rotate + 로그 파일 정리 + 노출 경위 짧게 기록(값 평문 미기록).
- Evidence links: [`../06-secrets-and-iam/operation-notes.md`](../06-secrets-and-iam/operation-notes.md), [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md).

### R-AUTO-007 Details

- Current status (R-AUTO-007): `Mitigated` (2026-06-21 검증 통과) — wrapper `step-02-interest-crawler.ps1` 가 KRX raw DB validation SSM step `INTEREST_CRAWLER_KRX_DB_VALIDATE` 자동 호출.
- R-AUTO-007 · Impact Medium · Probability Medium · Affected Spec 08.
- Mitigation history:
  - [2026-06-13 보강] SSM RunCommand → `schtasks /Run` 트리거 자동화 1차 검증 후 wrapper 성공 메시지만 신뢰하는 위험 재확인. wrapper 내 DB 검증 자동 출력 우선순위 task 57 로 유지.
  - [2026-06-21 보강] task 57 완료(OD-MS-026 / R-AUTO-020 정합). `interest_krx_raw_validate_daily.py` 가 `interest_program_raw` · `interest_shortsell_raw` 의 `ExpectedKrxRawDate` 기준 row_count + `max(trade_date)` 검증 → 부족 시 exit code 30 으로 Step 2 fail.
  - [2026-06-26 보강] worker 정상 종료 + validation 통과에도 KRX feature 최신성이 별도 신호일 수 있음 재확인. 본 위험 Status `Mitigated` 유지 / feature lag 사실은 R-DATA-017 신규로 별도 분리.
- Detection: wrapper Step 2 result 의 `KrxDbValidationCommandId` + SSM step `INTEREST_CRAWLER_KRX_DB_VALIDATE` 응답 `Success` / `ResponseCode 0` + validation exit code 0 + expected date row_count 자동 점검.
- Rollback: validation 실패 시 wrapper Step 2 자동 fail → Step 3 이후 진행 즉시 차단. KRX worker 단독 재실행 + DB 점검 + Step 2 단독 재실행.
- Evidence links: [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md).

### R-AUTO-005 Details

- Current status (R-AUTO-005): `Mitigated` — ECS crawler smoke RunTask 통과 + KRX GUI 는 Windows EC2 worker 로 분리.
- R-AUTO-005 · Impact Medium · Probability Medium · Affected Spec 08, 02.
- Mitigation history:
  - [2026-06-12 보강] KRX program / KRX shortsell 을 Windows EC2 worker 로 확정(OD-MS-011 / OD-MS-012). 2026-06-09 / 2026-06-10 / 2026-06-11 적재 확인.
  - [2026-06-13 보강] ECS crawler `portfolio-paper-interest-crawler:6` Selenium Chrome smoke RunTask 통과(exitCode 0 / `example.com` / Naver Finance 도달). revision 6 command 는 smoke 전용 / 운영용 Task Definition 분리는 후속(task 58).
- Detection:
  - 후속 phase 에서 crawler RunTask 실패 패턴 / KRX 로그인 차단 응답 / Naver · yfinance rate limit / Selenium WebDriver 예외 / NAT-free public subnet outbound 도달 실패 모니터.
  - EC2 worker 일자별 row count(`interest_program_raw` / `interest_shortsell_raw`) 비교 + wrapper 로그 `[Collected Date] None` 패턴 + `KRX Login Ready` 누락 + junction 결손 패턴 + smoke RunTask CloudWatch Logs 의 성공 마커 점검.
- Rollback: crawler 운영 모드를 OPT-3(ECS on EC2 또는 EC2 직접) 로 승격 또는 도메인 부분 적용. KRX GUI 경로는 EC2 worker wrapper 재실행. non-GUI 는 EC2 worker 임시 유지.
- Evidence links: [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md).

### R-AUTO-033 Details

- Current status (R-AUTO-033): `Mitigated` (View Local Batch Step 1~17 검증 통과 후 유지) — Fargate 진입 시점의 default ENABLE 회귀 audit 은 05 spec 후속 phase 책임.
- R-AUTO-033 · Impact High · Probability Low · Affected Spec 04, 05, 10.
- Mitigation core: Spring Boot feature flag 4종 default false + 서버측 POST 차단 + view_app read-only + Local 5종 화면 · Batch Step 1 · 1~11 · 12~17 · AWS SF · ECS View SF approval 실증 통과(OD-MS-037 / R-AUTO-034 별도).
- Mitigation history:
  - [2026-06-27 1차 실증] View Local Spring Boot(aws-paper profile) read-only 화면 검증 5종 통과. KIS · Connector · Slack · Batch · 주문 API 호출 0건.
  - [2026-06-28 보강] View Local Batch Step 1 단독(run `#46`) + Step 1~11(run `#47`) 통과. Step 별 DB role override 분리 확인. Step 8~11 NO_TARGET / broker 영향 0건.
  - [2026-06-29 보강] View Local Batch Step 12~17(run `#48`) 통과. preflight 4종(REQUESTED / retryable rejected / active / connector_strategy_order_execute.py SHA256 = EC2 정식 배포본) + View gate 6종 정합.
    - Step 12 NO_TARGET / Step 17 balance refresh 통과(`connector_balance_snapshot id=239` / `as_of_date=2026-06-29` / 보유 0건).
  - [2026-06-29 보강 (2)] port-view aws-stepfunctions Daily Batch trigger 분리(commit `e72de6f`). Local Step 1~11 StartExecution 통과. 서비스 레벨 안전 gate 1차 실증. Fargate Task Role 권한 · gate 회귀 위험은 R-AUTO-034 신규로 별도 분리.
  - [2026-06-29 보강 (3)] Local View 가 Step 12~17 approval workflow external caller 로 붙는 두 번째 phase 검증 통과(executionName `port-view-step12-17-step12-17-20260629-194314-ba5edaf8` / SUCCEEDED). payload 타입 보완(boolean / numeric) 1차 실증.
  - [2026-06-30 보강] Daily Batch gate 운영 의도 정합 수정 + Local View → AWS Step Functions Step 12~17 승인 실행 2차 실증 통과(executionName `port-view-daily-step12-17-20260630-095111-aae2595c` / SUCCEEDED).
  - [2026-06-30 오후 보강] port-view ECS Fargate Public IP 1차 포팅 통과. ECS View → Step Functions Step 12~17 승인 실행 3차 실증(executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` / SUCCEEDED / Slack `DAILY_EXECUTION_SUCCESS` / NO_TARGET). 검증 후 desiredCount 0 종료.
- Detection:
  - Spring Boot log 의 KIS / Connector / Slack / Daily Batch / Strategy Execution submit 호출 패턴 정기 점검. `connector.connector_order_request` 의 신규 row 가 View Local 가동 시간대에 발생하는 패턴 audit.
  - Spring Boot `application-aws-paper.properties` 의 `portfolio.*.enabled` 값이 true 로 변경된 시점 audit.
  - 외부에서 POST 시도 패턴(Local 가정에서는 보통 없음).
- Rollback: 의도하지 않은 실행성 호출 발견 시 Spring Boot shutdown + 영향 row 직접 SQL 점검 + broker 측 취소. feature flag 잘못 ENABLE 발견 시 즉시 false 로 되돌리고 재기동. ECS / Fargate 진입 시점에 재검토 + default ENABLE 회귀 cross-spec audit(05 spec 후속).
- Evidence links: [`../05-port-view-ecs-and-runbook/operation-notes.md`](../05-port-view-ecs-and-runbook/operation-notes.md).

### R-AUTO-034 Details

- Current status (R-AUTO-034): `Open` — Local View 측 분리는 통과했으나 Fargate Task Role 권한 분리 + Fargate 외부 노출 시점 default ENABLE 회귀 audit 통과 전까지 승격 보류(06 spec 후속 phase 책임).
- R-AUTO-034 · Impact High · Probability Low · Affected Spec 04, 05, 06, 10.
- Mitigation history:
  - Core mitigations: (a) Task Role `states:StartExecution` 최소 권한(state machine ARN 한정 / wildcard 0건). (b) Fargate 안전 기본값(`paper-order-enabled=false` / `full-pipeline-execution-enabled=false` / `max-executable-step-order=11` / `local-file-execution-enabled=false`).
    - (c) 서비스 레벨 안전 gate(`StepFunctionsDailyBatchExecutionService`). (d) executionArn / account-id redaction. (e) Step 12~17 승인형 · preflight · paper-order gate 분리. (f) Local 1차 실증(commit `e72de6f`). (g) 코드 / IAM Policy / ASL / 응답 본문 평문 인용 0건.
  - [2026-06-29 보강]
    - Step 12~17 approval state machine ARN 분리(일반 ARN + approval ARN 2종). `DailyBatchProperties.awsStepfunctionsApprovalStateMachineArn` 필드
    - `PORTFOLIO_BATCH_AWS_STEPFUNCTIONS_APPROVAL_STATE_MACHINE_ARN` 환경변수 / `startSafeRange` vs `startApprovalRange` 분리 / approval ARN 빈 값 서비스 레벨 차단
    - Controller `POST /daily-batch/aws-stepfunctions/start-approval-range` 추가.
    - Fargate Task Role 은 두 ARN 모두 한정 부여(06 spec 후속).
    - 본 일자 IAM Policy 변경 0건.
  - [2026-06-30 보강] Local View 4가지 운영 경로 분리 1차 실증. Local File 실행 gate 와 AWS Step Functions 실행 gate 분리. `fullPipelineExecutionEnabled=true` + `paperOrderEnabled=true` 상태에서 AWS Step 1~11 safe / Step 12~17 승인 버튼 모두 활성 + Local File 버튼과 충돌 회피. DB 검증 쿼리 작성 원칙 보강(`information_schema.columns` 사전 확인).
  -  [2026-06-30 오후 보강] port-view ECS Fargate Public IP 1차 포팅 실증. task role `portfolio-paper-view-task-role` 의 `states:StartExecution` Resource 는 Step 12~17 approval state machine ARN 한정. ECS Task Role / Task Execution Role(`portfolio-paper-ecs-task-execution-role`) 분리.
    - ECS View → Step 12~17 승인 실행 1차 실증(executionName `port-view-ecs-daily-step12-17-20260630-051537-9550f0e8` / SUCCEEDED / Slack `DAILY_EXECUTION_SUCCESS`). task definition `portfolio-view:1` → `:2` revision 업데이트는 기본 계좌번호 env 누락 보정 한정(gate 회귀 0건).
    - Public IP + 운영자 IP/32 SG inbound + desiredCount 0/1 수동 운영. 실제 ARN / account-id / IAM access key id 평문 기록 0건 정합.
- Detection:
  - Fargate Task Role `states:StartExecution` 권한 audit(CloudTrail `AttachRolePolicy` / `PutRolePolicy` event / Resource 패턴이 특정 state machine ARN 한정인지).
  - Step Functions execution history 의 source IP · IAM principal · runDate · payload audit. `connector_order_request` 의 신규 row 가 승인 시점이 아닌 패턴.
  - Fargate `application-aws-paper.properties` gate 값이 default 보다 완화된 상태로 deploy 되는지 cross-spec audit.
  - ASL 의 `runDate.$=$.runDate` 참조 state 가 input 없이 진입 시 `States.Runtime` 패턴.
- Rollback: 잘못 분기된 execution 발견 시 즉시 `StopExecution` + 영향 row 직접 SQL 점검 + broker 측 취소. Task Role 권한이 광역 부여된 상태면 즉시 revoke + Resource 한정 재부여 + Task 재기동. gate default ENABLE 회귀 식별 시 즉시 false 로 되돌리고 Task 재배포.
- Evidence links: [`../05-port-view-ecs-and-runbook/operation-notes.md`](../05-port-view-ecs-and-runbook/operation-notes.md), [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md).

### R-AUTO-035 Details

- Current status (R-AUTO-035): `Mitigated` — 별도 mini Step Functions + Scheduler 2개 ENABLED + Builder / Notifier / IAM Role 분리 정합.
- R-AUTO-035 · Impact Medium · Probability Medium · Affected Spec 04, 05, 10.
- Mitigation summary:
  -  (a) mini state machine `portfolio-daily-brief-slack-notification` 분리(OD-MS-038 정합). (b) Scheduler 2개 ENABLED — `portfolio-daily-brief-morning-slack-0750-kst` + `portfolio-daily-brief-evening-slack-1550-kst`.
    - cron / Asia/Seoul / Flexible OFF / MON-FRI / eventType `MORNING_BRIEF` · `EVENING_BRIEF` / 고정 `runDate` 미주입 → Builder 실행 시점 KST 기준. (c) 수동 smoke 2건 SUCCEEDED / Slack 수신 확인.
    - (d) Builder Lambda `portfolio-daily-brief-slack-summary-builder`(Python 3.12 + `pg8000` + Secrets Manager `valueFrom` / `psycopg2` 미사용) + Notifier `portfolio-event-notifier` 분리.
      - (e) IAM Role 2종(`portfolio-daily-brief-sfn-role` + `portfolio-daily-brief-scheduler-role`) — Daily 본 실행 IAM Role 과 분리.
    - (f) DB password Secrets Manager `valueFrom`. (g) Slack webhook 환경변수명(`SLACK_WEBHOOK_URL`) 까지만 기록. 운영 안정화 후 Secrets Manager / SSM SecureString 이전(R-AUTO-024 `Accepted`).
- Detection:
  - mini Step Functions execution history 의 `SUCCEEDED` / `FAILED` audit + Scheduler `LastInvocationDate` audit
  - Notifier Lambda CloudWatch Logs 의 `SlackPostSuccess` / `SlackPostFailure` audit + Slack 채널 수신 로그 audit
  - Builder output vs 최신 `connector_balance_snapshot` · `connector_position_snapshot` 정합 audit.
- Rollback: 알림 실패 인식 시 Scheduler 개별 DISABLE + 원인 Lambda / mini Step Functions 로그 점검 + 재발송은 운영자 수동 invoke(중복 발송 방지 위해 SF `Retry` 미적용). Builder 가 잘못된 row 조회 시 즉시 SQL 점검 + operation-notes 기록.
- Evidence links: [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md), [`../06-secrets-and-iam/operation-notes.md`](../06-secrets-and-iam/operation-notes.md).

### R-AUTO-036 Details

- Current status (R-AUTO-036): `Mitigated` — Intraday Stop Sell 은 `portfolio-paper-intraday-stop-sell-approval` state machine 안에서 승인 gate 통과 후에만 broker 제출. runner → Notifier Lambda invoke 는 IAM inline policy 로 Notifier 함수 한정.
- R-AUTO-036 · Impact Medium · Probability Low · Affected Spec 03, 04, 06, 10.
- Mitigation summary:
  - MarketConnector EC2 runner 는 `INTRADAY_STOP_SELL` READY 생성 + Notifier invoke 까지만 책임. state machine `CheckIntradayStopApproval` Choice 통과 후 broker 제출.
  - IAM `portfolio-paper-marketconnector-event-notifier-invoke` inline policy 는 Notifier Lambda 함수만 `lambda:InvokeFunction`(Resource · Action wildcard 0건). broker 주문 제출 자동 ENABLE 은 후속 phase / `allowIntradayStopOrderExecute=true` 승인 후에만.
- Evidence links: [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md), [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md).

### R-AUTO-037 Details

- Current status (R-AUTO-037): `Mitigated` (aws-paper 한정) — 09:01 Scheduler ENABLED 후 후속 실 회차 관찰 계속. aws-live 정책 변경 없음(OD-SAFE-002 / OD-SAFE-003 유지).
- R-AUTO-037 · Impact High · Probability Low · Affected Spec 04, 05, 10.
- Mitigation summary:
  - (a) Dispatcher Lambda `portfolio-paper-daily-scheduler-dispatcher` — KST `runDate` 생성 + 주말 / 휴장일 skip + Step Functions `StartExecution`. Step 1~11 성공 audit 은 04 spec 후속 phase.
  - (b) Step Functions Choice `Step12_CheckApproval` — `paperOrderEnabled=true` + `allowPaperOrderExecute=true` + step range gate 통과 시에만 Step 12 진입. 조건 미충족 시 `NoOpApprovalWaiting` / `ExecutionSkipped` 로 안전 종료.
  - (c) Slack `APPROVAL_REQUIRED` 안전 gate — 08:00 approval workflow 후 운영자 사전 인지(R-AUTO-023 / R-AUTO-025 결합).
  - (d) Step 12 retry-normalizer 자동 실행(OD-MS-028 / R-AUTO-022 결합) — 자동 재시도 금지 정책(OD-SAFE-004 / R-AUTO-001) 유지.
  - (e) DB after-check — 09:01 자동 실행 후 `strategy_execution_order` · `connector_order_request` · `connector_fill` · `strategy_position_state` · `connector_balance_snapshot` audit.
  - (f) 1차 통과 evidence — 본 일자 수동 실행 회차 executionName `port-manual-daily-step12-17-20260701-043747` / SUCCEEDED / start `2026-07-01T13:37:47.856+09:00`
    - stop `2026-07-01T13:40:41.212+09:00` / Slack 3종 수신 / NO_TARGET / DB after-check 통과(최신 `connector_balance_snapshot id=321` / `as_of_date=2026-07-01` / `total_eval_amount=8,706,505`
    - `cash_balance=8,706,505` / `eval_profit=0` / `strategy_execution_order` 0건 / active `connector_order_request` 0건).
    - 후보 있는 날 실 회차 검증은 후속.
  - (g) aws-live 정책 변경 없음(OD-SAFE-002 / OD-SAFE-003).
- Detection: (a) EventBridge Scheduler `get-schedule` 응답의 `State=ENABLED` · cron · Asia/Seoul · FlexibleTimeWindow OFF · Target Lambda · Target Input 정합. (b) Dispatcher Lambda CloudWatch Logs 의 09:01 invocation stack trace / `scheduleType=STEP12_17_ORDER` / KST `runDate` / 주말 · 휴장일 skip 라벨.
  - (c) Step Functions execution history 의 Choice 결과 · Step 12~17 각 결과 · `ExecutionSucceeded` / `ExecutionFailed`. (d) Slack `APPROVAL_REQUIRED`(08:00 이후) + `DAILY_EXECUTION_SUCCESS` / `DAILY_EXECUTION_FAILED`(09:01 이후) timing.
  - (e) DB after-check — `strategy_execution_order` · `connector_order_request` · `connector_fill` · `strategy_position_state` · 최신 `connector_balance_snapshot` row / stale `connector_position_snapshot`(2026-06-23 4건 잔여) 분리. (f) market status audit — 정상 개장일 / 반나절 / 임시 휴장 / 시스템 점검일.
- Rollback: (a) 잘못된 상태에서 진행 시 즉시 `aws scheduler update-schedule --state DISABLED` + Step 12~17 수동 실행으로 복귀. (b) 진행 중 execution 은 `stop-execution` + 영향 row 직접 SQL 점검 + broker 측 취소(R-AUTO-001 / R-AUTO-002 / R-AUTO-019 / R-BROKER-004 결합).
  - (c) 07:50 EC2 lifecycle 실패 시 08:00 Step 1~11 실패 → 09:01 fail-closed(R-AUTO-028 결합). (d) 잘못된 broker 주문 발견 시 KIS 취소 + SQL 정리 + ASL 안전 gate 강화 + 운영자 승인 후 ENABLED 복귀. (e) Status 승격 조건 — 1주일 운영 회차 누적에서 잘못된 broker 주문 · stale 신호 실행 · market status 미확인 실행 0건 audit 통과. (f) aws-live 재검토는 10 spec 후속.
- Evidence links: [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md).

### R-SEC-010 Details

- Current status (R-SEC-010): `Open` — 영향받은 role password rotate + Secrets Manager 갱신 + secret loader / env 재검증 완료 시점에 `Mitigated` 승격 후보.
- R-SEC-010 · Impact High · Probability Medium · Affected Spec 02, 03, 04, 05, 06, 08, 09, 10.
- Mitigation summary: (a) 노출된 role 인벤토리 작성 후 RDS 측 password rotate(06 spec 후속 phase 책임 / 본 spec 산출물에 신규 password 평문 기록 0건). (b) Secrets Manager · SSM SecureString 값 갱신.
  - (c) 운영자 로컬 secret loader / 환경변수 주입 경로(`Load-PortfolioViewAwsPaperBatchEnv.ps1` / `.kiro/scripts/run-daily-aws-paper.ps1` / View AWS Paper Batch starter 등) 재검증. (d) rotate 완료 전까지 신중 운영 — `pg_stat_activity` / `pg_stat_ssl` / CloudTrail RDS API event / `connector_api_call_log` audit.
  - (e) 본 spec 산출물에 신규 / 기존 password 평문 기록 0건 유지(R-DOCS-001 / R-DOCS-002 정합).
- Detection:
  - 영향받은 app role 의 `pg_stat_activity` / `pg_stat_ssl` row 가 운영자 의도와 다른 source(외부 IP / 비정상 client / 비정상 query 패턴) 에서 발생하는 패턴 + RDS API event(CloudTrail) 의 `ModifyDBInstance` / `DescribeDBInstances` 비정상 호출 + `connector_api_call_log` 비정상 호출 + Secrets Manager `GetSecretValue` 호출 타이밍이 운영자 시점이 아닌 패턴.
  - 운영자 노트 / launcher / 채팅 / 콘솔 출력에서 rotate 후 신규 password 재노출 grep 점검(`(password|PGPASSWORD|PORT_DB_PASSWORD)` 등).
- Rollback: 즉시 rotate + Secrets Manager 갱신 + secret loader 재검증 + Spring Boot Datasource 회귀 검증 + Daily Pipeline run 재실행 + 외부 무단 접근 흔적 점검. 노출 자격이 외부에 이미 도달했다고 판단되면 role 의 DB 권한 추가 점검 + 영향 row 운영자 직접 정리. rotate 완료 + 외부 무단 접근 흔적 0건 audit 통과 시 `Mitigated` 승격.
- Evidence links: [`../06-secrets-and-iam/operation-notes.md`](../06-secrets-and-iam/operation-notes.md).

### R-AUTO-025 Details

- Current status (R-AUTO-025): `Mitigated` — 2026-07-01 09:01 Scheduler ENABLED 진입 통과. 다음 영업일 실전 관찰 후속 유지.
- R-AUTO-025 · Impact High · Probability Medium · Affected Spec 04, 05, 10.
- Mitigation core:
  - Scheduler IAM Role `portfolio-paper-eventbridge-scheduler-role`(trust `scheduler.amazonaws.com`) + `simulate-principal-policy` allowed 확인. Resource · Action wildcard 0건(OD-SEC-005 · OD-SEC-006 정합).
  - Dispatcher Lambda `portfolio-paper-daily-scheduler-dispatcher` IAM Role · `states:StartExecution` 허용.
  - Lambda `dryRun=true` 검증(08:00 · 09:01) — `StartExecution` 0건 / 주문 제출 0건 / `reason=DRY_RUN_NO_START_EXECUTION`.
  - Scheduler `get-schedule` state · cron · `Asia/Seoul` · Flexible OFF · Target 정합.
  - 단계적 활성화(초기 08:00 ENABLED / 09:01 DISABLED — OD-MS-032 정합).
  - Dispatcher 환경변수 fail-closed `TIMEZONE=Asia/Seoul` · `HOLIDAY_COUNTRY=KR` · `FAIL_CLOSED_ON_HOLIDAY_ERROR=true`.
  - Step Functions Catch → `DAILY_EXECUTION_FAILED` Slack(R-AUTO-023 정합).
- Mitigation history:
  - [2026-06-23 1차 dry-run 통과] `simulate-principal-policy` + Lambda dryRun 08:00·09:01 + Scheduler `get-schedule` 정합 모두 통과. 실 `StartExecution` (`dryRun=false`) 은 다음 영업일 08:00 첫 검증 예정.
  - [2026-06-24 08:00 첫 실 실행 통과]
    - Scheduler invocation 1건 → Dispatcher Lambda → SF `portfolio-paper-daily-step1-17-approval` execution 생성 → Step 1~11 후 approval-required → `APPROVAL_REQUIRED` Slack 수신
    - Step 12 이후 주문 제출 경로 차단 / 2026-06-24 기준 `connector_order_request` · `strategy_execution_order` 신규 주문 제출 0건 / 09:01 DISABLED 유지.
  - [2026-06-24 두 번째 보강] EC2 lifecycle 자동화(07:50 start · 15:50 stop Scheduler ENABLED + Step Functions `StopCrawlerEc2AfterStep11Success` task) 진입. 4계층 연결 사슬 검증 통과(OD-MS-034 정합). 관련 위험 R-AUTO-028 신규.
  - [2026-07-01 09: 01 자동 ENABLE 진입] 09:01 Scheduler `portfolio-paper-daily-step12-17-order-0901-kst` DISABLED → ENABLED 전환(LastModificationDate `2026-07-01T13:53:57.160+09:00` / cron `cron(1 9 ? * MON-FRI *)` / Asia/Seoul / Flexible OFF / Target Lambda · Input 정합).
    - Step 12~17 수동 실행 검증 통과(executionName `port-manual-daily-step12-17-20260701-043747` / SUCCEEDED / Slack 3종 수신 / NO_TARGET / DB after-check 통과 — `connector_balance_snapshot id=321` / `as_of_date=2026-07-01`). Daily 라인업 7종 ENABLED. aws-paper 한정 / aws-live 정책 변경 없음(OD-SAFE-002 · OD-SAFE-003).
- Detection:
  - Scheduler `get-schedule` State · cron · Timezone · Target Lambda 정합 / Dispatcher Lambda CloudWatch Logs invocation 누락 · `errorMessage`
  - `errorType` / SF execution 생성 여부(2개 state machine `list-executions`) / `APPROVAL_REQUIRED` Slack 매 평일 08:00 수신 / 휴장일 · 주말 fail-closed(`started=false`
  - `reason=HOLIDAY_SKIPPED`) / IAM 정책 변경 CloudTrail audit / EventBridge Scheduler invocation 통계(CloudWatch).
- Rollback: (Scheduler) `get-schedule` · IAM Role trust 점검 후 `update-schedule` 또는 재생성. (Dispatcher) CloudWatch Logs traceback 분석 후 환경변수 · Handler · IAM Role 보정 → `dryRun=true` 재검증. (Step Functions) state machine ACTIVE · execution history audit + R-AUTO-021 · R-AUTO-022 · R-AUTO-023 정합 rollback 결합.
  - 단계적 활성화 정책 위반(09:01 미승인 ENABLED) 시 즉시 `disable-schedule` + 영향 row(`connector_order_request` · `strategy_execution_order` · `connector_fill` · `strategy_position_state`) 운영자 직접 SQL 점검 + broker 취소는 R-AUTO-001 · R-AUTO-002 · R-AUTO-019 · R-BROKER-004 정합.
  - 다음 영업일 09:01 자동 실행 실전 관찰 + 후보 있는 날 자동 주문 · 체결 · balance refresh · Slack 확인은 후속(followups-overview 2026-07-01).
- Evidence links: [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md), [`../05-port-view-ecs-and-runbook/operation-notes.md`](../05-port-view-ecs-and-runbook/operation-notes.md).

### R-AUTO-028 Details

- Current status (R-AUTO-028): `Mitigated` — 2026-06-24 1차 검증 통과. 실제 영업일 07:50 start · Step 1~11 후 Crawler stop · 15:50 stop 실 실행은 다음 영업일 첫 검증 예정.
- R-AUTO-028 · Impact Medium · Probability Medium · Affected Spec 03, 04, 05, 08, 10.
- Mitigation core:
  - 07:50 start Scheduler `portfolio-paper-ec2-start-0750-kst` ENABLED — cron `cron(50 7 ? * MON-FRI *)` · Asia/Seoul · Flexible OFF
    - Target Lambda `portfolio-paper-ec2-lifecycle-dispatcher` · input `{"action":"start","target":"BOTH","holidayCheck":true,"reason":"PRE_DAILY_STEP1_11","dryRun":false}`.
  - 15:50 stop Scheduler `portfolio-paper-marketconnector-stop-1550-kst` ENABLED — cron `cron(50 15 ? * MON-FRI *)` · Asia/Seoul · Flexible OFF · Target Lambda 동일 · input `{"action":"stop","target":"MARKETCONNECTOR","holidayCheck":false,"reason":"POST_MARKET_CLOSE","dryRun":false}`.
  - Lambda dryRun 3종 검증 통과 — `start BOTH dryRun` + `stop CRAWLER dryRun` + `stop MARKETCONNECTOR dryRun` / EC2 lifecycle 호출 0건 / 응답 라벨 정합.
  - 주말 skip 검증 통과 — `runDate=2026-06-27`(Saturday) Lambda invoke → Holiday guard fail-closed → EC2 start 호출 차단.
  - start 요청에만 휴일 체크 · stop 요청은 휴일 체크 미적용(idempotent no-op).
  - Step Functions 경로 연결 — `portfolio-paper-daily-step1-17-approval` state machine 에 `StopCrawlerEc2AfterStep11Success` task 삽입.
    - Step 6~11 성공 다음 Crawler stop → Approval Slack.
    - Step 1~11 실패 시 Crawler EC2 디버깅용 유지 + `DAILY_EXECUTION_FAILED` Slack(`SendDailyExecutionFailedSlack` / `portfolio-event-notifier`).
    - ASL 백업 후 update / RevisionId · `ACTIVE` 확인 / execution role EC2 lifecycle Lambda invoke 권한 추가 / Resource · Action wildcard 0건.
  - Lambda 환경변수 fail-closed — `TIMEZONE=Asia/Seoul` · `HOLIDAY_COUNTRY=KR` · `FAIL_CLOSED_ON_HOLIDAY_ERROR=true`. Holiday API 백업 · fallback 정책은 후속 분리(followups-overview 2026-06-24).
  - Lambda 코드 · IAM Policy · ASL · Lambda 응답 · 실제 ARN · instance-id 본 spec 평문 기록 0건(R-DOCS-001 정합).
- Mitigation history:
  - [2026-06-24 1차 검증 통과] Lambda source · Python `py_compile` · IAM Role · EC2 start · stop 권한 · Scheduler invoke Role · Lambda invoke 권한 · Lambda create · dryRun 3종 · 주말 skip · Scheduler `get-schedule` 정합 · SF ASL 백업 · update · RevisionId · `ACTIVE` 확인 — 모두 통과. 실제 영업일 실 실행은 다음 영업일 예정(OD-MS-034 정합).
- Detection:
  - Scheduler `get-schedule` `State=ENABLED` · cron · Timezone · Target Lambda · input JSON 정합 / Lambda CloudWatch Logs invocation 누락 · `errorMessage`
  - `errorType` / EC2 instance state — 07:50 후 MarketConnector
  - Crawler EC2 `running` / 15:50 후 MarketConnector `stopped` / SF Step 1~11 성공 직후 Crawler `stopped` / SF execution history `StopCrawlerEc2AfterStep11Success` task input · output 정합 /
  - `SendApprovalRequiredSlack` 진입 전 Crawler stop 완료 / Slack `APPROVAL_REQUIRED` timing(Crawler stop 직후) / Holiday guard fail-closed audit(`reason=HOLIDAY_SKIPPED`).
- Rollback: (Scheduler) `get-schedule` · IAM Role trust 점검 → `update-schedule` 또는 재생성 / 임시 `disable-schedule` 후 수동 EC2 start · stop. (Lambda) CloudWatch Logs traceback 분석 → 환경변수 · Handler · IAM Role 보정 → dryRun · 실 실행 재검증.
  - (Step Functions) ASL 백업본 rollback + execution history audit + R-AUTO-021 · R-AUTO-022 · R-AUTO-023 · R-AUTO-025 정합. EC2 단계 — 운영자 수동 start · stop / SSM RunCommand 정합 점검(R-AUTO-021 정합) / 영향 시점 길어지면 `DAILY_EXECUTION_FAILED` Slack 발송.
  - 단계적 활성화 정책(07:50 · 15:50 ENABLED · 09:01 DISABLED 유지) 우회 변경은 본 spec 안전 제약 위반 → 즉시 reverse.
- Evidence links: [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md), [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md), [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md).

### R-AUTO-015 Details

- Current status (R-AUTO-015): `Mitigated` — Status `Mitigated` 유지. Step Functions 자동 SubmitJob 연동 / EventBridge Scheduler 정기 트리거 도입 전까지 운영자 수동 SubmitJob 의존.
- R-AUTO-015 · Impact Medium · Probability Medium · Affected Spec 09, 10.
- Mitigation core (OD-MS-018 / OD-MS-019):
  - Research AWS Batch 포팅 대상 = `BACKTEST_RESEARCH` + `BACKTEST_REPORT` 2종 한정.
  - `run_extended_analysis.py` 는 `BACKTEST_RESEARCH` 내부에서 이미 수행 · 별도 포팅 제외 · 운영자 수동 보조 도구 분류.
  - `block_watch_*` / `block_exception_buy_*` 4종은 heavy 분류 · AWS Batch SubmitJob 대상 제외.
  - Job Definition rev1 / rev3 모두 `attemptDurationSeconds = 600` · vCPU 1 · memory 2048 · `attempts = 1` (자동 재시도 0건 / OD-SAFE-004 정합).
  - smoke / full job name prefix 분리(`smoke-*` / `full-*` 또는 명시 job name).
  - full backtest 가 `research.strategy_backtest_run` · `strategy_backtest_daily` · `strategy_trade_log` · 분석 테이블 갱신 entrypoint 는 별도 운영자 승인 gate.
  - S3 report artifact prefix `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/` 한정 · public read 0건 · Resource 한정 PutObject 권한.
- Mitigation history:
  - [2026-06-15 보강] 골격 신규 생성 + smoke `smoke-strategy-research-import-20260615` · `smoke-strategy-research-db-20260615` + BACKTEST_RESEARCH
    - REPORT 단건 full + S3 업로드 보강(`strategy-research-backtest-report-s3-20260615`) 모두 `attemptDurationSeconds = 600` 내 정상 종료 · image pull / secret 주입 / log delivery 오류 0건.
  -  [2026-06-16 보강] BACKTEST_REPORT wrapper 경로 진화 — rev1(`python -m port_strategy_research.backtest_report_run` / local-only) → rev2(`python aws_batch_backtest_report_wrapper.py` / 경로 부재 실패) → 최종 rev3(`python -m port_strategy_research.aws_batch_backtest_report_wrapper` module 호출).
    - Image `paper-20260615-report-s3` · env `REPORT_OUTPUT_DIR=/tmp/portfolio-reports` · `REPORT_S3_BUCKET=portfolio-paper-migration-yukiever`
      - `REPORT_S3_PREFIX=strategy-research/reports` · Job Role S3 PutObject Resource = `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` · public read 0건
      - wildcard 0건.
    - Job Definition 등록 직후 단건 SubmitJob 검증 정책 신설.
- Detection:
  - Job duration · `STATUS=RUNNING` 장시간 지속(smoke 는 분 단위 / full 은 시간 단위 alarm 임계값 분리) / CloudWatch Logs banner `smoke` · `full` 표기 정합 / Cost Explorer · CloudWatch metric Batch
  - Fargate 비용 spike / `research.strategy_backtest_run` 이상 증가 / Compute Environment desired vCPU 급상승 / Job Queue depth.
  - [2026-06-15 보강] Job Definition `attemptDurationSeconds = 600`
    - `attempts = 1` 외 변경 CloudTrail `RegisterJobDefinition` audit / SubmitJob `containerOverrides[].command` allowlist audit(`BACKTEST_RESEARCH` + `BACKTEST_REPORT`) / heavy entrypoint(`run_extended_analysis`
    - `block_watch_*` · `block_exception_buy_*`) 흘러들어가는지 즉시 식별 / S3 prefix `strategy-research/reports/` 외 PutObject 패턴 점검(R-COST-003 결합).
- Rollback:
  - `aws batch terminate-job` · Job Queue 일시 disable · Compute Environment desired vCPU 0 조정 / 잘못 생성된 `research.*` row 운영자 SQL 정리(02 spec db-roles-and-grants §5 결합) / Cost Anomaly Detection alarm · 사후 검토 보고.
  - [2026-06-15 보강] heavy 분류 entrypoint 가 SubmitJob 으로 흘러들어간 사실 식별 시 Job Queue 즉시 disable + 수동 보조 도구 회귀 + OD-MS-019 위반은 09 spec operation-notes 누적.
- Evidence links: [`../09-strategy-research-batch/operation-notes.md`](../09-strategy-research-batch/operation-notes.md), [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md).

### R-AUTO-018 Details

- Current status (R-AUTO-018): `Mitigated` — summary fallback guard 패치 + 단건 direct-only 기본화(OD-MS-025) 적용 후 회귀 0건.
- R-AUTO-018 · Impact High · Probability Medium · Affected Spec 03, 04.
- Mitigation core:
  - `connector_order_check.py` summary fallback guard — active 주문 후보 = 1일 때만 fallback 허용 / 2건 이상 시 `connector_order_event` · `connector_fill` insert + `connector_order_request status` 변경 금지.
  - 다건 active 상황 = `broker_order_no` 별 단건 조회(KIS `inquire-daily-ccld` broker_order_no 입력).
  - OD-MS-025 (2026-06-18) 단건 순차 조회 기본화 — active 주문 목록 조회 + 종목코드 / 주문번호 단건 direct-only 순차 조회. legacy broad 는 `--broad` 명시 시에만 진입(진단 / legacy 한정). 명시 조회 = `--code {ticker} --order-no {order_no} --no-broad` 조합.
  - wrapper ps1(`.kiro/scripts/steps/step-13-connector-order-check.ps1`) = Step 13 orchestration(SSM 호출 · 환경 검증 · log 저장) 만 담당 / 체결조회 방식 제어는 `connector_order_check.py` 내부 책임.
- Mitigation history:
  - [2026-06-17 1차 실증] 17-step E2E §3 — 응답 `output1 empty` + `output2 summary` 로 마지막 주문 id 37 / 088350 잘못 매핑 발견 → row 삭제 + `connector_order_request` 상태 복구 + summary fallback guard 패치 후 4건 단건 조회로 FILLED 동기화 (`connector_order_request 34~37` FILLED / `connector_fill 26~29`).
  - [2026-06-18 보강] Step 13 broad 조회가 `output1 empty` + `output2 summary-only` 다시 반환 → active candidate=4건이라 guard 자동 skip / 잘못된 fill 매핑 0건 / 단건 조회로 4건 반영(`connector_order_request 42~45` FILLED). wrapper 안 단건 조회 자동화 보완은 후속.
  - [2026-06-18 추가 보강] OD-MS-025 단건 direct-only 기본화 신설(운영 안전성 강화 목적).
    - EC2 반영 = S3 경유(`portfolio-paper-migration-yukiever` / `deploy/marketconnector/connector_order_check.py` / 39159 bytes / EC2 backup `connector_order_check.py.bak-20260618-step13-per-order`).
    - 단독 검증 `-StartStep 13 -EndStep 13` 통과 — 단건 direct-only 1건(`004990`) 기준 event · fill 정상 생성.
- Detection: `connector_order_event` · `connector_fill` 신규 row 수 vs active 주문 수 SQL / 마지막 주문 row 만 갱신되는 패턴 식별 / SUBMITTED → FILLED 전환 시 broker_order_no 매핑 정합 / KIS `inquire-daily-ccld` `output1` · `output2` 길이 · `output1 empty` mocking 테스트(03 spec task 29 후속) / summary fallback 호출 횟수 audit.
  - [2026-06-18 보강] wrapper Step 13 stdout / `connector_api_call_log` 의 `output1=0 / output2=summary-only` + `summary fallback skipped (active_order_candidates=N>1)` 라벨 / 단건 `--code` · `--order-no` · `--no-broad` 호출 빈도 / 단건 fill row = active candidate 수 SQL 점검.
  - [2026-06-18 추가 보강] broad 호출 빈도 audit(진단 · legacy 한정 여부) / `--active-limit` 기본값 시 처리 건수 vs candidate 수 / OD-MS-025 위반 정기 grep / 단독 실행 SSM 응답 direct-only 라벨 정합.
- Rollback: 잘못된 event/fill row 운영자 직접 SQL 삭제 · `connector_order_request status` SUBMITTED 복구 · execution FILLED 오전환 시 SUBMITTED 복구 · position OPEN 잘못 생성분 운영자 직접 정리(R-AUTO-001 / R-AUTO-002 정합) · summary fallback guard 재적용 후 broker_order_no 단건 재동기화 · 보정 결과 03 spec operation-notes 누적.
- Evidence links: [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md), [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md).

### R-AUTO-032 Details

- Current status (R-AUTO-032): `Mitigated` — 옵션 신설 + EC2 정식 배포 + dry-run 통과.
- R-AUTO-032 · Impact High · Probability Low · Affected Spec 03, 04, 10.
- Mitigation core (OD-MS-036):
  - `connector_strategy_order_execute.py` 신규 옵션 `--signal-type` · `--intraday-stop-only`. `--intraday-stop-only` 사용 시 `action_type=SELL` + `signal_type=INTRADAY_STOP_SELL` 강제. `--action BUY` 함께 사용 시 실패 · 잘못된 `signal_type` 입력 시 실패.
  - `fetch_requested_strategy_orders()` · `normalize_retryable_rejected_orders()` 에 `signal_type` 필터 추가. `_submit_order()` payload 에 실제 order 의 `signal_type` 전달 보완.
  - EC2 정식 배포 통과 — EC2 path `/home/ec2-user/apps/port-marketconnector/src/connector_strategy_order_execute.py`
    - EC2 backup `connector_strategy_order_execute.py.bak.20260625T055410Z.intraday-stop-filter-1.1.0`
    - 운영 marker `DEPLOY_CONNECTOR_STRATEGY_ORDER_EXECUTE_INTRADAY_STOP_FILTER=SUCCESS` + `..._COMMAND=SUCCESS`.
    - Local sha256 / SSM command_id 는 03 spec operation-notes.
  - EC2 dry-run 검증 통과 — `--intraday-stop-only --limit 20` + `--action SELL --signal-type INTRADAY_STOP_SELL --limit 20` 모두 no-target 정상 · guard failure(`--action BUY` 함께 · 잘못된 `signal_type`) 정상.
  - `--action SELL` 단독 사용 금지 — Daily Step 12 `--execute`(`signal_type` 필터 없음) 와 분리. Intraday 진입은 반드시 `--intraday-stop-only` 또는 `--signal-type INTRADAY_STOP_SELL` 명시.
  - state machine 분리 — `portfolio-paper-intraday-stop-sell-approval` 의 `RunIntradayStopOrderExecute` task 만 `--execute --intraday-stop-only` command 사용. Daily `portfolio-paper-daily-step12-17-approval` 은 기존 `--execute` 만(R-AUTO-031 결합).
- Detection: `--help` 옵션 grep · EC2 운영 marker · SF state machine command 에 `--intraday-stop-only` 포함 여부 · `strategy_execution_order.signal_type` audit(Daily = BUY/SELL / Intraday = INTRADAY_STOP_SELL 만) · 혼선 패턴 · broker 측 동일 종목·수량 주문 2건+ 발생(R-AUTO-001 · R-BROKER-004 결합).
- Rollback: EC2 backup 파일 rollback · State Machine `StopExecution` · 영향 row SQL 점검 + broker 취소(R-AUTO-001/002/019 · R-BROKER-004 정합). 옵션 필터 우회 변경은 본 spec 안전 제약 위반 → 즉시 reverse.
- Evidence links: [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md), [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md).

### R-AUTO-026 Details

- Current status (R-AUTO-026): `Mitigated` — 2026-06-24 자동 재시도 4건 전량체결 통과. broker 중복 주문 0건.
- R-AUTO-026 · Impact High · Probability Medium · Affected Spec 03, 04, 10.
- Mitigation core (OD-MS-033):
  - retry-normalizer 재시도 대상 `rejection_code IN (40580000, EGW00201)` 확장. OD-MS-028 의 복구 조건 6종 중 rejection_code 조건만 갱신 (`broker_order_no IS NULL` + `connector_fill` 없음 유지 · R-AUTO-001 · R-BROKER-004 정합).
  - 주문 사이 기본 sleep · `EGW00201` 응답 시 backoff retry(단순 반복 금지) · KIS 권고 backoff 간격 기본값.
  - `result_payload.submit_attempts` 메타데이터 — timestamp · response_code · rejection_code · `broker_order_no` 생성 여부.
  - `connector_strategy_order_execute.py` 전체 교체 + S3 업로드 + MarketConnector EC2 정식 배포 + `py_compile` + 운영 마커 통과. 본문 인용 0건(R-DOCS-001 정합).
  - dry-run 검증 — `candidate_count=4`(`40580000` 2 + `EGW00201` 2) 모두 재시도 후보 인식 · DB 변경 0건.
- Mitigation history:
  - [2026-06-24 1차 실증] 6/23 잔존 4건(`40580000` 2 + `EGW00201` 2) 자동 재시도 → 4건 전량체결.
    - Dispatcher Lambda `STEP12_17_ORDER` 수동 invoke · `DAILY_EXECUTION_SUCCESS` Slack 수신.
    - 새 `connector_order_request` 4건 · broker_order_no 4건 · 최종 체결(`042660` · `004990` · `003490` · `023530` FILLED). `004990` 단건 order-check 재조회 → `tot_ccld_qty=69` 전량체결 확인(OD-MS-025 · R-AUTO-018 정합). broker 측 중복 주문 0건 · 운영자 직접 SQL 복구 0건.
- Detection:
  - `connector_order_request.rejection_code = EGW00201` 다음 영업일 누적
  - `strategy_execution_order.execution_status IN (READY, FAILED) + connector_order_request_id IS NOT NULL` retry 후보 인벤토리 SQL · Step 12 SSM stdout
  - `connector_api_call_log` `EGW00201` 응답 빈도 · `submit_attempts` backoff 회수 · `retry_normalizer` old request 이력 매핑 · broker 중복 주문 감지(R-AUTO-001 · R-BROKER-004 결합).
- Rollback:
  - `retry_normalizer` old request 이력 기반 운영자 SQL 복구(원래 `connector_order_request_id` 재 link 또는 status 실제 broker 상태로 복원) · 코드 회귀 시 EC2/S3 backup 원복 · retry-normalizer 진입 차단 · Step 12 재실행 · broker 측 잘못된 중복 주문은 KIS 취소 + 정리(R-AUTO-001/002 · R-BROKER-004 정합).
  - 다른 rejection code(`40340000` · `41000000` 등)의 자동 재시도 확장은 본 결정 범위 밖.
- Evidence links: [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md), [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md).

### R-AUTO-027 Details

- Current status (R-AUTO-027): `Accepted` — Slack payload builder 정식 개선 시 `Mitigated` 승격 후보.
- R-AUTO-027 · Impact Medium · Probability Medium · Affected Spec 04, 05, 10.
- Mitigation summary:
  - (a) Slack approval payload builder 개선(후속) — SF approval state input 에 실제 Step · DB 요약(BUY/SELL 후보 수 · 종목 · 사유 · 예상 수량) 채워서 Slack notifier Lambda 가 0/0 대신 실제 값 표시. 04 / 05 spec 후속 phase 책임.
  - (b) 단기 mitigation — 운영자가 Slack summary 0/0 만으로 다음 단계 결정 금지. SF execution history `output` 본문 + `decision.strategy_daily_signal` · `strategy_daily_position_decision` · `execution.strategy_execution_plan` · `strategy_execution_order` DB 결과 직접 점검 보강.
  - (c) 09:01 schedule 자동 ENABLE 보류 정책 결합(OD-MS-033) — 본 위험이 09:01 자동화 진입 안전 게이트 역할.
  - (d) Dispatcher Lambda application log 보강(후속) — CloudWatch Logs 에 SF input · output · runDate · scheduleType · 휴장일 skip 판정 구조화 기록.
  - (e) Slack 본문 · Lambda 코드 · SF ASL 평문 인용 0건(R-DOCS-001 정합).
  - [2026-06-24 1차 식별] 08:00 schedule 첫 실 실행 + Step 12~17 수동 invoke 검증 중 Slack summary 0/0 사례 식별. DB · SF output 직접 점검으로 사후 검증 통과.
- Detection: Slack `APPROVAL_REQUIRED` summary vs SF output 후보 수 · 종목 · 사유 불일치 / Slack 0/0 + DB 신규 row 존재 / Slack summary 만 보고 09:01 결정 사례 / Dispatcher Lambda CloudWatch Logs input·output 구조화 부재.
- Rollback: payload builder 개선 전까지 운영자가 SF history + DB 직접 점검 사후 검증. 09:01 자동 ENABLE 결정은 Slack summary 만이 아닌 SF output + DB + 운영자 노트 결합. 개선 시점에 Status `Accepted` → `Mitigated` 승격. 개선 전까지 09:01 자동 ENABLE 보류(OD-MS-033) 유지.
- Evidence links: [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md), [`../05-port-view-ecs-and-runbook/operation-notes.md`](../05-port-view-ecs-and-runbook/operation-notes.md).

### R-DATA-017 Details

- Current status (R-DATA-017): `Open` — 정식 mitigation(Step2B 강화 · Slack KRX 기준일 · feature lag 정책) 진입 전까지 운영자 단기 mitigation 으로 운영.
- R-DATA-017 · Impact High · Probability Medium · Affected Spec 04, 08, 09, 10.
- Mitigation core:
  - (a) Step2B 성공 기준 강화(후속)
    - `interest_krx_raw_validate_daily.py` 또는 `step-02-interest-crawler.ps1` 안에서 KRX worker 종료 직후 `interest_program_raw`
    - `interest_shortsell_raw` `MAX(trade_date)` vs 직전 영업일 비교 + row_count 임계 비교 자동 수행. worker exit 0 만으로 Step2B SUCCESS 금지. lag N영업일 이상 시 Step2B fail-closed(R-AUTO-020 확장
    - 08 spec 후속 phase).
  - (b) Slack 메시지 KRX 기준일 표시(후속) — approval payload builder 또는 `portfolio-event-notifier` Lambda 에 KRX program · shortsell `MAX(trade_date)` 포함(R-AUTO-027 mitigation 결합).
  - (c) feature lag 허용 정책 명문화(후속) — preprocessor · decision · research feature(`pre_total_market_daily_feature` · `pre_total_stock_daily_feature` 등) 가 raw 직전 영업일 lag 허용 여부 · lag 한계(N영업일) 정의 · 미허용 시 다음 step fail-closed(R-DATA-010 결합 · 08 · 09 spec 후속).
  - (d) 단기 mitigation — 운영자가 SF `SUCCEEDED` 만으로 결정하지 않고 `MAX(trade_date)` vs 직전 영업일 SQL 직접 비교.
- Mitigation history:
  - [2026-06-26 1차 식별] 08:00 schedule → SF `step1-11-approval-20260626-080004-0904111b` `SUCCEEDED` · Scheduled Task `LastTaskResult=0` · worker 정상 종료. 6/28 Local Step 1~11 검증 중 6/26 08:05 KST worker 로그 기준 raw 수집일 = `2026-06-24`(직전 영업일 대비 lag).
  - [2026-06-28 결정적 증거] Local Step 2 에서 `interest_program_raw` · `interest_shortsell_raw` 의 `trade_date=2026-06-25` · `2026-06-26` row 가 row-level 로 최초 생성 — AWS 6/26 SF `SUCCEEDED` 시점에는 두 row 부재 · Local Step 2 가 2일분 한 번에 적재.
    - R-AUTO-020 [2026-06-26 보강] worker exit 0 ≠ KRX raw 최신성 보장 가설 row-level 입증.
    - Step2B 강화 + Slack KRX 기준일 + feature lag 정책 우선순위 격상.
    - Status `Open` 유지.
- Detection:
  - `interest_program_raw` · `interest_shortsell_raw` `MAX(trade_date)` vs 직전 영업일 SQL 정기 점검 / SF `SUCCEEDED` + KRX raw N영업일 이상 lag audit
  - Slack `APPROVAL_REQUIRED` KRX 기준일 표시(후속) vs 실제 DB 일치 / `ExpectedKrxRawDate` 산정 vs worker 실제 수집일 분포 / feature 신규 date 생성
  - `decision.strategy_daily_run.data_date` · `research.strategy_backtest_run.data_date` 직전 영업일 일치.
- Rollback:
  - KRX raw lag N영업일 이상 식별 → KRX worker 재실행 또는 KRX 측 데이터 공개 시점 확인 후 raw 회복 → preprocessor 재실행 → feature max date 회복 → BACKTEST_RESEARCH / BACKTEST_REPORT / DAILY_BUY_SIGNAL / DAILY_POSITION_SIGNAL 재진입(R-DATA-010 정합).
  - 보정 결과는 04 / 08 / 09 spec operation-notes 후속 갱신.
  - 정식 mitigation 진입 시 Status `Open` → `Mitigated` 승격.
- Evidence links: [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md), [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md), [`../09-strategy-research-batch/operation-notes.md`](../09-strategy-research-batch/operation-notes.md).

### R-DATA-010 Details

- Current status (R-DATA-010): `Open` — 정식 자동화 진입까지 운영자 raw 최신성 SQL 점검 단기 mitigation 유지.
- R-DATA-010 · Impact High · Probability Medium · Affected Spec 08, 04, 09.
- Mitigation summary (OD-MS-020 · OD-MS-021 정합):
  - Backend AWS E2E dry-run 17단계 선행 · stale raw data 조기 식별.
  - non-GUI raw 7종(`interest_price_raw` · `interest_investorflow_raw` · `interest_marketbreadth_raw` · `interest_commodity_raw` · `interest_foreignindex_raw` · `interest_news_raw` · `interest_agency_raw`) `MAX(trade_date)` 직전 거래일 N±1 게이트.
  - preprocessor 재실행 후 feature `max date` · `updated_at` 검증 · 신규 feature date 생성 여부(08 spec task 77/78).
  - Research → Decision 순서 유지(OD-MS-021).
  - BACKTEST_RESEARCH / BACKTEST_REPORT / DAILY_BUY_SIGNAL / DAILY_POSITION_SIGNAL 진입 전 raw 최신성 회복 게이트(08 spec task 74 · R-DATA-009 결합).
- Mitigation history:
  - [2026-06-15 보강] Backend AWS E2E dry-run 1차 — preprocessor exitCode 0 · `updated_at` 갱신에도 신규 2026-06-15 feature 0건 → 원인 raw 최신성 부족(`interest_price_raw` 등 7종 최신일 = 2026-06-08) 1차 실증.
  - [2026-06-16 보강] non-GUI crawler 실 운영 Task Definition rev7 분리 · RunTask exitCode 0 · 전체 step SUCCESS · raw 최신성 회복(price/investorflow/marketbreadth/commodity/foreignindex/macro 2026-06-15 · news/agency 2026-06-16).
    - KRX `interest_program_raw` 547→548 · `interest_shortsell_raw` 190,554→190,903. `interest_foreignindex_raw` HANGSENG · NIKKEI225 · SHANGHAI 일부 NULL 은 non-blocker.
- Detection: `interest_*_raw` `MAX(trade_date)` vs 직전 거래일 SQL / preprocessor `updated_at` 후 신규 feature date / Research · Decision `data_date` vs 직전 거래일 / N-2 이상 lag 즉시 식별 / Backend E2E dry-run step 4~7 진입 전 raw 최신성 점검.
- Rollback: raw 최신성 회복 후 preprocessor 재실행 → exitCode 0 → feature max date/`updated_at` 확인 → 신규 feature date 생성 → 정상 시 BACKTEST/DAILY 재진입. stale 결과 산출 시 영향 row 운영자 SQL 식별 후 manual rerun.
- Evidence links: [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md), [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md), [`../09-strategy-research-batch/operation-notes.md`](../09-strategy-research-batch/operation-notes.md).

### R-AUTO-016 Details

- Current status (R-AUTO-016): `Open` — 2026-06-16 · 2026-06-21 검증에서 정상 동작 확인. Autologon session 안정성 지속 관찰.
- R-AUTO-016 · Impact High · Probability Medium · Affected Spec 08.
- Mitigation summary (OD-MS-022):
  - Autologon bootstrap EC2 재부팅 직후 1회 확인 · `query user` Administrator `console` STATE `Active` pre-check.
  - schtasks trigger 전 SSM `query user`/`whoami` · Scheduled Task 등록 여부 점검.
  - wrapper log `C:\portfolio\logs\krx_worker_daily_*.log` KRX login 성공 라인 모니터.
  - DB 적재 검증(`interest_program_raw`·`interest_shortsell_raw` max date / row count).
  - 실패 시 운영자 RDP bootstrap 우회 — Autologon 재설정 · Administrator password 회전 후 Autologon 갱신.
- Mitigation history:
  - [2026-06-16 1차 실증] Autologon bootstrap → EC2 재부팅 → SSM Online → `query user` Active → schtasks trigger → KRX login 성공(elapsed 92.83s) · `interest_program` · `interest_shortsell` 2026-06-15 349 Company 적재.
  - [2026-06-17 wrapper 보강] wrapper `Step 2 INTEREST_CRAWLER` 가 EC2 instance state 사전 점검 → `running` 이 아니면 GUI Scheduled Task trigger skip · non-GUI ECS RunTask 는 무관하게 진행(OD-MS-023).
  - [2026-06-21 fail-closed 갱신] "자동 skip" → fail-closed 로 갱신(OD-MS-026 · R-AUTO-020). Crawler Worker EC2 `running` 이 아니면 Step 2 즉시 fail · instanceId/state 출력 · KRX GUI · DB validation 미수행 상태에서 Step 2 SUCCESS 진입 차단.
- Detection: `query user` 빈 결과/`Disc` · Scheduled Task `Last Result` 0외(`267009` 등) · KRX login 실패 라인 · DB max date 직전 거래일 미도달 · wrapper log 부재 · SSM `schtasks` 실패 · wrapper Step 2 worker `running` 여부 audit · `KRX GUI trigger skipped (worker not running)` 라벨.
- Rollback: RDP 직접 접속 → Administrator 로그인 → Autologon 재설정(`autologon.exe` · password 입력) → Scheduled Task 재실행 또는 wrapper 직접 실행 → KRX login 성공 · 적재 확인 → 영향 일자 재실행 후 preprocessor 재실행.
- Evidence links: [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md).

### R-AUTO-020 Details

- Current status (R-AUTO-020): `Mitigated` — 2026-06-21 Step 2 단독 실행 검증 통과. OD-MS-026 신규.
- R-AUTO-020 · Impact High · Probability Medium · Affected Spec 08, 04, 05, 09.
- Mitigation summary:
  - (a) Chrome / chromedriver stale process best-effort reset — `step-02-interest-crawler.ps1` 가 KRX worker 실행 직전 process 정리 시도 · 실패는 warning(R-AUTO-017 정합).
  - (b) Scheduled Task Running → Ready 복귀 wait — `Running` polling · `sawRunning` 로그 · `Ready` 복귀 · timeout 시 Step 2 실패.
  - (c) Last Result 0 또는 0x0 만 SUCCESS — trigger 성공 + non-zero 시 Step 2 실패.
  - (d) latest worker log 출력 — path · last write time · size · tail.
  - (e) KRX raw DB validation guard — SSM step `INTEREST_CRAWLER_KRX_DB_VALIDATE` → `interest_krx_raw_validate_daily.py --expected-date <ExpectedKrxRawDate>` · `interest_program_raw` · `interest_shortsell_raw` expected trade_date 기준 row_count + `max(trade_date)` 검증 · 실패 exit code 30 · Step 2 fail.
  - (f) crawler worker stopped fail-closed — EC2 state != `running` 시 Step 2 즉시 fail(R-AUTO-016 갱신).
  - (g) wrapper run 별 `KrxDbValidationCommandId` 자동 기록.
- Mitigation history:
  - [2026-06-21 1차 실증] Step 2 단독 실행(RunId `daily-aws-paper-20260621-204017`) 통과 — non-GUI ECS exitCode 0 · Crawler Worker EC2 running · Scheduled Task elapsedSeconds=111
    - sawRunning=True · FinalStatus=Ready · FinalLastResult=0 · KRX raw DB validation Success · `interest_program_raw` expected=2026-06-19 max_date=2026-06-19 expected_count=1
    - `interest_shortsell_raw` expected_count=349 · exit 0.
- Detection: `KrxDbValidationCommandId` 자동 기록 · SSM step `Success`/`ResponseCode 0` + validation exit 0 + expected date row_count 자동 점검 · wrapper summary ExpectedKrxRawDate · Last Result 0/0x0 외 값 · EC2 stopped fail-closed 진입 · worker log SUCCESS 라벨 부재 · non-GUI ECS exitCode != 0.
- Rollback: validation 실패 시 wrapper Step 2 자동 fail · KRX worker 단독 재실행 + DB 점검 + Step 2 단독 재실행 · EC2 stopped 분기 시 start + SSM Online wait 후 Step 2 재실행 · Chrome stale 시 RDP 강제 정리 또는 EC2 stop/restart(R-AUTO-017).
- Evidence links: [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md).

### R-AUTO-021 Details

- Current status (R-AUTO-021): `Mitigated` — 2026-06-22 1차 실증 후 2026-06-23 Step Functions 전환 이후에도 유지. 정식 systemd unit 등록까지 wrapper bootstrap 함수가 single source of truth.
- R-AUTO-021 · Impact High · Probability Medium · Affected Spec 03, 04, 06.
- Mitigation summary (OD-MS-027):
  - (a) wrapper `daily-aws-paper.functions.ps1` 에 MarketConnector env bootstrap 공통 함수 추가 · Step 1/12/13/17 진입 직전 호출.
  - (b) bootstrap 흐름 — `secretsmanager:GetSecretValue` → JSON parse → 내부 key(`APP_KEY`·`APP_SECRET`·`PAPER_ACNT`·`ACNT_PRDT_CD`·`BASE_URL`) 추출 → `APP_*` 호환 + `KIS_*` alias 동시 export → `/tmp/inject-env.sh` chmod 700 · 메모리 export 한정.
  - (c) secret value 평문 출력 0건(R-DOCS-001 정합 · length/key presence 만 출력).
  - (d) PowerShell parser validation 후 Step 1 재실행 검증.
  - (e) `/tmp/inject-env.sh` 선존재 가정 폐기(OD-MS-027).
  - (f) 정식 systemd unit + `EnvironmentFile` 등록은 03 spec task 7/26/27 후속.
- Mitigation history:
  - [2026-06-22 1차 실증] Step 1 최초 실패(`/tmp/inject-env.sh not found`) → 운영자 wrapper patch + Step 1 재실행 통과 → Step 12/13/17 모두 정상 진입 · Daily wrapper 1~17 두 번째 실 완주 · 첫 실 SELL E2E.
  - [2026-06-23 보강] SF state machine `portfolio-paper-daily-step1-17-approval` 실전 검증(false/true path 모두 통과 · OD-MS-029 정합). SF 전환 이후에도 wrapper 공통 bootstrap 호출 흐름 그대로 유지 필요. Step Functions state 정의에 동일 패턴 매핑 필요.
- Detection: wrapper SSM responseCode 0외 · stdout `/tmp/inject-env.sh: not found` · KIS API 인증 실패(`appkey`·`appsecret`·`cano` 빈값) · step FAILED 라벨 · MarketConnector `ls -la /tmp/inject-env.sh` 부재 확인(권한/크기만 · secret 평문 금지).
- Rollback: wrapper patch 반영 여부 확인 후 Step 1 단독 재실행 · patch 반영 전 운영자 EC2 직접 접속 → `/tmp/inject-env.sh` 수동 재생성(권한 700 · 평문 미저장 · 메모리 export · 03 runbook §2) → Step 1 재실행 · secret 평문 노출 시 즉시 파일 삭제 + 영향 secret 회전(R-DOCS-001).
- Evidence links: [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md), [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md).

### R-AUTO-022 Details

- Current status (R-AUTO-022): `Mitigated` — 2026-06-23 EC2 배포 + dry-run 통과. 실 retry 후보 첫 회차 검증은 후속.
- R-AUTO-022 · Impact Medium · Probability Medium · Affected Spec 03, 04, 10.
- Mitigation summary (OD-MS-028):
  - (a) Step 12 retry-normalizer 내장 — `connector_strategy_order_execute.py` 전체 교체 · Step 12 시작부에서 retry-normalizer 동작 · 별도 Step 11.5 분리 아님.
  - (b) 복구 조건 6종 만족 시에만 — `execution_mode=PAPER_STRATEGY` + `action_type IN (BUY, SELL)` + `execution_status IN (READY, FAILED)` + `connector_order_request_id IS NOT NULL` + linked `request_status=REJECTED` + `rejection_code=40580000` + `broker_order_no IS NULL` + `connector_fill` 없음.
    - broker 응답 도달(`broker_order_no IS NOT NULL`) 또는 fill 존재는 복구 대상 제외(R-BROKER-004 · R-AUTO-001 중복 주문 회피).
  - (c) 복구 처리 = `execution_status=REQUESTED` + `connector_order_request_id=NULL` + `result_payload.retry_normalizer` old request 이력 저장(`old_connector_order_request_id`·`request_status`·`rejection_code`·`recovered_at`).
  - (d) EC2 정식 배포 + `.venv/bin/python` dry-run 통과 — retry 후보 0건 · REQUESTED 주문 0건.
  - (e) Step Functions Step 12 도 `.venv/bin/python` 사용 정합.
  - (f) `40580000` 한정 · 다른 rejection code(거래정지/거부/호가 단위 오류 등)는 운영자 직접 점검(R-AUTO-001 · R-BROKER-004).
- Mitigation history:
  - [2026-06-23 1차 dry-run 통과] EC2 배포 + `.venv/bin/python` dry-run 통과 · 실 retry 후보 첫 운영 회차 검증은 후속.
- Detection: Step 12 dry-run `retry_normalizer recovered N rows` · `connector_order_request` REJECTED + `rejection_code=40580000` row · `strategy_execution_order` retry 후보 인벤토리 SQL · old request 이력 audit(같은 order_id 두 번 복구 정합성) · broker 중복 주문(R-AUTO-001 · R-BROKER-004 결합).
- Rollback: old request 이력 기반 SQL 복구 · 코드 회귀 시 이전 백업 복구 + retry-normalizer 진입 차단 + Step 12 재실행 · broker 잘못된 중복 시 KIS 취소 + 정리(R-AUTO-001/002 · R-BROKER-004 정합).
- Evidence links: [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md), [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md).

### R-AUTO-023 Details

- Current status (R-AUTO-023): `Mitigated` — 2026-06-23 3종 Slack 수신 검증 통과. 장 전/장 후 잔고 · 장중 손절 Slack 은 후속 분리(R-AUTO-035/036 신규).
- R-AUTO-023 · Impact High · Probability Medium · Affected Spec 04, 05, 10.
- Mitigation summary (OD-MS-030 · OD-MS-031):
  - (a) SF Catch 경로 `DAILY_EXECUTION_FAILED` Slack — state machine 의 모든 실패 가능 state(broker 호출/fill sync/position sync/balance refresh/approval gate)에 Catch 정의 · Slack notifier Lambda 로 연결.
  - (b) 1차 3종 Slack 이벤트 라벨 — `APPROVAL_REQUIRED`(Step 1~11 후 승인 대기) · `DAILY_EXECUTION_SUCCESS`(Step 17 완료) · `DAILY_EXECUTION_FAILED`(SF 실행 중 실패).
  - (c) test-only 실패 주입 검증 — 12~17 test-only · 1~17 full workflow test-only 실패 ASL · 실 broker 주문 실패 의도적 유발 아님(추가 broker 호출 0건 · aws-live 작업 0건).
  - (d) Slack 발송 실패 시 SF execution history Catch state 실행 audit · DLQ · retry · CloudWatch Alarm 은 후속(followups-overview 2026-06-23).
  - (e) Slack notifier Lambda IAM Role 최소 권한(Slack webhook 호출만 · Resource · Action wildcard 0건 · OD-SEC-005/006).
- Mitigation history:
  - [2026-06-23 1차 실증] 3종 Slack 수신 검증 통과 — `APPROVAL_REQUIRED` · `DAILY_EXECUTION_SUCCESS` · `DAILY_EXECUTION_FAILED` 12~17 test-only + 1~17 full test-only 수신 · Portfolio Daily Bot 메시지 표시.
- Detection: SF Failed/TimedOut/Aborted state 발생 시 Slack `DAILY_EXECUTION_FAILED` 부재 · Lambda CloudWatch error/timeout · webhook 응답 비-200 · Catch state input↔invoke output 매핑 실패. 메시지 본문/Lambda 코드/SF history/webhook 응답 본문 평문 인용 0건(R-DOCS-001 정합) — metadata 만 점검.
- Rollback: Slack 누락 시 Catch 정의 · invoke 매핑 · IAM 권한 점검 · Lambda 실패 시 운영자 직접 webhook 수동 호출(webhook URL 평문 0건) 또는 port-view 콘솔에서 Daily Batch 상태 확인 · broker 흐름 끊김은 R-AUTO-001/002 · R-BROKER-004 rollback 결합. DLQ/retry/CloudWatch Alarm 도입은 후속.
- Evidence links: [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md), [`../05-port-view-ecs-and-runbook/operation-notes.md`](../05-port-view-ecs-and-runbook/operation-notes.md).


### R-AUTO-019 Details

- Current status (R-AUTO-019): `Mitigated` -- 상세 이력은 아래 참조.
- R-AUTO-019 · Impact High · Probability Low · Affected Spec 03, 04, 10.
- Mitigation history:
  - (a) wrapper 중앙 PAPER_ORDER_GATE — Step 12 는 main wrapper 레벨에서 PAPER_ORDER_GATE 키워드를 가진 step 으로 분류되어 `-AllowPaperOrderExecute` 옵션 없이는 실행 자체가 차단(SSM command 제출 0건).
  - (b) Step 12 내부 이중 gate — step 파일 자체에서도 `-AllowPaperOrderExecute` 입력값을 다시 검증하고 미명시 시 즉시 종료.
  - (c) 옵션 default OFF — `[switch]$AllowPaperOrderExecute` 파라미터의 default 는 false / 운영자가 명시적으로 `-AllowPaperOrderExecute` 를 입력해야만 true.
  - (d) wrapper summary 의 `PaperOrder` 필드에 옵션 사용 여부를 명시 기록 — 운영자가 사후 검증 가능.
  - (e) Step 10 / Step 11 의 `--execute` 는 strategy execution 내부 상태 생성 / 갱신 의미로만 사용되고 broker · KIS 직접 제출 아님(OD-MS-016 책임 분리 정합) — Step 12 만 실제 KIS paper 주문 제출 가능 step.
  - (f) 옵션 사용 시 운영자 노트 사전 기록 권고 — 03 spec operation-notes 에 사용 사실 / 일자 / 영향 row 기록.
  - [2026-06-17 1차 실증] 본 일자 wrapper 1차 검증에서 `-StartStep 12 -EndStep 12` 실행 / `DryRun: False` / `PaperOrder: False` 기본 / 중앙 + 내부 이중 gate 차단 확인 / SSM command 제출 0건 / 실제 KIS 주문 제출 0건.
  - [2026-06-18 1차 실주문 사용]
    - 운영자가 `-AllowPaperOrderExecute` 명시로 첫 실 paper BUY 4건 제출 — 1차 시도에서 KIS paper API read timeout 발생(`connector_order_request 38 ~ 41` FAILED
    - R-BROKER-004 신규 정합) → 통제된 REQUESTED 복구 후 재시도 → KIS paper BUY 4건 제출 성공(broker_order_no `0000025576` / `0000025740` / `0000025744` / `0000025
  - 747` / `connector_order_request 42 ~ 45` ACCEPTED).
  - 옵션 사용 사실은 03 spec operation-notes / 04 spec operation-notes 에 사실 기록 / 의도하지 않은 옵션 사용 0건 정합
- Detection:
  - wrapper summary 파일 / `summary/run-summary.txt` 의 `PaperOrder : True` 항목 빈도 audit(`PaperOrder : True` 출력은 의도된 운영자 직접 옵션 명시일 때만 정상). Step 12 내부 stdout / stderr 파일에 `PAPER_ORDER_GATE blocked` / `PAPER_ORDER_GATE bypassed` 라벨 출력 / 운영자가 사용 직후 사후 검증.
  - `connector.connector_order_request` insert 시점과 wrapper run id / step 12 실행 시점의 매핑 정합성 점검(03 spec operation-notes 의 KIS paper 주문 4건 broker_order_no `0000035906` / `0000035912` / `0000035918` / `0000035932` 패턴과 같이 broker 응답값 매핑 확인).
  - `-AllowPaperOrderExecute` 옵션이 사용된 모든 wrapper run 의 summary 보존(R-DOCS-001 정합 — secret value 평문 출력 0건 점검)
- Rollback:
  - 즉시 wrapper Step 12 SSM command 중단(`aws ssm cancel-command` 또는 운영자 직접 수동 중단) / 잘못 제출된 broker 주문 발생 시 KIS 측 취소 + 운영자 직접 정리(R-AUTO-001 / R-AUTO-002 정합)
    - 영향 row(`strategy_execution_order` / `connector_order_request` / `connector_order_event` / `connector_fill` / `strategy_position_state`) 운영자 직접 SQL 점검 후 정리.
  - wrapper 옵션 사용 사실 / 영향 row / 보정 결과는 03 spec operation-notes 에 누적 기록. wrapper 자체의 PAPER_ORDER_GATE 를 우회하는 변경은 본 spec 안전 제약 위반이므로 즉시 reverse(git revert)

### R-AUTO-030 Details

- Current status (R-AUTO-030): `Open` -- 상세 이력은 아래 참조.
- R-AUTO-030 · Impact High · Probability Medium · Affected Spec 03, 04, 10.
- Mitigation history:
  - (a) **초기 `DISABLED` 유지 + 수동 · 승인 후 실행 정책** — 3단계 Submit & Refresh 별도 Step Functions state machine 정식 정의 시점에 schedule 또는 trigger 자동 ENABLE 진입을 의도적으로 보류 / 운영자가 직접 invoke 하는 수동 모드로 1차 운영(OD-MS-033 의 09:01 보류 패턴과 동일한 안전 게이트).
  - (b) **OD-SAFE-002 / OD-SAFE-003 본문 그대로 유지** — aws-live 초기 자동주문 금지 / 후보 + View 수동 승인 우선 / paper 환경에서도 1차 검증 + 운영자 승인 후 단계적 자동 진입.
  - (c) **자동 ENABLE 진입 시점 결정** — 1단계 + 2단계 구현 완료 + 안전장치 4종 검증 통과 + 운영 회차 누적 결과 점검 + Slack 알림 정합성 검증 + 운영자 별도 승인 후에만 진입 / 본 결정의 진입 시점은 결정의 후속 phase 책임.
  - (d) **Slack 알림 안전 게이트** — 3단계 Submit & Refresh 의 모든 운영 회차에서 Slack `INTRADAY_STOP_SELL_SUBMITTED` 또는 동등 eventType 발송 / 운영자가 사후 검증 가능. 본 위험은 설계 단계에서 mitigation 방향만 정의 / 정식 state machine + 자동 ENABLE 보류 정책은 구현 시점에 결정 / 정식 구현 + 검증 통과 후 `Mitigated` 승격 후보.
  - [2026-06-25 보강 — State Machine 생성 + blocked gate + no-target 검증 통과]
    - State Machine `portfolio-paper-intraday-stop-sell-approval`(STANDARD / ACTIVE / 18-state
    - 2026-06-25T14:58:45+09:00) 생성 통과 + blocked gate 안전 테스트(`intraday-stop-blocked-gate-20260625-145928` / SUCCEEDED / `allowIntradayStopOrderExec
  - ute=false` / SSM · ECS 미실행 / 주문 제출 0건) + true-path no-target 안전 테스트(`intraday-stop-truepath-notarget-20260625-150146` / SUCCEEDED / 신규 `connector_order_request` 0건 / KIS 주문 제출 0건) 통과(OD-MS-036 신규 정합).
  - 자동 트리거(EventBridge Scheduler 또는 다른 trigger)는 여전히 미연결 / 운영자 수동 `StartExecution` 한정 / 실제 1주 `INTRADAY_STOP_SELL` 주문 테스트는 KIS 보유 종목 0건 으로 보류(R-BROKER-005 신규 정합). Status `Open` 유지 — 정식 자동 ENABLE 진입은 후속 phase + 운영자 별도 승인 후 / 자동 trigger 연결 + 실주문 검증 + Slack 알림 정합성 검증 통과 후 `Mitigated` 승격 후보.
- Detection:
  - 3단계 별도 Step Functions state machine 의 `State=ENABLED` 진입 audit(CloudTrail) / 운영자 승인 게이트 우회 사례 / Slack `INTRADAY_STOP_SELL_SUBMITTED` 메시지 vs 실제 broker 호출 timing 불일치
    - `connector_order_request` 의 `source_type=INTRADAY_STOP_SELL` row 가 운영자 수동 invoke 시점이 아닌 자동 트리거로 생성되는 패턴 / OD-MS-033 의 09:01 보류 정책 위반 패
  - 턴과 동일 detection 구조.
- Rollback:
  - 자동 ENABLE 정책 위반 발생 시 즉시 `disable-schedule` 또는 state machine trigger 비활성화 + 영향 row(`strategy_execution_order` · `connector_order_request` · `connector_order_event`
    - `connector_fill` · `strategy_position_state`) 운영자 직접 SQL 점검 후 broker 측 주문 정리 + 보정 결과 03 / 04 spec operation-notes 사실 기록(R-AUTO-001 / R-
  - AUTO-002 / R-AUTO-019 / R-BROKER-004 정합).
  - 본 mitigation 의 단계적 활성화 정책(3단계 자동 ENABLE 보류 / 초기 수동 · 승인 후 실행) 을 우회하는 변경은 본 spec 안전 제약 위반이므로 즉시 reverse.

### R-AUTO-031 Details

- Current status (R-AUTO-031): `Mitigated` -- 상세 이력은 아래 참조.
- R-AUTO-031 · Impact High · Probability Low · Affected Spec 03, 04, 05, 10.
- Mitigation history:
  - (a) **`allowIntradayStopOrderExecute=false` blocked gate 안전 테스트 통과** — 2026-06-25 실 실행(`intraday-stop-blocked-gate-20260625-145928`) 에서 `CheckIntradayStopApproval` → `BlockedByIntradayStopApprovalGate` → `ExecutionSucceeded` 흐름 통과
    - `RunIntradayStopOrderExecute` 미진입 / `TaskStateEntered` 없음 / SSM · E
  - CS 미실행 / 주문 제출 0건 / 운영 marker `INTRADAY_STOP_SELL_BLOCKED_GATE_TEST=SUCCESS`.
  - (b) **true-path 수동 승인 입력 한정** — `allowIntradayStopOrderExecute=true` 입력값이 명시된 운영자 수동 `StartExecution` 호출에서만 broker 호출 가능 / 본 State Machine 은 EventBridge Scheduler 또는 다른 자동 trigger 에 연결되지 않은 상태 유지(R-AUTO-030
  - [2026-06-25 보강] 정합).
  - (c) **Daily Step 12 와 분리** — `portfolio-paper-daily-step12-17-approval` state machine 과 별도 / `portfolio-paper-intraday-stop-sell-approval` state machine 의 ASL 실행부는 Daily Step 12~17 ASL 을 참고해 신규 구성되었으나 Daily entrypoint 와 의도적으로 분리 / Daily 와 Intraday 흐름이 동일 State Machine 안에서 섞이지 않음.
  - (d) **`--intraday-stop-only` 필터로 `signal_type=INTRADAY_STOP_SELL` 만 처리** — 실행 command `connector_strategy_order_execute.py --execute --intraday-stop-only` 는 `signal_type=INTRADAY_STOP_SELL` `READY` order 만 처리 / Daily SELL · BUY 흐름의 `READY` order 와 분리(R-AUTO-032 신규 mitigation 정합).
  - (e) **검증 통과** — blocked gate + true-path no-target(`intraday-stop-truepath-notarget-20260625-150146` / SUCCEEDED / 신규 `connector_order_request` 0건 / KIS 주문 제출 0건) 두 가지 안전 테스트 모두 통과.
- Detection:
  - Step Functions execution history 의 `CheckIntradayStopApproval` → `RunIntradayStopOrderExecute` 진입 사례 audit / `TaskStateEntered` event 의 발생 여부
    - 운영자 수동 `StartExecution` 시점이 아닌 자동 trigger 로 invoke 된 사례 / `connector_order_request` 의 `signal_type=INTRADAY_STOP_SELL` 신규 row 가 운영자 명시 승인 시점이 아닌 패턴 / approv
  - al gate 분기 정합성 SQL 점검 / CloudTrail 의 `StartExecution` 호출 source IP / IAM principal audit.
- Rollback:
  - 잘못 분기된 execution 발견 시 운영자가 즉시 `StopExecution` 호출 + 영향 row(`strategy_execution_order` · `connector_order_request` · `connector_order_event` · `connector_fill` · `strategy_position_state`) 운영자 직접 SQL 점검.
  - broker 측에 잘못된 손절 주문이 흘러간 경우 KIS 측 즉시 취소 + 운영자 직접 정리(R-AUTO-001 / R-AUTO-002 / R-AUTO-019 / R-BROKER-004 정합). Scheduler / trigger 미연결 유지 정책을 우회하는 변경은 본 spec 안전 제약 위반이므로 즉시 reverse / State Machine 자체를 임시 `delete-state-machine` 후 ASL 백업본으로 재생성 가능.
  - 잘못 생성된 `READY` order 는 `connector_order_request` 생성 전이면 DB status 변경(예: `BLOCKED` 또는 `CANCELLED`) 으로 차단.

### R-AUTO-029 Details

- Current status (R-AUTO-029): `Mitigated` -- 상세 이력은 아래 참조.
- R-AUTO-029 · Impact High · Probability Medium · Affected Spec 03, 04, 10.
- Mitigation history:
  - (a) **2단계 evaluate 단계 사전 점검** — 신규 evaluate 파일에서 동일 `account_id` · `ticker_code` · `source_type=INTRADAY_STOP_SELL` · `execution_status IN (READY, REQUESTED, SUBMITTED, PARTIAL_FILLED)` row 가 이미 존재하면 신규 `READY` 생성 차단 / 기존 row 의 상태와 함께 응답 라벨 기록.
  - (b) **idempotency 키 도입 검토** — `strategy_execution_order` 또는 별도 idempotency 테이블에 `account_id` + `ticker_code` + `source_type` + 운영 일자 기준 unique constraint 부여 후속 / 구현 시점에 02 spec db-roles-and-grants 후속 갱신과 결합.
  - (c) **3단계 Submit & Refresh 의 `source_type=INTRADAY_STOP_SELL` 전용 필터** — Submit & Refresh 별도 Step Functions state machine 의 첫 task 에서 `source_type=INTRADAY_STOP_SELL` + `execution_status=READY` + `connector_order_request_id IS NULL` 조건만 처리
    - Daily BUY/SELL 의 `READY` order 와 분리 / R-AUTO-001 / R-BROKER
  - -004 의 중복 주문 위험 우회 0건.
  - (d) **3단계 초기 수동 · 승인 후 실행** — 자동 ENABLE 보류 정책(R-AUTO-030 신규)으로 인해 broker 측 중복 주문 위험은 운영자 승인 게이트에서 1차 차단.
  - (e) Slack 알림에 `source_type` 라벨 명시 — Submit & Refresh 결과 Slack 메시지에서 `source_type=INTRADAY_STOP_SELL` 명시 / 운영자가 Daily 흐름의 알림과 구분 가능. 본 mitigation 의 detection 은 OD-MS-035 의 안전장치 4종 중 duplicate order 검증 항목과 동일.
- Detection:
  - `strategy_execution_order` 의 동일 `account_id` + `ticker_code` + `source_type=INTRADAY_STOP_SELL` 조합 row 수 SQL 점검(GROUP BY 후 count > 1 패턴)
    - 신규 evaluate 파일 응답에서 `duplicate_skipped=true` 라벨 / `connector_order_request` 의 동일 종목 · 동일 수량 신규 row 가 짧은 시간 안에 2건 이상 생성되는 패턴(R-AUTO-001 / R-BROKER-004 detection 결
  - 합) / Slack 알림에서 동일 종목 손절 메시지가 짧은 시간 안에 2건 이상 수신되는 패턴 / 3단계 Step Functions execution history 의 input `source_type` 라벨 audit.
- Rollback:
  - 잘못 생성된 duplicate `READY` row 발견 시 운영자가 직접 SQL 로 중복 row 제거(broker 호출 발생 전이면 `READY` 단계 row 만 삭제).
  - broker 측에 잘못된 중복 주문이 흘러간 경우 → KIS 측 즉시 취소 + 운영자 직접 정리 + 영향 row(`strategy_execution_order` · `connector_order_request` · `connector_order_event` · `connector_fill` · `strategy_position_state`) SQL 점검(R-AUTO-001 / R-AUTO-002 / R-BROKER-004 정합).
  - idempotency 키 도입 후속 진행 시 unique constraint 부여 결과를 02 spec db-roles-and-grants + 03 spec operation-notes 에 사실 기록.

### R-DATA-013 Details

- Current status (R-DATA-013): `Mitigated` -- 상세 이력은 아래 참조.
- R-DATA-013 · Impact High · Probability Medium · Affected Spec 02, 04.
- Mitigation history:
  - (a) 운영자 직접 GRANT — `GRANT USAGE ON SCHEMA decision TO execution_app` + `GRANT UPDATE ON TABLE decision.strategy_daily_position_decision TO execution_app`.
  - (b) `execution_app` 에 SELECT + UPDATE 두 권한만 부여 — INSERT / DELETE / TRUNCATE 미부여 / `decision` schema 다른 테이블 UPDATE 미부여(권한 최소화 정합 / OD-DB-011 신규 정합 / OD-DB-007 SELECT-only 정책의 한정된 예외).
  - (c) GRANT 검증 SQL — `pg_has_role` / `has_table_privilege` 로 `execution_app` 의 SELECT / UPDATE 두 권한 확인.
  - (d) Step 9 재실행 통과 검증 — ECS RunTask exitCode 0 / `decision.strategy_daily_position_decision` 의 `execution_order_id` UPDATE 정합 / 후속 Step 10 ~ Step 17 진입 가능.
  - (e) 02 spec db-roles-and-grants 정식 매트릭스 갱신은 후속(R-DATA-005
  - [2026-06-22 보강] 정합).
  - [2026-06-22 1차 실증] 본 일자 Daily AWS Paper Step 9 1차 실패(`execution_app` UPDATE 권한 누락) → 운영자 직접 GRANT 보정 → Step 9 ~ Step 11 재실행 통과 → 후속 Step 12 ~ Step 17 정상 진입 → Daily wrapper 1 ~ 17 두 번째 실 완주 통과(첫 실제 SELL E2E / 04 spec operation-notes 2026-06-22 §2 정합)
- Detection:
  - ECS RunTask(`portfolio-paper-strategy-execution:1` + command override `python daily_sell_execution_run.py`) 의 stderr 에 `permission denied for table strategy_daily_position_decision` 또는 동등 권한 오류 패턴 / `decision.strategy_daily_position_decision` 의 `execution_order_id` 가 SELL execution_order 가 생성된 직후에도
  - NULL 인 패턴 / `execution_app` 의 `has_table_privilege('decision.strategy_daily_position_decision', 'UPDATE')` 결과 false / Step 9 step failure exit status / wrapper run summary 의 Step 9 FAILED 라벨
- Rollback:
  - GRANT 누락 발견 시 운영자가 직접 `GRANT USAGE ON SCHEMA decision TO execution_app` + `GRANT UPDATE ON TABLE decision.strategy_daily_position_decision TO execution_app` 수행 후 권한 확인 SQL 통과 + Step 9 단독 재실행. 잘못된 UPDATE 권한 광역 확장(`decision` schema 전체 UPDATE 등) 이 발견되면 즉시 회수(REVOKE) 후 영향 범위 점검.
  - 정식 매트릭스 갱신은 02 spec db-roles-and-grants 후속 / `decision` schema 의 다른 table(예: `strategy_daily_signal` / `strategy_daily_run`) 에 대한 `execution_app` UPDATE 는 본 결정 적용 범위 밖으로 미부여 유지(OD-DB-011 정합)

### R-BROKER-005 Details

- Current status (R-BROKER-005): `Mitigated` -- 상세 이력은 아래 참조.
- R-BROKER-005 · Impact High · Probability Medium · Affected Spec 03, 04, 10.
- Mitigation history:
  - (a) **실주문 테스트 전 KIS `output1` / sellable quantity
    - strategy OPEN position 사전 점검 필수화** — KIS 잔고 refresh 호출(예: SSM command_id `5b1e5757-ea93-4eb6-a7a0-6d5a38d87840` 와 동등 패턴) 으로 KIS 응답의 `output1_count` 확인 + `strategy_position_state` 의 `status=OPEN` 행 수 확인 + `connector_position_snapshot.sellable_quanti
  - ty` 확인 / 세 가지 모두 0 이면 실주문 테스트 진입 불가 정책.
  - (b) **2026-06-25 검증에서 보유 종목 0건 확인 후 실주문 보류** — KIS refresh `output1_count=0` + `output2_count=1` + `rt_cd=0` + `msg_cd=20310000` + `EMPTY_NORMAL` + `as_of_date=2026-06-25`
    - `strategy_position_state OPEN` 0건 / 기존 `INTRADAY_STOP_SELL` 0건 / `connector_order_request` 최신 id `56` / 실주문 테스트 중단·보류 결정 / 다음
  - 보유 포지션 발생 후 재개(OD-MS-036 신규 정합).
  - (c) **없는 포지션 매도 주문 생성 금지** — 신규 evaluate 파일 안에서 `strategy_position_state OPEN` 0건 또는 `connector_position_snapshot.sellable_quantity = 0` 또는 KIS `output1_count = 0` 조건 중 어느 하나라도 만족하면 `INTRADAY_STOP_SELL` `READY` order 생성 차단
    - 응답 라벨(예: `reason=NO_OPEN_POSITION` 또는 `reason=EMPTY_BALANCE`) 기록 / R-DATA-0
  - 15 mitigation 결합.
  - (d) **실주문 진입 운영자 체크리스트 후속 정리** — runbook 또는 operation-notes 에 실주문 검증 진입 전 사전 점검 4종(KIS `output1_count` + `output2_count` + `strategy_position_state OPEN` + `sellable_quantity`) 명시 정리 후속(05 spec / 10 spec 후속 phase 책임).
- Detection:
  - KIS balance response `output1_count` SQL 점검 / `strategy_position_state status='OPEN'` 행 수 SQL 점검 / `connector_position_snapshot.sellable_quantity` SQL 점검
    - 신규 evaluate 파일 응답에서 `reason=NO_OPEN_POSITION` 또는 `reason=EMPTY_BALANCE` 라벨 빈도 / `INTRADAY_STOP_SELL` 후보 발생 직전 KIS refresh 응답 metadata audit / br
  - oker 측 매도가능수량 부족 응답(예: REJECTED 응답의 sellable_qty 부족 코드) 발생 빈도.
- Rollback:
  - `READY` order 생성 전 중단 — evaluate 단계에서 사전 점검 실패 시 즉시 `READY` 생성 차단 + 응답 라벨 기록 + Step Functions execution history audit.
  - 잘못 생성된 `READY` order 는 broker 호출 전이면 `connector_order_request` 생성 전 단계이므로 DB status 변경(예: `BLOCKED` 또는 `CANCELLED`) 으로 차단 가능 / broker 측 호출이 발생한 경우 KIS 측 즉시 취소 + 운영자 직접 정리(R-AUTO-001 / R-AUTO-002 / R-BROKER-004 정합).
  - 실주문 테스트 진입 시점 결정은 OD-MS-036 의 운영 가능 상태 + 다음 보유 포지션 발생 시점 + 운영자 별도 승인 후 / 무조건적인 실주문 검증 진입은 본 spec 안전 제약 위반.

### R-DATA-008 Details

- Current status (R-DATA-008): `Open` — Strategy Common 정식 package 관리는 후속 spec 책임으로 분리 유지.
- R-DATA-008 · Impact Medium · Probability Medium · Affected Spec 09, 07, 10.
- Mitigation history:
  - OD-MS-018 결정에 따라 Research 내부 adapter 는 `port_strategy_common` 호출만 수행하고 자체 판단 로직을 넣지 않는다(adapter 본문 정합 점검은 09 spec operation-notes 의 §2 정합).
  - `port_strategy_common` 계약 변경 시 (a) Research py_compile / import smoke / small sample test 필수, (b) Daily Decision 결과와 Research backtest sample 결과 비교, (c) 장기적으로 adapter 를 `port_strategy_common` 정식 package 로 정식 이동(OD-MS-005 / OD-MS-014 후속 정합).
  - adapter 신규 / 변경 시점은 09 spec operation-notes 일자별 누적에 사실 기록(본문 전체 인용 0건). `port_strategy_decision` 의 `backtest_market` / `backtest_filter` / `backtest_sizing` 도 같은 common 호출 패턴을 유지해야 Research adapter 와 결과 정합성 유지 가능 — Decision MS 측 변경 시점도 함께 검토 필요.
  - [2026-06-15 보강] Strategy Common 1차 정합성 확인 절차를 정식 mitigation 루틴으로 채택 — common 주요 모듈 + Research adapter 3개 `python -m py_compile` 통과 + Decision · Execution · Research 각 MS 의 common import smoke 통과(09 spec operation-notes 2026-06-15 §9 정합).
  - 표현은 "Strategy Common 1차 정합성 확인 완료 / 정식 package 관리는 후속" 으로 통일하고, adapter 의 common package 정식 이동은 OD-MS-005 / OD-MS-014 / OD-MS-018 후속 분리 유지. smoke / sample 비교 절차의 정식 문서화는 후속(09 spec 후속 phase 책임).
- Detection:
  - Research py_compile / import 시점의 ImportError / AttributeError 패턴 / common config key missing error / dataclass 필드 mismatch 패턴
    - Daily Decision daily 실행 결과(decision.strategy_daily_run / strategy_daily_signal) 와 Research backtest sample 결과(research.strategy_backtest_run
    - strategy_backtest_daily) 비교 시 reason 문자열 / signal 값 불일치 / 동일 입력 feature 에 대해 Decision 과 Research 가 서로 다른 sizing 결과 출력하는 패턴.
  - [2026-06-15 보강] Strategy Common 1차 정합성 확인 절차의 통과 여부(common 주요 모듈 + Research adapter 3개 py_compile + 각 MS common import smoke) 를 정기 detection 항목으로 추가 — common 변경 시 Research image rebuild 직전 단계에 동일 절차 재수행 / 실패 시 image rebuild 보류 / smoke / sample 비교 절차 정식 문서화 후속 시점에 detection 강화.
- Rollback: adapter 계약 변경으로 backtest 결과 회귀 발견 시 이전 adapter 리비전으로 py_compile 통과분 rollback + common 변경분과의 정합성 재검증 + Research image rebuild 보류. Daily Decision 과 Research 결과가 어긋난 사례는 09 spec / 07 spec operation-notes 에 사실 기록 후 재현 조건 정리.

### R-DATA-012 Details

- Current status (R-DATA-012): `Mitigated` -- 상세 이력은 아래 참조.
- R-DATA-012 · Impact High · Probability Medium · Affected Spec 04, 10.
- Mitigation history:
  - (a) `execution_sync_buy_position.py` patch — 동일 `account_id` / `ticker_code` / OPEN status 기준 기존 position 조회를 신규 INSERT 직전에 수행.
  - (b) 기존 OPEN position 이 존재하면 신규 INSERT 대신 `merge_open_position_state()` 호출 — quantity 단순 합산 / entry_price 가중평균 / status `OPEN` 유지.
  - (c) `buy_info.additional_buys` 에 추가매수 이력 누적(`execution_order_id` / `connector_order_request_id` / 매수 일자 / 수량 / 단가).
  - (d) idempotency 체크 — 같은 `execution_order_id` 또는 `connector_order_request_id` 가 이미 `additional_buys` 에 존재하면 다시 누적하지 않음 / 같은 fill 이 두 번 sync 되어도 중복 누적 0건.
  - (e) 기존 OPEN position 부재 시에만 신규 INSERT 진행.
  - (f) Docker rebuild 후 ECR push + ECS task 재실행으로 patch 반영. 본 결정은 OD-MS-024 신규 정합.
  - [2026-06-18 1차 실증]
    - 본 일자 Step 16 1차 시도에서 추가매수 케이스(`004990` / `003490`) 에 대해 unique constraint 충돌 발견 → 운영자 직접 patch + Docker rebuild + ECR push + ECS 재실행 → exitCode 0 / Step 16 SUCCESS
    - 추가매수 2건(004990 65→69주 id `7` / 003490 52→58주 id `8`) merge 통과 + 신규 OPEN 2건(023530 8주 id `11` / 042660 11주 id `12`)
  - INSERT 통과 1차 실증
- Detection:
  - `strategy.strategy_execution_order` 가 FILLED 인데 같은 종목 OPEN `strategy_position_state` 의 quantity 가 갱신되지 않은 패턴
    - Step 16 ECS task 의 stderr 에 `duplicate key value violates unique constraint` 패턴
    - `strategy_position_state.buy_info` JSON 의 `additional_buys` 길이 vs 동일 ticker 의 FILLED execution_order 개수 비교
  - SQL / 같은 `execution_order_id` 가 두 번 누적된 패턴(idempotency 회귀) / wrapper Step 16 stdout 의 `position_sync_result` 누락 또는 SUCCESS 라벨 부재
- Rollback:
  - 운영자 직접 patch 후 Docker rebuild + ECR push + ECS task 재실행. patch 직후 `strategy_position_state` row 의 quantity / entry_price / additional_buys 정합성을 운영자 직접 SQL 점검(영향 종목 별 매수 합계 vs 실제 holding). idempotency 회귀가 발견되면 영향 row 의 `additional_buys` 중복 항목 운영자 직접 정리.
  - patch 자체는 04 spec operation-notes 에 사실 기록(본문 전체 인용 0건 / R-DOCS-001 정합) / 정식 commit + 07 spec CI/CD 연동은 후속(followups-overview 2026-06-18). SELL closing 흐름 / 부분 청산 / 전량 청산 시 `additional_buys` 처리 방식은 별도 검증 후속(OD-MS-024 정합)

### R-DATA-014 Details

- Current status (R-DATA-014): `Mitigated` -- 상세 이력은 아래 참조.
- R-DATA-014 · Impact High · Probability Medium · Affected Spec 03, 04, 10.
- Mitigation history:
  - (a) **1단계 refresh `as_of_ts` 검증** — `connector_balance_snapshot` · `connector_position_snapshot` 의 `as_of_ts` 가 1단계 진입 시점 기준 acceptable window(예: 직전 10분 이내) 안에 있는지 1단계 종료 직후 검증 / 실패 시 1단계 결과 응답에 `stale_snapshot=true` 라벨 기록.
  - (b) **2단계 evaluate 진입 전 snapshot 최신성 검증** — 신규 evaluate 파일 안에서 1단계 refresh 시점과의 lag 검증 / acceptable window 초과 시 2단계 evaluate 진입 차단 + Slack 알림 또는 응답 라벨 기록 / `READY` order 생성 0건.
  - (c) Lambda · SSM 응답 timing audit — 1단계 Lambda → SSM → MarketConnector EC2 → `connector_*_snapshot` insert 까지의 timestamp 비교(`as_of_ts` · `created_at` · SSM `requestedAt` · Lambda `request_id`) / acceptable window 초과 회차의 운영 회차 누적 결과를 followups 후속 phase 에서 점검.
  - (d) 1단계 SSM RunCommand 실패 시 fail-closed — 2단계 진입 차단 / Slack `INTRADAY_REFRESH_FAILED`(또는 동등 eventType) 알림은 후속 phase / 단기적으로는 운영자가 CloudWatch Logs + Step Functions execution history 직접 점검.
  - (e) acceptable window 의 정확한 값은 구현 시점에 결정 / 초기 후보는 직전 1단계 invocation 시점 + 5분 이내 / 운영 회차 누적 후 조정. 본 mitigation 의 detection 은 OD-MS-035 의 안전장치 4종(stale snapshot · duplicate order · `sellable_qty` · `current_price` 검증) 중 첫 번째 항목과 동일.
- Detection:
  - `connector_balance_snapshot` · `connector_position_snapshot` 의 `as_of_ts` vs 2단계 evaluate 실행 시점 lag SQL 점검 / 1단계 Lambda CloudWatch Logs 의 invocation 누락 · timeout 패턴
    - SSM RunCommand 응답의 `ResponseCode != 0` 패턴 / 신규 evaluate 파일 응답에서 `stale_snapshot=true` 라벨 / `strategy_intraday_position_check` row 의 `
  - evaluated_at` vs `as_of_ts` 차이 / 후속 phase 의 운영 회차 누적 결과 audit.
- Rollback:
  - 1단계 refresh 실패 시 운영자가 직접 MarketConnector EC2 의 SSM RunCommand 또는 venv python 으로 snapshot refresh 수동 재실행 + 2단계 evaluate 직접 재실행. 잘못된 `READY` order 가 이미 생성된 경우 → `INTRADAY_STOP_SELL` `READY` row 운영자 직접 SQL 점검 + 3단계 Submit & Refresh 진입 전 운영자 승인 시점에 stale 여부 확인 + 필요 시 `READY` row 운영자 직접 SQL 취소.
  - 본 mitigation 의 acceptable window 가 운영 회차 누적으로 부적절함이 식별되면 OD-MS-035 후속 결정으로 조정.

### R-AUTO-024 Details

- Current status (R-AUTO-024): `Accepted` -- 상세 이력은 아래 참조.
- R-AUTO-024 · Impact Medium · Probability Medium · Affected Spec 04, 06, 10.
- Mitigation history:
  - (a) **현재 환경변수는 1차 검증용 한정** — Lambda 환경변수 `SLACK_WEBHOOK_URL` 은 smoke test / template test / Step Functions 3종 Slack 수신 검증의 1차 검증 단계에서만 사용.
  - (b) 운영 안정화 후 Secrets Manager 또는 SSM Parameter Store(SecureString) 로 이전 예정 — 이전 시점에 Lambda 코드는 `secretsmanager:GetSecretValue` 또는 `ssm:GetParameter` 호출로 webhook URL 을 runtime 에 가져오도록 변경
    - IAM Role(`portfolio-event-notifier-lambda-role`) 에 secret read 권한 추가(Resource ARN 한정 / wildcard 0건 / OD-SEC-006 정합).
  - (c) **본 문서 / 운영자 노트 / Slack 메시지 본문 / Lambda 코드 본문 / Step Functions execution history 본문 평문 webhook URL 기록 0건**(R-DOCS-001 정합 / 모두 `[REDACTED]` 또는 placeholder).
  - (d) Slack notifier Lambda 의 환경변수 값은 운영자가 직접 설정 / Kiro 는 환경변수 key 이름과 의미만 기록 — value 평문 기록 0건.
  - (e) 1차 검증 단계 동안 webhook URL 노출 표면 최소화 — Lambda 콘솔 접근 권한 / `aws lambda get-function-configuration` 호출 권한 / CloudWatch Logs / Step Functions execution history 접근 권한이 운영자 portadmin 한정 유지(R-SEC-001 / R-SEC-002 / R-SEC-003 정합).
  - (f) Secrets Manager 이전 시 자동 rotation 정책 검토 — Slack workspace 측 webhook URL 갱신 절차와 결합.
  - [2026-06-23 1차 검증] 본 일자 환경변수 `SLACK_WEBHOOK_URL` 기반 1차 검증 통과(`SLACK_LAMBDA_SMOKE_TEST=SUCCESS` / `PORTFOLIO_EVENT_NOTIFIER_TEMPLATE_TEST=SUCCESS` / 3종 Slack 수신 검증 통과 / webhook URL 평문 기록 0건) — Secrets Manager 또는 SSM Parameter Store 이전은 후속 phase(followups-overview 2026-06-23).
- Detection: Lambda 콘솔 접근 시 환경변수 노출 여부 audit / `aws lambda get-function-configuration` 응답에 webhook URL 평문 포함 여부 / CloudFormation · SAM · Terraform state 파일의 webhook URL 평문 포함 여부 / git history 의 webhook URL 평문 grep(R-DOCS-001 결합) / Slack 채널 부정 알림 발송 흔적.
- Rollback:
  - 평문 노출 발견 시 즉시 webhook URL rotate(Slack workspace 측에서 새 webhook URL 발급 + 기존 webhook URL revoke). Lambda 환경변수 갱신 + 영향 git history / 노트 / 로그 정리(R-DOCS-001 정합). Secrets Manager 또는 SSM Parameter Store 이전을 우선순위 상향.
  - 본 mitigation 의 환경변수 단계 운영은 1차 검증용 한정이며 운영 안정화 후 즉시 이전 — 이전 시점은 followups-overview 2026-06-23 후속 phase 책임.

### R-AUTO-014 Details

- Current status (R-AUTO-014): `Open` -- 상세 이력은 아래 참조.
- R-AUTO-014 · Impact High · Probability Medium · Affected Spec 04, 05, 10.
- Mitigation history:
  - OD-MS-017 결정에 따라 단일 Task Definition `portfolio-paper-strategy-execution` revision 1 ACTIVE 의 default command 는 안전한 `py_compile` 계열로 유지하고, 7개 entrypoint 는 RunTask `--overrides` 의 `containerOverrides[].command` 로만 선택 실행.
  - 추가 mitigation: (a) Step Functions state name 과 command override 의 1:1 매핑 표를 04 spec 후속 phase 산출물(state machine definition 또는 별도 매핑 표)로 정식 정리, (b) command override 는 7종 entrypoint allowlist(`daily_buy_execution_run`
    - `daily_sell_execution_run` / `daily_auto_buy_execute_run` / `daily_auto_sell_execut
  - e_run` / `execution_sync_buy_fill` / `execution_sync_sell_fill` / `execution_sync_buy_position`) 기반으로만 생성, (c) BUY / SELL / fill sync / position 변경 계열은 자동 retry 금지(OD-SAFE-004
    - R-AUTO-001 정합), (d) View 또는 Step Functions 호출부에서 임의 command 입력 금지(allowlist 외 입력 거부), (e) MarketConnector executor `connec
  - tor_strategy_order_execute.py --execute` 는 본 Task Definition 에 포함하지 않으며 03 spec EC2 측 별도 책임으로 유지(OD-MS-016 정합).
  - 본 일자(2026-06-13) RunTask 단건 실행은 운영자가 직접 command override 를 입력했고 broker / KIS 호출 0건 / `connector_order_request` 생성 0건 / `READY -> REQUESTED` 실제 전환 0건으로 1차 mitigation 검증
- Detection:
  - CloudWatch Logs 의 Task 시작 banner 와 Step Functions state name 비교 / ECS RunTask `overrides` audit (CloudTrail `RunTask` event 의 `overrides.containerOverrides` 필드)
    - `execution.strategy_execution_order` / `connector.connector_order_request` row count 변화 검증 / 의도하지 않은 entrypoint 가 실행되어 `READY -> REQUESTE
  - D` 또는 `connector_order_request` insert 가 발생하는 패턴 즉시 식별 / Step Functions execution history 의 task input / output 정합성 점검
- Rollback:
  - 잘못 실행된 state machine execution 즉시 stop(Step Functions `StopExecution`) / 잘못 실행된 ECS RunTask 는 `StopTask` 로 즉시 종료 / `connector_order_request`
    - `strategy_execution_order` 변경분은 운영자 직접 SQL 점검(02 spec [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md)
  - §5 검증 SQL 패턴 정합).
  - 잘못 생성된 `REQUESTED` 행은 운영자 승인 후 취소 / 보정 — broker 측 호출이 발생했다면 KIS 측 취소 + 수동 정리(R-AUTO-001 / R-AUTO-002 정합). 보정 결과는 본 spec operation-notes 에 누적 기록
- Evidence links: [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md).

### R-AUTO-012 Details

- Current status (R-AUTO-012): `Mitigated` -- 상세 이력은 아래 참조.
- R-AUTO-012 · Impact Medium · Probability Medium · Affected Spec 02, 03, 04.
- Mitigation history:
  - OD-NET-010(SSM Port Forwarding 표준 경유지 = `portfolio-paper-marketconnector-ec2`) 결정에 따라 tunnel 유지 조건을 [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 연결 검증 + Runbook §10.2 / §10.7 에 명시(tunnel PowerShel
  - l / 터미널 창 종료 금지 / 추가 작업은 별도 창 / 실패 시 SSM Plugin / EC2 SSM Online / IAM Role / VPC Endpoint 5종 점검 후 재시도).
  - target EC2(`portfolio-paper-marketconnector-ec2`) 가 정식 운영 EC2 와 동일하므로 EC2 stop 정책 변경 시 본 리스크 즉시 영향 — 03 spec / 02 spec 변경 검토 시 본 리스크 재확인. SSM Port Forwarding session 자동 keep-alive / reconnect 는 후속 분리(`_common/followups-overview.md` 2026-06-13 SSM Port Forwarding 후속 메모).
  - EventBridge Scheduler / Step Functions 정기 트리거 / aws 측 ECS / Fargate 실행 흐름은 SSM tunnel 에 의존하지 않으므로 본 리스크는 운영자 로컬 검증 / 디버깅 흐름에 한정.
  - [2026-06-13 보강] pgAdmin4(OD-NET-011) 도 같은 SSM tunnel 위에서 동작하므로 tunnel 종료 시 pgAdmin4 connection 도 즉시 단절. tunnel 창 종료 / `Ctrl + C` 종료 시 pgAdmin4 / psql 18 / Python `psycopg2` 모두 즉시 영향 — 운영자가 별도 창에서 작업하는 동안 본 tunnel 창을 임의 종료하지 않도록 운영 노트에 명시
- Detection:
  - tunnel 창의 `Waiting for connections...` 메시지 유지 여부 / 로컬 `psycopg2` 접속 timeout 패턴 / `aws ssm describe-instance-information` 의 ping status `Online` 유지 / Session Manager Plugin 출력 / 운영자 PowerShell 의 tunnel 창 정상 동작 점검.
  - [2026-06-13 보강] pgAdmin4 의 `Connection terminated` / `server closed the connection unexpectedly` 패턴 / psql 18 의 `connection to server at "localhost"` timeout 패턴 / SSM session 의 `Connection accepted for session [...]` 메시지 부재 모니터
- Rollback:
  - 재기동 — 같은 명령(`aws ssm start-session --target i-0fce77927b7397b88 --document-name AWS-StartPortForwardingSessionToRemoteHost --parameters ...`) 으로 새 session 즉시 재오픈. 새 session id 가 발급되며 local port `15433` 동일하게 재사용.
  - target EC2 가 stop 상태이면 02 / 03 spec 운영자 절차로 EC2 start 후 재시도(EC2 신규 생성 / 재생성은 본 리스크 mitigation 범위 밖).
  - [2026-06-13 보강] tunnel 재기동 후 pgAdmin4 / psql 18 은 별도 재접속 필요 없이 자동 복구 가능(SSM session id 만 새로 발급, local port `15433` 재사용)
- Evidence links: [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md).

### R-BROKER-004 Details

- Current status (R-BROKER-004): `Mitigated` -- 상세 이력은 아래 참조.
- R-BROKER-004 · Impact High · Probability Medium · Affected Spec 03, 04, 10.
- Mitigation history:
  - (a) timeout 발견 시 즉시 단순 재실행 금지 — `connector.connector_order_request` / `connector.connector_api_call_log` / `broker_order_no` 존재 여부를 사전 점검(같은 strategy_execution_order_id 또는 idempotency_key 로 broker 응답이 이미 도달했는지 확인).
  - (b) broker_order_no 가 부재한 경우에만 통제된 REQUESTED 복구(`strategy_execution_order` 를 SUBMITTED 직전 상태인 REQUESTED 로 운영자 직접 SQL 보정 / wrapper 우회 금지).
  - (c) broker_order_no 가 존재하면 재실행 금지 / 단건 KIS `inquire-daily-ccld` 조회로 체결 동기화만 진행(R-AUTO-018 mitigation 정합).
  - (d) 재시도 정책 명문화 — 본 spec 후속 phase 또는 03 spec operation-notes 에 timeout 복구 절차 정식 기재.
  - (e) Step 12 `-AllowPaperOrderExecute` 옵션과 함께 사용되므로 R-AUTO-019 mitigation 과 결합.
  - [2026-06-18 1차 실증] 본 일자 Step 12 1차 시도에서 KIS paper API read timeout / `connector_order_request 38 ~ 41` FAILED / `strategy_execution_order 30 ~ 33` FAILED 발생. 네트워크 / DNS / TCP / HTTPS 기본 연결은 정상 확인되어 KIS paper endpoint 일시 지연성 장애로 판단.
  - broker_order_no 부재 + 사전 점검 통과 후 통제된 REQUESTED 복구 → 재시도 KIS paper BUY 4건 제출 성공(`connector_order_request 42 ~ 45` ACCEPTED / broker_order_no 4종) → 중복 주문 0건 1차 실증
- Detection:
  - `connector.connector_api_call_log` 의 `response_status` / 응답 timeout 패턴(503 / 504 / read timeout / 게이트웨이 무응답)
    - `connector.connector_order_request` 의 짧은 시간 내 동일 strategy_execution_order_id 에 대한 다중 row 패턴 / `broker_order_no` null + FAILED status 조합
    - wrapper Step 12 stdout 의 `KIS paper API timeout` 패턴
  - 재시도 직후 broker 측 `inquire-daily-ccld` 응답에서 같은 종목·수량의 주문이 2건 이상 발견되면 즉시 식별 / connector_order_request 와 broker_order_no 매핑 정합성 점검
- Rollback:
  - 잘못 제출된 broker 주문이 발견되면 KIS 측 즉시 취소 + 운영자 직접 정리(R-AUTO-001 / R-AUTO-002 정합) / `strategy_execution_order` / `connector_order_request` / `connector_order_event` / `connector_fill` 영향 row 운영자 직접 SQL 점검 후 보정 / 보정 결과는 03 spec operation-notes 2026-06-18 §2 정합 형식으로 누적 기록.
  - 단순 재실행으로 복구하지 않고 broker 측 응답 도달 여부 사전 점검을 강제하는 흐름이 무너지지 않도록 wrapper / executor / 운영 절차서 동시 점검

### R-DATA-011 Details

- Current status (R-DATA-011): `Mitigated` -- 상세 이력은 아래 참조.
- R-DATA-011 · Impact Medium · Probability Medium · Affected Spec 02, 03, 06.
- Mitigation history:
  - (a) `marketconnector_app` 의 database search_path 를 `connector, execution, legacy, reference, public` 로 보정(`ALTER ROLE marketconnector_app IN DATABASE portfolio SET search_path = ...`), (b) `legacy` schema USAGE GRANT + `legacy.holdings` SELECT
    - INSERT / UPDATE / DELETE GRANT(legacy 운영 데이터 갱신에 필요한 최
  - 소 권한 한정 / OD-DB-007 정책의 예외 1건), (c) `legacy` schema 의 sequence GRANT 검증, (d) future default privileges 보정(`ALTER DEFAULT PRIVILEGES IN SCHEMA legacy GRANT ...`)
    - 후속 객체 신규 생성 시 자동 적용, (e) bare table name 의존 legacy 경로(예: `holdings` / `holdings_history`)는 search_path 보정으로 처리 / 코드 변경(8개 MS 미수정 정책 정합) 0건.
  - [2026-06-17 1차 실증] 본 일자 17-step E2E §4 에서 BALANCE_REFRESH 1차 실패 → 운영자 직접 search_path 보정 + USAGE / DML / sequence GRANT + default privileges 보정 후 SSM 재실행 `Status Success` / `ResponseCode 0` / `connector_position_snapshot` 4종목 최신 생성 통과(`position_snapshot_id 120 ~ 123`).
  - 02 spec db-roles-and-grants 정식 매트릭스 갱신은 후속(R-DATA-005 정합)으로 유지.
  - [2026-06-17 보강] 관련 정합성 근거를 operation-notes에 누적.
- Detection:
  - SSM RunCommand 응답의 `relation "holdings" does not exist` 패턴 / `current_setting('search_path')` 출력으로 marketconnector_app 의 search_path 정합성 점검
    - `connector_position_snapshot` 신규 row 0건 패턴(BALANCE_REFRESH 직후) / `pg_has_role` / `has_schema_privilege` / `has_table_privilege` 결과 비교 SQL / legacy schema 의 US
  - AGE / DML 권한 변경 audit(CloudTrail RDS API event 또는 운영자 GRANT 명령 로그 / 02 spec operation-notes 누적)
- Rollback:
  - 운영자 직접 search_path 보정 + 누락 GRANT 보정 후 SSM RunCommand 로 BALANCE_REFRESH 재실행 → `connector_position_snapshot` 신규 row 생성 확인 → 잘못 생성된 보유 종목 / position state row 가 있다면 운영자 직접 정리. 보정 결과는 02 spec / 06 spec operation-notes 에 누적 기록.
  - legacy schema 의 USAGE / DML 권한이 광역으로 확장된 경우(예: 다른 app role 에 추가 GRANT) 즉시 회수(REVOKE) 후 영향 범위 점검

### R-AUTO-017 Details

- Current status (R-AUTO-017): `Mitigated` -- 상세 이력은 아래 참조.
- R-AUTO-017 · Impact Low · Probability Medium · Affected Spec 08.
- Mitigation history:
  - OD-MS-022 정합으로 (a) wrapper 종료 시점에 Chrome process 정리 옵션(예: `Stop-Process -Name chrome -Force` 또는 selenium driver `quit()` 보장) 검토 — 본 일자 시점에는 정리 옵션 미적용 / 잔존 가능성 인지
    - 후속 분리(task 92), (b) 다음 실행 전 wrapper pre-check 단계에서 잔존 Chrome process 정리 후 진입 — 후속 적용 시 검토, (c) EC2 stop / restart 정책으로 idle 비용 절감과 함께 잔존
  - process 자동 정리 효과 확보(task 59 와 결합), (d) 본 일자 KRX login 성공 / 적재 통과로 미치는 영향이 작아 Impact `Low` 분류 — 다만 누적되면 Probability `Medium` 으로 잔존 가능.
  - [2026-06-16 1차 관측] wrapper 종료 후 Python process 종료는 확인 / Chrome process 잔존은 KRX debug attach 구조상 잔존 가능 / 후속 정리 옵션 분리.
  - [2026-06-21 보강] `step-02-interest-crawler.ps1` 가 KRX worker 실행 직전 Chrome / chromedriver stale process best-effort reset 을 수행하도록 변경됐다(OD-MS-026 신규 / R-AUTO-020 신규 mitigation 정합 / task 92 / task 103 1차 완료).
  - reset 실패는 즉시 중단하지 않고 warning 로그만 남기고 진행 — 다음 KRX worker 실행 직전에 chrome / chromedriver process 잔존이 있으면 정리 시도 후 진입 / 정리 실패 시에도 Step 2 진행은 차단되지 않지만 wrapper 로그에 warning 라벨 남음.
  - 본 변경은 EC2 stop / restart 정책 도입 전 단계의 wrapper 측 1차 차단이며, EC2 stop / restart lifecycle 자동화는 R-AUTO-016 mitigation 갱신 + task 59 후속과 결합 유지. Status `Mitigated` 갱신(verified on 2026-06-21 Step 2 단독 실행 검증)
- Detection:
  - `Get-Process chrome` 결과 비정상 다수 / EC2 메모리 사용량 점진 증가 / 다음 실행 시 KRX login 단계의 Selenium WebDriver attach 실패 / chrome.exe profile lock 에러 / wrapper 로그의 Selenium 초기화 단계 timeout / EC2 CloudWatch agent 의 메모리 metric spike.
  - [2026-06-21 보강] wrapper Step 2 stdout 의 chrome / chromedriver reset 라벨 / reset 실패 warning 빈도 audit / Step 2 SUCCESS 직후의 chrome process 잔존 여부 점검(`Get-Process chrome` SSM RunCommand 또는 운영자 RDP 진입 시)
- Rollback:
  - `Stop-Process -Name chrome -Force` 으로 즉시 정리 또는 EC2 재부팅(Autologon bootstrap 으로 자동 복귀) → 다음 KRX worker 실행 → KRX login 성공 / 적재 확인. 만성화 시 wrapper 종료 단계에 Chrome 정리 step 추가 적용(task 92 후속).
  - [2026-06-21 보강] wrapper 측 best-effort reset 도입 후에도 EC2 메모리 누수 패턴이 지속되면 EC2 stop / restart lifecycle 자동화(task 59) 후속 책임으로 분리

### R-DATA-016 Details

- Current status (R-DATA-016): `Mitigated` — OD-MS-035 안전장치 4종의 `current_price` 검증 항목 정합.
- R-DATA-016 · Impact High · Probability Medium · Affected Spec 03, 04, 10.
- Mitigation history:
  - (a) **1단계 refresh 시 `current_price` null · 0 · 과거 timestamp 검증** — MarketConnector 측 신규 snapshot refresh 흐름에서 broker `current_price` 응답이 null · 0 · 음수
    - 직전 회차와 동일한 stale 값일 경우 `connector_position_snapshot` 의 `current_price` 컬럼에 null 또는 sentinel 값으로 저장 / `as_of_ts` 만 갱신하고 `current_price` 는 보존하지 않는 정책 검토 / 구현 시점에 03 spec operation-notes 사실 기록.
  - (b) **2단계 evaluate 진입 전 `current_price` 정합성 검증** — 신규 evaluate 파일 안에서 snapshot 의 `current_price` 가 null · 0 · 직전 회차와 동일 timestamp 인 경우 evaluate 진입 차단 + 응답 라벨 기록(`reason=INVALID_CURRENT_PRICE`) + `READY` order 생성 0건.
  - (c) **`current_price` 범위 sanity check** — 종목별 직전 종가 또는 직전 1단계 회차 대비 ±X% 범위 안에 있는지 확인 / 범위 초과 시 evaluate 진입 차단 + Slack 알림 또는 응답 라벨 기록. X 의 정확한 값은 구현 시점에 결정 / 초기 후보는 ±10% 또는 ±15%.
  - (d) **broker `current_price` 조회 실패 fail-closed** — 1단계 SSM RunCommand 응답 `ResponseCode != 0` 인 경우 2단계 진입 차단 / 운영자가 CloudWatch Logs + Step Functions execution history 직접 점검. 본 mitigation 의 detection 은 OD-MS-035 의 안전장치 4종 중 `current_price` 검증 항목과 동일.
- Detection:
  - `connector_position_snapshot.current_price` 가 null · 0 · 음수
  - 직전 회차 동일값 row 의 빈도 SQL 점검 / evaluate 파일 응답의 `reason=INVALID_CURRENT_PRICE` 라벨 빈도 / 1단계 SSM RunCommand 응답의 `ResponseCode != 0` 패턴 / `current_price` 가 종목별 직전 종가 대비 sanity check 범위를 초과하는 row 빈도 / 3단계 broker 응답의 매도가격 오차 관련 REJECTED 발생 빈도.
- Rollback:
  - 잘못된 `current_price` 기반 `READY` order 가 생성된 경우 운영자가 직접 SQL 로 row 점검 + 3단계 Submit & Refresh 진입 전 운영자 승인 시점에 stale 여부 확인 + 필요 시 `READY` row 운영자 직접 SQL 취소. broker `current_price` 조회 실패 빈도가 높으면 KIS API 측 별도 endpoint 사용 검토 또는 1단계 refresh 주기 조정.
  - 본 mitigation 의 sanity check 범위가 운영 회차 누적으로 부적절함이 식별되면 OD-MS-035 후속 결정으로 조정.

### R-DATA-015 Details

- Current status (R-DATA-015): `Mitigated` -- 상세 이력은 아래 참조.
- R-DATA-015 · Impact High · Probability Medium · Affected Spec 03, 04, 10.
- Mitigation history:
  - (a) **2단계 evaluate 단계 `sellable_qty` 사전 비교** — 신규 evaluate 파일 안에서 `connector_position_snapshot.sellable_qty` 와 손절 후보 수량 비교 / `sellable_qty == 0` 또는 `sellable_qty < candidate_qty` 인 경우 `READY` order 생성 차단 + 응답 라벨 기록(`reason=INSUFFICIENT_SELLABLE_QTY`).
  - (b) **기존 진행 중 SELL 흐름 회피** — `strategy_execution_order` 의 동일 종목 · `action_type=SELL` · `execution_status IN (READY, REQUESTED, SUBMITTED, PARTIAL_FILLED)` row 가 이미 존재하면 Intraday `READY` 생성 차단 / Daily SELL 흐름과 충돌 회피(OD-MS-016 책임 분리 정합).
  - (c) **3단계 Submit & Refresh 진입 전 `sellable_qty` 재검증** — Submit & Refresh state machine 의 첫 task 에서 최신 snapshot 의 `sellable_qty` 재조회 후 broker 제출 전 사전 비교 / 부족 시 제출 차단 + Slack 알림.
  - (d) **부분체결 후 단건 order-check 재조회 패턴 재사용** — 3단계 Submit & Refresh 의 체결조회 step 은 OD-MS-025 의 `--code` · `--order-no` · `--no-broad` 단건 direct-only 조회 기본 사용(R-AUTO-018 mitigation 정합). 본 mitigation 의 detection 은 OD-MS-035 의 안전장치 4종 중 `sellable_qty` 검증 항목과 동일.
- Detection:
  - `connector_position_snapshot.sellable_qty` 와 `strategy_intraday_position_check.candidate_qty` 비교 SQL / `strategy_execution_order` 의 `action_type=SELL` 동시 진행 row 점검
    - 신규 evaluate 파일 응답의 `reason=INSUFFICIENT_SELLABLE_QTY` 라벨 빈도 / 3단계 Submit & Refresh 의 broker 응답 `rejection_code` 가 매도가능수량 부족 관련(예: `402
  - 40000` 또는 동등 코드) 발생 빈도 / 부분체결 → 단건 재조회 패턴이 Daily SELL 흐름과 Intraday 흐름에서 모두 동작하는지 audit.
- Rollback:
  - broker 측 REJECTED 발생 시 운영자가 직접 `strategy_execution_order` 상태 점검 + 영향 종목 보유 수량 + 매도가능수량 재조회. 부분체결로 끝난 경우 → 단건 order-check 재조회로 `tot_ccld_qty` 확인 + 잔여 수량 처리 결정(추가 손절 시도 또는 운영자 직접 정리).
  - 본 mitigation 의 사전 비교가 evaluate 파일에서 작동하지 않은 사례가 식별되면 안전장치 4종 검증 로직을 신규 evaluate 파일 안에서 강화 + 03 spec operation-notes 사실 기록.

### R-COST-003 Details

- Current status (R-COST-003): `Open` -- 상세 이력은 아래 참조.
- R-COST-003 · Impact Medium · Probability Low · Affected Spec 09, 06, 10.
- Mitigation history:
  - OD-MS-019(Research AWS Batch 포팅 대상 + report artifact S3 prefix `strategy-research/reports/{YYYYMMDD}/{AWS_BATCH_JOB_ID}/`) 결정 정합으로 (a) Job Role 의 `s3:PutObject` Resource 를 `arn:aws:s3:::portfolio-paper-migration-yukiever/strategy-research/reports/*` 로 한정(wildcard 0건
    - public read 0건 / 09 spec operat
  - ion-notes 2026-06-15 §12 정합), (b) report wrapper 는 `REPORT_S3_BUCKET` 환경변수 미설정 시 업로드 skip / 설정 시에만 업로드(기능 회귀 없음
    - 09 spec operation-notes 2026-06-15 §14 정합), (c) `REPORT_S3_PREFIX` 기본값 `strategy-research/reports` 와 `AWS_BATCH_JOB_ID` 기준 하위 경로 분리로 prefix 오염 차단, (d) `REPORT_OUTPUT_DIR=/tmp/portfolio-r
  - eports`(Fargate ephemeral 영역) 기본값 유지 — 컨테이너 종료 시 자동 정리, (e) S3 lifecycle 정책(예: `Transition` `STANDARD` → `STANDARD_IA` 30일 / `GLACIER` 90일 / `Expiration` 365일 등) 결정과 KMS encryption 결정은 후속 spec(06 / 09 후속 phase) 책임으로 분리, (f) bucket 자체의 default SSE 사용 / public access block 정책 점검 정기 항목으로 유지
- Detection:
  - S3 storage 사용량 / 객체 수 정기 점검(예: AWS Cost Explorer / S3 storage class breakdown / `strategy-research/reports/` prefix object count 추세)
    - `s3:PutObject` 패턴 audit(CloudTrail data event 또는 S3 server access log) / bucket policy / object ACL 의 public 노출 alarm(Trusted Advisor / S3 public access block 결과) /
  - `strategy-research/reports/` 외 prefix 또는 다른 bucket 으로의 PutObject 패턴 즉시 식별 / Job Role inline policy 변경 audit(CloudTrail `PutRolePolicy`)
- Rollback:
  - S3 lifecycle 정책 도입 후 일부 보존 정책 적용(`Expiration` 또는 `Transition` rule 추가) / 잘못 업로드된 객체는 운영자 직접 정리(`aws s3 rm` 또는 Console 작업)
    - public 노출이 발생했다면 즉시 ACL 정정(`aws s3api put-object-acl`) + bucket policy 갱신(public read deny) / Job Role 의 PutObject Resource 가 광역으로 확장된 경우 inline policy 즉시 정정 후 09 spec operatio
  - n-notes 에 사실 기록

### R-DATA-007 Details

- Current status (R-DATA-007): `Open` — AWS Paper RDS 단일 source of truth 정책 명문화 유지.
- R-DATA-007 · Impact High · Probability Medium · Affected Spec 02, 03, 04, 05, 10.
- Mitigation history:
  - OD-ENV-006(Paper 환경 source of truth = AWS Paper RDS 단일) / OD-ENV-007(SSM Port Forwarding 기반 로컬→AWS Paper RDS 접속 / RDS Private 유지) / OD-ENV-008(local DB ↔ AWS Paper RDS 간 동기화 미사용) 정책을 명문화.
  - `connector_order_request` / `connector_fill` / `strategy_execution_order` / `strategy_position_state` 테이블의 병합 / 동기화 / dump-restore cross 운영 금지를 [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS 운영 모드 정리 섹션에 명시.
  - 실제 paper 주문 실행 guard 는 `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` 조합으로 판단(MarketConnector executor `connector_strategy_order_execute.py --execute` guard 정합) — DB host 만으로 환경 식별 금지(SSM Port Forwarding 으로 `localhost:15433` 가 실제 AWS Paper RDS 를 가리키는 경우 존재).
  - [2026-06-13 보강] SSM Port Forwarding 1차 실증 검증에서 `localhost:15433` 의 실제 대상이 AWS Paper RDS 임이 `inet_server_addr = 10.0.20.165` / `inet_server_port = 5432` 출력으로 확인됨([`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 연결 검증 + Runbook §8).
    - DB host 만으로 환경 식별이 불가함이 1차 실증되어 mitigation 의 핵심 근거가 됨.
- Detection:
  - application 로그의 `PORT_ENVIRONMENT` / `PORT_DB_TARGET` 출력값 정기 점검. local 과 AWS Paper RDS 양쪽의 `connector_order_request` / `connector_fill` / `strategy_execution_order` / `strategy_position_state` row count + 최근 row id 비교 정기 점검. 어느 한쪽에서만 신규 row 가 증가하는 패턴이 정상이며 양쪽 동시 증가 패턴은 즉시 식별.
  - [2026-06-13 보강] Python `psycopg2` 또는 psql 접속 시 `current_user` / `current_database` / `inet_server_addr` / `inet_server_port` 출력으로 실제 대상 RDS private IP 를 확인해 환경 식별 정합성 점검.
- Rollback: 동기화가 발생한 시점부터 영향받은 row 인벤토리 작성 후 운영자 직접 정리. 잘못된 broker 주문이 발생했다면 broker 측 취소 + 운영자 직접 정리. AWS Paper RDS 를 source of truth 로 두고 local 측 row 는 fixture 에서 분리.
- Evidence links: [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md).

### R-DATA-009 Details

- Current status (R-DATA-009): `Open` — non-GUI raw 최신성 회복 정합 검증 통과 전까지 표현 통일 정책 유지.
- R-DATA-009 · Impact Medium · Probability Medium · Affected Spec 08, 04, 09.
- Mitigation history:
  - "완료" 표기는 실제 데이터 적재 / 최신성 검증까지 확인된 경우에만 사용 정책(OD-MS-020 정합) — Interest Crawler hybrid 1차 구현은 "부분 완료" 로 표현 통일. KRX GUI worker 완료와 Interest Crawler 전체 완료를 혼동하지 않도록 운영 문서 / 보고 / 슬라이드의 표현을 정기 점검. smoke 성공과 daily raw 최신성 성공을 분리해 표기.
  - ECS / Fargate Selenium Chrome smoke 결과(`portfolio-paper-interest-crawler:6`)는 runtime 가용성 보증으로만 해석하고 실제 daily crawler 운영용 Task Definition 분리는 별도(08 spec task 58 / task 72 후속).
  - non-GUI raw 7종(`interest_agency_raw` / `interest_news_raw` / `interest_commodity_raw` / `interest_foreignindex_raw` / `interest_investorflow_raw` / `interest_marketbreadth_raw` / `interest_price_raw`) 의 `MAX(trade_date)` 가 직전 거래일까지 적재되었는지 별도 SQL 점검(08 spec task 73).
  - [2026-06-15 보강] 본 일자 backend AWS E2E dry-run 1차 점검에서 KRX raw 최신일 = 2026-06-12(직전 거래일까지 정상)이지만 non-GUI raw 7종이 직전 거래일까지 적재되지 않은 상태가 식별되어 본 리스크의 1차 실증으로 누적([`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) 2026-06-15 §3).
- Detection:
  - 운영 문서 / 보고 / 슬라이드에서 smoke 통과와 "완료" 를 혼용하는 표현 grep. `interest_*_raw` `MAX(trade_date)` 가 직전 거래일 대비 lag 인 상태에서 하위 흐름(BACKTEST_RESEARCH / BACKTEST_REPORT / DAILY_BUY_SIGNAL / DAILY_POSITION_SIGNAL) 진입 사례 audit.
  - Slack `APPROVAL_REQUIRED` KRX 기준일 표시(R-DATA-017 mitigation 결합) 대비 실제 raw 최신일 불일치.
- Rollback: 잔존 stale raw 로 산출된 결과 발견 시 raw 최신성 회복 → preprocessor 재실행 → 하위 흐름 재진입(R-DATA-010 정합). 운영 문서에 "완료" 로 표기된 항목 중 최신성 검증이 누락된 경우 표현을 "부분 완료" 로 재정정.
- Evidence links: [`../08-interest-crawler-and-preprocessor-ecs/operation-notes.md`](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md).

### R-AUTO-009 Details

- Current status (R-AUTO-009): `Mitigated` -- 상세 이력은 아래 참조.
- R-AUTO-009 · Impact High · Probability Medium · Affected Spec 03, 04, 05.
- Mitigation history:
  - 본 일자(2026-06-13) 검증은 정적(`python -m py_compile`) + dry run(`[NO_TARGET] REQUESTED strategy order 없음`) 까지만 수행. 실제 REQUESTED 주문 row 처리 출력은 미검증.
  - 평일 또는 안전한 테스트 데이터로 `READY -> REQUESTED -> SUBMITTED` end-to-end dry / integration 검증을 후속 분리한다([`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-13 §6 정합). 검증 전까지는 `--execute` 실호출 금지 정책을 운영 노트 / spec 산출물에 명문화.
  - View Daily Batch 의 신규 step `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE`(order 12) 도 검증 전까지는 paper 운영 환경에서 활성화하지 않는다.
  - [2026-06-17 보강]
    - 본 일자 17-step E2E §2 에서 `connector_strategy_order_execute.py` MarketConnector EC2 정식 배포 + venv python 사용 + execution table UPDATE 권한 보정
    - `source_run_id` fallback 패치 후 KIS paper BUY 4건 제출 성공(`execution_order` id `26 ~ 29` SUBMITTED / `connector_order_request` id `34 ~ 37` 생성 / broker_
  - order_no `0000035906` / `0000035912` / `0000035918` / `0000035932`) 로 1차 end-to-end 통과.
  - paper 환경 한정 / aws-live cutover 진입 전 평일 / 안전 테스트 데이터 환경에서 추가 검증 후속 유지
- Detection:
  - dry run 결과(`[NO_TARGET] REQUESTED strategy order 없음`) 와 실제 `--execute` 출력의 패턴 차이 점검. `strategy_execution_order` 의 `READY` / `REQUESTED` / `SUBMITTED` / `FAILED` 상태 전이 row count 일자별 비교. SELL 성공 시 position `SELL_ORDERED` 갱신 결과 정합성 점검.
  - [2026-06-17 보강] `connector_order_request` 의 SUBMITTED row 수 = `execution_order` 의 SUBMITTED row 수 정합 SQL / `connector_order_request_id` ↔ `execution_order` 매핑 정합 / SELL 성공 시 `mark_position_sell_ordered()` 호출 audit(본 일자 SELL 호출 0건이라 0건 정합)
- Rollback: 검증 통과 전까지 `--execute` 실호출 보류. 운영 단계 진입 후 처음 `--execute` 시점에는 운영자 직접 모니터링 + KIS 측 broker 응답 정합성 즉시 점검.
- Evidence links: [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md).

### R-AUTO-011 Details

- Current status (R-AUTO-011): `Mitigated` -- 상세 이력은 아래 참조.
- R-AUTO-011 · Impact Medium · Probability Medium · Affected Spec 04, 05.
- Mitigation history:
  - 본 일자(2026-06-13) 검증은 정적(`.\mvnw.cmd clean compile` 통과) + isNoTarget 문구 / 라벨 추가 + step order 조정까지만 수행. 실제 Daily Batch 실행 / Python 주문 스크립트 실행 / DB / AWS 접근 0건.
  - 운영 직전 평일 / 안전 환경에서 12 ~ 17단계 구간의 end-to-end 검증을 후속으로 분리([`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-13 §6 정합). 검증 전까지는 paper 운영 환경에서 신규 step 활성화 시 운영자 직접 모니터링 + 단계별 로그 점검 정책 유지.
  - [2026-06-17 보강]
    - 본 일자 17-step E2E 에서 12 ~ 17 구간 paper 환경 1차 end-to-end 통과(Step 12 KIS paper BUY 4건 SUBMITTED / Step 13 summary fallback guard 패치 후 broker_order_no 별 단건 조회로 FILLED 동기화
    - Step 14 SELL fill 정상 skip / Step 15 BUY fill sync 통과 / Step 16 strategy_position_state 4건 OPEN 생성 / Step 17 legacy 권
  - 한 보정 후 connector_position_snapshot 4종목 최신) — paper 환경 1차 통과로 mitigation 1차 실증 / live cutover 진입 전 평일 / 안전 데이터 추가 검증 후속 유지
- Detection:
  - Daily Batch 실행 결과의 step 별 종료 코드 / `[NO_TARGET]` 패턴 / `REQUESTED strategy order 없음` 패턴 모니터.
  - step 12(MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE) / step 13(CONNECTOR_ORDER_CHECK) / step 14(SYNC_SELL_FILL) / step 15(SYNC_BUY_FILL) / step 16(SYNC_BUY_POSITION) / step 17(BALANCE_REFRESH) 의 단계별 시간 소요 / 실패 / 재시도 패턴 점검.
  - [2026-06-17 보강] step 별 입력 / 출력 row count 정합 SQL(execution_order ↔ connector_order_request ↔ connector_fill ↔ strategy_position_state ↔ connector_position_snapshot 매핑 일관성 점검)
- Rollback: 신규 step 의 운영 단계 활성화 보류 + 운영자 직접 단일 step 검증 후 활성화 / 활성화 후 실패 시 즉시 step 12 비활성화 + 이전 16단계 형태로 복귀 가능하도록 변경 이전 형상을 git 으로 보존.
- Evidence links: [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md).

### R-SEC-009 Details

- Current status (R-SEC-009): `Open` -- 상세 이력은 아래 참조.
- R-SEC-009 · Impact Medium · Probability Medium · Affected Spec 08, 05, 10.
- Mitigation history:
  - OD-MS-022(KRX GUI crawler 자동 로그인 기반 운영 방식) 정합으로 (a) Autologon 은 paper 전용 Windows worker 한정
    - aws-live cutover 시 별도 결정으로 재검토 — 운영 방식에서 제외 후보 우선, (b) RDP inbound 는 운영자 IP 한정 또는 SSM Session Manager 우선 사용(OD-NET-009 / OD-SEC-007 정합), (c) Administrator password 본 노트 / spec 산출물
    - 콘솔 캡처 / 로그 평문 기록 금지(R-DOC
  - S-001 정합 / `[REDACTED]` 만 사용), (d) 자동 로그인 자격 증명은 운영자 직접 입력 / 문서화 금지, (e) EC2 worker 작업 완료 후 stop 절차로 idle 노출 시간 최소화(task 59 후속), (f) 추후 전용 local user(예: `krxworker`) 분리 검토 — Administrator 직접 사용 대신 최소 권한 user 후보로 운영자 결정.
  - [2026-06-16 1차 실증] 본 일자 Autologon 적용 후 EC2 재부팅 시점에 SSM Online + `query user` Administrator console session Active 확인까지 완료 / Administrator password 본 노트 평문 기록 0건.
- Detection: RDP inbound 노출 여부 정기 audit(SG inbound 3389 광범위 CIDR 존재 시 즉시 식별) / Autologon 관련 registry 값 변경 audit / `HKLM\Software\Microsoft\Windows NT\CurrentVersion\Winlogon\DefaultPassword` 존재 여부 / Administrator 로그인 시각 vs 운영자 RDP 진입 시각 audit / CloudTrail EC2 API event 의 Windows password reset 흐름.
- Rollback: Administrator password 회전 후 Autologon 재설정 / RDP inbound rule 회수 · SSM Session Manager 전환 / Autologon 미사용으로 전환 시 운영 방식 재검토(OD-MS-022 재검토). 침해 흔적 발견 시 EC2 stop → snapshot 보존 → 신규 EC2 재구성.


### R-AUTO-013 Details

- Current status (R-AUTO-013): `Mitigated` -- 상세 이력은 아래 참조.
- R-AUTO-013 · Impact Low · Probability Medium · Affected Spec 02, 03.
- Mitigation history:
  - 본 일자(2026-06-13) PowerShell 에서 `psql` 인식 실패가 1차 발견([`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md) 2026-06-13 Local-to-AWS Paper RDS SSM Port Forwarding 연결 검증 + Runbook §6).
  - 즉시 보정 방향은 Python `psycopg2` 대체 사용(`python -c "import psycopg2; print('psycopg2 OK')"` + 직접 `psycopg2.connect` 사용) — 본 일자 §7 ~ §9 에서 1차 실증 통과.
  - [2026-06-13 보강] 운영자 로컬 PC 의 PostgreSQL 18 client 가 `C:\Program Files\PostgreSQL\18\bin\psql.exe` 에 이미 설치되어 있어 full path 직접 실행으로 1차 실증 통과(client `18.1` / server `18.4` / `SSL TLSv1.3` / `inet_server_addr = 10.0.20.165` / `inet_server_port = 5432` 출력 확인).
  - 즉, 본 리스크는 client 미설치가 아니라 일반 `psql` PATH 미등록 문제로 좁혀짐.
    - 본 일자 검증으로 mitigation 범위 명확화 — full path 직접 실행 또는 PATH 등록 후속(`_common/followups-overview.md` 2026-06-13 SSM Port Forwarding 후속 메모 §1).
    - 본 리스크는 AWS / SSM / RDS 정합성 문제와 별개(운영자 PC 환경 문제) — 본 리스크 발견 시 SSM tunnel / RDS 정상성 판단을 잘못 내리지 않도록 진단 순서를 명확히 정리
- Detection:
  - PowerShell `psql` 호출 시 `'psql' is not recognized as the name of a cmdlet, function, ...` 또는 `command not found` 패턴 / `where.exe psql` 출력 빈 결과 / `Get-Command psql` 결과 없음 패턴 모니터.
  - [2026-06-13 보강] `Get-Item "C:\Program Files\PostgreSQL\18\bin\psql.exe"` 출력으로 psql 18 client 설치 여부 확인 — 설치되어 있으면 full path 직접 실행으로 우회. PATH 미등록은 `$env:Path -split ';' | Select-String "PostgreSQL\\18\\bin"` 출력 빈 결과로 식별
- Rollback:
  - 정식 설치 — 02 spec runbook 부록 A 의 PostgreSQL client 설치 절차로 운영자 직접 설치(major 18 / full 18.4 / R-DATA-003 mitigation 정합) + 시스템 / 사용자 PATH 등록. 임시 보정 — Python `psycopg2` 사용 또는 full path 직접 실행(`& "C:\Program Files\PostgreSQL\18\bin\psql.exe" ...`) 시 본 리스크 영향 없이 작업 진행 가능.
  - [2026-06-13 보강] PATH 등록 작업은 운영자 로컬 PC `setx PATH ...` 또는 GUI(시스템 속성 → 환경 변수) 직접 작업 책임 — 별도 AWS / RDS 작업 영향 없음
- Evidence links: [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md).


### R-DATA-006 Details

- Current status (R-DATA-006): `Mitigated` — Secrets Manager JSON multi-key 5종 재생성 정책 유지.
- R-DATA-006 · Impact High · Probability Medium · Affected Spec 06, 08, 04, 05, 09.
- Mitigation history:
  - secret 생성 시 JSON multi-key 5종(`host` / `port` / `dbname` / `username` / `password`) 모두 포함을 점검. ECS `secrets` 필드와 secret JSON key 가 1:1 매핑되는지 Task Definition 등록 전 검증. secret 변경 시 변경 사실 1줄을 운영자 노트에 기록. 실제 secret value 본 문서 / 운영자 노트 / 명령 출력에 평문 기록 금지(R-DOCS-001 정합).
- Detection: ECS Task event 또는 CloudWatch Logs 에서 `connect to /var/run/postgresql/.s.PGSQL.5432`, `Is the server running locally` 또는 `password authentication failed for user` 패턴. preprocessor / decision / execution / view RunTask 의 lastStatus / exitCode 비정상 종료 모니터.
- Rollback: 누락된 key 를 secret 에 추가(secret 재생성 또는 SecretString 갱신). Task Definition 변경 없이 RunTask 재시도. secret 변경 사실은 [`../06-secrets-and-iam/operation-notes.md`](../06-secrets-and-iam/operation-notes.md) IAM 변경 기록 템플릿 형식으로 누적.
- Evidence links: [`../06-secrets-and-iam/operation-notes.md`](../06-secrets-and-iam/operation-notes.md).

### R-AUTO-008 Details

- Current status (R-AUTO-008): `Mitigated` — SSM RunCommand → Windows Scheduled Task 트리거 방식 유지.
- R-AUTO-008 · Impact Medium · Probability Medium · Affected Spec 08.
- Mitigation history:
  - KRX GUI 경로의 1차 자동화는 SSM RunCommand 가 wrapper 를 직접 실행하지 않고 `schtasks /Run /TN Portfolio-KRX-Worker-Daily` 만 트리거한다(OD-MS-015 정합). 실제 wrapper 실행은 Administrator interactive session 의 Windows Scheduled Task 가 담당한다. Scheduled Task 등록 시 실행 사용자를 Administrator interactive 세션 기준으로 고정한다.
  - SYSTEM 또는 LocalService 등 비-interactive 계정 사용 금지. EC2 가 Stop 상태이거나 Administrator 세션이 미가용한 경우에는 wrapper 가 GUI 단계에서 실패할 수 있으므로, EventBridge Scheduler 정기 트리거 도입 시 EC2 Running 상태 + Administrator session 가용 여부를 사전 점검하는 흐름을 후속(task 54) 책임으로 명시한다.
- Detection:
  - wrapper 로그(`C:\portfolio\logs\krx_worker_daily_*.log`) 의 `KRX Login` 단계 실패 패턴 / Chrome 프로세스의 SessionId 점검(`Get-Process -Name chrome`
  - `Get-Process -Name chromium` 의 SessionId column) / SSM RunCommand 응답이 `SUCCESS` 라도 wrapper 안 KRX login 실패 응답 패턴 모니터 / Scheduled Task 의 Last Run Result code 점검.
- Rollback: 운영자가 RDP 재진입 후 Scheduled Task 의 실행 사용자 / 실행 옵션을 Administrator interactive 세션 기준으로 재설정. 임시 복구는 운영자 RDP 안에서 wrapper 직접 실행. 자동화 trigger 는 SSM RunCommand → `schtasks /Run` 으로만 사용한다(SSM 직접 wrapper 실행 채택 금지).

### R-DATA-003 Details

- Current status (R-DATA-003): `Mitigated` — pg_dump / pg_restore major version 정합 정책 유지.
- R-DATA-003 · Impact High · Probability Medium · Affected Spec 02, 03, 10.
- Mitigation history:
  - dump 시작 전 `pg_dump --version` 또는 `pg_restore --list` 메타데이터로 source major version 을 확인. RDS engine major version 을 dump source 이상으로 맞춘다(필요 시 RDS 재생성). client 버전도 dump source 이상으로 설치([`../02-aws-network-and-rds/runbook.md`](../02-aws-network-and-rds/runbook.md) 부록 A-1, A-3).
  - [2026-06-10 보강] 03 spec 시점에 client major 18 / full 18.4 유지 정책 추가 명시([`../03-marketconnector-ec2/design.md`](../03-marketconnector-ec2/design.md) §5.1, §5.2). 다운그레이드 금지.
- Detection: `pg_restore --list` 호출 시 archive header version 불일치 메시지. restore 시작 직후 catalog 관련 오류.
- Rollback: RDS engine major version 재선정 후 RDS 재생성 또는 적합한 client 재설치로 재시도. 기존 dump 는 보존.
- Evidence links: [`../02-aws-network-and-rds/runbook.md`](../02-aws-network-and-rds/runbook.md), [`../03-marketconnector-ec2/design.md`](../03-marketconnector-ec2/design.md).

### R-DATA-004 Details

- Current status (R-DATA-004): `Mitigated` — `--no-owner --no-privileges` + 사후 GRANT 재구성 정책 정합 유지.
- R-DATA-004 · Impact High · Probability Medium · Affected Spec 02, 10.
- Mitigation history:
  - restore 단계에서 `pg_restore --no-owner --no-privileges` 옵션 권고(02 runbook 부록 A-4). 권한 / owner 는 데이터 복원 이후 [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md) §4 SQL 로 일괄 재구성. 운영자 노트에 dump source role 인벤토리 사전 기록.
- Detection: restore 로그의 `role "..." does not exist` 메시지. restore 후 권한 매트릭스 검증 SQL 결과 불일치.
- Rollback: DB drop / recreate 후 `--no-owner --no-privileges` 로 재실행. 권한 / owner 는 02 spec db-roles-and-grants.md §4 SQL 로 재적용.
- Evidence links: [`../02-aws-network-and-rds/db-roles-and-grants.md`](../02-aws-network-and-rds/db-roles-and-grants.md).

### R-AUTO-006 Details

- Current status (R-AUTO-006): `Mitigated` — Windows EC2 worker 다운로드 경로 junction 연결 정책 유지.
- R-AUTO-006 · Impact Medium · Probability Medium · Affected Spec 08.
- Mitigation history:
  - EC2 worker 초기 셋업 시점에 `C:\Users\USER\Downloads` 를 `C:\Users\Administrator\Downloads` 로 junction 연결한다. wrapper(`run_krx_worker_daily.ps1`) 안에서 매 실행 시점에 junction 존재 여부와 정합성을 점검한다. 다운로드 경로를 운영자 노트에 사실로만 기록하고, 코드의 `interest_program.py` 다운로드 경로 가정을 임의로 수정하지 않는다(8개 MS 소스 미수정 정책 정합).
- Detection: wrapper 로그(`C:\portfolio\logs\krx_worker_daily_*.log`) 에서 CSV 인식 실패 / `[Collected Date] None` 패턴(이미 적재된 일자 외) / KRX program 진행 단계의 무한 대기 / `interest_program_raw` 의 일자별 row count 미증가 모니터. RDP 진입 후 `Get-Item` 으로 junction 존재 여부 점검.
- Rollback: junction 재생성 후 wrapper 재실행. 다운로드 폴더 동기화가 깨지면 빈 폴더 제거 → junction 재연결 → wrapper 재실행 흐름으로 즉시 복구.

### R-SEC-005 Details

- Current status (R-SEC-005): `Mitigated` — worker IAM Role inline policy 최소 권한 정책 유지.
- R-SEC-005 · Impact Medium · Probability Medium · Affected Spec 06, 08.
- Mitigation history:
  - worker IAM Role 의 inline policy 에 `secretsmanager:DescribeSecret` / `secretsmanager:GetSecretValue` 를 KRX 로그인 secret ARN 한정으로 부여한다. Resource · Action wildcard 0건 유지(03 §13 / OD-SEC-006 정합). secret 신규 생성 시점에 권한 부여 여부를 운영자 노트의 IAM 변경 4줄 요약 형식으로 기록한다(operation-notes.md IAM 변경 기록 템플릿 정합).
  - 자동 검증은 `secretsmanager:DescribeSecret` metadata 만 사용하고 `GetSecretValue` 는 운영자만 호출.
- Detection: wrapper / `interest_krx_login_new.py` 로그의 `AccessDeniedException` 패턴 / KRX 로그인 단계 실패 빈도 / IAM 정책 변경 audit(CloudTrail).
- Rollback: 누락된 inline policy 를 worker IAM Role 에 추가 후 wrapper 재실행. secret value 가 명령 출력 / 운영자 노트 / 콘솔 캡처에 평문 노출되지 않았는지 함께 점검(R-DOCS-001 정합).

### R-AUTO-010 Details

- Current status (R-AUTO-010): `Mitigated` — `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` guard 1차 실증 통과.
- R-AUTO-010 · Impact High · Probability Medium · Affected Spec 03, 04, 06.
- Mitigation history:
  - guard 코드 자체에 환경변수 누락(empty string / 미설정) 시 default `--execute` 진입 거부 정책 유지. 실제 `--execute` 검증 시점에 운영자 직접 환경변수 출력값을 점검(R-DOCS-001 정합 — 비밀번호 / 계좌번호 / RDS endpoint 평문 출력 금지). 환경변수 미설정 / 잘못된 조합 발견 시 `--execute` 즉시 중단 + 운영자 노트 누적.
  - [2026-06-17 보강] 본 일자 17-step E2E §2 에서 `PORT_ENVIRONMENT=paper` + `PORT_DB_TARGET=aws-paper` 환경에서만 KIS paper BUY 4건 진입 / 잘못된 DB / 잘못된 broker 계좌로 흘러간 사례 0건 / guard 정합 1차 실증. v5 env injection(JSON SecretString 내부 key 추출 + `APP_*` 호환 key + `KIS_*` alias 동시 export) 결과를 그대로 사용 — secret value 평문 0건.
- Detection: 환경변수 출력 로그 / executor `--execute` 시작 시 stdout 의 환경 식별 라인 모니터. `PORT_ENVIRONMENT` / `PORT_DB_TARGET` 가 paper / aws-paper 가 아닌 상태에서 `--execute` 가 진입하는 패턴 즉시 식별. [2026-06-17 보강] KIS broker_order_no 가 paper 응답값(예: `0000035906` 등 8자리 paper 패턴) 인지 / live 응답값과 혼재되지 않았는지 audit.
- Rollback: guard 우회 발견 시 즉시 process 중단 + 환경변수 정상화 + 운영자 직접 재실행. broker 측에 잘못된 주문이 흘러간 경우 broker 측 취소 + 운영자 직접 정리.


### R-NET-001 Details

- Current status (R-NET-001): `Open` — Impact High · Probability Medium · Affected Spec 02, 08.
- R-NET-001 · Impact High · Probability Medium · Affected Spec 02, 08.
- Mitigation: NAT-free OPT-1 적용 Task SG는 inbound 0.0.0.0/0 절대 미허용. outbound는 KRX/Naver/yfinance/holiday 등 필요 도메인만. SG 생성 시 02 runbook 단계에 따라 inbound 빈 상태로 시작
- Detection: VPC Flow Logs로 비정상 inbound 감지. SG 변경 audit(CloudTrail). 정기 SG 점검 SQL/CLI 결과를 운영자 노트에 기록
- Rollback: 노출된 SG 즉시 inbound rule 제거. Task 재기동. 노출 기간 동안 호출된 endpoint 점검

### R-NET-003 Details

- Current status (R-NET-003): `Open` — Impact High · Probability Medium · Affected Spec 02, 06, 07.
- R-NET-003 · Impact High · Probability Medium · Affected Spec 02, 06, 07.
- Mitigation: 02 runbook Step의 VPC Endpoint 5종 활성 확인. sg-vpc-endpoints inbound 443 허용 확인. private DNS 활성. validation-checklist의 endpoint state `available` 체크
- Detection: ECS Task 기동 실패(`CannotPullContainerError`, `ResourceInitializationError`). Secrets Manager API timeout 로그
- Rollback: 누락 endpoint 추가 생성. AZ 누락 시 해당 AZ subnet 추가. Task 재기동

### R-SEC-001 Details

- Current status (R-SEC-001): `Open` — Impact High · Probability Low · Affected Spec 02.
- R-SEC-001 · Impact High · Probability Low · Affected Spec 02.
- Mitigation: sg-rds-postgres inbound는 다른 SG 참조만 허용. 0.0.0.0/0 절대 금지(02 design.md, OD-NET-009 정책). RDS publicly accessible false
- Detection: SG 변경 audit(CloudTrail). Trusted Advisor 또는 Security Hub의 RDS public exposure 점검
- Rollback: inbound rule 즉시 제거. RDS master password Secrets Manager에서 즉시 rotate. 연결 로그 점검

### R-DATA-001 Details

- Current status (R-DATA-001): `Open` — Impact High · Probability Medium · Affected Spec 02.
- R-DATA-001 · Impact High · Probability Medium · Affected Spec 02.
- Mitigation: 02 runbook Step DB 초기화에서 7개 role 모두에 `ALTER ROLE <role> SET search_path = ...` 적용. 8개 MS README의 search_path 순서 그대로 유지(OD-DB-006). 검증 SQL `SHOW search_path;`를 role별로 실행
- Detection: application 실행 시 `relation does not exist` 또는 다른 schema 테이블 사용 정황. 검증 SQL 결과 로그
- Rollback: role search_path 재설정. 잘못 쓰여진 데이터가 있으면 schema별 row count 비교 후 수동 정리

### R-DATA-002 Details

- Current status (R-DATA-002): `Open` — Impact High · Probability Medium · Affected Spec 02, 10.
- R-DATA-002 · Impact High · Probability Medium · Affected Spec 02, 10.
- Mitigation: 02 runbook Step pg_dump 사전 준비 단계에서 dump 옵션(`--format=custom --no-owner --no-privileges`) 고정. restore 후 design.md "검증 SQL 후보" 실행. 핵심 테이블 row count를 local-dev와 ±0 비교
- Detection: 검증 SQL의 schema/table count 차이. application 실행 시 sequence 충돌(`duplicate key`) 또는 권한 오류
- Rollback: 즉시 cutover 중단. local-dev 환경으로 환경변수 복귀(RDS는 evidence로 보존). 다음 시도에 dump/restore 옵션 재검토

### R-BROKER-001 Details

- Current status (R-BROKER-001): `Open` — Impact High · Probability Medium · Affected Spec 03.
- R-BROKER-001 · Impact High · Probability Medium · Affected Spec 03.
- Mitigation: OD-NET-003에 따라 EC2+EIP 1순위 채택. EIP는 instance에 attach + running 상태 유지(detach 시 미사용 비용 + IP 변경). EC2 교체 시 EIP detach → 새 EC2에 attach Runbook 03에서 정의
- Detection: broker API 인증 실패 로그. KIS 측 IP 등록 미일치 알림
- Rollback: EIP를 새 EC2에 즉시 재attach. broker 측 IP 등록은 동일 EIP라 변경 불필요. EIP 자체 변경 시 broker 측 등록 절차(수동)

### R-BROKER-002 Details

- Current status (R-BROKER-002): `Open` — Impact High · Probability Low · Affected Spec 03.
- R-BROKER-002 · Impact High · Probability Low · Affected Spec 03.
- Mitigation: OD-MS-001 EC2+EIP 1순위 + 단일 instance 운영 정책. ECS Fargate 옵션 사용 시 동시 1 Task 강제(desiredCount=1, 무중단 배포 시 토큰 인계 절차). 토큰 백업은 EC2 로컬 + S3(OD-SEC-003)
- Detection: broker 측 토큰 invalid / 강제 만료 응답. 토큰 발급 직후 즉시 invalid되는 패턴
- Rollback: 토큰 강제 갱신. 한 instance만 운영 중인지 점검. 다중 Task가 떠 있다면 여분 Task 중지 후 재발급

### R-COST-001 Details

- Current status (R-COST-001): `Open` — Impact Medium · Probability Medium · Affected Spec 02, 05, 10.
- R-COST-001 · Impact Medium · Probability Medium · Affected Spec 02, 05, 10.
- Mitigation: OD-NET-001/002 NAT 미사용 기본안. OD-NET-007/008 ALB 초기 미사용. OD-RDS-002 paper single-AZ. 02 decision-matrix.md 비용 프로파일 표로 시나리오별 합계 사전 합의. AWS Cost Explorer 일별 그래프 점검
- Detection: AWS Billing Dashboard 일별 비용 spike. Cost Anomaly Detection alert
- Rollback: 비계획 리소스 즉시 식별 후 삭제(NAT GW, ALB, 미사용 Endpoint). RDS multi-AZ 전환은 운영자 결정에 한해 유지

### R-COST-002 Details

- Current status (R-COST-002): `Open` — Impact Medium · Probability Low · Affected Spec 02.
- R-COST-002 · Impact Medium · Probability Low · Affected Spec 02.
- Mitigation: 02 runbook Step `NAT Gateway 미사용 확인`을 명시 단계로 분리. tasks.md task 9에 CONFIRMED 기록. validation-checklist 항목 포함
- Detection: VPC 콘솔의 NAT Gateways 메뉴에 항목 존재. AWS Billing의 `NatGateway-Hours` 라인
- Rollback: NAT Gateway 즉시 삭제(EIP는 유지/회수 결정). Route Table에서 NAT 라우트 제거

### R-SEC-003 Details

- Current status (R-SEC-003): `Open` — Impact High · Probability Low · Affected Spec 02, 06.
- R-SEC-003 · Impact High · Probability Low · Affected Spec 02, 06.
- Mitigation: portadmin MFA 활성 + 백업 코드를 안전한 위치 보관. 비밀번호는 운영자만 알고 있고 본 문서 / 노트에 평문 기록 금지(`[REDACTED]`). Root는 비상용으로 보존하여 portadmin 비밀번호 재설정 경로 유지
- Detection: portadmin 로그인 실패 반복 / MFA 코드 검증 실패. CloudTrail의 portadmin 호출 흐름 단절
- Rollback: Root 재로그인 → IAM → Users → `portadmin` → Manage console access → Reset password → MFA 재등록. 그래도 복구 불가 시 portadmin 삭제 후 동일 권한 신규 사용자 재생성(권한 매트릭스는 06 spec 결정 따름)

### R-NET-004 Details

- Current status (R-NET-004): `Mitigated` — Impact Medium · Probability Medium · Affected Spec 02, 03, 10.
- R-NET-004 · Impact Medium · Probability Medium · Affected Spec 02, 03, 10.
- Mitigation: 같은 VPC 안의 EC2(예: aws-paper MarketConnector EC2) 또는 임시 EC2를 restore runner로 사용. 운영자 접속은 EC2 Instance Connect 또는 SSM Session Manager(OD-NET-009)로만 수행. 로컬 PC IP를 RDS SG에 직접 허용하지 않는다(R-SEC-001)
- Detection: 로컬 psql / pg_restore 호출이 timeout. RDS endpoint가 private IP로만 resolve
- Rollback: restore runner EC2를 통해 다시 진행. RDS SG에 로컬 IP 허용을 임시로 열지 않는다

### R-BROKER-003 Details

- Current status (R-BROKER-003): `Open` — Impact Medium · Probability Medium · Affected Spec 03.
- R-BROKER-003 · Impact Medium · Probability Medium · Affected Spec 03.
- Mitigation: 본 spec(03) 임시 검증 단계에서 호출 빈도 최소화. token 재사용 정책(OD-SEC-003) 유지. Connector 자동 재기동 횟수 제한(systemd Restart=on-failure + 횟수 캡 — 정상 운영 모드 진입 시점). 신규 주문 호출 0건 정책으로 호출 폭주 차단
- Detection: KIS API 응답 코드 / 메시지 패턴 점검(rate limit 식별 코드). Connector 로그의 재시도 패턴
- Rollback: 호출 일시 중지 + 운영자 결정으로 호출 빈도 재조정 후 재개. 토큰 강제 갱신은 별도 결정

### R-AUTO-004 Details

- Current status (R-AUTO-004): `Open` — Impact Medium · Probability Medium · Affected Spec 03.
- R-AUTO-004 · Impact Medium · Probability Medium · Affected Spec 03.
- Mitigation: 정상 운영 모드 전환(systemd unit 작성 + Restart=on-failure)을 03 후속 task 또는 별도 phase 책임으로 명시. 임시 검증 단계에서는 운영자가 EC2 reboot 시 수동 재기동을 운영자 노트에 기록
- Detection: EC2 reboot 후 Connector 재기동 점검 / Flask `/api/v1/view/health`(또는 동등) 헬스체크. CloudWatch alarm(systemd 진입 후)
- Rollback: systemd unit 적용 후 재기동. 임시 검증 단계에서는 운영자 수동 재기동 + operation-notes 누적 기록

### R-NET-002 Details

- Current status (R-NET-002): `Open` — Impact Medium · Probability Medium · Affected Spec 02, 08.
- R-NET-002 · Impact Medium · Probability Medium · Affected Spec 02, 08.
- Mitigation: 외부 outbound 필요 workload(crawler · preprocessor · research) 는 public subnet + assignPublicIp=ENABLED 로 배치 · private subnet 배치 시 NAT / VPC Endpoint 확인 후 진입.
- Detection: ECS Task lastStatus 실패 패턴 · CloudWatch Logs 의 outbound DNS/TCP 실패 · Task Definition subnet 배치 정합 audit.
- Rollback: 잘못 배치된 Task subnet 재배치 · assignPublicIp 재설정 · Task 재기동.
- Evidence links: [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md).

### R-AUTO-002 Details

- Current status (R-AUTO-002): `Open` — Impact High · Probability Medium · Affected Spec 04, 05, 10.
- R-AUTO-002 · Impact High · Probability Medium · Affected Spec 04, 05, 10.
- Mitigation: aws-live 자동 BUY/SELL 은 별도 Decision 승인 + paper N영업일 통과 후에만 진입(OD-SAFE-002 · OD-SAFE-003 정합).
- Detection: `strategy_execution_order` · `connector_order_request` 의 live 계정 신규 row 발생 audit · CloudTrail Step Functions StartExecution source audit.
- Rollback: 자동 활성화 이력 발견 시 즉시 Scheduler DISABLED · 영향 row 직접 SQL 점검 · KIS 측 취소.
- Evidence links: [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md).

### R-SEC-002 Details

- Current status (R-SEC-002): `Open` — Impact High · Probability Low · Affected Spec 02.
- R-SEC-002 · Impact High · Probability Low · Affected Spec 02.
- Mitigation: Root MFA 활성 + 백업 코드 안전 보관 · 일상 AWS 작업은 portadmin 사용 · Root 는 비상용으로만 사용.
- Detection: Root 로그인 시각 audit(CloudTrail `ConsoleLogin`) · Root MFA 검증 실패 반복 · Root Access Key 신규 발급.
- Rollback: MFA 재등록 · 자격 노출 시 즉시 rotate · Access Key 회수.
- Evidence links: [`../02-aws-network-and-rds/operation-notes.md`](../02-aws-network-and-rds/operation-notes.md).

### R-AUTO-003 Details

- Current status (R-AUTO-003): `Open` — Impact Medium · Probability Medium · Affected Spec 03, 04, 05, 08, 09.
- R-AUTO-003 · Impact Medium · Probability Medium · Affected Spec 03, 04, 05, 08, 09.
- Mitigation: EBS 사용률 CloudWatch 알람 · 로그 rotate · `access_token.txt` 백업 정책 유지 · venv 재설치 절차 문서화.
- Detection: EC2 CloudWatch metric `DiskSpaceUtilization` · Connector / Flask 비정상 종료 로그 · access_token 갱신 실패 응답.
- Rollback: EBS 볼륨 확장(`modify-volume`) 또는 EC2 재구성 · 로그 정리 후 Connector 재기동.
- Evidence links: [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md).

### R-SEC-004 Details

- Current status (R-SEC-004): `Open` — Impact High · Probability Low · Affected Spec 03, 05.
- R-SEC-004 · Impact High · Probability Low · Affected Spec 03, 05.
- Mitigation: 운영 환경 `CONNECTOR_DEBUG=false` 강제 · 임시 검증 후 즉시 unset · systemd env 관리 · Flask app app.debug 강제 false.
- Detection: `/api/v1/*` 응답 본문에 debug stack trace / 환경변수 노출 패턴 · Flask 실행 로그의 `DEBUG` 라벨.
- Rollback: 즉시 process 중단 · `CONNECTOR_DEBUG` unset 후 재기동 · 노출 secret 확인 시 rotate(R-DOCS-001 정합).
- Evidence links: [`../03-marketconnector-ec2/operation-notes.md`](../03-marketconnector-ec2/operation-notes.md).

## Accepted/Closed Risks

Status 가 `Accepted` 또는 `Closed` 인 리스크의 요약이다. 원본 row 는 `Risk Index` 에서 그대로 유지되고, 상세는 `Risk Details` 하위 anchor 에서 참조한다. 삭제하지 않고 이 섹션에서만 요약을 노출한다.

| Risk ID | Area | Status | Impact | Risk | Next Action |
|---|---|---|---|---|---|
| R-AUTO-024 | Automation | 🔵 Accepted | Medium | Slack notifier Lambda 의 webhook URL 환경변수 장기 보관 시 secret 노출 위험 | 운영 안정화 후 Secrets Manager / SSM SecureString 이전 재검토. [See details: R-AUTO-024 Details](#r-auto-024-details) |
| R-AUTO-027 | Automation | 🔵 Accepted | Medium | APPROVAL_REQUIRED Slack 이 0/0 표시 → 운영자 오해 · 잘못된 다음 단계 결정 위험 | Slack payload builder 개선 후 Mitigated 승격 후보. [See details: R-AUTO-027 Details](#r-auto-027-details) |

Closed 리스크가 신규 식별되면 본 표에 추가한다. 현재 회차 기준 Closed 상태 Risk ID 는 별도 없음(⚫ N/A).

## Risk Update Rules

- 새 리스크는 다음 ID부터 부여한다(예: `R-NET-004`, `R-DATA-003`).
- mitigation이 운영자 결정과 충돌하면 본 spec에서 임의로 결정값을 바꾸지 않고, [`./operator-decisions.md`](./operator-decisions.md)의 해당 Decision ID에 변경 제안만 기록한 뒤 운영자 승인 절차를 거친다.
- Status가 `Mitigated`로 바뀌어도 row를 삭제하지 않는다. detection / rollback이 여전히 유효하므로 운영 회고 자료로 보존한다.
- 본 문서는 후속 spec(03 ~ 10)에서 입력으로 사용된다.

## Security Notes

- 실제 AWS 리소스 생성 / 변경 없음.
- 8개 MS의 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 없음.
- 실제 secret 값 출력 없음(모두 `[REDACTED]`).
- mitigation 제안에 의한 실제 IaC 또는 Console 변경은 본 문서 범위 밖.
