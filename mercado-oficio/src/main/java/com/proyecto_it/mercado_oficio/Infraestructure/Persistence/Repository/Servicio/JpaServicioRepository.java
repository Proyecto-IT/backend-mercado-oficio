package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Servicio;

import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.ServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaServicioRepository extends JpaRepository<ServicioEntity, Integer> {

    List<ServicioEntity> findByUsuarioId(Integer usuarioId);

    List<ServicioEntity> findByOficioId(Integer oficioId);

    boolean existsByUsuarioId(Integer usuarioId);

    @Query("SELECT s FROM ServicioEntity s " +
            "JOIN FETCH s.usuario u " +
            "WHERE s.id = :id")
    Optional<ServicioEntity> findByIdWithUsuario(@Param("id") Integer id);

    @Query("SELECT DISTINCT s FROM ServicioEntity s " +
            "LEFT JOIN FETCH s.usuario u " +
            "LEFT JOIN FETCH s.portafolios p " +
            "WHERE s.id = :id")
    Optional<ServicioEntity> findByIdWithDetails(@Param("id") Integer id);


}
