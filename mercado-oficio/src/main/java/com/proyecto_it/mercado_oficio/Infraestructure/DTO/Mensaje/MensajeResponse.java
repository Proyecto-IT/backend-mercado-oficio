package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Mensaje;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class MensajeResponse {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("emisor_id")
    private Integer emisorId;

    @JsonProperty("receptor_id")
    private Integer receptorId;

    @JsonProperty("contenido")
    private String contenido;

    @JsonProperty("multimedia_ids")
    private List<Integer> multimediaIds;

    @JsonProperty("fecha_envio")
    private LocalDateTime fechaEnvio;

    @JsonProperty("tiene_archivos")
    private Boolean tieneArchivos;
}