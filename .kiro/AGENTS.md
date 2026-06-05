# Kiro AWS Migration 작업 규칙

이 `.kiro` 작업공간은 PORT-STRATEGY-AI 포트폴리오의 AWS Migration 관련 cross-service spec을 작성하고 관리하기 위한 공간이다.

## 범위

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

## 루트 참조 문서

AWS Migration spec을 새로 만들거나 수정하기 전에 `.kiro/specs` 아래의 루트 공통 문서를 먼저 확인한다.

* `operator-decisions.md`
* `ms-aws-service-decision-matrix.md`
* `cost-simulation.md`
* `followups-overview.md`
* `aws-resource-glossary.md`
* `note-aws-landscape-2021-vs-2026.md`

위 문서들은 모든 spec에서 공유하는 기준 문서로 사용한다.

## 단일 기준 문서 규칙

* `operator-decisions.md`는 운영자 결정사항의 단일 기준 문서다.
* `ms-aws-service-decision-matrix.md`는 MS별 AWS 서비스 권고와 판단 근거의 단일 기준 문서다.
* `cost-simulation.md`는 비용 가정과 월 예상 비용의 단일 기준 문서다.
* `followups-overview.md`는 후속 spec 진행 순서와 의존성 맵의 단일 기준 문서다.
* `aws-resource-glossary.md`는 AWS 용어 설명의 단일 기준 문서다.
* `01-aws-migration-foundation`은 baseline foundation spec이다. foundation 수준의 결정이 바뀌는 경우가 아니라면 반복해서 내용을 복사하거나 재작성하지 않는다.

## 업데이트 규칙

새 spec에서 운영자 결정이 바뀌면 `operator-decisions.md`를 업데이트한다.

새 spec에서 특정 MS의 AWS 서비스 권고가 바뀌면 `ms-aws-service-decision-matrix.md`를 업데이트하고, 필요한 경우 `operator-decisions.md`도 함께 업데이트한다.

새 spec에서 월 비용 가정, RDS 크기, VPC Endpoint 수, ALB 사용 여부, NAT 사용 여부, EC2 크기, Fargate 사용량 가정이 바뀌면 `cost-simulation.md`를 업데이트한다.

새 spec에서 후속 spec의 순서, 의존성, 범위가 바뀌면 `followups-overview.md`를 업데이트한다.

새 AWS 서비스나 용어가 Migration에서 중요하게 사용되기 시작하면 `aws-resource-glossary.md`를 업데이트한다.

루트 공통 문서를 기계적으로 전부 업데이트하지 않는다. 현재 결정이나 spec에 영향을 받는 문서만 업데이트한다.

## MS별 AGENTS.md 참조 규칙

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

## Spec 작성 규칙

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

## 보안 규칙

실제 secret, password, token, app key, app secret, 계좌번호, webhook URL을 절대 작성하지 않는다.

모든 민감정보 값은 `[REDACTED]`로 표기한다.

요약, 로그, 예시, 표, 생성 문서 어디에도 민감정보 값을 출력하지 않는다.

## 실행 규칙

Spec 작성 작업에서는 아래 행위를 하지 않는다.

* AWS 리소스 생성 금지
* 운영 entrypoint 실행 금지
* 애플리케이션 소스 코드 수정 금지
* broker 주문 스크립트 실행 금지
* live cutover 수행 금지

AWS Console 작업이나 구현 절차가 필요한 경우에는 실제 실행하지 않고 runbook 단계로만 작성한다.

## README / CHANGELOG / WORKLOG 관리 규칙

`.kiro` 작업공간은 각 MS와 달리 날짜별 worklog 파일을 만들지 않는다.

- `.kiro/README.md`는 Kiro AWS Migration 작업공간의 목적, 범위, 폴더 구조, 주요 문서 역할을 설명한다.
- `.kiro/CHANGELOG.md`는 spec 구조 변경, 루트 공통 문서 변경, 신규 spec 생성, 주요 문서 재구성 이력만 간단히 기록한다.
- `.kiro/WORKLOG.md`는 Kiro 작업 세션의 간단 로그를 하나의 파일에 누적한다.
- `.kiro/docs/worklog/YYYY-MM-DD.md` 같은 날짜별 worklog 파일은 만들지 않는다.
- 각 MS의 `docs/worklog/YYYY-MM-DD.md` 형식보다 훨씬 간단하게 작성한다.
- 의미 있는 Kiro 문서 작업을 수행한 경우 `.kiro/WORKLOG.md`를 업데이트한다.
- spec 구조, 루트 공통 문서, 신규 spec 생성 같은 주요 변경이 있으면 `.kiro/CHANGELOG.md`를 업데이트한다.
- 작업공간 목적, 폴더 구조, 주요 문서 목록이 바뀌면 `.kiro/README.md`를 업데이트한다.
- secret, password, token, app key, app secret, 계좌번호, webhook URL 같은 민감정보는 절대 기록하지 않는다.
- 민감정보는 필요한 경우 `[REDACTED]`로만 표기한다.
