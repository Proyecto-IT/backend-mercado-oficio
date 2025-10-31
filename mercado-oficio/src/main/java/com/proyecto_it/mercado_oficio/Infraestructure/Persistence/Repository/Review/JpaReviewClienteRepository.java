package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Review;

import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Review.ReviewClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaReviewClienteRepository extends JpaRepository<ReviewClienteEntity, Integer> {

    List<ReviewClienteEntity> findByIdServicioOrderByFechaDesc(Integer idServicio);

    Optional<ReviewClienteEntity> findByIdPresupuestoAndIdCliente(Integer idPresupuesto, Integer idCliente);

    boolean existsByIdPresupuestoAndIdCliente(Integer idPresupuesto, Integer idCliente);

    @Query("SELECT AVG(r.valoracion) FROM ReviewClienteEntity r WHERE r.idServicio = :idServicio")
    Double calcularPromedioValoracion(@Param("idServicio") Integer idServicio);

    @Query("SELECT COUNT(r) FROM ReviewClienteEntity r WHERE r.idServicio = :idServicio")
    Long contarReviewsPorServicio(@Param("idServicio") Integer idServicio);
}
