# DB Role / Permission Separation — 02-aws-network-and-rds auxiliary document

This document is the DB role / permission separation design and applied SQL draft at the point where the restore into the `portfolio` DB (`aws-paper`, RDS PostgreSQL) is complete. It expands the "DB Role Permission Matrix" section of this spec's design.md into an executable form and reflects the user's new decision (`the legacy schema does not grant permissions to app roles by default`).

This document does not write the actual password / secret value / RDS endpoint hostname / account number / token. All use only `[REDACTED]` or a placeholder.

Target environment

- DB name: `portfolio`
- Main schemas: `reference`, `interest`, `preprocessor`, `research`, `decision`, `execution`, `connector`, `ops`, `legacy`, `public`
- Operating assumption: the admin / restore account is for operational management only. It is not used in the application connection string.

The 7 target MS and their mapped roles

| MS | DB role |
|---|---|
| `port-marketconnector` | `marketconnector_app` |
| `port-view` | `view_app` |
| `port-interest-crawler` | `crawler_app` |
| `port-interest-preprocessor` | `preprocessor_app` |
| `port_strategy_decision` | `decision_app` |
| `port_strategy_research` | `research_app` |
| `port_strategy_execution` | `execution_app` |

`port_strategy_common` is a library, so it does not have its own role.

## 1. Per-MS DB Role Design

Three kinds of roles are separated.

| Role kind | Name | Responsibility | Note |
|---|---|---|---|
| Owner / DDL | `portfolio_owner` | Owns the DB / schema, DDL, permission definition, default privileges setup. | Prohibited for daily operation / application connection. Used only at SQL migration time. |
| Admin / Restore | `portfolio_admin` | RDS master user. backup / restore / parameter / extension management. | The master user of this RDS (currently `portfolio_admin`). app connection prohibited. Delegate SQL migration to `portfolio_owner` where possible. |
| App | 7 `*_app` | Dedicated per MS application. Least privilege. | See the §2 matrix below. |

Principles

- Grant only `LOGIN` to app roles. Do not grant `CREATEDB`, `CREATEROLE`, `SUPERUSER`, `BYPASSRLS`, `REPLICATION`.
- App roles do not own their responsible schema. The schema owner is unified as `portfolio_owner`, and app roles are granted only USAGE + the necessary DML permissions.
- The `legacy` schema does not grant USAGE / SELECT to any app role by default. If an MS that truly needs legacy data access is identified, it is granted then by a separate decision.
- The single App role password is stored in Secrets Manager / SSM SecureString. This document and the operator notes record only `[REDACTED]`.

## 2. Permission Matrix

Table symbols

- `R` = SELECT
- `W` = INSERT, UPDATE, DELETE
- `S` = sequence USAGE + SELECT (nextval / currval / lastval usable)
- `-` = neither USAGE / SELECT granted (schema not granted either)

| schema | marketconnector_app | crawler_app | preprocessor_app | decision_app | execution_app | research_app | view_app |
|---|---|---|---|---|---|---|---|
| reference | R | R/W (+S) | R | R | R | R | R |
| interest | - | R/W (+S) | R | - | - | R | R |
| preprocessor | - | - | R/W (+S) | R | - | R | R |
| research | - | - | - | R | R | R/W (+S) | R |
| decision | - | - | - | R/W (+S) | R | - | R |
| execution | R | - | - | - | R/W (+S) | - | R (write re-reviewed in `05-port-view-ecs-and-runbook`) |
| connector | R/W (+S) | - | - | R | R | - | R |
| ops | - | - | - | - | - | - | R/W (+S) |
| legacy | - | - | - | - | - | - | - (not granted by default) |
| public | R/W (+S) | R | R | R | R | R | R |

Notes

- A schema where the app role is `R/W` is automatically also granted `S` (sequence USAGE + SELECT). This is because a `nextval()` call is needed when inserting a new row. A read-only schema is not granted sequence permission.
- Differences from this spec's 02 design.md matrix:
  - Removed the `legacy` permission of all app roles (user decision).
  - Reduced the `execution` permission of `marketconnector_app` from R/W → R (read only). `execution` write is performed only by `execution_app`.
- The `view_app` write scope is currently only `ops`. Whether `execution` is written directly from view is re-reviewed in `05-port-view-ecs-and-runbook`.
- For `public`, all 7 roles have USAGE for compatibility, but write is granted only to `marketconnector_app`. New data is moved to a domain schema where possible.

## 3. search_path Strategy

Keep the existing search_path defined in each MS README, and enforce it on the PostgreSQL side with a role-level `ALTER ROLE ... SET search_path`. This makes the same search_path apply even if the application connection does not set it via `connection-init-sql`.

| Role | search_path | Source |
|---|---|---|
| `marketconnector_app` | `connector, execution, legacy, reference, public` | port-marketconnector README |
| `crawler_app` | `interest, reference, legacy, public` | port-interest-crawler README |
| `preprocessor_app` | `preprocessor, interest, reference, legacy, public` | port-interest-preprocessor README |
| `decision_app` | `decision, research, preprocessor, execution, connector, reference, legacy, public` | port_strategy_decision README |
| `research_app` | `research, preprocessor, interest, reference, legacy, public` | port_strategy_research README |
| `execution_app` | `execution, decision, research, connector, preprocessor, interest, reference, legacy, public` | port_strategy_execution README |
| `view_app` | `ops, execution, decision, research, connector, preprocessor, interest, reference, legacy, public` | port-view README |

Although `legacy` is in the search_path, since it has no USAGE permission per this decision, objects of that schema are denied access due to insufficient permission even if they are caught as resolution candidates. That is, the `legacy` entry is kept only for README compatibility, and an explicit grant decision is needed for actual use.

`portfolio_owner` and `portfolio_admin` do not enforce a separate search_path (SET per session if needed).

## 4. SQL Draft (GRANT / ALTER ROLE / DEFAULT PRIVILEGES)

This SQL is executed while connected to the `portfolio` DB as `portfolio_admin` (RDS master) or a superuser-equivalent role with equivalent permission. All password positions are placeholders.

### 4.1 Preliminary check

```sql
-- Confirm connection
SELECT current_database(), current_user, version();

-- Confirm the 9 domain schemas exist (if absent, CREATE and proceed)
SELECT n.nspname
FROM pg_namespace n
WHERE n.nspname IN ('reference','interest','preprocessor','research','decision',
                    'execution','connector','ops','legacy','public')
ORDER BY 1;
```

### 4.2 Organize the owner role and schema ownership

`portfolio_admin` first takes `portfolio_owner` membership, then transfers ownership of the 9 domain schemas to `portfolio_owner`. In RDS PostgreSQL, the account executing `ALTER SCHEMA ... OWNER TO` may need to be a member of the new owner role, so the order of granting membership before changing the schema owner is safe.

```sql
-- 1) Create portfolio_owner (NOLOGIN recommended. Delegate with SET ROLE during SQL execution)
CREATE ROLE portfolio_owner NOLOGIN;

-- 2) Grant portfolio_owner membership to the master user (portfolio_admin)
--    => can then execute ALTER SCHEMA ... OWNER TO portfolio_owner
GRANT portfolio_owner TO portfolio_admin;

-- 3) Unify ownership of the 9 domain schemas to portfolio_owner (including legacy)
ALTER SCHEMA reference    OWNER TO portfolio_owner;
ALTER SCHEMA interest     OWNER TO portfolio_owner;
ALTER SCHEMA preprocessor OWNER TO portfolio_owner;
ALTER SCHEMA research     OWNER TO portfolio_owner;
ALTER SCHEMA decision     OWNER TO portfolio_owner;
ALTER SCHEMA execution    OWNER TO portfolio_owner;
ALTER SCHEMA connector    OWNER TO portfolio_owner;
ALTER SCHEMA ops          OWNER TO portfolio_owner;
ALTER SCHEMA legacy       OWNER TO portfolio_owner;
-- public is usually left as-is per RDS policy. If needed:
-- ALTER SCHEMA public OWNER TO portfolio_owner;
```

#### 4.2.1 Confirm the existing table / sequence owner status

Even if you change only the schema owner to `portfolio_owner`, the owner of existing tables / sequences / views / indexes / matviews may remain the restore-executing account (e.g., `portfolio_admin`). If the existing object owner is `portfolio_admin`, the §4.5 `ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner` does not apply to those objects, so confirm the current owner distribution first.

```sql
-- Owner distribution of all relations in the 9 domain schemas + public
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

If there are rows where the owner is `portfolio_admin` (or another account), transfer the owner to `portfolio_owner` in bulk or per object with one of the following, just before applying the §4.5 default privileges. In this spec, the SQL is not actually executed and only the command form is presented.

```sql
-- Option A: bulk transfer (all objects owned by that account)
-- Caution: RDS operational objects under portfolio_admin's name may exist beyond the domain schemas,
--          so always review the owner status result above before executing.
REASSIGN OWNED BY portfolio_admin TO portfolio_owner;

-- Option B: per-object transfer by schema (table / sequence / view / matview)
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

-- view / matview use the same pattern (relkind = 'v', 'm') with ALTER VIEW / ALTER MATERIALIZED VIEW
```

### 4.3 Create the 7 App roles

Record only placeholders for the passwords. When the operator actually executes, inject them from environment variables or Secrets Manager.

#### 4.3.1 Caution on the password placeholder format

The `:'pwd_*'` notation in the SQL below is the `psql` client's variable substitution syntax. Pasting it as-is into another SQL client may fail.

- Usable: `psql` on a local / operator PC (e.g., `psql -v pwd_view='<APP_PASSWORD_PLACEHOLDER>' -f roles.sql`).
- Not usable / needs conversion: AWS RDS Query Editor v2, pgAdmin Query Tool, DBeaver Run SQL, JDBC-based SQL Workbench, and most GUI / web SQL clients. These tools do not support `:'var'` substitution, so substitute the placeholder with the actual password just before execution, or run this SQL only in `psql`.
- Operational recommendation: never record the actual password in this document. Inject it only at execution time via a safe channel (`psql -v` + a temporary shell environment variable, or a one-time lookup from Secrets Manager then `psql` stdin), and be careful that the password does not remain in the shell history / temporary files.

#### 4.3.2 SQL

```sql
CREATE ROLE marketconnector_app LOGIN PASSWORD :'pwd_marketconnector' NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
CREATE ROLE view_app             LOGIN PASSWORD :'pwd_view'            NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
CREATE ROLE crawler_app          LOGIN PASSWORD :'pwd_crawler'         NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
CREATE ROLE preprocessor_app     LOGIN PASSWORD :'pwd_preprocessor'    NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
CREATE ROLE decision_app         LOGIN PASSWORD :'pwd_decision'        NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
CREATE ROLE research_app         LOGIN PASSWORD :'pwd_research'        NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
CREATE ROLE execution_app        LOGIN PASSWORD :'pwd_execution'       NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;

-- DB connection permission itself
GRANT CONNECT ON DATABASE portfolio TO
  marketconnector_app, view_app, crawler_app, preprocessor_app,
  decision_app, research_app, execution_app;

-- Allow all to use the public schema for compatibility
GRANT USAGE ON SCHEMA public TO
  marketconnector_app, view_app, crawler_app, preprocessor_app,
  decision_app, research_app, execution_app;
```

Instead of `psql` variables (`:'pwd_*'`), you may use `\set` or an environment variable + a temporary file; follow the method that fits the operator policy. This document does not write passwords.

### 4.4 Grant schema USAGE / permissions (R / W / S)

Grant USAGE + SELECT to schemas needing read permission, and USAGE + SELECT/INSERT/UPDATE/DELETE + sequence USAGE/SELECT to schemas needing write permission. The legacy schema is not granted (omitted).

```sql
-- ============================================================
-- reference (all read, only crawler write)
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
-- legacy: not granted by default (intentionally no GRANT)
-- ============================================================
-- Add only when the operator judges a grant is needed by a separate decision.

-- ============================================================
-- public (compatibility. only marketconnector write)
-- ============================================================
GRANT SELECT ON ALL TABLES IN SCHEMA public TO
  marketconnector_app, view_app, crawler_app, preprocessor_app,
  decision_app, research_app, execution_app;

GRANT INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO marketconnector_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO marketconnector_app;
```

### 4.5 DEFAULT PRIVILEGES (automatically applied to objects created later)

Set default privileges so the matrix above automatically follows whenever `portfolio_owner` creates a new table / sequence.

```sql
-- To keep granting all schema permissions under portfolio_owner's name,
-- the ALTER DEFAULT PRIVILEGES below apply to "objects created by portfolio_owner"

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

-- legacy: default privileges are not granted either.

-- public
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA public
  GRANT SELECT ON TABLES TO marketconnector_app, view_app, crawler_app, preprocessor_app,
                            decision_app, research_app, execution_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA public
  GRANT INSERT, UPDATE, DELETE ON TABLES TO marketconnector_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA public
  GRANT USAGE, SELECT ON SEQUENCES TO marketconnector_app;
```

If, at restore time, some objects came in owned by `portfolio_admin` (or another user), the default privileges above do not apply. In that case, run `REASSIGN OWNED BY <original owner> TO portfolio_owner` or a per-object `ALTER TABLE / SEQUENCE ... OWNER TO portfolio_owner`, then run the explicit GRANT of 4.4 once more.

### 4.6 Fix the search_path

```sql
ALTER ROLE marketconnector_app SET search_path = connector, execution, legacy, reference, public;
ALTER ROLE crawler_app          SET search_path = interest, reference, legacy, public;
ALTER ROLE preprocessor_app     SET search_path = preprocessor, interest, reference, legacy, public;
ALTER ROLE decision_app         SET search_path = decision, research, preprocessor, execution, connector, reference, legacy, public;
ALTER ROLE research_app         SET search_path = research, preprocessor, interest, reference, legacy, public;
ALTER ROLE execution_app        SET search_path = execution, decision, research, connector, preprocessor, interest, reference, legacy, public;
ALTER ROLE view_app             SET search_path = ops, execution, decision, research, connector, preprocessor, interest, reference, legacy, public;
```

## 5. Validation SQL

### 5.1 Confirm role / attributes

```sql
-- Confirm the 7 app roles + portfolio_owner exist
SELECT rolname, rolcanlogin, rolsuper, rolcreatedb, rolcreaterole, rolreplication, rolbypassrls
FROM pg_roles
WHERE rolname IN ('portfolio_owner','portfolio_admin',
                  'marketconnector_app','view_app','crawler_app','preprocessor_app',
                  'decision_app','research_app','execution_app')
ORDER BY rolname;

-- Confirm search_path
SELECT r.rolname, s.setconfig
FROM pg_roles r
LEFT JOIN pg_db_role_setting s ON s.setrole = r.oid
WHERE r.rolname IN ('marketconnector_app','view_app','crawler_app','preprocessor_app',
                    'decision_app','research_app','execution_app')
ORDER BY r.rolname;
```

### 5.2 Confirm the schema permission matrix

```sql
-- USAGE permission (schema level)
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

-- Confirm legacy USAGE = false for all app roles
SELECT r.rolname, has_schema_privilege(r.rolname, 'legacy', 'USAGE') AS has_legacy_usage
FROM pg_roles r
WHERE r.rolname LIKE '%\_app' ESCAPE '\'
ORDER BY r.rolname;
```

### 5.3 Confirm table / sequence permission samples

```sql
-- Table permission summary (by schema, role, privilege)
SELECT table_schema, grantee, privilege_type, count(*) AS table_count
FROM information_schema.role_table_grants
WHERE table_schema IN ('reference','interest','preprocessor','research','decision',
                       'execution','connector','ops','legacy','public')
  AND grantee IN ('marketconnector_app','view_app','crawler_app','preprocessor_app',
                  'decision_app','research_app','execution_app')
GROUP BY table_schema, grantee, privilege_type
ORDER BY table_schema, grantee, privilege_type;

-- Sequence permission summary
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

Note: since `role_usage_grants` may also return objects other than sequences, filter with `object_type = 'SEQUENCE'`. If the column name differs by PostgreSQL version, substitute with `pg_class` + `aclcontains`.

### 5.4 Check actual connection / behavior (run after connecting as each app role)

```sql
-- Common: confirm your own permission view
SELECT current_user, current_database();
SHOW search_path;

-- Confirm legacy access is blocked (all app roles)
-- 1) Check the schema USAGE permission itself: expected has_legacy_usage = false
SELECT current_user,
       has_schema_privilege(current_user, 'legacy', 'USAGE') AS has_legacy_usage;

-- 2) Validate actual legacy schema access blocking
--    The SELECT below should raise permission denied for schema legacy or an equivalent permission error to be normal.
--    Replace <actual_legacy_table_name> with any one table that exists in the legacy schema.
SELECT count(*) FROM legacy.<actual_legacy_table_name>;

-- Confirm read of the key table of your responsible schema
-- (e.g., marketconnector_app)
SELECT count(*) FROM connector.connector_order_request;
SELECT count(*) FROM execution.strategy_execution_order; -- should be read only
-- Confirm a write attempt is isolated from other roles
-- INSERT INTO execution.strategy_execution_order ... → permission denied expected.

-- (e.g., decision_app)
SELECT count(*) FROM preprocessor.pre_total_market_daily_feature;
SELECT count(*) FROM decision.strategy_daily_signal;
-- INSERT INTO decision.strategy_daily_signal (...) → normal expected.

-- (e.g., view_app)
SELECT count(*) FROM ops.<operator-decided ops table>; -- read/write expected
SELECT count(*) FROM execution.strategy_execution_order; -- read only expected
```

### 5.5 Operational check (periodic recommendation)

```sql
-- Plaintext password evaluation is not possible via SQL. The Secrets Manager rotation-time check is in the 06 spec scope.
-- However, whether login is possible itself can be confirmed via the connection log / pg_stat_activity.

-- Recent failed connections (if RDS log is on, confirm via CloudWatch Logs. Limited at the SQL level.)
SELECT usename, count(*)
FROM pg_stat_activity
WHERE state IS NOT NULL
GROUP BY usename
ORDER BY 2 DESC;
```

## 6. Rollback / Permission Revocation Procedure

If a need arises to roll back this permission policy, follow the order below. Execute while connected as `portfolio_admin` (RDS master) or `portfolio_owner`.

### 6.1 Careful revocation (only a specific role / specific schema)

```sql
-- Example: revoke only the execution read permission of marketconnector_app
REVOKE SELECT ON ALL TABLES IN SCHEMA execution FROM marketconnector_app;
REVOKE USAGE ON SCHEMA execution FROM marketconnector_app;
ALTER DEFAULT PRIVILEGES FOR ROLE portfolio_owner IN SCHEMA execution
  REVOKE SELECT ON TABLES FROM marketconnector_app;
```

### 6.2 Fully disable a single role (emergency)

```sql
-- 1) Immediately block additional connections
ALTER ROLE <target_role> NOLOGIN;

-- 2) Terminate active sessions (optional)
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE usename = '<target_role>';

-- 3) Bulk revoke permissions
REVOKE ALL PRIVILEGES ON DATABASE portfolio FROM <target_role>;
REVOKE ALL PRIVILEGES ON ALL TABLES    IN SCHEMA reference, interest, preprocessor, research, decision, execution, connector, ops, public FROM <target_role>;
REVOKE ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA reference, interest, preprocessor, research, decision, execution, connector, ops, public FROM <target_role>;
REVOKE USAGE ON SCHEMA reference, interest, preprocessor, research, decision, execution, connector, ops, public FROM <target_role>;

-- 4) Revoke default privileges
-- Repeat ALTER DEFAULT PRIVILEGES ... REVOKE for each schema
```

### 6.3 Full rollback (remove the 7 App roles)

```sql
-- 0) Preliminary check: whether objects owned under each role's name exist
SELECT n.nspname, c.relname, c.relkind, r.rolname AS owner
FROM pg_class c
JOIN pg_namespace n ON n.oid = c.relnamespace
JOIN pg_roles r ON r.oid = c.relowner
WHERE r.rolname IN ('marketconnector_app','view_app','crawler_app','preprocessor_app',
                    'decision_app','research_app','execution_app')
ORDER BY 4, 1, 2;
-- 0 results is normal. If 1 or more, REASSIGN or ALTER OWNER is needed.

-- 1) Organize object ownership / residual permissions
REASSIGN OWNED BY marketconnector_app, view_app, crawler_app, preprocessor_app,
                  decision_app, research_app, execution_app
              TO portfolio_owner;
DROP OWNED BY marketconnector_app, view_app, crawler_app, preprocessor_app,
              decision_app, research_app, execution_app;

-- 2) Remove roles
DROP ROLE IF EXISTS marketconnector_app;
DROP ROLE IF EXISTS view_app;
DROP ROLE IF EXISTS crawler_app;
DROP ROLE IF EXISTS preprocessor_app;
DROP ROLE IF EXISTS decision_app;
DROP ROLE IF EXISTS research_app;
DROP ROLE IF EXISTS execution_app;
```

### 6.4 Restore schema owner (optional)

If you want to undo the introduction of `portfolio_owner` itself:

```sql
-- Restore the schema owner (e.g., to portfolio_admin)
ALTER SCHEMA reference    OWNER TO portfolio_admin;
-- ... (repeat for the 9 schemas)

-- Revoke portfolio_owner permission then remove it
REVOKE portfolio_owner FROM portfolio_admin;
DROP ROLE IF EXISTS portfolio_owner;
```

### 6.5 Operational safeguards

- Take a manual snapshot just before revocation (per the 02 spec decision, PITR is also enabled, so a double safety net is secured).
- Prepare in advance an environment-variable toggle that can temporarily revert the application's connection string to `portfolio_admin` or a read-only backup role (formalized at the `06-secrets-and-iam` progress point).
- For a service with many connections (view, etc.), lock traffic before revocation by ECS Service desired count = 0 or maintenance mode.

## 7. Text for reflection into operation-notes / validation-checklist

The operator reflects the result into the document after running this SQL. This spec does not auto-update (per the max-3-auto-update-files policy after work completion). After the SQL execution ends, the operator directly adds the text below to the operation notes and checklist.

### 7.1 `operation-notes.md` addition section (copy as-is after execution)

```markdown
## 2026-06-09 DB Role / Permission Separation First Application

- Record of the result of applying this spec's [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §4 SQL.
- Applied environment: aws-paper, RDS PostgreSQL, DB `portfolio`, master = `portfolio_admin`.
- New owner role: created `portfolio_owner` + transferred ownership of the 9 domain schemas (reference / interest / preprocessor / research / decision / execution / connector / ops / legacy).
- Created 7 new app roles: `marketconnector_app`, `view_app`, `crawler_app`, `preprocessor_app`, `decision_app`, `research_app`, `execution_app` (all LOGIN, non-SUPERUSER, non-CREATEDB, non-CREATEROLE, non-REPLICATION, non-BYPASSRLS).
- Permission matrix application complete. Key changes: the legacy schema is not granted to any app role, and marketconnector_app's execution is reduced to R only.
- Set DEFAULT PRIVILEGES under portfolio_owner's name on the 9 schemas. The matrix is applied automatically when new objects are created afterward.
- search_path is fixed at the role level via 7 `ALTER ROLE ... SET search_path`. Consistent with each MS README definition.
- No secret value recorded. All password / endpoint / account-id use only `[REDACTED]` or a placeholder.
- This work runs only SQL inside the DB. No AWS resource creation / change / deletion. No modification of the 8 MS code / README / AGENTS.md / CHANGELOG / docs / worklog.
- Follow-up: at the 06 spec stage, store the 7 app role passwords separately in Secrets Manager / SSM SecureString.
```

Briefly correct the text above to match the SQL execution result. If there is a failure / partial application, record it as-is.

### 7.2 `validation-checklist.md` addition items (after execution, add to §6 or a new §11 section)

Recommendation: promote the `[운영자 확인 필요]` items of the existing `## 6. DB / schema / role preparation Validation` section to `[O]` per the SQL execution result. Additionally, add 5 permission validation result items.

```markdown
## 11. DB Role Permission Matrix Validation

- <span style="color:red">[O]</span> Confirm the 7 app roles + `portfolio_owner` all exist (§5.1 first query result matches 7+1 rows)
- <span style="color:red">[O]</span> Confirm `rolsuper / rolcreatedb / rolcreaterole / rolreplication / rolbypassrls` = false for all app roles
- <span style="color:red">[O]</span> Confirm `legacy` schema USAGE = false for all app roles (all 7 rows false in the §5.2 second query)
- <span style="color:red">[O]</span> The §5.2 schema USAGE / CREATE matrix matches the [`./db-roles-and-grants.md`](./db-roles-and-grants.md) §2 table (0 mismatches)
- <span style="color:red">[O]</span> The §5.3 table permission summary and sequence permission summary match the matrix (0 mismatches)
- <span style="color:red">[O]</span> After connecting as each app role, write to your responsible schema is normal / write to another schema is permission denied normal (§5.4)
- <span style="color:black">[운영자 확인 필요]</span> Whether the passwords of the 7 app roles are registered in the Secrets Manager / SSM SecureString placeholder positions (formally recorded at the `06-secrets-and-iam` stage)
```

The label / color notation follows the existing `validation-checklist.md` 4-label rule as-is. Write `[O]` or `[X]` per the SQL result.

## 8. Follow-up Update Candidates (2026-06-17 17-step E2E reinforcement)

This section accumulates, as follow-up update candidates, the DB role permission / search_path facts the operator found / corrected during the 2026-06-17 Daily AWS 17-step E2E flow.

The operator's direct GRANT correction results are recorded as facts below, and the formal matrix update of this spec's body §4 GRANT / §5 validation SQL is a follow-up phase responsibility.

- [`./operation-notes.md`](./operation-notes.md) 2026-06-17 §1 ~ §4
- [`../_common/risk-register.md`](../_common/risk-register.md) R-DATA-005 [2026-06-17 reinforcement] / R-DATA-011 new consistency

### 8.1. `execution_app`'s `interest` schema permission formal update candidate

- Candidate to add to the `execution_app` row of the §4.4 GRANT matrix: `interest` schema USAGE / `interest.*` table SELECT / sequence USAGE+SELECT / future default privileges (`ALTER DEFAULT PRIVILEGES IN SCHEMA interest GRANT SELECT ON TABLES TO execution_app`, etc.).
- Candidate to add to the `execution_app` row of the §5 validation SQL: `has_schema_privilege('execution_app', 'interest', 'USAGE')` / `has_table_privilege('execution_app', 'interest.<key table name>', 'SELECT')` inspection SQL.
- This date's first-failure fact is consistent with [`../04-strategy-batch-stepfunctions/operation-notes.md`](../04-strategy-batch-stepfunctions/operation-notes.md) 2026-06-17 §3 / consistent with R-DATA-005 [2026-06-17 reinforcement].

### 8.2. `marketconnector_app`'s `legacy` schema · `legacy.holdings` · search_path formal update candidate

- Candidate to add to the `marketconnector_app` row of the §4.4 GRANT matrix: `legacy` schema USAGE / `legacy.holdings` DML (SELECT / INSERT / UPDATE / DELETE) / sequence USAGE+SELECT+UPDATE / future default privileges.
- Candidate to add to the `marketconnector_app` row of the §3 search_path strategy: database search_path = `connector, execution, legacy, reference, public` (`ALTER ROLE marketconnector_app IN DATABASE portfolio SET search_path = connector, execution, legacy, reference, public`).
- Candidate to add to the `marketconnector_app` row of the §5 validation SQL: `current_setting('search_path')` / `has_schema_privilege('marketconnector_app', 'legacy', 'USAGE')` / `has_table_privilege('marketconnector_app', 'legacy.holdings', 'SELECT,INSERT,UPDATE,DELETE')` inspection SQL.
- Candidate to add a bare-table-name-dependent legacy path validation SQL (`SET ROLE marketconnector_app; SELECT 1 FROM holdings LIMIT 1;`).
- The single marketconnector_app-only exception fact of the OD-DB-007 (legacy schema not granted to any app role) policy is consistent with [`../_common/operator-decisions.md`](../_common/operator-decisions.md) Change Log 2026-06-17 second item / R-DATA-011 new. The change of this spec's body decision value is a follow-up separated.

### 8.3. `marketconnector_app`'s execution table UPDATE permission (OD-DB-008 R-only policy follow-up re-review)

- One cause of this date's Step 12 `MARKETCONNECTOR_STRATEGY_ORDER_EXECUTE` first failure is the missing `marketconnector_app` execution table UPDATE permission (consistent with the OD-DB-008 R-only policy). The operator directly GRANTed to first-correct it so the SUBMITTED transition is possible — consistent with 03 spec operation-notes 2026-06-17 §2.
- Follow-up re-review candidate for the OD-DB-008 R-only policy — since the MarketConnector executor has the responsibility to update `strategy_execution_order` to SUBMITTED / FAILED, whether some UPDATE permission on the execution table is needed for marketconnector_app is a follow-up separated in [`../_common/operator-decisions.md`](../_common/operator-decisions.md) OD-DB-008.

### 8.4. Follow-up update schedule

- The GRANT / search_path correction results the operator performed directly on this date are recorded as facts only — the formal matrix update of this spec's body §4 / §5 / §3 is a follow-up phase responsibility.
- At the formal matrix update point, integrate these §8 items into the §4 / §5 / §3 body.
- This §8 itself is an update-candidate accumulation section, with 0 body decision-value changes.

## Safety Constraints for This Document's Work

- This document is a SQL draft / validation / rollback / reflection-text deliverable, and the actual SQL execution is performed by the operator.
- This work is not included in the scope of modifying the 8 MS's README / AGENTS.md / CHANGELOG / docs / worklog.
- This work does not create / change / delete AWS resources.
- This document does not write the actual password / secret value / endpoint / account-id / account number / token. All use only `[REDACTED]` or a placeholder.
- Where the decision values (legacy not granted, marketconnector_app execution reduced to R only) differ from the 02 spec design.md matrix, a follow-up update may be needed in the OD-DB category of `../_common/operator-decisions.md` (registered as a current decision change).
