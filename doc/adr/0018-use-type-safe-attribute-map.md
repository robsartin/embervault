# 18. Use Type-Safe Attribute Map for Note Model

Date: 2026-03-27

## Status

Accepted

## Context

EmberVault models notes inspired by Tinderbox, where every note is fundamentally a bag of typed attributes rather than a fixed set of fields. Tinderbox defines 11 attribute types (String, Number, Boolean, Color, Date, Interval, File, URL, List, Set, Action), hundreds of built-in system attributes, and supports user-defined attributes at runtime. Attributes inherit through a prototype chain: note -> prototype -> prototype's prototype -> document default.

We needed to decide how to represent note data internally. Two alternatives were considered:

**Alternative A: Type-Safe Attribute Map** -- A sealed `AttributeValue` interface with record variants for each type, stored in a sparse `AttributeMap`. Attributes are resolved through a prototype inheritance chain.

**Alternative B: Fixed Field Entity** -- Keep the original `Note` class with hardcoded fields (title, content, createdAt, updatedAt) and add new fields as needed.

## Decision

We chose **Alternative A: Type-Safe Attribute Map**.

The core design consists of:

- **AttributeType** enum with 11 variants mapping to Java types
- **AttributeValue** sealed interface with 11 record variants enabling exhaustive pattern matching
- **TbxColor** value object supporting named, hex, RGB, and HSV color formats
- **AttributeDefinition** record defining schema (name, type, default, intrinsic/read-only/system flags)
- **AttributeMap** sparse map holding only locally-set values
- **AttributeSchemaRegistry** pre-populated with essential system attributes using the "$" naming convention
- **AttributeResolver** domain service implementing the inheritance chain (local -> prototype chain -> document default), with intrinsic attributes skipping the prototype chain

The `Note` entity was refactored to use `AttributeMap` internally while maintaining backward-compatible convenience methods (`getTitle()`, `getContent()`, `getCreatedAt()`, `getUpdatedAt()`).

The `AttributeResolver` accepts a `Function<UUID, Optional<Note>>` for prototype lookups rather than depending on `NoteRepository` directly, preserving the hexagonal architecture constraint that domain code must not depend on the application layer.

## Consequences

### Positive

- Compile-time type safety via Java sealed types and pattern matching in switch expressions
- Extensible: new attributes (system or user-defined) can be added without modifying the Note class
- Prototype inheritance enables reusable note templates, matching Tinderbox's powerful prototype system
- Sparse storage is memory-efficient since notes only store locally-overridden values
- Backward-compatible: existing code using `getTitle()`/`getContent()` continues to work

### Negative

- More indirection when accessing common attributes compared to direct field access
- The attribute resolution chain adds complexity compared to a simple field lookup
- Cast-based convenience methods (e.g., extracting a String from AttributeValue) are not statically type-checked at the accessor level
