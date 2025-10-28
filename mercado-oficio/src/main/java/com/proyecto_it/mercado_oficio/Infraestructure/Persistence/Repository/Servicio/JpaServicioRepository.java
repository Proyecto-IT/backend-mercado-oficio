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

    @Query("SELECT DISTINCT s FROM ServicioEntity s " +
            "LEFT JOIN FETCH s.usuario " +
            "WHERE s.id = :id")
    Optional<ServicioEntity> findByIdWithUsuario(@Param("id") Integer id);

    @Query("SELECT DISTINCT s FROM ServicioEntity s " +
            "LEFT JOIN FETCH s.usuario u " +
            "WHERE s.usuario.id = :usuarioId")
    List<ServicioEntity> findByUsuarioIdWithUsuario(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT DISTINCT s FROM ServicioEntity s " +
            "LEFT JOIN FETCH s.usuario " +
            "WHERE s.oficioId = :oficioId")
    List<ServicioEntity> findByOficioIdWithUsuario(@Param("oficioId") Integer oficioId);

    @Query("SELECT DISTINCT s FROM ServicioEntity s " +
            "LEFT JOIN FETCH s.usuario")
    List<ServicioEntity> findAllWithUsuarios();

    List<ServicioEntity> findByUsuarioId(Integer usuarioId);

    List<ServicioEntity> findByOficioId(Integer oficioId);
}