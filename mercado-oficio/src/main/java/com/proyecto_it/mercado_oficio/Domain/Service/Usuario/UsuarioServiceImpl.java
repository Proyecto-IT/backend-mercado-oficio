package com.proyecto_it.mercado_oficio.Domain.Service.Usuario;


import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Repository.UsuarioRepository;
import com.proyecto_it.mercado_oficio.Domain.Service.Email.EmailService;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Usuario.CambioPasswordRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Usuario.UsuarioUpdate;
import com.proyecto_it.mercado_oficio.Security.Hasher.PasswordHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final EmailService emailService;

    @Override
    public Usuario registrarUsuario(Usuario usuario) {
        log.info("Registrando usuario con Gmail: {}", usuario.getGmail());
        usuario.validar();

        if (usuarioRepository.existePorGmail(usuario.getGmail())) {
            log.warn("Intento de registro fallido: Gmail {} ya existe", usuario.getGmail());
            throw new IllegalArgumentException("Ya existe un usuario con ese Gmail.");
        }

        String hash = passwordHasher.hash(usuario.getPassword());
        usuario.setPassword(hash);
        usuario.setPermiso(0);
        usuario.setVerificado(false);

        Usuario usuarioGuardado = usuarioRepository.guardar(usuario);
        emailService.enviarEmailVerificacion(usuarioGuardado);
        log.info("Usuario registrado exitosamente: {}", usuario.getGmail());

        return usuarioGuardado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        log.debug("Listando todos los usuarios");
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorGmail(String gmail) {
        boolean existe = usuarioRepository.existePorGmail(gmail);
        log.debug("Chequeo existencia de Gmail {}: {}", gmail, existe);
        return existe;
    }

    @Override
    public Usuario actualizarUsuarioPorGmail(String gmail, UsuarioUpdate usuarioUpdate) {
        log.info("Actualizando usuario por Gmail: {}", gmail);
        Usuario usuario = usuarioRepository.buscarPorGmail(gmail)
                .orElseThrow(() -> new IllegalArgumentException("El usuario a actualizar no existe"));

        usuarioRepository.actualizarUsuarioParcial(gmail, usuarioUpdate);
        Usuario actualizado = usuarioRepository.buscarPorGmail(gmail)
                .orElseThrow(() -> new RuntimeException("Error al obtener el usuario actualizado"));

        log.info("Usuario {} actualizado correctamente", gmail);
        return actualizado;
    }

    @Override
    public Usuario actualizarUsuario(Usuario usuario) {
        if (usuario.getId() == null || usuarioRepository.buscarPorId(usuario.getId()).isEmpty()) {
            log.warn("Intento de actualización fallido: usuario con ID {} no existe", usuario.getId());
            throw new IllegalArgumentException("El usuario no existe para actualizar.");
        }
        log.info("Actualizando usuario con ID {}", usuario.getId());
        return usuarioRepository.guardar(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorGmail(String gmail) {
        log.debug("Buscando usuario por Gmail: {}", gmail);
        return usuarioRepository.buscarPorGmail(gmail);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Integer id) {
        log.debug("Buscando usuario por ID: {}", id);
        return usuarioRepository.buscarPorId(id);
    }

    @Override
    public void cambiarPassword(String gmail, CambioPasswordRequest request) {
        log.info("Cambiando contraseña del usuario: {}", gmail);
        request.validar();

        Usuario usuario = usuarioRepository.buscarPorGmail(gmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordHasher.matches(request.getPasswordActual(), usuario.getPassword())) {
            log.warn("Contraseña actual incorrecta para usuario: {}", gmail);
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        usuario.setPassword(passwordHasher.hash(request.getNuevaPassword()));
        usuarioRepository.guardar(usuario);
        log.info("Contraseña actualizada correctamente para usuario: {}", gmail);
    }

    @Override
    public void modificarPermisoUsuario(int id, int permiso) {
        log.info("Modificando permiso del usuario con ID {} a {}", id, permiso);
        usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario a modificar no existe"));

        usuarioRepository.modificarPermisoUsuario(id, permiso);
        log.info("Permiso modificado correctamente para usuario ID {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public String buscarGmailPorId(int id) {
        Usuario usuario = usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));
        log.debug("Gmail del usuario ID {}: {}", id, usuario.getGmail());
        return usuario.getGmail();
    }

    @Override
    public void vincularGoogleAUsuario(String gmail) {
        log.info("Vinculando Google a usuario: {}", gmail);
        Usuario usuario = usuarioRepository.buscarPorGmail(gmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.puedeUsarGoogle()) {
            log.info("Usuario {} ya tiene vinculación con Google", gmail);
            return;
        }

        usuario.vincularGoogle();
        usuarioRepository.guardar(usuario);
        log.info("Usuario {} vinculado con Google exitosamente. Proveedor: {}", gmail, usuario.getProveedor());
    }

    @Override
    public Usuario establecerPasswordLocalAUsuario(String gmail, String password) {
        log.info("Estableciendo contraseña local para usuario: {}", gmail);
        Usuario usuario = usuarioRepository.buscarPorGmail(gmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.tienePasswordLocal()) {
            log.warn("Usuario {} ya tiene contraseña local configurada", gmail);
            throw new IllegalArgumentException("El usuario ya tiene contraseña local configurada");
        }

        if (password == null || password.length() < 8) {
            log.warn("Contraseña inválida proporcionada para usuario {}", gmail);
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }

        usuario.establecerPasswordLocal(passwordHasher.hash(password));
        Usuario usuarioActualizado = usuarioRepository.guardar(usuario);
        log.info("Contraseña local establecida para usuario: {}. Ahora es híbrido: {}", gmail, usuarioActualizado.esUsuarioHibrido());
        return usuarioActualizado;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeUsarMetodoAutenticacion(String gmail, String metodo) {
        log.debug("Chequeando método de autenticación '{}' para usuario {}", metodo, gmail);
        Usuario usuario = usuarioRepository.buscarPorGmail(gmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        return switch (metodo.toUpperCase()) {
            case "LOCAL" -> usuario.puedeUsarLocal();
            case "GOOGLE" -> usuario.puedeUsarGoogle();
            default -> {
                log.warn("Método de autenticación no válido: {}", metodo);
                throw new IllegalArgumentException("Método de autenticación no válido: " + metodo);
            }
        };
    }
}
