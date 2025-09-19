package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.RefreshToken;

import com.proyecto_it.mercado_oficio.Domain.Model.RefreshToken;
import com.proyecto_it.mercado_oficio.Domain.Repository.RefreshTokenRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.RefreshToken.RefreshTokenEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import com.proyecto_it.mercado_oficio.Mapper.RefreshToken.RefreshTokenMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final JpaRefreshTokenRepository jpaRepository;
    private final RefreshTokenMapper mapper;
    private final JpaUsuarioRepository usuarioRepository;

    @Override
    public RefreshToken guardar(RefreshToken token) {
        log.info("Guardando refresh token para usuarioId={}", token.getUsuarioId());

        RefreshTokenEntity entity = mapper.toEntity(token);

        // Establecer referencia al usuario si no existe
        if (entity.getUsuario() == null && token.getUsuarioId() != null) {
            UsuarioEntity usuarioRef = usuarioRepository.getReferenceById(token.getUsuarioId());
            entity.setUsuario(usuarioRef);
            log.debug("Referencia al usuario establecida para token: {}", token.getUsuarioId());
        }

        RefreshTokenEntity savedEntity = jpaRepository.save(entity);
        log.info("Refresh token guardado con id={}", savedEntity.getId());
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<RefreshToken> buscarPorTokenYEstado(String token, String estado) {
        log.info("Buscando refresh token '{}' con estado '{}'", token, estado);
        Optional<RefreshToken> result = jpaRepository.findByTokenAndEstadoForUpdate(token, estado)
                .map(mapper::toDomain);

        if (result.isPresent()) {
            log.info("Refresh token encontrado para token '{}'", token);
        } else {
            log.warn("Refresh token no encontrado para token '{}' con estado '{}'", token, estado);
        }

        return result;
    }

    @Override
    public void expirarTokensPorUsuario(Integer usuarioId) {
        log.info("Expirando refresh tokens para usuarioId={}", usuarioId);
        jpaRepository.expireTokensByUsuarioId(usuarioId);
        log.info("Refresh tokens expirados para usuarioId={}", usuarioId);
    }

    @Override
    public List<RefreshToken> buscarPorUsuarioYEstado(Integer usuarioId, String estado) {
        log.info("Buscando refresh tokens para usuarioId={} con estado={}", usuarioId, estado);
        List<RefreshToken> tokens = jpaRepository.findAllByUsuarioIdAndEstado(usuarioId, estado)
                .stream()
                .map(mapper::toDomain)
                .toList();

        log.info("{} refresh tokens encontrados para usuarioId={}", tokens.size(), usuarioId);
        return tokens;
    }
}
