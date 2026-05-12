package com.mini3.backend.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponseDto {

    private String accessToken;
    private Long empNo;
    private String empName;
    private String deptName; 
    private String role;
}
