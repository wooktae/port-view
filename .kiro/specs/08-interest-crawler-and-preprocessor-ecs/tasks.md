# Tasks — 08-interest-crawler-and-preprocessor-ecs

These tasks are the minimal checklist that decomposes the [`./design.md`](./design.md) / [`./requirements.md`](./requirements.md) decisions into progress units. The operator performs the actual AWS / docker / ecs / iam work directly, and Kiro handles only document / procedure / validation-item organization. For per-date detailed results, see [`./operation-notes.md`](./operation-notes.md) (2026-06-10 · 2026-06-12 · 2026-06-13 · 2026-06-15 · 2026-06-16 · 2026-06-17 · 2026-06-20 · 2026-06-21 · 2026-06-22).

**Hybrid execution model summary** — for detailed results, see operation-notes.

- 2026-06-12 separation confirmed: non-GUI crawler = ECS Fargate Task candidate maintained / KRX GUI crawler = Windows EC2 worker / preprocessor = ECS Fargate Task
- 2026-06-13 automation entry point confirmed: SSM RunCommand → `schtasks /Run` → Windows Scheduled Task → Administrator interactive session → `run_krx_worker_daily.ps1` / ECS crawler rev6 Selenium Chrome smoke passed → hybrid first completion
- 2026-06-15 stale raw data identified: rev6 is smoke-only / first identification of insufficient raw freshness due to the absence of a non-GUI daily operation path (R-DATA-009 / R-DATA-010)
- 2026-06-16 hybrid structure complete: `interest_crawler_daily_nongui.py` new + ECS Task Definition rev7 (`paper-20260616-nongui`) RunTask exitCode 0 / 8 steps SUCCESS / 7 non-GUI raw + 2 KRX raw loaded up to the previous trade day / Windows EC2 worker Autologon + Administrator interactive session + Scheduled Task + SSM trigger automation success
- Decision lock: OD-MS-022 new + OD-MS-011 / OD-MS-015 / OD-MS-020 first demonstration
- Follow-up: Preprocessor ECS re-run (task 77) / Backend AWS E2E dry-run resumption (OD-MS-021)

## Task Section Overview

Per-section complete / carryover status summary. For detailed basis, see each section.

| Section | Range | Status | Note |
|---|---|---|---|
| §1 ECR Repository | task 1~5 | 🟢 Complete | 2026-06-10 |
| §2 Dockerfile inspection | task 6~9 | 🟢 Complete | 2026-06-10 |
| §3 Local build | task 10~13 | 🟢 Complete | 2026-06-10 |
| §4 ECR Push | task 14~17 | 🟢 Complete | 2026-06-10 |
| §5 ECS Cluster/Role/Log Group | task 18~24 | 🟢 Complete | 2026-06-10 |
| §6 Preprocessor single-run | task 25~28 | 🟢 Complete | 2026-06-10 |
| §7 Crawler outbound risk | task 29~32 | 🟠 Partial complete | task 30 / 31 outbound follow-up (58) |
| §8 NAT-free re-confirmation | task 33~34 | 🟢 Complete | — |
| §9 Safety constraints / deliverable limitation | task 35~40 | 🟢 Complete | — |
| §10 Completion criteria | task 41~43 | 🟢 Complete | — |
| §11 Windows EC2 worker (KRX GUI) | task 44~52 | 🟢 Complete | 2026-06-12 |
| §12 Follow-up tasks | task 53~60 | 🟠 Partial | 53/57 complete / 54·55·56·58·59·60 follow-up |
| §13 SSM + ECS smoke | task 61~69 | 🟢 Complete | 2026-06-13 |
| §14 E2E dry-run / re-judgment | task 70~81 | 🟢 Complete | 2026-06-15 / 77·78 follow-up |
| §15 Resolve crawler data non-collection | task 82~92 | 🟢 Complete | 2026-06-16 |
| §16 Strengthen Step 2 success judgment | task 100~110 | 🟢 Complete | 2026-06-20 / 21 |
| §17 Daily run re-validation | task 112~115 | 🟢 Complete | 2026-06-22 |

**Remaining (open)** — task 54 / 55 / 56 / 59 / 78 / 111. For detailed basis, see each section and the [`./operation-notes.md`](./operation-notes.md) Open Risks & Next Checks.

## 1. ECR Repository Creation Preparation (§2)

- [x] 1. Organize the `portfolio-interest-crawler` repository creation criteria (name / region / scan on push) → 2026-06-10 operator direct creation complete (§2.1)
- [x] 2. Organize the `portfolio-interest-preprocessor` repository creation criteria (name / region / scan on push) → 2026-06-10 operator direct creation complete (§2.1)
- [x] 3. Specify the repository URI placeholder notation (`<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-crawler:<image-tag>`, `<account-id>.dkr.ecr.<region>.amazonaws.com/portfolio-interest-preprocessor:<image-tag>`) (§2.1, §11)
- [x] 4. Specify separating whether to split the common base image as a follow-up review (§2.2)
- [x] 5. Organize the ECR repository environment-non-separation policy (paper / live same artifact, 6-item separation) → 2026-06-10 paper / live non-separation confirmed (§2.3)

## 2. Dockerfile Baseline Inspection (§3)

- [x] 6. Organize the crawler Dockerfile existence / Selenium·Chrome·chromedriver dependency inspection items → 2026-06-10 operator direct new creation (base `python:3.13-slim`, Chromium / chromedriver included, command `python interest_crawler_daily.py`) (§3.2)
- [x] 7. Organize the preprocessor Dockerfile existence / 5 RDS env compatibility inspection items → 2026-06-10 operator direct new creation (base `python:3.13-slim`, command `python pre_daily.py`) (§3.1)
- [x] 8. Organize the requirements install method / entrypoint·CMD / environment-variable injection method inspection items → 2026-06-10 new creation of the two MS requirements.txt (preprocessor: `psycopg2-binary` / `requests`. crawler: `beautifulsoup4` / `pandas` / `psycopg2-binary` / `requests` / `selenium` / `yfinance`) (§3.1, §3.2)
- [x] 9. Specify the follow-up spec / operator stage responsibility separation when a Dockerfile is absent·defective → 2026-06-10 the two MS Dockerfile / requirements resolved by operator direct new creation (§3)

## 3. Local Image Build Organization (§4)

- [x] 10. Organize the preprocessor-first build procedure → 2026-06-10 `portfolio-interest-preprocessor:paper-20260610` / `paper-latest` build complete (§4.1)
- [x] 11. Organize the crawler build procedure → 2026-06-10 `portfolio-interest-crawler:paper-20260610` / `paper-latest` build complete (a larger image than preprocessor due to Chromium inclusion) (§4.1)
- [x] 12. Organize the 5 build-failure-cause candidates (requirements / Python ver / import / system pkg / Selenium) → 2026-06-10 found·fixed 1 Dockerfile line-continuation character (`\`) error (not a PowerShell backtick) (§4.2)
- [x] 13. Specify that the crawler Selenium dependency defect does not block the preprocessor flow → 2026-06-10 crawler build-stage risk first-resolved (build success) / 0 preprocessor flow blocks (§4.1)

## 4. ECR Push Organization (§5)

- [x] 14. Organize the aws-paper image tag criterion (`paper-<yyyymmdd>` / `paper-latest` placeholder) decision basis → 2026-06-10 decided / used (§5.1, §5.2)
- [x] 15. Organize the preprocessor push procedure → 2026-06-10 `paper-20260610` / `paper-latest` push complete (§5.1)
- [x] 16. Organize the crawler push procedure or 5 push-failure-cause candidates → 2026-06-10 `paper-20260610` / `paper-latest` push complete (§5.3)
- [x] 17. Organize the image digest confirmation procedure (record only the `<image-digest>` placeholder) → 2026-06-10 the operator confirmed the digest / the actual value is not recorded in this spec's deliverables (§5.1, §11)

## 5. ECS Cluster / Role / Log Group Preparation (§6)

- [x] 18. Organize the aws-paper ECS Cluster creation criterion (name / Fargate) → 2026-06-10 `portfolio-paper-cluster` creation / confirmation, Fargate capacity provider, ACTIVE status confirmed (§6.1)
- [x] 19. Organize the Task Execution Role permission matrix (ECR pull / Logs write / Secrets·SSM read) → 2026-06-10 `portfolio-paper-ecs-task-execution-role` creation, trust `ecs-tasks.amazonaws.com`, managed `AmazonECSTaskExecutionRolePolicy` + preprocessor DB secret read inline policy (limited to a single secret ARN) (§6.2)
- [x] 20. Organize the crawler / preprocessor Task Role separation (separate service prefix) → 2026-06-10 `portfolio-paper-preprocessor-task-role` / `portfolio-paper-crawler-task-role` creation·confirmation (§6.1, §10.2)
- [x] 21. Organize the criterion for pre-creating 2 CloudWatch Log Groups → 2026-06-10 `/portfolio/paper/preprocessor` / `/portfolio/paper/crawler` creation + retention 14 days set (§6.1)
- [x] 22. Organize the RDS-connection SG-pass policy (`sg-preprocessor-task` → `sg-rds-postgres` 5432) → 2026-06-10 preprocessor task SG → RDS PostgreSQL SG 5432 inbound allowed confirmed (§7.2)
- [x] 23. Re-confirm the Resource·Action wildcard prohibition policy → 2026-06-10 preprocessor DB secret read policy limited to a single secret ARN / 0 wildcards (§6.3)
- [x] 24. Organize the Task Execution Role / Task Role responsibility separation policy (Task Definition `secrets` first) → 2026-06-10 this first validation passed with the Task Definition `secrets` injection method (§6.4)

## 6. Preprocessor Single-Run Validation Organization (§7)

- [x] 25. Organize the awsvpc / public subnet / `assignPublicIp = ENABLED` confirmation items → 2026-06-10 preprocessor RunTask validation passed (§7.1)
- [x] 26. Organize the RDS connection success inspection items on the `preprocessor_app` basis → 2026-06-10 after resolving 1st host missing / 2nd sequence permission insufficient, connection·execution success (§7.2, §7.3)
- [x] 27. Organize the CloudWatch Logs output / Task exit code 0 inspection items → 2026-06-10 lastStatus `STOPPED` / exitCode `0` / `PREPROCESSOR PIPELINE END` confirmed (§7.2)
- [x] 28. Organize the 5 execution-failure-cause candidates (env / Secret permission / SG / VPC Endpoint / image) → 2026-06-10 cases accumulated: (a) Secrets Manager JSON `host` key missing → psycopg2 tried the Unix socket `/var/run/postgresql/.s.PGSQL.5432` → resolved by re-creating the secret. (b) `permission denied for sequence pre_marketbreadth_daily_feature_id_seq` → resolved by granting `preprocessor_app` USAGE / SELECT on 2 residual public schema sequences (§7.4)

## 7. Crawler External Outbound Risk Separate Organization (§8)

- [x] 29. Organize the Selenium / Chrome need first-review items → 2026-06-10 Chromium / chromedriver included in the Crawler Dockerfile / build stage passed. 2026-06-12 validation result: KRX GUI-dependent collection (KRX program / KRX shortsell) separation confirmed to the Windows EC2 worker (§8.1)
- [ ] 30. Organize the KRX / Naver / yfinance outbound reachability first-review items → partial complete: 2026-06-12 KRX program / KRX shortsell outbound reachability·collection success on the Windows EC2 worker (each date 2026-06-09 ~ 2026-06-11 loading confirmed). 2026-06-13 ECS / Fargate `portfolio-paper-interest-crawler:6` Selenium Chrome smoke connected to example.com / Naver Finance (`SELENIUM CHROME SMOKE SUCCESS` confirmed). The KRX outbound ECS Fargate path validation and yfinance ECS Fargate path validation, and the non-GUI crawler actual Task Definition separation, are carried over to a follow-up (task 58) (§8.1)
- [ ] 31. Organize the public subnet + `assignPublicIp` reachability validation items → partial complete: 2026-06-10 NAT-free self-validation passed via preprocessor RunTask. 2026-06-13 in the ECS crawler smoke RunTask (public-a / public-b subnet + `sgroup-crawler-tasks` SG + `assignPublicIp = ENABLED`), external outbound reachability first-passed (exitCode 0 / Naver Finance connection success). The non-GUI crawler actual Task Definition separation is carried over to a follow-up (task 58) (the KRX GUI path is separated to the EC2 worker) (§8.2, §9)
- [x] 32. Specify that operational stabilization is out of this spec's scope / follow-up spec·follow-up phase responsibility separation → 2026-06-10 100% crawler stabilization kept out of today's scope. 2026-06-12 KRX GUI-dependent collection reached a first-operable state on the EC2 worker / full automation is a follow-up (§8)

## 8. NAT-free Policy Re-confirmation (§9)

- [x] 33. Organize the 0-NAT-Gateway-usage inspection items → 2026-06-10 NAT-free first-validation via preprocessor RunTask public subnet + `assignPublicIp = ENABLED` pass (§9.1)
- [x] 34. Organize the OD-NET-001 / OD-NET-002 re-confirmation flow when a NAT Gateway is found (§9.1)

## 9. Safety Constraints / Deliverable Limitation Re-confirmation (§11)

- [x] 35. Re-confirm 0 actual AWS / ECR / ECS / IAM changes / operator-direct-only → 0 AWS resource changes due to Kiro's work on this spec. For operator-direct items, see the [`./operation-notes.md`](./operation-notes.md) 2026-06-10 section (§11.1)
- [x] 36. Re-confirm no 8 MS code / docs / packaging / Dockerfile modification → 0 changes due to Kiro's work on this spec. By operator direct work, the Dockerfile / requirements.txt of port-interest-preprocessor / port-interest-crawler were newly created (see operation-notes) (§11.1)
- [x] 37. Re-confirm 0 external calls / crawls / orders / RDS DDL·DML → 0 calls due to Kiro's work on this spec. For the 2 RDS sequence GRANTs and the preprocessor pipeline RunTask result the operator performed directly, see operation-notes (§11.1)
- [x] 38. Re-confirm 0 sensitive-information plaintext records (`[REDACTED]` / placeholder) (§11.1)
- [x] 39. Re-confirm this 08 initial-document phase deliverables are limited to the 3 requirements.md / design.md / tasks.md → operation-notes.md is authored separately for accumulating operator execution results (consistent with R11) (§11.2)
- [x] 40. Re-confirm runbook.md / validation-checklist.md / CHANGELOG.md / WORKLOG.md are authored separately after operator execution → operation-notes.md newly created from the 2026-06-10 operator execution result. runbook / validation-checklist / CHANGELOG / WORKLOG are a follow-up stage (§11.2)

## 10. Completion Criteria

- [x] 41. All 3 requirements.md / design.md / tasks.md exist
- [x] 42. The key decision points handed off to the follow-up operator stage (ECR repo / image tag / Cluster·Role·Log Group / preprocessor single-run skeleton) are specified in design.md
- [x] 43. The crawler Selenium / Chrome / KRX·Naver·yfinance risk is separately split into §8

## 11. Windows EC2 worker-based KRX GUI-dependent collection (added 2026-06-12)

- [x] 44. Organize the EC2 worker start / RDP connection / work directory / venv / Python 3.13.5 / IAM Role recognition inspection items → 2026-06-12 inspection passed ([`./operation-notes.md`](./operation-notes.md) §1)
- [x] 45. Organize the RDS Secret-based DB environment-variable injection procedure (`load-crawler-db-env.ps1`) → 2026-06-12 5 env injection based on `/portfolio/paper/rds/crawler-app` success / password value not output (§1, §2)
- [x] 46. Organize the junction procedure for the download path mismatch (`C:\Users\USER\Downloads` vs `C:\Users\Administrator\Downloads`) → 2026-06-12 CSV recognition success after applying the junction ([`./operation-notes.md`](./operation-notes.md) §3)
- [x] 47. Organize the KRX program standalone collection validation (`interest_program.py`) procedure → 2026-06-12 1 record loaded each for 2026-06-09 / 2026-06-10 / 2026-06-11 / `interest_program_raw` 543 → 546 confirmed ([`./operation-notes.md`](./operation-notes.md) §4)
- [x] 48. Organize the KRX shortsell standalone collection validation (`interest_shortsell.py`) procedure → 2026-06-12 349 records loaded each for 2026-06-09 / 2026-06-10 / 2026-06-11 / `interest_shortsell_raw` 189158 → 190205 confirmed ([`./operation-notes.md`](./operation-notes.md) §5)
- [x] 49. Organize the Korean literal / encoding validation procedure (BOM removal + utf-8 save) → 2026-06-12 decided to adopt the Python patch script method ([`./operation-notes.md`](./operation-notes.md) §6)
- [x] 50. Organize the KRX Secrets Manager integration (`/portfolio/paper/krx/crawler-login`, `username` / `password`) procedure → 2026-06-12 Secret new creation / IAM permission added / `load-krx-env.ps1` injection / login success ([`./operation-notes.md`](./operation-notes.md) §7)
- [x] 51. Organize the EC2 worker daily wrapper (`run_krx_worker_daily.ps1`) procedure → 2026-06-12 wrapper first run / `[Collected Date] None` no-op normal completion on re-run / log file format `krx_worker_daily_yyyyMMdd_HHmmss.log` confirmed ([`./operation-notes.md`](./operation-notes.md) §8)
- [x] 52. The hybrid execution model classification decision that KRX GUI-dependent collection = Windows EC2 worker / non-GUI crawler = ECS Fargate Task candidate maintained / preprocessor = ECS Fargate Task → 2026-06-12 confirmed by operator decision ([`./operation-notes.md`](./operation-notes.md) §9, [`./design.md`](./design.md) Hybrid execution model)

## 12. Follow-up tasks (carryover / separated)

- [x] 53. Organize the SSM RunCommand-based EC2 worker unattended-execution procedure → partial complete (2026-06-13). The way SSM RunCommand directly executes the wrapper in SYSTEM Session 0 was judged unsuitable for KRX GUI login and not adopted. The first automation method was confirmed as the SSM RunCommand → `schtasks /Run /TN Portfolio-KRX-Worker-Daily` → Windows Scheduled Task → Administrator interactive session → `run_krx_worker_daily.ps1` flow ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §1 ~ §4)
- [ ] 54. Organize the EventBridge Scheduler → SSM RunCommand integration procedure (follow-up separated — `schtasks /Run` trigger periodic execution)
- [ ] 55. Organize the mixed ECS Task + EC2 worker orchestration skeleton in Step Functions (follow-up separated)
- [ ] 56. Organize the EC2 worker log collection procedure based on CloudWatch Logs Agent or SSM output (follow-up separated)
- [x] 57. Organize the automatic DB validation output addition in the wrapper (`interest_program_raw` / `interest_shortsell_raw` per-date row count output) procedure → 2026-06-21 complete
  - Structure: `step-02-interest-crawler.ps1` computes `ExpectedKrxRawDate` (the previous business day relative to RunDate) → calls SSM `INTEREST_CRAWLER_KRX_DB_VALIDATE` → runs `interest_krx_raw_validate_daily.py --expected-date <yyyy-mm-dd>` → validates row_count + `max(trade_date)`
  - fail-closed: `KrxDbValidationCommandId` included in the step result / Step 2 fail on non-zero exit or row_count 0
  - 2026-06-21 standalone validation: SSM commandId `2279c6d7-2da6-4317-9c10-7cc77374b317` / interest_program_raw expected=`2026-06-19` max_date=`2026-06-19` expected_count=`1` / interest_shortsell_raw expected=`2026-06-19` max_date=`2026-06-19` expected_count=`349` / R-AUTO-007 mitigation first demonstration
  - 2026-06-22 Daily run re-validation: 0 regressions / validation passed on the `ExpectedKrxRawDate 2026-06-19` basis / Step 2 SUCCESS ([`./operation-notes.md`](./operation-notes.md) 2026-06-21 §4 / 2026-06-22 §1)
- [ ] 58. ECS / Fargate non-GUI crawler actual Task Definition separation (confirm the inventory + separate the smoke revision and the operation Task Definition) (follow-up separated)
- [ ] 59. Specify the EC2 worker stop procedure after work completion (idle cost reduction) (follow-up separated)
- [ ] 60. KRX GUI collection headless refactoring is maintained only as a long-term candidate (the current decision is to use the EC2 worker) (long-term candidate)

## 13. SSM RunCommand automation + ECS crawler smoke (added 2026-06-13)

- [x] 61. Organize the SSM Managed Node registration / `AWS-RunPowerShellScript` availability / `hostname` · `whoami` · PowerShell 5.1 / `C:\portfolio` access inspection items → 2026-06-13 inspection passed (`whoami` = `nt authority\system` confirmed) ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §1)
- [x] 62. Organize the case where direct wrapper execution via SSM RunCommand is unsuitable for KRX GUI login due to SYSTEM Session 0 / SessionId 0 / SessionId 2 separation → 2026-06-13 direct-execution method rejected ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §2)
- [x] 63. Organize the Windows Scheduled Task `Portfolio-KRX-Worker-Daily` registration (on the Administrator interactive session basis / runs `C:\portfolio\run_krx_worker_daily.ps1` / `Start-ScheduledTask` first validation) procedure → 2026-06-13 registration·execution success ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §3)
- [x] 64. Organize the SSM RunCommand → `schtasks /Run /TN Portfolio-KRX-Worker-Daily` trigger validation procedure in the RDP-closed state → 2026-06-13 `SUCCESS: Attempted to run the scheduled task` / Task State `Running` → `Ready` / wrapper latest log `krx_worker_daily_20260613_021904.log` / `[Collected Date] None` idempotent normal completion confirmed ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §4)
- [x] 65. Specify the separation of the ECS crawler Task Definition `portfolio-paper-interest-crawler` revision 6's Selenium Chrome smoke command (separate from the actual daily crawler entrypoint) → 2026-06-13 confirmed / log stream prefix `ecs-selenium-chrome-smoke` ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §5)
- [x] 66. Organize the ECS / Fargate Selenium Chrome smoke RunTask (public-a / public-b + `sgroup-crawler-tasks` + `assignPublicIp = ENABLED`) procedure → 2026-06-13 exitCode 0 / `SELENIUM CHROME SMOKE SUCCESS` / `DRIVER QUIT` / `SELENIUM CHROME SMOKE END` confirmed. example.com connection success / Naver Finance TITLE `Npay 증권` confirmed ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §5)
- [x] 67. First classification of the non-GUI crawler candidate inventory (`requests` / `yfinance`-centric = ECS Fargate candidate maintained / `interest_crawler_daily.py` is excluded from ECS standalone execution because it calls both KRX GUI-dependent files and non-GUI files simultaneously) → 2026-06-13 classification organized / the actual operation Task Definition separation is task 58 (follow-up) ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §5)
- [x] 68. Hybrid execution model first-completion judgment (KRX program / shortsell = EC2 worker / non-GUI crawler = ECS Fargate candidate / preprocessor = ECS Fargate / Selenium Chrome smoke passed / public subnet + `assignPublicIp` outbound passed / CloudWatch Logs confirmable) → 2026-06-13 judgment complete ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §6)
- [x] 69. Re-confirm this date's safety / document-record inspection (0 plaintext records of secret value / password / actual ARN / image digest / task ARN / instance-id / account-id / 8 MS unmodified / 0 external calls) → 2026-06-13 passed ([`./operation-notes.md`](./operation-notes.md) 2026-06-13 §7)

## 14. Backend AWS E2E dry-run first / Interest Crawler status re-judgment (added 2026-06-15)

This section decomposes and records, in task units, the Backend AWS E2E dry-run first inspection and the Interest Crawler status re-judgment the operator performed directly on 2026-06-15. The hybrid execution model first-completion judgment at the time of this spec (2026-06-13 §13) itself is kept as-is, and KRX GUI worker operable / non-GUI daily operation incomplete / insufficient raw freshness validation / preprocessor ECS execution success (data freshness constraint) are separated as follow-ups. For detailed results, see [`./operation-notes.md`](./operation-notes.md) 2026-06-15 §1 ~ §8. This date's decision lock is consistent with OD-MS-020 / OD-MS-021.

- [x] 70. Re-confirm the KRX GUI worker operable-state first completion (Windows EC2 worker / Scheduled Task / wrapper / KRX login / program / shortsell single collection / DB Secret · KRX Secret loading / download path junction / log file creation) → 2026-06-15 KRX worker re-run result `KRX already logged in` / `KRX Login Ready` / `[Collected Date] None` idempotent normal completion confirmed / `interest_program_raw` · `interest_shortsell_raw` latest date 2026-06-12 confirmed (loaded up to the previous trade day as of Monday 2026-06-15) ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §3)
- [x] 71. Organize the KRX raw latest-date SQL inspection procedure → 2026-06-15 re-confirmed the DB state with `check_program_rows.py` / `check_shortsell_rows.py`. KRX raw loaded up to the previous trade day normally ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §3)
- [x] 72. Separate the non-GUI Interest Crawler daily operation Task Definition / command → 2026-06-16 complete. For detailed results, see [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §1, §2
  - `interest_crawler_daily_nongui.py` newly created (KRX GUI series imports excluded / non-GUI series imports included)
  - `paper-20260616-nongui` build + ECR push
  - Task Definition `portfolio-paper-interest-crawler:7` registration: command `["python", "interest_crawler_daily_nongui.py"]` / log stream prefix `ecs-crawler-nongui-daily`
  - RunTask success: exitCode 0 / about 9 min 51 sec / all steps SUCCESS (8 non-GUI: `interest_news` · `interest_agency` · `interest_foreignindex` · `interest_commodity` · `interest_macroeconomic` · `interest_price` · `interest_investorflow` · `interest_marketbreadth`)
- [x] 73. Organize the periodic items for the non-GUI raw latest-date SQL inspection → 2026-06-16 first recovery loading result confirmed. 7 non-GUI raw (`interest_news_raw` 2026-06-16 / `interest_agency_raw` 2026-06-16 / `interest_price_raw` 2026-06-15 / `interest_investorflow_raw` 2026-06-15 / `interest_marketbreadth_raw` 2026-06-15 / `interest_commodity_raw` 2026-06-15 / `interest_foreignindex_raw` 2026-06-15) recovered loading up to the previous trade day. Periodic automation is separated as a follow-up (task 78 / consistent with R-DATA-009 / R-DATA-010 mitigation) ([`./operation-notes.md`](./operation-notes.md) 2026-06-16 §3)
- [x] 74. Organize the raw freshness recovery work → 2026-06-16 complete / R-DATA-010 mitigation first demonstration / for detailed results, see [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §3, §4

  The following raw freshness recovered via the non-GUI ECS / Fargate rev7 RunTask (max date moved / row count increased):

  | raw table | max date | row count |
  |---|---|---|
  | `interest_price_raw` | 2026-06-08 → 2026-06-15 | 1,207,904 → 1,209,624 |
  | `interest_investorflow_raw` | 2026-06-08 → 2026-06-15 | 274,204 → 275,949 |
  | `interest_marketbreadth_raw` | 2026-06-08 → 2026-06-15 | 4,788 → 4,793 |
  | `interest_commodity_raw` | 2026-06-08 → 2026-06-15 | 29,132 → 29,162 |
  | `interest_foreignindex_raw` | 2026-06-08 → 2026-06-15 | 33,738 → 33,766 |
  | `interest_news_raw` | 2026-06-11 → 2026-06-16 | 68,881 → 71,614 |
  | `interest_agency_raw` | 2026-06-11 → 2026-06-16 | 49,018 → 49,044 |
  | `interest_macroeconomic_raw` | 2026-06-08 → 2026-06-15 | 75,742 → 75,777 |

  The `interest_foreignindex_raw` HANGSENG · NIKKEI225 · SHANGHAI partial NULL Data is separated as a non-blocker candidate. Via the KRX EC2 worker, `interest_program_raw` 547 → 548 / `interest_shortsell_raw` 190,554 → 190,903 additional loading.
- [x] 75. Organize the preprocessor ECS RunTask single-run (Backend E2E dry-run #3) procedure → 2026-06-15 cluster `portfolio-paper-cluster` / task definition `portfolio-paper-interest-preprocessor:1` / FARGATE / awsvpc / public-a + public-b / `assignPublicIp = ENABLED` / `sgroup-preprocessor-tasks` / lastStatus `STOPPED` / desiredStatus `STOPPED` / stopCode `EssentialContainerExited` / container `interest-preprocessor` / exitCode 0 / execution time about 3 min 43 sec ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §4)
- [x] 76. Organize the preprocessor `updated_at` update / new feature date inspection procedure → 2026-06-15 `updated_at` 2026-06-15 11:03:55+00 (KST 2026-06-15 20:03:55) update confirmed / DB write path operation confirmed. 0 new 2026-06-15 feature dates — the cause is not a preprocessor failure but the §3 insufficient raw freshness (consistent with R-DATA-010) ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §4)
- [ ] 77. Organize the preprocessor re-run procedure — incomplete / follow-up separated. After raw freshness recovery, re-run `portfolio-paper-interest-preprocessor` → confirm exitCode 0 → confirm the feature table max date / `updated_at` → confirm whether new feature dates are created ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §8)
- [ ] 78. Automate the raw / feature freshness validation SQL after preprocessor execution — incomplete / follow-up separated (consistent with R-DATA-009 / R-DATA-010 mitigation) ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §8)
- [x] 79. Interest Crawler wording-correction decision lock — correct the existing "Interest Crawler completion: complete" / "Interest Crawler is first-complete on the hybrid execution model basis" wording to "Interest Crawler hybrid first implementation: partial complete" / "the KRX GUI worker is first-complete in an operable state" / "the ECS / Fargate crawler is smoke-validation complete" / "the non-GUI daily raw collection operation path and full raw freshness validation are a follow-up" → 2026-06-15 OD-MS-020 decision lock ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §3)
- [x] 80. Organize the Backend AWS E2E dry-run 17-step checklist → 2026-06-15 #1 CONNECTOR_BALANCE complete / #2 INTEREST_CRAWLER partial complete / #3 PREPROCESSOR execution complete (data freshness constraint) / #4 ~ #7 not started / #8 ~ #17 not started or dry-run skip planned. 0 actual BUY / SELL / `--execute` order submissions. 0 fill / position sync auto-retries. 0 aws-live work. OD-MS-021 decision lock ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §6)
- [x] 81. Organize the Secret / IAM permission separation first demonstration — when the MarketConnector EC2 role (`portfolio-paper-marketconnector-ec2-role`) attempted `/portfolio/paper/rds/preprocessor-app` `GetSecretValue`, `AccessDeniedException`. Not a failure but normal behavior consistent with OD-SEC-006 / OD-DB-008. 0 preprocessor secret read permission additions to the MarketConnector EC2 role. preprocessor DB confirmation is performed via the preprocessor ECS Task or operator local SSM Port Forwarding ([`./operation-notes.md`](./operation-notes.md) 2026-06-15 §5)

## 15. Resolve crawler data non-collection + KRX EC2 automation success (added 2026-06-16)

This section records, in task units, the following work the operator performed directly on 2026-06-16.

- (a) Diagnose the cause of crawler data non-collection
- (b) Separate the non-GUI crawler operation ECS Task Definition revision 7 + RunTask success
- (c) raw freshness recovery
- (d) Re-validate the KRX EC2 worker Autologon + Administrator console session + Scheduled Task + SSM trigger automation

Among the 2026-06-15 carryover items, task 72 (non-GUI Task Definition separation) / task 73 (non-GUI raw inspection) / task 74 (raw freshness recovery) are marked complete. Decision lock OD-MS-022 new / OD-MS-011 · OD-MS-015 · OD-MS-020 first-demonstration memo reinforcement. For detailed results, see [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §1 ~ §5. 0 actual BUY / SELL / `--execute` / fill·position sync auto-retry / aws-live work.

- [x] 82. Organize the crawler data non-collection cause diagnosis → 2026-06-16 (a) ECS / Fargate revision 6 = Selenium / Chrome smoke command (not the daily collection entrypoint) (b) the original `interest_crawler_daily.py` is unsuitable for ECS / Fargate direct execution because it includes the KRX GUI stage (c) organized the absence of a non-GUI-only orchestration / Task Definition as the core cause ([`./operation-notes.md`](./operation-notes.md) 2026-06-16 §1)
- [x] 83. Separate the non-GUI crawler actual operation Task Definition → 2026-06-16 complete (proceeded combined with task 58 / task 72). For detailed results, see [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §2
  - Python patch script: `interest_crawler_daily_nongui.py` newly created / `encoding="utf-8"` save / `py_compile` / AST import validation / confirmed the file is included inside the Docker image
  - KRX GUI series imports excluded: `interest_krx_login_new` / `interest_program` / `interest_shortsell`
  - 8 non-GUI series imports included
  - Docker rebuild + ECR push: `paper-20260616-nongui`
  - Task Definition registration: `portfolio-paper-interest-crawler:7` (FARGATE / awsvpc / cpu 1024 / memory 2048 / log group `/portfolio/paper/crawler` / log stream prefix `ecs-crawler-nongui-daily` / crawler-app DB secret injection)
- [x] 84. non-GUI crawler RunTask first-run validation → 2026-06-16 passed. For detailed results, see [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §2
  - cluster `portfolio-paper-cluster` / Task Definition `portfolio-paper-interest-crawler:7`
  - RunTask submit success / failures 0 / lastStatus `STOPPED` / stopCode `EssentialContainerExited`
  - container exitCode 0 / about 9 min 51 sec / CloudWatch log stream creation confirmed
  - all steps SUCCESS (8 non-GUI: `interest_news` · `interest_agency` · `interest_foreignindex` · `interest_commodity` · `interest_macroeconomic` · `interest_price` · `interest_investorflow` · `interest_marketbreadth`)
- [x] 85. non-GUI raw freshness recovery SQL validation → 2026-06-16 complete / for per-raw-table freshness confirmation, see the task 74 table / for detailed results, see [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §3
  - Confirmed the max date move + row count change of the 8 raw tables (price · investorflow · marketbreadth · commodity · foreignindex · macroeconomic 2026-06-08 → 2026-06-15 / news · agency 2026-06-11 → 2026-06-16)
  - The `interest_foreignindex_raw` HANGSENG · NIKKEI225 · SHANGHAI partial NULL Data is separated as a non-blocker candidate rather than a Preprocessor blocker
- [x] 86. KRX EC2 worker Autologon bootstrap → 2026-06-16 Microsoft Sysinternals Autologon applied / Administrator auto-login set / SSM managed instance Online confirmed after EC2 reboot / Autologon is a paper-only Windows worker security exception (R-SEC-009 new) / 0 Administrator password plaintext records in this note ([`./operation-notes.md`](./operation-notes.md) 2026-06-16 §4)
- [x] 87. Confirm the Administrator console session Active → 2026-06-16 `query user` result USERNAME `administrator` / SESSIONNAME `console` / ID `1` / STATE `Active` / `whoami` result `nt authority\system` (normal because the SSM RunCommand itself runs as SYSTEM / the required condition was the existence of a separate Administrator console interactive session, and Active is confirmed) ([`./operation-notes.md`](./operation-notes.md) 2026-06-16 §4)
- [x] 88. Re-validate the SSM RunCommand → schtasks /Run → Scheduled Task → Administrator interactive session flow → 2026-06-16 complete. For detailed results, see [`./operation-notes.md`](./operation-notes.md) 2026-06-16 §4
  - trigger: `schtasks /Run /TN "Portfolio-KRX-Worker-Daily"` SUCCESS
  - Scheduled Task: Status `Running` → `Ready` / Last Result `267009` → `0` / Last Run Time 2026-06-16 04:55:49
  - wrapper log: `C:\portfolio\logs\krx_worker_daily_20260616_045550.log`
  - KRX login success (elapsed 92.83s) / `interest_program` 2026-06-15 / `interest_shortsell` 2026-06-15 349 Company / wrapper `DONE :: KRX worker daily`
- [x] 89. Confirm the KRX program / shortsell 2026-06-15 DB freshness → 2026-06-16 `interest_program_raw` max date 2026-06-15 / row count 547 → 548 / `interest_shortsell_raw` max date 2026-06-15 / row count 190,554 → 190,903 / duplicate insert · upsert behavior within normal range ([`./operation-notes.md`](./operation-notes.md) 2026-06-16 §4)
- [x] 90. SSM direct Python execution operation-method exclusion decision lock + Headless · non-interactive KRX collection operation-method exclusion decision lock → 2026-06-16 OD-MS-022 new (Windows Autologon + Administrator interactive session + Scheduled Task + SSM trigger / SYSTEM Session 0 direct execution unsuitable / Headless · non-interactive KRX collection excluded from the operation method per local validation) ([`./operation-notes.md`](./operation-notes.md) 2026-06-16 §4, §5)
- [x] 91. Judge the Preprocessor re-runnable state → 2026-06-16 raw input data (6 non-GUI + 2 KRX + news / agency) recovery complete / Preprocessor MS = ECS Fargate Task maintained / `portfolio-paper-interest-preprocessor` reached a re-runnable state. The actual re-run / feature table max date / `updated_at` / whether new feature dates are created is separated as task 77 follow-up ([`./operation-notes.md`](./operation-notes.md) 2026-06-16 §5)
- [x] 92. Review the Chrome process residual cleanup option → 2026-06-21 first complete. `step-02-interest-crawler.ps1` performs a chrome / chromedriver stale process best-effort reset before running the KRX worker / a reset failure leaves only a warning log and proceeds (R-AUTO-017 mitigation first demonstration). The follow-up EC2 stop / restart procedure is combined with the R-AUTO-016 / EC2 lifecycle automation follow-up ([`./operation-notes.md`](./operation-notes.md) 2026-06-21 §1)

## Task Dependency Graph (simple)

```text
1~5 (ECR Repository)
  └─> 6~9 (Dockerfile inspection)
        └─> 10~13 (local build)
              └─> 14~17 (ECR Push)
                    └─> 18~24 (ECS Cluster / Role / Log Group)
                          └─> 25~28 (Preprocessor single-run validation)
                                ├─> 29~32 (Crawler outbound risk / 30·31 partial complete)
                                ├─> 33~34 (NAT-free re-confirmation)
                                ├─> 44~52 (Windows EC2 worker / KRX GUI collection / 2026-06-12)
                                ├─> 61~69 (SSM automation + ECS crawler smoke / 2026-06-13)
                                ├─> 70~81 (Backend AWS E2E dry-run first / Interest Crawler status re-judgment / 2026-06-15)
                                ├─> 82~92 (Resolve crawler data non-collection + KRX EC2 automation success / 2026-06-16)
                                └─> 35~40 (safety constraints / deliverable limitation)
                                      └─> 41~43 (completion criteria)
                                            └─> 53~60 (follow-up tasks / carryover / 53 partial complete)
```

## 2026-06-10 Carryover Item Summary

1. crawler Task Definition registration / RunTask runtime validation (task 30, 31 — the 2026-06-12 KRX GUI path was concluded by EC2 worker separation, the 2026-06-13 ECS smoke first passed / the actual daily Task Definition separation is task 58 follow-up)
2. crawler outbound (KRX / Naver / yfinance) reachability / Selenium runtime stabilization (2026-06-12 the KRX GUI-dependent path reached a first-operable state on the EC2 worker, 2026-06-13 Selenium Chrome smoke first passed, the KRX outbound ECS Fargate path / yfinance ECS path validation is a follow-up)
3. Additional inspection of residual public schema sequences (whether domain sequences other than preprocessor remain)
4. Decide the timing of authoring runbook.md / validation-checklist.md

## 2026-06-12 Carryover Item Summary

1. SSM RunCommand-based EC2 worker unattended execution (task 53 — 2026-06-13 partial complete / confirmed as the Scheduled Task trigger method)
2. EventBridge Scheduler → SSM RunCommand integration (task 54 — `schtasks /Run` trigger periodic execution)
3. Mixed ECS Task + EC2 worker orchestration in Step Functions (task 55)
4. EC2 worker log collection based on CloudWatch Logs Agent or SSM output (task 56)
5. Automatic DB validation output addition in the wrapper (task 57 / consistent with R-AUTO-007)
6. ECS / Fargate non-GUI crawler scope reorganization (task 58)
7. Specify the EC2 worker stop procedure after work completion (task 59)
8. KRX GUI collection headless refactoring maintained only as a long-term candidate (task 60)

## 2026-06-13 Carryover Item Summary

1. EventBridge Scheduler → SSM RunCommand → `schtasks /Run` periodic trigger integration (task 54)
2. Hybrid ECS Task + EC2 worker orchestration in Step Functions (task 55)
3. EC2 worker log collection based on CloudWatch Logs Agent or SSM output (task 56)
4. Automatic DB validation output addition in the wrapper (`interest_program_raw` / `interest_shortsell_raw` per-date row count output / consistent with R-AUTO-007) (task 57)
5. Separate the non-GUI crawler actual operation Task Definition (separate the smoke revision and the operation Task Definition) (task 58)
6. Specify the EC2 worker stop procedure after work completion (idle cost reduction) (task 59)
7. KRX GUI collection headless refactoring maintained only as a long-term candidate (the current decision is to use the EC2 worker) (task 60)

## 2026-06-15 Carryover Item Summary

1. Separate the non-GUI Interest Crawler daily operation Task Definition / command (proceeded combined with task 72 / task 58)
2. raw freshness recovery — `interest_price_raw` / `interest_investorflow_raw` / `interest_marketbreadth_raw` / `interest_commodity_raw` / `interest_foreignindex_raw` / `interest_news_raw` / `interest_agency_raw` loading up to the previous trade day (task 74)
3. preprocessor re-run — after raw freshness recovery, re-run `portfolio-paper-interest-preprocessor` → confirm exitCode 0 → confirm the feature table max date / `updated_at` → confirm whether new feature dates are created (task 77)
4. Automate the raw / feature freshness validation SQL after preprocessor execution (task 78 / consistent with R-DATA-009 / R-DATA-010 mitigation)
5. Automate the non-GUI raw latest-date SQL inspection (task 73)
6. Resume the Backend AWS E2E dry-run — proceed in the order `BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL`. Research runs before Decision (consistent with OD-MS-021). Order-submission / execution series are skipped or dry-run-only per the safety criteria
7. Interest Crawler wording-unification follow-up inspection (task 79 / OD-MS-020) — periodically inspect whether the "Interest Crawler completion: complete" wording remains in operation documents / reports / slides, etc.

## 2026-06-15 Carryover Item Recovery / Follow-up Separation (as of 2026-06-16)

This section re-classifies the 2026-06-15 carryover item table (the previous section) as recovered / follow-up separated / no change on the 2026-06-16 result basis. The task number mapping is kept as-is.

- Recovery complete (2026-06-16):
  1. Separate the non-GUI Interest Crawler daily operation Task Definition / command — task 72 / task 58 → recovered as 2026-06-16 task 83 (8 non-GUI included / Task Definition revision 7 new registration / RunTask exitCode 0 pass)
  2. raw freshness recovery — task 74 → recovered as 2026-06-16 task 85 (7 non-GUI + macro 2026-06-15 / news · agency 2026-06-16 / `interest_foreignindex_raw` partial NULL separated as non-blocker)
  3. non-GUI raw latest-date SQL inspection — task 73 → recovered together with 2026-06-16 task 85 (inspection result recovery passed)
- Remaining follow-up:
  4. Preprocessor re-run — task 77 → as of 2026-06-16, only "reached a re-runnable state" (task 90) is recovered, the actual re-run is separated as a follow-up
  5. Automate the raw / feature freshness validation SQL after preprocessor execution — task 78 → follow-up separated (consistent with R-DATA-009 / R-DATA-010 mitigation)
  6. Resume the Backend AWS E2E dry-run — order `BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL` / Research first / order·execution series skip or dry-run → follow-up separated (consistent with OD-MS-021)
  7. Interest Crawler wording-unification periodic inspection — task 79 → update to OD-MS-020 / this date's OD-MS-022-consistent wording follow-up separated

## 2026-06-16 Carryover / Follow-up Item Summary

1. EventBridge Scheduler → SSM RunCommand → `schtasks /Run` periodic trigger integration (task 54)
2. Step Functions ECS Task + EC2 worker hybrid orchestration (task 55)
3. EC2 worker log collection based on CloudWatch Logs Agent or SSM output (task 56)
4. Automatic DB validation output addition in the wrapper (task 57 / consistent with R-AUTO-007)
5. Specify the EC2 worker stop procedure after work completion (task 59)
6. Chrome process cleanup option — cleanup candidate at wrapper termination (R-AUTO-017 mitigation follow-up / this §15 task 88)
7. Preprocessor ECS re-run + confirm whether new feature dates are created (task 77)
8. Resume the Backend AWS E2E dry-run (`BACKTEST_RESEARCH` / `BACKTEST_REPORT` / `DAILY_BUY_SIGNAL` / `DAILY_POSITION_SIGNAL`)
9. `interest_foreignindex_raw` HANGSENG / NIKKEI225 / SHANGHAI NULL Data follow-up inspection (non-blocker candidate)
10. KRX GUI collection headless / non-interactive refactoring excluded from local validation / excluded from the operation method — the existing "long-term candidate" wording is corrected on this date (consistent with OD-MS-022)

## 2026-06-17 Carryover Item Recovery / Follow-up Separation (Daily AWS 17-step E2E completion point)

This section is the carryover item recovery / follow-up separation organization at the point where, in the 2026-06-17 Daily AWS 17-step E2E flow, this spec's scope step 2 `INTEREST_CRAWLER` / step 3 `PREPROCESSOR` both passed. For detailed results, consistent with [`./operation-notes.md`](./operation-notes.md) 2026-06-17 Daily AWS 17-step E2E complete (Interest Crawler + Preprocessor) §1 ~ §3.

### 2026-06-17 Recovered items (Daily AWS 17-step E2E pass)

1. Step 2 `INTEREST_CRAWLER` 17-step E2E pass: complete
   1) non-GUI ECS Fargate `portfolio-paper-interest-crawler:7` RunTask exitCode 0 / all steps SUCCESS
   2) KRX Windows EC2 worker Scheduled Task flow passed (SSM RunCommand → `schtasks /Run` → Administrator interactive session → `run_krx_worker_daily.ps1` → KRX login)
   3) `interest_program_raw` / `interest_shortsell_raw` 2026-06-16 loading confirmed
   4) crawler worker stop request complete — idle cost reduction + minimize the Autologon exposure time (consistent with R-SEC-009 mitigation)
2. Step 3 `PREPROCESSOR` 17-step E2E pass: complete
   1) ECS Fargate `portfolio-paper-interest-preprocessor:1` RunTask exitCode 0 / `PREPROCESSOR PIPELINE END`
   2) `pre_total_market_daily_feature` / `pre_total_stock_daily_feature` freshness `2026-06-16` confirmed
   3) The `execution_app` interest permission missing is not this step's impact but a step 8 impact (consistent with R-DATA-005 [2026-06-17 reinforcement]) — the preprocessor itself completed normally
3. R-DATA-009 / R-DATA-010 mitigation first demonstration: complete (consistent with 2026-06-16 §3 / this date's §3)

### 2026-06-17 Carryover Item Summary

1. EventBridge Scheduler → SSM RunCommand → `schtasks /Run` periodic trigger integration (task 54)
2. Hybrid ECS Task + EC2 worker orchestration in Step Functions (task 55)
3. EC2 worker log collection based on CloudWatch Logs Agent or SSM output (task 56)
4. Automatic DB validation output addition in the wrapper (task 57 / consistent with R-AUTO-007)
5. Automate the EC2 worker stop procedure after work completion (task 59 / this date's operator direct stop request passed / periodic automation is a follow-up)
6. Review the Chrome process cleanup option (task 92 / R-AUTO-017 new)
7. `interest_foreignindex_raw` HANGSENG / NIKKEI225 / SHANGHAI NULL Data follow-up inspection (non-blocker)
8. `interest_ticker_value_raw` freshness recovery (2026-03-09 → previous trade day / separate follow-up)
9. Automate the raw / feature freshness validation SQL after preprocessor execution (task 78 / consistent with R-DATA-009 / R-DATA-010 mitigation)
10. View implementation — follow-up planned

## 16. 2026-06-20 ~ 2026-06-21 Step 2 `INTEREST_CRAWLER` success-judgment reinforcement (operator direct work + Kiro document organization)

This section records, in task units, the following work the operator performed directly on 2026-06-20 ~ 2026-06-21.

- 2026-06-20: Daily AWS Paper Wrapper final inspection / 6/18 duplicate-run attempt safe stop / 6/19 KRX raw freshness recovery state
- 2026-06-21: `step-02-interest-crawler.ps1` success-judgment reinforcement / non-GUI ECS env reinforcement / KRX raw DB validation script creation + S3 deployment + EC2 standalone validation / step-02 DB validation integration / worker stopped fail-closed / Step 2 standalone execution validation

Reference: [`./operation-notes.md`](./operation-notes.md) 2026-06-20 §1 ~ §5 / 2026-06-21 §1 ~ §8 / [`../_common/operator-decisions.md`](../_common/operator-decisions.md) Change Log 2026-06-21 (OD-MS-026 new) / [`../_common/risk-register.md`](../_common/risk-register.md) R-AUTO-020 new + R-AUTO-007 / R-AUTO-016 / R-AUTO-017 reinforcement.

- [x] 100. Final inspection of the Daily AWS Paper Wrapper structure / safety criteria / EC2 start criteria → 2026-06-20 complete. For detailed results, see [`./operation-notes.md`](./operation-notes.md) 2026-06-20 §1
  - (a) wrapper structure: `run-daily-aws-paper.ps1` + `daily-aws-paper.config.ps1` + `daily-aws-paper.functions.ps1` + `steps/step-01 ~ step-17`
  - (b) order safety: Step 1 ~ 11 are stages before broker · KIS order submission / only Step 12 submits an actual KIS paper order / blocked if `-AllowPaperOrderExecute` is not specified / Step 12 excluded on Saturday · market holiday
  - (c) EC2 start criteria: MarketConnector EC2 (Step 1 / 12 / 13 / 17) + Crawler Worker EC2 (Step 2 KRX GUI worker) / `/tmp/inject-env.sh` may be lost after EC2 stop · start → lifecycle reinforcement follow-up separated
- [x] 101. 6/18 wrapper duplicate-run attempt safe stop → 2026-06-20 complete. For detailed results, see [`./operation-notes.md`](./operation-notes.md) 2026-06-20 §2
  - RunDate `2026-06-18` / Step 1 ~ 11 range re-run attempt
  - Step 1 initial failure (`/tmp/inject-env.sh` lost) → passed after regeneration
  - Step 2 non-GUI ECS exitCode 0 + KRX worker Scheduled Task trigger success
  - Ctrl+C stop right after Step 3 entry
  - Termination state: 0 ECS RUNNING + 0 AWS Batch RUNNING · SUBMITTED · PENDING · RUNNABLE + Scheduled Task `Ready` + LastTaskResult 0
  - 0 new orders: Step 12 not executed / 0 execution_plan / 0 strategy_execution_order / 0 connector_order_request / 0 KIS new orders
  - Existing `execution_plan_id 94` normal-completion history / Crawler Worker chrome residual processes cleaned up by EC2 stop / both EC2 stopped
- [x] 102. Inspect the 6/19 KRX raw freshness recovery state → 2026-06-20 `interest_program_raw` max_date `2026-06-19` / 2026-06-18 row_count `1` / 2026-06-19 row_count `1` / `interest_shortsell_raw` max_date `2026-06-19` / 2026-06-18 row_count `349` / 2026-06-19 row_count `349` / KRX raw freshness recovery complete / identified the limitation of Scheduled Task trigger / LASTEXITCODE-centric success judgment (R-AUTO-007) → separated the Step 2 wrapper success-judgment reinforcement follow-up (task 103 ~ 110) ([`./operation-notes.md`](./operation-notes.md) 2026-06-20 §3)
- [x] 103. Add `step-02-interest-crawler.ps1` Chrome / chromedriver best-effort reset → 2026-06-21 chrome / chromedriver stale process cleanup / a reset failure leaves only a warning log and proceeds / R-AUTO-017 mitigation first demonstration ([`./operation-notes.md`](./operation-notes.md) 2026-06-21 §1.1)
- [x] 104. Maintain the `step-02-interest-crawler.ps1` Administrator interactive Scheduled Task execution path → 2026-06-21 `Portfolio-KRX-Worker-Daily` `schtasks /Run` execution / SSM direct python rejected / consistent with OD-MS-022 / OD-MS-015 / Windows Administrator interactive session basis maintained ([`./operation-notes.md`](./operation-notes.md) 2026-06-21 §1.2)
- [x] 105. `step-02-interest-crawler.ps1` Scheduled Task termination wait + Last Result confirmation → 2026-06-21 `Running` status polling → `Ready` return wait / `sawRunning` log output / Step 2 failure on timeout / only Last Result 0 or 0x0 is SUCCESS / prohibit marking Step 2 SUCCESS on the Scheduled Task trigger success alone / R-AUTO-020 new mitigation first demonstration ([`./operation-notes.md`](./operation-notes.md) 2026-06-21 §1.3 ~ §1.4)
- [x] 106. `step-02-interest-crawler.ps1` latest worker log path output → 2026-06-21 `C:\portfolio\logs\krx_worker_daily_*.log` latest file path / last write time / size / tail output ([`./operation-notes.md`](./operation-notes.md) 2026-06-21 §1.5)
- [x] 107. `step-02-interest-crawler.ps1` crawler worker stopped fail-closed handling → 2026-06-21 abolished the pre-change behavior (auto-skip + possibility of Step 2 SUCCESS) / post-change behavior (immediate failure if EC2 is not `running` / instanceId · state output / block Step 2 SUCCESS entry while the KRX GUI worker · DB validation is not performed) / R-AUTO-016 mitigation update ([`./operation-notes.md`](./operation-notes.md) 2026-06-21 §1.6)
- [x] 108. non-GUI ECS crawler env reinforcement — `daily-aws-paper.functions.ps1` common function + ECS RunTask overrides environment variables → 2026-06-21 `Invoke-DailyAwsPaperEcsTask` `EnvironmentVariables` parameter / ECS RunTask `containerOverrides.environment` passing / `New-SsmParameterFile` `ExecutionTimeoutSeconds` / `Invoke-SsmCommandAndWait` `ExecutionTimeoutSeconds` passing / inject environment variables `TEMP=/tmp` · `TMP=/tmp` · `PYTHONUTF8=1` · `PYTHONIOENCODING=utf-8` ([`./operation-notes.md`](./operation-notes.md) 2026-06-21 §2)
- [x] 109. `interest_krx_raw_validate_daily.py` operational validation script new creation + local validation + S3 deployment + EC2 standalone validation → 2026-06-21 complete. For detailed results, see [`./operation-notes.md`](./operation-notes.md) 2026-06-21 §3
  - New file creation: operator direct (this spec's deliverable records only the fact) / local py_compile + UTF-8 read + program · shortsell · exit30 marker passed
  - S3 deployment: `s3://portfolio-paper-migration-yukiever/tmp/krx/interest_krx_raw_validate_daily.py` / EC2 download via presigned URL (SSM commandId `dd8e0f3e-df9a-4268-b11a-eea3f3df66c3`)
  - EC2 deployment target: `C:\portfolio\port-interest-crawler\interest_krx_raw_validate_daily.py`
  - EC2 standalone validation: SSM commandId `c844aea5-1429-430a-9510-39fc99f17f05` / venv `C:\portfolio\venvs\interest-crawler` / Python `3.13.5`
  - DB session: user=`crawler_app` / schema=`interest` / search_path=`interest, reference, legacy, public`
  - interest_program_raw: expected=`2026-06-19` / max_date=`2026-06-19` / expected_count=`1`
  - interest_shortsell_raw: expected=`2026-06-19` / max_date=`2026-06-19` / expected_count=`349` / exit code 0
- [x] 110. `step-02-interest-crawler.ps1` DB validation integration (`INTEREST_CRAWLER_KRX_DB_VALIDATE`) + Step 2 standalone execution validation → 2026-06-21 complete. For detailed results, see [`./operation-notes.md`](./operation-notes.md) 2026-06-21 §4 ~ §5
  - Integration: `ExpectedKrxRawDate` computation (the previous business day relative to RunDate) → run `load-crawler-db-env.ps1` + venv + `interest_krx_raw_validate_daily.py --expected-date <yyyy-mm-dd>`
  - fail-closed: Step 2 fail on row_count 0 or non-zero exit / `KrxDbValidationCommandId` included in the step result
  - Step 2 standalone validation: RunId `daily-aws-paper-20260621-204017` / Status `SUCCESS` / Runner `ECS+SSM` / ExpectedKrxRawDate `2026-06-19`
  - non-GUI ECS: `portfolio-paper-interest-crawler:7` / taskId `78979b5cbb714d0eb94f5946e15a14ce` / exitCode 0
  - KRX worker SSM: commandId `f9d82fcc-1e26-4710-87c3-1d20483b63ef` / Scheduled Task elapsedSeconds=`111` / sawRunning=True / FinalStatus=Ready / FinalLastResult=0
  - latest worker log: `krx_worker_daily_20260621_114154.log` / KRX login · program · shortsell SUCCESS
  - KRX raw DB validation SSM commandId `2279c6d7-2da6-4317-9c10-7cc77374b317` / ResponseCode 0 / interest_program_raw OK / interest_shortsell_raw OK / validation exit code 0

### Remaining follow-up

- [ ] 54. EventBridge Scheduler → SSM RunCommand → `schtasks /Run` periodic trigger integration (follow-up separated)
- [ ] 55. Mixed ECS RunTask + SSM RunCommand orchestration in Step Functions (follow-up separated)
- [ ] 56. EC2 worker log collection based on CloudWatch Logs Agent or SSM output (follow-up separated)
- [ ] 59. Specify the EC2 worker stop procedure after work completion (idle cost reduction) — integrated with the `/tmp/inject-env.sh` loss handling of 2026-06-20 §1 / §2 + auto start / stop lifecycle reinforcement (follow-up separated)
- [ ] 111. Review whether to display `KrxDbValidationCommandId` / latest worker log / Step 2 validation result on the View Daily Batch screen (follow-up separated / 05 spec)

## 17. 2026-06-22 Daily run re-validation (Step 2 / Step 3 0 regressions)

This section is the re-validation memo for this spec's (08 / Interest Crawler · Preprocessor) responsibility tasks among the 2026-06-22 Daily AWS Paper Wrapper 1 ~ 17 second live operational execution result. For detailed results, see [`./operation-notes.md`](./operation-notes.md) 2026-06-22 §1 ~ §4 / [`../_common/followups-overview.md`](../_common/followups-overview.md) 2026-06-22 follow-up memo.

- [x] 112. Step 2 KRX raw DB validation Daily run re-validation — 0 `INTEREST_CRAWLER_KRX_DB_VALIDATE` SSM step regressions / DB session user `crawler_app` / schema `interest` / search_path `interest, reference, legacy, public` / `interest_program_raw` · `interest_shortsell_raw` validation passed on the `ExpectedKrxRawDate 2026-06-19` basis / step result `KrxDbValidationCommandId` auto-recorded (OD-MS-026 / R-AUTO-020 mitigation 0 regressions / R-AUTO-007 mitigation 0 regressions) ([`./operation-notes.md`](./operation-notes.md) 2026-06-22 §1.4)
- [x] 113. Step 2 worker stopped fail-closed Daily run 0 regressions — this date's Crawler Worker EC2 state `running` / fail-closed branch not entered / R-AUTO-016 mitigation update 0 regressions (auto-skip behavior abolition kept as-is) ([`./operation-notes.md`](./operation-notes.md) 2026-06-22 §1.3)
- [x] 114. Step 2 Scheduled Task Running → Ready wait + Last Result confirmation Daily run 0 regressions — `Portfolio-KRX-Worker-Daily` Scheduled Task's `sawRunning=True` / FinalLastResult 0 / latest worker log path · last write time · size · tail normal output / Chrome / chromedriver best-effort reset 0 regressions (R-AUTO-017 mitigation 0 regressions) ([`./operation-notes.md`](./operation-notes.md) 2026-06-22 §1.3)
- [x] 115. Step 3 Preprocessor Daily wrapper integration validation complete — `portfolio-paper-interest-preprocessor:1` ECS RunTask exitCode 0 / `PREPROCESSOR PIPELINE END` label confirmed / raw → feature flow consistency (R-DATA-009 / R-DATA-010 mitigation 0 regressions) ([`./operation-notes.md`](./operation-notes.md) 2026-06-22 §2)

### Remaining follow-up (as-is from 2026-06-22)

- [ ] 54. EventBridge Scheduler → SSM RunCommand → `schtasks /Run` periodic trigger integration (follow-up separated)
- [ ] 55. Hybrid ECS RunTask + SSM RunCommand orchestration in Step Functions (follow-up separated)
- [ ] 56. EC2 worker log collection based on CloudWatch Logs Agent or SSM output (follow-up separated)
- [ ] 59. Specify the EC2 worker stop procedure after work completion (idle cost reduction) — combined with the Daily wrapper-side EC2 lifecycle auto start / stop reinforcement (consistent with followups-overview 2026-06-20 §1 / 2026-06-22 §1 / follow-up separated)
- [ ] 111. Review whether to display `KrxDbValidationCommandId` / latest worker log / Step 2 validation result on the View Daily Batch screen (follow-up separated / 05 spec)
