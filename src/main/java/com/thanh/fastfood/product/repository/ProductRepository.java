package com.thanh.fastfood.product.repository;

import com.thanh.fastfood.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
