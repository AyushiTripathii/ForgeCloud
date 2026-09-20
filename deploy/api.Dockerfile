FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src src
RUN mvn -q verify

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S forgecloud && adduser -S forgecloud -G forgecloud
USER forgecloud
COPY --from=build /workspace/target/forgecloud-api-0.1.0-SNAPSHOT.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]

