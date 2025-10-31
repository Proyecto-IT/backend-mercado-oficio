package com.proyecto_it.mercado_oficio.Web;

import com.proyecto_it.mercado_oficio.Domain.Model.Mensaje;
import com.proyecto_it.mercado_oficio.Domain.Model.Multimedia;
import com.proyecto_it.mercado_oficio.Domain.Service.Mensaje.MensajeService;
import com.proyecto_it.mercado_oficio.Domain.Service.Mensaje.MensajeServiceImpl;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Mensaje.MensajeRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Mensaje.MensajeResponse;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Mensaje.MensajeResponseConArchivos;
import com.proyecto_it.mercado_oficio.Mapper.Mensaje.MensajeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final MensajeService mensajeService;
    private final MensajeServiceImpl mensajeServiceImpl;
    private final MensajeMapper mensajeMapper;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Enviar mensaje con o sin archivos adjuntos
     * El backend guarda primero los archivos en la tabla multimedia,
     * luego guarda el mensaje con los IDs en multimedia_ids (JSON)
     */
    @PostMapping("/enviar")
    public ResponseEntity<MensajeResponseConArchivos> enviarMensaje(
            @RequestParam("emisorId") Integer emisorId,
            @RequestParam("receptorId") Integer receptorId,
            @RequestParam(value = "contenido", required = false) String contenido,
            @RequestParam(value = "archivos", required = false) List<MultipartFile> archivos) {

        log.info("Recibiendo mensaje de usuario {} a usuario {}", emisorId, receptorId);

        Mensaje mensaje = Mensaje.builder()
                .emisorId(emisorId)
                .receptorId(receptorId)
                .contenido(contenido)
                .build();

        // El servicio se encarga de:
        // 1. Guardar archivos en tabla multimedia (si existen)
        // 2. Obtener los IDs generados
        // 3. Guardar el mensaje con multimedia_ids en formato JSON
        Mensaje mensajeGuardado = mensajeService.enviarMensaje(mensaje, archivos);

        // Cargar archivos con base64 para imágenes y thumbnails para videos
        List<Multimedia> archivosMultimedia = mensajeService.obtenerArchivosAdjuntos(mensajeGuardado.getId());
        MensajeResponseConArchivos response = mensajeMapper.toResponseConArchivos(mensajeGuardado, archivosMultimedia);

        // Enviar notificación en tiempo real al receptor vía WebSocket
        messagingTemplate.convertAndSendToUser(
                receptorId.toString(),
                "/queue/mensajes",
                response
        );

        log.info("Mensaje enviado exitosamente con ID: {}", mensajeGuardado.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener chat entre dos usuarios
     * Devuelve:
     * - Imágenes: base64 completo
     * - Videos: thumbnail del primer frame
     * - Otros archivos: solo metadatos con urlDescarga
     */
    @GetMapping("/historial")
    public ResponseEntity<List<MensajeResponseConArchivos>> obtenerChat(
            @RequestParam("usuarioId1") Integer usuario1Id,
            @RequestParam("usuarioId2") Integer usuario2Id) {

        log.info("Obteniendo historial completo de chat entre usuarios {} y {}", usuario1Id, usuario2Id);

        List<Mensaje> mensajes = mensajeService.obtenerMensajesDeChat(usuario1Id, usuario2Id);

        // Cargar archivos para cada mensaje con base64/thumbnails
        List<MensajeResponseConArchivos> responses = mensajes.stream()
                .map(mensaje -> {
                    List<Multimedia> archivos = mensajeService.obtenerArchivosAdjuntos(mensaje.getId());
                    return mensajeMapper.toResponseConArchivos(mensaje, archivos);
                })
                .collect(Collectors.toList());

        log.info("Historial obtenido: {} mensajes", responses.size());

        return ResponseEntity.ok(responses);
    }


    @GetMapping("/mensaje/{id}")
    public ResponseEntity<MensajeResponseConArchivos> obtenerMensaje(@PathVariable Long id) {
        log.info("Obteniendo mensaje con ID: {}", id);

        Mensaje mensaje = mensajeService.obtenerMensajePorId(id);
        List<Multimedia> archivos = mensajeService.obtenerArchivosAdjuntos(id);
        MensajeResponseConArchivos response = mensajeMapper.toResponseConArchivos(mensaje, archivos);

        return ResponseEntity.ok(response);
    }

    /**
     * Descargar/visualizar archivo completo por ID
     * Este endpoint se usa cuando:
     * - Usuario hace click en una imagen (ver tamaño completo)
     * - Usuario hace click en un video (reproducir completo)
     * - Usuario hace click en descargar cualquier archivo
     */
    @GetMapping("/archivo/{id}")
    public ResponseEntity<Resource> obtenerArchivo(@PathVariable Integer id) {
        log.info("Obteniendo archivo completo con ID: {}", id);

        Multimedia multimedia = mensajeServiceImpl.obtenerMultimediaCompleto(id);
        ByteArrayResource resource = new ByteArrayResource(multimedia.getDatos());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(multimedia.getTipoContenido()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + multimedia.getNombre() + "\"")
                .contentLength(multimedia.getTamano())
                .body(resource);
    }
}