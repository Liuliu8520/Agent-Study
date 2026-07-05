# API 文档

本文档记录当前后端主要 REST API。所有业务响应统一使用：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

## 学习会话

### 创建学习会话

```http
POST /api/learn/sessions
Content-Type: application/json
```

```json
{
  "studentName": "Alice"
}
```

`studentName` 可省略或为空，后端会默认使用 `anonymous`。

### 查询学习会话列表

```http
GET /api/learn/sessions?studentName=Alice&status=IN_PROGRESS&limit=20
```

查询参数：

- `studentName`: 可选，按学生名精确匹配，忽略大小写。
- `status`: 可选，支持 `CREATED`、`IN_PROGRESS`、`FINISHED`。
- `limit`: 可选，默认 20，服务端限制在 1 到 100。

### 查询单个学习会话

```http
GET /api/learn/sessions/{sessionId}
```

## 学习闭环

### Step 1 生成诊断题

```http
POST /api/learn/sessions/{sessionId}/diagnosis/questions
```

生成内置高数诊断题，响应不会返回正确答案。

### Step 1 提交诊断答案

```http
POST /api/learn/sessions/{sessionId}/diagnosis/submit
Content-Type: application/json
```

```json
{
  "answers": [
    {
      "questionId": "limit-basic",
      "selectedOption": "A"
    }
  ]
}
```

提交后会识别 `weakPoints`，推进到 Step 2，并记录 `diagnosis.default` Agent 调用日志。

### Step 2 生成学习计划

```http
POST /api/learn/sessions/{sessionId}/plan
```

根据薄弱点生成 3 天学习计划，并记录 `planner.three-day` Agent 调用日志。

### Step 3 生成 RAG 微讲义

```http
POST /api/learn/sessions/{sessionId}/lesson
```

基于知识切片检索结果生成 Markdown 微讲义，并记录 `lesson.micro` Agent 调用日志。

### Step 4 生成练习题

```http
POST /api/learn/sessions/{sessionId}/exercises
```

生成可自动判卷的表达式练习题，并记录 `exercise.generate` Agent 调用日志。

### Step 4 提交练习答案

```http
POST /api/learn/sessions/{sessionId}/exercises/submit
Content-Type: application/json
```

```json
{
  "answers": [
    {
      "questionId": "exercise-chain-derivative",
      "answerExpression": "2*x*cos(x^2)"
    }
  ]
}
```

后端使用表达式采样比对进行自动判卷。

### 查询练习提交记录

```http
GET /api/learn/sessions/{sessionId}/exercise-attempts
```

返回该学习会话的练习提交记录，包含正确数、总题数、错误率、判卷明细和提交时间。

### Step 5 生成复习或结业结果

```http
POST /api/learn/sessions/{sessionId}/review
```

根据错误率返回 `COMPLETED`、`REVIEW_SUGGESTED` 或 `NEEDS_REINFORCEMENT`，并记录 `review.feedback` Agent 调用日志。

## Agent

```http
GET /api/agent/prompts
PUT /api/agent/prompts/{code}
POST /api/agent/mock-chat
GET /api/agent/call-logs?limit=20
GET /api/agent/call-logs/{callId}
```

详见 [agent-api.md](agent-api.md)。

## RAG

```http
GET /api/rag/chunks
GET /api/rag/chunks/{chunkId}
POST /api/rag/retrieve
```

详见 [rag-api.md](rag-api.md)。

## Statistics

```http
GET /api/statistics/dashboard
```

详见 [statistics-api.md](statistics-api.md)。
