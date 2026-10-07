package com.thanh.fastfood.product.repository;

import com.thanh.fastfood.category.entity.Category;
import com.thanh.fastfood.category.repository.CategoryRepository;
import com.thanh.fastfood.product.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @Transactional
    void shouldSaveAndFindProductWithCategory() {

        Category category = Category.builder()
                .name("Pizza Test")
                .description("Các loại pizza")
                .build();

        Category savedCategory = categoryRepository.save(category);

        Product product = Product.builder()
                .category(savedCategory)
                .name("Pizza Hải Sản")
                .description("Pizza hải sản đặc biệt")
                .price(new BigDecimal("89000.00"))
                .status("AVAILABLE")
                .build();

        Product savedProduct = productRepository.save(product);

        assertThat(savedProduct.getId()).isNotNull();

        Product foundProduct = productRepository
                .findById(savedProduct.getId())
                .orElseThrow();

        assertThat(foundProduct.getName())
                .isEqualTo("Pizza Hải Sản");

        assertThat(foundProduct.getPrice())
                .isEqualByComparingTo("89000.00");

        assertThat(foundProduct.getStatus())
                .isEqualTo("AVAILABLE");

        assertThat(foundProduct.getCategory().getId())
                .isEqualTo(savedCategory.getId());

        assertThat(foundProduct.getCategory().getName())
                .isEqualTo("Pizza Test");
    }
}