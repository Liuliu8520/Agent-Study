# RAG API

接口前缀：

```text
/api/rag
```

## 查询知识切片

```http
GET /api/rag/chunks
```

返回当前可用的知识切片列表。

## 查询单个知识切片

```http
GET /api/rag/chunks/{chunkId}
```

示例：

```http
GET /api/rag/chunks/chunk-chain-rule
```

## 按关键词检索

```http
POST /api/rag/retrieve
Content-Type: application/json
```

请求体：

```json
{
  "keywords": ["chain_rule"],
  "limit": 2
}
```

响应体：

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": "chunk-chain-rule",
      "chapter": "导数与微分",
      "title": "链式法则",
      "content": "...",
      "score": 3.0
    }
  ]
}
```

说明：

- 默认 profile 使用内存知识库仓库，方便测试和快速启动。
- `dev` profile 使用 MySQL `knowledge_chunk` 表。
- `SimpleRagService` 当前使用关键词打分，后续可以替换为 Embedding/向量检索。
