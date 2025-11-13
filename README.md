# Payment Service (PS)

## Swagger / OpenAPI

1. Service starten, z. B. `mvn spring-boot:run`.
2. Swagger UI aufrufen unter `http://localhost:8083/swagger-ui`.
3. Die rohe OpenAPI-Spezifikation liegt unter `http://localhost:8083/api-docs`.

Dokumentiert werden alle drei Payment-Flows (`/payments/authorize`, `/payments/capture`, `/payments/refund`) inklusive Idempotency-Header, Erfolgsantworten (`PaymentResponse`) sowie das gemeinsame Fehlerobjekt `ApiErrorResponse`.
