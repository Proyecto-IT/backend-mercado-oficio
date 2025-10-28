package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HitoResponseDTO {
    private Integer id;
    private Integer presupuestoId;
    private Double porcentajePresupuesto;
    private Double monto;
    private String estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFinalizacionEstimada;
    private LocalDateTime fechaCompletado;



    public static HitoResponseDTO fromDTO(HitoDTO dto) {
        return HitoResponseDTO.builder()
                .id(dto.getId())
                .presupuestoId(dto.getPresupuestoId())
                .porcentajePresupuesto(dto.getPorcentajePresupuesto())
                .monto(dto.getMonto())
                .estado(dto.getEstado())
                .build();
    }
}
