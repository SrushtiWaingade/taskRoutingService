## Task Routing System – Microservices

This repository contains three Spring Boot microservices plus supporting infrastructure (MongoDB, RabbitMQ, Elasticsearch, Kibana). Together they form a complete pipeline for routing messages, delivering them, and centralizing logs.

### Architecture Overview

- **Task Router (`taskRouter`)**
  - Accepts incoming message requests (to, channel, body) via REST.
  - Hashes payloads and checks MongoDB for duplicates.
  - Stores accepted messages in MongoDB (`taskRouteApplication` database).
  - Routes messages to RabbitMQ queues (`email-queue`, `sms-queue`, `whatsapp-queue`).
  - Publishes log events to a logs exchange/queue.

- **Delivery Service (`delivery`)**
  - Listens to the per-channel queues from RabbitMQ.
  - Simulates delivery (e.g., email/SMS/WhatsApp) and persists records to MongoDB.
  - Publishes delivery logs back to the logging pipeline.

- **Logging Service (`loggingservice`)**
  - Consumes log events from RabbitMQ.
  - Writes logs to Elasticsearch.
  - Data can be explored visually via Kibana.

### Repository Layout

- **`taskRouter/`** – Task routing service (Spring Boot, MongoDB, RabbitMQ).
- **`delivery/`** – Delivery service (Spring Boot, MongoDB, RabbitMQ).
- **`loggingservice/`** – Logging service (Spring Boot, Elasticsearch, RabbitMQ).
- **`docker-compose.yml`** – Local dev stack: MongoDB, RabbitMQ, Elasticsearch, Kibana, and all three services.

Each service has its own `pom.xml`, `src/main/java`, and `src/main/resources/application.yml`.

## Running Everything with Docker

### Prerequisites

- **Docker** and **Docker Compose** installed and running.

### 1. Build and Start the Stack

From the project root (where `docker-compose.yml` lives):

```bash
docker compose up --build
```

This will:

- Build Docker images for `taskRouter`, `delivery`, and `loggingservice`.
- Start MongoDB, RabbitMQ, Elasticsearch, Kibana, and all three services.

To stop everything:

```bash
docker compose down
```

Volumes (`mongo_data`, `es_data`) will persist data between runs. To wipe all data:

```bash
docker compose down -v
```

## Service URLs

- **Task Router API**: `http://localhost:8080/tasks/route`
- **RabbitMQ Management UI**: `http://localhost:15672` (user: `guest`, pass: `guest`)
- **MongoDB**: `localhost:27017` (DB: `taskRouteApplication`)
- **Elasticsearch**: `http://localhost:9200`
- **Kibana**: `http://localhost:5601`

## Example API Usage (Task Router)

- **Endpoint**: `POST http://localhost:8080/tasks/route`
- **Sample Request Body**:

```json
{
  "to": "test@example.com",
  "channel": "email",
  "body": "Hello world"
}
```

- **Sample Success Response**:

```json
{
  "details": "Forwarded to Email Service",
  "duplicate": false,
  "status": "ROUTED",
  "traceId": "eea78425-5acd-45b7-a72a-0a435ae4ece0"
}
```

- **Sample Duplicate Response**:

```json
{
  "status": "DUPLICATE_SUPPRESSED",
  "duplicate": true,
  "traceId": "eea78425-5acd-45b7-a72a-0a435ae4ece0",
  "details": "Message with identical channel, recipient, and body already processed."
}
```

## Observability with Kibana

1. Ensure the stack is running (`docker compose up --build`).
2. Open Kibana in a browser: `http://localhost:5601`.
3. Go to **Discover**.
4. Create an index pattern, for example:
   - **`logs-*`** (or whatever index name pattern is configured in the logging service).
5. Explore logs coming from:
   - **Task Router**
   - **Delivery Service**
   - **Logging Service**

## What This Project Demonstrates

- **Microservices architecture** using Spring Boot.
- **Asynchronous communication** with RabbitMQ.
- **MongoDB persistence** for messages and delivery records.
- **Duplicate message detection** via hashing.
- **Centralized logging** with Elasticsearch and **visualization** in Kibana.
- **Containerized local environment** using Docker Compose for easy spin‑up and teardown.
