package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Mensaje;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajeRequest {

    @NotNull(message = "El ID del emisor es obligatorio")
    private Integer emisorId;

    @NotNull(message = "El ID del receptor es obligatorio")
    private Integer receptorId;

    private String contenido;
}