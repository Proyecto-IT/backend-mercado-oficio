package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.Model.TokenVerificacion;

import java.util.List;
import java.util.Optional;

public interface TokenVerificacionRepository {
    TokenVerificacion guardar(TokenVerificacion token);
    Optional<TokenVerificacion> buscarPorToken(String token);
    Optional<TokenVerificacion> buscarPorTokenValido(String token);
    void eliminarTokensExpirados();
    void eliminarTokensPorUsuario(Integer usuarioId);
    List<TokenVerificacion> buscarPorUsuario(Integer usuarioId);
}

