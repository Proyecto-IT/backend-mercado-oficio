package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.TokenVerificacion;

import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.TokenVerificacion.TokenVerificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaTokenVerificacionRepository extends JpaRepository<TokenVerificacionEntity, Long> {
    Optional<TokenVerificacionEntity> findByToken(String token);

    @Query("SELECT t FROM TokenVerificacionEntity t WHERE t.token = :token AND t.usado = false")
    Optional<TokenVerificacionEntity> findByTokenAndNotUsed(@Param("token") String token);

    @Transactional
    @Modifying
    @Query("DELETE FROM TokenVerificacionEntity t WHERE t.fechaExpiracion < :fecha")
    void deleteByFechaExpiracionBefore(@Param("fecha") LocalDateTime fecha);

    @Transactional
    @Modifying
    @Query("DELETE FROM TokenVerificacionEntity t WHERE t.usuario.id = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Integer usuarioId);

    List<TokenVerificacionEntity> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);
}

