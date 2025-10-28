package com.proyecto_it.mercado_oficio.Domain.Service.Mensaje;

import com.proyecto_it.mercado_oficio.Domain.Model.Mensaje;
import com.proyecto_it.mercado_oficio.Domain.Model.Multimedia;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MensajeCacheProxy implements MensajeService {

    private final MensajeServiceImpl mensajeServiceImpl;
    private final MensajeCacheService cacheService;

    //@PostConstruct
    public void inicializarCache() {
        log.info("🚀 Inicializando caché de mensajes...");
        // El caché de mensajes se construye bajo demanda
        // No precargamos todos los chats al inicio por el volumen de datos
        log.info("ℹ️ Caché de mensajes configurado para carga bajo demanda");
    }

    /**
     * Enviar mensaje - siempre va a BD y actualiza caché
     */
    @Override
    public Mensaje enviarMensaje(Mensaje mensaje, List<MultipartFile> archivos) {
        log.info("📤 Enviando mensaje de {} a {}", mensaje.getEmisorId(), mensaje.getReceptorId());

        // Guardar en BD
        Mensaje mensajeGuardado = mensajeServiceImpl.enviarMensaje(mensaje, archivos);

        // Sincronizar caché
        cacheService.sincronizarDespuesDeEnviarMensaje(mensajeGuardado);

        return mensajeGuardado;
    }

    /**
     * Obtener mensaje por ID - intenta desde caché primero
     */
    @Override
    public Mensaje obtenerMensajePorId(Integer id) {
        log.info("🔍 Buscando mensaje con ID: {}", id);

        // Intentar obtener del caché
        if (cacheService.existeMensajeEnCache(id)) {
            log.info("✅ Mensaje {} encontrado en caché", id);
            return cacheService.obtenerMensajePorIdCached(id).orElse(null);
        }

        // Si no está en caché, obtener de BD
        log.info("⚠️ Mensaje {} no está en caché, consultando BD", id);
        Mensaje mensaje = mensajeServiceImpl.obtenerMensajePorId(id);

        // Cachear para próximas consultas
        cacheService.cachearMensaje(mensaje);

        return mensaje;
    }

    /**
     * Obtener mensajes de chat - usa caché si está disponible
     */
    @Override
    public List<Mensaje> obtenerMensajesDeChat(Integer usuario1Id, Integer usuario2Id) {
        log.info("💬 Obteniendo mensajes del chat entre {} y {}", usuario1Id, usuario2Id);

        // Intentar obtener del caché
        if (cacheService.existeChatEnCache(usuario1Id, usuario2Id)) {
            log.info("✅ Chat {}-{} encontrado en caché", usuario1Id, usuario2Id);
            List<Mensaje> mensajesCache = cacheService.obtenerMensajesChatCached(usuario1Id, usuario2Id);
            if (mensajesCache != null) {
                log.info("📊 Retornando {} mensajes desde caché", mensajesCache.size());
                return mensajesCache;
            }
        }

        // Si no está en caché, obtener de BD
        log.info("⚠️ Chat {}-{} no está en caché, consultando BD", usuario1Id, usuario2Id);
        List<Mensaje> mensajes = mensajeServiceImpl.obtenerMensajesDeChat(usuario1Id, usuario2Id);

        // Cachear para próximas consultas
        cacheService.cachearMensajesChat(usuario1Id, usuario2Id, mensajes);
        log.info("📊 {} mensajes cargados y cacheados", mensajes.size());

        return mensajes;
    }

    /**
     * Obtener archivos adjuntos de un mensaje
     */
    @Override
    public List<Multimedia> obtenerArchivosAdjuntos(Integer mensajeId) {
        log.info("📎 Obteniendo archivos adjuntos del mensaje: {}", mensajeId);
        return mensajeServiceImpl.obtenerArchivosAdjuntos(mensajeId);
    }

    /**
     * Obtener multimedia completo - usa caché si está disponible
     */
    public Multimedia obtenerMultimediaCompleto(Integer id) {
        log.info("🖼️ Obteniendo archivo multimedia con ID: {}", id);

        // Intentar obtener del caché
        if (cacheService.existeMultimediaEnCache(id)) {
            log.info("✅ Multimedia {} encontrado en caché", id);
            return cacheService.obtenerMultimediaPorIdCached(id).orElse(null);
        }

        // Si no está en caché, obtener de BD
        log.info("⚠️ Multimedia {} no está en caché, consultando BD", id);
        Multimedia multimedia = mensajeServiceImpl.obtenerMultimediaCompleto(id);

        // Cachear para próximas consultas
        cacheService.cachearMultimedia(multimedia);
        log.info("✅ Multimedia {} cacheado ({}, {})", id, multimedia.getNombre(),
                formatearTamano(multimedia.getTamano()));

        return multimedia;
    }

    /**
     * Eliminar mensaje - elimina de BD y limpia caché
     */
    @Override
    public void eliminarMensaje(Integer id) {
        log.info("🗑️ Eliminando mensaje con ID: {}", id);

        // Obtener información del mensaje antes de eliminarlo
        Mensaje mensaje = obtenerMensajePorId(id);

        // Eliminar de BD
        mensajeServiceImpl.eliminarMensaje(id);

        // Sincronizar caché
        cacheService.sincronizarDespuesDeEliminarMensaje(id, mensaje.getEmisorId(), mensaje.getReceptorId());
    }

    // ==================== MÉTODOS DE GESTIÓN DE CACHÉ ====================

    /**
     * Precarga un chat específico en caché
     */
    public void precargarChat(Integer usuario1Id, Integer usuario2Id) {
        log.info("⏳ Precargando chat entre {} y {}...", usuario1Id, usuario2Id);

        try {
            List<Mensaje> mensajes = mensajeServiceImpl.obtenerMensajesDeChat(usuario1Id, usuario2Id);
            cacheService.cachearMensajesChat(usuario1Id, usuario2Id, mensajes);
            log.info("✅ Chat precargado: {} mensajes", mensajes.size());
        } catch (Exception e) {
            log.error("❌ Error al precargar chat {}-{}: {}", usuario1Id, usuario2Id, e.getMessage(), e);
        }
    }

    /**
     * Precarga un archivo multimedia en caché
     */
    public void precargarMultimedia(Integer multimediaId) {
        log.info("⏳ Precargando multimedia {}...", multimediaId);

        try {
            Multimedia multimedia = mensajeServiceImpl.obtenerMultimediaCompleto(multimediaId);
            cacheService.cachearMultimedia(multimedia);
            log.info("✅ Multimedia {} precargado ({})", multimediaId, multimedia.getNombre());
        } catch (Exception e) {
            log.error("❌ Error al precargar multimedia {}: {}", multimediaId, e.getMessage(), e);
        }
    }

    /**
     * Limpia todo el caché de mensajes
     */
    public void limpiarCacheMensajes() {
        log.info("🧹 Limpiando todo el caché de mensajes...");
        cacheService.evictTodosLosMensajes();
        log.info("✅ Caché de mensajes limpiado");
    }

    /**
     * Limpia todo el caché de multimedia
     */
    public void limpiarCacheMultimedia() {
        log.info("🧹 Limpiando todo el caché de multimedia...");
        cacheService.evictTodosLosMultimedia();
        log.info("✅ Caché de multimedia limpiado");
    }

    /**
     * Invalida y recarga un chat específico
     */
    public void recargarChat(Integer usuario1Id, Integer usuario2Id) {
        log.info("🔄 Recargando chat entre {} y {}...", usuario1Id, usuario2Id);

        // Limpiar caché
        cacheService.evictMensajesChat(usuario1Id, usuario2Id);

        // Recargar
        precargarChat(usuario1Id, usuario2Id);
    }

    // ==================== UTILIDADES ====================

    private String formatearTamano(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}