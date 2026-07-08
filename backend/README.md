# Backend

Agent_Study 后端服务，基于 Spring Boot 3 + Java 17。后端负责学习会话编排、Agent 调用、Prompt 模板、RAG 知识库、后台鉴权、操作审计、统计接口和持久化。

## 模块说明

- `learn`：学习会话、LearningState、诊断、学习计划、讲义、练习、复习结果。
- `agent`：PromptService、LLM Client、AgentRuntimeService、AgentCallLog、LLM 错误分类。
- `rag`：知识切片、Embedding 生成、向量检索、后台知识库管理。
- `admin`：后台登录、JWT 鉴权、操作审计、Prompt 版本管理。
- `statistics`：学习会话、薄弱点、Agent 调用耗时等统计。
- `security`：Spring Security 配置和 JWT 过滤器。
- `common`：统一响应、业务异常、全局异常处理。

## 运行模式

默认 profile 使用内存仓库，适合快速启动和测试。

`dev` profile 使用 MySQL/Redis：

- MySQL 保存 `LearningState`、练习提交、Agent 调用日志、Prompt 模板、Prompt 版本、知识切片、操作审计。
- Redis 缓存热点学习会话。
- 启动时会自动执行 `src/main/resources/db/schema-mysql.sql` 建表。
- `knowledge_chunk` 表为空时会写入默认高数知识切片。

## 启动

### 内存模式

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" "-Dmaven.repo.local=D:\Users\Desktop\NUIT_STUDY\Agent_Study\.m2\repository" spring-boot:run
```

### MySQL/Redis 模式

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study
docker compose -f .\docker\docker-compose.yml up -d

cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" "-Dmaven.repo.local=D:\Users\Desktop\NUIT_STUDY\Agent_Study\.m2\repository" spring-boot:run "-Dspring-boot.run.profiles=dev"
```

使用本机已有 MySQL/Redis 时，可在启动前覆盖：

```powershell
$env:AGENT_STUDY_MYSQL_URL="jdbc:mysql://localhost:3306/agent_study?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
$env:AGENT_STUDY_MYSQL_USERNAME="root"
$env:AGENT_STUDY_MYSQL_PASSWORD="<你的 MySQL 密码>"
$env:AGENT_STUDY_REDIS_HOST="localhost"
$env:AGENT_STUDY_REDIS_PORT="6379"
$env:AGENT_STUDY_REDIS_DATABASE="0"
```

## LLM 配置

默认使用 Mock LLM：

```text
AGENT_STUDY_LLM_PROVIDER=mock
```

接入 GLM 或其他 OpenAI-compatible Chat Completions 服务：

```powershell
$env:AGENT_STUDY_LLM_PROVIDER="openai-compatible"
$env:AGENT_STUDY_LLM_BASE_URL="https://open.bigmodel.cn/api/paas/v4/chat/completions"
$env:AGENT_STUDY_LLM_API_KEY="<你的 API Key>"
$env:AGENT_STUDY_LLM_MODEL="glm-4-flash-250414"
$env:AGENT_STUDY_LLM_TEMPERATURE="0.2"
$env:AGENT_STUDY_LLM_MAX_TOKENS="1024"
$env:AGENT_STUDY_LLM_TIMEOUT_SECONDS="60"
$env:AGENT_STUDY_LLM_FALLBACK_TO_MOCK="false"
```

说明：

- `AGENT_STUDY_LLM_BASE_URL` 是完整的 Chat Completions 地址，后端不会自动拼接路径。
- `AGENT_STUDY_LLM_FALLBACK_TO_MOCK=false` 适合联调真实模型，可以让错误直接暴露出来。
- 如果修改环境变量，需要重启后端进程。
- `POST /api/agent/mock-chat` 是历史调试接口名，实际会根据 `AGENT_STUDY_LLM_PROVIDER` 调用 Mock 或真实 LLM。
- 真实 LLM 调用失败时会返回 `errorType`，例如 `LLM_CONFIGURATION`、`LLM_AUTHENTICATION`、`LLM_MODEL`、`LLM_TIMEOUT`。

## 管理员配置

```text
AGENT_STUDY_ADMIN_USERNAME=admin
AGENT_STUDY_ADMIN_PASSWORD=agentstudy
AGENT_STUDY_ADMIN_JWT_SECRET=agent-study-local-development-secret-change-me
AGENT_STUDY_ADMIN_TOKEN_TTL_MINUTES=120
```

本地默认值仅用于开发。生产环境需要设置更强的管理员密码和 JWT 密钥。

## 常用接口

```text
GET  /api/health
POST /api/learn/sessions
GET  /api/learn/sessions
GET  /api/learn/sessions/{sessionId}
POST /api/learn/sessions/{sessionId}/diagnosis/questions
POST /api/learn/sessions/{sessionId}/diagnosis/submit
POST /api/learn/sessions/{sessionId}/plan
POST /api/learn/sessions/{sessionId}/lesson
POST /api/learn/sessions/{sessionId}/exercises
POST /api/learn/sessions/{sessionId}/exercises/submit
POST /api/learn/sessions/{sessionId}/review
POST /api/admin/auth/login
GET  /api/agent/call-logs
GET  /api/rag/chunks
POST /api/rag/retrieve
GET  /api/statistics/dashboard
```

完整接口以 OpenAPI 为准：

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs/agent-study
```

## 测试和打包

运行测试：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" "-Dmaven.repo.local=D:\Users\Desktop\NUIT_STUDY\Agent_Study\.m2\repository" test
```

打包：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" "-Dmaven.repo.local=D:\Users\Desktop\NUIT_STUDY\Agent_Study\.m2\repository" package
```

运行 Jar：

```powershell
java -jar .\target\agent-study-backend-0.1.0-SNAPSHOT.jar
```

## 常见问题

- `Port 8080 was already in use`：已有后端进程占用端口，停止旧进程后重新启动。
- 真实 LLM 仍返回 Mock：确认 `AGENT_STUDY_LLM_PROVIDER=openai-compatible`，并在设置环境变量后重启后端。
- GLM 返回 401/403：检查 API Key 是否正确、是否有权限、是否复制了多余空格。
- GLM 返回 400：检查模型名是否是当前账号可用的模型 ID。
- GLM 返回 404：检查 `AGENT_STUDY_LLM_BASE_URL` 是否完整包含 `/chat/completions`。
