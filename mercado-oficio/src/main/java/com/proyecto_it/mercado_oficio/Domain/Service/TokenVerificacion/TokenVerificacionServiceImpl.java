package com.proyecto_it.mercado_oficio.Domain.Service.TokenVerificacion;

import com.proyecto_it.mercado_oficio.Domain.Model.TokenVerificacion;
import com.proyecto_it.mercado_oficio.Domain.Repository.TokenVerificacionRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TokenVerificacionServiceImpl implements TokenVerificacionService {

    private final TokenVerificacionRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public TokenVerificacion crearTokenParaUsuario(Integer usuarioId) {
        log.info("Creando token de verificación para usuario ID {}", usuarioId);

        usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> {
                    log.warn("No se puede crear token: usuario ID {} no encontrado", usuarioId);
                    return new IllegalArgumentException("Usuario no encontrado");
                });

        TokenVerificacion token = TokenVerificacion.crearNuevo(usuarioId, 24);

        TokenVerificacion tokenGuardado = tokenRepository.guardar(token);
        log.info("Token creado exitosamente para usuario ID {}: {}", usuarioId, token.getToken());
        return tokenGuardado;
    }


    @Override
    public Optional<TokenVerificacion> validarToken(String token) {
        log.info("Validando token: {}", token);

        Optional<TokenVerificacion> tokenOpt = tokenRepository.buscarPorToken(token)
                .filter(TokenVerificacion::esValido);

        if (tokenOpt.isPresent()) {
            log.info("Token '{}' válido para usuario ID {}", token, tokenOpt.get().getUsuarioId());
        } else {
            log.warn("Token '{}' inválido o expirado", token);
        }

        return tokenOpt;
    }
}
