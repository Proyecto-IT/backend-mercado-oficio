package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Presupuesto;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoPresupuesto;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto.PresupuestoServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface JpaPresupuestoServicioRepository extends JpaRepository<PresupuestoServicioEntity, Integer> {

    @Query("SELECT new com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioDTO(" +
            "p.id, s.id, c.id, c.nombre, c.apellido, " +
            "pr.id, pr.nombre, pr.apellido, p.descripcionProblema, p.fechaCreacion, " +
            "p.estado, s.tarifaHora, p.respondido, p.presupuesto, p.descripcionSolucion, " +
            "p.fechaActualizacion, p.costoMateriales, p.horasEstimadas, s.disponibilidad) " +
            "FROM PresupuestoServicioEntity p " +
            "LEFT JOIN p.servicio s " +
            "LEFT JOIN UsuarioEntity c ON c.id = p.idCliente " +
            "LEFT JOIN UsuarioEntity pr ON pr.id = s.usuario.id " +
            "WHERE p.id = :id")
    Optional<PresupuestoServicioDTO> findDTOById(@Param("id") Integer id);




    @Query("""
    SELECT new com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioDTO(
        p.id,
        p.servicio.id,
        c.id,
        c.nombre,
        c.apellido,
        pr.id,
        pr.nombre,
        pr.apellido,
        p.descripcionProblema,
        p.fechaCreacion,
        p.estado,
        s.tarifaHora,
        p.respondido,
        p.presupuesto,
        p.descripcionSolucion,
        p.fechaActualizacion,
        p.costoMateriales,
        p.horasEstimadas
    )
    FROM PresupuestoServicioEntity p
    LEFT JOIN p.servicio s
    LEFT JOIN UsuarioEntity c ON c.id = p.idCliente
    LEFT JOIN UsuarioEntity pr ON pr.id = p.idPrestador
    WHERE p.id = :id
""")
    Optional<PresupuestoServicioDTO> findDTOByIdWithPrestador(@Param("id") Integer id);


    @Query("""
    SELECT new com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioDTO(
        p.id, p.servicio.id, p.idCliente,
        u1.nombre, u1.apellido,
        p.idPrestador, u2.nombre, u2.apellido,
        p.descripcionProblema, p.fechaCreacion,
        p.estado, p.servicio.tarifaHora, p.respondido,
        p.presupuesto, p.descripcionSolucion, p.fechaActualizacion,
        p.costoMateriales, p.horasEstimadas,
        p.servicio.disponibilidad
    )
    FROM PresupuestoServicioEntity p
    LEFT JOIN UsuarioEntity u1 ON p.idCliente = u1.id
    LEFT JOIN UsuarioEntity u2 ON p.idPrestador = u2.id
    WHERE p.idCliente = :idCliente
""")
    List<PresupuestoServicioDTO> findByIdCliente(@Param("idCliente") Integer idCliente);


    @Query("SELECT DISTINCT p FROM PresupuestoServicioEntity p LEFT JOIN FETCH p.archivos WHERE p.idPrestador = :idPrestador")
    List<PresupuestoServicioEntity> findByIdPrestador(@Param("idPrestador") Integer idPrestador);

    @Query("SELECT DISTINCT p FROM PresupuestoServicioEntity p LEFT JOIN FETCH p.archivos WHERE p.estado = :estado")
    List<PresupuestoServicioEntity> findByEstado(@Param("estado") EstadoPresupuesto estado);

    List<PresupuestoServicioEntity> findByServicioId(Integer servicioId); // si no necesitas archivos, lo dejás así

    @Query("SELECT p FROM PresupuestoServicioEntity p WHERE p.id = :id AND p.respondido = true")
    Optional<PresupuestoServicioEntity> findByIdRespondido(@Param("id") Integer id);
}
