package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Mensaje;

import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Mensaje.MensajeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaMensajeRepository extends JpaRepository<MensajeEntity, Integer> {

    @Query("SELECT m FROM MensajeEntity m WHERE " +
            "(m.emisorId = :usuario1Id AND m.receptorId = :usuario2Id) OR " +
            "(m.emisorId = :usuario2Id AND m.receptorId = :usuario1Id) " +
            "ORDER BY m.fechaEnvio ASC")
    List<MensajeEntity> findByChat(@Param("usuario1Id") Integer usuario1Id,
                                   @Param("usuario2Id") Integer usuario2Id);

    List<MensajeEntity> findByEmisorIdOrderByFechaEnvioDesc(Integer emisorId);

    List<MensajeEntity> findByReceptorIdOrderByFechaEnvioDesc(Integer receptorId);
}