package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoPresupuesto;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoServicioDTO {
    private Integer id;
    private Integer servicioId;
    private Integer idCliente;
    private Integer idPrestador;
    private String descripcionProblema;
    private BigDecimal presupuesto;
    private String descripcionSolucion;
    private EstadoPresupuesto estado;
    private Double horasEstimadas;
    private BigDecimal costoMateriales;
    private List<PresupuestoArchivoDTO> archivos;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String tarifaHora;
    private String nombreCliente;
    private String apellidoCliente;
    private Boolean respondido;
    private String nombrePrestador;
    private String apellidoPrestador;

    // Constructor para JPQL sin archivos
    public PresupuestoServicioDTO(Integer id, Integer servicioId, Integer idCliente,
                                  String nombreCliente, String apellidoCliente,
                                  String descripcionProblema, LocalDateTime fechaCreacion,
                                  EstadoPresupuesto estado, String tarifaHora) {
        this.id = id;
        this.servicioId = servicioId;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.apellidoCliente = apellidoCliente;
        this.descripcionProblema = descripcionProblema;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.tarifaHora = tarifaHora;
    }
    // Constructor para JPQL
    public PresupuestoServicioDTO(Integer id, Integer servicioId, Integer idCliente,
                                  String nombreCliente, String apellidoCliente,
                                  Integer idPrestador, String nombrePrestador, String apellidoPrestador,
                                  String descripcionProblema, LocalDateTime fechaCreacion,
                                  EstadoPresupuesto estado, String tarifaHora, Boolean respondido,
                                  BigDecimal presupuesto, String descripcionSolucion, LocalDateTime fechaActualizacion,
                                  BigDecimal costoMateriales, Double horasEstimadas) {
        this.id = id;
        this.servicioId = servicioId;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.apellidoCliente = apellidoCliente;
        this.idPrestador = idPrestador;
        this.nombrePrestador = nombrePrestador;
        this.apellidoPrestador = apellidoPrestador;
        this.descripcionProblema = descripcionProblema;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.tarifaHora = tarifaHora;
        this.respondido = respondido;
        this.presupuesto = presupuesto;
        this.descripcionSolucion = descripcionSolucion;
        this.fechaActualizacion = fechaActualizacion;
        this.costoMateriales = costoMateriales;
        this.horasEstimadas = horasEstimadas;
    }
}
