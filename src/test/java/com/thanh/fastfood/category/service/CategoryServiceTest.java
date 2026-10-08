package com.thanh.fastfood.category.service;

import com.thanh.fastfood.category.dto.CategoryCreateRequest;
import com.thanh.fastfood.category.dto.CategoryResponse;
import com.thanh.fastfood.category.dto.CategoryUpdateRequest;
import com.thanh.fastfood.category.entity.Category;
import com.thanh.fastfood.category.repository.CategoryRepository;
import com.thanh.fastfood.common.exception.DuplicateResourceException;
import com.thanh.fastfood.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldCreateCategory() {

        String name = "Test Burger Create";

        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName(name);
        request.setDescription("Các loại burger");

        CategoryResponse response =
                categoryService.create(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo(name);
        assertThat(response.getDescription())
                .isEqualTo("Các loại burger");
    }

    @Test
    void shouldThrowExceptionWhenCreateDuplicateCategory() {

        String name = "Test Pizza Duplicate";

        categoryRepository.save(
                Category.builder()
                        .name(name)
                        .description("Các loại pizza")
                        .build()
        );

        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName(name);
        request.setDescription("Pizza khác");

        assertThatThrownBy(() ->
                categoryService.create(request)
        )
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Danh mục đã tồn tại: " + name);
    }

    @Test
    void shouldGetCategoryById() {

        Category category = Category.builder()
                .name("Test Chicken Get")
                .description("Các món gà")
                .build();

        Category savedCategory =
                categoryRepository.save(category);

        CategoryResponse response =
                categoryService.getById(savedCategory.getId());

        assertThat(response.getId())
                .isEqualTo(savedCategory.getId());

        assertThat(response.getName())
                .isEqualTo("Test Chicken Get");

        assertThat(response.getDescription())
                .isEqualTo("Các món gà");
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {

        Long nonExistentId = 999999L;

        assertThatThrownBy(() ->
                categoryService.getById(nonExistentId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        "Không tìm thấy danh mục với id: "
                                + nonExistentId
                );
    }

    @Test
    void shouldGetAllCategories() {

        categoryRepository.save(
                Category.builder()
                        .name("Test Burger List")
                        .description("Burger")
                        .build()
        );

        categoryRepository.save(
                Category.builder()
                        .name("Test Pizza List")
                        .description("Pizza")
                        .build()
        );

        List<CategoryResponse> responses =
                categoryService.getAll();

        assertThat(responses)
                .extracting(CategoryResponse::getName)
                .contains(
                        "Test Burger List",
                        "Test Pizza List"
                );
    }

    @Test
    void shouldUpdateCategory() {

        Category category = Category.builder()
                .name("Test Burger Old")
                .description("Burger cũ")
                .build();

        Category savedCategory =
                categoryRepository.save(category);

        CategoryUpdateRequest request =
                new CategoryUpdateRequest();

        request.setName("Test Burger Updated");
        request.setDescription("Burger cao cấp");

        CategoryResponse response =
                categoryService.update(
                        savedCategory.getId(),
                        request
                );

        assertThat(response.getId())
                .isEqualTo(savedCategory.getId());

        assertThat(response.getName())
                .isEqualTo("Test Burger Updated");

        assertThat(response.getDescription())
                .isEqualTo("Burger cao cấp");
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithDuplicateName() {

        String existingName = "Test Existing Category";

        categoryRepository.save(
                Category.builder()
                        .name(existingName)
                        .description("Existing")
                        .build()
        );

        Category target = categoryRepository.save(
                Category.builder()
                        .name("Test Update Target")
                        .description("Target")
                        .build()
        );

        CategoryUpdateRequest request =
                new CategoryUpdateRequest();

        request.setName(existingName);
        request.setDescription("Đổi tên");

        assertThatThrownBy(() ->
                categoryService.update(
                        target.getId(),
                        request
                )
        )
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(
                        "Danh mục đã tồn tại: " + existingName
                );
    }

    @Test
    void shouldDeleteCategory() {

        Category category = Category.builder()
                .name("Test Dessert Delete")
                .description("Món tráng miệng")
                .build();

        Category savedCategory =
                categoryRepository.save(category);

        categoryService.delete(savedCategory.getId());

        assertThat(
                categoryRepository.existsById(
                        savedCategory.getId()
                )
        ).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenDeleteNonExistentCategory() {

        Long nonExistentId = 999999L;

        assertThatThrownBy(() ->
                categoryService.delete(nonExistentId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        "Không tìm thấy danh mục với id: "
                                + nonExistentId
                );
    }
}