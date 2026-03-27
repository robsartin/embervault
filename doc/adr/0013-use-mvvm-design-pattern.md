# 13. Use MVVM Design Pattern

Date: 2026-03-27

## Status

Accepted

## Context

EmberVault uses JavaFX for its UI (ADR-0006) within a hexagonal architecture (ADR-0009). JavaFX provides a rich binding and observable-property system that naturally supports the Model-View-ViewModel (MVVM) pattern. Without a clear separation between presentation logic and view layout, UI code tends to accumulate business and formatting logic inside FXML controllers, making the UI layer difficult to test and maintain. MVVM addresses this by extracting presentation logic into ViewModel classes that expose observable properties the View can bind to declaratively.

## Decision

We will use the Model-View-ViewModel (MVVM) pattern for the JavaFX UI layer. The three roles are defined as follows:

- **Model** — Domain objects from `com.embervault.domain`. These are already defined by the hexagonal architecture and remain unchanged. ViewModels interact with the domain exclusively through inbound ports (`com.embervault.application.port.in`), preserving the dependency direction mandated by ADR-0009.

- **View** — FXML layout files paired with minimal controller classes that live in `com.embervault.adapter.in.ui.view`. Views are responsible only for declaring the visual layout and binding UI controls to ViewModel properties. They contain no business logic and no direct references to domain or infrastructure packages.

- **ViewModel** — Presentation-logic classes that live in `com.embervault.adapter.in.ui.viewmodel`. Each ViewModel exposes JavaFX observable properties (e.g., `StringProperty`, `BooleanProperty`, `ObservableList`) that Views bind to. ViewModels orchestrate user interactions by calling inbound ports and transforming domain data into a form suitable for display. ViewModels must not reference `javafx.scene` classes (scene-graph nodes, controls, layouts); they depend only on `javafx.beans` and `javafx.collections` for their observable-property contracts.

### Relationship to Hexagonal Architecture

MVVM operates entirely within the inbound UI adapter layer of the hexagonal architecture. Both the `view` and `viewmodel` sub-packages are children of `com.embervault.adapter.in.ui`, meaning they sit on the outer ring of the hexagon. The dependency flow is:

```
View  -->  ViewModel  -->  Inbound Port  -->  Domain
```

The domain remains unaware of ViewModels or Views, and outbound adapters remain completely decoupled from the UI. This layering ensures that presentation logic can be unit-tested without starting a JavaFX application, while the domain stays free of UI concerns.

### Package Structure

```
com.embervault.adapter.in.ui.view       — FXML controllers and layout files
com.embervault.adapter.in.ui.viewmodel  — ViewModel classes with observable properties
```

## Consequences

- Presentation logic is testable without launching a JavaFX stage, since ViewModels use only observable properties and inbound ports.
- Views stay thin and declarative, reducing the surface area for UI bugs.
- ArchUnit rules enforce the boundary: ViewModels cannot reference `javafx.scene` classes, and Views cannot reach into domain or infrastructure packages directly.
- Developers must decide whether new UI logic belongs in the View or the ViewModel, which adds a small amount of design overhead.
- The pattern introduces additional classes (one ViewModel per logical screen), which increases file count but improves cohesion and testability.
