package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.TokenVerificacion;

import com.proyecto_it.mercado_oficio.Domain.Model.TokenVerificacion;
import com.proyecto_it.mercado_oficio.Domain.Repository.TokenVerificacionRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.TokenVerificacion.TokenVerificacionEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import com.proyecto_it.mercado_oficio.Mapper.TokenVerificacion.TokenVerificacionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TokenVerificacionRepositoryImpl implements TokenVerificacionRepository {

    private final JpaTokenVerificacionRepository jpaRepository;
    private final TokenVerificacionMapper mapper;
    private final JpaUsuarioRepository usuarioRepository; // Para crear referencias

    @Override
    public TokenVerificacion guardar(TokenVerificacion token) {
        log.info("Guardando token de verificación para usuarioId={}", token.getUsuarioId());

        // Obtener referencia al usuario
        UsuarioEntity usuarioRef = usuarioRepository.getReferenceById(token.getUsuarioId());

        // Mapear directamente incluyendo el usuario
        TokenVerificacionEntity entity = mapper.toEntity(token, usuarioRef);

        TokenVerificacionEntity savedEntity = jpaRepository.save(entity);
        log.info("Token de verificación guardado con id={}", savedEntity.getId());
        return mapper.toDomain(savedEntity);
    }


    @Override
    public Optional<TokenVerificacion> buscarPorToken(String token) {
        log.info("Buscando token de verificación: {}", token);
        Optional<TokenVerificacion> result = jpaRepository.findByToken(token)
                .map(mapper::toDomain);

        if (result.isPresent()) {
            log.info("Token de verificación encontrado: {}", token);
        } else {
            log.warn("Token de verificación NO encontrado: {}", token);
        }

        return result;
    }

    @Override
    public Optional<TokenVerificacion> buscarPorTokenValido(String token) {
        log.info("Buscando token de verificación válido: {}", token);
        Optional<TokenVerificacion> result = jpaRepository.findByTokenAndNotUsed(token)
                .filter(entity -> entity.getFechaExpiracion().isAfter(LocalDateTime.now()))
                .map(mapper::toDomain);

        if (result.isPresent()) {
            log.info("Token de verificación válido encontrado: {}", token);
        } else {
            log.warn("Token de verificación inválido o expirado: {}", token);
        }

        return result;
    }

    @Override
    @Transactional
    public void eliminarTokensExpirados() {
        log.info("Eliminando tokens de verificación expirados");
        jpaRepository.deleteByFechaExpiracionBefore(LocalDateTime.now());
        log.info("Tokens de verificación expirados eliminados");
    }

    @Override
    @Transactional
    public void eliminarTokensPorUsuario(Integer usuarioId) {
        log.info("Eliminando tokens de verificación para usuarioId={}", usuarioId);
        jpaRepository.deleteByUsuarioId(usuarioId);
        log.info("Tokens de verificación eliminados para usuarioId={}", usuarioId);
    }

    @Override
    public List<TokenVerificacion> buscarPorUsuario(Integer usuarioId) {
        log.info("Buscando tokens de verificación para usuarioId={}", usuarioId);
        List<TokenVerificacion> tokens = jpaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(mapper::toDomain)
                .toList();
        log.info("{} tokens encontrados para usuarioId={}", tokens.size(), usuarioId);
        return tokens;
    }
}
