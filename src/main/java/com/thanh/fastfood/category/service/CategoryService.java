package com.thanh.fastfood.category.service;

import com.thanh.fastfood.category.dto.CategoryCreateRequest;
import com.thanh.fastfood.category.dto.CategoryResponse;
import com.thanh.fastfood.category.dto.CategoryUpdateRequest;

import java.util.List;

public interface CategoryService {
    CategoryResponse create(CategoryCreateRequest request);
    CategoryResponse getById(Long id);
    List<CategoryResponse> getAll();
    CategoryResponse update(Long id , CategoryUpdateRequest request);
    void delete(Long id);
}
