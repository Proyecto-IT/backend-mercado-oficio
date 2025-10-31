package com.proyecto_it.mercado_oficio.Domain.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewPrestador {
    private Integer id;
    private Integer idPrestador;
    private Integer idReviewCliente;
    private String comentario;
    private LocalDateTime fecha;
    private String nombrePrestador; // Para mostrar en el frontend
}
