package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Mensaje;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultimediaDTO {
    private Integer id;
    private String nombre;
    private String tipoContenido;
    private String extension;
    private Long tamano;

    // Para imágenes: base64 completo para mostrar directamente
    private String base64Preview;

    // Para videos: primer frame en base64
    private String thumbnailBase64;

    // Indica el tipo de archivo para el frontend
    private TipoArchivo tipoArchivo;

    public enum TipoArchivo {
        IMAGEN,      // Mostrar directamente con base64Preview
        VIDEO,       // Mostrar thumbnail, descargar al hacer click
        AUDIO,       // Mostrar player o botón de descarga
        DOCUMENTO,   // Botón de descarga
        OTRO         // Botón de descarga
    }
}