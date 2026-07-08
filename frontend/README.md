# Frontend

Agent_Study 前端基于 Vue 3 + Vite，包含学生端学习工作台和后台管理台。

## 页面入口

```text
/student  学生端学习工作台
/admin    后台管理台
```

根路径 `/` 会重定向到 `/student`。

## 功能说明

学生端：

- 创建学习会话。
- 按步骤完成诊断、学习计划、RAG 微讲义、练习判卷、复习结果。
- 展示多智能体工作台日志。
- 支持 Markdown/KaTeX 微讲义渲染。
- 支持数学表达式快捷输入和练习提交记录查看。

后台端：

- 管理员登录。
- Prompt 模板编辑和版本启用。
- RAG 知识库新增、编辑、删除和 Embedding 重建。
- 操作审计、Agent 调用日志、学习统计概览。
- Agent 调用日志展示模型名、Token、状态、耗时和失败原因。
- 展示薄弱点排行和 Agent 平均耗时。

## 启动

先启动后端，默认要求后端监听 `http://localhost:8080`。

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\frontend
npm.cmd install --cache ..\.npm-cache
npm.cmd run dev
```

访问：

```text
http://localhost:5173/student
http://localhost:5173/admin
```

开发环境下 Vite 会把 `/api` 代理到 `http://localhost:8080`。

## 后端地址配置

默认情况下前端请求相对路径 `/api`，由 Vite dev server 代理到后端。

如果需要直接指定后端地址，可以设置：

```powershell
$env:VITE_API_BASE_URL="http://localhost:8080"
npm.cmd run dev
```

## 构建和预览

```powershell
npm.cmd run build
npm.cmd run preview
```

构建产物输出到 `dist/`，该目录不会提交到 Git。

## 主要文件

```text
src/router/index.js                  路由配置
src/api/client.js                    请求封装
src/api/learn.js                     学习流程 API
src/api/agent.js                     Agent/Prompt API
src/api/rag.js                       RAG API
src/api/admin.js                     后台登录和审计 API
src/api/statistics.js                统计 API
src/views/student/StudentWorkspace.vue
src/views/admin/AdminDashboard.vue
src/styles/main.css
```

## 本地排查

- 页面没有数据：先确认后端已启动，并访问 `http://localhost:8080/api/health`。
- 后台登录失败：确认账号密码是否为后端当前配置，默认是 `admin / agentstudy`。
- LLM 内容没有变化：真实模型配置在后端完成，前端只展示后端返回结果。
- 数学公式不渲染：确认依赖已安装，并重新执行 `npm.cmd install --cache ..\.npm-cache`。
