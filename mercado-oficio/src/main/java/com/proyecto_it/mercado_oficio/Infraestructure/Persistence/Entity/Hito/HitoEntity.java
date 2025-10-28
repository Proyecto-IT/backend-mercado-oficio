package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Hito;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoHito;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto.PresupuestoServicioEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "hito")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HitoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_id")
    private PresupuestoServicioEntity presupuesto;

    @Column(name = "porcentaje_presupuesto")
    private BigDecimal porcentajePresupuesto;

    @Column(name = "monto")
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoHito estado = EstadoHito.PENDIENTE;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_finalizacion_estimada")
    private LocalDateTime fechaFinalizacionEstimada;
}
