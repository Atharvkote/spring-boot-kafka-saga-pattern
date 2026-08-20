# Centralized Configuration

This directory contains the externalized YAML configuration files for all business microservices. These files are served by the **Spring Cloud Config Server** at runtime.

---

## How It Works

1. The Config Server starts with the `native` profile, pointing to this directory
2. When a service (e.g., `order-service`) starts, it requests its configuration from the Config Server
3. The Config Server returns the matching YAML file (e.g., `order-service.yml`)
4. The service applies the configuration and begins operation

---

## Configuration Files

| File                        | Service              | Port  | Database       |
| --------------------------- | -------------------- | ----- | -------------- |
| `order-service.yml`         | Order Service        | 8082  | `order_db`     |
| `inventory-service.yml`     | Inventory Service    | 8083  | `inventory_db` |
| `payment-service.yml`       | Payment Service      | 8084  | `payment_db`   |
| `notification-service.yml`  | Notification Service | 8085  | None           |

---

## Common Configuration Sections

### Database (Order, Inventory, Payment)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST:127.0.0.1}:${POSTGRES_PORT:5433}/<db_name>
    username: ${POSTGRES_USER:postgres}
    password: ${POSTGRES_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: validate       # Flyway manages schema
  flyway:
    enabled: true
    baseline-on-migrate: true
```

### Kafka

All services use a standardized Kafka configuration:

- **Producer**: `StringSerializer` / `JsonSerializer` with idempotence enabled
- **Consumer**: `StringDeserializer` for both key and value (manual ObjectMapper parsing)
- **Ack Mode**: `manual_immediate` (manual offset commits)
- **Auto Commit**: Disabled
- **Offset Reset**: `earliest`

### Service Discovery

```yaml
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka/}
```

### Actuator

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

---

## Environment Variable Overrides

All sensitive and environment-specific values are externalized via environment variables with sensible defaults for local development:

| Variable                           | Default Value                          | Used By                   |
| ---------------------------------- | -------------------------------------- | ------------------------- |
| `POSTGRES_HOST`                    | `127.0.0.1`                            | Order, Inventory, Payment |
| `POSTGRES_PORT`                    | `5433`                                 | Order, Inventory, Payment |
| `POSTGRES_USER`                    | `postgres`                             | Order, Inventory, Payment |
| `POSTGRES_PASSWORD`                | `postgres`                             | Order, Inventory, Payment |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS`   | `localhost:9094`                       | All services              |
| `EUREKA_SERVER_URL`                | `http://localhost:8761/eureka/`         | All services              |

---

## Failure Simulation

Inventory and Payment services include configurable failure rates for testing saga compensating transactions:

```yaml
# inventory-service.yml
inventory:
  failure-rate: 0.10    # 10% chance of inventory failure

# payment-service.yml
payment:
  failure-rate: 0.10    # 10% chance of payment failure
```

Adjust these values to stress-test the saga rollback mechanism.

---

## Adding a New Service

1. Create a new YAML file: `configs/<service-name>.yml`
2. Configure the service's `spring.application.name` to match the filename
3. Point the service to the Config Server via `spring.config.import`
4. The Config Server will automatically serve the new configuration
