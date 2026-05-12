package com.mini3.backend.domain.auth.service;

import com.mini3.backend.domain.auth.dto.*;
import com.mini3.backend.global.security.custom.CustomUserDetails;
import com.mini3.backend.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public LoginResponseDto login(LoginRequestDto requestDto) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                requestDto.getEmpNo(),
                                requestDto.getPassword()
                        )
                );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        String token = jwtProvider.createToken(
                userDetails.getEmployee().getEmpNo(),
                userDetails.getRole().name()
        );

        return LoginResponseDto.builder()
                .accessToken(token)
                .empNo(userDetails.getEmployee().getEmpNo())
                .empName(userDetails.getEmployee().getEmpName())

                .deptName(
                        userDetails.getEmployee()
                                .getDepartment()
                                .getDeptName()
                )

                .role(userDetails.getRole().name())
                .build();
    }
}
