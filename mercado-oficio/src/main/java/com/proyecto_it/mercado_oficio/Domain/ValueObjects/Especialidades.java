package com.proyecto_it.mercado_oficio.Domain.ValueObjects;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class Especialidades {
    List<String> items;

    public Especialidades(List<String> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Debe tener al menos una especialidad");
        }

        if (items.size() > 10) {
            throw new IllegalArgumentException("No puede tener más de 10 especialidades");
        }

        for (String item : items) {
            if (item == null || item.trim().isEmpty()) {
                throw new IllegalArgumentException("Las especialidades no pueden estar vacías");
            }
            if (item.length() > 50) {
                throw new IllegalArgumentException("Cada especialidad no puede exceder 50 caracteres");
            }
        }

        this.items = new ArrayList<>(items);
    }
}
