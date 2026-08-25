# DropClip

DropClip is a **daily video challenge** social app. Every day a new prompt ("drop") is published; users record a short video response, upload it, and vote on other clips. A real-time leaderboard ranks the most-voted clips.

This repository contains the **Spring Boot backend API only**. The planned React Native (Expo) mobile frontend is not in this repo yet.

---

## Table of Contents

- [Core Concept](#core-concept)
- [How It Works (User Flow)](#how-it-works-user-flow)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [API Reference](#api-reference)
- [Key Services & Logic](#key-services--logic)
- [Local Development Setup](#local-development-setup)
- [Configuration](#configuration)
- [Current Status](#current-status)
- [Known Issues / Work In Progress](#known-issues--work-in-progress)
- [Roadmap](#roadmap)
- [Notes for AI Assistants](#notes-for-ai-assistants)

---

## Core Concept


| Term            | Meaning                                                                                                               |
| --------------- | --------------------------------------------------------------------------------------------------------------------- |
| **Drop**        | A daily challenge with a prompt (e.g. "Show us your hidden talent in 60 seconds"). Only one drop is active at a time. |
| **Clip**        | A user's video response to the active drop. One clip per user per drop.                                               |
| **Vote**        | An upvote on someone else's clip. One vote per user per clip.                                                         |
| **Leaderboard** | Ranked list of clips by vote count for a given drop, backed by Redis sorted sets.                                     |
| **Badge**       | Achievement earned for milestones (planned — entity exists, logic not implemented).                                   |
| **Streak**      | Consecutive days a user submitted a clip (planned — DB field exists, logic not implemented).                          |


Inspired by apps like BeReal: a daily prompt, short video responses, and social voting.

---

## How It Works (User Flow)

```
1. User registers / logs in          → receives JWT access + refresh tokens
2. Midnight scheduler creates a drop → picks an unused prompt from a pool of 10
3. User requests presigned S3 URL    → uploads video directly to S3 (LocalStack in dev)
4. User confirms upload              → clip saved to DB, added to Redis leaderboard
5. User browses feed                 → sees all clips for today's drop
6. User votes on clips               → vote saved to DB, Redis score incremented
7. User checks leaderboard           → top N clips by vote count
```

---

## Architecture

### Planned (full product)

```
┌─────────────────────────────────────────────────────────┐
│  React Native / Expo (NOT IN THIS REPO)                 │
│  Onboarding · Camera · Upload · Feed · Vote · Badges    │
└────────────────────────┬────────────────────────────────┘
                         │ HTTPS + JWT
┌────────────────────────▼────────────────────────────────┐
│  Spring Boot API (THIS REPO)                            │
│  Auth · Drops · Clips · Votes · Badges · Leaderboard    │
└──────┬──────────────┬──────────────┬─────────────────────┘
       │              │              │
  PostgreSQL        Redis         AWS S3
  (persistent)   (leaderboard)   (video storage)
```

### Implemented today

- Single **monolith** Spring Boot app (not split into microservices)
- **Custom JWT auth** (register / login / refresh) — not Clerk
- **PostgreSQL** for all persistent data
- **Redis** for leaderboard sorted sets only (not sessions)
- **LocalStack** for S3 in local dev (real AWS S3 in production)
- **Flyway** for DB migrations (manual config bean, auto-config excluded)
- **Docker Compose** for Postgres, Redis, and LocalStack

### Planned but not implemented

- React Native / Expo frontend
- Clerk authentication (diagram originally showed Clerk; code uses custom JWT)
- WebSocket live leaderboard updates (dependency in `pom.xml`, no handlers)
- AWS SES email notifications (dependency in `pom.xml`, no service)
- AWS API Gateway
- Wallet feature
- Reactions beyond simple upvote
- User profile update API
- Badge award logic and API
- Streak tracking logic

---

## Tech Stack


| Layer               | Technology                                |
| ------------------- | ----------------------------------------- |
| Language            | Java 21                                   |
| Framework           | Spring Boot 3.5.13                        |
| Security            | Spring Security + JWT (jjwt 0.12.5)       |
| ORM                 | Spring Data JPA / Hibernate               |
| Database            | PostgreSQL 16                             |
| Migrations          | Flyway                                    |
| Cache / Leaderboard | Redis 7 (sorted sets via `RedisTemplate`) |
| Object Storage      | AWS S3 SDK 2.25.6 (LocalStack locally)    |
| Build               | Maven                                     |
| Dev Infra           | Docker Compose                            |


---

## Project Structure

```
dropclip/
├── docker-compose.yml          # Postgres, Redis, LocalStack
├── pom.xml
└── src/main/
    ├── java/com/dropclip/dropclip/
    │   ├── DropclipApplication.java
    │   ├── config/
    │   │   ├── AwsConfig.java          # S3 client + presigner (LocalStack)
    │   │   ├── FlywayConfig.java       # Manual Flyway bean
    │   │   ├── RedisConfig.java
    │   │   └── SecurityConfig.java     # JWT filter chain
    │   ├── controller/
    │   │   ├── AuthController.java     # /api/auth/*
    │   │   ├── ClipController.java     # /api/clips/*
    │   │   ├── DropController.java     # /api/drops/*
    │   │   └── VoteController.java     # /api/clips/{id}/vote, leaderboard
    │   ├── dto/                        # Request/response DTOs
    │   ├── entity/
    │   │   ├── User, Drop, Clip, Vote, Badge, DropPrompt
    │   │   └── BaseEntity.java         # id, createdAt, updatedAt
    │   ├── exception/
    │   │   ├── ApiException.java
    │   │   └── GlobalExceptionHandler.java
    │   ├── repository/                 # Spring Data JPA repos
    │   ├── security/
    │   │   ├── JwtUtil.java
    │   │   ├── JwtAuthFilter.java
    │   │   └── UserDetailsServiceImpl.java
    │   └── service/
    │       ├── AuthService.java
    │       ├── ClipService.java
    │       ├── DropSchedulerService.java  # @Scheduled daily drop
    │       ├── LeaderboardService.java    # Redis ZSET ops
    │       ├── S3Service.java
    │       └── VoteService.java
    └── resources/
        ├── application.yaml
        └── db/migration/
            ├── V1__create_tables.sql
            ├── V2__create_prompts_table.sql
            └── V3__add_badges_table.sql   # WIP — see Known Issues
```

---

## Database Schema

### Tables


| Table          | Purpose                                                                           |
| -------------- | --------------------------------------------------------------------------------- |
| `users`        | Accounts with username, email, password hash, profile fields, streak, total clips |
| `drops`        | Daily challenges with title, prompt, start/expiry, active flag                    |
| `drop_prompts` | Pool of 10 reusable prompts; scheduler marks them used/reset                      |
| `clips`        | Video responses (S3 key, status, vote count); unique per (user, drop)             |
| `votes`        | Upvotes; unique per (user, clip)                                                  |
| `badges`       | User achievements by type; unique per (user, badge_type)                          |


### Migrations


| Version | File                           | Description                                             |
| ------- | ------------------------------ | ------------------------------------------------------- |
| V1      | `V1__create_tables.sql`        | Core tables: users, drops, clips, votes, badges         |
| V2      | `V2__create_prompts_table.sql` | Prompt pool with 10 seed prompts                        |
| V3      | `V3__add_badges_table.sql`     | WIP — intended to align badges schema with `BaseEntity` |


### Redis keys

```
leaderboard:{dropId}   → sorted set, member = clipId, score = vote count
```

---

## API Reference

Base URL: `http://localhost:8080`

Protected endpoints require header: `Authorization: Bearer <accessToken>`

### Auth (public)


| Method | Path                 | Body                                         | Response                              |
| ------ | -------------------- | -------------------------------------------- | ------------------------------------- |
| `POST` | `/api/auth/register` | `{ username, email, password, displayName }` | `{ accessToken, refreshToken, user }` |
| `POST` | `/api/auth/login`    | `{ email, password }`                        | `{ accessToken, refreshToken, user }` |
| `POST` | `/api/auth/refresh`  | `{ refreshToken }`                           | `{ accessToken, refreshToken, user }` |


### Drops (authenticated)


| Method | Path                 | Description                                     |
| ------ | -------------------- | ----------------------------------------------- |
| `GET`  | `/api/drops/active`  | Returns the current active drop, or 404         |
| `POST` | `/api/drops/trigger` | Manually creates a new daily drop (for testing) |


### Clips (authenticated)


| Method | Path                 | Description                                     |
| ------ | -------------------- | ----------------------------------------------- |
| `GET`  | `/api/clips/presign` | Returns presigned S3 upload URL + s3Key         |
| `POST` | `/api/clips/confirm` | `{ title, s3Key }` — saves clip after S3 upload |
| `GET`  | `/api/clips`         | All clips for the active drop                   |


### Voting (authenticated)


| Method | Path                                       | Description        |
| ------ | ------------------------------------------ | ------------------ |
| `POST` | `/api/clips/{clipId}/vote`                 | Upvote a clip      |
| `GET`  | `/api/drops/{dropId}/leaderboard?limit=10` | Top clips by votes |


### Health (public)


| Method | Path               | Description                  |
| ------ | ------------------ | ---------------------------- |
| `GET`  | `/actuator/health` | Spring Actuator health check |


### Error format

```json
{
  "status": 409,
  "error": "CONFLICT",
  "message": "You already submitted a clip for today's drop",
  "path": "/api/clips/presign"
}
```

---

## Key Services & Logic

### DropSchedulerService

- Runs daily at midnight (`@Scheduled(cron = "0 0 0 * * *")`)
- Deactivates current drop and clears its Redis leaderboard
- Picks next unused prompt from `drop_prompts`; resets pool when all are used
- Creates new drop valid for 24 hours
- Manual trigger via `POST /api/drops/trigger`

### ClipService

- `generatePresignedUrl()` — ensures active drop exists, user hasn't already submitted, returns 15-min presigned PUT URL
- `confirmUpload()` — saves clip with status `READY`, adds to Redis leaderboard, increments `user.totalClips`
- S3 key format: `clips/{dropId}/{userId}/{uuid}.mp4`

### VoteService

- Validates clip exists and user hasn't already voted
- Saves vote to PostgreSQL, increments `clip.voteCount`, increments Redis ZSET score

### LeaderboardService

- Redis sorted set per drop: `leaderboard:{dropId}`
- `addClipToLeaderboard` — initial score 0
- `incrementClipScore` — +1 per vote
- `getTopClips` — reverse range (highest scores first)

### Auth

- JWT access token expires in 24 hours; refresh token in 7 days
- Token subject = user email
- BCrypt password hashing

---

## Local Development Setup

### Prerequisites

- Java 21
- Docker & Docker Compose
- AWS CLI (for creating the LocalStack S3 bucket)

### 1. Start infrastructure

```bash
docker compose up -d
```


| Service         | Port |
| --------------- | ---- |
| PostgreSQL      | 5433 |
| Redis           | 6379 |
| LocalStack (S3) | 4566 |


### 2. Create S3 bucket in LocalStack

```bash
aws --endpoint-url=http://localhost:4566 s3 mb s3://dropclip-videos
```

### 3. Run the application

```bash
./mvnw spring-boot:run
```

App starts on `http://localhost:8080`.

### 4. Smoke test

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@test.com","password":"secret1","displayName":"Alice"}'

# Create a drop (use token from register response)
curl -X POST http://localhost:8080/api/drops/trigger \
  -H "Authorization: Bearer <accessToken>"

# Get presigned upload URL
curl http://localhost:8080/api/clips/presign \
  -H "Authorization: Bearer <accessToken>"
```

---

## Configuration

Key settings in `src/main/resources/application.yaml`:


| Key                             | Value (dev)                                 | Notes                                |
| ------------------------------- | ------------------------------------------- | ------------------------------------ |
| `spring.datasource.url`         | `jdbc:postgresql://127.0.0.1:5433/dropclip` | Postgres via Docker                  |
| `spring.jpa.hibernate.ddl-auto` | `validate`                                  | Schema managed by Flyway             |
| `spring.flyway.enabled`         | `true`                                      | Manual Flyway bean in `FlywayConfig` |
| `spring.data.redis.host`        | `127.0.0.1:6379`                            | Redis via Docker                     |
| `jwt.secret`                    | hardcoded dev secret                        | Change for production                |
| `jwt.expiration`                | 86400000 (24h)                              | Access token TTL                     |
| `aws.endpoint`                  | `http://127.0.0.1:4566`                     | LocalStack                           |
| `aws.s3.bucket`                 | `dropclip-videos`                           | Must exist in LocalStack             |


> **Note:** `DropclipApplication` excludes `FlywayAutoConfiguration` because migrations run via a custom `FlywayConfig` bean with `baselineOnMigrate(true)`.

---

## Current Status

Last committed work: **April 22, 2026** — voting system and exception handling.

### Done

- [x] User registration, login, JWT refresh
- [x] Daily drop scheduler with prompt rotation
- [x] Manual drop trigger for testing
- [x] S3 presigned upload flow (LocalStack)
- [x] Clip confirm and feed
- [x] Voting with duplicate-vote prevention
- [x] Redis-backed leaderboard
- [x] Global exception handling with consistent error DTOs
- [x] Docker Compose dev environment
- [x] Flyway migrations V1 and V2

### In Progress (uncommitted local changes)

- [ ] Badge entity refactored to extend `BaseEntity`
- [ ] `application.yaml` switched from `ddl-auto: update` to `validate` + Flyway enabled
- [ ] `V3__add_badges_table.sql` migration (needs fix — see below)

### Not Started

- [ ] BadgeService and badge award logic
- [ ] Badge API endpoints
- [ ] Streak tracking (field exists, no update logic)
- [ ] User profile update API (`PATCH /api/users/me`)
- [ ] WebSocket live updates
- [ ] SES email notifications
- [ ] React Native / Expo frontend
- [ ] Integration tests beyond `contextLoads()`
- [ ] Production deployment (real AWS S3, secrets management)

---

## Known Issues / Work In Progress

### Badges migration conflict

`V1__create_tables.sql` already creates a `badges` table with a simple schema (no `created_at`, `updated_at`, or unique constraint). The uncommitted `V3__add_badges_table.sql` uses `CREATE TABLE IF NOT EXISTS`, which will **not alter** an existing table. On a DB that ran V1, V3 is a no-op and Hibernate `validate` will fail because `Badge` extends `BaseEntity` and expects `created_at` / `updated_at` columns.

**Fix needed:** Replace V3 with `ALTER TABLE` statements to add missing columns and constraints.

### Auth approach vs original plan

The architecture diagram planned **Clerk** for auth. The implementation uses **custom JWT** with email/password. Any frontend should integrate with the existing `/api/auth/`* endpoints unless a Clerk migration is explicitly done.

### No frontend

All API endpoints are testable via curl/Postman. There is no mobile or web client in this repository.

---

## Roadmap

### Phase 1 — Stabilize backend

1. Fix badges migration (V3 ALTER TABLE)
2. Commit Flyway + validate changes
3. Verify full API smoke test passes

### Phase 2 — Complete gamification

1. Implement `BadgeService` (award on first clip, first vote, streak milestones)
2. Implement streak logic in `ClipService.confirmUpload`
3. Add `GET /api/users/me` and `GET /api/users/me/badges`

### Phase 3 — Frontend

1. Scaffold Expo React Native app
2. Screens: auth → camera/upload → feed → vote → leaderboard → badges

### Phase 4 — Production

1. WebSocket live leaderboard
2. SES notifications
3. Deploy to AWS (real S3, optional API Gateway)
4. Switch JWT secret to environment variable / secrets manager

---

## Notes for AI Assistants

If you are an AI agent working on this codebase, here is the context you need:

**What this project is:** A daily video challenge app backend. Users respond to daily prompts with short videos, vote on others' clips, and compete on a leaderboard.

**What exists:** A working Spring Boot monolith with auth, drops, clips (S3 upload), voting, and Redis leaderboard. ~65–70% of the planned backend is done.

**What does NOT exist:** Frontend, BadgeService, streak logic, WebSocket, email, user profile API, Clerk auth.

**Where development stopped:** Uncommitted work on badges (entity refactor + Flyway migration + switching Hibernate to `validate`). This is the first thing to fix before adding new features.

**Conventions to follow:**

- Java 21, Spring Boot 3.5, Lombok for entities/DTOs
- Services throw `ApiException` with `HttpStatus` for business errors
- `GlobalExceptionHandler` catches and formats all errors as `ErrorResponseDTO`
- Entities extend `BaseEntity` (UUID id, `createdAt`, `updatedAt`)
- Auth identity = email (JWT subject); services use `SecurityContextHolder` to get current user
- Redis leaderboard key pattern: `leaderboard:{dropId}`
- S3 presigned URLs expire in 15 minutes; clips stored at `clips/{dropId}/{userId}/{uuid}.mp4`
- One clip per user per drop; one vote per user per clip
- Daily drop created at midnight UTC via `@Scheduled`; use `POST /api/drops/trigger` for manual testing
- Local dev requires Docker Compose (Postgres on 5433, Redis 6379, LocalStack 4566) and S3 bucket creation

**Do not:**

- Split into microservices unless explicitly asked (monolith is intentional for MVP)
- Use `ddl-auto: update` in production (project is migrating to Flyway + validate)
- Commit secrets from `application.yaml` to production configs

**Git history:**

```
f08fbe3  voting system done exception handling done
723bc83  redis impl
71b27f6  DTOs setup
f5f6b61  project started
```

---

## License

Not specified.