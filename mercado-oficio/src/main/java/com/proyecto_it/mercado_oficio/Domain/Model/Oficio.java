package com.proyecto_it.mercado_oficio.Domain.Model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Oficio {
    private Integer id;
    private String nombre;

    public void validar(){
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
    }
}
