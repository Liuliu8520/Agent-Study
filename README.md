# Agent_Study

基于大模型的个性化高数学习多智能体系统。

项目定位：面向 Java 后端 / Agent 工程方向实习简历的完整作品，优先做出可运行、可演示、可讲清楚技术取舍的学生端学习闭环。

## 项目结构

```text
Agent_Study/
├── backend/   Spring Boot 后端
├── frontend/  前端预留目录
├── docker/    MySQL、Redis 本地开发环境
├── docs/      API、数据库、Prompt 文档
└── Agent.md   开发协作说明
```

## 已完成能力

- 后端 Spring Boot 基础骨架
- 统一响应、业务异常、全局异常处理
- 学习会话创建与查询
- Step 1 诊断题生成与弱点识别
- Step 2 根据弱点生成 3 天学习计划
- Step 3 RAG 微讲义生成入口
- Step 4 练习题生成与表达式自动判卷
- Step 5 智能复习或结业结果生成
- MySQL 持久化 `LearningState` 快照
- Redis 缓存热点学习会话

## 后端运行

默认内存模式，不需要数据库：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" spring-boot:run
```

MySQL/Redis 模式：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study
docker compose -f .\docker\docker-compose.yml up -d

cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" spring-boot:run "-Dspring-boot.run.profiles=dev"
```

健康检查：

```text
GET http://localhost:8080/api/health
```

测试：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" test
```
