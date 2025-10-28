package com.proyecto_it.mercado_oficio.Domain.Model;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoAprobacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModificacionHito {
    private Integer id;
    private Integer hitoId;
    private String descripcionCambio;
    private BigDecimal montoAnterior;
    private BigDecimal montoNuevo;
    private LocalDateTime fechaInicioAnterior;
    private LocalDateTime fechaIniciNueva;
    private EstadoAprobacion estadoAprobacion;
    private Boolean aprobadoCliente;
    private Boolean aprobadoPrestador;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
