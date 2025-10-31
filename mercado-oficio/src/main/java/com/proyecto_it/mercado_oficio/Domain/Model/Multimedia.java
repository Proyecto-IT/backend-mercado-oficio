package com.proyecto_it.mercado_oficio.Domain.Model;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Multimedia {
    private Integer id;
    private String nombre;
    private String tipoContenido; //MIME
    private String extension;
    @ToString.Exclude
    private byte[] datos;

    // Validación de tipos de archivo permitidos
    private static final long MAX_FILE_SIZE = 200 * 1024 * 1024; // 200MB
    private static final String[] ALLOWED_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "video/mp4", "video/webm",
            "audio/mpeg", "audio/wav"
    };

    public void validar() {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Es obligatorio un nombre.");
        }
        if (tipoContenido == null) {
            throw new IllegalArgumentException("El tipo de contenido es obligatorio.");
        }
        if (datos == null || datos.length == 0) {
            throw new IllegalArgumentException("El archivo debe tener contenido.");
        }
        if (datos.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo debe pesar hasta el máximo permitido.");
        }
        if (Arrays.asList(ALLOWED_TYPES).contains(tipoContenido)) {
            throw new IllegalArgumentException("El tipo de contenido debe ser válido.");
        }
    }

    public long getTamano() {
        return datos != null ? datos.length : 0;
    }

    public boolean esImagen() {
        return tipoContenido != null && tipoContenido.startsWith("image/");
    }
}