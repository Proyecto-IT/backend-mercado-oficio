package com.proyecto_it.mercado_oficio.Domain.ValueObjects;


import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
public class Disponibilidad {
    Map<String, String> horarios;

    public Disponibilidad(Map<String, String> horarios) {
        if (horarios == null || horarios.isEmpty()) {
            throw new IllegalArgumentException("La disponibilidad no puede estar vacía");
        }

        // Validar formato de horarios (HH:mm-HH:mm)
        horarios.forEach((dia, horario) -> {
            if (!horario.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]-([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                throw new IllegalArgumentException("Formato de horario inválido para " + dia + ": " + horario);
            }
        });

        this.horarios = new HashMap<>(horarios);
    }

    public static Disponibilidad fromJson(String json) {
        // Implementar deserialización desde JSON
        return new Disponibilidad(new HashMap<>());
    }

    public String toJson() {
        // Implementar serialización a JSON
        return "";
    }
}
