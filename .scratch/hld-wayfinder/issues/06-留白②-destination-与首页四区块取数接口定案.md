<!-- DSH-FORMAT: md-v1 -->
<!-- DSH-PARENT: 00 -->
<!-- DSH-LABELS: wayfinder:grilling -->
<!-- DSH-IDEMPOTENCY-KEY: deck-plan-hld-wayfinder-2026-10-29-child-G6 -->
<!-- DSH-CREATED: 2026-10-29T08:16:10.575Z -->
<!-- DSH-BLOCKED-BY: 01 -->
# 06: 留白②：destination 与首页四区块取数接口定案

Type: grilling

Blocked by: 01

Status: resolved
## 维护者原话
出处：本会话（2026-10-29 18:21 第 1 轮拷问批复）
> 全按推荐
## Question

hld.md §4.3 留白②：目的地简介与图片、首页四区块（轮播/热门/最新/热门目的地）的只读取数接口明细如何定？

## 约束

- 权限档：公开只读（§4.2）
- 排序与条数口径回源 GDD §0.5（最热公式、分页基线）与 §4（destination/home）
- 定案后回写方案文档 §八或 hld.md 修订版（§4.3 指定入口）；需求锚点 FR-DST-01 / FR-HOME-01

## Answer

留白②闭环：10 题全按推荐批复——Q1 前缀两族 /api/destination/** 与 /api/home/**（公开档）；Q2 聚合页两个独立 GET（GET /api/destination/{id} 简介与图片 + GET /api/destination/{id}/article/page 分页）；Q3 首页四个独立 GET（carousel / article/hot / article/latest / destination/hot），非分页接口 data 直接为数组；Q4 分页基线 + sort=latest|hot（默认 latest、非法 400、命名立词供 article 模块沿用），目的地不存在或已逻辑删 → 404、空列表 200；Q5 轮播不按目标可见性过滤（目标失效由详情页/聚合页「内容不存在或不可见」统一兜底）；Q6 攻略卡片 VO 三处统一 11 件；Q7 热门目的地不排除 0 篇、不足 6 返回实际条数；Q8 顺手定案 GET /api/destination/list 选项列表（id/name，sort ASC 并列 id ASC）；Q9 GDD §0.5 就地补两条排序破缺（「最新」排序 + 目的地选项列表）；Q10 回写落点 = 方案 v1.3（§8.2 补 7 行 + 新增 §8.4）+ GDD v1.3（§0.5 补两条）+ hld 三处小修不升版（§2.2 前缀 / §4.2 公开档 / §4.3 留白②指针）。回写已落地并逐处核验。

## 进度

- 进度：100%（2026-10-29T18:39:04+08:00）：10 题全按推荐批复，前沿已空；回写完成：方案升 v1.3（§8.2 补 7 行 + 新增 §8.4）、GDD 升 v1.3（§0.5 补两条破缺）、hld 三处小修不升版，三文档落点已逐处核验
- 进度：40%（2026-10-29T18:19:17+08:00）：取证完成（hld §2.2/§4.1–4.3 / GDD §0.5·§4·§6.1 / 方案 §7.2·§八 v1.2 / SRS 锚点 / 票 04 先例），发现 2 处 GDD 破缺缺口与目的地选项接口缺口，发出第 1 轮拷问 10 题，等维护者批复
