<!-- DSH-FORMAT: md-v1 -->
<!-- DSH-PARENT: 00 -->
<!-- DSH-LABELS: wayfinder:task -->
<!-- DSH-IDEMPOTENCY-KEY: deck-plan-hld-wayfinder-2026-10-29-child-G4 -->
<!-- DSH-CREATED: 2026-10-29T08:16:10.575Z -->
<!-- DSH-BLOCKED-BY: 03, 01 -->
# 05: 前后端工程骨架与 /api 代理冒烟

Type: task

Blocked by: 03, 01
Status: resolved
## Question

前后端工程骨架的最小可信形态是什么，能让 /api 代理冒烟通过？（task · AFK）

## 现状（2026-10-29）

- backend/ 只有 mvnw 引导（.mvn/wrapper），无 pom.xml、无 src
- 前端工程不存在（无 package.json / vite 配置）

## 工作内容

- 后端：Spring Boot 2.7.x 最小骨架（pom + 启动类 + 一个 ping/health 接口；连接串按 DEV-ENVIRONMENT.md，端口 3307）
- 前端：Vite + Vue3 + TS 最小骨架，dev server 起来，server.proxy 把 /api 转发 8080
- 落地时顺带裁决 npm 还是 pnpm（两者都已装），裁决记录在本票

## 验收

mvnw 编译通过；前端 dev 起来；浏览器经 /api 代理拿到后端 ping 响应。

## Comments

### local — 2026-10-29T11:09:09.476Z
## 落地记录（2026-10-29）

### 裁决（按票面要求二选一）

**包管理器 = pnpm 12.6.0**。理由：store 内容寻址 + 硬链接（`F:\.pnpm-store\v11`），实测 47 个包 2.2s 装完；票 03 实测 npm 18.5s vs pnpm 4.8s。前端只留 `pnpm-lock.yaml`，无 `package-lock.json`。

### 骨架形状（Q1–Q9 批复后定案，附录进 ADR 0001）

| 项 | 定案 |
| --- | --- |
| 后端坐标 | `com.travelshare` / `travel-share-backend` / Spring Boot 2.7.18 |
| 依赖 | web + validation + MyBatis-Plus 3.5.3.2 + mysql-connector-j 8.0.33 + lombok |
| 端口 | 后端 8080（**无 context-path**，控制器显式写 `/api/xxx`）；Vite dev 5173 |
| 接口 | 仅 `GET /api/ping` → `{code:200,message:"success",data:{service,time}}`；不建 8 个业务包、不建拦截器 |
| 连库 | 数据源已配（3307 / travel_share / caching_sha2）但**启动不验库**，ping 不碰数据源 |
| 前端 | `frontend/` 平级；vite + vue-ts（手写最小骨架，未走 create-vite 模板）；`server.proxy` `/api → http://localhost:8080`（`changeOrigin:true`，不改写前缀） |
| 代理可见性 | `App.vue` 挂按钮调 `fetch('/api/ping')` 展示结果，肉眼可验 |

### 验收取证（三条全过）

1. **后端编译**：`backend> mvnw.cmd -B -DskipTests package` → **BUILD SUCCESS**，1:00 min，产出 `target/travel-share-backend-0.0.1-SNAPSHOT.jar`
   - 前提：`export JAVA_HOME=/f/dev-tools/jdk17`（会话 shell 继承的仍是 `E:\space\09-tools\Jdk1.8`，不设会报 1.8.0_144）
2. **后端直连**：`curl http://127.0.0.1:8080/api/ping` → `{"code":200,"message":"success","data":{"service":"travel-share-backend","time":"2026-10-29T19:08:09+08:00"}}` HTTP:200
3. **经 Vite 代理**：`curl http://localhost:5173/api/ping` → **同一个 body**、HTTP:200（证明 5173 → 8080 代理链路通，而不是两端口各自活着）
   - 附带：`curl http://localhost:5173/` → 200 且返回 index.html（dev server 正常出页）
   - ⚠️ 必须用 `localhost`：Vite 5 默认只监听 `[::1]`，`http://127.0.0.1:5173` 会 curl exit 7
4. **前端编译**：`pnpm build`（`vue-tsc --noEmit && vite build`）→ 0 error，`dist/` 65.56 kB / gzip 26.25 kB

### 两个坑与处置（零系统改动）

- **`JAVA_HOME` 旧值**：未改任何系统环境变量（DEV-ENVIRONMENT 已记「未授权改动、仅记录」）。只在会话内 `export JAVA_HOME=/f/dev-tools/jdk17`，复现命令已写进票与 ADR。
- **Maven 镜像**：`settings.xml` 里阿里云 `repository/public` **实测 404**，但实际下拉依赖**全部成功**（走的是可用路径，1 分钟拉完 Spring Boot 全家桶）。记录不改，兜底方案 repo1 可达（200）写进 ADR。
- **esbuild 构建脚本**：pnpm 12 默认拦 install-scripts，首次 `pnpm install` 报 `ERR_PNPM_IGNORED_BUILDS`。用 pnpm 自己给出的 `pnpm-workspace.yaml` 里的 `allowBuilds.esbuild: true` 放行后重装通过（比 `pnpm approve-builds` 的交互式更可复现）。

## Answer

最小可信形态 = **Spring Boot 2.7.18 单模块 + 一个 `/api/ping` + Vite/Vue3/TS 单页 + `/api` 代理**，包管理器裁 **pnpm**。三条验收全部实测通过：后端 `mvnw package` BUILD SUCCESS，直连 8080 返回统一响应，经 5173 代理拿到同一个 body。骨架刻意不写业务包、不建 JWT 拦截器、不验库连接——这些留给实施票，骨架只证「HTTP + 代理 + 统一响应信封」这一条链路。

附带产出：根 `.gitignore`、`docs/adr/0001-工程骨架与工具链基线.md`、`GLOSSARY.md` 新增「统一响应」。

前后端工程骨架立起且 /api 代理冒烟三条验收全部实测通过（mvnw package BUILD SUCCESS / 8080 直连 200 / 经 5173 代理同 body 200）

## 进度
