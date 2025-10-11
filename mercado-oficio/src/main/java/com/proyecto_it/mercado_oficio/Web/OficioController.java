package com.proyecto_it.mercado_oficio.Web;

import com.proyecto_it.mercado_oficio.Domain.Model.Oficio;
import com.proyecto_it.mercado_oficio.Domain.Service.Oficio.OficioService;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Oficio.OficioCreateRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Oficio.OficioUpdateRequest;
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

    // para ver coincidencias
    @GetMapping("/buscar")
    public ResponseEntity<List<OficioResponse>> buscarPorNombre(@RequestParam String nombre) {
        List<OficioResponse> response = oficioService.buscarPorNombre(nombre).stream()
                .map(o -> new OficioResponse(o.getId(), o.getNombre()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<OficioResponse> actualizarOficio(@Valid @RequestBody OficioUpdateRequest request) {
        Oficio oficio = new Oficio(request.getId(), request.getNombre());
        Oficio actualizado = oficioService.actualizarOficio(oficio);
        return ResponseEntity.ok(new OficioResponse(actualizado.getId(), actualizado.getNombre()));
    }

    @DeleteMapping
    public ResponseEntity<Void> eliminarOficio(@RequestParam Integer id) {
        oficioService.eliminarOficio(id);
        return ResponseEntity.noContent().build();
    }
}
