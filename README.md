# MetricFlow Dashboard

A Spring Boot web application that displays real-time system metrics on a modern dashboard interface, backed by PostgreSQL and deployable via Docker.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Run with Docker Compose](#run-with-docker-compose)
  - [Run Locally (Native)](#run-locally-native)
- [API Reference](#api-reference)
- [Configuration](#configuration)
- [Database](#database)
- [Docker](#docker)
- [CI/CD Pipeline](#cicd-pipeline)
- [Running Tests](#running-tests)

---

## Overview

MetricFlow is a lightweight dashboard application that:

- Displays live system metrics (CPU usage, memory, latency, etc.) in a clean dark-theme UI
- Lets you add sample metrics via a button click
- Auto-refreshes the table every 5 seconds (togglable)
- Persists all data in PostgreSQL with indexed queries for performance

---

## Tech Stack

| Layer      | Technology                          |
|------------|-------------------------------------|
| Backend    | Java 17, Spring Boot 3.2, Spring MVC |
| Templating | Thymeleaf                           |
| Database   | PostgreSQL 16, Spring Data JPA      |
| Build      | Maven (Maven Wrapper)               |
| Container  | Docker, Docker Compose              |
| CI/CD      | GitHub Actions → GHCR → Railway     |
| Tests      | JUnit 5, Mockito, H2 (in-memory)    |

---

## Project Structure

```
metrics-dashboard/
├── src/
│   ├── main/
│   │   ├── java/com/metrics/dashboard/
│   │   │   ├── DashboardApplication.java
│   │   │   ├── controller/DashboardController.java
│   │   │   ├── entity/Metric.java
│   │   │   ├── repository/MetricRepository.java
│   │   │   └── service/MetricService.java
│   │   └── resources/
│   │       ├── templates/dashboard.html
│   │       ├── application.properties
│   │       └── application-test.properties
│   └── test/
│       └── java/com/metrics/dashboard/
│           ├── DashboardControllerTest.java
│           └── MetricServiceTest.java
├── db/
│   └── init.sql                  # Table creation + seed data
├── .github/
│   └── workflows/
│       └── ci-cd.yml             # GitHub Actions pipeline
├── Dockerfile                    # Multi-stage build
├── docker-compose.yml            # Local dev stack
├── .env.example                  # Environment variable template
└── pom.xml
```

---

## Getting Started

### Prerequisites

- **Docker & Docker Compose** — for the containerised route
- **Java 17+** and **Maven** — for running natively
- **PostgreSQL 16** — only needed for the native route

### Run with Docker Compose

This is the recommended way to get started. It spins up both the app and a PostgreSQL instance, runs the init script, and seeds sample data automatically.

```bash
# 1. Clone the repository
git clone https://github.com/your-org/metrics-dashboard.git
cd metrics-dashboard

# 2. Start the full stack
docker compose up --build

# 3. Open the dashboard
open http://localhost:8080/dashboard
```

To stop and remove containers:

```bash
docker compose down
# To also remove the database volume:
docker compose down -v
```

### Run Locally (Native)

```bash
# 1. Create a PostgreSQL database
createdb metricsdb

# 2. Apply the schema and seed data
psql -d metricsdb -f db/init.sql

# 3. Copy and fill in environment variables
cp .env.example .env

# 4. Run the application
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080` by default.

---

## API Reference

### `GET /dashboard`

Returns the main HTML dashboard page.

---

### `GET /api/metrics`

Returns the 10 most recent metrics as JSON, ordered by timestamp descending.

**Response:**
```json
[
  {
    "id": 12,
    "name": "cpu_usage",
    "value": 73.4,
    "status": "WARNING",
    "timestamp": "2024-04-11T14:22:01"
  }
]
```

---

### `POST /api/metrics/add`

Generates and saves a random metric entry.

**Response:**
```json
{
  "success": true,
  "metric": {
    "id": 13,
    "name": "memory_usage",
    "value": 55.1,
    "status": "WARNING",
    "timestamp": "2024-04-11T14:22:10"
  },
  "totalCount": 13,
  "averageValue": 48.72
}
```

---

## Configuration

All configuration is driven by environment variables. Copy `.env.example` to `.env` to get started:

| Variable            | Default                                    | Description                   |
|---------------------|--------------------------------------------|-------------------------------|
| `DATABASE_URL`      | `jdbc:postgresql://localhost:5432/metricsdb` | JDBC connection URL           |
| `DATABASE_USERNAME` | `postgres`                                 | Database username             |
| `DATABASE_PASSWORD` | `postgres`                                 | Database password             |
| `PORT`              | `8080`                                     | HTTP server port              |

---

## Database

The schema is defined in `db/init.sql` and runs automatically when using Docker Compose.

```sql
CREATE TABLE IF NOT EXISTS metrics (
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(100)     NOT NULL,
    value     DOUBLE PRECISION NOT NULL,
    status    VARCHAR(50)      NOT NULL,
    timestamp TIMESTAMP        NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_metrics_timestamp ON metrics (timestamp DESC);
```

The `status` field follows this convention:

| Status     | Value range  |
|------------|--------------|
| `OK`       | < 50         |
| `WARNING`  | 50 – 79.99   |
| `CRITICAL` | ≥ 80         |

---

## Docker

### Build the image manually

```bash
docker build -t metrics-dashboard .
```

### Run with environment variables

```bash
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/metricsdb \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=postgres \
  metrics-dashboard
```

The `Dockerfile` uses a two-stage build — a JDK image compiles the fat JAR, and a slim JRE image runs it — keeping the final image size small.

---

## CI/CD Pipeline

The pipeline is defined at `.github/workflows/ci-cd.yml` and triggers automatically on every push or pull request to `main` / `master`.

### Pipeline Flow

```
┌─────────────────────────────────────────────────────────┐
│                    Push to main/master                  │
└──────────────────────────┬──────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│               Job 1: build-and-test                     │
│                                                         │
│  1. Checkout code                                       │
│  2. Set up JDK 17 (Temurin) with Maven cache            │
│  3. mvnw clean package -B        ← compile + package    │
│  4. mvnw test -Dspring.profiles.active=test             │
│  5. Upload surefire test report as artifact             │
│  6. Upload metrics-dashboard.jar as artifact            │
└──────────────────────────┬──────────────────────────────┘
                           │  (only on push, not PR)
                           ▼
┌─────────────────────────────────────────────────────────┐
│               Job 2: docker                             │
│               needs: build-and-test                     │
│                                                         │
│  1. Log in to GitHub Container Registry (GHCR)         │
│  2. Extract Docker metadata → generate tags:            │
│       • latest          (default branch only)           │
│       • <branch-name>                                   │
│       • sha-<git-sha>                                   │
│  3. Set up Docker Buildx (multi-platform builder)       │
│  4. docker build --push  ← multi-stage build            │
│     Cache layers via GitHub Actions Cache (gha)         │
│  5. Image published to:                                 │
│     ghcr.io/<owner>/<repo>/metrics-dashboard            │
└──────────────────────────┬──────────────────────────────┘
                           │  (only on push to main/master)
                           ▼
┌─────────────────────────────────────────────────────────┐
│               Job 3: deploy                             │
│               needs: docker                             │
│               environment: production                   │
│                                                         │
│  1. Install Railway CLI (npm)                           │
│  2. railway up --detach   ← deploy latest image         │
│     Uses RAILWAY_TOKEN secret for auth                  │
└─────────────────────────────────────────────────────────┘
```

### Triggers

| Event                        | Jobs that run                          |
|------------------------------|----------------------------------------|
| Push to `main` / `master`    | build-and-test → docker → deploy       |
| Pull request to `main`       | build-and-test only (no deploy)        |

### Required GitHub Secrets

Go to **Settings → Secrets and variables → Actions** and add:

| Secret            | Where to get it                                                                 |
|-------------------|---------------------------------------------------------------------------------|
| `RAILWAY_TOKEN`   | [Railway dashboard](https://railway.app) → Account Settings → Tokens           |

> The `GITHUB_TOKEN` secret for pushing to GHCR is provided automatically by GitHub Actions — no setup needed.

### Switching to Heroku

The workflow includes a commented-out Heroku deploy block. To use it instead of Railway:

1. Comment out the Railway step in `ci-cd.yml`
2. Uncomment the `akhileshns/heroku-deploy` step
3. Add these secrets to GitHub:

| Secret             | Description                        |
|--------------------|------------------------------------|
| `HEROKU_API_KEY`   | From Heroku → Account Settings     |
| `HEROKU_APP_NAME`  | Your Heroku app name               |
| `HEROKU_EMAIL`     | Email associated with your account |

### Pulling the Published Image

Once the pipeline has run, pull the image directly from GHCR:

```bash
docker pull ghcr.io/<your-github-username>/<repo>/metrics-dashboard:latest

docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/metricsdb \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=postgres \
  ghcr.io/<your-github-username>/<repo>/metrics-dashboard:latest
```

### Viewing Test Reports

After any pipeline run, download the test report artifact from the **Actions** tab:

```
GitHub → Actions → <workflow run> → Artifacts → test-results
```

The surefire XML reports inside can be imported into any CI dashboard or test reporting tool.

---

## Running Tests

Tests use an H2 in-memory database — no external services needed.

```bash
# Run all tests
./mvnw test

# Run with the test profile explicitly
./mvnw test -Dspring.profiles.active=test
```

Two test classes are included:

- **`MetricServiceTest`** — `@DataJpaTest` slice testing persistence logic against H2
- **`DashboardControllerTest`** — `@WebMvcTest` slice testing HTTP endpoints with mocked service
