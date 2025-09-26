package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Oficio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class OficioResponse {
    private Integer id;
    private String nombre;
}
