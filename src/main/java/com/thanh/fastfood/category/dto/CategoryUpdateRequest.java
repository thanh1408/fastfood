package com.thanh.fastfood.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100 , message = "Tên danh mục không được quá 100 kí tự")
    private String name;

    private String description;
}
