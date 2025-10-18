package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Presupuesto.PresupuestoArchivo;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.TipoArchivo;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto.PresupuestoArchivo.PresupuestoArchivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaPresupuestoArchivoRepository extends JpaRepository<PresupuestoArchivoEntity, Integer> {
    List<PresupuestoArchivoEntity> findByPresupuestoServicioId(Integer presupuestoServicioId);
    long countByPresupuestoServicioIdAndTipoArchivo(Integer presupuestoServicioId, TipoArchivo tipoArchivo);
    @Query("SELECT new com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoDTO(" +
            "a.id, a.nombreArchivo, a.tipoMime, a.tipoArchivo, a.tamanioMb, a.fechaCarga) " +
            "FROM PresupuestoArchivoEntity a " +
            "WHERE a.presupuestoServicio.id = :presupuestoId")
    List<PresupuestoArchivoDTO> findArchivosByPresupuestoId(@Param("presupuestoId") Integer presupuestoId);

}
