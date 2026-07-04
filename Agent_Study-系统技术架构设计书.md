# 系统技术架构设计书

## 1. 总体架构

### 1.1 架构原则

系统采用前后端分离架构，后端使用单体应用、模块化分层设计，不在第一阶段拆分微服务。

选择单体模块化的原因：

- 项目目标是实习简历与可演示系统，优先保证完整闭环和工程质量。
- 业务边界清晰，可以在代码包层面拆分 learn、agent、rag、admin、statistics、log 等模块。
- 避免过早引入服务治理、注册中心、分布式事务等额外复杂度。

### 1.2 系统组成

```text
Vue3 前端
  ├── 学生端学习驾驶舱
  └── 管理端 Admin Dashboard

Spring Boot 后端
  ├── 学习流程编排
  ├── 多 Agent 调用
  ├── RAG 知识检索
  ├── Prompt 管理
  ├── 表达式自动判卷
  ├── 后台鉴权
  ├── 数据统计
  └── 全链路日志

基础设施
  ├── MySQL：业务数据持久化
  ├── Redis：学习状态、Token 黑名单、Prompt 缓存
  └── LLM API：大模型能力
```

### 1.3 推荐项目目录

```text
Agent_Study/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/agentstudy/
│       │   │   ├── AgentStudyApplication.java
│       │   │   ├── common/
│       │   │   ├── config/
│       │   │   ├── security/
│       │   │   ├── learn/
│       │   │   ├── agent/
│       │   │   ├── rag/
│       │   │   ├── admin/
│       │   │   ├── statistics/
│       │   │   └── log/
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── mapper/
│       │       └── db/
│       └── test/
├── frontend/
│   ├── package.json
│   └── src/
│       ├── api/
│       ├── router/
│       ├── stores/
│       ├── views/student/
│       ├── views/admin/
│       └── components/
├── docker/
│   └── docker-compose.yml
├── docs/
│   ├── api.md
│   ├── database.md
│   └── prompt-templates.md
├── Agent_Study-产品需求文档.md
├── Agent_Study-系统技术架构设计书.md
├── Agent_Study-简历导向需求与开发规划.md
└── README.md
```

## 2. 技术栈

| 层级 | 技术选型 | 说明 |
| :--- | :--- | :--- |
| 前端框架 | Vue 3 + Vite + TypeScript | 学生端和管理端共用一个前端项目，通过路由区分 |
| UI 组件 | Element Plus | 管理后台表格、表单、弹窗和布局 |
| 状态管理 | Pinia | 管理登录态、学习流程状态和全局配置 |
| 请求库 | Axios | 统一封装 Token、错误提示和接口响应 |
| 数据可视化 | ECharts / vue-echarts | 后台薄弱点统计、Agent 耗时统计 |
| 后端核心 | Spring Boot + Maven | REST API、业务编排、依赖管理 |
| 持久层 | MyBatis-Plus + MySQL 8 | 表结构清晰，支持分页、JSON 字段和统计查询 |
| 缓存/状态 | Redis | 学习状态、Token 黑名单、Prompt 缓存 |
| 安全 | Spring Security + JJWT | 后台 /api/admin/** 强制鉴权 |
| LLM 调用 | WebClient | 调用 GLM-4-Flash 或兼容 OpenAI 风格接口的大模型 |
| 数学判卷 | exp4j | 使用随机数代入法验证表达式近似等价 |
| 接口文档 | Knife4j / springdoc-openapi | 方便演示和调试接口 |
| 部署辅助 | Docker Compose | 启动 MySQL、Redis 等依赖 |

## 3. 后端模块设计

### 3.1 common 模块

职责：

- 统一响应结构。
- 统一异常处理。
- 分页请求与分页响应。
- 常量、枚举、工具类。

建议类：

- ApiResponse
- PageRequestDTO
- PageResult
- BusinessException
- GlobalExceptionHandler
- JsonUtils

### 3.2 security 模块

职责：

- 管理员登录鉴权。
- JWT 生成与解析。
- Token 黑名单。
- Spring Security 过滤器配置。

建议类：

- SecurityConfig
- JwtAuthenticationFilter
- JwtTokenProvider
- PasswordEncoderConfig
- AdminUserDetailsService

安全规则：

- /api/learn/** 允许匿名访问。
- /api/admin/auth/login 允许匿名访问。
- /api/admin/** 必须认证。

### 3.3 learn 模块

职责：

- 学生端学习流程。
- 创建和维护学习会话。
- 调用 LearningOrchestrator 推进 5 步流程。
- 保存学习会话、讲义、练习和复习结果。

建议类：

- LearningController
- LearningService
- LearningOrchestrator
- LearningState
- LearningSessionEntity
- ExerciseAttemptEntity
- ExpressionJudgeService

### 3.4 agent 模块

职责：

- 封装 LLM 调用。
- 管理 5 个 Agent 的输入输出。
- 动态读取 Prompt。
- 记录 Agent 调用日志。
- 校验 LLM JSON 输出。

建议类：

- AgentClient
- PromptService
- DiagnosticianAgent
- PlannerAgent
- LessonGeneratorAgent
- ExerciseAgent
- ReviewerAgent
- AgentCallLogService
- LlmJsonValidator

设计原则：

- Agent 不直接操作数据库。
- Agent 输入输出使用 DTO。
- Prompt 不硬编码在 Agent 类中。
- 每次调用都记录 agentName、promptVersion、durationMs、success、errorMessage。

### 3.5 rag 模块

职责：

- 知识库切片 CRUD。
- Embedding 生成。
- 向量相似度检索。
- 为讲义生成提供上下文。

建议类：

- KnowledgeController
- KnowledgeService
- EmbeddingClient
- VectorSearchService
- CosineSimilarity
- KnowledgeChunkEntity

第一阶段实现方式：

- MySQL 存储 chunk_text 和 embedding_json。
- Java 层从 MySQL 取出候选向量后计算余弦相似度。
- 后续可替换为专业向量数据库，但不作为第一阶段目标。

### 3.6 admin 模块

职责：

- 管理员登录登出。
- Prompt 管理。
- 知识库管理入口。
- 后台操作审计。

建议类：

- AuthController
- AdminUserService
- PromptAdminController
- OperationLogService

### 3.7 statistics 模块

职责：

- 薄弱点统计。
- 学习会话趋势。
- Agent 平均耗时统计。
- Redis 活跃会话数统计。

建议类：

- StatisticsController
- StatisticsService
- WeakPointStatDTO
- AgentDurationStatDTO

### 3.8 log 模块

职责：

- 学习会话快照查询。
- Agent 调用日志查询。
- 后台操作日志查询。
- 异常追踪。

建议类：

- SessionLogController
- AgentCallLogController
- OperationLogController

## 4. 核心状态流转

### 4.1 LearningState

LearningState 是学生一次学习流程的上下文对象，Redis 存储运行态，MySQL 存储最终结果。

建议字段：

- sessionId
- studentName
- currentStep
- diagnosisQuestions
- diagnosisAnswers
- weakPoints
- learningPlan
- retrievedChunks
- generatedLesson
- exercises
- exerciseResults
- reviewResult
- startedAt
- updatedAt

### 4.2 LearningOrchestrator

LearningOrchestrator 负责流程推进，不直接生成内容。

流程：

```text
createSession
  -> generateDiagnosisQuestions
  -> submitDiagnosisAnswers
  -> generateLearningPlan
  -> generateLessonWithRag
  -> generateExercises
  -> submitExercisesAndJudge
  -> generateReview
  -> finishSession
```

设计原则：

- 每一步只允许在前一步完成后执行。
- 每一步执行完成后更新 current_step。
- 每一步关键输出都写入 MySQL。
- Redis 中的 LearningState 用于减少流程中重复查库。

## 5. 数据库设计

### 5.1 learning_session

```sql
CREATE TABLE `learning_session` (
  `session_id` varchar(64) PRIMARY KEY,
  `student_name` varchar(50),
  `weak_points` json,
  `learning_plan` json,
  `current_step` int DEFAULT 1,
  `generated_lesson` longtext,
  `review_result` text,
  `is_finished` tinyint(1) DEFAULT 0,
  `total_duration_ms` bigint,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 5.2 exercise_attempt

```sql
CREATE TABLE `exercise_attempt` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `session_id` varchar(64) NOT NULL,
  `question_text` text NOT NULL,
  `standard_answer` varchar(500) NOT NULL,
  `student_answer` varchar(500),
  `knowledge_point` varchar(100),
  `is_correct` tinyint(1),
  `judge_detail` json,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_session_id` (`session_id`)
);
```

### 5.3 knowledge_chunk

```sql
CREATE TABLE `knowledge_chunk` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `chapter` varchar(100),
  `title` varchar(200),
  `chunk_text` text NOT NULL,
  `embedding_json` json,
  `enabled` tinyint(1) DEFAULT 1,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_chapter` (`chapter`)
);
```

### 5.4 prompt_template

```sql
CREATE TABLE `prompt_template` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `agent_name` varchar(50) NOT NULL,
  `version` varchar(30) NOT NULL,
  `system_prompt` text NOT NULL,
  `is_active` tinyint(1) DEFAULT 0,
  `operator_id` bigint,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_agent_active` (`agent_name`, `is_active`)
);
```

### 5.5 agent_call_log

```sql
CREATE TABLE `agent_call_log` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `session_id` varchar(64),
  `agent_name` varchar(50) NOT NULL,
  `prompt_version` varchar(30),
  `request_summary` text,
  `response_summary` text,
  `duration_ms` bigint,
  `success` tinyint(1) DEFAULT 1,
  `error_message` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_session_id` (`session_id`),
  INDEX `idx_agent_name` (`agent_name`)
);
```

### 5.6 admin_user

```sql
CREATE TABLE `admin_user` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `username` varchar(50) UNIQUE NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` varchar(30) DEFAULT 'ROLE_ADMIN',
  `last_login_ip` varchar(64),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 5.7 operation_log

```sql
CREATE TABLE `operation_log` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `admin_id` bigint,
  `operation_type` varchar(50) NOT NULL,
  `detail` json,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_admin_id` (`admin_id`),
  INDEX `idx_operation_type` (`operation_type`)
);
```

## 6. API 设计

### 6.1 学生端 API

前缀：/api/learn

| 接口 | 方法 | 说明 | 鉴权 |
| :--- | :--- | :--- | :--- |
| /api/learn/sessions | POST | 创建学习会话 | 否 |
| /api/learn/sessions/{sessionId} | GET | 获取学习状态 | 否 |
| /api/learn/sessions/{sessionId}/diagnosis/questions | POST | 生成诊断题 | 否 |
| /api/learn/sessions/{sessionId}/diagnosis/submit | POST | 提交诊断答案 | 否 |
| /api/learn/sessions/{sessionId}/plan | POST | 生成学习计划 | 否 |
| /api/learn/sessions/{sessionId}/lesson | POST | 生成 RAG 微讲义 | 否 |
| /api/learn/sessions/{sessionId}/exercises | POST | 生成随堂练习 | 否 |
| /api/learn/sessions/{sessionId}/exercises/submit | POST | 提交练习答案并判卷 | 否 |
| /api/learn/sessions/{sessionId}/review | POST | 生成复习结果 | 否 |

### 6.2 管理端 API

前缀：/api/admin

| 接口 | 方法 | 说明 | 鉴权 |
| :--- | :--- | :--- | :--- |
| /api/admin/auth/login | POST | 管理员登录 | 否 |
| /api/admin/auth/logout | POST | 管理员登出 | 是 |
| /api/admin/knowledge/page | GET | 分页查询知识切片 | 是 |
| /api/admin/knowledge | POST | 新增知识切片 | 是 |
| /api/admin/knowledge/{id} | PUT | 编辑知识切片 | 是 |
| /api/admin/knowledge/{id} | DELETE | 删除知识切片 | 是 |
| /api/admin/knowledge/{id}/embedding | POST | 重新向量化 | 是 |
| /api/admin/prompts | GET | 查询 Prompt 模板 | 是 |
| /api/admin/prompts/{id} | PUT | 更新 Prompt | 是 |
| /api/admin/prompts/{id}/activate | POST | 启用指定版本 | 是 |
| /api/admin/prompts/{id}/rollback | POST | 回滚 Prompt | 是 |
| /api/admin/statistics/weak-points | GET | 薄弱点统计 | 是 |
| /api/admin/statistics/agent-duration | GET | Agent 耗时统计 | 是 |
| /api/admin/sessions/page | GET | 分页查询学习会话 | 是 |
| /api/admin/sessions/{sessionId} | GET | 查看会话快照 | 是 |
| /api/admin/logs/agent-calls | GET | 查看 Agent 调用日志 | 是 |
| /api/admin/logs/operations | GET | 查看后台操作日志 | 是 |

## 7. 前端路由设计

### 7.1 学生端路由

```text
/
/learn/:sessionId
/learn/:sessionId/diagnosis
/learn/:sessionId/plan
/learn/:sessionId/lesson
/learn/:sessionId/exercises
/learn/:sessionId/review
```

学生端页面：

- StudentHomeView：学习入口。
- DiagnosisView：诊断题。
- PlanView：学习计划。
- LessonView：Markdown 微讲义。
- ExerciseView：随堂练习和判卷结果。
- ReviewView：复习建议或结业结果。

### 7.2 管理端路由

```text
/admin/login
/admin/dashboard
/admin/knowledge
/admin/prompts
/admin/sessions
/admin/logs
```

管理端页面：

- AdminLoginView：后台登录。
- AdminLayout：后台布局。
- DashboardView：数据看板。
- KnowledgeView：知识库管理。
- PromptEditorView：Agent Prompt 管理。
- SessionLogView：学习会话查询。
- SystemLogView：Agent 调用与操作日志。

## 8. 关键实现设计

### 8.1 Prompt 热更新

实现方式：

- prompt_template 表保存所有 Prompt 版本。
- 每个 agent_name 只允许一个 is_active = 1 的模板。
- PromptService 优先从 Redis 读取当前启用 Prompt。
- 管理员更新或启用 Prompt 后，删除 Redis 缓存。
- 下一次 Agent 调用自动读取最新 Prompt。

### 8.2 LLM JSON 输出校验

问题：

LLM 可能输出无法解析的 JSON，或缺失必要字段。

处理策略：

- 每个 Agent 定义明确的响应 DTO。
- 使用 JSON 解析失败捕获。
- 对必要字段做非空校验。
- 校验失败时记录 agent_call_log。
- 第一阶段允许进行一次自动重试。

### 8.3 RAG 检索

流程：

```text
weakPoints + learningPlan
  -> 构造检索 query
  -> EmbeddingClient 生成 query embedding
  -> VectorSearchService 计算相似度
  -> 返回 Top K 知识切片
  -> LessonGeneratorAgent 生成微讲义
```

返回结果应包含：

- chunkId
- chapter
- title
- score
- chunkText

### 8.4 表达式自动判卷

第一阶段使用 exp4j 随机代入法。

流程：

```text
standardAnswer + studentAnswer
  -> 解析表达式
  -> 随机生成多个 x 取值
  -> 分别计算两个表达式结果
  -> 判断误差是否在阈值内
  -> 生成 judge_detail
```

注意事项：

- 避开导致除零、开方负数等异常的取值。
- 连续多次有效采样都通过才判正确。
- 对异常表达式返回明确错误信息。

### 8.5 Agent 调用日志

每次 Agent 调用都要记录：

- sessionId
- agentName
- promptVersion
- requestSummary
- responseSummary
- durationMs
- success
- errorMessage

作用：

- 后台排查异常。
- 数据看板统计耗时。
- 面试时体现可观测性设计。

### 8.6 Redis Key 设计

建议：

```text
learn:state:{sessionId}
admin:token:blacklist:{tokenId}
prompt:active:{agentName}
stat:active_sessions
```

过期策略：

- learn:state:{sessionId} 可设置 24 小时过期。
- admin:token:blacklist:{tokenId} 过期时间与 JWT 剩余有效期一致。
- prompt:active:{agentName} 不设置过期，由后台更新时主动删除。

## 9. 初始化数据

### 9.1 默认管理员

开发环境启动时，如果 admin_user 表为空，自动创建：

```text
username: admin
password: admin123
```

密码必须使用 BCrypt 存储。

### 9.2 默认 Prompt

系统初始化 5 个默认 Prompt：

- diagnostician
- planner
- lesson_generator
- exercise
- reviewer

### 9.3 默认知识切片

第一阶段建议内置少量高数知识切片，覆盖：

- 极限。
- 导数。
- 链式法则。
- 洛必达法则。
- 不定积分。
- 定积分。

## 10. 测试建议

### 10.1 单元测试

优先测试：

- ExpressionJudgeService。
- PromptService。
- VectorSearchService。
- LearningOrchestrator 状态流转。

### 10.2 集成测试

优先测试：

- 创建学习会话。
- 提交诊断答案。
- 生成学习计划。
- 提交练习并判卷。
- 管理员登录。
- Prompt 更新后 Agent 能读取新版本。

### 10.3 手工验收

手工演示路径：

1. 启动 MySQL、Redis、后端、前端。
2. 学生端创建学习会话。
3. 完成诊断题。
4. 生成学习计划。
5. 生成 Markdown 微讲义。
6. 完成练习并查看判卷结果。
7. 查看复习建议。
8. 管理端登录。
9. 查看学习会话快照。
10. 查看 Agent 调用日志。
11. 修改 Prompt 并验证热更新。

## 11. 部署与配置

### 11.1 配置项

后端配置应包含：

- MySQL 连接。
- Redis 连接。
- JWT 密钥和过期时间。
- LLM API Base URL。
- LLM API Key。
- LLM 模型名称。
- Embedding 模型名称。

### 11.2 环境变量

敏感信息必须通过环境变量或本地配置文件注入，不提交到 Git。

建议环境变量：

```text
AGENT_STUDY_DB_URL
AGENT_STUDY_DB_USERNAME
AGENT_STUDY_DB_PASSWORD
AGENT_STUDY_REDIS_HOST
AGENT_STUDY_JWT_SECRET
AGENT_STUDY_LLM_API_KEY
```

### 11.3 Docker Compose

第一阶段 Docker Compose 至少包含：

- MySQL 8。
- Redis。

后续可加入：

- backend。
- frontend。
- nginx。

