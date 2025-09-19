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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


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
}
