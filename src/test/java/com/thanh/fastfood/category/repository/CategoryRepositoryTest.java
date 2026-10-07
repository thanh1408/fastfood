package com.thanh.fastfood.category.repository;

import com.thanh.fastfood.category.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldSaveAndFindCategory() {
        Category category = Category.builder()
                .name("Burger")
                .description("Các loại burger")
                .build();

        Category savedCategory = categoryRepository.save(category);

        assertThat(savedCategory.getId()).isNotNull();

        Category foundCategory = categoryRepository
                .findById(savedCategory.getId())
                .orElseThrow();

        assertThat(foundCategory.getName()).isEqualTo("Burger");
        assertThat(foundCategory.getDescription())
                .isEqualTo("Các loại burger");
    }
}