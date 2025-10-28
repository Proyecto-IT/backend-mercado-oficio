package com.proyecto_it.mercado_oficio.Domain.Service.Notificacion;

import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Notificacion.NotificacionRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Notificacion.NotificacionResponse;
import com.proyecto_it.mercado_oficio.Domain.Repository.NotificacionRepository;
import com.proyecto_it.mercado_oficio.Mapper.Notificacion.NotificacionMapper;
import com.proyecto_it.mercado_oficio.Domain.Model.Notificacion;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final NotificacionMapper mapper;
    private final JpaUsuarioRepository usuarioRepository;

    public NotificacionServiceImpl(NotificacionRepository notificacionRepository,
                                   NotificacionMapper mapper,
                                   JpaUsuarioRepository usuarioRepository) {
        this.notificacionRepository = notificacionRepository;
        this.mapper = mapper;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public NotificacionResponse guardarNotificacion(NotificacionRequest request, String username) {
        UsuarioEntity usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        //limitar 10 notificaciones
        List<Notificacion> existentes = notificacionRepository.obtenerPorUsuario(username);
        if (existentes.size() >= 10) {
            Notificacion masAntigua = existentes.stream()
                    .min(Comparator.comparing(Notificacion::getFechaCreacion))
                    .orElse(null);
            if (masAntigua != null)
                notificacionRepository.eliminar(masAntigua.getId());
        }

        Notificacion notificacion = Notificacion.builder()
                .mensaje(request.getMensaje())
                .tipo(request.getTipo())
                .destinoUrl(request.getDestinoUrl())
                .leida(false)
                .fechaCreacion(LocalDateTime.now())
                .usuarioDestinoId(usuario.getId())
                .build();

        Notificacion guardada = notificacionRepository.guardar(notificacion, usuario);

        NotificacionResponse response = mapper.toResponse(guardada);

        //enviar por WebSocket
        simpMessagingTemplate.convertAndSendToUser(
                String.valueOf(guardada.getUsuarioId()),
                "/queue/notificaciones",
                response
        );

        return response;
    }

    @Override
    public List<NotificacionResponse> obtenerUltimasNotificaciones(String username) {
        return notificacionRepository.obtenerUltimas(username).stream()
                .map(n -> NotificacionResponse.builder()
                        .id(n.getId())
                        .mensaje(n.getMensaje())
                        .tipo(n.getTipo())
                        .destinoUrl(n.getDestinoUrl())
                        .leida(n.isLeida())
                        .fechaCreacion(n.getFechaCreacion())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void marcarComoLeida(Long id) {
        Notificacion notificacion = notificacionRepository.buscarPorId(id);
        if (notificacion != null) {
            notificacion.setLeida(true);
            notificacionRepository.actualizar(notificacion);
        }
    }
}
