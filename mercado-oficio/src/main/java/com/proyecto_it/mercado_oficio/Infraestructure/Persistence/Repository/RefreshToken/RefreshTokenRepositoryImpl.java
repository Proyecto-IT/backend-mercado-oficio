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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Repository
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final JpaRefreshTokenRepository jpaRepository;
    private final RefreshTokenMapper mapper;

    @Override
    @Transactional
    public RefreshToken guardar(RefreshToken token) {
        log.info("Guardando refresh token para usuarioId={}", token.getUsuarioId());

        log.info("TOKEN A GUARDAR: {}", token.getToken());

        RefreshTokenEntity entity = mapper.toEntity(token);
        RefreshTokenEntity savedEntity = jpaRepository.save(entity);

        log.info("Refresh token guardado con id={}", savedEntity.getId());
        log.info("TOKEN GUARDADO EN BD: {}", savedEntity.getToken());

        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> buscarPorTokenYEstado(String token, String estado) {
        log.info("Buscando refresh token '{}...' con estado '{}'",
                token.substring(0, Math.min(20, token.length())), estado);

        Optional<RefreshToken> result = jpaRepository.findByTokenAndEstadoForUpdate(token, estado)
                .map(mapper::toDomain);

        if (result.isPresent()) {
            log.info("Refresh token encontrado con id={}", result.get().getId());
        } else {
            log.warn("Refresh token NO encontrado para estado '{}'", estado);
        }

        return result;
    }

    @Override
    @Transactional
    public int expirarTokensPorUsuario(Integer usuarioId) {
        log.info("Expirando refresh tokens VALID para usuarioId={}", usuarioId);

        int updated = jpaRepository.expireTokensByUsuarioId(usuarioId);

        log.info("{} refresh tokens expirados para usuarioId={}", updated, usuarioId);

        return updated;
    }

    @Override
    @Transactional(readOnly = true)
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