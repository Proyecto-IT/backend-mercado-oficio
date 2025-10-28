package com.proyecto_it.mercado_oficio.Domain.Service.Servicio;
import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;
import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;
import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Repository.OficioRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.PortafolioRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.ServicioRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.UsuarioRepository;
import com.proyecto_it.mercado_oficio.Domain.Service.FilesStorage.FileStorageService;
import com.proyecto_it.mercado_oficio.Domain.Service.Servicio.Portafolio.PortafolioService;
import com.proyecto_it.mercado_oficio.Domain.Service.Usuario.UsuarioCacheService;
import com.proyecto_it.mercado_oficio.Domain.Service.Usuario.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;
    private final ServicioCacheService cacheService;
    private final UsuarioService usuarioService;
    private final FileStorageService fileStorageService;
    private final PortafolioService portafolioService;

    @Override
    public Servicio crearServicio(Servicio servicio, MultipartFile imagen) {
        try {
            log.info("Creando servicio para usuario {}", servicio.getUsuarioId());

            // 1. Guardar imagen si existe
            if (imagen != null && !imagen.isEmpty()) {
                String imagenUrl = fileStorageService.guardarImagen(imagen);
                servicio = Servicio.builder()
                        .usuarioId(servicio.getUsuarioId())
                        .oficioId(servicio.getOficioId())
                        .descripcion(servicio.getDescripcion())
                        .tarifaHora(servicio.getTarifaHora())
                        .disponibilidad(servicio.getDisponibilidad())
                        .experiencia(servicio.getExperiencia())
                        .especialidades(servicio.getEspecialidades())
                        .ubicacion(servicio.getUbicacion())
                        .trabajosCompletados(servicio.getTrabajosCompletados())
                        .imagenUrl(imagenUrl)
                        .build();
            }

            Servicio servicioGuardado = servicioRepository.save(servicio);

            if (servicioGuardado == null || servicioGuardado.getId() == null) {
                throw new RuntimeException("Error: El servicio no se guardó correctamente en la base de datos");
            }

            log.info("Servicio {} guardado en DB", servicioGuardado.getId());

            Usuario usuario = usuarioService.buscarPorId(servicio.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (usuario.getPermiso() == 3) {
                log.info("Usuario {} necesita cambio de permiso (de 3 a 2)", usuario.getId());
                usuarioService.modificarPermisoUsuario(usuario.getId(), 2);
            } else {
                log.info("Usuario {} ya tiene permiso {} (no se requiere cambio)",
                        usuario.getId(), usuario.getPermiso());
            }

            cacheService.sincronizarDespuesDeCrear(servicioGuardado);

            return servicioGuardado;

        } catch (Exception e) {
            log.error("Error al crear servicio: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear servicio: " + e.getMessage(), e);
        }
    }

    @Override
    public Servicio crearServicioConPortafolios(Servicio servicio, MultipartFile imagen,
                                                List<Portafolio> portafolios) {
        try {
            log.info("Creando servicio CON portafolios para usuario {}", servicio.getUsuarioId());

            Servicio servicioGuardado = crearServicio(servicio, imagen);

            if (servicioGuardado.getId() == null) {
                throw new RuntimeException("Error: El servicio no tiene ID después de guardarlo");
            }

            if (portafolios != null && !portafolios.isEmpty()) {
                for (Portafolio portafolio : portafolios) {
                    Portafolio portafolioConServicio = Portafolio.builder()
                            .servicioId(servicioGuardado.getId())
                            .titulo(portafolio.getTitulo())
                            .descripcion(portafolio.getDescripcion())
                            .build();

                    portafolioService.crearPortafolio(portafolioConServicio);
                }
                log.info("{} portafolios creados para servicio {}",
                        portafolios.size(), servicioGuardado.getId());
            }

            return servicioGuardado;

        } catch (Exception e) {
            log.error("Error al crear servicio con portafolios: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear servicio con portafolios: " + e.getMessage(), e);
        }
    }

    @Override
    public Servicio actualizarServicio(Integer id, Servicio servicio, MultipartFile imagen) {
        try {
            log.info("Actualizando servicio {}", id);

            Servicio servicioAnterior = servicioRepository.findByIdWithDetails(id)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

            String imagenUrl = servicioAnterior.getImagenUrl();
            if (imagen != null && !imagen.isEmpty()) {
                if (imagenUrl != null) {
                    fileStorageService.eliminarImagen(imagenUrl);
                }
                imagenUrl = fileStorageService.guardarImagen(imagen);
            }

            Servicio servicioActualizado = Servicio.builder()
                    .id(id)
                    .usuarioId(servicioAnterior.getUsuarioId())
                    .oficioId(servicio.getOficioId())
                    .descripcion(servicio.getDescripcion())
                    .tarifaHora(servicio.getTarifaHora())
                    .disponibilidad(servicio.getDisponibilidad())
                    .experiencia(servicio.getExperiencia())
                    .especialidades(servicio.getEspecialidades())
                    .ubicacion(servicio.getUbicacion())
                    .trabajosCompletados(servicioAnterior.getTrabajosCompletados())
                    .imagenUrl(imagenUrl)
                    .build();

            Servicio servicioGuardado = servicioRepository.save(servicioActualizado);

            if (servicioGuardado == null || servicioGuardado.getId() == null) {
                throw new RuntimeException("Error al actualizar servicio en la base de datos");
            }

            cacheService.sincronizarDespuesDeActualizar(servicioAnterior, servicioGuardado);

            log.info("Servicio {} actualizado correctamente", id);
            return servicioGuardado;

        } catch (Exception e) {
            log.error("Error al actualizar servicio {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar servicio: " + e.getMessage(), e);
        }
    }

    @Override
    public Servicio actualizarServicioConPortafolios(Integer id, Servicio servicio,
                                                     MultipartFile imagen,
                                                     List<Portafolio> portafolios) {
        try {
            log.info("Actualizando servicio {} CON portafolios", id);

            Servicio servicioActualizado = actualizarServicio(id, servicio, imagen);

            if (portafolios != null) {
                List<Portafolio> portafoliosAnteriores = portafolioService
                        .obtenerPortafoliosPorServicio(id);

                for (Portafolio p : portafoliosAnteriores) {
                    portafolioService.eliminarPortafolio(p.getId());
                }

                for (Portafolio portafolio : portafolios) {
                    Portafolio nuevoPortafolio = Portafolio.builder()
                            .servicioId(id)
                            .titulo(portafolio.getTitulo())
                            .descripcion(portafolio.getDescripcion())
                            .build();

                    portafolioService.crearPortafolio(nuevoPortafolio);
                }

                log.info("{} portafolios actualizados para servicio {}",
                        portafolios.size(), id);
            }

            return servicioActualizado;

        } catch (Exception e) {
            log.error("Error al actualizar servicio con portafolios {}: {}",
                    id, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar servicio con portafolios: " +
                    e.getMessage(), e);
        }
    }

    @Override
    public void eliminarServicio(Integer id, Integer usuarioId) {
        try {
            log.info("Eliminando servicio {}", id);

            Servicio servicio = servicioRepository.findByIdWithDetails(id)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

            validarPermisos(id, usuarioId);

            if (servicio.getImagenUrl() != null) {
                fileStorageService.eliminarImagen(servicio.getImagenUrl());
            }

            servicioRepository.deleteById(id);

            cacheService.sincronizarDespuesDeEliminar(servicio);

            log.info("Servicio {} eliminado correctamente", id);

        } catch (Exception e) {
            log.error("Error al eliminar servicio {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error al eliminar servicio: " + e.getMessage(), e);
        }
    }

    @Override
    public Servicio obtenerServicioPorId(Integer id) {
        log.info("Obteniendo servicio {}", id);
        return cacheService.obtenerServicioPorIdCached(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
    }

    @Override
    public List<Servicio> obtenerServiciosPorUsuario(Integer usuarioId) {
        log.info("Obteniendo servicios del usuario {}", usuarioId);
        return cacheService.obtenerServiciosPorUsuarioCached(usuarioId);
    }

    @Override
    public List<Servicio> obtenerServiciosPorOficio(Integer oficioId) {
        log.info("Obteniendo servicios del oficio {}", oficioId);
        return cacheService.obtenerServiciosPorOficioCached(oficioId);
    }

    @Override
    public List<Servicio> obtenerTodosLosServicios() {
        log.info("Obteniendo todos los servicios desde cache");
        return cacheService.obtenerTodosLosServiciosCached();
    }

    @Override
    public void validarPermisos(Integer servicioId, Integer usuarioId) {
        Servicio servicio = obtenerServicioPorId(servicioId);
        if (!servicio.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permisos para modificar este servicio");
        }
    }
}