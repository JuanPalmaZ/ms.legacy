package cl.paris.marketplace.ms.legacy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.paris.marketplace.ms.legacy.dto.LegacyRecordResponse;
import cl.paris.marketplace.ms.legacy.dto.LegacySyncRequest;
import cl.paris.marketplace.ms.legacy.service.LegacyService;
import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/legacy")
@Tag(name = "Legacy", description = "Endpoints para la sincronización y consulta de datos del ERP heredado")
public class LegacyController {

    private final LegacyService legacyService;

    public LegacyController(LegacyService legacyService) {
        this.legacyService = legacyService;
    }

    @Operation(summary = "Sincroniza un dato antiguo del sistema ERP")
    @ApiResponse(responseCode = "201", description = "Sincronización exitosa y registro creado")
    @ApiResponse(responseCode = "403", description = "Acceso denegado: Llave de sistema inválida o ausente")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Payload con los datos migrados del ERP",
        content = @Content(
            examples = @ExampleObject(
                name = "EjemploLegacySync",
                value = "{\n  \"tipoEntidad\": \"PRODUCTO\",\n  \"codigoAntiguo\": \"PRD-1029\",\n  \"datosMigrados\": \"{\\\"nombre\\\": \\\"Zapatillas\\\", \\\"precio\\\": 45000}\"\n}"
            )
        )
    )
    @PostMapping("/sincronizar")
    public ResponseEntity<?> sincronizarDatoAntiguo(
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret,
            @Valid @RequestBody LegacySyncRequest request) {
        
        if (!"paris-legacy-secret-2026".equals(secret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Acceso denegado: Llave de sistema inválida o ausente.");
        }

        LegacyRecordResponse response = legacyService.sincronizarDatoAntiguo(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED); 
    }

    @Operation(summary = "Obtiene el historial completo de sincronizaciones")
    @ApiResponse(responseCode = "200", description = "Historial listado exitosamente")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/historial")
    public ResponseEntity<List<LegacyRecordResponse>> listarHistorialSincronizaciones() {
        return ResponseEntity.ok(legacyService.listarHistorialSincronizaciones());
    }

    @Operation(summary = "Filtra el historial de sincronizaciones según el tipo de entidad (PRODUCTO/CLIENTE/STOCK)")
    @ApiResponse(responseCode = "200", description = "Registros obtenidos exitosamente para la entidad solicitada")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/entidad/{tipoEntidad}")
    public ResponseEntity<List<LegacyRecordResponse>> listarPorTipoEntidad(@PathVariable String tipoEntidad) {
        return ResponseEntity.ok(legacyService.listarPorTipoEntidad(tipoEntidad));
    }
}