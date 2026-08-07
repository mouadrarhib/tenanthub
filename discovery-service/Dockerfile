# syntax=docker/dockerfile:1
#
# Build context must be the repo root (tenanthub/), NOT this directory --
# discovery-service has no shared-events dependency, but the root pom.xml
# still declares every module, so the reactor needs all their pom.xml files
# present to resolve itself, even though only this module's source is built.
#
#   docker build -f discovery-service/Dockerfile -t tenanthub/discovery-service:latest .

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Every module's pom.xml (not source yet) so Maven can resolve the full
# multi-module reactor. This layer only invalidates when a pom.xml changes,
# not on every source edit, so dependency:go-offline stays cached.
COPY pom.xml .
COPY shared-events/pom.xml shared-events/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY tenant-service/pom.xml tenant-service/pom.xml
COPY project-service/pom.xml project-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY billing-service/pom.xml billing-service/pom.xml
COPY gateway-service/pom.xml gateway-service/pom.xml
COPY discovery-service/pom.xml discovery-service/pom.xml
RUN mvn -B -pl discovery-service dependency:go-offline

# Real source: this service only -- no shared-events dependency here.
COPY discovery-service/src discovery-service/src
RUN mvn -B -pl discovery-service clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine AS runtime
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app
COPY --from=build /workspace/discovery-service/target/*.jar app.jar

# MaxRAMPercentage keeps the JVM heap sized relative to the container's
# memory limit (set in docker-compose/K8s) instead of the host's, which is
# what -Xmx would otherwise see.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
EXPOSE 8761
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
