package com.proyecto_it.mercado_oficio.Infraestructure.DTO.TrustlessWork;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiberarFondosResponseDTO {
    private Integer hitoId;
    private Double monto;
    private String mensaje;
    private String estado;
}
