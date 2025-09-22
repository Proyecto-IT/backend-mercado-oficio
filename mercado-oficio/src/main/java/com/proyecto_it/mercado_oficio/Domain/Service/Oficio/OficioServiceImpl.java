package com.proyecto_it.mercado_oficio.Domain.Service.Oficio;

import com.proyecto_it.mercado_oficio.Domain.Model.Oficio;
import com.proyecto_it.mercado_oficio.Domain.Repository.OficioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OficioServiceImpl implements OficioService {

    private final OficioRepository oficioRepository;

    @Override
    public Oficio crearOficio(Oficio oficio) {
        return oficioRepository.guardar(oficio);
    }

    @Override
    public List<Oficio> listarTodos() {
        return oficioRepository.findAll();
    }

    @Override
    public List<Oficio> buscarPorNombre(String nombre) {
        return oficioRepository.buscarPorNombre(nombre);
    }

    @Override
    public Optional<Oficio> buscarPorId(Long id) {
        return oficioRepository.buscarPorId(id);
    }

    @Override
    public Oficio actualizarOficio(Oficio oficio) {
        return oficioRepository.actualizar(oficio);
    }

    @Override
    public void eliminarOficio(Long id) {
        oficioRepository.eliminar(id);
    }
}