package com.proyecto_it.mercado_oficio.Security.Hasher;

public interface PasswordHasher {
    String hash(String contraseña);
    boolean matches(String rawPassword, String hashedPassword);
}

