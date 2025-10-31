package com.proyecto_it.mercado_oficio.Domain.Repository;

import com.proyecto_it.mercado_oficio.Domain.Model.Multimedia;

import java.util.List;
import java.util.Optional;

public interface MultimediaRepository {
    Multimedia guardar(Multimedia multimedia);
    Optional<Multimedia> findById(Integer id);
    List<Multimedia> findByIds(List<Integer> ids);
    void deleteById(Integer id);
}