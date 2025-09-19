package com.proyecto_it.mercado_oficio.Mapper.RefreshToken;

import com.proyecto_it.mercado_oficio.Domain.Model.RefreshToken;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.RefreshToken.RefreshTokenEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenMapper {

    public RefreshToken toDomain(RefreshTokenEntity entity) {
        if (entity == null) return null;

        Integer usuarioId = null;
        if (entity.getUsuario() != null) {
            try {
                usuarioId = entity.getUsuario().getId();
            } catch (Exception e) {
                // Si hay problemas con lazy loading, usuarioId será null
                usuarioId = null;
            }
        }

        return RefreshToken.builder()
                .id(entity.getId())
                .token(entity.getToken())
                .usuarioId(usuarioId)
                .fechaExpiracion(entity.getFechaExpiracion())
                .fechaCreacion(entity.getFechaCreacion())
                .estado(entity.getEstado())
                .build();
    }

    public RefreshTokenEntity toEntity(RefreshToken domain) {
        if (domain == null) return null;

        return RefreshTokenEntity.builder()
                .id(domain.getId())
                .token(domain.getToken())
                .fechaExpiracion(domain.getFechaExpiracion())
                .fechaCreacion(domain.getFechaCreacion())
                .estado(domain.getEstado())
                .build();
        // La referencia al usuario se establece manualmente en el repository
    }

    public RefreshToken toDomainWithUsuarioId(RefreshTokenEntity entity, Integer usuarioId) {
        if (entity == null) return null;

        return RefreshToken.builder()
                .id(entity.getId())
                .token(entity.getToken())
                .usuarioId(usuarioId)
                .fechaExpiracion(entity.getFechaExpiracion())
                .fechaCreacion(entity.getFechaCreacion())
                .estado(entity.getEstado())
                .build();
    }
}
