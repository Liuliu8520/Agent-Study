# Prompt 模板

当前后端通过 `PromptService` 管理 Agent Prompt 模板：

- 默认 profile 使用内存 `PromptTemplateRepository`。
- `dev` profile 使用 MySQL `prompt_template` 表。
- 启动时会自动补齐 5 个默认模板。
- 可以通过 `PUT /api/agent/prompts/{code}` 新增或更新模板。

## 模板列表

| code | Agent | 用途 |
| --- | --- | --- |
| `diagnosis.default` | `DIAGNOSTICIAN` | 根据诊断题结果识别薄弱点 |
| `planner.three-day` | `PLANNER` | 根据薄弱点生成 3 天学习计划 |
| `lesson.micro` | `LESSON_GENERATOR` | 基于 RAG 切片生成 Markdown 微讲义 |
| `exercise.generate` | `EXERCISE_GENERATOR` | 根据讲义生成表达式练习题 |
| `review.feedback` | `REVIEWER` | 根据判卷结果生成复习建议 |

## 变量语法

Prompt 模板使用双花括号变量：

```text
{{weakPoints}}
{{learningPlan}}
{{retrievedChunks}}
```

`PromptService` 会用请求中的 `variables` 字段替换变量。缺失变量会保留原占位符，方便调试 Prompt 是否缺参。

## Mock LLM

当前 `MockLlmClient` 不调用真实模型，而是根据 `AgentType` 返回确定性文本，并记录：

- `promptCode`
- `sessionId`
- `modelName`
- 渲染后的 system/user prompt
- 输出文本
- 调用耗时

这样可以先跑通 Agent 工程链路，后续替换真实 LLM Client 时不用改业务入口。
