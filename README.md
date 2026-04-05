# EmberVault

A [Tinderbox](http://www.eastgate.com/Tinderbox/)-inspired note-taking application built with JavaFX.

Notes are containers of typed attributes organized in hierarchies and visualized through multiple interactive views. EmberVault brings Tinderbox's powerful concepts — prototypes, stamps, links, and spatial maps — to a modern Java platform.

## Features

- **Five view types** — Map (spatial canvas), Outline (hierarchical tree), Treemap (area-proportional), Hyperbolic (link graph), and Attribute Browser (group by attribute)
- **Type-safe attributes** — Notes use a flexible attribute map with 11 typed value kinds (String, Number, Boolean, Color, Date, List, Set, Action, and more)
- **Prototype inheritance** — Notes inherit attribute defaults through a prototype chain
- **Stamps** — Reusable named actions that set attributes on notes (e.g., `$Color=red`)
- **Links** — Directed connections between notes, visualized in the Hyperbolic view
- **Search** — Incremental search with attribute filters (`color:red`, `has:children`)
- **Export/Import** — Multi-format export (JSON, Markdown, OPML)

## Tech Stack

| Component | Version |
|-----------|---------|
| Java | 25 |
| JavaFX | 24 |
| Build | Maven Wrapper (`./mvnw`) |
| Testing | JUnit 5, TestFX |
| Architecture | Hexagonal (Ports & Adapters) + MVVM |

## Getting Started

### Prerequisites

- **JDK 25** — Set `JAVA_HOME` before running any command:

```bash
export JAVA_HOME=/Users/sartin/Library/Java/JavaVirtualMachines/openjdk-25.0.2/Contents/Home
```

> **Note:** Always use the literal path. Do **not** use `$(...)` command substitution.

### Run the Application

```bash
JAVA_HOME=/Users/sartin/Library/Java/JavaVirtualMachines/openjdk-25.0.2/Contents/Home ./mvnw javafx:run
```

### Build and Test

```bash
# Full verify (tests + checkstyle + coverage + ArchUnit):
JAVA_HOME=/Users/sartin/Library/Java/JavaVirtualMachines/openjdk-25.0.2/Contents/Home ./mvnw verify

# Include UI tests (requires a display or xvfb):
JAVA_HOME=/Users/sartin/Library/Java/JavaVirtualMachines/openjdk-25.0.2/Contents/Home ./mvnw verify -Pui-tests

# Compile only (fast feedback):
JAVA_HOME=/Users/sartin/Library/Java/JavaVirtualMachines/openjdk-25.0.2/Contents/Home ./mvnw compile -q
```

## Documentation

| Document | Description |
|----------|-------------|
| [Architecture](doc/ARCHITECTURE.md) | System overview, domain model, MVVM layers, data flow diagrams |
| [Developer Guide](doc/DEVELOPER_GUIDE.md) | Code-first walkthrough for building documents, notes, and views |
| [Contributing](CONTRIBUTING.md) | TDD workflow, PR checklist, and coding conventions |
| [ADRs](doc/adr/) | Architecture Decision Records for all major design choices |

## Architecture Decision Records

This project uses [Architecture Decision Records](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions) (ADRs) to document significant architectural choices. All ADRs are stored in [`doc/adr/`](doc/adr/).

To create a new ADR, install [adr-tools](https://github.com/npryce/adr-tools) and run:

```sh
adr new "Title of Decision"
```
