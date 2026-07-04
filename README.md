# Agent_Study

基于大模型的个性化高数学习多智能体系统。

本项目采用前后端分离架构：

- backend：Spring Boot 后端，负责学习流程编排、Agent 调用、RAG 检索、表达式判卷、后台鉴权和日志统计。
- frontend：Vue3 前端，包含学生端学习驾驶舱和管理端 Admin Dashboard。
- docker：MySQL、Redis 等本地开发依赖。
- docs：接口、数据库、Prompt 模板等工程文档。

当前阶段先搭建基础目录，后续优先完成 P0 学生端 5 步学习闭环。

## 后端启动

当前后端已具备最小可运行 Spring Boot 骨架。

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" spring-boot:run
```

健康检查：

```text
GET http://localhost:8080/api/health
```

构建测试：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" test
```
