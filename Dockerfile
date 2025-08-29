FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src


COPY resources/ ./src/main/resources/

RUN mvn clean package -DskipTests -Pprod

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/jira-1.0.jar app.jar


COPY resources/static ./resources/static/
COPY resources/view ./resources/view/

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]