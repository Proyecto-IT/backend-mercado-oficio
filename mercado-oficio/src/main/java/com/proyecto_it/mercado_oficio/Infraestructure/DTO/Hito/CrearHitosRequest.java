package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito;

import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.HorarioServicioDTO;
import lombok.Data;

import java.util.List;
@Data
public class CrearHitosRequest {
    private Integer presupuestoId;
    private List<HorarioServicioDTO> horariosSeleccionados;
}
