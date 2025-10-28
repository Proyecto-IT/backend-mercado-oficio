package com.proyecto_it.mercado_oficio.Domain.Service.JWT;

import com.proyecto_it.mercado_oficio.Domain.Model.RefreshToken;
import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Repository.RefreshTokenRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.UsuarioRepository;
import com.proyecto_it.mercado_oficio.Exception.RefreshTokenExpiredException;
import com.proyecto_it.mercado_oficio.Exception.RefreshTokenNotFoundException;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Auth.AuthResponse;
import com.proyecto_it.mercado_oficio.Security.SecurityConfig.JWT.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtService jwtTokenService;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public AuthResponse generarTokens(UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("Generando tokens para usuario: {}", username);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        log.info("Roles del usuario: {}", roles);

        Map<String, Object> claims = Map.of("roles", roles);

        // Buscar usuario PRIMERO
        Usuario usuario = usuarioRepository.buscarPorGmail(username)
                .orElseThrow(() -> {
                    log.error("Usuario '{}' no encontrado al generar tokens", username);
                    return new IllegalArgumentException("Usuario no encontrado");
                });

        log.info("Expirando tokens anteriores para usuarioId={}", usuario.getId());
        int expiredCount = refreshTokenRepository.expirarTokensPorUsuario(usuario.getId());
        log.info("{} tokens anteriores expirados", expiredCount);

        String accessToken = jwtTokenService.generateAccessToken(claims, username);
        String refreshTokenValue = jwtTokenService.generateRefreshToken(claims, username);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .usuarioId(usuario.getId())
                .fechaCreacion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusDays(7))
                .estado("VALID")
                .build();

        RefreshToken savedToken = refreshTokenRepository.guardar(refreshToken);

        log.info("Tokens generados y refresh token guardado (id={}) para usuario: {}",
                savedToken.getId(), username);

        return new AuthResponse(accessToken, refreshTokenValue, usuario.getId());
    }

    @Override
    @Transactional
    public AuthResponse refrescarTokens(String refreshToken) {
        log.info("Procesando refresh de tokens...");

        try {
            String username = jwtTokenService.extractUsername(refreshToken);
            log.info("Usuario extraído del token: {}", username);

            if (jwtTokenService.isTokenExpired(refreshToken)) {
                log.warn("Refresh token JWT expirado para usuario: {}", username);
                throw new RefreshTokenExpiredException("Refresh token expirado");
            }

            RefreshToken tokenDomain = refreshTokenRepository
                    .buscarPorTokenYEstado(refreshToken, "VALID")
                    .orElseThrow(() -> {
                        log.warn("Refresh token no encontrado en BD o no es VALID");
                        log.warn("Token recibido: {}...", refreshToken.substring(0, 30));
                        try {
                            Usuario user = usuarioRepository.buscarPorGmail(username).orElse(null);
                            if (user != null) {
                                List<RefreshToken> validTokens = refreshTokenRepository
                                        .buscarPorUsuarioYEstado(user.getId(), "VALID");

                                if (!validTokens.isEmpty()) {
                                    log.warn("Usuario tiene {} tokens VALID, pero el recibido no coincide",
                                            validTokens.size());
                                    log.warn("Primer token válido en BD: {}...",
                                            validTokens.get(0).getToken().substring(0, 30));
                                }
                            }
                        } catch (Exception debugEx) {
                            log.error("Error en debug: {}", debugEx.getMessage());
                        }

                        throw new RefreshTokenNotFoundException("Refresh token no válido o ya fue usado");
                    });

            log.info("Token encontrado en BD con id={}", tokenDomain.getId());
            if (tokenDomain.estaVencido()) {
                log.warn("Refresh token vencido en BD para usuario: {}", username);
                tokenDomain.expirar();
                refreshTokenRepository.guardar(tokenDomain);
                throw new RefreshTokenExpiredException("Refresh token expirado");
            }

            Usuario usuario = usuarioRepository.buscarPorId(tokenDomain.getUsuarioId())
                    .orElseThrow(() -> {
                        log.error("Usuario con ID {} no encontrado durante refresh",
                                tokenDomain.getUsuarioId());
                        return new IllegalArgumentException("Usuario no encontrado");
                    });

            if (!usuario.getGmail().equals(username)) {
                log.error("Token no corresponde al usuario correcto");
                throw new RefreshTokenNotFoundException("Token no válido para el usuario");
            }

            log.info("Generando nuevos tokens para usuario: {}", username);

            List<String> roles = List.of("ROLE_" + mapearPermiso(usuario.getPermiso()));
            Map<String, Object> claims = Map.of("roles", roles);

            String newAccessToken = jwtTokenService.generateAccessToken(claims, username);
            String newRefreshToken = jwtTokenService.generateRefreshToken(claims, username);

            log.info("Expirando refresh token id={}", tokenDomain.getId());
            tokenDomain.expirar();
            refreshTokenRepository.guardar(tokenDomain);

            RefreshToken newTokenDomain = RefreshToken.builder()
                    .token(newRefreshToken)
                    .usuarioId(usuario.getId())
                    .fechaCreacion(LocalDateTime.now())
                    .fechaExpiracion(LocalDateTime.now().plusDays(7))
                    .estado("VALID")
                    .build();

            RefreshToken savedToken = refreshTokenRepository.guardar(newTokenDomain);

            log.info("Refresh exitoso. Nuevo token guardado (id={}) para usuario: {}",
                    savedToken.getId(), username);

            return new AuthResponse(newAccessToken, newRefreshToken, usuario.getId());

        } catch (RefreshTokenExpiredException | RefreshTokenNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado en refresh: {}", e.getMessage(), e);
            throw new RuntimeException("Error al refrescar tokens", e);
        }
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

    @Override
    @Transactional
    public void invalidarRefreshToken(String refreshToken) {
        log.info("Buscando token para invalidar...");

        Optional<RefreshToken> tokenOpt = refreshTokenRepository
                .buscarPorTokenYEstado(refreshToken, "VALID");

        if (tokenOpt.isPresent()) {
            RefreshToken token = tokenOpt.get();
            token.expirar();
            refreshTokenRepository.guardar(token);
            log.info("Token id={} invalidado", token.getId());
        } else {
            log.warn("Token no encontrado o ya estaba expirado");
        }
    }
}