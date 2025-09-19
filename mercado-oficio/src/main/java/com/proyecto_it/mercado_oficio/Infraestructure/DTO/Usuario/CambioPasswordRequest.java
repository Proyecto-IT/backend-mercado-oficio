package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioPasswordRequest {
    private String passwordActual;
    private String nuevaPassword;

    public void validar() {
        if (passwordActual == null || passwordActual.isBlank()) {
            throw new IllegalArgumentException("La contraseña actual es obligatoria");
        }
        if (nuevaPassword == null || nuevaPassword.length() < 6) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres");
        }
    }
}

