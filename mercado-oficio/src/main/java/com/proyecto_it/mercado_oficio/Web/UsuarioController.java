package com.proyecto_it.mercado_oficio.Web;

import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Service.Email.EmailService;
import com.proyecto_it.mercado_oficio.Domain.Service.Usuario.UsuarioService;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Usuario.*;
import com.proyecto_it.mercado_oficio.Mapper.Usuario.UsuarioMapper;
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


@RestController
@RequestMapping("/api/usuario")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final EmailService emailService;
    private final UsuarioMapper usuarioMapper;

    @GetMapping("/{gmail}")
    public ResponseEntity<UsuarioGetUpdateResponse> obtenerUsuario(@PathVariable String gmail) {
        log.info("Solicitud de obtención de usuario, gmail={}", gmail);
        return usuarioService.buscarPorGmail(gmail)
                .map(usuario -> {
                    UsuarioGetUpdateResponse dto = usuarioMapper.toGetUpdateResponseFromDomain(usuario);
                    log.info("Usuario encontrado: gmail={}", gmail);
                    return ResponseEntity.ok(dto);
                })
                .orElseGet(() -> {
                    log.warn("Usuario no encontrado: gmail={}", gmail);
                    return ResponseEntity.notFound().build();
                });
    }

    @PutMapping("/{gmail}")
    public ResponseEntity<?> actualizarUsuario(
            @PathVariable String gmail,
            @Valid @RequestBody UsuarioUpdate usuarioUpdate
    ) {
        log.info("Solicitud de actualización de usuario, gmail={}", gmail);
        try {
            Usuario actualizado = usuarioService.actualizarUsuarioPorGmail(gmail, usuarioUpdate);
            log.info("Usuario actualizado correctamente: gmail={}", gmail);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            log.warn("Error al actualizar usuario: gmail={}, mensaje={}", gmail, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al actualizar usuario: gmail={}, mensaje={}", gmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    @PutMapping("/{gmail}/establecer-password-local")
    public ResponseEntity<?> establecerPasswordLocal(
            @PathVariable String gmail,
            @Valid @RequestBody EstablecerPasswordRequest request) {
        log.info("Solicitud de establecer contraseña local, gmail={}", gmail);
        try {
            Usuario usuario = usuarioService.establecerPasswordLocalAUsuario(gmail, request.getNuevaPassword());
            log.info("Contraseña local establecida correctamente, gmail={}", gmail);
            return ResponseEntity.ok(Map.of(
                    "message", "Contraseña local establecida correctamente. Ahora puedes usar ambos métodos de login.",
                    "metodosDisponibles", usuario.getProveedor(),
                    "esUsuarioHibrido", usuario.esUsuarioHibrido()
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Error al establecer contraseña local, gmail={}, mensaje={}", gmail, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al establecer contraseña local, gmail={}, mensaje={}", gmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }
    @GetMapping("/me/imagen")
    public ResponseEntity<Map<String, Object>> obtenerImagenPerfil(Authentication authentication) {
        try {
            String gmail = obtenerGmailDeAuth(authentication);

            Usuario usuario = usuarioService.buscarPorGmail(gmail)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Map<String, Object> response = new HashMap<>();

            if (usuario.getImagen() != null && usuario.getImagen().length > 0) {
                String imagenBase64 = Base64.getEncoder().encodeToString(usuario.getImagen());
                response.put("tieneImagen", true);
                response.put("imagen", imagenBase64);
                response.put("imagenTipo", usuario.getImagenTipo());
            } else {
                response.put("tieneImagen", false);
                response.put("imagen", null);
                response.put("imagenTipo", null);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener imagen de perfil: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo obtener la imagen de perfil"));
        }
    }

    @PutMapping(value = "/me/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> actualizarImagenPerfil(
            @RequestPart("imagen") MultipartFile imagen,
            Authentication authentication) {

        try {
            String gmail = obtenerGmailDeAuth(authentication);

            log.info("Actualizando imagen de perfil para usuario: {}", gmail);

            if (imagen.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La imagen no puede estar vacía"));
            }

            String contentType = imagen.getContentType();
            List<String> tiposPermitidos = Arrays.asList(
                    "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
            );

            if (contentType == null || !tiposPermitidos.contains(contentType)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Tipo de archivo no válido. Solo se permiten JPG, PNG, GIF o WEBP"));
            }

            long maxSize = 10 * 1024 * 1024; // 10MB
            if (imagen.getSize() > maxSize) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La imagen no debe superar los 10MB"));
            }

            boolean actualizado = usuarioService.actualizarImagenPerfil(gmail, imagen);

            if (actualizado) {
                log.info("Imagen de perfil actualizada para usuario: {}", gmail);

                Usuario usuario = usuarioService.buscarPorGmail(gmail)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                String imagenBase64 = Base64.getEncoder().encodeToString(usuario.getImagen());

                Map<String, Object> response = new HashMap<>();
                response.put("mensaje", "Imagen de perfil actualizada correctamente");
                response.put("imagen", imagenBase64);
                response.put("imagenTipo", usuario.getImagenTipo());
                response.put("advertencia", "Esta imagen se reflejará en todos tus servicios");

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "No se pudo actualizar la imagen"));
            }

        } catch (Exception e) {
            log.error("Error al actualizar imagen de perfil: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar la imagen: " + e.getMessage()));
        }
    }

    @DeleteMapping("/me/imagen")
    public ResponseEntity<Map<String, String>> eliminarImagenPerfil(Authentication authentication) {
        try {
            String gmail = obtenerGmailDeAuth(authentication);

            log.info("Eliminando imagen de perfil para usuario: {}", gmail);

            boolean eliminado = usuarioService.eliminarImagenPerfil(gmail);

            if (eliminado) {
                log.info("Imagen de perfil eliminada para usuario: {}", gmail);
                return ResponseEntity.ok(Map.of(
                        "mensaje", "Imagen de perfil eliminada correctamente",
                        "advertencia", "Esta imagen se ha eliminado de todos tus servicios"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "No se pudo eliminar la imagen"));
            }

        } catch (Exception e) {
            log.error("Error al eliminar imagen de perfil: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar la imagen"));
        }
    }

    private String obtenerGmailDeAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        throw new RuntimeException("Usuario no autenticado");
    }
}
