package com.proyecto_it.mercado_oficio.Domain.Service.JWT;

import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Auth.AuthResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtTokenService {
    AuthResponse generarTokens(UserDetails userDetails);
    AuthResponse refrescarTokens(String refreshToken);
}
