package com.proyecto_it.mercado_oficio.Domain.Model;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.Disponibilidad;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.Especialidades;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
public class Servicio {
    private Integer id;
    private Integer usuarioId;
    private Integer oficioId;
    private String descripcion;
    private BigDecimal tarifaHora;
    private Disponibilidad disponibilidad;
    private Integer experiencia;
    private Especialidades especialidades;
    private String ubicacion;
    private Integer trabajosCompletados;

    // Datos adicionales para respuestas
    private String nombreOficio;
    private String imagenUrl;

    public void validar() {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID de usuario es requerido");
        }
        if (oficioId == null) {
            throw new IllegalArgumentException("El ID de oficio es requerido");
        }
        if (descripcion != null && descripcion.length() > 400) {
            throw new IllegalArgumentException("La descripción no puede exceder 400 caracteres");
        }
        if (tarifaHora != null && tarifaHora.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La tarifa por hora debe ser positiva");
        }
        if (experiencia != null && experiencia < 0) {
            throw new IllegalArgumentException("La experiencia no puede ser negativa");
        }
        if (ubicacion != null && ubicacion.length() > 150) {
            throw new IllegalArgumentException("La ubicación no puede exceder 150 caracteres");
        }
    }
}
