# API 文档

本文件用于记录学生端和管理端 REST API。

## 学生端

前缀：`/api/learn`

### 创建学习会话

```http
POST /api/learn/sessions
Content-Type: application/json
```

请求体：

```json
{
  "studentName": "Alice"
}
```

`studentName` 字段可省略或为空。为空时后端默认使用 `anonymous`。

响应示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "sessionId": "uuid",
    "studentName": "Alice",
    "currentStep": 1,
    "status": "CREATED",
    "createdAt": "2026-07-05T00:00:00Z",
    "updatedAt": "2026-07-05T00:00:00Z",
    "nextAction": "diagnosis"
  }
}
```

### 查询学习会话

```http
GET /api/learn/sessions/{sessionId}
```

响应结构同创建学习会话。

如果会话不存在，返回：

```json
{
  "code": -1,
  "message": "Learning session not found: missing-session",
  "data": null
}
```

### 生成诊断题

```http
POST /api/learn/sessions/{sessionId}/diagnosis/questions
```

响应示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "sessionId": "uuid",
    "currentStep": 1,
    "nextAction": "submitDiagnosis",
    "questions": [
      {
        "id": "limit-basic",
        "topicCode": "limit",
        "topicName": "极限",
        "difficulty": 1,
        "stem": "计算 lim_{x->0} sin(x) / x 的值。",
        "options": [
          {
            "key": "A",
            "text": "1"
          }
        ]
      }
    ]
  }
}
```

说明：

- 第一版使用内置确定性题库，共 5 道题。
- 题目覆盖极限、导数、积分。
- 响应不会返回正确答案。

### 提交诊断答案

```http
POST /api/learn/sessions/{sessionId}/diagnosis/submit
Content-Type: application/json
```

请求体：

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

响应示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "sessionId": "uuid",
    "correctCount": 4,
    "totalCount": 5,
    "weakPoints": [
      {
        "code": "chain_rule",
        "name": "链式法则",
        "reason": "复合函数求导时遗漏内层函数的导数"
      }
    ],
    "currentStep": 2,
    "nextAction": "plan"
  }
}
```

说明：

- 提交前必须先调用生成诊断题接口。
- 提交成功后会将 `LearningState.currentStep` 推进到 2。
- Step 2 的下一步动作是生成学习计划。

### 生成学习计划

```http
POST /api/learn/sessions/{sessionId}/plan
```

响应示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "sessionId": "uuid",
    "plan": {
      "title": "3 天高数薄弱点补强计划",
      "days": [
        {
          "day": 1,
          "goal": "回顾薄弱知识点的定义、公式和适用条件",
          "concepts": ["链式法则"],
          "practiceSuggestions": [
            "整理错题原因",
            "为每个薄弱点写出 1 个公式和 1 个典型例题"
          ]
        }
      ]
    },
    "currentStep": 3,
    "nextAction": "lesson"
  }
}
```

说明：

- 生成计划前必须先完成 Step 1 诊断答案提交。
- 第一版使用确定性规则根据 `weakPoints` 生成 3 天计划。
- 如果诊断全部正确，则生成巩固提升计划。
- 生成成功后会将 `LearningState.currentStep` 推进到 3。

### 生成 RAG 微讲义

```http
POST /api/learn/sessions/{sessionId}/lesson
```

响应示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "sessionId": "uuid",
    "lessonMarkdown": "# 个性化高数微讲义\n\n## 学习目标\n...",
    "retrievedChunks": [
      {
        "id": "chunk-chain-rule",
        "chapter": "导数与微分",
        "title": "链式法则",
        "content": "复合函数求导需要先对外层函数求导...",
        "score": 6.0
      }
    ],
    "currentStep": 4,
    "nextAction": "exercises"
  }
}
```

说明：

- 生成微讲义前必须先完成 Step 2 学习计划。
- 第一版使用内置知识切片和关键词召回，形成 RAG 接口形态。
- 响应包含 Markdown 讲义和召回切片，便于前端展示和后台调试。
- 生成成功后会将 `LearningState.currentStep` 推进到 4。

### 生成随堂练习

```http
POST /api/learn/sessions/{sessionId}/exercises
```

响应示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "sessionId": "uuid",
    "currentStep": 4,
    "nextAction": "submitExercises",
    "questions": [
      {
        "id": "exercise-chain-derivative",
        "stem": "求函数 f(x)=sin(x^2) 的导数，答案输入关于 x 的表达式。",
        "knowledgePoint": "链式法则"
      }
    ]
  }
}
```

说明：

- 生成练习前必须先完成 Step 3 微讲义生成。
- 响应不会返回标准答案。
- 第一版使用确定性题库，后续可替换为 ExerciseAgent。

### 提交练习答案并自动判卷

```http
POST /api/learn/sessions/{sessionId}/exercises/submit
Content-Type: application/json
```

请求体：

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

响应示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "sessionId": "uuid",
    "correctCount": 3,
    "totalCount": 3,
    "errorRate": 0.0,
    "results": [
      {
        "questionId": "exercise-chain-derivative",
        "studentAnswer": "2*x*cos(x^2)",
        "standardAnswer": "2*x*cos(x^2)",
        "correct": true,
        "detail": "Expression matched on all sample points"
      }
    ],
    "currentStep": 5,
    "nextAction": "review"
  }
}
```

说明：

- 判卷使用 exp4j 解析表达式。
- 第一版支持单变量 `x`。
- 系统通过多个采样点比较标准答案和学生答案是否近似等价。
- 提交成功后会将 `LearningState.currentStep` 推进到 5。

### 生成智能复习结果

```http
POST /api/learn/sessions/{sessionId}/review
```

响应示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "sessionId": "uuid",
    "status": "NEEDS_REINFORCEMENT",
    "message": "本次练习错误率较高，建议先完成 2 道同类型变式题再进入下一轮学习。",
    "errorRate": 0.67,
    "suggestions": [
      "优先复盘错题对应的公式和解题步骤。"
    ],
    "variantExercises": [
      {
        "id": "variant-chain-derivative",
        "stem": "求函数 f(x)=cos(x^2) 的导数，答案输入关于 x 的表达式。",
        "knowledgePoint": "链式法则"
      }
    ],
    "currentStep": 5,
    "nextAction": "finished",
    "finished": true
  }
}
```

规则：

- 错误率 `>= 50%`：返回 `NEEDS_REINFORCEMENT`，生成 2 道同类型变式题。
- 错误率 `> 0` 且 `< 50%`：返回 `REVIEW_SUGGESTED`，生成针对性复习建议。
- 全部正确：返回 `COMPLETED`，输出结业鼓励语。
- 生成成功后会将学习会话状态标记为 `FINISHED`。

## 管理端

前缀：`/api/admin`

待实现。
