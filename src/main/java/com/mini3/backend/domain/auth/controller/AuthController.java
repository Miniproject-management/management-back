package com.mini3.backend.domain.auth.controller;

import com.mini3.backend.domain.auth.dto.*;
import com.mini3.backend.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponseDto login(
            @RequestBody LoginRequestDto requestDto
    ) {
        return authService.login(requestDto);
    }
}
