package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Oficio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OficioUpdateRequest {
    private Integer id;

    @NotBlank(message = "El nombre del oficio es obligatorio")
    @Size(max = 100, message = "El nombre del oficio puede tener hasta 100 caracteres")
    private String nombre;
}
