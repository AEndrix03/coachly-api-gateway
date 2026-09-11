package it.aredegalli.coachly.app;

/**
 * Risposta di {@code GET /public/app/requirements}.
 *
 * @param minSupportedVersion sotto questa versione il client si blocca
 * @param recommendedVersion  sotto questa versione il client avvisa
 * @param message             testo straordinario, stringa vuota se non serve
 */
public record AppRequirementsResponse(
    String minSupportedVersion,
    String recommendedVersion,
    String message
) {
}
