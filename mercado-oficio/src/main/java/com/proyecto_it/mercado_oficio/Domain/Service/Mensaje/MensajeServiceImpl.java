package com.proyecto_it.mercado_oficio.Domain.Service.Mensaje;

import com.proyecto_it.mercado_oficio.Domain.Model.Mensaje;
import com.proyecto_it.mercado_oficio.Domain.Model.Multimedia;
import com.proyecto_it.mercado_oficio.Domain.Repository.MensajeRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.MultimediaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MensajeServiceImpl implements MensajeService {

    private final MensajeRepository mensajeRepository;
    private final MultimediaRepository multimediaRepository;

    @Override
    @Transactional
    public Mensaje enviarMensaje(Mensaje mensaje, List<MultipartFile> archivos) {
        log.info("Enviando mensaje de usuario {} a usuario {}",
                mensaje.getEmisorId(), mensaje.getReceptorId());
        mensaje.validar();

        // Procesar archivos adjuntos si existen
        List<Integer> multimediaIds = new ArrayList<>();
        if (archivos != null && !archivos.isEmpty()) {
            multimediaIds = procesarArchivosAdjuntos(archivos);
            mensaje.setMultimediaIds(multimediaIds);
        }

        Mensaje mensajeGuardado = mensajeRepository.guardar(mensaje);
        log.info("Mensaje guardado con ID: {}", mensajeGuardado.getId());

        return mensajeGuardado;
    }

    @Override
    public Mensaje obtenerMensajePorId(Long id) {
        log.info("Obteniendo mensaje con ID: {}", id);
        return mensajeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado con ID: " + id));
    }

    @Override
    public List<Mensaje> obtenerMensajesDeChat(Integer usuario1Id, Integer usuario2Id) {
        log.info("Obteniendo mensajes del chat entre usuarios {} y {}", usuario1Id, usuario2Id);

        if (usuario1Id == null || usuario2Id == null) {
            throw new IllegalArgumentException("Los IDs de usuario no pueden ser nulos");
        }

        if (usuario1Id.equals(usuario2Id)) {
            throw new IllegalArgumentException("Los usuarios deben ser diferentes");
        }

        List<Mensaje> mensajes = mensajeRepository.findByChat(usuario1Id, usuario2Id);
        log.info("Se encontraron {} mensajes", mensajes.size());

        return mensajes;
    }

    @Override
    public List<Multimedia> obtenerArchivosAdjuntos(Long mensajeId) {
        log.info("Obteniendo archivos adjuntos del mensaje: {}", mensajeId);

        Mensaje mensaje = obtenerMensajePorId(mensajeId);

        if (mensaje.getMultimediaIds() == null || mensaje.getMultimediaIds().isEmpty()) {
            log.info("El mensaje no tiene archivos adjuntos");
            return new ArrayList<>();
        }

        List<Multimedia> archivos = mensajeRepository.getArchivosMensaje(mensajeId);
        log.info("Se encontraron {} archivos adjuntos", archivos.size());

        return archivos;
    }

    public Multimedia obtenerMultimediaCompleto(Integer id) {
        return multimediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public void eliminarMensaje(Long id) {
        log.info("Eliminando mensaje con ID: {}", id);

        // Obtener archivos adjuntos para eliminarlos también
        Mensaje mensaje = obtenerMensajePorId(id);
        if (mensaje.getMultimediaIds() != null && !mensaje.getMultimediaIds().isEmpty()) {
            mensaje.getMultimediaIds().forEach(multimediaRepository::deleteById);
            log.info("Archivos adjuntos eliminados");
        }

        mensajeRepository.deleteById(id);
        log.info("Mensaje eliminado exitosamente");
    }

    private List<Integer> procesarArchivosAdjuntos(List<MultipartFile> archivos) {
        return archivos.stream()
                .map(this::guardarArchivo)
                .collect(Collectors.toList());
    }

    private Integer guardarArchivo(MultipartFile file) {
        try {
            String nombreOriginal = file.getOriginalFilename();
            String extension = nombreOriginal != null && nombreOriginal.contains(".")
                    ? nombreOriginal.substring(nombreOriginal.lastIndexOf(".") + 1)
                    : "";

            Multimedia multimedia = Multimedia.builder()
                    .nombre(nombreOriginal)
                    .tipoContenido(file.getContentType())
                    .extension(extension)
                    .datos(file.getBytes())
                    .build();

            multimedia.validar();

            Multimedia guardado = multimediaRepository.guardar(multimedia);
            log.info("Archivo guardado: {} con ID: {}", nombreOriginal, guardado.getId());

            return guardado.getId();
        } catch (IOException e) {
            log.error("Error al procesar archivo: {}", e.getMessage());
            throw new RuntimeException("Error al procesar archivo", e);
        }
    }
}