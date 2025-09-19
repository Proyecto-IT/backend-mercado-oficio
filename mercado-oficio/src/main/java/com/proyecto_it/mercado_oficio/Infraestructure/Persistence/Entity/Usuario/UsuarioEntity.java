package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, length = 200, unique = true)
    private String gmail;

    @Column(nullable = false, length = 64)
    private String password;

    @Column(nullable = false)
    private int permiso;

    @Column(nullable = false)
    private boolean verificado = false;

    @Column(nullable = false)
    private String proveedor; // LOCAL o GOOGLE

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false, length = 20)
    private String cp;      // código postal

    @Column(nullable = false, length = 100)
    private String ciudad;

    @Column(nullable = false, length = 20)
    private String telefono;
}
