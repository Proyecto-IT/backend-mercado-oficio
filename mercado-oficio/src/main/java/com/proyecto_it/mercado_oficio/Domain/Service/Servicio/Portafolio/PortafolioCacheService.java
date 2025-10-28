package com.proyecto_it.mercado_oficio.Domain.Service.Servicio.Portafolio;

import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;
import com.proyecto_it.mercado_oficio.Domain.Repository.PortafolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortafolioCacheService {

    private final PortafolioRepository portafolioRepository;
    private final CacheManager cacheManager;

    @Cacheable(value = "portafoliosPorServicio", key = "#servicioId")
    public List<Portafolio> obtenerPortafoliosPorServicioCached(Integer servicioId) {
        log.info("Consultando DB para portafolios del servicio: {}", servicioId);
        return portafolioRepository.findByServicioId(servicioId);
    }

    public void cachearPortafolios(Integer servicioId, List<Portafolio> portafolios) {
        Cache cache = cacheManager.getCache("portafoliosPorServicio");
        if (cache != null) {
            cache.put(servicioId, new ArrayList<>(portafolios));
            log.info("{} portafolios del servicio {} cacheados", portafolios.size(), servicioId);
        }
    }
    public void cachearListaVacia(Integer servicioId) {
        Cache cache = cacheManager.getCache("portafoliosPorServicio");
        if (cache != null) {
            cache.put(servicioId, new ArrayList<>());
            log.info("Lista vacía de portafolios cacheada para servicio {}", servicioId);
        }
    }

    public void evictPortafoliosPorServicio(Integer servicioId) {
        Cache cache = cacheManager.getCache("portafoliosPorServicio");
        if (cache != null) {
            cache.evict(servicioId);
            log.info("Portafolios del servicio {} eliminados del cache", servicioId);
        }
    }

    public void evictTodos() {
        Cache cache = cacheManager.getCache("portafoliosPorServicio");
        if (cache != null) {
            cache.clear();
            log.info("Todo el cache de portafolios eliminado");
        }
    }

    public void sincronizarDespuesDeGuardar(Integer servicioId) {
        evictPortafoliosPorServicio(servicioId);
        log.info("Cache de portafolios sincronizado para servicio {}", servicioId);
    }

    public boolean existeEnCache(Integer servicioId) {
        Cache cache = cacheManager.getCache("portafoliosPorServicio");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(servicioId);
            boolean existe = wrapper != null;
            log.info("Verificando cache de portafolios para servicio {}: {}",
                    servicioId, existe ? "EXISTE" : "NO EXISTE");
            return existe;
        }
        return false;
    }
}