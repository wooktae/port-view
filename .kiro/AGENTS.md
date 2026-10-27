# Kiro AWS Migration Working Rules

This `.kiro` workspace is the space for authoring and managing the AWS Migration-related cross-service specs of the PORT-STRATEGY-AI portfolio.

## 0. Highest-Priority Document Readability Rules

Apply this section with the highest priority in all document work.

### 0.0 Scope Limitation Rule

The document readability rules in this Section 0 are the priority basis for all document work, but the default scope is limited to **the parts newly written or directly modified in the current task**.

Unless the user explicitly requests "full-document readability cleanup", "full exhaustive scan", "full violation cleanup", "full audit", or "baseline / after validation", Kiro does not exhaustively scan the entire existing document against the Section 0 basis or perform large-scale refactoring.

In default work, only the following scope is tidied against the Section 0 basis.

| Item | Applied scope |
| ------ | ----------------------------------------------------------- |
| New authoring | Sentences · tables · bullets newly added in the current task |
| Direct modification | Existing sentences · tables · bullets actually changed in the current task |
| Change-adjacent area | Cells over 300 chars · lines over 500 chars · slash chains of 4+ that may newly arise from this modification |
| Existing violations | Keep the original if outside the current task scope |

The change-adjacent area is limited to the same table row, the same bullet group, or the same short paragraph. Other sections of the same file, past-date sessions and the entire Historical Notes are not treated as change-adjacent areas.

Even if a Section 0 violation is found in an existing document, do not fix it immediately if it is not directly related to the current request. When necessary, record it only as a "follow-up readability cleanup candidate".

The following work is performed only when the user explicitly requests it as a separate run.

| Work | Default handling |
| ------------------------------------- | ------- |
| Full exhaustive scan of existing documents | Not performed |
| Create a ViolationRecord matrix | Not performed |
| Generate content_hash | Not performed |
| Generate a full before / after audit | Not performed |
| Full bulk cleanup of README / WORKLOG / CHANGELOG | Not performed |
| Bulk cleanup of past-date sections | Not performed |

Even when performing a separate full-document readability cleanup run, Kiro prioritizes speed and safety. Keep ambiguous items as the original and locally tidy only clear surface violations.


### 0.1 Highest-Priority Table Authoring Rules

When authoring a 2-column table in a document, first choose the most meaningful one among the table-header candidates below.

Before creating an arbitrary new 2-column header, prefer the candidates below.

(1) Item / Value
(2) Item / Result
(3) ID / Change
(4) Section / Content
(5) Category / Content
(6) ID / Content
(7) File / Change
(8) Step / Result
(9) Category / Item
(10) ID / Value
(11) Item / Reinforcement Target
(12) Memo / Content
(13) Follow-up Item / Value
(14) File / Role
(15) Path / Date · Run
(16) Launcher / Role
(17) View / Step / DB Role
(18) Item / Validation Content
(19) ID / Status
(20) Number / Message
(21) Event / Result
(22) Validation / Result
(23) Target / Content
(24) Table / Value
(25) Follow-up / Value
(26) Row / Content
(27) Follow-up / Content
(28) Table / 2026-06-17 added row
(29) Term / Description
(30) Case / Result

The most basic default is `Item / Value`.

However, use a more specific header in the following cases.

| Situation | Preferred header |
| ----------------- | ----------- |
| Validation result summary | `Item / Result` |
| Per-file change summary | `File / Change` |
| Decision ID / Risk ID change | `ID / Change` |
| Per-section organization | `Section / Content` |
| Per-Step execution result | `Step / Result` |
| Follow-up organization | `Follow-up / Value` |
| Term description | `Term / Description` |

### 0.1.1 2-Column Table Default Creation Rule

From now on, tables Kiro newly creates are authored as **2-column tables** by default.

The default header is `Item / Value`, and one of the 0.1 candidate headers is chosen only when a more specific meaning is needed.

Tables with 3 or more columns are allowed only when one of the conditions below applies.

| Condition | Handling |
| --- | --- |
| The user explicitly requests 3+ columns | Use the requested column count |
| An existing table has 3+ columns and preserving it is safer | Keep the existing structure |
| A row/column cross structure is essential, like a traceability matrix | Allow 3+ columns |
| Converting to 2 columns loses significant meaning | Allow 3+ columns and report the reason |

If none of the exceptions apply, long bullets · enumerated sentences · status summaries · per-file changes · validation results · security results are all organized into 2-column tables.

Even when making a 2-column table, keep the cells short and separate long rationale into Details / Evidence / Historical Notes / Usage Notes.

### 0.2 Table Cell Content Authoring Rule

(Important) Keep the content inside a table cell as short as possible.

* Always write one cell in 2 sentences or fewer.
* When a cell contains multiple items, split them with `<br>`.
* Avoid long comma enumerations, long slash chains and long parenthetical explanations.
* Move long rationale outside the table to `Details`, `Evidence`, `Historical Notes` or `Usage Notes`.
* Do not put operation-notes-level execution logs in a table; keep only a link.

Example:

| Item | Value |
| ----- | --------------------------------------------- |
| Target files | `README.md`<br>`WORKLOG.md`<br>`CHANGELOG.md` |
| Result | 🟢 `Complete`<br>0 new sensitive-information verbatim |
| Follow-up | 🟠 2026-06 historical section additional cleanup |

### 0.3 `<br>`-First Line-Break Rule

When multiple values must be enumerated inside a table cell, prefer `<br>`.

Recommended:

| Item | Value |
| -------- | ----------------------------------------------------------------- |
| Security | 0 AWS CLI executions<br>0 psql executions<br>0 broker order submissions<br>0 secret verbatim records |

Not recommended:

| Item | Value |
| -------- | -------------------------------------------------------------- |
| Security | 0 AWS CLI executions · 0 psql executions · 0 broker order submissions · 0 secret verbatim records |

### 0.4 Color / Status Indicator Rule

Express status first with a badge.

| Badge | Meaning |
| -- | ----------------------------- |
| 🔴 | Prohibited · fatal · live · high-risk |
| 🟠 | Waiting · observing · Open · undecided |
| 🟢 | Complete · success · ENABLED · Mitigated |
| 🔵 | Reference · evidence · information |
| ⚫  | Not applicable · N/A |

Use HTML color only in a limited way when needed.

| Purpose | Color |
| -------- | ------------------------------------------- |
| critical | `<span style="color:#D1242F">**Prohibited**</span>` |
| warning  | `<span style="color:#BF8700">**Caution**</span>` |
| done     | `<span style="color:#1A7F37">**Complete**</span>` |
| info     | `<span style="color:#0969DA">**Reference**</span>` |

If a status badge is sufficient, do not additionally apply an HTML color.

### 0.5 Document Authoring Density Rule

Follow the priority below when authoring documents.

1. Short Summary
2. Short table
3. Short bullets
4. Details / Evidence / Historical Notes link
5. Long body text as the last resort

Prohibited.

* Long paragraphs inside a table
* Cells over 300 characters
* Lines over 500 characters
* Slash chains of 4 or more
* Pasting full raw logs
* Repeating the same content at length across README / WORKLOG / CHANGELOG / operation-notes

### 0.6 Work-Speed Limitation Rule

Document work prioritizes fast local edits and safe reporting over perfect automatic analysis.

Unless the user explicitly requests it, Kiro does not perform the following work.

| Prohibited work | Default handling |
| --- | --- |
| Create or re-invoke a sub-agent | Not performed |
| Create a separate orchestrator / coordinator task | Not performed |
| Write a new scanner / audit framework | Not performed |
| Generate a content_hash-based full matrix | Not performed |
| Repeatedly rescan all files | Not performed |
| Create temporary files outside the workspace | Not performed |
| Use `$env:TEMP`, `C:\Users\Public` | Not performed |
| Run long PowerShell one-liners | Not performed |
| Repeat command variations that trigger a trust prompt | Not performed |

The default time limits follow the below.

| Work unit | Max time | Handling on exceed |
| --- | --- | --- |
| Single-document local edit | 15 min | Report partial completion |
| Readability correction of 1 root document | 30 min | Keep ambiguous items as original |
| Correction of 3 root documents | 90 min | Separate per-file complete/incomplete |
| Full baseline / after validation | 30 min | Report only the required counts |
| A specific task bottleneck | 15 min | Carry over as `needs_manual_review=true` |

When exceeding the time limit, do not build more elaborate automation. Briefly report only the facts confirmed so far, the edited files, remaining items and manual-review items, and move on to the next work.

### 0.7 Document Work Execution Method Limitation

Document work proceeds only in the order below.

1. Confirm the requested scope
2. Read the target file directly
3. Locally edit only the necessary parts
4. Save as UTF-8 No BOM
5. Short after check
6. Report the change summary

The following methods are prohibited by default.

| Method | Reason |
| --- | --- |
| The "analysis script → JSON → re-parse → matrix → edit" flow | Excessive relative to document work |
| Decomposing one task into multiple sub-agents | Orchestration cost exceeds the edit cost |
| Re-analyzing the whole document to resolve an uncertain item | High time consumption and increased fact-loss risk |
| Fully reconstructing past-date sections | High likelihood of exceeding the requested scope |

Do not edit uncertain items; leave them as `needs_manual_review=true` or a Follow-up.

## 1. Scope

### 1.1 Base Paths

* Base working directory: `C:\Workspaces\port-view\.kiro`
* AWS Migration spec storage location: `C:\Workspaces\port-view\.kiro\specs`

### 1.2 Target Microservices

Although this directory is inside `port-view`, the specs authored here cover the AWS Migration of all 8 MS below.

* `port-marketconnector`
* `port-view`
* `port-interest-crawler`
* `port-interest-preprocessor`
* `port_strategy_common`
* `port_strategy_decision`
* `port_strategy_research`
* `port_strategy_execution`

### 1.3 Per-MS File Modification Principle

Each MS's `AGENTS.md`, `README.md` and existing documents are modified only when the current work explicitly requires modifying that MS.

When authoring an AWS Migration spec, per-MS documents are used only as read-only references to understand the following.

| Item | Content |
| --- | -------------------- |
| Runtime | structure · entrypoint |
| Configuration | environment variables · secret items |
| DB | schema · search_path |
| Operations | build · deployment constraints · cautions |

Without explicit permission, do not modify MS source code, README, CHANGELOG, worklog or AGENTS.md.

## 2. Single Source of Truth

Before creating or modifying a new spec, first check the root common documents under `.kiro/specs/_common`.

| Document | Role |
| --------------------------------------------- | ---------------------------------------- |
| `_common/operator-decisions.md` | The single source-of-truth document for operator decisions |
| `_common/ms-aws-service-decision-matrix.md` | The single source-of-truth document for the per-MS AWS service recommendation and rationale across the 8 MS |
| `_common/cost-simulation.md` | The single source-of-truth document for cost assumptions and expected monthly cost |
| `_common/followups-overview.md` | The single source-of-truth document for the follow-up spec progression order and dependency map |
| `_common/aws-resource-glossary.md` | The single source-of-truth document for AWS terminology explanations |
| `_common/risk-register.md` | The single accumulative document for AWS Migration operational / security / cost risks |
| `_archive/note-aws-landscape-2021-vs-2026.md` | Reference archive |

Do not copy large content from the common documents verbatim into another spec.

Summarize only the necessary content briefly and reference the common documents for detailed criteria.

## 3. Document Authoring Basic Principles

### 3.1 Spec Authoring Rules

Each spec focuses on its own scope.

The general spec folder composition follows the below.

| File | Role |
| ------------------------- | ----------------------- |
| `requirements.md` | Requirements |
| `design.md` | Design |
| `tasks.md` | Work plan |
| `decision-matrix.md` | Written optionally when the operator has many options |
| `runbook.md` | Operational procedure |
| `validation-checklist.md` | Validation checklist |
| `operation-notes.md` | Operational evidence accumulation |

### 3.2 Update Rules

| Change content | Document to update first |
| ------------------------ | --------------------------------------------------------------------------- |
| Operator decision change | `_common/operator-decisions.md` |
| Per-MS AWS service recommendation change | `_common/ms-aws-service-decision-matrix.md`<br>`operator-decisions.md` if needed |
| Cost assumption change | `_common/cost-simulation.md` |
| Follow-up spec order / dependency / scope change | `_common/followups-overview.md` |
| Introducing a new AWS service or important term | `_common/aws-resource-glossary.md` |
| New risk identified | `_common/risk-register.md` |

Do not mechanically update all root common documents.

Update only the documents affected by the current decision or spec.

### 3.3 README / CHANGELOG / WORKLOG Management Rules

Unlike each MS, the `.kiro` workspace does not create per-date worklog files.

| Document | Recording basis |
| -------------------- | ---------------------------------------------------- |
| `.kiro/README.md` | The workspace's purpose · scope · folder structure · key document roles |
| `.kiro/CHANGELOG.md` | spec structure change · root common document change · new spec creation · major document reorganization history |
| `.kiro/WORKLOG.md` | Accumulates a simple log of Kiro work sessions in a single file |

Rules:

* Do not create per-date worklog files like `.kiro/docs/worklog/YYYY-MM-DD.md`.
* When a meaningful Kiro document task is performed, update `.kiro/WORKLOG.md`.
* When there is a major change such as spec structure, a root common document or a new spec creation, update `.kiro/CHANGELOG.md`.
* When the workspace purpose, folder structure or key document list changes, update `.kiro/README.md`.
* `CHANGELOG.md` is updated only for structure changes, new spec creation and major document reorganization.

## 4. Document Readability Criteria

### 4.1 Reference Samples

When authoring a new spec document, a root common document, or a large reconstruction of an existing document, refer first to the density and section structure of the 3 documents below.

| Document | Reference basis |
| -------------------- | ---------------------------------------------------------------- |
| `.kiro/README.md` | Purpose · scope · current status · safety guidance · additional reference locations |
| `.kiro/WORKLOG.md` | `Summary / Completed / Evidence / Risks / Follow-ups / Security` |
| `.kiro/CHANGELOG.md` | `Added / Changed / Removed / Security` |

The top of a document must let the operator grasp the current status and next action within 1 minute.

### 4.2 Root Common Document Structure Principles

The single source-of-truth documents under `specs/_common/` take the following flow as their default structure.

| Order | Section | Content |
| -- | ----------------------------------------------------------- | --------------------------------- |
| 1  | `Purpose` | Explain the question this document answers in 1~3 lines |
| 2  | `Dashboard` / `Summary` | The current status the operator checks daily |
| 3  | `Index` | A table centered on ID · status · next action · Details link |
| 4  | `Details` / `Appendix` / `Historical Notes` / `Usage Notes` | Long rationale and past history |
| 5  | `Update Rules` / `Security Notes` | Update conditions and sensitive-information prohibition rules |

Recommended structure per file:

| Document | Recommended structure |
| ----------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `operator-decisions.md` | Purpose → Review Needed → Status Legend → Decision Dashboard → At a Glance → Decision Index → Decision Details → Change Log → Update Rules → Security Notes |
| `risk-register.md` | Purpose → Risk Dashboard → Immediate Action Risks → Risk Index → Risk Details → Accepted/Closed Risks → Update Rules → Security Notes |
| `followups-overview.md` | Purpose → Current Follow-up Dashboard → Now/Next/Later/Blocked/Done Recently → Spec Roadmap → Historical Notes → Update Rules → Security Notes |
| `ms-aws-service-decision-matrix.md` | Purpose → Final Recommendation Summary → MS Decision Cards → Rejected/Deferred Services → Portfolio Appeal Notes → Appendix → Security Notes |
| `aws-resource-glossary.md` | Purpose → Category TOC → Glossary → Usage Notes → Update Rules → Security Notes |
| `cost-simulation.md` | Purpose → Cost Dashboard → Assumptions → Scenario Details → Cost Drivers → Update Rules → Security Notes |

### 4.3 Table and Details Separation Principle

* A table acts only as an Index.
* Keep only the core columns in a table, such as ID, status, selected value, next action and Details link.
* Separate long rationale, cost impact, operational risk, mitigation, detection, rollback, evidence and past history outside the table into Details / Appendix / Historical Notes / Usage Notes.
* Do not repeat the same fact at length across multiple documents.
* Reference the detailed source-of-truth document or operation-notes links.
* Do not paste operation-notes-level long execution logs, raw evidence or full validation results directly into a root common document.

### 4.4 Document Minimization Principle

* Before creating a new document, first judge whether it can be absorbed into an existing document.
* Limit the documents the operator must check daily to each spec's `README.md`, `runbook.md`, `validation-checklist.md` and `operation-notes.md`.
* Treat `requirements.md`, `design.md`, `tasks.md` and `traceability-matrix.md` as Kiro/audit documents.
* Record the same decision value in detail only in `operator-decisions.md`, and reference only the Decision ID and a short summary in other documents.
* After completing work, limit the auto-update targets to at most 3 files by default.

## 5. Markdown Readability Rules

### 5.1 Principles

* Author Markdown to be easy to read on the GitHub preview basis.
* Author it so the operator can understand it immediately even when opening it a day later.
* Prefer short bullets over long paragraphs, tables over long bullets, and an Index + Details structure over long tables.
* When a line becomes excessively long, choose one of sentence splitting, table conversion or moving to Details.
* When a line break is needed inside a table cell, prefer `<br>`.

### 5.2 Status Badges

Use only the following 5 status badges.

| Badge | Meaning | Example |
| -- | ----------------------------- | ------------------------ |
| 🔴 | Fatal · prohibited · live · high-risk | aws-live automatic BUY/SELL not approved |
| 🟠 | Waiting · observing · Open · undecided | Follow-up validation needed |
| 🟢 | Complete · success · ENABLED · Mitigated | Scheduler ENABLED |
| 🔵 | Reference · evidence · information | Validation link |
| ⚫  | Not applicable · N/A | A value that is actually meaningless |

The priority is `🔴 > 🟠 > 🟢 > 🔵 > ⚫`.

When one item matches multiple statuses, show only the one with the higher risk.

### 5.3 HTML Colors

Use HTML inline color in a limited way only for fatal, prohibited, live and approval-gate safety guidance.

| Purpose | Color |
| -------- | --------- |
| critical | `#D1242F` |
| warning  | `#BF8700` |
| done     | `#1A7F37` |
| info     | `#0969DA` |

Do not additionally apply an HTML color to information already sufficiently expressed by a status badge.

### 5.4 Bold Usage Criteria

Use Bold only for the below.

| Target | Example |
| -------- | ---------------------------- |
| Final status value | **SUCCEEDED** |
| Core safety phrase | **aws-live automatic BUY/SELL not started** |
| Core judgment | **NO_TARGET safe termination** |
| Important ID | **R-AUTO-037** |
| Major conclusion | **No service selection change** |

### 5.5 Prohibited Formatting

* No overuse of decorative emoji
* No long paragraphs inside a table
* No slash chains of 4 or more
* No pasting full raw logs
* No recording success/failure markers differently from the actual result
* No use of tab characters

## 6. Security Rules

Never write actual secret, password, token, app key, app secret, account number or webhook URL.

When sensitive information is needed, use only the placeholders below.

| Placeholder | Purpose |
| ---------------------------- | --------------- |
| `[REDACTED]` | General sensitive information |
| `[REDACTED_ACCOUNT_NO]` | Account number |
| `[REDACTED_PUBLIC_IP]` | public IP / EIP |
| `[REDACTED_ARN]` | ARN |
| `[REDACTED_TASK_ARN]` | task ARN |
| `[REDACTED_SECRET_ARN]` | secret ARN |
| `[REDACTED_BROKER_ORDER_NO]` | broker order number |

Do not print sensitive-information values anywhere in summaries, logs, examples, tables or generated documents.

Sensitive-information examples:

| Category | Content |
| ------ | -------------------------------------------- |
| DB | password |
| KIS | app key · app secret |
| Slack | webhook URL |
| Auth | token |
| Account | actual account number · actual account-id |
| AWS | actual ARN · public IP · EIP · task ARN · ENI ID |
| broker | broker_order_no |
| image | image digest full sha256 |

## 7. Execution Rules

In spec authoring work, do not perform the following actions.

| Prohibited item | Content |
| ----- | ------------------------------ |
| AWS | Resource creation · modification · deletion |
| Operations | Running operational entrypoints |
| Code | Modifying application source code |
| Orders | Running broker order scripts |
| API | KIS API calls · Slack webhook delivery |
| DB | DB DDL / DML / psql execution |
| Crawler | crawler / Selenium / Chrome execution |
| live | Performing live cutover |

When AWS Console work or an implementation procedure is needed, do not actually execute it; write it only as runbook steps.

### 7.1 Kiro Self-Execution Overhead Limitation

Kiro does not create unnecessary execution overhead during document work.

| Item | Rule |
| --- | --- |
| sub-agent | Used only when the user directly requests it |
| orchestrator | Do not create separately |
| parallel wave | Allowed only for clearly independent read work such as baseline measurement |
| retry | Stop and report after repeating the same failure twice |
| temporary files | Only `.kiro/tmp-*` inside the workspace is allowed |
| output | Report only a summary instead of long stdout |
| validation | Centered on required counts and diff check |

Document editing takes "read directly and modify directly" as the default. If Kiro starts building a complex execution system on its own, stop immediately and reduce to a simple prompt.

## 8. Operational Command Authoring Rules

Applies only when authoring command examples in runbook, validation-checklist and operation-notes.

Do not actually execute commands.

### 8.1 AWS CLI Commands

Author AWS CLI commands in the `list/describe → extract variable → subsequent verification` pattern.

* Do not write a 1-line example where the operator must hand-substitute ARN, task ARN, ENI ID, LOG_STREAM or image digest.
* Query dynamic identifiers first with `list` or `describe` and then store them in a variable for use.
* Do not record sensitive candidate values such as public IP in plaintext; use `[REDACTED_PUBLIC_IP]`.

### 8.2 psql Validation Queries

Author psql validation queries after first confirming the actual column names with `information_schema.columns`.

* Do not write `SELECT` / `JOIN` with assumed column names.
* Do not pass Korean SQL directly with `psql -c`.
* For SQL containing Korean, use the UTF-8 No BOM `.sql` file + `psql -f` pattern.

### 8.3 Failure Handling

* Do not print a SUCCESS / DONE marker after a failed SQL / command.
* For a command that may fail, write the validation condition and the stop-on-failure condition together.
* For a PowerShell native command failure, explicitly state the `$LASTEXITCODE` check.

### 8.4 Windows / PowerShell / SSM

* Author Windows PowerShell, SQL, JSON and SSM command files on the UTF-8 No BOM basis.
* For an SSM multiline command, use the UTF-8 No BOM JSON file + `--parameters file://...` pattern.
* Consider cp949 encoding errors of emoji / special characters in Windows AWS CLI stdout.
* Prefer the pattern of saving to a file and then confirming for long stdout or emoji-containing output.
* For bash variables inside a PowerShell here-string, prefer the single-quoted here-string + placeholder `.Replace()` pattern.

### 8.5 DB password / secret

Do not include secret values such as DB password, API key, token, Slack webhook and presigned URL in documents, commands, logs or memory.

For an example needing a DB password, explain only the priority below.

| Priority | Value |
| ---- | --------------------------------------- |
| 1 | `$env:INTEREST_DB_PASSWORD` already set by the user |
| 2 | Existing `$env:PGPASSWORD` |
| 3 | local secret loader |

ChatGPT / Kiro do not ask for a DB password value or provide a command of the form `$env:PGPASSWORD = "password"`.

## 9. Git Rules

Allowed git commands are limited to read-only status checks.

| Allowed | Prohibited |
| --------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| `git status --short`<br>`git diff --stat`<br>`git diff --check` | `git add`<br>`git commit`<br>`git rm`<br>`git mv`<br>`git push`<br>`git checkout`<br>`git reset`<br>`git stash`<br>`git restore` |

Do not auto-rollback on validation failure.

Report the failed file, failure attribute, cause and recommended action.

## 10. Update Checklist

Confirm the below before completing document work.

| Item | Confirmation |
| ---------- | ------------------------------------------------------------------------------------------------- |
| Scope | Is it limited to the requested spec / common documents? |
| MS files | Were the 8 MS source / README / CHANGELOG / worklog / AGENTS.md not modified unnecessarily? |
| Single source-of-truth | Does it not conflict with the single source-of-truth of the common documents? |
| Root documents | Does it follow the short top structure of the README / WORKLOG / CHANGELOG style? |
| 2-column table | Was the most fitting one among the 0.1 table-header candidates preferred? |
| Table cells | Were multiple values split with `<br>` and the cell content kept short? |
| Details separation | Was long rationale separated into Details / Appendix / Historical Notes / Usage Notes? |
| Duplication | Was the same fact not repeated at length across multiple documents? |
| Sensitive information | Were secret / password / token / webhook URL / account number / actual ARN / public IP / broker_order_no not recorded? |
| Execution prohibition | Was no actual AWS / DB / broker / KIS / Slack / crawler / Spring Boot execution performed? |
| 2-column default | Were new tables made 2-column by default, and were 3+ column exceptions given a reason? |
| Speed limitation | Was the work done without a sub-agent / orchestrator / new scanner / TEMP outside the workspace? |
| Time limit | If a bottleneck task exceeds 15 minutes, was it moved to partial completion or manual review? |
| Encoding | Were Korean-containing files saved as UTF-8 No BOM? |
| marker | Was no SUCCESS / DONE marker written after a failed command? |
| Root logs | Were the WORKLOG / CHANGELOG / README update criteria followed when needed? |
