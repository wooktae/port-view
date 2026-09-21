# Source File Catalog

This document provides a quick reference to the responsibilities of primary port-view files and directories.

It is not an inventory of every file. It records only the items needed to understand the structure and assess change impact.

Paths are relative to the repository root; build outputs and cache files are excluded.

## Usage Rules

| Item | Value |
| --- | --- |
| Baseline | Current port-view code and documentation structure |
| Included | Primary entrypoints · layer groups · files with operational impact |
| Excluded | Build outputs · cache · simple generated files |
| Update | When a file path · responsibility · operational impact changes |
| Omit | When only internal implementation changes and responsibility remains the same |
| Sensitive information | Do not record actual account numbers · secrets · tokens · ARNs · public IPs |

## Root

| File | Responsibility |
| --- | --- |
| `AGENTS.md` | Rules for port-view code and documentation work |
| `README.md` | Current structure · execution method · production AS-IS |
| `CHANGELOG.md` | Primary port-view change history |
| `pom.xml` | Maven dependencies and build configuration |
| `mvnw` · `mvnw.cmd` | Maven Wrapper |
| `Dockerfile` | Container image build for ECS Fargate |
| `.devops/codebuild/buildspec.yml` | CodeBuild Maven Test · Package · Docker build · ECR Push · Evidence recording |
| `.github/workflows/view-codebuild.yml` | Main push/manual execution · CodeBuild · Candidate Smoke · approval-gated Production Promotion |

### Change Checks

| Target | Check |
| --- | --- |
| `AGENTS.md` | Work scope · safety gates · documentation update rules |
| `README.md` | User-facing structure and production state |
| `CHANGELOG.md` | Record only actual port-view changes |
| `pom.xml` | Compile · test · package impact |
| `Dockerfile` | Runtime · port · profile · build context |
| `.devops/codebuild/buildspec.yml` | Test · package · image · ECR Push · Evidence |
| `.github/workflows/view-codebuild.yml` | Candidate Smoke · `production` approval · Promotion flow |

## Application

| File | Responsibility |
| --- | --- |
| `src/main/java/my/portfolio/port_view/PortViewApplication.java` | Spring Boot entrypoint and ConfigurationProperties scan |
| `src/main/java/my/portfolio/port_view/common/ViewNames.java` | Thymeleaf View name constants |

When changing `PortViewApplication`, verify context loading and configuration scanning.

When changing `ViewNames`, verify Controller return values and template paths together.

## Controller

| File · Path | Responsibility |
| --- | --- |
| `BalanceController.java` | `/balance-summary` Balance view |
| `DashboardController.java` | `/` · `/dashboard` consolidated view |
| `PositionController.java` | `/positions` list and details |
| `OrderController.java` | `/orders` list and details |
| `DailyBatchController.java` | Daily Batch queries · local actions · AWS triggers |
| `StrategyExecutionViewController.java` | Execution Plan queries and submission actions |
| `StrategyReportController.java` | Latest · specific run · Research Version-selected Report · production Version selection action |
| `controller/*.java` | View Controller group including Strategy Daily |

### Change Checks

| Item | Value |
| --- | --- |
| URL | Existing endpoint compatibility |
| Model | Template attribute names |
| Execution action | Gates and running state |
| Sensitive information | Do not expose account numbers · ARNs · order numbers |
| External calls | Distinguish query requests from execution requests |

## Service

| File · Path | Responsibility |
| --- | --- |
| `BalanceService.java` | Query balance · initial capital · cumulative profit/loss |
| `DashboardService.java` | Assemble Dashboard DTOs |
| `PositionService.java` | Assemble Position list and detail DTOs |
| `OrderService.java` | Assemble Order · Fill · event timeline |
| `ReportService.java` | Assemble Report summary · statistics · details · Research Version list/selected Version Report |
| `OperatingResearchVersionProvider.java` | Query actual OPERATING RSCFG from Production Step4 → Batch Job Definition command |
| `ResearchChampionPromotionService.java` | RSCFG production promotion Plan · Job Definition Revision creation · dual State Machine preflight/promotion/after-check/rollback |
| `StrategyExecutionViewService.java` | Query Execution Plans and Order Candidates |
| `StrategyExecutionSubmitService.java` | Submit external execution requests |
| `ConnectorSnapshotRefreshService.java` | Local View Snapshot Refresh |
| `ConnectorOrderRefreshService.java` | Local View Order Status Refresh |
| `DailyBatchService.java` | local-file execution and Step Log queries |
| `DailyBatchAsyncService.java` | Delegate asynchronous local-file execution |
| `StepFunctionsDailyBatchExecutionService.java` | AWS Step Functions `StartExecution` |
| `SlackNotificationService.java` | Assemble Local View Slack messages |
| `SlackClient.java` | Send Slack webhook HTTP requests |
| `service/*.java` | View application service group |

### Responsibility Boundary

| Service | Production Standard |
| --- | --- |
| `StepFunctionsDailyBatchExecutionService` | Default Fargate and AWS Paper trigger path |
| `DailyBatchService` | local-file validation and recovery |
| Connector Refresh Service | Subprocess use allowed only in Local View |
| Submit · Slack Service | May perform actual external requests · do not run arbitrarily |
| `OperatingResearchVersionProvider` | Read-only production RSCFG query · Production runtime source of truth |
| `ResearchChampionPromotionService` | Operator-approved RSCFG promotion · preserve Runtime Contract except `--strategy-config-version` · do not run arbitrarily |

When changing a Service, verify success, failure, and blocked paths separately.

## Repository

| File · Path | Responsibility |
| --- | --- |
| `repository/*.java` | DB queries using JPA and JdbcTemplate |
| `ConnectorOrderRequestRepository.java` | Order list · details · Refresh targets |
| `ReportRepository.java` | Report summary · statistics · Trade Details · `strategy_config_version` list and latest run for selected Version |
| Daily Batch repository | Query `ops` Batch Run and Step Log |
| Strategy repository | Query `execution` · `decision` · `research` |

### Change Checks

| Item | Value |
| --- | --- |
| Schema | `ops` · `execution` · `decision` · `research` · `connector` |
| SQL | Do not use assumed column names |
| search path | Resolution order for existing unqualified SQL |
| Mapping | Entity · DTO type consistency |
| Result semantics | Preserve view status and aggregation standards |

Use schema-qualified names for new production and diagnostic SQL whenever possible.

## Entity

| Path | Responsibility |
| --- | --- |
| `src/main/java/my/portfolio/port_view/entity/*.java` | Map DB rows to JPA Entities |

Primary areas:

- Balance
- Connector Snapshot
- Connector Order Request
- Fill
- Event
- Position

Verify actual tables and columns when changing an Entity.

Do not modify an Entity based on assumed DDL or columns.

## DTO

| Path | Responsibility |
| --- | --- |
| `dto/dashboard/*.java` | Dashboard cards · positions · recent orders |
| `dto/position/*.java` | Position list · details · insights |
| `dto/order/*.java` | Order list · details · Fill · event |
| `dto/dailybatch/*.java` | Batch Run · Step Log · execution options |
| `dto/strategy/*.java` | Execution Plan · Daily Signal · Report |

When changing a DTO, verify the Service assembly code and Thymeleaf fields together.

Keep display-only values in DTOs and do not expose Entities directly.

## Config

| File · Path | Responsibility |
| --- | --- |
| `AsyncConfig.java` | Asynchronous executor |
| `LocaleConfig.java` | MessageSource · `lang` locale interceptor · cookie-backed locale persistence · English default |
| `ConnectorProperties.java` | Connector URL and API configuration |
| `DailyBatchProperties.java` | backend · gate · Step Functions configuration |
| `PortfolioViewProperties.java` | Default account and view limits |
| `SlackProperties.java` | Slack enablement and webhook configuration |
| `SnapshotRefreshProperties.java` | Snapshot Refresh configuration |
| `config/*.java` | Typed configuration group |

When changing configuration, verify the following together.

1. `application.properties`
2. Profile-specific properties
3. `@ConfigurationProperties`
4. README
5. Container environment-variable names

## Util

| Path | Responsibility |
| --- | --- |
| `src/main/java/my/portfolio/port_view/util/*.java` | Amount · date · locale-aware label · CSS class · account resolution |
| `ViewMessages.java` | Locale-context lookup for display-only Java text in the EN/KO message bundles |

When changing a Util, verify the impact on display values and CSS classes across all views.

Do not expose account numbers verbatim in views or logs.

## Templates

| File · Path | Responsibility |
| --- | --- |
| `templates/fragments/sidebar.html` | Common navigation and `EN | KO` language switch |
| `templates/pages/dashboard.html` | Dashboard |
| `templates/pages/balance-summary.html` | Balance |
| `templates/pages/positions.html` | Position list |
| `templates/pages/position-detail.html` | Position details |
| `templates/pages/orders.html` | Order list |
| `templates/pages/order-detail.html` | Order details |
| `templates/pages/daily_batch.html` | Batch status and execution actions |
| `templates/pages/strategy_report.html` | Strategy Report · Research Version Dropdown · OPERATING display · production selection button for non-production Versions |
| `templates/pages/*.html` | View group including Strategy · Daily · Report |

### Change Checks

| Item | Value |
| --- | --- |
| Model | Controller attribute names |
| Link | Existing URL |
| Form | Action endpoint and method |
| Gate | Safe and approval button conditions |
| Status | Badge and CSS class |
| Security | Redact account · ARN · order identifiers |

Do not arbitrarily click execution buttons in `daily_batch.html` for view validation.

## Static

| Path | Responsibility |
| --- | --- |
| `static/css/layout/app-layout.css` | Common layout and sidebar language-switch styles |
| `static/css/pages/*.css` | View-specific card · grid · status styles |
| `static/*` | Static resource group |

Keep common CSS changes small because they affect multiple views.

When changing a Template class, verify the corresponding CSS.

## Resources

| File · Path | Responsibility |
| --- | --- |
| `application.properties` | Common server · DB · View · Connector · Batch · Slack configuration |
| `application-aws-paper.properties` | AWS Paper profile overrides |
| `application-local.properties.example` | Local configuration example without sensitive information |
| `messages.properties` | Default English-first MessageSource bundle and Class B UI keys |
| `messages_en.properties` | English locale marker; inherits default English values |
| `messages_ko.properties` | Korean localization values for Class B UI keys |
| `src/main/resources/*` | Profile-specific configuration and View resources |

### Primary Configuration Categories

| Setting | Responsibility |
| --- | --- |
| `spring.datasource.*` | PostgreSQL |
| `spring.jpa.*` | JPA · Hibernate |
| `portfolio.view.*` | View defaults |
| `connector.*` | Connector integration |
| `portfolio.batch.*` | backend and execution gates |
| `portfolio.snapshot-refresh.*` | Snapshot Refresh |
| `slack.*` | Local View Slack |

Do not record actual passwords, webhooks, accounts, or ARNs in the repository.

## Database Contract

| Item | Value |
| --- | --- |
| Database | `portfolio` |
| View DB user | `view_app` |
| Connector subprocess user | `marketconnector_app` |
| SQL resolution | Hikari `search_path` |
| Batch Run | `ops.strategy_daily_batch_run` |
| Step Log | `ops.strategy_daily_batch_step_log` |

Default schema order:

```text
ops, execution, decision, research, connector,
preprocessor, interest, reference, legacy, public
```

Do not bypass user separation by granting connector write permissions to `view_app`.

## Tests

| File · Path | Responsibility |
| --- | --- |
| `PortViewApplicationTests.java` | Spring context Smoke Test |
| `src/test/**/*.java` | Controller · Service · Repository tests |

Minimum validation by change scope:

| Change | Validation |
| --- | --- |
| Java | Compile or relevant test |
| Controller | Mapping · gate |
| Service | Success · failure · blocked |
| Repository | Query · schema · mapping |
| Template | Parse · Model field |
| Properties | Binding · default |
| Docker | Build context · runtime |

Do not report validation that could not run because of an external DB or API dependency as complete.

## Documents

| Document | Responsibility |
| --- | --- |
| `AGENTS.md` | port-view working rules |
| `README.md` | Current structure and production AS-IS |
| `CHANGELOG.md` | port-view change history |
| `docs/source-file-catalog.md` | Primary files and responsibilities |

Structure · configuration · Daily Batch · security · refactoring standards are consolidated in the README and this catalog.

Do not create date-specific `docs/worklog/*.md` files.

Record code and documentation changes in `CHANGELOG.md`, and place detailed standards in the relevant `docs` document.

## External Dependencies

| Module | Relationship |
| --- | --- |
| `port-marketconnector` | Account · order · Fill |
| `port-interest-crawler` | Source data collection |
| `port-interest-preprocessor` | Preprocessing |
| `port_strategy_common` | Shared strategy model |
| `port_strategy_research` | Strategy Research |
| `port_strategy_decision` | Strategy Decision |
| `port_strategy_execution` | Planning and Order Execution |

Code and documentation in another microservice are not automatically included in the port-view change scope.

## Catalog Update Conditions

| Change | Handling |
| --- | --- |
| Create · delete · rename a primary file | Update |
| Change a package · template · CSS directory | Update |
| Change Controller · Service · Repository responsibility | Update |
| Change configuration file · Docker · build responsibility | Update |
| Create · delete documentation or change its responsibility | Update |
| Change only internal implementation and keep responsibility unchanged | May omit |

Do not create a new full-repository inventory when updating the catalog.

Review only the changed areas and adjacent entries.
