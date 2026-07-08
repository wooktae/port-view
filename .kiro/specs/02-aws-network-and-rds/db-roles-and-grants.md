# DB Role / 권한 분리 — 02-aws-network-and-rds 보조 문서

본 문서는 `portfolio` DB(`aws-paper`, RDS PostgreSQL)에 restore가 끝난 시점의 DB role / 권한 분리 설계와 적용 SQL 초안이다. 본 spec design.md "DB Role 권한 매트릭스" 섹션을 실행 가능한 형태로 풀어 쓰고, 사용자 신규 결정(`legacy schema는 기본적으로 app role에 권한을 주지 않는다`)을 반영했다.

본 문서에는 실제 password / secret value / RDS endpoint hostname / 계좌번호 / token을 적지 않는다. 모두 `[REDACTED]` 또는 placeholder만 사용한다.

대상 환경

- DB name: `portfolio`
- 주요 schema: `reference`, `interest`, `preprocessor`, `research`, `decision`, `execution`, `connector`, `ops`, `legacy`, `public`
- 운영 가정: admin / restore 계정은 운영 관리 전용. 애플리케이션 connection 문자열에 사용하지 않는다.

대상 7개 MS와 매핑 role

| MS | DB role |
|---|---|
| `port-marketconnector` | `marketconnector_app` |
| `port-view` | `view_app` |
| `port-interest-crawler` | `crawler_app` |
| `port-interest-preprocessor` | `preprocessor_app` |
| `port_strategy_decision` | `decision_app` |
| `port_strategy_research` | `research_app` |
| `port_strategy_execution` | `execution_app` |

`port_strategy_common`은 라이브러리이므로 자체 role을 두지 않는다.

## 1. MS별 DB role 설계안

세 종류 role을 분리한다.

| Role 종류 | 이름 | 책임 | 비고 |
|---|---|---|---|
| Owner / DDL | `portfolio_owner` | DB / schema 소유, DDL, 권한 정의, default privileges 설정. | 일상 운영 / 애플리케이션 connection 금지. SQL migration 시점에만 사용. |
| Admin / Restore | `portfolio_admin` | RDS master user. backup / restore / parameter / extension 관리. | 본 RDS의 master user(현 `portfolio_admin`). app connection 금지. SQL migration도 가능하면 `portfolio_owner`에 위임. |
| App | `*_app` 7종 | 각 MS application 전용. 최소 권한. | 아래 §2 매트릭스 참조. |

원칙

- App role에는 `LOGIN`만 부여한다. `CREATEDB`, `CREATEROLE`, `SUPERUSER`, `BYPASSRLS`, `REPLICATION`은 부여하지 않는다.
- App role은 자기 책임 schema를 own하지 않는다. schema owner는 `portfolio_owner`로 통일하고, app role에는 USAGE + 필요한 DML 권한만 grant한다.
- `legacy` schema는 기본적으로 모든 app role에 USAGE / SELECT를 grant하지 않는다. legacy 데이터 접근이 꼭 필요한 MS가 식별되면 그때 별도 결정으로 grant한다.
- 단일 App role 비밀번호는 Secrets Manager / SSM SecureString에 저장한다. 본 문서와 운영자 노트에는 `[REDACTED]`만 기록한다.

## 2. 권한 매트릭스

표 기호

- `R` = SELECT
- `W` = INSERT, UPDATE, DELETE
- `S` = sequence USAGE + SELECT (nextval / currval / lastval 사용 가능)
- `-` = USAGE / SELECT 모두 미부여 (schema도 grant 안 함)

| schema | marketconnector_app | crawler_app | preprocessor_app | decision_app | execution_app | research_app | view_app |
|---|---|---|---|---|---|---|---|
| reference | R | R/W (+S) | R | R | R | R | R |
| interest | - | R/W (+S) | R | - | - | R | R |
| preprocessor | - | - | R/W (+S) | R | - | R | R |
| research | - | - | - | R | R | R/W (+S) | R |
| decision | - | - | - | R/W (+S) | R | - | R |
| execution | R | - | - | - | R/W (+S) | - | R (write는 `05-port-view-ecs-and-runbook`에서 재검토) |
| connector | R/W (+S) | - | - | R | R | - | R |
| ops | - | - | - | - | - | - | R/W (+S) |
| legacy | - | - | - | - | - | - | - (기본 미부여) |
| public | R/W (+S) | R | R | R | R | R | R |

비고

- App role이 `R/W`인 schema는 자동으로 `S`(sequence USAGE + SELECT)도 부여된다. 신규 row 삽입 시 `nextval()` 호출이 필요하기 때문이다. 읽기만 하는 schema에는 sequence 권한을 부여하지 않는다.
- 본 spec의 02 design.md 매트릭스와 다른 점:
  - 모든 app role의 `legacy` 권한 제거 (사용자 결정).
  - `marketconnector_app`의 `execution` 권한을 R/W → R (read only)로 축소. `execution` write는 `execution_app`만 수행한다.
- `view_app` write 범위는 현재 `ops`만. `execution`을 view에서 직접 write할지 여부는 `05-port-view-ecs-and-runbook`에서 재검토.
- `public`은 호환성을 위해 7개 role 모두 USAGE는 가지지만, write는 `marketconnector_app`만 부여한다. 신규 데이터는 가능한 한 도메인 schema로 옮긴다.

## 3. search_path 전략

각 MS README에 정의된 기존 search_path를 유지하고, role 단위 `ALTER ROLE ... SET search_path`로 PostgreSQL 측에서 강제한다. application connection이 `connection-init-sql`로 설정하지 않더라도 같은 search_path가 적용되도록 한다.

| Role | search_path | 출처 |
|---|---|---|
| `marketconnector_app` | `connector, execution, legacy, reference, public` | port-marketconnector README |
| `crawler_app` | `interest, reference, legacy, public` | port-interest-crawler README |
| `preprocessor_app` | `preprocessor, interest, reference, legacy, public` | port-interest-preprocessor README |
| `decision_app` | `decision, research, preprocessor, execution, connector, reference, legacy, public` | port_strategy_decision README |
| `research_app` | `research, preprocessor, interest, reference, legacy, public` | port_strategy_research README |
| `execution_app` | `execution, decision, research, connector, preprocessor, interest, reference, legacy, public` | port_strategy_execution README |
| `view_app` | `ops, execution, decision, research, connector, preprocessor, interest, reference, legacy, public` | port-view README |

`legacy`가 search_path에 들어 있지만 본 결정상 USAGE 권한이 없으므로 해당 schema의 객체는 해석 후보에 잡혀도 권한 부족으로 접근이 거부된다. 즉 `legacy` 항목은 README 호환성 유지용으로만 남기고 실제 사용 시 명시적 grant 결정이 필요하다.

`portfolio_owner`와 `portfolio_admin`은 별도 search_path를 강제하지 않는다(필요 시 세션 단위 SET).

## 4. SQL 초안 (GRANT / ALTER ROLE / DEFAULT PRIVILEGES)

본 SQL은 `portfolio_admin`(RDS master) 또는 권한이 동등한 superuser-equivalent role로 `portfolio` DB에 접속한 상태에서 실행한다. 모든 password 자리는 placeholder다.

### 4.1 사전 점검

```sql
-- 접속 확인
SELECT current_database(), current_user, version();

-- 9개 도메인 schema가 존재하는지 확인 (없으면 CREATE 후 진행)
SELECT n.nspname
FROM pg_namespace n
WHERE n.nspname IN ('reference','interest','preprocessor','research','decision',
                    'execution','connector','ops','legacy','public')
ORDER BY 1;
```

### 4.2 owner role과 schema 소유권 정리

`portfolio_admin`이 `portfolio_owner` 멤버십을 먼저 가진 뒤 9개 도메인 schema owner를 `portfolio_owner`로 이관한다. RDS PostgreSQL에서는 `ALTER SCHEMA ... OWNER TO` 실행 계정이 새 owner role의 member여야 할 수 있으므로, schema owner 변경 전에 membership을 부여하는 순서가 안전하다.

```sql
-- 1) portfolio_owner 생성 (NOLOGIN 권고. SQL 실행 시 SET ROLE로 위임)
CREATE ROLE portfolio_owner NOLOGIN;

-- 2) master user(portfolio_admin)에게 portfolio_owner 멤버십 부여
--    => 이후 ALTER SCHEMA ... OWNER TO portfolio_owner 실행 가능
GRANT portfolio_owner TO portfolio_admin;

-- 3) 9개 도메인 schema 소유권을 portfolio_owner로 통일 (legacy 포함)
ALTER SCHEMA reference    OWNER TO portfolio_owner;
ALTER SCHEMA interest     OWNER TO portfolio_owner;
ALTER SCHEMA preprocessor OWNER TO portfolio_owner;
ALTER SCHEMA research     OWNER TO portfolio_owner;
ALTER SCHEMA decision     OWNER TO portfolio_owner;
ALTER SCHEMA execution    OWNER TO portfolio_owner;
ALTER SCHEMA connector    OWNER TO portfolio_owner;
ALTER SCHEMA ops          OWNER TO portfolio_owner;
ALTER SCHEMA legacy       OWNER TO portfolio_owner;
-- public은 RDS 정책상 보통 그대로 둔다. 필요 시:
-- ALTER SCHEMA public OWNER TO portfolio_owner;
```

#### 4.2.1 기존 table / sequence owner 현황 확인

schema owner만 `portfolio_owner`로 바꿔도 기존 table / sequence / view / index / matview의 owner는 restore 실행 계정(예: `portfolio_admin`)으로 남아 있을 수 있다. 기존 객체 owner가 `portfolio_admin`이면 §4.5의 `ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner`가 그 객체에는 적용되지 않으므로, 현재 owner 분포를 먼저 확인한다.

```sql
-- 9개 도메인 schema + public 안의 모든 relation owner 분포
SELECT n.nspname AS schema_name,
       c.relkind,            -- r=table, p=partitioned table, v=view, m=matview, S=sequence, i=index, ...
       r.rolname AS owner,
       count(*) AS object_count
FROM pg_class c
JOIN pg_namespace n ON n.oid = c.relnamespace
JOIN pg_roles r ON r.oid = c.relowner
WHERE n.nspname IN ('reference','interest','preprocessor','research','decision',
                    'execution','connector','ops','legacy','public')
GROUP BY n.nspname, c.relkind, r.rolname
ORDER BY n.nspname, c.relkind, r.rolname;
```

owner가 `portfolio_admin`(또는 다른 계정)으로 나오는 row가 있으면 §4.5 default privileges 적용 직전에 아래 중 하나로 owner를 `portfolio_owner`로 일괄 또는 객체별 이관한다. 본 spec에서는 SQL을 실제 실행하지 않고 명령 형식만 제시한다.

```sql
-- 옵션 A: 일괄 이관 (해당 계정이 소유한 모든 객체)
-- 주의: portfolio_admin 명의 RDS 운영 객체가 도메인 schema 외에도 있을 수 있으므로
--       반드시 위 owner 현황 결과를 먼저 검토한 뒤 실행한다.
REASSIGN OWNED BY portfolio_admin TO portfolio_owner;

-- 옵션 B: schema 단위 객체별 이관 (table / sequence / view / matview)
-- table / partitioned table
DO $$
DECLARE r record;
BEGIN
  FOR r IN
    SELECT n.nspname, c.relname
    FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE c.relkind IN ('r','p')
      AND n.nspname IN ('reference','interest','preprocessor','research','decision',
                        'execution','connector','ops','legacy')
  LOOP
    EXECUTE format('ALTER TABLE %I.%I OWNER TO portfolio_owner', r.nspname, r.relname);
  END LOOP;
END $$;

-- sequence
DO $$
DECLARE r record;
BEGIN
  FOR r IN
    SELECT n.nspname, c.relname
    FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE c.relkind = 'S'
      AND n.nspname IN ('reference','interest','preprocessor','research','decision',
                        'execution','connector','ops','legacy')
  LOOP
    EXECUTE format('ALTER SEQUENCE %I.%I OWNER TO portfolio_owner', r.nspname, r.relname);
  END LOOP;
END $$;

-- view / matview도 같은 패턴 (relkind = 'v', 'm') 으로 ALTER VIEW / ALTER MATERIALIZED VIEW 사용
```

### 4.3 App role 7개 생성

비밀번호는 placeholder만 기록한다. 운영자가 실제 실행할 때 환경변수 또는 Secrets Manager에서 주입한다.

#### 4.3.1 password placeholder 형식 주의

아래 SQL의 `:'pwd_*'` 표기는 `psql` 클라이언트의 변수 치환 문법이다. 그대로 다른 SQL 클라이언트에 붙여 넣으면 실패할 수 있다.

- 사용 가능: 로컬 / 운영자 PC의 `psql` (예: `psql -v pwd_view='<APP_PASSWORD_PLACEHOLDER>' -f roles.sql`).
- 사용 불가 / 변환 필요: AWS RDS Query Editor v2, pgAdmin Query Tool, DBeaver Run SQL, JDBC 기반 SQL Workbench 등 대부분의 GUI / 웹 SQL 클라이언트. 이 도구들은 `:'var'` 치환을 지원하지 않으므로 실행 직전에 placeholder를 실제 password로 치환하거나, 본 SQL을 `psql`에서만 실행한다.
- 운영 권고: 본 문서에 실제 password를 절대 기록하지 않는다. 실행 시점에만 안전한 채널(`psql -v` + 임시 셸 환경변수, 또는 Secrets Manager에서 일회성 조회 후 `psql` stdin)로 주입하고, 셸 history / 임시 파일에 password가 남지 않도록 주의한다.

#### 4.3.2 SQL

```sql
CREATE ROLE marketconnector_app LOGIN PASSWORD :'pwd_marketconnector' NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
CREATE ROLE view_app             LOGIN PASSWORD :'pwd_view'            NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
CREATE ROLE crawler_app          LOGIN PASSWORD :'pwd_crawler'         NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
CREATE ROLE preprocessor_app     LOGIN PASSWORD :'pwd_preprocessor'    NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
CREATE ROLE decision_app         LOGIN PASSWORD :'pwd_decision'        NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
CREATE ROLE research_app         LOGIN PASSWORD :'pwd_research'        NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
CREATE ROLE execution_app        LOGIN PASSWORD :'pwd_execution'       NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;

-- DB connection 자체 권한
GRANT CONNECT ON DATABASE portfolio TO
  marketconnector_app, view_app, crawler_app, preprocessor_app,
  decision_app, research_app, execution_app;

-- public schema 사용은 호환성 차원에서 모두 허용
GRANT USAGE ON SCHEMA public TO
  marketconnector_app, view_app, crawler_app, preprocessor_app,
  decision_app, research_app, execution_app;
```

`psql` 변수(`:'pwd_*'`) 대신 `\set` 또는 환경변수 + 임시 파일을 사용해도 되고, 운영자 정책에 맞는 방법을 따른다. 본 문서는 password를 적지 않는다.

### 4.4 schema USAGE / 권한 부여 (R / W / S)

읽기 권한이 필요한 schema에 USAGE + SELECT, 쓰기 권한이 필요한 schema에 USAGE + SELECT/INSERT/UPDATE/DELETE + sequence USAGE/SELECT를 부여한다. legacy schema는 미부여(생략).

```sql
-- ============================================================
-- reference (모두 read, crawler만 write)
-- ============================================================
GRANT USAGE ON SCHEMA reference TO
  marketconnector_app, view_app, crawler_app, preprocessor_app,
  decision_app, research_app, execution_app;

GRANT SELECT ON ALL TABLES IN SCHEMA reference TO
  marketconnector_app, view_app, crawler_app, preprocessor_app,
  decision_app, research_app, execution_app;

GRANT INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA reference TO crawler_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA reference TO crawler_app;

-- ============================================================
-- interest (crawler write, preprocessor/research/view read)
-- ============================================================
GRANT USAGE ON SCHEMA interest TO
  view_app, crawler_app, preprocessor_app, research_app;

GRANT SELECT ON ALL TABLES IN SCHEMA interest TO
  view_app, crawler_app, preprocessor_app, research_app;

GRANT INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA interest TO crawler_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA interest TO crawler_app;

-- ============================================================
-- preprocessor (preprocessor write, decision/research/view read)
-- ============================================================
GRANT USAGE ON SCHEMA preprocessor TO
  view_app, preprocessor_app, decision_app, research_app;

GRANT SELECT ON ALL TABLES IN SCHEMA preprocessor TO
  view_app, preprocessor_app, decision_app, research_app;

GRANT INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA preprocessor TO preprocessor_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA preprocessor TO preprocessor_app;

-- ============================================================
-- research (research write, decision/execution/view read)
-- ============================================================
GRANT USAGE ON SCHEMA research TO
  view_app, decision_app, execution_app, research_app;

GRANT SELECT ON ALL TABLES IN SCHEMA research TO
  view_app, decision_app, execution_app, research_app;

GRANT INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA research TO research_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA research TO research_app;

-- ============================================================
-- decision (decision write, execution/view read)
-- ============================================================
GRANT USAGE ON SCHEMA decision TO
  view_app, decision_app, execution_app;

GRANT SELECT ON ALL TABLES IN SCHEMA decision TO
  view_app, decision_app, execution_app;

GRANT INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA decision TO decision_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA decision TO decision_app;

-- ============================================================
-- execution (execution write, marketconnector/view read)
-- ============================================================
GRANT USAGE ON SCHEMA execution TO
  view_app, marketconnector_app, execution_app;

GRANT SELECT ON ALL TABLES IN SCHEMA execution TO
  view_app, marketconnector_app, execution_app;

GRANT INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA execution TO execution_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA execution TO execution_app;

-- ============================================================
-- connector (marketconnector write, decision/execution/view read)
-- ============================================================
GRANT USAGE ON SCHEMA connector TO
  view_app, marketconnector_app, decision_app, execution_app;

GRANT SELECT ON ALL TABLES IN SCHEMA connector TO
  view_app, marketconnector_app, decision_app, execution_app;

GRANT INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA connector TO marketconnector_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA connector TO marketconnector_app;

-- ============================================================
-- ops (view write only)
-- ============================================================
GRANT USAGE ON SCHEMA ops TO view_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA ops TO view_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA ops TO view_app;

-- ============================================================
-- legacy: 기본 미부여 (의도적으로 GRANT 없음)
-- ============================================================
-- 운영자가 별도 결정으로 grant가 필요하다고 판단할 때만 추가.

-- ============================================================
-- public (호환성. marketconnector만 write)
-- ============================================================
GRANT SELECT ON ALL TABLES IN SCHEMA public TO
  marketconnector_app, view_app, crawler_app, preprocessor_app,
  decision_app, research_app, execution_app;

GRANT INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO marketconnector_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO marketconnector_app;
```

### 4.5 DEFAULT PRIVILEGES (이후 생성될 객체에 자동 적용)

`portfolio_owner`가 새 table / sequence를 만들 때마다 위 매트릭스가 자동으로 따라오도록 default privileges를 설정한다.

```sql
-- 모든 schema 권한 부여를 portfolio_owner 명의로 지속하도록
-- 아래 ALTER DEFAULT PRIVILEGES는 "portfolio_owner가 만든 객체"에 적용됨

-- reference
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA reference
  GRANT SELECT ON TABLES TO marketconnector_app, view_app, crawler_app, preprocessor_app,
                            decision_app, research_app, execution_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA reference
  GRANT INSERT, UPDATE, DELETE ON TABLES TO crawler_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA reference
  GRANT USAGE, SELECT ON SEQUENCES TO crawler_app;

-- interest
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA interest
  GRANT SELECT ON TABLES TO view_app, crawler_app, preprocessor_app, research_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA interest
  GRANT INSERT, UPDATE, DELETE ON TABLES TO crawler_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA interest
  GRANT USAGE, SELECT ON SEQUENCES TO crawler_app;

-- preprocessor
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA preprocessor
  GRANT SELECT ON TABLES TO view_app, preprocessor_app, decision_app, research_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA preprocessor
  GRANT INSERT, UPDATE, DELETE ON TABLES TO preprocessor_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA preprocessor
  GRANT USAGE, SELECT ON SEQUENCES TO preprocessor_app;

-- research
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA research
  GRANT SELECT ON TABLES TO view_app, decision_app, execution_app, research_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA research
  GRANT INSERT, UPDATE, DELETE ON TABLES TO research_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA research
  GRANT USAGE, SELECT ON SEQUENCES TO research_app;

-- decision
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA decision
  GRANT SELECT ON TABLES TO view_app, decision_app, execution_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA decision
  GRANT INSERT, UPDATE, DELETE ON TABLES TO decision_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA decision
  GRANT USAGE, SELECT ON SEQUENCES TO decision_app;

-- execution
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA execution
  GRANT SELECT ON TABLES TO view_app, marketconnector_app, execution_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA execution
  GRANT INSERT, UPDATE, DELETE ON TABLES TO execution_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA execution
  GRANT USAGE, SELECT ON SEQUENCES TO execution_app;

-- connector
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA connector
  GRANT SELECT ON TABLES TO view_app, marketconnector_app, decision_app, execution_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA connector
  GRANT INSERT, UPDATE, DELETE ON TABLES TO marketconnector_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA connector
  GRANT USAGE, SELECT ON SEQUENCES TO marketconnector_app;

-- ops
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA ops
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO view_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA ops
  GRANT USAGE, SELECT ON SEQUENCES TO view_app;

-- legacy: default privileges도 부여하지 않는다.

-- public
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA public
  GRANT SELECT ON TABLES TO marketconnector_app, view_app, crawler_app, preprocessor_app,
                            decision_app, research_app, execution_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA public
  GRANT INSERT, UPDATE, DELETE ON TABLES TO marketconnector_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA public
  GRANT USAGE, SELECT ON SEQUENCES TO marketconnector_app;
```

restore 시 일부 객체가 `portfolio_admin`(또는 다른 사용자) 소유로 들어왔다면, 위 default privileges가 적용되지 않는다. 그 경우 `REASSIGN OWNED BY <원소유자> TO portfolio_owner` 또는 객체별 `ALTER TABLE / SEQUENCE ... OWNER TO portfolio_owner` 후 4.4의 명시적 GRANT를 한 번 더 실행한다.

### 4.6 search_path 고정

```sql
ALTER ROLE marketconnector_app SET search_path = connector, execution, legacy, reference, public;
ALTER ROLE crawler_app          SET search_path = interest, reference, legacy, public;
ALTER ROLE preprocessor_app     SET search_path = preprocessor, interest, reference, legacy, public;
ALTER ROLE decision_app         SET search_path = decision, research, preprocessor, execution, connector, reference, legacy, public;
ALTER ROLE research_app         SET search_path = research, preprocessor, interest, reference, legacy, public;
ALTER ROLE execution_app        SET search_path = execution, decision, research, connector, preprocessor, interest, reference, legacy, public;
ALTER ROLE view_app             SET search_path = ops, execution, decision, research, connector, preprocessor, interest, reference, legacy, public;
```

## 5. 검증 SQL

### 5.1 role / 속성 확인

```sql
-- 7개 app role + portfolio_owner 존재 확인
SELECT rolname, rolcanlogin, rolsuper, rolcreatedb, rolcreaterole, rolreplication, rolbypassrls
FROM pg_roles
WHERE rolname IN ('portfolio_owner','portfolio_admin',
                  'marketconnector_app','view_app','crawler_app','preprocessor_app',
                  'decision_app','research_app','execution_app')
ORDER BY rolname;

-- search_path 확인
SELECT r.rolname, s.setconfig
FROM pg_roles r
LEFT JOIN pg_db_role_setting s ON s.setrole = r.oid
WHERE r.rolname IN ('marketconnector_app','view_app','crawler_app','preprocessor_app',
                    'decision_app','research_app','execution_app')
ORDER BY r.rolname;
```

### 5.2 schema 권한 매트릭스 확인

```sql
-- USAGE 권한 (schema 단위)
SELECT n.nspname AS schema, r.rolname AS role,
       has_schema_privilege(r.rolname, n.nspname, 'USAGE')  AS has_usage,
       has_schema_privilege(r.rolname, n.nspname, 'CREATE') AS has_create
FROM pg_namespace n
CROSS JOIN pg_roles r
WHERE n.nspname IN ('reference','interest','preprocessor','research','decision',
                    'execution','connector','ops','legacy','public')
  AND r.rolname IN ('marketconnector_app','view_app','crawler_app','preprocessor_app',
                    'decision_app','research_app','execution_app')
ORDER BY n.nspname, r.rolname;

-- 모든 app role에 대해 legacy USAGE = false 인지 확인
SELECT r.rolname, has_schema_privilege(r.rolname, 'legacy', 'USAGE') AS has_legacy_usage
FROM pg_roles r
WHERE r.rolname LIKE '%\_app' ESCAPE '\'
ORDER BY r.rolname;
```

### 5.3 table / sequence 권한 sample 확인

```sql
-- table 권한 요약 (schema, role, privilege 기준)
SELECT table_schema, grantee, privilege_type, count(*) AS table_count
FROM information_schema.role_table_grants
WHERE table_schema IN ('reference','interest','preprocessor','research','decision',
                       'execution','connector','ops','legacy','public')
  AND grantee IN ('marketconnector_app','view_app','crawler_app','preprocessor_app',
                  'decision_app','research_app','execution_app')
GROUP BY table_schema, grantee, privilege_type
ORDER BY table_schema, grantee, privilege_type;

-- sequence 권한 요약
SELECT sequence_schema, grantee, privilege_type, count(*) AS seq_count
FROM information_schema.role_usage_grants  -- usage on sequences
WHERE object_type = 'SEQUENCE'
  AND sequence_schema IN ('reference','interest','preprocessor','research','decision',
                          'execution','connector','ops','public')
  AND grantee IN ('marketconnector_app','view_app','crawler_app','preprocessor_app',
                  'decision_app','research_app','execution_app')
GROUP BY sequence_schema, grantee, privilege_type
ORDER BY sequence_schema, grantee;
```

비고: `role_usage_grants`가 sequence 외 다른 object를 함께 반환할 수 있으므로 `object_type = 'SEQUENCE'`로 필터한다. PostgreSQL 버전 차이로 컬럼명이 다르면 `pg_class` + `aclcontains`로 대체한다.

### 5.4 실제 connection / 동작 점검 (각 app role로 접속 후 실행)

```sql
-- 공통: 자기 권한 화면 확인
SELECT current_user, current_database();
SHOW search_path;

-- legacy 접근이 막혀 있는지 확인 (모든 app role)
-- 1) schema USAGE 권한 자체 점검: 기대값 has_legacy_usage = false
SELECT current_user,
       has_schema_privilege(current_user, 'legacy', 'USAGE') AS has_legacy_usage;

-- 2) 실제 legacy schema 접근 차단 검증
--    아래 SELECT는 permission denied for schema legacy 또는 동등한 권한 오류가 발생해야 정상이다.
--    <실제_legacy_테이블명>은 운영자가 legacy schema 안에 존재하는 임의 테이블 1개로 치환한다.
SELECT count(*) FROM legacy.<실제_legacy_테이블명>;

-- 자기 책임 schema의 핵심 테이블 read 확인
-- (예: marketconnector_app)
SELECT count(*) FROM connector.connector_order_request;
SELECT count(*) FROM execution.strategy_execution_order; -- read만 가능해야 함
-- write 시도는 다른 role과 격리되었는지 확인
-- INSERT INTO execution.strategy_execution_order ... → permission denied 기대.

-- (예: decision_app)
SELECT count(*) FROM preprocessor.pre_total_market_daily_feature;
SELECT count(*) FROM decision.strategy_daily_signal;
-- INSERT INTO decision.strategy_daily_signal (...) → 정상 기대.

-- (예: view_app)
SELECT count(*) FROM ops.<운영자 결정 ops 테이블>; -- read/write 기대
SELECT count(*) FROM execution.strategy_execution_order; -- read only 기대
```

### 5.5 운영 점검 (주기 권고)

```sql
-- 평문 password 평가는 SQL로 불가. Secrets Manager rotation 시점 점검은 06 spec 범위.
-- 단, login 가능 여부 자체는 connection log / pg_stat_activity로 확인 가능.

-- 최근 실패 connection (RDS log on이면 CloudWatch Logs로 확인. SQL 차원에서는 제한적.)
SELECT usename, count(*)
FROM pg_stat_activity
WHERE state IS NOT NULL
GROUP BY usename
ORDER BY 2 DESC;
```

## 6. Rollback / 권한 회수 절차

본 권한 정책을 롤백할 필요가 생기면 아래 순서를 따른다. `portfolio_admin`(RDS master) 또는 `portfolio_owner`로 접속해 실행한다.

### 6.1 신중한 회수 (특정 role / 특정 schema만)

```sql
-- 예: marketconnector_app의 execution read 권한만 회수
REVOKE SELECT ON ALL TABLES IN SCHEMA execution FROM marketconnector_app;
REVOKE USAGE ON SCHEMA execution FROM marketconnector_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA execution
  REVOKE SELECT ON TABLES FROM marketconnector_app;
```

### 6.2 단일 role 전체 비활성화 (긴급)

```sql
-- 1) 즉시 추가 connection 차단
ALTER ROLE <target_role> NOLOGIN;

-- 2) 활성 session 종료 (선택)
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE usename = '<target_role>';

-- 3) 권한 일괄 회수
REVOKE ALL PRIVILEGES ON DATABASE portfolio FROM <target_role>;
REVOKE ALL PRIVILEGES ON ALL TABLES    IN SCHEMA reference, interest, preprocessor, research, decision, execution, connector, ops, public FROM <target_role>;
REVOKE ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA reference, interest, preprocessor, research, decision, execution, connector, ops, public FROM <target_role>;
REVOKE USAGE ON SCHEMA reference, interest, preprocessor, research, decision, execution, connector, ops, public FROM <target_role>;

-- 4) default privileges 회수
-- 각 schema에 대해 ALTER DEFAULT PRIVILEGES ... REVOKE 반복
```

### 6.3 전체 롤백 (App role 7종 제거)

```sql
-- 0) 사전 점검: 각 role 명의로 소유한 객체 존재 여부
SELECT n.nspname, c.relname, c.relkind, r.rolname AS owner
FROM pg_class c
JOIN pg_namespace n ON n.oid = c.relnamespace
JOIN pg_roles r ON r.oid = c.relowner
WHERE r.rolname IN ('marketconnector_app','view_app','crawler_app','preprocessor_app',
                    'decision_app','research_app','execution_app')
ORDER BY 4, 1, 2;
-- 결과 0건이 정상. 1건 이상이면 REASSIGN 또는 ALTER OWNER 필요.

-- 1) 객체 소유권 / 잔여 권한 정리
REASSIGN OWNED BY marketconnector_app, view_app, crawler_app, preprocessor_app,
                  decision_app, research_app, execution_app
              TO portfolio_owner;
DROP OWNED BY marketconnector_app, view_app, crawler_app, preprocessor_app,
              decision_app, research_app, execution_app;

-- 2) role 제거
DROP ROLE IF EXISTS marketconnector_app;
DROP ROLE IF EXISTS view_app;
DROP ROLE IF EXISTS crawler_app;
DROP ROLE IF EXISTS preprocessor_app;
DROP ROLE IF EXISTS decision_app;
DROP ROLE IF EXISTS research_app;
DROP ROLE IF EXISTS execution_app;
```

### 6.4 schema owner 원복 (선택)

`portfolio_owner` 도입 자체를 되돌리고 싶다면:

```sql
-- schema owner 환원 (예: portfolio_admin으로)
ALTER SCHEMA reference    OWNER TO portfolio_admin;
-- ... (9개 schema 반복)

-- portfolio_owner 권한 회수 후 제거
REVOKE portfolio_owner FROM portfolio_admin;
DROP ROLE IF EXISTS portfolio_owner;
```

### 6.5 운영 안전장치

- 회수 직전에 manual snapshot을 하나 찍는다(02 spec 결정상 PITR도 활성이라 이중 안전망 확보).
- application의 connection 문자열을 임시로 `portfolio_admin` 또는 read-only 백업 role로 되돌릴 수 있는 환경변수 토글을 미리 준비한다(`06-secrets-and-iam` 진행 시점에 정식화).
- connection 갯수가 많은 서비스(view 등)는 회수 전에 ECS Service desired count = 0 또는 maintenance 모드로 트래픽을 잠근다.

## 7. operation-notes / validation-checklist 반영용 문구

본 SQL은 운영자가 실행한 뒤 결과를 문서에 반영한다. 본 spec에서는 자동 갱신하지 않는다(작업 완료 후 자동 갱신 파일 최대 3개 정책). SQL 실행이 끝나면 운영자가 아래 문구를 운영 노트와 체크리스트에 직접 추가한다.

### 7.1 `operation-notes.md` 추가 섹션 (실행 후 그대로 복사)

```markdown
## 2026-06-09 DB Role / 권한 분리 1차 적용

- 본 spec [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §4 SQL 적용 결과 기록.
- 적용 환경: aws-paper, RDS PostgreSQL, DB `portfolio`, master = `portfolio_admin`.
- 신규 owner role: `portfolio_owner` 생성 + 9개 도메인 schema(reference / interest / preprocessor / research / decision / execution / connector / ops / legacy) 소유권 이관.
- 신규 app role 7종 생성: `marketconnector_app`, `view_app`, `crawler_app`, `preprocessor_app`, `decision_app`, `research_app`, `execution_app` (모두 LOGIN, 비SUPERUSER, 비CREATEDB, 비CREATEROLE, 비REPLICATION, 비BYPASSRLS).
- 권한 매트릭스 적용 완료. 핵심 변경: legacy schema는 모든 app role에 미부여, marketconnector_app의 execution은 R only로 축소.
- DEFAULT PRIVILEGES를 portfolio_owner 명의로 9개 schema에 설정. 이후 새 객체 생성 시 매트릭스 자동 적용.
- search_path는 `ALTER ROLE ... SET search_path` 7건으로 role 단위 고정. 각 MS README 정의와 일치.
- secret value 기록 없음. 모든 password / endpoint / account-id는 `[REDACTED]` 또는 placeholder만 사용.
- 본 작업은 DB 내부 SQL만 실행. AWS 리소스 생성 / 변경 / 삭제 없음. 8개 MS 코드 / README / AGENTS.md / CHANGELOG / docs / worklog 수정 없음.
- 후속: 06 spec 단계에서 7개 app role 비밀번호를 Secrets Manager / SSM SecureString에 분리 저장.
```

위 문구를 SQL 실행 결과에 맞게 짧게 보정한다. 실패 / 부분 적용이 있으면 사실대로 기록한다.

### 7.2 `validation-checklist.md` 추가 항목 (실행 후 §6 또는 §11 신규 섹션에 추가)

권고: 기존 `## 6. DB / schema / role 준비 Validation` 섹션의 `[운영자 확인 필요]` 항목을 SQL 실행 결과에 맞춰 `[O]`로 격상한다. 추가로 권한 검증 결과 항목을 5건 추가한다.

```markdown
## 11. DB Role 권한 매트릭스 Validation

- <span style="color:red">[O]</span> 7개 app role + `portfolio_owner` 모두 존재 확인 (§5.1 first query 결과 7+1행 일치)
- <span style="color:red">[O]</span> 모든 app role의 `rolsuper / rolcreatedb / rolcreaterole / rolreplication / rolbypassrls` = false 확인
- <span style="color:red">[O]</span> 모든 app role의 `legacy` schema USAGE = false 확인 (§5.2 두 번째 query에서 7행 모두 false)
- <span style="color:red">[O]</span> §5.2 schema USAGE / CREATE 매트릭스가 [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §2 표와 일치 (불일치 0건)
- <span style="color:red">[O]</span> §5.3 table 권한 요약과 sequence 권한 요약이 매트릭스와 일치 (불일치 0건)
- <span style="color:red">[O]</span> 각 app role 명의로 접속 후 자기 책임 schema write 정상 / 다른 schema write permission denied 정상 (§5.4)
- <span style="color:black">[운영자 확인 필요]</span> 7개 app role의 비밀번호가 Secrets Manager / SSM SecureString placeholder 자리에 등록되었는지(`06-secrets-and-iam` 단계에서 정식 기록)
```

라벨 / 색상 표기는 기존 `validation-checklist.md` 4종 라벨 규칙을 그대로 따른다. SQL 결과에 따라 `[O]` 또는 `[X]`로 적는다.

## 8. 후속 갱신 후보 (2026-06-17 17-step E2E 보강)

본 섹션은 2026-06-17 Daily AWS 17-step E2E 흐름 중 운영자가 발견 / 보정한 DB role 권한 / search_path 사실을 후속 갱신 후보로 누적한다.

운영자 직접 GRANT 보정 결과는 아래에 사실 기록되어 있고, 본 spec 본문 §4 GRANT / §5 검증 SQL 의 정식 매트릭스 갱신은 후속 phase 책임이다.

- [`./operation-notes.md`](./operation-notes.md) 2026-06-17 §1 ~ §4
- [`../_common/risk-register.md`](../_common/risk-register.md) R-DATA-005 [2026-06-17 보강] / R-DATA-011 신규 정합

### 8.1. `execution_app` 의 `interest` schema 권한 정식 갱신 후보

- §4.4 GRANT 매트릭스의 `execution_app` 행에 `interest` schema USAGE / `interest.*` table SELECT / sequence USAGE+SELECT / future default privileges(`ALTER DEFAULT PRIVILEGES IN SCHEMA interest GRANT SELECT ON TABLES TO execution_app` 등) 추가 후보.
- §5 검증 SQL 의 `execution_app` 행에 `has_schema_privilege('execution_app', 'interest', 'USAGE')` / `has_table_privilege('execution_app', 'interest.<핵심 table 이름>', 'SELECT')` 점검 SQL 추가 후보.
- 본 일자 1차 실패 사실은 [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-17 §3 정합 / R-DATA-005 [2026-06-17 보강] 정합.

### 8.2. `marketconnector_app` 의 `legacy` schema · `legacy.holdings` · search_path 정식 갱신 후보

- §4.4 GRANT 매트릭스의 `marketconnector_app` 행에 `legacy` schema USAGE / `legacy.holdings` DML(SELECT / INSERT / UPDATE / DELETE) / sequence USAGE+SELECT+UPDATE / future default privileges 추가 후보.
- §3 search_path 전략의 `marketconnector_app` 행에 database search_path = `connector, execution, legacy, reference, public` 추가 후보(`ALTER ROLE marketconnector_app IN DATABASE portfolio SET search_path = connector, execution, legacy, reference, public`).
- §5 검증 SQL 의 `marketconnector_app` 행에 `current_setting('search_path')` / `has_schema_privilege('marketconnector_app', 'legacy', 'USAGE')` / `has_table_privilege('marketconnector_app', 'legacy.holdings', 'SELECT,INSERT,UPDATE,DELETE')` 점검 SQL 추가 후보.
- bare table name 의존 legacy 경로 검증 SQL 후보(`SET ROLE marketconnector_app; SELECT 1 FROM holdings LIMIT 1;`) 추가 후보.
- OD-DB-007(legacy schema 모든 app role 미부여) 정책의 marketconnector_app 한정 1건 예외 사실은 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) Change Log 2026-06-17 두 번째 항목 / R-DATA-011 신규 정합. 본 spec 본문 결정값 변경은 후속 분리.

### 8.3. `marketconnector_app` 의 execution table UPDATE 권한 (OD-DB-008 R-only 정책 후속 재검토)

- 본 일자 Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` 1차 실패의 한 원인은 `marketconnector_app` 의 execution table UPDATE 권한 누락(OD-DB-008 R-only 정책 정합). 운영자 직접 GRANT 로 SUBMITTED 전환 가능하도록 1차 보정 — 03 spec operation-notes 2026-06-17 §2 정합.
- OD-DB-008 R-only 정책의 후속 재검토 후보 — MarketConnector executor 가 `strategy_execution_order` 를 SUBMITTED / FAILED 로 갱신해야 하는 책임을 가지므로 execution-table 의 일부 UPDATE 권한이 marketconnector_app 에 필요한지 [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-DB-008 후속 분리.

### 8.4. 후속 갱신 일정

- 본 일자에 운영자가 직접 수행한 GRANT / search_path 보정 결과는 사실 기록만 — 본 spec 본문 §4 / §5 / §3 의 정식 매트릭스 갱신은 후속 phase 책임.
- 정식 매트릭스 갱신 시점에 본 §8 항목들을 §4 / §5 / §3 본문으로 통합한다.
- 본 §8 자체는 갱신 후보 누적 섹션이며 본문 결정값 변경 0건.

## 본 문서 작업 안전 제약

- 본 문서는 SQL 초안 / 검증 / rollback / 반영 문구 산출물이며, 실제 SQL 실행은 운영자가 진행한다.
- 본 작업은 8개 MS의 README / AGENTS.md / CHANGELOG / docs / worklog 수정 범위에 포함되지 않는다.
- 본 작업으로 AWS 리소스를 생성 / 변경 / 삭제하지 않는다.
- 본 문서에는 실제 password / secret value / endpoint / account-id / 계좌번호 / token을 적지 않는다. 모두 `[REDACTED]` 또는 placeholder만 사용한다.
- 결정값(legacy 미부여, marketconnector_app execution R only 축소)이 02 spec design.md 매트릭스와 다른 부분은 `../_common/operator-decisions.md`의 OD-DB 카테고리에서 후속 갱신이 필요할 수 있다(현 결정 변경 사항으로 등록).
