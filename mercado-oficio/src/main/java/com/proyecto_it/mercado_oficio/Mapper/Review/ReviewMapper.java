package com.proyecto_it.mercado_oficio.Mapper.Review;

import com.proyecto_it.mercado_oficio.Domain.Model.ReviewCliente;
import com.proyecto_it.mercado_oficio.Domain.Model.ReviewPrestador;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Review.ReviewClienteEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Review.ReviewPrestadorEntity;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewCliente toDomain(ReviewClienteEntity entity) {
        if (entity == null) return null;

        return ReviewCliente.builder()
                .id(entity.getId())
                .idCliente(entity.getIdCliente())
                .idServicio(entity.getIdServicio())
                .idPresupuesto(entity.getIdPresupuesto())
                .comentario(entity.getComentario())
                .valoracion(entity.getValoracion())
                .fecha(entity.getFecha())
                .respuestaPrestador(entity.getRespuestaPrestador() != null ?
                        toDomainPrestador(entity.getRespuestaPrestador()) : null)
                .build();
    }

    public ReviewClienteEntity toEntity(ReviewCliente domain) {
        if (domain == null) return null;

        ReviewClienteEntity entity = new ReviewClienteEntity();
        entity.setId(domain.getId());
        entity.setIdCliente(domain.getIdCliente());
        entity.setIdServicio(domain.getIdServicio());
        entity.setIdPresupuesto(domain.getIdPresupuesto());
        entity.setComentario(domain.getComentario());
        entity.setValoracion(domain.getValoracion());
        entity.setFecha(domain.getFecha());

        return entity;
    }

    public ReviewPrestador toDomainPrestador(ReviewPrestadorEntity entity) {
        if (entity == null) return null;

        return ReviewPrestador.builder()
                .id(entity.getId())
                .idPrestador(entity.getIdPrestador())
                .idReviewCliente(entity.getReviewCliente().getId())
                .comentario(entity.getComentario())
                .fecha(entity.getFecha())
                .build();
    }

    public ReviewPrestadorEntity toEntity(ReviewPrestador domain) {
        if (domain == null) return null;

        ReviewPrestadorEntity entity = new ReviewPrestadorEntity();
        entity.setId(domain.getId());
        entity.setIdPrestador(domain.getIdPrestador());
        entity.setComentario(domain.getComentario());
        entity.setFecha(domain.getFecha());

        // La relación con ReviewClienteEntity se maneja en el servicio
        if (domain.getIdReviewCliente() != null) {
            ReviewClienteEntity reviewCliente = new ReviewClienteEntity();
            reviewCliente.setId(domain.getIdReviewCliente());
            entity.setReviewCliente(reviewCliente);
        }

        return entity;
    }
}