package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModificacionHitoCreateDTO {
    @NotBlank(message = "La descripción del cambio es requerida")
    @Size(min = 10, max = 1000, message = "La descripción debe tener entre 10 y 1000 caracteres")
    private String descripcionCambio;

    @NotNull(message = "El monto nuevo es requerido")
    @Positive(message = "El monto debe ser mayor a 0")
    private Double montoNuevo;

    @NotNull(message = "La nueva fecha de inicio es requerida")
    private String fechaIniciNueva;
}