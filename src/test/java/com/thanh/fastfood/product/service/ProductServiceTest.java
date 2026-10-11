
package com.thanh.fastfood.product.service;

import com.thanh.fastfood.category.entity.Category;
import com.thanh.fastfood.category.repository.CategoryRepository;
import com.thanh.fastfood.common.exception.ResourceNotFoundException;
import com.thanh.fastfood.product.dto.ProductCreateRequest;
import com.thanh.fastfood.product.dto.ProductResponse;
import com.thanh.fastfood.product.dto.ProductUpdateRequest;
import com.thanh.fastfood.product.entity.Product;
import com.thanh.fastfood.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category createCategory(String name) {
        return categoryRepository.save(
                Category.builder()
                        .name(name)
                        .description("Danh mục dùng để test")
                        .build()
        );
    }

    @Test
    void shouldCreateProduct() {
        Category category = createCategory("Test Product Create Category");

        ProductCreateRequest request = new ProductCreateRequest();
        request.setCategoryId(category.getId());
        request.setName("Test Burger Create");
        request.setDescription("Burger dùng để test");
        request.setPrice(new BigDecimal("59000.00"));

        ProductResponse response = productService.create(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getCategoryId()).isEqualTo(category.getId());
        assertThat(response.getCategoryName()).isEqualTo(category.getName());
        assertThat(response.getName()).isEqualTo("Test Burger Create");
        assertThat(response.getPrice()).isEqualByComparingTo("59000.00");
        assertThat(response.getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void shouldGetProductById() {
        Category category = createCategory("Test Product Get Category");

        Product savedProduct = productRepository.save(
                Product.builder()
                        .category(category)
                        .name("Test Pizza Get")
                        .description("Pizza dùng để test")
                        .price(new BigDecimal("89000.00"))
                        .status("AVAILABLE")
                        .build()
        );

        ProductResponse response =
                productService.getById(savedProduct.getId());

        assertThat(response.getId()).isEqualTo(savedProduct.getId());
        assertThat(response.getName()).isEqualTo("Test Pizza Get");
        assertThat(response.getCategoryId()).isEqualTo(category.getId());
        assertThat(response.getCategoryName()).isEqualTo(category.getName());
        assertThat(response.getPrice()).isEqualByComparingTo("89000.00");
    }

    @Test
    void shouldGetAllProducts() {
        Category category = createCategory("Test Product List Category");

        productRepository.save(
                Product.builder()
                        .category(category)
                        .name("Test Burger List")
                        .description("Burger")
                        .price(new BigDecimal("49000.00"))
                        .status("AVAILABLE")
                        .build()
        );

        productRepository.save(
                Product.builder()
                        .category(category)
                        .name("Test Pizza List")
                        .description("Pizza")
                        .price(new BigDecimal("79000.00"))
                        .status("AVAILABLE")
                        .build()
        );

        List<ProductResponse> responses = productService.getAll();

        assertThat(responses)
                .extracting(ProductResponse::getName)
                .contains("Test Burger List", "Test Pizza List");
    }

    @Test
    void shouldUpdateProduct() {
        Category oldCategory = createCategory("Test Product Old Category");
        Category newCategory = createCategory("Test Product New Category");

        Product savedProduct = productRepository.save(
                Product.builder()
                        .category(oldCategory)
                        .name("Test Product Old Name")
                        .description("Mô tả cũ")
                        .price(new BigDecimal("50000.00"))
                        .status("AVAILABLE")
                        .build()
        );

        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setCategoryId(newCategory.getId());
        request.setName("Test Product Updated Name");
        request.setDescription("Mô tả mới");
        request.setPrice(new BigDecimal("65000.00"));

        ProductResponse response =
                productService.update(savedProduct.getId(), request);

        assertThat(response.getId()).isEqualTo(savedProduct.getId());
        assertThat(response.getCategoryId()).isEqualTo(newCategory.getId());
        assertThat(response.getName()).isEqualTo("Test Product Updated Name");
        assertThat(response.getDescription()).isEqualTo("Mô tả mới");
        assertThat(response.getPrice()).isEqualByComparingTo("65000.00");
    }

    @Test
    void shouldDeleteProduct() {
        Category category = createCategory("Test Product Delete Category");

        Product savedProduct = productRepository.save(
                Product.builder()
                        .category(category)
                        .name("Test Product Delete")
                        .description("Sản phẩm dùng để test xóa")
                        .price(new BigDecimal("39000.00"))
                        .status("AVAILABLE")
                        .build()
        );

        productService.delete(savedProduct.getId());

        assertThat(productRepository.existsById(savedProduct.getId()))
                .isFalse();
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        Long nonExistentId = 999999999L;

        assertThatThrownBy(() -> productService.getById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Không tìm thấy sản phẩm với id: " + nonExistentId);
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setCategoryId(999999999L);
        request.setName("Test Product Invalid Category");
        request.setDescription("Danh mục không tồn tại");
        request.setPrice(new BigDecimal("59000.00"));

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Không tìm thấy danh mục với id: 999999999");
    }
}
