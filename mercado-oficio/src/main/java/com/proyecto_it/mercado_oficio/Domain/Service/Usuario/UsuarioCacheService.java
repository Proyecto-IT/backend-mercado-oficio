package com.proyecto_it.mercado_oficio.Domain.Service.Usuario;


import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioCacheService {

    private final UsuarioRepository usuarioRepository;
    private final CacheManager cacheManager;

    // ==================== LECTURA CON CACHE ====================

    @Cacheable(value = "usuarios", key = "#gmail.toLowerCase()")
    public Optional<Usuario> buscarPorGmailCached(String gmail) {
        log.info("🔴 CACHE MISS - Consultando DB para usuario con gmail: {}", gmail);
        return usuarioRepository.buscarPorGmail(gmail);
    }

    @Cacheable(value = "usuariosPorId", key = "#id")
    public Optional<Usuario> buscarPorIdCached(int id) {
        log.info("🔴 CACHE MISS - Consultando DB para usuario con ID: {}", id);
        return usuarioRepository.buscarPorId(id);
    }

    @Cacheable(value = "todosLosUsuarios", key = "'ALL'")
    public List<Usuario> listarTodosCached() {
        log.info("🔴 CACHE MISS - Consultando DB para todos los usuarios");
        return usuarioRepository.findAll();
    }

    @Cacheable(value = "existeUsuario", key = "#gmail.toLowerCase()")
    public boolean existePorGmailCached(String gmail) {
        log.info("🔴 CACHE MISS - Verificando existencia en DB para gmail: {}", gmail);
        return usuarioRepository.existePorGmail(gmail);
    }

    // ==================== ACTUALIZACIÓN MANUAL ====================

    public void cachearUsuario(Usuario usuario) {
        // Cachear por ID
        Cache cachePorId = cacheManager.getCache("usuariosPorId");
        if (cachePorId != null) {
            cachePorId.put(usuario.getId(), Optional.of(usuario));
            log.info("✅ Usuario {} cacheado por ID", usuario.getId());
        }

        // Cachear por Gmail
        Cache cachePorGmail = cacheManager.getCache("usuarios");
        if (cachePorGmail != null) {
            cachePorGmail.put(usuario.getGmail().toLowerCase(), Optional.of(usuario));
            log.info("✅ Usuario {} cacheado por Gmail", usuario.getGmail());
        }

        // Marcar como existente
        Cache cacheExiste = cacheManager.getCache("existeUsuario");
        if (cacheExiste != null) {
            cacheExiste.put(usuario.getGmail().toLowerCase(), true);
        }
    }

    public void cachearTodosLosUsuarios(List<Usuario> usuarios) {
        Cache cache = cacheManager.getCache("todosLosUsuarios");
        if (cache != null) {
            cache.put("ALL", new ArrayList<>(usuarios));
            log.info("✅ {} usuarios cacheados en lista completa", usuarios.size());
        }
    }
    public void agregarUsuarioALista(Usuario usuario) {
        Cache cache = cacheManager.getCache("todosLosUsuarios");
        if (cache != null) {
            @SuppressWarnings("unchecked")
            List<Usuario> lista = (List<Usuario>) cache.get("ALL", List.class);

            if (lista != null) {
                boolean existe = lista.stream().anyMatch(u -> u.getId() == usuario.getId());
                if (!existe) {
                    lista.add(usuario);
                    cache.put("ALL", new ArrayList<>(lista));
                    log.info("✅ Usuario {} agregado a la lista cacheada de todos los usuarios", usuario.getId());
                } else {
                    log.info("ℹ️ Usuario {} ya estaba en la lista cacheada, no se agregó", usuario.getId());
                }
            }
        }
    }

    public void actualizarUsuarioEnLista(Usuario usuario) {
        Cache cache = cacheManager.getCache("todosLosUsuarios");
        if (cache != null) {
            @SuppressWarnings("unchecked")
            List<Usuario> lista = (List<Usuario>) cache.get("ALL", List.class);

            if (lista != null) {
                // Reemplazar usuario existente o agregarlo
                lista.removeIf(u -> u.getId() == usuario.getId());
                lista.add(usuario);
                cache.put("ALL", new ArrayList<>(lista));

                log.info("♻️ Usuario {} actualizado en lista cacheada", usuario.getId());
            }
        }
    }

    // ==================== EVICCIÓN ====================

    public void evictUsuarioPorId(int id) {
        Cache cache = cacheManager.getCache("usuariosPorId");
        if (cache != null) {
            cache.evict(id);
            log.info("🗑️ Usuario {} eliminado del cache (por ID)", id);
        }
    }

    public void evictUsuarioPorGmail(String gmail) {
        String gmailLower = gmail.toLowerCase();

        Cache cachePorGmail = cacheManager.getCache("usuarios");
        if (cachePorGmail != null) {
            cachePorGmail.evict(gmailLower);
        }

        Cache cacheExiste = cacheManager.getCache("existeUsuario");
        if (cacheExiste != null) {
            cacheExiste.evict(gmailLower);
        }

        log.info("🗑️ Usuario {} eliminado del cache (por Gmail)", gmail);
    }

    public void evictTodosLosUsuarios() {
        Cache cache = cacheManager.getCache("todosLosUsuarios");
        if (cache != null) {
            cache.clear();
            log.info("🗑️ Lista completa de usuarios eliminada del cache");
        }
    }

    // ==================== SINCRONIZACIÓN ====================

    public void sincronizarDespuesDeCrear(Usuario usuario) {
        cachearUsuario(usuario);
        evictTodosLosUsuarios();
        log.info("✅ Cache sincronizado después de crear usuario {}", usuario.getId());
    }

    public void sincronizarDespuesDeActualizar(Usuario usuario) {
        cachearUsuario(usuario);
        evictTodosLosUsuarios();
        log.info("✅ Cache sincronizado después de actualizar usuario {}", usuario.getId());
    }

    public void sincronizarDespuesDeEliminar(int id, String gmail) {
        evictUsuarioPorId(id);
        evictUsuarioPorGmail(gmail);
        evictTodosLosUsuarios();
        log.info("✅ Cache sincronizado después de eliminar usuario {}", id);
    }

    // ==================== VERIFICACIÓN ====================

    public boolean existeEnCachePorId(int id) {
        Cache cache = cacheManager.getCache("usuariosPorId");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(id);
            boolean existe = wrapper != null;
            log.info("🔍 Usuario {} en cache: {}", id, existe ? "✅ SÍ" : "❌ NO");
            return existe;
        }
        return false;
    }
}