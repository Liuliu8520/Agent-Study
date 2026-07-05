# Statistics API

接口前缀：

```text
/api/statistics
```

## 查询学习统计仪表盘

```http
GET /api/statistics/dashboard
```

返回学习会话状态、薄弱点分布，以及最近 100 次 Agent 调用统计。

响应示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "sessions": {
      "totalSessions": 12,
      "createdSessions": 1,
      "inProgressSessions": 8,
      "finishedSessions": 3
    },
    "weakPoints": [
      {
        "code": "chain_rule",
        "name": "链式法则",
        "latestReason": "复合函数求导时遗漏内层函数的导数",
        "count": 5
      }
    ],
    "agentCalls": {
      "sampleSize": 20,
      "successCount": 20,
      "failedCount": 0,
      "successRate": 1.0,
      "averageDurationMillis": 3.5,
      "byAgentType": [
        {
          "agentType": "DIAGNOSTICIAN",
          "callCount": 5,
          "averageDurationMillis": 2.0
        }
      ]
    }
  }
}
```

说明：

- `sessions` 来自学习会话仓库。
- `weakPoints` 按 `WeakPoint.code` 聚合，并按出现次数倒序排序。
- `agentCalls` 基于最近 100 条 `AgentCallLog` 计算，适合作为演示版可观测性统计。
