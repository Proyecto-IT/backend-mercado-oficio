package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.RefreshToken;


import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.RefreshToken.RefreshTokenEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    // ✅ Para actualización (usa en refrescar tokens)
    @Query("SELECT rt FROM RefreshTokenEntity rt WHERE rt.token = :token AND rt.estado = :estado")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshTokenEntity> findByTokenAndEstadoForUpdate(
            @Param("token") String token,
            @Param("estado") String estado
    );

    // ✅ Invalidación masiva - CORREGIDO para retornar cantidad
    @Modifying
    @Transactional
    @Query("UPDATE RefreshTokenEntity rt SET rt.estado = 'EXPIRED' " +
            "WHERE rt.usuario.id = :usuarioId AND rt.estado = 'VALID'")
    int expireTokensByUsuarioId(@Param("usuarioId") Integer usuarioId);

    // ✅ Búsqueda simple (sin lock)
    Optional<RefreshTokenEntity> findByTokenAndEstado(String token, String estado);

    // ✅ Búsqueda por usuario
    @Query("SELECT rt FROM RefreshTokenEntity rt WHERE rt.usuario.id = :usuarioId AND rt.estado = :estado")
    List<RefreshTokenEntity> findAllByUsuarioIdAndEstado(
            @Param("usuarioId") Integer usuarioId,
            @Param("estado") String estado
    );

    // ✅ Para limpieza automática
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.estado = 'EXPIRED' " +
            "AND rt.fechaExpiracion < :fecha")
    int deleteExpiredTokens(@Param("fecha") LocalDateTime fecha);
}