package com.proyecto_it.mercado_oficio.Web.WebSocket;

import com.proyecto_it.mercado_oficio.Domain.Service.JWT.JwtTokenService;
import com.proyecto_it.mercado_oficio.Security.SecurityConfig.JWT.CustomUserDetailsService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.messaging.simp.stomp.StompCommand;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenService jwtTokenService;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthChannelInterceptor(JwtTokenService jwtTokenService,
                                  CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenService = jwtTokenService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        //Accede a los headers STOMP
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand()) ||
                StompCommand.SEND.equals(accessor.getCommand())) {

            String token = null;

            // Puede venir en el header "Authorization"
            if (accessor.getNativeHeader("Authorization") != null &&
                    !accessor.getNativeHeader("Authorization").isEmpty()) {
                token = accessor.getFirstNativeHeader("Authorization");
            }

            // O puede venir en la query del WebSocket (si lo mandás por URL)
            if (token == null && accessor.getNativeHeader("token") != null) {
                token = accessor.getFirstNativeHeader("token");
            }

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // Quita "Bearer "
            }

            if (token != null && jwtTokenService.validateToken(token)) {
                String username = jwtTokenService.getUsernameFromToken(token);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                // Crea el objeto de autenticación
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                // Asigna el usuario autenticado al mensaje
                accessor.setUser(authentication);
            } else {
                // Si el token no es válido, se puede bloquear la conexión o dejar sin usuario
                System.out.println("Token inválido o ausente en WebSocket: " + token);
            }
        }

        return message;
    }
}
