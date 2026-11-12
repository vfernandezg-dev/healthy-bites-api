package com.healthybites.shared.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TextSearchTests {

    /** Lo que escribe la persona se busca literalmente: %, _ y la barra invertida no actúan como comodines. */
    @Test
    void theTextIsTrimmedLowercasedAndItsWildcardsEscaped() {
        assertThat(TextSearch.containsPattern("  50%_Off\\ ")).isEqualTo("%50\\%\\_off\\\\%");
    }
}
