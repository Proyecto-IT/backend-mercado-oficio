package com.proyecto_it.mercado_oficio.Web;

import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;
import com.proyecto_it.mercado_oficio.Domain.Service.Servicio.Portafolio.PortafolioService;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.Portafolio.PortafolioRequestDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.Portafolio.PortafolioResponseDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.Portafolio.PortafolioUpdateDTO;
import com.proyecto_it.mercado_oficio.Mapper.Servicio.Portafolio.PortafolioMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/portafolios")
@RequiredArgsConstructor
public class PortafolioController {

    private final PortafolioService portafolioService;
    private final PortafolioMapper mapper;

    /**
     * Crear un nuevo portafolio para un servicio
     */
    @PostMapping("/servicio/{servicioId}")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<PortafolioResponseDTO> crearPortafolio(
            @PathVariable Integer servicioId,
            @Valid @RequestBody PortafolioRequestDTO requestDTO,
            Authentication authentication) {

        Integer usuarioId = obtenerUsuarioIdDeAuth(authentication);

        Portafolio portafolio = mapper.toDomain(requestDTO, servicioId);
        Portafolio portafolioCreado = portafolioService.crearPortafolio(portafolio, usuarioId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponseDTO(portafolioCreado));
    }

    /**
     * Actualizar un portafolio existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<PortafolioResponseDTO> actualizarPortafolio(
            @PathVariable Integer id,
            @Valid @RequestBody PortafolioUpdateDTO updateDTO,
            Authentication authentication) {

        Integer usuarioId = obtenerUsuarioIdDeAuth(authentication);

        Portafolio portafolioActualizado = mapper.toDomain(updateDTO);
        Portafolio portafolioGuardado = portafolioService.actualizarPortafolio(
                id,
                portafolioActualizado,
                usuarioId
        );

        return ResponseEntity.ok(mapper.toResponseDTO(portafolioGuardado));
    }

    /**
     * Eliminar un portafolio
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<Void> eliminarPortafolio(
            @PathVariable Integer id,
            Authentication authentication) {

        Integer usuarioId = obtenerUsuarioIdDeAuth(authentication);
        portafolioService.eliminarPortafolio(id, usuarioId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener un portafolio por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PortafolioResponseDTO> obtenerPortafolioPorId(
            @PathVariable Integer id) {

        Portafolio portafolio = portafolioService.obtenerPortafolioPorId(id);
        return ResponseEntity.ok(mapper.toResponseDTO(portafolio));
    }

    /**
     * Obtener todos los portafolios de un servicio
     */
    @GetMapping("/servicio/{servicioId}")
    public ResponseEntity<List<PortafolioResponseDTO>> obtenerPortafoliosPorServicio(
            @PathVariable Integer servicioId) {

        List<Portafolio> portafolios = portafolioService.obtenerPortafoliosPorServicio(servicioId);
        List<PortafolioResponseDTO> response = portafolios.stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private Integer obtenerUsuarioIdDeAuth(Authentication authentication) {
        // Implementar según tu sistema de autenticación
        return 1; // CAMBIAR ESTO
    }
}