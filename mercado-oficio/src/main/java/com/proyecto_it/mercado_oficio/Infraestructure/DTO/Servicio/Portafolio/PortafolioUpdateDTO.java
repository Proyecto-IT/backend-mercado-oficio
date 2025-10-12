package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.Portafolio;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortafolioUpdateDTO {

    @Size(max = 100, message = "El título no puede exceder 100 caracteres")
    private String titulo;

    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;
}
