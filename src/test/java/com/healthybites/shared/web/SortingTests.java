package com.healthybites.shared.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

class SortingTests {

    private static final Set<String> ALLOWED = Set.of("createdAt", "title");

    @Test
    void allowedFieldsPassThrough() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("title").descending());

        assertThat(Sorting.restrict(pageable, ALLOWED)).isSameAs(pageable);
    }

    @Test
    void anyOtherFieldIsA400WithTheAllowedOnes() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("passwordHash"));

        assertThatThrownBy(() -> Sorting.restrict(pageable, ALLOWED)).isInstanceOfSatisfying(ApiException.class, ex -> {
            assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getCode()).isEqualTo("invalid-sort");
            assertThat(ex.getProperties()).containsKey("allowed");
        });
    }
}
