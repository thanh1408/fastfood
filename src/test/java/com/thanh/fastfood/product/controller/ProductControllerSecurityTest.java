package com.thanh.fastfood.product.controller;

import com.thanh.fastfood.common.exception.ResourceNotFoundException;
import com.thanh.fastfood.common.security.JwtAuthenticationFilter;
import com.thanh.fastfood.product.dto.ProductCreateRequest;
import com.thanh.fastfood.product.dto.ProductUpdateRequest;
import com.thanh.fastfood.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.ArgumentMatchers.anyLong;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private static final String PRODUCT_JSON = """
            {
                "categoryId": 1,
                "name": "Test Burger",
                "description": "Test description",
                "price": 25000
            }
            """;

    @Test
    void createProduct_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCT_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    @Test
    void createProduct_shouldReturn403_whenCustomer() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .with(user("customer").roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCT_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(productService);
    }

    @Test
    void createProduct_shouldAllowAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCT_JSON))
                .andExpect(status().isCreated());

        verify(productService).create(any());
    }

    @Test
    void createProduct_shouldReturn400_whenNameIsBlank() throws Exception {
        String invalidJson = """
            {
                "categoryId": 1,
                "name": "   ",
                "description": "Test description",
                "price": 25000
            }
            """;

        mockMvc.perform(post("/api/v1/products")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void createProduct_shouldReturn400_whenPriceIsZero() throws Exception {
        String invalidJson = """
            {
                "categoryId": 1,
                "name": "Test Burger",
                "description": "Test description",
                "price": 0
            }
            """;

        mockMvc.perform(post("/api/v1/products")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }


    @Test
    void createProduct_shouldReturn404_whenCategoryNotFound() throws Exception {
        when(productService.create(any(ProductCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException(
                        "Không tìm thấy danh mục với id: 999"
                ));

        String json = """
            {
                "categoryId": 999,
                "name": "Test Burger",
                "description": "Test description",
                "price": 25000
            }
            """;

        mockMvc.perform(post("/api/v1/products")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy danh mục với id: 999"))
                .andExpect(jsonPath("$.path").value("/api/v1/products"));
    }

    @Test
    void getProduct_shouldReturn404_whenProductNotFound() throws Exception {
        when(productService.getById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Không tìm thấy sản phẩm với id: 999"
                ));

        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy sản phẩm với id: 999"))
                .andExpect(jsonPath("$.path").value("/api/v1/products/999"));
    }

    @Test
    void updateProduct_shouldReturn404_whenProductNotFound() throws Exception {
        when(productService.update(
                anyLong(),
                any(ProductUpdateRequest.class)
        )).thenThrow(new ResourceNotFoundException(
                "Không tìm thấy sản phẩm với id: 999"
        ));

        String json = """
            {
                "categoryId": 1,
                "name": "Updated Burger",
                "description": "Updated description",
                "price": 30000
            }
            """;

        mockMvc.perform(put("/api/v1/products/999")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/products/999"));
    }

    @Test
    void deleteProduct_shouldReturn404_whenProductNotFound() throws Exception {
        doThrow(new ResourceNotFoundException(
                "Không tìm thấy sản phẩm với id: 999"
        )).when(productService).delete(999L);

        mockMvc.perform(delete("/api/v1/products/999")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy sản phẩm với id: 999"));
    }


}
