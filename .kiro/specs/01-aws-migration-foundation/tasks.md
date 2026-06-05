# Implementation Plan

본 작업 계획은 **이 spec(`01-aws-migration-foundation`) 한정**의 실행 단위다. 모든 task는 산출물이 문서이거나 정적 분석 결과이며, 실제 AWS 리소스 생성, 8개 MS의 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정, 8개 MS entrypoint 실행은 본 spec 범위가 아니다. AWS 리소스 생성을 동반하는 작업은 모두 후속 spec(`02-` 이후)으로 분리되며 본 spec에는 포함하지 않는다.

각 task는 운영자 1인이 단일 세션에서 검토하고 승인할 수 있도록 작은 단위로 나눠 두었다. task 본문의 들여쓰기 하위 항목은 모두 동일 task 안의 acceptance 항목이다.

- [ ] 1. 8개 MS 인벤토리 표 작성
  - 8개 MS 각각에 대해 언어 / 프레임워크 / entrypoint 종류(서비스/배치/CLI) / 외부 의존성 / DB schema / `search_path` / 민감정보 항목을 한 표로 정리한다.
  - 산출물 위치: `design.md` 안의 별도 부록 섹션(`Appendix A: MS Inventory`).
  - 참고 문서: 8개 MS의 README와 AGENTS.md(읽기 전용).
  - 외부 호출 / 코드 실행 금지.
  - _Requirements: 1, 2, 3_

- [ ] 2. MS별 컴퓨트 후보 비교 표 확정
  - 8개 MS 각각에 대해 최소 2개 AWS 컴퓨트 후보, 권고, 권고 사유를 표로 확정한다.
  - 산출물 위치: `design.md`의 `MS별 컴퓨트 후보 비교 및 권고` 섹션을 표 형식으로 보강한다.
  - `port-marketconnector`는 broker IP / 토큰 단일성 근거를 명시하고 EC2를 1순위 후보 중 하나로 유지한다.
  - `port_strategy_common`은 별도 컴퓨트 없음, packaging 권고만 명시한다.
  - 외부 호출 / 코드 실행 금지.
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8_

- [ ] 3. RDS for PostgreSQL 전환 결정 사항 확정
  - 환경별 RDS 인스턴스(`portfolio-dev`, `portfolio-paper`, `portfolio-live`) 권고를 확정한다.
  - 10개 schema 유지 정책, MS별 `search_path` 유지 정책을 본문에 명시한다.
  - 데이터 이전을 `pg_dump` / `pg_restore` 1순위, AWS DMS 2순위로 확정한다.
  - 백업 정책(automated backup retention, manual snapshot, PITR)을 환경별로 명시한다.
  - 산출물 위치: `design.md`의 `RDS for PostgreSQL 전환 설계` 섹션 보강.
  - 실제 RDS 생성 금지. 실제 dump / restore 금지.
  - _Requirements: 2.1, 2.2, 2.4, 2.5, 2.6, 2.7_

- [ ] 4. DB Role 매트릭스 초안 작성
  - MS별 DB role(예: `marketconnector_app`, `crawler_app`, `preprocessor_app`, `decision_app`, `execution_app`, `research_app`, `view_app`)에 대해 schema별 read / write 권한 매트릭스를 표로 작성한다.
  - 환경변수 키(`INTEREST_DB_USER`, `INTEREST_DB_PASSWORD`)는 그대로 유지하고 값만 role별로 분기하는 정책을 명시한다.
  - 산출물 위치: `design.md`의 `DB Role / 권한` 섹션 표 형식으로 확정.
  - 실제 GRANT / REVOKE 실행 금지.
  - _Requirements: 2.3, 3.6_

- [ ] 5. 민감정보 외부화 매핑 표 확정
  - Secrets Manager / SSM Parameter Store 사용 기준을 표로 확정한다.
  - 항목: KIS app key / app secret / base URL / 계좌번호 / 계좌 상품 코드 / `access_token.txt` / `INTEREST_DB_PASSWORD` / Slack webhook / Naver API client secret / `INTEREST_DB_HOST`,`PORT`,`NAME` / `PORTFOLIO_DB_NAME` / `INTEREST_DB_USER` / `PORT_ACCOUNT_NO` / `PORT_BROKER_NAME` / `PORT_ENVIRONMENT` / `PORT_STRATEGY_NAME` / `PORT_STRATEGY_VERSION` / `PORT_MAX_ORDER_AMOUNT_RATIO` / `PORT_MIN_ORDER_AMOUNT` / Naver API client id / Chrome 경로.
  - 모든 secret 자리에는 `[REDACTED]`만 적는다.
  - 환경변수 키 호환성을 본문에 명시한다.
  - 산출물 위치: `design.md`의 `Secrets / 환경변수 설계` 섹션을 표 형식으로 확정.
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

- [ ] 6. `access_token.txt` 보관 위치 결정 근거 확정
  - 후보 비교(EC2 로컬 + S3 정기 backup, EFS, Secrets Manager)를 표로 정리한다.
  - 마켓커넥터 EC2 단일 instance 전제에서 EC2 로컬 + S3 backup 1순위, EFS 2순위로 확정 사유를 적는다.
  - EC2 교체 시 토큰 백업 / 복원 절차를 Runbook 후보 항목으로 표시한다.
  - 산출물 위치: `design.md`의 `access_token.txt 보관 위치` 섹션을 표 형식으로 보강.
  - 실제 EC2 / S3 / EFS 생성 금지.
  - _Requirements: 3.8, 6.5_

- [ ] 7. 네트워크 결정 사항 확정
  - 단일 VPC, 다중 AZ, public/private subnet 구성 권고를 다이어그램(텍스트)으로 보강한다.
  - 마켓커넥터 outbound: EC2 + EIP 1순위, NAT Gateway EIP 2순위 확정.
  - `port-view` → `port-marketconnector` 내부 호출: ECS Service Discovery / Internal ALB 1순위 확정.
  - 운영자 접속: SSH 직접 노출 금지, SSM Session Manager 사용 권고.
  - VPC endpoint 권고 항목(S3, ECR, Secrets Manager, SSM, CloudWatch Logs)을 명시한다.
  - 산출물 위치: `design.md`의 `네트워크 설계` 섹션 보강.
  - 실제 VPC / SG / EIP 생성 금지.
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7_

- [ ] 8. 컨테이너 / ECR / CI/CD 표준 확정
  - ECR 명명 규칙(`port-{ms}`)과 태그 규칙(`:{git-sha}` + `:{env}` alias) 확정.
  - `port_strategy_common` packaging: git submodule + Dockerfile 빌드 단계 1순위, CodeArtifact 2순위.
  - CI/CD: GitHub Actions → ECR → ECS / EC2 배포 1순위, CodePipeline+CodeBuild+CodeDeploy 2순위.
  - 빌드 시 secret을 이미지에 굽지 않는 정책 명시.
  - 환경별 promotion 흐름(dev 자동, paper / live manual approval) 명시.
  - 마켓커넥터 EC2 배포는 AMI baseline + systemd unit + CodeDeploy 1순위로 별도 명시.
  - 산출물 위치: `design.md`의 `컨테이너 이미지와 CI/CD` 섹션 보강.
  - 실제 ECR / 파이프라인 생성 금지.
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

- [ ] 9. 관측 표준 확정 (로그 / 메트릭 / Alarm)
  - log group 명명 규칙(`/portfolio/{env}/{ms}`) 확정.
  - retention 환경별 권고(live 90 / paper 30 / dev 7~14) 확정.
  - 표준 메트릭 + 도메인 메트릭(broker error rate, Daily Batch step 결과, intraday heartbeat, fill sync lag) 확정.
  - alarm threshold는 placeholder만 유지하고 운영자 결정 표시.
  - 산출물 위치: `design.md`의 `Observability / 알림 / Runbook` 섹션 중 로그/메트릭 부분 보강.
  - CloudWatch 리소스 생성 금지.
  - _Requirements: 6.1, 6.2_

- [ ] 10. Slack 알림 / Daily Batch 통합 권고 확정
  - 도메인 알림은 기존 `port-view`의 `SlackNotificationService` 유지 명시.
  - 인프라 알람은 CloudWatch Alarm → SNS → Lambda → Slack webhook 1순위, AWS Chatbot 2순위.
  - Daily Batch는 EventBridge Scheduler → Step Functions → ECS RunTask 권고로 명시. `strategy_daily_batch_run`, `strategy_daily_batch_step_log` 테이블은 유지.
  - 산출물 위치: `design.md`의 `Slack 알림` 및 `Daily Batch 통합` 부분 보강.
  - 실제 SNS / Lambda / Step Functions 생성 금지.
  - _Requirements: 6.3, 6.4_

- [ ] 11. 운영 Runbook 골격 v0 작성
  - 5개 시나리오(broker 토큰 만료/재발급, KIS 주문 실패, RDS failover, Daily Batch 실패 재실행, intraday monitor 중단)에 대해 증상 / 절차 / 자동 재시도 허용 여부를 본문 항목으로 작성한다.
  - 자동 재시도 정책 표(허용 / 금지)를 별도 항목으로 작성한다.
  - 산출물 위치: `design.md`의 `Runbook 항목` 섹션 보강.
  - 실제 운영 절차 실행 금지.
  - _Requirements: 6.5, 6.6_

- [ ] 12. 환경 분리 / 비용 추정 / 단계적 cutover 로드맵 확정
  - dev / paper / live 환경에서 broker live 연결 허용 여부 명시.
  - 비용 항목 후보 목록을 low / medium / high 추정 범위로 표기.
  - 6 stage cutover 로드맵에 진입 조건 / 이탈(rollback) 조건 명시.
  - paper 검증 N영업일은 placeholder 유지.
  - 산출물 위치: `design.md`의 `환경 분리 / 비용 / 단계적 cutover` 섹션 보강.
  - 실제 비용 정산 금지. 실제 cutover 금지.
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ] 13. 후속 spec 후보 목록 확정
  - 후속 spec 후보(`02-aws-network-and-rds`, `03-marketconnector-ec2`, `04-strategy-batch-stepfunctions`, `05-port-view-ecs-and-runbook`, `06-secrets-and-iam`, `07-cicd-pipelines`)에 대해 1줄 요약과 입력 의존성을 표로 작성한다.
  - 산출물 위치: `design.md`의 `후속 spec 후보` 섹션 보강.
  - 후속 spec은 본 spec 범위에서 생성하지 않는다.
  - _Requirements: 7.3_

- [ ] 14. 본 spec 안전 제약 재검증
  - 산출물이 `requirements.md`, `design.md`, `tasks.md` 3개 파일로만 한정되었는지 확인한다.
  - 8개 MS의 README / AGENTS.md / CHANGELOG / docs / worklog가 수정되지 않았는지 `git status --short`로 확인한다(읽기 전용 명령만).
  - 8개 MS의 소스 코드 변경이 없는지 확인한다(읽기 전용).
  - 본 spec 산출물에 실제 secret이 포함되지 않았는지 grep으로 확인한다(검색만, 외부 호출 없음). password / token / app key / app secret / 계좌번호 / webhook URL 패턴이 없는지 확인하고 모두 `[REDACTED]`인지 확인한다.
  - 어떤 8개 MS entrypoint도 실행하지 않았는지 확인한다.
  - 산출물 위치: 본 spec 내 모든 문서.
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7_

# 본 spec에 포함하지 않는 항목 (후속 spec으로 분리)

다음 항목은 명시적으로 본 spec 범위가 아니다. 후속 spec에서 다룬다.

- 실제 VPC / Subnet / SG / NAT Gateway / EIP 생성
- 실제 RDS for PostgreSQL 인스턴스 생성, schema 생성, 데이터 이전
- 실제 ECR 저장소 / 이미지 빌드 / push
- 실제 ECS Cluster / Task Definition / Service / Step Functions / EventBridge Scheduler 생성
- 실제 Secrets Manager / SSM Parameter Store 등록
- IAM Role / Policy 작성 및 적용
- 마켓커넥터 EC2 + EIP 생성 및 broker 측 IP 등록
- `port_strategy_common` packaging 코드 변경 또는 빌드 자동화 작성
- `port-view` Daily Batch 코드의 외부 호출 방식 변경
- Slack webhook URL 등록, SNS Topic 생성, AWS Chatbot 연동
- CloudWatch Alarm / Metric Filter 등록
- 운영 Runbook 실제 테스트(특히 broker 주문 실패 시나리오 리허설)

위 항목들은 각각 후속 spec에서 별도로 진행하며, 후속 spec에서는 본 `01-aws-migration-foundation` spec의 결정 사항을 입력으로 사용한다.
