package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Servicio.Portafolio;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.PortafolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JpaPortafolioRepository extends JpaRepository<PortafolioEntity, Integer> {
    List<PortafolioEntity> findByServicioId(Integer servicioId);

    void deleteAllByServicioId(Integer servicioId);
}

