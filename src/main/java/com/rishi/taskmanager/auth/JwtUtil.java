package com.rishi.taskmanager.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:900000}")
    private long expirationMs;

    private static final String ISSUER = "taskmanager";

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ── Generate token ────────────────────────────────────────
    public String generateToken(String userId, String role) {
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getKey())
                .compact();
    }

    // ── Extract userId as String ──────────────────────────────
    public String extractUserId(String token) {
        return parseClaims(token).getSubject(); // was parseLong()
    }

    // ── Extract userId as Long ────────────────────────────────
    public Long extractUserIdAsLong(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    // ── Extract role ──────────────────────────────────────────
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // ── Validate token ────────────────────────────────────────
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            //throw new RuntimeException("Token has expired");
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            //throw new RuntimeException("Invalid token");
            return false;
        }
    }

    // ── Internal parser ───────────────────────────────────────
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}