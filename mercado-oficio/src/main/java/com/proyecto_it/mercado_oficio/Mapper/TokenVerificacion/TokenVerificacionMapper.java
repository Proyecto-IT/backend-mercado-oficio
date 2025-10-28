package com.proyecto_it.mercado_oficio.Mapper.TokenVerificacion;

import com.proyecto_it.mercado_oficio.Domain.Model.TokenVerificacion;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.TokenVerificacion.TokenVerificacionEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class TokenVerificacionMapper{

    public TokenVerificacion toDomain(TokenVerificacionEntity entity) {
        if (entity == null) return null;

        Integer usuarioId = null;
        if (entity.getUsuario() != null) {
            try {
                usuarioId = entity.getUsuario().getId();
            } catch (Exception e) {
                usuarioId = null;
            }
        }

        return TokenVerificacion.builder()
                .id(entity.getId())
                .token(entity.getToken())
                .fechaExpiracion(entity.getFechaExpiracion())
                .fechaCreacion(entity.getFechaCreacion())
                .usuarioId(usuarioId)
                .usado(entity.getUsado() != null ? entity.getUsado() : false)
                .build();
    }

    public TokenVerificacionEntity toEntity(TokenVerificacion domain, UsuarioEntity usuario) {
        if (domain == null) return null;

        return TokenVerificacionEntity.builder()
                .id(domain.getId())
                .token(domain.getToken())
                .fechaExpiracion(domain.getFechaExpiracion())
                .fechaCreacion(domain.getFechaCreacion())
                .usado(domain.isUsado())
                .usuario(usuario)
                .build();
    }


    public TokenVerificacion toDomainWithUsuarioId(TokenVerificacionEntity entity, Integer usuarioId) {
        if (entity == null) return null;

        return TokenVerificacion.builder()
                .id(entity.getId())
                .token(entity.getToken())
                .fechaExpiracion(entity.getFechaExpiracion())
                .fechaCreacion(entity.getFechaCreacion())
                .usuarioId(usuarioId)
                .usado(entity.getUsado() != null ? entity.getUsado() : false)
                .build();
    }
}