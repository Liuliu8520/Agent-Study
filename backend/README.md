# Backend

Spring Boot 后端服务目录。

## 当前状态

已完成最小可运行 Spring Boot 后端骨架：

- 应用启动类：`com.agentstudy.AgentStudyApplication`
- 健康检查接口：`GET /api/health`
- 学习会话创建接口：`POST /api/learn/sessions`
- 学习会话查询接口：`GET /api/learn/sessions/{sessionId}`
- 诊断题生成接口：`POST /api/learn/sessions/{sessionId}/diagnosis/questions`
- 诊断答案提交接口：`POST /api/learn/sessions/{sessionId}/diagnosis/submit`
- 学习计划生成接口：`POST /api/learn/sessions/{sessionId}/plan`
- RAG 微讲义生成接口：`POST /api/learn/sessions/{sessionId}/lesson`
- 随堂练习生成接口：`POST /api/learn/sessions/{sessionId}/exercises`
- 练习答案提交与判卷接口：`POST /api/learn/sessions/{sessionId}/exercises/submit`
- 智能复习生成接口：`POST /api/learn/sessions/{sessionId}/review`
- 统一响应结构：`ApiResponse`
- 全局异常处理：`GlobalExceptionHandler`
- 业务异常：`BusinessException`
- 内存版学习状态仓库：`InMemoryLearningSessionRepository`
- 学习流程编排器：`LearningOrchestrator`
- 基础配置：`application.yml`
- 启动上下文测试：`AgentStudyApplicationTests`
- 学习会话接口测试：`LearningControllerTests`

## 本地运行

当前机器可使用 C 盘 Maven：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" test
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" spring-boot:run
```

启动后访问：

```text
http://localhost:8080/api/health
```

预期响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "status": "ok",
    "service": "agent-study-backend"
  }
}
```

也可以先打包再运行：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" package
java -jar .\target\agent-study-backend-0.1.0-SNAPSHOT.jar
```

## 模块规划

计划模块：

- common：统一响应、异常处理、分页与工具类。
- config：Spring、MyBatis、Redis、WebClient 等配置。
- security：Spring Security、JWT、管理员鉴权。
- learn：学生端学习流程、LearningState、LearningOrchestrator。
- agent：LLM 调用、PromptService、5 个 Agent、调用日志。
- rag：知识库切片、Embedding、向量检索。
- admin：后台登录、Prompt 管理、操作审计。
- statistics：薄弱点统计、Agent 耗时统计、会话趋势。
- log：学习会话快照、Agent 调用日志、操作日志查询。

## 已实现接口

创建学习会话：

```http
POST /api/learn/sessions
```

请求体：

```json
{
  "studentName": "Alice"
}
```

查询学习会话：

```http
GET /api/learn/sessions/{sessionId}
```

生成诊断题：

```http
POST /api/learn/sessions/{sessionId}/diagnosis/questions
```

提交诊断答案：

```http
POST /api/learn/sessions/{sessionId}/diagnosis/submit
```

生成学习计划：

```http
POST /api/learn/sessions/{sessionId}/plan
```

生成 RAG 微讲义：

```http
POST /api/learn/sessions/{sessionId}/lesson
```

生成随堂练习：

```http
POST /api/learn/sessions/{sessionId}/exercises
```

提交练习答案：

```http
POST /api/learn/sessions/{sessionId}/exercises/submit
```

生成智能复习结果：

```http
POST /api/learn/sessions/{sessionId}/review
```
