package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.TrustlessWork;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "trustless_escrow_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrustlessEscrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hito_id", nullable = false)
    private Integer hitoId;

    @Column(name = "contract_id", unique = true, nullable = false)
    private String contractId;

    @Column(name = "transaction_hash", nullable = false)
    private String transactionHash;

    @Column(name = "estado", nullable = false)
    private String estado;  // CREADO, FINANCIADO, COMPLETADO, LIBERADO, EN_DISPUTA, RESUELTO

    @Column(name = "unsigned_xdr", columnDefinition = "LONGTEXT")
    private String unsignedXdr;

    @Column(name = "signed_xdr", columnDefinition = "LONGTEXT")
    private String signedXdr;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    @Column(name = "wallet_cliente", nullable = false)
    private String walletCliente;

    @Column(name = "wallet_prestador", nullable = false)
    private String walletPrestador;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        ultimaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        ultimaActualizacion = LocalDateTime.now();
    }
}
