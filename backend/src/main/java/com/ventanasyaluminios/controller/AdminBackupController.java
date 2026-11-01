package com.ventanasyaluminios.controller;

import com.ventanasyaluminios.dto.request.BackupRequest;
import com.ventanasyaluminios.dto.response.BackupImportResponse;
import com.ventanasyaluminios.service.BackupService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Respaldo del contenido del sitio. Solo rol ADMIN. */
@RestController
@RequestMapping("/api/v1/admin/backup")
public class AdminBackupController {

    private final BackupService service;

    public AdminBackupController(BackupService service) {
        this.service = service;
    }

    /** Se descarga como archivo; el nombre lo pone el frontend con la fecha. */
    @GetMapping
    public ResponseEntity<BackupRequest> export() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"respaldo-ventanas-y-aluminios.json\"")
                .body(service.export());
    }

    /**
     * Recibe el texto del archivo tal cual, para responder con los mensajes del briefing §3.10 si no es un JSON válido
     * o no tiene la forma de un respaldo.
     */
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public BackupImportResponse importBackup(@RequestBody String json) {
        return service.importBackup(json);
    }
}
