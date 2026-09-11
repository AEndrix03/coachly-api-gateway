package it.aredegalli.coachly.app;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Soglie di versione del client, da configurazione di rilascio.
 *
 * <p>Non sono un dato di dominio e non stanno a database: cambiano quando si
 * pubblica una release, quindi vivono nella configurazione e si sovrascrivono
 * con variabili d'ambiente senza ricostruire l'immagine.
 */
@ConfigurationProperties(prefix = "coachly.app.requirements")
public class AppRequirementsProperties {

    /**
     * Sotto questa versione il client mostra una schermata bloccante di
     * aggiornamento.
     */
    private String minSupportedVersion = "0.0.0";

    /**
     * Sotto questa versione il client mostra un avviso ignorabile.
     */
    private String recommendedVersion = "0.0.0";

    /**
     * Testo opzionale mostrato all'utente. Vuoto significa "usa il testo che il
     * client ha gia' tradotto": la localizzazione e' un problema del client,
     * qui si passa solo un eventuale messaggio straordinario.
     */
    private String message = "";

    public String getMinSupportedVersion() {
        return minSupportedVersion;
    }

    public void setMinSupportedVersion(String minSupportedVersion) {
        this.minSupportedVersion = minSupportedVersion;
    }

    public String getRecommendedVersion() {
        return recommendedVersion;
    }

    public void setRecommendedVersion(String recommendedVersion) {
        this.recommendedVersion = recommendedVersion;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
