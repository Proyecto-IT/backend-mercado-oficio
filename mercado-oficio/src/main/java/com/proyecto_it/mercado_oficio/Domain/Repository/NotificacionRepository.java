package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.Model.Notificacion;
import java.util.List;

public interface NotificacionRepository {
    List<Notificacion> obtenerPorUsuario(Integer usuarioId);
    Notificacion guardar(Notificacion notificacion);
    void marcarComoLeida(Integer notificacionId);
    void eliminarPorId(Integer id);
}
