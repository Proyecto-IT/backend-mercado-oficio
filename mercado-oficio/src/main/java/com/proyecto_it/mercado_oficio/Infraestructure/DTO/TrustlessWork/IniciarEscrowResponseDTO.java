package com.proyecto_it.mercado_oficio.Infraestructure.DTO.TrustlessWork;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IniciarEscrowResponseDTO {
    private Integer hitoId;
    private String contractId;              // Dirección del contrato en Stellar
    private String transactionHash;        // Hash de la transacción
    private String walletCliente;          // Wallet generada para cliente
    private String walletPrestador;        // Wallet generada para prestador
    private String mensaje;
    private String estado;                 // FINANCIADO
}