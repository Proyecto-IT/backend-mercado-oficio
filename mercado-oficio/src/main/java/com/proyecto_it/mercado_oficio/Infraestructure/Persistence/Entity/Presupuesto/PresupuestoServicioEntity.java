package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoPresupuesto;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto.PresupuestoArchivo.PresupuestoArchivoEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.ServicioEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "presupuesto_servicio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoServicioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    private ServicioEntity servicio;

    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(name = "id_prestador", nullable = true)
    private Integer idPrestador;

    @Column(name = "descripcion_problema", columnDefinition = "VARCHAR(1000)")
    private String descripcionProblema;

    @Column(name = "horas_estimadas", nullable = true)
    private Double horasEstimadas;

    @Column(name = "costo_materiales", nullable = true)
    private BigDecimal costoMateriales;

    @Column(nullable = true)
    private BigDecimal presupuesto;

    @Column(name = "descripcion_solucion", columnDefinition = "VARCHAR(1000)", nullable = true)
    private String descripcionSolucion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", columnDefinition = "ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO')", nullable = true)
    private EstadoPresupuesto estado;

    @OneToMany(mappedBy = "presupuestoServicio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PresupuestoArchivoEntity> archivos = new ArrayList<>();

    @OneToMany(mappedBy = "presupuestoServicio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HorarioServicioEntity> horariosSeleccionados = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = true)
    private LocalDateTime fechaActualizacion;

    @Column(name = "respondido", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean respondido = false;

    // Método auxiliar para calcular el presupuesto automáticamente
    @PrePersist
    @PreUpdate
    public void calcularPresupuesto() {
        if (this.horasEstimadas != null && this.servicio != null && this.servicio.getTarifaHora() != null) {
            BigDecimal valorHora = new BigDecimal(this.servicio.getTarifaHora());
            BigDecimal costoLaboral = BigDecimal.valueOf(this.horasEstimadas)
                    .multiply(valorHora)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal materiales = this.costoMateriales != null ? this.costoMateriales : BigDecimal.ZERO;
            this.presupuesto = costoLaboral.add(materiales)
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }
}
