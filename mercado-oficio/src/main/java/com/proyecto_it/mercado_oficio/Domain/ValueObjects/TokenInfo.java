package com.proyecto_it.mercado_oficio.Domain.ValueObjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class TokenInfo {
    private final String token;
    private final String email;
    private final String tipoToken;
    private final boolean esValido;
}
