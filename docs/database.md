# 数据库设计

当前阶段已经落地七类核心数据：

- 学习会话状态快照：支撑学生端 5 步学习闭环的恢复和查询。
- 练习提交记录：沉淀学生练习答案、判卷结果和错误率。
- Agent 调用日志：支撑 Prompt、模型输入输出、耗时和失败原因追踪。
- Prompt 模板：支撑 Agent Prompt 的持久化和在线更新。
- Prompt 模板版本：支撑历史版本追踪和回滚启用。
- RAG 知识切片：支撑微讲义生成前的向量检索。
- 后台操作审计：记录 Prompt 和知识库管理动作。

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

用途：保存 Agent Prompt 当前启用版本，支持通过 API 新增和更新。

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

## prompt_template_version

用途：保存 Prompt 模板历史版本，支持查看版本历史和激活旧版本。

字段：

- `version_id`: Prompt 版本 ID，主键。
- `code`: Prompt 模板编码。
- `agent_type`: Agent 类型。
- `version_label`: 业务版本号，例如 `v2`。
- `name`: 模板展示名。
- `system_prompt`: system prompt。
- `user_prompt_template`: 带变量占位符的 user prompt。
- `active`: 是否为当前启用版本。
- `created_by`: 操作管理员。
- `created_at`: 创建时间。

索引：

- `idx_prompt_template_version_code_created`: 按 Prompt 编码查看历史版本。
- `idx_prompt_template_version_code_active`: 快速定位启用版本。

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
- `embedding_json`: 知识切片向量，默认由本地 `HashEmbeddingClient` 生成。
- `enabled`: 是否启用。
- `created_at`: 创建时间。
- `updated_at`: 更新时间。

索引：

- `idx_knowledge_chunk_chapter`: 按章节筛选知识切片。
- `idx_knowledge_chunk_enabled_updated`: 按启用状态和更新时间筛选。

`dev` profile 下，如果 `knowledge_chunk` 表为空，后端会自动写入当前内置的高数基础切片，保证本地启动后可以直接检索。

## operation_log

用途：记录后台关键操作，方便审计 Prompt 调整、知识库维护和问题排查。

字段：

- `log_id`: 操作日志 ID，主键。
- `operator`: 操作管理员。
- `action`: 操作动作，例如 `PROMPT_TEMPLATE_UPSERT`。
- `target_type`: 操作对象类型，例如 `PROMPT_TEMPLATE`、`KNOWLEDGE_CHUNK`。
- `target_id`: 操作对象 ID。
- `detail`: 操作说明。
- `created_at`: 创建时间。

索引：

- `idx_operation_log_operator_created`: 按操作人查看历史。
- `idx_operation_log_target_created`: 按对象查看操作链路。
- `idx_operation_log_action_created`: 按动作筛选。

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
