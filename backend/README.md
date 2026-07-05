# Backend

Agent_Study 后端服务，基于 Spring Boot 3 + Java 17。

## 当前状态

已完成学生端 P0 学习闭环：

- `GET /api/health` 健康检查
- `POST /api/learn/sessions` 创建学习会话
- `GET /api/learn/sessions/{sessionId}` 查询学习会话
- `POST /api/learn/sessions/{sessionId}/diagnosis/questions` Step 1 生成诊断题
- `POST /api/learn/sessions/{sessionId}/diagnosis/submit` Step 1 提交诊断答案
- `POST /api/learn/sessions/{sessionId}/plan` Step 2 生成 3 天学习计划
- `POST /api/learn/sessions/{sessionId}/lesson` Step 3 生成 RAG 微讲义
- `POST /api/learn/sessions/{sessionId}/exercises` Step 4 生成练习题
- `POST /api/learn/sessions/{sessionId}/exercises/submit` Step 4 表达式自动判卷
- `POST /api/learn/sessions/{sessionId}/review` Step 5 生成智能复习或结业结果

持久化状态：

- 默认 profile 使用 `InMemoryLearningSessionRepository`，方便本地测试和快速启动。
- `dev` profile 使用 `MySqlRedisLearningSessionRepository`，MySQL 保存 `LearningState` 快照，Redis 缓存热点会话。
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
