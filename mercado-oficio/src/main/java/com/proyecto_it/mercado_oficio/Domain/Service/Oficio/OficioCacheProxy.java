package com.proyecto_it.mercado_oficio.Domain.Service.Oficio;

import com.proyecto_it.mercado_oficio.Domain.Model.Oficio;
import com.proyecto_it.mercado_oficio.Domain.Repository.OficioRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OficioCacheProxy {

    private final OficioRepository oficioRepository;
    private final OficioCacheService cacheService;
    //@PostConstruct
    public void inicializarCache() {
        log.info("=== Inicializando caché de oficios al arranque ===");
        precargarOficios();
    }
    public void precargarOficios() {
        try {
            List<Oficio> todosLosOficios = oficioRepository.findAll();

            log.info("Precargando {} oficios en caché...", todosLosOficios.size());

            for (Oficio oficio : todosLosOficios) {
                cacheService.cachearOficio(oficio);
            }
            log.info("Caché de oficios inicializado exitosamente con {} oficios", todosLosOficios.size());
        } catch (Exception e) {
            log.error("Error al inicializar caché de oficios: {}", e.getMessage(), e);
        }
    }

    public void recargarCacheCompleto() {
        log.info("Recargando caché completo de oficios desde BD...");

        cacheService.evictTodosLosOficios();

        precargarOficios();

        log.info("Caché recargado completamente");
    }

    public void precargarOficio(Integer id) {
        try {
            Optional<Oficio> oficioOpt = oficioRepository.buscarPorId(id);

            if (oficioOpt.isPresent()) {
                Oficio oficio = oficioOpt.get();
                cacheService.cachearOficio(oficio);
                log.info("Oficio precargado en caché: id={}, nombre={}", id, oficio.getNombre());
            } else {
                log.warn("No se encontró oficio con id={} para precargar", id);
            }
        } catch (Exception e) {
            log.error("Error al precargar oficio con id={}: {}", id, e.getMessage(), e);
        }
    }

    public void precargarOficioPorNombre(String nombre) {
        try {
            List<Oficio> oficios = oficioRepository.buscarPorNombre(nombre);

            if (!oficios.isEmpty()) {
                for (Oficio oficio : oficios) {
                    cacheService.cachearOficio(oficio);
                }
                log.info("{} oficio(s) con nombre '{}' precargado(s) en caché", oficios.size(), nombre);
            } else {
                log.warn("No se encontraron oficios con nombre '{}' para precargar", nombre);
            }
        } catch (Exception e) {
            log.error("Error al precargar oficios con nombre '{}': {}", nombre, e.getMessage(), e);
        }
    }
}