package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Notificacion;

import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Notificacion.NotificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaNotificacionRepository extends JpaRepository<NotificacionEntity, Integer> {
    List<NotificacionEntity> findTop10ByUsuario_IdOrderByFechaCreacionDesc(Integer usuarioId);
}

