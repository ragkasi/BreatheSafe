# BreatheSafe Locker System

A Spring Boot-based locker system that uses MySQL and Twilio for SMS-based locker control. This application is containerized with Docker Compose.

## Table of Contents

- [Introduction](#introduction)
- [Architecture Overview](#architecture-overview)
- [Prerequisites](#prerequisites)
- [Setup and Implementation](#setup-and-implementation)
  - [1. Project Initialization & Dependencies](#1-project-initialization--dependencies)
  - [2. Code Structure Overview](#2-code-structure-overview)
    - [Entities](#entities)
    - [Controllers](#controllers)
    - [Security Configuration](#security-configuration)
    - [SMS Service Integration](#sms-service-integration)
  - [3. Application Properties](#3-application-properties)
  - [4. Containerization with Docker Compose](#4-containerization-with-docker-compose)
- [Running the Application](#running-the-application)
- [Next Steps and Improvements](#next-steps-and-improvements)
- [Troubleshooting](#troubleshooting)

## Introduction

The **BreatheSafe Locker System** manages lockers by accepting user information and controlling physical locks. Unlocking is done via SMS messages (handled by Twilio). This document details the end-to-end process from initial setup to running the application in Docker containers.

## Architecture Overview

- **Backend:** Java Spring Boot 3.4.3 with Maven  
- **Database:** MySQL (containerized)  
- **SMS Integration:** Twilio API  
- **Security:** Spring Security with custom JWT filters  
- **Containerization:** Docker & Docker Compose

## Prerequisites

- Java 17  
- Maven  
- Docker Desktop (using Linux containers is recommended)  
- A Twilio account with valid credentials  
- Git (optional)

## Setup and Implementation

### 1. Project Initialization & Dependencies

- **Generate the project:**  
  Use [Spring Initializr](https://start.spring.io/) to create a Maven project with Java 17. Select dependencies such as Spring Web, Spring Data JPA, and Spring Security.

- **Configure Maven:**  
  Update your `pom.xml` to use Spring Boot 3.4.3. Important dependencies include:
  - `spring-boot-starter-web` (for REST controllers)
  - `spring-boot-starter-data-jpa` (for JPA using Jakarta Persistence)
  - `spring-boot-starter-security`
  - `twilio` (Twilio SDK)
  - `mysql-connector-j` (MySQL driver)
  - `jakarta.annotation-api` (for annotations like `@PostConstruct`)

  *Example snippet:*
  ```xml
  <dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <!-- Twilio SDK -->
    <dependency>
      <groupId>com.twilio.sdk</groupId>
      <artifactId>twilio</artifactId>
      <version>8.32.0</version>
    </dependency>
    <!-- MySQL Connector -->
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <scope>runtime</scope>
    </dependency>
    <!-- Jakarta Annotation API -->
    <dependency>
      <groupId>jakarta.annotation</groupId>
      <artifactId>jakarta.annotation-api</artifactId>
      <version>2.1.1</version>
    </dependency>
    <!-- ... other dependencies ... -->
  </dependencies>

### 2. Code Structure Overview

This section outlines the main components of our codebase, including how we organize our entities, controllers, security configuration, and SMS service integration.

#### Entities

- **Location:** `com.example.breathesafe.entities`
- **Purpose:** Define the data model using JPA entities.
- **Details:**  
  - Use Jakarta Persistence annotations (e.g., `@Entity`, `@Table`, `@Id`, `@GeneratedValue`).
  - Example snippet:
    ```java
    @Entity
    @Table(name = "users")
    public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;
        private String phoneNumber;
        // Getters and setters...
    }
    ```
  - A similar structure is applied for the `Locker` entity.

#### Controllers

- **Location:** `com.example.breathesafe.controllers`
- **Purpose:** Handle incoming HTTP requests and expose REST endpoints.
- **Details:**  
  - Controllers are annotated with Spring Web annotations such as `@RestController`, `@RequestMapping`, `@PostMapping`, and `@RequestParam`.
  - Example snippet (for SMS webhook processing):
    ```java
    @RestController
    @RequestMapping("/api")
    public class SmsWebhookController {
        @PostMapping("/sms-webhook")
        public void receiveSms(@RequestParam("From") String from,
                               @RequestParam("Body") String body) {
            // Process the SMS message (e.g., parse commands and validate the sender)
        }
    }
    ```

#### Security Configuration

- **Location:** `com.example.breathesafe.security`
- **Purpose:** Secure the application using Spring Security.
- **Details:**  
  - A `SecurityConfig` class defines a `SecurityFilterChain` bean for HTTP security.
  - Custom JWT filters (such as `JwtAuthenticationFilter` and `JwtAuthorizationFilter`) are added to handle token-based authentication.
  - Example snippet:
    ```java
    http
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth
          .requestMatchers("/api/sms-webhook").permitAll()
          .anyRequest().authenticated()
      )
      .addFilter(new JwtAuthenticationFilter(authManager))
      .addFilter(new JwtAuthorizationFilter(authManager));
    ```
  - Note: Minimal placeholder implementations for the JWT filters are provided, with the expectation that you will later add JWT parsing, validation, and token generation logic.

#### SMS Service Integration

- **Location:** `com.example.breathesafe.services`
- **Purpose:** Integrate with Twilio to send SMS messages.
- **Details:**  
  - The `SmsService` uses `@Value` annotations to inject Twilio credentials from the `application.properties` file.
  - The Twilio client is initialized in a `@PostConstruct` method.
  - Example snippet:
    ```java
    @Service
    public class SmsService {
    
        @Value("${twilio.accountSid}")
        private String accountSid;
    
        @Value("${twilio.authToken}")
        private String authToken;
    
        @Value("${twilio.phoneNumber}")
        private String fromNumber;
    
        @PostConstruct
        public void init() {
            Twilio.init(accountSid, authToken);
        }
    
        public void sendSms(String to, String body) {
            Message message = Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(fromNumber),
                body
            ).create();
        }
    }
    ```
  - This service is responsible for sending commands (such as locker unlock requests) via SMS.

This overview provides a high-level guide to how the application’s code is structured and how its core functionalities are implemented.

### 3. Application Properties

The application properties for BreatheSafe are defined in the `src/main/resources/application.properties` file. This file is used by Spring Boot to configure various aspects of the application at runtime, including the application name, server port, database settings, and external service credentials such as those for Twilio.

#### Key Configurations

- **Application Settings:**  
  Sets the name and port for the Spring Boot application.
  ```properties
  spring.application.name=breathesafe
  server.port=8080
  ```

- **Datasource Configuration**
  Configures the database connection using environment variables. This enables flexibility and security by not hardcoding sensitive information in your code.
  ```properties
  spring.datasource.url=${SPRING_DATASOURCE_URL}
  spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
  spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
  spring.jpa.hibernate.ddl-auto=update
  ```

- **Twilio Credentials**
  Sets the credentials needed to interact with the Twilio API. These values are also injected via environment variables.
  ```properties
  twilio.accountSid=${TWILIO_ACCOUNT_SID}
  twilio.authToken=${TWILIO_AUTH_TOKEN}
  twilio.phoneNumber=${TWILIO_PHONE_NUMBER}
  ```

Create a .env file to store the above information.

#### How It Works

- **Environment Variable Injection:**
  Spring Boot automatically loads the properties from this file at startup. Using the ${VARIABLE_NAME} syntax allows you to inject values from your environment. For instance, in your SmsService.java, the Twilio credentials are injected as follows:
  ```java
  @Value("${twilio.accountSid}")
  private String accountSid;
  ```
  
- **Docker and Deployment:**
  When running the application via Docker Compose, the environment variables are passed in the docker-compose.yml file. For example:
  ```yaml
  environment:
  SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/locker_db
  SPRING_DATASOURCE_USERNAME: locker_user
  SPRING_DATASOURCE_PASSWORD: password
  TWILIO_ACCOUNT_SID: ACxxxx...
  TWILIO_AUTH_TOKEN: xxxx...
  TWILIO_PHONE_NUMBER: +123456789
  ```
  This setup ensures that the application uses the correct configuration based on its deployment environment.

### 4. Containerization with Docker Compose

This section explains how to containerize the BreatheSafe application using Docker and Docker Compose. We set up a multi-stage Docker build for the Spring Boot application and configure Docker Compose to run both the application and MySQL database.

#### Dockerfile

The `Dockerfile` builds the application in two stages:
1. **Build Stage:** Uses a Maven image with Java 17 to compile and package the application.
2. **Runtime Stage:** Uses a slim JDK image to run the generated JAR file.

*Example Dockerfile snippet:*
```dockerfile
# Build stage: use a Maven image with Java 17 to build the app
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app
# Copy Maven configuration and source code
COPY pom.xml /app/
COPY src /app/src
# Build the application without running tests
RUN mvn clean package -DskipTests

# Runtime stage: use a slim JDK image for running the app
FROM eclipse-temurin:17-jdk
WORKDIR /app
# Copy the packaged JAR from the build stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Running the Application

To run the BreatheSafe Locker System after you’ve completed the setup and containerization, follow these steps:

1. **Build the Docker Images:**
   - From the project root, run:
     ```bash
     docker compose build
     ```

2. **Start the Containers:**
   - Launch the services with:
     ```bash
     docker compose up
     ```
   - This will start both the Spring Boot application (exposed on port 8080) and the MySQL database (mapped to host port 3307).

3. **Verify Operation:**
   - Open cmd and ran:
   ```cmd
   ngrok http http://localhost:8080
   ```
   - Open your browser and navigate to [http://localhost:8080](http://localhost:8080) to access the application.
   - Use a MySQL client to connect to your database at `localhost:3307` if needed.
   - Monitor the logs in a separate terminal with:
     ```bash
     docker compose logs -f
     ```
   - Ensure Twilio is configured to send SMS to your webhook endpoint (e.g., `http://<your-domain>:8080/api/sms-webhook`).

## Next Steps and Improvements

After getting the application running, consider the following improvements and next steps:

- **Implement Full JWT Functionality:**
  - Complete the logic in `JwtAuthenticationFilter` and `JwtAuthorizationFilter` to generate, validate, and parse JWT tokens.
  
- **Enhance Security:**
  - Configure HTTPS for secure communication.
  - Consider integrating a more robust user management system and storing credentials securely.
  
- **Write Tests:**
  - Develop unit tests and integration tests for controllers, services, and security components.
  
- **Set Up CI/CD:**
  - Automate your builds, tests, and deployments using a CI/CD pipeline (e.g., GitHub Actions, Jenkins, GitLab CI).
  
- **Monitoring and Logging:**
  - Integrate monitoring tools (e.g., Prometheus, Grafana) and centralized logging (e.g., ELK stack) for production environments.
  
- **Documentation:**
  - Expand project documentation to cover API endpoints, architecture decisions, and troubleshooting guides.
  
- **Performance Tuning:**
  - Analyze application performance and optimize resource usage, database queries, and Docker configurations for production.

## Troubleshooting

If you encounter issues during development or deployment, consider the following troubleshooting tips:

- **Port Conflicts:**
  - Verify that no other service is using the host ports (e.g., port 3306 for MySQL). Adjust the port mapping in `docker-compose.yml` if needed.
  
- **Dependency Problems:**
  - Run `mvn clean install` locally to check for any compilation or dependency errors.
  - Ensure that your IDE has re-imported the Maven dependencies properly.
  
- **Docker Issues:**
  - Confirm that Docker Desktop is running and that you are using Linux containers (recommended for this project).
  - Use `docker compose logs -f` to inspect container logs for errors.
  
- **Environment Variables:**
  - Ensure that the environment variables (for datasource and Twilio credentials) are correctly set in your `docker-compose.yml` and not missing.
  
- **Property Injection:**
  - Make sure `application.properties` is located in `src/main/resources` so Spring Boot can load it.
  
- **JWT and Security Errors:**
  - If JWT filters or security configurations are causing errors, temporarily disable them or revert to default settings until you resolve configuration issues.
  
- **General Debugging:**
  - Use verbose logging (`--debug` or `--info` flags) with Maven and Docker for more detailed error messages.

By following these troubleshooting steps and next steps for improvements, you can iteratively refine your application toward a production-ready state.


