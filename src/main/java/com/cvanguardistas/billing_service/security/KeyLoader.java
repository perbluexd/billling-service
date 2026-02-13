package com.cvanguardistas.billing_service.security;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.*;
import java.util.Base64;

@Component
public class KeyLoader {

    public PrivateKey loadPrivateKey(String pem, String path) {
        String content = (pem != null && !pem.isBlank())
                ? pem
                : readFileOrNull(path);
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("JWT private key not configured (PEM or path).");
        }
        return parsePrivateKeyPKCS8(stripPem(content));
    }

    public PublicKey loadPublicKey(String pem, String path) {
        String content = (pem != null && !pem.isBlank())
                ? pem
                : readFileOrNull(path);
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("JWT public key not configured (PEM or path).");
        }
        return parsePublicKeyX509(stripPem(content));
    }

    private static String readFileOrNull(String path) {
        try {
            if (path == null || path.isBlank()) return null;
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            return null;
        }
    }

    private static byte[] stripPem(String pem) {
        String s = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(s);
    }

    private static PrivateKey parsePrivateKeyPKCS8(byte[] der) {
        try {
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid RSA private key (expect PKCS#8). " + e.getMessage(), e);
        }
    }

    private static PublicKey parsePublicKeyX509(byte[] der) {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid RSA public key (expect X.509). " + e.getMessage(), e);
        }
    }
}
