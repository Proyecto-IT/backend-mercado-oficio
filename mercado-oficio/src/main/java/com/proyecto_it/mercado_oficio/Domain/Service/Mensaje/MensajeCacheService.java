package com.proyecto_it.mercado_oficio.Domain.Service.Mensaje;

import com.proyecto_it.mercado_oficio.Domain.Model.Mensaje;
import com.proyecto_it.mercado_oficio.Domain.Model.Multimedia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MensajeCacheService {

    private final CacheManager cacheManager;

    // ==================== LECTURA CON CACHE ====================

    /**
     * Obtiene mensajes de un chat desde caché
     * La clave se genera ordenando los IDs para que sea consistente
     */
    @Cacheable(value = "mensajesChat", key = "T(java.lang.Math).min(#usuario1Id, #usuario2Id) + '-' + T(java.lang.Math).max(#usuario1Id, #usuario2Id)")
    public List<Mensaje> obtenerMensajesChatCached(Integer usuario1Id, Integer usuario2Id) {
        log.info("🔴 CACHE MISS - No hay datos en caché para chat {}-{}", usuario1Id, usuario2Id);
        // Este método será llamado por el proxy cuando no haya caché
        return null;
    }

    /**
     * Obtiene un mensaje específico desde caché
     */
    @Cacheable(value = "mensajesPorId", key = "#mensajeId")
    public Optional<Mensaje> obtenerMensajePorIdCached(Integer mensajeId) {
        log.info("🔴 CACHE MISS - Consultando DB para mensaje con ID: {}", mensajeId);
        return Optional.empty();
    }

    /**
     * Obtiene un archivo multimedia desde caché
     */
    @Cacheable(value = "multimediaPorId", key = "#multimediaId")
    public Optional<Multimedia> obtenerMultimediaPorIdCached(Integer multimediaId) {
        log.info("🔴 CACHE MISS - Consultando DB para multimedia con ID: {}", multimediaId);
        return Optional.empty();
    }

    // ==================== ACTUALIZACIÓN MANUAL ====================

    /**
     * Cachea los mensajes de un chat completo
     */
    public void cachearMensajesChat(Integer usuario1Id, Integer usuario2Id, List<Mensaje> mensajes) {
        String cacheKey = generarCacheKeyChat(usuario1Id, usuario2Id);
        Cache cache = cacheManager.getCache("mensajesChat");
        if (cache != null) {
            cache.put(cacheKey, new ArrayList<>(mensajes));
            log.info("✅ {} mensajes del chat {}-{} cacheados", mensajes.size(), usuario1Id, usuario2Id);
        }
    }

    /**
     * Cachea un mensaje individual
     */
    public void cachearMensaje(Mensaje mensaje) {
        Cache cache = cacheManager.getCache("mensajesPorId");
        if (cache != null) {
            cache.put(mensaje.getId(), Optional.of(mensaje));
            log.info("✅ Mensaje {} cacheado por ID", mensaje.getId());
        }
    }

    /**
     * Cachea un archivo multimedia
     */
    public void cachearMultimedia(Multimedia multimedia) {
        Cache cache = cacheManager.getCache("multimediaPorId");
        if (cache != null) {
            cache.put(multimedia.getId(), Optional.of(multimedia));
            log.info("✅ Multimedia {} cacheado ({})", multimedia.getId(), multimedia.getNombre());
        }
    }

    /**
     * Agrega un mensaje nuevo a la lista cacheada de un chat
     */
    public void agregarMensajeAChat(Integer usuario1Id, Integer usuario2Id, Mensaje mensaje) {
        String cacheKey = generarCacheKeyChat(usuario1Id, usuario2Id);
        Cache cache = cacheManager.getCache("mensajesChat");

        if (cache != null) {
            @SuppressWarnings("unchecked")
            List<Mensaje> mensajes = (List<Mensaje>) cache.get(cacheKey, List.class);

            if (mensajes != null) {
                // Verificar que no exista
                boolean existe = mensajes.stream().anyMatch(m -> m.getId().equals(mensaje.getId()));
                if (!existe) {
                    mensajes.add(mensaje);
                    cache.put(cacheKey, new ArrayList<>(mensajes));
                    log.info("✅ Mensaje {} agregado al chat {}-{} en caché", mensaje.getId(), usuario1Id, usuario2Id);
                } else {
                    log.info("ℹ️ Mensaje {} ya estaba en caché del chat {}-{}", mensaje.getId(), usuario1Id, usuario2Id);
                }
            } else {
                log.info("ℹ️ No hay caché del chat {}-{}, se debe cargar completo", usuario1Id, usuario2Id);
            }
        }
    }

    // ==================== EVICCIÓN ====================

    /**
     * Elimina del caché los mensajes de un chat
     */
    public void evictMensajesChat(Integer usuario1Id, Integer usuario2Id) {
        String cacheKey = generarCacheKeyChat(usuario1Id, usuario2Id);
        Cache cache = cacheManager.getCache("mensajesChat");
        if (cache != null) {
            cache.evict(cacheKey);
            log.info("🗑️ Mensajes del chat {}-{} eliminados del caché", usuario1Id, usuario2Id);
        }
    }

    /**
     * Elimina un mensaje específico del caché
     */
    public void evictMensaje(Long mensajeId) {
        Cache cache = cacheManager.getCache("mensajesPorId");
        if (cache != null) {
            cache.evict(mensajeId);
            log.info("🗑️ Mensaje {} eliminado del caché", mensajeId);
        }
    }

    /**
     * Elimina un archivo multimedia del caché
     */
    public void evictMultimedia(Integer multimediaId) {
        Cache cache = cacheManager.getCache("multimediaPorId");
        if (cache != null) {
            cache.evict(multimediaId);
            log.info("🗑️ Multimedia {} eliminado del caché", multimediaId);
        }
    }

    /**
     * Limpia todo el caché de mensajes
     */
    public void evictTodosLosMensajes() {
        Cache cacheChat = cacheManager.getCache("mensajesChat");
        if (cacheChat != null) {
            cacheChat.clear();
            log.info("🗑️ Todos los chats eliminados del caché");
        }

        Cache cacheMensajes = cacheManager.getCache("mensajesPorId");
        if (cacheMensajes != null) {
            cacheMensajes.clear();
            log.info("🗑️ Todos los mensajes individuales eliminados del caché");
        }
    }

    /**
     * Limpia todo el caché de multimedia
     */
    public void evictTodosLosMultimedia() {
        Cache cache = cacheManager.getCache("multimediaPorId");
        if (cache != null) {
            cache.clear();
            log.info("🗑️ Todos los archivos multimedia eliminados del caché");
        }
    }

    // ==================== SINCRONIZACIÓN ====================

    /**
     * Sincroniza el caché después de enviar un mensaje
     */
    public void sincronizarDespuesDeEnviarMensaje(Mensaje mensaje) {
        // Cachear el mensaje individual
        cachearMensaje(mensaje);

        // Agregar a la lista del chat si existe en caché
        agregarMensajeAChat(mensaje.getEmisorId(), mensaje.getReceptorId(), mensaje);

        log.info("✅ Caché sincronizado después de enviar mensaje {}", mensaje.getId());
    }

    /**
     * Sincroniza el caché después de eliminar un mensaje
     */
    public void sincronizarDespuesDeEliminarMensaje(Long mensajeId, Integer usuario1Id, Integer usuario2Id) {
        // Eliminar mensaje individual
        evictMensaje(mensajeId);

        // Invalidar el chat completo para que se recargue
        evictMensajesChat(usuario1Id, usuario2Id);

        log.info("✅ Caché sincronizado después de eliminar mensaje {}", mensajeId);
    }

    // ==================== VERIFICACIÓN ====================

    /**
     * Verifica si un chat está en caché
     */
    public boolean existeChatEnCache(Integer usuario1Id, Integer usuario2Id) {
        String cacheKey = generarCacheKeyChat(usuario1Id, usuario2Id);
        Cache cache = cacheManager.getCache("mensajesChat");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(cacheKey);
            boolean existe = wrapper != null;
            log.info("🔍 Chat {}-{} en caché: {}", usuario1Id, usuario2Id, existe ? "✅ SÍ" : "❌ NO");
            return existe;
        }
        return false;
    }

    /**
     * Verifica si un mensaje está en caché
     */
    public boolean existeMensajeEnCache(Integer mensajeId) {
        Cache cache = cacheManager.getCache("mensajesPorId");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(mensajeId);
            boolean existe = wrapper != null;
            log.info("🔍 Mensaje {} en caché: {}", mensajeId, existe ? "✅ SÍ" : "❌ NO");
            return existe;
        }
        return false;
    }

    /**
     * Verifica si un archivo multimedia está en caché
     */
    public boolean existeMultimediaEnCache(Integer multimediaId) {
        Cache cache = cacheManager.getCache("multimediaPorId");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(multimediaId);
            boolean existe = wrapper != null;
            log.info("🔍 Multimedia {} en caché: {}", multimediaId, existe ? "✅ SÍ" : "❌ NO");
            return existe;
        }
        return false;
    }

    // ==================== UTILIDADES ====================

    /**
     * Genera la clave de caché para un chat ordenando los IDs
     */
    private String generarCacheKeyChat(Integer usuario1Id, Integer usuario2Id) {
        int menor = Math.min(usuario1Id, usuario2Id);
        int mayor = Math.max(usuario1Id, usuario2Id);
        return menor + "-" + mayor;
    }
}