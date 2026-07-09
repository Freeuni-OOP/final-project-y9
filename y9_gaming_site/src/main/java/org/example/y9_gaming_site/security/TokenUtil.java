package org.example.y9_gaming_site.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class TokenUtil {

    private static final PrivateKey privateKey;
    public static final PublicKey publicKey;
    private static final String[] PRIVATE_KEY_PATHS = {"../keys/jwt_private.key", "keys/jwt_private.key"};
    private static final String[] PUBLIC_KEY_PATHS = {"../keys/jwt_public.key", "keys/jwt_public.key"};

    static {
        KeyPair loaded = loadPersistedKeyPair();
        if (loaded != null) {
            privateKey = loaded.getPrivate();
            publicKey = loaded.getPublic();
        } else {
            System.err.println("WARNING: could not find keys/jwt_private.key + keys/jwt_public.key");
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
    }

    private static KeyPair loadPersistedKeyPair() {
        try {
            Path privatePath = firstExisting(PRIVATE_KEY_PATHS);
            Path publicPath = firstExisting(PUBLIC_KEY_PATHS);
            if (privatePath == null || publicPath == null) {
                return null;
            }

            byte[] privateBytes = Files.readAllBytes(privatePath);
            byte[] publicBytes = Files.readAllBytes(publicPath);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey priv = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
            PublicKey pub = keyFactory.generatePublic(new X509EncodedKeySpec(publicBytes));
            return new KeyPair(pub, priv);
        } catch (Exception e) {
            System.err.println("Failed to load persisted JWT keys falling back to a random pair : " + e.getMessage());
            return null;
        }
    }

    private static Path firstExisting(String[] candidates) {
        for (String candidate : candidates) {
            Path path = Path.of(candidate);
            if (Files.exists(path)) {
                return path;
            }
        }
        return null;
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

    public static String getUsernameFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) return null;

            String payload = new String(Base64.getDecoder().decode(parts[0]));
            String[] data = payload.split(":");
            return data[0];
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