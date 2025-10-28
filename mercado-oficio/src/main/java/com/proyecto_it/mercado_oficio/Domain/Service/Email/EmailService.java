package com.proyecto_it.mercado_oficio.Domain.Service.Email;

import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;

import java.util.Map;

public interface EmailService {
    void enviarEmailVerificacion(Usuario usuario);
    void enviarEmailRestablecimientoPassword(String gmail);
    void enviarEmailCambioEmail(String gmailActual, String nuevoEmail);
    void confirmarCambioEmail(String token);
}
