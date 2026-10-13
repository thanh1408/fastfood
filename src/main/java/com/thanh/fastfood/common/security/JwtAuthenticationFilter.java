package com.thanh.fastfood.common.security;

import com.thanh.fastfood.auth.entity.User;
import com.thanh.fastfood.auth.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        // Không có Bearer token: chuyển tiếp request
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            // 1. Xác thực chữ ký và lấy email từ JWT
            String email = jwtService.extractEmail(token);

            // 2. Chỉ xác thực nếu request chưa có Authentication
            if (email != null
                    && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                User user = userRepository.findByEmail(email)
                        .orElse(null);

                if (user != null) {

                    // Lấy role mới nhất từ database
                    String roleName = user.getRole()
                            .getName()
                            .name();

                    // Tạo principal và gán quyền
                    UserDetails principal =
                            org.springframework.security.core.userdetails.User
                                    .withUsername(user.getEmail())
                                    .password("")
                                    .authorities(List.of(
                                            new SimpleGrantedAuthority(
                                                    "ROLE_" + roleName
                                            )
                                    ))
                                    .build();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    principal.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    // Đặt Authentication vào SecurityContext
                    SecurityContext context =
                            SecurityContextHolder.createEmptyContext();

                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                }
            }

        } catch (JwtException | IllegalArgumentException exception) {

            // Token không hợp lệ hoặc hết hạn
            SecurityContextHolder.clearContext();
        }

        // 3. Chuyển request sang filter tiếp theo
        filterChain.doFilter(request, response);
    }
}
