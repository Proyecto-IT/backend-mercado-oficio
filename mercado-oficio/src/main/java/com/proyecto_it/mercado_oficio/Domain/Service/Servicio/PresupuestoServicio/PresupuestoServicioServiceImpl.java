package com.proyecto_it.mercado_oficio.Domain.Service.Servicio.PresupuestoServicio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto_it.mercado_oficio.Domain.Model.PresupuestoServicio;
import com.proyecto_it.mercado_oficio.Domain.Model.Servicio;
import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Repository.PresupuestoArchivoRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.PresupuestoServicioRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.ServicioRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.UsuarioRepository;
import com.proyecto_it.mercado_oficio.Domain.Service.Escrow.Hito.HitoService;
import com.proyecto_it.mercado_oficio.Domain.Service.Servicio.ServicioService;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.Disponibilidad;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoPresupuesto;
import com.proyecto_it.mercado_oficio.Exception.ResourceNotFoundException;
import com.proyecto_it.mercado_oficio.Exception.ValidationException;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.HorarioServicioDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoCreateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioCreateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioUpdateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Servicio.DisponibilidadHoraria;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto.PresupuestoServicioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.ServicioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Presupuesto.JpaPresupuestoServicioRepository;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Servicio.JpaServicioRepository;
import com.proyecto_it.mercado_oficio.Mapper.Presupuesto.PresupuestoArchivo.PresupuestoServicioMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PresupuestoServicioServiceImpl implements PresupuestoServicioService {
    @Autowired
    private PresupuestoServicioRepository presupuestoRepository;

    @Autowired
    private PresupuestoArchivoRepository archivoRepository;

    @Autowired
    private JpaPresupuestoServicioRepository jpaPresupuestoServicioRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private JpaServicioRepository jpaServicioRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PresupuestoServicioMapper mapper;

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private HitoService hitoService;

    ObjectMapper objectMapper = new ObjectMapper();


    public PresupuestoServicioDTO crear(PresupuestoServicioCreateDTO dto) {
        try {
            Servicio servicio = servicioService.obtenerServicioPorId(dto.getServicioId());
            PresupuestoServicioDTO presupuestoDTO = new PresupuestoServicioDTO();
            presupuestoDTO.setIdCliente(dto.getIdCliente());
            presupuestoDTO.setDescripcionProblema(dto.getDescripcionProblema());
            presupuestoDTO.setEstado(EstadoPresupuesto.PENDIENTE);

            return presupuestoRepository.guardar(presupuestoDTO, servicio);
        } catch (Exception e) {
            log.error("Error al crear presupuesto", e);
            throw new RuntimeException("No se pudo crear el presupuesto: " + e.getMessage());
        }
    }

    @Override
    public boolean estaRespondido(Integer presupuestoId) {
        return presupuestoRepository.estaRespondido(presupuestoId);
    }

    public PresupuestoServicioDTO obtener(Integer id) {
        return presupuestoRepository.getPresupuestoById(id);
    }

    public List<PresupuestoServicioDTO> obtenerPorCliente(Integer idCliente) {
        return presupuestoRepository.obtenerPorCliente(idCliente);
    }

    public List<PresupuestoServicioDTO> obtenerPorPrestador(Integer idPrestador) {
        return presupuestoRepository.obtenerPorPrestador(idPrestador);
    }

    public List<PresupuestoServicioDTO> obtenerPorEstado(EstadoPresupuesto estado) {
        return presupuestoRepository.obtenerPorEstado(estado);
    }

    public PresupuestoServicioDTO actualizar(Integer id, PresupuestoServicioUpdateDTO dto) {
        PresupuestoServicioDTO presupuestoDTO = presupuestoRepository.actualizar(id, dto);

        if (dto.getEstado() == EstadoPresupuesto.APROBADO &&
                dto.getHorariosSeleccionados() != null &&
                !dto.getHorariosSeleccionados().isEmpty()) {

            actualizarDisponibilidadPrestador(dto.getIdPrestador(), dto.getHorariosSeleccionados());

            hitoService.crearHitosAutomaticos(id, dto.getHorariosSeleccionados());
        }

        return presupuestoDTO;
    }

    private void actualizarDisponibilidadPrestador(Integer idPrestador, List<HorarioServicioDTO> horariosSeleccionados) {
        try {
            Usuario prestador = usuarioRepository.buscarPorId(idPrestador)
                    .orElseThrow(() -> new ResourceNotFoundException("Prestador no encontrado"));

            ServicioEntity servicioEntity = jpaServicioRepository.findByUsuarioId(prestador.getId())
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Servicio del prestador no encontrado"));

            Map<String, String> horarios = servicioEntity.getDisponibilidad() != null
                    ? objectMapper.readValue(servicioEntity.getDisponibilidad(), new TypeReference<Map<String, String>>() {})
                    : new HashMap<>();

            for (HorarioServicioDTO horario : horariosSeleccionados) {
                String dia = obtenerDiaSemana(horario.getFecha()).toLowerCase();

                if (horarios.containsKey(dia)) {
                    String rangosActuales = horarios.get(dia);

                    String nuevosRangos = restarHorarioDeDisponibilidad(
                            rangosActuales,
                            horario.getHoraInicio(),
                            horario.getHoraFin()
                    );

                    if (nuevosRangos != null && !nuevosRangos.isEmpty()) {
                        horarios.put(dia, nuevosRangos);
                    } else {
                        horarios.remove(dia);
                    }
                }
            }

            String json = objectMapper.writeValueAsString(horarios);
            servicioEntity.setDisponibilidad(json);
            jpaServicioRepository.save(servicioEntity);

        } catch (Exception e) {
            System.err.println("Error al actualizar disponibilidad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String obtenerDiaSemana(LocalDate fecha) {
        return fecha.getDayOfWeek().getDisplayName(
                TextStyle.FULL,
                new Locale("es", "ES")
        );
    }

    private String restarHorarioDeDisponibilidad(
            String rangosActuales,
            LocalTime horaOcupadaInicio,
            LocalTime horaOcupadaFin) {

        List<String> rangos = new ArrayList<>(Arrays.asList(rangosActuales.split(",")));
        List<String> nuevosRangos = new ArrayList<>();

        for (String rango : rangos) {
            String[] partes = rango.trim().split("-");
            LocalTime horaDispoInicio = LocalTime.parse(partes[0]);
            LocalTime horaDispoFin = LocalTime.parse(partes[1]);

            if (horaOcupadaFin.isBefore(horaDispoInicio) || horaOcupadaInicio.isAfter(horaDispoFin)) {
                nuevosRangos.add(horaDispoInicio + "-" + horaDispoFin);
                continue;
            }

            if ((horaOcupadaInicio.isBefore(horaDispoInicio) || horaOcupadaInicio.equals(horaDispoInicio)) &&
                    (horaOcupadaFin.isAfter(horaDispoFin) || horaOcupadaFin.equals(horaDispoFin))) {
                continue;
            }

            if (horaDispoInicio.isBefore(horaOcupadaInicio)) {
                nuevosRangos.add(horaDispoInicio + "-" + horaOcupadaInicio);
            }

            if (horaOcupadaFin.isBefore(horaDispoFin)) {
                nuevosRangos.add(horaOcupadaFin + "-" + horaDispoFin);
            }
        }

        return nuevosRangos.isEmpty() ? null : String.join(",", nuevosRangos);
    }

    @Transactional
    public PresupuestoServicioDTO actualizarEstado(Integer id, EstadoPresupuesto nuevoEstado) {
        PresupuestoServicioEntity entity = presupuestoRepository.getEntityById(id);

        entity.setEstado(nuevoEstado);
        entity.setFechaActualizacion(LocalDateTime.now());

        PresupuestoServicioEntity actualizado = jpaPresupuestoServicioRepository.save(entity);

        return mapper.toDTO(actualizado);
    }



    public void eliminar(Integer id) {
        presupuestoRepository.eliminar(id);
    }

    public PresupuestoArchivoDTO cargarArchivo(Integer presupuestoId, PresupuestoArchivoCreateDTO dto) throws ValidationException {
        return archivoRepository.guardar(presupuestoId, dto);
    }

    public List<PresupuestoArchivoDTO> obtenerArchivos(Integer presupuestoId) {
        return archivoRepository.obtenerPorPresupuesto(presupuestoId);
    }
    @Override
    public PresupuestoArchivoDTO obtenerPorId(Integer id) {
        return archivoRepository.obtenerPorId(id);
    }
    public void eliminarArchivo(Integer archivoId) {
        archivoRepository.eliminar(archivoId);
    }

    private List<DisponibilidadHoraria> parseDisponibilidad(String disponibilidadJSON) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(disponibilidadJSON, new TypeReference<List<DisponibilidadHoraria>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    private List<DisponibilidadHoraria> eliminarHorarioDeDisponibilidad(
            List<DisponibilidadHoraria> disponibilidad,
            HorarioServicioDTO horario) {

        String diaSemana = horario.getFecha().getDayOfWeek().getDisplayName(
                TextStyle.FULL, Locale.forLanguageTag("es")).toUpperCase();

        for (DisponibilidadHoraria disp : disponibilidad) {
            if (disp.getDia().equals(diaSemana)) {
                LocalTime dispInicio = LocalTime.parse(disp.getHoraInicio());
                LocalTime dispFin = LocalTime.parse(disp.getHoraFin());
                LocalTime servicioInicio = horario.getHoraInicio();
                LocalTime servicioFin = horario.getHoraFin();

                if (servicioInicio.isAfter(dispInicio) && servicioFin.isBefore(dispFin)) {
                    disp.setHoraFin(servicioInicio.toString());
                    DisponibilidadHoraria nuevaFranja = new DisponibilidadHoraria();
                    nuevaFranja.setDia(disp.getDia());
                    nuevaFranja.setHoraInicio(servicioFin.toString());
                    nuevaFranja.setHoraFin(dispFin.toString());
                    disponibilidad.add(nuevaFranja);
                } else if (servicioInicio.isBefore(dispInicio) && servicioFin.isAfter(dispFin)) {
                    disponibilidad.remove(disp);
                } else if (servicioFin.isAfter(dispInicio) && servicioFin.isBefore(dispFin)) {
                    disp.setHoraInicio(servicioFin.toString());
                } else if (servicioInicio.isAfter(dispInicio) && servicioInicio.isBefore(dispFin)) {
                    disp.setHoraFin(servicioInicio.toString());
                }
            }
        }

        return disponibilidad;
    }
    @Override
    public List<PresupuestoServicioDTO> obtenerPorServicio(Integer servicioId) {
        log.info("Obteniendo presupuestos para servicio: {}", servicioId);
        return presupuestoRepository.obtenerPorServicio(servicioId);
    }
    private String convertToJSON(List<DisponibilidadHoraria> disponibilidad) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(disponibilidad);
        } catch (Exception e) {
            return "[]";
        }
    }
}