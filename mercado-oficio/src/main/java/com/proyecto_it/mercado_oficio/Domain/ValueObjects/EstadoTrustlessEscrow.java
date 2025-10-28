package com.proyecto_it.mercado_oficio.Domain.ValueObjects;

public enum EstadoTrustlessEscrow {
    CREADO("Creado en blockchain"),
    FINANCIADO("Fondos depositados"),
    COMPLETADO("Trabajo completado"),
    LIBERADO("Fondos liberados"),
    EN_DISPUTA("En disputa"),
    RESUELTO("Disputa resuelta");

    private final String descripcion;

    EstadoTrustlessEscrow(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}