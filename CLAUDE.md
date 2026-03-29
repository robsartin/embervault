# CLAUDE.md — EmberVault

EmberVault is a Tinderbox-inspired note-taking application built with JavaFX. Java 25, Maven.

## Build

```bash
# Logic tests only (no display needed):
JAVA_HOME=/Users/sartin/Library/Java/JavaVirtualMachines/openjdk-25.0.2/Contents/Home ./mvnw verify

# Full suite including UI tests (requires display or xvfb):
JAVA_HOME=/Users/sartin/Library/Java/JavaVirtualMachines/openjdk-25.0.2/Contents/Home ./mvnw verify -Pui-tests
```

## Pure TDD Workflow (mandatory for all code changes)

Every code change follows the Red-Green-Commit-Refactor cycle. No exceptions.

1. **Red** — Write one failing test that defines the desired behavior.
2. **Green** — Write the minimum code to make the test pass.
3. **Commit** — Commit the passing test + implementation together.
4. **Refactor** — Improve design and code quality while keeping tests green.
5. **Repeat** — Continue until the issue is complete.

Tests MUST be written BEFORE implementation code. Each red-green-refactor cycle should be its own commit.

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed TDD guidelines and examples.

## Architecture

Hexagonal architecture (ports & adapters) with MVVM for the UI layer.

### Key packages

- `com.embervault.domain` — entities, value objects, domain services
- `com.embervault.application.port.in` — inbound use case interfaces
- `com.embervault.application.port.out` — outbound port interfaces
- `com.embervault.application` — application service implementations
- `com.embervault.adapter.in.ui.view` — FXML controllers (Views)
- `com.embervault.adapter.in.ui.viewmodel` — ViewModels
- `com.embervault.adapter.out.persistence` — persistence adapters

Dependency flow is strictly inward: adapters -> ports -> domain.

### View types

- **Map** — spatial canvas where notes are positioned by `$Xpos`/`$Ypos`
- **Outline** — hierarchical list ordered by `$OutlineOrder`
- **Treemap** — area-proportional rectangles (uses `TreemapLayout`)
- **Hyperbolic** — graph of note-to-note `Link` connections (uses `HyperbolicLayout`)
- **Attribute Browser** — groups notes by attribute values into categories
- Views are switched via right-click context menu (tab switching)

### Note model

- Notes use a **type-safe attribute map** — not fixed fields (see ADR-0018)
- Use `Attributes` constants (e.g., `Attributes.NAME`, `Attributes.COLOR`) — never raw `"$Name"` strings
- Attribute values are typed via `AttributeValue` sealed hierarchy (`StringValue`, `ColorValue`, `NumberValue`, etc.)

### Stamps

- `Stamp` record: reusable named actions applied to notes (e.g., `"$Color=red"`, `"$Checked=true"`)
- Created via `Stamp.create(name, action)` with auto-generated UUID
- Managed through `StampEditorViewController` / `StampEditorViewModel`

### Links

- `Link` record: directed connection between two notes (source -> destination)
- Types: `"untitled"` (default), `"web"`, `"prototype"`, etc.
- Primary navigation primitive for the Hyperbolic view

### Text pane

- `TextPaneViewController` — editor below the view area for the selected note's `$Text`
- Bound to the currently selected note via `SelectedNoteViewModel`

### Search

- `Cmd+F` opens search; uses debounce for incremental filtering
- `SearchViewController` / `SearchViewModel`

### Shared ViewModel utilities

- `NoteDisplayHelper` — extracts color hex, badge symbol, and numeric attributes from notes
- `DataChangeSupport` — composable callback holder (`setOnDataChanged` / `notifyDataChanged`)
- `NavigationStack` — drill-down history with observable `canNavigateBack` property
- `TextUtils` — text truncation with ellipsis

## Key Design Decisions

- Notes use a type-safe attribute map (not fixed fields) — see ADR-0018
- Result type for expected outcomes, exceptions for unexpected — see ADR-0017
- SOLID and DRY principles enforced via code review
- ArchUnit tests enforce architecture boundaries

## Quality Gates

All enforced by `mvn verify`:

- JUnit 5 tests must pass
- Checkstyle (Google-based) must pass
- JaCoCo coverage thresholds must be met
- ArchUnit architecture rules must pass

## Test Conventions

- `@Tag("ui")` on all TestFX UI tests
- `mvn verify` excludes `@Tag("ui")` tests by default (no display needed)
- `mvn verify -Pui-tests` includes UI tests (requires display or xvfb)
- CI runs full suite with `-Pui-tests` under `xvfb` (X virtual framebuffer)

## PR Workflow

All work is done on issue branches and submitted as PRs to main (see ADR-0001):

- Branch from main: `issue-<number>/<short-description>` or `fix/<description>`
- PRs require passing CI (tests, checkstyle, JaCoCo, ArchUnit)
- Manual merge only — no auto-merge, no direct pushes to main
- Do not merge PRs automatically — leave them for manual review and merge
