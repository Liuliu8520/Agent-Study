# Agent_Study

基于大模型的个性化高数学习多智能体系统。项目提供学生端学习闭环、后台管理台、Prompt 管理、RAG 知识库、Agent 调用日志、MySQL/Redis 持久化和可切换的真实 LLM 调用。

## 功能概览

- 学生端学习闭环：创建学习会话、诊断题、薄弱点识别、3 天学习计划、RAG 微讲义、练习题生成、表达式自动判卷、智能复习或结业结果。
- 多智能体编排：诊断 Agent、规划 Agent、讲义 Agent、练习 Agent、复习 Agent 通过统一 `AgentRuntimeService` 执行。
- LLM 调用：默认使用 `MockLlmClient`，可通过环境变量切换到 OpenAI-compatible 接口，例如 GLM Chat Completions。
- RAG 知识库：支持知识切片持久化、后台维护、Hash Embedding 向量检索和关键词加权检索。
- 后台管理：JWT 登录、Prompt 模板编辑、Prompt 版本启用、知识库管理、操作审计、Agent 调用日志、学习统计。
- 可观测性：Agent 日志记录模型名、Token 消耗、耗时、状态和失败原因；LLM 调用失败会返回分类错误。
- 持久化：默认内存模式可快速启动；`dev` profile 使用 MySQL 保存业务数据，并使用 Redis 缓存热点学习会话。

## 技术栈

- 后端：Java 17、Spring Boot 3.3、Spring Web、Spring Security、Spring Validation、Spring Data Redis、MySQL、Springdoc OpenAPI、exp4j。
- 前端：Vue 3、Vite、Vue Router、Pinia、Markdown-it、KaTeX。
- 基础设施：Docker Compose、MySQL 8、Redis 7。

## 项目结构

```text
Agent_Study/
├── backend/                 Spring Boot 后端服务
├── frontend/                Vue 3 学生端和后台管理端
├── docker/                  MySQL、Redis 本地开发环境
├── docs/                    API、数据库、Prompt、RAG 等补充文档
├── Agent.md                 Codex/Agent 协作说明
└── README.md                项目总览和启动说明
```

## 快速启动

### 1. 启动后端：内存模式

内存模式不依赖 MySQL/Redis，适合快速查看接口和页面流程。

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" "-Dmaven.repo.local=D:\Users\Desktop\NUIT_STUDY\Agent_Study\.m2\repository" spring-boot:run
```

### 2. 启动后端：MySQL/Redis 模式

如果使用项目自带 Docker 环境：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study
docker compose -f .\docker\docker-compose.yml up -d

cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" "-Dmaven.repo.local=D:\Users\Desktop\NUIT_STUDY\Agent_Study\.m2\repository" spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Docker 默认连接信息：

```text
MySQL: localhost:3306/agent_study
MySQL 用户名: root
MySQL 密码: agentstudy
Redis: localhost:6379
```

如果使用本机已有 MySQL，可以在启动后端前覆盖环境变量：

```powershell
$env:AGENT_STUDY_MYSQL_USERNAME="root"
$env:AGENT_STUDY_MYSQL_PASSWORD="<你的本机 MySQL 密码>"
```

### 3. 可选：启用 GLM 真实 LLM 调用

默认配置会走 Mock LLM。要调用 GLM，需要在启动后端前设置以下环境变量。不要把真实 API Key 写入配置文件或提交到 Git。

```powershell
$env:AGENT_STUDY_LLM_PROVIDER="openai-compatible"
$env:AGENT_STUDY_LLM_BASE_URL="https://open.bigmodel.cn/api/paas/v4/chat/completions"
$env:AGENT_STUDY_LLM_API_KEY="<你的 GLM API Key>"
$env:AGENT_STUDY_LLM_MODEL="glm-4-flash-250414"
$env:AGENT_STUDY_LLM_TEMPERATURE="0.2"
$env:AGENT_STUDY_LLM_MAX_TOKENS="1024"
$env:AGENT_STUDY_LLM_TIMEOUT_SECONDS="60"
$env:AGENT_STUDY_LLM_FALLBACK_TO_MOCK="false"
```

环境变量只会在后端进程启动时读取；如果修改了模型名或 API Key，需要重启 Spring Boot。

### 4. 启动前端

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\frontend
npm.cmd install --cache ..\.npm-cache
npm.cmd run dev
```

访问地址：

```text
学生端: http://localhost:5173/student
后台端: http://localhost:5173/admin
```

后台默认账号：

```text
admin / agentstudy
```

## 验证方式

后端健康检查：

```text
GET http://localhost:8080/api/health
```

接口文档：

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs/agent-study
```

后端测试：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" "-Dmaven.repo.local=D:\Users\Desktop\NUIT_STUDY\Agent_Study\.m2\repository" test
```

前端构建：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\frontend
npm.cmd run build
```

## 文档索引

- [后端说明](backend/README.md)
- [前端说明](frontend/README.md)
- [API 文档](docs/api.md)
- [后台 API 文档](docs/admin-api.md)
- [Agent API 文档](docs/agent-api.md)
- [RAG API 文档](docs/rag-api.md)
- [统计 API 文档](docs/statistics-api.md)
- [数据库文档](docs/database.md)
- [Prompt 模板文档](docs/prompt-templates.md)

## 后续待办

- 增加前端自动化测试或关键流程 E2E 测试。
- 增加 LLM 请求重试、限流退避和失败告警策略。
- 将本地 Hash Embedding 抽象为可替换接口，后续可接入真实 Embedding 服务。
- 增加生产部署配置示例，例如 Nginx、Docker 镜像、环境变量清单和数据库初始化步骤。
