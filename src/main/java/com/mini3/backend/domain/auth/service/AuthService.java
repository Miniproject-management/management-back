package com.mini3.backend.domain.auth.service;

import com.mini3.backend.domain.auth.dto.*;
import com.mini3.backend.global.security.custom.CustomUserDetails;
import com.mini3.backend.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    // 로그인 실패 횟수 저장
    private final Map<String, Integer> loginFailMap
            = new ConcurrentHashMap<>();

    // 최대 로그인 실패 횟수
    private static final int MAX_LOGIN_FAIL = 5;

    public LoginResponseDto login(LoginRequestDto requestDto) {

        String empNo =
                String.valueOf(requestDto.getEmpNo());

        // 로그인 실패 5회 이상 차단
        if (loginFailMap.getOrDefault(empNo, 0)
                >= MAX_LOGIN_FAIL) {

            throw new RuntimeException(
                    "로그인 실패 5회 초과로 계정이 잠겼습니다."
            );
        }

        try {

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    requestDto.getEmpNo(),
                                    requestDto.getPassword()
                            )
                    );

            // 로그인 성공 시 실패 횟수 초기화
            loginFailMap.remove(empNo);

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

        } catch (BadCredentialsException e) {

            // 로그인 실패 횟수 증가
            loginFailMap.put(
                    empNo,
                    loginFailMap.getOrDefault(empNo, 0) + 1
            );

            int failCount =
                    loginFailMap.get(empNo);

            // 남은 횟수 계산
            int remain =
                    MAX_LOGIN_FAIL - failCount;

            if (remain <= 0) {
                throw new RuntimeException(
                        "로그인 실패 5회 초과로 계정이 잠겼습니다."
                );
            }

            throw new RuntimeException(
                    "로그인 실패 (" + failCount +
                            "회). 남은 횟수 : " +
                            remain + "회"
            );
        }
    }
}