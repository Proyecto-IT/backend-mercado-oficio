package com.proyecto_it.mercado_oficio.Domain.Service.Escrow;

import com.proyecto_it.mercado_oficio.Domain.Model.Hito;
import com.proyecto_it.mercado_oficio.Domain.Service.Escrow.Hito.HitoService;
import com.proyecto_it.mercado_oficio.Exception.ResourceNotFoundException;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.TrustlessWork.TrustlessWorkCreateResponse;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.TrustlessWork.TrustlessWorkEscrowData;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.TrustlessWork.TrustlessWorkResponse;
import com.proyecto_it.mercado_oficio.Infraestructure.DTO.TrustlessWork.TrustlessWorkSendResponse;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.Hito.HitoEntity;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.TrustlessWork.TrustlessEscrowRecord;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Entity.TrustlessWork.UsuarioWallet;
import com.proyecto_it.mercado_oficio.Infraestructure.Persistence.Repository.TrustlessWork.TrustlessWorkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EscrowService {

    @Value("${trustless.work.api.key}")
    private String apiKey;

    @Value("${trustless.work.api.url:https://dev.api.trustlesswork.com}")
    private String apiUrl;

    @Value("${trustless.work.platform.address}")
    private String platformAddress;

    @Value("${trustless.work.platform.fee:0.5}")
    private Double platformFee;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TrustlessWorkRepository trustlessRepository;

    @Autowired
    private UsuarioWalletService usuarioWalletService;

    @Autowired
    private StellarSigningService stellarSigningService;

    @Autowired
    private HitoService hitoService;

    @Transactional
    public TrustlessEscrowRecord crearEscrowCompleto(Hito hito, Integer idCliente, Integer idPrestador) {
        try {
            // Obtener o crear wallets Stellar para cliente y prestador
            UsuarioWallet walletCliente = usuarioWalletService.obtenerOCrearWallet(idCliente);
            UsuarioWallet walletPrestador = usuarioWalletService.obtenerOCrearWallet(idPrestador);

            // Crear escrow en Trustless Work (retorna XDR sin firmar)
            TrustlessWorkCreateResponse createResponse = crearEscrowEnTrustlessWork(
                    hito,
                    walletCliente.getWalletAddress(),
                    walletPrestador.getWalletAddress()
            );

            // Firmar el XDR con la clave privada del cliente
            String signedXdr = stellarSigningService.firmarXdr(
                    createResponse.getUnsignedTransaction(),
                    walletCliente.getPrivateKeyEncriptada()
            );

            // Enviar a Stellar
            TrustlessWorkSendResponse sendResponse = enviarTransaccionFirmada(signedXdr);

            // Guardar registro del escrow
            TrustlessEscrowRecord record = new TrustlessEscrowRecord();
            record.setHitoId(hito.getId());
            record.setContractId(sendResponse.getContractId());
            record.setTransactionHash(sendResponse.getTxHash());
            record.setUnsignedXdr(createResponse.getUnsignedTransaction());
            record.setSignedXdr(signedXdr);
            record.setEstado("CREADO");
            record.setWalletCliente(walletCliente.getWalletAddress());
            record.setWalletPrestador(walletPrestador.getWalletAddress());

            TrustlessEscrowRecord guardado = trustlessRepository.save(record);

            log.info("Escrow creado, firmado y enviado a Stellar - ContractId: {} - Hito: {}",
                    sendResponse.getContractId(), hito.getId());

            return guardado;

        } catch (Exception e) {
            log.error("Error al crear escrow completo para hito {}", hito.getId(), e);
            throw new RuntimeException("No se pudo crear el escrow: " + e.getMessage());
        }
    }

    @Transactional
    public TrustlessEscrowRecord financiarEscrowCompleto(Integer hitoId, Integer idCliente) {
        try {
            TrustlessEscrowRecord record = trustlessRepository.findByHitoId(hitoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Escrow no encontrado para el hito"));

            UsuarioWallet wallet = usuarioWalletService.obtenerOCrearWallet(idCliente);

            // Obtener el monto del hito
            BigDecimal monto = obtenerMontoHito(hitoId);

            // Crear payload para financiar
            Map<String, Object> payload = new HashMap<>();
            payload.put("contractId", record.getContractId());
            payload.put("signer", wallet.getWalletAddress());
            payload.put("amount", monto.toPlainString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<TrustlessWorkResponse> response = restTemplate.postForEntity(
                    apiUrl + "/escrow/multi-release/fund-escrow",
                    request,
                    TrustlessWorkResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                // Firmar XDR de financiación
                String unsignedXdr = response.getBody().getUnsignedTransaction();
                String signedXdr = stellarSigningService.firmarXdr(
                        unsignedXdr,
                        wallet.getPrivateKeyEncriptada()
                );

                // Enviar transacción firmada
                TrustlessWorkSendResponse sendResponse = enviarTransaccionFirmada(signedXdr);

                record.setEstado("FINANCIADO");
                record = trustlessRepository.save(record);

                log.info("Escrow financiado exitosamente - ContractId: {} - Monto: {}",
                        record.getContractId(), monto);

                return record;
            }
            throw new RuntimeException("No se pudo financiar el escrow");

        } catch (Exception e) {
            log.error("Error al financiar escrow para hito {}", hitoId, e);
            throw new RuntimeException("No se pudo financiar el escrow: " + e.getMessage());
        }
    }

    @Transactional
    public void completarMilestoneAutomatico(Integer hitoId, Integer idPrestador, String evidence) {
        try {
            TrustlessEscrowRecord record = trustlessRepository.findByHitoId(hitoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Escrow no encontrado"));

            UsuarioWallet wallet = usuarioWalletService.obtenerOCrearWallet(idPrestador);

            Map<String, Object> payload = new HashMap<>();
            payload.put("contractId", record.getContractId());
            payload.put("milestoneIndex", "0");
            payload.put("serviceProvider", wallet.getWalletAddress());
            payload.put("newStatus", "Completed");
            payload.put("newEvidence", evidence != null ? evidence : "");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<TrustlessWorkResponse> response = restTemplate.postForEntity(
                    apiUrl + "/escrow/multi-release/change-milestone-status",
                    request,
                    TrustlessWorkResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String signedXdr = stellarSigningService.firmarXdr(
                        response.getBody().getUnsignedTransaction(),
                        wallet.getPrivateKeyEncriptada()
                );
                enviarTransaccionFirmada(signedXdr);
                log.info("Milestone completado - Hito: {}", hitoId);
            }

        } catch (Exception e) {
            log.error("Error al completar milestone para hito {}", hitoId, e);
            throw new RuntimeException("No se pudo completar el milestone: " + e.getMessage());
        }
    }

    @Transactional
    public void aprobarMilestoneAutomatico(Integer hitoId, Integer idCliente) {
        try {
            TrustlessEscrowRecord record = trustlessRepository.findByHitoId(hitoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Escrow no encontrado"));

            UsuarioWallet wallet = usuarioWalletService.obtenerOCrearWallet(idCliente);

            Map<String, Object> payload = new HashMap<>();
            payload.put("contractId", record.getContractId());
            payload.put("milestoneIndex", "0");
            payload.put("approver", wallet.getWalletAddress());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<TrustlessWorkResponse> response = restTemplate.postForEntity(
                    apiUrl + "/escrow/multi-release/approve-milestone",
                    request,
                    TrustlessWorkResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String signedXdr = stellarSigningService.firmarXdr(
                        response.getBody().getUnsignedTransaction(),
                        wallet.getPrivateKeyEncriptada()
                );
                enviarTransaccionFirmada(signedXdr);
                log.info("Milestone aprobado - Hito: {}", hitoId);
            }

        } catch (Exception e) {
            log.error("Error al aprobar milestone para hito {}", hitoId, e);
            throw new RuntimeException("No se pudo aprobar el milestone: " + e.getMessage());
        }
    }

    @Transactional
    public void liberarFondosAutomatico(Integer hitoId, Integer idCliente) {
        try {
            TrustlessEscrowRecord record = trustlessRepository.findByHitoId(hitoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Escrow no encontrado"));

            UsuarioWallet wallet = usuarioWalletService.obtenerOCrearWallet(idCliente);

            Map<String, Object> payload = new HashMap<>();
            payload.put("contractId", record.getContractId());
            payload.put("releaseSigner", wallet.getWalletAddress());
            payload.put("milestoneIndex", "0");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<TrustlessWorkResponse> response = restTemplate.postForEntity(
                    apiUrl + "/escrow/multi-release/release-milestone-funds",
                    request,
                    TrustlessWorkResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String signedXdr = stellarSigningService.firmarXdr(
                        response.getBody().getUnsignedTransaction(),
                        wallet.getPrivateKeyEncriptada()
                );
                enviarTransaccionFirmada(signedXdr);

                record.setEstado("LIBERADO");
                trustlessRepository.save(record);

                log.info("Fondos liberados - Hito: {}", hitoId);
            }

        } catch (Exception e) {
            log.error("Error al liberar fondos para hito {}", hitoId, e);
            throw new RuntimeException("No se pudieron liberar los fondos: " + e.getMessage());
        }
    }

    @Transactional
    public void levantarDisputaAutomatica(Integer hitoId, Integer idUsuario) {
        try {
            TrustlessEscrowRecord record = trustlessRepository.findByHitoId(hitoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Escrow no encontrado"));

            UsuarioWallet wallet = usuarioWalletService.obtenerOCrearWallet(idUsuario);

            Map<String, Object> payload = new HashMap<>();
            payload.put("contractId", record.getContractId());
            payload.put("milestoneIndex", "0");
            payload.put("signer", wallet.getWalletAddress());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<TrustlessWorkResponse> response = restTemplate.postForEntity(
                    apiUrl + "/escrow/multi-release/dispute-milestone",
                    request,
                    TrustlessWorkResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String signedXdr = stellarSigningService.firmarXdr(
                        response.getBody().getUnsignedTransaction(),
                        wallet.getPrivateKeyEncriptada()
                );
                enviarTransaccionFirmada(signedXdr);

                record.setEstado("EN_DISPUTA");
                trustlessRepository.save(record);

                log.info("Disputa levantada - Hito: {}", hitoId);
            }

        } catch (Exception e) {
            log.error("Error al levantar disputa para hito {}", hitoId, e);
            throw new RuntimeException("No se pudo levantar la disputa: " + e.getMessage());
        }
    }

    public TrustlessWorkEscrowData obtenerEstadoEscrow(String contractId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", apiKey);

            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<TrustlessWorkEscrowData> response = restTemplate.exchange(
                    apiUrl + "/helper/get-escrow-by-contract-ids?contractIds=" + contractId,
                    HttpMethod.GET,
                    request,
                    TrustlessWorkEscrowData.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            return null;

        } catch (Exception e) {
            log.error("Error al obtener estado del escrow", e);
            return null;
        }
    }

    private TrustlessWorkCreateResponse crearEscrowEnTrustlessWork(
            Hito hito, String walletCliente, String walletPrestador) {

        List<Map<String, Object>> milestones = new ArrayList<>();
        Map<String, Object> milestone = new HashMap<>();
        milestone.put("amount", hito.getMonto().toPlainString());
        milestone.put("status", "pending");
        milestone.put("approved", false);
        milestones.add(milestone);

        Map<String, String> roles = new HashMap<>();
        roles.put("approver", walletCliente);
        roles.put("serviceProvider", walletPrestador);
        roles.put("platformAddress", platformAddress);
        roles.put("releaseSigner", walletCliente);
        roles.put("disputeResolver", platformAddress);
        roles.put("receiver", walletPrestador);

        List<Map<String, Object>> trustlines = new ArrayList<>();
        Map<String, Object> trustline = new HashMap<>();
        trustline.put("address", "GBUQWP3BOUZX34ULNQG23RQ6F4BFSRJZ4VS5RM42ZSXV7DWNEKMZTUW");
        trustlines.add(trustline);

        Map<String, Object> payload = new HashMap<>();
        payload.put("signer", walletCliente);
        payload.put("engagementId", "ENG-" + hito.getPresupuestoId() + "-" + hito.getId());
        payload.put("roles", roles);
        payload.put("milestones", milestones);
        payload.put("platformFee", platformFee);
        payload.put("trustline", trustlines);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<TrustlessWorkCreateResponse> response = restTemplate.postForEntity(
                apiUrl + "/deployer/multi-release",
                request,
                TrustlessWorkCreateResponse.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }
        throw new RuntimeException("No se pudo crear escrow en Trustless Work");
    }

    private TrustlessWorkSendResponse enviarTransaccionFirmada(String signedXdr) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("signedXdr", signedXdr);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<TrustlessWorkSendResponse> response = restTemplate.postForEntity(
                apiUrl + "/helper/send-transaction",
                request,
                TrustlessWorkSendResponse.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }
        throw new RuntimeException("No se pudo enviar la transacción a Stellar");
    }

    public BigDecimal obtenerMontoHito(Integer hitoId) {
        Hito hito = hitoService.obtenerPorId(hitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Hito no encontrado con id " + hitoId));

        if (hito.getMonto() == null) {
            log.warn("Hito {} no tiene monto definido, devolviendo 0", hitoId);
            return BigDecimal.ZERO;
        }
        return hito.getMonto();
    }


}