## Tech stack

- Java 21, Spring Boot 4
- PostgreSQL (Docker)
- Spring Data JPA / Hibernate
- Groq API (OpenAI-compatible LLM provider)
- Plain HTML/JS dashboard (no frontend framework)

## Features

- Multi-tenant architecture (companies, teams, API keys)
- Real-time cost/token/latency tracking per LLM call
- Hashed API keys (SHA-256) — raw keys never stored
- Budget enforcement — blocks calls before they exceed a set monthly limit
- Graceful handling of upstream provider failures (rate limits, downtime)
- Dashboard with usage totals and cost breakdown by feature

---

## How to Run

### Prerequisites
- Java 21 (JDK)
- Docker Desktop (running)
- IntelliJ IDEA (or any IDE that supports Maven/Spring Boot)
- A free Groq API key — sign up at [console.groq.com](https://console.groq.com) → API Keys → Create API Key

### 1. Clone the repo
```bash
git clone https://github.com/YOUR_USERNAME/ai-token-tracker.git
cd ai-token-tracker
```

### 2. Start PostgreSQL via Docker
```bash
docker-compose up -d
```
This starts a Postgres container named `tokentrack-db` on port 5432, with a database called `tokentrack`.

Verify it's running:
```bash
docker ps
```

### 3. Set environment variables

This project reads two secrets from environment variables instead of hardcoding them:
- `DB_PASSWORD` — your Postgres password (matches `docker-compose.yml`)
- `API_KEY` — your Groq API key

**In IntelliJ:**
1. Click the run configuration dropdown (top right) → **Edit Configurations**
2. Select `AiTokenTrackerApplication`
3. Find **Environment variables** → click the folder icon
4. Add: db password ,API_KEY=gsk_your_actual_groq_key_here
5. Apply → OK

**If running from the command line instead**, set them in your terminal session first:
```bash
export DB_PASSWORD
export API_KEY=gsk_your_actual_groq_key_here
```
(On Windows PowerShell: `$env:DB_PASSWORD="****"` and `$env:API_KEY="gsk_..."`)

### 4. Run the application

**In IntelliJ:** open the project, let Maven finish importing dependencies, then click the green ▶ button on `AiTokenTrackerApplication`.

**Or via terminal:**
```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`. On first run, Hibernate automatically creates all required tables in Postgres.

### 5. Open the dashboard
### 6. Seed test data (via Postman or curl)

Create a company:
```bash
curl -X POST http://localhost:8080/v1/companies -H "Content-Type: application/json" -d "{\"name\": \"Acme Corp\"}"
```

Create an API key for that company (replace `{companyId}` with the id from the response above):
```bash
curl -X POST http://localhost:8080/v1/companies/{companyId}/api-keys -H "Content-Type: application/json" -d "{\"name\": \"test-key\"}"
```

Copy the `apiKey` value from the response — this is shown only once.

### 7. Make a test proxy call
```bash
curl -X POST http://localhost:8080/v1/proxy/chat -H "X-Api-Key: YOUR_KEY_HERE" -H "X-Feature: test" -H "Content-Type: application/json" -d "{\"provider\": \"groq\", \"model\": \"llama-3.1-8b-instant\", \"messages\": [{\"role\": \"user\", \"content\": \"hello\"}]}"
```

You should get back the LLM's response along with token counts, cost, and latency. Refresh the dashboard with the company ID to see it reflected.

---

## API Overview

| Endpoint | Purpose |
|---|---|
| `POST /v1/companies` | Create a company |
| `POST /v1/companies/{id}/api-keys` | Generate an API key for a company |
| `POST /v1/companies/{id}/budgets` | Set (or update) a monthly spend limit |
| `POST /v1/proxy/chat` | Proxy a chat request to the LLM, with auth + budget check + logging |
| `GET /v1/companies/{id}/dashboard/summary` | Aggregated usage/cost stats |

## What I'd do at scale

- Replace `ddl-auto=update` with Flyway/Liquibase for versioned schema migrations
- Fix a known race condition in budget checking (check-then-act) using optimistic locking (`@Version`) or a DB-level atomic increment
- Move usage ingestion to Kafka for durability and to decouple logging from the request path
- Add retry/circuit-breaker (Resilience4j) around the LLM provider call
- Add a unique DB constraint on `(company_id, team_id)` for budgets to prevent duplicates at the data layer, not just in application logic

## Notable bugs fixed during development

- A Windows/JDK timezone name mismatch (`Asia/Calcutta` vs `Asia/Kolkata`) caused Hibernate to fail connecting to Postgres — fixed by forcing the JVM default timezone to UTC at startup
- After adding API key hashing, authentication silently broke because the lookup compared a raw key against a stored hash — the write path was updated but the read path wasn't, a classic partial-migration bug
- Duplicate company-wide budgets could be created, causing the budget check to arbitrarily pick an old/exhausted one — fixed by making budget creation idempotent (update in place instead of inserting duplicates)