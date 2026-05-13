package com.mini3.backend.global.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    private final String SECRET_KEY =
            "mysecretkeymysecretkeymysecretkey123456";

    private final long EXPIRATION = 1000 * 60 * 60;

    private final Key key =
            Keys.hmacShaKeyFor(
                    SECRET_KEY.getBytes()
            );

    // JWT 생성
    public String createToken(Long empNo,
                              String role) {

        return Jwts.builder()
                .setSubject(String.valueOf(empNo))
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + EXPIRATION
                        )
                )
                .signWith(
                        key,
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    // JWT Claims 추출
    public Claims getClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // JWT 검증
    public boolean validateToken(String token) {

        try {

            getClaims(token);

            return true;

        } catch (ExpiredJwtException e) {

            System.out.println(
                    "만료된 JWT 토큰"
            );

            return false;

        } catch (MalformedJwtException e) {

            System.out.println(
                    "잘못된 JWT 형식"
            );

            return false;

        } catch (SignatureException e) {

            System.out.println(
                    "JWT 서명 오류"
            );

            return false;

        } catch (UnsupportedJwtException e) {

            System.out.println(
                    "지원하지 않는 JWT"
            );

            return false;

        } catch (IllegalArgumentException e) {

            System.out.println(
                    "JWT 값이 비어있음"
            );

            return false;
        }
    }
}