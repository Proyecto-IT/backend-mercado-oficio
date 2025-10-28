package com.proyecto_it.mercado_oficio.Domain.Service.TokenVerificacion;

import com.proyecto_it.mercado_oficio.Domain.Model.TokenVerificacion;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.TokenVerificacion.TokenVerificacionEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;

import java.util.Optional;

public interface TokenVerificacionService {
    TokenVerificacion crearTokenParaUsuario(Integer usuarioId);
    Optional<TokenVerificacion> validarToken(String token);
}
