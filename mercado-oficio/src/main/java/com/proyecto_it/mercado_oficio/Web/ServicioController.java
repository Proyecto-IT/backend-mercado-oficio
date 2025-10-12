package com.proyecto_it.mercado_oficio.Web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;
import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;
import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Service.FilesStorage.FileStorageService;
import com.proyecto_it.mercado_oficio.Domain.Service.Servicio.Portafolio.PortafolioService;
import com.proyecto_it.mercado_oficio.Domain.Service.Servicio.ServicioService;
import com.proyecto_it.mercado_oficio.Domain.Service.Servicio.ServicioServiceImpl;
import com.proyecto_it.mercado_oficio.Domain.Service.Usuario.UsuarioService;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.ServicioRequestDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.ServicioResponseDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.ServicioUpdateDTO;
import com.proyecto_it.mercado_oficio.Mapper.Servicio.Portafolio.PortafolioMapper;
import com.proyecto_it.mercado_oficio.Mapper.Servicio.ServicioMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;
    private final PortafolioService portafolioService;
    private final ServicioMapper mapper;
    private final PortafolioMapper portafolioMapper;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;
    private final UsuarioService usuarioService;
    /**
     * Crear un nuevo servicio con archivo de imagen (Solo TRABAJADOR)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crearServicio(
            @RequestPart("servicio") String servicioJson,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            Authentication authentication) {

        try {
            Integer usuarioId = obtenerUsuarioIdDeAuth(authentication);

            // Obtener el usuario ANTES de crear el servicio
            Usuario usuarioAntes = usuarioService.buscarPorId(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Integer permisoAntes = usuarioAntes.getPermiso();

            log.info("📝 Creando servicio - Usuario: {}, Permiso actual: {}",
                    usuarioId, permisoAntes);

            ServicioRequestDTO requestDTO = objectMapper.readValue(servicioJson, ServicioRequestDTO.class);

            String imagenUrl = null;
            if (imagen != null && !imagen.isEmpty()) {
                imagenUrl = fileStorageService.guardarImagen(imagen);
            }

            Servicio servicio = mapper.toDomain(requestDTO, usuarioId);

            // Crear servicio (puede cambiar el rol del usuario)
            Servicio servicioCreado;
            List<Portafolio> portafoliosCreados = null;

            if (requestDTO.getPortafolios() != null && !requestDTO.getPortafolios().isEmpty()) {
                List<Portafolio> portafolios = requestDTO.getPortafolios().stream()
                        .map(pDto -> portafolioMapper.toDomain(pDto, null))
                        .collect(Collectors.toList());

                servicioCreado = ((ServicioServiceImpl) servicioService)
                        .crearServicioConPortafolios(servicio, imagenUrl, portafolios);

                portafoliosCreados = portafolioService.obtenerPortafoliosPorServicio(servicioCreado.getId());
            } else {
                servicioCreado = servicioService.crearServicio(servicio, imagenUrl);
            }

            // 🔥 VERIFICAR SI EL ROL CAMBIÓ Y OBTENER USUARIO ACTUALIZADO
            Usuario usuarioDespues = usuarioService.buscarPorId(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Integer permisoDespues = usuarioDespues.getPermiso();

            boolean rolCambio = !permisoAntes.equals(permisoDespues);

            // 🔥 Si cambió el rol, usar el método del servicio para actualizar todo
            if (rolCambio) {
                log.info("🔄 Rol cambió de {} a {} - Delegando actualización a UsuarioService",
                        permisoAntes, permisoDespues);

                // 🎯 DELEGAR TODO A UsuarioService (actualiza BD y cache)
                usuarioService.modificarPermisoUsuario(usuarioId, permisoDespues);

                // Obtener usuario actualizado desde el cache ya refrescado
                usuarioDespues = usuarioService.buscarPorId(usuarioId)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                log.info("✅ Usuario actualizado correctamente por UsuarioService");
            }

            // Preparar respuesta
            Map<String, Object> responseBody = new HashMap<>();

            if (portafoliosCreados != null) {
                responseBody.put("servicio", mapper.toResponseDTOWithPortafolios(
                        servicioCreado, portafoliosCreados, portafolioMapper));
            } else {
                responseBody.put("servicio", mapper.toResponseDTO(servicioCreado));
            }

            // 🔥 Si el rol cambió, incluir flag y usuario actualizado
            if (rolCambio) {
                responseBody.put("rolActualizado", true);

                // Construir objeto de usuario para el frontend
                Map<String, Object> usuarioActualizado = new HashMap<>();
                usuarioActualizado.put("nombre", usuarioDespues.getNombre());
                usuarioActualizado.put("apellido", usuarioDespues.getApellido());
                usuarioActualizado.put("gmail", usuarioDespues.getGmail());
                usuarioActualizado.put("rol", mapearPermiso(permisoDespues));

                responseBody.put("usuarioActualizado", usuarioActualizado);

                log.info("✅ Enviando usuario actualizado al frontend: {}", usuarioActualizado);
            }

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(responseBody);

        } catch (Exception e) {
            log.error("❌ Error al crear servicio: ", e);
            throw new RuntimeException("Error al procesar la solicitud: " + e.getMessage(), e);
        }
    }
    /**
     * Actualizar servicio con archivo de imagen opcional (Solo el propietario)
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServicioResponseDTO> actualizarServicio(
            @PathVariable Integer id,
            @RequestPart("servicio") String servicioJson,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            Authentication authentication) {

        try {
            Integer usuarioId = obtenerUsuarioIdDeAuth(authentication);

            // Validar permisos
            servicioService.validarPermisos(id, usuarioId);

            // Deserializar el JSON del servicio
            ServicioUpdateDTO updateDTO = objectMapper.readValue(servicioJson, ServicioUpdateDTO.class);

            // Guardar nueva imagen si se proporciona
            String imagenUrl = null;
            if (imagen != null && !imagen.isEmpty()) {
                // Obtener servicio actual para eliminar imagen antigua
                Servicio servicioActual = servicioService.obtenerServicioPorId(id);
                if (servicioActual.getImagenUrl() != null) {
                    fileStorageService.eliminarImagen(servicioActual.getImagenUrl());
                }

                imagenUrl = fileStorageService.guardarImagen(imagen);
            }

            Servicio servicioActualizado = mapper.toDomain(updateDTO);
            servicioActualizado = Servicio.builder()
                    .id(id)
                    .usuarioId(usuarioId)
                    .oficioId(servicioActualizado.getOficioId())
                    .descripcion(servicioActualizado.getDescripcion())
                    .tarifaHora(servicioActualizado.getTarifaHora())
                    .disponibilidad(servicioActualizado.getDisponibilidad())
                    .experiencia(servicioActualizado.getExperiencia())
                    .especialidades(servicioActualizado.getEspecialidades())
                    .ubicacion(servicioActualizado.getUbicacion())
                    .build();

            // Si hay portafolios, actualizar servicio con portafolios
            if (updateDTO.getPortafolios() != null) {
                List<Portafolio> portafolios = updateDTO.getPortafolios().stream()
                        .map(pDto -> portafolioMapper.toDomain(pDto, id))
                        .collect(Collectors.toList());

                Servicio servicioGuardado = ((ServicioServiceImpl) servicioService)
                        .actualizarServicioConPortafolios(id, servicioActualizado, imagenUrl, portafolios);

                List<Portafolio> portafoliosActualizados = portafolioService
                        .obtenerPortafoliosPorServicio(id);

                return ResponseEntity.ok(mapper.toResponseDTOWithPortafolios(
                        servicioGuardado, portafoliosActualizados, portafolioMapper));
            } else {
                Servicio servicioGuardado = servicioService.actualizarServicio(
                        id, servicioActualizado, imagenUrl);

                List<Portafolio> portafoliosExistentes = portafolioService
                        .obtenerPortafoliosPorServicio(id);

                return ResponseEntity.ok(mapper.toResponseDTOWithPortafolios(
                        servicioGuardado, portafoliosExistentes, portafolioMapper));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la solicitud: " + e.getMessage(), e);
        }
    }

    /**
     * Eliminar un servicio (Solo el propietario)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarServicio(
            @PathVariable Integer id,
            Authentication authentication) {

        Integer usuarioId = obtenerUsuarioIdDeAuth(authentication);

        // Obtener servicio para eliminar su imagen
        Servicio servicio = servicioService.obtenerServicioPorId(id);
        if (servicio.getImagenUrl() != null) {
            fileStorageService.eliminarImagen(servicio.getImagenUrl());
        }

        servicioService.eliminarServicio(id, usuarioId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener un servicio por ID (Público)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServicioResponseDTO> obtenerServicioPorId(
            @PathVariable Integer id) {

        Servicio servicio = servicioService.obtenerServicioPorId(id);
        List<Portafolio> portafolios = portafolioService.obtenerPortafoliosPorServicio(id);

        return ResponseEntity.ok(mapper.toResponseDTOWithPortafolios(
                servicio, portafolios, portafolioMapper));
    }

    /**
     * Obtener mis servicios (Autenticado - TRABAJADOR)
     */
    @GetMapping("/mis-servicios")
    public ResponseEntity<List<ServicioResponseDTO>> obtenerMisServicios(
            Authentication authentication) {

        Integer usuarioId = obtenerUsuarioIdDeAuth(authentication);
        List<Servicio> servicios = servicioService.obtenerServiciosPorUsuario(usuarioId);
        List<ServicioResponseDTO> response = servicios.stream()
                .map(servicio -> {
                    List<Portafolio> portafolios = portafolioService
                            .obtenerPortafoliosPorServicio(servicio.getId());
                    return mapper.toResponseDTOWithPortafolios(servicio, portafolios, portafolioMapper);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ServicioResponseDTO>> obtenerTodosLosServicios() {
        List<Servicio> servicios = servicioService.obtenerTodosLosServicios();

        List<ServicioResponseDTO> response = servicios.stream()
                .map(servicio -> {
                    List<Portafolio> portafolios = portafolioService
                            .obtenerPortafoliosPorServicio(servicio.getId());
                    return mapper.toResponseDTOWithPortafolios(servicio, portafolios, portafolioMapper);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
    // ===== MÉTODOS AUXILIARES =====

    private Integer obtenerUsuarioIdDeAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }

        // Obtenemos el gmail del UserDetails (username)
        String gmail = authentication.getName();

        // Buscamos el usuario en la DB
        Usuario usuario = usuarioService.buscarPorGmail(gmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return usuario.getId();
    }
    private String mapearPermiso(Integer permiso) {
        return switch (permiso) {
            case 0 -> "CLIENTE";
            case 1 -> "ADMIN";
            case 2 -> "TRABAJADOR";
            default -> "DESCONOCIDO";
        };
    }
}
