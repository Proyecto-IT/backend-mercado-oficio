package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Hito;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoAprobacion;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Hito.ModificacionHitoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaModificacionHitoRepository extends JpaRepository<ModificacionHitoEntity, Integer> {
    List<ModificacionHitoEntity> findByHitoId(Integer hitoId);

    @Query("SELECT m FROM ModificacionHitoEntity m WHERE m.hito.id = :hitoId AND m.estadoAprobacion = :estado")
    List<ModificacionHitoEntity> findModificacionesPendientes(
            @Param("hitoId") Integer hitoId,
            @Param("estado") EstadoAprobacion estado
    );


}
