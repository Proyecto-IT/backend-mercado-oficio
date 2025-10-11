package com.proyecto_it.mercado_oficio.Domain.Service.Auth;

import org.springframework.security.core.Authentication;

import java.util.Map;

public interface AuthService {
    Map<String, Object> obtenerInfoUsuario(Authentication authentication);
}
