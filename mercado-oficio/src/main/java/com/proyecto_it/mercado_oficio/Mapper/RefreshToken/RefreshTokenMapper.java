package com.proyecto_it.mercado_oficio.Mapper.RefreshToken;

import com.proyecto_it.mercado_oficio.Domain.Model.RefreshToken;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.RefreshToken.RefreshTokenEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.RefreshToken.JpaRefreshTokenRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenMapper {

    private final JpaUsuarioRepository usuarioRepository;
    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;
    /**
     * Convierte de entidad JPA a dominio
     */
    public RefreshToken toDomain(RefreshTokenEntity entity) {
        if (entity == null) {
            return null;
        }

        return RefreshToken.builder()
                .id(entity.getId())
                .token(entity.getToken())
                .usuarioId(entity.getUsuario() != null ? entity.getUsuario().getId() : null)
                .fechaCreacion(entity.getFechaCreacion())
                .fechaExpiracion(entity.getFechaExpiracion())
                .estado(entity.getEstado())
                .build();
    }

    /**
     * Convierte de dominio a entidad JPA
     * 🔥 IMPORTANTE: Si el token tiene ID, carga la entidad existente para actualizar
     */
    public RefreshTokenEntity toEntity(RefreshToken domain) {
        if (domain == null) {
            return null;
        }

        RefreshTokenEntity entity;

        // 🔥 Si el token ya existe (tiene ID), actualizar la entidad existente
        if (domain.getId() != null) {
            entity = jpaRefreshTokenRepository.findById(domain.getId())
                    .orElseGet(RefreshTokenEntity::new);
        } else {
            entity = new RefreshTokenEntity();
        }

        // Mapear campos básicos
        entity.setToken(domain.getToken());
        entity.setFechaCreacion(domain.getFechaCreacion() != null ? domain.getFechaCreacion() : LocalDateTime.now());
        entity.setFechaExpiracion(domain.getFechaExpiracion());
        entity.setEstado(domain.getEstado());

        // 🔥 Establecer la referencia al usuario si se proporciona usuarioId
        if (domain.getUsuarioId() != null) {
            // Usar getReferenceById para evitar carga innecesaria de la entidad completa
            UsuarioEntity usuario = usuarioRepository.getReferenceById(domain.getUsuarioId());
            entity.setUsuario(usuario);
        }

        return entity;
    }

    /**
     * Convierte una lista de entidades a dominios
     */
    public List<RefreshToken> toDomainList(List<RefreshTokenEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
}