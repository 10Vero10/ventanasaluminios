-- Foto del taller en "Quiénes somos" (briefing §5.5.1), editable desde el panel. Antes estaba fija en el código del
-- sitio viejo; se siembra con la misma imagen.

ALTER TABLE site_settings ADD COLUMN about_image_url VARCHAR(2048);

UPDATE site_settings
SET about_image_url = 'https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=1200&q=80'
WHERE id = 1;
