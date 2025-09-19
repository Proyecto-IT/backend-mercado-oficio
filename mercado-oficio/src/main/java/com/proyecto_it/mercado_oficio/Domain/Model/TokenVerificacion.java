package com.proyecto_it.mercado_oficio.Domain.Model;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenVerificacion {
    private Long id;
    private String token;
    private LocalDateTime fechaExpiracion;
    private LocalDateTime fechaCreacion;
    private Integer usuarioId;
    private boolean usado;

    public boolean estaVencido() {
        return fechaExpiracion.isBefore(LocalDateTime.now());
    }

    public boolean esValido() {
        return !usado && !estaVencido();
    }

    public void marcarComoUsado() {
        this.usado = true;
    }

    public static TokenVerificacion crearNuevo(Integer usuarioId, int horasValidez) {
        return TokenVerificacion.builder()
                .token(UUID.randomUUID().toString())
                .usuarioId(usuarioId)
                .fechaCreacion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusHours(horasValidez))
                .usado(false)
                .build();
    }
}
