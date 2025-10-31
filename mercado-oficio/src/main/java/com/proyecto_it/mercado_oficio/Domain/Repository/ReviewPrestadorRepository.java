package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.Model.ReviewPrestador;

import java.util.Optional;

public interface ReviewPrestadorRepository {
    ReviewPrestador save(ReviewPrestador reviewPrestador);
    Optional<ReviewPrestador> findById(Integer id);
    Optional<ReviewPrestador> findByIdReviewCliente(Integer idReviewCliente);
    boolean existsByIdReviewCliente(Integer idReviewCliente);
}
