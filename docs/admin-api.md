# Admin API

后台接口前缀：

```text
/api/admin
```

除登录接口外，`/api/admin/**` 都需要 Bearer Token。

当前接入后台权限的管理操作：

- `PUT /api/agent/prompts/{code}` 新增或更新 Prompt 模板。
- `GET /api/admin/prompts/{code}/versions` 查询 Prompt 模板版本历史。
- `POST /api/admin/prompts/{code}/versions/{versionId}/activate` 激活指定 Prompt 历史版本。
- `POST /api/admin/rag/chunks` 新增知识切片并生成 embedding。
- `PUT /api/admin/rag/chunks/{chunkId}` 更新知识切片并重新生成 embedding。
- `DELETE /api/admin/rag/chunks/{chunkId}` 软删除知识切片。
- `POST /api/admin/rag/chunks/{chunkId}/embedding` 重新生成知识切片 embedding。
- `GET /api/admin/operation-logs` 查询后台操作审计日志。

## 管理员登录

```http
POST /api/admin/auth/login
Content-Type: application/json
```

请求体：

```json
{
  "username": "admin",
  "password": "agentstudy"
}
```

响应体：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "tokenType": "Bearer",
    "accessToken": "jwt-token",
    "expiresInSeconds": 7200,
    "username": "admin"
  }
}
```

## 查询当前管理员

```http
GET /api/admin/me
Authorization: Bearer jwt-token
```

响应体：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "username": "admin",
    "role": "ADMIN"
  }
}
```

## Prompt 版本管理

```http
GET /api/admin/prompts/{code}/versions
Authorization: Bearer jwt-token
```

```http
POST /api/admin/prompts/{code}/versions/{versionId}/activate
Authorization: Bearer jwt-token
```

## RAG 知识库管理

```http
POST /api/admin/rag/chunks
PUT /api/admin/rag/chunks/{chunkId}
DELETE /api/admin/rag/chunks/{chunkId}
POST /api/admin/rag/chunks/{chunkId}/embedding
Authorization: Bearer jwt-token
```

新增或更新请求体：

```json
{
  "id": "chunk-custom",
  "chapter": "导数与微分",
  "title": "链式法则补充",
  "content": "复合函数求导需要从外层到内层逐层求导。",
  "tags": ["chain_rule", "derivative"]
}
```

## 操作审计

```http
GET /api/admin/operation-logs?targetType=PROMPT_TEMPLATE&limit=20
Authorization: Bearer jwt-token
```

可选筛选参数：`operator`、`action`、`targetType`、`targetId`、`limit`。

## 本地配置

默认本地管理员：

```text
username: admin
password: agentstudy
```

可通过环境变量覆盖：

- `AGENT_STUDY_ADMIN_USERNAME`
- `AGENT_STUDY_ADMIN_PASSWORD`
- `AGENT_STUDY_ADMIN_JWT_SECRET`
- `AGENT_STUDY_ADMIN_TOKEN_TTL_MINUTES`

默认账号只用于本地开发演示；部署或提交演示视频前应改成自己的密码和 JWT secret。
