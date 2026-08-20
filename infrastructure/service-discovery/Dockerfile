FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Install curl for healthcheck
RUN apk add --no-cache curl
COPY target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
