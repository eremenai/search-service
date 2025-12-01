FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew
# Download dependencies first to leverage Docker layer caching
RUN ./gradlew --no-daemon dependencies

COPY src ./src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
