package com.proyecto_it.mercado_oficio.Mapper.Mensaje;

import com.proyecto_it.mercado_oficio.Domain.Model.Mensaje;
import com.proyecto_it.mercado_oficio.Domain.Model.Multimedia;
import com.proyecto_it.mercado_oficio.Domain.Service.Mensaje.VideoThumbnailService;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Mensaje.MensajeRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Mensaje.MensajeResponse;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Mensaje.MensajeResponseConArchivos;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Mensaje.MultimediaDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Mensaje.MensajeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MensajeMapper {

    private final VideoThumbnailService videoThumbnailService;

    public Mensaje toDomain(MensajeEntity entity) {
        if (entity == null) return null;

        return Mensaje.builder()
                .id(entity.getId())
                .emisorId(entity.getEmisorId())
                .receptorId(entity.getReceptorId())
                .contenido(entity.getContenido())
                .multimediaIds(entity.getMultimediaIds())
                .fechaEnvio(entity.getFechaEnvio())
                .build();
    }

    public MensajeEntity toEntity(Mensaje domain) {
        if (domain == null) return null;

        return MensajeEntity.builder()
                .id(domain.getId())
                .emisorId(domain.getEmisorId())
                .receptorId(domain.getReceptorId())
                .contenido(domain.getContenido())
                .multimediaIds(domain.getMultimediaIds())
                .fechaEnvio(domain.getFechaEnvio())
                .build();
    }

    /**
     * Convierte a Response con archivos y previews
     */
    public MensajeResponseConArchivos toResponseConArchivos(Mensaje domain, List<Multimedia> archivos) {
        if (domain == null) return null;

        List<MultimediaDTO> archivosDTO = archivos != null
                ? archivos.stream()
                .map(this::toMultimediaDTO)
                .collect(Collectors.toList())
                : null;

        return MensajeResponseConArchivos.builder()
                .id(domain.getId())
                .emisorId(domain.getEmisorId())
                .receptorId(domain.getReceptorId())
                .contenido(domain.getContenido())
                .fechaEnvio(domain.getFechaEnvio())
                .archivos(archivosDTO)
                .build();
    }

    /**
     * Convierte Multimedia a DTO con preview según el tipo
     */
    public MultimediaDTO toMultimediaDTO(Multimedia multimedia) {
        if (multimedia == null) return null;

        MultimediaDTO.TipoArchivo tipo = determinarTipoArchivo(multimedia.getTipoContenido());
        String base64Preview = null;
        String thumbnailBase64 = null;

        // Para imágenes: base64 completo (hasta 5MB, luego thumbnail)
        if (tipo == MultimediaDTO.TipoArchivo.IMAGEN) {
            if (multimedia.getTamano() <= 5 * 1024 * 1024) { // 5MB
                base64Preview = "data:" + multimedia.getTipoContenido() + ";base64," +
                        Base64.getEncoder().encodeToString(multimedia.getDatos());
            } else {
                // Para imágenes muy grandes, generar thumbnail
                base64Preview = generarThumbnailImagen(multimedia.getDatos(), multimedia.getTipoContenido());
            }
        }

        // Para videos: generar thumbnail del primer frame
        if (tipo == MultimediaDTO.TipoArchivo.VIDEO) {
            thumbnailBase64 = videoThumbnailService.generarThumbnailBase64(
                    multimedia.getDatos(),
                    multimedia.getTipoContenido()
            );
        }

        return MultimediaDTO.builder()
                .id(multimedia.getId())
                .nombre(multimedia.getNombre())
                .extension(multimedia.getExtension())
                .tamano(multimedia.getTamano())
                .urlDescarga("/api/chat/archivo/" + multimedia.getId())
                .TipoArchivo(tipo)
                .base64Preview(base64Preview)
                .thumbnailBase64(thumbnailBase64)
                .build();
    }

    private MultimediaDTO.TipoArchivo determinarTipoArchivo(String tipoContenido) {
        if (tipoContenido == null) return MultimediaDTO.TipoArchivo.OTRO;

        if (tipoContenido.startsWith("image/")) return MultimediaDTO.TipoArchivo.IMAGEN;
        if (tipoContenido.startsWith("video/")) return MultimediaDTO.TipoArchivo.VIDEO;
        if (tipoContenido.startsWith("audio/")) return MultimediaDTO.TipoArchivo.AUDIO;
        if (tipoContenido.contains("pdf") ||
                tipoContenido.contains("document") ||
                tipoContenido.contains("word") ||
                tipoContenido.contains("excel") ||
                tipoContenido.contains("powerpoint")) {
            return MultimediaDTO.TipoArchivo.DOCUMENTO;
        }

        return MultimediaDTO.TipoArchivo.OTRO;
    }

    private String generarThumbnailImagen(byte[] datos, String tipoContenido) {
        try {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(datos);
            java.awt.image.BufferedImage originalImage = javax.imageio.ImageIO.read(bais);

            if (originalImage == null) return null;

            // Redimensionar a 320px de ancho manteniendo proporción
            int targetWidth = 320;
            int targetHeight = (int) ((double) originalImage.getHeight() / originalImage.getWidth() * targetWidth);

            java.awt.image.BufferedImage resizedImage = new java.awt.image.BufferedImage(
                    targetWidth, targetHeight, java.awt.image.BufferedImage.TYPE_INT_RGB
            );

            java.awt.Graphics2D g2d = resizedImage.createGraphics();
            g2d.setRenderingHint(
                    java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );
            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(resizedImage, "jpg", baos);

            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    public Mensaje toDomain(MensajeRequest request) {
        if (request == null) return null;

        return Mensaje.builder()
                .emisorId(request.getEmisorId())
                .receptorId(request.getReceptorId())
                .contenido(request.getContenido())
                .build();
    }
}