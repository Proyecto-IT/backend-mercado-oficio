package com.proyecto_it.mercado_oficio.Domain.Service.Servicio;
import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;
import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;
import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Repository.OficioRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.PortafolioRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.ServicioRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository; // Necesitarás inyectar esto
    private final OficioRepository oficioRepository; // Necesitarás inyectar esto
    private final PortafolioRepository portafolioRepository;

    @Override
    @Transactional
    public Servicio crearServicio(Servicio servicio, String imagenUrl) {
        servicio.validar();

        // Validar que el oficio existe
        validarOficioExiste(servicio.getOficioId());

        // Guardar la imagen en el usuario si se proporciona
        if (imagenUrl != null && !imagenUrl.trim().isEmpty()) {
            actualizarImagenUsuario(servicio.getUsuarioId(), imagenUrl);
        }

        // 🔥 CAMBIAR PERMISO DEL USUARIO A TRABAJADOR (2) SI AÚN NO LO ES
        cambiarPermisoATrabajador(servicio.getUsuarioId());

        return servicioRepository.save(servicio);
    }

    // NUEVO MÉTODO: Crear servicio con portafolios
    @Transactional
    public Servicio crearServicioConPortafolios(Servicio servicio, String imagenUrl, List<Portafolio> portafolios) {
        // Crear el servicio primero (esto ya cambia el permiso)
        Servicio servicioCreado = crearServicio(servicio, imagenUrl);

        // Crear los portafolios asociados si existen
        if (portafolios != null && !portafolios.isEmpty()) {
            for (Portafolio portafolio : portafolios) {
                Portafolio portafolioConServicio = Portafolio.builder()
                        .servicioId(servicioCreado.getId())
                        .titulo(portafolio.getTitulo())
                        .descripcion(portafolio.getDescripcion())
                        .build();
                portafolioConServicio.validar();
                portafolioRepository.save(portafolioConServicio);
            }
        }

        return servicioCreado;
    }

    @Override
    @Transactional
    public Servicio actualizarServicio(Integer id, Servicio servicioActualizado, String imagenUrl) {
        Servicio servicioExistente = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        // Validar permisos
        validarPermisos(id, servicioActualizado.getUsuarioId());

        // Construir servicio actualizado solo con los campos proporcionados
        Servicio.ServicioBuilder builder = Servicio.builder()
                .id(id)
                .usuarioId(servicioExistente.getUsuarioId())
                .oficioId(servicioActualizado.getOficioId() != null ?
                        servicioActualizado.getOficioId() : servicioExistente.getOficioId())
                .descripcion(servicioActualizado.getDescripcion() != null ?
                        servicioActualizado.getDescripcion() : servicioExistente.getDescripcion())
                .tarifaHora(servicioActualizado.getTarifaHora() != null ?
                        servicioActualizado.getTarifaHora() : servicioExistente.getTarifaHora())
                .disponibilidad(servicioActualizado.getDisponibilidad() != null ?
                        servicioActualizado.getDisponibilidad() : servicioExistente.getDisponibilidad())
                .experiencia(servicioActualizado.getExperiencia() != null ?
                        servicioActualizado.getExperiencia() : servicioExistente.getExperiencia())
                .especialidades(servicioActualizado.getEspecialidades() != null ?
                        servicioActualizado.getEspecialidades() : servicioExistente.getEspecialidades())
                .ubicacion(servicioActualizado.getUbicacion() != null ?
                        servicioActualizado.getUbicacion() : servicioExistente.getUbicacion())
                .trabajosCompletados(servicioExistente.getTrabajosCompletados());

        Servicio servicioFinal = builder.build();
        servicioFinal.validar();

        // Actualizar imagen si se proporciona
        if (imagenUrl != null && !imagenUrl.trim().isEmpty()) {
            actualizarImagenUsuario(servicioExistente.getUsuarioId(), imagenUrl);
        }

        return servicioRepository.save(servicioFinal);
    }

    // NUEVO MÉTODO: Actualizar servicio con portafolios
    @Transactional
    public Servicio actualizarServicioConPortafolios(Integer id, Servicio servicioActualizado,
                                                     String imagenUrl, List<Portafolio> portafolios) {
        // Actualizar el servicio
        Servicio servicioGuardado = actualizarServicio(id, servicioActualizado, imagenUrl);

        // Si se envían portafolios, reemplazar los existentes
        if (portafolios != null) {
            // Eliminar portafolios antiguos
            portafolioRepository.deleteAllByServicioId(id);

            // Crear los nuevos portafolios
            if (!portafolios.isEmpty()) {
                for (Portafolio portafolio : portafolios) {
                    Portafolio nuevoPortafolio = Portafolio.builder()
                            .servicioId(id)
                            .titulo(portafolio.getTitulo())
                            .descripcion(portafolio.getDescripcion())
                            .build();
                    nuevoPortafolio.validar();
                    portafolioRepository.save(nuevoPortafolio);
                }
            }
        }

        return servicioGuardado;
    }

    @Override
    @Transactional
    public void eliminarServicio(Integer id, Integer usuarioId) {
        validarPermisos(id, usuarioId);
        servicioRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Servicio obtenerServicioPorId(Integer id) {
        return servicioRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Servicio> obtenerServiciosPorUsuario(Integer usuarioId) {
        return servicioRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Servicio> obtenerServiciosPorOficio(Integer oficioId) {
        return servicioRepository.findByOficioId(oficioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Servicio> obtenerTodosLosServicios() {
        return servicioRepository.findAll();
    }

    @Override
    public void validarPermisos(Integer servicioId, Integer usuarioId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        if (!servicio.getUsuarioId().equals(usuarioId)) {
            throw new SecurityException("No tiene permisos para modificar este servicio");
        }
    }

    private void validarOficioExiste(Integer oficioId) {
        // Implementar validación de que el oficio existe
        // if (!oficioRepository.existsById(oficioId)) throw new Exception...
    }

    private void actualizarImagenUsuario(Integer usuarioId, String imagenUrl) {
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setImagenUrl(imagenUrl);
        usuarioRepository.guardar(usuario);

        log.info("✅ Imagen actualizada para usuario {}: {}", usuarioId, imagenUrl);
    }
    // 🔥 NUEVO: Método para cambiar permiso a TRABAJADOR
    private void cambiarPermisoATrabajador(Integer usuarioId) {
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        // Solo cambiar si no es ya TRABAJADOR (2) o ADMIN (3)
        if (usuario.getPermiso() < 2) {
            log.info("🔄 Cambiando permiso de usuario {} de {} a TRABAJADOR (2)",
                    usuarioId, usuario.getPermiso());

            usuarioRepository.modificarPermisoUsuario(usuarioId, 2);

            log.info("✅ Permiso actualizado correctamente en BD");
        } else {
            log.info("ℹ️ Usuario {} ya tiene permiso {} (no se requiere cambio)",
                    usuarioId, usuario.getPermiso());
        }
    }

}