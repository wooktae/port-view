# Security Notes

이 문서는 `port-view` 저장소에서 주의해야 할 민감정보 후보와 관리 원칙을 정리합니다. 실제 값은 절대 문서에 기록하지 않습니다.

## 민감정보 후보 위치

값은 기록하지 않고 위치와 유형만 정리합니다.

### application.properties

위치:

- `src/main/resources/application.properties`

후보 유형:

- DB URL
- DB username
- DB password
- 기본 계좌번호
- Connector base URL
- 외부 프로젝트 절대 경로
- Slack webhook 설정

### Templates

위치:

- `src/main/resources/templates/pages/*.html`
- `src/main/resources/templates/*.html`

후보 유형:

- 기본 계좌번호 fallback 문자열
- 화면에 노출되는 계좌번호
- 외부 URL 표시 또는 link

### Config Classes

위치:

- `DailyBatchProperties`
- `SnapshotRefreshProperties`
- `ConnectorProperties`
- `PortfolioViewProperties`

후보 유형:

- 외부 프로젝트 절대 경로 fallback
- Connector URL fallback
- 기본 계좌번호 fallback
- Python/script 실행 경로

### Logs and Payloads

위치:

- Daily Batch stdout/stderr tail
- step result payload
- application log
- Slack message text

후보 유형:

- 외부 command 출력에 포함될 수 있는 계좌번호
- API 응답 일부
- 오류 메시지 내 URL/path
- 운영 환경 정보

## 값 기록 금지 원칙

다음 값은 README/docs/issue/PR description/log에 직접 쓰지 않습니다.

- DB password
- Slack webhook URL
- 실제 계좌번호
- token/API key
- Authorization header
- 운영 DB URL
- 내부 운영 endpoint
- 개인 PC 또는 운영 서버의 민감한 절대 경로

문서에는 설정 키, 유형, 관리 방법만 기록합니다.

## DB Password 관리 원칙

- Git tracked `application.properties`에 실제 password를 두지 않습니다.
- 로컬 개발은 Git ignored local config 또는 환경변수를 사용합니다.
- 운영 환경은 secret manager 또는 deployment secret으로 주입합니다.
- SQL 로그가 credential을 노출하지 않는지 확인합니다.

## Slack Webhook 관리 원칙

- Slack webhook URL은 secret입니다.
- README/docs에 값 자체를 쓰지 않습니다.
- application log에 webhook URL을 출력하지 않습니다.
- 테스트 메시지 실패 시에도 webhook 값을 masking합니다.
- 환경변수 또는 secret manager로 주입합니다.

## 계좌번호 관리 원칙

- 실제 계좌번호는 민감정보 후보로 취급합니다.
- 기본 계좌번호는 환경변수/local config로 분리합니다.
- template에 fallback으로 직접 넣는 방식은 줄이고, server-side resolver에서 관리하는 방향을 권장합니다.
- 화면 노출이 필요한 경우에도 접근 통제/배포 범위를 고려합니다.

## 외부 절대 경로 관리 원칙

- 외부 프로젝트 절대 경로는 개인/서버 환경에 종속됩니다.
- Git tracked config에 실제 운영 경로를 두지 않습니다.
- profile별 config 또는 환경변수로 분리합니다.
- batch 실행 계정의 권한 범위를 최소화합니다.

## Connector URL 관리 원칙

- Connector URL은 환경별로 분리합니다.
- 운영 endpoint는 문서에 직접 기록하지 않습니다.
- API 인증이 도입되면 token/API key는 secret manager를 사용합니다.
- 에러 로그에 full URL과 query parameter가 노출되지 않도록 주의합니다.

## Local/Dev/Prod 분리 방향

권장 구조:

- `application.properties`
  - 공통 non-secret 기본값
- `application-local.properties`
  - 개인 로컬 개발값, Git ignore
- `application-dev.properties`
  - 개발 서버 non-secret 설정
- `application-prod.properties`
  - 운영 non-secret 설정
- Environment variables
  - DB password, Slack webhook, 계좌번호, token
- Secret manager/deployment secrets
  - 운영 민감정보

예시 원칙:

- 문서에는 `${ENV_NAME}` 형태의 placeholder만 사용합니다.
- 실제 값은 각 환경에서 주입합니다.

## Git Commit 전 점검 Checklist

커밋 전 다음을 확인합니다.

- DB password가 포함되지 않았는가
- Slack webhook URL이 포함되지 않았는가
- 실제 계좌번호가 새로 추가되지 않았는가
- token/API key/Authorization header가 포함되지 않았는가
- 운영 endpoint나 내부망 URL이 직접 추가되지 않았는가
- 개인 PC 또는 운영 서버 절대 경로가 새로 추가되지 않았는가
- Daily Batch 로그나 payload sample에 민감정보가 포함되지 않았는가
- README/docs에 실제 값이 아니라 설정 키와 관리 원칙만 적었는가
- `git diff`에서 민감정보 후보 문자열을 검색했는가

## 검색 키워드 예시

커밋 전 검색할 수 있는 키워드 유형:

- `password`
- `secret`
- `token`
- `webhook`
- `authorization`
- `bearer`
- `account`
- `accountNo`
- `jdbc`
- `url`

검색 결과는 값 자체를 공유하지 말고 파일/위치/유형만 공유합니다.
