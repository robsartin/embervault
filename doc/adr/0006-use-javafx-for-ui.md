# 6. Use JavaFX for the User Interface

Date: 2026-03-27

## Status

Accepted

## Context

EmberVault is a desktop application inspired by Tinderbox that requires a rich, interactive user interface. We need a UI framework that supports Java, provides modern controls, and enables custom layouts and visualizations.

Options considered:
- **JavaFX** — Modern Java UI toolkit with CSS styling, FXML, and rich control library
- **Swing** — Legacy Java UI toolkit, still maintained but showing its age
- **Web-based (Electron/Tauri)** — Would require leaving the Java ecosystem

## Decision

We will use JavaFX as the UI framework for EmberVault.

## Consequences

- **Positive:** Native Java integration, modern controls, CSS-based styling, FXML for declarative layouts, active community and ongoing development
- **Positive:** Scene graph architecture suits the spatial/visual nature of a Tinderbox-inspired application
- **Negative:** JavaFX is not bundled with the JDK since Java 11; must be managed as a separate dependency
- **Negative:** Platform-specific native libraries required for distribution
