package com.mini3.backend.global.security.jwt;

import com.mini3.backend.global.security.custom.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService
            userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 로그인 요청은 JWT 검사 안 함
        String path = request.getRequestURI();

        if (path.equals("/api/auth/login")) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String bearer =
                request.getHeader(
                        "Authorization"
                );

        if (bearer != null &&
                bearer.startsWith("Bearer ")) {

            String token =
                    bearer.substring(7);

            // JWT 검증
            if (jwtProvider.validateToken(token)) {

                Claims claims =
                        jwtProvider.getClaims(token);

                String empNo =
                        claims.getSubject();

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(
                                        empNo
                                );

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(auth);

            } else {

                System.out.println(
                        "JWT 검증 실패"
                );
            }
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}