package com.proyecto_it.mercado_oficio.Domain.Service.Oficio;

import java.util.List;
import java.util.Optional;

import com.proyecto_it.mercado_oficio.Domain.Model.Oficio;
import com.proyecto_it.mercado_oficio.Domain.Repository.OficioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;


@Service
@RequiredArgsConstructor
@Slf4j
public class OficioCacheService {

    private final OficioRepository oficioRepository;
    private final CacheManager cacheManager;

    // ==================== LECTURA CON CACHE ====================

    @Cacheable(value = "oficios", key = "#nombre.toLowerCase()")
    public List<Oficio> buscarPorNombreCached(String nombre) {
        log.info("🔴 CACHE MISS - Consultando DB para oficio con nombre: {}", nombre);
        return oficioRepository.buscarPorNombre(nombre);
    }

    @Cacheable(value = "oficiosPorId", key = "#id")
    public Optional<Oficio> buscarPorIdCached(Integer id) {
        log.info("🔴 CACHE MISS - Consultando DB para oficio con ID: {}", id);
        return oficioRepository.buscarPorId(id);
    }

    @Cacheable(value = "todosLosOficios", key = "'ALL'")
    public List<Oficio> listarTodosCached() {
        log.info("🔴 CACHE MISS - Consultando DB para todos los oficios");
        return oficioRepository.findAll();
    }

    // ==================== ACTUALIZACIÓN MANUAL ====================

    public void cachearOficio(Oficio oficio) {
        // Cachear por ID
        Cache cachePorId = cacheManager.getCache("oficiosPorId");
        if (cachePorId != null) {
            cachePorId.put(oficio.getId(), Optional.of(oficio));
            log.info("✅ Oficio {} ({}) cacheado por ID", oficio.getId(), oficio.getNombre());
        }

        // Cachear por nombre
        Cache cachePorNombre = cacheManager.getCache("oficios");
        if (cachePorNombre != null) {
            cachePorNombre.put(oficio.getNombre().toLowerCase(), List.of(oficio));
            log.info("✅ Oficio {} cacheado por nombre", oficio.getNombre());
        }
    }

    public void cachearTodosLosOficios(List<Oficio> oficios) {
        Cache cache = cacheManager.getCache("todosLosOficios");
        if (cache != null) {
            cache.put("ALL", new ArrayList<>(oficios));
            log.info("✅ {} oficios cacheados en lista completa", oficios.size());
        }
    }
    public void actualizarOficioEnCache(Oficio oficio) {
        // === Actualizar cache por ID ===
        Cache cachePorId = cacheManager.getCache("oficiosPorId");
        if (cachePorId != null) {
            cachePorId.put(oficio.getId(), Optional.of(oficio));
            log.info("♻️ Oficio {} ({}) actualizado en cache por ID", oficio.getId(), oficio.getNombre());
        }

        // === Actualizar cache por nombre ===
        Cache cachePorNombre = cacheManager.getCache("oficios");
        if (cachePorNombre != null) {
            cachePorNombre.put(oficio.getNombre().toLowerCase(), List.of(oficio));
            log.info("♻️ Oficio {} actualizado en cache por nombre", oficio.getNombre());
        }

        // === Actualizar lista completa si ya está cacheada ===
        Cache cacheLista = cacheManager.getCache("todosLosOficios");
        if (cacheLista != null) {
            @SuppressWarnings("unchecked")
            List<Oficio> lista = (List<Oficio>) cacheLista.get("ALL", List.class);
            if (lista != null) {
                boolean existe = false;
                for (int i = 0; i < lista.size(); i++) {
                    if (lista.get(i).getId().equals(oficio.getId())) {
                        lista.set(i, oficio);
                        existe = true;
                        break;
                    }
                }
                if (!existe) {
                    lista.add(oficio);
                }
                cacheLista.put("ALL", new ArrayList<>(lista));
                log.info("♻️ Oficio {} sincronizado en la lista completa cacheada", oficio.getId());
            }
        }
    }

    // ==================== EVICCIÓN ====================

    public void evictOficioPorId(Integer id) {
        Cache cache = cacheManager.getCache("oficiosPorId");
        if (cache != null) {
            cache.evict(id);
            log.info("🗑️ Oficio {} eliminado del cache (por ID)", id);
        }
    }

    public void evictOficioPorNombre(String nombre) {
        Cache cache = cacheManager.getCache("oficios");
        if (cache != null) {
            cache.evict(nombre.toLowerCase());
            log.info("🗑️ Oficio {} eliminado del cache (por nombre)", nombre);
        }
    }

    public void evictTodosLosOficios() {
        Cache cache = cacheManager.getCache("todosLosOficios");
        if (cache != null) {
            cache.clear();
            log.info("🗑️ Lista completa de oficios eliminada del cache");
        }
    }

    // ==================== SINCRONIZACIÓN ====================

    public void sincronizarDespuesDeCrear(Oficio oficio) {
        cachearOficio(oficio);
        evictTodosLosOficios();
        log.info("✅ Cache sincronizado después de crear oficio {}", oficio.getId());
    }

    public void sincronizarDespuesDeActualizar(Oficio oficio) {
        cachearOficio(oficio);
        evictTodosLosOficios();
        log.info("✅ Cache sincronizado después de actualizar oficio {}", oficio.getId());
    }

    public void sincronizarDespuesDeEliminar(Integer id, String nombre) {
        evictOficioPorId(id);
        evictOficioPorNombre(nombre);
        evictTodosLosOficios();
        log.info("✅ Cache sincronizado después de eliminar oficio {}", id);
    }

    // ==================== VERIFICACIÓN ====================

    public boolean existeEnCachePorId(Integer id) {
        Cache cache = cacheManager.getCache("oficiosPorId");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(id);
            boolean existe = wrapper != null;
            log.info("🔍 Oficio {} en cache: {}", id, existe ? "✅ SÍ" : "❌ NO");
            return existe;
        }
        return false;
    }
}