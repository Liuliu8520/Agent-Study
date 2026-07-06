# Agent_Study

基于大模型的个性化高数学习多智能体系统。

项目定位：面向 Java 后端 / Agent 工程方向实习简历的完整作品，优先做出可运行、可演示、可讲清楚技术取舍的学生端学习闭环。

## 项目结构

```text
Agent_Study/
├── backend/   Spring Boot 后端
├── frontend/  Vue3 学生端与后台管理端
├── docker/    MySQL、Redis 本地开发环境
├── docs/      API、数据库、Prompt 文档
└── Agent.md   开发协作说明
```

## 已完成能力

- 后端 Spring Boot 基础骨架
- 统一响应、业务异常、全局异常处理
- OpenAPI/Swagger UI 接口文档
- Spring Security + JWT 后台鉴权
- 学习会话创建与查询
- 学习会话列表查询，支持学生名、状态和 limit 筛选
- Step 1 诊断题生成与弱点识别
- Step 2 根据弱点生成 3 天学习计划
- Step 3 RAG 微讲义生成入口
- Step 4 练习题生成与表达式自动判卷
- `exercise_attempt` 持久化练习提交记录和判卷明细
- Step 5 智能复习或结业结果生成
- MySQL 持久化 `LearningState` 快照
- Redis 缓存热点学习会话
- `PromptService` 内置 5 类 Agent Prompt 模板
- `prompt_template` 支撑 Prompt 模板持久化和在线更新，写接口已接入后台 JWT 鉴权
- `prompt_template_version` 支撑 Prompt 模板版本历史和回滚启用
- `MockLlmClient` 支撑无 API Key 的 Agent 调用演示
- 可配置 OpenAI-compatible 真实 LLM Client，并保留 Mock fallback
- `AgentCallLog` 记录 Prompt、模型输出、状态和耗时
- Agent 调用日志支持按会话、Agent 类型、状态和 Prompt 编码筛选
- `knowledge_chunk` 支撑 RAG 知识切片持久化、后台管理和向量检索
- `operation_log` 记录 Prompt 和知识库后台操作审计
- `statistics` 模块提供学习会话、薄弱点和 Agent 调用统计
- Step 1 诊断结果分析已接入 `diagnosis.default` Agent 调用日志
- Step 2 学习计划生成已接入 `planner.three-day` Agent 调用日志
- Step 3 RAG 微讲义生成已接入 `lesson.micro` Agent 调用日志
- Step 4 练习题生成已接入 `exercise.generate` Agent 调用日志
- Step 5 智能复习生成已接入 `review.feedback` Agent 调用日志
- Vue3 前端学生端支持步骤条式学习流程、多智能体工作台日志、Markdown/KaTeX 微讲义渲染和数学表达式快捷输入
- Vue3 后台端支持 Prompt 管理、版本启用、RAG 知识库维护、操作审计、Agent 调用日志和统计洞察

## 后端运行

默认内存模式，不需要数据库：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" "-Dmaven.repo.local=D:\Users\Desktop\NUIT_STUDY\Agent_Study\.m2\repository" spring-boot:run
```

MySQL/Redis 模式：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study
docker compose -f .\docker\docker-compose.yml up -d

cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
$env:AGENT_STUDY_MYSQL_USERNAME="root"
$env:AGENT_STUDY_MYSQL_PASSWORD="<你的 MySQL 密码>"
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" "-Dmaven.repo.local=D:\Users\Desktop\NUIT_STUDY\Agent_Study\.m2\repository" spring-boot:run "-Dspring-boot.run.profiles=dev"
```

健康检查：

```text
GET http://localhost:8080/api/health
```

接口文档：

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs/agent-study
```

测试：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" test
```

## 前端运行

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\frontend
npm.cmd install --cache ..\.npm-cache
npm.cmd run dev
```

访问：

```text
学生端：http://localhost:5173
后台端：http://localhost:5173/admin
```

后台默认账号：

```text
admin / agentstudy
```
