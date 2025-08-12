# Stofina – Microservices Backend

A production-ready, Docker-orchestrated **stock trading** backend built with **Java 17**, **Spring Boot 3**, **Spring Cloud 2023**, **Kafka**, **Redis**, **MSSQL**, and a full **observability** stack (Grafana, Prometheus, Loki, Tempo).

---

## Contents
- [Architecture](#architecture)
- [Modules / Services](#modules--services)
- [Technology Stack](#technology-stack)
- [Runtime Profiles](#runtime-profiles)
- [Environment Variables](#environment-variables)
- [Ports](#ports)
- [Run Scenarios](#run-scenarios)
    - [A) Dev Local (host apps + local infra)](#a-dev-local-host-apps--local-infra)
    - [B) Dev (all-in-docker demo)](#b-dev-all-in-docker-demo)
    - [C) Prod (all-in-docker, remote DB ready)](#c-prod-all-in-docker-remote-db-ready)
- [Build & Images](#build--images)
- [Observability](#observability)
- [Troubleshooting](#troubleshooting)
---

## Architecture

```
                    +-----------------+
                    |   API Gateway   |  :8080
                    +--------+--------+
                             |
       +---------------------+---------------------+
       |           Service Discovery (Eureka)      | :8761
       +---------------------+---------------------+
                             |
    +-----------+   +----------------+   +------------------+
    | User Svc  |   | Customer Svc   |   | Portfolio Svc    |
    | :9002     |   | :9003          |   | :9001            |
    +-----------+   +----------------+   +------------------+
           \            \                    \
            \            \                    \
             \            \                    +--------------------+
              \            +------------------->|  Order Service     | :9006
               \                                | (Kafka + Market)   |
                \                               +---------+----------+
                 \                                        |
                  \                                       v
                   \                              +--------------------+
                    \----> Kafka <----------------| Market-Data Svc    | :9005
                                                   +--------------------+

  Redis (cache/rate-limit), MSSQL (DB), SMTP (mail-service)
  Observability: Grafana/Prometheus/Loki/Tempo
```

---

## Modules / Services

- **api-gateway** – Reactive Spring Cloud Gateway routing, CORS, rate-limit (Redis), circuit breaking.
- **eureka-server** – Service registry.
- **user-service** – Users/auth, JWT.
- **customer-service** – Customer profiles.
- **portfolio-service** – Accounts, holdings, settlement, reserved balances.
- **order-service** – Order lifecycle, talks to portfolio + market-data, uses Kafka.
- **market-data-service** – Symbols, prices, publishes to Kafka.
- **mail-service** – Notification e-mails (SMTP).
- **clients-service** – B2B client apps registry.
- **common-data** – Shared DTOs/utilities.
- **docker-compose** – Orchestration (dev/local/prod) and observability assets.

> Multi-module Maven parent: packages and orchestrates all services.

---

## Technology Stack

- **Java 17**, **Spring Boot 3.3.x**, **Spring Cloud 2023.0.x**
- **Spring Data JPA** + **MSSQL**
- **Spring Security** + **JWT**
- **Apache Kafka** (+ Zookeeper) for async messaging
- **Redis** for cache and rate-limiting
- **Eureka** for service discovery
- **Docker Compose** for orchestration
- **Grafana + Prometheus + Loki + Tempo** for metrics, logs, and tracing

---

## Runtime Profiles

Each service uses one of these Spring profiles (see each module’s `application.yml`):

- **`dev-local`** – App runs on host/IDE and uses **local infra** (Kafka/Redis/Eureka on localhost). DB is typically local `MSSQL` container.
- **`dev-remote`** – App runs on host/IDE but uses a **remote MSSQL** (and optionally remote infra). Defaults in configs point Eureka/Redis to localhost.
- **`prod`** – All services run in **Docker**, with Docker DNS names (`eureka-server`, `redis`, `kafka`) and optionally **remote MSSQL** via env vars.

> Default active profile in services is often `dev-remote`; override with `SPRING_PROFILES_ACTIVE`.

---

## Environment Variables

Common variables (vary by profile):

```
# Database (MSSQL)
DB_HOST / DEV_DB_HOST / PROD_DB_HOST
DB_NAME (default: Troya)
DB_USER / DEV_DB_USER / PROD_DB_USER
DB_PASSWORD / DEV_DB_PASSWORD / PROD_DB_PASSWORD

# Discovery
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE (e.g., http://localhost:8761/eureka)

# Cache
REDIS_HOST (localhost or redis)
REDIS_PORT (default: 6379)

# Kafka
KAFKA_BOOTSTRAP_SERVERS (localhost:9092 or kafka:9092/19092)

# Security
JWT_SECRET
```

Service-specific:
```
# order-service
MARKET_DATA_BASE_URL (e.g., http://market-data-service:9005/api)

# market-data-service
# Uses Redis/Kafka/Eureka vars above
```

---

## Ports

- **Eureka**: `8761`
- **API Gateway**: `8080`
- **User**: `9002`, **Customer**: `9003`, **Portfolio**: `9001`
- **Market-Data**: `9005`, **Order**: `9006`, **Mail**: `9004`
- **Redis**: `6379`, **Kafka**: `9092` (plus internal), **Zookeeper**: `2181`
- **Grafana**: `3200`, **Prometheus**: `9090`, **Loki Gateway**: `3100`, **Tempo**: `3110`

---

## Run Scenarios

### A) Dev Local (host apps + local infra)
1) Start minimal infra (Kafka + Redis + Zookeeper) locally:
```bash
docker compose -f docker-compose/local/docker-compose-local.yml up -d
```
2) Run a service from IDE with `dev-local`:
```
SPRING_PROFILES_ACTIVE=dev-local
DB_HOST=localhost
DB_USER=sa
DB_PASSWORD=YourStrong!Passw0rd
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka
REDIS_HOST=localhost
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```
3) Repeat for other services.

### B) Dev (all-in-docker demo)
Brings up full stack including MSSQL (Azure SQL Edge), Redis, Kafka, Eureka, services and observability:
```bash
docker compose -f docker-compose/dev/docker-compose.yml up -d
```
Access:
- Eureka: http://localhost:8761
- Grafana: http://localhost:3200

### C) Prod (all-in-docker, remote DB ready)
Runs all services in Docker. DB host/user/password are supplied via env and point to a **remote MSSQL** if desired.
```bash
docker compose -f docker-compose/prod/docker-compose.yml up -d
```
Override DB with environment variables if needed:
```bash
export PROD_DB_HOST=192.168.144.76:1450
export PROD_DB_USER=grup5
export PROD_DB_PASSWORD=******
docker compose -f docker-compose/prod/docker-compose.yml up -d
```

---

## Build & Images

### Build all modules
```bash
mvn clean package -DskipTests
```

### Container images (Jib)
Jib is configured in the parent POM (Java 17, amd64 base). If your child modules inherit the plugin, images are built during `package`. Otherwise, run Jib explicitly per module:
```bash

# Build and push ALL modules from the root project (if Jib is configured in child POMs)
cd Stofina
mvn clean package -DskipTests
# This will trigger Jib for each module and push all service images to the configured registry
```
> Base image: `eclipse-temurin:17` (amd64). Adjust for Apple Silicon if needed.

---

## Observability

The dev/prod compose files provision Loki (logs), Prometheus (metrics), Tempo (traces), and Grafana with a datasource.
- Grafana: `http://localhost:3200`
- Prometheus: `http://localhost:9090`
- Loki HTTP gateway: `http://localhost:3100`
- Tempo OTEL endpoint: `:4318` (prod/dev compose)

Dashboards and datasources are auto-provisioned from `observability/*` configs.

---

## Troubleshooting

- **Kafka connection refused**: ensure Zookeeper/Kafka are up and correct advertised listeners for host (`localhost:9092`) vs internal (`kafka:29092`/`19092`).
- **Eureka not discovering services**: verify `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` matches where Eureka runs (`localhost` on host runs, `eureka-server` in Docker).
- **MSSQL login/SSL**: default connection uses `encrypt=true;trustServerCertificate=true`; for production, use proper certificates.
- **Apple M1**: prefer ARM-ready images (Bitnami Kafka/Zookeeper) or run services on host with Docker only for infra.

---

**Author:** Cihan Dilsiz
