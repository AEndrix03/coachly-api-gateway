package it.aredegalli.coachly.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Il test parte sul server reale, non su un mock: la garanzia che conta e' che
 * l'endpoint risponda **senza token**, e quella dipende dalla catena di
 * sicurezza, non dal controller.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "server.ssl.enabled=false",
    "coachly.app.requirements.min-supported-version=1.4.0",
    "coachly.app.requirements.recommended-version=1.6.2",
    "coachly.app.requirements.message=Aggiorna per continuare a sincronizzare."
})
class AppRequirementsControllerTest {

    @LocalServerPort
    private int port;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        this.client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + port)
            .build();
    }

    @Test
    void requirementsAreServedWithoutAuthentication() {
        client.get()
            .uri("/api/app/requirements")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.minSupportedVersion").isEqualTo("1.4.0")
            .jsonPath("$.recommendedVersion").isEqualTo("1.6.2")
            .jsonPath("$.message").isEqualTo("Aggiorna per continuare a sincronizzare.");
    }

    @Test
    void apiRoutesStillRequireAuthentication() {
        client.get()
            .uri("/api/workouts/user")
            .exchange()
            .expectStatus().isUnauthorized();
    }
}
