package org.example.y9_gaming_site.security;

import java.security.*;
import java.util.Base64;

public class TokenUtil {

    private static final PrivateKey privateKey;
    public static final PublicKey publicKey;

    static {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateToken(String username) {
        try {
            long expiry = System.currentTimeMillis() + 86400000;
            String payload = username + ":" + expiry;

            Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(privateKey);
            privateSignature.update(payload.getBytes());
            byte[] signature = privateSignature.sign();

            String base64Payload = Base64.getEncoder().encodeToString(payload.getBytes());
            String base64Signature = Base64.getEncoder().encodeToString(signature);

            return base64Payload + "." + base64Signature;
        } catch (Exception e) {
            return null;
        }
    }

    public static String validateTokenAndGetUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) return null;

            String payload = new String(Base64.getDecoder().decode(parts[0]));
            byte[] signature = Base64.getDecoder().decode(parts[1]);

            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(publicKey);
            publicSignature.update(payload.getBytes());

            if (!publicSignature.verify(signature)) return null;

            String[] data = payload.split(":");
            String username = data[0];
            long expiry = Long.parseLong(data[1]);

            if (System.currentTimeMillis() > expiry) return null;

            return username;
        } catch (Exception e) {
            return null;
        }
    }
}