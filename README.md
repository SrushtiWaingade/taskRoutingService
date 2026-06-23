# Task Routing System

A production-style microservices pipeline that accepts messages, routes them to the right delivery channel (email/SMS/WhatsApp), detects duplicates, and centralises logs — all wired together with RabbitMQ, MongoDB, Elasticsearch, and Kibana.

> Built to demonstrate real-world patterns: async communication, deduplication, observability, and containerised deployment.

---

## What It Does

1. A client sends a message request (recipient, channel, body) to the **Task Router**
2. The router hashes the payload, checks for duplicates, and routes to the correct RabbitMQ queue
3. The **Delivery Service** picks up the message and simulates delivery (email / SMS / WhatsApp)
4. Both services emit structured logs to the **Logging Service**, which indexes them in Elasticsearch
5. Everything is visible in **Kibana**

---

## Architecture

```
Client
  │
  ▼
Task Router (REST API)
  ├── Deduplication (hash → MongoDB)
  ├── Persistence (MongoDB)
  └── Routes to RabbitMQ
        ├── email-queue   →  Delivery Service
        ├── sms-queue     →  Delivery Service
        └── whatsapp-queue → Delivery Service

Task Router + Delivery Service
  └── Publishes logs → logs-queue → Logging Service → Elasticsearch → Kibana
```

---

## Tech Stack

| Concern | Technology |
|---|---|
| Services | Java, Spring Boot |
| Messaging | RabbitMQ |
| Persistence | MongoDB |
| Observability | Elasticsearch + Kibana |
| Containerisation | Docker Compose |

---

## Key Design Decisions

- **Duplicate suppression** — payloads are SHA-hashed before routing; identical messages are rejected with a `DUPLICATE_SUPPRESSED` response, preventing double-delivery without a distributed lock
- **Async delivery** — channel queues decouple routing from delivery, so a slow SMS provider doesn't block email throughput
- **Centralised logging** — all services emit structured log events to a shared RabbitMQ exchange, consumed by a dedicated logging service and indexed in Elasticsearch

---

## Running Locally

**Prerequisites:** Docker and Docker Compose

```bash
# Start the full stack (builds images + spins up all services)
docker compose up --build

# Stop
docker compose down

# Stop and wipe persisted data
docker compose down -v
```

### Service URLs

| Service | URL |
|---|---|
| Task Router API | `http://localhost:8080/tasks/route` |
| RabbitMQ Management | `http://localhost:15672` (guest / guest) |
| MongoDB | `localhost:27017` |
| Elasticsearch | `http://localhost:9200` |
| Kibana | `http://localhost:5601` |

---

## API Reference

### Route a Message

`POST http://localhost:8080/tasks/route`

```json
{
  "to": "test@example.com",
  "channel": "email",
  "body": "Hello world"
}
```

**Success**
```json
{
  "status": "ROUTED",
  "duplicate": false,
  "details": "Forwarded to Email Service",
  "traceId": "eea78425-5acd-45b7-a72a-0a435ae4ece0"
}
```

**Duplicate detected**
```json
{
  "status": "DUPLICATE_SUPPRESSED",
  "duplicate": true,
  "details": "Message with identical channel, recipient, and body already processed.",
  "traceId": "eea78425-5acd-45b7-a72a-0a435ae4ece0"
}
```

---

## Observability with Kibana

1. Start the stack: `docker compose up --build`
2. Open Kibana: `http://localhost:5601`
3. Go to **Discover** → create an index pattern (`logs-*`)
4. Explore structured logs from all three services in real time

---

## Repository Structure

```
taskRouter/       — REST API, deduplication, RabbitMQ routing (Spring Boot)
delivery/         — Channel delivery simulation + persistence (Spring Boot)
loggingservice/   — Log consumer → Elasticsearch indexer (Spring Boot)
docker-compose.yml
```

---

## What This Demonstrates

- Microservices architecture with clear service boundaries
- Asynchronous inter-service communication via RabbitMQ
- Idempotent message handling through hash-based deduplication
- Centralised, structured observability with ELK stack
- One-command local environment via Docker Compose
