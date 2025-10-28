package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Hito;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoHito;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Hito.HitoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaHitoRepository extends JpaRepository<HitoEntity, Integer> {
    List<HitoEntity> findByPresupuestoId(Integer presupuestoId);

    @Query("SELECT h FROM HitoEntity h WHERE h.presupuesto.id = :presupuestoId")
    List<HitoEntity> findByPresupuestoIdOrdenado(@Param("presupuestoId") Integer presupuestoId);

    @Query("SELECT COUNT(h) FROM HitoEntity h WHERE h.presupuesto.id = :presupuestoId AND h.estado = :estado")
    Long countHitosPagados(@Param("presupuestoId") Integer presupuestoId,
                           @Param("estado") EstadoHito estado);


    @Query("SELECT h.presupuesto.idCliente FROM HitoEntity h WHERE h.id = :hitoId")
    Integer obtenerClienteId(@Param("hitoId") Integer hitoId);

    @Query("SELECT h.presupuesto.idPrestador FROM HitoEntity h WHERE h.id = :hitoId")
    Integer obtenerPrestadorId(@Param("hitoId") Integer hitoId);

    @Query("""
    SELECT DISTINCT h FROM HitoEntity h 
    JOIN FETCH h.presupuesto p 
    WHERE p.idCliente = :clienteId 
    ORDER BY p.id, h.fechaInicio
    """)
    List<HitoEntity> findByClienteId(@Param("clienteId") Integer clienteId);

}
