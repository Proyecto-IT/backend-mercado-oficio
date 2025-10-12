package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio;

import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.Portafolio.PortafolioRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioUpdateDTO {

    @Positive(message = "El ID del oficio debe ser positivo")
    private Integer oficioId;

    @Size(max = 400, message = "La descripción no puede exceder 400 caracteres")
    private String descripcion;

    @DecimalMin(value = "0.0", inclusive = false, message = "La tarifa debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "La tarifa debe tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal tarifaHora;

    @Size(min = 1, message = "Debe indicar al menos un día de disponibilidad")
    private Map<String, String> disponibilidad;

    @Min(value = 0, message = "La experiencia no puede ser negativa")
    @Max(value = 50, message = "La experiencia no puede exceder 50 años")
    private Integer experiencia;

    @Size(min = 1, max = 10, message = "Debe tener entre 1 y 10 especialidades")
    private List<@NotBlank(message = "La especialidad no puede estar vacía")
    @Size(max = 50, message = "Cada especialidad no puede exceder 50 caracteres") String> especialidades;

    @Size(max = 150, message = "La ubicación no puede exceder 150 caracteres")
    private String ubicacion;

    // Lista de portafolios opcionales al actualizar servicio
    @Valid
    private List<PortafolioRequestDTO> portafolios;

    @AssertTrue(message = "El formato de disponibilidad es inválido. Use HH:mm-HH:mm")
    public boolean isDisponibilidadValida() {
        if (disponibilidad == null) return true;

        for (Map.Entry<String, String> entry : disponibilidad.entrySet()) {
            String horario = entry.getValue();
            if (!horario.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]-([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                return false;
            }

            String[] partes = horario.split("-");
            if (partes.length == 2) {
                String[] inicio = partes[0].split(":");
                String[] fin = partes[1].split(":");
                int horaInicio = Integer.parseInt(inicio[0]) * 60 + Integer.parseInt(inicio[1]);
                int horaFin = Integer.parseInt(fin[0]) * 60 + Integer.parseInt(fin[1]);
                if (horaInicio >= horaFin) {
                    return false;
                }
            }
        }
        return true;
    }

    @AssertTrue(message = "Los días de disponibilidad deben ser válidos")
    public boolean isDiasValidos() {
        if (disponibilidad == null) return true;

        List<String> diasValidos = List.of("lunes", "martes", "miercoles", "jueves",
                "viernes", "sabado", "domingo");
        return disponibilidad.keySet().stream()
                .allMatch(dia -> diasValidos.contains(dia.toLowerCase()));
    }
}