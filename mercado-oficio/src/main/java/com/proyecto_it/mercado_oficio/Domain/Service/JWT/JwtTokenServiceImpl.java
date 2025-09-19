package com.proyecto_it.mercado_oficio.Domain.Service.JWT;

import com.proyecto_it.mercado_oficio.Domain.Model.RefreshToken;
import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Repository.UsuarioRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Auth.AuthResponse;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.RefreshToken.RefreshTokenRepositoryImpl;
import com.proyecto_it.mercado_oficio.Security.SecurityConfig.JWT.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtService jwtTokenService;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepositoryImpl refreshTokenRepository;

    private final ConcurrentHashMap<String, AuthResponse> refreshCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> cacheTimestamps = new ConcurrentHashMap<>();
    private static final Duration CACHE_DURATION = Duration.ofMinutes(2);

    @Override
    @Transactional
    public AuthResponse generarTokens(UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("Generando tokens para usuario: {}", username);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Map<String, Object> claims = Map.of("roles", roles);

        String accessToken = jwtTokenService.generateAccessToken(claims, username);
        String refreshTokenValue = jwtTokenService.generateRefreshToken(claims, username);

        Usuario usuario = usuarioRepository.buscarPorGmail(username)
                .orElseThrow(() -> {
                    log.error("Usuario '{}' no encontrado al generar tokens", username);
                    return new IllegalArgumentException("Usuario no encontrado");
                });

        // Expirar tokens anteriores
        refreshTokenRepository.expirarTokensPorUsuario(usuario.getId());

        // Crear nuevo refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .usuarioId(usuario.getId())
                .fechaCreacion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusDays(30))
                .estado("VALID")
                .build();
        refreshTokenRepository.guardar(refreshToken);

        log.info("Tokens generados exitosamente para usuario: {}", username);
        return new AuthResponse(accessToken, refreshTokenValue, usuario.getId());
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AuthResponse refrescarTokens(String refreshToken) {
        log.debug("Solicitado refresh de tokens para refreshToken: {}", refreshToken);

        AuthResponse cachedResponse = getCachedRefreshResponse(refreshToken);
        if (cachedResponse != null) {
            log.info("Retornando refresh desde cache para token");
            return cachedResponse;
        }

        try {
            String username = jwtTokenService.extractUsername(refreshToken);
            log.info("Procesando refresh para usuario: {}", username);

            if (jwtTokenService.isTokenExpired(refreshToken)) {
                log.warn("Refresh token expirado para usuario: {}", username);
                throw new RefreshTokenExpiredException("Refresh token expirado");
            }

            RefreshToken tokenDomain = refreshTokenRepository
                    .buscarPorTokenYEstado(refreshToken, "VALID")
                    .orElseThrow(() -> {
                        log.warn("Refresh token no válido o no encontrado para usuario: {}", username);
                        return new RefreshTokenNotFoundException("Refresh token no válido");
                    });

            Usuario usuario = usuarioRepository.buscarPorId(tokenDomain.getUsuarioId())
                    .orElseThrow(() -> {
                        log.error("Usuario con ID {} no encontrado durante refresh", tokenDomain.getUsuarioId());
                        return new IllegalArgumentException("Usuario no encontrado");
                    });

            if (!usuario.getGmail().equals(username)) {
                log.error("El refresh token no corresponde al usuario correcto. Usuario esperado: {}, token: {}", username, tokenDomain.getToken());
                throw new RefreshTokenNotFoundException("Token no válido para el usuario");
            }

            List<String> roles = List.of("ROLE_" + mapearPermiso(usuario.getPermiso()));
            Map<String, Object> claims = Map.of("roles", roles);

            String newAccessToken = jwtTokenService.generateAccessToken(claims, username);
            String newRefreshToken = refreshToken;

            boolean shouldRenewRefreshToken = tokenDomain.getFechaExpiracion()
                    .isBefore(LocalDateTime.now().plusDays(2));

            if (shouldRenewRefreshToken) {
                newRefreshToken = jwtTokenService.generateRefreshToken(claims, username);

                // Expirar token actual
                RefreshToken expiredToken = RefreshToken.builder()
                        .id(tokenDomain.getId())
                        .token(tokenDomain.getToken())
                        .usuarioId(tokenDomain.getUsuarioId())
                        .fechaExpiracion(tokenDomain.getFechaExpiracion())
                        .estado("EXPIRED")
                        .build();
                refreshTokenRepository.guardar(expiredToken);

                // Crear nuevo token
                RefreshToken newTokenDomain = RefreshToken.builder()
                        .token(newRefreshToken)
                        .usuarioId(usuario.getId())
                        .fechaExpiracion(LocalDateTime.now().plusDays(30))
                        .estado("VALID")
                        .build();
                refreshTokenRepository.guardar(newTokenDomain);

                log.info("Refresh token renovado para usuario: {}", username);
            } else {
                log.info("Refresh token reutilizado para usuario: {}", username);
            }

            AuthResponse response = new AuthResponse(newAccessToken, newRefreshToken, usuario.getId());
            cacheRefreshResponse(refreshToken, response);

            log.info("Refresh exitoso para usuario: {}", username);
            return response;

        } catch (Exception e) {
            log.error("Error en refresh para token '{}': {}", refreshToken, e.getMessage(), e);
            throw e;
        }
    }

    private AuthResponse getCachedRefreshResponse(String refreshToken) {
        LocalDateTime cacheTime = cacheTimestamps.get(refreshToken);
        if (cacheTime != null && cacheTime.plus(CACHE_DURATION).isAfter(LocalDateTime.now())) {
            log.debug("Cache válido encontrado para refreshToken");
            return refreshCache.get(refreshToken);
        }

        refreshCache.remove(refreshToken);
        cacheTimestamps.remove(refreshToken);
        log.debug("No hay cache válido para refreshToken o expirado");
        return null;
    }

    private void cacheRefreshResponse(String refreshToken, AuthResponse response) {
        refreshCache.put(refreshToken, response);
        cacheTimestamps.put(refreshToken, LocalDateTime.now());
        log.debug("Refresh token cacheado temporalmente");

        if (refreshCache.size() > 1000) {
            log.debug("Limpiando entradas antiguas del cache de refresh tokens");
            cleanOldCacheEntries();
        }
    }

    private void cleanOldCacheEntries() {
        LocalDateTime cutoff = LocalDateTime.now().minus(CACHE_DURATION);
        cacheTimestamps.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(cutoff)) {
                refreshCache.remove(entry.getKey());
                return true;
            }
            return false;
        });
        log.debug("Cache de refresh tokens limpiado");
    }

    private String mapearPermiso(Integer permiso) {
        return switch (permiso) {
            case 0 -> "CLIENTE";
            case 1 -> "ADMIN";
            case 2 -> "TRABAJADOR";
            default -> {
                log.error("Permiso inválido: {}", permiso);
                throw new IllegalArgumentException("Permiso inválido: " + permiso);
            }
        };
    }
}
