FROM eclipse-temurin:21 as build
LABEL author=sambhavmahajan
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
FROM eclipse-temurin:21-jre as deploy
WORKDIR /app
COPY --from=build /app/target/CrowPing-0.0.1-SNAPSHOT.jar /app/app.jar
CMD ["java", "-jar", "app.jar"]