package com.proyecto_it.mercado_oficio.Domain.Service.JWT;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException(String message) {
        super(message);
    }
}
