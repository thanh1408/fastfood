
package com.thanh.fastfood.product.service;

import com.thanh.fastfood.category.entity.Category;
import com.thanh.fastfood.category.repository.CategoryRepository;
import com.thanh.fastfood.common.exception.ResourceNotFoundException;
import com.thanh.fastfood.product.dto.ProductCreateRequest;
import com.thanh.fastfood.product.dto.ProductResponse;
import com.thanh.fastfood.product.dto.ProductUpdateRequest;
import com.thanh.fastfood.product.entity.Product;
import com.thanh.fastfood.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        Category category = findCategoryById(request.getCategoryId());

        Product product = Product.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .status("AVAILABLE")
                .build();

        Product savedProduct = productRepository.save(product);

        return toResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = findProductById(id);

        return toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse update(
            Long id,
            ProductUpdateRequest request
    ) {
        Product product = findProductById(id);
        Category category = findCategoryById(request.getCategoryId());

        product.setCategory(category);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());

        Product updatedProduct = productRepository.save(product);

        return toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = findProductById(id);

        productRepository.delete(product);
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy sản phẩm với id: " + id
                        )
                );
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy danh mục với id: " + id
                        )
                );
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStatus(product.getStatus());
        response.setCreatedAt(product.getCreateAt());
        response.setUpdatedAt(product.getUpdateAt());

        return response;
    }
}
