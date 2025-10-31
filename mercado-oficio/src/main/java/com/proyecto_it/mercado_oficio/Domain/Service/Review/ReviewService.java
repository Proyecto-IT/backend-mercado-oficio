package com.proyecto_it.mercado_oficio.Domain.Service.Review;

import com.proyecto_it.mercado_oficio.Domain.Model.ReviewCliente;
import com.proyecto_it.mercado_oficio.Domain.Model.ReviewPrestador;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Review.CreateReviewClienteRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Review.CreateReviewPrestadorRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Review.ElegibilidadReviewResponse;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Review.ReviewsResponse;

public interface ReviewService {


    ElegibilidadReviewResponse verificarElegibilidadParaRevisar(
            Integer idCliente,
            Integer idServicio,
            Integer idPresupuesto
    );
    ReviewCliente crearReviewCliente(Integer idCliente, CreateReviewClienteRequest request);
    ReviewPrestador crearRespuestaPrestador(Integer idPrestador, CreateReviewPrestadorRequest request);
    ReviewsResponse obtenerReviewsPorServicio(Integer idServicio);
    ReviewCliente obtenerReviewPorId(Integer idReview);
}