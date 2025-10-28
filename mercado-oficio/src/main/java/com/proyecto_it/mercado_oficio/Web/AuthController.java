package com.proyecto_it.mercado_oficio.Web;

// ============= CONTROLLER LAYER =============

import com.proyecto_it.mercado_oficio.Domain.Model.TokenVerificacion;
import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Repository.RefreshTokenRepository;
import com.proyecto_it.mercado_oficio.Domain.Service.Auth.AuthService;
import com.proyecto_it.mercado_oficio.Domain.Service.Email.EmailService;
import com.proyecto_it.mercado_oficio.Domain.Service.JWT.JwtTokenService;
import com.proyecto_it.mercado_oficio.Exception.RefreshTokenExpiredException;
import com.proyecto_it.mercado_oficio.Exception.RefreshTokenNotFoundException;
import com.proyecto_it.mercado_oficio.Domain.Service.TokenVerificacion.TokenVerificacionService;
import com.proyecto_it.mercado_oficio.Domain.Service.Usuario.UsuarioService;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Auth.AuthRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Auth.AuthResponse;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Auth.RefreshTokenRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Usuario.*;
import com.proyecto_it.mercado_oficio.Mapper.Usuario.UsuarioMapper;
import com.proyecto_it.mercado_oficio.Security.SecurityConfig.JWT.CustomUserDetailsService;
import com.proyecto_it.mercado_oficio.Security.SecurityConfig.JWT.JwtService;
import com.proyecto_it.mercado_oficio.Security.SecurityConfig.JWT.JwtSpecialTokenService;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {

    private final UsuarioService usuarioService;
    private final TokenVerificacionService tokenService;
    private final EmailService emailService;
    private final JwtTokenService jwtTokenService;
    private final AuthService authService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final JwtSpecialTokenService jwtSpecialTokenService;
    private final CustomUserDetailsService userDetailsService;

    private final UsuarioMapper usuarioMapper;

    @Value("${app.frontend.url}")
    private String frontendUrl;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UsuarioRegistroRequest request) {
        log.info("Registro solicitado para gmail={}", request.getGmail());
        try {
            if (usuarioService.existePorGmail(request.getGmail())) {
                log.warn("Intento de registro fallido, gmail ya existe={}", request.getGmail());
                return ResponseEntity.badRequest().body("El gmail ya está registrado");
            }

            Usuario usuario = usuarioMapper.fromRegistroRequest(request);
            usuarioService.registrarUsuario(usuario);
            log.info("Usuario registrado correctamente, gmail={}", request.getGmail());

            return ResponseEntity.ok("Usuario registrado correctamente. Verifica tu email para activar la cuenta.");

        } catch (IllegalArgumentException e) {
            log.warn("Registro fallido: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado en registro: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validarEmail(@RequestParam String token) {
        log.info("Validación de email con token={}", token);
        try {
            Optional<TokenVerificacion> tokenOpt = tokenService.validarToken(token);

            if (tokenOpt.isEmpty()) {
                log.warn("Token inválido o expirado");
                return ResponseEntity.badRequest().body("Token inválido o expirado.");
            }

            TokenVerificacion tokenVerificacion = tokenOpt.get();
            Usuario usuario = usuarioService.buscarPorId(tokenVerificacion.getUsuarioId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            usuario.setVerificado(true);
            usuario.setProveedor("LOCAL");
            usuarioService.actualizarUsuario(usuario);

            tokenVerificacion.marcarComoUsado();
            log.info("Usuario verificado correctamente, gmail={}", usuario.getGmail());

            return ResponseEntity.ok("Usuario validado correctamente.");

        } catch (Exception e) {
            log.error("Error en validación de email: ", e);
            return ResponseEntity.badRequest().body("Error en la validación");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        return ResponseEntity.ok(authService.obtenerInfoUsuario(authentication));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody @Valid AuthRequest request,
            HttpServletRequest httpRequest) {

        log.info("Intento de login para gmail={}", request.getGmail());

        try {
            String oldRefreshToken = Arrays.stream(
                            Optional.ofNullable(httpRequest.getCookies()).orElse(new Cookie[0]))
                    .filter(c -> c.getName().equals("refreshToken"))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);

            if (oldRefreshToken != null && !oldRefreshToken.isEmpty()) {
                log.info("Token anterior detectado en cookie, invalidándolo...");
                try {
                    jwtTokenService.invalidarRefreshToken(oldRefreshToken);
                    log.info("Token anterior invalidado antes del login");
                } catch (Exception e) {
                    log.warn("No se pudo invalidar token anterior: {}", e.getMessage());
                }
            }

            Optional<Usuario> optUser = usuarioService.buscarPorGmail(request.getGmail());
            if (optUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciales inválidas"));
            }

            Usuario usuario = optUser.get();

            if (!usuario.isVerificado()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Usuario no verificado"));
            }

            if (!usuario.puedeUsarLocal()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Usa el login con Google para este usuario"));
            }

            var authToken = new UsernamePasswordAuthenticationToken(
                    request.getGmail(),
                    request.getPassword()
            );
            var auth = authManager.authenticate(authToken);
            var userDetails = (UserDetails) auth.getPrincipal();

            AuthResponse tokens = jwtTokenService.generarTokens(userDetails);
            log.info("Login exitoso, gmail={}, usuarioId={}",
                    request.getGmail(), tokens.getUsuarioId());

            //Crear cookie con el nuevo refresh token
            ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7 días
                    .build();

            log.info("Nueva cookie de refresh token establecida");
            log.info("Access token generado: {}...", tokens.getAccessToken().substring(0, 20));

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(Map.of(
                            "accessToken", tokens.getAccessToken(),
                            "usuarioId", tokens.getUsuarioId()
                    ));

        } catch (AuthenticationException e) {
            log.warn("Autenticación fallida para gmail={}", request.getGmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        } catch (Exception e) {
            log.error("Error inesperado en login: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        log.info("Refresh token solicitado");

        try {
            String refreshToken = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                    .filter(c -> c.getName().equals("refreshToken"))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElseThrow(() -> {
                        log.warn("No se encontró cookie refreshToken");
                        return new RuntimeException("No se encontró el refresh token");
                    });

            log.info("Refresh token encontrado en cookie: {}...", refreshToken.substring(0, 20));
            String username = jwtService.extractUsername(refreshToken);
            log.info("Username extraído del token: {}", username);
            if (jwtService.isTokenExpired(refreshToken)) {
                log.warn("JWT del refresh token expirado para {}", username);
                ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        .secure(false)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(0)
                        .build();

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                        .body(Map.of("error", "Token expirado, por favor inicia sesión nuevamente"));
            }

            AuthResponse newTokens = jwtTokenService.refrescarTokens(refreshToken);

            log.info("Nuevos tokens generados para usuario: {}", username);

            ResponseCookie cookie = ResponseCookie.from("refreshToken", newTokens.getRefreshToken())
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .build();

            log.info("Refresh exitoso, enviando nuevo accessToken");

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(Map.of(
                            "accessToken", newTokens.getAccessToken(),
                            "usuarioId", newTokens.getUsuarioId()
                    ));

        } catch (RefreshTokenNotFoundException | RefreshTokenExpiredException e) {
            log.warn("Token inválido: {}", e.getMessage());

            ResponseCookie clearCookie = createClearCookie();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                    .body(Map.of("error", "Refresh token inválido o expirado"));

        } catch (Exception e) {
            log.error("Error al refrescar token: ", e);

            ResponseCookie clearCookie = createClearCookie();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                    .body(Map.of("error", "Error al procesar el refresh token"));
        }
    }

    private ResponseCookie createClearCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }

    private ResponseEntity<?> refreshTokenWithRetry(RefreshTokenRequest request, int attempt) {
        final int MAX_RETRIES = 3;
        final long RETRY_DELAY_MS = 100;

        try {
            String username = jwtService.extractUsername(request.getRefreshToken());
            log.info("Procesando refresh token para usuario={} (intento {})", username, attempt + 1);

            AuthResponse response = jwtTokenService.refrescarTokens(request.getRefreshToken());
            log.info("Refresh exitoso para usuario={} - ID={}", username, response.getUsuarioId());

            return ResponseEntity.ok(Map.of(
                    "accessToken", response.getAccessToken(),
                    "refreshToken", response.getRefreshToken(),
                    "usuarioId", response.getUsuarioId()
            ));

        } catch (RefreshTokenExpiredException | RefreshTokenNotFoundException e) {
            log.warn("Refresh token inválido o expirado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());

        } catch (Exception e) {
            if (attempt < MAX_RETRIES && isConcurrencyError(e)) {
                log.warn("Error de concurrencia en refresh (intento {}), reintentando: {}", attempt + 1, e.getMessage());
                try {
                    Thread.sleep(RETRY_DELAY_MS * (attempt + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Proceso interrumpido");
                }
                return refreshTokenWithRetry(request, attempt + 1);
            }

            log.error("Error inesperado en refresh (intento {}): {}", attempt + 1, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    private boolean isConcurrencyError(Exception e) {
        String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        return message.contains("lock") || message.contains("timeout") || message.contains("deadlock")
                || message.contains("concurrent") || e instanceof OptimisticLockException || e instanceof PessimisticLockException;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        log.info("Logout solicitado");

        try {
            String refreshToken = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                    .filter(c -> c.getName().equals("refreshToken"))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);

            if (refreshToken != null && !refreshToken.isEmpty()) {
                log.info("Invalidando refresh token en BD");
                jwtTokenService.invalidarRefreshToken(refreshToken);
                log.info("Token invalidado exitosamente");
            } else {
                log.info("No se encontró refresh token para invalidar");
            }

            ResponseCookie clearCookie = createClearCookie();

            log.info("Logout completado");

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                    .body(Map.of("message", "Logout exitoso"));

        } catch (Exception e) {
            log.error("Error en logout: ", e);

            ResponseCookie clearCookie = createClearCookie();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                    .body(Map.of("message", "Logout procesado"));
        }
    }
    @PostMapping("/reset-password-request")
    public ResponseEntity<?> solicitarRestablecerPassword(@RequestBody @Valid EmailRequest request) {
        log.info("Solicitud de restablecimiento de password para gmail={}", request.getGmail());
        try {
            String email = request.getGmail();
            if (email == null || email.trim().isEmpty()) {
                log.warn("Email vacío en solicitud de restablecimiento");
                return ResponseEntity.badRequest().body("El email es obligatorio");
            }

            Optional<Usuario> usuarioOpt = usuarioService.buscarPorGmail(email.trim());
            if (usuarioOpt.isPresent()) {
                emailService.enviarEmailRestablecimientoPassword(email.trim());
                log.info("Email de restablecimiento enviado a gmail={}", email);
            }

            return ResponseEntity.ok("Si el email existe, se envió un enlace para restablecer la contraseña");

        } catch (Exception e) {
            log.error("Error al enviar email de restablecimiento: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la solicitud");
        }
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<?> confirmarRestablecerPassword(@RequestBody @Valid ResetPasswordConfirmRequest request) {
        log.info("Confirmación de restablecimiento de password");
        try {
            String token = request.getToken();
            String nuevaPassword = request.getNuevaPassword();

            if (nuevaPassword == null || nuevaPassword.trim().isEmpty()) {
                log.warn("Nueva contraseña vacía");
                return ResponseEntity.badRequest().body("La nueva contraseña no puede estar vacía");
            }

            String email = jwtSpecialTokenService.getSubjectFromToken(token);

            if (!jwtSpecialTokenService.validateResetPasswordToken(token, email)) {
                log.warn("Token de restablecimiento inválido o expirado para gmail={}", email);
                return ResponseEntity.badRequest().body("Token inválido o expirado");
            }

            Usuario usuario = usuarioService.buscarPorGmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            usuario.setPassword(passwordEncoder.encode(nuevaPassword));
            usuarioService.actualizarUsuario(usuario);

            log.info("Password actualizada correctamente para gmail={}", usuario.getGmail());

            return ResponseEntity.ok("Contraseña actualizada correctamente");

        } catch (Exception e) {
            log.error("Error al confirmar restablecimiento de password: ", e);
            return ResponseEntity.badRequest().body("Error al procesar la confirmación");
        }
    }

    @PutMapping("/actualizar-usuario")
    public ResponseEntity<?> actualizarUsuario(@RequestBody @Valid UsuarioUpdateRequest request,
                                               @RequestParam String gmail) {
        log.info("Solicitud de actualización de usuario, gmail={}", gmail);
        try {
            UsuarioUpdate usuarioUpdate = usuarioMapper.toUsuarioUpdate(request);
            usuarioService.actualizarUsuarioPorGmail(gmail, usuarioUpdate);
            log.info("Usuario actualizado correctamente, gmail={}", gmail);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Actualización fallida, usuario no encontrado: gmail={}", gmail);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al actualizar usuario, gmail={}: {}", gmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/oauth2/success")
    public void getUserInfo(HttpServletResponse response, OAuth2AuthenticationToken authentication) {
        try {
            Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
            String email = (String) attributes.get("email");
            String nombre = (String) attributes.getOrDefault("given_name", "");
            String apellido = (String) attributes.getOrDefault("family_name", "");
            Boolean verificadoGoogle = (Boolean) attributes.getOrDefault("email_verified", true);

            log.info("OAuth2 - Procesando usuario: {}", email);

            Usuario usuario = procesarUsuarioOAuth2(email, nombre, apellido, verificadoGoogle);

            UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getGmail());
            log.info("OAuth2 - Authorities cargadas para usuario: {}", usuario.getGmail());

            AuthResponse tokens = jwtTokenService.generarTokens(userDetails);
            log.info("OAuth2 - Tokens generados para usuario ID={}", tokens.getUsuarioId());

            if (tokens.getAccessToken() == null || tokens.getRefreshToken() == null) {
                log.error("OAuth2 - Tokens nulos generados para usuario={}", email);
                response.sendRedirect(frontendUrl + "/usuario/login/login.html?error=token_generation_failed");
                return;
            }

            String redirectUrl = String.format(
                    "%s/index.html?accessToken=%s&refreshToken=%s&usuarioId=%d",
                    frontendUrl,
                    URLEncoder.encode(tokens.getAccessToken(), StandardCharsets.UTF_8),
                    URLEncoder.encode(tokens.getRefreshToken(), StandardCharsets.UTF_8),
                    tokens.getUsuarioId()
            );

            log.info("OAuth2 - Redirigiendo usuario: {}", email);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 - Error en procesamiento: ", e);
            try {
                response.sendRedirect(frontendUrl + "/usuario/login/login.html?error=oauth_processing_failed");
            } catch (IOException ioException) {
                log.error("OAuth2 - Error en redirección de error: ", ioException);
            }
        }
    }


    public Usuario procesarUsuarioOAuth2(String email, String nombre, String apellido, Boolean verificadoGoogle) {
        Optional<Usuario> usuarioExistente = usuarioService.buscarPorGmail(email);

        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();
            log.info("OAuth2 - Usuario existente encontrado: {}", email);

            boolean necesitaActualizacion = actualizarDatosOAuth2(usuario, nombre, apellido, verificadoGoogle);

            if (necesitaActualizacion) {
                usuario = usuarioService.actualizarUsuario(usuario);
                log.info("OAuth2 - Usuario actualizado: {}", email);
            }

            return usuario;

        } else {
            log.info("OAuth2 - Creando nuevo usuario: {}", email);

            Usuario nuevoUsuario = Usuario.builder()
                    .gmail(email)
                    .nombre(nombre)
                    .apellido(apellido)
                    .verificado(true)
                    .proveedor("GOOGLE")
                    .permiso(0)
                    .build();

            // Password ficticio para usuarios de Google
            nuevoUsuario.setPassword(passwordEncoder.encode("google123!"));

            return usuarioService.actualizarUsuario(nuevoUsuario);
        }
    }

    public boolean actualizarDatosOAuth2(Usuario usuario, String nombre, String apellido, Boolean verificadoGoogle) {
        boolean necesitaActualizacion = false;

        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            usuario.setNombre(nombre);
            necesitaActualizacion = true;
        }

        if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
            usuario.setApellido(apellido);
            necesitaActualizacion = true;
        }

        if (!usuario.isVerificado() && verificadoGoogle) {
            usuario.setVerificado(true);
            necesitaActualizacion = true;
        }

        if (!usuario.puedeUsarGoogle()) {
            usuario.vincularGoogle();
            necesitaActualizacion = true;
        }

        return necesitaActualizacion;
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@RequestBody @Valid CambioPasswordDto request,
                                             @RequestParam String gmail) {
        log.info("Solicitud de cambio de contraseña, gmail={}", gmail);
        try {
            CambioPasswordRequest cambioPassword = usuarioMapper.toCambioPasswordRequest(request);
            usuarioService.cambiarPassword(gmail, cambioPassword);
            log.info("Contraseña cambiada correctamente, gmail={}", gmail);
            return ResponseEntity.ok("Contraseña cambiada correctamente");
        } catch (IllegalArgumentException e) {
            log.warn("Error en cambio de contraseña, gmail={}: {}", gmail, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al cambiar contraseña, gmail={}: {}", gmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    @PostMapping("/cambiar-email")
    public ResponseEntity<?> solicitarCambioEmail(@RequestBody @Valid CambioEmailRequest request,
                                                  @RequestParam String gmailActual) {
        log.info("Solicitud de cambio de email, gmailActual={}, nuevoEmail={}", gmailActual, request.getNuevoEmail());
        try {
            emailService.enviarEmailCambioEmail(gmailActual, request.getNuevoEmail());
            log.info("Email de confirmación enviado al nuevo email={}", request.getNuevoEmail());
            return ResponseEntity.ok("Se envió un enlace de confirmación al nuevo email");
        } catch (IllegalArgumentException e) {
            log.warn("Error en cambio de email: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al solicitar cambio de email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    @PostMapping("/confirmar-cambio-email")
    public ResponseEntity<?> confirmarCambioEmail(@RequestParam String token) {
        log.info("Confirmación de cambio de email con token={}", token);
        try {
            emailService.confirmarCambioEmail(token);
            log.info("Email cambiado correctamente");
            return ResponseEntity.ok("Email cambiado correctamente");
        } catch (IllegalArgumentException e) {
            log.warn("Error en confirmación de cambio de email: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al confirmar cambio de email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }
}