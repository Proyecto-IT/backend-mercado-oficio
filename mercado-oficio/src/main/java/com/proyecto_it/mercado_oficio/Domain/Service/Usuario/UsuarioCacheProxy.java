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

    //@PostConstruct
    public void inicializarCache() {
        log.info("🚀 Inicializando caché de usuarios...");
        precargarUsuarios();
    }

    /**
     * Precarga TODOS los usuarios en cache al inicio
     */
    public void precargarUsuarios() {
        try {
            List<Usuario> todosLosUsuarios = usuarioRepository.findAll();

            if (todosLosUsuarios.isEmpty()) {
                log.info("ℹ️ No hay usuarios para precargar en caché");
                return;
            }

            log.info("📊 Precargando {} usuarios en caché...", todosLosUsuarios.size());

            // 1. Cachear cada usuario individualmente (por ID y por Gmail)
            for (Usuario usuario : todosLosUsuarios) {
                cacheService.cachearUsuario(usuario);
            }

            // 2. Cachear lista completa
            cacheService.cachearTodosLosUsuarios(todosLosUsuarios);

            log.info("✅ Caché de usuarios inicializado exitosamente con {} usuarios",
                    todosLosUsuarios.size());

        } catch (Exception e) {
            log.error("❌ Error al inicializar caché de usuarios: {}", e.getMessage(), e);
        }
    }

    /**
     * Recarga todo el cache desde cero
     */
    public void recargarCacheCompleto() {
        log.info("🔄 Recargando caché completo de usuarios...");
        precargarUsuarios();
    }

    /**
     * Precarga un usuario específico
     */
    public void precargarUsuario(String gmail) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorGmail(gmail);
            if (usuarioOpt.isPresent()) {
                cacheService.cachearUsuario(usuarioOpt.get());
                log.info("✅ Usuario '{}' precargado en caché", gmail);
            } else {
                log.warn("⚠️ Usuario '{}' no encontrado para precargar", gmail);
            }
        } catch (Exception e) {
            log.error("❌ Error al precargar usuario '{}': {}", gmail, e.getMessage(), e);
        }
    }

    /**
     * Precarga un usuario por ID
     */
    public void precargarUsuarioPorId(int id) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorId(id);
            if (usuarioOpt.isPresent()) {
                cacheService.cachearUsuario(usuarioOpt.get());
                log.info("✅ Usuario con ID {} precargado en caché", id);
            } else {
                log.warn("⚠️ Usuario con ID {} no encontrado para precargar", id);
            }
        } catch (Exception e) {
            log.error("❌ Error al precargar usuario con ID {}: {}", id, e.getMessage(), e);
        }
    }
}