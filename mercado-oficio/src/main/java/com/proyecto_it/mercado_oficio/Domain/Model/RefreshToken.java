package com.proyecto_it.mercado_oficio.Domain.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    private Long id;
    private String token;
    private Integer usuarioId; // Agregado campo faltante
    private LocalDateTime fechaExpiracion;
    private LocalDateTime fechaCreacion;
    private String estado; // VALID, EXPIRED, REVOKED

    public boolean esValido() {
        return "VALID".equals(estado) && !estaVencido();
    }

    public boolean estaVencido() {
        return fechaExpiracion.isBefore(LocalDateTime.now());
    }

    public void expirar() {
        this.estado = "EXPIRED";
    }

    public void revocar() {
        this.estado = "REVOKED";
    }
}
