package it.aredegalli.coachly.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "server.ssl.enabled=false",
    "coachly.app.requirements.min-supported-version=1.4.0",
    "coachly.app.requirements.recommended-version=1.6.2",
    "coachly.app.requirements.message=Aggiorna per continuare a sincronizzare."
})
class AppRequirementsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void requirementsAreServedWithoutAuthentication() {
        webTestClient.get()
            .uri("/public/app/requirements")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.minSupportedVersion").isEqualTo("1.4.0")
            .jsonPath("$.recommendedVersion").isEqualTo("1.6.2")
            .jsonPath("$.message").isEqualTo("Aggiorna per continuare a sincronizzare.");
    }
}
