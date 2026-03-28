# EmberVault Architecture

EmberVault is a Tinderbox-inspired note-taking application built with JavaFX, following hexagonal architecture (ports and adapters) with MVVM for the UI layer.

## System Overview

```mermaid
flowchart TB
    subgraph Adapters["Adapters (outer ring)"]
        direction TB
        subgraph InAdapters["Inbound Adapters"]
            Views["FXML Views<br/>(Controllers)"]
            ViewModels["ViewModels"]
        end
        subgraph OutAdapters["Outbound Adapters"]
            InMemRepo["InMemoryNoteRepository"]
            InMemLink["InMemoryLinkRepository"]
            InMemStamp["InMemoryStampRepository"]
        end
    end

    subgraph Application["Application (middle ring)"]
        subgraph PortsIn["Inbound Ports"]
            NoteService["NoteService"]
            LinkService["LinkService"]
            StampService["StampService"]
            ProjectService["ProjectService"]
        end
        subgraph Impl["Service Implementations"]
            NoteServiceImpl["NoteServiceImpl"]
            LinkServiceImpl["LinkServiceImpl"]
            StampServiceImpl["StampServiceImpl"]
            ProjectServiceImpl["ProjectServiceImpl"]
        end
        subgraph PortsOut["Outbound Ports"]
            NoteRepo["NoteRepository"]
            LinkRepo["LinkRepository"]
            StampRepo["StampRepository"]
        end
    end

    subgraph Domain["Domain (core)"]
        Note["Note"]
        AttrMap["AttributeMap"]
        AttrVal["AttributeValue"]
        Stamp["Stamp"]
        Link["Link"]
        Project["Project"]
        Result["Result"]
        Resolver["AttributeResolver"]
        Schema["AttributeSchemaRegistry"]
    end

    Views --> ViewModels
    ViewModels --> PortsIn
    Impl --> PortsOut
    OutAdapters -.->|implements| PortsOut
    Impl -.->|implements| PortsIn
    Application --> Domain
```

## Domain Model

```mermaid
classDiagram
    class Note {
        -UUID id
        -AttributeMap attributes
        -UUID prototypeId
        +create(title, content) Note
        +getAttribute(name) Optional~AttributeValue~
        +setAttribute(name, value)
        +getTitle() String
        +getContent() String
        +getPrototypeId() Optional~UUID~
    }

    class AttributeMap {
        -Map~String, AttributeValue~ values
        +get(name) Optional~AttributeValue~
        +set(name, value)
        +remove(name)
        +hasLocalValue(name) boolean
        +localEntries() Map
    }

    class AttributeValue {
        <<sealed interface>>
        +of(Object) AttributeValue
    }
    class StringValue { +String value }
    class NumberValue { +double value }
    class BooleanValue { +boolean value }
    class ColorValue { +TbxColor value }
    class DateValue { +Instant value }
    class ListValue { +List~String~ values }
    class SetValue { +Set~String~ values }
    class ActionValue { +String expression }

    class AttributeDefinition {
        <<record>>
        +String name
        +AttributeType type
        +AttributeValue defaultValue
        +boolean readOnly
        +boolean intrinsic
        +boolean system
    }

    class AttributeSchemaRegistry {
        +register(AttributeDefinition)
        +get(name) Optional~AttributeDefinition~
    }

    class AttributeResolver {
        +resolve(Note, name, noteLookup) AttributeValue
    }

    class Stamp {
        <<record>>
        +UUID id
        +String name
        +String action
        +create(name, action) Stamp
    }

    class Link {
        <<record>>
        +UUID id
        +UUID sourceId
        +UUID destinationId
        +String type
        +create(source, dest) Link
    }

    class Project {
        +UUID id
        +String name
        +Note rootNote
        +createEmpty() Project
    }

    class Result~T~ {
        <<sealed interface>>
        +success(value) Result
        +failure(message) Result
    }
    class Success~T~ { +T value }
    class Failure~T~ { +String message }

    Note *-- AttributeMap : contains
    AttributeMap o-- AttributeValue : stores
    AttributeValue <|-- StringValue
    AttributeValue <|-- NumberValue
    AttributeValue <|-- BooleanValue
    AttributeValue <|-- ColorValue
    AttributeValue <|-- DateValue
    AttributeValue <|-- ListValue
    AttributeValue <|-- SetValue
    AttributeValue <|-- ActionValue
    Result <|-- Success
    Result <|-- Failure
    AttributeResolver --> AttributeSchemaRegistry : uses
    AttributeResolver --> Note : resolves for
    AttributeSchemaRegistry o-- AttributeDefinition : registers
    Project *-- Note : rootNote
    Note ..> Note : prototypeId
    Link ..> Note : sourceId / destinationId
```

## MVVM Architecture

Each view follows the Model-View-ViewModel pattern. Views are FXML controllers that delegate all logic to ViewModels, which interact with application services through inbound ports.

```mermaid
flowchart LR
    subgraph View["View Layer (FXML + Controller)"]
        MVC["MapViewController"]
        OVC["OutlineViewController"]
        TVC["TreemapViewController"]
        HVC["HyperbolicViewController"]
        ABC["AttributeBrowserViewController"]
    end

    subgraph ViewModel["ViewModel Layer"]
        MVM["MapViewModel"]
        OVM["OutlineViewModel"]
        TVM["TreemapViewModel"]
        HVM["HyperbolicViewModel"]
        ABV["AttributeBrowserViewModel"]
    end

    subgraph Service["Application Services"]
        NS["NoteService"]
        LS["LinkService"]
        SS["StampService"]
    end

    subgraph Repository["Outbound Ports"]
        NR["NoteRepository"]
        LR["LinkRepository"]
        SR["StampRepository"]
    end

    MVC --> MVM
    OVC --> OVM
    TVC --> TVM
    HVC --> HVM
    ABC --> ABV

    MVM --> NS
    OVM --> NS
    TVM --> NS
    HVM --> NS
    HVM --> LS
    ABV --> NS

    NS --> NR
    LS --> LR
    SS --> SR
```

## View Types and Switching

Five view types are available. Each view pane can switch between types via a right-click context menu on its tab title label. `ViewPaneContext` manages the lifecycle of each pane.

```mermaid
flowchart TB
    VPC["ViewPaneContext"]
    VT["ViewType (enum)"]
    VPD["ViewPaneDeps"]

    VPC -->|current type| VT
    VPC -->|shared deps| VPD

    VT --- MAP["MAP<br/>Spatial canvas<br/>$Xpos / $Ypos"]
    VT --- OUTLINE["OUTLINE<br/>Hierarchical list<br/>$OutlineOrder"]
    VT --- TREEMAP["TREEMAP<br/>Area-proportional<br/>TreemapLayout"]
    VT --- HYPERBOLIC["HYPERBOLIC<br/>Link graph<br/>HyperbolicLayout"]
    VT --- BROWSER["BROWSER<br/>Group by attribute<br/>Categories"]

    MAP ---|FXML| MapView["MapView.fxml"]
    OUTLINE ---|FXML| OutlineView["OutlineView.fxml"]
    TREEMAP ---|FXML| TreemapView["TreemapView.fxml"]
    HYPERBOLIC ---|FXML| HyperbolicView["HyperbolicView.fxml"]
    BROWSER ---|FXML| BrowserView["AttributeBrowserView.fxml"]

    subgraph Supporting Views
        TP["TextPaneViewController<br/>Selected note $Text editor"]
        SE["SearchViewController<br/>Cmd+F incremental search"]
        NE["NoteEditorViewController<br/>Attribute editor"]
        STE["StampEditorViewController<br/>Stamp management"]
    end

    TP --- SNV["SelectedNoteViewModel"]
    SE --- SVM["SearchViewModel"]
```

## Data Flow

When a note is created or modified, all views refresh from the single authoritative repository. No view caches `Note` objects across refreshes.

```mermaid
sequenceDiagram
    actor User
    participant View as ViewController
    participant VM as ViewModel
    participant Svc as NoteService
    participant Repo as NoteRepository

    User->>View: create note action
    View->>VM: createChildNote("Untitled")
    VM->>Svc: createChildNote(parentId, title)
    Svc->>Repo: save(note)
    Repo-->>Svc: saved note
    Svc-->>VM: new Note
    VM->>VM: notifyDataChanged()
    VM->>VM: refreshAll lambda

    par Refresh all views
        VM->>VM: mapPane.refreshCurrentView()
        VM->>VM: outlinePane.refreshCurrentView()
        VM->>VM: treemapPane.refreshCurrentView()
        VM->>VM: browserViewModel.groupNotes()
        VM->>VM: hyperbolicViewModel.setFocusNote()
        VM->>VM: selectedNoteVm.refresh()
        VM->>VM: searchViewModel.refreshResults()
    end

    VM-->>View: UI bindings update
    View-->>User: all views reflect new state
```

## Attribute Inheritance

`AttributeResolver` resolves attribute values through a prototype chain. Intrinsic attributes skip the chain and go directly to the document default.

```mermaid
flowchart TB
    Start["resolve(note, attributeName)"] --> Local{"Note has<br/>local value?"}
    Local -->|Yes| ReturnLocal["Return local value"]
    Local -->|No| CheckDef{"AttributeDefinition<br/>found?"}
    CheckDef -->|No| ReturnEmpty["Return empty string default"]
    CheckDef -->|Yes| Intrinsic{"Intrinsic<br/>attribute?"}
    Intrinsic -->|Yes| ReturnDefault["Return definition default"]
    Intrinsic -->|No| Proto{"Note has<br/>prototype?"}
    Proto -->|Yes| ProtoLocal{"Prototype has<br/>local value?"}
    ProtoLocal -->|Yes| ReturnProto["Return prototype value"]
    ProtoLocal -->|No| ProtoChain{"Prototype has<br/>its own prototype?"}
    ProtoChain -->|Yes| Proto
    ProtoChain -->|No| ReturnDefault
    Proto -->|No| ReturnDefault

    style ReturnLocal fill:#4a9,color:#fff
    style ReturnProto fill:#4a9,color:#fff
    style ReturnDefault fill:#69c,color:#fff
    style ReturnEmpty fill:#999,color:#fff
```

Example chain: a note's `$Color` is resolved as Note local value, then Prototype's value, then Prototype's Prototype's value, then the `AttributeDefinition` default.

## Package Structure

```mermaid
graph TD
    Root["com.embervault"]

    Root --> Domain["domain"]
    Root --> App["application"]
    Root --> Adapter["adapter"]

    Domain --> DomEntities["Note, Project<br/>Link, Stamp"]
    Domain --> DomValues["AttributeValue (sealed)<br/>AttributeMap<br/>AttributeDefinition"]
    Domain --> DomServices["AttributeResolver<br/>AttributeSchemaRegistry"]
    Domain --> DomTypes["Result, TbxColor<br/>AttributeType, StampAction"]

    App --> PortIn["port.in<br/>NoteService, LinkService<br/>StampService, ProjectService"]
    App --> PortOut["port.out<br/>NoteRepository<br/>LinkRepository<br/>StampRepository"]
    App --> AppImpl["NoteServiceImpl<br/>LinkServiceImpl<br/>StampServiceImpl<br/>ProjectServiceImpl"]

    Adapter --> InUI["in.ui"]
    Adapter --> OutPersist["out.persistence"]

    InUI --> Views["view<br/>MapViewController<br/>OutlineViewController<br/>TreemapViewController<br/>HyperbolicViewController<br/>AttributeBrowserViewController<br/>+ TextPane, Search,<br/>NoteEditor, StampEditor"]
    InUI --> VMs["viewmodel<br/>MapViewModel<br/>OutlineViewModel<br/>TreemapViewModel<br/>HyperbolicViewModel<br/>AttributeBrowserViewModel<br/>+ SelectedNote, Search,<br/>NoteEditor, StampEditor"]
    InUI --> VMUtils["viewmodel (utilities)<br/>DataChangeSupport<br/>NoteDisplayHelper<br/>NavigationStack<br/>TextUtils"]

    OutPersist --> Repos["InMemoryNoteRepository<br/>InMemoryLinkRepository<br/>InMemoryStampRepository"]

    Root --> TopLevel["App (wiring)<br/>ViewPaneContext<br/>ViewPaneDeps<br/>ViewType (enum)"]
```

Dependency flow is strictly inward: `adapter` depends on `application` ports, `application` depends on `domain`. ArchUnit tests enforce these boundaries at build time.
