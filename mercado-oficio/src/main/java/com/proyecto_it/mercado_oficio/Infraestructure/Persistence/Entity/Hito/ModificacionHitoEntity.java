package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Hito;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoAprobacion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "modificacion_hito")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModificacionHitoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hito_id", nullable = false)
    private HitoEntity hito;

    @Column(name = "descripcion_cambio", columnDefinition = "VARCHAR(1000)", nullable = false)
    private String descripcionCambio;

    @Column(name = "monto_anterior", nullable = false)
    private BigDecimal montoAnterior;

    @Column(name = "monto_nuevo", nullable = false)
    private BigDecimal montoNuevo;

    @Column(name = "fecha_inicio_anterior")
    private LocalDateTime fechaInicioAnterior;

    @Column(name = "fecha_inicio_nueva")
    private LocalDateTime fechaIniciNueva;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_aprobacion", columnDefinition = "ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO')")
    private EstadoAprobacion estadoAprobacion = EstadoAprobacion.PENDIENTE;

    @Column(name = "aprobado_cliente")
    private Boolean aprobadoCliente = false;

    @Column(name = "aprobado_prestador")
    private Boolean aprobadoPrestador = false;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}