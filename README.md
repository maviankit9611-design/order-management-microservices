# Order Management Microservices

A backend Order Management application built using Java 17 and Spring
Boot following a microservices architecture.

The application demonstrates REST API development, service discovery,
inter-service communication, event-driven architecture, caching,
authentication, containerization, and cloud-ready backend development.

## Architecture

```text
                         ┌──────────────────────┐
                         │       Client         │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │     API Gateway      │
                         └──────────┬───────────┘
                                    │
                 ┌──────────────────┼──────────────────┐
                 │                  │                  │
                 ▼                  ▼                  ▼
        ┌────────────────┐ ┌────────────────┐ ┌────────────────┐
        │ Order Service  │ │Product Service │ │ Notification   │
        │                │ │                │ │ Service        │
        └───────┬────────┘ └───────┬────────┘ └───────▲────────┘
                │                  │                  │
                │                  │                  │
                ▼                  ▼                  │
        ┌────────────────┐ ┌────────────────┐          │
        │   PostgreSQL   │ │   PostgreSQL   │          │
        └────────────────┘ └────────────────┘          │
                                                       │
                         ┌──────────────────────┐       │
                         │       Kafka          │───────┘
                         │  Event Streaming     │
                         └──────────────────────┘

                         ┌──────────────────────┐
                         │        Redis        │
                         │       Caching       │
                         └──────────────────────┘

                         ┌──────────────────────┐
                         │   Eureka Server     │
                         │  Service Discovery  │
                         └──────────────────────┘
