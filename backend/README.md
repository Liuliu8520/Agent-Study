# Backend

Spring Boot 后端服务目录。

## 当前状态

已完成最小可运行 Spring Boot 后端骨架：

- 应用启动类：`com.agentstudy.AgentStudyApplication`
- 健康检查接口：`GET /api/health`
- 统一响应结构：`ApiResponse`
- 全局异常处理：`GlobalExceptionHandler`
- 基础配置：`application.yml`
- 启动上下文测试：`AgentStudyApplicationTests`

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
