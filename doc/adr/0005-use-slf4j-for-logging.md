# 5. Use SLF4J for Logging

Date: 2026-03-27

## Status

Accepted

## Context

The application needs a logging framework to provide structured, configurable logging output. Java's built-in `java.util.logging` is limited in flexibility and configuration. The project needs a logging facade that decouples application code from the underlying logging implementation, allowing the backend to be swapped without code changes.

## Decision

We will use SLF4J (Simple Logging Facade for Java) as the logging API and Logback as the logging backend.

- `slf4j-api` provides the facade that application code programs against.
- `logback-classic` provides the native SLF4J implementation with rich configuration options.
- A `logback.xml` configuration for production use (INFO level, console appender).
- A `logback-test.xml` configuration for test use (DEBUG level, console appender).

## Consequences

- All application logging will use the SLF4J API (`LoggerFactory.getLogger()`), keeping code decoupled from the backend.
- Logback provides powerful configuration including log levels per package, multiple appenders, and pattern layouts without code changes.
- The logging backend can be replaced in the future (e.g., Log4j2) by swapping the dependency, with no changes to application code.
- Test output uses DEBUG level for more detailed diagnostic information during development and CI.
- Third-party libraries that use SLF4J will automatically route through the same logging configuration.
