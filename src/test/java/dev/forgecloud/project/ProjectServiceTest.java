package dev.forgecloud.project;

import dev.forgecloud.common.BadRequestException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectServiceTest {
    @Test
    void normalizesGithubRepository() {
        var result = ProjectService.parse("https://github.com/acme/payment-api.git/");
        assertEquals("acme/payment-api", result.fullName());
        assertEquals("https://github.com/acme/payment-api", result.url());
    }

    @Test
    void rejectsNonGithubAndNestedPaths() {
        assertThrows(BadRequestException.class, () -> ProjectService.parse("https://example.com/acme/api"));
        assertThrows(BadRequestException.class, () -> ProjectService.parse("https://github.com/acme/api/tree/main"));
        assertThrows(BadRequestException.class, () -> ProjectService.parse("http://github.com/acme/api"));
    }
}

