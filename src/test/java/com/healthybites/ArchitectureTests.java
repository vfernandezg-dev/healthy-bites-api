package com.healthybites;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;
import org.springframework.web.bind.annotation.RestController;

/** Reglas de diseño que no se pueden romper sin que falle la build. */
class ArchitectureTests {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.healthybites");

    /** Las entidades cambian con métodos de dominio, no con setters (antes: @Data en todas). */
    @Test
    void entitiesHaveNoPublicSetters() {
        ArchRule rule = methods()
                .that()
                .areDeclaredInClassesThat()
                .areAnnotatedWith(Entity.class)
                .and()
                .haveNameStartingWith("set")
                .should()
                .notBePublic()
                .because("el estado de una entidad cambia con métodos de dominio que mantienen sus reglas");
        rule.allowEmptyShould(true).check(CLASSES);
    }

    /** Los controladores llaman a servicios; los repositorios son cosa de la capa de aplicación. */
    @Test
    void controllersDoNotUseRepositories() {
        noClasses()
                .that()
                .areAnnotatedWith(RestController.class)
                .should()
                .dependOnClassesThat()
                .areAssignableTo(Repository.class)
                .check(CLASSES);
    }

    /** Los 201 llevan una Location absoluta con el context path: se construye siempre con Locations.of. */
    @Test
    void controllersBuildLocationsWithTheHelper() {
        noClasses()
                .that()
                .areAnnotatedWith(RestController.class)
                .should()
                .callMethod(URI.class, "create", String.class)
                .orShould()
                .dependOnClassesThat()
                .haveFullyQualifiedName("org.springframework.web.servlet.support.ServletUriComponentsBuilder")
                .because("Locations.of incluye /api/v1 y el host; URI.create(\"/x\") apunta fuera de la API")
                .check(CLASSES);
    }

    /** Fechas en la API: Instant (con zona) o LocalDate (un día); nunca LocalDateTime ni Date, que pierden la zona. */
    @Test
    void webLayerUsesUnambiguousDates() {
        noClasses()
                .that()
                .resideInAPackage("..web..")
                .should()
                .dependOnClassesThat()
                .areAssignableTo(LocalDateTime.class)
                .orShould()
                .dependOnClassesThat()
                .areAssignableTo(Date.class)
                .check(CLASSES);
    }

    /** Inyección por constructor: dependencias explícitas, inmutables y fáciles de sustituir en tests. */
    @Test
    void noFieldInjection() {
        fields().should().notBeAnnotatedWith(Autowired.class).check(CLASSES);
    }

    /** Los DTO de la API son records inmutables. */
    @Test
    void webDtosAreRecords() {
        classes()
                .that()
                .resideInAPackage("..web..")
                .and(simpleNameEndingWith("Request").or(simpleNameEndingWith("Response")))
                .should()
                .beRecords()
                .allowEmptyShould(true)
                .check(CLASSES);
    }

    /** Ningún módulo depende de las clases internas de otro (además de lo que comprueba Spring Modulith). */
    @Test
    void sharedDoesNotDependOnFeatureModules() {
        noClasses()
                .that()
                .resideInAPackage("com.healthybites.shared..")
                .should()
                .dependOnClassesThat()
                .resideOutsideOfPackages(
                        "com.healthybites.shared..",
                        "java..",
                        "jakarta..",
                        "javax..",
                        "org..",
                        "io.swagger..",
                        "lombok..",
                        "net.javacrumbs.shedlock..",
                        "tools.jackson..",
                        "com.fasterxml.jackson..")
                .check(CLASSES);
    }
}
