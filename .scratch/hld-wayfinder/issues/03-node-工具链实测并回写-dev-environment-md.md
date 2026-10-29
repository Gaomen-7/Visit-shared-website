<!-- DSH-FORMAT: md-v1 -->
<!-- DSH-PARENT: 00 -->
<!-- DSH-LABELS: wayfinder:task -->
<!-- DSH-IDEMPOTENCY-KEY: deck-plan-hld-wayfinder-2026-10-29-child-G3 -->
<!-- DSH-CREATED: 2026-10-29T08:16:10.574Z -->
<!-- DSH-ASSIGNEES: lead -->
# 03: Node 工具链实测并回写 DEV-ENVIRONMENT.md

Type: task

Status: resolved
## Question

Node 工具链（node v24.21.0 / npm 11.19.0 / pnpm 12.6.0，2026-10-29 实测在 PATH）如何落进 DEV-ENVIRONMENT.md，使其保持「环境唯一事实源」地位？

## 工作内容（task · AFK）

- 实测：node/npm/pnpm 版本与解析路径（where）、npm 镜像配置现状
- 回写 DEV-ENVIRONMENT.md 新增「Node.js」一节：版本、位置、PATH 来源、镜像配置，与 JDK/MySQL/Maven 各节同格式

## 验收

文档补上 Node 一节且与实测一致；「前后端工程骨架与 /api 代理冒烟」票可凭它开工。

## Comments

### local — 2026-10-29T09:31:10.390Z
## Node 工具链实测与回写完成

### 实测取证（2026-10-29 17:25–17:30，全部当场跑）

| 项 | 实测值 | 取证命令 |
| --- | --- | --- |
| node | v24.21.0，首命中 `F:\node\node.exe` | `where node`（其后是 `F:\Anaconda\node.exe` = v20.19.5） |
| npm | 11.19.0，`F:\node\npm(.cmd)` | `npm -v` / `where npm` |
| pnpm | 12.6.0，`F:\npm-global\pnpm(.cmd)`（npm 全局装 `pnpm@12.6.0`） | `pnpm -v` / `npm ls -g --depth=0` |
| PATH 来源 | **机器级** `Path` 里的 `F:\node\` 与 `F:\npm-global`（用户级 Path 里没有） | `[Environment]::GetEnvironmentVariable('Path','Machine')` |
| registry | `https://registry.npmmirror.com/`（npm 与 pnpm 共用同一份用户级 .npmrc） | `npm config get registry` / `pnpm config get registry` |
| 缓存/全局/store | `F:\Cache\npm` · `F:\npm-global` · `F:\.pnpm-store\v11` | `npm config list` / `pnpm store path` |
| 连通性 | `npm ping` → PONG；`npm view vue version` → 3.5.44 | 直连实测 |

### 关键发现（比票面要求多看了一层，已写进文档）

1. **全新终端解析链可信**：用注册表 `Machine` + `User` 的 Path 重建一个干净进程复核，结论是 `node v24.21.0 / npm 11.19.0 / pnpm 12.6.0`，与 DSH 会话内一致。唯一差别是会话 shell 会在 PATH **最前**插自己的 node（`C:\Users\Administrator\.dsh-win\node`，同为 v24.21.0），已在文档里提示「版本判据用 `F:\node\node.exe` 全路径」。
2. **构建脚本默认被拦**（npm 11 / pnpm 12 的 install-scripts 安全策略）：装 @esbuild/win32-x64 时报 `Ignored build scripts`。为此专门在临时目录各跑一遍冒烟——npm 18.5s 装完 vite@5.4.21 且 `npx vite --version` 正常；pnpm 4.8s 装完，同样报被拦但**预编译二进制照旧可用**，`npx vite --version` → `vite/5.4.21 win32-x64 node-v24.21.0`（exit 0）。结论：**两条路都能跑通，拦脚本不影响本项目**；放行办法写进文档（`npm install-scripts approve` / `pnpm approve-builds`）。
3. **npm 还是 pnpm 不在本票裁**：两者实测都可用，选择留给票 05 落地时定（前端锁文件只能留一个），文档里只写「票 05 落地时二选一并把理由写进该票」。

### 顺手更正了 JDK 节一处**不成立**的记录（越界但必须说）

核 JDK 时复核了 PATH，发现文档原写的「机器级 Path **最前**追加了 `%JAVA_HOME%\bin`（新开终端 `java -version` = 17.0.20.1）」**与实测不符**：

- 机器级 `Path` 里**没有任何** `%JAVA_HOME%\bin` 条目（38 条逐条筛过），`where java` 首命中仍是 `C:\Program Files\Common Files\Oracle\Java\javapath\java.exe`，全新终端 `java -version` = **18.0.2**；`E:\space\09-tools\Jdk1.8\bin` 也还在 Path 里。
- 但机器级 `JAVA_HOME` **确实是** `F:\dev-tools\jdk17`，所以走 `JAVA_HOME` 的工具都是 17：从注册表重建干净进程后 `mvnw.cmd -v` → `Java version: 17.0.20.1, vendor: Eclipse Adoptium, runtime: F:\dev-tools\jdk17`（本会话 shell 因为继承了旧 `JAVA_HOME=E:\space\09-tools\Jdk1.8`，同一条命令报 `1.8.0_144`，这就是踩坑点）。
- 已把这条更正、判据口径（**构建判据只看 mvnw/IDEA，不看命令行 java**），以及「把 `%JAVA_HOME%\bin` 前置进机器级 Path 即可修」写进 DEV-ENVIRONMENT.md，并标注**未授权改动、仅记录**——本票没动任何系统环境变量，全部取证只读。

### 改动文件

- `docs/DEV-ENVIRONMENT.md`（28 → 53 行）：新增「Node.js / npm / pnpm」一节（版本 / 位置 / PATH 来源 / where 解析顺序 / 镜像与缓存 / 连通性 / 两个坑 / 包管理器待裁决），与 JDK、MySQL、Maven 各节同一格式；「磁盘与安装包」节补一行指明 Node 工具链的缓存与全局目录不在 `F:\dev-tools`

## Answer

`docs/DEV-ENVIRONMENT.md` 已补 Node 一节且逐项与实测一致：node v24.21.0 / npm 11.19.0 / pnpm 12.6.0，位置 `F:\node` + 全局 `F:\npm-global`，PATH 来自**机器级** Path（已用注册表重建干净进程复核），registry = npmmirror（npm/pnpm 共用一份 .npmrc），store = `F:\.pnpm-store\v11`，`npm ping` PONG 且实装 vite@5.4.21 冒烟通过。
同时更正了原 JDK 节一处不成立记录：机器级 Path **并没有** `%JAVA_HOME%\bin`，命令行 `java` 仍是 18.0.2；17 只经 `JAVA_HOME` 对 mvnw / IDEA 生效（干净进程 `mvnw -v` = 17.0.20.1 已实测），判据口径已写清。未改任何系统环境变量。
票 05 可凭本节开工；npm / pnpm 二选一留给票 05 落地时裁决（文档已注明）。

## 进度

- 进度：100%（2026-10-29T17:31:10+08:00）：Node 工具链取证完成，DEV-ENVIRONMENT.md 新增 Node 节并更正 JDK 节 Path 记录
