package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Review;

import com.proyecto_it.mercado_oficio.Domain.Model.ReviewCliente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewsResponse {
    private List<ReviewCliente> reviews;
    private Double promedioValoracion;
    private Integer totalReviews;
}