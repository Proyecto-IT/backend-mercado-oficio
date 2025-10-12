package com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio;

import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.Portafolio.PortafolioResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioResponseDTO {

    private Integer id;
    private Integer usuarioId;
    private Integer oficioId;
    private String nombreOficio; // Nombre del oficio
    private String descripcion;
    private BigDecimal tarifaHora;
    private Map<String, String> disponibilidad;
    private Integer experiencia;
    private List<String> especialidades;
    private String ubicacion;
    private Integer trabajosCompletados;
    private String imagenUrl; // URL de imagen del usuario
    private List<PortafolioResponseDTO> portafolios;
    private String nombreTrabajador;
    private String apellidoTrabajador;
    private String emailTrabajador;
}
