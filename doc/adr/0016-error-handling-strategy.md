# 16. Error Handling Strategy

Date: 2026-03-27

## Status

Accepted

## Context

EmberVault needs a consistent, layered error handling strategy that aligns with the hexagonal architecture (ADR-0009) and the MVVM pattern (ADR-0013). Without a deliberate approach, error handling tends to become ad-hoc: domain validation failures may surface as raw exceptions in the UI, error messages may leak implementation details to users, and catch blocks may silently swallow important failures. A well-defined strategy ensures that each layer handles errors at the appropriate level of abstraction and that users receive clear, actionable feedback.

Java's checked exception mechanism, while well-intentioned, introduces tight coupling between layers and clutters method signatures with exception declarations that must be propagated through every intermediate caller. Unchecked exceptions provide the same signalling capability without forcing every method in the call chain to declare or catch them, which better suits a layered architecture where exceptions naturally bubble up to a designated handling boundary.

## Decision

We will adopt a layered error handling strategy using exclusively unchecked exceptions. All custom exceptions extend `RuntimeException` (directly or through a base class), and no checked exceptions are introduced by application code.

### Domain Layer

The domain layer defines a base `DomainException` class (extending `RuntimeException`) and domain-specific subclasses that communicate business rule violations:

- `DomainException` — Abstract base class for all domain-originated errors. Resides in `com.embervault.domain`.
- `EntityNotFoundException` — Thrown when a requested entity does not exist. Extends `DomainException`.
- `ValidationException` — Thrown when a domain invariant or input validation rule is violated. Extends `DomainException`.

Additional domain exception subclasses may be introduced as the domain model grows; all must extend `DomainException`. This constraint is enforced by an ArchUnit rule in the build.

### Application Layer

The application layer (use cases / inbound ports) catches domain exceptions when translation or enrichment is needed. If a use case must signal a failure that is not purely domain-related (e.g., a coordination failure across multiple domain operations), it may throw an `ApplicationException` (extending `RuntimeException`). In most cases, domain exceptions pass through the application layer unmodified.

### UI Layer — ViewModel

ViewModels (ADR-0013) are the designated error handling boundary for the UI. A ViewModel catches application and domain exceptions arising from use case calls and translates them into observable error state — typically a `StringProperty` for an error message and a `BooleanProperty` indicating whether an error is active. ViewModels never let exceptions propagate to the View or to the JavaFX runtime.

### UI Layer — View

Views bind to the ViewModel's error properties and display user-friendly messages using standard JavaFX controls (labels, alerts, banners). Views contain no try-catch blocks and no exception handling logic of their own; they rely entirely on the ViewModel to manage error state.

### Summary of Error Flow

```
Domain (throws DomainException subclasses)
  --> Application (catches/translates if needed, may throw ApplicationException)
    --> ViewModel (catches all exceptions, exposes error state via observable properties)
      --> View (binds to error properties, displays messages)
```

## Consequences

- Every layer handles errors at its own level of abstraction, preventing leaky abstractions.
- Using only unchecked exceptions keeps method signatures clean and avoids forced propagation through intermediate layers.
- The `DomainException` base class provides a single type to catch when a layer needs to handle all domain errors generically.
- ArchUnit enforces that domain exceptions extend `DomainException`, preventing accidental introduction of ad-hoc exception classes in the domain layer.
- ViewModels serve as the error boundary for the UI, ensuring that unhandled exceptions never crash the JavaFX application thread.
- Views remain thin and declarative, consistent with ADR-0013.
- Developers must decide the appropriate exception granularity for new domain rules, which adds a small amount of design overhead.
