# Contributing to EmberVault

This project follows a **pure Test-Driven Development (TDD)** workflow. Every code change -- whether a new feature, a bug fix, or a refactoring -- begins with a test. No exceptions.

## The TDD Cycle

### 1. Red -- Write a Failing Test

Write **one** test that describes the behavior you want. Run it and confirm it fails. The failure message should clearly indicate what is missing.

**Guidelines:**
- Test behavior, not implementation details.
- Name the test after what it verifies (e.g., `vault_rejects_duplicate_entry`, not `test1`).
- Keep each test focused on a single assertion or closely related set of assertions.
- Do not write production code yet.

**Example (JUnit 5 / JavaFX context):**

```java
@Test
void newVaultStartsEmpty() {
    Vault vault = new Vault();
    assertEquals(0, vault.size());
}
```

Run the test. It will fail because `Vault` does not exist yet. That failure is the signal to move on.

### 2. Green -- Make the Test Pass

Write the **minimum** production code needed to make the failing test pass. Resist the urge to add anything extra.

```java
public class Vault {
    public int size() {
        return 0;
    }
}
```

Run the test again. It passes. Stop here.

**Guidelines:**
- "Minimum" means it: if a hard-coded value satisfies the test, that is fine for now. The next test will force you to generalize.
- Do not add error handling, logging, or optimizations that no test requires.
- If you notice another behavior that should exist, write it down as a future test -- do not implement it now.

### 3. Commit -- Save the Passing State

Commit the new test **and** the production code together. Each commit should represent one small, provably correct step.

```
git add src/test/java/.../VaultTest.java src/main/java/.../Vault.java
git commit -m "Add Vault class that reports its size as 0"
```

**Guidelines:**
- Every commit on the branch must leave all tests green.
- Write a short, descriptive commit message explaining what behavior was added or fixed.
- Small, frequent commits make code review easier and let you revert cleanly if something goes wrong.

### 4. Refactor -- Improve the Design

With the tests green you have a safety net. Improve the code: rename variables, extract methods, remove duplication, simplify conditionals. Run the tests after every change to make sure they still pass.

**Guidelines:**
- Do not add new behavior during a refactor step. If you spot missing behavior, add a test for it in the next Red step.
- Refactoring applies to test code too -- keep tests readable and free of duplication.
- Commit the refactoring separately so the history shows intent clearly.

### 5. Repeat

Go back to step 1. Pick the next small piece of behavior, write a failing test, and continue the cycle until the issue is complete.

## Practical Tips

### Start With the Simplest Case

When tackling a new feature, begin with the most trivial scenario (e.g., an empty collection, a default value, a null input). Each subsequent test should push the implementation one small step further.

### Triangulate to Force Generalization

If a single test can be satisfied with a hard-coded return value, write a second test with a different expected value. Now you are forced to write real logic.

```java
@Test
void vaultSizeReflectsAddedEntries() {
    Vault vault = new Vault();
    vault.add(new Entry("secret-1"));
    assertEquals(1, vault.size());
}
```

### One Assertion Per Test (When Practical)

A test with a single assertion is easier to name, easier to read, and produces clearer failure messages. Multiple assertions are acceptable when they describe the same logical behavior.

### Test the Interface, Not the Wiring

Write tests against public behavior. Avoid reaching into private fields or mocking every collaborator. If a test is hard to write, that is often a sign the design needs rethinking -- let the tests guide you.

### Use Descriptive Test Names

Good test names act as documentation. Anyone reading the test class should understand what the system does without opening the production code.

| Weak name             | Strong name                         |
|-----------------------|-------------------------------------|
| `testAdd`             | `addingEntryIncreasesSize`          |
| `testError`           | `duplicateEntryThrowsException`     |
| `testUI`              | `entryListRefreshesAfterAdd`        |

### Keep the Feedback Loop Fast

- Run the relevant test file, not the entire suite, while iterating.
- Use your IDE's "rerun last test" shortcut liberally.
- A slow test suite discourages TDD; flag slow tests for review.

## Pull Request Checklist

Before opening a PR, verify that:

- [ ] All tests pass (`./gradlew test` or equivalent).
- [ ] Every new behavior has at least one corresponding test.
- [ ] Commit history follows the Red-Green-Commit-Refactor pattern.
- [ ] No production code exists without a test that exercises it.
- [ ] Test names clearly describe the behavior under test.

## Why TDD?

See [ADR-0001](doc/adr/0001-use-pure-tdd-as-development-process.md) for the full rationale behind adopting pure TDD as the development process for EmberVault.
