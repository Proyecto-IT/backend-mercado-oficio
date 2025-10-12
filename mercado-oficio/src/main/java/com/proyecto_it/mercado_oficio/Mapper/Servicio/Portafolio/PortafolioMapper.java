package com.proyecto_it.mercado_oficio.Mapper.Servicio.Portafolio;
import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.Portafolio.PortafolioRequestDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.Portafolio.PortafolioResponseDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.Portafolio.PortafolioUpdateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.PortafolioEntity;
import org.springframework.stereotype.Component;

@Component
public class PortafolioMapper {

    // ===== DTO -> DOMAIN =====

    public Portafolio toDomain(PortafolioRequestDTO dto, Integer servicioId) {
        return Portafolio.builder()
                .servicioId(servicioId)
                .titulo(dto.getTitulo())
                .descripcion(dto.getDescripcion())
                .build();
    }

    public Portafolio toDomain(PortafolioUpdateDTO dto) {
        return Portafolio.builder()
                .titulo(dto.getTitulo())
                .descripcion(dto.getDescripcion())
                .build();
    }

    public Portafolio toDomain(PortafolioEntity entity) {
        return Portafolio.builder()
                .id(entity.getId())
                .servicioId(entity.getServicio().getId())
                .titulo(entity.getTitulo())
                .descripcion(entity.getDescripcion())
                .build();
    }

    // ===== DOMAIN -> DTO =====

    public PortafolioResponseDTO toResponseDTO(Portafolio portafolio) {
        return PortafolioResponseDTO.builder()
                .id(portafolio.getId())
                .servicioId(portafolio.getServicioId())
                .titulo(portafolio.getTitulo())
                .descripcion(portafolio.getDescripcion())
                .build();
    }

    // ===== DOMAIN -> ENTITY =====

    public PortafolioEntity toEntity(Portafolio portafolio) {
        PortafolioEntity entity = new PortafolioEntity();
        entity.setId(portafolio.getId());
        // El servicio debe ser cargado del repositorio
        entity.setTitulo(portafolio.getTitulo());
        entity.setDescripcion(portafolio.getDescripcion());
        return entity;
    }
}
