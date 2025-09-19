package com.proyecto_it.mercado_oficio.Security.SecurityConfig.JWT;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    // Duraciones en milisegundos
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 15; // 15 minutos
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7 días

    // Clave de firma
    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Extraer claims
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                // ⏰ Clock skew de 60 segundos para tokens de 15 minutos
                .setAllowedClockSkewSeconds(60)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            // Cualquier otro error del token lo consideramos como expirado
            return true;
        }
    }

    /**
     * ⏰ Verifica si el token expirará en los próximos minutos (útil para renovación proactiva)
     */
    public boolean isTokenNearExpiration(String token, int minutesThreshold) {
        try {
            Date expiration = extractExpiration(token);
            long timeToExpire = expiration.getTime() - System.currentTimeMillis();
            return timeToExpire < (minutesThreshold * 60 * 1000);
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isTokenValid(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return extractedUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // ========== Generación de Tokens ==========

    public String generateAccessToken(Map<String, Object> extraClaims, String username) {
        return generateToken(extraClaims, username, ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(Map<String, Object> extraClaims, String username) {
        return generateToken(extraClaims, username, REFRESH_TOKEN_EXPIRATION);
    }

    private String generateToken(Map<String, Object> extraClaims, String subject, long expirationMillis) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}