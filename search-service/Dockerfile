FROM eclipse-temurin:25-jdk-jammy AS builder
SHELL ["/bin/bash", "-c"]
WORKDIR /workspace

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew
# Normalize line endings to avoid shell parsing issues when building on Windows hosts
RUN apt-get update \
    && apt-get install -y --no-install-recommends dos2unix \
    && dos2unix gradlew gradle/wrapper/gradle-wrapper.properties \
    && sed -i '/^set -- "$@" ""/d' gradlew \
    && sed -i 's/^DEFAULT_JVM_OPTS=.*/DEFAULT_JVM_OPTS=""/' gradlew \
    && sed -i '1s|/bin/sh|/bin/bash|' gradlew \
    && rm -rf /var/lib/apt/lists/*

COPY src ./src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
