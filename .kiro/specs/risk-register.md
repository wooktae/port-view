# Risk Register — AWS Migration

본 문서는 PORT-STRATEGY-AI AWS Migration 전체(`01-aws-migration-foundation`, `02-aws-network-and-rds`, 후속 03 ~ 10)에 걸친 운영 / 보안 / 비용 리스크를 단일 표로 누적 관리하는 문서다. spec 진행 중 새 리스크가 식별되면 본 표 아래에 같은 형식으로 추가한다. 한 번 부여한 Risk ID는 재사용하지 않는다.

본 문서에는 실제 secret / password / token / app key / app secret / 계좌번호 / webhook URL 값을 절대 적지 않는다. 모두 `[REDACTED]`만 사용한다.

## 컬럼 정의

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

## 상태 정의

- Open: 식별되었으나 mitigation이 적용되지 않았다.
- Mitigated: mitigation이 적용되어 영향이 작다. 그러나 모니터링은 계속한다.
- Accepted: 의도적으로 수용한다(예: aws-paper single-AZ 다운타임 허용).
- Closed: 구조 변경으로 더 이상 해당하지 않는다.

## Risk Table

| Risk ID | Area | Risk | Impact | Probability | Mitigation | Detection | Rollback | Affected Spec | Status |
|---|---|---|---|---|---|---|---|---|---|
| R-NET-001 | Network | Public subnet에 배치한 ECS Task의 SG inbound를 잘못 열어 외부에 직접 노출 | High | Medium | NAT-free OPT-1 적용 Task SG는 inbound 0.0.0.0/0 절대 미허용. outbound는 KRX/Naver/yfinance/holiday 등 필요 도메인만. SG 생성 시 02 runbook 단계에 따라 inbound 빈 상태로 시작 | VPC Flow Logs로 비정상 inbound 감지. SG 변경 audit(CloudTrail). 정기 SG 점검 SQL/CLI 결과를 운영자 노트에 기록 | 노출된 SG 즉시 inbound rule 제거. Task 재기동. 노출 기간 동안 호출된 endpoint 점검 | 02, 08 | Open |
| R-NET-002 | Network | NAT-free 구조에서 외부 outbound가 필요한 workload(crawler/preprocessor/research)를 private (app) subnet에 잘못 배치해 KRX/Naver/yfinance/holiday 호출 실패 | Medium | Medium | 02 design.md OPT-1~OPT-4 매핑을 운영자 노트에 명시. OD-NET-004 결정과 일치하는 subnet 배치만 허용. tasks.md Phase 1 단계에서 subnet ID 검증 | 첫 실행 시 외부 호출 실패 로그(crawler / preprocessor 외부 API timeout). VPC Flow Logs에서 outbound가 IGW로 나가는지 확인 | Task 재배치(ECS Task Definition의 networkConfiguration.subnets를 public subnet으로 변경). 또는 OPT-3로 승격 | 02, 08 | Open |
| R-NET-003 | Network | VPC Endpoint(ECR api+dkr / Secrets Manager / SSM / CloudWatch Logs) 누락 또는 비활성으로 NAT-free 환경에서 ECR pull / secret 주입 / 로그 송신 실패 | High | Medium | 02 runbook Step의 VPC Endpoint 5종 활성 확인. sg-vpc-endpoints inbound 443 허용 확인. private DNS 활성. validation-checklist의 endpoint state `available` 체크 | ECS Task 기동 실패(`CannotPullContainerError`, `ResourceInitializationError`). Secrets Manager API timeout 로그 | 누락 endpoint 추가 생성. AZ 누락 시 해당 AZ subnet 추가. Task 재기동 | 02, 06, 07 | Open |
| R-SEC-001 | Security | RDS Security Group inbound를 0.0.0.0/0 또는 광범위 CIDR로 열어 RDS가 외부에 노출 | High | Low | sg-rds-postgres inbound는 다른 SG 참조만 허용. 0.0.0.0/0 절대 금지(02 design.md, OD-NET-009 정책). RDS publicly accessible false | SG 변경 audit(CloudTrail). Trusted Advisor 또는 Security Hub의 RDS public exposure 점검 | inbound rule 즉시 제거. RDS master password Secrets Manager에서 즉시 rotate. 연결 로그 점검 | 02 | Open |
| R-DATA-001 | Data | RDS role의 search_path 미설정으로 application이 schema 미지정 SQL 실행 시 테이블 조회 실패 또는 잘못된 schema의 테이블 조회 | High | Medium | 02 runbook Step DB 초기화에서 7개 role 모두에 `ALTER ROLE <role> SET search_path = ...` 적용. 8개 MS README의 search_path 순서 그대로 유지(OD-DB-006). 검증 SQL `SHOW search_path;`를 role별로 실행 | application 실행 시 `relation does not exist` 또는 다른 schema 테이블 사용 정황. 검증 SQL 결과 로그 | role search_path 재설정. 잘못 쓰여진 데이터가 있으면 schema별 row count 비교 후 수동 정리 | 02 | Open |
| R-DATA-002 | Data | `pg_dump` / `pg_restore` cutover 중 테이블 row count 불일치 또는 sequence / default privilege 누락으로 데이터 정합성 깨짐 | High | Medium | 02 runbook Step pg_dump 사전 준비 단계에서 dump 옵션(`--format=custom --no-owner --no-privileges`) 고정. restore 후 design.md "검증 SQL 후보" 실행. 핵심 테이블 row count를 local-dev와 ±0 비교 | 검증 SQL의 schema/table count 차이. application 실행 시 sequence 충돌(`duplicate key`) 또는 권한 오류 | 즉시 cutover 중단. local-dev 환경으로 환경변수 복귀(RDS는 evidence로 보존). 다음 시도에 dump/restore 옵션 재검토 | 02, 10 | Open |
| R-BROKER-001 | Broker | KIS broker가 marketconnector outbound IP 등록을 요구하는데 EIP가 detach되거나 EC2 교체로 IP가 바뀌어 broker 호출 실패 | High | Medium | OD-NET-003에 따라 EC2+EIP 1순위 채택. EIP는 instance에 attach + running 상태 유지(detach 시 미사용 비용 + IP 변경). EC2 교체 시 EIP detach → 새 EC2에 attach Runbook 03에서 정의 | broker API 인증 실패 로그. KIS 측 IP 등록 미일치 알림 | EIP를 새 EC2에 즉시 재attach. broker 측 IP 등록은 동일 EIP라 변경 불필요. EIP 자체 변경 시 broker 측 등록 절차(수동) | 03 | Open |
| R-BROKER-002 | Broker | `access_token.txt` 단일 broker 세션 전제에서 EC2 다중화 또는 ECS Fargate 다중 Task로 토큰 동시 갱신 충돌 발생 | High | Low | OD-MS-001 EC2+EIP 1순위 + 단일 instance 운영 정책. ECS Fargate 옵션 사용 시 동시 1 Task 강제(desiredCount=1, 무중단 배포 시 토큰 인계 절차). 토큰 백업은 EC2 로컬 + S3(OD-SEC-003) | broker 측 토큰 invalid / 강제 만료 응답. 토큰 발급 직후 즉시 invalid되는 패턴 | 토큰 강제 갱신. 한 instance만 운영 중인지 점검. 다중 Task가 떠 있다면 여분 Task 중지 후 재발급 | 03 | Open |
| R-AUTO-001 | Automation | Step Functions Retry 정책을 BUY/SELL/fill sync/position 변경 step에 잘못 활성화해 broker 중복 주문 발생 | High | Medium | OD-SAFE-004 `자동 재시도 금지 step` 명시. 04 spec에서 state machine의 BUY/SELL/fill sync/position 변경/intraday stop SELL 생성 step은 Retry 정책 비활성화. Step Functions 정의 review checklist | `connector_order_request` 단기간 중복 insert. broker 측 동일 종목 동일 가격 중복 주문 알림 | 즉시 해당 Step Functions execution stop. 중복 주문은 broker 측 취소 또는 수동 정리. retry 비활성화로 state machine 재배포 | 04, 10 | Open |
| R-AUTO-002 | Automation | aws-live 환경에서 자동 BUY/SELL이 paper 검증 전에 또는 운영자 승인 없이 조기 활성화되어 실계좌 잘못된 주문 발생 | High | Medium | OD-SAFE-002, OD-SAFE-003 정책: aws-live 초기 자동주문 금지, 후보+View 수동 승인 우선. `PORT_ENVIRONMENT=live` + 자동 활성 flag는 별도 Decision 후 적용. paper N영업일 검증 통과를 10 spec에서 게이트 | broker live 호출 로그 패턴 점검. View 수동 승인 없이 진행된 주문 audit | live 자동매매 flag 즉시 OFF. 발생한 주문은 broker 취소 또는 수동 정리. 원인 분석 후 10 spec 검증 절차 재적용 | 04, 05, 10 | Open |
| R-DOCS-001 | Docs | spec / runbook / 검증 결과 / 운영자 노트에 실제 secret / token / webhook URL / app key / app secret / 계좌번호가 평문으로 기록 | High | Medium | 모든 spec / runbook / decision 문서에 `[REDACTED]` 정책 명시(AGENTS.md). 작성 후 `git diff`에 secret 패턴 grep. PR 단계에서 secret scan | git history secret scan(예: `truffleHog`, `gitleaks`). 운영자 점검 | 즉시 secret rotate(KIS app secret / DB master password / Slack webhook 등). git history 정리 검토. 노출된 token은 broker 측 만료 처리 | 모든 spec | Open |
| R-COST-001 | Cost | ALB / NAT Gateway / VPC Endpoint(multi-AZ) / RDS multi-AZ가 동시에 활성화되어 운영자 결정과 무관하게 월 비용 급증 | Medium | Medium | OD-NET-001/002 NAT 미사용 기본안. OD-NET-007/008 ALB 초기 미사용. OD-RDS-002 paper single-AZ. 02 decision-matrix.md 비용 프로파일 표로 시나리오별 합계 사전 합의. AWS Cost Explorer 일별 그래프 점검 | AWS Billing Dashboard 일별 비용 spike. Cost Anomaly Detection alert | 비계획 리소스 즉시 식별 후 삭제(NAT GW, ALB, 미사용 Endpoint). RDS multi-AZ 전환은 운영자 결정에 한해 유지 | 02, 05, 10 | Open |
| R-COST-002 | Cost | NAT Gateway 미사용 의사결정에도 불구하고 IaC 또는 Console 실수로 NAT Gateway가 생성되어 시간당 ~$0.06 + 데이터 처리 비용 누적 | Medium | Low | 02 runbook Step `NAT Gateway 미사용 확인`을 명시 단계로 분리. tasks.md task 9에 CONFIRMED 기록. validation-checklist 항목 포함 | VPC 콘솔의 NAT Gateways 메뉴에 항목 존재. AWS Billing의 `NatGateway-Hours` 라인 | NAT Gateway 즉시 삭제(EIP는 유지/회수 결정). Route Table에서 NAT 라우트 제거 | 02 | Open |
| R-SEC-002 | Security | Root 계정 자격(비밀번호 / MFA) 분실 또는 노출로 비상 복구 경로 차단 또는 외부 침해 발생 | High | Low | Root MFA 강제 활성(02 runbook Step 0-6). Root access key 발급되어 있으면 즉시 삭제. 일상 작업은 portadmin으로만 수행. account-id / sign-in URL / 결제 수단은 운영자가 외부의 안전한 위치에 보관(본 문서 / 노트에 평문 기록 금지) | AWS account의 Root 로그인 활동 audit(CloudTrail의 `userIdentity.type=Root`). 비정상 IAM 정책 변경 / 결제 정보 변경 알림 | AWS Account recovery 절차(이메일 인증, 결제 정보 인증)로 Root 복구 우선. 복구 전까지 다른 spec 진행 중단. 침해 의심 시 모든 Root access key 삭제 + IAM 사용자 비밀번호 / MFA 재설정 | 02 | Open |
| R-SEC-003 | Security | portadmin 자격(비밀번호 / MFA) 분실로 운영자가 일상 AWS Console / IAM 작업을 수행하지 못해 후속 spec 진행 중단 | High | Low | portadmin MFA 활성 + 백업 코드를 안전한 위치 보관. 비밀번호는 운영자만 알고 있고 본 문서 / 노트에 평문 기록 금지(`[REDACTED]`). Root는 비상용으로 보존하여 portadmin 비밀번호 재설정 경로 유지 | portadmin 로그인 실패 반복 / MFA 코드 검증 실패. CloudTrail의 portadmin 호출 흐름 단절 | Root 재로그인 → IAM → Users → `portadmin` → Manage console access → Reset password → MFA 재등록. 그래도 복구 불가 시 portadmin 삭제 후 동일 권한 신규 사용자 재생성(권한 매트릭스는 06 spec 결정 따름) | 02, 06 | Open |

## 추가 식별 시 갱신 규칙

- 새 리스크는 다음 ID부터 부여한다(예: `R-NET-004`, `R-DATA-003`).
- mitigation이 운영자 결정과 충돌하면 본 spec에서 임의로 결정값을 바꾸지 않고, [`./operator-decisions.md`](./operator-decisions.md)의 해당 Decision ID에 변경 제안만 기록한 뒤 운영자 승인 절차를 거친다.
- Status가 `Mitigated`로 바뀌어도 row를 삭제하지 않는다. detection / rollback이 여전히 유효하므로 운영 회고 자료로 보존한다.
- 본 문서는 후속 spec(03 ~ 10)에서 입력으로 사용된다.

## 본 문서 작업 안전 제약

- 실제 AWS 리소스 생성 / 변경 없음.
- 8개 MS의 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 없음.
- 실제 secret 값 출력 없음(모두 `[REDACTED]`).
- mitigation 제안에 의한 실제 IaC 또는 Console 변경은 본 문서 범위 밖.
