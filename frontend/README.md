# Frontend

Vue 3 + Vite 前端，包含学生端学习闭环和后台管理台。

## 功能

- 学生端：创建学习会话、诊断、学习计划、RAG 微讲义、练习判卷、复习结果、练习记录。
- 后台端：管理员登录、Prompt 模板编辑、Prompt 版本启用、RAG 知识库管理、操作审计、Agent 调用日志、统计概览。

## 启动

先启动后端：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" spring-boot:run
```

再启动前端：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\frontend
npm.cmd install --cache ..\.npm-cache
npm.cmd run dev
```

访问：

```text
http://localhost:5173
```

开发环境下 Vite 会把 `/api` 代理到 `http://localhost:8080`。
