import type { BackupImportResult } from '@/types/admin'

import { httpClient } from './httpClient'

/** Respaldo tal como lo entrega el backend (JSON con versión 1). Se guarda en un archivo sin tocarlo. */
export async function exportBackup(): Promise<unknown> {
  const { data } = await httpClient.get<unknown>('/admin/backup')
  return data
}

/** Envía el archivo tal cual; el backend valida la versión y cada elemento. */
export async function importBackup(json: string): Promise<BackupImportResult> {
  const { data } = await httpClient.post<BackupImportResult>('/admin/backup', json, {
    headers: { 'Content-Type': 'application/json' },
    timeout: 60_000,
  })
  return data
}
