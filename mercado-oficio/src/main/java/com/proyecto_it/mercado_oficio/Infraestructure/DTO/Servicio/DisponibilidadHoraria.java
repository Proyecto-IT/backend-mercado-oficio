package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadHoraria {
    private String dia; // "LUNES", "MARTES", etc.
    private String horaInicio; // "09:00"
    private String horaFin; // "17:00"
}
