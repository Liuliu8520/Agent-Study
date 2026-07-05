# Agent API

接口前缀：

```text
/api/agent
```

## 查询 Prompt 模板

```http
GET /api/agent/prompts
```

返回当前可用的 Prompt 模板。默认会初始化 5 个内置模板，后续通过接口新增的模板也会出现在列表中。

## 新增或更新 Prompt 模板

```http
PUT /api/agent/prompts/{code}
Content-Type: application/json
Authorization: Bearer jwt-token
```

请求体：

```json
{
  "agentType": "LESSON_GENERATOR",
  "version": "v2",
  "name": "Custom Lesson Agent",
  "systemPrompt": "你是高数微讲义生成 Agent。",
  "userPromptTemplate": "请围绕 {{topic}} 生成一段讲义。"
}
```

`code` 由路径提供，同一个 code 再次提交会覆盖对应模板。模板变量使用 `{{variableName}}` 语法。

该接口属于后台管理操作，需要先通过 `POST /api/admin/auth/login` 获取管理员 Bearer Token。

## 调用 Mock Agent

```http
POST /api/agent/mock-chat
Content-Type: application/json
```

请求体：

```json
{
  "sessionId": "session-id",
  "promptCode": "lesson.micro",
  "variables": {
    "learningPlan": "3 天链式法则补强计划",
    "weakPoints": "链式法则",
    "retrievedChunks": "复合函数求导知识切片"
  }
}
```

响应体：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "callId": "uuid",
    "sessionId": "session-id",
    "agentType": "LESSON_GENERATOR",
    "promptCode": "lesson.micro",
    "promptVersion": "v1",
    "modelName": "mock-llm-v1",
    "outputText": "【Mock讲义】...",
    "durationMillis": 1,
    "createdAt": "2026-07-05T00:00:00Z"
  }
}
```

## 查询 Agent 调用日志

```http
GET /api/agent/call-logs?limit=20
```

支持筛选：

```http
GET /api/agent/call-logs?sessionId=session-id&agentType=LESSON_GENERATOR&status=SUCCESS&promptCode=lesson.micro&limit=20
```

可选参数：

- `sessionId`: 学习会话 ID。
- `agentType`: Agent 类型，例如 `DIAGNOSTICIAN`、`LESSON_GENERATOR`。
- `status`: 调用状态，`SUCCESS` 或 `FAILED`。
- `promptCode`: Prompt 模板编码。
- `limit`: 返回条数，默认 20，服务端限制在 1 到 100。

## 查询单条 Agent 调用日志

```http
GET /api/agent/call-logs/{callId}
```

返回渲染后的 Prompt、模型输出、状态、错误信息和耗时。
