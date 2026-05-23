# AGENTS.md - port-view

## 프로젝트
- 이 프로젝트는 포트폴리오 시스템의 Spring MVC / Thymeleaf 기반 View 마이크로서비스다.
- 주요 화면은 대시보드, 잔고, 보유 종목, 주문 내역, 전략 실행 계획, 백테스트 리포트, Daily Batch 화면이다.
- 화면 문구는 기본적으로 한글을 유지한다.
- 기존 URL, Controller endpoint, Thymeleaf model attribute 이름은 명시 요청 없이는 변경하지 않는다.

## 작업 범위
- 현재 `port-view` 폴더 안에서만 작업한다.
- `port-view` 밖의 파일은 수정하지 않는다.
- 큰 리팩토링 전에는 먼저 짧은 분석 문서나 계획 문서를 작성한다.
- 변경은 작고 검토 가능한 단위로 진행한다.

## 허용 작업
- README / docs 작성 및 수정
- View 폴더 구조 분석
- 사용하지 않는 파일 후보 분석
- 동작을 바꾸지 않는 작은 리팩토링
- Template / CSS / Java 코드 정리
- 문구, 라벨, 화면 구성 개선

## 절대 금지
- Daily Batch 실행 금지
- Slack Webhook 테스트 실행 금지
- 외부 투자/주문 API 호출 금지
- DB DDL/DML 직접 실행 금지
- 실제 로컬 설정 파일 수정 금지
- 비밀번호, 토큰, Webhook URL, API Key, 계좌번호 전체 출력 금지
- 파일 즉시 삭제 금지. 먼저 삭제 후보로만 정리한다.
- 명시 요청 없이는 commit 금지

## 유지해야 할 것
- 기존 화면 URL 유지
- 기존 Controller endpoint 유지
- 기존 Thymeleaf model attribute 이름 유지
- 기존 DB 테이블명/컬럼명 유지
- 기존 설정 key 이름 유지
- 기존 기능 동작 유지

## Git 규칙
- 작업 전 `git status --short`를 확인한다.
- 작업 후 `git diff --stat`를 확인한다.
- 명시 요청 없이는 commit 하지 않는다.

## 검증 명령
코드 수정 후:

```powershell
.\mvnw.cmd clean compile
git diff --stat
```

문서만 수정한 경우:

```powershell
git diff --stat
```

## 완료 보고
작업 완료 후 아래만 짧게 보고한다.

- 변경 파일
- 변경 요약
- 검증 결과
- 남은 위험 또는 후속 작업

## 추가 요청 사항
- 코드 수정 전에 짧은 계획을 먼저 보여준다.
- 결과는 30줄 이내로 정리해준다.
- 표는 쓰지 않는다.
- 긴 설명은 생략한다.
- 관련 없는 파일은 열거나 수정하지 않는다.
- 동작을 바꾸지 않는 최소 수정만 한다.
