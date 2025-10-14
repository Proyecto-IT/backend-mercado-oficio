package com.proyecto_it.mercado_oficio.Mapper.Usuario;

import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Usuario.*;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) return null;

        return Usuario.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .apellido(entity.getApellido())
                .gmail(entity.getGmail())
                .password(entity.getPassword())
                .permiso(entity.getPermiso())
                .verificado(entity.isVerificado())
                .proveedor(entity.getProveedor())
                .direccion(entity.getDireccion())
                .cp(entity.getCp())
                .ciudad(entity.getCiudad())
                .telefono(entity.getTelefono())
                .imagen(entity.getImagen())
                .imagenTipo(entity.getImagenTipo())
                .build();
    }

    public UsuarioEntity toEntity(Usuario domain) {
        if (domain == null) return null;

        return UsuarioEntity.builder()
                .id(domain.getId())
                .nombre(domain.getNombre())
                .apellido(domain.getApellido())
                .gmail(domain.getGmail())
                .password(domain.getPassword())
                .permiso(domain.getPermiso())
                .verificado(domain.isVerificado())
                .proveedor(domain.getProveedor())
                .direccion(domain.getDireccion())
                .cp(domain.getCp())
                .ciudad(domain.getCiudad())
                .telefono(domain.getTelefono())
                .imagen(domain.getImagen())
                .imagenTipo(domain.getImagenTipo())
                .build();
    }

    public Usuario fromRegistroRequest(UsuarioRegistroRequest request) {
        if (request == null) return null;

        return Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .gmail(request.getGmail())
                .password(request.getPassword())
                .permiso(0)
                .verificado(false)
                .build();
    }

    public UsuarioGetUpdateResponse toGetUpdateResponse(Usuario usuario) {
        if (usuario == null) return null;

        return UsuarioGetUpdateResponse.builder()
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .gmail(usuario.getGmail())
                .direccion(usuario.getDireccion())
                .cp(usuario.getCp())
                .ciudad(usuario.getCiudad())
                .telefono(usuario.getTelefono())
                .build();
    }

    public CambioPasswordRequest toCambioPasswordRequest(CambioPasswordDto dto) {
        if (dto == null) return null;

        return new CambioPasswordRequest(
                dto.getPasswordActual(),
                dto.getNuevaPassword()
        );
    }

    public UsuarioUpdate toUsuarioUpdate(UsuarioUpdateRequest request) {
        if (request == null) return null;

        return new UsuarioUpdate(
                request.getNombre(),
                request.getApellido(),
                request.getDireccion(),
                request.getCp(),
                request.getCiudad(),
                request.getTelefono()
        );
    }

    public UsuarioGetUpdateResponse toGetUpdateResponseFromDomain(Usuario usuario) {
        return toGetUpdateResponse(usuario);
    }
}