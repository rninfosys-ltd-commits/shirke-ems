# Use Maven with Eclipse Temurin for build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
ENV MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m"
RUN mvn -B clean package -DskipTests

# Use Eclipse Temurin JRE for runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/shirke-erp.war app.war
EXPOSE 8080
ENV JAVA_OPTS="-Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.war"]
