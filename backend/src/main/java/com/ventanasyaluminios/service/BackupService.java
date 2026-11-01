package com.ventanasyaluminios.service;

import com.ventanasyaluminios.dto.request.BackupRequest;
import com.ventanasyaluminios.dto.response.BackupImportResponse;

/** Exportar e importar el contenido del sitio como respaldo JSON. */
public interface BackupService {

    BackupRequest export();

    /**
     * Reemplaza todo el contenido por el del respaldo, en una sola transacción: si algo falla, nada cambia.
     *
     * @param json texto del archivo tal como lo subió el administrador
     */
    BackupImportResponse importBackup(String json);
}
