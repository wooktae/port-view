# CHANGELOG

This document records major changes to port-view code and documentation.

## Recording Rules

| Item | Value |
| --- | --- |
| Included scope | port-view code · configuration · views · tests · documentation changes |
| Excluded scope | Internal changes in other microservices · internal Step Functions history · one-time production logs |
| Sort order | Add the latest date at the top |
| Categories | Added · Changed · Fixed · Removed · Security |
| Detailed evidence | Reference `docs` or commits when needed |
| Sensitive information | Do not record actual account numbers · secrets · tokens · ARNs · public IPs |

## 2026-09-21 — View i18n Infrastructure

### Added

| File · Item | Change |
| --- | --- |
| `LocaleConfig.java` | Added Spring `MessageSource`, `lang` locale interceptor, English default, and cookie-backed locale persistence |
| `messages*.properties` | Added default, English, and Korean message-bundle infrastructure without migrating UI text |

### Changed

| File | Change |
| --- | --- |
| `README.md` | Documented locale defaults, query parameter, persistence, and bundle structure |
| `docs/source-file-catalog.md` | Added the locale configuration and message-bundle responsibilities |

### Validation

| Item | Result |
| --- | --- |
| Maven compile | Successful · 95 Java sources |
| CI-profile context test | Successful · 1 test, 0 failures |
| Class B UI text migration | Not performed |
| AWS · DB · broker · Slack · deployment operations | 0 occurrences |

## 2026-08-25 — Research Operating Version Selection and Champion Promotion

### Added

| File · Item | Change |
| --- | --- |
| `OperatingResearchVersionProvider.java` | Query the actual OPERATING RSCFG from the Production Step4 → Batch Job Definition command |
| `ResearchChampionPromotionService.java` | RSCFG production promotion · Job Definition Revision creation · dual State Machine transition |
| `StrategyReportController.java` | Added production-selection endpoint `POST /strategy/reports/promote` |
| `strategy_report.html` | Added "Select for Production" button for non-production Versions |

### Changed

| Item | Change |
| --- | --- |
| Default Strategy Report selection | Changed from the latest Version to the actual OPERATING Version |
| Version Dropdown | Display the current production Version as `RSCFG-xxxx · OPERATING` |
| Production promotion | Promote the selected RSCFG to a new Batch Job Definition Revision |
| Transition targets | Transition both the Production Step4 and Step4-only State Machines |

### Validation

| Item | Result |
| --- | --- |
| `RSCFG-0001` → `RSCFG-0002` promotion | Successful |
| `RSCFG-0002` → `RSCFG-0001` rollback | Successful |
| Both State Machines reference the same Revision | Confirmed |
| Batch command target RSCFG | Confirmed |
| Maven compile | Successful |

### Security

| Item | Result |
| --- | --- |
| Actual ARN · account-id · digest recorded in documentation | 0 occurrences |
| Other Research business logic duplicated in View | None |
| running execution · preflight · fail-closed · rollback guards | Retained |

## 2026-08-11 — Research Version Queries and View Release Workflow Completion

### Added

| File · Item | Change |
| --- | --- |
| `StrategyReportController.java` | Added Report path for Research Version selection |
| `ReportService.java` | Assemble Research Version list and selected Version Report |
| `ReportRepository.java` | Query `strategy_config_version` list and latest run for selected Version |
| `strategy_report.html` | Added Research Version Dropdown to the Hero area |
| `strategy-report.css` | Added Research Version Dropdown styles |
| `.github/workflows/view-codebuild.yml` | Added main-push Candidate Smoke Workflow |
| `.github/workflows/view-codebuild.yml` | Added `production` approval-gated Promotion Workflow |

### Changed

| Item | Change |
| --- | --- |
| Strategy Report | Separated the display meanings of Strategy Config Version and Engine Version |
| View deployment flow | Expanded from manual Candidate/promotion to automatic main-push Candidate validation and approval-gated Production Promotion |

### Validation

| Item | Result |
| --- | --- |
| Local `RSCFG-0001` Report query | Successful |
| Maven Test · Package | Successful |
| Main push → CodeBuild → ECR | Successful |
| Candidate Standalone Fargate execution | Successful |
| Candidate primary-view HTTP | 200 |
| Research Version UI Smoke | Successful |
| ECS Service Promotion after `production` approval | Successful |
| Production primary-view HTTP | 200 |
| Rollback to previous known-good Revision | Successful |
| Rollback Smoke | Successful |
| Latest known-good Revision re-promotion and Service Stable | Successful |

### Security

| Item | Result |
| --- | --- |
| Candidate Batch execution | Disabled |
| Candidate Step Functions execution | Disabled |
| Candidate Paper orders | Disabled |
| Candidate Strategy Execution submission | Disabled |
| Candidate and Operating configuration separation | Retained |
| Direct Production promotion of Candidate Task Definition | Prohibited |
| Newly recorded sensitive values | 0 occurrences |

## 2026-07-29 — View ECR Deployment and Candidate Promotion/Rollback Validation

### Added

| File | Change |
| --- | --- |
| `.devops/codebuild/buildspec.yml` | Added ECR login |
| `.devops/codebuild/buildspec.yml` | Added an Image Tag based on the first 12 characters of the Git Commit SHA |
| `.devops/codebuild/buildspec.yml` | Added push to the portfolio-view ECR Repository |
| `.devops/codebuild/buildspec.yml` | Added Image Digest and CodeBuild Build ID Evidence recording |

### Changed

| Item | Change |
| --- | --- |
| CodeBuild Service Role | Added least-privilege Push permissions for portfolio-view ECR only |
| Docker Image Build | Create local and ECR Tags together |
| View deployment procedure | Finalized manual production promotion after Candidate validation |
| Task Definition | Separated Candidate and production promotion definitions |

### Validation

| Item | Result |
| --- | --- |
| GitHub Actions → OIDC → CodeBuild → ECR Push | Successful |
| New Git SHA Image Tag and Digest creation | Confirmed |
| Standalone Candidate Task | RUNNING confirmed |
| Eight primary Candidate views | HTTP 200 |
| Production ECS Service promotion to new Revision | Successful |
| Eight primary production views | HTTP 200 |
| Rollback to previous known-good Revision | Successful |
| Eight primary views after Rollback | HTTP 200 |
| Final re-promotion of new Revision | RUNNING confirmed |
| Normal Candidate Task termination | Confirmed |

### Security

| Item | Result |
| --- | --- |
| Candidate Batch execution | Disabled |
| Candidate Step Functions execution | Disabled |
| Candidate Paper orders | Disabled |
| Candidate Strategy Execution submission | Disabled |
| Candidate and production configuration separation before promotion | Confirmed |
| MarketConnector EC2 | Not used |
| Direct DB connection and DDL · DML | Not performed |
| Slack deployment notification | Not sent |
| Newly recorded sensitive values | 0 occurrences |

## 2026-07-24 — View CodeBuild CI and CI Context Test Isolation

### Added

| File | Change |
| --- | --- |
| `.devops/codebuild/buildspec.yml` | Added View CodeBuild buildspec |
| `.github/workflows/view-codebuild.yml` | Added workflow that executes CodeBuild through `workflow_dispatch` |
| `src/test/resources/application-ci.properties` | Added H2-based `ci` profile · disabled external execution and Slack |
| `pom.xml` | Added test-scope H2 dependency for CI context smoke only |

### Changed

| File | Change |
| --- | --- |
| `PortViewApplicationTests.java` | Applied `@ActiveProfiles("ci")` to `contextLoads` · removed external DB dependency |
| `.devops/codebuild/buildspec.yml` | Improved CodeBuild shell-state retention and LF handling |
| `.gitattributes` | Added LF enforcement for `*.yml` · `*.yaml` · `.gitattributes` |
| `mvnw` | Added executable permission for CodeBuild execution |
| `.gitignore` | Added ignore rule for `/evidence/` |

### Security

| Item | Result |
| --- | --- |
| External execution in `ci` profile | Snapshot · order · strategy · batch · Slack disabled |
| Actual AWS · DB · broker · KIS · Slack execution | 0 occurrences |
| Git write commands | 0 occurrences · only read-only `log` · `show` · `status` · `diff` used |
| Newly recorded sensitive values | 0 occurrences |

## 2026-07-22 — port-view Documentation Standards Restructuring

### Changed

| Item | Value |
| --- | --- |
| `AGENTS.md` | Removed `.kiro`-specific rules and rewrote as port-view-specific working rules |
| Work scope | Clarified standards for Java · configuration · Thymeleaf · CSS · tests · documentation · Docker |
| Responsibility boundary | Limited to query · approval · Step Functions trigger UI |
| Execution backends | Distinguished Fargate `aws-stepfunctions` from `local-file` for local validation and recovery |
| Paper order gates | Documented separation between Steps 1~11 safe triggers and Steps 12~17 approval triggers |
| DB standards | Documented `view_app` · `marketconnector_app` responsibilities and schema-per-domain cautions |
| Documentation standards | Defined two-column default for new standalone tables · local edits · no long cells · UTF-8 without BOM |
| `AGENTS.md` assessment | Confirmed as independently sufficient for port-view code and documentation work |
| `README.md` | Removed links to deleted docs · reduced detailed-document list to files that exist |
| `docs/source-file-catalog.md` | Reduced Documents list to four maintained documents · removed references to deleted documents |
| Documentation structure | Simplified around README · CHANGELOG · `docs/source-file-catalog.md` |
| `CHANGELOG.md` | Removed duplicate history and repetitive Notes; reorganized around key changes by date |

### Removed

| Item | Value |
| --- | --- |
| Detailed docs | Removed architecture · configuration · daily-batch · security · refactoring · unused-candidate documents |
| worklog | Removed `docs/worklog` directory and date-specific files · stopped creating new files |

### Security

| Item | Result |
| --- | --- |
| Code changes | None |
| AWS · DB · broker · KIS · Slack execution | 0 occurrences |
| Git write commands | 0 occurrences |
| Newly recorded sensitive values | 0 occurrences |

## 2026-07-01 — View Responsibility Boundary and Fargate Documentation

### Added

| File | Change |
| --- | --- |
| `README.md` | Added View responsibility boundary and ECS Fargate deployment perspective |
| `docs/architecture.md` | Documented query flow and Step Functions `StartExecution` trigger flow |
| `docs/configuration.md` | Documented Fargate safe defaults and ARN placeholders |
| `docs/daily-batch.md` | Documented safe/approval endpoints and payload type consistency |
| `docs/worklog/2026-07-01.md` | Recorded port-view documentation update work |

### Changed

| Item | Value |
| --- | --- |
| View responsibility | Query · approval · trigger UI |
| Actual execution responsibility | Step Functions · EventBridge Scheduler · ECS · SSM · AWS Batch · Lambda |
| Fargate safe defaults | `aws-stepfunctions` · local-file disabled · Paper orders disabled · maximum Step 11 |
| Local paths | Not used in the Fargate production path |

### Security

| Item | Result |
| --- | --- |
| Application code changes | None |
| AWS · DB · external API execution | 0 occurrences |
| Newly recorded sensitive values | 0 occurrences |

## 2026-06-29 — AWS Step Functions Backend and Approved Execution UI

### Added

| Item | Value |
| --- | --- |
| Execution backend | `aws-stepfunctions` mode |
| Safe endpoint | `POST /daily-batch/aws-stepfunctions/start-range` |
| Approval endpoint | `POST /daily-batch/aws-stepfunctions/start-approval-range` |
| View | Steps 1~11 safe button and Steps 12~17 approval button |
| Configuration | Separated general workflow ARN and approval workflow ARN |
| Local tools | Separated Local File and Step Functions wrappers |

### Changed

| File · Area | Change |
| --- | --- |
| `pom.xml` | Added AWS SDK v2 Step Functions dependency |
| `DailyBatchProperties` | Added backend · ARN · execution gate configuration |
| `StepFunctionsDailyBatchExecutionService` | Separated safe and approval execution paths |
| `DailyBatchController` | Added safe and approval endpoint handling |
| `daily_batch.html` | Display execution buttons by backend and gate |
| `application-aws-paper.properties` | Added Step Functions environment-variable placeholders |
| StartExecution payload | Distinguished boolean · numeric · string types |
| Run date | Added Asia/Seoul-based `runDate` |

### Fixed

| Problem | Resolution |
| --- | --- |
| `States.Runtime` after Step 11 because `runDate` was missing | Added KST-based `runDate` to View input |
| Approval boolean passed as a string | Changed to JSON boolean |
| Step order passed as a string | Changed to JSON numeric |
| Approval button called the general workflow ARN | Used approval-specific ARN |
| Safe-only gate remained in local wrapper | Separated wrappers by backend |

### Validation

| Item | Result |
| --- | --- |
| Steps 1~11 | Passed approval-required path after `StartExecution` |
| Steps 12~17 | Approval gate passed and execution succeeded |
| Orders created | No new broker orders in the validation run |
| Reference commit | `e72de6f` |

### Security

| Item | Result |
| --- | --- |
| Account number exposure in view | Redaction retained |
| ARN · account-id recorded in documentation | Placeholders only |
| Additional production execution | 0 occurrences during documentation cleanup |

## 2026-06-29 — Snapshot Refresh and Daily Batch View Improvements

### Added

| Item | Value |
| --- | --- |
| AWS Paper Local View | Documented execution prerequisites and starter usage |
| Snapshot Refresh | Documented Dashboard · Balance · Positions behavior |
| Daily Batch UI | Improved execution history cards and status display |

### Changed

| Item | Value |
| --- | --- |
| `aws-paper` profile | Organized Snapshot Refresh settings around environment overrides |
| Spring datasource | Uses `view_app` |
| Connector subprocess | Uses `marketconnector_app` |
| Daily Batch view | Improved Step Logs · Payload · execution mode display |

### Fixed

| Problem | Resolution |
| --- | --- |
| `connector_balance.py` executed as `view_app` | Separated subprocess DB user as `marketconnector_app` |
| Insufficient execution-history and status-pill styling | Improved view CSS |

## 2026-05-28 — Source File Catalog and Documentation Improvements

### Added

| Item | Value |
| --- | --- |
| `docs/source-file-catalog.md` | Documented responsibilities of primary Java · configuration · documentation files |
| Source documentation | Added Korean comments for Connector · Dashboard · Position · Report areas |

### Changed

| Item | Value |
| --- | --- |
| `README.md` | Added Source File Catalog link |
| Documentation scope | Organized changes since 2026-05-27 |

## 2026-05-27 — schema-per-domain Transition Documentation

### Added

| Item | Value |
| --- | --- |
| Database | Single PostgreSQL database `portfolio` |
| Schema | Domain-specific schema structure |
| View SQL | Queries based on Hikari `search_path` |

### Changed

| Item | Value |
| --- | --- |
| Datasource configuration | Based on `INTEREST_DB_*` environment variables |
| Default DB name | `portfolio` |
| Validated views | Dashboard · Balance · Holdings · Strategy Plan · Daily Batch · Report |

## 2026-05-26 — Block Automated Buy Before Market Open

### Changed

| Item | Value |
| --- | --- |
| Target Step | `DAILY_AUTO_BUY` |
| Time threshold | Before 09:00 KST |
| Handling | Deferred without calling the actual automated Buy script |
| Result type | `NO_TARGET` |

## 2026-05-23 — Unused View Structure Cleanup

### Added

| Item | Value |
| --- | --- |
| `docs/unused-view-file-candidates.md` | Analysis of unused View file candidates |

### Changed

| Item | Value |
| --- | --- |
| Template · CSS | Organized into `templates/pages` · `static/css/pages` structure |
| Controller | Removed legacy URLs linked to deleted views |
| Service · Repository | Removed unused methods and implementations |
| DTO | Organized into feature-specific subpackages |
| Configuration | Removed unused `connector.api.*` and selected `portfolio.view.*` settings |

### Fixed

| Problem | Resolution |
| --- | --- |
| Possible return of a deleted template | Removed related Controller and View constants |
| Compile consistency | `mvnw.cmd clean compile` successful |
