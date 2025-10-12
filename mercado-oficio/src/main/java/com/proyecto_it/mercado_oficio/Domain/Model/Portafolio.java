package com.proyecto_it.mercado_oficio.Domain.Model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Portafolio {
    private Integer id;
    private Integer servicioId;
    private String titulo;
    private String descripcion;

    public void validar() {
        if (servicioId == null) {
            throw new IllegalArgumentException("El ID de servicio es requerido");
        }
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título es requerido");
        }
        if (titulo.length() > 100) {
            throw new IllegalArgumentException("El título no puede exceder 100 caracteres");
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción es requerida");
        }
        if (descripcion.length() > 200) {
            throw new IllegalArgumentException("La descripción no puede exceder 200 caracteres");
        }
    }
}