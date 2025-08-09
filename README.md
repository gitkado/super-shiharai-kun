# super-shiharai-kun

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- The [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). You'll need to [request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up) to join.

## Features

Here's a list of features included in this project:

| Name                                                                   | Description                                                                        |
| ------------------------------------------------------------------------|------------------------------------------------------------------------------------ |
| [Call Logging](https://start.ktor.io/p/call-logging)                   | Logs client requests                                                               |
| [Call ID](https://start.ktor.io/p/callid)                              | Allows to identify a request/call.                                                 |
| [Routing](https://start.ktor.io/p/routing)                             | Provides a structured routing DSL                                                  |
| [Swagger](https://start.ktor.io/p/swagger)                             | Serves Swagger UI for your project                                                 |
| [OpenAPI](https://start.ktor.io/p/openapi)                             | Serves OpenAPI documentation                                                       |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [Postgres](https://start.ktor.io/p/postgres)                           | Adds Postgres database to your application                                         |
| [Exposed](https://start.ktor.io/p/exposed)                             | Adds Exposed database to your application                                          |
| [CORS](https://start.ktor.io/p/cors)                                   | Enables Cross-Origin Resource Sharing (CORS)                                       |
| [Status Pages](https://start.ktor.io/p/status-pages)                   | Provides exception handling for routes                                             |
| [Request Validation](https://start.ktor.io/p/request-validation)       | Adds validation for incoming requests                                              |
| [Authentication](https://start.ktor.io/p/auth)                         | Provides extension point for handling the Authorization header                     |
| [Authentication JWT](https://start.ktor.io/p/auth-jwt)                 | Handles JSON Web Token (JWT) bearer authentication scheme                          |
| [Jackson](https://start.ktor.io/p/ktor-jackson)                        | Handles JSON serialization using Jackson library                                   |

## Prerequisites

Ensure you have Java installed and `JAVA_HOME` environment variable set:

```bash
# Check Java version
java -version

# Set JAVA_HOME (example for Unix-like systems)
export JAVA_HOME=/path/to/your/java

# For SDKMAN users
export JAVA_HOME=$HOME/.sdkman/candidates/java/current
```

## Building & Running

To build or run the project, use one of the following tasks:

| Task                          | Description                                                          |
| -------------------------------|---------------------------------------------------------------------- |
| `./gradlew test`              | Run the tests                                                        |
| `./gradlew build`             | Build everything                                                     |
| `buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `buildImage`                  | Build the docker image to use with the fat JAR                       |
| `publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `run`                         | Run the server                                                       |
| `runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## Project Structure

The project follows a layered architecture pattern with clear separation of concerns:

```
src/main/kotlin/com/example/
├── Application.kt                    # Main application entry point
├── config/                          # Configuration layer
│   ├── database/                    # Database connection settings
│   ├── security/                    # JWT and authentication configuration
│   ├── http/                        # CORS and HTTP settings
│   └── serialization/               # JSON serialization settings
├── domain/                          # Domain layer (business logic)
│   ├── model/                       # Domain entities
│   ├── repository/                  # Repository interfaces
│   ├── service/                     # Business logic services
│   ├── constants/                   # Business constants and rules
│   └── exception/                   # Business exceptions
├── infrastructure/                  # Infrastructure layer
│   ├── database/                    # Database implementation
│   │   ├── schema/                  # Database table definitions
│   │   └── repository/              # Repository implementations
│   └── security/                    # Security implementations
├── presentation/                    # Presentation layer (API)
│   ├── dto/                        # Data Transfer Objects
│   │   ├── request/                # Request DTOs
│   │   └── response/               # Response DTOs
│   ├── controller/                 # REST controllers
│   └── routing/                    # Route definitions
└── util/                           # Technical utility functions
```

### Layer Responsibilities

- **Domain Layer**: Contains business logic, entities, and rules. Independent of external concerns.
- **Infrastructure Layer**: Implements technical concerns like database access and external services.
- **Presentation Layer**: Handles HTTP requests/responses and API contracts.
- **Configuration Layer**: Manages application settings and framework configuration.
- **Utility Layer**: Provides technical helper functions and extensions.

