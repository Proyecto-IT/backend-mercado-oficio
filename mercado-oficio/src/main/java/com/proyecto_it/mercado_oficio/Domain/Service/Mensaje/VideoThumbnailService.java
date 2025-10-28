package com.proyecto_it.mercado_oficio.Domain.Service.Mensaje;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Servicio para generar thumbnails de videos.
 * Nota: Para producción, considera usar FFmpeg con ProcessBuilder
 * Esta implementación es básica y puede requerir dependencias adicionales.
 */
@Slf4j
@Service
public class VideoThumbnailService {

    /**
     * Genera un thumbnail del primer frame de un video
     *
     * NOTA IMPORTANTE: Esta es una implementación básica.
     * Para producción real, necesitarás:
     * 1. Instalar FFmpeg en el servidor
     * 2. Usar ProcessBuilder para ejecutar: ffmpeg -i video.mp4 -ss 00:00:01 -vframes 1 output.jpg
     *
     * Alternativa: Usar librerías como Jaffree, Xuggler o JavaCV (pero son pesadas)
     */
    public String generarThumbnailBase64(byte[] videoData, String tipoContenido) {
        try {
            // Crear archivo temporal
            File tempVideo = File.createTempFile("video_", getExtensionFromMimeType(tipoContenido));
            try (FileOutputStream fos = new FileOutputStream(tempVideo)) {
                fos.write(videoData);
            }

            // Generar thumbnail usando FFmpeg
            File thumbnailFile = File.createTempFile("thumb_", ".jpg");

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", tempVideo.getAbsolutePath(),
                    "-ss", "00:00:01",
                    "-vframes", "1",
                    "-vf", "scale=320:-1", // Reducir tamaño para preview
                    "-y",
                    thumbnailFile.getAbsolutePath()
            );

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0 && thumbnailFile.exists()) {
                // Leer el thumbnail generado
                BufferedImage image = ImageIO.read(thumbnailFile);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", baos);

                String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

                // Limpiar archivos temporales
                tempVideo.delete();
                thumbnailFile.delete();

                return "data:image/jpeg;base64," + base64;
            } else {
                log.warn("No se pudo generar thumbnail para video. Usando imagen por defecto.");
                return generarThumbnailPorDefecto();
            }

        } catch (Exception e) {
            log.error("Error al generar thumbnail de video: {}", e.getMessage());
            return generarThumbnailPorDefecto();
        }
    }

    /**
     * Genera un thumbnail por defecto cuando no se puede extraer el frame del video
     */
    private String generarThumbnailPorDefecto() {
        try {
            // Crear una imagen simple con ícono de video
            BufferedImage image = new BufferedImage(320, 180, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2d = image.createGraphics();

            // Fondo gris
            g2d.setColor(java.awt.Color.DARK_GRAY);
            g2d.fillRect(0, 0, 320, 180);

            // Dibujar ícono de play
            g2d.setColor(java.awt.Color.WHITE);
            int[] xPoints = {120, 120, 200};
            int[] yPoints = {60, 120, 90};
            g2d.fillPolygon(xPoints, yPoints, 3);

            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);

            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.error("Error al generar thumbnail por defecto: {}", e.getMessage());
            return null;
        }
    }

    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType.contains("mp4")) return ".mp4";
        if (mimeType.contains("webm")) return ".webm";
        if (mimeType.contains("avi")) return ".avi";
        if (mimeType.contains("quicktime")) return ".mov";
        return ".mp4";
    }

    /**
     * Verifica si FFmpeg está disponible en el sistema
     */
    public boolean isFFmpegAvailable() {
        try {
            Process process = new ProcessBuilder("ffmpeg", "-version").start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}