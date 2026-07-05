# 数据库设计

当前阶段已经落地五类核心数据：

- 学习会话状态快照：支撑学生端 5 步学习闭环的恢复和查询。
- 练习提交记录：沉淀学生练习答案、判卷结果和错误率。
- Agent 调用日志：支撑 Prompt、模型输入输出、耗时和失败原因追踪。
- Prompt 模板：支撑 Agent Prompt 的持久化和在线更新。
- RAG 知识切片：支撑微讲义生成前的关键词召回。

## learning_session

用途：保存一次学生学习闭环的 `LearningState` 快照。

字段：

- `session_id`: 学习会话 ID，主键。
- `student_name`: 学生展示名。
- `current_step`: 当前学习步骤，1 到 5。
- `status`: 会话状态，`CREATED`、`IN_PROGRESS`、`FINISHED`。
- `state_json`: 完整 `LearningState` JSON 快照，包含诊断题、弱点、计划、讲义、练习、判卷结果和复习结果。
- `created_at`: 创建时间。
- `updated_at`: 更新时间。

索引：

- `idx_learning_session_student`: 按学生名查询会话。
- `idx_learning_session_status_updated`: 按状态和更新时间筛选会话。

## agent_call_log

用途：记录一次 Agent/LLM 调用，方便调试 Prompt、排查输出和统计耗时。

字段：

- `call_id`: 调用 ID，主键。
- `session_id`: 关联学习会话 ID，可为空。
- `agent_type`: Agent 类型，例如 `LESSON_GENERATOR`。
- `prompt_code`: Prompt 模板编码，例如 `lesson.micro`。
- `prompt_version`: Prompt 模板版本。
- `model_name`: 模型名称，当前 Mock 实现为 `mock-llm-v1`。
- `request_payload`: 渲染后的 system/user prompt。
- `response_text`: LLM 输出文本。
- `status`: 调用状态，`SUCCESS` 或 `FAILED`。
- `error_message`: 失败原因。
- `duration_millis`: 调用耗时。
- `created_at`: 创建时间。

索引：

- `idx_agent_call_log_session_created`: 按学习会话查看 Agent 调用链路。
- `idx_agent_call_log_agent_created`: 按 Agent 类型统计调用情况。

## prompt_template

用途：保存 Agent Prompt 模板，支持通过 API 新增和更新。

字段：

- `code`: Prompt 模板编码，主键，例如 `diagnosis.default`。
- `agent_type`: Agent 类型。
- `version`: 模板版本。
- `name`: 模板展示名。
- `system_prompt`: system prompt。
- `user_prompt_template`: 带变量占位符的 user prompt。
- `created_at`: 创建时间。
- `updated_at`: 更新时间。

索引：

- `idx_prompt_template_agent_type`: 按 Agent 类型筛选 Prompt 模板。

## exercise_attempt

用途：保存一次练习提交记录和判卷明细，便于后续做错题本、学习报告和历史趋势分析。

字段：

- `attempt_id`: 练习提交 ID，主键。
- `session_id`: 学习会话 ID。
- `correct_count`: 答对题数。
- `total_count`: 总题数。
- `error_rate`: 错误率。
- `results_json`: 完整判卷结果 JSON，包含题目 ID、学生答案、标准答案、是否正确和判卷详情。
- `submitted_at`: 提交时间。

索引：

- `idx_exercise_attempt_session_submitted`: 按学习会话查看提交历史。

## knowledge_chunk

用途：保存高数知识库切片，供 RAG 检索使用。

字段：

- `chunk_id`: 知识切片 ID，主键。
- `chapter`: 所属章节。
- `title`: 切片标题。
- `content`: 切片正文。
- `tags_json`: 检索标签 JSON 数组，例如 `["derivative", "chain_rule"]`。
- `enabled`: 是否启用。
- `created_at`: 创建时间。
- `updated_at`: 更新时间。

索引：

- `idx_knowledge_chunk_chapter`: 按章节筛选知识切片。
- `idx_knowledge_chunk_enabled_updated`: 按启用状态和更新时间筛选。

`dev` profile 下，如果 `knowledge_chunk` 表为空，后端会自动写入当前内置的高数基础切片，保证本地启动后可以直接检索。

DDL 文件：

```text
backend/src/main/resources/db/schema-mysql.sql
```

`dev` profile 下后端启动时会自动执行建表语句。

## Redis 缓存

用途：缓存热点学习会话，减少 MySQL 查询。

Key 格式：

```text
agent-study:learning-session:{sessionId}
```

Value：`LearningState` JSON 快照。

默认 TTL：20 分钟，可通过 `agent-study.persistence.cache-ttl-minutes` 配置。

## 后续规划表

- `admin_user`: 管理员账号。
- `operation_log`: 后台操作审计。
