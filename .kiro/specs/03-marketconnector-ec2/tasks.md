# Tasks — 03-marketconnector-ec2

본 tasks 는 [`./design.md`](./design.md) / [`./requirements.md`](./requirements.md) 결정을 진행 단위로 분해한 최소 체크리스트다. 실제 AWS 작업과 EC2 shell 작업은 운영자가 직접 수행하고, Kiro 는 문서 / 절차 / 검증 항목 정리만 담당한다.

## 1. 사전 상태 확인

- [ ] 1. EC2 / SG / Instance Profile attach 상태 확인 (§2)
- [ ] 2. Python 3.9.25 / venv 1개 / 의존 5종 설치 결과 확인 (§4)
- [ ] 3. PostgreSQL client / pg_restore major 18 / full 18.4 확인 (§5)
- [ ] 4. 06 spec Secrets 4건 + SSM Parameter 6건 등록 상태 확인 (§8)

## 2. env 주입 절차 정리

- [x] 5. 임시 export 스크립트(`/tmp/inject-env.sh`) 패턴 정리 (§8.3) — [2026-06-17] v5 패턴(JSON SecretString 내부 key 추출 + `KIS_*` alias 동시 export) 보강 완료(operation-notes 2026-06-17 §1 / runbook §2 / design §8.2.2 정합)
- [x] 6. 환경변수 매핑(KIS / RDS / CONNECTOR_*) 표 확정 (§8.2) — [2026-06-17] `APP_*` 호환 key + `KIS_*` alias 동시 export 정책으로 완료(design §8.2.1 정합)
- [ ] 7. 정상 운영 모드 전환(systemd / startup script)은 후속 task / 별도 phase 로 분리 (§7, §8.3)

## 3. 조회성 smoke test 정리

- [ ] 8. `marketconnector_app` 기준 RDS 접속 절차 정리 (§6)
- [x] 9. `connector_balance.py` / `connector_order_check.py` 조회성 실행 절차 정리 (§7.3, §10) — [2026-06-17] SSM RunCommand 단건 재검증 완료(operation-notes 2026-06-17 §1 / §2 / runbook §4.1 / §4.2 정합)
- [ ] 10. Flask 내부 smoke test (조회성 endpoint) 절차 정리 (§7.3, §10)
- [ ] 11. 운영 가능 후보 entrypoint(`connector_quote_realtime.py` / `connector_quote_closed.py` / `connector_view_service.py`) 검증은 후속 phase 로 분리 (§7.3, §7.4)
- [x] 12. 신규 주문 / 매수 / 매도 / 취소 / 정정 entrypoint 호출 0건 정책 명시 (§7.3, R14) — [2026-06-17] 본 일자 신규 주문 / `--execute` / aws-live 작업 0건 재검증(operation-notes 2026-06-17 §3 정합)

## 4. Instance Role / Access Key 미사용 검증 정리

- [ ] 13. 자격증명 정상 상태 점검 항목 정리(`aws sts get-caller-identity` / `aws configure list`) (§9.4)
- [ ] 14. EC2 안 access key 파일 / 환경변수 / dotfile / systemd 안 access key 0건 점검 항목 정리 (§9.5)
- [ ] 15. 03 보조 권한(`AmazonSSMManagedInstanceCore` + CloudWatchLogsWrite 권고 옵션 = log group 사전 생성) 검증 항목 정리 (§9.2)

## 5. 후속 phase 산출물 생성 계획

- [ ] 16. runbook.md 생성 — 8단계 + [실행]/[확인]/[준비]/[복구] 라벨 (§11.1)
- [ ] 17. validation-checklist.md 생성 — 7개 점검 영역 + 4종 라벨 (§11.2, §11.3)
- [ ] 18. operation-notes.md 생성 — 일자별 누적 + 6/9 / 6/10 사실 + 8건 결과 + IAM 변경 템플릿 (§3, §10, §11.4)

## 6. _common 갱신 후보 정리

- [ ] 19. operator-decisions.md 갱신 후보 인계(OD-SEC-005 / OD-SEC-006 잠정→확정 후보, `AmazonSSMManagedInstanceCore` 사용 정책 신규 후보) (§12.1)
- [ ] 20. risk-register.md 갱신 후보 인계(R-DATA / R-CAP / R-BROKER / R-SEC / R-AUTO 5건) (§12.2)
- [ ] 21. followups-overview.md 갱신 후보 인계(03 1차 적용 환경 / 1차 범위 / 범위 밖 / 04·05·08·09·10 인계) (§12.3)

## 7. 완료 기준

- [ ] 22. requirements.md / design.md / tasks.md / runbook.md / validation-checklist.md / operation-notes.md 4 + 2 = 6 산출물 모두 존재
- [ ] 23. 8건 검증 결과 모두 [O] 또는 운영자 직접 확인 결과로 누적 기록
- [ ] 24. 본 spec 산출물 어디에도 실제 secret value / 계좌번호 / RDS endpoint / account-id / 실제 ARN / IAM access key id / instance-id / EIP 미기록 (R14)
- [ ] 25. 04 / 05 / 08 / 09 / 10 spec 진입 시 본 spec §13 매핑을 입력으로 받을 수 있는 상태 (§13)

## 8. 신규 후속 task (2026-06-17 보강)

- [ ] 26. systemd / startup script 정상 운영 모드 전환 시 v5 env mapping 패턴(JSON SecretString 내부 key 추출 + `APP_*` 호환 key + `KIS_*` alias 동시 export) 반영 — task 7 후속
- [ ] 27. `/tmp/inject-env.sh` 운영 스크립트 승격 여부 판단(권한 700 / 메모리 export 만 / secret 평문 미저장 정책 유지 / 정식 systemd EnvironmentFile vs runtime AWS CLI 호출 패턴 비교)
- [ ] 28. `APP_*` 호환 key vs `KIS_*` alias 단일화 / 양쪽 유지 결정(현재는 동시 export 로 안전 / 후속 코드 정리 시점에 단일화 후보 검토)

## 9. 신규 후속 task (2026-06-17 17-step E2E 보강)

본 섹션은 Daily AWS 17-step E2E 완료 시점의 후속 task 다. 본 노트 / operation-notes.md 2026-06-17 (Daily AWS 17-step E2E 완료) §1 ~ §5 정합. 본 일자에 운영자가 직접 패치 / 정식 배포한 `connector_strategy_order_execute.py` / `connector_order_check.py` 변경 사실은 operation-notes 에 사실로만 기록 / 본문 전체 인용 0건.

- [ ] 29. `connector_order_check.py` summary fallback guard 테스트 케이스 추가(active 후보 0 / 1 / 2 / 다건 시 동작 / `output1 empty` + `output2 aggregate summary` 입력값 / `output1` 정상 row 입력값 / event · fill · status 변경 0건 검증 / KIS API 응답 fixture) — R-AUTO-018 mitigation 강화 후속
- [ ] 30. `connector_strategy_order_execute.py` 의 `result_payload` 내 `source_daily_signal_id null` 보정 검토(OPTIONAL_COLUMNS 또는 fallback 패턴 / R-AUTO-009 [2026-06-17 보강] 정합) — paper 환경 한정 평일 / 안전 데이터 추가 검증 후속
- [ ] 31. `connector_balance_snapshot` 최신성 검증 SQL 정리 — `created_at` 단독이 아닌 `as_of_ts` / `max(created_at)` 의미 구분(`as_of_ts` = broker 응답 기준 시점 / `created_at` = row insert 시점) — `connector_balance_snapshot` / `connector_position_snapshot` / `connector_api_call_log` 분리 점검 SQL 후보 정리(R-DOCS-001 [2026-06-17 보강] / [2026-06-17 보강(17-step E2E)] 정합)

## Task Dependency Graph (간단)

```text
1~4 (사전 상태)
  └─> 5~7 (env 주입)
        └─> 8~12 (조회성 smoke test)
              └─> 13~15 (Access Key 미사용 검증)
                    └─> 16~18 (후속 phase 산출물)
                          └─> 19~21 (_common 갱신 후보)
                                └─> 22~25 (완료 기준)
                                      └─> 26~28 (2026-06-17 v5 env mapping 후속)
                                            └─> 29~31 (2026-06-17 17-step E2E 보강)
```
