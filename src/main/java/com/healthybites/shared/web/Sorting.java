package com.healthybites.shared.web;

import java.util.Set;
import java.util.TreeSet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Lista blanca de campos de ordenación. Sin ella, {@code ?sort=} acepta cualquier propiedad de la entidad (incluidas
 * las que no se exponen) y un nombre inválido acaba en un 500.
 */
public final class Sorting {

    private Sorting() {}

    /**
     * @return la misma paginación si solo ordena por campos permitidos
     * @throws ApiException 400 {@code invalid-sort} si pide ordenar por otro campo
     */
    public static Pageable restrict(Pageable pageable, Set<String> allowed) {
        for (Sort.Order order : pageable.getSort()) {
            if (!allowed.contains(order.getProperty())) {
                throw ApiException.badRequest("invalid-sort", "No se puede ordenar por '" + order.getProperty() + "'")
                        .with("allowed", new TreeSet<>(allowed));
            }
        }
        return pageable;
    }
}
