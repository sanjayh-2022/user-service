FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/user-service-0.0.1-SNAPSHOT.jar user-service.jar
EXPOSE 8082
CMD ["java", "-jar", "user-service.jar"]
