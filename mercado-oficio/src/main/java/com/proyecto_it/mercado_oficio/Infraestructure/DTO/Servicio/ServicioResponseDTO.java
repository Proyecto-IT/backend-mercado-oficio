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
    private String nombreOficio;
    private String descripcion;
    private BigDecimal tarifaHora;
    private Map<String, String> disponibilidad;
    private int experiencia;
    private List<String> especialidades;
    private String ubicacion;
    private int trabajosCompletados;
    private String nombreTrabajador;
    private String apellidoTrabajador;
    private String emailTrabajador;
    private String imagenUsuario; // Base64
    private String imagenUsuarioTipo; // image/jpeg, image/png, etc.

    private List<PortafolioResponseDTO> portafolios;
}
