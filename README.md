[繁體中文](#繁體中文) | [English](#english)

---

## 繁體中文

# Database-as-Code

> 把資料庫 schema 當成程式碼管理的工作流：schema 存在 Git，每次變更都要通過 CI 關卡，每個環境都可重現且不漂移——關聯式（Oracle）與文件式（MongoDB）兩邊都涵蓋。

目標只有一句：**永遠不要在線上資料庫手動下 `ALTER`**。一次 schema 變更是經過 review、測過、進版控的產出物，跟應用程式碼一樣。

> **關於範圍。** 這是從實務經驗萃取、一般化並去識別化後的**參考實作**，驗證於本機與 CI，尚未在線上系統實戰。領域（通用的帳務／訂單平台）、schema 名稱與工具皆為示意。我把原本分屬兩個 repo 的兩側包在同一個 repo 裡，讓整個生命週期可以在這裡展示。

### 從哪裡開始

| 你有            | 看這個                                                                                                           |
| ------------- | ------------------------------------------------------------------------------------------------------------- |
| **30 秒**      | 下方的[五階段流程](#五階段流程)圖                                                                                           |
| **3 分鐘**      | [三層衝突預防](#三層衝突預防)與 [squash，不是 append](#squash不是-append)——整套設計真正的取捨在這兩段                                        |
| **想看完整流程**    | [`docs/workflow.md`](docs/workflow.md)                                                                        |
| **想看零停機遷移說明** | [`docs/expand-contract.md`](docs/expand-contract.md)（Expand–Contract 模式）                                      |
| **想看範例檔案**    | [`sample-service/docs/db/`](sample-service/docs/db/)（應用端待同步 DDL）對照 [`database-repo/`](database-repo/)（中央事實來源） |

### 要解決的問題

這套工作流消除四個症狀：**schema 漂移**（線上與版控對不起來）、**「在我機器上是好的」**（欄位只在某人的開發機被手動加過）、**無法被審查的變更**（DDL 直接打上去，沒有 diff、沒有稽核軌跡、沒有回滾路徑）、**本機迭代被中央 schema 發布卡住**。

### 五階段流程

![Schema change lifecycle|700](docs/diagrams/workflow-5-stages.svg)

從開發者的分支到同步後的正式環境 schema：本機（Entity + 待同步 DDL + 模擬真實環境的 TestContainers）→ MR 與 CI 關卡 → 同步進中央 repo（去重、squash 重生、封存）→ 多環境推進（保留資料）→ 整合測試以 Maven 取得 schema。

### 三層衝突預防

| 層級                       | 位置    | 保證                                                           |
| ------------------------ | ----- | ------------------------------------------------------------ |
| **架構**                   | 設計期   | 每張表只有一個擁有者服務，其餘唯讀。跨服務 DDL 衝突不可能發生。                           |
| **真實結構驗證**               | 本機／CI | 每個 PR 用該服務已入庫的唯一事實來源啟動與真實環境一致的 容器，結構性衝突會產生真實資料庫錯誤讓 build 失敗。 |
| **Hibernate `validate`** | 本機／CI | 拿 Entity 的 `@Column` 集合比對同一次重放產生的 DDL，不一致就讓 context 啟動失敗。    |

關鍵在於**容器是用「即將被 review 與同步的檔案與真實線上結構」建起來的**——不是單純一個假的測試容器。所以測試通過就是 *Entity ↔ DDL* 同步的證據，直接比對真實環境結構。

### squash，不是 append

`oracle-ddl.sql` 永遠保存**當前的完整 schema**，做法是把完整票務歷史重放進一個用完即丟的容器再 dump 出來，而不是累加一條無止盡的 `ALTER` 鏈。

| 模式 | `*-ddl.sql` 大小 | 歷史在哪 | 容器啟動 |
|------|-----------------|---------|---------|
| Append | 無止盡成長 | 在檔案本身 | 逐漸變慢 |
| **Squash（本 repo）** | 固定，反映當前 schema | `migrations/` + `changelog.md` | 穩定 |

這是整套設計裡最違反直覺的取捨：**放棄「檔案本身就是完整歷史」，換取容器啟動時間不隨專案年齡衰退**。歷史沒有消失，只是搬到封存與變更紀錄裡。

### 這個 repo 有什麼

| 位置                                         | 內容                                     |
| ------------------------------------------ | -------------------------------------- |
| [`docs/`](docs/)                           | 完整工作流與零停機遷移模式                          |
| [`sample-service/`](sample-service/)       | 應用端：Entity、完整 `docs/db/` 票務歷史、把關它的契約測試 |
| [`database-repo/`](database-repo/)         | 中央事實來源：squash 後的完整 schema、遷移封存、變更紀錄    |

相關技術棧：Java 21 · Spring Boot 3.4 · Spring Data JPA · Testcontainers · Oracle + MongoDB · SQLFluff · Python · GitLab CI

---

## English

# Database-as-Code

> A schema-as-code workflow, shown end to end: schema lives in Git, every change passes CI gates, and every environment is reproducible and drift-free — for a relational store (Oracle) and a document store (MongoDB) alike.

One goal: **never run a manual `ALTER` on a live database**. A schema change is a reviewed, tested, version-controlled artifact, the same as application code.

> **On scope.** This is a **reference implementation** distilled, generalised and de-identified from production experience, validated locally and in CI but not yet proven on a production system. The domain (a generic account/orders platform), schema names and tooling are illustrative; the workflow, CI gates and tooling design are the real content. Both sides of what is normally a two-repository workflow are packaged here so the whole lifecycle can be read in one place.

### Where to start

| You have | Read this |
| --- | --- |
| **30 seconds** | The [five-stage lifecycle](#the-five-stage-lifecycle) diagram below |
| **3 minutes** | [Three layers of conflict prevention](#three-layers-of-conflict-prevention) and [squash, not append](#squash-not-append) — where the real trade-offs are |
| **The full workflow** | [`docs/workflow.md`](docs/workflow.md) |
| **Zero-downtime migration** | [`docs/expand-contract.md`](docs/expand-contract.md) (the Expand–Contract pattern) |
| **What the files look like** | [`sample-service/docs/db/`](sample-service/docs/db/) (pending DDL on the app side) against [`database-repo/`](database-repo/) (the central source of truth) |

### The problem

In a microservices estate, the database is the one piece of shared state that resists the usual "infrastructure as code" discipline. This workflow removes four symptoms: **schema drift** (the live DB no longer matches version control), **"it works on my machine"** (a column that only ever existed on someone's dev DB), **unreviewable change** (DDL applied directly, with no diff, no audit trail, no rollback), and **local iteration blocked** waiting on a central schema release.

### The five-stage lifecycle

![Schema change lifecycle|700](docs/diagrams/workflow-5-stages.svg)

From a developer's branch to a synchronised production schema: local work (Entity + pending DDL + Testcontainers) → MR and CI gates → sync into the central repo (dedup, squash-regenerate, archive) → promotion across environments with data retained → integration tests that pull the schema via Maven.

### Three layers of conflict prevention

| Layer | Where | Guarantee |
|-------|-------|-----------|
| **Architecture** | design | Each table has exactly one owner service; all others are read-only. Cross-service DDL conflicts cannot occur. |
| **Full-history replay** | local / CI | Every PR replays the service's complete ticket history through a live Oracle container; a structural conflict produces a real database error and fails the build. Because the history is never cleaned up, the guarantee holds as it grows. |
| **Hibernate `validate`** | local / CI | The Entity's `@Column` set is checked against the DDL produced by that same replay; any mismatch fails start-up. |

What makes this work is that **the container is built from the same files that will be reviewed and synced** — exposed onto the test classpath directly, not copied. A green test is evidence that *Entity ↔ DDL* are in lockstep, checked against the literal artifact rather than a re-typed stand-in.

### Squash, not append

`oracle-ddl.sql` always holds the **current full schema**, regenerated by replaying the complete ticket history through a disposable container and dumping the result — never an append-only chain of `ALTER`s.

| Mode | `*-ddl.sql` size | History | Container start |
|------|------------------|---------|-----------------|
| Append | grows forever | in the file itself | progressively slower |
| **Squash (this repo)** | fixed, reflects current schema | `migrations/` + `changelog.md` | stable |

This is the least obvious trade-off in the design: **giving up "the file is its own complete history" to stop container start-up from degrading with project age**. The history is not lost — it moves to the archive and the changelog.

### What is in this repo

| Location | Contents |
| --- | --- |
| [`docs/`](docs/) | The full workflow and the zero-downtime migration pattern |
| [`sample-service/`](sample-service/) | App side: an Entity, its full `docs/db/` ticket history, and the contract test that gates it |
| [`database-repo/`](database-repo/) | Central source of truth: the squashed schema, migration archive, changelog |

Stack: Java 21 · Spring Boot 3.4 · Spring Data JPA · Testcontainers · Oracle + MongoDB · SQLFluff · Python · GitLab CI
