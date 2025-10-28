package com.proyecto_it.mercado_oficio.Mapper.Notificacion;

import com.proyecto_it.mercado_oficio.Domain.Model.Notificacion;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Notificacion.NotificacionEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificacionMapper {

    public Notificacion toDomain(NotificacionEntity entity) {
        if (entity == null) return null;
        return Notificacion.builder()
                .id(entity.getId())
                .mensaje(entity.getMensaje())
                .tipo(entity.getTipo())
                .destinoUrl(entity.getDestinoUrl())
                .leida(entity.isLeida())
                .fechaCreacion(entity.getFechaCreacion())
                .usuarioDestinoId(entity.getUsuarioDestino() != null ? entity.getUsuarioDestino().getId() : null)
                .build();
    }

    public NotificacionEntity toEntity(Notificacion domain, UsuarioEntity usuario) {
        if (domain == null) return null;
        return NotificacionEntity.builder()
                .id(domain.getId())
                .mensaje(domain.getMensaje())
                .tipo(domain.getTipo())
                .destinoUrl(domain.getDestinoUrl())
                .leida(domain.isLeida())
                .fechaCreacion(domain.getFechaCreacion())
                .usuarioDestino(usuario)
                .build();
    }
}
