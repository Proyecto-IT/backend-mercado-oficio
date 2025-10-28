package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoPresupuesto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoServicioUpdateDTO {
    @NotNull(message = "El ID del prestador es requerido")
    @Positive(message = "El ID del prestador debe ser positivo")
    private Integer idPrestador;

    @Positive(message = "Las horas estimadas deben ser mayores a 0")
    private Double horasEstimadas;

    @DecimalMin(value = "0.0", inclusive = true, message = "El costo de materiales no puede ser negativo")
    private BigDecimal costoMateriales;

    @Size(max = 1000, message = "La descripción de solución no puede exceder 1000 caracteres")
    private String descripcionSolucion;

    private EstadoPresupuesto estado;
    private List<HorarioServicioDTO> horariosSeleccionados;
}
