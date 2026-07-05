# Agent API

接口前缀：

```text
/api/agent
```

## 查询 Prompt 模板

```http
GET /api/agent/prompts
```

返回当前内置的 5 个 Prompt 模板。

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

默认最多返回 20 条，服务端会把 `limit` 限制在 1 到 100 之间。

## 查询单条 Agent 调用日志

```http
GET /api/agent/call-logs/{callId}
```

返回渲染后的 Prompt、模型输出、状态、错误信息和耗时。
