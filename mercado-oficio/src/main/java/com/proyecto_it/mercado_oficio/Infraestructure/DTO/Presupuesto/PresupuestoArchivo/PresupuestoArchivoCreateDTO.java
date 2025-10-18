package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo;

import com.proyecto_it.mercado_oficio.Domain.ValueObjects.TipoArchivo;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoArchivoCreateDTO {
    private MultipartFile archivo;

    public void validarArchivo() throws ValidationException {
        if (archivo == null || archivo.isEmpty()) {
            throw new ValidationException("El archivo no puede estar vacío");
        }

        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || nombreOriginal.isEmpty()) {
            throw new ValidationException("El nombre del archivo es inválido");
        }

        double tamaniomB = archivo.getSize() / (1024.0 * 1024.0);
        if (tamaniomB > 10) {
            throw new ValidationException("El archivo no puede exceder 10 MB");
        }

        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf(".") + 1).toLowerCase();
        boolean esImagenValida = extension.matches("(jpg|jpeg|png|gif|bmp)");
        boolean esVideoValido = extension.matches("(mp4|avi|mov|wmv|flv)");

        if (!esImagenValida && !esVideoValido) {
            throw new ValidationException("Formato de archivo no permitido");
        }
    }

    public TipoArchivo obtenerTipoArchivo() {
        String extension = archivo.getOriginalFilename().substring(
                archivo.getOriginalFilename().lastIndexOf(".") + 1).toLowerCase();
        return extension.matches("(mp4|avi|mov|wmv|flv)") ? TipoArchivo.VIDEO : TipoArchivo.IMAGEN;
    }
}
