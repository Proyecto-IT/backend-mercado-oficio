package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Presupuesto.PresupuestoArchivo;

import com.proyecto_it.mercado_oficio.Domain.Repository.PresupuestoArchivoRepository;
import com.proyecto_it.mercado_oficio.Domain.ValueObjects.TipoArchivo;
import com.proyecto_it.mercado_oficio.Exception.ResourceNotFoundException;
import com.proyecto_it.mercado_oficio.Exception.ValidationException;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoCreateDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.Presupuesto.PresupuestoArchivo.PresupuestoArchivoDTO;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto.PresupuestoArchivo.PresupuestoArchivoEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Presupuesto.PresupuestoServicioEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Presupuesto.JpaPresupuestoServicioRepository;
import com.proyecto_it.mercado_oficio.Mapper.Presupuesto.PresupuestoArchivo.PresupuestoServicioMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class PresupuestoArchivoRepositoryImpl implements PresupuestoArchivoRepository {

    @Autowired
    private JpaPresupuestoArchivoRepository jpaRepository;

    @Autowired
    private JpaPresupuestoServicioRepository jpaPresupuestoRepository;

    @Autowired
    private PresupuestoServicioMapper mapper;

    @Override
    public PresupuestoArchivoDTO guardar(Integer presupuestoId, PresupuestoArchivoCreateDTO dto) {
        try {
            dto.validarArchivo();

            PresupuestoServicioEntity presupuesto = jpaPresupuestoRepository.findById(presupuestoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado con ID: " + presupuestoId));

            TipoArchivo tipoArchivo = dto.obtenerTipoArchivo();

            long cantidadExistente = jpaRepository.countByPresupuestoServicioIdAndTipoArchivo(presupuestoId, tipoArchivo);

            if (tipoArchivo == TipoArchivo.VIDEO && cantidadExistente >= 1) {
                throw new ValidationException("Solo se permite 1 video por presupuesto");
            }

            if (tipoArchivo == TipoArchivo.IMAGEN && cantidadExistente >= 5) {
                throw new ValidationException("Solo se permite 5 imágenes por presupuesto");
            }

            byte[] contenido = dto.getArchivo().getBytes();
            double tamaniomB = dto.getArchivo().getSize() / (1024.0 * 1024.0);

            PresupuestoArchivoEntity archivo = new PresupuestoArchivoEntity();
            archivo.setPresupuestoServicio(presupuesto);
            archivo.setNombreArchivo(dto.getArchivo().getOriginalFilename());
            archivo.setContenido(contenido);
            archivo.setTipoMime(dto.getArchivo().getContentType());
            archivo.setTipoArchivo(tipoArchivo);
            archivo.setTamanioMb(BigDecimal.valueOf(tamaniomB));

            PresupuestoArchivoEntity guardado = jpaRepository.save(archivo);
            return mapper.archivoToDTO(guardado);

        } catch (IOException e) {
            log.error("Error al leer el archivo", e);
            throw new RuntimeException("Error al procesar el archivo: " + e.getMessage());
        }
    }
    public List<PresupuestoArchivoDTO> getArchivosByPresupuestoId(Integer id) {
        return jpaRepository.findArchivosByPresupuestoId(id);
    }
    @Override
    public PresupuestoArchivoDTO obtenerPorId(Integer id) {
        PresupuestoArchivoEntity archivo = jpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado con ID: " + id));
        return mapper.archivoToDTO(archivo);
    }

    @Override
    public List<PresupuestoArchivoDTO> obtenerPorPresupuesto(Integer presupuestoId) {
        List<PresupuestoArchivoEntity> archivos = jpaRepository.findByPresupuestoServicioId(presupuestoId);
        return archivos.stream()
                .map(mapper::archivoToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Integer id) {
        PresupuestoArchivoEntity archivo = jpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado con ID: " + id));
        jpaRepository.delete(archivo);
    }

    @Override
    public long contarPorTipo(Integer presupuestoId, TipoArchivo tipoArchivo) {
        return jpaRepository.countByPresupuestoServicioIdAndTipoArchivo(presupuestoId, tipoArchivo);
    }
}
