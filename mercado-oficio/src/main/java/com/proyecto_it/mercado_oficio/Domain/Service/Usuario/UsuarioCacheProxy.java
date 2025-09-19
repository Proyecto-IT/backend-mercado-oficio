package com.proyecto_it.mercado_oficio.Domain.Service.Usuario;


import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UsuarioCacheProxy {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioCacheService cacheService;

    @PostConstruct
    public void inicializarCache() {
        log.info("Inicializando caché de usuarios...");
        precargarUsuarios();
    }

    public void precargarUsuarios() {
        try {
            List<Usuario> todosLosUsuarios = usuarioRepository.findAll();
            cacheService.actualizarListaCompleta(todosLosUsuarios);

            for (Usuario usuario : todosLosUsuarios) {
                cacheService.actualizarUsuarioEnCache(usuario);
            }

            log.info("Caché de usuarios inicializado con {} usuarios", todosLosUsuarios.size());
        } catch (Exception e) {
            log.error("Error al inicializar caché de usuarios: {}", e.getMessage(), e);
        }
    }

    public void recargarCacheCompleto() {
        log.info("Recargando caché completo de usuarios...");
        precargarUsuarios();
    }

    public void precargarUsuario(String gmail) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorGmail(gmail);
            if (usuarioOpt.isPresent()) {
                cacheService.actualizarUsuarioEnCache(usuarioOpt.get());
                log.info("Usuario '{}' precargado en caché correctamente", gmail);
            } else {
                log.warn("Usuario '{}' no encontrado para precargar en caché", gmail);
            }
        } catch (Exception e) {
            log.error("Error al precargar usuario '{}': {}", gmail, e.getMessage(), e);
        }
    }
}

