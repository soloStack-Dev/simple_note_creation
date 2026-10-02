package com.example.demo.Model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CategoryTest {

    @Test
    void fromMatchesTheEnumNameIgnoringCase() {
        assertThat(Category.from("memories")).isEqualTo(Category.MEMORIES);
        assertThat(Category.from("DAILY_ROUTINE")).isEqualTo(Category.DAILY_ROUTINE);
        assertThat(Category.from("  Something_Else  ")).isEqualTo(Category.SOMETHING_ELSE);
    }

    @Test
    void fromFallsBackInsteadOfThrowingOnUnknownInput() {
        // A stale or hand-crafted category must not be able to crash the request.
        assertThat(Category.from("recipes")).isEqualTo(Category.SOMETHING_ELSE);
        assertThat(Category.from(null)).isEqualTo(Category.SOMETHING_ELSE);
        assertThat(Category.from("")).isEqualTo(Category.SOMETHING_ELSE);
    }

    @Test
    void everyCategoryHasALabel() {
        for (Category category : Category.values()) {
            assertThat(category.getLabel()).isNotBlank();
        }
    }
}
