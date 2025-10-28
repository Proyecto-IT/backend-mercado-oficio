package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Notificacion;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class NotificacionRequest {
    private String mensaje;
    private String tipo;
    private String destinoUrl;
    private String destinatarioUsername;
}
