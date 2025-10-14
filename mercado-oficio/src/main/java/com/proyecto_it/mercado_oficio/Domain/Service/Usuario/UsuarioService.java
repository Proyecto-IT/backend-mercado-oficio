package com.proyecto_it.mercado_oficio.Domain.Service.Usuario;

import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Usuario.CambioPasswordRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Usuario.UsuarioUpdate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    Usuario registrarUsuario(Usuario usuario);
    List<Usuario> listarTodos();
    boolean existePorGmail(String gmail);
    Usuario actualizarUsuarioPorGmail(String gmail, UsuarioUpdate usuarioUpdate);
    Usuario actualizarUsuario(Usuario usuario);
    Optional<Usuario> buscarPorGmail(String gmail);
    Optional<Usuario> buscarPorId(Integer id); // Agregado método faltante
    void cambiarPassword(String gmail, CambioPasswordRequest request);
    void modificarPermisoUsuario(int id, int permiso); // Corregido tipo de retorno
    String buscarGmailPorId(int id); // Corregido nombre del método
    // Métodos para autenticación híbrida
    void vincularGoogleAUsuario(String gmail);
    Usuario establecerPasswordLocalAUsuario(String gmail, String password);
    boolean puedeUsarMetodoAutenticacion(String gmail, String metodo);
    boolean actualizarImagenPerfil(String gmail, MultipartFile imagen);
    boolean eliminarImagenPerfil(String gmail);
    boolean tieneImagenPerfil(String gmail);
}


