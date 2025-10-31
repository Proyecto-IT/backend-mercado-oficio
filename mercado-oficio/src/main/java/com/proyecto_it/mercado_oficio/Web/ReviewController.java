package com.proyecto_it.mercado_oficio.Web;

import com.proyecto_it.mercado_oficio.Domain.Model.ReviewCliente;
import com.proyecto_it.mercado_oficio.Domain.Model.ReviewPrestador;
import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Service.Review.ReviewService;
import com.proyecto_it.mercado_oficio.Domain.Service.Usuario.UsuarioService;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Review.CreateReviewClienteRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Review.CreateReviewPrestadorRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Review.ElegibilidadReviewResponse;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Review.ReviewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UsuarioService usuarioService;

    @GetMapping("/elegibilidad")
    public ResponseEntity<ElegibilidadReviewResponse> verificarElegibilidad(
            @RequestParam Integer idServicio,
            @RequestParam Integer idPresupuesto,
            Authentication authentication) {

        Integer idCliente = obtenerIdUsuarioAutenticado(authentication);

        ElegibilidadReviewResponse response = reviewService.verificarElegibilidadParaRevisar(
                idCliente, idServicio, idPresupuesto
        );

        return ResponseEntity.ok(response);
    }


    @PostMapping("/cliente")
    public ResponseEntity<?> crearReviewCliente(
            @Valid @RequestBody CreateReviewClienteRequest request,
            Authentication authentication) {

        try {
            Integer idCliente = obtenerIdUsuarioAutenticado(authentication);
            ReviewCliente review = reviewService.crearReviewCliente(idCliente, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/prestador")
    public ResponseEntity<?> crearRespuestaPrestador(
            @Valid @RequestBody CreateReviewPrestadorRequest request,
            Authentication authentication) {

        try {
            Integer idPrestador = obtenerIdUsuarioAutenticado(authentication);
            ReviewPrestador respuesta = reviewService.crearRespuestaPrestador(idPrestador, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/servicio/{idServicio}")
    public ResponseEntity<ReviewsResponse> obtenerReviewsPorServicio(
            @PathVariable Integer idServicio) {

        ReviewsResponse response = reviewService.obtenerReviewsPorServicio(idServicio);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{idReview}")
    public ResponseEntity<?> obtenerReviewPorId(@PathVariable Integer idReview) {
        try {
            ReviewCliente review = reviewService.obtenerReviewPorId(idReview);
            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Método auxiliar para obtener el ID del usuario autenticado
    private Integer obtenerIdUsuarioAutenticado(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String gmail = userDetails.getUsername();
            return usuarioService.buscarPorGmail(gmail)
                    .map(Usuario::getId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        }
        throw new RuntimeException("Usuario no autenticado");
    }

}