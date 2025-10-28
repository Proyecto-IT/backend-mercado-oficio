package com.proyecto_it.mercado_oficio.Domain.Service.Notificacion;

import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Notificacion.NotificacionRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Notificacion.NotificacionResponse;

import java.util.List;

public interface NotificacionService {
    NotificacionResponse guardarNotificacion(NotificacionRequest request, String username);
    List<NotificacionResponse> obtenerUltimasNotificaciones(String username);
    void marcarComoLeida(Long id);
}
