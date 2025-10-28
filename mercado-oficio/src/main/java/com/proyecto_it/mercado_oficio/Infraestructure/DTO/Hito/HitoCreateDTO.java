package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HitoCreateDTO {
    @NotNull(message = "El porcentaje de presupuesto es requerido")
    @Positive(message = "El porcentaje debe ser mayor a 0")
    @DecimalMax(value = "100", message = "El porcentaje no puede exceder 100")
    private Double porcentajePresupuesto;

    @NotNull(message = "La fecha de finalización estimada es requerida")
    private String fechaFinalizacionEstimada;

    private String fechaInicio;
}