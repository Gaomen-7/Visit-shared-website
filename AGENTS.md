# Agents

Instructions for AI coding agents working in this repo.

## 工作流

**一次一个模块。一个模块 = 一份 spec = 一条分支 = 一个 commit。**

- **主线干净**：`main` 是一条始终可构建、可演示的线性主线。新功能一律从 `main` 切分支开工，完工后以**单个 commit** 落回 `main`。
- **分支即模块**：分支名 `feature/<feature-slug>`，与 spec 目录同名；一条分支只服务一个模块。
- **commit 粗于票**：一个 commit 就是一份 spec 的全部交付（代码 + 测试）；message 写清这个模块交付了什么、验收依据是哪几条。
- **代码是唯一事实源**：`.scratch/<feature-slug>/spec.md` 与 `issues/*.md` 是施工脚手架。模块代码合进 `main` 后，spec 与票随即删除——它们承载的内容此时由代码和测试承载。
- **文档随口径变**：`docs/`（FSR / SRS / GDD / HLD）在范围、数值、规则真正变更时才改；实现细节留在代码里。
- **模块完成的判据**：`git checkout main` 后从零跑测试全绿，且该模块的 spec 与票已删除。

选择下一个模块时读 `.scratch/p0-wayfinder/p0-dag.md`：节点、依赖边与每个节点的完成判据都在那里。

## Agent skills

### Issue tracker

Issues are tracked as local markdown files under `.scratch/<feature-slug>/`. See `docs/agents/issue-tracker.md`.

### Triage labels

Default five-role triage vocabulary: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

single-context: one `GLOSSARY.md` at the repo root, ADRs in `docs/adr/`. See `docs/agents/domain.md`.
