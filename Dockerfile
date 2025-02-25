# 1. Use an official JDK base image (e.g., Eclipse Temurin / OpenJDK)
FROM eclipse-temurin:17-jdk AS build

# 2. Create a directory for our app
WORKDIR /app

# 3. Copy the Maven build files (pom.xml, etc.) first to leverage caching
COPY pom.xml /app/
COPY src /app/src

# 4. Build the application (this step will run Maven)
RUN mvn -f pom.xml clean package -DskipTests

# 5. Create a "runtime" image (slimmer) - optional multi-stage build
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 6. Copy the JAR file from the "build" stage into this final stage
COPY --from=build /app/target/*.jar app.jar

# 7. Expose port 8080 for the Spring Boot app
EXPOSE 8080

# 8. Run the JAR
ENTRYPOINT ["java","-jar","app.jar"]
