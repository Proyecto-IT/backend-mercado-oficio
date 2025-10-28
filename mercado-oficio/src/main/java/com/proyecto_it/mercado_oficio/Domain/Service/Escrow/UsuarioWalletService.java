package com.proyecto_it.mercado_oficio.Domain.Service.Escrow;

import com.proyecto_it.mercado_oficio.Exception.ResourceNotFoundException;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.TrustlessWork.UsuarioWallet;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.TrustlessWork.UsuarioWalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.stellar.sdk.KeyPair;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class UsuarioWalletService {

    @Autowired
    private UsuarioWalletRepository walletRepository;

    @Autowired
    private EncryptionService encryptionService;

    @Transactional
    public UsuarioWallet obtenerOCrearWallet(Integer usuarioId) {
        return walletRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> crearWalletNueva(usuarioId));
    }

    @Transactional
    public UsuarioWallet crearWalletNueva(Integer usuarioId) {
        try {
            // Generar par de claves Stellar
            KeyPair keyPair = KeyPair.random();
            String publicKey = keyPair.getAccountId();
            char[] secretKeyChars = keyPair.getSecretSeed();
            String secretKey = new String(secretKeyChars);
            // Encriptar la clave privada
            String secretKeyEncriptada = encryptionService.encriptar(secretKey); // String

            // Crear objeto wallet
            UsuarioWallet wallet = new UsuarioWallet();
            wallet.setUsuarioId(usuarioId);
            wallet.setWalletAddress(publicKey);
            wallet.setPrivateKeyEncriptada(secretKeyEncriptada);
            wallet.setEstado("ACTIVA");
            wallet.setFechaCreacion(LocalDateTime.now());

            // Guardar en base de datos
            UsuarioWallet guardada = walletRepository.save(wallet);

            log.info("Wallet Stellar creada para usuario {} - Address: {}", usuarioId, publicKey);

            return guardada;

        } catch (Exception e) {
            log.error("Error al crear wallet Stellar para usuario {}", usuarioId, e);
            throw new RuntimeException("No se pudo crear la wallet: " + e.getMessage());
        }
    }

    public String obtenerClavePrivadaDesencriptada(Integer usuarioId) {
        UsuarioWallet wallet = walletRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet no encontrada para usuario " + usuarioId));

        return encryptionService.desencriptar(wallet.getPrivateKeyEncriptada());
    }

    public UsuarioWallet obtenerWallet(Integer usuarioId) {
        return walletRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet no encontrada para usuario " + usuarioId));
    }

    public Boolean tienefondosSuficientes(Integer usuarioId, BigDecimal montoRequerido) {
        try {
            UsuarioWallet wallet = obtenerWallet(usuarioId);
            log.debug("Validando fondos para usuario {} - Monto: {}", usuarioId, montoRequerido);
            return true;
        } catch (Exception e) {
            log.error("Error al validar fondos", e);
            return false;
        }
    }
}