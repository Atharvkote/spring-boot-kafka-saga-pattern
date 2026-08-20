# Spring Boot Kafka Saga Pattern

<img width="1536" height="1024" alt="ChatGPT Image Aug 21, 2026, 12_09_49 AM" src="https://github.com/user-attachments/assets/2ea73f07-4b53-4dbb-9dbe-b7b568dd5b41" />

An **event-driven microservices** order management system implementing the **Saga Orchestration Pattern** using Apache Kafka and Spring Boot. This project demonstrates distributed transaction management across four business services with automatic compensating transactions on failure.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Service Endpoints](#service-endpoints)
- [Saga Flow](#saga-flow)
- [Configuration](#configuration)
- [Monitoring](#monitoring)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Overview

This project implements a production-grade **Order Management System** that processes orders through a distributed saga spanning multiple microservices. When a customer places an order, the system coordinates inventory reservation, payment processing, and notification delivery through Kafka event streams. If any step fails, compensating transactions automatically roll back the preceding steps.

### Key Features

- **Saga Pattern**: Choreography-based saga with compensating transactions for rollback
- **Event-Driven Architecture**: Asynchronous inter-service communication via Apache Kafka
- **Service Discovery**: Dynamic service registration and lookup with Netflix Eureka
- **Centralized Configuration**: Externalized configuration management with Spring Cloud Config Server
- **API Gateway**: Single entry point for all client requests with intelligent routing
- **Database per Service**: Each service owns its dedicated PostgreSQL database
- **Idempotent Producers**: Kafka producers configured with `enable.idempotence=true` and `acks=all`
- **Manual Acknowledgment**: Consumer offset management with `manual_immediate` ack mode
- **Health Checks**: Full Docker health checks with startup dependency ordering
- **Schema Migration**: Flyway-based database versioning for order, inventory, and payment services
- **Failure Simulation**: Configurable failure rates for inventory and payment services to test saga rollbacks

## Tech Stack

| Technology               | Version   | Purpose                          |
| ------------------------ | --------- | -------------------------------- |
| Java                     | 17        | Runtime platform                 |
| Spring Boot              | 3.3.2     | Application framework            |
| Spring Cloud             | 2023.0.3  | Microservices toolkit            |
| Apache Kafka (KRaft)     | 7.6.0     | Event streaming platform         |
| PostgreSQL               | 15 Alpine | Relational database              |
| Spring Cloud Config      | -         | Centralized configuration        |
| Netflix Eureka           | -         | Service discovery                |
| Spring Cloud Gateway     | -         | API gateway                      |
| Flyway                   | -         | Database migration               |
| Lombok                   | -         | Boilerplate reduction            |
| Testcontainers           | 1.19.8    | Integration testing              |
| Docker & Docker Compose  | -         | Container orchestration          |

## Project Structure

```
spring-boot-kafka-saga-pattern/
├── configs/                          # Centralized service configurations
│   ├── order-service.yml             #   Order service config (port 8082)
│   ├── inventory-service.yml         #   Inventory service config (port 8083)
│   ├── payment-service.yml           #   Payment service config (port 8084)
│   └── notification-service.yml      #   Notification service config (port 8085)
│
├── docker/                           # Docker Compose definitions
│   ├── deps.compose.yml              #   Full stack (infra + all services)
│   ├── infra.compose.yml             #   Infrastructure only (Postgres, Kafka, Kafka UI)
│   └── init.sql                      #   Database initialization script
│
├── infra/                            # Infrastructure services
│   ├── api-gateway/                  #   Spring Cloud Gateway (port 8081)
│   ├── config-server/                #   Spring Cloud Config Server (port 8888)
│   └── service-discovery/            #   Netflix Eureka Server (port 8761)
│
├── services/                         # Business microservices
│   ├── order-service/                #   Order management & saga orchestrator
│   ├── inventory-service/            #   Stock reservation & release
│   ├── payment-service/              #   Payment processing & refund
│   └── notification-service/         #   Event notification consumer
│
├── docker-compose.yml                # Root compose file (infrastructure deps)
├── pom.xml                           # Parent Maven POM (multi-module)
├── .env.example                      # Environment variable template
├── .gitignore                        # Git ignore rules
├── ARCHITECTURE.md                   # Detailed architecture documentation
└── LICENSE                           # Apache License 2.0
```

## Prerequisites

Ensure the following are installed on your system:

- **Java 17** (JDK) &mdash; [Eclipse Temurin](https://adoptium.net/) recommended
- **Maven 3.8+** &mdash; for building the project
- **Docker** &mdash; version 20.10+ with Docker Compose v2
- **Git** &mdash; for cloning the repository

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/Atharvkote/spring-boot-kafka-saga-pattern.git
cd spring-boot-kafka-saga-pattern
```

### 2. Set Up Environment Variables

```bash
cp .env.example .env
# Edit .env if you need to customize ports or credentials
```

### 3. Start Infrastructure Dependencies

Start only the infrastructure layer (PostgreSQL, Kafka, Kafka UI):

```bash
docker compose -f docker/infra.compose.yml up -d
```

Wait for all containers to pass their health checks:

```bash
docker compose -f docker/infra.compose.yml ps
```

### 4. Build All Modules

```bash
mvn clean package -DskipTests
```

### 5. Run the Full Stack

**Option A &mdash; Docker (recommended)**

Launch everything including all services:

```bash
docker compose -f docker/deps.compose.yml up -d --build
```

**Option B &mdash; Local Development**

Start each service individually (infrastructure must be running via Docker):

```bash
# Terminal 1 - Config Server
cd infra/config-server && mvn spring-boot:run

# Terminal 2 - Service Discovery (wait for Config Server)
cd infra/service-discovery && mvn spring-boot:run

# Terminal 3 - API Gateway
cd infra/api-gateway && mvn spring-boot:run

# Terminal 4 - Order Service
cd services/order-service && mvn spring-boot:run

# Terminal 5 - Inventory Service
cd services/inventory-service && mvn spring-boot:run

# Terminal 6 - Payment Service
cd services/payment-service && mvn spring-boot:run

# Terminal 7 - Notification Service
cd services/notification-service && mvn spring-boot:run
```

### 6. Verify Startup

| Service              | URL                                      | Expected                    |
| -------------------- | ---------------------------------------- | --------------------------- |
| Config Server        | http://localhost:8888/actuator/health     | `{"status": "UP"}`          |
| Eureka Dashboard     | http://localhost:8761                     | All services registered     |
| API Gateway          | http://localhost:8081/actuator/health     | `{"status": "UP"}`          |
| Kafka UI             | http://localhost:8088                     | Kafka cluster visible       |
| Order Service        | http://localhost:8082/actuator/health     | `{"status": "UP"}`          |
| Inventory Service    | http://localhost:8083/actuator/health     | `{"status": "UP"}`          |
| Payment Service      | http://localhost:8084/actuator/health     | `{"status": "UP"}`          |
| Notification Service | http://localhost:8085/actuator/health     | `{"status": "UP"}`          |

## Service Endpoints

### Order Service (port 8082)

| Method | Endpoint                | Description               |
| ------ | ----------------------- | ------------------------- |
| POST   | `/api/orders`           | Create a new order        |
| GET    | `/api/orders/{id}`      | Get order by ID           |
| GET    | `/api/orders`           | List all orders           |

#### Create Order Example

```bash
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-001",
    "quantity": 2,
    "price": 49.99,
    "customerId": "CUST-001"
  }'
```

## Saga Flow

### Success Path

```
1. Client  ──POST /api/orders──▶  Order Service
2. Order Service  ──ORDER_CREATED──▶  Kafka
3. Inventory Service  ◀──consumes──  Kafka
4. Inventory Service  ──INVENTORY_RESERVED──▶  Kafka
5. Payment Service  ◀──consumes──  Kafka
6. Payment Service  ──PAYMENT_COMPLETED──▶  Kafka
7. Order Service  ◀──consumes──  Kafka  (status → COMPLETED)
8. Notification Service  ◀──consumes──  Kafka  (sends notification)
```

### Failure Path (Payment Fails)

```
1-4. Same as success path
5. Payment Service  ──PAYMENT_FAILED──▶  Kafka
6. Inventory Service  ◀──consumes──  Kafka  (releases reserved stock)
7. Order Service  ◀──consumes──  Kafka  (status → FAILED)
8. Notification Service  ◀──consumes──  Kafka  (sends failure notification)
```

### Failure Path (Inventory Fails)

```
1-3. Same as success path
4. Inventory Service  ──INVENTORY_FAILED──▶  Kafka
5. Order Service  ◀──consumes──  Kafka  (status → FAILED)
6. Notification Service  ◀──consumes──  Kafka  (sends failure notification)
```

## Configuration

All service configurations are centralized in the `configs/` directory and served by the Config Server.

| Variable                         | Default                | Description                        |
| -------------------------------- | ---------------------- | ---------------------------------- |
| `POSTGRES_HOST`                  | `127.0.0.1`            | PostgreSQL host                    |
| `POSTGRES_PORT`                  | `5433`                 | PostgreSQL exposed port            |
| `POSTGRES_USER`                  | `postgres`             | PostgreSQL username                |
| `POSTGRES_PASSWORD`              | `postgres`             | PostgreSQL password                |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9094`       | Kafka bootstrap servers            |
| `EUREKA_SERVER_URL`              | `http://localhost:8761/eureka/` | Eureka server URL           |
| `CONFIG_SERVER_URL`              | `http://localhost:8888`| Config Server URL                  |
| `inventory.failure-rate`         | `0.10`                 | Inventory failure simulation rate  |
| `payment.failure-rate`           | `0.10`                 | Payment failure simulation rate    |

## Monitoring

### Kafka UI

Access the Kafka UI at **http://localhost:8088** to:
- Browse topics and consumer groups
- Inspect messages in real-time
- Monitor consumer lag

### Eureka Dashboard

Access the Eureka Dashboard at **http://localhost:8761** to:
- View registered service instances
- Monitor service health and availability

### Actuator Endpoints

Each service exposes Spring Boot Actuator endpoints:

```bash
# Health check
curl http://localhost:{port}/actuator/health

# Service info
curl http://localhost:{port}/actuator/info

# Metrics
curl http://localhost:{port}/actuator/metrics
```

## Testing

### Run Unit Tests

```bash
mvn test
```

### Run Integration Tests (requires Docker)

Integration tests use Testcontainers to spin up PostgreSQL and Kafka:

```bash
mvn verify
```

### Manual Saga Testing

**Test success scenario:**
```bash
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId":"PROD-001","quantity":1,"price":29.99,"customerId":"CUST-001"}'
```

**Verify order status:**
```bash
curl http://localhost:8082/api/orders/{orderId}
```

## Troubleshooting

| Issue                              | Solution                                                               |
| ---------------------------------- | ---------------------------------------------------------------------- |
| Kafka not starting                 | Ensure port 9092/9094 are free. Check `docker logs kafka`              |
| Service can't connect to Config    | Verify Config Server is healthy before starting dependent services     |
| Eureka shows service as DOWN       | Check the service's health endpoint and actuator config                |
| Database connection refused        | Ensure PostgreSQL is running on port 5433 and databases are created    |
| Consumer deserialization errors    | All consumers use `StringDeserializer` with manual `ObjectMapper`      |
| Orders stuck in PENDING            | Check Kafka UI for unprocessed messages and consumer group lag         |

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add your feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

Please ensure all tests pass before submitting a PR.

## License

This project is licensed under the **Apache License 2.0** &mdash; see the [LICENSE](LICENSE) file for details.

<p align="center">
  Built with Spring Boot, Apache Kafka, and the Saga Pattern
</p>
