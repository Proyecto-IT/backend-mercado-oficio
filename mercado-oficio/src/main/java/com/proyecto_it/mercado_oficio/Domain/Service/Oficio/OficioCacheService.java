package com.proyecto_it.mercado_oficio.Domain.Service.Oficio;

import java.util.List;
import java.util.Optional;

import com.proyecto_it.mercado_oficio.Domain.Model.Oficio;
import com.proyecto_it.mercado_oficio.Domain.Repository.OficioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;


@Service
@RequiredArgsConstructor
public class OficioCacheService {

    private final OficioRepository oficioRepository;

    // ==========================================
    // MÉTODOS DE LECTURA CON CACHE
    // ==========================================

    @Cacheable(value = "oficios", key = "#nombre.toLowerCase()")
    public List<Oficio> buscarPorNombreCached(String nombre) {
        return oficioRepository.buscarPorNombre(nombre);
    }

    @Cacheable(value = "oficiosPorId", key = "#id")
    public Optional<Oficio> buscarPorIdCached(Integer id) {
        return oficioRepository.buscarPorId(id);
    }

    @Cacheable(value = "todosLosOficios")
    public List<Oficio> listarTodosCached() {
        return oficioRepository.findAll();
    }

    // ==========================================
    // MÉTODOS DE ACTUALIZACIÓN DE CACHE
    // ==========================================

    /**
     * Actualiza el cache individual de un oficio (por nombre e ID)
     */
    @Caching(
            put = {
                    @CachePut(value = "oficios", key = "#oficio.nombre.toLowerCase()"),
                    @CachePut(value = "oficiosPorId", key = "#oficio.id")
            },
            evict = {
                    @CacheEvict(value = "todosLosOficios", allEntries = true)
            }
    )
    public Oficio actualizarOficioEnCache(Oficio oficio) {
        return oficio;
    }

    /**
     * Elimina oficio por nombre del cache
     */
    @Caching(evict = {
            @CacheEvict(value = "oficios", key = "#nombre.toLowerCase()"),
            @CacheEvict(value = "todosLosOficios", allEntries = true)
    })
    public void evictOficioPorNombre(String nombre) {
        // Cache se limpia automáticamente
    }

    /**
     * Elimina oficio por ID del cache
     */
    @Caching(evict = {
            @CacheEvict(value = "oficiosPorId", key = "#id"),
            @CacheEvict(value = "todosLosOficios", allEntries = true)
    })
    public void evictOficioPorId(Integer id) {
        // Cache se limpia automáticamente
    }

    /**
     * Invalida completamente el cache de todos los oficios
     * Se llamará después de crear, actualizar o eliminar
     */
    @CacheEvict(value = "todosLosOficios", allEntries = true)
    public void invalidarListaCompleta() {
        // El cache se limpiará automáticamente y se recargará en el próximo GET
    }
}