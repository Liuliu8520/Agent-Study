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

The project uses a frontend-backend separated architecture.

```text
Agent_Study/
├── backend/   Spring Boot backend
├── frontend/  Vue3 frontend
├── docker/    Local infrastructure configs
├── docs/      API, database, and prompt documents
└── README.md
```

Backend modules:

- common: unified response, exceptions, pagination, utilities.
- config: Spring, MyBatis, Redis, WebClient configuration.
- security: Spring Security, JWT, admin authentication.
- learn: learning sessions, LearningState, LearningOrchestrator.
- agent: LLM client, PromptService, agents, call logs.
- rag: knowledge chunks, embeddings, vector search.
- admin: admin login, prompt management, operation audit.
- statistics: weak point stats, agent duration stats.
- log: session snapshots, agent call logs, operation logs.

## Current Backend Status

The backend currently has a minimal runnable Spring Boot skeleton.

Implemented:

- Spring Boot application entry.
- `GET /api/health` health check.
- Unified `ApiResponse`.
- Global exception handler.
- Basic `application.yml`.
- Context loading test.

Health check:

```text
GET http://localhost:8080/api/health
```

Expected response:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "status": "ok",
    "service": "agent-study-backend"
  }
}
```

## Local Commands

Java version:

```powershell
java -version
```

Maven path on this machine:

```powershell
C:\Maven\apache-maven-3.8.2\bin\mvn.cmd
```

Run backend tests:

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" test
```

Run backend:

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" spring-boot:run
```

Package backend:

```powershell
cd D:\Users\Desktop\NUIT_STUDY\Agent_Study\backend
& "C:\Maven\apache-maven-3.8.2\bin\mvn.cmd" package
```

Run packaged jar:

```powershell
java -jar .\target\agent-study-backend-0.1.0-SNAPSHOT.jar
```

## Development Rules

- Keep the backend modular but do not split microservices in the first phase.
- Prioritize a complete P0 learning flow before adding optional features.
- Do not hardcode real API keys, JWT secrets, database passwords, or LLM credentials.
- Do not commit local caches, downloaded tools, build outputs, or IDE metadata.
- Agent prompts should eventually be stored in MySQL and managed by `PromptService`.
- Agent input/output should use DTOs instead of raw maps.
- LLM calls must eventually be logged through `agent_call_log`.
- Learning flow state should eventually be coordinated by `LearningOrchestrator`.

## Git Notes

Recommended remotes:

```text
origin  -> https://github.com/Liuliu8520/Agent-Study.git
gitee   -> https://gitee.com/liu-liu-37/agent-study.git
```

Recommended commit style:

```text
init backend skeleton and project docs
```

## Next Milestone

Build the first real backend business module:

- `learn` module.
- Create learning session API.
- Define `LearningState`.
- Add simple in-memory session service first.
- Then connect MySQL and Redis in the next phase.

