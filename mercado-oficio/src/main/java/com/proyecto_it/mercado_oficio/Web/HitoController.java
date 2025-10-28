package com.proyecto_it.mercado_oficio.Web;

import com.proyecto_it.mercado_oficio.Domain.Model.Hito;
import com.proyecto_it.mercado_oficio.Domain.Service.Escrow.EscrowService;
import com.proyecto_it.mercado_oficio.Domain.Service.Escrow.Hito.HitoService;
import com.proyecto_it.mercado_oficio.Exception.ResourceNotFoundException;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito.CrearHitosRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito.HitoCreateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito.HitoDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito.HitoResponseDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.HorarioServicioDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.TrustlessWork.*;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.TrustlessWork.TrustlessEscrowRecord;
import com.proyecto_it.mercado_oficio.Mapper.Hito.HitoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hitos")
@Slf4j
public class HitoController {

    @Autowired
    private HitoService hitoService;

    @Autowired
    private EscrowService escrowService;

    @Autowired
    private HitoMapper hitoMapper;

    @PostMapping("/crear")
    public ResponseEntity<?> crearHitos(@RequestBody CrearHitosRequest request) {
        try {
            // ✅ LOGS DE DEBUG
            log.info("Request recibido completo: {}", request);
            log.info("presupuestoId: {}", request.getPresupuestoId());
            log.info("horariosSeleccionados: {}", request.getHorariosSeleccionados());
            log.info("horariosSeleccionados size: {}",
                    request.getHorariosSeleccionados() != null ? request.getHorariosSeleccionados().size() : "null");

            if (request.getPresupuestoId() == null) {
                log.error("❌ presupuestoId es NULL en el request");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "presupuestoId es requerido"));
            }

            List<HitoDTO> hitos = hitoService.crearHitosAutomaticos(
                    request.getPresupuestoId(),
                    request.getHorariosSeleccionados()
            );

            List<HitoResponseDTO> response = hitos.stream()
                    .map(HitoResponseDTO::fromDTO)
                    .collect(Collectors.toList());

            log.info("Hitos creados para presupuesto {}: {}",
                    request.getPresupuestoId(), hitos.size());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (ResourceNotFoundException e) {
            log.error("Presupuesto no encontrado: {}", request.getPresupuestoId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al crear hitos", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{hitoId}/completar")
    public ResponseEntity<?> completarHito(
            @PathVariable Integer hitoId,
            @RequestBody CompletarHitoRequest request) {

        try {
            Hito hitoM = hitoService.obtenerPorId(hitoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hito no encontrado con id " + hitoId));

            HitoDTO hito = hitoMapper.toDTO(hitoM);

            Integer usuarioPrestador = hitoService.obtenerPrestadorId(hitoId);

            hitoService.completarHito(hitoId);


            Hito hitoMM = hitoService.obtenerPorId(hitoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hito no encontrado con id " + hitoId));

            HitoDTO updated = hitoMapper.toDTO(hitoMM);

            log.info("Hito {} marcado como completado", hitoId);

            return ResponseEntity.ok(HitoResponseDTO.fromDTO(updated));

        } catch (ResourceNotFoundException e) {
            log.error("Hito no encontrado: {}", hitoId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al completar hito {}", hitoId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{hitoId}/aprobar")
    public ResponseEntity<?> aprobarHito(@PathVariable Integer hitoId) {

        try {
            Hito hitoM = hitoService.obtenerPorId(hitoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hito no encontrado con id " + hitoId));

            HitoDTO hito = hitoMapper.toDTO(hitoM);

            Integer usuarioCliente = hitoService.obtenerClienteId(hitoId);

            hitoService.aprobarHito(hitoId);

            Hito hitoMM = hitoService.obtenerPorId(hitoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hito no encontrado con id " + hitoId));

            HitoDTO updated = hitoMapper.toDTO(hitoMM);
            log.info("Hito {} aprobado por cliente", hitoId);

            return ResponseEntity.ok(HitoResponseDTO.fromDTO(updated));

        } catch (ResourceNotFoundException e) {
            log.error("Hito no encontrado: {}", hitoId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al aprobar hito {}", hitoId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{hitoId}/liberar-fondos")
    public ResponseEntity<?> liberarFondos(@PathVariable Integer hitoId) {

        try {
            Hito hitoM = hitoService.obtenerPorId(hitoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hito no encontrado con id " + hitoId));

            HitoDTO hito = hitoMapper.toDTO(hitoM);

            Integer usuarioCliente = hitoService.obtenerClienteId(hitoId);

            hitoService.actualizarEstadoHito(hitoId, "PAGADO");

            Hito hitoMM = hitoService.obtenerPorId(hitoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hito no encontrado con id " + hitoId));

            HitoDTO updated = hitoMapper.toDTO(hitoMM);

            LiberarFondosResponseDTO respuesta = new LiberarFondosResponseDTO();
            respuesta.setHitoId(hitoId);
            respuesta.setMonto(updated.getMonto());
            respuesta.setMensaje("Fondos liberados exitosamente al prestador en blockchain");
            respuesta.setEstado("PAGADO");

            log.info("Fondos liberados para hito {} - Monto: {}", hitoId, updated.getMonto());
            return ResponseEntity.ok(respuesta);

        } catch (ResourceNotFoundException e) {
            log.error("Hito no encontrado: {}", hitoId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al liberar fondos para hito {}", hitoId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{hitoId}/estado")
    public ResponseEntity<?> obtenerEstado(@PathVariable Integer hitoId) {

        try {
            Hito hitoM = hitoService.obtenerPorId(hitoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hito no encontrado con id " + hitoId));

            HitoDTO hito = hitoMapper.toDTO(hitoM);

            EstadoHitoDTO respuesta = new EstadoHitoDTO();
            respuesta.setHito(hito);

            return ResponseEntity.ok(respuesta);

        } catch (ResourceNotFoundException e) {
            log.error("Hito no encontrado: {}", hitoId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener estado del hito {}", hitoId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/presupuesto/{presupuestoId}")
    public ResponseEntity<?> obtenerHitosPresupuesto(@PathVariable Integer presupuestoId) {

        try {
            List<HitoDTO> hitos = hitoService.obtenerHitosDelPresupuesto(presupuestoId);
            List<HitoResponseDTO> response = hitos.stream()
                    .map(HitoResponseDTO::fromDTO)
                    .collect(Collectors.toList());

            log.info("Obtenidos {} hitos para presupuesto {}", response.size(), presupuestoId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener hitos del presupuesto {}", presupuestoId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{hitoId}")
    public ResponseEntity<?> obtenerHito(@PathVariable Integer hitoId) {

        try {
            Hito hitoM = hitoService.obtenerPorId(hitoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hito no encontrado con id " + hitoId));

            HitoDTO hito = hitoMapper.toDTO(hitoM);
            return ResponseEntity.ok(HitoResponseDTO.fromDTO(hito));

        } catch (ResourceNotFoundException e) {
            log.error("Hito no encontrado: {}", hitoId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener hito {}", hitoId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> obtenerHitosCliente(@PathVariable Integer clienteId) {
        try {
            List<HitoDTO> hitos = hitoService.obtenerHitosDelCliente(clienteId);
            List<HitoResponseDTO> response = hitos.stream()
                    .map(HitoResponseDTO::fromDTO)
                    .collect(Collectors.toList());

            log.info("Obtenidos {} hitos para cliente {}", response.size(), clienteId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener hitos del cliente {}", clienteId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}