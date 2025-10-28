package com.proyecto_it.mercado_oficio.Web;

import com.proyecto_it.mercado_oficio.Domain.Model.Oficio;
import com.proyecto_it.mercado_oficio.Domain.Service.Oficio.OficioService;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Oficio.OficioCreateRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Oficio.OficioUpdateRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Oficio.OficioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/oficios")
@RequiredArgsConstructor
public class OficioController {

    private final OficioService oficioService;

    @PostMapping
    public ResponseEntity<OficioResponse> crearOficio(@Valid @RequestBody OficioCreateRequest request) {
        log.info("Creando nuevo oficio con nombre: {}", request.getNombre());
        Oficio oficio = new Oficio(null, request.getNombre());
        Oficio creado = oficioService.crearOficio(oficio);
        log.info("Oficio creado con ID: {}", creado.getId());
        return ResponseEntity.ok(new OficioResponse(creado.getId(), creado.getNombre()));
    }

    @GetMapping
    public ResponseEntity<List<OficioResponse>> listarTodos() {
        log.info("Listando todos los oficios");
        List<OficioResponse> response = oficioService.listarTodos().stream()
                .map(o -> new OficioResponse(o.getId(), o.getNombre()))
                .toList();
        log.info("Se encontraron {} oficios", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<OficioResponse>> buscarPorNombre(@RequestParam String nombre) {
        log.info("Buscando oficios que coincidan con el nombre: {}", nombre);
        List<OficioResponse> response = oficioService.buscarPorNombre(nombre).stream()
                .map(o -> new OficioResponse(o.getId(), o.getNombre()))
                .toList();
        log.info("Se encontraron {} coincidencias para '{}'", response.size(), nombre);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OficioResponse> buscarPorId(@PathVariable Integer id) {
        log.info("Buscando oficio por ID: {}", id);
        return oficioService.buscarPorId(id)
                .map(oficio -> {
                    log.info("Oficio encontrado: {}", oficio.getNombre());
                    return ResponseEntity.ok(new OficioResponse(oficio.getId(), oficio.getNombre()));
                })
                .orElseGet(() -> {
                    log.warn("No se encontró oficio con ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PutMapping
    public ResponseEntity<OficioResponse> actualizarOficio(@Valid @RequestBody OficioUpdateRequest request) {
        log.info("Actualizando oficio ID: {}", request.getId());
        Oficio oficio = new Oficio(request.getId(), request.getNombre());
        Oficio actualizado = oficioService.actualizarOficio(oficio);
        log.info("Oficio actualizado: {} -> {}", request.getId(), actualizado.getNombre());
        return ResponseEntity.ok(new OficioResponse(actualizado.getId(), actualizado.getNombre()));
    }

    @DeleteMapping
    public ResponseEntity<Void> eliminarOficio(@RequestParam Integer id) {
        log.info("Eliminando oficio con ID: {}", id);
        oficioService.eliminarOficio(id);
        log.info("Oficio con ID {} eliminado correctamente", id);
        return ResponseEntity.noContent().build();
    }
}