package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Review;

import com.proyecto_it.mercado_oficio.Domain.Model.ReviewPrestador;
import com.proyecto_it.mercado_oficio.Domain.Repository.ReviewPrestadorRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Review.ReviewPrestadorEntity;
import com.proyecto_it.mercado_oficio.Mapper.Review.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewPrestadorRepositoryImpl implements ReviewPrestadorRepository {

    private final JpaReviewPrestadorRepository jpaRepository;
    private final ReviewMapper mapper;

    @Override
    public ReviewPrestador save(ReviewPrestador reviewPrestador) {
        ReviewPrestadorEntity entity = mapper.toEntity(reviewPrestador);
        ReviewPrestadorEntity saved = jpaRepository.save(entity);
        return mapper.toDomainPrestador(saved);
    }

    @Override
    public Optional<ReviewPrestador> findById(Integer id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomainPrestador);
    }

    @Override
    public Optional<ReviewPrestador> findByIdReviewCliente(Integer idReviewCliente) {
        return jpaRepository.findByReviewClienteId(idReviewCliente)
                .map(mapper::toDomainPrestador);
    }

    @Override
    public boolean existsByIdReviewCliente(Integer idReviewCliente) {
        return jpaRepository.existsByReviewClienteId(idReviewCliente);
    }
}
