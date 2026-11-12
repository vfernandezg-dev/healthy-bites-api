package com.healthybites;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Estructura modular verificada: cada módulo solo usa la API de los demás (su paquete raíz y los paquetes marcados
 * con {@code @NamedInterface}), sin ciclos. También genera la documentación de los módulos (diagramas C4 y Mermaid)
 * en {@code target/spring-modulith-docs}.
 */
class ModularityTests {

    private final ApplicationModules modules = ApplicationModules.of(HealthyBitesApplication.class);

    @Test
    void modulesRespectTheirBoundaries() {
        modules.verify();
    }

    @Test
    void writesModuleDocumentation() {
        new Documenter(modules).writeDocumentation();
    }
}
