package dev.forgecloud.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.forgecloud.common.BadRequestException;
import dev.forgecloud.pipeline.PipelineResponse;
import dev.forgecloud.pipeline.PipelineService;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/github")
public class GitHubWebhookController {
    private final WebhookSignatureVerifier verifier;
    private final ObjectMapper mapper;
    private final PipelineService pipelines;

    public GitHubWebhookController(WebhookSignatureVerifier verifier, ObjectMapper mapper, PipelineService pipelines) {
        this.verifier = verifier; this.mapper = mapper; this.pipelines = pipelines;
    }

    @PostMapping
    public ResponseEntity<?> receive(
        @RequestHeader(value = "X-GitHub-Event", required = false) String event,
        @RequestHeader(value = "X-GitHub-Delivery", required = false) String deliveryId,
        @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
        @RequestBody byte[] body
    ) {
        verifier.verify(body, signature);
        if (deliveryId == null || deliveryId.isBlank()) throw new BadRequestException("Missing delivery ID");
        if (!"push".equals(event)) return ResponseEntity.accepted().body(Map.of("message", "Event ignored"));
        try {
            JsonNode payload = mapper.readTree(body);
            String repository = required(payload, "/repository/full_name");
            String ref = required(payload, "/ref");
            String sha = required(payload, "/after");
            if (!ref.startsWith("refs/heads/")) return ResponseEntity.accepted().body(Map.of("message", "Non-branch push ignored"));
            PipelineResponse pipeline = pipelines.githubPush(repository, ref.substring("refs/heads/".length()), sha, deliveryId);
            return ResponseEntity.accepted().body(Map.of("pipeline", pipeline));
        } catch (IOException exception) {
            throw new BadRequestException("Webhook payload is not valid JSON");
        }
    }

    private String required(JsonNode payload, String pointer) {
        String value = payload.at(pointer).asText("");
        if (value.isBlank()) throw new BadRequestException("Webhook payload is missing " + pointer);
        return value;
    }
}

