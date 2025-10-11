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

    /**
     * Inicializa el cache al arrancar la aplicación.
     * Precarga todos los oficios en memoria para mejorar el rendimiento.
     */
    @PostConstruct
    public void inicializarCache() {
        log.info("=== Inicializando caché de oficios al arranque ===");
        precargarOficios();
    }

    /**
     * Precarga todos los oficios en el cache.
     * Este método consulta la BD y puebla el cache con todos los datos.
     */
    public void precargarOficios() {
        try {
            List<Oficio> todosLosOficios = oficioRepository.findAll();

            log.info("Precargando {} oficios en caché...", todosLosOficios.size());

            // Precargar cada oficio individualmente (por nombre e ID)
            for (Oficio oficio : todosLosOficios) {
                cacheService.actualizarOficioEnCache(oficio);
            }

            // La lista completa se cacheará automáticamente en el primer GET
            // No necesitamos forzar el cacheo aquí porque @Cacheable lo hará

            log.info("✓ Caché de oficios inicializado exitosamente con {} oficios", todosLosOficios.size());
        } catch (Exception e) {
            log.error("✗ Error al inicializar caché de oficios: {}", e.getMessage(), e);
        }
    }

    /**
     * Recarga completamente el cache desde la base de datos.
     * Útil después de cambios masivos en la BD o para sincronización manual.
     */
    public void recargarCacheCompleto() {
        log.info("Recargando caché completo de oficios desde BD...");

        // Limpiar todo el cache antes de recargar
        cacheService.invalidarListaCompleta();

        // Precargar nuevamente
        precargarOficios();

        log.info("✓ Caché recargado completamente");
    }

    /**
     * Precarga un oficio específico en el cache por su ID.
     *
     * @param id ID del oficio a precargar
     */
    public void precargarOficio(Integer id) {
        try {
            Optional<Oficio> oficioOpt = oficioRepository.buscarPorId(id);

            if (oficioOpt.isPresent()) {
                Oficio oficio = oficioOpt.get();
                cacheService.actualizarOficioEnCache(oficio);
                log.info("✓ Oficio precargado en caché: id={}, nombre={}", id, oficio.getNombre());
            } else {
                log.warn("⚠ No se encontró oficio con id={} para precargar", id);
            }
        } catch (Exception e) {
            log.error("✗ Error al precargar oficio con id={}: {}", id, e.getMessage(), e);
        }
    }

    /**
     * Precarga un oficio específico en el cache por su nombre.
     *
     * @param nombre Nombre del oficio a precargar
     */
    public void precargarOficioPorNombre(String nombre) {
        try {
            List<Oficio> oficios = oficioRepository.buscarPorNombre(nombre);

            if (!oficios.isEmpty()) {
                for (Oficio oficio : oficios) {
                    cacheService.actualizarOficioEnCache(oficio);
                }
                log.info("✓ {} oficio(s) con nombre '{}' precargado(s) en caché", oficios.size(), nombre);
            } else {
                log.warn("⚠ No se encontraron oficios con nombre '{}' para precargar", nombre);
            }
        } catch (Exception e) {
            log.error("✗ Error al precargar oficios con nombre '{}': {}", nombre, e.getMessage(), e);
        }
    }

    /**
     * Obtiene estadísticas del cache (si Caffeine está configurado con recordStats()).
     * Útil para monitoreo y debugging.
     */
    public void mostrarEstadisticasCache() {
        log.info("=== Estadísticas del Caché de Oficios ===");
        log.info("Para ver estadísticas detalladas, asegúrate de tener .recordStats() en CacheConfig");
        log.info("y usa un CacheManager que soporte métricas");
    }
}