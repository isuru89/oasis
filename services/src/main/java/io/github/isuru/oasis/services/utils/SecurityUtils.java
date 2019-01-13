package io.github.isuru.oasis.services.utils;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.model.EventSourceToken;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Formatter;
import java.util.Random;

public class SecurityUtils {

    private static final KeyFactory KEY_FACTORY;

    private static final int RSA_KEY_SIZE = 2048;
    private static final String HMAC_ALGORITHM = "HmacSHA1";

    private static MessageDigest digest;

    static {
        try {
            KEY_FACTORY = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot load RSA algorithm in this JRE!", e);
        }

        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot load SHA-1 hash algorithm in this JRE!", e);
        }
    }

    public static Pair<PrivateKey, PublicKey> generateRSAKey(String tokenSourceName) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = new SecureRandom(tokenSourceName.getBytes());
        keyGen.initialize(RSA_KEY_SIZE, secureRandom);
        KeyPair keyPair = keyGen.generateKeyPair();
        return Pair.of(keyPair.getPrivate(), keyPair.getPublic());
    }

    public static PrivateKey convertToPrivateKey(byte[] data) throws InvalidKeySpecException {
        return KEY_FACTORY.generatePrivate(new PKCS8EncodedKeySpec(data));
    }

    public static void verifyIntegrity(EventSourceToken token, String hash, String body) throws ApiAuthException {
        try {
            String hmac = generateHMAC(body, token.getSecretPrivateKey());
            if (!hmac.equals(hash)) {
                throw new IOException("Integrity of the event has been compromised!");
            }
        } catch (NoSuchAlgorithmException | IOException | InvalidKeyException e) {
            throw new ApiAuthException("Unable to verify integrity of the event!", e);
        }
    }

    public static void verifyIntegrity(EventSourceToken token, String algo, String digest, byte[] body) throws ApiAuthException {
        try {
            String hmac = generateHMAC(algo, body, token.getSecretPrivateKey());
            if (!hmac.equals(digest)) {
                throw new IOException("Integrity of the event has been compromised!");
            }
        } catch (NoSuchAlgorithmException | IOException | InvalidKeyException e) {
            throw new ApiAuthException("Unable to verify integrity of the event!", e);
        }
    }

    private static String generateHMAC(String algo, byte[] data, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key.getEncoded(), algo);
        Mac mac = Mac.getInstance(algo);
        mac.init(signingKey);
        return toHexString(mac.doFinal(data));
    }

    public static String generateHMAC(String data, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key.getEncoded(), HMAC_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(signingKey);
        return toHexString(mac.doFinal(data.getBytes()));
    }

    public static Pair<String, Integer> issueSourceToken(EventSourceToken token) {
        long seed = System.currentTimeMillis();
        int nonce = SecurityUtils.generateNonce(seed);
        String text = String.format("%s-%d-%d", token.getSourceName(), seed, nonce);
        byte[] digestBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        return Pair.of(toHexString(digestBytes), nonce);
    }

    private static int generateNonce(long seed) {
        return new Random(seed).nextInt(999999) + 1;
    }

    private static String toHexString(byte[] data) {
        Formatter result = new Formatter();
        for (byte b : data) {
            result.format("%02x", b);
        }
        return result.toString();
    }



}
