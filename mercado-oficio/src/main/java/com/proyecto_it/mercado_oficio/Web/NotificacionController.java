package com.proyecto_it.mercado_oficio.Infraestructure.Web.Controller;

import com.proyecto_it.mercado_oficio.Domain.Model.Notificacion;
import com.proyecto_it.mercado_oficio.Domain.Service.Notificacion.NotificacionService;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Notificacion.NotificacionRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Notificacion.NotificacionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @PostMapping
    public ResponseEntity<NotificacionResponse> enviar(@RequestBody NotificacionRequest request) {
        Notificacion noti = Notificacion.builder()
                .usuarioId(request.getUsuarioId())
                .titulo(request.getTitulo())
                .mensaje(request.getMensaje())
                .build();

        Notificacion enviada = notificacionService.enviar(noti);
        return ResponseEntity.ok(new NotificacionResponse(
                enviada.getId(), enviada.getTitulo(), enviada.getMensaje(),
                enviada.isLeida(), enviada.getFechaCreacion()
        ));
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<List<NotificacionResponse>> obtenerUltimas(@PathVariable Integer usuarioId) {
        List<NotificacionResponse> response = notificacionService.obtenerUltimas(usuarioId)
                .stream()
                .map(n -> new NotificacionResponse(
                        n.getId(), n.getTitulo(), n.getMensaje(),
                        n.isLeida(), n.getFechaCreacion()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @MessageMapping("/notificacion")
    public void recibirNotificacion(NotificacionRequest request) {
        Notificacion noti = Notificacion.builder()
                .usuarioId(request.getUsuarioId())
                .titulo(request.getTitulo())
                .mensaje(request.getMensaje())
                .build();

        notificacionService.enviar(noti);
    }

    @PatchMapping("/{id}/leida")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id) {
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.ok().build();
    }
}
