package com.proyecto_it.mercado_oficio.Domain.Service.Review;

import com.proyecto_it.mercado_oficio.Domain.Model.ReviewCliente;
import com.proyecto_it.mercado_oficio.Domain.Model.ReviewPrestador;
import com.proyecto_it.mercado_oficio.Domain.Repository.ReviewClienteRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.ReviewPrestadorRepository;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoHito;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Review.CreateReviewClienteRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Review.CreateReviewPrestadorRequest;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Review.ElegibilidadReviewResponse;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Review.ReviewsResponse;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Hito.HitoEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto.PresupuestoServicioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.ServicioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Hito.JpaHitoRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Presupuesto.JpaPresupuestoServicioRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Servicio.JpaServicioRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewClienteRepository reviewClienteRepository;
    private final ReviewPrestadorRepository reviewPrestadorRepository;
    private final JpaPresupuestoServicioRepository presupuestoRepository;
    private final JpaHitoRepository hitoRepository;
    private final JpaServicioRepository servicioRepository;
    private final JpaUsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public ElegibilidadReviewResponse verificarElegibilidadParaRevisar(
            Integer idCliente,
            Integer idServicio,
            Integer idPresupuesto) {

        // 1. Verificar que el presupuesto existe
        PresupuestoServicioEntity presupuesto = presupuestoRepository.findById(idPresupuesto)
                .orElse(null);

        if (presupuesto == null) {
            return ElegibilidadReviewResponse.builder()
                    .puedeRevisar(false)
                    .mensaje("El presupuesto no existe")
                    .build();
        }

        // 2. Verificar que el presupuesto pertenece al cliente
        if (!presupuesto.getIdCliente().equals(idCliente)) {
            return ElegibilidadReviewResponse.builder()
                    .puedeRevisar(false)
                    .mensaje("Este presupuesto no te pertenece")
                    .build();
        }

        // 3. Verificar que el presupuesto es del servicio correcto
        if (!presupuesto.getServicio().getId().equals(idServicio)) {
            return ElegibilidadReviewResponse.builder()
                    .puedeRevisar(false)
                    .mensaje("Este presupuesto no corresponde al servicio indicado")
                    .build();
        }

        // 4. Verificar que ya no ha dejado una reseña para este presupuesto
        if (reviewClienteRepository.existsByIdPresupuestoAndIdCliente(idPresupuesto, idCliente)) {
            return ElegibilidadReviewResponse.builder()
                    .puedeRevisar(false)
                    .mensaje("Ya has dejado una reseña para este servicio")
                    .build();
        }

        // 5. Obtener todos los hitos del presupuesto
        List<HitoEntity> hitos = hitoRepository.findByPresupuestoId(idPresupuesto);

        if (hitos.isEmpty()) {
            return ElegibilidadReviewResponse.builder()
                    .puedeRevisar(false)
                    .mensaje("Este presupuesto no tiene hitos registrados")
                    .build();
        }

        // 6. Verificar que TODOS los hitos estén pagados
        boolean todosHitosPagados = hitos.stream()
                .allMatch(hito -> hito.getEstado() == EstadoHito.PAGADO);

        if (!todosHitosPagados) {
            long hitosPendientes = hitos.stream()
                    .filter(hito -> hito.getEstado() != EstadoHito.PAGADO)
                    .count();

            return ElegibilidadReviewResponse.builder()
                    .puedeRevisar(false)
                    .mensaje("Aún hay " + hitosPendientes + " hito(s) sin completar. Debes completar todos los hitos para dejar una reseña")
                    .build();
        }

        // 7. Todo está correcto, puede dejar una reseña
        return ElegibilidadReviewResponse.builder()
                .puedeRevisar(true)
                .mensaje("Puedes dejar una reseña para este servicio")
                .idPresupuesto(idPresupuesto)
                .build();
    }

    @Override
    @Transactional
    public ReviewCliente crearReviewCliente(Integer idCliente, CreateReviewClienteRequest request) {
        // Validar entrada
        if (request.getValoracion() < 1 || request.getValoracion() > 5) {
            throw new IllegalArgumentException("La valoración debe estar entre 1 y 5");
        }

        if (request.getComentario() == null || request.getComentario().trim().isEmpty()) {
            throw new IllegalArgumentException("El comentario es obligatorio");
        }

        // Verificar elegibilidad
        ElegibilidadReviewResponse elegibilidad = verificarElegibilidadParaRevisar(
                idCliente,
                request.getIdServicio(),
                request.getIdPresupuesto()
        );

        if (!elegibilidad.getPuedeRevisar()) {
            throw new IllegalStateException(elegibilidad.getMensaje());
        }

        // Crear la reseña
        ReviewCliente reviewCliente = ReviewCliente.builder()
                .idCliente(idCliente)
                .idServicio(request.getIdServicio())
                .idPresupuesto(request.getIdPresupuesto())
                .comentario(request.getComentario().trim())
                .valoracion(request.getValoracion())
                .build();

        return reviewClienteRepository.save(reviewCliente);
    }

    @Override
    @Transactional
    public ReviewPrestador crearRespuestaPrestador(Integer idPrestador, CreateReviewPrestadorRequest request) {
        // Validar entrada
        if (request.getComentario() == null || request.getComentario().trim().isEmpty()) {
            throw new IllegalArgumentException("El comentario es obligatorio");
        }

        // Verificar que la review existe
        ReviewCliente reviewCliente = reviewClienteRepository.findById(request.getIdReviewCliente())
                .orElseThrow(() -> new IllegalArgumentException("La reseña no existe"));

        // Verificar que el prestador es el dueño del servicio
        ServicioEntity servicio = servicioRepository.findById(reviewCliente.getIdServicio())
                .orElseThrow(() -> new IllegalArgumentException("El servicio no existe"));

        if (!servicio.getUsuario().getId().equals(idPrestador)) {
            throw new IllegalStateException("No tienes permiso para responder esta reseña");
        }

        // Verificar que no haya respondido antes
        if (reviewPrestadorRepository.existsByIdReviewCliente(request.getIdReviewCliente())) {
            throw new IllegalStateException("Ya has respondido esta reseña");
        }

        // Crear la respuesta
        ReviewPrestador reviewPrestador = ReviewPrestador.builder()
                .idPrestador(idPrestador)
                .idReviewCliente(request.getIdReviewCliente())
                .comentario(request.getComentario().trim())
                .build();

        return reviewPrestadorRepository.save(reviewPrestador);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewsResponse obtenerReviewsPorServicio(Integer idServicio) {
        List<ReviewCliente> reviews = reviewClienteRepository.findByIdServicio(idServicio);

        // Enriquecer con nombres de usuarios
        reviews.forEach(review -> {
            usuarioRepository.findById(review.getIdCliente())
                    .ifPresent(usuario -> review.setNombreCliente(usuario.getNombre()));

            if (review.getRespuestaPrestador() != null) {
                usuarioRepository.findById(review.getRespuestaPrestador().getIdPrestador())
                        .ifPresent(usuario ->
                                review.getRespuestaPrestador().setNombrePrestador(usuario.getNombre()));
            }
        });

        Double promedio = reviewClienteRepository.calcularPromedioValoracion(idServicio);
        Long total = reviewClienteRepository.contarReviewsPorServicio(idServicio);

        return ReviewsResponse.builder()
                .reviews(reviews)
                .promedioValoracion(promedio != null ? promedio : 0.0)
                .totalReviews(total != null ? total.intValue() : 0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewCliente obtenerReviewPorId(Integer idReview) {
        ReviewCliente review = reviewClienteRepository.findById(idReview)
                .orElseThrow(() -> new IllegalArgumentException("La reseña no existe"));

        // Enriquecer con nombres
        usuarioRepository.findById(review.getIdCliente())
                .ifPresent(usuario -> review.setNombreCliente(usuario.getNombre()));

        if (review.getRespuestaPrestador() != null) {
            usuarioRepository.findById(review.getRespuestaPrestador().getIdPrestador())
                    .ifPresent(usuario ->
                            review.getRespuestaPrestador().setNombrePrestador(usuario.getNombre()));
        }

        return review;
    }
}