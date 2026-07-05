# OpenAPI 文档

后端已接入 `springdoc-openapi`，启动服务后可访问：

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs/agent-study
```

当前 OpenAPI 分组：

- `agent-study`: 匹配 `/api/**` 下的业务接口。

主要用途：

- 面试或演示时快速展示 REST API。
- 前端开发时查看请求参数和响应结构。
- 后续可以导出 OpenAPI JSON 给接口调试工具使用。
