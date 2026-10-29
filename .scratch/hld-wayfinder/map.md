<!-- DSH-FORMAT: md-v1 -->
<!-- DSH-TITLE: hld.md 实施就绪 · 探索地图 -->
<!-- DSH-STATUS: ready-for-agent -->
<!-- DSH-LABELS: wayfinder:map -->
<!-- DSH-IDEMPOTENCY-KEY: deck-plan-hld-wayfinder-2026-10-29-map -->
<!-- DSH-CREATED: 2026-10-29T08:16:10.524Z -->

## Destination

hld.md 从草案走到「实施就绪」：批复定稿（状态转已确认）、《完善方案》回源链与确认链对齐、两组设计留白（user 侧 / destination+首页）接口明细补齐、前后端工程骨架立起且 /api 代理冒烟通过。此后无未决事项，交接实施票（create-plan）开工。

## Notes

- 域：毕设「基于 Spring Boot 与 Vue3 的旅游攻略分享网站」；文档链事实源路由见 hld.md 文档头（SRS=功能与验收基线 / GDD=数值规则边界 / FSR=范围分层 / 方案文档=字段与接口明细 / DEV-ENVIRONMENT=环境现状 / GLOSSARY=术语）
- 本图默认「计划不做实施」：各票产出决策与定案；例外是两张 task 票（Node 工具链实测、工程骨架冒烟）——它们以解锁后续决策与实施就绪为目的一并入图
- 各会话按票的 wayfinder:&lt;type&gt; 标签行事：grilling 票先调 Skill「grilling」+「domain-modeling」；research 票由子代理调 Skill「research」
- 候选目标依赖 DAG（本图建图依据）：`.scratch/hld-wayfinder/wayfinder-dag.drawio`
- 定案项变更唯一入口：先修订 hld.md 再动下游代码（hld.md §6 指令 3）

## Decisions so far

- [《完善方案》× 确认链一致性审计](issues/02-完善方案-确认链一致性审计.md)：方案 §一–§十一 共 21 处与确认链冲突或需补（硬冲突 8：逻辑删除全域化 / Spring Security 残留 / 状态机排期错位 / 四态 / 10 张+report / 评论点赞 / OSS / cost 类型）；裁决方向 = 全部改方案；另暴露确认链自身 2 处瑕疵（SRS·GDD 用 Avoid 词「普通用户」、FSR 写「四态」）。清单 [audit-完善方案-确认链冲突清单.md](audit-完善方案-确认链冲突清单.md) 为票 01 的弹药，含 6 个待裁决问题
- [HLD v1.0 定案项拷问与批复](issues/01-hld-v1-0-定案项拷问与批复.md): 13 题全按推荐批复：Q1 Knife4j 挂入 hld §2.2 common 职责（NFR-07 追溯闭环，指针补方案 §6.1）；Q2–Q5 四剪裁逐项确认（comment.parent_id 建表即含 P0 恒空 / like·co…
- [《完善方案》v1.1 回写执行](issues/07-完善方案-v1-1-回写执行.md): 四条完成判据全部实测通过：24 行落点表在冲突清单末尾、方案补 v1.1 文档头、LC01/LC02 三文档换词并记修订行、禁用词 grep 归零。
- [Node 工具链实测并回写 DEV-ENVIRONMENT.md](issues/03-node-工具链实测并回写-dev-environment-md.md): `docs/DEV-ENVIRONMENT.md` 已补 Node 一节且逐项与实测一致：node v24.21.0 / npm 11.19.0 / pnpm 12.6.0，位置 `F:\node` + 全局 `F:\npm-global`…
- [留白①：user 侧接口明细定案](issues/04-留白①-user-侧接口明细定案.md): 留白①闭环：8 题全按推荐批复——Q1 四个独立 GET（/api/user/article|collect|like|comment/page）；Q2 status 0–4 可选、非法 400、update_time DESC 并列 id…
- [留白②：destination 与首页四区块取数接口定案](issues/06-留白②-destination-与首页四区块取数接口定案.md): 留白②闭环：10 题全按推荐批复——Q1 前缀两族 /api/destination/** 与 /api/home/**（公开档）；Q2 聚合页两个独立 GET（GET /api/destination/{id} 简介与图片 + GET /…
- [前后端工程骨架与 /api 代理冒烟](issues/05-前后端工程骨架与-api-代理冒烟.md): 最小可信形态 = **Spring Boot 2.7.18 单模块 + 一个 `/api/ping` + Vite/Vue3/TS 单页 + `/api` 代理**，包管理器裁 **pnpm**。三条验收全部实测通过：后端 `mvnw pa…

## Not yet specified

- ~~《完善方案》一致性审计暴露的新冲突~~ **已收口**（票 02）：21 条全部落地为裁决，无新迷雾。「16 周进度安排对齐」为方案 §10.1 把 P0 状态机排进 P1 时间盒（DB07），改方案排期即可，不需要新票；执行侧移交票 07《《完善方案》v1.1 回写执行》
- P1「猜你喜欢」的「最近浏览」取数依赖（无浏览记录表）——**不属本图**（见 Out of scope 的 P1 裁决），已在审计清单 §8 留证，待 CP2 闸门后另开图处理
- 留白定案的二阶问题：两张留白票拷问中若发现 GDD 取数口径（排序/条数/聚合规则）覆盖不全，届时毕业新票

## Out of scope

- P2 全部（支付/预订/小程序/ES/关注流/举报/系统设置——FSR §7 排除）
- P1 是否进毕设时间盒（Redis/楼中楼/轻量分析/标签推荐「猜你喜欢」）——CP2 闸门（第 11–12 周）后的独立裁决，在本图终点之外；含 P1 标签推荐「最近浏览」的取数口径（无浏览记录表，审计清单 §8 已留证）
- 实施切片与施工票编排（create-plan）——下一效程，本图终点即交接点
- 论文成文（第四章取材见 hld.md §5）——写作活动，非本图决策

## 进度
