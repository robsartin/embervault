package com.embervault.architecture;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

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
}
