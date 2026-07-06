# Frontend

Vue 3 + Vite 前端，包含学生端学习闭环和后台管理台。

## 功能

- 学生端：步骤条式学习流程、创建学习会话、诊断、学习计划、RAG 微讲义、练习判卷、复习结果、练习记录。
- 学生端增强：多智能体工作台日志、Markdown/KaTeX 微讲义渲染、数学表达式快捷输入。
- 后台端：管理员登录、Prompt 模板编辑、Prompt 版本启用、RAG 知识库管理、操作审计、Agent 调用日志、统计概览。
- 后台端增强：薄弱点排行、Agent 平均耗时条形图。

## 启动

先启动后端：

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" "-Dmaven.repo.local=D:\Users\Desktop\NUIT_STUDY\Agent_Study\.m2\repository" spring-boot:run
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
http://localhost:5173/admin
```

开发环境下 Vite 会把 `/api` 代理到 `http://localhost:8080`。
