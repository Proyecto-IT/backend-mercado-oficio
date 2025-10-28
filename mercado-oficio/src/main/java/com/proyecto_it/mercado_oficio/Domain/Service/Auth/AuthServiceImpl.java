package com.proyecto_it.mercado_oficio.Domain.Service.Auth;

import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Service.Usuario.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    private final UsuarioService usuarioService;

    public Map<String, Object> obtenerInfoUsuario(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No autenticado");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioService.buscarPorGmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return Map.of(
                "nombre", usuario.getNombre(),
                "apellido", usuario.getApellido(),
                "gmail", usuario.getGmail(),
                "rol", switch (usuario.getPermiso()) {
                    case 0 -> "CLIENTE";
                    case 1 -> "ADMIN";
                    case 2 -> "TRABAJADOR";
                    default -> "DESCONOCIDO";
                }
        );
    }
}
