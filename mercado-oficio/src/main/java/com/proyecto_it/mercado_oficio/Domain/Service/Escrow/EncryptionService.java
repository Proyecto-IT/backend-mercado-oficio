package com.proyecto_it.mercado_oficio.Domain.Service.Escrow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class EncryptionService {

    @Value("${encryption.secret.key}")
    private String secretKey;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    public String encriptar(String valor) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(secretKey);
            SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[cipher.getBlockSize()];
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] encryptedBytes = cipher.doFinal(valor.getBytes(StandardCharsets.UTF_8));

            // Combinar IV + datos encriptados
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            log.error("Error al encriptar", e);
            throw new RuntimeException("Error en encriptación: " + e.getMessage());
        }
    }

    public String desencriptar(String valor) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(secretKey);
            SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);

            byte[] combined = Base64.getDecoder().decode(valor);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            int blockSize = cipher.getBlockSize();

            IvParameterSpec ivSpec = new IvParameterSpec(combined, 0, blockSize);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

            byte[] decryptedBytes = cipher.doFinal(combined, blockSize, combined.length - blockSize);

            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Error al desencriptar", e);
            throw new RuntimeException("Error en desencriptación: " + e.getMessage());
        }
    }

    public static String generarClaveEncriptacion() {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[32]; // 256 bits para AES-256
        random.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }
}
