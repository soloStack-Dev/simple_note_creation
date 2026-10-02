# Build the jar with Maven, then ship it on a JRE-only image.
# The wrapper is script-only, so it bootstraps Maven 3.9.16 itself: no local
# Maven install is needed inside the build stage.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

# Dependencies first, so a source-only change reuses the cached layer.
# go-offline needs the POM present, hence the two COPY lines.
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./
RUN ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q clean package -DskipTests


FROM eclipse-temurin:21-jre
WORKDIR /app

# curl is not in the base image, and the healthcheck below needs it.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Run unprivileged: nothing here needs root.
RUN useradd --system --create-home --uid 1001 appuser
COPY --from=build /build/target/*.jar app.jar
USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD ["sh", "-c", "curl -fsS http://localhost:8080/actuator/health || exit 1"]

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
