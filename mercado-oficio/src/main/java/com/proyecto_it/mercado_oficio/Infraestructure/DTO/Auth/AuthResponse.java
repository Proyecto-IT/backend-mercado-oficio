package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private final Integer usuarioId;
}
