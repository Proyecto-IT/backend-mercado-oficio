package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.TrustlessWork;

import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.TrustlessWork.TrustlessEscrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrustlessWorkRepository extends JpaRepository<TrustlessEscrowRecord, Long> {

    Optional<TrustlessEscrowRecord> findByContractId(String contractId);

    Optional<TrustlessEscrowRecord> findByHitoId(Integer hitoId);

    List<TrustlessEscrowRecord> findByEstado(String estado);

    @Query("SELECT t FROM TrustlessEscrowRecord t WHERE t.estado IN ('CREADO', 'FINANCIADO', 'COMPLETADO')")
    List<TrustlessEscrowRecord> findAllActivos();

    @Query("SELECT t FROM TrustlessEscrowRecord t WHERE t.ultimaActualizacion < :fecha AND t.estado IN ('CREADO', 'FINANCIADO')")
    List<TrustlessEscrowRecord> findSinSincronizar(LocalDateTime fecha);
}

