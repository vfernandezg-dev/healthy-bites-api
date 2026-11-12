package com.healthybites;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModuleDependency;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.DependencyType;
import org.springframework.modulith.core.NamedInterface;
import org.springframework.modulith.docs.Documenter;

/**
 * Estructura modular verificada: cada módulo solo usa la API de los demás (su paquete raíz y los paquetes marcados
 * con {@code @NamedInterface}), sin ciclos.
 *
 * <p>También documenta los módulos a partir del código: {@code docs/modules.md} (diagrama Mermaid y tabla, que GitHub
 * muestra directamente; CI falla si difiere del commiteado) y la documentación completa de Spring Modulith (PlantUML y
 * fichas de cada módulo) en {@code target/spring-modulith-docs}.
 */
class ModularityTests {

    static final Path OVERVIEW = Path.of("docs", "modules.md");

    private final ApplicationModules modules = ApplicationModules.of(HealthyBitesApplication.class);

    @Test
    void modulesRespectTheirBoundaries() {
        modules.verify();
    }

    @Test
    void writesModuleDocumentation() {
        new Documenter(modules).writeDocumentation();
    }

    @Test
    void writesTheModuleOverview() throws IOException {
        String overview = overview();

        assertThat(overview).contains("flowchart").contains("identity").contains("notifications");
        Files.createDirectories(OVERVIEW.getParent());
        Files.writeString(OVERVIEW, overview, StandardCharsets.UTF_8);
    }

    // ---------------------------------------------------------------- docs/modules.md

    private String overview() {
        List<ApplicationModule> sorted = modules.stream()
                .sorted(Comparator.comparing(module -> id(module)))
                .toList();
        StringBuilder md = new StringBuilder();
        md.append("<!-- Generado por ModularityTests a partir del código: no lo edites a mano. -->\n\n")
                .append("# Módulos\n\n")
                .append("Monolito modular verificado con Spring Modulith: cada módulo solo usa la API pública de")
                .append(" los demás (su paquete raíz y sus paquetes `events`) y no hay ciclos. Todos usan `shared`,")
                .append(" que se omite en el diagrama.\n\n")
                .append("- Flecha continua: el módulo de origen usa la API del de destino.\n")
                .append("- Flecha discontinua: el evento viaja del módulo que lo publica a los que lo escuchan")
                .append(" (persistido en `event_publication` y entregado tras el commit).\n\n")
                .append("```mermaid\nflowchart LR\n");
        for (ApplicationModule module : sorted) {
            if (!isShared(module)) {
                md.append("    ")
                        .append(id(module))
                        .append("[\"")
                        .append(module.getDisplayName())
                        .append("\"]\n");
            }
        }
        for (ApplicationModule module : sorted) {
            for (String target : apiDependencies(module)) {
                if (!target.equals("shared")) {
                    md.append("    ")
                            .append(id(module))
                            .append(" --> ")
                            .append(target)
                            .append('\n');
                }
            }
        }
        for (Map.Entry<String, TreeSet<String>> edge : eventEdges().entrySet()) {
            String[] ends = edge.getKey().split(">");
            md.append("    ")
                    .append(ends[0])
                    .append(" -. \"")
                    .append(String.join(", ", edge.getValue()))
                    .append("\" .-> ")
                    .append(ends[1])
                    .append('\n');
        }
        md.append("```\n\n")
                .append("| Módulo | Paquete | Usa | Publica | Escucha |\n")
                .append("|---|---|---|---|---|\n");
        for (ApplicationModule module : sorted) {
            md.append("| ")
                    .append(module.getDisplayName())
                    .append(" | `")
                    .append(module.getBasePackage().getName())
                    .append("` | ")
                    .append(list(apiDependencies(module)))
                    .append(" | ")
                    .append(list(published(module).stream()
                            .map(JavaClass::getSimpleName)
                            .toList()))
                    .append(" | ")
                    .append(list(listenedTo(module).stream()
                            .map(JavaClass::getSimpleName)
                            .toList()))
                    .append(" |\n");
        }
        return md.toString();
    }

    private static String id(ApplicationModule module) {
        return module.getIdentifier().toString();
    }

    private static boolean isShared(ApplicationModule module) {
        return id(module).equals("shared");
    }

    /** Módulos cuya API usa (sin contar los eventos que escucha). */
    private List<String> apiDependencies(ApplicationModule module) {
        return module.getDirectDependencies(modules).stream()
                .filter(dependency -> dependency.getDependencyType() != DependencyType.EVENT_LISTENER)
                .map(ApplicationModuleDependency::getTargetModule)
                .filter(target -> !target.equals(module))
                .map(ModularityTests::id)
                .filter(target -> !isEventsOnly(module, target))
                .collect(Collectors.toCollection(TreeSet::new))
                .stream()
                .toList();
    }

    /** ¿Solo usa los eventos de {@code target} (es decir, lo escucha y nada más)? */
    private boolean isEventsOnly(ApplicationModule module, String target) {
        return module.getDirectDependencies(modules).stream()
                .filter(dependency -> id(dependency.getTargetModule()).equals(target))
                .allMatch(dependency -> isEvent(dependency.getTargetType()));
    }

    private boolean isEvent(JavaClass type) {
        return modules.stream().anyMatch(module -> published(module).contains(type));
    }

    /** Los eventos son los tipos de los paquetes {@code events} de cada módulo. */
    private static List<JavaClass> published(ApplicationModule module) {
        return module.getNamedInterfaces().stream()
                .filter(named -> named.getName().equals("events"))
                .flatMap(NamedInterface::asJavaClasses)
                .filter(type -> !type.getSimpleName().equals("package-info"))
                .sorted(Comparator.comparing(JavaClass::getSimpleName))
                .toList();
    }

    private List<JavaClass> listenedTo(ApplicationModule module) {
        return module.getEventsListenedTo(modules).stream()
                .distinct()
                .sorted(Comparator.comparing(JavaClass::getSimpleName))
                .toList();
    }

    /** "publicador>oyente" → nombres de los eventos. */
    private Map<String, TreeSet<String>> eventEdges() {
        Map<String, TreeSet<String>> edges = new TreeMap<>();
        modules.stream()
                .forEach(listener -> listenedTo(listener)
                        .forEach(event -> modules.stream()
                                .filter(publisher -> published(publisher).contains(event))
                                .findFirst()
                                .ifPresent(publisher -> edges.computeIfAbsent(
                                                id(publisher) + ">" + id(listener), key -> new TreeSet<>())
                                        .add(event.getSimpleName()))));
        return edges;
    }

    private static String list(List<String> items) {
        return items.isEmpty() ? "—" : String.join(", ", items);
    }
}
