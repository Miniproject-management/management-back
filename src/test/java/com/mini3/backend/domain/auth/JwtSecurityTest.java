package com.mini3.backend.domain.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class JwtSecurityTest {
    @Test
    void bcryptPassword() {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        System.out.println(
                encoder.encode("password123")
        );
    }
    
}
