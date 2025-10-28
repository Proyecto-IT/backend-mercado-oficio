package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HitoDTO {
    private Integer id;
    private Integer presupuestoId;
    private Double porcentajePresupuesto;
    private Double monto;
    private String estado;
    private String fechaInicio;
    private String fechaFinalizacionEstimada;
}
