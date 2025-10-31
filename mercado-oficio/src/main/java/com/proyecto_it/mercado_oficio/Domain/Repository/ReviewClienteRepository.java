package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.Model.ReviewCliente;

import java.util.List;
import java.util.Optional;

public interface ReviewClienteRepository {
    ReviewCliente save(ReviewCliente reviewCliente);
    Optional<ReviewCliente> findById(Integer id);
    List<ReviewCliente> findByIdServicio(Integer idServicio);
    Optional<ReviewCliente> findByIdPresupuestoAndIdCliente(Integer idPresupuesto, Integer idCliente);
    boolean existsByIdPresupuestoAndIdCliente(Integer idPresupuesto, Integer idCliente);
    Double calcularPromedioValoracion(Integer idServicio);
    Long contarReviewsPorServicio(Integer idServicio);
}