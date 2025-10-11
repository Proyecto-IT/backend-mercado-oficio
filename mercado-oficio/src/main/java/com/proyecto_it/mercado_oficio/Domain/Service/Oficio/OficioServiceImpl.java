package com.proyecto_it.mercado_oficio.Domain.Service.Oficio;

import com.proyecto_it.mercado_oficio.Domain.Model.Oficio;
import com.proyecto_it.mercado_oficio.Domain.Repository.OficioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OficioServiceImpl implements OficioService {

    private final OficioRepository oficioRepository;
    private final OficioCacheService cacheService;

    @Override
    public Oficio crearOficio(Oficio oficio) {
        log.info("Intentando crear oficio: {}", oficio.getNombre());

        try {
            // Verificar que no exista otro oficio con el mismo nombre
            List<Oficio> existentes = cacheService.buscarPorNombreCached(oficio.getNombre());
            if (!existentes.isEmpty()) {
                log.warn("No se puede crear oficio, ya existe: {}", oficio.getNombre());
                throw new IllegalArgumentException("Ya existe un oficio con ese nombre");
            }

            Oficio creado = oficioRepository.guardar(oficio);
            log.info("Oficio creado exitosamente: id={}, nombre={}", creado.getId(), creado.getNombre());

            // Actualizar cache: agrega el nuevo oficio e invalida la lista completa
            cacheService.actualizarOficioEnCache(creado);

            return creado;
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al crear oficio: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al crear oficio {}: {}", oficio.getNombre(), e.getMessage(), e);
            throw new RuntimeException("Error al crear oficio", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Oficio> listarTodos() {
        log.info("Listando todos los oficios desde cache");
        try {
            return cacheService.listarTodosCached();
        } catch (Exception e) {
            log.error("Error al listar oficios: {}", e.getMessage(), e);
            throw new RuntimeException("Error al listar oficios", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Oficio> buscarPorNombre(String nombre) {
        log.info("Buscando oficios por nombre: {}", nombre);
        try {
            return cacheService.buscarPorNombreCached(nombre);
        } catch (Exception e) {
            log.error("Error al buscar oficios por nombre {}: {}", nombre, e.getMessage(), e);
            throw new RuntimeException("Error al buscar oficios por nombre", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Oficio> buscarPorId(Integer id) {
        log.info("Buscando oficio por id: {}", id);
        try {
            return cacheService.buscarPorIdCached(id);
        } catch (Exception e) {
            log.error("Error al buscar oficio por ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error al buscar oficio", e);
        }
    }

    @Override
    public Oficio actualizarOficio(Oficio oficio) {
        log.info("Intentando actualizar oficio: id={}, nombre={}", oficio.getId(), oficio.getNombre());

        try {
            // Verificar que no exista otro oficio con el mismo nombre
            List<Oficio> existentes = cacheService.buscarPorNombreCached(oficio.getNombre());
            boolean existeOtro = existentes.stream()
                    .anyMatch(o -> !o.getId().equals(oficio.getId()));

            if (existeOtro) {
                log.warn("No se puede actualizar, ya existe otro oficio con el nombre: {}", oficio.getNombre());
                throw new IllegalArgumentException("Ya existe otro oficio con ese nombre");
            }

            // Obtener el oficio antiguo para limpiar su cache por nombre si cambió
            Optional<Oficio> antiguoOpt = cacheService.buscarPorIdCached(oficio.getId());

            Oficio actualizado = oficioRepository.actualizar(oficio);
            log.info("Oficio actualizado: id={}, nombre={}", actualizado.getId(), actualizado.getNombre());

            // Limpiar cache del nombre antiguo si cambió
            if (antiguoOpt.isPresent() && !antiguoOpt.get().getNombre().equalsIgnoreCase(actualizado.getNombre())) {
                cacheService.evictOficioPorNombre(antiguoOpt.get().getNombre());
            }

            // Actualizar cache con el nuevo oficio e invalidar lista completa
            cacheService.actualizarOficioEnCache(actualizado);

            return actualizado;
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al actualizar oficio ID {}: {}", oficio.getId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar oficio ID {}: {}", oficio.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al actualizar oficio", e);
        }
    }

    @Override
    public void eliminarOficio(Integer id) {
        log.info("Eliminando oficio con id: {}", id);

        try {
            // Obtener el oficio antes de eliminarlo para limpiar cache
            Optional<Oficio> oficioOpt = cacheService.buscarPorIdCached(id);

            if (oficioOpt.isEmpty()) {
                log.warn("Intento de eliminar oficio inexistente con ID: {}", id);
                throw new IllegalArgumentException("El oficio no existe");
            }

            oficioRepository.eliminar(id);
            log.info("Oficio eliminado: id={}", id);

            // Limpiar todos los caches relacionados
            cacheService.evictOficioPorNombre(oficioOpt.get().getNombre());
            cacheService.evictOficioPorId(id);

        } catch (IllegalArgumentException e) {
            log.error("Error de validación al eliminar oficio ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al eliminar oficio ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error al eliminar oficio", e);
        }
    }
}