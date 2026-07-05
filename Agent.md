# Agent_Study Development Guide

## Project Goal

Agent_Study is a personalized advanced mathematics learning platform powered by LLM agents.

The project is positioned as a Java backend oriented portfolio project with AI Agent and RAG capabilities as highlights.

Core learning flow:

1. Intelligent diagnosis.
2. Personalized 3-day learning plan.
3. RAG-based Markdown micro-lesson generation.
4. In-class exercises with expression auto-grading.
5. Review reinforcement or completion summary.

## Architecture

```text
Agent_Study/
├── backend/   Spring Boot backend
├── frontend/  Vue3 frontend placeholder
├── docker/    MySQL and Redis local infrastructure
├── docs/      API, database, and prompt documents
└── README.md
```

Backend modules:

- common: unified response and exception handling.
- learn: learning sessions, `LearningState`, `LearningOrchestrator`.
- learn.persistence: MySQL snapshot storage and Redis session cache.
- agent: PromptService, Mock LLM Client, AgentCallLog, and Agent debug APIs.
- rag: knowledge chunks, repository abstraction, and keyword retrieval for the P0 RAG entry.
- rag.persistence: MySQL knowledge chunk repository for dev profile.
- statistics: learning session, weak point, and Agent call dashboard APIs.
- admin: JWT login and protected backend management APIs.

## Current Backend Status

Implemented:

- Spring Boot application entry.
- `GET /api/health` health check.
- `POST /api/learn/sessions` create learning session.
- `GET /api/learn/sessions` list learning sessions with optional `studentName`, `status`, and `limit` filters.
- `GET /api/learn/sessions/{sessionId}` get learning session.
- `GET /v3/api-docs/agent-study` and `/swagger-ui.html` expose OpenAPI/Swagger documentation.
- `POST /api/admin/auth/login` admin login issues JWT.
- `GET /api/admin/me` verifies JWT-protected admin access.
- `POST /api/learn/sessions/{sessionId}/diagnosis/questions` generate Step 1 diagnosis questions.
- `POST /api/learn/sessions/{sessionId}/diagnosis/submit` submit Step 1 diagnosis answers.
- `POST /api/learn/sessions/{sessionId}/plan` generate Step 2 3-day learning plan.
- `POST /api/learn/sessions/{sessionId}/lesson` generate Step 3 RAG-style Markdown micro-lesson.
- `POST /api/learn/sessions/{sessionId}/exercises` generate Step 4 exercises.
- `POST /api/learn/sessions/{sessionId}/exercises/submit` submit Step 4 expressions and auto-grade with exp4j.
- `GET /api/learn/sessions/{sessionId}/exercise-attempts` list persisted exercise attempts.
- `POST /api/learn/sessions/{sessionId}/review` generate Step 5 review or completion result.
- Default in-memory learning session repository.
- `dev` profile MySQL + Redis learning session repository.
- MySQL `learning_session` table auto initialization.
- MySQL `exercise_attempt` table auto initialization.
- Redis cache key: `agent-study:learning-session:{sessionId}`.
- `PromptService` with 5 default Agent prompt templates.
- Prompt templates can be stored in MySQL through `prompt_template` and updated through `/api/agent/prompts/{code}`.
- `MockLlmClient` for deterministic local Agent calls without an API key.
- OpenAI-compatible real LLM client can be enabled by configuration while keeping Mock fallback.
- `AgentCallLog` with default in-memory storage and dev MySQL storage.
- Agent debug APIs under `/api/agent`.
- Agent call logs can be filtered by `sessionId`, `agentType`, `status`, and `promptCode`.
- `knowledge_chunk` repository abstraction with default in-memory storage and dev MySQL storage.
- RAG debug APIs under `/api/rag`.
- Statistics dashboard API under `/api/statistics/dashboard`.
- Step 1 diagnosis result analysis now invokes the `diagnosis.default` Mock Agent and records AgentCallLog.
- Step 2 learning plan generation now invokes the `planner.three-day` Mock Agent and records AgentCallLog.
- Step 3 micro-lesson generation now invokes the `lesson.micro` Mock Agent and records AgentCallLog.
- Step 4 exercise generation now invokes the `exercise.generate` Mock Agent and records AgentCallLog.
- Step 5 review generation now invokes the `review.feedback` Mock Agent and records AgentCallLog.
- Context loading, API flow, and JSON snapshot codec tests.

## Local Commands

Run backend tests:

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" test
```

Run backend with in-memory repository:

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" spring-boot:run
```

Run backend with MySQL and Redis:

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study
docker compose -f .\docker\docker-compose.yml up -d

cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" spring-boot:run "-Dspring-boot.run.profiles=dev"
```

## Development Rules

- Keep the backend modular but do not split microservices in the first phase.
- Prioritize a complete P0 learning flow before optional features.
- Do not commit real API keys, JWT secrets, database passwords, or LLM credentials.
- Do not commit local caches, downloaded tools, build outputs, or IDE metadata.
- Use Chinese commit messages from this point forward.
- Keep prompt template management small and API-first before adding a visual admin console.
- Agent input/output should use DTOs instead of raw maps.
- LLM calls should eventually be logged through `agent_call_log`.

## Git Notes

Remotes:

```text
origin  -> https://github.com/Liuliu8520/Agent-Study.git
gitee   -> https://gitee.com/liu-liu-37/agent-study.git
```

Recommended commit style:

```text
实现学习会话 MySQL 和 Redis 持久化
```

## Next Milestone

- Replace keyword retrieval with embedding/vector search.
- Move prompt templates from memory to a `prompt_template` table.
- Replace deterministic planner logic with `PlannerAgent`.
- Evolve `DiagnosisAgentService` from logging/analyzing diagnosis results to structured diagnosis report generation.
- Add `agent_call_log` for LLM call observability.
