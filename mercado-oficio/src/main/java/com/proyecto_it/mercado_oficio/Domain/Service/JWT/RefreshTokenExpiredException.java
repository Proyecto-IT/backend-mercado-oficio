package com.proyecto_it.mercado_oficio.Domain.Service.JWT;

public class RefreshTokenExpiredException extends RuntimeException {
    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
