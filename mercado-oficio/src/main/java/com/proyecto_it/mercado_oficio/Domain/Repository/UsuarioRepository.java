package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Usuario.UsuarioUpdate;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {
    Usuario guardar(Usuario usuario);
    List<Usuario> findAll();
    boolean existePorGmail(String gmail);
    Optional<Usuario> buscarPorGmail(String gmail);
    Optional<Usuario> buscarPorId(Integer id);
    boolean actualizarUsuario(Usuario usuario);
    boolean modificarPermisoUsuario(int id, int permiso);
    void actualizarUsuarioParcial(String gmail, UsuarioUpdate usuarioUpdate);
}
