
package com.thanh.fastfood.product.service;

import com.thanh.fastfood.product.dto.ProductCreateRequest;
import com.thanh.fastfood.product.dto.ProductResponse;
import com.thanh.fastfood.product.dto.ProductUpdateRequest;

import java.util.List;

public interface ProductService {

    ProductResponse create(ProductCreateRequest request);

    ProductResponse getById(Long id);

    List<ProductResponse> getAll();

    ProductResponse update(Long id, ProductUpdateRequest request);

    void delete(Long id);
}
