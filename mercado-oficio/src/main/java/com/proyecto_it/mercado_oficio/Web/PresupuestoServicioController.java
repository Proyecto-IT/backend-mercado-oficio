package com.proyecto_it.mercado_oficio.Web;

import com.proyecto_it.mercado_oficio.Domain.Model.PresupuestoServicio;
import com.proyecto_it.mercado_oficio.Domain.Service.Servicio.PresupuestoServicio.PresupuestoServicioService;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoPresupuesto;
import com.proyecto_it.mercado_oficio.Exception.ResourceNotFoundException;
import com.proyecto_it.mercado_oficio.Exception.ValidationException;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoCreateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioCreateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioUpdateDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presupuestos")
@Slf4j
@Validated
public class PresupuestoServicioController {
    @Autowired
    private PresupuestoServicioService presupuestoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PresupuestoServicioDTO> crear(@Valid @RequestBody PresupuestoServicioCreateDTO dto) {
        log.info("Creando nuevo presupuesto");
        return ResponseEntity.status(HttpStatus.CREATED).body(presupuestoService.crear(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PresupuestoServicioDTO> obtener(@PathVariable Integer id) {
        PresupuestoServicioDTO dto = presupuestoService.obtener(id);

        List<PresupuestoArchivoDTO> archivos = presupuestoService.obtenerArchivos(id);
        dto.setArchivos(archivos);

        return ResponseEntity.ok(dto);
    }
    @GetMapping("/{presupuestoId}/respondido")
    public ResponseEntity<Map<String, Boolean>> estaRespondido(@PathVariable Integer presupuestoId) {
        boolean respondido = presupuestoService.estaRespondido(presupuestoId);
        return ResponseEntity.ok(Map.of("respondido", respondido));
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<PresupuestoServicioDTO>> obtenerPorCliente(@PathVariable Integer idCliente) {
        return ResponseEntity.ok(presupuestoService.obtenerPorCliente(idCliente));
    }

    @GetMapping("/prestador/{idPrestador}")
    public ResponseEntity<List<PresupuestoServicioDTO>> obtenerPorPrestador(@PathVariable Integer idPrestador) {
        return ResponseEntity.ok(presupuestoService.obtenerPorPrestador(idPrestador));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PresupuestoServicioDTO>> obtenerPorEstado(@PathVariable EstadoPresupuesto estado) {
        return ResponseEntity.ok(presupuestoService.obtenerPorEstado(estado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PresupuestoServicioDTO> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody PresupuestoServicioUpdateDTO dto) {
        return ResponseEntity.ok(presupuestoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        presupuestoService.eliminar(id);
    }

    @PostMapping("/{presupuestoId}/archivos")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PresupuestoArchivoDTO> cargarArchivo(
            @PathVariable Integer presupuestoId,
            @RequestParam("archivo") MultipartFile archivo) throws ValidationException {
        log.info("Cargando archivo para presupuesto: {}", presupuestoId);
        PresupuestoArchivoCreateDTO dto = new PresupuestoArchivoCreateDTO();
        dto.setArchivo(archivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(presupuestoService.cargarArchivo(presupuestoId, dto));
    }

    @GetMapping("/{presupuestoId}/archivos")
    public ResponseEntity<List<PresupuestoArchivoDTO>> obtenerArchivos(@PathVariable Integer presupuestoId) {
        return ResponseEntity.ok(presupuestoService.obtenerArchivos(presupuestoId));
    }

    @GetMapping("/archivos/{archivoId}/descargar")
    public ResponseEntity<byte[]> descargarArchivo(@PathVariable Integer archivoId) {
        PresupuestoArchivoDTO archivo = presupuestoService.obtenerPorId(archivoId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + archivo.getNombreArchivo() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, archivo.getTipoMime())
                .body(archivo.getContenido());
    }

    @DeleteMapping("/archivos/{archivoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarArchivo(@PathVariable Integer archivoId) {
        presupuestoService.eliminarArchivo(archivoId);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {

        String estadoStr = request.get("estado");
        if (estadoStr == null || estadoStr.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El estado es requerido"));
        }

        EstadoPresupuesto nuevoEstado;
        try {
            nuevoEstado = EstadoPresupuesto.valueOf(estadoStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Estado inválido: " + estadoStr));
        }

        PresupuestoServicioDTO actualizado = presupuestoService.actualizarEstado(id, nuevoEstado);
        return ResponseEntity.ok(actualizado);
    }
    @GetMapping("/servicio/{servicioId}")
    public ResponseEntity<List<PresupuestoServicioDTO>> obtenerPorServicio(@PathVariable Integer servicioId) {
        log.info("Obteniendo presupuestos para servicio: {}", servicioId);
        return ResponseEntity.ok(presupuestoService.obtenerPorServicio(servicioId));
    }
}