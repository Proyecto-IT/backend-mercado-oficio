package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.DisponibilidadConverter;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "servicio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relación con Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    // Relación con Oficio (si tenés una entidad para eso)
    @Column(name = "oficio_id", nullable = false)
    private Integer oficioId;

    @Column(length = 400)
    private String descripcion;

    @Column(name = "tarifa_hora", length = 64)
    private String tarifaHora;


    @Column(columnDefinition = "json")
    private String disponibilidad; // Guardado como JSON string

    private int experiencia;

    @Column(columnDefinition = "json")
    private String especialidades; // También como JSON string

    @Column(length = 150)
    private String ubicacion;

    @Column(name = "trabajos_completados")
    private int trabajosCompletados;

    // Relación con portafolio
    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortafolioEntity> portafolios;
}