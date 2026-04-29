# Use Maven for build
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Use OpenJDK for runtime
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/shirke-erp.war app.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.war"]
