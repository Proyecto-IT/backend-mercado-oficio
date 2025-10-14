package com.proyecto_it.mercado_oficio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto_it.mercado_oficio.Domain.Model.Oficio;
import com.proyecto_it.mercado_oficio.Domain.Service.Oficio.OficioService;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Oficio.OficioCreateRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Oficio.OficioUpdateRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Oficio.OficioResponse;
import com.proyecto_it.mercado_oficio.Web.OficioController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OficioControllerTest {

    @Mock
    private OficioService oficioService;

    @InjectMocks
    private OficioController oficioController;

    private ObjectMapper objectMapper;
    private Oficio oficioEjemplo;
    private OficioCreateRequest createRequest;
    private OficioUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        oficioEjemplo = new Oficio(1, "Plomero");

        createRequest = OficioCreateRequest.builder()
                .nombre("Plomero")
                .build();

        updateRequest = OficioUpdateRequest.builder()
                .id(1)
                .nombre("Plomero 2")
                .build();
    }

    @Test
    void crearOficio_DeberiaRetornarOficioCreado() {
        // Given
        when(oficioService.crearOficio(any(Oficio.class))).thenReturn(oficioEjemplo);

        // When
        ResponseEntity<OficioResponse> response = oficioController.crearOficio(createRequest);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertEquals("Plomero", response.getBody().getNombre());

        verify(oficioService).crearOficio(any(Oficio.class));
    }

    @Test
    void crearOficio_ConRequestValido_DeberiaLlamarServicioConParametrosCorrectos() {
        // Given
        when(oficioService.crearOficio(any(Oficio.class))).thenReturn(oficioEjemplo);

        // When
        oficioController.crearOficio(createRequest);

        // Then
        verify(oficioService).crearOficio(argThat(oficio ->
                oficio.getNombre().equals("Plomero") && oficio.getId() == null
        ));
    }

    @Test
    void listarTodos_DeberiaRetornarListaDeOficios() {
        // Given
        List<Oficio> oficios = Arrays.asList(
                new Oficio(1, "Plomero"),
                new Oficio(2, "Electricista")
        );
        when(oficioService.listarTodos()).thenReturn(oficios);

        // When
        ResponseEntity<List<OficioResponse>> response = oficioController.listarTodos();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(oficioService).listarTodos();
    }

    @Test
    void listarTodos_ConListaVacia_DeberiaRetornarListaVacia() {
        // Given
        when(oficioService.listarTodos()).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<OficioResponse>> response = oficioController.listarTodos();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(oficioService).listarTodos();
    }

    @Test
    void buscarPorNombre_DeberiaRetornarOficiosCoincidentes() {
        // Given
        String nombreBusqueda = "Plom";
        List<Oficio> oficiosEncontrados = Arrays.asList(oficioEjemplo);
        when(oficioService.buscarPorNombre(nombreBusqueda)).thenReturn(oficiosEncontrados);

        // When
        ResponseEntity<List<OficioResponse>> response = oficioController.buscarPorNombre(nombreBusqueda);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(oficioService).buscarPorNombre(nombreBusqueda);
    }

    @Test
    void buscarPorNombre_SinCoincidencias_DeberiaRetornarListaVacia() {
        // Given
        String nombreBusqueda = "NoExiste";
        when(oficioService.buscarPorNombre(nombreBusqueda)).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<OficioResponse>> response = oficioController.buscarPorNombre(nombreBusqueda);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(oficioService).buscarPorNombre(nombreBusqueda);
    }

    @Test
    void actualizarOficio_DeberiaRetornarOficioActualizado() {
        // Given
        Oficio oficioActualizado = new Oficio(1, "Plomero 2");
        when(oficioService.actualizarOficio(any(Oficio.class))).thenReturn(oficioActualizado);

        // When
        ResponseEntity<OficioResponse> response = oficioController.actualizarOficio(updateRequest);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertEquals("Plomero 2", response.getBody().getNombre());

        verify(oficioService).actualizarOficio(any(Oficio.class));
    }

    @Test
    void actualizarOficio_ConRequestValido_DeberiaLlamarServicioConParametrosCorrectos() {
        // Given
        Oficio oficioActualizado = new Oficio(1, "Plomero 2");
        when(oficioService.actualizarOficio(any(Oficio.class))).thenReturn(oficioActualizado);

        // When
        oficioController.actualizarOficio(updateRequest);

        // Then
        verify(oficioService).actualizarOficio(argThat(oficio ->
                oficio.getId().equals(1) && oficio.getNombre().equals("Plomero 2")
        ));
    }

    @Test
    void eliminarOficio_DeberiaEliminarCorrectamente() {
        // Given
        Integer oficioId = 1;
        doNothing().when(oficioService).eliminarOficio(oficioId);

        // When
        ResponseEntity<Void> response = oficioController.eliminarOficio(oficioId);

        // Then
        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(oficioService).eliminarOficio(oficioId);
    }

    @Test
    void eliminarOficio_ConIdInvalido_DeberiaLlamarServicio() {
        // Given
        Integer oficioId = 999;
        doNothing().when(oficioService).eliminarOficio(oficioId);

        // When
        ResponseEntity<Void> response = oficioController.eliminarOficio(oficioId);

        // Then
        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());

        verify(oficioService).eliminarOficio(oficioId);
    }

    @Test
    void eliminarOficio_ConIdNull_DeberiaLlamarServicio() {
        // Given
        Integer oficioId = null;
        doNothing().when(oficioService).eliminarOficio(oficioId);

        // When
        ResponseEntity<Void> response = oficioController.eliminarOficio(oficioId);

        // Then
        assertNotNull(response);

        verify(oficioService).eliminarOficio(oficioId);
    }

    // Tests para validar la lógica de mapeo
    @Test
    void crearOficio_DeberiaMapearCorrectamenteRequestADominio() {
        // Given
        OficioCreateRequest request = OficioCreateRequest.builder()
                .nombre("Carpintero")
                .build();
        when(oficioService.crearOficio(any(Oficio.class))).thenReturn(new Oficio(2, "Carpintero"));

        // When
        oficioController.crearOficio(request);

        // Then
        verify(oficioService).crearOficio(argThat(oficio ->
                oficio.getNombre().equals("Carpintero") && oficio.getId() == null
        ));
    }

    @Test
    void crearOficio_DeberiaMapearCorrectamenteDominioAResponse() {
        // Given
        Oficio oficioCreado = new Oficio(3, "Electricista");
        when(oficioService.crearOficio(any(Oficio.class))).thenReturn(oficioCreado);

        // When
        ResponseEntity<OficioResponse> response = oficioController.crearOficio(createRequest);

        // Then
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getId());
        assertEquals("Electricista", response.getBody().getNombre());
    }

    @Test
    void actualizarOficio_DeberiaMapearCorrectamenteRequestADominio() {
        // Given
        OficioUpdateRequest request = OficioUpdateRequest.builder()
                .id(5)
                .nombre("Pintor")
                .build();
        when(oficioService.actualizarOficio(any(Oficio.class))).thenReturn(new Oficio(5, "Pintor"));

        // When
        oficioController.actualizarOficio(request);

        // Then
        verify(oficioService).actualizarOficio(argThat(oficio ->
                oficio.getId().equals(5) && oficio.getNombre().equals("Pintor")
        ));
    }
}