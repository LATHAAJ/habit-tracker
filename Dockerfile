FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /build
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle .
COPY src ./src
COPY frontend ./frontend
RUN ./gradlew -q -x test bootJar

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/build/libs/habit-tracker-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
