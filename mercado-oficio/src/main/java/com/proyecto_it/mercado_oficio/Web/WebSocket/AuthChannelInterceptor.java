package com.proyecto_it.mercado_oficio.Web.WebSocket;

import com.proyecto_it.mercado_oficio.Security.SecurityConfig.JWT.CustomUserDetailsService;
import com.proyecto_it.mercado_oficio.Security.SecurityConfig.JWT.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null &&
                (StompCommand.CONNECT.equals(accessor.getCommand()) ||
                        StompCommand.SEND.equals(accessor.getCommand()))) {

            String token = extraerToken(accessor);

            if (token != null) {
                try {
                    String username = jwtService.extractUsername(token);

                    // Validar el token con el username
                    if (jwtService.isTokenValid(token, username)) {

                        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        // Asociar el usuario autenticado al mensaje WebSocket
                        accessor.setUser(authentication);

                        log.info("✅ Usuario autenticado en WebSocket: {}", username);
                    } else {
                        log.warn("⚠️ Token JWT inválido o expirado en WebSocket");
                    }
                } catch (Exception e) {
                    log.error("❌ Error al autenticar usuario en WebSocket: {}", e.getMessage());
                }
            } else {
                log.warn("⚠️ No se encontró token en la conexión WebSocket");
            }
        }

        return message;
    }

    /**
     * Extrae el token JWT de los headers STOMP.
     */
    private String extraerToken(StompHeaderAccessor accessor) {
        String token = null;

        // Intentar desde el header "Authorization"
        if (accessor.getNativeHeader("Authorization") != null &&
                !accessor.getNativeHeader("Authorization").isEmpty()) {
            token = accessor.getFirstNativeHeader("Authorization");
        }

        // Intentar desde el header "token"
        if (token == null && accessor.getNativeHeader("token") != null) {
            token = accessor.getFirstNativeHeader("token");
        }

        // Limpiar el prefijo "Bearer " si existe
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return token;
    }
}
