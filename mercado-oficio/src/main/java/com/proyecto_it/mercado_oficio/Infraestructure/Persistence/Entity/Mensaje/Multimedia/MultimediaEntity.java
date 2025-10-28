package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Mensaje;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "multimedia")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultimediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", length = 255)
    private String nombre;

    @Column(name = "tipo_contenido", length = 100)
    private String tipoContenido;

    @Column(name = "extension", length = 10)
    private String extension;

    @Lob
    @Column(name = "datos", columnDefinition = "LONGBLOB")
    private byte[] datos;
}