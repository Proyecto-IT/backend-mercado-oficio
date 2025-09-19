package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Usuario;


import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Repository.UsuarioRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Usuario.UsuarioUpdate;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.proyecto_it.mercado_oficio.Mapper.Usuario.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final JpaUsuarioRepository jpaRepository;
    private final UsuarioMapper mapper;

    @Override
    public Usuario guardar(Usuario usuario) {
        log.info("Guardando usuario con gmail={}", usuario.getGmail());
        UsuarioEntity entity = mapper.toEntity(usuario);
        UsuarioEntity savedEntity = jpaRepository.save(entity);
        log.info("Usuario guardado con id={}", savedEntity.getId());
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<Usuario> findAll() {
        log.info("Obteniendo todos los usuarios");
        List<Usuario> usuarios = jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
        log.info("{} usuarios obtenidos", usuarios.size());
        return usuarios;
    }

    @Override
    public boolean existePorGmail(String gmail) {
        log.info("Verificando existencia de usuario con gmail={}", gmail);
        boolean existe = jpaRepository.existsByGmail(gmail);
        log.info("Usuario con gmail={} existe={}", gmail, existe);
        return existe;
    }

    @Override
    public Optional<Usuario> buscarPorGmail(String gmail) {
        log.info("Buscando usuario por gmail={}", gmail);
        Optional<Usuario> usuario = jpaRepository.findByGmail(gmail)
                .map(mapper::toDomain);
        if (usuario.isPresent()) {
            log.info("Usuario encontrado con gmail={}", gmail);
        } else {
            log.warn("Usuario no encontrado con gmail={}", gmail);
        }
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
        log.info("Buscando usuario por id={}", id);
        Optional<Usuario> usuario = jpaRepository.findById(id)
                .map(mapper::toDomain);
        if (usuario.isPresent()) {
            log.info("Usuario encontrado con id={}", id);
        } else {
            log.warn("Usuario no encontrado con id={}", id);
        }
        return usuario;
    }

    @Override
    public boolean actualizarUsuario(Usuario usuario) {
        log.info("Actualizando usuario con id={}", usuario.getId());
        try {
            UsuarioEntity entity = mapper.toEntity(usuario);
            jpaRepository.save(entity);
            log.info("Usuario actualizado con id={}", usuario.getId());
            return true;
        } catch (Exception e) {
            log.error("Error actualizando usuario con id={}: {}", usuario.getId(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean modificarPermisoUsuario(int id, int permiso) {
        log.info("Modificando permiso de usuario id={} a permiso={}", id, permiso);
        try {
            jpaRepository.modificarPermiso(id, permiso);
            log.info("Permiso modificado correctamente para usuario id={}", id);
            return true;
        } catch (Exception e) {
            log.error("Error modificando permiso de usuario id={}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void actualizarUsuarioParcial(String gmail, UsuarioUpdate usuarioUpdate) {
        log.info("Actualizando parcialmente usuario con gmail={}", gmail);
        try {
            jpaRepository.actualizarUsuario(
                    gmail,
                    usuarioUpdate.getNombre(),
                    usuarioUpdate.getApellido(),
                    usuarioUpdate.getDireccion(),
                    usuarioUpdate.getCp(),
                    usuarioUpdate.getCiudad(),
                    usuarioUpdate.getTelefono()
            );
            log.info("Usuario parcialmente actualizado con gmail={}", gmail);
        } catch (Exception e) {
            log.error("Error actualizando parcialmente usuario con gmail={}: {}", gmail, e.getMessage(), e);
            throw e;
        }
    }
}
