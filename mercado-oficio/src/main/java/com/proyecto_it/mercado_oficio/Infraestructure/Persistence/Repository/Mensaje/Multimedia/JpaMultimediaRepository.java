package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Mensaje.Multimedia;

import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Mensaje.MultimediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaMultimediaRepository extends JpaRepository<MultimediaEntity, Integer> {
    List<MultimediaEntity> findByIds(List<Integer> ids);
}