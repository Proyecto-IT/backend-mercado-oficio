package com.proyecto_it.mercado_oficio.Domain.Service.Servicio;

import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;
import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;
import com.proyecto_it.mercado_oficio.Domain.Repository.PortafolioRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.ServicioRepository;
import com.proyecto_it.mercado_oficio.Domain.Service.Oficio.OficioCacheService;
import com.proyecto_it.mercado_oficio.Domain.Service.Servicio.Portafolio.PortafolioCacheService;
import com.proyecto_it.mercado_oficio.Domain.Service.Usuario.UsuarioCacheService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
@Slf4j
public class ServicioCacheProxy {

    private final ServicioRepository servicioRepository;
    private final ServicioCacheService cacheService;
    private final PortafolioRepository portafolioRepository;
    private final PortafolioCacheService portafolioCacheService;
    private final UsuarioCacheService usuarioCacheService;
    private final OficioCacheService oficioCacheService;

    //@PostConstruct
    public void inicializarCache() {
        log.info("🚀 Inicializando caché de servicios...");
        precargarServicios();
    }

    /**
     * Pre-carga todos los servicios Y sus dependencias en cache
     */
    public void precargarServicios() {
        try {
            // 1. Obtener todos los servicios de la DB (CON usuarios y oficios)
            List<Servicio> todosLosServicios = servicioRepository.findAll();

            if (todosLosServicios.isEmpty()) {
                log.info("ℹ️ No hay servicios para precargar en caché");
                return;
            }

            // 2. Extraer IDs únicos de usuarios y oficios
            Set<Integer> usuariosIds = todosLosServicios.stream()
                    .map(Servicio::getUsuarioId)
                    .collect(Collectors.toSet());

            Set<Integer> oficiosIds = todosLosServicios.stream()
                    .map(Servicio::getOficioId)
                    .collect(Collectors.toSet());

            log.info("📊 Precargando: {} servicios, {} usuarios, {} oficios",
                    todosLosServicios.size(), usuariosIds.size(), oficiosIds.size());

            // 3. Precargar usuarios en cache
            for (Integer usuarioId : usuariosIds) {
                usuarioCacheService.buscarPorIdCached(usuarioId);
            }
            log.info("✅ {} usuarios precargados", usuariosIds.size());

            // 4. Precargar oficios en cache
            for (Integer oficioId : oficiosIds) {
                oficioCacheService.buscarPorIdCached(oficioId);
            }
            log.info("✅ {} oficios precargados", oficiosIds.size());

            // 5. Cachear cada servicio individual
            for (Servicio servicio : todosLosServicios) {
                cacheService.cachearServicio(servicio);

                // 6. Precargar portafolios de cada servicio
                List<Portafolio> portafolios = portafolioRepository.findByServicioId(servicio.getId());
                if (!portafolios.isEmpty()) {
                    portafolioCacheService.cachearPortafolios(servicio.getId(), portafolios);
                }
            }

            // 7. Cachear lista completa
            cacheService.cachearTodosLosServicios(todosLosServicios);

            // 8. Agrupar y cachear por usuario
            Map<Integer, List<Servicio>> serviciosPorUsuario = todosLosServicios.stream()
                    .collect(Collectors.groupingBy(Servicio::getUsuarioId));

            for (Map.Entry<Integer, List<Servicio>> entry : serviciosPorUsuario.entrySet()) {
                cacheService.cachearServiciosPorUsuario(entry.getKey(), entry.getValue());
            }

            // 9. Agrupar y cachear por oficio
            Map<Integer, List<Servicio>> serviciosPorOficio = todosLosServicios.stream()
                    .collect(Collectors.groupingBy(Servicio::getOficioId));

            for (Map.Entry<Integer, List<Servicio>> entry : serviciosPorOficio.entrySet()) {
                cacheService.cachearServiciosPorOficio(entry.getKey(), entry.getValue());
            }

            log.info("✅ Caché de servicios inicializado exitosamente:");
            log.info("   - {} servicios individuales", todosLosServicios.size());
            log.info("   - {} grupos de usuarios", serviciosPorUsuario.size());
            log.info("   - {} grupos de oficios", serviciosPorOficio.size());

        } catch (Exception e) {
            log.error("❌ Error al inicializar caché de servicios: {}", e.getMessage(), e);
        }
    }

    /**
     * Recarga todo el cache desde cero
     */
    public void recargarCacheCompleto() {
        log.info("🔄 Recargando caché completo de servicios...");
        precargarServicios();
    }

    /**
     * Pre-carga un servicio específico con todas sus dependencias
     */
    public void precargarServicio(Integer servicioId) {
        try {
            Optional<Servicio> servicioOpt = servicioRepository.findByIdWithDetails(servicioId);
            if (servicioOpt.isPresent()) {
                Servicio servicio = servicioOpt.get();

                // Cachear servicio
                cacheService.cachearServicio(servicio);

                // Precargar usuario
                usuarioCacheService.buscarPorIdCached(servicio.getUsuarioId());

                // Precargar oficio
                oficioCacheService.buscarPorIdCached(servicio.getOficioId());

                // Precargar portafolios
                List<Portafolio> portafolios = portafolioRepository.findByServicioId(servicioId);
                if (!portafolios.isEmpty()) {
                    portafolioCacheService.cachearPortafolios(servicioId, portafolios);
                }

                log.info("✅ Servicio {} precargado con todas sus dependencias", servicioId);
            } else {
                log.warn("⚠️ Servicio {} no encontrado para precargar", servicioId);
            }
        } catch (Exception e) {
            log.error("❌ Error al precargar servicio {}: {}", servicioId, e.getMessage(), e);
        }
    }

    /**
     * Pre-carga servicios de un usuario en cache
     */
    public void precargarServiciosPorUsuario(Integer usuarioId) {
        try {
            List<Servicio> servicios = servicioRepository.findByUsuarioId(usuarioId);
            cacheService.cachearServiciosPorUsuario(usuarioId, servicios);

            // Precargar portafolios de cada servicio
            for (Servicio servicio : servicios) {
                List<Portafolio> portafolios = portafolioRepository.findByServicioId(servicio.getId());
                if (!portafolios.isEmpty()) {
                    portafolioCacheService.cachearPortafolios(servicio.getId(), portafolios);
                }
            }

            log.info("✅ Servicios del usuario {} precargados ({} servicios)", usuarioId, servicios.size());
        } catch (Exception e) {
            log.error("❌ Error al precargar servicios del usuario {}: {}", usuarioId, e.getMessage(), e);
        }
    }

    /**
     * Pre-carga servicios de un oficio en cache
     */
    public void precargarServiciosPorOficio(Integer oficioId) {
        try {
            List<Servicio> servicios = servicioRepository.findByOficioId(oficioId);
            cacheService.cachearServiciosPorOficio(oficioId, servicios);
            log.info("✅ Servicios del oficio {} precargados ({} servicios)", oficioId, servicios.size());
        } catch (Exception e) {
            log.error("❌ Error al precargar servicios del oficio {}: {}", oficioId, e.getMessage(), e);
        }
    }
}