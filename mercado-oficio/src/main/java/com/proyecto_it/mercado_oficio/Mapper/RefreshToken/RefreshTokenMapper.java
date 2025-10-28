package com.proyecto_it.mercado_oficio.Mapper.RefreshToken;

import com.proyecto_it.mercado_oficio.Domain.Model.RefreshToken;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.RefreshToken.RefreshTokenEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.RefreshToken.JpaRefreshTokenRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public RefreshTokenEntity toEntity(RefreshToken domain) {
        if (domain == null) {
            return null;
        }

        RefreshTokenEntity entity;

        if (domain.getId() != null) {
            entity = jpaRefreshTokenRepository.findById(domain.getId())
                    .orElseGet(RefreshTokenEntity::new);
        } else {
            entity = new RefreshTokenEntity();
        }

        entity.setToken(domain.getToken());
        entity.setFechaCreacion(domain.getFechaCreacion() != null ? domain.getFechaCreacion() : LocalDateTime.now());
        entity.setFechaExpiracion(domain.getFechaExpiracion());
        entity.setEstado(domain.getEstado());

        if (domain.getUsuarioId() != null) {
            // Usar getReferenceById para evitar carga innecesaria de la entidad completa
            UsuarioEntity usuario = usuarioRepository.getReferenceById(domain.getUsuarioId());
            entity.setUsuario(usuario);
        }

        return entity;
    }

    public List<RefreshToken> toDomainList(List<RefreshTokenEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
}