# ClearWatch

**Service health monitoring and observability platform** — a real-time dashboard for tracking the health, metrics, logs, alerts, and incidents of a fleet of microservices.

ClearWatch ships with a built-in telemetry simulator, so it runs end-to-end with **zero external infrastructure**: the moment you start it, dashboards fill with live metrics, logs stream in, alert rules fire, and incidents open and resolve on their own. Point it at real services later, or leave the simulator on as a self-contained demo.

<p>
  <img alt="Java" src="https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=springboot&logoColor=white">
  <img alt="React" src="https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black">
  <img alt="TypeScript" src="https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript&logoColor=white">
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white">
  <img alt="Vite" src="https://img.shields.io/badge/Vite-5-646CFF?logo=vite&logoColor=white">
</p>

---

## Overview

ClearWatch models the core loop of an observability tool like Datadog or Grafana, scoped to the essentials:

- A **fleet of services** reports telemetry (request rate, latency percentiles, error rate, CPU, memory).
- Incoming samples are evaluated against **alert rules**; a sustained breach fires an **alert**.
- Related alerts roll up into **incidents** with a timeline, so operators see one event instead of a storm of notifications.
- **Logs** stream alongside metrics, searchable and filterable by service, level, and time window.

The backend seeds a fictional fleet of six services (`auth-service`, `orders-api`, `payments`, `inventory`, `notifications`, `gateway`) and a set of default alert rules on first boot, then the simulator drives realistic traffic against them.

## Key Features

- **Service overview** — status at a glance (Healthy / Degraded / Down) with error rate, p95 latency, request rate, and a request-rate sparkline per service.
- **Service detail** — time-series charts for every metric type across selectable windows.
- **Live logs** — INFO / WARN / ERROR entries with trace IDs, full-text search, and filtering by service, level, and window.
- **Alerting engine** — configurable rules (metric, comparator, threshold, duration, severity) evaluated continuously; alerts transition between `FIRING` and `RESOLVED`.
- **Incident management** — alerts correlate into incidents with open/resolved status and a severity ranking.
- **JWT authentication** — stateless auth with a seeded admin account and role-based access.
- **Telemetry simulator** — emits synthetic metrics and logs on a daily traffic curve, backfills 24 hours of history on first boot, and periodically injects failure scenarios so the alerting and incident flows are never empty.
- **Automatic retention** — old metrics and logs are pruned on a configurable schedule.

## Architecture

```
┌──────────────────────┐        REST / JWT        ┌───────────────────────────┐
│   React + Vite SPA    │  ───────────────────▶   │   Spring Boot API          │
│                       │                          │                            │
│  • Service dashboards │                          │  • Controllers (/api/**)   │
│  • Metric charts      │  ◀───────────────────   │  • Alert evaluation        │
│  • Log viewer         │        JSON              │  • Incident correlation    │
│  • Alert rule editor  │                          │  • Telemetry simulator     │
│  • Incident timeline  │                          │  • Retention cleanup       │
└──────────────────────┘                          └─────────────┬─────────────┘
                                                                 │ JPA / Hibernate
                                                                 ▼
                                                      ┌─────────────────────┐
                                                      │    PostgreSQL 16     │
                                                      └─────────────────────┘
```

## Tech Stack

| Layer        | Technologies                                                                 |
|--------------|------------------------------------------------------------------------------|
| **Backend**  | Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA, Spring Security, Spring Boot Actuator, JJWT |
| **Frontend** | React 18, TypeScript 5, Vite 5, React Router 6, Recharts, Axios              |
| **Database** | PostgreSQL 16                                                                |
| **Build**    | Maven (backend), npm / Vite (frontend)                                       |
| **Infra**    | Docker Compose (local Postgres); Supabase-compatible connection string       |

## Getting Started

### Prerequisites

- **Java 17+** and **Maven**
- **Node.js 18+** and **npm**
- **Docker** (for the local Postgres container) — or an existing PostgreSQL 16 database

### 1. Clone and configure

```bash
git clone https://github.com/MarlonHaynes/clearwatch.git
cd clearwatch
cp .env.example .env
```

Review `.env` and change at least `ADMIN_PASSWORD` and `JWT_SECRET` before using it anywhere beyond local development.

### 2. Start the database

```bash
docker compose up -d
```

This starts PostgreSQL 16 on `localhost:5432` with the `clearwatch` database, user, and password used by the defaults in `.env`.

### 3. Run the backend

```bash
cd server
mvn spring-boot:run
```

On first boot the app seeds the admin user, the service fleet, and default alert rules, then the simulator begins emitting telemetry. The API listens on **http://localhost:8080**.

### 4. Run the frontend

```bash
cd client
npm install
npm run dev
```

The SPA runs on **http://localhost:5173**. Sign in with the seeded admin credentials from your `.env`:

```
Email:    admin@clearwatch.dev
Password: ChangeMe123!
```

## Configuration

All configuration is driven by environment variables (see `.env.example`). Defaults are suitable for local development.

| Variable                             | Default                                    | Description                                              |
|--------------------------------------|--------------------------------------------|----------------------------------------------------------|
| `DATABASE_URL`                       | `jdbc:postgresql://localhost:5432/clearwatch` | JDBC connection string (works with Supabase)          |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | `clearwatch` / `clearwatch`           | Database credentials                                     |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD`     | `admin@clearwatch.dev` / `ChangeMe123!`    | Seeded admin login (created on first boot)               |
| `JWT_SECRET`                         | *(dev placeholder)*                        | Secret used to sign JWTs — generate your own with `openssl rand -base64 48` |
| `JWT_EXPIRATION_MS`                  | `86400000`                                 | Token lifetime (24h)                                     |
| `SIMULATOR_ENABLED`                  | `true`                                     | Toggle the telemetry simulator                           |
| `SIMULATOR_SPEED`                    | `1`                                        | Pacing multiplier (`1` = realistic, higher = faster demo)|
| `SIMULATOR_FAILURE_INTERVAL_MINUTES` | `15`                                       | How often a failure scenario is injected                 |
| `RETENTION_DAYS`                     | `7`                                        | How long metrics and logs are retained                   |
| `SERVER_PORT`                        | `8080`                                     | Backend port                                             |
| `CORS_ALLOWED_ORIGINS`               | `http://localhost:5173`                    | Allowed frontend origin(s)                               |
| `VITE_API_BASE_URL`                  | `http://localhost:8080`                    | API base URL the client calls                            |

## API Reference

All endpoints are prefixed with `/api`. Every route except `POST /api/auth/login` requires a `Bearer` token.

| Method   | Endpoint                        | Description                                           |
|----------|---------------------------------|-------------------------------------------------------|
| `POST`   | `/api/auth/login`               | Authenticate and receive a JWT                        |
| `GET`    | `/api/auth/me`                  | Current authenticated user                            |
| `GET`    | `/api/services`                 | List all services with summary status and metrics     |
| `GET`    | `/api/services/{id}`            | Service summary detail                                |
| `GET`    | `/api/services/{id}/metrics`    | Time-series metrics (`type`, `window` query params)   |
| `GET`    | `/api/logs`                     | Logs, filterable by `serviceId`, `level`, `q`, `window`, `limit` |
| `GET`    | `/api/alerts`                   | Alerts, optionally filtered by `state`                |
| `GET`    | `/api/alert-rules`              | List alert rules                                      |
| `POST`   | `/api/alert-rules`              | Create an alert rule                                  |
| `PUT`    | `/api/alert-rules/{id}`         | Update an alert rule                                  |
| `DELETE` | `/api/alert-rules/{id}`         | Delete an alert rule                                  |
| `GET`    | `/api/incidents`                | List incidents                                        |
| `GET`    | `/api/incidents/{id}`           | Incident detail with correlated alerts                |
| `GET`    | `/actuator/health`              | Health check                                          |

**Metric types:** `REQUEST_RATE`, `LATENCY_P50`, `LATENCY_P95`, `LATENCY_P99`, `ERROR_RATE`, `CPU`, `MEMORY`

## Project Structure

```
clearwatch/
├── server/                     # Spring Boot backend
│   └── src/main/java/com/clearwatch/
│       ├── config/             # Properties, data seeder, security config
│       ├── controller/         # REST controllers
│       ├── domain/             # JPA entities and enums
│       ├── dto/                # Request/response DTOs and mappers
│       ├── repository/         # Spring Data repositories
│       ├── security/           # JWT filter, token service, user details
│       ├── service/            # Alert evaluation, incidents, retention, simulator
│       └── simulator/          # Traffic curves, failure scenarios, log generation
├── client/                     # React + Vite frontend
│   └── src/
│       ├── api/                # Axios client and endpoint definitions
│       ├── components/         # Reusable UI (badges, charts, layout)
│       ├── hooks/              # Auth and polling hooks
│       ├── pages/              # Dashboard, logs, alerts, incidents views
│       └── types/              # Shared TypeScript types
├── infra/                      # Terraform and deployment scripts
├── docker-compose.yml          # Local PostgreSQL
└── .env.example                # Configuration template
```

## Deployment

The backend is a standard Spring Boot application and runs on any JVM host (Render, Railway, Fly.io, etc.). The frontend is a static Vite build (`npm run build`) deployable to Vercel, Netlify, or any static host.

For a managed database, swap `DATABASE_URL` for a Supabase connection string (use the session pooler host) — no code changes required. Set a strong `JWT_SECRET` and `ADMIN_PASSWORD`, and restrict `CORS_ALLOWED_ORIGINS` to your deployed frontend origin.

## License

This project is part of a personal engineering portfolio. All rights reserved unless a license file is added.

---

Built by [Marlon Haynes](https://github.com/MarlonHaynes).
