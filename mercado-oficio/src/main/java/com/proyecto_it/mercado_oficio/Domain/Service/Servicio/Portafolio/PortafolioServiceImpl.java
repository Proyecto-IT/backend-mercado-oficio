package com.proyecto_it.mercado_oficio.Domain.Service.Servicio.Portafolio;

import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;
import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;
import com.proyecto_it.mercado_oficio.Domain.Repository.PortafolioRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PortafolioServiceImpl implements PortafolioService {

    private final PortafolioRepository portafolioRepository;
    private final ServicioRepository servicioRepository;
    private final PortafolioCacheService cacheService;

    @Override
    @Transactional(readOnly = true)
    public List<Portafolio> obtenerPortafoliosPorServicio(Integer servicioId) {
        log.info("Obteniendo portafolios del servicio {} desde cache", servicioId);
        return cacheService.obtenerPortafoliosPorServicioCached(servicioId);
    }

    @Override
    public Portafolio crearPortafolio(Portafolio portafolio) {
        try {
            log.info("Creando portafolio para servicio {}", portafolio.getServicioId());
            servicioRepository.findByIdWithDetails(portafolio.getServicioId())
                    .orElseThrow(() -> new RuntimeException(
                            "Servicio no encontrado: " + portafolio.getServicioId()));

            if (portafolio.getTitulo() == null || portafolio.getTitulo().trim().isEmpty()) {
                throw new RuntimeException("El título del portafolio es requerido");
            }

            Portafolio portafolioGuardado = portafolioRepository.save(portafolio);

            if (portafolioGuardado == null || portafolioGuardado.getId() == null) {
                throw new RuntimeException("Error al guardar el portafolio en la base de datos");
            }

            cacheService.sincronizarDespuesDeGuardar(portafolio.getServicioId());

            log.info("Portafolio {} creado para servicio {}",
                    portafolioGuardado.getId(), portafolio.getServicioId());

            return portafolioGuardado;

        } catch (Exception e) {
            log.error("Error al crear portafolio: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear portafolio: " + e.getMessage(), e);
        }
    }

    @Override
    public Portafolio actualizarPortafolio(Integer id, Portafolio portafolio) {
        try {
            log.info("Actualizando portafolio {}", id);
            Portafolio portafolioExistente = portafolioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Portafolio no encontrado: " + id));

            Portafolio portafolioActualizado = Portafolio.builder()
                    .id(id)
                    .servicioId(portafolioExistente.getServicioId())
                    .titulo(portafolio.getTitulo() != null ?
                            portafolio.getTitulo() : portafolioExistente.getTitulo())
                    .descripcion(portafolio.getDescripcion() != null ?
                            portafolio.getDescripcion() : portafolioExistente.getDescripcion())
                    .build();

            Portafolio portafolioGuardado = portafolioRepository.save(portafolioActualizado);

            if (portafolioGuardado == null || portafolioGuardado.getId() == null) {
                throw new RuntimeException("Error al actualizar el portafolio en la base de datos");
            }

            cacheService.sincronizarDespuesDeGuardar(portafolioExistente.getServicioId());

            log.info("Portafolio {} actualizado", id);

            return portafolioGuardado;

        } catch (Exception e) {
            log.error("Error al actualizar portafolio {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar portafolio: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminarPortafolio(Integer id) {
        try {
            log.info("🗑Eliminando portafolio {}", id);

            Portafolio portafolio = portafolioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Portafolio no encontrado: " + id));

            Integer servicioId = portafolio.getServicioId();

            portafolioRepository.deleteById(id);

            cacheService.sincronizarDespuesDeGuardar(servicioId);

            log.info("Portafolio {} eliminado correctamente", id);

        } catch (Exception e) {
            log.error("Error al eliminar portafolio {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error al eliminar portafolio: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Portafolio obtenerPortafolioPorId(Integer id) {
        log.info("Obteniendo portafolio {}", id);
        return portafolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Portafolio no encontrado: " + id));
    }
}