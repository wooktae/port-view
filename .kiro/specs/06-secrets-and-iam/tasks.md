# Tasks — 06-secrets-and-iam

본 tasks 는 [`./design.md`](./design.md) 의 결정과 [`./requirements.md`](./requirements.md) 의 R1~R14 를 실제 진행 단위 task 로 쪼갠 최소 세트다. 오늘 06 을 문서상 닫고 03-marketconnector-ec2 후속 정리로 넘어가는 것이 목표.

각 task 는 02 spec tasks.md 형식의 체크박스 형태(`- [ ] N. ...`)로 작성하되, 본 spec 은 운영자 직접 작업 / 문서 정리 task 가 대부분이라 sub-bullet 으로 산출물 / 책임 / 입력 / 완료 기준을 짧게 명시한다.

본 phase 에서는 `tasks.md` 한 개 파일만 생성한다.
`runbook.md` / `validation-checklist.md` / `operation-notes.md` 는 후속 phase 책임이며,
본 tasks 의 task 19 ~ 21 에서 생성 계획만 잡는다.
모든 산출물은 [`./design.md`](./design.md) §11 안전 제약을 따른다.

- 실제 AWS 리소스 미생성 · 미수정 · 미삭제
- 8개 MS 코드 / docs 미수정
- 실제 secret value / 계좌번호 / account-id / RDS endpoint hostname / 실제 secret ARN / access key 미기록

## 1. 문서 상태 확인

- [ ] 1. requirements.md / README.md / design.md 생성 확인
  - 산출물: 없음(상태 점검만)
  - 책임: Kiro
  - 입력: [`./requirements.md`](./requirements.md), [`./README.md`](./README.md), [`./design.md`](./design.md)
  - 완료 기준: 세 파일 모두 존재. requirements.md 의 R1~R14 와 design.md 의 §1~§11 매핑 확인.
  - _Requirements: R7.1, R8.1, R9.1, R14.7_

## 2. Secrets / Parameters 구조 확정

- [ ] 2. KIS / RDS 비밀 항목을 Secrets Manager 후보로 락
  - 산출물: design.md §1.1 / §1.4 / §2.4 표 그대로 채택. 변경 없음
  - 입력: design.md §1, requirements.md R1.1 / R1.2 / R1.5
  - 완료 기준: KIS app key, KIS app secret, KIS paper 계좌번호(`PAPER_ACNT` / `ACNT_PRDT_CD`), RDS `marketconnector_app` 접속정보, RDS master 비밀번호(`/portfolio/paper/rds/master` 호환 유지)가 Secrets Manager 후보로 명시되어 있음
  - _Requirements: R1.1, R1.2, R1.5, R2.5_

- [ ] 3. 일반 설정값을 SSM Parameter Store 후보로 락
  - 산출물: design.md §1.2 표 그대로 채택
  - 입력: design.md §1.2, requirements.md R1.3 / R1.6
  - 완료 기준: KIS base URL, Connector Flask host / port / debug, `PORT_ENVIRONMENT`, `PORT_BROKER_NAME`, `PORT_STRATEGY_NAME`, `PORT_STRATEGY_VERSION` 가 SSM Parameter Store 후보로 명시
  - _Requirements: R1.3, R1.6_

- [ ] 4. Slack webhook 보관 위치 1차 락
  - 산출물: design.md §1.3 결정값(잠정: Secrets Manager 권고, 절감안 SSM SecureString) 그대로 채택. 본 task 에서는 결정 갱신만 §7 OD-OBS-004 갱신 후보로 인계
  - 입력: design.md §1.3, requirements.md R1.4
  - 완료 기준: Secrets Manager 권고 / SSM SecureString 절감안 두 후보 비교가 한 곳(design §1.3) 에 명시됨. 최종 락은 task 17 의 OD-OBS-004 갱신에서 진행
  - _Requirements: R1.4_

## 3. Naming / 환경변수 호환 확정

- [ ] 5. naming `/portfolio/{env}/{service}/{item}` 1차 권고 채택
  - 산출물: design.md §2 채택. 본 spec 1차 확정 service `marketconnector` / `rds` 한정
  - 입력: design.md §2, requirements.md R2
  - 완료 기준: env=`paper`/`live`, service 1차 확정 2개, 호환 유지(`/portfolio/paper/rds/master`), 이름에 endpoint / 계좌번호 / secret value / password / token 미포함 정책이 design 에 명시
  - _Requirements: R2.1, R2.2, R2.3, R2.4, R2.5, R2.6_

- [ ] 6. 8개 MS 환경변수 키 호환 확정
  - 산출물: design.md §3 채택. 코드 / README / AGENTS.md 미수정 원칙 유지
  - 입력: design.md §3, requirements.md R3
  - 완료 기준: `INTEREST_DB_*` / `PORTFOLIO_DB_NAME` / `PORT_*` 13개 키 유지 + `port-marketconnector` config.py KIS 식별자 외부화 매핑이 design §2.4 / §3.2 에 명시
  - _Requirements: R3.1, R3.2, R3.3, R3.4_

## 4. IAM Role 최소 권한 구조 확정

- [ ] 7. MarketConnector EC2 Instance Role / Profile 이름 후보 확정
  - 산출물: design.md §4.1 채택. Role: `portfolio-paper-marketconnector-ec2-role`. Profile: `portfolio-paper-marketconnector-ec2-profile`
  - 입력: design.md §4.1, requirements.md R4.1
  - 완료 기준: 이름 후보가 design 에 명시. Trust Policy 의 Principal `Service: ec2.amazonaws.com` 명시
  - _Requirements: R4.1_

- [ ] 8. Permission Policy 매트릭스 확정
  - 산출물: design.md §4.2 채택
  - 입력: design.md §4.2, requirements.md R4.2 / R4.3 / R4.6
  - 완료 기준: 세 statement 가 design 에 명시
    - `SecretsManagerRead` — `secretsmanager:GetSecretValue`, `DescribeSecret` + 정확한 secret ARN placeholder
    - `SsmParameterRead` — `ssm:GetParameter` / `GetParameters` / `GetParametersByPath` + `/portfolio/paper/marketconnector/*` prefix
    - `KmsDecrypt` (조건부)
  - _Requirements: R4.2, R4.3, R4.6_

- [ ] 9. 금지 정책 매트릭스 확정
  - 산출물: design.md §4.3 채택
  - 입력: design.md §4.3, requirements.md R4.4
  - 완료 기준: `Resource: "*"`, `Action: "*"`, `secretsmanager:*`, `ssm:*`, 다른 service prefix Resource, 다른 환경 prefix Resource 가 모두 금지로 명시
  - _Requirements: R4.4_

- [ ] 10. ARN 표기 규칙 확정
  - 산출물: design.md §4.5 채택
  - 입력: design.md §4.5, requirements.md R4.5 / R14.4
  - 완료 기준: 모든 ARN 자리에 placeholder(`arn:aws:iam::<account-id>:role/...` 등) 또는 `[REDACTED]` 만 사용. 실제 account-id / 실제 secret ARN 미기록 확인
  - _Requirements: R4.5, R14.4_

## 5. Access Key 미사용 원칙 확정

- [ ] 11. EC2 내부 Access Key 미사용 원칙 락
  - 산출물: design.md §5 채택
  - 입력: design.md §5, requirements.md R5
  - 완료 기준: 아래 정책이 design 에 명시
    - access key 저장 금지 위치: `~/.aws/credentials` / `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` /
      systemd Environment / EnvironmentFile / dotfile / `.env`
    - IMDSv2 + Instance Role 자격증명 만 사용
  - _Requirements: R5.1, R5.2, R5.5_

- [ ] 12. Access Key 검증 위치 확정(실제 검증은 후속 phase)
  - 산출물: design.md §5.2 표 채택. 실제 검증 절차는 후속 runbook.md / validation-checklist.md 책임
  - 입력: design.md §5.2, requirements.md R5.3
  - 완료 기준: `aws sts get-caller-identity` / `aws configure list` 검증 위치가 runbook.md 책임으로 명시. validation-checklist.md 의 8개 점검 영역 중 access key 파일 미존재 / assumed-role ARN 일치 항목 명시
  - _Requirements: R5.3_

## 6. ECS Task Role 재사용 패턴 확정

- [ ] 13. Task Execution Role / Task Role 분리 패턴 락
  - 산출물: design.md §6.1 채택
  - 입력: design.md §6.1, requirements.md R6.2
  - 완료 기준: Task Execution Role(이미지 pull / Logs write / `secrets` 필드 주입) 과 Task Role(런타임 read) 책임 분리 표 명시
  - _Requirements: R6.2_

- [ ] 14. ECS `secrets` 필드 패턴 확정
  - 산출물: design.md §6.2 채택. JSON multi-key `:json-key::` 접미 형태 placeholder 만 명시
  - 입력: design.md §6.2, requirements.md R6.3
  - 완료 기준: `name=<ENV_KEY>, valueFrom=<arn placeholder>` 형식 명시. 실제 ARN suffix / json-key 분기는 03 / 08 / 04 / 05 / 09 spec 책임으로 인계
  - _Requirements: R6.3, R6.4, R6.5_

- [ ] 15. 후속 spec 분담 매트릭스 확정
  - 산출물: design.md §6.3 / §9.1 채택
  - 입력: design.md §6.3 / §9.1, requirements.md R6.4 / R13
  - 완료 기준: 03 / 08 / 04 / 05 / 09 / 10 spec 별 본 spec(06) 입력 활용 항목 매트릭스 명시. naming / env / wildcard 금지 결정이 후속 spec 변경 금지 항목으로 명시
  - _Requirements: R13.1, R13.2, R13.3, R13.4_

## 7. _common 문서 갱신 후보 인계

본 spec phase 안에서만 수행. 실제 _common 파일 수정은 운영자 승인 후 별도 작업으로 진행 가능.

- [ ] 16. operator-decisions.md 갱신 후보 정리
  - 산출물: design.md §7 표 그대로 채택. 실제 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) 수정은 본 task 에서 운영자 승인 시 진행
  - 입력: design.md §7, requirements.md R10
  - 완료 기준: OD-SEC-001 / OD-OBS-004 갱신 후보값과 신규 OD-SEC-005 / OD-SEC-006 후보가 design §7.1 표에 명시. OD-SEC-002 / OD-SEC-003 정합성 §7.2 명시. Status 라벨(CONFIRMED/TENTATIVE/TBD/DEFERRED + 🟢/🟡/🔴/🔵) 규칙 §7.3 명시
  - _Requirements: R10.1, R10.2, R10.3, R10.4, R10.5, R10.6, R10.7_

- [ ] 17. risk-register.md 갱신 후보 정리
  - 산출물: design.md §8 표 채택. R-SEC 후보 4건의 실제 ID는 [`../_common/risk-register.md`](../_common/risk-register.md) 의 다음 가용 번호 부여 후 본 task 에서 운영자 승인 시 갱신
  - 입력: design.md §8, requirements.md R12
  - 완료 기준: 권한 과다 / EC2 secret 평문 노출 / EC2 access key 파일 / naming 불일치 4건의 mitigation / detection / rollback 한 줄씩 명시. 02 spec 컬럼 형식과 동일
  - _Requirements: R12.1, R12.2, R12.3, R12.4, R12.5, R12.6_

- [ ] 18. followups-overview.md 갱신 후보 정리
  - 산출물: design.md §9 표 채택. 실제 [`../_common/followups-overview.md`](../_common/followups-overview.md) 수정은 본 task 에서 운영자 승인 시 진행
  - 입력: design.md §9, requirements.md R11
  - 완료 기준: 1차 적용 환경 / 1차 범위 / 범위 밖, 03 / 08 / 04 / 05 / 09 / 10 spec 입력 항목, 06 ↔ 03 순서 한 줄이 §9 에 명시
  - _Requirements: R11.1, R11.2, R11.3, R11.4_

## 8. 후속 phase 산출물 생성 계획 (본 phase 에서는 생성하지 않음)

- [ ] 19. runbook.md 생성 예정
  - 산출물 후보: [`./runbook.md`](./runbook.md) (별도 phase 에서 생성)
  - 입력: design.md §10, requirements.md R7
  - 완료 기준(후속 phase): 아래 단계가 [실행]/[확인]/[준비]/[복구] 라벨로 분해
    - (a) Secrets Manager 등록 → (b) SSM Parameter 등록 → (c) Instance Role / Profile 생성
    - (d) 정책 attach → (e) EC2 attach → (f) read 검증 → (g) Connector smoke test
  - _Requirements: R7.1, R7.2, R7.3, R7.4, R7.5, R7.6_

- [ ] 20. validation-checklist.md 생성 예정
  - 산출물 후보: [`./validation-checklist.md`](./validation-checklist.md)
  - 입력: design.md §10, requirements.md R8
  - 완료 기준(후속 phase): 4종 라벨 + 8개 점검 영역 모두 포함
    - 라벨: `[O]` / `[X]` / `[Kiro 후속 작업 필요]` / `[운영자 확인 필요]`
    - 점검 영역: Secrets 인벤토리 / Parameter 인벤토리 / Role · Profile 존재 / 정책 매트릭스 일치 /
      wildcard 0건 / access key 파일 미존재 / assumed-role ARN 일치 / Connector smoke test
  - _Requirements: R8.1, R8.2, R8.3, R8.4, R8.5_

- [ ] 21. operation-notes.md 생성 예정
  - 산출물 후보: [`./operation-notes.md`](./operation-notes.md)
  - 입력: design.md §10, requirements.md R9
  - 완료 기준(후속 phase): 일자별 누적 기록(`## YYYY-MM-DD ...`), secret 값 미기록(성공/실패만), IAM 변경 기록 템플릿(변경 일자/변경자/사유/전후 항목 요약), 운영자 직접 수행 분담 본문 명시
  - _Requirements: R9.1, R9.2, R9.3, R9.4, R9.5, R9.6_

## 9. 06 닫기 / 03 인계

- [ ] 22. 06 최소 설계 문서상 닫기
  - 산출물: 본 spec 폴더의 requirements.md / README.md / design.md / tasks.md 4개 파일 상태 확인
  - 입력: 본 tasks 의 task 1 ~ 21
  - 완료 기준
    - 4개 파일 존재. design.md §11 안전 제약과 task 22 가 일치.
    - 실제 AWS 리소스 미생성 / 미수정 / 미삭제.
    - 8개 MS 코드 / docs 미수정.
    - 모든 산출물에 실제 secret value / 계좌번호 / account-id / RDS endpoint hostname / 실제 secret ARN / access key 미기록
  - _Requirements: R14.1, R14.2, R14.3, R14.4, R14.5, R14.6, R14.7_

- [ ] 23. 03-marketconnector-ec2 후속 정리로 인계
  - 산출물: 03 spec(예정) 의 입력 항목으로 본 spec 의 §4(Instance Role 매트릭스), §5(Access Key 미사용 원칙), §6(Task Role 골격), §7(OD 후보) 인계 명시
  - 입력: design.md §6.3 / §9.1 / §9.3
  - 완료 기준: 03 spec 진입 시 본 spec 결정을 입력으로 받아 EC2 정식 운영(Connector / Flask / KIS API / RDS 접속) 단계로 진행 가능
  - _Requirements: R11.4, R13.1_

## 10. 이번 tasks.md 에서 하지 않는 것 (안전 제약)

- 실제 Secrets Manager secret 생성 / 수정 / 삭제 (운영자 직접 수행, 후속 runbook.md)
- 실제 SSM Parameter 생성 / 수정 / 삭제 (동)
- 실제 IAM Role / Policy / Instance Profile 생성 / 변경 / 삭제 (동)
- EC2 instance 의 Instance Profile attach 변경 (동)
- KMS Key 생성 / 변경 (해당 시 운영자 직접 수행)
- 아래 8개 MS 의 README / AGENTS.md / CHANGELOG / docs / worklog / 소스 코드 수정
  - `port-view`, `port-marketconnector`
  - `port-interest-crawler`, `port-interest-preprocessor`
  - `port_strategy_common`, `port_strategy_decision`, `port_strategy_execution`, `port_strategy_research`
- 8개 MS entrypoint 실행, broker / KIS / Selenium / KRX / Naver / yfinance / RDS DDL/DML / 주문 / 체결 / Daily Batch / intraday monitor 호출
- `secretsmanager:GetSecretValue` 실호출(운영자만 수행, Kiro 자동 검증은 `DescribeSecret` metadata 만)
- live rotation 자동화 / GitHub Actions OIDC / 8개 MS full IAM 매트릭스 / aws-live IAM 작업

## Task Dependency Graph

문서 / 결정 정리 흐름이라 wave 는 짧다. 같은 design 섹션을 입력으로 쓰는 task 라도 산출물 충돌이 없으므로 동일 wave 에 둔다.

```text
1 (문서 상태 확인)
  └─> 2, 3, 4 (Secrets/Parameter/Slack 보관 위치)
        └─> 5, 6 (naming / 환경변수 호환)
              └─> 7, 8, 9, 10 (Instance Role 매트릭스)
                    └─> 11, 12 (Access Key 미사용 원칙)
                          └─> 13, 14, 15 (Task Role 패턴 / 후속 spec 분담)
                                └─> 16, 17, 18 (_common 갱신 후보)
                                      └─> 19, 20, 21 (후속 phase 산출물 생성 계획)
                                            └─> 22 (06 닫기)
                                                  └─> 23 (03 인계)
```
