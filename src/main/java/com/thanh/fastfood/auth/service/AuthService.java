package com.thanh.fastfood.auth.service;

import com.thanh.fastfood.auth.dto.RegisterRequest;
import com.thanh.fastfood.auth.dto.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);
}