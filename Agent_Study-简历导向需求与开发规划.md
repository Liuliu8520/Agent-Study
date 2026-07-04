# Agent_Study 简历导向需求与开发规划

## 1. 项目定位

本项目定位为一个面向高校学生的个性化高等数学学习平台，同时提供教师/管理员后台用于知识库维护、Agent Prompt 管控、学习数据分析和全链路日志追踪。

从简历角度，本项目不建议包装成单纯的“大模型聊天应用”，而应包装成：

> 基于 Spring Boot 的个性化高数学习 Agent 平台，融合 RAG 知识检索、多 Agent 流程编排、数学表达式自动判卷、Prompt 热更新和学习数据分析。

主线能力面向 Java 后端开发实习，AI Agent 与 RAG 能力作为项目差异化亮点。

## 2. 面向岗位的能力映射

### 2.1 Java 后端开发实习

重点展示能力：

- Spring Boot RESTful API 设计与分层架构。
- MySQL 表设计、JSON 字段存储、分页查询和统计聚合。
- Redis 存储学习会话状态、Token 黑名单和热点 Prompt 缓存。
- Spring Security + JWT 实现后台鉴权。
- WebClient 调用第三方大模型 API。
- Agent 调用日志、异常捕获、耗时统计和全链路追踪。
- Docker Compose 一键启动 MySQL、Redis 和后端服务。

### 2.2 AI 应用 / Agent 工程实习

重点展示能力：

- 将学习流程拆解为诊断、规划、讲义生成、练习生成、复习强化 5 个 Agent。
- 使用 Orchestrator 编排多 Agent 顺序执行，维护统一 LearningState。
- 使用 RAG 从高数知识库检索教材切片并生成微讲义。
- 通过 Prompt 模板表支持 Prompt 热更新和版本回滚。
- 对 LLM 输出进行 JSON Schema 校验、失败重试和降级处理。
- 使用 exp4j 对学生输入的数学表达式做随机代入验证。

### 2.3 普通软件开发 / 全栈实习

重点展示能力：

- 完整的前后端分离项目。
- 学生端和管理端双端视图。
- 登录鉴权、数据看板、CRUD、日志查询、表单校验等常规业务能力。
- 项目文档、接口文档、启动说明和演示数据完整。

## 3. 最终技术路线

### 3.1 前端

- Vue 3
- Vite
- TypeScript
- Element Plus
- ECharts / vue-echarts
- Pinia
- Vue Router
- Axios

前端划分为两个主视图：

- 学生端学习驾驶舱：面向免登录学习流程。
- 管理端 Admin Dashboard：面向教师/管理员，需要登录鉴权。

### 3.2 后端

- Spring Boot
- Maven
- Spring Web
- Spring Security
- MyBatis-Plus
- MySQL 8
- Redis
- JJWT
- WebClient
- exp4j
- Lombok
- Knife4j / springdoc-openapi

后端采用单体应用、模块化分层，不做过早微服务拆分。

### 3.3 AI 与 RAG

- LLM：优先接入 GLM-4-Flash 或其他兼容 OpenAI 风格接口的大模型。
- Embedding：封装为独立 EmbeddingClient，便于后续替换模型。
- RAG：MySQL 存储知识切片与 embedding JSON，项目演示阶段可使用 Java 内存相似度计算或 MySQL 取出后计算余弦相似度。
- Prompt：存储在 MySQL，支持后台在线编辑、版本记录和启用切换。

## 4. 推荐项目目录

```text
Agent_Study/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/agentstudy/
│       ├── AgentStudyApplication.java
│       ├── common/
│       ├── config/
│       ├── security/
│       ├── learn/
│       ├── agent/
│       ├── rag/
│       ├── admin/
│       ├── statistics/
│       └── log/
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
└── README.md
```

## 5. 核心业务闭环

学生端严格按照线性流水线执行。每一步的输出进入统一的 LearningState，作为下一步输入。

```text
开始学习
  -> Step 1 智能诊断
  -> Step 2 动态规划
  -> Step 3 RAG 微讲义生成
  -> Step 4 随堂练习与自动判卷
  -> Step 5 智能复习或结业归档
```

### 5.1 Step 1 智能诊断

目标：生成 5 道覆盖极限、导数、积分的选择题，根据学生答案输出薄弱知识点。

后端职责：

- 生成诊断题。
- 接收学生答案。
- 调用 DiagnosticianAgent 生成 weakPoints。
- 将诊断结果写入 learning_session。

### 5.2 Step 2 动态规划

目标：根据 weakPoints 生成 3 天学习计划。

后端职责：

- 调用 PlannerAgent。
- 要求 LLM 输出固定 JSON 结构。
- 校验 JSON 字段完整性。
- 将学习计划写入 learning_session.learning_plan。

### 5.3 Step 3 RAG 微讲义生成

目标：检索高数知识库切片，生成包含定义、公式、例题的 Markdown 微讲义。

后端职责：

- 根据 weakPoints 和 learningPlan 构造检索 query。
- 从 knowledge_chunk 中召回相关切片。
- 调用 LessonGeneratorAgent 生成 Markdown。
- 保存 generated_lesson。

### 5.4 Step 4 随堂练习与自动判卷

目标：基于讲义生成 3 道计算/填空题，学生输入数学表达式，系统自动判卷。

后端职责：

- 调用 ExerciseAgent 生成练习题和标准答案。
- 接收学生答案。
- 使用 exp4j 对学生表达式和标准表达式进行随机点代入验证。
- 记录每道题的判卷结果。

### 5.5 Step 5 智能复习

目标：根据错误率自动生成复习策略。

规则：

- 错误率 >= 50%：调用 ReviewerAgent 生成 2 道同类型变式题。
- 全部正确：输出结业鼓励语并归档学习记录。
- 部分错误但低于 50%：输出针对性复习建议并归档。

## 6. 后端模块边界

### 6.1 learn 模块

负责学生端学习流程。

核心类建议：

- LearningController
- LearningService
- LearningOrchestrator
- LearningState
- LearningSessionEntity
- ExerciseAttemptEntity

接口职责：

- 创建学习会话。
- 获取诊断题。
- 提交诊断答案。
- 生成学习计划。
- 生成讲义。
- 生成练习。
- 提交练习答案。
- 获取复习结果。

### 6.2 agent 模块

负责 LLM 调用与多 Agent 抽象。

核心类建议：

- AgentClient
- PromptService
- DiagnosticianAgent
- PlannerAgent
- LessonGeneratorAgent
- ExerciseAgent
- ReviewerAgent
- AgentCallLogger

设计原则：

- Agent 不直接操作数据库，由 Service 层负责状态落库。
- Agent 输入输出使用 DTO，避免到处传 Map。
- Prompt 从 PromptService 动态读取，不硬编码在 Agent 类中。
- 每次 LLM 调用都记录 agentName、promptVersion、durationMs、success、errorMessage。

### 6.3 rag 模块

负责知识库切片、向量化和相似度检索。

核心类建议：

- KnowledgeController
- KnowledgeService
- EmbeddingClient
- VectorSearchService
- KnowledgeChunkEntity

设计原则：

- 知识切片 CRUD 属于后台能力。
- 重新向量化由 KnowledgeService 调用 EmbeddingClient 完成。
- RAG 检索结果需要返回 chunkId、chapter、score、chunkText，方便讲义生成和后台调试。

### 6.4 admin 模块

负责后台登录、管理员信息、Prompt 管理和系统运维入口。

核心类建议：

- AuthController
- AdminUserService
- PromptAdminController
- OperationLogService

设计原则：

- /api/admin/** 必须 JWT 鉴权。
- 登录、Prompt 修改、知识库删除等操作写入 operation_log。
- 默认管理员仅在开发环境自动初始化。

### 6.5 statistics 模块

负责后台看板。

核心能力：

- 薄弱点 Top N 统计。
- Agent 平均耗时统计。
- 近 7 日学习会话数。
- Redis 活跃学习会话数。

### 6.6 log 模块

负责会话快照和异常追踪。

核心能力：

- 按学生名、日期、sessionId 查询学习记录。
- 查看完整 LearningState。
- 查看 Agent 调用日志。
- 查看 LLM 调用异常、JSON 解析异常和判卷异常。

## 7. 建议数据库表

在原有表基础上，建议增加练习记录表和 Agent 调用日志表，这两个表对简历和面试讲解很有价值。

### 7.1 learning_session

存储一次完整学习流程的主状态。

关键字段：

- session_id
- student_name
- weak_points
- learning_plan
- current_step
- generated_lesson
- review_result
- is_finished
- total_duration_ms
- create_time
- update_time

### 7.2 exercise_attempt

存储学生每道练习题的作答与判卷结果。

关键字段：

- id
- session_id
- question_text
- standard_answer
- student_answer
- is_correct
- judge_detail
- create_time

### 7.3 knowledge_chunk

存储高数教材知识切片和向量。

关键字段：

- id
- chapter
- title
- chunk_text
- embedding_json
- enabled
- create_time
- update_time

### 7.4 prompt_template

存储 Agent Prompt 模板。

关键字段：

- id
- agent_name
- version
- system_prompt
- is_active
- operator_id
- create_time
- update_time

### 7.5 agent_call_log

存储每次 LLM 调用的运行记录。

关键字段：

- id
- session_id
- agent_name
- prompt_version
- request_summary
- response_summary
- duration_ms
- success
- error_message
- create_time

### 7.6 admin_user

存储后台管理员账号。

关键字段：

- id
- username
- password_hash
- role
- last_login_ip
- create_time

### 7.7 operation_log

存储后台关键操作审计。

关键字段：

- id
- admin_id
- operation_type
- detail
- create_time

## 8. API 范围

### 8.1 学生端 API

前缀：/api/learn

- POST /api/learn/sessions：创建学习会话。
- GET /api/learn/sessions/{sessionId}：获取学习状态。
- POST /api/learn/sessions/{sessionId}/diagnosis/questions：生成诊断题。
- POST /api/learn/sessions/{sessionId}/diagnosis/submit：提交诊断答案。
- POST /api/learn/sessions/{sessionId}/plan：生成学习计划。
- POST /api/learn/sessions/{sessionId}/lesson：生成微讲义。
- POST /api/learn/sessions/{sessionId}/exercises：生成随堂练习。
- POST /api/learn/sessions/{sessionId}/exercises/submit：提交练习答案并判卷。
- POST /api/learn/sessions/{sessionId}/review：生成复习结果。

### 8.2 管理端 API

前缀：/api/admin

- POST /api/admin/auth/login：管理员登录。
- POST /api/admin/auth/logout：管理员登出。
- GET /api/admin/knowledge/page：分页查询知识切片。
- POST /api/admin/knowledge：新增知识切片。
- PUT /api/admin/knowledge/{id}：编辑知识切片。
- DELETE /api/admin/knowledge/{id}：删除知识切片。
- POST /api/admin/knowledge/{id}/embedding：重新向量化。
- GET /api/admin/prompts：查询 Prompt 模板。
- PUT /api/admin/prompts/{id}：更新 Prompt。
- POST /api/admin/prompts/{id}/activate：启用指定版本。
- POST /api/admin/prompts/{id}/rollback：回滚 Prompt。
- GET /api/admin/statistics/weak-points：薄弱点统计。
- GET /api/admin/statistics/agent-duration：Agent 耗时统计。
- GET /api/admin/sessions/page：分页查询学习会话。
- GET /api/admin/sessions/{sessionId}：查看完整会话快照。
- GET /api/admin/logs/agent-calls：查看 Agent 调用日志。
- GET /api/admin/logs/operations：查看后台操作日志。

## 9. 开发优先级

### 9.1 P0：必须完成的 MVP

目标：项目能完整演示，简历上能讲清楚主流程。

- 搭建 Spring Boot 后端项目。
- 搭建 Vue3 前端项目。
- 完成 MySQL、Redis、后端、前端的基础配置。
- 完成学生端 5 步学习闭环。
- 完成 LearningState 状态流转。
- 完成 LLM 调用封装。
- 完成 Prompt 从数据库读取。
- 完成 exp4j 自动判卷。
- 完成 learning_session、exercise_attempt、prompt_template、agent_call_log 表。

### 9.2 P1：后端简历增强

目标：让项目不像普通 Demo，而是像完整工程。

- Spring Security + JWT 后台鉴权。
- 知识库切片 CRUD。
- Prompt 在线编辑、启用和版本回滚。
- Agent 调用日志和异常捕获。
- Knife4j / OpenAPI 接口文档。
- Docker Compose 启动 MySQL 和 Redis。
- 单元测试：表达式判卷、PromptService、RAG 检索。

### 9.3 P2：AI 与数据亮点

目标：增强 Agent 工程方向竞争力。

- RAG 向量检索与讲义引用来源展示。
- 后台 ECharts 薄弱点 Top N 看板。
- Agent 平均响应耗时统计。
- SSE 流式输出学习过程日志。
- LLM 输出 JSON Schema 校验与自动修复。
- 学习报告导出 Markdown。

### 9.4 P3：可选加分项

目标：时间充足时再做，不影响主线。

- 多模型切换。
- 学生历史学习趋势。
- 错题本。
- 题目难度自适应。
- 管理员多角色权限。
- 前端深色模式。

## 10. 当前阶段暂不做的内容

为了避免项目过大，以下内容不作为第一阶段目标：

- 不做复杂微服务拆分。
- 不做真实支付、课程购买、社交社区。
- 不做完整 LMS 教务系统。
- 不做复杂公式编辑器，第一版用普通文本输入数学表达式。
- 不追求生产级向量数据库，第一版用 MySQL + Java 相似度计算即可。
- 不把全部数学知识覆盖完，先覆盖极限、导数、积分三个主题。

## 11. 简历描述示例

项目名称：

基于大模型的个性化高数学习 Agent 平台

简历描述：

- 基于 Spring Boot + Vue3 设计并实现个性化高数学习平台，支持智能诊断、学习规划、RAG 微讲义生成、随堂练习判卷和复习强化的完整学习闭环。
- 设计 LearningOrchestrator 编排 5 类学习 Agent，使用 Redis 维护学习上下文，并将会话快照、练习记录和 Agent 调用日志持久化到 MySQL。
- 构建高数知识库管理模块，支持教材切片 CRUD、Embedding 重新生成和基于相似度的 RAG 检索，提升讲义生成的可解释性。
- 实现 Prompt 模板热更新与版本回滚，管理员可在后台动态调整 Agent System Prompt，无需重启服务。
- 使用 exp4j 实现数学表达式随机代入判卷，支持对学生填空题答案与标准表达式进行等价性验证。
- 基于 Spring Security + JWT 实现后台鉴权，结合 ECharts 展示薄弱知识点统计、Agent 平均耗时和学习会话趋势。

## 12. 面试讲解主线

面试时建议按以下顺序讲：

1. 这个项目解决什么问题：学生高数薄弱点难定位，普通题库缺少个性化讲解和复习闭环。
2. 为什么需要多 Agent：不同学习阶段的任务不同，诊断、规划、讲义、练习、复习分别需要不同 Prompt、输入输出和校验逻辑。
3. 后端如何编排：LearningOrchestrator 控制流程，LearningState 保存上下文，每一步结果落库并进入下一步。
4. RAG 怎么做：管理员维护知识切片，系统向量化后按 weakPoints 检索相关内容，再交给讲义生成 Agent。
5. 自动判卷怎么做：使用 exp4j 对标准答案和学生答案进行多点随机代入，判断表达式是否近似恒等。
6. 如何保证工程可靠性：JWT 鉴权、Prompt 版本管理、Agent 调用日志、异常捕获、耗时统计、JSON 输出校验。
7. 项目难点：LLM 输出不稳定、数学表达式判等、RAG 检索质量、学习流程状态一致性。

## 13. 第一阶段验收标准

第一阶段完成后，项目至少应满足：

- 本地可通过 README 一键启动前端、后端、MySQL、Redis。
- 学生端能完整走完 5 步学习流程。
- 后端真实调用 LLM，而不是纯前端假数据。
- 学习会话、讲义、练习作答、判卷结果和 Agent 调用日志能落库。
- 管理端能登录，并至少完成 Prompt 查看/编辑和学习会话查询。
- README 中有项目介绍、技术栈、架构图、启动方式、演示账号和接口文档地址。

