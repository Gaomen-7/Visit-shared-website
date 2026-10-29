# 开发环境记录（2026-10-29 安装）

毕设项目环境落地记录。改环境前先读本文；与 FSR 冲突时以本文为准（本文是环境现状的唯一事实源）。

## JDK 17（Temurin）

- 位置：`F:\dev-tools\jdk17`（zip 解压版，来自清华 TUNA 镜像 Adoptium 17.0.20.1）
- 机器级 `JAVA_HOME` = `F:\dev-tools\jdk17`（已实测：从注册表变量重建出的全新进程里就是该值）
- 改动前旧值：`JAVA_HOME` = `E:\space\09-tools\Jdk1.8`，还原用 [restore-env.ps1](F:\dev-tools\scripts\restore-env.ps1)（管理员 PowerShell 运行）
- ⚠️ **机器级 `Path` 里并没有 `%JAVA_HOME%\bin`**（2026-10-29 17:27 实测更正，原「Path 最前已追加」的记录不成立）：全新终端 `java -version` 仍是 **18.0.2**，`where java` 首命中 `C:\Program Files\Common Files\Oracle\Java\javapath\java.exe`；`E:\space\09-tools\Jdk1.8\bin` 仍在 Path 里（排在 javapath 之后）。
  - 结论：**靠 `JAVA_HOME` 的工具（`mvnw`、IDEA、Spring Boot 运行）拿到的是 17.0.20.1**；命令行直接敲 `java`/`javac` 拿到的是 Oracle 18 —— 别用命令行那份做构建判据
  - 命令行要用 17：全路径 `F:\dev-tools\jdk17\bin\java.exe -version`，或当前终端先 `set JAVA_HOME=F:\dev-tools\jdk17`
  - 待修（**未授权改动，仅记录**）：把 `%JAVA_HOME%\bin` 前置进机器级 `Path` 即可让 `java` 命令也回到 17；[restore-env.ps1](F:\dev-tools\scripts\restore-env.ps1) 中的旧 Path 是有意保留的原值，不要照它回滚
- ⚠️ 安装前已开着的终端/IDE 进程仍持有旧环境变量，**重启这些程序才生效**（IntelliJ IDEA 重启后建议在 Project Structure 里选 JDK 17）。本会话的 shell 实测继承到的 `JAVA_HOME` 还是 `E:\space\09-tools\Jdk1.8`，此时 `mvnw.cmd -v` 报 `Java version: 1.8.0_144` —— 见到 1.8 就是这个原因，不是安装失败

## MySQL 8.0.46

- 位置：`F:\dev-tools\mysql-8.0`（zip 版，官方 CDN 下载）；配置文件 `my.ini` 同目录；错误日志 `logs\error.log`
- Windows 服务名 **MySQL80**（开机自启），端口 **3307**（与老 MySQL 5.6 共存：5.6 服务名 MySQL、端口 3306，未动）
- 连接：`root` / `Travel@2026`（仅 root@localhost，本机开发用）；已建库 `travel_share`（utf8mb4 / utf8mb4_unicode_ci）；认证插件 caching_sha2_password → **JDBC 必须用 mysql-connector-j 8.x**
- ⚠️ PATH 里的 `mysql` 命令仍解析到老的 5.6 客户端；连 8.0 请用全路径：`F:\dev-tools\mysql-8.0\bin\mysql.exe -h127.0.0.1 -P3307 -uroot -p`
- 项目 JDBC URL：`jdbc:mysql://127.0.0.1:3307/travel_share?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true`

## Maven Wrapper（backend/）

> 2026-10-29 票 05 落地后追加三条实测：① `~/.m2/settings.xml` 里阿里云镜像 `https://maven.aliyun.com/repository/public` **直接访问返回 404**，但 `mvnw package` 实际下拉依赖**全部成功**（1 分钟拉完 Spring Boot 全家桶），记录不改，兜底备选 `https://repo1.maven.org/maven2/`（实测 200）；② 本会话 shell 继承的 `JAVA_HOME` 仍是 `E:\space\09-tools\Jdk1.8`，构建前须 `export JAVA_HOME=/f/dev-tools/jdk17`（或 PowerShell `$env:JAVA_HOME="F:\dev-tools\jdk17"`），否则 `mvnw` 报 `1.8.0_144`；③ 票 05 定案后端根包 `com.travelshare`、artifact `travel-share-backend`、Spring Boot 2.7.18，详见 [ADR 0001](adr/0001-工程骨架与工具链基线.md)。


- `backend\mvnw.cmd` + `.mvn\wrapper\`（wrapper 3.3.2）；Maven 发行版 3.8.8 经华为云镜像自动下载到 `~\.m2\wrapper\dists`
- `~\.m2\settings.xml` 已创建（原本不存在）：central 走阿里云镜像加速依赖下载
- 验证命令（在 backend/ 下）：`mvnw.cmd -v` → 应输出 Maven 3.8.8 + Java 17.0.20.1

## Node.js / npm / pnpm

实测时间 2026-10-29 17:25–17:30（版本、解析路径、镜像、安装连通性全部当场跑过）。

- 版本：`node` **v24.21.0** · `npm` **11.19.0** · `pnpm` **12.6.0**
- 位置：node/npm 装在 `F:\node`（Windows 官方 zip 版；`node_modules\npm` 与之配套，corepack 同目录）
- PATH 来源：**机器级 `Path` 里的 `F:\node\` 与 `F:\npm-global`**（不是用户级 Path），故全新终端直接可用、无需再配
- `where` 解析顺序（机器级 Path 在前，用户级 Path 在后）：
  - `node` → `F:\node\node.exe`（首命中；其后是 `F:\Anaconda\node.exe` = **v20.19.5**，别误用）
  - `npm` / `npx` → `F:\node\npm(.cmd)`、`F:\node\npx(.cmd)`
  - `pnpm` → `F:\npm-global\pnpm(.cmd)`（npm 全局安装，`npm ls -g` 里可见 `pnpm@12.6.0`）
- 镜像与缓存（用户级 `C:\Users\Administrator\.npmrc`，npm 与 pnpm **共用同一份**）：
  - `registry` = **https://registry.npmmirror.com/**（npmmirror 淘宝镜像；`npm config get registry` 与 `pnpm config get registry` 一致）
  - `cache` = `F:\Cache\npm` · `prefix`（npm 全局安装目录）= `F:\npm-global`
  - pnpm 独立内容寻址仓库（store）：`F:\.pnpm-store\v11`（`pnpm store path`）
- 连通性实测：`npm ping` → PONG（走 npmmirror）；`npm view vue version` → 3.5.44（可正常取包元数据）
- ⚠️ 沙箱/DSH 会话内的 shell 会在 PATH **最前**插入自己的 node（`C:\Users\Administrator\.dsh-win\node`，同为 v24.21.0）；写脚本做版本判据时用 `F:\node\node.exe` 全路径更稳
- ⚠️ **构建脚本默认被拦**（npm 11 与 pnpm 12 的 install-scripts 安全策略）：`esbuild` 这类带 postinstall 的包会报 `Ignored build scripts`。实测 vite@5 仍可正常跑（`@esbuild/win32-x64` 预编译二进制直接可用，`npx vite --version` → `vite/5.4.21 win32-x64 node-v24.21.0`），**不影响本项目**；若将来某个包确实需要跑 postinstall，用 `npm install-scripts approve <pkg>` / `pnpm approve-builds` 放行
- 包管理器选择：**npm 与 pnpm 两者都可用**（即便拦构建脚本也能跑通 vite）。工程骨架票（票 05）落地时二选一并把理由写进该票（前端锁文件只能留一个）

## 磁盘与安装包

- 全部装在 `F:\dev-tools`（downloads/ 安装包已清理；scripts/ 保留安装与还原脚本）
- Node 工具链的缓存与全局目录不在 `F:\dev-tools`：npm 缓存在 `F:\Cache\npm`、全局包在 `F:\npm-global`、pnpm store 在 `F:\.pnpm-store`
