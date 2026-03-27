package com.embervault.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Architecture fitness tests enforcing ADR compliance via ArchUnit.
 *
 * <p>These rules run as part of the normal test suite and fail the build
 * when architectural constraints are violated.</p>
 *
 * <p>Uses {@link ClassFileImporter#importPath(Path)} to scan the compiled
 * classes directory directly, which works reliably with Java modules (JPMS)
 * where package-based scanning cannot see into named modules.</p>
 */
class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        Path classesDir = Paths.get("target", "classes");
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPath(classesDir);
    }

    @Test
    @DisplayName("ADR-0005: SLF4J must be used for logging, not java.util.logging")
    void shouldUseSl4jForLogging() {
        noClasses()
                .should().dependOnClassesThat()
                .resideInAPackage("java.util.logging")
                .because("ADR-0005 mandates SLF4J for logging, not java.util.logging")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("All production classes reside under com.embervault")
    void classesShouldResideInEmbervaultPackage() {
        classes()
                .should().resideInAPackage("com.embervault..")
                .because("all production code must live under the com.embervault package hierarchy")
                .check(classes);
    }

    @Test
    @DisplayName("No field injection via javax.inject or com.google.inject")
    void noFieldInjection() {
        noClasses()
                .should().dependOnClassesThat()
                .haveNameMatching(".*javax\\.inject\\.Inject|.*com\\.google\\.inject\\.Inject")
                .because("field injection is discouraged; use constructor injection instead")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("ADR-0009: Domain must not depend on adapter packages")
    void domainShouldNotDependOnAdapters() {
        noClasses()
                .that().resideInAPackage("com.embervault.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.embervault.adapter..")
                .because("ADR-0009 mandates that domain logic is isolated from adapters "
                        + "(dependency flows inward only)")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("ADR-0009: Domain must not depend on application packages")
    void domainShouldNotDependOnApplication() {
        noClasses()
                .that().resideInAPackage("com.embervault.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.embervault.application..")
                .because("ADR-0009 mandates that domain logic does not depend on the "
                        + "application layer (dependency flows inward only)")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("ADR-0009: Domain must not depend on JavaFX")
    void domainShouldNotDependOnJavaFx() {
        noClasses()
                .that().resideInAPackage("com.embervault.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("javafx..")
                .because("ADR-0009 mandates that domain logic is free of UI framework dependencies")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("ADR-0009: Domain must not depend on Spring Framework")
    void domainShouldNotDependOnSpring() {
        noClasses()
                .that().resideInAPackage("com.embervault.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("org.springframework..")
                .because("ADR-0009 mandates that domain logic is free of infrastructure "
                        + "framework dependencies")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("ADR-0009: Adapter classes should not be accessed by domain")
    void adaptersShouldNotBeAccessedByDomain() {
        noClasses()
                .that().resideInAPackage("com.embervault.adapter..")
                .should().onlyBeAccessed().byClassesThat()
                .resideInAPackage("com.embervault.domain..")
                .because("ADR-0009 mandates that adapters are not accessed directly by domain code")
                .allowEmptyShould(true)
                .check(classes);
    }
}
