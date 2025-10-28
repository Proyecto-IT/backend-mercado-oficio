package com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.TrustlessWork;

import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.TrustlessWork.UsuarioWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioWalletRepository extends JpaRepository<UsuarioWallet, Long> {

    Optional<UsuarioWallet> findByUsuarioId(Integer usuarioId);

    Optional<UsuarioWallet> findByWalletAddress(String walletAddress);

    List<UsuarioWallet> findByEstado(String estado);

    @Query("SELECT w FROM UsuarioWallet w WHERE w.estado = 'ACTIVA'")
    List<UsuarioWallet> findAllActivas();
}
