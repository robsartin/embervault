# 17. Use Result-Oriented APIs

Date: 2026-03-27

## Status

Accepted

## Context

ADR-0016 established a layered error handling strategy using unchecked exceptions for truly exceptional and unexpected situations. However, many operations have expected failure modes that are part of normal business logic rather than exceptional circumstances — for example, a validation that rejects user input, or a lookup that finds no matching entity. When these expected outcomes are communicated via exceptions, the resulting code has hidden control flow paths: callers may forget to catch, catch too broadly, or let exceptions propagate to inappropriate layers. The compiler cannot enforce that a caller handles both the success and failure cases.

Java 25 provides sealed interfaces, records, and pattern matching with switch expressions — features that make it practical to represent success and failure as explicit types rather than relying on exception-based signalling for expected outcomes.

## Decision

We will introduce a `Result<T>` sealed interface in `com.embervault.domain` with two permitted subtypes: `Success<T>` and `Failure<T>`, both implemented as records.

- **`Result.Success<T>`** carries the successful value.
- **`Result.Failure<T>`** carries an error message and an optional `Throwable` cause.

Service methods and domain operations whose failure is an expected outcome will return `Result<T>` instead of throwing exceptions. Callers handle both cases explicitly using Java 25 switch expressions with pattern matching:

```java
Result<Note> result = noteService.findById(id);
switch (result) {
    case Result.Success<Note> s -> display(s.value());
    case Result.Failure<Note> f -> showError(f.message());
}
```

Because `Result` is a sealed interface, the compiler verifies that switch expressions are exhaustive — every possible outcome must be handled. There is no risk of an unhandled case slipping through.

### When to use Result vs. exceptions

| Situation | Mechanism |
|---|---|
| Expected failure (validation, not-found, business rule violation) | `Result.Failure` |
| Unexpected failure (programming error, infrastructure outage, corruption) | Exception (per ADR-0016) |

Exceptions (ADR-0016) remain the correct tool for truly exceptional situations: null pointer violations, I/O failures, concurrency bugs, and similar conditions that indicate something is fundamentally wrong. `Result` is reserved for outcomes that a well-behaved caller should anticipate and handle as part of normal control flow.

### Design choices

- **Sealed interface with records** — leverages Java 25 language features for concise, immutable types with compiler-enforced exhaustiveness.
- **Factory methods** — `Result.success(value)`, `Result.failure(message)`, and `Result.failure(message, cause)` provide a clean creation API.
- **Accessor methods** — `isSuccess()`, `isFailure()`, `getValue()`, and `getError()` support imperative-style checks when pattern matching is not convenient, though pattern matching is preferred.
- **Domain package** — `Result` resides in `com.embervault.domain` because it is a domain-level concept that represents the outcome of domain operations.

## Consequences

- Method signatures explicitly communicate that an operation can fail, making success and failure part of the type contract rather than hidden in documentation or exception declarations.
- The compiler enforces exhaustive handling via sealed type pattern matching, eliminating the risk of unhandled failure cases.
- No hidden control flow — unlike exceptions, `Result` values follow normal return paths and cannot skip intermediate stack frames.
- Code is easier to reason about: the return type tells the full story of what can happen.
- Developers must decide whether a failure mode is expected (use `Result`) or exceptional (use exceptions per ADR-0016), which adds a small amount of design overhead.
- Existing exception-based code (ADR-0016) remains valid and will be migrated to `Result`-based APIs incrementally as appropriate.
