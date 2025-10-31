package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Review;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_prestador")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPrestadorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_prestador", nullable = false)
    private Integer idPrestador;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_review_cliente", nullable = false, unique = true)
    private ReviewClienteEntity reviewCliente;

    @Column(name = "comentario", columnDefinition = "VARCHAR(1000)", nullable = false)
    private String comentario;

    @CreationTimestamp
    @Column(name = "fecha", updatable = false)
    private LocalDateTime fecha;
}