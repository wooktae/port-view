# 사용하지 않는 View 파일 후보 분석

## 확인 범위

- `src/main/resources/templates`
- `src/main/resources/static/css`

## 확인 기준

- Controller `return` view name 확인
- `ViewNames` 상수 확인
- Thymeleaf HTML의 CSS link 확인
- `static/css` 참조 문자열 검색

## 요약

- `ViewNames`에서 대부분의 화면은 `pages/*` 템플릿으로 전환되어 있다.
- 루트 템플릿 중 `holdings`, `trade-orders`, `get-price-realtime`, `strategy_daily`는 현재 Controller에서 반환된다.
- `templates/pages` 하위 HTML은 모두 `ViewNames`를 통해 Controller에서 반환되는 것으로 확인된다.
- `static/css/pages` 하위 CSS는 모두 `templates/pages` 하위 HTML에서 참조된다.
- 루트 `static/css/dashboard.css`, `static/css/strategy-execution.css`는 아직 활성 루트 템플릿에서 참조되므로 삭제 후보로 보지 않는다.

## 삭제 후보

### `src/main/resources/templates/balance-summary.html`

- 사용하지 않는 것으로 의심되는 이유: `ViewNames.BALANCE_SUMMARY`가 `pages/balance-summary`를 반환한다.
- 확인 근거: `BalanceController`는 `ViewNames.BALANCE_SUMMARY`를 반환하고, 상수 값은 `pages/balance-summary`이다.
- 위험도: MEDIUM
- 추천 조치: 삭제 후보

### `src/main/resources/templates/dashboard.html`

- 사용하지 않는 것으로 의심되는 이유: `ViewNames.DASHBOARD`가 `pages/dashboard`를 반환한다.
- 확인 근거: `DashboardController`는 `ViewNames.DASHBOARD`를 반환하고, 상수 값은 `pages/dashboard`이다.
- 위험도: MEDIUM
- 추천 조치: 삭제 후보

### `src/main/resources/templates/order-detail.html`

- 사용하지 않는 것으로 의심되는 이유: `ViewNames.ORDER_DETAIL`이 `pages/order-detail`을 반환한다.
- 확인 근거: `OrderController`는 `ViewNames.ORDER_DETAIL`을 반환하고, 상수 값은 `pages/order-detail`이다.
- 위험도: MEDIUM
- 추천 조치: 삭제 후보

### `src/main/resources/templates/orders.html`

- 사용하지 않는 것으로 의심되는 이유: `ViewNames.ORDERS`가 `pages/orders`를 반환한다.
- 확인 근거: `OrderController`는 `ViewNames.ORDERS`를 반환하고, 상수 값은 `pages/orders`이다.
- 위험도: MEDIUM
- 추천 조치: 삭제 후보

### `src/main/resources/templates/positions.html`

- 사용하지 않는 것으로 의심되는 이유: `ViewNames.POSITIONS`가 `pages/positions`를 반환한다.
- 확인 근거: `PositionController`는 `ViewNames.POSITIONS`를 반환하고, 상수 값은 `pages/positions`이다.
- 위험도: MEDIUM
- 추천 조치: 삭제 후보

### `src/main/resources/templates/strategy_detail.html`

- 사용하지 않는 것으로 의심되는 이유: `ViewNames.STRATEGY_DETAIL`이 `pages/strategy_detail`을 반환한다.
- 확인 근거: `StrategyExecutionViewController`는 `ViewNames.STRATEGY_DETAIL`을 반환하고, 상수 값은 `pages/strategy_detail`이다.
- 위험도: MEDIUM
- 추천 조치: 삭제 후보

### `src/main/resources/templates/strategy_plans.html`

- 사용하지 않는 것으로 의심되는 이유: `ViewNames.STRATEGY_PLANS`가 `pages/strategy_plans`를 반환한다.
- 확인 근거: `StrategyExecutionViewController`는 `ViewNames.STRATEGY_PLANS`를 반환하고, 상수 값은 `pages/strategy_plans`이다.
- 위험도: MEDIUM
- 추천 조치: 삭제 후보

### `src/main/resources/templates/strategy_report.html`

- 사용하지 않는 것으로 의심되는 이유: `ViewNames.STRATEGY_REPORT`가 `pages/strategy_report`를 반환한다.
- 확인 근거: `StrategyReportController`는 `ViewNames.STRATEGY_REPORT`를 반환하고, 상수 값은 `pages/strategy_report`이다.
- 위험도: MEDIUM
- 추천 조치: 삭제 후보

### `src/main/resources/static/css/order-detail.css`

- 사용하지 않는 것으로 의심되는 이유: 루트 `order-detail.html`에서만 참조되고, 현재 상세 주문 화면은 `pages/order-detail`을 사용한다.
- 확인 근거: CSS 참조 검색 결과 `@{/css/order-detail.css}`는 루트 `templates/order-detail.html`에서만 확인되며, `pages/order-detail.html`은 `/css/pages/order-detail.css`를 참조한다.
- 위험도: MEDIUM
- 추천 조치: 후보 보류

### `src/main/resources/static/css/orders.css`

- 사용하지 않는 것으로 의심되는 이유: 루트 `orders.html`, `order-detail.html`에서만 참조되고, 현재 주문 화면은 `pages/orders`, `pages/order-detail`을 사용한다.
- 확인 근거: CSS 참조 검색 결과 `@{/css/orders.css}`는 루트 템플릿에서만 확인되며, `pages/orders.html`은 `/css/pages/orders.css`를 참조한다.
- 위험도: MEDIUM
- 추천 조치: 후보 보류

### `src/main/resources/static/css/positions.css`

- 사용하지 않는 것으로 의심되는 이유: 루트 `positions.html`에서만 참조되고, 현재 보유 종목 화면은 `pages/positions`와 `pages/position-detail`을 사용한다.
- 확인 근거: CSS 참조 검색 결과 `@{/css/positions.css}`는 루트 `templates/positions.html`에서만 확인되며, pages 템플릿은 `/css/pages/positions.css`를 참조한다.
- 위험도: MEDIUM
- 추천 조치: 후보 보류

### `src/main/resources/static/css/strategy-report.css`

- 사용하지 않는 것으로 의심되는 이유: 루트 `strategy_report.html`에서만 참조되고, 현재 리포트 화면은 `pages/strategy_report`를 사용한다.
- 확인 근거: CSS 참조 검색 결과 `@{/css/strategy-report.css}`는 루트 `templates/strategy_report.html`에서만 확인되며, `pages/strategy_report.html`은 `/css/pages/strategy-report.css`를 참조한다.
- 위험도: MEDIUM
- 추천 조치: 후보 보류

## 유지 권장

- `src/main/resources/templates/get-price-realtime.html`: `ViewNames.GET_PRICE_REALTIME = "get-price-realtime"`
- `src/main/resources/templates/holdings.html`: `ViewNames.HOLDINGS = "holdings"`
- `src/main/resources/templates/strategy_daily.html`: `ViewNames.STRATEGY_DAILY = "strategy_daily"`
- `src/main/resources/templates/trade-orders.html`: `ViewNames.TRADE_ORDERS = "trade-orders"`
- `src/main/resources/templates/pages/*.html`: 현재 `ViewNames`에서 반환 중
- `src/main/resources/static/css/dashboard.css`: 활성 루트 템플릿에서 참조 중
- `src/main/resources/static/css/strategy-execution.css`: 활성 루트 `strategy_daily.html`에서 참조 중
- `src/main/resources/static/css/pages/*.css`: 현재 `templates/pages` 하위 HTML에서 참조 중
