package com.barbeariasorrisobarber.agendamento.api.mobile.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MobileTokenService {

    private static final long TOKEN_TTL_HOURS = 168L;
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final SecretKeySpec secretKeySpec;

    public MobileTokenService(@Value("${mobile.api.token-secret:agendamento-mobile-secret}") String secret) {
        this.secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }

    public TokenData gerarToken(String username) {
        Instant expiresAt = Instant.now().plus(Duration.ofHours(TOKEN_TTL_HOURS));
        String payload = encode(username) + ":" + expiresAt.toEpochMilli();
        String signature = sign(payload);
        String token = encode(payload) + "." + signature;
        return new TokenData(token, expiresAt);
    }

    public Optional<TokenPayload> validarToken(String token) {
        if (token == null || token.isBlank() || !token.contains(".")) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.", 2);
        if (parts.length != 2) {
            return Optional.empty();
        }

        String payload;
        try {
            payload = decode(parts[0]);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }

        if (!MessageDigest.isEqual(sign(payload).getBytes(StandardCharsets.UTF_8), parts[1].getBytes(StandardCharsets.UTF_8))) {
            return Optional.empty();
        }

        String[] payloadParts = payload.split(":", 2);
        if (payloadParts.length != 2) {
            return Optional.empty();
        }

        try {
            String username = decode(payloadParts[0]);
            Instant expiresAt = Instant.ofEpochMilli(Long.parseLong(payloadParts[1]));
            if (Instant.now().isAfter(expiresAt)) {
                return Optional.empty();
            }
            return Optional.of(new TokenPayload(username, expiresAt));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return ENCODER.encodeToString(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao assinar token mobile.", ex);
        }
    }

    private String encode(String value) {
        return ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(DECODER.decode(value), StandardCharsets.UTF_8);
    }

    public record TokenData(String token, Instant expiresAt) {
    }

    public record TokenPayload(String username, Instant expiresAt) {
    }
}
