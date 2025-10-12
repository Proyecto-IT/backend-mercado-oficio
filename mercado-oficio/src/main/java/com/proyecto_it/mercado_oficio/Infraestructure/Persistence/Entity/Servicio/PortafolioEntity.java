package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "portafolio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortafolioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relación con Servicio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private ServicioEntity servicio;

    @Column(length = 100, nullable = false)
    private String titulo;

    @Column(length = 200, nullable = false)
    private String descripcion;
}