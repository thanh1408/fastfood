package com.thanh.fastfood.category.controller;

import com.thanh.fastfood.category.dto.CategoryCreateRequest;
import com.thanh.fastfood.category.dto.CategoryResponse;
import com.thanh.fastfood.category.dto.CategoryUpdateRequest;
import com.thanh.fastfood.category.service.CategoryService;
import com.thanh.fastfood.common.exception.ResourceNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoryControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void getAll_shouldReturn200() throws Exception {
        when(categoryService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk());

        verify(categoryService).getAll();
    }

    @Test
    void create_shouldReturn403_whenCustomer() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .with(user("customer").roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Burger",
                                "description": "Danh mục burger"
                            }
                            """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(categoryService);
    }

    @Test
    void create_shouldReturn400_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "   ",
                                "description": "Danh mục thử nghiệm"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void create_shouldReturn201_whenAdmin() throws Exception {
        CategoryResponse response = new CategoryResponse();
        response.setId(1L);
        response.setName("Burger");
        response.setDescription("Danh mục burger");

        when(categoryService.create(any(CategoryCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/categories")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Burger",
                                "description": "Danh mục burger"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Burger"));

        verify(categoryService).create(any(CategoryCreateRequest.class));
    }

    @Test
    void getById_shouldReturn200_whenCategoryExists() throws Exception {
        CategoryResponse response = new CategoryResponse();
        response.setId(1L);
        response.setName("Burger");
        response.setDescription("Danh mục burger");

        when(categoryService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Burger"));

        verify(categoryService).getById(1L);
    }

    @Test
    void getById_shouldReturn404_whenCategoryNotFound() throws Exception {
        when(categoryService.getById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Không tìm thấy danh mục với id: 999"));

        mockMvc.perform(get("/api/v1/categories/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy danh mục với id: 999"));

        verify(categoryService).getById(999L);
    }

    @Test
    void update_shouldReturn200_whenAdmin() throws Exception {
        CategoryResponse response = new CategoryResponse();
        response.setId(1L);
        response.setName("Burger Updated");
        response.setDescription("Danh mục burger đã cập nhật");

        when(categoryService.update(
                eq(1L),
                any(CategoryUpdateRequest.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/v1/categories/1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Burger Updated",
                                "description": "Danh mục burger đã cập nhật"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Burger Updated"));

        verify(categoryService).update(
                eq(1L),
                any(CategoryUpdateRequest.class)
        );
    }

    @Test
    void update_shouldReturn404_whenCategoryNotFound() throws Exception {
        when(categoryService.update(
                eq(999L),
                any(CategoryUpdateRequest.class)
        )).thenThrow(new ResourceNotFoundException(
                "Không tìm thấy danh mục với id: 999"
        ));

        mockMvc.perform(put("/api/v1/categories/999")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Burger Updated",
                                "description": "Danh mục đã cập nhật"
                            }
                            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy danh mục với id: 999"));

        verify(categoryService).update(
                eq(999L),
                any(CategoryUpdateRequest.class)
        );
    }

    @Test
    void delete_shouldReturn204_whenAdmin() throws Exception {
        doNothing().when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/v1/categories/1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());

        verify(categoryService).delete(1L);
    }
}