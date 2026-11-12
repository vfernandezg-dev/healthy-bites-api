package com.healthybites.shared.persistence;

import jakarta.persistence.criteria.Predicate;
import java.util.Arrays;
import java.util.Locale;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Búsqueda "contiene" sin distinguir mayúsculas, la misma en todos los listados: recorta el texto, lo pasa a
 * minúsculas y escapa los comodines de LIKE ({@code %}, {@code _} y el propio carácter de escape). Las columnas que
 * se buscan tienen índices de trigramas sobre {@code lower(columna)} (migración V15).
 */
public final class TextSearch {

    /** Carácter de escape de los patrones ({@code like ... escape '\'}). */
    public static final char ESCAPE = '\\';

    private TextSearch() {}

    /** Patrón LIKE {@code %texto%} en minúsculas, con los comodines escapados con {@link #ESCAPE}. */
    public static String containsPattern(String text) {
        String escaped = text.trim()
                .toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    /** Alguno de los atributos de texto contiene {@code text}; sin texto, no filtra nada. */
    public static <T> Specification<T> anyContains(@Nullable String text, String... attributes) {
        if (text == null || text.isBlank()) {
            return Specification.unrestricted();
        }
        String pattern = containsPattern(text);
        return (root, query, cb) -> cb.or(Arrays.stream(attributes)
                .map(attribute -> cb.like(cb.lower(root.get(attribute)), pattern, ESCAPE))
                .toArray(Predicate[]::new));
    }
}
