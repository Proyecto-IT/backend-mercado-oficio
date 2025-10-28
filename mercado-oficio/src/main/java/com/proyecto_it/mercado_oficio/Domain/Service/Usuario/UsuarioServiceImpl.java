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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioCacheService cacheService;
    private final PasswordHasher passwordHasher;
    private final EmailService emailService;

    @Override
    public Usuario registrarUsuario(Usuario usuario) {
        log.info("Registrando usuario con Gmail: {}", usuario.getGmail());

        try {
            usuario.validar();

            if (cacheService.existePorGmailCached(usuario.getGmail())) {
                log.warn("Intento de registro fallido: Gmail {} ya existe", usuario.getGmail());
                throw new IllegalArgumentException("Ya existe un usuario con ese Gmail.");
            }

            String hash = passwordHasher.hash(usuario.getPassword());
            usuario.setPassword(hash);
            usuario.setPermiso(0);
            usuario.setVerificado(false);

            Usuario usuarioGuardado = usuarioRepository.guardar(usuario);

            cacheService.cachearUsuario(usuarioGuardado);
            cacheService.agregarUsuarioALista(usuarioGuardado);

            emailService.enviarEmailVerificacion(usuarioGuardado);
            log.info("Usuario registrado exitosamente: {}", usuario.getGmail());

            return usuarioGuardado;
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al registrar usuario: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al registrar usuario {}: {}", usuario.getGmail(), e.getMessage(), e);
            throw new RuntimeException("Error al registrar usuario", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        log.debug("Listando todos los usuarios desde cache");
        try {
            return cacheService.listarTodosCached();
        } catch (Exception e) {
            log.error("Error al listar usuarios: {}", e.getMessage(), e);
            throw new RuntimeException("Error al listar usuarios", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorGmail(String gmail) {
        try {
            boolean existe = cacheService.existePorGmailCached(gmail);
            log.debug("Chequeo existencia de Gmail {}: {}", gmail, existe);
            return existe;
        } catch (Exception e) {
            log.error("Error al verificar existencia de Gmail {}: {}", gmail, e.getMessage(), e);
            throw new RuntimeException("Error al verificar existencia de usuario", e);
        }
    }

    @Override
    public Usuario actualizarUsuarioPorGmail(String gmail, UsuarioUpdate usuarioUpdate) {
        log.info("Actualizando usuario por Gmail: {}", gmail);

        try {
            Usuario usuario = cacheService.buscarPorGmailCached(gmail)
                    .orElseThrow(() -> new IllegalArgumentException("El usuario a actualizar no existe"));

            usuarioRepository.actualizarUsuarioParcial(gmail, usuarioUpdate);

            Usuario actualizado = usuarioRepository.buscarPorGmail(gmail)
                    .orElseThrow(() -> new RuntimeException("Error al obtener el usuario actualizado"));

            cacheService.cachearUsuario(actualizado);
            cacheService.cachearUsuario(actualizado);

            log.info("Usuario {} actualizado correctamente", gmail);
            return actualizado;
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al actualizar usuario {}: {}", gmail, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar usuario {}: {}", gmail, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar usuario", e);
        }
    }

    @Override
    public Usuario actualizarUsuario(Usuario usuario) {
        log.info("Actualizando usuario con ID {}", usuario.getId());

        try {
            if (usuario.getId() == null || cacheService.buscarPorIdCached(usuario.getId()).isEmpty()) {
                log.warn("Intento de actualización fallido: usuario con ID {} no existe", usuario.getId());
                throw new IllegalArgumentException("El usuario no existe para actualizar.");
            }

            Usuario actualizado = usuarioRepository.guardar(usuario);

            cacheService.cachearUsuario(actualizado);
            cacheService.cachearUsuario(actualizado);

            log.info("Usuario ID {} actualizado correctamente", usuario.getId());
            return actualizado;
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al actualizar usuario ID {}: {}", usuario.getId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar usuario ID {}: {}", usuario.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al actualizar usuario", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorGmail(String gmail) {
        log.debug("Buscando usuario por Gmail desde cache: {}", gmail);
        try {
            return cacheService.buscarPorGmailCached(gmail);
        } catch (Exception e) {
            log.error("Error al buscar usuario por Gmail {}: {}", gmail, e.getMessage(), e);
            throw new RuntimeException("Error al buscar usuario", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Integer id) {
        log.debug("Buscando usuario por ID desde cache: {}", id);
        try {
            return cacheService.buscarPorIdCached(id);
        } catch (Exception e) {
            log.error("Error al buscar usuario por ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error al buscar usuario", e);
        }
    }

    @Override
    public void cambiarPassword(String gmail, CambioPasswordRequest request) {
        log.info("Cambiando contraseña del usuario: {}", gmail);

        try {
            request.validar();

            Usuario usuario = cacheService.buscarPorGmailCached(gmail)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            if (!passwordHasher.matches(request.getPasswordActual(), usuario.getPassword())) {
                log.warn("Contraseña actual incorrecta para usuario: {}", gmail);
                throw new IllegalArgumentException("La contraseña actual es incorrecta.");
            }

            usuario.setPassword(passwordHasher.hash(request.getNuevaPassword()));
            Usuario actualizado = usuarioRepository.guardar(usuario);

            cacheService.cachearUsuario(actualizado);
            cacheService.actualizarUsuarioEnLista(actualizado);

            log.info("Contraseña actualizada correctamente para usuario: {}", gmail);
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al cambiar contraseña de {}: {}", gmail, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al cambiar contraseña de {}: {}", gmail, e.getMessage(), e);
            throw new RuntimeException("Error al cambiar contraseña", e);
        }
    }

    @Override
    public void modificarPermisoUsuario(int id, int permiso) {
        log.info("Modificando permiso del usuario con ID {} a {}", id, permiso);

        try {
            Usuario usuario = cacheService.buscarPorIdCached(id)
                    .orElseThrow(() -> new IllegalArgumentException("El usuario a modificar no existe"));

            usuarioRepository.modificarPermisoUsuario(id, permiso);

            Usuario actualizado = usuarioRepository.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Error al obtener usuario actualizado"));

            cacheService.cachearUsuario(actualizado);
            cacheService.cachearUsuario(actualizado);

            log.info("Permiso modificado correctamente para usuario ID {}", id);
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al modificar permiso de usuario ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al modificar permiso de usuario ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error al modificar permiso de usuario", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String buscarGmailPorId(int id) {
        try {
            Usuario usuario = cacheService.buscarPorIdCached(id)
                    .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

            log.debug("Gmail del usuario ID {}: {}", id, usuario.getGmail());
            return usuario.getGmail();
        } catch (IllegalArgumentException e) {
            log.error("Usuario ID {} no encontrado: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al buscar Gmail por ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error al buscar Gmail de usuario", e);
        }
    }

    @Override
    public void vincularGoogleAUsuario(String gmail) {
        log.info("Vinculando Google a usuario: {}", gmail);

        try {
            Usuario usuario = cacheService.buscarPorGmailCached(gmail)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            if (usuario.puedeUsarGoogle()) {
                log.info("Usuario {} ya tiene vinculación con Google", gmail);
                return;
            }

            usuario.vincularGoogle();
            Usuario actualizado = usuarioRepository.guardar(usuario);

            cacheService.cachearUsuario(actualizado);
            cacheService.actualizarUsuarioEnLista(actualizado);

            log.info("Usuario {} vinculado con Google exitosamente. Proveedor: {}", gmail, actualizado.getProveedor());
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al vincular Google a {}: {}", gmail, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al vincular Google a {}: {}", gmail, e.getMessage(), e);
            throw new RuntimeException("Error al vincular cuenta con Google", e);
        }
    }

    @Override
    public Usuario establecerPasswordLocalAUsuario(String gmail, String password) {
        log.info("Estableciendo contraseña local para usuario: {}", gmail);

        try {
            Usuario usuario = cacheService.buscarPorGmailCached(gmail)
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

            cacheService.cachearUsuario(usuarioActualizado);
            cacheService.actualizarUsuarioEnLista(usuarioActualizado);

            log.info("Contraseña local establecida para usuario: {}. Ahora es híbrido: {}",
                    gmail, usuarioActualizado.esUsuarioHibrido());
            return usuarioActualizado;
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al establecer password local para {}: {}", gmail, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al establecer password local para {}: {}", gmail, e.getMessage(), e);
            throw new RuntimeException("Error al establecer contraseña local", e);
        }
    }
    @Override
    @Transactional
    public boolean actualizarImagenPerfil(String gmail, MultipartFile imagen) {
        try {
            log.info("Actualizando imagen de perfil para usuario: {}", gmail);

            Usuario usuario = usuarioRepository.buscarPorGmail(gmail)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + gmail));

            byte[] imagenBytes = imagen.getBytes();
            String imagenTipo = imagen.getContentType();

            Usuario usuarioActualizado = Usuario.builder()
                    .id(usuario.getId())
                    .nombre(usuario.getNombre())
                    .apellido(usuario.getApellido())
                    .gmail(usuario.getGmail())
                    .password(usuario.getPassword())
                    .permiso(usuario.getPermiso())
                    .verificado(usuario.isVerificado())
                    .proveedor(usuario.getProveedor())
                    .direccion(usuario.getDireccion())
                    .cp(usuario.getCp())
                    .ciudad(usuario.getCiudad())
                    .telefono(usuario.getTelefono())
                    .imagen(imagenBytes)
                    .imagenTipo(imagenTipo)
                    .build();

            boolean actualizado = usuarioRepository.actualizarUsuario(usuarioActualizado);

            if (actualizado) {
                cacheService.sincronizarDespuesDeActualizar(usuarioActualizado);
                log.info("Imagen de perfil actualizada y cache sincronizado para: {}", gmail);
            }

            return actualizado;

        } catch (IOException e) {
            log.error("Error al leer imagen: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar la imagen", e);
        } catch (Exception e) {
            log.error("Error al actualizar imagen de perfil: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar imagen de perfil", e);
        }
    }

    /**
     * Elimina la imagen de perfil del usuario
     */
    @Override
    @Transactional
    public boolean eliminarImagenPerfil(String gmail) {
        try {
            log.info("🗑️ Eliminando imagen de perfil para usuario: {}", gmail);

            // Buscar usuario
            Usuario usuario = usuarioRepository.buscarPorGmail(gmail)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + gmail));

            // Verificar que tenga imagen
            if (usuario.getImagen() == null || usuario.getImagen().length == 0) {
                log.warn("⚠️ El usuario {} no tiene imagen para eliminar", gmail);
                return false;
            }

            // Actualizar usuario sin imagen
            Usuario usuarioSinImagen = Usuario.builder()
                    .id(usuario.getId())
                    .nombre(usuario.getNombre())
                    .apellido(usuario.getApellido())
                    .gmail(usuario.getGmail())
                    .password(usuario.getPassword())
                    .permiso(usuario.getPermiso())
                    .verificado(usuario.isVerificado())
                    .proveedor(usuario.getProveedor())
                    .direccion(usuario.getDireccion())
                    .cp(usuario.getCp())
                    .ciudad(usuario.getCiudad())
                    .telefono(usuario.getTelefono())
                    .imagen(null)  // 🔥 Eliminar imagen
                    .imagenTipo(null)
                    .build();

            // Guardar en BD
            boolean eliminado = usuarioRepository.actualizarUsuario(usuarioSinImagen);

            if (eliminado) {
                // Actualizar cache
                cacheService.sincronizarDespuesDeActualizar(usuarioSinImagen);
                log.info("✅ Imagen de perfil eliminada y cache sincronizado para: {}", gmail);
            }

            return eliminado;

        } catch (Exception e) {
            log.error("❌ Error al eliminar imagen de perfil: {}", e.getMessage(), e);
            throw new RuntimeException("Error al eliminar imagen de perfil", e);
        }
    }

    /**
     * Verifica si un usuario tiene imagen de perfil
     */
    @Override
    public boolean tieneImagenPerfil(String gmail) {
        try {
            Usuario usuario = usuarioRepository.buscarPorGmail(gmail)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + gmail));

            return usuario.getImagen() != null && usuario.getImagen().length > 0;

        } catch (Exception e) {
            log.error("❌ Error al verificar imagen de perfil: {}", e.getMessage(), e);
            return false;
        }
    }
    @Override
    @Transactional(readOnly = true)
    public boolean puedeUsarMetodoAutenticacion(String gmail, String metodo) {
        log.debug("Chequeando método de autenticación '{}' para usuario {}", metodo, gmail);

        try {
            // Buscar usando cache
            Usuario usuario = cacheService.buscarPorGmailCached(gmail)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            return switch (metodo.toUpperCase()) {
                case "LOCAL" -> usuario.puedeUsarLocal();
                case "GOOGLE" -> usuario.puedeUsarGoogle();
                default -> {
                    log.warn("Método de autenticación no válido: {}", metodo);
                    throw new IllegalArgumentException("Método de autenticación no válido: " + metodo);
                }
            };
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al verificar método de autenticación para {}: {}", gmail, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al verificar método de autenticación para {}: {}", gmail, e.getMessage(), e);
            throw new RuntimeException("Error al verificar método de autenticación", e);
        }
    }
}