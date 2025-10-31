package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElegibilidadReviewResponse {
    private Boolean puedeRevisar;
    private String mensaje;
    private Integer idPresupuesto;
}