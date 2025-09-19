package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.Model.RefreshToken;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken guardar(RefreshToken token);
    Optional<RefreshToken> buscarPorTokenYEstado(String token, String estado);
    void expirarTokensPorUsuario(Integer usuarioId);
    List<RefreshToken> buscarPorUsuarioYEstado(Integer usuarioId, String estado);
}