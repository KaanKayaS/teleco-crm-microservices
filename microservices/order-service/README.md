# Order Service

This microservice handles the order taking and saga orchestration for the Teleco CRM project.

## Features
- Handles `POST /api/v1/orders` to create orders.
- Handles `GET /api/v1/orders/{id}` to fetch orders.
- Handles `POST /api/v1/orders/{id}/cancel` to cancel orders.
- Saga Orchestration: Manages Customer -> Catalog -> Subscription -> Payment chain.
- Produces events: `OrderCreated`, `OrderConfirmed`, `OrderCancelled`.
- Consumes events: `PaymentCompleted`, `PaymentFailed`, `SubscriptionActivated`.
- Secured with Keycloak (JWT).
- Uses PostgreSQL for state management.
- Uses Kafka for asynchronous event messaging.
