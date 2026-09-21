# port-view

A Portfolio View microservice built with Spring MVC and Thymeleaf.

It provides a View UI for querying accounts, balances, positions, orders, Strategy Execution Plans, strategy results, and Daily Batch execution status, and for safely requesting AWS Step Functions execution by an operator.

Internal business logic from external microservices and implementation details of AWS orchestration are outside this repository's responsibility.

## Current Status

| Item | Value |
| --- | --- |
| Application | 🟢 Primary views and AWS Paper DB queries validated |
| ECS Fargate | 🟢 Initial port and view-query validation complete |
| View CI/CD | 🟢 Main-push automatic Release Workflow · Candidate Smoke · approval-gated Promotion validated |
| Production deployment validation | 🟢 Candidate validation · production promotion · Rollback re-promotion confirmed |
| Research Version query · production selection | 🟢 OPERATING display · production Version selection (Champion Promotion) implemented and validated |
| Current production profile | Portfolio validation state retained |
| Default execution backend | `aws-stepfunctions` |
| Local File backend | For operator local validation and recovery |
| Step 1~11 | Separated as safe triggers |
| Step 12~17 | Separated as approval triggers |
| Paper Daily | Initial stabilization complete as of 2026-07-22 |
| P2 View enhancement | 🟠 Not performed · currently out of scope |
| aws-live BUY/SELL | 🔴 Not started |

> ALB, HTTPS, Route53, authentication, Auto Scaling, Blue/Green, and public exposure are not currently complete. Reconsider them when public exposure or multi-user operation is required.

## Technical Stack

| Item | Value |
| --- | --- |
| Java | 25 |
| Spring Boot | 4.1.0-SNAPSHOT |
| Web | Spring MVC |
| Template | Thymeleaf |
| Persistence | Spring Data JPA · JdbcTemplate |
| Database | PostgreSQL |
| Utility | Lombok |
| Build | Maven Wrapper |
| Container | Docker · validated ECS Fargate configuration |

## Primary Views

| View | Path |
| --- | --- |
| Dashboard | `/` · `/dashboard` |
| Balance | `/balance-summary` |
| Positions | `/positions` · `/positions/{tickerCode}` |
| Orders | `/orders` · `/orders/{id}` |
| Strategy Execution | `/strategy/execution/plans` · `/strategy/execution/plans/{planId}` |
| Strategy Report | `/strategy/reports/latest` · `/strategy/reports/{runId}` |
| Strategy Daily | `/strategy/daily/latest` · `/strategy/daily/{dailyRunId}` |
| Daily Batch | `/daily-batch` · `/daily-batch/{batchRunId}` |

### Responsibilities by View

| View | Content |
| --- | --- |
| Dashboard | Account summary · recent orders · positions · latest strategy results |
| Balance | Balance and valuation amount queries |
| Positions | Position list and detail queries |
| Orders | Order request · Order Chain · event · Fill queries |
| Strategy Execution | Execution Plan and Order Candidate queries |
| Strategy Report | Backtest Report · statistics · Trade Details · Research Strategy Config Version selection query and production selection |
| Strategy Daily | Daily Run · signal · position decision |
| Daily Batch | Batch Run · Step result · status · log queries |

The Daily Batch view provides an AWS Step Functions execution-request UI.

- Steps 1~11 are separated as safe triggers.
- Steps 12~17 are allowed only behind an approval trigger and Paper order gate.
- `local-file` execution and Slack test functionality are used only for operator local validation and recovery.

The latest Strategy Report view supports Research Strategy Config Version selection queries and limited production selection in addition to Backtest Report queries.

- Select a Strategy Config Version from the Research Version Dropdown in the Hero area.
- The Dropdown list is based on `strategy_config_version` values present in `research.strategy_backtest_run`.
- The current production Version is determined from the actual Production runtime rather than DB status and displayed as `RSCFG-xxxx · OPERATING`.
- On initial entry, the actual OPERATING Version is selected by default rather than the latest Version.
- The latest Backtest Run for the selected Version is queried.
- When a non-production Version is selected, a "Select for Production" button appears.
- Production selection promotes the selected RSCFG to a new Research Batch Job Definition Revision and transitions both the Production Step4 and Step4-only State Machines.
- Bidirectional promotion and Rollback transitions have been validated.
- Strategy Config Version and Research Engine Version have distinct display meanings.
- Currently validated example Versions are `RSCFG-0001` and `RSCFG-0002`; the final production Version is `RSCFG-0001`.

## View Responsibility Boundary

### Responsibilities

| Item | Content |
| --- | --- |
| Queries | Dashboard · Balance · Positions · Orders · Strategy · Daily Batch |
| Status display | Batch Run · Step Log · order · Fill · position · balance |
| Safe trigger | Request AWS Step 1~11 execution |
| Approval trigger | Request approved AWS Step 12~17 execution |
| Gate | Display executable range and Paper order permission state |
| Research production selection | Request and perform operator-approved Research Strategy Config Version promotion |

View serves as an operator control UI that receives an explicit operator approval action and requests or performs limited production promotion of a Research Strategy Config Version. This promotion changes only the `--strategy-config-version` value and preserves the remaining Runtime Contract. View does not own the complete Step Functions orchestration.

### Excluded Responsibilities

| Item | Responsible Area |
| --- | --- |
| Broker order submission | MarketConnector and Strategy Execution |
| Strategy decisions | Strategy Research · Decision · Execution |
| Data collection | Interest Crawler |
| Preprocessing | Interest Preprocessor |
| Orchestration | Step Functions · EventBridge Scheduler |
| Remote execution | ECS RunTask · SSM RunCommand · AWS Batch |
| Automated Slack | Lambda and cross-service workflow |
| aws-live | Separate approval and cutover scope |

Do not duplicate core logic from another microservice inside View code.

## Package Structure

| Package | Responsibility |
| --- | --- |
| `controller` | Request parameter handling · Model assembly · View return |
| `service` | View DTO assembly · Connector integration · execution request |
| `repository` | JPA Repository · JdbcTemplate query |
| `dto` | View display DTOs |
| `entity` | JPA Entity |
| `config` | `@ConfigurationProperties` · executor configuration |
| `util` | Formatting · label · account resolver · CSS helper |
| `common` | Thymeleaf View name constants |

See the [Source File Catalog](docs/source-file-catalog.md) for detailed responsibilities of primary files.

## Local Execution

### Basic Execution

Linux and macOS:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The default port follows `server.port` in `application.properties`.

### Build

Linux and macOS:

```bash
./mvnw clean package
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean package
```

### AWS Paper Local View

A Local View that uses AWS Paper RDS and secrets requires the following prerequisites.

| Item | Value |
| --- | --- |
| RDS connection | Operator prepares port forwarding separately |
| Spring profile | `aws-paper` |
| DB · KIS secret | Local secret loader or environment variables |
| Account number | Inject through environment variable · never record in repository |
| wrapper location | `C:\Workspaces\portfolio-local-env` |

Wrappers are separated by backend.

| backend | Starter |
| --- | --- |
| `local-file` | `Start-PortfolioViewAwsPaperLocalFile.ps1` |
| `aws-stepfunctions` | `Start-PortfolioViewAwsPaperStepFunctions.ps1` |

Each starter dot-sources a same-named `Load-*Env.ps1` file before executing port-view.

Do not document the legacy `Start-PortfolioViewAwsPaperBatch.ps1` as the current default wrapper.

## Execution Backends

Select the backend with `portfolio.batch.execution-mode`.

### `aws-stepfunctions`

This is the default path for ECS Fargate and AWS Paper View.

View does not execute a Python subprocess directly; it calls only AWS Step Functions `StartExecution`.

The following services perform the actual Step execution.

- AWS Step Functions
- ECS RunTask
- SSM RunCommand
- AWS Batch
- Lambda

### `local-file`

This backend is for operator Local View validation and recovery.

It may execute Python commands from operator-local source through ProcessBuilder.

Do not use it in the Fargate production path.

| Environment | backend |
| --- | --- |
| ECS Fargate | `aws-stepfunctions` |
| Local View production validation | `aws-stepfunctions` |
| Local View recovery | `local-file` when needed |

## AWS Step Functions Trigger

### Controller Endpoints

| Path | Responsibility |
| --- | --- |
| `POST /daily-batch/aws-stepfunctions/start-range` | Step 1~11 safe trigger |
| `POST /daily-batch/aws-stepfunctions/start-approval-range` | Step 12~17 approval trigger |

The general workflow ARN and approval workflow ARN are injected separately.

Requests for Steps 12~17 are blocked when the approval ARN is missing or the gate is disabled.

### StartExecution Payload

| Type | Field |
| --- | --- |
| boolean | `allowPaperOrderExecute` · `paperOrderEnabled` |
| numeric | `fromStepOrder` · `toStepOrder` · `startStep` · `endStep` |
| date string | `runDate` · `yyyy-MM-dd` in Asia/Seoul |
| string | `environment` · `dbTarget` · `source` · `requestedBy` |
| string | `requestedFrom` · `fromStepCode` · `toStepCode` |

Do not send boolean and numeric values as strings because their types must match the Choice State.

The success view displays only `executionName` and a redacted `executionArn`.

## Daily Batch Gates

| Setting | Responsibility |
| --- | --- |
| `portfolio.batch.execution-enabled` | Allow all execution actions |
| `portfolio.batch.local-file-execution-enabled` | Allow Local File execution |
| `portfolio.batch.full-pipeline-execution-enabled` | Allow complete Steps 1~17 execution |
| `portfolio.batch.paper-order-enabled` | Allow Paper order-related Steps |
| `portfolio.batch.min-executable-step-order` | Minimum executable Step |
| `portfolio.batch.max-executable-step-order` | Maximum executable Step |
| `portfolio.batch.aws-stepfunctions-start-enabled` | Allow Step Functions backend |
| `portfolio.batch.aws-stepfunctions-step-start-enabled` | Allow Step 1~11 buttons |

### Fargate Safe Defaults

| Setting | Value |
| --- | --- |
| execution mode | `aws-stepfunctions` |
| local file | `false` |
| Paper order | `false` |
| full pipeline | `false` |
| max Step | `11` |

> AWS Paper orders may be submitted when `BATCH_PAPER_ORDER_ENABLED=true` and Step 12 or later executes. Always verify the execution range, approval workflow, and gates.

## Snapshot Refresh

Snapshot Refresh for Dashboard, Balance, and Positions has environment-specific responsibilities.

| Environment | Handling |
| --- | --- |
| Local View | May use MarketConnector subprocess refresh |
| ECS Fargate | Local subprocess prohibited |
| Fargate queries | Primarily query Snapshots stored in the DB |
| Remote refresh | Use AWS orchestration or the Connector path |

Primary settings:

| Setting | Responsibility |
| --- | --- |
| `portfolio.snapshot-refresh.enabled` | Enable all Snapshot Refresh behavior |
| `portfolio.dashboard.snapshot-refresh-enabled` | Enable on Dashboard entry |
| `portfolio.snapshot-refresh.stale-minutes` | Stale threshold |
| `portfolio.snapshot-refresh.timeout-seconds` | Subprocess timeout |
| `portfolio.snapshot-refresh.marketconnector-dir` | Local MarketConnector path |
| `portfolio.snapshot-refresh.balance-script-name` | Balance script |
| `portfolio.snapshot-refresh.python-executable` | Python executable |

Do not use a local absolute path such as `C:/Workspaces/...` as a production path in Fargate.

## Database

### Connection Standards

| Item | Value |
| --- | --- |
| Database | `portfolio` |
| Spring datasource user | `view_app` |
| Connector subprocess user | `marketconnector_app` |
| Password | Injected through environment variable or secret |
| SQL resolution | Based on Hikari `search_path` |

Do not undermine DB user separation by granting connector write permissions to `view_app`.

### Domain Schemas

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

Default `search_path`:

```text
ops, execution, decision, research, connector,
preprocessor, interest, reference, legacy, public
```

Existing unqualified SQL resolves against this `search_path`.

Use schema-qualified names for new production and diagnostic SQL whenever possible.

OPS Mirror baseline tables:

| Table | Responsibility |
| --- | --- |
| `ops.strategy_daily_batch_run` | Batch Run |
| `ops.strategy_daily_batch_step_log` | Step execution log |

### DB Environment Variables

| Environment Variable | Default · Responsibility |
| --- | --- |
| `INTEREST_DB_HOST` | `localhost` |
| `INTEREST_DB_PORT` | `5433` |
| `INTEREST_DB_NAME` | `portfolio` |
| `PORTFOLIO_DB_NAME` | `portfolio` |
| `INTEREST_DB_USER` | Environment-specific DB user |
| `INTEREST_DB_PASSWORD` | No default |

## Primary Configuration Categories

| Setting | Responsibility |
| --- | --- |
| `spring.datasource.*` | PostgreSQL connection |
| `spring.jpa.*` | JPA · Hibernate |
| `portfolio.view.*` | Default account and view limits |
| `connector.*` | Connector URL and API path |
| `portfolio.batch.*` | backend · gate · timeout · log |
| `portfolio.snapshot-refresh.*` | Snapshot Refresh |
| `slack.*` | Local View Slack functionality |

When adding or changing a configuration key, verify the properties, `@ConfigurationProperties`, README, and deployment environment-variable names together.

## Localization Infrastructure

| Item | Value |
| --- | --- |
| Default locale | English (`en`) |
| Alternate locale | Korean (`ko`) |
| Locale query parameter | `lang` |
| Persistence | `PORT_VIEW_LOCALE` cookie with a one-year maximum age |
| Message bundles | `messages.properties` · `messages_en.properties` · `messages_ko.properties` |

`LocaleConfig` provides the Spring `MessageSource`, cookie-backed locale resolution, and `LocaleChangeInterceptor`.

Task 1.3 adds infrastructure only. Existing Class B UI text remains unchanged, and no language switch is present until the later UI migration tasks.

## ECS Fargate

port-view has completed its initial ECS Fargate Service port and AWS Paper integration validation.

Deployment through GitHub Actions · OIDC · CodeBuild · ECR, Candidate validation, production promotion, Rollback, and re-promotion have been validated.

The current main-push automatic Release Workflow performs Candidate validation and promotes to production after `production` approval. Manual `workflow_dispatch` execution is also retained.

It remains in a portfolio validation state rather than operating as a continuously public service.

### Deployment Flow

1. Main push or `workflow_dispatch`
2. GitHub Actions OIDC authentication
3. CodeBuild Maven Test · Package
4. Docker Image Build and Git SHA-based ECR Push
5. Automatic Standalone Candidate Task execution
6. Candidate primary-view HTTP/UI Smoke and Research Version view validation
7. `production` approval
8. Create a new Operating Revision from the validated Image Digest
9. ECS Service Promotion
10. Production Smoke Test
11. Validate Rollback to the previous known-good Revision
12. Re-promote the latest known-good Revision

Internal implementation details such as GitHub OIDC · IAM · Security Group remain the responsibility of port-devops and are not documented here.

### Deployment Safety Standards

| Item | Value |
| --- | --- |
| Candidate execution | Standalone Task separated from the production Service |
| Candidate Batch · Step Functions | Disabled |
| Candidate Paper orders · Strategy Execution submission | Disabled |
| Candidate termination | Task stops after HTTP/UI Smoke |
| Production promotion Task Definition | Candidate Revision is not used directly |
| Operating Revision composition | Existing production environment variables plus validated Image Digest |
| Promotion Gate | After Candidate Smoke succeeds and `production` is approved |
| Rollback baseline | Save previous known-good Revision before promotion |
| Smoke scope | Candidate · Production · Rollback |

> Deployment Slack notifications are not implemented in the current scope. They are deferred until channel separation from existing pre-market · Daily validation · Daily execution · post-market notifications is reviewed.

### Container Configuration

| Item | Value |
| --- | --- |
| Spring profile | `SPRING_PROFILES_ACTIVE=aws-paper` |
| Database | `INTEREST_DB_*` |
| Step Functions | Region · general ARN · approval ARN |
| Gate | `portfolio.batch.*` |
| Snapshot | `portfolio.snapshot-refresh.*` |
| Account | Inject through environment variable · never record verbatim |

### External Exposure AS-IS

| Item | Value |
| --- | --- |
| Current validation | ECS Fargate Public IP-based |
| ALB | Not performed |
| HTTPS · Route53 | Not performed |
| Authentication | Not performed |
| Auto Scaling | Not performed |
| Blue/Green | Not performed |
| Public exposure | Not performed |
| Reconsideration condition | Public exposure or multi-user operation |

If ALB is introduced later, apply source IP restrictions or an authentication gate first.

## Slack

`SlackNotificationService` can assemble the following messages in Local View.

- Daily Batch results
- Daily Run summary
- Strategy Execution summary
- Balance and Position summary

Cross-service workflows and Lambda own primary responsibility for automated production Slack notifications.

Inject the Webhook URL through an environment variable or local configuration; never record it in the repository.

## External Dependencies

| Module | Relationship |
| --- | --- |
| `port-marketconnector` | Account · order · Fill |
| `port-interest-crawler` | Source data collection |
| `port-interest-preprocessor` | Preprocessing |
| `port_strategy_research` | Strategy Research |
| `port_strategy_decision` | Strategy Decision |
| `port_strategy_execution` | Order planning and execution |

Separate actual paths and production commands through environment variables or local configuration.

## Security

Do not record the following values verbatim in code, documentation, or logs.

- DB password
- KIS app key · app secret
- token
- Slack webhook URL
- actual account number
- AWS account-id
- actual ARN
- public IP
- broker order number
- full SHA256 image digest

Placeholders:

| Value | Placeholder |
| --- | --- |
| General sensitive information | `[REDACTED]` |
| Account number | `[REDACTED_ACCOUNT_NO]` |
| ARN | `[REDACTED_ARN]` |
| Secret ARN | `[REDACTED_SECRET_ARN]` |
| Task ARN | `[REDACTED_TASK_ARN]` |
| Public IP | `[REDACTED_PUBLIC_IP]` |
| Broker order number | `[REDACTED_BROKER_ORDER_NO]` |
| ECR image | `<ECR_IMAGE_URI>` |
| Future ALB | `<ALB_ENDPOINT>` |

The list and standards above are sufficient for security guidance in this README; no separate detailed document is maintained.

## Detailed Documentation

The following is the only detailed port-view document currently maintained.

| Document | Responsibility |
| --- | --- |
| [source-file-catalog](docs/source-file-catalog.md) | Source file responsibilities |

Structure · configuration · Daily Batch · security · refactoring standards are consolidated in the README and `docs/source-file-catalog.md`.

## Refactoring Candidates

- Separate `DailyBatchService` responsibilities
- Separate query · message assembly · delivery responsibilities in `SlackNotificationService`
- Consolidate common View util and label util code
- Clean up legacy Template and CSS structures
- Verify references before deleting unused candidates
