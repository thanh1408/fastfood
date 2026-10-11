
package com.thanh.fastfood.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductCreateRequest {

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 150, message = "Tên sản phẩm không được quá 150 ký tự")
    private String name;

    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(
            value = "0.00",
            inclusive = false,
            message = "Giá sản phẩm phải lớn hơn 0"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Giá sản phẩm tối đa 10 chữ số phần nguyên và 2 chữ số thập phân"
    )
    private BigDecimal price;
}
