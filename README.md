# 旅游攻略分享网站（毕业设计）

Spring Boot 2.7 + Vue 3 的旅游攻略分享网站。当前已交付「认证与账号」模块：注册、登录、JWT 鉴权三档、个人信息读写、头像上传、改密。

## 环境前置

- JDK **17**：`JAVA_HOME` 必须指向 `F:\dev-tools\jdk17`（命令行直接敲 `java` 可能是 18，构建与运行前请显式设置）。
- MySQL **8.0**：端口 **3307**，账号 `root` / `Travel@2026`，库 `travel_share`（utf8mb4 / utf8mb4_unicode_ci）。
- Node.js + pnpm（前端包管理器为 pnpm，锁文件只留 `pnpm-lock.yaml`）。

## 1. 建库（开发库与测试库执行同一份脚本）

在 `backend/` 目录下执行：

```powershell
& 'F:\dev-tools\mysql-8.0\bin\mysql.exe' -h 127.0.0.1 -P 3307 -uroot -pTravel@2026 travel_share -e "source src/main/resources/db/schema.sql"
& 'F:\dev-tools\mysql-8.0\bin\mysql.exe' -h 127.0.0.1 -P 3307 -uroot -pTravel@2026 travel_share -e "source src/main/resources/db/seed.sql"
```

- `schema.sql`：建 `user` 表（`username` 唯一索引，大小写不敏感由排序规则保证）。
- `seed.sql`：预置管理员 `admin` / `admin123`（口令为 BCrypt 密文）。
- 测试库 `travel_share_test` 由测试启动时自动创建，并执行**同一份**脚本，不会污染开发库。

## 2. 启动后端

```powershell
cd backend
$env:JAVA_HOME = "F:\dev-tools\jdk17"
.\mvnw.cmd -B -DskipTests package
& "$env:JAVA_HOME\bin\java.exe" -jar .\target\travel-share-backend-0.0.1-SNAPSHOT.jar
```

- 服务地址 `http://localhost:8080`。
- 接口文档（Knife4j）：`http://localhost:8080/doc.html`。
- 上传目录 `./uploads`（配置项 `app.upload-dir`），静态映射到 `/uploads/**`，只读公开访问。

## 3. 启动前端

```powershell
cd frontend
pnpm install
pnpm dev
```

- 页面地址 `http://localhost:5173`。
- 开发代理：`/api` 与 `/uploads` 均转发到 `http://localhost:8080`（见 `vite.config.ts`）。

## 4. 代理与全链路验证

1. `http://localhost:5173/api/ping` 返回 `{"code":200,...}`（与直接打 8080 的 body 一致）。
2. 注册 → 登录 → 进入个人中心，能看到资料与头像。
3. 管理员 `admin` / `admin123` 登录后自动落到 `/admin` 占位页。
4. 未登录访问 `/user/profile` 被拦到 `/login?redirect=/user/profile`；登录后回跳。
5. 用普通用户 token 访问 `GET /api/admin/ping` 得到 **403**；管理员 token 得到 **200**；无 token 得到 **401**。

## 5. 自动化测试

```powershell
cd backend
$env:JAVA_HOME = "F:\dev-tools\jdk17"
.\mvnw.cmd -B test
```

- 后端：真实启动 + 真实 HTTP + 真 MySQL（`travel_share_test`），上传文件落在 `backend/target/test-uploads`。
- 前端：

```powershell
cd frontend
pnpm test
```

- 前端测试用 MSW 在网络层打桩，覆盖「401 清态跳登录并带来源路由」与「登录后按角色落地」。

## 目录速览

```
backend/   Spring Boot（com.travelshare：common / auth / account / file / admin）
frontend/  Vue 3 + TS + Pinia + Vue Router + Element Plus + Axios
docs/      FSR / SRS / GDD / HLD / ADR / 环境记录
```
