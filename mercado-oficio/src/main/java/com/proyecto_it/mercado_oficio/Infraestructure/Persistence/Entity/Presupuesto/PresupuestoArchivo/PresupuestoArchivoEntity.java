package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto.PresupuestoArchivo;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.TipoArchivo;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto.PresupuestoServicioEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "presupuesto_archivo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoArchivoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_servicio_id", nullable = false)
    private PresupuestoServicioEntity presupuestoServicio;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] contenido;

    @Column(name = "tipo_mime", nullable = false, length = 100)
    private String tipoMime;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_archivo", nullable = false)
    private TipoArchivo tipoArchivo;

    @Column(name = "tamanio_mb", nullable = false, precision = 10, scale = 2)
    private BigDecimal tamanioMb;

    @CreationTimestamp
    @Column(name = "fecha_carga", nullable = false, updatable = false)
    private LocalDateTime fechaCarga;
}
