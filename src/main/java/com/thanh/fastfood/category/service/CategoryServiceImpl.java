package com.thanh.fastfood.category.service;

import com.thanh.fastfood.category.dto.CategoryCreateRequest;
import com.thanh.fastfood.category.dto.CategoryResponse;
import com.thanh.fastfood.category.dto.CategoryUpdateRequest;
import com.thanh.fastfood.category.entity.Category;
import com.thanh.fastfood.category.repository.CategoryRepository;
import com.thanh.fastfood.common.exception.DuplicateResourceException;
import com.thanh.fastfood.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse create(CategoryCreateRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    "Danh mục đã tồn tại: " + request.getName()
            );
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Category savedCategory = categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy danh mục với id: " + id
                        )
                );

        return toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với id: " + id)
                );
        if (categoryRepository.existsByNameAndIdNot(
                request.getName(),
                id
        )) {
            throw new DuplicateResourceException(
                    "Danh mục đã tồn tại: " + request.getName()
            );
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updatedCategory = categoryRepository.save(category);

        return toResponse(updatedCategory);

    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy danh mục với id: " + id
                        )
                );

        categoryRepository.delete(category);
    }


    // ham toresponse
    private CategoryResponse toResponse(Category category) {

        CategoryResponse response = new CategoryResponse();

        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setCreatedAt(category.getCreateAt());
        response.setUpdatedAt(category.getUpdateAt());

        return response;
    }
}
