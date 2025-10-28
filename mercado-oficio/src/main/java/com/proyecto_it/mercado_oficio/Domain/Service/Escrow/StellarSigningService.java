package com.proyecto_it.mercado_oficio.Domain.Service.Escrow;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.stellar.sdk.*;



@Service
@Slf4j
public class StellarSigningService {

    @Autowired
    private EncryptionService encryptionService;

    public String firmarXdr(String unsignedXdr, String privateKeyEncriptada) {
        try {
            // Desencriptar la clave privada
            String privateKey = encryptionService.desencriptar(privateKeyEncriptada);

            // Crear KeyPair desde la clave privada
            KeyPair keyPair = KeyPair.fromSecretSeed(privateKey);

            // Decodificar el XDR a Transaction
            byte[] xdrBytes = Base64.getDecoder().decode(unsignedXdr);
            Transaction transaction = (Transaction) Transaction.fromEnvelopeXdr(
                    Base64.getEncoder().encodeToString(xdrBytes),
                    Network.TESTNET // o Network.PUBLIC si es mainnet
            );

            // Firmar la transacción
            transaction.sign(keyPair);

            // Obtener el XDR firmado
            String signedXdr = transaction.toEnvelopeXdrBase64();

            log.debug("XDR firmado exitosamente para wallet {}", keyPair.getAccountId());

            return signedXdr;

        } catch (Exception e) {
            log.error("Error al firmar XDR", e);
            throw new RuntimeException("No se pudo firmar la transacción: " + e.getMessage());
        }
    }

    public List<String> firmarMultiplesXdr(List<String> unsignedXdrs, String privateKeyEncriptada) {
        return unsignedXdrs.stream()
                .map(xdr -> firmarXdr(xdr, privateKeyEncriptada))
                .collect(Collectors.toList());
    }

    public Boolean validarXdr(String xdr) {
        try {
            // Decodificar el XDR a Transaction
            Transaction.fromEnvelopeXdr(xdr, Network.TESTNET); // o Network.PUBLIC
            return true;
        } catch (Exception e) {
            log.warn("XDR inválido: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, Object> obtenerInfoXdr(String xdr) {
        try {
            AbstractTransaction abstractTx = AbstractTransaction.fromEnvelopeXdr(xdr, Network.TESTNET);
            Map<String, Object> info = new HashMap<>();

            if (abstractTx instanceof Transaction tx) {
                info.put("sourceAccount", tx.getSourceAccount()); // ya es String
                info.put("fee", tx.getFee());
                info.put("operationCount", tx.getOperations().length);
                info.put("memo", tx.getMemo() != null ? tx.getMemo().toString() : null);
            } else if (abstractTx instanceof FeeBumpTransaction feeBumpTx) {
                info.put("sourceAccount", feeBumpTx.getFeeSource()); // ya es String
                info.put("fee", feeBumpTx.getFee());
                info.put("operationCount", feeBumpTx.getInnerTransaction().getOperations().length);
                info.put("memo", feeBumpTx.getInnerTransaction().getMemo() != null
                        ? feeBumpTx.getInnerTransaction().getMemo().toString() : null);
            } else {
                throw new RuntimeException("Tipo de transacción no soportado");
            }

            return info;

        } catch (Exception e) {
            log.error("Error al obtener info del XDR", e);
            throw new RuntimeException("No se pudo obtener información del XDR: " + e.getMessage());
        }
    }

}