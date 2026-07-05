# Backend

Agent_Study 后端服务，基于 Spring Boot 3 + Java 17。

## 当前状态

已完成学生端 P0 学习闭环：

- `GET /api/health` 健康检查
- `POST /api/learn/sessions` 创建学习会话
- `GET /api/learn/sessions` 查询学习会话列表，支持 `studentName`、`status`、`limit`
- `GET /api/learn/sessions/{sessionId}` 查询学习会话
- `POST /api/learn/sessions/{sessionId}/diagnosis/questions` Step 1 生成诊断题
- `POST /api/learn/sessions/{sessionId}/diagnosis/submit` Step 1 提交诊断答案
- `POST /api/learn/sessions/{sessionId}/plan` Step 2 生成 3 天学习计划
- `POST /api/learn/sessions/{sessionId}/lesson` Step 3 生成 RAG 微讲义
- `POST /api/learn/sessions/{sessionId}/exercises` Step 4 生成练习题
- `POST /api/learn/sessions/{sessionId}/exercises/submit` Step 4 表达式自动判卷
- `GET /api/learn/sessions/{sessionId}/exercise-attempts` 查询练习提交记录
- `POST /api/learn/sessions/{sessionId}/review` Step 5 生成智能复习或结业结果
- `GET /api/agent/prompts` 查询默认 Prompt 模板
- `PUT /api/agent/prompts/{code}` 新增或更新 Prompt 模板
- `POST /api/agent/mock-chat` 调用 Mock LLM Client
- `GET /api/agent/call-logs` 查询 Agent 调用日志，支持 `sessionId`、`agentType`、`status`、`promptCode`、`limit`
- `GET /api/rag/chunks` 查询 RAG 知识切片
- `POST /api/rag/retrieve` 按关键词检索知识切片
- `GET /api/statistics/dashboard` 查询学习会话、薄弱点和 Agent 调用统计

持久化状态：

- 默认 profile 使用 `InMemoryLearningSessionRepository`，方便本地测试和快速启动。
- `dev` profile 使用 `MySqlRedisLearningSessionRepository`，MySQL 保存 `LearningState` 快照，Redis 缓存热点会话。
- `dev` profile 使用 MySQL 保存 `exercise_attempt`，默认 profile 使用内存练习提交记录仓库。
- `dev` profile 使用 MySQL 保存 `AgentCallLog`，默认 profile 使用内存日志仓库。
- `dev` profile 使用 MySQL 保存 `prompt_template`，默认 profile 使用内存 Prompt 模板仓库。
- `dev` profile 使用 MySQL 保存 `knowledge_chunk`，表为空时自动写入默认高数切片。
- Step 1 诊断结果分析会调用 `diagnosis.default` Mock Agent，并写入 Agent 调用日志。
- Step 2 学习计划生成会调用 `planner.three-day` Mock Agent，并写入 Agent 调用日志。
- Step 3 RAG 微讲义生成会调用 `lesson.micro` Mock Agent，并写入 Agent 调用日志。
- Step 4 练习题生成会调用 `exercise.generate` Mock Agent，并写入 Agent 调用日志。
- Step 5 智能复习生成会调用 `review.feedback` Mock Agent，并写入 Agent 调用日志。
- MySQL 表结构见 `src/main/resources/db/schema-mysql.sql`，dev 仓库启动时会自动建表。

## 运行测试

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" test
```

## 默认内存模式启动

不依赖 MySQL/Redis：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" spring-boot:run
```

健康检查：

```text
GET http://localhost:8080/api/health
```

## MySQL/Redis 模式启动

先启动本地基础设施：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study
docker compose -f .\docker\docker-compose.yml up -d
```

再启用 `dev` profile：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" spring-boot:run "-Dspring-boot.run.profiles=dev"
```

默认连接信息：

- MySQL: `localhost:3306/agent_study`
- MySQL 用户名: `root`
- MySQL 密码: `agentstudy`
- Redis: `localhost:6379`

可通过环境变量覆盖：

- `AGENT_STUDY_MYSQL_URL`
- `AGENT_STUDY_MYSQL_USERNAME`
- `AGENT_STUDY_MYSQL_PASSWORD`
- `AGENT_STUDY_REDIS_HOST`
- `AGENT_STUDY_REDIS_PORT`
- `AGENT_STUDY_REDIS_DATABASE`

## 打包运行

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" package
java -jar .\target\agent-study-backend-0.1.0-SNAPSHOT.jar
```
