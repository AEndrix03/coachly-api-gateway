package it.aredegalli.coachly.app;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Versione minima e consigliata del client.
 *
 * <p>Sta sul gateway e non su un servizio di dominio per due ragioni. E'
 * l'unico endpoint che deve rispondere anche quando i servizi a valle sono
 * giu', perche' e' quello che spiega all'utente perche' la app non va. Ed e'
 * **pubblico**: un client troppo vecchio per parlare col backend puo' esserlo
 * anche per autenticarsi, e la schermata di aggiornamento deve comparire lo
 * stesso. Per questo il path e' sotto {@code /public/**}, gia' {@code permitAll}
 * in {@code SecurityConfig}.
 */
@RestController
@RequestMapping("/public/app")
public class AppRequirementsController {

    private final AppRequirementsProperties properties;

    public AppRequirementsController(AppRequirementsProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/requirements")
    public Mono<ResponseEntity<AppRequirementsResponse>> requirements() {
        AppRequirementsResponse body = new AppRequirementsResponse(
            properties.getMinSupportedVersion(),
            properties.getRecommendedVersion(),
            properties.getMessage()
        );

        // Cache breve: la soglia cambia a ogni rilascio, non a ogni richiesta,
        // ma un client bloccato deve poter vedere presto che e' stata abbassata.
        return Mono.just(ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
            .body(body));
    }
}
