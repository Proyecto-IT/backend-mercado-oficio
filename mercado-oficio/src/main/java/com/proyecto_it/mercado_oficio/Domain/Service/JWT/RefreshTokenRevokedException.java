package com.proyecto_it.mercado_oficio.Domain.Service.JWT;

public class RefreshTokenRevokedException extends RuntimeException {
    public RefreshTokenRevokedException(String message) {
        super(message);
    }
}
