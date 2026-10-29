FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY api/pom.xml .
COPY api/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 21090
ENTRYPOINT ["java","-jar","app.jar"]
