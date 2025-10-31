package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewClienteRequest {
    private Integer idServicio;
    private Integer idPresupuesto;
    private String comentario;
    private Integer valoracion;
}
