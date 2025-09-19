package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.TokenVerificacion;


import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens_verificacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenVerificacionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "fecha_creacion", nullable = false, columnDefinition = "datetime(6)")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false, columnDefinition = "datetime(6)")
    private LocalDateTime fechaExpiracion;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(nullable = false)
    private Boolean usado = false;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
