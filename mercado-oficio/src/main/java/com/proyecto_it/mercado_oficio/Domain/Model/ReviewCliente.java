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
public class ReviewCliente {
    private Integer id;
    private Integer idCliente;
    private Integer idServicio;
    private Integer idPresupuesto;
    private String comentario;
    private Integer valoracion;
    private LocalDateTime fecha;
    private ReviewPrestador respuestaPrestador;
    private String nombreCliente; // Para mostrar en el frontend
}

