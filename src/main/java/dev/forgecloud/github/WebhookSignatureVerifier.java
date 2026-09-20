package dev.forgecloud.github;

import dev.forgecloud.common.BadRequestException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebhookSignatureVerifier {
    private final byte[] secret;

    public WebhookSignatureVerifier(@Value("${forgecloud.github.webhook-secret}") String secret) {
        if (secret == null || secret.isBlank()) throw new IllegalStateException("GitHub webhook secret is required");
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public void verify(byte[] payload, String supplied) {
        if (supplied == null || !supplied.startsWith("sha256=")) {
            throw new BadRequestException("Missing webhook signature");
        }
        byte[] expected = ("sha256=" + hmacHex(payload)).getBytes(StandardCharsets.US_ASCII);
        byte[] actual = supplied.getBytes(StandardCharsets.US_ASCII);
        if (!MessageDigest.isEqual(expected, actual)) throw new BadRequestException("Invalid webhook signature");
    }

    private String hmacHex(byte[] payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return java.util.HexFormat.of().formatHex(mac.doFinal(payload));
        } catch (NoSuchAlgorithmException | java.security.InvalidKeyException exception) {
            throw new IllegalStateException("HMAC-SHA256 is unavailable", exception);
        }
    }
}

