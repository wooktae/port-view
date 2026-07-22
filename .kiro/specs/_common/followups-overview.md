# PORT-STRATEGY-AI AWS Migration 후속 Spec 개요

## Purpose

본 문서는 `01-aws-migration-foundation` spec 이후의 후속 spec 9개 큰 그림을 정리하는 단일 진실원(source of truth) 이다. spec 사이의 의존성과 진행 순서를 한눈에 보기 위한 인덱스 역할을 한다.

문서 흐름: **Purpose → Current Follow-up Dashboard(Now · Next · Later · Blocked · Done Recently) → Spec Roadmap → 이력 관리 원칙 → Update Rules → Security Notes**.

긴 배경과 날짜별 완료 이력은 `WORKLOG.md`, 상세 실행 근거는 각 spec의 `operation-notes.md`, 결정은 `operator-decisions.md`, 리스크는 `risk-register.md`에서 관리한다. 본 문서에는 현재 후속 작업과 spec 진행 순서만 유지한다.

운영자 결정 변경(2026-06 갱신) 에 맞춰 환경 모델은 `local-dev` · `aws-paper` · `aws-live` 3개로 정리되었고, NAT Gateway 는 paper / live 모두 기본 미사용이다. AWS dev 환경은 별도 구축하지 않는다.

각 spec은 별도 폴더(`02-` ~ `10-`)로 관리한다.

본 문서는 후속 작업 인덱스이며 실제 AWS 리소스 생성, IaC 작성, 애플리케이션 코드 수정은 다루지 않는다.

민감정보 원문은 기록하지 않고 `[REDACTED]` 계열 placeholder만 사용한다.

## Current Follow-up Dashboard

운영자가 매일 조회하는 후속 작업 요약이다. 긴 배경과 날짜별 완료 이력은 `WORKLOG.md`, 상세 실행 근거는 각 spec `operation-notes.md`로 분리한다.

### 🟠 Now — 즉시 착수

#### OPS Mirror 세부화

| 항목 | 값 |
| --- | --- |
| 현재 | run-level + 대표 workflow step mirror 완료 |
| 후속 | 전체 세부 step으로 확장 |
| 구분 필요 | 자동 실행 run · 수동 복구 run |
| 보호 장치 | Recorder Lambda 실패 CloudWatch Alarm 검토 |
| 관련 spec | 04 |

#### 운영 데이터·Slack 후속

| 항목 | 값 |
| --- | --- |
| 잔고 정합 | 10분 잔고 스냅샷과 주문·체결·Position 보정 연계 검토 |
| Daily Brief | 다음 평일 07:50 장전 Slack 수신 확인 |
| 종목 표시 | 보유 종목 존재 시 종목별 표시 재확인 |
| Holiday Guard | API fallback 정책 결정 |
| stale 데이터 | `connector_position_snapshot` 정리 또는 최신 balance 기준 판정 보완 |
| 손절 Slack | 실제 hard stop 조건에서 `INTRADAY_STOP_LOSS` 실이벤트 확인 |
| 승인 Slack | 다음 Step 1~11 정기 회차 `APPROVAL_REQUIRED` 실전 수신 확인 |
| 관련 spec | 03, 04, 05 |

#### 주문 검증 잔여 축

| 항목 | 값 |
| --- | --- |
| 완료 | Plan → Order → Connector Request → Fill → Position validator |
| 잔여 | Signal Order Map · Broker 접수 구간 |
| NO_TARGET | 정상 무주문은 명시 성공 처리 |
| fail-closed | 실제 주문 있는 회차는 체인 불일치 시 실패 유지 |
| 관련 spec | 03, 04 |

### 🟠 Next — 준비 완료 후 착수

| 항목 | 값 |
| --- | --- |
| View 배포 | Dockerfile · ECR tag · Task Definition · Service 정식화 |
| Daily orchestration | State Machine · Scheduler 구축·ENABLED 완료 · 실패 전파(ECS ExitCode · SFN Fail · 실패 Slack) 정합 구조화 |
| Intraday | 실 보유 포지션 발생 후 1주 Stop Sell 주문 테스트 |
| KRX | 정상 자동 회차에서 Python 실패 전파 · DB validator 실운영 관찰 |
| Approval Slack | 0/0 대신 실 값 · KRX 기준일 표시 |
| Daily Batch UI | payload·계좌·ARN redaction · UTC→KST · 운영자 친화 문구 |
| Dispatcher | runDate · scheduleType · 휴장일 skip 구조화 로그 |
| stale order | 2026-04-27 ACCEPTED 요청 6건 cleanup |
| View backend | ECS에서 Local File 실행 제외 또는 운영자 전용 유지 결정 |
| 관련 spec | 03, 04, 05, 06, 07, 08, 10 |

#### P2 — View 운영 보안·표시 고도화 (범위 제외 결정)

ALB · HTTPS · Route53 · 인증 · Auto Scaling · Blue/Green · UI 표시 고도화 · 외부 공개 운영은 현재 활성 후속 작업에서 제외한다. 실패·미완료가 아니라 운영자의 의도적인 범위 제외 결정이다.

| 항목 | 값 |
| --- | --- |
| P2 상태 | 미수행 |
| 현재 범위 | ECS Fargate 1차 실증 상태 유지 |
| 사유 | 개인 운영·포트폴리오 시연 목적 |
| 재검토 | 외부 공개 또는 다중 사용자 운영 필요 시 |

### 🟠 Later — 장기 후보

| 항목 | 값 |
| --- | --- |
| 공통 패키지 | `port_strategy_common` wheel · CodeArtifact · version 관리 |
| Research adapter | 내부 adapter 3개 공통 패키지 이동 |
| Heavy job | `block_watch_*` · `block_exception_buy_*` 운영 절차 |
| S3 | lifecycle · KMS encryption 정책 |
| Holiday | 백업 경로 · fallback 정책 |
| Slack | DLQ · retry · CloudWatch Alarm |
| Intraday | `INTRADAY_STOP_SELL` 자동 ENABLE 진입 |
| DB 운영 | pgAdmin4 paper/live 분리 등록 표준화 |
| KRX | GUI 수집 headless 리팩토링 |
| Live | 자동화 cutover phase 재검토 |

### 🔴 Blocked — 진입 차단

#### aws-live 자동 BUY / SELL

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 차단 |
| 막힘 사유 | live 자동 재시도 금지 정책 · paper 안정 회차 부족 |
| 해제 조건 | paper 자동화 7종 안정 회차 누적 · 10 spec phase 진입 |
| 근거 | OD-SAFE-002 · OD-SAFE-003 |

#### DB password rotate

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 차단 |
| 막힘 사유 | 06 spec 승인 대기 |
| 선행 조건 | secret loader · 환경변수 재검증 |
| 해제 조건 | 운영자 승인 후 rotation |
| Risk | R-SEC-010 |

#### Slack webhook secret 이전

| 항목 | 값 |
| --- | --- |
| 상태 | 🔴 차단 |
| 현재 | Lambda 환경변수 평문 |
| 해제 조건 | 06 spec phase · Notifier IAM 권한 추가 |
| Risk | R-AUTO-024 |

#### 실후보 기반 검증·로컬 환경

| 항목 | 값 |
| --- | --- |
| retry-normalizer | REQUESTED · retry 실후보 발생 전까지 차단 |
| 실후보 조건 | 장종료 REJECTED · `40580000` · `EGW00201` |
| psql PATH | 운영자 로컬 PC 미등록 |
| 해제 조건 | `setx PATH` 또는 GUI 등록 |
| Risk | R-AUTO-013 |

### 🟢 Done recently — 최근 완료

#### 2026-07-22 — Paper Daily 정상 자동 회차 성공 · 1차 안정화 완료

| 항목 | 값 |
| --- | --- |
| Step 1~11 | 정상 Scheduler 자동 실행 SUCCEEDED |
| Step 12~17 | 정상 Scheduler 자동 실행 SUCCEEDED |
| DB after-check | Daily Run COMPLETED · Execution Plan READY |
| validator | READY Plan → Order · Order Chain 자동 통과 |
| 주문·체결 | 한국전력 83주 BUY FILLED · Position 83주 OPEN |
| Slack·OPS | 성공 Slack 자동 수신 · 성공 기록 확인 |
| P1 acceptance | 다음 정상 자동 회차 end-to-end 관찰 완료 |
| 선언 | Paper Daily 1차 안정화 완료 |
| P2 결정 | View 운영 보안·표시 고도화 미수행 (범위 제외) |
| Evidence | [04 operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) |

#### 2026-07-21 — 정상 무주문 validator 보완

| 항목 | 값 |
| --- | --- |
| 문제 | 정상 주문 0건 회차를 `NO_EXECUTION_ORDER_TARGET`로 FAILED 처리 |
| 수정 | State Machine command에 `--allow-no-target` 추가 |
| 검증 | ASL OK · 배포 재조회 · validator 단독 ECS smoke ExitCode 0 |
| 보존 | 09:01 자동 execution FAILED 이력 유지 |
| 미수행 | 전체 Step 12~17 재실행 없음 |
| Evidence | [04 operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) |

#### 2026-07-21 — KRX 실패 전파·DB validator

| 항목 | 값 |
| --- | --- |
| Python | 실패·부분 오류·미정 상태 non-zero exit |
| runner | expected trade date · Program 1건 · Shortsell 최소 300건 검증 |
| 단독 결과 | Program 1건 · Shortsell 349건 · ExitCode 0 |
| Risk | R-DATA-017 Open → Mitigated |
| 미수행 | crawler 재수집 · 전체 State Machine 재실행 없음 |
| Evidence | [08 operation-notes](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) |

#### 2026-07-20 — Step 13 복구·P0 강화

| 항목 | 값 |
| --- | --- |
| 장애 | `EGW00201` rate-limit |
| 복구 | Step 12 미재실행 · Step 13~17 수동 완주 |
| 체결 | 매도 2건 FILLED |
| 보완 | 조회 간격 · 제한 polling · Step 14~16 fail-closed |
| Slack | 실패 경로 · 성공 체결 목록 표시 |
| Evidence | [03 operation-notes](../03-marketconnector-ec2/operation-notes.md) · [04 operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) |

#### 2026-07-15 ~ 2026-07-16 — KST·Daily 자동화 실증

| 항목 | 값 |
| --- | --- |
| KST 보완 | naive 날짜 오판 제거 · ECS `TZ=Asia/Seoul` |
| BUY E2E | 후보 2건 · 주문 제출 · 체결 · Fill · Position · balance refresh |
| Slack | Approval · Daily Brief 실전 수신 |
| Evidence | [04 operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) |

#### 2026-07-01 ~ 2026-07-09 — 자동화 기반선

| 항목 | 값 |
| --- | --- |
| Scheduler | 7종 ENABLED · Asia/Seoul · 평일 |
| OPS Mirror | Recorder Lambda · run-level · 대표 step |
| View | Daily Batch / OPS Mirror 조회 확인 |
| KRX | Windows EC2 timezone KST 변경 · 최신성 회복 |
| Evidence | [04 operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) · [05 operation-notes](../05-port-view-ecs-and-runbook/operation-notes.md) · [08 operation-notes](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) |

## Spec Roadmap

본 섹션은 02~10 spec의 역할과 다음 진입점을 빠르게 확인하기 위한 인덱스다. 날짜별 실행 이력과 상세 검증 결과는 각 spec의 `operation-notes.md`에서 관리한다.

### 환경 모델

| 항목 | 값 |
| --- | --- |
| `local-dev` | 로컬 개발·단위 검증 환경 · AWS 리소스 없음 |
| `aws-paper` | KIS 모의투자 기반 AWS 운영 검증 환경 |
| `aws-live` | paper 검증 이후 진입하는 실계좌 환경 |
| AWS dev | 별도 구축하지 않음 |
| 자동 재시도 | idempotent step만 허용 |
| 주문 관련 step | BUY · SELL · Fill Sync · Position 변경 · Intraday Stop SELL 자동 재시도 금지 |
| NAT Gateway | paper · live 모두 기본 미사용 |

### 진행 순서

| 순서 | Spec |
| --- | --- |
| 1 | `02-aws-network-and-rds` |
| 2 | `06-secrets-and-iam` |
| 3 | `03-marketconnector-ec2` |
| 4 | `08-interest-crawler-and-preprocessor-ecs` |
| 5 | `04-strategy-batch-stepfunctions` |
| 6 | `05-port-view-ecs-and-runbook` |
| 7 | `09-strategy-research-batch` |
| 8 | `07-cicd-pipelines` |
| 9 | `10-cutover-and-validation-runbook` |

### 02-aws-network-and-rds

| 항목 | 값 |
| --- | --- |
| 목적 | VPC · Subnet · Route Table · Security Group · RDS 기반 공통 인프라 확정 |
| 대상 | 8개 MS 공통 |
| 핵심 결정 | NAT-free · Private RDS · schema-per-domain · paper/live 분리 |
| 현재 상태 | aws-paper 기반 구축 및 운영 검증 진행 |
| 핵심 후속 | DB password rotation · app role 권한 정리 · live RDS 설계 |
| 선행 조건 | `01-aws-migration-foundation` |
| 다음 연결 | 06 · 03 · 08 · 04 · 05 · 09 · 10 |
| 상세 | [operation-notes](../02-aws-network-and-rds/operation-notes.md) |

### 06-secrets-and-iam

| 항목 | 값 |
| --- | --- |
| 목적 | Secrets Manager · SSM Parameter Store · IAM Role/Policy 기준 확정 |
| 대상 | 8개 MS 공통 |
| 핵심 결정 | Role 기반 접근 · Access Key 미사용 · Resource/Action 최소 권한 |
| 현재 상태 | paper 주요 Role·Secret 적용 |
| 핵심 후속 | DB password rotate · Slack webhook secret 이전 · 권한 audit |
| 선행 조건 | 02 |
| 다음 연결 | 03 · 04 · 05 · 07 · 08 · 09 · 10 |
| 상세 | [operation-notes](../06-secrets-and-iam/operation-notes.md) |

### 03-marketconnector-ec2

| 항목 | 값 |
| --- | --- |
| 목적 | MarketConnector를 EC2 + EIP로 운영 |
| 대상 | `port-marketconnector` |
| 핵심 결정 | broker 등록 IP 고정 · 단일 token/session · SSM 운영 |
| 현재 상태 | 조회 · 주문 제출 · 체결 조회 · 잔고 refresh 운영 경로 검증 |
| 핵심 후속 | Intraday Stop Sell 실포지션 검증 · stale request 정리 |
| 선행 조건 | 02 · 06 |
| 다음 연결 | 04 · 05 · 10 |
| 상세 | [operation-notes](../03-marketconnector-ec2/operation-notes.md) |

### 08-interest-crawler-and-preprocessor-ecs

| 항목 | 값 |
| --- | --- |
| 목적 | Crawler와 Preprocessor의 AWS 실행 모델 확정 |
| 대상 | `port-interest-crawler` · `port-interest-preprocessor` |
| 핵심 결정 | non-GUI는 ECS Fargate · KRX GUI는 Windows EC2 worker |
| 현재 상태 | Hybrid execution · KRX 실패 전파 · DB validator 적용 |
| 핵심 후속 | 정상 자동 회차 실운영 관찰 · headless 리팩토링 장기 검토 |
| 선행 조건 | 02 · 06 |
| 다음 연결 | 04 · 05 · 10 |
| 상세 | [operation-notes](../08-interest-crawler-and-preprocessor-ecs/operation-notes.md) |

### 04-strategy-batch-stepfunctions

| 항목 | 값 |
| --- | --- |
| 목적 | Daily Batch와 Intraday 흐름을 Step Functions + ECS로 운영 |
| 대상 | `port_strategy_decision` · `port_strategy_execution` |
| 핵심 결정 | Standard workflow · 주문 step 자동 Retry 금지 · 승인/실패 경로 분리 |
| 현재 상태 | Step 1~11 · Step 12~17 정상 자동 회차 성공 · Paper Daily 1차 안정화 완료 |
| 핵심 후속 | OPS Mirror 세부화 · 장기 무장애 회차 누적 관찰 |
| 선행 조건 | 02 · 06 · 03 · 08 |
| 다음 연결 | 05 · 10 |
| 상세 | [operation-notes](../04-strategy-batch-stepfunctions/operation-notes.md) |

### 05-port-view-ecs-and-runbook

| 항목 | 값 |
| --- | --- |
| 목적 | port-view를 ECS Fargate 운영 콘솔로 이전 |
| 대상 | `port-view` |
| 핵심 결정 | View는 화면·제어 역할 · 실제 Batch는 Step Functions 실행 |
| 현재 상태 | ECS Fargate 1차 실증 상태 유지 · Dashboard/OPS Mirror 조회 검증 |
| 핵심 후속 | P2 운영 보안·표시 고도화는 범위 제외 (외부 공개·다중 사용자 시 재검토) |
| 선행 조건 | 02 · 04 · 06 |
| 다음 연결 | 07 · 10 |
| 상세 | [operation-notes](../05-port-view-ecs-and-runbook/operation-notes.md) |

### 09-strategy-research-batch

| 항목 | 값 |
| --- | --- |
| 목적 | 장시간 backtest와 report 생성을 AWS Batch로 운영 |
| 대상 | `port_strategy_research` |
| 핵심 결정 | AWS Batch + S3 · Step Functions 보조 |
| 현재 상태 | AWS Batch 1차 실행 검증 완료 |
| 핵심 후속 | S3 lifecycle · KMS · heavy job 운영 절차 |
| 선행 조건 | 02 · 06 |
| 다음 연결 | 07 · 10 |
| 상세 | [operation-notes](../09-strategy-research-batch/operation-notes.md) |

### 07-cicd-pipelines

| 항목 | 값 |
| --- | --- |
| 목적 | GitHub Actions → ECR → ECS/EC2 배포 표준화 |
| 대상 | 8개 MS 공통 |
| 핵심 결정 | paper 자동 배포 · live 수동 승인 · OIDC Role |
| 현재 상태 | 후속 진입 대기 |
| 핵심 후속 | 공통 workflow · image promotion · rollback · package version 관리 |
| 선행 조건 | 02 · 06 · 배포 대상 MS 1개 이상 안정화 |
| 다음 연결 | 10 |
| 상세 | [spec 폴더](../07-cicd-pipelines/) |

### 10-cutover-and-validation-runbook

| 항목 | 값 |
| --- | --- |
| 목적 | local-dev → aws-paper → aws-live 전환과 rollback 절차 확정 |
| 대상 | 8개 MS 공통 |
| 핵심 결정 | paper 검증 기간 · live 진입 기준 · rollback trigger |
| 현재 상태 | aws-live 진입 차단 유지 |
| 핵심 후속 | paper 안정 회차 누적 후 live cutover phase 설계 |
| 선행 조건 | 02~09의 live 대상 항목 paper 검증 완료 |
| 다음 연결 | 최종 운영 전환 |
| 상세 | [spec 폴더](../10-cutover-and-validation-runbook/) |

### 의존성

```text
01 Foundation
  ├─ 02 Network & RDS
  └─ 06 Secrets & IAM
        ↓
03 MarketConnector
        ↓
08 Crawler & Preprocessor
        ↓
04 Strategy Batch
        ↓
05 View
        ↓
09 Research
        ↓
07 CI/CD
        ↓
10 Cutover
```

## 이력 관리 원칙

| 항목 | 값 |
| --- | --- |
| 본 문서 | 현재 후속 작업 · 차단 항목 · 최근 완료 · spec 로드맵만 유지 |
| 날짜별 작업 | 루트 `WORKLOG.md`에 기록 |
| 문서 변경 | 루트 `CHANGELOG.md`에 기록 |
| 상세 실행 근거 | 각 spec의 `operation-notes.md`에 기록 |
| 결정 변경 | `operator-decisions.md`에 기록 |
| 리스크 변경 | `risk-register.md`에 기록 |
| 오래된 완료 | 본문에 누적하지 않고 WORKLOG와 operation-notes 링크로 대체 |

## Update Rules

| 항목 | 값 |
| --- | --- |
| 새 후속 | Now · Next · Later · Blocked 중 한 곳에 추가 |
| 완료 항목 | Done Recently에 추가 |
| Done Recently 범위 | 최근 핵심 완료 5~10개만 유지 |
| 상세 설명 | 2열 표의 짧은 사실형 문장으로 작성 |
| 긴 실행 이력 | 해당 spec `operation-notes.md`로 분리 |
| 중복 금지 | Dashboard와 Spec Roadmap에 같은 실행 이력을 반복하지 않음 |
| 상태 변경 | 기존 항목의 상태와 다음 행동을 갱신 |
| 민감정보 | `[REDACTED]` 계열 placeholder만 사용 |

## Security Notes

| 항목 | 값 |
| --- | --- |
| 실제 AWS 실행 | 본 문서 범위 밖 |
| 코드 수정 | 본 문서 범위 밖 |
| broker · KIS · DB 실행 | 금지 |
| aws-live 자동 주문 | paper 검증과 별도 승인 전까지 금지 |
| 자동 재시도 | idempotent step만 허용 |
| 민감정보 원문 | 기록 금지 |
| AWS dev 환경 | 구축하지 않음 |
