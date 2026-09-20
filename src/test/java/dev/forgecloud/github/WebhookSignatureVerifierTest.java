package dev.forgecloud.github;

import dev.forgecloud.common.BadRequestException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WebhookSignatureVerifierTest {
    @Test
    void verifiesKnownSignature() {
        var verifier = new WebhookSignatureVerifier("It's a Secret to Everybody");
        byte[] payload = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        assertDoesNotThrow(() -> verifier.verify(payload,
            "sha256=757107ea0eb2509fc211221cce984b8a37570b6d7586c22c46f4379c8b043e17"));
        assertThrows(BadRequestException.class, () -> verifier.verify(payload, "sha256=wrong"));
    }
}

