# Architecture Boundaries

This project is organized as a Maven multi-module client-server system.

## Modules

- `auction-common`: shared contracts only. This module owns DTOs, domain models, and role policies used by both sides.
- `auction-server`: server runtime, socket handling, action routing, business services, DAO, database configuration, and server resources.
- `auction-client`: JavaFX client runtime, FXML resources, controllers, presenters, client services, session state, and socket client code.

## Dependency Rules

- `auction-client` depends on `auction-common`.
- `auction-server` depends on `auction-common`.
- `auction-common` must not depend on `auction-client` or `auction-server`.
- `auction-client` must not import `com.auction.server.*`.
- `auction-server` must not import `com.auction.client.*`.

## Runtime Responsibility

- The client sends requests, receives responses/events, stores local UI/session state, and renders JavaFX views.
- The server owns business decisions, auction state transitions, database access, authentication, bidding, admin actions, and broadcasting.
- The common module is a protocol and model contract. It should stay free of runtime behavior specific to either side.
