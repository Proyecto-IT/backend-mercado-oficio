package com.proyecto_it.mercado_oficio.Domain.Model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {
    private Long id;
    private String mensaje;
    private String destinoUrl;
    private boolean leida;
    private LocalDateTime fechaCreacion;
    private Integer usuarioDestinoId;
}
