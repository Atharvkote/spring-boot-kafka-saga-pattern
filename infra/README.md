# Infrastructure Services

This directory contains the **infrastructure backbone** services required by all business microservices. These must be started before any business service.

## Services

### Config Server (`config-server/`)

| Property       | Value                                                  |
| -------------- | ------------------------------------------------------ |
| **Port**       | `8888`                                                 |
| **Framework**  | Spring Cloud Config Server                             |
| **Backend**    | Native filesystem (`configs/` directory)               |
| **Base Image** | `eclipse-temurin:17-jre-alpine`                        |

The Config Server provides centralized, externalized configuration for all microservices. On startup, each service fetches its YAML configuration from this server.

**Health Check:**
```bash
curl http://localhost:8888/actuator/health
```

**Fetch a service config:**
```bash
curl http://localhost:8888/order-service/default
```

### Service Discovery (`service-discovery/`)

| Property       | Value                                                  |
| -------------- | ------------------------------------------------------ |
| **Port**       | `8761`                                                 |
| **Framework**  | Netflix Eureka Server                                  |
| **Dashboard**  | http://localhost:8761                                   |
| **Base Image** | `eclipse-temurin:17-jre-alpine`                        |

Eureka Server enables dynamic service registration and discovery. All microservices register themselves on startup and can locate other services by name instead of hardcoded URLs.

**Health Check:**
```bash
curl http://localhost:8761/actuator/health
```

### API Gateway (`api-gateway/`)

| Property       | Value                                                  |
| -------------- | ------------------------------------------------------ |
| **Port**       | `8081`                                                 |
| **Framework**  | Spring Cloud Gateway                                   |
| **Routing**    | Eureka-based dynamic routing                           |
| **Base Image** | `eclipse-temurin:17-jre-alpine`                        |

The API Gateway acts as the single entry point for all client requests. It routes traffic to the appropriate downstream service using Eureka for service lookup.

**Health Check:**
```bash
curl http://localhost:8081/actuator/health
```

## Startup Order

Infrastructure services must be started in the following order due to dependency chains:

```
1. Config Server     (no dependencies)
2. Service Discovery (depends on Config Server)
3. API Gateway       (depends on Service Discovery)
```

## Building

From the project root:

```bash
# Build all infrastructure modules
mvn clean package -DskipTests -pl infra/config-server,infra/service-discovery,infra/api-gateway

# Or build individually
cd infra/config-server && mvn clean package -DskipTests
```

## Docker

Each service has its own `Dockerfile`. They are built automatically when using the full-stack Docker Compose:

```bash
docker compose -f docker/deps.compose.yml up -d --build
```

## Dockerfile Template

All infrastructure services follow this pattern:

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl    # For health checks (config-server, service-discovery)
COPY target/*.jar app.jar
EXPOSE <port>
ENTRYPOINT ["java", "-jar", "app.jar"]
```
