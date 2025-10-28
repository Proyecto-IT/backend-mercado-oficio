package com.proyecto_it.mercado_oficio.Domain.Model;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoPresupuesto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoServicio {
    private Integer id;
    private Integer servicioId;
    private Integer idCliente;
    private Integer idPrestador;
    private String descripcionProblema;
    private Double horasEstimadas;
    private BigDecimal costoMateriales;
    private BigDecimal presupuesto;
    private String descripcionSolucion;
    private EstadoPresupuesto estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String tarifaHora;
    private String nombreCliente;
    private String apellidoCliente;
    private String emailCliente;
    private String nombrePrestador;
    private String apellidoPrestador;
    private String emailPrestador;
    private Boolean respondido;
    private LocalDateTime fechaRespuesta;
    private List<PresupuestoArchivo> archivos;
}
