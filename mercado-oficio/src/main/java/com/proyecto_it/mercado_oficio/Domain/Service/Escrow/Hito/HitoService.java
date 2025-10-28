package com.proyecto_it.mercado_oficio.Domain.Service.Escrow.Hito;

import com.proyecto_it.mercado_oficio.Domain.Model.Hito;
import com.proyecto_it.mercado_oficio.Domain.Model.ModificacionHito;
import com.proyecto_it.mercado_oficio.Domain.Model.PresupuestoServicio;
import com.proyecto_it.mercado_oficio.Domain.Model.Usuario;
import com.proyecto_it.mercado_oficio.Domain.Repository.HitoRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.ModificacionHitoRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.PresupuestoServicioRepository;
import com.proyecto_it.mercado_oficio.Domain.Repository.UsuarioRepository;
import com.proyecto_it.mercado_oficio.Domain.Service.Escrow.EscrowService;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoAprobacion;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.EstadoHito;
import com.proyecto_it.mercado_oficio.Exception.ResourceNotFoundException;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito.HitoCreateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito.HitoDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito.ModificacionHitoCreateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Hito.ModificacionHitoDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.HorarioServicioDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoServicioDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto.PresupuestoServicioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Presupuesto.JpaPresupuestoServicioRepository;
import com.proyecto_it.mercado_oficio.Mapper.Hito.HitoMapper;
import com.proyecto_it.mercado_oficio.Mapper.Hito.ModificacionHitoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
@Slf4j
public class HitoService {

    @Autowired
    private HitoRepository hitoRepository;

    @Autowired
    private ModificacionHitoRepository modificacionRepository;

    @Autowired
    private PresupuestoServicioRepository presupuestoRepository;

    @Autowired
    private HitoMapper hitoMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModificacionHitoMapper modificacionMapper;

    @Autowired
    private JpaPresupuestoServicioRepository jpaPresupuestoServicioRepository;

    public Optional<Hito> obtenerPorId(Integer id) {
        return hitoRepository.obtenerPorId(id);
    }

    public Integer obtenerClienteId(Integer hitoId) {
        return hitoRepository.obtenerClienteId(hitoId);
    }

    public Integer obtenerPrestadorId(Integer hitoId) {
        return hitoRepository.obtenerPrestadorId(hitoId);
    }


    @Transactional
    public List<HitoDTO> crearHitosAutomaticos(Integer presupuestoId, List<HorarioServicioDTO> horariosSeleccionados) {
        PresupuestoServicioEntity presupuestoEntity = jpaPresupuestoServicioRepository.findById(presupuestoId)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado"));

        BigDecimal presupuestoTotal = presupuestoEntity.getPresupuesto();
        if (presupuestoTotal == null || presupuestoTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("El presupuesto total no es válido");
        }

        // Calcular porcentaje por hito
        double porcentajePorHito = 100.0 / horariosSeleccionados.size();
        List<HitoCreateDTO> hitosACrear = new ArrayList<>();

        for (HorarioServicioDTO horario : horariosSeleccionados) {
            HitoCreateDTO hitoDTO = new HitoCreateDTO();
            hitoDTO.setPorcentajePresupuesto(porcentajePorHito);

            LocalDateTime fechaInicio = LocalDateTime.parse(horario.getFecha() + "T" + horario.getHoraInicio());
            LocalDateTime fechaFin = LocalDateTime.parse(horario.getFecha() + "T" + horario.getHoraFin());

            hitoDTO.setFechaInicio(fechaInicio.toString());
            hitoDTO.setFechaFinalizacionEstimada(fechaFin.toString());

            hitosACrear.add(hitoDTO);
        }

        List<HitoDTO> hitosCreados = crearHitos(presupuestoId, hitosACrear);

        log.info("✅ {} hitos creados automáticamente para presupuesto {}", hitosCreados.size(), presupuestoId);
        return hitosCreados;
    }

    @Transactional
    public List<HitoDTO> crearHitos(Integer presupuestoId, List<HitoCreateDTO> hitosCreateDTO) {
        // Obtener el presupuesto
        PresupuestoServicioDTO presupuestoDTO = presupuestoRepository.getPresupuestoPrestadorById(presupuestoId);

        if (presupuestoDTO == null) {
            throw new ResourceNotFoundException("Presupuesto no encontrado");
        }

        // Validar que los IDs del presupuesto no sean nulos
        Integer idCliente = presupuestoDTO.getIdCliente();
        Integer idPrestador = presupuestoDTO.getIdPrestador();

        if (idCliente == null || idPrestador == null) {
            throw new IllegalStateException(
                    "El presupuesto no tiene usuario cliente o prestador asignado. " +
                            "idCliente=" + idCliente + ", idPrestador=" + idPrestador
            );
        }
        Usuario usuarioCliente = usuarioRepository.buscarPorId(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario cliente no encontrado"));

        Usuario usuarioPrestador = usuarioRepository.buscarPorId(idPrestador)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario prestador no encontrado"));

        List<HitoDTO> hitosGuardados = new ArrayList<>();

        for (HitoCreateDTO hitoCreateDTO : hitosCreateDTO) {
            Hito hito = new Hito();
            hito.setPresupuestoId(presupuestoId);
            hito.setPorcentajePresupuesto(BigDecimal.valueOf(hitoCreateDTO.getPorcentajePresupuesto()));

            hito.setMonto(presupuestoDTO.getPresupuesto()
                    .multiply(BigDecimal.valueOf(hitoCreateDTO.getPorcentajePresupuesto()).divide(BigDecimal.valueOf(100)))
                    .setScale(2, RoundingMode.HALF_UP));

            hito.setFechaInicio(hitoCreateDTO.getFechaInicio() != null ?
                    LocalDateTime.parse(hitoCreateDTO.getFechaInicio()) : LocalDateTime.now());
            hito.setFechaFinalizacionEstimada(LocalDateTime.parse(hitoCreateDTO.getFechaFinalizacionEstimada()));
            hito.setEstado(EstadoHito.PENDIENTE);

            Hito hitoGuardado = hitoRepository.guardar(hito);
            hitosGuardados.add(hitoMapper.toDTO(hitoGuardado));

            log.info("Hito #{} creado para presupuesto {}", presupuestoId);
        }

        return hitosGuardados;
    }

    @Transactional
    public void completarHito(Integer hitoId) {
        Hito hito = hitoRepository.obtenerPorId(hitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Hito no encontrado"));

        hito.setEstado(EstadoHito.COMPLETADO);
        hitoRepository.actualizar(hito);

        log.info("Hito {} marcado como completado", hitoId);
    }

    @Transactional
    public void aprobarHito(Integer hitoId) {
        Hito hito = hitoRepository.obtenerPorId(hitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Hito no encontrado"));

        hito.setEstado(EstadoHito.APROBADO_CLIENTE);
        hitoRepository.actualizar(hito);

        log.info("Hito {} aprobado por cliente", hitoId);
    }

    @Transactional
    public void actualizarEscrowId(Integer hitoId, String escrowId) {
        Hito hito = hitoRepository.obtenerPorId(hitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Hito no encontrado"));

        hitoRepository.actualizar(hito);

        log.info("Escrow ID actualizado para hito {}: {}", hitoId, escrowId);
    }

    @Transactional
    public void actualizarEstadoHito(Integer hitoId, String estadoStr) {
        Hito hito = hitoRepository.obtenerPorId(hitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Hito no encontrado"));

        EstadoHito nuevoEstado;
        try {
            nuevoEstado = EstadoHito.valueOf(estadoStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado inválido: " + estadoStr);
        }

        hito.setEstado(nuevoEstado);
        hitoRepository.actualizar(hito);

        log.info("Estado del hito {} actualizado a {}", hitoId, estadoStr);
    }

    public List<HitoDTO> obtenerHitosDelPresupuesto(Integer presupuestoId) {
        List<Hito> hitos = hitoRepository.obtenerPorPresupuestoOrdenado(presupuestoId);
        return hitoMapper.toDTOList(hitos);
    }

    public List<ModificacionHitoDTO> obtenerModificacionesPendientes(Integer hitoId) {
        List<ModificacionHito> modificaciones = modificacionRepository.obtenerModificacionesPendientes(hitoId);
        return modificacionMapper.toDTOList(modificaciones);
    }

    @Transactional
    public ModificacionHitoDTO solicitarModificacion(Integer hitoId, ModificacionHitoCreateDTO modificacionCreateDTO) {
        Hito hito = hitoRepository.obtenerPorId(hitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Hito no encontrado"));

        ModificacionHito modificacion = new ModificacionHito();
        modificacion.setHitoId(hitoId);
        modificacion.setDescripcionCambio(modificacionCreateDTO.getDescripcionCambio());
        modificacion.setMontoAnterior(hito.getMonto());
        modificacion.setMontoNuevo(BigDecimal.valueOf(modificacionCreateDTO.getMontoNuevo()));
        modificacion.setFechaInicioAnterior(hito.getFechaInicio());
        modificacion.setFechaIniciNueva(LocalDateTime.parse(modificacionCreateDTO.getFechaIniciNueva()));
        modificacion.setEstadoAprobacion(EstadoAprobacion.PENDIENTE);
        modificacion.setAprobadoCliente(false);
        modificacion.setAprobadoPrestador(false);

        ModificacionHito guardada = modificacionRepository.guardar(modificacion);
        return modificacionMapper.toDTO(guardada);
    }

    @Transactional
    public void aprobarModificacion(Integer modificacionId, Boolean porCliente) {
        ModificacionHito modificacion = modificacionRepository.obtenerPorId(modificacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Modificación no encontrada"));

        if (porCliente) {
            modificacion.setAprobadoCliente(true);
        } else {
            modificacion.setAprobadoPrestador(true);
        }

        if (Boolean.TRUE.equals(modificacion.getAprobadoCliente()) &&
                Boolean.TRUE.equals(modificacion.getAprobadoPrestador())) {

            Hito hito = hitoRepository.obtenerPorId(modificacion.getHitoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hito no encontrado"));

            hito.setMonto(modificacion.getMontoNuevo());
            hito.setFechaInicio(modificacion.getFechaIniciNueva());
            hitoRepository.actualizar(hito);

            modificacion.setEstadoAprobacion(EstadoAprobacion.APROBADO);
        }

        modificacionRepository.actualizar(modificacion);
    }

    public List<HitoDTO> obtenerHitosDelCliente(Integer clienteId) {
        List<Hito> hitos = hitoRepository.obtenerPorCliente(clienteId);
        return hitoMapper.toDTOList(hitos);
    }
}