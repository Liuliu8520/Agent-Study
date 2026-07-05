# 数据库设计

当前阶段先完成学习会话持久化，后续再拆分更细的练习记录、Agent 调用日志、知识库切片和 Prompt 管理表。

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

DDL 文件：

```text
backend/src/main/resources/db/schema-mysql.sql
```

`dev` profile 下后端启动时会自动执行同等建表语句。

## Redis 缓存

用途：缓存热点学习会话，减少 MySQL 查询。

Key 格式：

```text
agent-study:learning-session:{sessionId}
```

Value：`LearningState` JSON 快照。

默认 TTL：120 分钟，可通过 `agent-study.persistence.cache-ttl-minutes` 配置。

## 后续规划表

- `exercise_attempt`: 单次练习提交记录和判卷明细。
- `knowledge_chunk`: RAG 知识切片。
- `prompt_template`: Agent Prompt 模板。
- `agent_call_log`: LLM/Agent 调用日志。
- `admin_user`: 管理员账号。
- `operation_log`: 后台操作审计。
