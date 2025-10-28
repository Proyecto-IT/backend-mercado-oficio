package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Mensaje;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
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
    @JsonProperty("emisor_id")
    private Integer emisorId2;

    @NotNull(message = "El ID del receptor es obligatorio")
    @JsonProperty("receptor_id")
    private Integer receptorId;

    @JsonProperty("contenido")
    private String contenido;
}