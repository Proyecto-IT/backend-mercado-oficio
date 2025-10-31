package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Review;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewClienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;

    @Column(name = "id_servicio", nullable = false)
    private Integer idServicio;

    @Column(name = "id_presupuesto", nullable = false)
    private Integer idPresupuesto;

    @Column(name = "comentario", columnDefinition = "VARCHAR(1000)", nullable = false)
    private String comentario;

    @Column(name = "valoracion", nullable = false)
    private Integer valoracion; // 1-5 estrellas

    @CreationTimestamp
    @Column(name = "fecha", updatable = false)
    private LocalDateTime fecha;

    @OneToOne(mappedBy = "reviewCliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReviewPrestadorEntity respuestaPrestador;
}