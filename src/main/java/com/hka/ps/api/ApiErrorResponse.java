package com.hka.ps.api;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiErrorResponse", description = "Standardisiertes Fehlerobjekt des Payment Service")
public record ApiErrorResponse(
    @Schema(description = "Zeitpunkt des Fehlers (ISO-8601)", example = "2024-10-06T14:32:11.451Z")
    Instant timestamp,
    @Schema(description = "HTTP Status Code", example = "400")
    int status,
    @Schema(description = "Kurzbeschreibung des Fehlers", example = "Bad Request")
    String error,
    @Schema(description = "Detailnachricht", example = "amount must be > 0")
    String message
) {}
