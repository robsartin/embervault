# 14. Use TestFX for UI Testing

Date: 2026-03-27

## Status

Accepted

## Context

The project uses JavaFX for its user interface (ADR-0006) and JUnit 5 as the testing framework (ADR-0003). Unit tests cover domain logic and architecture rules, but there is no mechanism to test JavaFX UI components in an automated fashion. Manual UI testing is slow, error-prone, and does not scale as the application grows.

TestFX is the de facto standard library for automated JavaFX UI testing. It integrates with JUnit 5, can drive a live JavaFX stage, simulate user interactions (clicks, typing, drag-and-drop), and assert on the state of UI nodes.

Headless testing via Monocle is desirable for CI environments but is not available for Java 25 at this time. Until a compatible headless toolkit is released, UI tests require a display (or software rendering via `-Dprism.order=sw`).

## Decision

We will use TestFX (`org.testfx:testfx-core` and `org.testfx:testfx-junit5`) for automated JavaFX UI testing.

- UI tests live under `src/test/java/com/embervault/ui/` and use the `@ExtendWith(ApplicationExtension.class)` JUnit 5 integration.
- All UI tests are tagged with `@Tag("ui")` so they can be excluded in headless CI environments using the `skip-ui-tests` Maven profile (`mvn test -Pskip-ui-tests`).
- Monocle (`org.testfx:openjfx-monocle`) is not included because no release is compatible with Java 25. This decision should be revisited when a compatible Monocle version becomes available.
- The Surefire plugin is configured with `--add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED` to allow TestFX reflective access to internal JavaFX APIs.

## Consequences

- Developers can write automated tests that launch real JavaFX stages, interact with UI controls, and assert on observable state.
- UI regressions are caught earlier in the development cycle rather than relying solely on manual testing.
- UI tests require a graphical display (or software rendering) to run, so CI pipelines that lack a display must either use Xvfb/virtual framebuffer or activate the `skip-ui-tests` profile.
- When Monocle adds Java 25 support, it can be added as a test dependency to enable true headless UI testing without a display server.
