package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.Portafolio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortafolioResponseDTO {

    private Integer id;
    private Integer servicioId;
    private String titulo;
    private String descripcion;
}
