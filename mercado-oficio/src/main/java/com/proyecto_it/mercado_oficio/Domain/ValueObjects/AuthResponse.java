package com.proyecto_it.mercado_oficio.Domain.ValueObjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
    private final String accessToken;
    private final String refreshToken;
    private final Integer usuarioId;
}