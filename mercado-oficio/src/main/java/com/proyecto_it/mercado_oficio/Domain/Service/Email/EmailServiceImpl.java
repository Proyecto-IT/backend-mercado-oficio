package com.proyecto_it.mercado_oficio.Domain.Service.Email;


import com.proyecto_it.mercado_oficio.Domain.Model.TokenVerificacion;
import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Repository.UsuarioRepository;
import com.proyecto_it.mercado_oficio.Domain.Service.TokenVerificacion.TokenVerificacionService;
import com.proyecto_it.mercado_oficio.Security.SecurityConfig.JWT.JwtSpecialTokenService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

import javax.naming.Context;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final JwtSpecialTokenService jwtSpecialTokenService;
    private final TokenVerificacionService tokenService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void enviarEmailVerificacion(Usuario usuario) {
        log.info("Generando email de verificación para usuario: {}", usuario.getGmail());

        TokenVerificacion token = tokenService.crearTokenParaUsuario(usuario.getId());

        // ⭐ CAMBIA ESTA LÍNEA - Apunta a la ruta de React
        String link = frontendUrl + "/validate-email?token=" + token.getToken();

        Map<String, Object> variables = Map.of(
                "nombre", usuario.getNombre(),
                "urlValidacion", link
        );

        String asunto = "Verifica tu cuenta";
        String plantilla = "email-verificacion";

        enviarEmailHtml(usuario.getGmail(), asunto, variables, plantilla);

        log.info("Email de verificación enviado a: {}", usuario.getGmail());
    }

    @Override
    public void enviarEmailRestablecimientoPassword(String gmail) {
        log.info("Generando email de restablecimiento de contraseña para usuario: {}", gmail);

        Usuario usuario = usuarioRepository.buscarPorGmail(gmail)
                .orElseThrow(() -> {
                    log.warn("Usuario '{}' no encontrado para restablecimiento de contraseña", gmail);
                    return new IllegalArgumentException("Usuario no encontrado");
                });

        String token = jwtSpecialTokenService.generateResetPasswordToken(gmail);
        String link = frontendUrl + "/usuario/confirmacion-password/confirmar-password.html?token=" + token;

        Map<String, Object> variables = Map.of("link", link);
        String asunto = "Restablecimiento de contraseña";

        enviarEmailHtml(gmail, asunto, variables, "reset-password");

        log.info("Email de restablecimiento de contraseña enviado a: {}", gmail);
    }

    @Override
    public void enviarEmailCambioEmail(String gmailActual, String nuevoEmail) {
        log.info("Generando email de confirmación de cambio de email: {} -> {}", gmailActual, nuevoEmail);

        Usuario usuario = usuarioRepository.buscarPorGmail(gmailActual)
                .orElseThrow(() -> {
                    log.warn("Usuario '{}' no encontrado para cambio de email", gmailActual);
                    return new IllegalArgumentException("Usuario no encontrado");
                });

        if (usuarioRepository.existePorGmail(nuevoEmail)) {
            log.warn("El nuevo email '{}' ya está en uso", nuevoEmail);
            throw new IllegalArgumentException("El nuevo email ya está en uso");
        }

        String token = jwtSpecialTokenService.generateConfirmEmailToken(nuevoEmail, gmailActual);
        String link = frontendUrl + "/usuario/confirmacion/confirmar-email.html?token=" + token;

        Map<String, Object> variables = Map.of("link", link);
        String asunto = "Confirmación de cambio de email";

        enviarEmailHtml(nuevoEmail, asunto, variables, "confirmar-email");

        log.info("Email de confirmación de cambio de email enviado a: {}", nuevoEmail);
    }

    @Override
    public void confirmarCambioEmail(String token) {
        log.info("Confirmando cambio de email con token: {}", token);

        String nuevoEmail = jwtSpecialTokenService.getSubjectFromToken(token);
        String gmailActual = jwtSpecialTokenService.getClaimFromToken(token, "gmailActual", String.class);

        if (nuevoEmail == null || gmailActual == null) {
            log.warn("Token inválido o mal formado para confirmar cambio de email");
            throw new IllegalArgumentException("Token inválido o mal formado");
        }

        if (!jwtSpecialTokenService.validateConfirmEmailToken(token, nuevoEmail)) {
            log.warn("Token inválido o expirado para cambio de email: {}", token);
            throw new IllegalArgumentException("Token inválido o expirado");
        }

        Usuario usuario = usuarioRepository.buscarPorGmail(gmailActual)
                .orElseThrow(() -> {
                    log.warn("Usuario '{}' no encontrado durante confirmación de cambio de email", gmailActual);
                    return new IllegalArgumentException("Usuario no encontrado");
                });

        if (usuarioRepository.existePorGmail(nuevoEmail)) {
            log.warn("El nuevo email '{}' ya está en uso", nuevoEmail);
            throw new IllegalArgumentException("El nuevo email ya está en uso");
        }

        usuario.setGmail(nuevoEmail);
        usuarioRepository.guardar(usuario);

        log.info("Cambio de email confirmado exitosamente: {} -> {}", gmailActual, nuevoEmail);
    }

    private void enviarEmailHtml(String destinatario, String asunto, Map<String, Object> variables, String plantilla) {
        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        context.setVariables(variables);
        String contenidoHtml = templateEngine.process(plantilla, context);

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);
            mailSender.send(mensaje);

            log.info("Correo enviado a {} con asunto '{}'", destinatario, asunto);
        } catch (MessagingException e) {
            log.error("Error al enviar correo a {}: {}", destinatario, e.getMessage(), e);
            throw new RuntimeException("Error al enviar correo", e);
        }
    }
}
