package com.thanh.fastfood.common.config;

import com.thanh.fastfood.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // API dùng JWT nên không sử dụng CSRF token
                .csrf(csrf -> csrf.disable())

                // Không tạo session để lưu trạng thái đăng nhập
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Các API đăng ký, đăng nhập được phép truy cập công khai
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/error"
                        ).permitAll()

                        // Các API GET danh mục và sản phẩm được phép truy cập công khai
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/categories/**",
                                "/api/v1/products/**"
                        ).permitAll()

                        // Chỉ ADMIN được tạo, sửa, xóa danh mục
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/categories/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/categories/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/categories/**"
                        ).hasRole("ADMIN")

                        // Chỉ ADMIN được tạo, sửa, xóa sản phẩm
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/products/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/products/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/products/**"
                        ).hasRole("ADMIN")

                        // Chỉ ADMIN được cập nhật trạng thái đơn hàng
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/orders/*/status"
                        ).hasRole("ADMIN")

                        // Các API còn lại yêu cầu đăng nhập
                        .anyRequest().authenticated()
                )

                // Request chưa xác thực trả về 401
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(
                                        HttpStatus.UNAUTHORIZED
                                )
                        )
                )

                // Không dùng form login hoặc HTTP Basic
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // Đặt JWT filter trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }


    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter>
    jwtFilterRegistration(JwtAuthenticationFilter filter) {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);

        return registration;
    }
}
