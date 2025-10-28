package com.proyecto_it.mercado_oficio.Infraestructure.DTO.TrustlessWork;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletInfoDTO {
    private Integer usuarioId;
    private String walletAddress;
    private LocalDateTime fechaCreacion;
    private String estado;
}