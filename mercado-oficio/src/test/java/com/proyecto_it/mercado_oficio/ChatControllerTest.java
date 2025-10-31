package com.proyecto_it.mercado_oficio;

import com.proyecto_it.mercado_oficio.Domain.Model.Mensaje;
import com.proyecto_it.mercado_oficio.Domain.Model.Multimedia;
import com.proyecto_it.mercado_oficio.Domain.Service.Mensaje.MensajeCacheProxy;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Mensaje.MensajeResponse;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Mensaje.MultimediaDTO;
import com.proyecto_it.mercado_oficio.Mapper.Mensaje.MensajeMapper;
import com.proyecto_it.mercado_oficio.Web.ChatController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ChatController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private MensajeCacheProxy mensajeCacheProxy;

    @Mock
    private MensajeMapper mensajeMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatController chatController;

    private Mensaje mensajeEjemplo;
    private Multimedia multimediaEjemplo;
    private MensajeResponse mensajeResponseEjemplo;
    private MultimediaDTO multimediaDTOEjemplo;

    @BeforeEach
    void setUp() {
        // Crear mensaje de ejemplo
        mensajeEjemplo = Mensaje.builder()
                .id(1)
                .emisorId(1)
                .receptorId(2)
                .contenido("Hola, ¿cómo estás?")
                .multimediaIds(Arrays.asList(101, 102))
                .fechaEnvio(LocalDateTime.now())
                .build();

        // Crear multimedia de ejemplo
        multimediaEjemplo = Multimedia.builder()
                .id(101)
                .nombre("foto.jpg")
                .tipoContenido("image/jpeg")
                .extension("jpg")
                .datos(new byte[]{1, 2, 3, 4, 5})
                .build();

        // Crear DTO de ejemplo
        multimediaDTOEjemplo = MultimediaDTO.builder()
                .id(101)
                .nombre("foto.jpg")
                .tipoContenido("image/jpeg")
                .extension("jpg")
                .tamano(5L)
                .urlDescarga("/api/chat/archivo/101")
                .base64Preview("data:image/jpeg;base64,AQIDBAU=")
                .tipoArchivo(MultimediaDTO.TipoArchivo.IMAGEN)
                .build();

        mensajeResponseEjemplo = MensajeResponse.builder()
                .id(1)
                .emisorId(1)
                .receptorId(2)
                .contenido("Hola, ¿cómo estás?")
                .fechaEnvio(LocalDateTime.now())
                .tieneArchivos(true)
                .archivos(Arrays.asList(multimediaDTOEjemplo))
                .build();
    }

    // ==================== TESTS PARA ENVIAR MENSAJE ====================

    @Test
    void enviarMensaje_SinArchivos_DeberiaRetornarMensajeCreado() {
        // Given
        when(mensajeCacheProxy.enviarMensaje(any(Mensaje.class), isNull()))
                .thenReturn(mensajeEjemplo);
        when(mensajeCacheProxy.obtenerArchivosAdjuntos(1))
                .thenReturn(Collections.emptyList());
        when(mensajeMapper.toResponseConArchivos(any(Mensaje.class), anyList()))
                .thenReturn(mensajeResponseEjemplo);

        // When
        ResponseEntity<MensajeResponse> response = chatController.enviarMensaje(
                1, 2, "Hola, ¿cómo estás?", null
        );

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertEquals("Hola, ¿cómo estás?", response.getBody().getContenido());

        verify(mensajeCacheProxy).enviarMensaje(any(Mensaje.class), isNull());
        verify(mensajeCacheProxy).obtenerArchivosAdjuntos(1);
        verify(mensajeMapper).toResponseConArchivos(any(Mensaje.class), anyList());
        verify(messagingTemplate).convertAndSendToUser(eq("2"), eq("/queue/mensajes"), any());
    }

    @Test
    void enviarMensaje_ConArchivos_DeberiaGuardarArchivosPrimero() {
        // Given
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );
        List<MultipartFile> archivos = Arrays.asList(archivo);

        when(mensajeCacheProxy.enviarMensaje(any(Mensaje.class), anyList()))
                .thenReturn(mensajeEjemplo);
        when(mensajeCacheProxy.obtenerArchivosAdjuntos(1))
                .thenReturn(Arrays.asList(multimediaEjemplo));
        when(mensajeMapper.toResponseConArchivos(any(Mensaje.class), anyList()))
                .thenReturn(mensajeResponseEjemplo);

        // When
        ResponseEntity<MensajeResponse> response = chatController.enviarMensaje(
                1, 2, "Mira esta foto", archivos
        );

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().getTieneArchivos());

        verify(mensajeCacheProxy).enviarMensaje(any(Mensaje.class), eq(archivos));
        verify(mensajeCacheProxy).obtenerArchivosAdjuntos(1);
        verify(messagingTemplate).convertAndSendToUser(eq("2"), eq("/queue/mensajes"), any());
    }

    @Test
    void enviarMensaje_DeberiaEnviarNotificacionWebSocket() {
        // Given
        when(mensajeCacheProxy.enviarMensaje(any(Mensaje.class), isNull()))
                .thenReturn(mensajeEjemplo);
        when(mensajeCacheProxy.obtenerArchivosAdjuntos(1))
                .thenReturn(Collections.emptyList());
        when(mensajeMapper.toResponseConArchivos(any(Mensaje.class), anyList()))
                .thenReturn(mensajeResponseEjemplo);

        // When
        chatController.enviarMensaje(1, 2, "Test", null);

        // Then
        verify(messagingTemplate).convertAndSendToUser(
                eq("2"),
                eq("/queue/mensajes"),
                any(MensajeResponse.class)
        );
    }

    @Test
    void enviarMensaje_ConContenidoVacio_DeberiaAceptarlo() {
        // Given
        when(mensajeCacheProxy.enviarMensaje(any(Mensaje.class), isNull()))
                .thenReturn(mensajeEjemplo);
        when(mensajeCacheProxy.obtenerArchivosAdjuntos(1))
                .thenReturn(Collections.emptyList());
        when(mensajeMapper.toResponseConArchivos(any(Mensaje.class), anyList()))
                .thenReturn(mensajeResponseEjemplo);

        // When
        ResponseEntity<MensajeResponse> response = chatController.enviarMensaje(
                1, 2, null, null
        );

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(mensajeCacheProxy).enviarMensaje(any(Mensaje.class), isNull());
    }

    @Test
    void enviarMensaje_DeberiaMapearCorrectamenteParametros() {
        // Given
        when(mensajeCacheProxy.enviarMensaje(any(Mensaje.class), isNull()))
                .thenReturn(mensajeEjemplo);
        when(mensajeCacheProxy.obtenerArchivosAdjuntos(1))
                .thenReturn(Collections.emptyList());
        when(mensajeMapper.toResponseConArchivos(any(Mensaje.class), anyList()))
                .thenReturn(mensajeResponseEjemplo);

        // When
        chatController.enviarMensaje(1, 2, "Test mensaje", null);

        // Then
        verify(mensajeCacheProxy).enviarMensaje(argThat(mensaje ->
                mensaje.getEmisorId().equals(1) &&
                        mensaje.getReceptorId().equals(2) &&
                        mensaje.getContenido().equals("Test mensaje")
        ), isNull());
    }

    // ==================== TESTS PARA OBTENER CHAT ====================

    @Test
    void obtenerChat_DeberiaRetornarListaDeMensajes() {
        // Given
        List<Mensaje> mensajes = Arrays.asList(mensajeEjemplo);
        when(mensajeCacheProxy.obtenerMensajesDeChat(1, 2))
                .thenReturn(mensajes);
        when(mensajeCacheProxy.obtenerArchivosAdjuntos(1))
                .thenReturn(Arrays.asList(multimediaEjemplo));
        when(mensajeMapper.toResponseConArchivos(any(Mensaje.class), anyList()))
                .thenReturn(mensajeResponseEjemplo);

        // When
        ResponseEntity<List<MensajeResponse>> response = chatController.obtenerChat(1, 2);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        verify(mensajeCacheProxy).obtenerMensajesDeChat(1, 2);
        verify(mensajeCacheProxy).obtenerArchivosAdjuntos(1);
    }

    @Test
    void obtenerChat_ConChatVacio_DeberiaRetornarListaVacia() {
        // Given
        when(mensajeCacheProxy.obtenerMensajesDeChat(1, 2))
                .thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<MensajeResponse>> response = chatController.obtenerChat(1, 2);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(mensajeCacheProxy).obtenerMensajesDeChat(1, 2);
    }

    @Test
    void obtenerChat_ConVariosArchivos_DeberiaCargarTodosLosArchivos() {
        // Given
        Mensaje mensaje1 = Mensaje.builder()
                .id(1)
                .emisorId(1)
                .receptorId(2)
                .contenido("Mensaje 1")
                .multimediaIds(Arrays.asList(101))
                .fechaEnvio(LocalDateTime.now())
                .build();

        Mensaje mensaje2 = Mensaje.builder()
                .id(2)
                .emisorId(2)
                .receptorId(1)
                .contenido("Mensaje 2")
                .multimediaIds(Arrays.asList(102, 103))
                .fechaEnvio(LocalDateTime.now())
                .build();

        List<Mensaje> mensajes = Arrays.asList(mensaje1, mensaje2);

        when(mensajeCacheProxy.obtenerMensajesDeChat(1, 2))
                .thenReturn(mensajes);
        when(mensajeCacheProxy.obtenerArchivosAdjuntos(anyInt()))
                .thenReturn(Arrays.asList(multimediaEjemplo));
        when(mensajeMapper.toResponseConArchivos(any(Mensaje.class), anyList()))
                .thenReturn(mensajeResponseEjemplo);

        // When
        ResponseEntity<List<MensajeResponse>> response = chatController.obtenerChat(1, 2);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getBody().size());
        verify(mensajeCacheProxy, times(2)).obtenerArchivosAdjuntos(anyInt());
    }

    @Test
    void obtenerChat_DeberiaCargarArchivosConBase64ParaImagenes() {
        // Given
        when(mensajeCacheProxy.obtenerMensajesDeChat(1, 2))
                .thenReturn(Arrays.asList(mensajeEjemplo));
        when(mensajeCacheProxy.obtenerArchivosAdjuntos(1))
                .thenReturn(Arrays.asList(multimediaEjemplo));
        when(mensajeMapper.toResponseConArchivos(any(Mensaje.class), anyList()))
                .thenReturn(mensajeResponseEjemplo);

        // When
        ResponseEntity<List<MensajeResponse>> response = chatController.obtenerChat(1, 2);

        // Then
        assertNotNull(response);
        assertNotNull(response.getBody().get(0).getArchivos());
        assertEquals("data:image/jpeg;base64,AQIDBAU=",
                response.getBody().get(0).getArchivos().get(0).getBase64Preview());
        verify(mensajeMapper).toResponseConArchivos(any(Mensaje.class), eq(Arrays.asList(multimediaEjemplo)));
    }

    // ==================== TESTS PARA OBTENER MENSAJE ====================

    @Test
    void obtenerMensaje_DeberiaRetornarMensajeConArchivos() {
        // Given
        when(mensajeCacheProxy.obtenerMensajePorId(1))
                .thenReturn(mensajeEjemplo);
        when(mensajeCacheProxy.obtenerArchivosAdjuntos(1))
                .thenReturn(Arrays.asList(multimediaEjemplo));
        when(mensajeMapper.toResponseConArchivos(any(Mensaje.class), anyList()))
                .thenReturn(mensajeResponseEjemplo);

        // When
        ResponseEntity<MensajeResponse> response = chatController.obtenerMensaje(1);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertTrue(response.getBody().getTieneArchivos());

        verify(mensajeCacheProxy).obtenerMensajePorId(1);
        verify(mensajeCacheProxy).obtenerArchivosAdjuntos(1);
    }

    @Test
    void obtenerMensaje_ConIdValido_DeberiaConsultarCache() {
        // Given
        when(mensajeCacheProxy.obtenerMensajePorId(1))
                .thenReturn(mensajeEjemplo);
        when(mensajeCacheProxy.obtenerArchivosAdjuntos(1))
                .thenReturn(Collections.emptyList());
        when(mensajeMapper.toResponseConArchivos(any(Mensaje.class), anyList()))
                .thenReturn(mensajeResponseEjemplo);

        // When
        chatController.obtenerMensaje(1);

        // Then
        verify(mensajeCacheProxy).obtenerMensajePorId(1);
    }

    // ==================== TESTS PARA OBTENER ARCHIVO ====================

    @Test
    void obtenerArchivo_DeberiaRetornarArchivoCompleto() {
        // Given
        when(mensajeCacheProxy.obtenerMultimediaCompleto(101))
                .thenReturn(multimediaEjemplo);

        // When
        ResponseEntity<Resource> response = chatController.obtenerArchivo(101);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(MediaType.parseMediaType("image/jpeg"), response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("inline"));
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("foto.jpg"));

        verify(mensajeCacheProxy).obtenerMultimediaCompleto(101);
    }

    @Test
    void obtenerArchivo_DeberiaEstablecerContentType() {
        // Given
        Multimedia pdf = Multimedia.builder()
                .id(102)
                .nombre("documento.pdf")
                .tipoContenido("application/pdf")
                .extension("pdf")
                .datos(new byte[]{1, 2, 3})
                .build();

        when(mensajeCacheProxy.obtenerMultimediaCompleto(102))
                .thenReturn(pdf);

        // When
        ResponseEntity<Resource> response = chatController.obtenerArchivo(102);

        // Then
        assertNotNull(response);
        assertEquals(MediaType.parseMediaType("application/pdf"), response.getHeaders().getContentType());
    }

    @Test
    void obtenerArchivo_DeberiaEstablecerContentLength() {
        // Given
        when(mensajeCacheProxy.obtenerMultimediaCompleto(101))
                .thenReturn(multimediaEjemplo);

        // When
        ResponseEntity<Resource> response = chatController.obtenerArchivo(101);

        // Then
        assertNotNull(response);
        assertEquals(5L, response.getHeaders().getContentLength());
    }

    @Test
    void obtenerArchivo_DeberiaUsarCacheParaArchivosRepetidos() {
        // Given
        when(mensajeCacheProxy.obtenerMultimediaCompleto(101))
                .thenReturn(multimediaEjemplo);

        // When
        chatController.obtenerArchivo(101);
        chatController.obtenerArchivo(101);

        // Then
        verify(mensajeCacheProxy, times(2)).obtenerMultimediaCompleto(101);
    }

    @Test
    void obtenerArchivo_DeberiaRetornarInlineDisposition() {
        // Given
        when(mensajeCacheProxy.obtenerMultimediaCompleto(101))
                .thenReturn(multimediaEjemplo);

        // When
        ResponseEntity<Resource> response = chatController.obtenerArchivo(101);

        // Then
        String disposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(disposition);
        assertTrue(disposition.startsWith("inline"));
        assertTrue(disposition.contains("filename=\"foto.jpg\""));
    }

    // ==================== TESTS DE INTEGRACIÓN ====================

    @Test
    void flujoCompleto_EnviarYObtenerMensaje() {
        // Given - Enviar mensaje
        when(mensajeCacheProxy.enviarMensaje(any(Mensaje.class), isNull()))
                .thenReturn(mensajeEjemplo);
        when(mensajeCacheProxy.obtenerArchivosAdjuntos(1))
                .thenReturn(Collections.emptyList());
        when(mensajeMapper.toResponseConArchivos(any(Mensaje.class), anyList()))
                .thenReturn(mensajeResponseEjemplo);

        // When - Enviar
        ResponseEntity<MensajeResponse> enviarResponse = chatController.enviarMensaje(
                1, 2, "Test", null
        );

        // Given - Obtener
        when(mensajeCacheProxy.obtenerMensajePorId(1))
                .thenReturn(mensajeEjemplo);

        // When - Obtener
        ResponseEntity<MensajeResponse> obtenerResponse = chatController.obtenerMensaje(1);

        // Then
        assertEquals(enviarResponse.getBody().getId(), obtenerResponse.getBody().getId());
        verify(mensajeCacheProxy).enviarMensaje(any(Mensaje.class), isNull());
        verify(mensajeCacheProxy).obtenerMensajePorId(1);
    }

    @Test
    void flujoCompleto_EnviarConArchivosYDescargar() {
        // Given
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "test.jpg", "image/jpeg", "content".getBytes()
        );

        when(mensajeCacheProxy.enviarMensaje(any(Mensaje.class), anyList()))
                .thenReturn(mensajeEjemplo);
        when(mensajeCacheProxy.obtenerArchivosAdjuntos(1))
                .thenReturn(Arrays.asList(multimediaEjemplo));
        when(mensajeMapper.toResponseConArchivos(any(Mensaje.class), anyList()))
                .thenReturn(mensajeResponseEjemplo);
        when(mensajeCacheProxy.obtenerMultimediaCompleto(101))
                .thenReturn(multimediaEjemplo);

        // When - Enviar mensaje con archivo
        ResponseEntity<MensajeResponse> enviarResponse = chatController.enviarMensaje(
                1, 2, "Con archivo", Arrays.asList(archivo)
        );

        // When - Descargar archivo
        Integer archivoId = enviarResponse.getBody().getArchivos().get(0).getId();
        ResponseEntity<Resource> descargarResponse = chatController.obtenerArchivo(archivoId);

        // Then
        assertNotNull(enviarResponse.getBody());
        assertTrue(enviarResponse.getBody().getTieneArchivos());
        assertNotNull(descargarResponse.getBody());
        assertEquals(200, descargarResponse.getStatusCodeValue());
    }

    // ==================== TESTS DE VALIDACIÓN ====================

    @Test
    void enviarMensaje_ConMultiplesArchivos_DeberiaGuardarTodos() {
        // Given
        MockMultipartFile archivo1 = new MockMultipartFile("archivo1", "test1.jpg", "image/jpeg", "content1".getBytes());
        MockMultipartFile archivo2 = new MockMultipartFile("archivo2", "test2.jpg", "image/jpeg", "content2".getBytes());
        List<MultipartFile> archivos = Arrays.asList(archivo1, archivo2);

        Multimedia multimedia1 = Multimedia.builder().id(101).nombre("test1.jpg").datos(new byte[]{1}).build();
        Multimedia multimedia2 = Multimedia.builder().id(102).nombre("test2.jpg").datos(new byte[]{2}).build();

        when(mensajeCacheProxy.enviarMensaje(any(Mensaje.class), anyList()))
                .thenReturn(mensajeEjemplo);
        when(mensajeCacheProxy.obtenerArchivosAdjuntos(1))
                .thenReturn(Arrays.asList(multimedia1, multimedia2));
        when(mensajeMapper.toResponseConArchivos(any(Mensaje.class), anyList()))
                .thenReturn(mensajeResponseEjemplo);

        // When
        ResponseEntity<MensajeResponse> response = chatController.enviarMensaje(
                1, 2, "Múltiples archivos", archivos
        );

        // Then
        assertNotNull(response);
        verify(mensajeCacheProxy).enviarMensaje(any(Mensaje.class), eq(archivos));
    }

    @Test
    void obtenerChat_ConMensajesSinArchivos_NoDeberiaFallar() {
        // Given
        Mensaje mensajeSinArchivos = Mensaje.builder()
                .id(1)
                .emisorId(1)
                .receptorId(2)
                .contenido("Solo texto")
                .multimediaIds(Collections.emptyList())
                .fechaEnvio(LocalDateTime.now())
                .build();

        when(mensajeCacheProxy.obtenerMensajesDeChat(1, 2))
                .thenReturn(Arrays.asList(mensajeSinArchivos));
        when(mensajeCacheProxy.obtenerArchivosAdjuntos(1))
                .thenReturn(Collections.emptyList());
        when(mensajeMapper.toResponseConArchivos(any(Mensaje.class), anyList()))
                .thenReturn(mensajeResponseEjemplo);

        // When
        ResponseEntity<List<MensajeResponse>> response = chatController.obtenerChat(1, 2);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }
}