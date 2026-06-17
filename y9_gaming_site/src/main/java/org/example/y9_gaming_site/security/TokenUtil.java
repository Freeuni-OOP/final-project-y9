package org.example.y9_gaming_site.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class TokenUtil {
    private static final byte[] SECRET_KEY = new byte[32];
    static {
        new SecureRandom().nextBytes(SECRET_KEY);
    }
    private static final long EXPIRATION_TIME = 86400000;



    public static String generateToken(String username) {
        try {
            long expiry = System.currentTimeMillis() + EXPIRATION_TIME;
            String payload = username + ":" + expiry;
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY, "HmacSHA256");
            sha256Hmac.init(secretKeySpec);
            byte[] signatureBytes = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
            return encodedPayload + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Error creating token", e);
        }
    }

    public static String validateTokenAndGetUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) return null; // Invalid token format
            String encodedPayload = parts[0];
            String providedSignature = parts[1];
            String payload = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
            String[] payloadParts = payload.split(":");
            if (payloadParts.length != 2) return null;
            String username = payloadParts[0];
            long expiry = Long.parseLong(payloadParts[1]);
            if (System.currentTimeMillis() > expiry) {
                return null;
            }
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY, "HmacSHA256");
            sha256Hmac.init(secretKeySpec);
            byte[] computedSignatureBytes = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(computedSignatureBytes);
            if (MessageDigest.isEqual(computedSignature.getBytes(), providedSignature.getBytes())) {
                return username;
            }

        } catch (Exception e) {
            return null;
        }
        return null;
    }
}