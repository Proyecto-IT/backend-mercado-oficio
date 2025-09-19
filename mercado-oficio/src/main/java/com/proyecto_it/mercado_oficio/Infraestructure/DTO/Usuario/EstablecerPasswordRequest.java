package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstablecerPasswordRequest {
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 16, message = "La nueva contraseña debe tener entre 8 y 16 caracteres")
    private String nuevaPassword;
}