FROM maven:3.9.5-eclipse-temurin-21 AS build
LABEL author=sambhavmahajan
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
FROM eclipse-temurin:21-jre as deploy
WORKDIR /app
COPY --from=build /app/target/CrowPing-0.0.1-SNAPSHOT.jar /app/app.jar
CMD ["java", "-jar", "app.jar"]