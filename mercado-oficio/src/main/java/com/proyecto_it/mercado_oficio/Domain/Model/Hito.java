package com.proyecto_it.mercado_oficio.Domain.Model;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoHito;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hito {
    private Integer id;
    private Integer presupuestoId;
    private BigDecimal porcentajePresupuesto;
    private BigDecimal monto;
    private EstadoHito estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFinalizacionEstimada;
}
