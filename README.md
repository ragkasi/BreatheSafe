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
    - [Dockerfile](#dockerfile)
    - [Ngrok Tunnel Setup](#ngrok-tunnel-setup)
- [Running the Application](#running-the-application)
- [Next Steps and Improvements](#next-steps-and-improvements)
- [Troubleshooting](#troubleshooting)

## Introduction

The **BreatheSafe Locker System** manages lockers by accepting user information and controlling physical locks. Unlocking is done via SMS messages (handled by Twilio). This document details the end-to-end process from initial setup to running the application in Docker containers. This project is registered to respond to the number +12346023617.

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
- An ngrok account with an authtoken and a reusable URL (optional but recommended)  
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


### 2. Code Structure Overview

This section outlines the main components of our codebase, including how we organize our entities, controllers, security configuration, and SMS service integration.

#### Entities

- **Location:** `com.example.breathesafe.entities`
- **Purpose:** Define the data model using JPA entities.
- **Details:**  
  - Use Jakarta Persistence annotations (e.g., `@Entity`, `@Table`, `@Id`, `@GeneratedValue`).

#### Controllers

- **Location:** `com.example.breathesafe.controllers`
- **Purpose:** Handle incoming HTTP requests and expose REST endpoints.
- **Details:**  
  - Controllers are annotated with Spring Web annotations such as `@RestController`, `@RequestMapping`, `@PostMapping`, and `@RequestParam`.

#### Security Configuration

- **Location:** `com.example.breathesafe.security`
- **Purpose:** Secure the application using Spring Security.
- **Details:**  
  - A `SecurityConfig` class defines a `SecurityFilterChain` bean for HTTP security.
  - Custom JWT filters (such as `JwtAuthenticationFilter` and `JwtAuthorizationFilter`) are added to handle token-based authentication.

#### SMS Service Integration

- **Location:** `com.example.breathesafe.services`
- **Purpose:** Integrate with Twilio to send SMS messages.
- **Details:**  
  - The `SmsService` uses `@Value` annotations to inject Twilio credentials from the `application.properties` file.
  - The Twilio client is initialized in a `@PostConstruct` method.  

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
  This setup ensures that the application uses the correct configuration based on its deployment environment.

### 4. Containerization with Docker Compose

This section explains how to containerize the BreatheSafe application using Docker and Docker Compose. We set up a multi-stage Docker build for the Spring Boot application and configure Docker Compose to run both the application and MySQL database.

#### Dockerfile

The `Dockerfile` builds the application in two stages:
1. **Build Stage:** Uses a Maven image with Java 17 to compile and package the application.
2. **Runtime Stage:** Uses a slim JDK image to run the generated JAR file.

#### Ngrok Tunnel Setup

The ngrok service in the Docker Compose file is used to expose your application to the public internet. Key points for setting up the tunnel:

- **NGROK_AUTHTOKEN**
  Ensure you have an ngrok authtoken registered in your ngrok account. Set this value in your environment or .env file as NGROK_AUTH_TOKEN.

- **Reusable URL**
  The command includes the parameter --url=hawk-capable-greatly.ngrok-free.app, which is your reusable ngrok URL. This ensures that your public URL remains constant across tunnel restarts.

- **Upstream Service**
  The command parameter "locker-app:8080" tells ngrok to route traffic to your Spring Boot container by its service name (app is aliased as locker-app in our Compose file) on port 8080.

- **Dashboard**
  Mapping port 4040 lets you access the ngrok dashboard locally via http://localhost:4040.

Make sure your .env file includes both NGROK_AUTH_TOKEN and other required environment variables.

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
   - This will start the Spring Boot application on port 8080, the MySQL database on host port 3307, and the ngrok tunnel.

3. **Verify Operation:**
   - Access the application via the public ngrok URL (e.g., https://hawk-capable-greatly.ngrok-free.app).
   - Use a MySQL client to connect to your database at localhost:3307 if needed.
   - Open http://localhost:4040 to view the ngrok dashboard.
   - Configure Twilio to forward SMS to your public ngrok URL (e.g., https://hawk-capable-greatly.ngrok-free.app/api/sms-webhook).

## Next Steps and Improvements

This application is still in development. Next steps:

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

- **Ngrok Tunnel:**
  - Verify that your ngrok configuration includes a valid authtoken and that the reusable URL is correctly specified. If the tunnel does not connect, check the logs using docker compose logs ngrok.


By following these troubleshooting steps and next steps for improvements, you can iteratively refine your application toward a production-ready state.


