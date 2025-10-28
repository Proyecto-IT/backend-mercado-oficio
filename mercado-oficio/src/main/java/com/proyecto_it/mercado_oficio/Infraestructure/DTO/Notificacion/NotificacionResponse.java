package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Notificacion;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class NotificacionResponse {
    private Long id;
    private String mensaje;
    private String tipo;
    private String destinoUrl;
    private boolean leida;
    private LocalDateTime fechaCreacion;
}
