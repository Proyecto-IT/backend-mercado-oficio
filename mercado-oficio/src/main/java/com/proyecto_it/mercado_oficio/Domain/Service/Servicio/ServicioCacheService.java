package com.proyecto_it.mercado_oficio.Domain.Service.Servicio;

import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;
import com.proyecto_it.mercado_oficio.Domain.Repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class ServicioCacheService {

    private final ServicioRepository servicioRepository;
    private final CacheManager cacheManager;

    // ==================== MÉTODOS DE LECTURA CON CACHE ====================

    @Cacheable(value = "serviciosPorId", key = "#id")
    public Optional<Servicio> obtenerServicioPorIdCached(Integer id) {
        log.info("🔴 CACHE MISS - Consultando DB para servicio ID: {}", id);
        return servicioRepository.findByIdWithDetails(id);
    }

    @Cacheable(value = "serviciosPorUsuario", key = "#usuarioId")
    public List<Servicio> obtenerServiciosPorUsuarioCached(Integer usuarioId) {
        log.info("🔴 CACHE MISS - Consultando DB para servicios del usuario: {}", usuarioId);
        return servicioRepository.findByUsuarioId(usuarioId);
    }

    @Cacheable(value = "serviciosPorOficio", key = "#oficioId")
    public List<Servicio> obtenerServiciosPorOficioCached(Integer oficioId) {
        log.info("🔴 CACHE MISS - Consultando DB para servicios del oficio: {}", oficioId);
        return servicioRepository.findByOficioId(oficioId);
    }

    @Cacheable(value = "todosLosServicios", key = "'ALL'")
    public List<Servicio> obtenerTodosLosServiciosCached() {
        log.info("🔴 CACHE MISS - Consultando DB para todos los servicios");
        return servicioRepository.findAll();
    }

    // ==================== ACTUALIZACIÓN MANUAL DE CACHE ====================

    public void cachearServicio(Servicio servicio) {
        // 🔥 VALIDACIÓN CRÍTICA: Caffeine no permite valores null
        if (servicio == null) {
            log.warn("⚠️ No se puede cachear servicio nulo");
            return;
        }

        if (servicio.getId() == null) {
            log.warn("⚠️ No se puede cachear servicio sin ID: {}", servicio);
            return;
        }

        Cache cache = cacheManager.getCache("serviciosPorId");
        if (cache != null) {
            cache.put(servicio.getId(), Optional.of(servicio));
            log.info("✅ Servicio {} cacheado manualmente", servicio.getId());
        }
    }

    public void cachearServiciosPorUsuario(Integer usuarioId, List<Servicio> servicios) {
        if (usuarioId == null) {
            log.warn("⚠️ No se puede cachear con usuarioId nulo");
            return;
        }

        Cache cache = cacheManager.getCache("serviciosPorUsuario");
        if (cache != null) {
            cache.put(usuarioId, new ArrayList<>(servicios != null ? servicios : List.of()));
            log.info("✅ Servicios del usuario {} cacheados manualmente ({})",
                    usuarioId, servicios != null ? servicios.size() : 0);
        }
    }

    public void cachearServiciosPorOficio(Integer oficioId, List<Servicio> servicios) {
        if (oficioId == null) {
            log.warn("⚠️ No se puede cachear con oficioId nulo");
            return;
        }

        Cache cache = cacheManager.getCache("serviciosPorOficio");
        if (cache != null) {
            cache.put(oficioId, new ArrayList<>(servicios != null ? servicios : List.of()));
            log.info("✅ Servicios del oficio {} cacheados manualmente ({})",
                    oficioId, servicios != null ? servicios.size() : 0);
        }
    }

    public void cachearTodosLosServicios(List<Servicio> servicios) {
        Cache cache = cacheManager.getCache("todosLosServicios");
        if (cache != null) {
            cache.put("ALL", new ArrayList<>(servicios != null ? servicios : List.of()));
            log.info("✅ Todos los servicios cacheados manualmente ({})",
                    servicios != null ? servicios.size() : 0);
        }
    }

    // ==================== EVICCIÓN MANUAL ====================

    public void evictServicioPorId(Integer servicioId) {
        if (servicioId == null) {
            log.warn("⚠️ No se puede evict con servicioId nulo");
            return;
        }

        Cache cache = cacheManager.getCache("serviciosPorId");
        if (cache != null) {
            cache.evict(servicioId);
            log.info("🗑️ Servicio {} eliminado del cache", servicioId);
        }
    }

    public void evictServiciosPorUsuario(Integer usuarioId) {
        if (usuarioId == null) {
            log.warn("⚠️ No se puede evict con usuarioId nulo");
            return;
        }

        Cache cache = cacheManager.getCache("serviciosPorUsuario");
        if (cache != null) {
            cache.evict(usuarioId);
            log.info("🗑️ Servicios del usuario {} eliminados del cache", usuarioId);
        }
    }

    public void evictServiciosPorOficio(Integer oficioId) {
        if (oficioId == null) {
            log.warn("⚠️ No se puede evict con oficioId nulo");
            return;
        }

        Cache cache = cacheManager.getCache("serviciosPorOficio");
        if (cache != null) {
            cache.evict(oficioId);
            log.info("🗑️ Servicios del oficio {} eliminados del cache", oficioId);
        }
    }

    public void evictTodosLosServicios() {
        Cache cache = cacheManager.getCache("todosLosServicios");
        if (cache != null) {
            cache.clear();
            log.info("🗑️ Todos los servicios eliminados del cache");
        }
    }

    // ==================== VERIFICACIÓN DE CACHE ====================

    public boolean existeEnCache(Integer servicioId) {
        if (servicioId == null) {
            return false;
        }

        Cache cache = cacheManager.getCache("serviciosPorId");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(servicioId);
            boolean existe = wrapper != null;
            log.info("🔍 Verificando cache para servicio {}: {}",
                    servicioId, existe ? "✅ EXISTE" : "❌ NO EXISTE");
            return existe;
        }
        return false;
    }

    // ==================== SINCRONIZACIÓN INTELIGENTE ====================

    public void sincronizarDespuesDeCrear(Servicio servicio) {
        if (servicio == null || servicio.getId() == null) {
            log.warn("⚠️ No se puede sincronizar servicio nulo o sin ID");
            return;
        }

        cachearServicio(servicio);
        evictServiciosPorUsuario(servicio.getUsuarioId());
        evictServiciosPorOficio(servicio.getOficioId());
        evictTodosLosServicios();
        log.info("✅ Cache sincronizado después de crear servicio {}", servicio.getId());
    }

    public void sincronizarDespuesDeActualizar(Servicio servicioAnterior, Servicio servicioNuevo) {
        if (servicioNuevo == null || servicioNuevo.getId() == null) {
            log.warn("⚠️ No se puede sincronizar servicio nulo o sin ID");
            return;
        }

        cachearServicio(servicioNuevo);
        evictServiciosPorUsuario(servicioNuevo.getUsuarioId());

        if (servicioAnterior != null &&
                !servicioAnterior.getOficioId().equals(servicioNuevo.getOficioId())) {
            evictServiciosPorOficio(servicioAnterior.getOficioId());
            evictServiciosPorOficio(servicioNuevo.getOficioId());
        } else {
            evictServiciosPorOficio(servicioNuevo.getOficioId());
        }

        evictTodosLosServicios();
        log.info("✅ Cache sincronizado después de actualizar servicio {}", servicioNuevo.getId());
    }

    public void sincronizarDespuesDeEliminar(Servicio servicio) {
        if (servicio == null) {
            log.warn("⚠️ No se puede sincronizar eliminación de servicio nulo");
            return;
        }

        evictServicioPorId(servicio.getId());
        evictServiciosPorUsuario(servicio.getUsuarioId());
        evictServiciosPorOficio(servicio.getOficioId());
        evictTodosLosServicios();
        log.info("✅ Cache sincronizado después de eliminar servicio {}", servicio.getId());
    }
}