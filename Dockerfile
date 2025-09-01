# Build stage
FROM maven:3.9.7-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml /app/
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:21

WORKDIR /app

COPY --from=build /app/target/EcoTracker-0.0.1-SNAPSHOT.jar /app/EcoTracker-0.0.1-SNAPSHOT.jar

EXPOSE 8080

CMD ["java", "-jar", "EcoTracker-0.0.1-SNAPSHOT.jar"]
