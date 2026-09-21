# port-view Working Rules

This document defines the standards for modifying the code, configuration, views, tests, and documentation of the `port-view` microservice.

A contributor should be able to understand the scope, responsibility boundaries, safety standards, documentation practices, and validation principles for `port-view` by reading this document alone.

## 0. Highest-Priority Documentation Readability Rules

Apply this section before all other rules for documentation work.

### 0.0 Scope Limitation

The readability rules apply only to content newly written or directly modified in the current task.

Unless the user explicitly requests a full-document cleanup or comprehensive review, do not perform the following work.

| Item | Default Handling |
| --- | --- |
| Full scan of existing documentation | Do not perform |
| Complete README restructuring | Do not perform |
| Large-scale cleanup of historical CHANGELOG entries | Do not perform |
| Creation of a new scanner or audit tool | Do not perform |
| Creation of a sub-agent or orchestrator | Do not perform |

For adjacent content, review only the same table row, bullet group, or short paragraph.

Preserve existing out-of-scope violations and, when necessary, list them only as follow-up candidates.

### 0.1 Table Rules

New standalone summary tables should use two columns by default.

The default headers are `Item / Value`.

More specific two-column headers may be used in the following situations.

| Situation | Preferred Headers |
| --- | --- |
| Validation results | `Item / Result` |
| Changes by file | `File / Change` |
| Description by view | `View / Content` |
| Configuration summary | `Setting / Value` |
| Endpoint summary | `Path / Responsibility` |
| Test results | `Test / Result` |

When adding a row to an existing table, preserve its existing column structure.

Tables with three or more columns are allowed only in the following cases.

| Condition | Handling |
| --- | --- |
| Explicitly requested by the user | Use the requested structure |
| Preserving the existing table is safer | Keep the existing structure |
| Converting a comparison to two columns would lose meaning | Allow an exception |

### 0.2 Table Cell and Sentence Length

- Keep table cells to no more than two sentences.
- Separate multiple values in one cell with `<br>`.
- Do not place three or more facts in a long sentence within one cell.
- Move lengthy evidence outside the table or link to the relevant document.
- Do not create cells longer than 300 characters or lines longer than 500 characters.
- Do not paste raw logs, complete SQL output, or complete AWS responses into documentation.

### 0.3 Documentation Density

Use the following priority when writing documentation.

1. Short summary
2. Short two-column table
3. Short bullets
4. Link to detailed documentation
5. Long-form text

Do not repeat the same facts at length across the README, CHANGELOG, worklog, and detailed documentation.

### 0.4 Status Indicators

Use only the following five status indicators.

| Indicator | Meaning |
| --- | --- |
| 🔴 | Prohibited · live · high risk |
| 🟠 | Pending · observing · unconfirmed |
| 🟢 | Complete · successful · ENABLED |
| 🔵 | Reference · information · evidence |
| ⚫ | Not applicable |

Do not add HTML colors when a status indicator is sufficient.

### 0.5 Work Method Restrictions

Perform documentation work in the following order.

1. Confirm the requested scope
2. Read the target file directly
3. Modify only the necessary sections
4. Save as UTF-8 without BOM
5. Perform a short after-check
6. Report a change summary

Unless explicitly requested by the user, do not use the following methods.

- Full workspace scan
- Sub-agent
- Orchestrator
- New scanner
- Content hash matrix
- Temporary files outside the workspace
- Excessive automation scripts

## 1. Scope

### 1.1 Default Working Directory

`C:\Workspaces\port-view`

### 1.2 Default Modification Targets

| Category | Target |
| --- | --- |
| Java | `src/main/java` |
| Configuration | `src/main/resources/*.properties` |
| Views | `src/main/resources/templates` |
| CSS · static files | `src/main/resources/static` |
| Tests | `src/test` |
| Documentation | `README.md` · `CHANGELOG.md` · `docs` |
| Build | `pom.xml` · Maven Wrapper |
| Container | `Dockerfile` and deployment-related files |
| CI/CD | `.devops/codebuild/buildspec.yml` · GitHub Actions workflow |
| Deployment | ECR · ECS Task Definition · deployment scripts |

Do not modify files that are not included in the current request.

### 1.3 Other Microservices

The following projects are external dependencies of port-view.

- `port-marketconnector`
- `port-interest-crawler`
- `port-interest-preprocessor`
- `port_strategy_common`
- `port_strategy_decision`
- `port_strategy_research`
- `port_strategy_execution`

Do not modify code or documentation in another microservice unless the current task explicitly requires it.

Cross-service specs under `.kiro` are not automatically included in the port-view work scope.

## 2. port-view Responsibility Boundary

port-view is a Spring MVC-based View microservice responsible for query, approval, and trigger UI functions.

### 2.1 Responsibilities

| Item | Content |
| --- | --- |
| Dashboard | Account · order · position · strategy result summary |
| Balance | Balance and valuation amount queries |
| Positions | Position list and detail queries |
| Orders | Order request · fill · event queries |
| Strategy | Execution plan · Daily Run · Report queries |
| Daily Batch | Run history and Step result queries |
| Trigger UI | Step Functions execution requests |
| Approval UI | Approval trigger for the Paper order-related range |

### 2.2 Excluded Responsibilities

- Broker order submission logic
- Strategy decision logic
- Data collection and preprocessing
- Internal Step Functions orchestration
- EventBridge Scheduler automated execution
- Internal MarketConnector EC2 commands
- KRX crawler execution
- aws-live automated BUY/SELL

Do not duplicate core business logic from another microservice inside View code.

### 2.3 Research Strategy Config Version Promotion

Research Strategy Config Version Promotion is an exceptionally permitted operator-approved control action.

| Item | Standard |
| --- | --- |
| Nature | Limited production promotion based on explicit operator approval |
| Decision logic | Do not duplicate Strategy decision or backtest logic in View |
| Production source of truth | Batch Job Definition command referenced by Production Step4, not DB status |
| Runtime Contract | Do not change any item other than `--strategy-config-version` |
| Safety controls | dual target preflight · running execution check · fail-closed · after-check · rollback retained |
| endpoint | Clearly separate the production transition endpoint from general query endpoints |
| Sensitive information | Do not record actual ARN · account-id · digest values in code descriptions or documentation |

## 3. Technical Standards

| Item | Standard |
| --- | --- |
| Java | 25 |
| Spring Boot | 4.1.0-SNAPSHOT |
| Web | Spring MVC |
| Template | Thymeleaf |
| Persistence | Spring Data JPA · JdbcTemplate |
| Database | PostgreSQL |
| Build | Maven Wrapper |
| Container | Docker · validated ECS Fargate configuration |

Preserve the existing package structure and naming conventions.

Do not introduce unnecessary frameworks or large-scale structural changes.

## 4. Production AS-IS Baseline

### 4.1 Current State

| Item | Value |
| --- | --- |
| ECS Fargate | Initial port and view-query validation complete |
| Current production profile | Portfolio validation state retained |
| Default backend | `aws-stepfunctions` |
| Local File backend | For operator local validation and recovery |
| Step 1~11 | Separated as safe triggers |
| Step 12~17 | Separated as approval triggers |
| P2 enhancement | Not performed · out of scope |
| aws-live | Not started |

ALB, HTTPS, Route53, authentication, Auto Scaling, Blue/Green, and public exposure are not currently complete.

Record them as items to reconsider when public exposure or multi-user operation is required.

### 4.2 Execution Backends

| backend | Responsibility |
| --- | --- |
| `aws-stepfunctions` | Default execution path for ECS Fargate and the operator View |
| `local-file` | Local validation and recovery |
| View subprocess | Not used in the Fargate production path |

Do not directly execute local absolute paths or source directories from other microservices in the Fargate production path.

### 4.3 Paper Order Gates

Step 12 and later may submit Paper orders.

Do not bypass or loosen the defaults of the following gates.

- `portfolio.batch.execution-enabled`
- `portfolio.batch.local-file-execution-enabled`
- `portfolio.batch.full-pipeline-execution-enabled`
- `portfolio.batch.paper-order-enabled`
- `portfolio.batch.min-executable-step-order`
- `portfolio.batch.max-executable-step-order`
- `portfolio.batch.aws-stepfunctions-start-enabled`
- `portfolio.batch.aws-stepfunctions-step-start-enabled`

Do not combine the responsibilities of approval endpoints and general endpoints.

## 5. Spring Code Rules

### 5.1 Controller

- Focus on interpreting request parameters and assembling the Model.
- Do not place business logic directly in a Controller.
- Prefer compatibility with existing endpoint names and URLs.
- Validate gates and running state before an execution endpoint proceeds.
- Do not expose sensitive information in the user interface.

### 5.2 Service

- Assemble view DTOs and application workflows.
- Do not duplicate logic from external modules.
- Do not convert failures into successes.
- Clearly separate AWS SDK calls from the local-file execution path.
- Require an explicit approval gate for Paper order-related behavior.

### 5.3 Repository and SQL

- Respect the existing use of JPA and JdbcTemplate.
- Do not write SQL using assumed column names.
- Before modifying SQL, verify actual columns through entities, repositories, migration documentation, or `information_schema.columns`.
- Because multiple schemas may contain tables with the same name, use schema-qualified names for new production SQL whenever possible.
- Confirm that existing unqualified SQL is consistent with the datasource `search_path`.

### 5.4 DTO and Entity

- Preserve the existing patterns for Lombok class DTOs and Java records.
- Keep display-only values in DTOs.
- Do not expose Entities directly as view Models.
- Do not add fields based on assumptions that conflict with the DB schema.

### 5.5 Thymeleaf and CSS

- Prefer the existing page layout and fragment structure.
- Reuse the existing badge or status class system for status presentation.
- Clearly indicate backend and gate state on execution buttons.
- Visually distinguish dangerous execution buttons from general query buttons.
- Do not render sensitive information such as account numbers, ARNs, or public IPs.

## 6. Database and Configuration Standards

### 6.1 Database

The default database is `portfolio`.

The primary domain schemas are:

- `ops`
- `execution`
- `decision`
- `research`
- `connector`
- `preprocessor`
- `interest`
- `reference`
- `legacy`
- `public`

The port-view datasource queries multiple domain schemas.

When changing the `search_path`, verify the impact on the Dashboard, Balance, Positions, Orders, Strategy, and Daily Batch views.

### 6.2 DB Users

| Purpose | user |
| --- | --- |
| Spring View datasource | `view_app` |
| MarketConnector subprocess | `marketconnector_app` |

Do not arbitrarily grant connector write permissions to `view_app`.

### 6.3 Environment Variables

Do not record actual values in the repository.

Primary configuration categories include:

- `INTEREST_DB_*`
- `PORTFOLIO_DB_NAME`
- `PORTFOLIO_VIEW_*`
- `PORTFOLIO_BATCH_*`
- `PORTFOLIO_SNAPSHOT_REFRESH_*`
- `SPRING_PROFILES_ACTIVE`
- AWS region and Step Functions ARN
- Slack configuration
- Default account number

When adding or changing a configuration key, also verify:

1. `application.properties`
2. Profile-specific properties
3. `@ConfigurationProperties`
4. README
5. Deployment environment-variable names

## 7. Snapshot Refresh Rules

Separate Snapshot Refresh responsibilities by execution environment.

| Environment | Handling |
| --- | --- |
| Local View | May use the MarketConnector subprocess |
| ECS Fargate | Local subprocess prohibited |
| Fargate queries | Primarily query Snapshots stored in the DB |
| Remote refresh | Use AWS orchestration or the Connector path |

Do not use a local path such as `C:/Workspaces/...` as a production path in Fargate.

## 8. AWS and Security Rules

### 8.1 Sensitive Information

Do not record the following values verbatim in code, documentation, examples, or logs.

- secret
- password
- token
- KIS app key · app secret
- Slack webhook URL
- account number
- AWS account-id
- actual ARN
- public IP
- broker order number
- full SHA256 image digest

Use the following placeholders when needed.

| Placeholder | Purpose |
| --- | --- |
| `[REDACTED]` | General sensitive information |
| `[REDACTED_ACCOUNT_NO]` | Account number |
| `[REDACTED_ARN]` | ARN |
| `[REDACTED_SECRET_ARN]` | Secret ARN |
| `[REDACTED_TASK_ARN]` | ECS task ARN |
| `[REDACTED_PUBLIC_IP]` | Public IP |
| `[REDACTED_BROKER_ORDER_NO]` | Broker order number |
| `<ECR_IMAGE_URI>` | ECR image URI |
| `<ALB_ENDPOINT>` | Future ALB endpoint |

### 8.2 Execution Restrictions

Do not perform the following work unless explicitly requested by the user.

| Category | Prohibited Work |
| --- | --- |
| AWS | Create · modify · delete resources |
| DB | DDL · DML · psql execution |
| Broker | Submit orders |
| KIS | Call external APIs |
| Slack | Deliver an actual webhook |
| Production | Execute Scheduler · Step Functions · ECS |
| Git | add · commit · push · reset · restore |

Run read-only validation commands only within the user-requested scope.

### 8.3 AWS Command Examples

When writing AWS CLI examples, use the following flow.

1. `list` or `describe`
2. Extract identifiers into variables
3. Run a subsequent `describe`
4. Validate state

Avoid examples that require the operator to manually insert an ARN, task ARN, or ENI ID.

### 8.4 PowerShell and Encoding

- Save files containing Korean as UTF-8 without BOM.
- Check native command results with `$LASTEXITCODE`.
- Do not print a SUCCESS or DONE marker after a failed command.
- Prefer file-based input for multiline JSON.

### 8.5 Deployment and CI/CD Safety Standards

Deployment and CI/CD files are also valid port-view modification targets.

| Target | File |
| --- | --- |
| CI Build | `.devops/codebuild/buildspec.yml` |
| Workflow | GitHub Actions workflow |
| Image | `Dockerfile` |
| Deployment | ECR · ECS Task Definition · deployment scripts |

Deployment validation rules:

- Run Candidate validation with Batch · Step Functions · Paper orders · Strategy Execution submission disabled.
- Do not promote the Candidate Revision directly to production.
- Create a separate production promotion Revision by combining the existing production environment variables with the validated Image Digest.
- Save the previous known-good Revision as the Rollback baseline before production promotion.
- Perform Candidate · production · Rollback Smoke Tests against actual View menu paths.
- Deployment Slack notifications are not a current completion requirement and are deferred until a separate channel policy is defined.

Rules for the main-push Release Workflow:

- Even with a main-push Workflow, perform Production promotion only after the `production` approval Gate.
- Do not perform Production Promotion before Candidate Smoke succeeds.
- The Artifact validated in Candidate and the Image Digest used in Production must be identical.
- Preserve production configuration by basing Promotion on the current Operating Task Definition.
- When changing the Research Version UI, verify the Strategy Report path in Candidate and Production Smoke Tests.

Detailed DevOps implementation such as GitHub OIDC Trust Policy, IAM permissions, Environment Required Reviewer settings, and temporary Runner IP access through an SG remains the responsibility of port-devops and is not documented here.

Actual CI/CD execution, ECR Push, ECS promotion, and Rollback are subject to the execution restrictions in section 8.2. Perform them only during explicitly approved deployment work.

## 9. Tests and Validation

Perform the minimum validation appropriate to the change scope.

| Change Target | Minimum Validation |
| --- | --- |
| Java | Compile or relevant test |
| Controller | Endpoint mapping · gate conditions |
| Service | Success · failure · blocked paths |
| Repository | Query syntax · schema · mapping |
| Thymeleaf | Template parsing · variable consistency |
| Properties | Binding names · defaults |
| README · CHANGELOG | Links · factual consistency · readability |
| Docker | Build context and runtime configuration |

If full testing is excessive, run the relevant module tests first.

Do not report validations that could not be run as complete; record the reason they were not run.

## 10. Documentation Management Rules

### 10.1 README.md

The README describes the current structure and usage of port-view.

It should include:

- Technical stack
- Primary views
- Responsibility boundaries
- Current production AS-IS
- Execution backends
- Configuration
- Database and schemas
- Local execution
- ECS Fargate validation status
- Security and safety gates
- Links to detailed documentation

Do not copy lengthy cross-service execution history into the README.

Include only results that directly affect port-view, and keep them concise.

### 10.2 CHANGELOG.md

The CHANGELOG records only changes to port-view code, documentation, configuration, tests, and builds.

- Add the latest date at the top.
- When a CHANGELOG update is requested, inspect Git history for the specified date or commit range using read-only commands.
- Record only changes actually reflected in port-view code, configuration, tests, builds, or documentation.
- Use `Added`, `Changed`, `Fixed`, `Removed`, and `Security` as needed.
- Record changes in other microservices or internal Step Functions behavior only when port-view code or documentation actually changed.
- Do not record raw logs or one-time execution identifiers.
- Use primarily two-column tables in new sections.
- Preserve historical entries unless explicitly requested otherwise.

### 10.3 docs

Keep detailed explanations in the following document.

| Document | Responsibility |
| --- | --- |
| `docs/source-file-catalog.md` | Responsibilities of primary files and directories |

Before creating a new document, first determine whether the content can be incorporated into an existing document.

Do not create date-specific `docs/worklog/*.md` files.

Record port-view code and documentation changes in `CHANGELOG.md`, and place detailed design or production standards in the relevant `docs` document.

### 10.4 Automatic source-file-catalog.md Updates

When one of the following changes occurs, determine within the same task whether `docs/source-file-catalog.md` must be updated.

| Change | Handling |
| --- | --- |
| Create · delete · rename a primary Java file | Update the catalog |
| Change Controller · Service · Repository responsibility | Update responsibilities and cautions |
| Change a package or directory structure | Update paths and structure descriptions |
| Create · delete a primary template or CSS file | Update the view file list |
| Change the responsibility of properties · Docker · build files | Update configuration and build sections |
| Create · delete a documentation file or change its responsibility | Update the Documents section |
| Change only internal implementation without changing file responsibility | The update may be omitted |

The catalog is not an inventory of every file.

Record only files, grouped paths, and responsibilities meaningful to production and maintenance.

When there is no catalog change, do not repeatedly record that fact in the CHANGELOG.

## 11. Git Rules

Only read-only status inspection is allowed by default.

| Allowed | Purpose |
| --- | --- |
| `git status --short` | Inspect current working-tree changes |
| `git diff --stat` · `git diff --check` | Check change scope and whitespace errors |
| `git log` · `git show` · `git diff <commit>` | Inspect change history and CHANGELOG evidence within the requested scope |

Do not run the following Git write commands unless explicitly requested by the user.

- `git add`
- `git commit`
- `git push`
- `git reset`
- `git restore`
- `git checkout`
- `git stash`

## 12. Completion Report

At completion, report only the following and keep it concise.

| Item | Content |
| --- | --- |
| Changed files | Files actually modified |
| Key changes | Summary of functional or documentation changes |
| Validation | Tests · compilation · diff checks performed |
| Not performed | Validation that could not be run |
| Security | Whether sensitive values were recorded verbatim |
| Follow-up | Only items that actually remain |

Distinguish work performed by the operator from documentation work performed by Kiro.

## 13. Completion Checklist

- [ ] Were only the requested port-view files modified?
- [ ] Were other microservices and `.kiro` files left unchanged unless needed?
- [ ] Were the highest-priority documentation readability rules applied?
- [ ] Do new standalone tables use two columns by default?
- [ ] Were long cells and lines avoided?
- [ ] Was the port-view responsibility boundary preserved?
- [ ] Were Fargate and local-file backends kept distinct?
- [ ] Were the Step 12~17 Paper order gates preserved?
- [ ] Were schema and DB user consistency verified?
- [ ] Were sensitive values kept out of the documentation?
- [ ] Were failures not reported as successes?
- [ ] Was validation appropriate to the change scope performed?
- [ ] If file structure or responsibility changed, was `docs/source-file-catalog.md` updated?
- [ ] Were no date-specific `docs/worklog/*.md` files created?
- [ ] Were files saved as UTF-8 without BOM?
- [ ] Were only actual changes reflected in the README and CHANGELOG?
