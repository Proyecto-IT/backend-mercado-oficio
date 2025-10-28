package com.proyecto_it.mercado_oficio.Mapper.Servicio;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto_it.mercado_oficio.Domain.Model.Oficio;
import com.proyecto_it.mercado_oficio.Domain.Model.Portafolio;
import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;
import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Repository.OficioRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.UsuarioRepository;
import com.proyecto_it.mercado_oficio.Domain.Service.Oficio.OficioCacheService;
import com.proyecto_it.mercado_oficio.Domain.Service.Oficio.OficioService;
import com.proyecto_it.mercado_oficio.Domain.Service.Usuario.UsuarioCacheService;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.Disponibilidad;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.Especialidades;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.Portafolio.PortafolioResponseDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.ServicioRequestDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.ServicioResponseDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.ServicioUpdateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.ServicioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.proyecto_it.mercado_oficio.Mapper.Servicio.Portafolio.PortafolioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
public class ServicioMapper {

    private final ObjectMapper objectMapper;
    private final UsuarioCacheService usuarioCacheService;
    private final OficioCacheService oficioCacheService;

    public Servicio toDomain(ServicioRequestDTO dto, Integer usuarioId) {
        return Servicio.builder()
                .usuarioId(usuarioId)
                .oficioId(dto.getOficioId())
                .descripcion(dto.getDescripcion())
                .tarifaHora(dto.getTarifaHora())
                .disponibilidad(new Disponibilidad(dto.getDisponibilidad()))
                .experiencia(dto.getExperiencia())
                .especialidades(new Especialidades(dto.getEspecialidades()))
                .ubicacion(dto.getUbicacion())
                .trabajosCompletados(0)
                .build();
    }

    public Servicio toDomain(ServicioUpdateDTO dto) {
        return Servicio.builder()
                .oficioId(dto.getOficioId())
                .descripcion(dto.getDescripcion())
                .tarifaHora(dto.getTarifaHora())
                .disponibilidad(dto.getDisponibilidad() != null ?
                        new Disponibilidad(dto.getDisponibilidad()) : null)
                .experiencia(dto.getExperiencia())
                .especialidades(dto.getEspecialidades() != null ?
                        new Especialidades(dto.getEspecialidades()) : null)
                .ubicacion(dto.getUbicacion())
                .build();
    }

    public Servicio toDomain(ServicioEntity entity) {
        return Servicio.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuario().getId())
                .oficioId(entity.getOficioId())
                .descripcion(entity.getDescripcion())
                .tarifaHora(entity.getTarifaHora() != null ?
                        new java.math.BigDecimal(entity.getTarifaHora()) : null)
                .disponibilidad(parseDisponibilidad(entity.getDisponibilidad()))
                .experiencia(entity.getExperiencia())
                .especialidades(new Especialidades(parseEspecialidades(entity.getEspecialidades())))
                .ubicacion(entity.getUbicacion())
                .trabajosCompletados(entity.getTrabajosCompletados())
                .build();
    }


    public ServicioResponseDTO toResponseDTO(Servicio servicio) {
        Usuario usuario = usuarioCacheService.buscarPorIdCached(servicio.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Oficio oficio = oficioCacheService.buscarPorIdCached(servicio.getOficioId())
                .orElseThrow(() -> new RuntimeException("Oficio no encontrado"));

        return ServicioResponseDTO.builder()
                .id(servicio.getId())
                .usuarioId(servicio.getUsuarioId())
                .oficioId(servicio.getOficioId())
                .nombreOficio(oficio.getNombre())
                .descripcion(servicio.getDescripcion())
                .tarifaHora(servicio.getTarifaHora())
                .disponibilidad(servicio.getDisponibilidad() != null ?
                        servicio.getDisponibilidad().getHorarios() : null)
                .experiencia(servicio.getExperiencia())
                .especialidades(servicio.getEspecialidades() != null ?
                        servicio.getEspecialidades().getItems() : null)
                .ubicacion(servicio.getUbicacion())
                .trabajosCompletados(servicio.getTrabajosCompletados())
                .nombreTrabajador(usuario.getNombre())
                .apellidoTrabajador(usuario.getApellido())
                .emailTrabajador(usuario.getGmail())
                .imagenUsuario(convertirImagenABase64(usuario.getImagen()))
                .imagenUsuarioTipo(usuario.getImagenTipo())
                .portafolios(new ArrayList<>())
                .build();
    }

    public ServicioResponseDTO toResponseDTOWithPortafolios(
            Servicio servicio,
            List<Portafolio> portafolios,
            PortafolioMapper portafolioMapper) {

        Usuario usuario = usuarioCacheService.buscarPorIdCached(servicio.getUsuarioId())
                .orElse(null);

        String nombreOficio = oficioCacheService.buscarPorIdCached(servicio.getOficioId())
                .map(Oficio::getNombre)
                .orElse("Desconocido");

        return ServicioResponseDTO.builder()
                .id(servicio.getId())
                .usuarioId(servicio.getUsuarioId())
                .oficioId(servicio.getOficioId())
                .nombreOficio(nombreOficio)
                .descripcion(servicio.getDescripcion())
                .tarifaHora(servicio.getTarifaHora())
                .disponibilidad(servicio.getDisponibilidad() != null ?
                        servicio.getDisponibilidad().getHorarios() : null)
                .experiencia(servicio.getExperiencia())
                .especialidades(servicio.getEspecialidades() != null ?
                        servicio.getEspecialidades().getItems() : null)
                .ubicacion(servicio.getUbicacion())
                .trabajosCompletados(servicio.getTrabajosCompletados())
                .portafolios(portafolios.stream()
                        .map(portafolioMapper::toResponseDTO)
                        .collect(Collectors.toList()))
                .nombreTrabajador(usuario != null ? usuario.getNombre() : null)
                .apellidoTrabajador(usuario != null ? usuario.getApellido() : null)
                .emailTrabajador(usuario != null ? usuario.getGmail() : null)
                .imagenUsuario(usuario != null ? convertirImagenABase64(usuario.getImagen()) : null)
                .imagenUsuarioTipo(usuario != null ? usuario.getImagenTipo() : null)
                .build();
    }


    public ServicioEntity toEntity(Servicio servicio, UsuarioEntity usuario) {
        ServicioEntity entity = new ServicioEntity();
        entity.setId(servicio.getId());
        entity.setUsuario(usuario);
        entity.setOficioId(servicio.getOficioId());
        entity.setDescripcion(servicio.getDescripcion());
        entity.setTarifaHora(servicio.getTarifaHora() != null ?
                servicio.getTarifaHora().toString() : null);
        entity.setDisponibilidad(serializeDisponibilidad(servicio.getDisponibilidad()));
        entity.setExperiencia(servicio.getExperiencia());
        entity.setEspecialidades(serializeEspecialidades(servicio.getEspecialidades()));
        entity.setUbicacion(servicio.getUbicacion());
        entity.setTrabajosCompletados(servicio.getTrabajosCompletados());

        return entity;
    }


    public Disponibilidad parseDisponibilidad(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return null;
            }
            Map<String, String> horarios = objectMapper.readValue(
                    json,
                    new TypeReference<Map<String, String>>() {}
            );
            return new Disponibilidad(horarios);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al parsear disponibilidad: " + e.getMessage());
        }
    }

    public String serializeDisponibilidad(Disponibilidad disponibilidad) {
        try {
            if (disponibilidad == null) {
                return null;
            }
            return objectMapper.writeValueAsString(disponibilidad.getHorarios());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar disponibilidad: " + e.getMessage());
        }
    }

    public List<String> parseEspecialidades(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return List.of();
            }
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al parsear especialidades: " + e.getMessage());
        }
    }

    public String serializeEspecialidades(Especialidades especialidades) {
        try {
            if (especialidades == null) {
                return null;
            }
            return objectMapper.writeValueAsString(especialidades.getItems());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar especialidades: " + e.getMessage());
        }
    }

    private String convertirImagenABase64(byte[] imagen) {
        if (imagen == null || imagen.length == 0) {
            return null;
        }
        return java.util.Base64.getEncoder().encodeToString(imagen);
    }
}