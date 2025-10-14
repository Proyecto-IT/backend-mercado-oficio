package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.Servicio.Portafolio;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Servicio.PortafolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

// JPA Repository
@Repository
public interface JpaPortafolioRepository extends JpaRepository<PortafolioEntity, Integer> {

    @Query("SELECT p FROM PortafolioEntity p " +
            "LEFT JOIN FETCH p.servicio " +
            "WHERE p.servicio.id = :servicioId")
    List<PortafolioEntity> findByServicioId(@Param("servicioId") Integer servicioId);
}


