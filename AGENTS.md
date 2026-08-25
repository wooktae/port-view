# port-view 작업 규칙

이 문서는 `port-view` 마이크로서비스의 코드, 설정, 화면, 테스트, 문서를 수정할 때 적용하는 기준이다.

`port-view` 관련 작업은 이 문서만 읽어도 범위, 책임 경계, 안전 기준, 문서 작성 방식과 검증 원칙을 이해할 수 있어야 한다.

## 0. 최우선 문서 가독성 규칙

모든 문서 작업에서 본 섹션을 최우선으로 적용한다.

### 0.0 적용 범위 제한

가독성 규칙은 이번 작업에서 새로 작성하거나 직접 수정하는 부분에만 적용한다.

사용자가 문서 전체 정리나 전수 점검을 명시하지 않은 경우 아래 작업은 수행하지 않는다.

| 항목 | 기본 처리 |
| --- | --- |
| 기존 문서 전체 전수 스캔 | 수행하지 않음 |
| README 전체 재구성 | 수행하지 않음 |
| CHANGELOG 과거 이력 대량 정리 | 수행하지 않음 |
| 신규 scanner · audit 도구 작성 | 수행하지 않음 |
| sub-agent · orchestrator 생성 | 수행하지 않음 |

변경 인접부는 같은 표 행, 같은 bullet 묶음, 같은 짧은 문단까지만 본다.

범위 밖의 기존 위반은 원본을 유지하고 필요 시 후속 후보로만 남긴다.

### 0.1 표 작성 규칙

새로 만드는 독립 요약 표는 기본적으로 2컬럼으로 작성한다.

기본 헤더는 `항목 / 값`이다.

아래 상황에서는 더 구체적인 2컬럼 헤더를 사용할 수 있다.

| 상황 | 우선 헤더 |
| --- | --- |
| 검증 결과 | `항목 / 결과` |
| 파일별 변경 | `파일 / 변경` |
| 화면별 설명 | `화면 / 내용` |
| 설정 정리 | `설정 / 값` |
| endpoint 정리 | `경로 / 역할` |
| 테스트 결과 | `테스트 / 결과` |

기존 표에 행을 추가하는 경우 기존 컬럼 구조를 유지한다.

3컬럼 이상 표는 다음 경우에만 허용한다.

| 조건 | 처리 |
| --- | --- |
| 사용자가 명시적으로 요청 | 요청 구조 사용 |
| 기존 표 보존이 더 안전 | 기존 구조 유지 |
| 비교 구조상 2컬럼 변환 시 의미 손실 | 예외 허용 |

### 0.2 표 셀과 문장 길이

- 표 셀은 2문장 이하로 유지한다.
- 한 셀에 여러 값이 있으면 `<br>`로 나눈다.
- 한 셀에 3개 이상의 사실을 장문으로 넣지 않는다.
- 긴 근거는 표 밖 설명이나 관련 문서 링크로 분리한다.
- 300자 초과 셀과 500자 초과 라인을 만들지 않는다.
- raw log, 전체 SQL 출력, 전체 AWS 응답을 문서에 붙이지 않는다.

### 0.3 문서 밀도

문서 작성 우선순위는 아래를 따른다.

1. 짧은 Summary
2. 짧은 2컬럼 표
3. 짧은 bullet
4. 상세 문서 링크
5. 긴 본문

같은 사실을 README, CHANGELOG, worklog, 상세 문서에 장문으로 반복하지 않는다.

### 0.4 상태 표시

상태 배지는 아래 5종만 사용한다.

| 배지 | 의미 |
| --- | --- |
| 🔴 | 금지 · live · 고위험 |
| 🟠 | 대기 · 관찰 · 미확정 |
| 🟢 | 완료 · 성공 · ENABLED |
| 🔵 | 참고 · 정보 · evidence |
| ⚫ | 해당 없음 |

상태 배지로 충분하면 HTML 색상을 추가하지 않는다.

### 0.5 작업 방식 제한

문서 작업은 아래 순서로 진행한다.

1. 요청 범위 확인
2. 대상 파일 직접 읽기
3. 필요한 부분만 수정
4. UTF-8 No BOM 저장
5. 짧은 after-check
6. 변경 요약 보고

사용자가 명시적으로 요청하지 않는 한 아래 방식은 사용하지 않는다.

- 전체 workspace 전수 스캔
- sub-agent
- orchestrator
- 신규 scanner
- content hash matrix
- workspace 밖 임시 파일
- 과도한 자동화 스크립트

## 1. Scope

### 1.1 기본 작업 디렉터리

`C:\Workspaces\port-view`

### 1.2 기본 수정 대상

| 구분 | 대상 |
| --- | --- |
| Java | `src/main/java` |
| 설정 | `src/main/resources/*.properties` |
| 화면 | `src/main/resources/templates` |
| CSS · 정적 파일 | `src/main/resources/static` |
| 테스트 | `src/test` |
| 문서 | `README.md` · `CHANGELOG.md` · `docs` |
| 빌드 | `pom.xml` · Maven Wrapper |
| 컨테이너 | `Dockerfile` 및 배포 관련 파일 |
| CI/CD | `.devops/codebuild/buildspec.yml` · GitHub Actions workflow |
| 배포 | ECR · ECS Task Definition · 배포 스크립트 |

현재 요청에 포함되지 않은 파일은 수정하지 않는다.

### 1.3 다른 마이크로서비스

아래 프로젝트는 port-view의 외부 의존 모듈이다.

- `port-marketconnector`
- `port-interest-crawler`
- `port-interest-preprocessor`
- `port_strategy_common`
- `port_strategy_decision`
- `port_strategy_research`
- `port_strategy_execution`

현재 작업이 명시적으로 요구하지 않는 한 다른 MS의 코드와 문서는 수정하지 않는다.

`.kiro`의 cross-service spec도 port-view 작업 범위에 자동 포함하지 않는다.

## 2. port-view 책임 경계

port-view는 조회, 승인, 트리거 UI를 담당하는 Spring MVC 기반 View 마이크로서비스다.

### 2.1 담당 범위

| 항목 | 내용 |
| --- | --- |
| Dashboard | 계좌 · 주문 · 포지션 · 전략 결과 요약 |
| Balance | 잔고와 평가금액 조회 |
| Positions | 보유 종목 목록과 상세 조회 |
| Orders | 주문 요청 · 체결 · 이벤트 조회 |
| Strategy | 실행 계획 · Daily Run · Report 조회 |
| Daily Batch | 실행 이력과 Step 결과 조회 |
| Trigger UI | Step Functions 실행 요청 |
| Approval UI | Paper 주문성 구간 승인 트리거 |

### 2.2 직접 담당하지 않는 범위

- broker 주문 제출 로직
- 전략 판단 로직
- 데이터 수집과 전처리
- Step Functions 내부 orchestration
- EventBridge Scheduler 자동 실행
- MarketConnector EC2 내부 명령
- KRX crawler 실행
- aws-live 자동 BUY/SELL

View 코드 안에 다른 MS의 핵심 비즈니스 로직을 복제하지 않는다.

### 2.3 Research Strategy Config Version Promotion

Research Strategy Config Version Promotion은 예외적으로 허용된 운영자 승인형 control action이다.

| 항목 | 기준 |
| --- | --- |
| 성격 | 운영자 명시 승인 기반 제한된 운영 승격 |
| 판단 로직 | Strategy 판단·백테스트 로직 자체는 View에 복제하지 않음 |
| 운영 source of truth | DB status가 아니라 Production Step4가 참조하는 Batch Job Definition command |
| Runtime Contract | `--strategy-config-version` 외 항목을 임의 변경하지 않음 |
| 안전 장치 | dual target preflight · running execution 확인 · fail-closed · after-check · rollback 유지 |
| endpoint | 운영 전환 endpoint를 일반 조회 endpoint와 명확히 분리 |
| 민감정보 | 실제 ARN · account-id · digest를 코드 설명이나 문서에 기록하지 않음 |

## 3. 기술 기준

| 항목 | 기준 |
| --- | --- |
| Java | 25 |
| Spring Boot | 4.1.0-SNAPSHOT |
| Web | Spring MVC |
| Template | Thymeleaf |
| Persistence | Spring Data JPA · JdbcTemplate |
| Database | PostgreSQL |
| Build | Maven Wrapper |
| Container | Docker · ECS Fargate 실증 구성 |

기존 코드의 패키지 구조와 네이밍을 우선 유지한다.

불필요한 프레임워크 도입과 대규모 구조 변경은 하지 않는다.

## 4. 운영 AS-IS 기준

### 4.1 현재 상태

| 항목 | 값 |
| --- | --- |
| ECS Fargate | 1차 포팅과 화면 조회 실증 완료 |
| 현재 운영 성격 | 포트폴리오 실증 상태 유지 |
| 기본 backend | `aws-stepfunctions` |
| Local File backend | 운영자 로컬 검증 · 복구용 |
| Step 1~11 | safe trigger 분리 |
| Step 12~17 | approval trigger 분리 |
| P2 고도화 | 미수행 · 범위 제외 |
| aws-live | 미진행 |

ALB, HTTPS, Route53, 인증, Auto Scaling, Blue/Green, 외부 공개는 현재 완료 상태로 쓰지 않는다.

외부 공개 또는 다중 사용자 운영이 필요할 때 재검토 대상으로 기록한다.

### 4.2 실행 backend

| backend | 역할 |
| --- | --- |
| `aws-stepfunctions` | ECS Fargate와 운영자 View의 기본 실행 경로 |
| `local-file` | 로컬 검증과 복구용 |
| View subprocess | Fargate 운영 경로에서 사용하지 않음 |

Fargate에서 로컬 절대 경로나 다른 MS source directory를 직접 실행하지 않는다.

### 4.3 Paper 주문 gate

Step 12 이상은 Paper 주문이 발생할 수 있는 구간이다.

아래 gate를 우회하거나 기본값을 느슨하게 변경하지 않는다.

- `portfolio.batch.execution-enabled`
- `portfolio.batch.local-file-execution-enabled`
- `portfolio.batch.full-pipeline-execution-enabled`
- `portfolio.batch.paper-order-enabled`
- `portfolio.batch.min-executable-step-order`
- `portfolio.batch.max-executable-step-order`
- `portfolio.batch.aws-stepfunctions-start-enabled`
- `portfolio.batch.aws-stepfunctions-step-start-enabled`

승인 endpoint와 일반 endpoint의 책임을 합치지 않는다.

## 5. Spring 코드 작성 규칙

### 5.1 Controller

- 요청 파라미터 해석과 Model 조립에 집중한다.
- 비즈니스 로직을 Controller에 직접 넣지 않는다.
- 기존 endpoint 이름과 URL 호환성을 우선한다.
- 실행 endpoint는 gate와 running 상태를 먼저 검증한다.
- 사용자 화면에 민감정보를 출력하지 않는다.

### 5.2 Service

- 화면 DTO 조립과 application workflow를 담당한다.
- 외부 모듈 로직을 복제하지 않는다.
- 실패를 성공으로 변환하지 않는다.
- AWS SDK 호출과 local-file 실행 경로를 명확히 분리한다.
- Paper 주문성 동작은 명시적인 승인 gate를 요구한다.

### 5.3 Repository와 SQL

- JPA와 JdbcTemplate 기존 사용 방식을 존중한다.
- 추정 컬럼명으로 SQL을 작성하지 않는다.
- SQL 수정 전 entity, repository, migration 문서 또는 `information_schema.columns` 기준으로 실제 컬럼을 확인한다.
- 여러 schema에 같은 이름의 테이블이 있을 수 있으므로 신규 운영 SQL은 가능한 한 schema-qualified 이름을 사용한다.
- 기존 unqualified SQL은 datasource `search_path`와 정합성을 확인한다.

### 5.4 DTO와 Entity

- 기존 Lombok class DTO와 Java record 사용 패턴을 유지한다.
- 화면 전용 값은 DTO에 둔다.
- Entity를 화면 Model로 직접 노출하지 않는다.
- DB schema와 불일치하는 필드를 추정해서 추가하지 않는다.

### 5.5 Thymeleaf와 CSS

- 기존 page layout과 fragment 구조를 우선 사용한다.
- 상태 표시는 기존 badge 또는 status class 체계를 재사용한다.
- 실행 버튼은 backend와 gate 상태를 명확히 표시한다.
- 위험한 실행 버튼은 일반 조회 버튼과 시각적으로 구분한다.
- 계좌번호, ARN, public IP 등 민감정보를 렌더링하지 않는다.

## 6. DB와 설정 기준

### 6.1 Database

기본 database는 `portfolio`다.

주요 domain schema는 아래와 같다.

- `ops`
- `execution`
- `decision`
- `research`
- `connector`
- `preprocessor`
- `interest`
- `reference`
- `legacy`
- `public`

port-view datasource는 여러 domain schema를 조회한다.

`search_path` 변경 시 Dashboard, Balance, Positions, Orders, Strategy, Daily Batch 화면 영향을 함께 확인한다.

### 6.2 DB user

| 용도 | user |
| --- | --- |
| Spring View datasource | `view_app` |
| MarketConnector subprocess | `marketconnector_app` |

`view_app`에 connector 쓰기 권한을 임의로 추가하지 않는다.

### 6.3 환경변수

실제 값은 저장소에 기록하지 않는다.

주요 설정 범주는 아래와 같다.

- `INTEREST_DB_*`
- `PORTFOLIO_DB_NAME`
- `PORTFOLIO_VIEW_*`
- `PORTFOLIO_BATCH_*`
- `PORTFOLIO_SNAPSHOT_REFRESH_*`
- `SPRING_PROFILES_ACTIVE`
- AWS region과 Step Functions ARN
- Slack 설정
- 기본 계좌번호

설정 키를 추가하거나 변경하면 아래 항목을 함께 확인한다.

1. `application.properties`
2. profile별 properties
3. `@ConfigurationProperties`
4. README 확인
5. 배포 환경변수 이름

## 7. Snapshot Refresh 규칙

Snapshot Refresh는 실행 환경별 책임을 구분한다.

| 환경 | 처리 |
| --- | --- |
| Local View | MarketConnector subprocess 사용 가능 |
| ECS Fargate | 로컬 subprocess 사용 금지 |
| Fargate 조회 | DB에 적재된 Snapshot 조회 중심 |
| 원격 refresh | AWS orchestration 또는 Connector 경로 사용 |

Fargate에서 `C:/Workspaces/...` 같은 로컬 경로를 운영 경로로 사용하지 않는다.

## 8. AWS와 보안 규칙

### 8.1 민감정보

아래 값은 코드, 문서, 예시, 로그에 원문으로 기록하지 않는다.

- secret
- password
- token
- KIS app key · app secret
- Slack webhook URL
- 계좌번호
- AWS account-id
- 실제 ARN
- public IP
- broker 주문번호
- image digest full SHA256

필요한 경우 아래 placeholder를 사용한다.

| Placeholder | 용도 |
| --- | --- |
| `[REDACTED]` | 일반 민감정보 |
| `[REDACTED_ACCOUNT_NO]` | 계좌번호 |
| `[REDACTED_ARN]` | ARN |
| `[REDACTED_SECRET_ARN]` | secret ARN |
| `[REDACTED_TASK_ARN]` | ECS task ARN |
| `[REDACTED_PUBLIC_IP]` | public IP |
| `[REDACTED_BROKER_ORDER_NO]` | broker 주문번호 |
| `<ECR_IMAGE_URI>` | ECR image URI |
| `<ALB_ENDPOINT>` | 향후 ALB endpoint |

### 8.2 실행 제한

사용자가 명시적으로 요청하지 않는 한 아래 작업을 수행하지 않는다.

| 구분 | 금지 작업 |
| --- | --- |
| AWS | 리소스 생성 · 수정 · 삭제 |
| DB | DDL · DML · psql 실행 |
| Broker | 주문 제출 |
| KIS | 외부 API 호출 |
| Slack | 실제 webhook 전송 |
| 운영 | Scheduler · Step Functions · ECS 실행 |
| Git | add · commit · push · reset · restore |

읽기 전용 검증 명령도 사용자 요청 범위에서만 실행한다.

### 8.3 AWS 명령 예시

AWS CLI 예시를 작성할 때는 아래 흐름을 사용한다.

1. `list` 또는 `describe`
2. 식별자 변수 추출
3. 후속 `describe`
4. 상태 검증

ARN, task ARN, ENI ID를 운영자가 직접 끼워 넣어야 하는 예시는 피한다.

### 8.4 PowerShell과 인코딩

- 한글 포함 파일은 UTF-8 No BOM으로 저장한다.
- native command 결과는 `$LASTEXITCODE`로 확인한다.
- 실패한 명령 뒤에 SUCCESS 또는 DONE marker를 출력하지 않는다.
- multiline JSON은 파일 기반 전달을 우선한다.

### 8.5 배포와 CI/CD 안전 기준

배포와 CI/CD 관련 파일도 port-view 기본 수정 대상이다.

| 대상 | 파일 |
| --- | --- |
| CI Build | `.devops/codebuild/buildspec.yml` |
| Workflow | GitHub Actions workflow |
| Image | `Dockerfile` |
| 배포 | ECR · ECS Task Definition · 배포 스크립트 |

배포 검증 규칙:

- Candidate 검증은 배치 · Step Functions · Paper 주문 · Strategy Execution 제출을 비활성화한 상태에서 수행한다.
- Candidate Revision을 그대로 운영에 승격하지 않는다.
- 운영 승격용 Revision은 기존 운영 환경변수와 검증된 Image Digest를 결합해 별도로 만든다.
- 운영 승격 전 이전 정상 Revision을 Rollback 기준으로 저장한다.
- Candidate · 운영 · Rollback Smoke Test는 실제 View 메뉴 경로 기준으로 수행한다.
- 배포 Slack 알림은 현재 필수 완료 기준이 아니며, 별도 채널 정책 확정 후 적용한다.

main push 기반 Release Workflow 규칙:

- main push 기반 Workflow라도 Production 승격은 `production` 승인 Gate 뒤에서만 수행한다.
- Candidate Smoke 성공 전 Production Promotion을 수행하지 않는다.
- Candidate에서 검증한 Artifact와 Production에 사용하는 Image Digest가 동일해야 한다.
- Promotion 시 현재 Operating Task Definition을 기준으로 운영 설정을 유지한다.
- Research Version UI 변경 시 Candidate · Production Smoke에서 Strategy Report 경로를 확인한다.

GitHub OIDC Trust Policy, IAM 권한 구성, Environment Required Reviewer 설정과 Runner IP 임시 SG 허용 등 DevOps 심화 구현 상세는 port-devops 책임 범위이며 이 문서에 기록하지 않는다.

실제 CI/CD 실행, ECR Push, ECS 승격과 Rollback은 8.2 실행 제한을 따른다. 사용자가 명시적으로 승인한 배포 작업에서만 실행한다.

## 9. 테스트와 검증

변경 범위에 맞는 최소 검증을 수행한다.

| 변경 대상 | 최소 검증 |
| --- | --- |
| Java | compile 또는 관련 test |
| Controller | endpoint mapping · gate 조건 |
| Service | 성공 · 실패 · 차단 경로 |
| Repository | query 문법 · schema · mapping |
| Thymeleaf | template parse · 변수명 정합 |
| Properties | binding 이름 · 기본값 |
| README · CHANGELOG | 링크 · 사실 정합 · 가독성 |
| Docker | build context와 runtime 설정 |

전체 테스트가 과도한 경우 관련 모듈 테스트부터 수행한다.

실행하지 못한 검증은 완료로 쓰지 않고 미수행 사유를 남긴다.

## 10. 문서 관리 규칙

### 10.1 README.md

README는 port-view의 현재 구조와 사용 방법을 설명한다.

README에 포함할 내용:

- 기술 스택
- 주요 화면
- 책임 경계
- 현재 운영 AS-IS
- 실행 backend
- 설정
- DB와 schema
- 로컬 실행
- ECS Fargate 실증 상태
- 보안과 안전 gate
- 상세 문서 링크

cross-service 실행 이력은 길게 복사하지 않는다.

현재 port-view에 직접 영향을 주는 결과만 짧게 반영한다.

### 10.2 CHANGELOG.md

CHANGELOG는 port-view 코드와 문서의 변경 이력만 기록한다.

- 최신 날짜를 상단에 추가한다.
- CHANGELOG 업데이트 요청 시 지정 날짜 또는 지정 commit 범위의 Git 이력을 읽기 전용으로 확인한다.
- Git 이력에서 실제 port-view 코드, 설정, 테스트, 빌드, 문서에 반영된 변경만 기록한다.
- `Added`, `Changed`, `Fixed`, `Removed`, `Security`를 필요에 따라 사용한다.
- 다른 MS와 Step Functions 내부 변경은 port-view 코드나 문서가 실제 변경된 경우에만 기록한다.
- raw log와 일회성 실행 식별자는 기록하지 않는다.
- 새 섹션부터 2컬럼 표 중심으로 작성한다.
- 과거 이력은 별도 요청이 없으면 원본을 유지한다.

### 10.3 docs

상세 설명은 아래 문서로 분리한다.

| 문서 | 역할 |
| --- | --- |
| `docs/source-file-catalog.md` | 주요 파일과 디렉터리의 역할 |

새 문서를 만들기 전에 기존 문서에 흡수 가능한지 먼저 확인한다.

날짜별 `docs/worklog/*.md`는 새로 만들지 않는다.

port-view 코드와 문서의 변경 이력은 `CHANGELOG.md`에 기록하고, 상세 설계나 운영 기준은 해당 `docs` 문서에 반영한다.

### 10.4 source-file-catalog.md 자동 갱신

아래 변경이 발생하면 같은 작업에서 `docs/source-file-catalog.md` 갱신 여부를 반드시 확인한다.

| 변경 | 처리 |
| --- | --- |
| 주요 Java 파일 신규 생성 · 삭제 · 이름 변경 | 카탈로그 갱신 |
| Controller · Service · Repository 책임 변경 | 역할과 주의사항 갱신 |
| package 또는 디렉터리 구조 변경 | 경로와 구조 설명 갱신 |
| 주요 template · CSS 파일 신규 생성 · 삭제 | 화면 파일 목록 갱신 |
| properties · Docker · build 파일 역할 변경 | 설정과 빌드 항목 갱신 |
| 문서 파일 신규 생성 · 삭제 · 역할 변경 | Documents 항목 갱신 |
| 내부 구현만 변경되고 파일 책임이 동일 | 갱신 생략 가능 |

카탈로그는 모든 파일을 나열하는 inventory가 아니다.

운영과 유지보수에 의미 있는 파일, 묶음 경로와 책임만 기록한다.

변경이 없으면 CHANGELOG에 카탈로그 미변경 사실을 반복 기록하지 않는다.

## 11. Git 규칙

기본적으로 읽기 전용 상태 확인만 허용한다.

| 허용 | 용도 |
| --- | --- |
| `git status --short` | 현재 작업 트리 변경 상태 확인 |
| `git diff --stat` · `git diff --check` | 수정 범위와 공백 오류 확인 |
| `git log` · `git show` · `git diff <commit>` | 사용자 요청 범위의 변경 이력과 CHANGELOG 근거 확인 |

아래 Git write 명령은 사용자가 명시적으로 요청하지 않는 한 실행하지 않는다.

- `git add`
- `git commit`
- `git push`
- `git reset`
- `git restore`
- `git checkout`
- `git stash`

## 12. 완료 보고

작업 완료 시 아래만 짧게 보고한다.

| 항목 | 내용 |
| --- | --- |
| 변경 파일 | 실제 수정한 파일 |
| 핵심 변경 | 기능 또는 문서 변경 요약 |
| 검증 | 실행한 test · compile · diff check |
| 미수행 | 실행하지 못한 검증 |
| 보안 | 민감정보 원문 기록 여부 |
| 후속 | 실제로 남은 항목만 기록 |

운영자가 수행한 작업과 Kiro가 수행한 작업을 구분한다.

## 13. 완료 체크리스트

- [ ] 요청된 port-view 파일만 수정했는가?
- [ ] 다른 MS와 `.kiro` 파일을 불필요하게 수정하지 않았는가?
- [ ] 최우선 문서 가독성 규칙을 적용했는가?
- [ ] 신규 독립 표를 기본 2컬럼으로 작성했는가?
- [ ] 긴 셀과 긴 라인을 만들지 않았는가?
- [ ] port-view 책임 경계를 유지했는가?
- [ ] Fargate와 local-file backend를 혼동하지 않았는가?
- [ ] Step 12~17 Paper 주문 gate를 우회하지 않았는가?
- [ ] schema와 DB user 정합을 확인했는가?
- [ ] 민감정보 원문을 기록하지 않았는가?
- [ ] 실패를 성공으로 기록하지 않았는가?
- [ ] 변경 범위에 맞는 검증을 수행했는가?
- [ ] 파일 구조나 책임이 바뀌었다면 `docs/source-file-catalog.md`를 갱신했는가?
- [ ] 날짜별 `docs/worklog/*.md`를 새로 만들지 않았는가?
- [ ] UTF-8 No BOM으로 저장했는가?
- [ ] 실제 수정 내용만 README와 CHANGELOG에 반영했는가?
