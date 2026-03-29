package com.embervault.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.embervault.domain.DomainException;
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

    @Test
    @DisplayName("ADR-0013: ViewModels must not reference javafx.scene classes")
    void viewModelsShouldNotReferenceJavaFxScene() {
        noClasses()
                .that().resideInAPackage("com.embervault.adapter.in.ui.viewmodel..")
                .should().dependOnClassesThat()
                .resideInAPackage("javafx.scene..")
                .because("ADR-0013 mandates that ViewModels use observable properties "
                        + "(javafx.beans/javafx.collections) but not scene-graph nodes")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("ADR-0013: Views must not access domain packages directly")
    void viewsShouldNotAccessDomainDirectly() {
        noClasses()
                .that().resideInAPackage("com.embervault.adapter.in.ui.view..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.embervault.domain..")
                .because("ADR-0013 mandates that Views interact with the domain only "
                        + "through ViewModels, not directly")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("ADR-0013: Views must not access infrastructure packages directly")
    void viewsShouldNotAccessInfrastructureDirectly() {
        noClasses()
                .that().resideInAPackage("com.embervault.adapter.in.ui.view..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.embervault.adapter.out..")
                .because("ADR-0013 mandates that Views do not reference infrastructure "
                        + "(outbound adapter) packages")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("ADR-0016: Domain exceptions must extend DomainException")
    void domainExceptionsShouldExtendDomainException() {
        classes()
                .that().resideInAPackage("com.embervault.domain..")
                .and().areAssignableTo(Exception.class)
                .should().beAssignableTo(DomainException.class)
                .because("ADR-0016 mandates that all domain exceptions extend DomainException")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("ADR-0010: Inbound port types must be interfaces")
    void inboundPortTypesMustBeInterfaces() {
        classes()
                .that().resideInAPackage("com.embervault.application.port.in..")
                .and().areTopLevelClasses()
                .should().beInterfaces()
                .because("ADR-0010 mandates that inbound ports (use case contracts) "
                        + "are defined as interfaces")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("ADR-0010: Outbound port types must be interfaces")
    void outboundPortTypesMustBeInterfaces() {
        classes()
                .that().resideInAPackage("com.embervault.application.port.out..")
                .and().areTopLevelClasses()
                .should().beInterfaces()
                .because("ADR-0010 mandates that outbound ports (repository contracts) "
                        + "are defined as interfaces")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("ADR-0009: Application ports must not depend on adapter packages")
    void applicationPortsMustNotDependOnAdapters() {
        noClasses()
                .that().resideInAPackage("com.embervault.application.port..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.embervault.adapter..")
                .because("ADR-0009 mandates that ports define contracts independent "
                        + "of adapter implementations (dependency flows inward only)")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("ADR-0009: Application services must not depend on adapter packages")
    void applicationServicesMustNotDependOnAdapters() {
        noClasses()
                .that().resideInAPackage("com.embervault.application..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.embervault.adapter..")
                .because("ADR-0009 mandates that application services depend on ports "
                        + "(abstractions), not on adapter implementations "
                        + "(dependency flows inward only)")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("ADR-0010: Service implementations must reside in application package")
    void serviceImplementationsMustResideInApplicationPackage() {
        classes()
                .that().haveSimpleNameEndingWith("ServiceImpl")
                .should().resideInAPackage("com.embervault.application..")
                .because("ADR-0010 mandates that service implementations live in the "
                        + "application layer, not in adapters or domain")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("ADR-0018: No raw $-prefixed attribute strings outside Attributes.java")
    void noRawAttributeStringsOutsideAttributes() throws IOException {
        Path srcDir = Paths.get("src", "main", "java", "com", "embervault");
        Path attributesFile = srcDir.resolve("domain").resolve("Attributes.java");

        // Pattern matches string literals containing "$" followed by a capital
        // letter (the Tinderbox attribute naming convention, e.g. "$Color").
        // Allows "$" in Javadoc comments and non-attribute contexts.
        Pattern rawAttrPattern = Pattern.compile("\"\\$[A-Z][A-Za-z]*\"");

        List<String> violations = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(srcDir)) {
            paths.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.equals(attributesFile))
                    .forEach(path -> {
                        try {
                            List<String> lines = Files.readAllLines(path);
                            for (int i = 0; i < lines.size(); i++) {
                                String line = lines.get(i).trim();
                                // Skip comments and Javadoc
                                if (line.startsWith("//") || line.startsWith("*")
                                        || line.startsWith("/*")) {
                                    continue;
                                }
                                Matcher m = rawAttrPattern.matcher(line);
                                if (m.find()) {
                                    violations.add(path.getFileName()
                                            + ":" + (i + 1) + " -> " + line);
                                }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        if (!violations.isEmpty()) {
            fail("ADR-0018: Raw $-prefixed attribute strings found outside "
                    + "Attributes.java. Use Attributes.* constants instead:\n"
                    + String.join("\n", violations));
        }
    }
}
