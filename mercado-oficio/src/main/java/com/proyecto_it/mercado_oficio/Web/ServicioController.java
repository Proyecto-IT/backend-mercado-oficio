package com.proyecto_it.mercado_oficio.Web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;
import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;
import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Service.FilesStorage.FileStorageService;
import com.proyecto_it.mercado_oficio.Domain.Service.Servicio.Portafolio.PortafolioCacheService;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
@Slf4j
public class ServicioController {

    private final ServicioService servicioService;
    private final PortafolioService portafolioService;
    private final UsuarioService usuarioService;
    private final FileStorageService fileStorageService;
    private final ServicioMapper mapper;
    private final PortafolioMapper portafolioMapper;
    private final ObjectMapper objectMapper;
    private final PortafolioCacheService portafolioCacheService;

    @GetMapping
    public ResponseEntity<List<ServicioResponseDTO>> obtenerTodosLosServicios() {
        List<Servicio> servicios = servicioService.obtenerTodosLosServicios();

        List<ServicioResponseDTO> response = servicios.stream()
                .map(servicio -> {
                    List<Portafolio> portafolios = portafolioCacheService
                            .obtenerPortafoliosPorServicioCached(servicio.getId());
                    return mapper.toResponseDTOWithPortafolios(servicio, portafolios, portafolioMapper);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioResponseDTO> obtenerServicioPorId(@PathVariable Integer id) {
        Servicio servicio = servicioService.obtenerServicioPorId(id);

        List<Portafolio> portafolios = portafolioCacheService
                .obtenerPortafoliosPorServicioCached(id);

        return ResponseEntity.ok(mapper.toResponseDTOWithPortafolios(
                servicio, portafolios, portafolioMapper));
    }

    @GetMapping("/mis-servicios")
    public ResponseEntity<List<ServicioResponseDTO>> obtenerMisServicios(
            Authentication authentication) {

        Integer usuarioId = obtenerUsuarioIdDeAuth(authentication);
        List<Servicio> servicios = servicioService.obtenerServiciosPorUsuario(usuarioId);

        List<ServicioResponseDTO> response = servicios.stream()
                .map(servicio -> {
                    List<Portafolio> portafolios = portafolioCacheService
                            .obtenerPortafoliosPorServicioCached(servicio.getId());
                    return mapper.toResponseDTOWithPortafolios(servicio, portafolios, portafolioMapper);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crearServicio(
            @RequestPart("servicio") String servicioJson,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            Authentication authentication) {

        try {
            String gmail = authentication.getName();

            Usuario usuarioAntes = usuarioService.buscarPorGmail(gmail)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Integer usuarioId = usuarioAntes.getId();
            Integer permisoAntes = usuarioAntes.getPermiso();

            log.info("Creando servicio - Usuario: {}, Gmail: {}, Permiso actual: {}", usuarioId, gmail, permisoAntes);

            ServicioRequestDTO requestDTO = objectMapper.readValue(servicioJson, ServicioRequestDTO.class);
            Servicio servicio = mapper.toDomain(requestDTO, usuarioId);

            if ("nueva".equalsIgnoreCase(requestDTO.getImagenOpcion()) && imagen != null) {
                log.info("Subiendo nueva imagen para el usuario {}", gmail);
                usuarioService.actualizarImagenPerfil(gmail, imagen);

            } else if ("mantener".equalsIgnoreCase(requestDTO.getImagenOpcion())) {
                log.info("Manteniendo imagen existente del usuario {}", gmail);

            } else if ("ninguna".equalsIgnoreCase(requestDTO.getImagenOpcion())) {
                log.info("Eliminando imagen del usuario {}", gmail);
                usuarioService.eliminarImagenPerfil(gmail);
            } else {
                log.info("Opción de imagen no especificada o inválida, no se modifica la imagen del usuario.");
            }

            Servicio servicioCreado;
            List<Portafolio> portafoliosCreados = null;

            if (requestDTO.getPortafolios() != null && !requestDTO.getPortafolios().isEmpty()) {
                List<Portafolio> portafolios = requestDTO.getPortafolios().stream()
                        .map(pDto -> portafolioMapper.toDomain(pDto, null))
                        .collect(Collectors.toList());

                servicioCreado = ((ServicioServiceImpl) servicioService)
                        .crearServicioConPortafolios(servicio, null, portafolios);

                portafoliosCreados = portafolioCacheService
                        .obtenerPortafoliosPorServicioCached(servicioCreado.getId());
            } else {
                servicioCreado = servicioService.crearServicio(servicio, null);
            }

            Usuario usuarioDespues = usuarioService.buscarPorGmail(gmail)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Integer permisoDespues = usuarioDespues.getPermiso();

            boolean rolCambio = !permisoAntes.equals(permisoDespues);

            if (rolCambio) {
                log.info("Rol cambió de {} a {} - Actualizando...", permisoAntes, permisoDespues);
                usuarioService.modificarPermisoUsuario(usuarioId, permisoDespues);
                usuarioDespues = usuarioService.buscarPorGmail(gmail)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            }

            Map<String, Object> responseBody = new HashMap<>();

            if (portafoliosCreados != null) {
                responseBody.put("servicio", mapper.toResponseDTOWithPortafolios(
                        servicioCreado, portafoliosCreados, portafolioMapper));
            } else {
                responseBody.put("servicio", mapper.toResponseDTO(servicioCreado));
            }

            if (rolCambio) {
                responseBody.put("rolActualizado", true);
                Map<String, Object> usuarioActualizado = new HashMap<>();
                usuarioActualizado.put("nombre", usuarioDespues.getNombre());
                usuarioActualizado.put("apellido", usuarioDespues.getApellido());
                usuarioActualizado.put("gmail", usuarioDespues.getGmail());
                usuarioActualizado.put("rol", mapearPermiso(permisoDespues));
                responseBody.put("usuarioActualizado", usuarioActualizado);
                log.info("Usuario actualizado correctamente: {}", usuarioActualizado);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);

        } catch (Exception e) {
            log.error("Error al crear servicio: ", e);
            throw new RuntimeException("Error al procesar la solicitud: " + e.getMessage(), e);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServicioResponseDTO> actualizarServicio(
            @PathVariable Integer id,
            @RequestPart("servicio") String servicioJson,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            Authentication authentication) {

        try {
            Integer usuarioId = obtenerUsuarioIdDeAuth(authentication);
            String gmail = authentication.getName();

            servicioService.validarPermisos(id, usuarioId);

            ServicioUpdateDTO updateDTO = objectMapper.readValue(servicioJson, ServicioUpdateDTO.class);

            if ("nueva".equalsIgnoreCase(updateDTO.getImagenOpcion()) && imagen != null) {
                log.info("Subiendo nueva imagen para el usuario {}", gmail);
                usuarioService.actualizarImagenPerfil(gmail, imagen);

            } else if ("mantener".equalsIgnoreCase(updateDTO.getImagenOpcion())) {
                log.info("Manteniendo imagen existente del usuario {}", gmail);

            } else if ("ninguna".equalsIgnoreCase(updateDTO.getImagenOpcion())) {
                log.info("Eliminando imagen del usuario {}", gmail);
                usuarioService.eliminarImagenPerfil(gmail);
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

            if (updateDTO.getPortafolios() != null) {
                List<Portafolio> portafolios = updateDTO.getPortafolios().stream()
                        .map(pDto -> portafolioMapper.toDomain(pDto, id))
                        .collect(Collectors.toList());

                Servicio servicioGuardado = ((ServicioServiceImpl) servicioService)
                        .actualizarServicioConPortafolios(id, servicioActualizado, imagen, portafolios);

                List<Portafolio> portafoliosActualizados = portafolioCacheService
                        .obtenerPortafoliosPorServicioCached(id);

                return ResponseEntity.ok(mapper.toResponseDTOWithPortafolios(
                        servicioGuardado, portafoliosActualizados, portafolioMapper));
            } else {
                Servicio servicioGuardado = servicioService.actualizarServicio(
                        id, servicioActualizado, imagen);

                List<Portafolio> portafoliosExistentes = portafolioCacheService
                        .obtenerPortafoliosPorServicioCached(id);

                return ResponseEntity.ok(mapper.toResponseDTOWithPortafolios(
                        servicioGuardado, portafoliosExistentes, portafolioMapper));
            }

        } catch (Exception e) {
            log.error("Error al actualizar servicio: ", e);
            throw new RuntimeException("Error al procesar la solicitud: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarServicio(
            @PathVariable Integer id,
            Authentication authentication) {

        Integer usuarioId = obtenerUsuarioIdDeAuth(authentication);

        Servicio servicio = servicioService.obtenerServicioPorId(id);
        if (servicio.getImagenUrl() != null) {
            fileStorageService.eliminarImagen(servicio.getImagenUrl());
        }

        servicioService.eliminarServicio(id, usuarioId);

        return ResponseEntity.noContent().build();
    }

    private Integer obtenerUsuarioIdDeAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String gmail = userDetails.getUsername();
            return usuarioService.buscarPorGmail(gmail)
                    .map(Usuario::getId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        }
        throw new RuntimeException("Usuario no autenticado");
    }

    private String mapearPermiso(Integer permiso) {
        switch (permiso) {
            case 1: return "ADMIN";
            case 2: return "TRABAJADOR";
            case 3: return "USUARIO";
            default: return "DESCONOCIDO";
        }
    }
}