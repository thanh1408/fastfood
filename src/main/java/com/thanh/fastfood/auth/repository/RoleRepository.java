package com.thanh.fastfood.auth.repository;

import com.thanh.fastfood.auth.entity.Role;
import com.thanh.fastfood.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(UserRole name);
}