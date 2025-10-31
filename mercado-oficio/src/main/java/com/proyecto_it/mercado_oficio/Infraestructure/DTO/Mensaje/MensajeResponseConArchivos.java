package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Mensaje;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajeResponseConArchivos {
    private Long id;
    private Integer emisorId;
    private Integer receptorId;
    private String contenido;
    private List<MultimediaDTO> archivos;
    private LocalDateTime fechaEnvio;
}