# Kiro AWS Migration 작업 규칙

이 `.kiro` 작업공간은 PORT-STRATEGY-AI 포트폴리오의 AWS Migration 관련 cross-service spec을 작성하고 관리하기 위한 공간이다.

## 0. 최우선 문서 가독성 규칙

모든 문서 작업에서 본 섹션을 최우선으로 적용한다.

### 0.0 적용 범위 제한 규칙

본 섹션 0의 문서 가독성 규칙은 모든 문서 작업의 우선 기준이지만, 기본 적용 범위는 **이번 작업에서 새로 작성하거나 직접 수정하는 부분**으로 한정한다.

Kiro는 사용자가 명시적으로 “문서 전체 가독성 정리”, “전체 전수 스캔”, “전체 위반 정리”, “전체 audit”, “baseline / after 검증”을 요청하지 않는 한, 기존 문서 전체를 Section 0 기준으로 전수 스캔하거나 대량 리팩토링하지 않는다.

기본 작업에서는 아래 범위만 Section 0 기준으로 정돈한다.

| 항목     | 적용 범위                                                       |
| ------ | ----------------------------------------------------------- |
| 신규 작성  | 이번 작업에서 새로 추가하는 문장 · 표 · bullet                             |
| 직접 수정  | 이번 작업에서 실제로 변경하는 기존 문장 · 표 · bullet                         |
| 변경 인접부 | 이번 수정으로 인해 새로 생길 수 있는 300자 초과 셀 · 500자 초과 라인 · 4개 이상 슬래시 체인 |
| 기존 위반  | 이번 작업 범위 밖이면 원본 유지                                          |

변경 인접부는 같은 표 행, 같은 bullet 묶음, 또는 같은 짧은 문단으로 한정한다. 같은 파일의 다른 섹션, 과거 날짜 세션, Historical Notes 전체는 변경 인접부로 보지 않는다.

기존 문서에서 Section 0 위반을 발견하더라도, 이번 요청과 직접 관련이 없으면 즉시 수정하지 않는다. 필요한 경우 “후속 가독성 정리 후보”로만 기록한다.

아래 작업은 사용자가 별도 회차로 명시적으로 요청한 경우에만 수행한다.

| 작업                                    | 기본 처리   |
| ------------------------------------- | ------- |
| 기존 문서 전체 전수 스캔                        | 수행하지 않음 |
| ViolationRecord matrix 생성             | 수행하지 않음 |
| content_hash 생성                       | 수행하지 않음 |
| 전체 before / after audit 생성            | 수행하지 않음 |
| README / WORKLOG / CHANGELOG 전체 일괄 정리 | 수행하지 않음 |
| 과거 날짜 섹션 대량 정리                        | 수행하지 않음 |

문서 전체 가독성 정리 회차를 별도로 수행하는 경우에도, Kiro는 속도와 안전성을 우선한다. 모호한 항목은 원본 유지하고, 명백한 표면 위반만 국소적으로 정돈한다.


### 0.1 표 작성 최우선 규칙

문서에 2컬럼 표를 작성할 때는 먼저 아래 표 헤더 후보 중 가장 의미가 맞는 것을 선택한다.

임의의 새 2컬럼 헤더를 만들기 전에 아래 후보를 우선 사용한다.

(1) 항목 / 값
(2) 항목 / 결과
(3) ID / 변경
(4) 섹션 / 내용
(5) 구분 / 내용
(6) ID / 내용
(7) 파일 / 변경
(8) Step / 결과
(9) 구분 / 항목
(10) ID / 값
(11) 항목 / 보강 대상
(12) 메모 / 내용
(13) 후속 항목 / 값
(14) 파일 / 역할
(15) 경로 / 일자 · Run
(16) Launcher / 역할
(17) View / Step / DB Role
(18) 항목 / 검증 내용
(19) ID / Status
(20) 번호 / 메시지
(21) 이벤트 / 결과
(22) 검증 / 결과
(23) 대상 / 내용
(24) 테이블 / 값
(25) 후속 작업 / 값
(26) 행 / 내용
(27) 후속 / 내용
(28) 표 / 2026-06-17 추가 행
(29) 용어 / 설명
(30) 케이스 / 결과

가장 기본값은 `항목 / 값`이다.

단, 아래 경우에는 더 구체적인 헤더를 사용한다.

| 상황                | 우선 헤더       |
| ----------------- | ----------- |
| 검증 결과 요약          | `항목 / 결과`   |
| 파일별 변경 요약         | `파일 / 변경`   |
| 결정 ID / 리스크 ID 변경 | `ID / 변경`   |
| 섹션별 정리            | `섹션 / 내용`   |
| Step별 실행 결과       | `Step / 결과` |
| 후속 작업 정리          | `후속 작업 / 값` |
| 용어 설명             | `용어 / 설명`   |

### 0.1.1 2컬럼 표 기본 생성 규칙

앞으로 Kiro가 새로 만드는 표는 기본적으로 **2컬럼 표**로 작성한다.

기본 헤더는 `항목 / 값`이며, 더 구체적인 의미가 필요할 때만 0.1의 후보 헤더 중 하나를 선택한다.

3컬럼 이상 표는 아래 조건 중 하나에 해당할 때만 허용한다.

| 조건 | 처리 |
| --- | --- |
| 사용자가 명시적으로 3컬럼 이상을 요청 | 요청한 컬럼 수 사용 |
| 기존 표가 3컬럼 이상이고 보존이 더 안전 | 기존 구조 유지 |
| traceability matrix 처럼 행/열 교차 구조가 본질 | 3컬럼 이상 허용 |
| 2컬럼 변환 시 의미 손실이 큼 | 3컬럼 이상 허용 후 사유 보고 |

위 예외에 해당하지 않으면, 긴 bullet · 나열형 문장 · 상태 요약 · 파일별 변경 · 검증 결과 · 보안 결과는 모두 2컬럼 표로 정리한다.

2컬럼 표를 만들 때도 표 셀은 짧게 유지하고, 긴 근거는 Details / Evidence / Historical Notes / Usage Notes 로 분리한다.

### 0.2 표 셀 내용 작성 규칙

(중요) 표 셀 안 내용은 최대한 짧게 쓴다.

* 한 셀은 무조건 2문장 이하로 작성한다.
* 한 셀에 여러 항목이 들어가면 `<br>`로 나눈다.
* 긴 쉼표 나열, 긴 슬래시 체인, 긴 괄호 설명은 피한다.
* 긴 근거는 표 밖 `Details`, `Evidence`, `Historical Notes`, `Usage Notes`로 이동한다.
* operation-notes 수준의 실행 로그는 표에 넣지 않고 링크만 둔다.

예시:

| 항목    | 값                                             |
| ----- | --------------------------------------------- |
| 대상 파일 | `README.md`<br>`WORKLOG.md`<br>`CHANGELOG.md` |
| 결과    | 🟢 `완료`<br>신규 민감정보 원문 0건                      |
| 후속    | 🟠 2026-06 historical 섹션 추가 정리                |

### 0.3 `<br>` 우선 줄바꿈 규칙

표 셀 안에서 여러 값을 나열해야 하면 `<br>`을 우선 사용한다.

권장:

| 항목       | 값                                                                 |
| -------- | ----------------------------------------------------------------- |
| Security | AWS CLI 실행 0건<br>psql 실행 0건<br>broker 주문 제출 0건<br>secret 원문 기록 0건 |

비권장:

| 항목       | 값                                                              |
| -------- | -------------------------------------------------------------- |
| Security | AWS CLI 실행 0건 · psql 실행 0건 · broker 주문 제출 0건 · secret 원문 기록 0건 |

### 0.4 색상 / 상태 표시 규칙

상태는 먼저 배지로 표현한다.

| 배지 | 의미                            |
| -- | ----------------------------- |
| 🔴 | 금지 · 치명 · live · 고위험          |
| 🟠 | 대기 · 관찰 · Open · 미확정          |
| 🟢 | 완료 · 성공 · ENABLED · Mitigated |
| 🔵 | 참고 · evidence · 정보            |
| ⚫  | 해당 없음 · N/A                   |

HTML 색상은 필요한 경우에만 제한적으로 사용한다.

| 용도       | 색상                                          |
| -------- | ------------------------------------------- |
| critical | `<span style="color:#D1242F">**금지**</span>` |
| warning  | `<span style="color:#BF8700">**주의**</span>` |
| done     | `<span style="color:#1A7F37">**완료**</span>` |
| info     | `<span style="color:#0969DA">**참고**</span>` |

상태 배지로 충분하면 HTML 색상을 중복 적용하지 않는다.

### 0.5 문서 작성 밀도 규칙

문서 작성 시 아래 우선순위를 따른다.

1. 짧은 Summary
2. 짧은 표
3. 짧은 bullet
4. Details / Evidence / Historical Notes 링크
5. 긴 본문은 최후 수단

금지한다.

* 표 안 장문 paragraph
* 300자 초과 표 셀
* 500자 초과 라인
* 4개 이상 슬래시 체인
* raw log 전문 붙여넣기
* 같은 내용을 README / WORKLOG / CHANGELOG / operation-notes에 장문 반복

### 0.6 작업 속도 제한 규칙

문서 작업은 완벽한 자동 분석보다 빠른 국소 편집과 안전한 보고를 우선한다.

Kiro는 사용자가 명시적으로 요청하지 않는 한 아래 작업을 수행하지 않는다.

| 금지 작업 | 기본 처리 |
| --- | --- |
| sub-agent 생성 또는 재호출 | 수행하지 않음 |
| 별도 orchestrator / coordinator task 생성 | 수행하지 않음 |
| 새 scanner / audit framework 작성 | 수행하지 않음 |
| content_hash 기반 전체 매트릭스 생성 | 수행하지 않음 |
| 전체 파일 재스캔 반복 | 수행하지 않음 |
| workspace 밖 임시 파일 생성 | 수행하지 않음 |
| `$env:TEMP`, `C:\Users\Public` 사용 | 수행하지 않음 |
| 긴 PowerShell one-liner 실행 | 수행하지 않음 |
| trust prompt 를 유발하는 명령 변형 반복 | 수행하지 않음 |

기본 시간 상한은 아래를 따른다.

| 작업 단위 | 최대 시간 | 초과 시 처리 |
| --- | --- | --- |
| 단일 문서 국소 편집 | 15분 | 부분 완료 보고 |
| 루트 1개 문서 가독성 보정 | 30분 | 모호 항목 원본 유지 |
| 루트 3개 문서 보정 | 90분 | 파일별 완료/미완료 분리 |
| 전체 baseline / after 검증 | 30분 | 필수 count 만 보고 |
| 특정 task 병목 | 15분 | `needs_manual_review=true` 로 이월 |

시간 상한을 넘기면 더 정교한 자동화를 만들지 않는다. 현재까지 확인한 사실, 편집한 파일, 남은 항목, 수동 검토 항목만 짧게 보고하고 다음 작업으로 넘어간다.

### 0.7 문서 작업 실행 방식 제한

문서 작업은 아래 순서로만 진행한다.

1. 요청 범위 확인
2. 대상 파일 직접 읽기
3. 필요한 부분만 국소 편집
4. UTF-8 No BOM 저장
5. 짧은 after check
6. 변경 요약 보고

아래 방식은 기본 금지한다.

| 방식 | 이유 |
| --- | --- |
| “분석용 스크립트 → JSON → 재파싱 → 매트릭스 → 편집” 흐름 | 문서 작업 대비 과도함 |
| task 하나를 여러 sub-agent 로 분해 | 오케스트레이션 비용이 편집 비용보다 큼 |
| 불확실 항목을 해결하려고 전체 문서 재분석 | 시간 소모가 크고 fact-loss 위험 증가 |
| 과거 날짜 섹션 전체 재구성 | 요청 범위 초과 가능성 큼 |

불확실한 항목은 편집하지 말고 `needs_manual_review=true` 또는 Follow-up 으로 남긴다.

## 1. Scope

### 1.1 기본 경로

* 기본 작업 디렉터리: `C:\Workspaces\port-view\.kiro`
* AWS Migration spec 저장 위치: `C:\Workspaces\port-view\.kiro\specs`

### 1.2 대상 마이크로서비스

이 디렉터리는 `port-view` 안에 있지만, 여기서 작성하는 spec은 아래 8개 MS 전체의 AWS Migration을 다룬다.

* `port-marketconnector`
* `port-view`
* `port-interest-crawler`
* `port-interest-preprocessor`
* `port_strategy_common`
* `port_strategy_decision`
* `port_strategy_research`
* `port_strategy_execution`

### 1.3 MS별 파일 수정 원칙

각 MS의 `AGENTS.md`, `README.md`, 기존 문서는 현재 작업이 그 MS 수정을 명시적으로 요구할 때만 수정한다.

AWS Migration spec 작성 시 MS별 문서는 아래 내용을 이해하기 위한 read-only 참조로만 사용한다.

| 항목  | 내용                   |
| --- | -------------------- |
| 런타임 | 구조 · entrypoint      |
| 설정  | 환경변수 · secret 항목     |
| DB  | schema · search_path |
| 운영  | 빌드 · 배포 제약 · 주의사항    |

명시 허용이 없으면 MS 소스 코드, README, CHANGELOG, worklog, AGENTS.md를 수정하지 않는다.

## 2. Single Source of Truth

새 spec을 만들거나 수정하기 전에 `.kiro/specs/_common` 아래의 루트 공통 문서를 먼저 확인한다.

| 문서                                            | 역할                                       |
| --------------------------------------------- | ---------------------------------------- |
| `_common/operator-decisions.md`               | 운영자 결정사항의 단일 기준 문서                       |
| `_common/ms-aws-service-decision-matrix.md`   | 8개 MS별 AWS 서비스 권고와 판단 근거의 단일 기준 문서       |
| `_common/cost-simulation.md`                  | 비용 가정과 월 예상 비용의 단일 기준 문서                 |
| `_common/followups-overview.md`               | 후속 spec 진행 순서와 의존성 맵의 단일 기준 문서           |
| `_common/aws-resource-glossary.md`            | AWS 용어 설명의 단일 기준 문서                      |
| `_common/risk-register.md`                    | AWS Migration 운영 / 보안 / 비용 리스크의 단일 누적 문서 |
| `_archive/note-aws-landscape-2021-vs-2026.md` | 참고용 아카이브                                 |

공통 문서의 큰 내용을 다른 spec에 그대로 복사하지 않는다.

필요한 내용만 짧게 요약하고 상세 기준은 공통 문서를 참조한다.

## 3. 문서 작성 기본 원칙

### 3.1 Spec 작성 규칙

각 spec은 자기 범위에 집중한다.

일반적인 spec 폴더 구성은 아래를 따른다.

| 파일                        | 역할                      |
| ------------------------- | ----------------------- |
| `requirements.md`         | 요구사항                    |
| `design.md`               | 설계                      |
| `tasks.md`                | 작업 계획                   |
| `decision-matrix.md`      | 운영자 선택지가 많은 경우 선택적으로 작성 |
| `runbook.md`              | 운영 절차                   |
| `validation-checklist.md` | 검증 체크리스트                |
| `operation-notes.md`      | 운영 evidence 누적          |

### 3.2 업데이트 규칙

| 변경 내용                    | 우선 갱신 문서                                                                    |
| ------------------------ | --------------------------------------------------------------------------- |
| 운영자 결정 변경                | `_common/operator-decisions.md`                                             |
| MS별 AWS 서비스 권고 변경        | `_common/ms-aws-service-decision-matrix.md`<br>필요 시 `operator-decisions.md` |
| 비용 가정 변경                 | `_common/cost-simulation.md`                                                |
| 후속 spec 순서 / 의존성 / 범위 변경 | `_common/followups-overview.md`                                             |
| 새 AWS 서비스나 중요 용어 도입      | `_common/aws-resource-glossary.md`                                          |
| 새 리스크 식별                 | `_common/risk-register.md`                                                  |

루트 공통 문서를 기계적으로 전부 업데이트하지 않는다.

현재 결정이나 spec에 영향을 받는 문서만 업데이트한다.

### 3.3 README / CHANGELOG / WORKLOG 관리 규칙

`.kiro` 작업공간은 각 MS와 달리 날짜별 worklog 파일을 만들지 않는다.

| 문서                   | 기록 기준                                                |
| -------------------- | ---------------------------------------------------- |
| `.kiro/README.md`    | 작업공간의 목적 · 범위 · 폴더 구조 · 주요 문서 역할                     |
| `.kiro/CHANGELOG.md` | spec 구조 변경 · 루트 공통 문서 변경 · 신규 spec 생성 · 주요 문서 재구성 이력 |
| `.kiro/WORKLOG.md`   | Kiro 작업 세션의 간단 로그를 단일 파일에 누적                         |

규칙:

* `.kiro/docs/worklog/YYYY-MM-DD.md` 같은 날짜별 worklog 파일은 만들지 않는다.
* 의미 있는 Kiro 문서 작업을 수행한 경우 `.kiro/WORKLOG.md`를 업데이트한다.
* spec 구조, 루트 공통 문서, 신규 spec 생성 같은 주요 변경이 있으면 `.kiro/CHANGELOG.md`를 업데이트한다.
* 작업공간 목적, 폴더 구조, 주요 문서 목록이 바뀌면 `.kiro/README.md`를 업데이트한다.
* `CHANGELOG.md`는 구조 변경, 신규 spec 생성, 주요 문서 재구성 때만 갱신한다.

## 4. 문서 가독성 기준

### 4.1 기준 샘플

새로운 spec 문서, 루트 공통 문서, 기존 문서의 대규모 재구성을 수행할 때는 아래 3개 문서의 밀도와 섹션 구조를 우선 참고한다.

| 문서                   | 참고 기준                                                            |
| -------------------- | ---------------------------------------------------------------- |
| `.kiro/README.md`    | 목적 · 범위 · 현재 상태 · 안전 안내 · 추가 참고 위치                               |
| `.kiro/WORKLOG.md`   | `Summary / Completed / Evidence / Risks / Follow-ups / Security` |
| `.kiro/CHANGELOG.md` | `Added / Changed / Removed / Security`                           |

문서 상단은 운영자가 1분 안에 현재 상태와 다음 조치를 파악할 수 있어야 한다.

### 4.2 루트 공통 문서 구조 원칙

`specs/_common/` 하위 단일 기준 문서는 아래 흐름을 기본 구조로 삼는다.

| 순서 | 섹션                                                          | 내용                                |
| -- | ----------------------------------------------------------- | --------------------------------- |
| 1  | `Purpose`                                                   | 이 문서가 답하는 질문을 1~3줄로 설명            |
| 2  | `Dashboard` / `Summary`                                     | 운영자가 매일 확인할 현재 상태                 |
| 3  | `Index`                                                     | ID · 상태 · 다음 조치 · Details 링크 중심 표 |
| 4  | `Details` / `Appendix` / `Historical Notes` / `Usage Notes` | 긴 근거와 과거 이력                       |
| 5  | `Update Rules` / `Security Notes`                           | 갱신 조건과 민감정보 금지 규칙                 |

파일별 권장 구조:

| 문서                                  | 권장 구조                                                                                                                                                       |
| ----------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `operator-decisions.md`             | Purpose → Review Needed → Status Legend → Decision Dashboard → At a Glance → Decision Index → Decision Details → Change Log → Update Rules → Security Notes |
| `risk-register.md`                  | Purpose → Risk Dashboard → Immediate Action Risks → Risk Index → Risk Details → Accepted/Closed Risks → Update Rules → Security Notes                       |
| `followups-overview.md`             | Purpose → Current Follow-up Dashboard → Now/Next/Later/Blocked/Done Recently → Spec Roadmap → Historical Notes → Update Rules → Security Notes              |
| `ms-aws-service-decision-matrix.md` | Purpose → Final Recommendation Summary → MS Decision Cards → Rejected/Deferred Services → Portfolio Appeal Notes → Appendix → Security Notes                |
| `aws-resource-glossary.md`          | Purpose → Category TOC → Glossary → Usage Notes → Update Rules → Security Notes                                                                             |
| `cost-simulation.md`                | Purpose → Cost Dashboard → Assumptions → Scenario Details → Cost Drivers → Update Rules → Security Notes                                                    |

### 4.3 표와 Details 분리 원칙

* 표는 Index 역할만 한다.
* 표에는 ID, 상태, 선택값, 다음 조치, Details 링크 같은 핵심 컬럼만 둔다.
* 긴 근거, 비용 영향, 운영 리스크, mitigation, detection, rollback, evidence, 과거 이력은 표 밖 Details / Appendix / Historical Notes / Usage Notes로 분리한다.
* 같은 사실을 여러 문서에 장문으로 반복하지 않는다.
* 상세 기준 문서나 operation-notes 링크를 참조한다.
* operation-notes 수준의 긴 실행 로그, raw evidence, 검증 결과 전문은 루트 공통 문서에 직접 붙여넣지 않는다.

### 4.4 문서 최소화 원칙

* 새 문서를 만들기 전에 기존 문서에 흡수 가능한지 먼저 판단한다.
* 운영자가 매일 봐야 하는 문서는 각 spec의 `README.md`, `runbook.md`, `validation-checklist.md`, `operation-notes.md`로 제한한다.
* `requirements.md`, `design.md`, `tasks.md`, `traceability-matrix.md`는 Kiro/감사용 문서로 취급한다.
* 같은 결정값은 `operator-decisions.md`에만 상세히 기록하고, 다른 문서에서는 Decision ID와 짧은 요약만 참조한다.
* 작업 완료 후 자동 갱신 대상은 기본적으로 최대 3개 파일로 제한한다.

## 5. Markdown Readability Rules

### 5.1 원칙

* Markdown은 GitHub preview 기준으로 읽기 쉽게 작성한다.
* 운영자가 하루 뒤에 열어도 바로 이해할 수 있게 작성한다.
* 긴 문단보다 짧은 bullet, 긴 bullet보다 표, 긴 표보다 Index + Details 구조를 우선한다.
* 한 줄이 과도하게 길어지면 문장 분할, 표 전환, Details 이동 중 하나를 선택한다.
* 표 셀 안에서 줄바꿈이 필요하면 `<br>`을 우선 사용한다.

### 5.2 상태 배지

상태 배지는 아래 5종만 사용한다.

| 배지 | 의미                            | 예시                       |
| -- | ----------------------------- | ------------------------ |
| 🔴 | 치명 · 금지 · live · 고위험          | aws-live 자동 BUY/SELL 미승인 |
| 🟠 | 대기 · 관찰 · Open · 미확정          | 후속 검증 필요                 |
| 🟢 | 완료 · 성공 · ENABLED · Mitigated | Scheduler ENABLED        |
| 🔵 | 참고 · evidence · 정보            | 검증 링크                    |
| ⚫  | 해당 없음 · N/A                   | 실제로 무의미한 값               |

우선순위는 `🔴 > 🟠 > 🟢 > 🔵 > ⚫`다.

하나의 항목이 여러 상태에 해당하면 위험도가 높은 하나만 표시한다.

### 5.3 HTML 컬러

HTML 인라인 색상은 치명, 금지, live, 승인 게이트 안전 안내에만 제한적으로 사용한다.

| 용도       | 색상        |
| -------- | --------- |
| critical | `#D1242F` |
| warning  | `#BF8700` |
| done     | `#1A7F37` |
| info     | `#0969DA` |

상태 배지로 이미 충분히 표현되는 정보에는 HTML 색상을 중복 적용하지 않는다.

### 5.4 Bold 사용 기준

Bold는 아래에만 사용한다.

| 대상       | 예시                           |
| -------- | ---------------------------- |
| 최종 상태값   | **SUCCEEDED**                |
| 핵심 안전 문구 | **aws-live 자동 BUY/SELL 미진행** |
| 핵심 판정    | **NO_TARGET 안전 종료**          |
| 중요 ID    | **R-AUTO-037**               |
| 주요 결론    | **서비스 선택 변경 없음**             |

### 5.5 금지 서식

* 장식용 이모지 남발 금지
* 표 안 장문 paragraph 금지
* 4개 이상 슬래시 체인 금지
* raw log 전문 붙여넣기 금지
* 성공/실패 marker를 실제 결과와 다르게 기록 금지
* 탭 문자 사용 금지

## 6. Security Rules

실제 secret, password, token, app key, app secret, 계좌번호, webhook URL을 절대 작성하지 않는다.

민감정보가 필요한 경우 아래 placeholder만 사용한다.

| Placeholder                  | 용도              |
| ---------------------------- | --------------- |
| `[REDACTED]`                 | 일반 민감정보         |
| `[REDACTED_ACCOUNT_NO]`      | 계좌번호            |
| `[REDACTED_PUBLIC_IP]`       | public IP / EIP |
| `[REDACTED_ARN]`             | ARN             |
| `[REDACTED_TASK_ARN]`        | task ARN        |
| `[REDACTED_SECRET_ARN]`      | secret ARN      |
| `[REDACTED_BROKER_ORDER_NO]` | broker 주문번호     |

요약, 로그, 예시, 표, 생성 문서 어디에도 민감정보 값을 출력하지 않는다.

민감정보 예시:

| 구분     | 내용                                           |
| ------ | -------------------------------------------- |
| DB     | password                                     |
| KIS    | app key · app secret                         |
| Slack  | webhook URL                                  |
| 인증     | token                                        |
| 계정     | 실제 계좌번호 · 실제 account-id                      |
| AWS    | 실제 ARN · public IP · EIP · task ARN · ENI ID |
| broker | broker_order_no                              |
| image  | image digest full sha256                     |

## 7. Execution Rules

Spec 작성 작업에서는 아래 행위를 하지 않는다.

| 금지 항목 | 내용                             |
| ----- | ------------------------------ |
| AWS   | 리소스 생성 · 수정 · 삭제               |
| 운영    | 운영 entrypoint 실행               |
| 코드    | 애플리케이션 소스 코드 수정                |
| 주문    | broker 주문 스크립트 실행              |
| API   | KIS API 호출 · Slack webhook 전송  |
| DB    | DB DDL / DML / psql 실행         |
| 크롤러   | crawler / Selenium / Chrome 실행 |
| live  | live cutover 수행                |

AWS Console 작업이나 구현 절차가 필요한 경우 실제 실행하지 않고 runbook 단계로만 작성한다.

### 7.1 Kiro 자체 실행 오버헤드 제한

Kiro는 문서 작업 중 불필요한 실행 오버헤드를 만들지 않는다.

| 항목 | 규칙 |
| --- | --- |
| sub-agent | 사용자가 직접 요청한 경우에만 사용 |
| orchestrator | 별도 생성 금지 |
| 병렬 wave | baseline 측정처럼 명백히 독립적인 읽기 작업에만 허용 |
| 재시도 | 같은 실패를 2회 반복하면 중단 후 보고 |
| 임시 파일 | workspace 내부 `.kiro/tmp-*` 만 허용 |
| 출력 | 긴 stdout 대신 요약만 보고 |
| 검증 | 필수 count 와 diff check 중심 |

문서 편집은 “직접 읽고 직접 수정”을 기본값으로 한다. Kiro가 스스로 복잡한 실행 체계를 만들기 시작하면 즉시 중단하고 단순 프롬프트로 축소한다.

## 8. 운영 명령 작성 규칙

runbook, validation-checklist, operation-notes에 명령 예시를 작성할 때만 적용한다.

실제 명령 실행은 하지 않는다.

### 8.1 AWS CLI 명령

AWS CLI 명령은 `list/describe → 변수 추출 → 후속 검증` 패턴으로 작성한다.

* 운영자가 ARN, task ARN, ENI ID, LOG_STREAM, image digest를 손으로 치환해야 하는 1줄 예시는 작성하지 않는다.
* 동적 식별자는 `list` 또는 `describe`로 먼저 조회한 뒤 변수에 담아 사용한다.
* public IP 같은 민감 후보 값은 문서에 평문 기록하지 않고 `[REDACTED_PUBLIC_IP]`를 사용한다.

### 8.2 psql 검증 쿼리

psql 검증 쿼리는 `information_schema.columns`로 실제 컬럼명을 먼저 확인한 뒤 작성한다.

* 추정 컬럼명으로 `SELECT` / `JOIN`을 작성하지 않는다.
* 한글 SQL은 `psql -c`로 직접 넘기지 않는다.
* 한글이 포함된 SQL은 UTF-8 No BOM `.sql` 파일 + `psql -f` 패턴을 사용한다.

### 8.3 실패 처리

* 실패한 SQL / 명령 뒤에 SUCCESS / DONE marker를 출력하지 않는다.
* 실패 가능성이 있는 명령은 검증 조건과 실패 시 중단 조건을 함께 적는다.
* PowerShell native command 실패는 `$LASTEXITCODE` 확인을 명시한다.

### 8.4 Windows / PowerShell / SSM

* Windows PowerShell, SQL, JSON, SSM command 파일은 UTF-8 No BOM 기준으로 작성한다.
* SSM multiline command는 UTF-8 No BOM JSON 파일 + `--parameters file://...` 패턴을 사용한다.
* Windows AWS CLI stdout의 emoji / 특수문자 cp949 인코딩 오류를 고려한다.
* 긴 stdout 또는 emoji 포함 출력은 파일 저장 후 확인하는 패턴을 선호한다.
* PowerShell here-string 안 bash 변수는 single-quoted here-string + placeholder `.Replace()` 패턴을 권장한다.

### 8.5 DB password / secret

DB password, API key, token, Slack webhook, presigned URL 등 secret 값은 문서, 명령, 로그, 메모리에 포함하지 않는다.

DB 암호가 필요한 예시는 아래 우선순위만 설명한다.

| 우선순위 | 값                                       |
| ---- | --------------------------------------- |
| 1    | 사용자가 이미 세팅한 `$env:INTEREST_DB_PASSWORD` |
| 2    | 기존 `$env:PGPASSWORD`                    |
| 3    | local secret loader                     |

ChatGPT / Kiro는 DB password 값을 묻거나 `$env:PGPASSWORD = "비밀번호"` 형태의 명령을 제공하지 않는다.

## 9. Git Rules

허용되는 git 명령은 읽기 전용 상태 확인에 한정한다.

| 허용                                                              | 금지                                                                                                                               |
| --------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| `git status --short`<br>`git diff --stat`<br>`git diff --check` | `git add`<br>`git commit`<br>`git rm`<br>`git mv`<br>`git push`<br>`git checkout`<br>`git reset`<br>`git stash`<br>`git restore` |

검증 실패 시 자동 롤백하지 않는다.

실패한 파일, 실패 속성, 원인, 권장 조치를 보고한다.

## 10. Update Checklist

문서 작업 완료 전 아래를 확인한다.

| 항목         | 확인                                                                                                |
| ---------- | ------------------------------------------------------------------------------------------------- |
| 범위         | 요청된 spec / 공통 문서에 한정되어 있는가?                                                                       |
| MS 파일      | 8개 MS 소스 / README / CHANGELOG / worklog / AGENTS.md를 불필요하게 수정하지 않았는가?                             |
| 단일 기준      | 공통 문서의 단일 기준과 충돌하지 않는가?                                                                           |
| 루트 문서      | README / WORKLOG / CHANGELOG 스타일의 짧은 상단 구조를 따르는가?                                                 |
| 2컬럼 표      | 0.1의 표 헤더 후보 중 가장 맞는 것을 우선 사용했는가?                                                                 |
| 표 셀        | 여러 값은 `<br>`로 나누고, 셀 안 내용을 짧게 유지했는가?                                                              |
| Details 분리 | 긴 근거는 Details / Appendix / Historical Notes / Usage Notes로 분리했는가?                                 |
| 중복         | 같은 사실을 여러 문서에 장문으로 반복하지 않았는가?                                                                     |
| 민감정보       | secret / password / token / webhook URL / 계좌번호 / 실제 ARN / public IP / broker_order_no를 기록하지 않았는가? |
| 실행 금지      | 실제 AWS / DB / broker / KIS / Slack / crawler / Spring Boot 실행을 하지 않았는가?                           |
| 2컬럼 기본     | 신규 표를 기본 2컬럼으로 만들고, 3컬럼 이상 예외는 사유를 남겼는가?                                             |
| 속도 제한      | sub-agent / orchestrator / 새 scanner / workspace 밖 TEMP 사용 없이 작업했는가?                              |
| 시간 상한      | 병목 task가 15분을 넘으면 부분 완료 또는 manual review 로 넘겼는가?                                           |
| 인코딩        | 한글 포함 파일은 UTF-8 No BOM으로 저장했는가?                                                                   |
| marker     | 실패한 명령 뒤에 SUCCESS / DONE marker를 쓰지 않았는가?                                                         |
| 루트 로그      | 필요 시 WORKLOG / CHANGELOG / README 갱신 기준을 지켰는가?                                                    |
