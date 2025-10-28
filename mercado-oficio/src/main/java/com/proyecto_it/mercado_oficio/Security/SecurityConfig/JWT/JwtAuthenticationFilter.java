package com.proyecto_it.mercado_oficio.Security.SecurityConfig.JWT;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/validate"
    );

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // Permitir rutas públicas sin autenticación
        if (PUBLIC_PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        // Validar que el header existe
        if (authHeader == null || authHeader.isBlank()) {
            logger.debug("No Authorization header presente para: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // Validar que comienza con "Bearer "
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Authorization header no tiene formato Bearer para: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extraer token (después de "Bearer ")
            final String jwtToken = authHeader.substring(7).trim();

            // ✅ VALIDACIÓN CRÍTICA: Verificar que el token no esté vacío
            if (jwtToken.isEmpty()) {
                logger.warn("Token JWT vacío después de 'Bearer ' para: {}", path);
                filterChain.doFilter(request, response);
                return;
            }

            // ✅ VALIDACIÓN ADICIONAL: Verificar formato básico de JWT (3 partes separadas por puntos)
            if (jwtToken.split("\\.").length != 3) {
                logger.warn("Token JWT con formato inválido (debe tener 3 partes). Token recibido tiene {} partes",
                        jwtToken.split("\\.").length);
                filterChain.doFilter(request, response);
                return;
            }

            logger.debug("Procesando token para path: {}", path);
            logger.debug("Token preview: {}...", jwtToken.substring(0, Math.min(20, jwtToken.length())));

            // Extraer username del token
            final String username = jwtService.extractUsername(jwtToken);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwtToken, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    logger.debug("✅ Usuario autenticado exitosamente: {}", username);
                } else {
                    logger.warn("❌ Token inválido para usuario: {}", username);
                }
            }

        } catch (io.jsonwebtoken.MalformedJwtException e) {
            logger.error("❌ Token JWT mal formado: {}", e.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.warn("⏰ Token JWT expirado: {}", e.getMessage());
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            logger.error("❌ Token JWT no soportado: {}", e.getMessage());
        } catch (io.jsonwebtoken.SignatureException e) {
            logger.error("❌ Firma JWT inválida: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("❌ Token JWT vacío o nulo: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Error procesando JWT: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            logger.debug("Stack trace:", e);
        }

        filterChain.doFilter(request, response);
    }
}

