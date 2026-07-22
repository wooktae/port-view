# Risk Register — AWS Migration

## Purpose

본 문서는 PORT-STRATEGY-AI AWS Migration의 운영·보안·데이터·비용 리스크를 관리하는 단일 진실원이다.

한 번 부여한 Risk ID는 재사용하거나 재번호를 부여하지 않는다. 각 Risk는 아래 항목을 `항목 / 값` 2열 표로 관리한다.

| 항목 | 값 |
| --- | --- |
| 상태 | Open · Mitigated · Accepted · Closed |
| 영향도 | High · Medium · Low |
| 발생 가능성 | High · Medium · Low |
| 위험 | 발생 가능한 문제 |
| 대응 | 현재 적용하거나 수행할 조치 |
| 관련 spec | 책임 spec 번호 |

날짜별 mitigation history, executionName, ARN, commandId, revision, SHA256, DB after-check 수치와 raw log는 본 문서에 누적하지 않는다.

상세 실행 근거는 각 spec의 `operation-notes.md`, 작업 이력은 `WORKLOG.md`, 결정 변경은 `operator-decisions.md`에서 관리한다.

민감정보 원문은 기록하지 않고 `[REDACTED]` 계열 placeholder만 사용한다.

## Risk Dashboard

### 상태 요약

| 항목 | 값 |
| --- | --- |
| 전체 | 76건 |
| 🔴·🟠 Open | 29건 |
| 🟢 Mitigated | 45건 |
| 🔵 Accepted | 2건 |
| ⚫ Closed | 0건 |
| Open High | 19건 |

### 영역별 현황

| 항목 | 값 |
| --- | --- |
| Automation | 38건 |
| Data | 17건 |
| Security | 7건 |
| Broker | 5건 |
| Network | 4건 |
| Cost | 3건 |
| Docs | 2건 |

### 영향도 현황

| 항목 | 값 |
| --- | --- |
| High | 46건 |
| Medium | 28건 |
| Low | 2건 |

### 🔴 Open High — 우선 조치

| 항목 | 값 |
| --- | --- |
| R-NET-001 | Public subnet ECS Task SG inbound 잘못 열어 외부 노출 가능 |
| R-NET-003 | 현재 네트워크 구조에 필요한 AWS API 접근 경로 또는 Endpoint 세트 누락·불일치 |
| R-SEC-001 | RDS SG inbound 광범위 CIDR · RDS 외부 노출 |
| R-DATA-001 | RDS role search_path 미설정 · 잘못된 schema 조회 |
| R-DATA-002 | pg_dump / pg_restore cutover 정합성 깨짐 · sequence · default privilege 누락 |
| R-BROKER-001 | EIP detach / EC2 교체로 broker 등록 IP 불일치 · broker 호출 실패 |
| R-BROKER-002 | 단일 access_token 다중 갱신 충돌 (EC2 다중화 · Fargate 다중 Task) |
| R-AUTO-001 | Step Functions Retry 잘못 활성화 · BUY / SELL / fill sync / position 중복 주문 |
| R-AUTO-002 | aws-live 자동 BUY/SELL 승인 없이 조기 활성화 · 실계좌 잘못된 주문 위험 |
| R-DOCS-001 | spec 산출물에 secret / token / 계좌번호 / webhook 평문 기록 위험 |
| R-SEC-002 | Root 자격(비밀번호 / MFA) 분실 · 노출 · 비상 복구 불가 · 외부 침해 위험 |
| R-SEC-003 | portadmin 자격 분실 · AWS Console / IAM 작업 중단 |
| R-SEC-004 | Flask debug 모드가 운영 환경 진입 시 stack trace · env · 내부 경로 외부 노출 |
| R-DATA-007 | local PostgreSQL ↔ AWS Paper RDS 병합 · 중복 주문 · 잘못된 fill · source of truth 붕괴 |
| R-AUTO-014 | 단일 Task Definition + command override 오매핑 · 잘못된 entrypoint 실행 위험 |
| R-AUTO-016 | Windows worker Administrator interactive session 부재 · KRX GUI 수집 실패 위험 |
| R-AUTO-030 | 3단계 Intraday Stop Sell 자동 ENABLE 조기 진입 · 승인 gate 없이 broker 손절 위험 |
| R-SEC-010 | app role DB password 평문 노출 이력 후 rotate 없이 운영 시 무단 접근 위험 |
| R-AUTO-034 | Fargate Task Role states:StartExecution 광역 부여 · gate default ENABLE 회귀 위험 |

## Risk Inventory

Risk는 Area별로 정리한다. 상태가 `Open`인 항목은 대응 완료 후 증거를 해당 spec `operation-notes.md`에 남기고 본 문서의 상태만 갱신한다.

## Network
### R-NET-001

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | Public subnet ECS Task SG inbound 잘못 열어 외부 노출 가능 |
| 대응 | Task SG inbound 0.0.0.0/0 금지 · outbound 필요 도메인만 허용. |
| 관련 spec | 02, 08 |

### R-NET-002

| 항목 | 값 |
| --- | --- |
| 상태 | 🟠 Open |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | NAT-free 구조에서 외부 outbound workload 를 private subnet 에 잘못 배치해 외부 호출 실패 |
| 대응 | 외부 outbound workload 는 public subnet + assignPublicIp=ENABLED 배치. |
| 관련 spec | 02, 08 |

### R-NET-003

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | 현재 네트워크 구조에 필요한 AWS API 접근 경로 또는 Endpoint 세트 누락·불일치 |
| 대응 | ECR API / ECR DKR / Secrets Manager / Logs Endpoint 확인 |
| 대응 | S3 Gateway Endpoint 확인 |
| 대응 | SSM은 Public outbound 또는 SSM Endpoint 중 실제 운영 경로 확인 |
| 대응 | Interface Endpoint SG inbound 443 확인 |
| 관련 spec | 02, 06, 07 |

### R-NET-004

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | RDS Public No · 로컬 PC 직접 접속 불가 · restore / 검증 SQL 수행 불가 |
| 대응 | 같은 VPC EC2 를 restore runner 로 · SSM Port Forwarding tunnel 사용. |
| 관련 spec | 02, 03, 10 |

## Security
### R-SEC-001

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Low |
| 위험 | RDS SG inbound 광범위 CIDR · RDS 외부 노출 |
| 대응 | sg-rds-postgres inbound 는 다른 SG 참조만 허용 · publicly accessible false. |
| 관련 spec | 02 |

### R-SEC-002

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Low |
| 위험 | Root 자격(비밀번호 / MFA) 분실 · 노출 · 비상 복구 불가 · 외부 침해 위험 |
| 대응 | Root MFA · 백업 코드 안전 보관 · 일상 작업은 portadmin 사용. |
| 관련 spec | 02 |

### R-SEC-003

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Low |
| 위험 | portadmin 자격 분실 · AWS Console / IAM 작업 중단 |
| 대응 | portadmin MFA · 백업 코드 안전 보관 · Root 로 재설정 경로 유지. |
| 관련 spec | 02, 06 |

### R-SEC-004

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Low |
| 위험 | Flask debug 모드가 운영 환경 진입 시 stack trace · env · 내부 경로 외부 노출 |
| 대응 | CONNECTOR_DEBUG 운영 강제 false · 검증 이후 즉시 unset · systemd env 관리. |
| 관련 spec | 03, 05 |

### R-SEC-005

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | worker IAM Role 의 KRX 로그인 secret read 권한 누락 · AccessDenied · 기동 실패 |
| 대응 | worker IAM Role inline policy 에 secret ARN 한정 GetSecretValue 부여. |
| 관련 spec | 06, 08 |

### R-SEC-009

| 항목 | 값 |
| --- | --- |
| 상태 | 🟠 Open |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | Windows worker Autologon 자격 · RDP 노출로 KRX worker + EC2 OS 동시 침해 위험 |
| 대응 | Autologon 은 paper worker 한정 · RDP 운영자 IP 한정 · SSM 우선. |
| 관련 spec | 08, 05, 10 |

### R-SEC-010

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | app role DB password 평문 노출 이력 후 rotate 없이 운영 시 무단 접근 위험 |
| 대응 | 노출된 app role password를 rotate하고 Secrets Manager·loader·환경변수 주입을 재검증한다. |
| 관련 spec | 02, 03, 04, 05, 06, 08, 09, 10 |

## Data
### R-DATA-001

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | RDS role search_path 미설정 · 잘못된 schema 조회 |
| 대응 | 7 role 에 ALTER ROLE ... SET search_path 적용 · role 별 SHOW search_path 검증. |
| 관련 spec | 02 |

### R-DATA-002

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | pg_dump / pg_restore cutover 정합성 깨짐 · sequence · default privilege 누락 |
| 대응 | dump 옵션 고정 + restore 후 검증 SQL · row count local-dev ±0 비교. |
| 관련 spec | 02, 10 |

### R-DATA-003

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | dump source · RDS engine major version 불일치 · pg_restore 실패 |
| 대응 | client / RDS 를 dump source 이상 major 로 유지 · 다운그레이드 금지. |
| 관련 spec | 02, 03, 10 |

### R-DATA-004

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | dump 안 owner role 부재로 restore 실패 · 권한 부정확 설정 |
| 대응 | pg_restore --no-owner --no-privileges + 사후 GRANT 재구성. |
| 관련 spec | 02, 10 |

### R-DATA-005

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | DB Role 최소 권한 적용 시 시나리오별 권한 부족 · 과다 가능 |
| 대응 | 02 db-roles-and-grants §5 검증 SQL 정기 점검 · 개별 GRANT 보정 누적. |
| 관련 spec | 02, 04, 05, 06, 08 |

### R-DATA-006

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | Secrets Manager JSON multi-key 누락 · ECS Task 잘못된 DB host 접속 실패 |
| 대응 | JSON 5종 key 포함 · Task Definition secrets 1:1 매핑 사전 점검. |
| 관련 spec | 06, 08, 04, 05, 09 |

### R-DATA-007

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | local PostgreSQL ↔ AWS Paper RDS 병합 · 중복 주문 · 잘못된 fill · source of truth 붕괴 |
| 대응 | OD-ENV-006/007/008 명문화 · PORT_ENVIRONMENT + PORT_DB_TARGET guard. |
| 관련 spec | 02, 03, 04, 05, 10 |

### R-DATA-008

| 항목 | 값 |
| --- | --- |
| 상태 | 🟠 Open |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | Research 내부 adapter 3종이 port_strategy_common 계약 변경 미추종 · 결과 불일치 |
| 대응 | py_compile / import smoke + Daily Decision vs Research sample 비교. |
| 관련 spec | 09, 07, 10 |

### R-DATA-009

| 항목 | 값 |
| --- | --- |
| 상태 | 🟠 Open |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | smoke 통과 표현이 raw 최신성 완료로 오해되어 stale data 기반 운영 진입 위험 |
| 대응 | 표현을 "부분 완료" 로 통일 · non-GUI raw 7종 MAX(trade_date) SQL 점검. |
| 관련 spec | 08, 04, 09 |

### R-DATA-010

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | non-GUI raw 최신성 미회복 · preprocessor · Research · Decision stale raw 전파 위험 |
| 대응 | raw 최신성 회복 후 preprocessor 재실행 · 신규 feature date 검증. |
| 관련 spec | 08, 04, 09 |

### R-DATA-011

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | marketconnector_app legacy schema USAGE/DML/search_path 누락 · BALANCE_REFRESH 실패 |
| 대응 | legacy schema USAGE/DML + search_path 보정 + default privileges 갱신. |
| 관련 spec | 02, 03, 06 |

### R-DATA-012

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | 추가매수 case unique 제약 충돌 · position_state 갱신 실패 · 정합성 위험 |
| 대응 | execution_sync_buy_position INSERT/UPDATE merge patch. |
| 관련 spec | 04, 10 |

### R-DATA-013

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | execution_app 의 decision.strategy_daily_position_decision UPDATE 권한 누락 |
| 대응 | GRANT USAGE ON SCHEMA decision + UPDATE ON TABLE 한정 부여. |
| 관련 spec | 02, 04 |

### R-DATA-014

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | 장중 Snapshot Refresh 실패/지연 시 stale snapshot 상태에서 Evaluate 진입 위험 |
| 대응 | OD-MS-035 안전장치 4종 · snapshot as_of_ts 검증 · fail-closed. |
| 관련 spec | 03, 04, 10 |

### R-DATA-015

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | sellable_qty 0 / 부족 상태에서 손절 후보 인식 · Daily SELL 중복 손절 위험 |
| 대응 | evaluate 단계 sellable_qty 사전 비교 · 기존 SELL 진행 시 회피. |
| 관련 spec | 03, 04, 10 |

### R-DATA-016

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | Snapshot current_price null / 0 / stale 상태에서 Evaluate · 잘못된 손절 판단 |
| 대응 | current_price null·0·과거·범위 초과 시 evaluate 진입 차단. |
| 관련 spec | 03, 04, 10 |

### R-DATA-017

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | KRX worker exit 0 만으로 Step2B SUCCESS 판정 시 raw N영업일 lag stale 전파 위험 |
| 대응 | Python 결과 기반 non-zero exit와 expected trade date·row count validator를 유지한다. |
| 관련 spec | 04, 08, 09, 10 |

## Broker
### R-BROKER-001

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | EIP detach / EC2 교체로 broker 등록 IP 불일치 · broker 호출 실패 |
| 대응 | EC2+EIP 1순위 유지 · EIP running attach 유지 · 교체 시 detach·attach 절차. |
| 관련 spec | 03 |

### R-BROKER-002

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Low |
| 위험 | 단일 access_token 다중 갱신 충돌 (EC2 다중화 · Fargate 다중 Task) |
| 대응 | 단일 instance 운영 · Fargate 옵션 시 desiredCount=1 · 토큰 백업 정책. |
| 관련 spec | 03 |

### R-BROKER-003

| 항목 | 값 |
| --- | --- |
| 상태 | 🟠 Open |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | KIS API rate limit 초과로 잔고 / 주문 조회 / view API 실패 |
| 대응 | 임시 검증 단계 호출 빈도 최소화 · token 재사용 · Restart 캡. |
| 관련 spec | 03 |

### R-BROKER-004

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | KIS paper timeout 후 단순 재실행 · 같은 주문 broker 중복 접수 위험 |
| 대응 | 재실행 전 broker_order_no 사전 점검 · 부재 시만 REQUESTED 통제 복구. |
| 관련 spec | 03, 04, 10 |

### R-BROKER-005

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | 보유 없음 상태 실주문 테스트 시 REJECTED · OPEN 포지션 없음으로 잘못된 row 생성 |
| 대응 | output1_count + strategy_position_state OPEN + sellable_qty 4종 사전 점검. |
| 관련 spec | 03, 04, 10 |

## Automation
### R-AUTO-001

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | Step Functions Retry 잘못 활성화 · BUY / SELL / fill sync / position 중복 주문 |
| 대응 | 주문 제출·Fill Sync·Position 변경 State에는 Retry를 두지 않는다. State별 Retry와 command 매핑을 정기 audit한다. |
| 관련 spec | 04, 10 |

### R-AUTO-002

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | aws-live 자동 BUY/SELL 승인 없이 조기 활성화 · 실계좌 잘못된 주문 위험 |
| 대응 | aws-live BUY/SELL 별도 Decision + paper N영업일 통과 후 진입. |
| 관련 spec | 04, 05, 10 |

### R-AUTO-003

| 항목 | 값 |
| --- | --- |
| 상태 | 🟠 Open |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | EC2 EBS 부족 · venv / 로그 / access_token / Connector 산출물 저장 실패 |
| 대응 | EBS 사용률 알람 · 로그 rotate · access_token 백업 정책. |
| 관련 spec | 03, 04, 05, 08, 09 |

### R-AUTO-004

| 항목 | 값 |
| --- | --- |
| 상태 | 🟠 Open |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | systemd unit / startup 미설정 · EC2 reboot 후 Connector · Flask 자동 재기동 실패 |
| 대응 | systemd unit + Restart=on-failure 도입 · 임시 단계는 수동 재기동 노트. |
| 관련 spec | 03 |

### R-AUTO-005

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | crawler ECS Task Selenium / Chromium / KRX outbound 검증 없이 운영 진입 위험 |
| 대응 | ECS smoke RunTask 통과 · KRX GUI 는 Windows EC2 worker 분리(OD-MS-011/012). |
| 관련 spec | 08, 02 |

### R-AUTO-006

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | Windows worker 다운로드 경로 가정 vs 실제 Chrome 경로 불일치로 CSV 인식 실패 |
| 대응 | C:\Users\USER\Downloads · Administrator\Downloads junction 유지 + 매 실행 점검. |
| 관련 spec | 08 |

### R-AUTO-007

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | EC2 worker wrapper exit 0 이 실제 DB 적재 성공을 보장 못함 · stale data 전파 |
| 대응 | wrapper 안 KRX raw DB validation SSM step 자동 호출 · 실패 시 Step 2 fail. |
| 관련 spec | 08 |

### R-AUTO-008

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | SSM SYSTEM SessionId 0 컨텍스트 wrapper 실행 시 KRX GUI 로그인 실패 위험 |
| 대응 | SSM 은 schtasks /Run 만 트리거 · wrapper 실행은 Administrator interactive Task. |
| 관련 spec | 08 |

### R-AUTO-009

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | MarketConnector executor --execute 실 REQUESTED 처리 미검증 상태로 운영 진입 위험 |
| 대응 | 17-step E2E 로 KIS paper BUY 4건 SUBMITTED 통과 · aws-live 진입 전 추가 검증. |
| 관련 spec | 03, 04, 05 |

### R-AUTO-010

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | executor --execute guard 우회 · 환경변수 누락 시 잘못된 DB / broker 로 흘러갈 위험 |
| 대응 | PORT_ENVIRONMENT=paper + PORT_DB_TARGET=aws-paper 조합 강제 · env 누락 시 진입 거부. |
| 관련 spec | 03, 04, 06 |

### R-AUTO-011

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | View Daily Batch 12~17 구간 paper end-to-end 미검증 채로 운영 진입 위험 |
| 대응 | 17-step E2E 로 12~17 paper 통과 · live cutover 전 평일 / 안전 데이터 추가 검증. |
| 관련 spec | 04, 05 |

### R-AUTO-012

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | SSM Port Forwarding tunnel 단절 시 로컬 paper 검증 · app role 접속 즉시 불가 |
| 대응 | OD-NET-010 표준 경유지 유지 · tunnel 창 종료 금지 · 실패 시 재기동 절차. |
| 관련 spec | 02, 03, 04 |

### R-AUTO-013

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Low |
| 발생 가능성 | Medium |
| 위험 | 로컬 PC psql client 미설치 / PATH 미등록으로 SSM tunnel 위 SQL 점검 불가 |
| 대응 | PostgreSQL 18 client 설치 + PATH 등록 또는 full path · psycopg2 우회. |
| 관련 spec | 02, 03 |

### R-AUTO-014

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | 단일 Task Definition + command override 오매핑 · 잘못된 entrypoint 실행 위험 |
| 대응 | SF state name ↔ command override 매핑 표 정식 정리 · review checklist. |
| 관련 spec | 04, 05, 10 |

### R-AUTO-015

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | Research heavy backtest / report full 실행 실수 · Batch 비용 · vCPU · RDS 부하 |
| 대응 | smoke / full prefix + attempt=1 · SubmitJob 수동 유지. |
| 관련 spec | 09, 10 |

### R-AUTO-016

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | Windows worker Administrator interactive session 부재 · KRX GUI 수집 실패 위험 |
| 대응 | Autologon bootstrap · query user Active pre-check · wrapper 로그 login 모니터. |
| 관련 spec | 08 |

### R-AUTO-017

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Low |
| 발생 가능성 | Medium |
| 위험 | wrapper 종료 후 Chrome process 잔존 · 메모리 누수 · 다음 Selenium attach 충돌 |
| 대응 | step-02 wrapper 가 KRX 실행 직전 chrome/chromedriver best-effort reset. |
| 관련 spec | 08 |

### R-AUTO-018

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | KIS output1 empty + summary fallback 오매핑 · order/fill 오염 · 잘못 전파 위험 |
| 대응 | summary fallback guard 패치 + 단건 direct-only 기본화(OD-MS-025). |
| 관련 spec | 03, 04 |

### R-AUTO-019

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Low |
| 위험 | Step 12 wrapper 가 의도 없이 -AllowPaperOrderExecute 로 실행 · KIS 실주문 위험 |
| 대응 | wrapper 중앙 + 내부 이중 gate · default OFF · summary PaperOrder 필드 기록. |
| 관련 spec | 03, 04, 10 |

### R-AUTO-020

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | Scheduled Task trigger 성공만으로 Step 2 SUCCESS 시 stale raw · KRX login 실패 미탐 |
| 대응 | chrome reset + Running→Ready wait + Last Result 0 + KRX raw DB validation. |
| 관련 spec | 08, 04, 05, 09 |

### R-AUTO-021

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | EC2 stop/start 후 /tmp/inject-env.sh 휘발 · Daily wrapper Step 1/12/13/17 실패 |
| 대응 | wrapper bootstrap 함수가 Step 1/12/13/17 직전 env 재생성. |
| 관련 spec | 03, 04, 06 |

### R-AUTO-022

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | REJECTED rejection_code=40580000 시 Step 12 기본 필터 미포함 · 자동 재제출 단절 |
| 대응 | Step 12 retry-normalizer 내장 · 6종 조건 만족 시만 REQUESTED 복구. |
| 관련 spec | 03, 04, 10 |

### R-AUTO-023

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | SF Catch/Fail state 에 Slack notifier 누락 시 실패 인지 지연 · 정합성 위험 |
| 대응 | SF Catch · DAILY_EXECUTION_FAILED Slack notifier · 3종 이벤트 라벨. |
| 관련 spec | 04, 05, 10 |

### R-AUTO-024

| 항목 | 값 |
| --- | --- |
| 상태 | 🔵 Accepted |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | Slack notifier Lambda 의 webhook URL 환경변수 장기 보관 시 secret 노출 위험 |
| 대응 | 운영 안정화 후 Secrets Manager / SSM SecureString 이전 재검토. |
| 관련 spec | 04, 06, 10 |

### R-AUTO-025

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | Scheduler · Dispatcher · SF 사슬 실패 시 Daily 자동 검증 진입 누락 |
| 대응 | Scheduler IAM 최소 권한 + Dispatcher dryRun 검증 + 단계적 활성화 + Catch Slack. |
| 관련 spec | 04, 05, 10 |

### R-AUTO-028

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | EC2 lifecycle Scheduler 오류로 07:50 start · 15:50 stop · Crawler stop 실행 실패 |
| 대응 | 07:50 · 15:50 Scheduler 2건 ENABLED + Lambda dryRun 통과 + 주말 skip. |
| 관련 spec | 03, 04, 05, 08, 10 |

### R-AUTO-029

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | Intraday Evaluate 반복 호출로 duplicate READY 생성 · signal_type 필터 누락 |
| 대응 | evaluate 사전 duplicate 점검 · Submit & Refresh 의 signal_type 전용 필터. |
| 관련 spec | 03, 04, 10 |

### R-AUTO-030

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | 3단계 Intraday Stop Sell 자동 ENABLE 조기 진입 · 승인 gate 없이 broker 손절 위험 |
| 대응 | 초기 DISABLED 유지 · 운영자 별도 승인 후 진입 · Slack 안전 gate. |
| 관련 spec | 03, 04, 10 |

### R-AUTO-031

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Low |
| 위험 | Intraday Stop Sell approval gate 오구성으로 false 상태에서도 true-path 진입 위험 |
| 대응 | blocked gate 안전 테스트 · Daily Step 12 와 분리 · signal_type 필터. |
| 관련 spec | 03, 04, 05, 10 |

### R-AUTO-032

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Low |
| 위험 | Daily SELL · Intraday 이 동일 executor 호출 시 signal_type 필터 부재로 중복 broker 호출 |
| 대응 | --intraday-stop-only 옵션 · signal_type=INTRADAY_STOP_SELL 만 처리. |
| 관련 spec | 03, 04, 10 |

### R-AUTO-026

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | KIS EGW00201 REJECTED 자동 재시도 미지원 · 주문 이월 · 매수/매도 기회 상실 |
| 대응 | Step 12 retry-normalizer EGW00201 확장 · 자동 재시도 4건 통과. |
| 관련 spec | 03, 04, 10 |

### R-AUTO-027

| 항목 | 값 |
| --- | --- |
| 상태 | 🔵 Accepted |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | APPROVAL_REQUIRED Slack 이 0/0 표시 · 운영자 오해 · 잘못된 다음 단계 결정 위험 |
| 대응 | Slack payload builder 개선 후 Mitigated 승격 후보. |
| 관련 spec | 04, 05, 10 |

### R-AUTO-033

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Low |
| 위험 | port-view Local Spring Boot 실행성 호출 default ENABLE 회귀 · POST 우회 위험 |
| 대응 | feature flag 4종 default false · view_app read-only · Fargate 진입 후속 audit. |
| 관련 spec | 04, 05, 10 |

### R-AUTO-034

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Low |
| 위험 | Fargate Task Role states:StartExecution 광역 부여 · gate default ENABLE 회귀 위험 |
| 대응 | Task Role의 `states:StartExecution` Resource를 허용된 State Machine ARN으로 제한하고 실행 gate 기본값을 false로 유지한다. |
| 관련 spec | 04, 05, 06, 10 |

### R-AUTO-035

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | Daily Brief Slack 자동 발송 실패 · 중복 발송으로 운영자 인지 누락 · 오해 위험 |
| 대응 | Daily Brief는 Dispatcher·Builder·Notifier 책임을 분리하고 Scheduler·Step Functions·Slack 수신을 함께 점검한다. |
| 관련 spec | 04, 05, 10 |

### R-AUTO-036

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Medium |
| 발생 가능성 | Low |
| 위험 | Intraday hard stop 후 Slack 실패 시 READY rollback 없이 warning 만 출력 위험 |
| 대응 | state machine approval gate 통과 후에만 broker 제출 · Notifier IAM 한정. |
| 관련 spec | 03, 04, 06, 10 |

### R-AUTO-037

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Low |
| 위험 | Step 12~17 자동 실행이 Step 1~11 실패 · 데이터 미준비 상태에서 실행될 위험 |
| 대응 | Dispatcher 휴장일 guard와 Step Functions 준비 상태 gate를 유지하고 정상 자동 회차를 계속 관찰한다. |
| 관련 spec | 04, 05, 10 |

### R-AUTO-038

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | Step Functions 실행 이력이 `ops.strategy_daily_batch_run` 에 mirror 되지 않아 View Daily Batch 화면이 AWS Step Functions 실행 이력을 직접 보여주지 못하는 위험 |
| 대응 | Recorder Lambda와 전용 DB Role로 run-level·대표 step mirror를 유지하고 세부 step 확장을 검토한다. |
| 관련 spec | 04, 05, 10 |

## Cost
### R-COST-001

| 항목 | 값 |
| --- | --- |
| 상태 | 🟠 Open |
| 영향도 | Medium |
| 발생 가능성 | Medium |
| 위험 | ALB / NAT / VPC Endpoint multi-AZ / RDS multi-AZ 동시 활성화로 월 비용 급증 |
| 대응 | NAT 미사용 · ALB 초기 미사용 · paper single-AZ 유지 · Cost Explorer 점검. |
| 관련 spec | 02, 05, 10 |

### R-COST-002

| 항목 | 값 |
| --- | --- |
| 상태 | 🟠 Open |
| 영향도 | Medium |
| 발생 가능성 | Low |
| 위험 | IaC / Console 실수로 NAT Gateway 생성 · 시간당 ~$0.06 + 데이터 처리 비용 |
| 대응 | 02 runbook NAT 미사용 확인 · validation-checklist 항목 포함. |
| 관련 spec | 02 |

### R-COST-003

| 항목 | 값 |
| --- | --- |
| 상태 | 🟠 Open |
| 영향도 | Medium |
| 발생 가능성 | Low |
| 위험 | S3 리포트 lifecycle 미설정 · storage 비용 누적 · public read 부주의 노출 위험 |
| 대응 | Job Role PutObject prefix 한정 · S3 lifecycle 정책 도입 후속. |
| 관련 spec | 09, 06, 10 |

## Docs
### R-DOCS-001

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 Open |
| 영향도 | High |
| 발생 가능성 | Medium |
| 위험 | spec 산출물에 secret / token / 계좌번호 / webhook 평문 기록 위험 |
| 대응 | 민감정보는 `[REDACTED]` 계열로만 기록한다. 문서와 git history를 정기 scan한다. |
| 관련 spec | 모든 spec |

### R-DOCS-002

| 항목 | 값 |
| --- | --- |
| 상태 | 🟢 Mitigated |
| 영향도 | High |
| 발생 가능성 | Low |
| 위험 | Windows worker KRX / RDS password 가 PowerShell · wrapper · 로그 파일 평문 노출 위험 |
| 대응 | wrapper password Write-Host 금지 · 로그 secret grep 정기 점검. |
| 관련 spec | 06, 08 |

## Accepted Risks

| 항목 | 값 |
| --- | --- |
| R-AUTO-024 | Slack webhook 환경변수 보관 · 운영 안정화 후 Secrets Manager 또는 SSM SecureString 이전 검토 |
| R-AUTO-027 | APPROVAL_REQUIRED Slack 0/0 표시 · Builder 개선 후 Mitigated 승격 검토 |
| 운영 원칙 | Accepted는 위험을 인지하고 현재 조건에서 수용한 상태이며 정기 재검토 대상 |

## Evidence Management

| 항목 | 값 |
| --- | --- |
| 본 문서 | Risk ID · 상태 · 영향도 · 가능성 · 위험 · 대응 · 관련 spec |
| 상세 실행 근거 | 각 spec `operation-notes.md` |
| 날짜별 작업 | `WORKLOG.md` |
| 결정 변경 | `operator-decisions.md` |
| 비용 상세 | `cost-simulation.md` |
| raw log · SQL · AWS JSON | 본 문서에 기록하지 않음 |
| executionName · ARN · SHA256 | 본 문서에 기록하지 않음 |
| Risk Details | 별도 장문 섹션을 사용하지 않음 |

## Risk Update Rules

| 항목 | 값 |
| --- | --- |
| 새 Risk | 마지막 번호 다음 ID를 부여 |
| ID 재사용 | 금지 |
| 상태 변경 | 검증 근거 확인 후 갱신 |
| Open | 대응이 완료되지 않았거나 정기 실증이 필요한 상태 |
| Mitigated | 대응 적용과 검증이 완료된 상태 |
| Accepted | 위험을 인지하고 현재 조건에서 수용 |
| Closed | 위험 원인이 제거되어 재발 가능성이 없는 상태 |
| 상세 이력 | operation-notes와 WORKLOG에 기록 |
| 본문 길이 | 위험과 대응은 각각 한두 문장으로 유지 |
| 표 형식 | 독립 표는 `항목 / 값` 2열 |
| 민감정보 | `[REDACTED]` 계열 placeholder만 사용 |

## Security Notes

| 항목 | 값 |
| --- | --- |
| 실제 AWS 실행 | 없음 |
| 애플리케이션 코드 수정 | 없음 |
| AWS 리소스 변경 | 없음 |
| broker · KIS · DB 실행 | 없음 |
| 민감정보 원문 | 기록 금지 |
| 허용 표기 | `[REDACTED]` 계열 placeholder |
| 문서 역할 | AWS Migration 공통 Risk Register |
