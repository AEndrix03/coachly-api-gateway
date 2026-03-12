# coachly-api-gateway

Il filtro `UserContextHeaders` risolve `X-User-Id` chiamando `coachly-users-be` su `/internal/identity/resolve` con `externalId = jwt.sub`, quindi mette il mapping in cache Caffeine prima di inoltrare la richiesta ai servizi downstream.
