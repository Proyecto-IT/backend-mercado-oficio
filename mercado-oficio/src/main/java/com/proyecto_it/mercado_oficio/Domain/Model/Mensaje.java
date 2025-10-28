package com.proyecto_it.mercado_oficio.Domain.Model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mensaje {
    private Long id;
    private Integer emisorId;
    private Integer receptorId;
    private String contenido;
    private LocalDateTime fechaEnvio;
    private List<Long> multimediaIds;

    public void validar(){
        if ( (contenido == null || contenido.isBlank()) && multimediaIds.isEmpty() ) {
            throw new IllegalArgumentException("El mensaje debe contener algo.");
        }
        if (emisorId == null || receptorId == null) {
            throw new IllegalArgumentException("El emisor y receptor son obligatorios.");
        }
        if (emisorId.equals(receptorId)) {
            throw new IllegalArgumentException("El emisor y receptor no pueden ser el mismo.");
        }
    }

    public boolean perteneceAlChat(Integer usuarioId1, Integer usuarioId2) {
        return (emisorId.equals(usuario1Id) && receptorId.equals(usuario2Id)) ||
                (emisorId.equals(usuario2Id) && receptorId.equals(usuario1Id));
    }
}
