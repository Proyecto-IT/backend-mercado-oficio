package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Review;

import com.proyecto_it.mercado_oficio.Domain.Model.ReviewCliente;
import com.proyecto_it.mercado_oficio.Domain.Repository.ReviewClienteRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Review.ReviewClienteEntity;
import com.proyecto_it.mercado_oficio.Mapper.Review.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReviewClienteRepositoryImpl implements ReviewClienteRepository {

    private final JpaReviewClienteRepository jpaRepository;
    private final ReviewMapper mapper;

    @Override
    public ReviewCliente save(ReviewCliente reviewCliente) {
        ReviewClienteEntity entity = mapper.toEntity(reviewCliente);
        ReviewClienteEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ReviewCliente> findById(Integer id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<ReviewCliente> findByIdServicio(Integer idServicio) {
        return jpaRepository.findByIdServicioOrderByFechaDesc(idServicio).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReviewCliente> findByIdPresupuestoAndIdCliente(Integer idPresupuesto, Integer idCliente) {
        return jpaRepository.findByIdPresupuestoAndIdCliente(idPresupuesto, idCliente)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByIdPresupuestoAndIdCliente(Integer idPresupuesto, Integer idCliente) {
        return jpaRepository.existsByIdPresupuestoAndIdCliente(idPresupuesto, idCliente);
    }

    @Override
    public Double calcularPromedioValoracion(Integer idServicio) {
        return jpaRepository.calcularPromedioValoracion(idServicio);
    }

    @Override
    public Long contarReviewsPorServicio(Integer idServicio) {
        return jpaRepository.contarReviewsPorServicio(idServicio);
    }
}