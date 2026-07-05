# Admin API

后台接口前缀：

```text
/api/admin
```

除登录接口外，`/api/admin/**` 都需要 Bearer Token。

当前接入后台权限的管理操作：

- `PUT /api/agent/prompts/{code}` 新增或更新 Prompt 模板。

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
