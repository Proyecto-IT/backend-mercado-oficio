package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Oficio;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "oficio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OficioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;
}
