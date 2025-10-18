package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto;

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
public class PresupuestoServicioCreateDTO {
    @NotNull(message = "El servicio es requerido")
    private Integer servicioId;

    @NotNull(message = "El ID del cliente es requerido")
    @Positive(message = "El ID del cliente debe ser positivo")
    private Integer idCliente;

    @NotBlank(message = "La descripción del problema es requerida")
    @Size(min = 10, max = 1000, message = "La descripción debe tener entre 10 y 1000 caracteres")
    private String descripcionProblema;
}