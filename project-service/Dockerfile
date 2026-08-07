# syntax=docker/dockerfile:1
#
# Build context must be the repo root (tenanthub/), NOT this directory --
# this service depends on the sibling `shared-events` module through Maven's
# multi-module reactor, so the build stage needs every module's pom.xml to
# resolve that reactor, plus shared-events' own source to build it first.
#
#   docker build -f project-service/Dockerfile -t tenanthub/project-service:latest .

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
RUN mvn -B -pl project-service -am dependency:go-offline

# Real source: only the dependency (shared-events) and this service itself.
COPY shared-events/src shared-events/src
COPY project-service/src project-service/src
RUN mvn -B -pl project-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine AS runtime
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app
COPY --from=build /workspace/project-service/target/*.jar app.jar

# MaxRAMPercentage keeps the JVM heap sized relative to the container's
# memory limit (set in docker-compose/K8s) instead of the host's, which is
# what -Xmx would otherwise see.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
EXPOSE 8083
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
