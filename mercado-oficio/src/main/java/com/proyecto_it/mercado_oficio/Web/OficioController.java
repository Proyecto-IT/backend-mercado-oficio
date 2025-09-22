package com.proyecto_it.mercado_oficio.Infraestructure.Controller;

import com.proyecto_it.mercado_oficio.Domain.Model.Oficio;
import com.proyecto_it.mercado_oficio.Domain.Service.Oficio.OficioService;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Oficio.OficioCreateRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Oficio.OficioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/oficios")
@RequiredArgsConstructor
public class OficioController {

    private final OficioService oficioService;

    @PostMapping
    public ResponseEntity<OficioResponse> crearOficio(@Valid @RequestBody OficioCreateRequest request) {
        Oficio oficio = new Oficio(null, request.getNombre());
        Oficio creado = oficioService.crearOficio(oficio);
        return ResponseEntity.ok(new OficioResponse(creado.getId(), creado.getNombre()));
    }

    @GetMapping
    public ResponseEntity<List<OficioResponse>> listarTodos() {
        List<OficioResponse> response = oficioService.listarTodos().stream()
                .map(o -> new OficioResponse(o.getId(), o.getNombre()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OficioResponse> buscarPorId(@PathVariable Integer id) {
        return oficioService.buscarPorId(id)
                .map(o -> ResponseEntity.ok(new OficioResponse(o.getId(), o.getNombre())))
                .orElse(ResponseEntity.notFound().build());
    }

    // para ver coincidencias
    @GetMapping("/buscar")
    public ResponseEntity<List<OficioResponse>> buscarPorNombre(@RequestParam String nombre) {
        List<OficioResponse> response = oficioService.buscarPorNombre(nombre).stream()
                .map(o -> new OficioResponse(o.getId(), o.getNombre()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OficioResponse> actualizarOficio(@PathVariable Integer id, @Valid @RequestBody OficioUpdateRequest request) {
        Oficio oficio = new Oficio(id, request.getNombre());
        Oficio actualizado = oficioService.actualizarOficio(oficio);
        return ResponseEntity.ok(new OficioResponse(actualizado.getId(), actualizado.getNombre()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarOficio(@PathVariable Integer id) {
        oficioService.eliminarOficio(id);
        return ResponseEntity.noContent().build();
    }
}
