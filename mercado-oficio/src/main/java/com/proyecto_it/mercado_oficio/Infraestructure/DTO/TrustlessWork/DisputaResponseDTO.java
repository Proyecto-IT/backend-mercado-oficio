package com.proyecto_it.mercado_oficio.Infraestructure.DTO.TrustlessWork;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputaResponseDTO {
    private Integer hitoId;
    private String motivo;
    private String mensaje;
    private String estado;
}
