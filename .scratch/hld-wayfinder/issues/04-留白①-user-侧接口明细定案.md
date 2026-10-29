<!-- DSH-FORMAT: md-v1 -->
<!-- DSH-PARENT: 00 -->
<!-- DSH-LABELS: wayfinder:grilling -->
<!-- DSH-IDEMPOTENCY-KEY: deck-plan-hld-wayfinder-2026-10-29-child-G5 -->
<!-- DSH-CREATED: 2026-10-29T08:16:10.574Z -->
<!-- DSH-BLOCKED-BY: 01 -->
# 04: 留白①：user 侧接口明细定案

Type: grilling

Blocked by: 01

Status: resolved
## 维护者原话
出处：本会话（2026-10-29 17:56 第 1 轮拷问批复）
> Q1 4 个独立 GET（推荐）；Q2 照此定案（推荐）；Q3 带计数四元组（推荐）；Q4 记录时间 DESC + id 破缺（推荐）；Q5 照此定案（推荐）；Q6 camelCase（推荐）；Q7 方案 v1.2 + GDD §0.5 补两行（推荐）；Q8 顺手定案（推荐）
## Question

hld.md §4.3 留白①：user 侧「我的发布（五态徽标列表）」与「个人中心聚合三列表（点赞/收藏/评论记录）」的取数接口明细（URL、参数、返回结构、分页与排序）如何定？

## 约束

- 遵守 hld.md §4.1 统一约定（{code,message,data}；pageNum/pageSize → records/total/pages）
- 前缀落在 /api/user（§4.2 登录档）
- 规则口径回源 GDD §1（user 模块）与 §0.5/§0.6
- 定案后回写方案文档 §八或 hld.md 修订版（§4.3 指定入口）；需求锚点 FR-USER-03/04/05

## Answer

留白①闭环：8 题全按推荐批复——Q1 四个独立 GET（/api/user/article|collect|like|comment/page）；Q2 status 0–4 可选、非法 400、update_time DESC 并列 id DESC 破缺；Q3 我的发布带计数四元组；Q4 三列表记录时间 DESC + 记录 id DESC 破缺；Q5 收藏/点赞五件（含 authorNickname）、评论六件（含 commentId/content/封面）；Q6 JSON 一律 camelCase（DB snake_case 由 MyBatis-Plus 映射）；Q7 回写落点 = 方案 v1.2（§8.2 补 5 行 + 新增 §8.3）+ GDD v1.2（§0.5 补两条排序破缺）+ hld 不动；Q8 顺手定案 /api/user/profile（GET/PUT）与 PUT /api/user/password。回写已落地并逐处核验（方案文档 §8.1/§8.2/§8.3 与文档头 v1.2；GDD §0.5 与文档头 v1.2）。

## 进度

- 进度：100%（2026-10-29T18:04:12+08:00）：8 题全按推荐批复，前沿已空；回写完成：方案升 v1.2（§8.1 camelCase + §8.2 补 5 行 + 新增 §8.3）、GDD 升 v1.2（§0.5 补两条排序破缺）、hld 不动，两文档落点已核验
- 进度：40%（2026-10-29T17:53:10+08:00）：取证完成（hld §4.1–4.3 / GDD §0.5·§0.6·FR-USER-03–05 / 方案 §7.2·§八 v1.1 / GLOSSARY 术语核对无冲突），发出第 1 轮拷问 8 题，等维护者批复
