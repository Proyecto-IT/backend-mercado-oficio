package com.proyecto_it.mercado_oficio.Domain.ValueObjects;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.HorarioServicioDTO;
import lombok.Value;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Value
public class Disponibilidad {
    Map<String, String> horarios;

    public Disponibilidad(Map<String, String> horarios) {
        if (horarios == null || horarios.isEmpty()) {
            throw new IllegalArgumentException("La disponibilidad no puede estar vacía");
        }

        // Validar formato de horarios (HH:mm-HH:mm o múltiples rangos separados por comas)
        horarios.forEach((dia, horario) -> {
            String rangoPattern = "([0-1]?[0-9]|2[0-3]):[0-5][0-9]-([0-1]?[0-9]|2[0-3]):[0-5][0-9]";
            String multipleRangosPattern = "^" + rangoPattern + "(," + rangoPattern + ")*$";

            if (!horario.matches(multipleRangosPattern)) {
                throw new IllegalArgumentException("Formato de horario inválido para " + dia + ": " + horario);
            }
        });

        this.horarios = new HashMap<>(horarios);
    }

    public String getValor(String dia) {
        return horarios.get(dia);
    }

    // Convertir JSON a Disponibilidad
    public static Disponibilidad fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> map = mapper.readValue(json, new TypeReference<Map<String, String>>() {});
            return new Disponibilidad(map);
        } catch (Exception e) {
            throw new RuntimeException("Error parseando disponibilidad desde JSON", e);
        }
    }

    // Convertir Disponibilidad a JSON
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(horarios);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando disponibilidad a JSON", e);
        }
    }

    public List<HorarioServicioDTO> toHorariosDTO() {
        return horarios.entrySet().stream()
                .flatMap(e -> {
                    String[] rangos = e.getValue().split(",");
                    return Arrays.stream(rangos).map(rango -> {
                        String[] partes = rango.trim().split("-");
                        LocalTime inicio = partes.length > 0 ? LocalTime.parse(partes[0]) : null;
                        LocalTime fin = partes.length > 1 ? LocalTime.parse(partes[1]) : null;

                        Double duracion = (inicio != null && fin != null)
                                ? (double) Duration.between(inicio, fin).toMinutes() / 60
                                : null;

                        HorarioServicioDTO dto = new HorarioServicioDTO();
                        dto.setId(null);
                        dto.setFecha(null);
                        dto.setHoraInicio(inicio);
                        dto.setHoraFin(fin);
                        dto.setDuracionHoras(duracion);

                        return dto;
                    });
                })
                .collect(Collectors.toList());
    }
}