package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Review;

import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Review.ReviewPrestadorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaReviewPrestadorRepository extends JpaRepository<ReviewPrestadorEntity, Integer> {

    Optional<ReviewPrestadorEntity> findByReviewClienteId(Integer idReviewCliente);

    boolean existsByReviewClienteId(Integer idReviewCliente);
}
