-- Los 8 productos del briefing §5.4.4 y las opciones de vidrio de §3.5.1.
-- Botones corregidos (ADR-008): "Cotizar ventana corrediza" y "Cotizar portón americano".
-- Las imágenes son provisionales y se reemplazan desde el panel.

INSERT INTO product (position, category, category_number, title, subtitle, description, image_url, badge, cta_text, active)
VALUES (1, 'VENTANAS', '01', 'Corredizas', 'Línea Panorámica',
        'Ideal para espacios amplios: aperturas suaves, buena ventilación y máxima luz natural.',
        'https://images.unsplash.com/photo-1600565193348-f74bd3c7ccdf?auto=format&fit=crop&w=1200&q=80',
        '4 Obras instaladas', 'Cotizar ventana corrediza', TRUE),
       (2, 'VENTANAS', '01', 'Batientes', 'Apertura 90° con freno',
        'Hojas abatibles con sellado hermético. Un clásico funcional para hogares y oficinas con aislamiento termoacústico.',
        'https://images.unsplash.com/photo-1503174971373-b1f69850bded?auto=format&fit=crop&w=1200&q=80',
        'Apertura 90° con freno', 'Cotizar sistema batiente', TRUE),
       (3, 'PUERTAS', '02', 'Aluminio', 'Línea Pesada & Fachadas',
        'Resistentes a la intemperie y de bajo mantenimiento, para interiores y exteriores residenciales y comerciales.',
        'https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=1200&q=80',
        'Línea Pesada', 'Cotizar puerta de aluminio', TRUE),
       (4, 'PUERTAS', '02', 'Vidrio', 'Templado Monumental & Pivote',
        'Puertas de vidrio templado, elegantes y seguras, para interiores de oficina y entradas principales de diseño arquitectónico.',
        'https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=1200&q=80',
        'Templado 8mm - 12mm', 'Cotizar puerta de vidrio templado', TRUE),
       (5, 'PUERTAS', '02', 'PVC & Termoacústicas', 'Máximo Aislamiento',
        'Buen aislamiento térmico y acústico para hogares, oficinas y espacios comerciales que requieren confort climático total.',
        'https://images.unsplash.com/photo-1600585152220-90363fe7e115?auto=format&fit=crop&w=1200&q=80',
        'Aislamiento Térmico', 'Cotizar puerta termoacústica', TRUE),
       (6, 'PORTONES', '03', 'Instalación · Americanos', 'Robustez & Automatización',
        'Instalación de portones tipo americano: robustos, duraderos y con herrajes confiables para residencias y empresas.',
        'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80',
        'Herrajes de alta resistencia', 'Cotizar portón americano', TRUE),
       (7, 'DIVISIONES', '04', 'Vidrio Templado', 'Baños Master & Oficinas',
        'Separadores elegantes, luminosos y seguros, fabricados a la medida de tu espacio con herrajes en acero inoxidable.',
        'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=1200&q=80',
        'Espesor 8mm a 10mm', 'Cotizar división en vidrio templado', TRUE),
       (8, 'DIVISIONES', '04', 'Acrílico & Policarbonato', 'Económicas y Resistentes',
        'Divisiones livianas, económicas y resistentes al agua, perfectas para baños familiares y áreas de alto tránsito.',
        'https://images.unsplash.com/photo-1507652313519-d4e9174996dd?auto=format&fit=crop&w=1200&q=80',
        'Antigolpes y Ligero', 'Cotizar división en acrílico', TRUE);

-- Las especificaciones y características se enlazan por la posición del producto, no por su id generado.
INSERT INTO product_spec (product_id, position, label, value)
SELECT p.id, s.position, s.label, s.value
FROM (VALUES (1, 1, 'Hojas', '2, 3 o 4 hojas deslizantes'),
             (1, 2, 'Hermeticidad', 'Felpa siliconada perimetral'),
             (1, 3, 'Vidrios compatibles', 'Sencillo, doble, templado'),
             (2, 1, 'Aislamiento', 'Acústico superior'),
             (2, 2, 'Cierre', 'Punto múltiple hermético'),
             (2, 3, 'Perfilería', 'Aluminio extruido calibre pesado'),
             (3, 1, 'Resistencia', 'Resistente a Corrosión y Salitre'),
             (3, 2, 'Seguridad', 'Cerraduras de seguridad multipunto'),
             (3, 3, 'Bisagras', 'Bisagras en acero'),
             (4, 1, 'Cristal', 'Templado 8mm - 12mm de seguridad'),
             (4, 2, 'Mecanismo', 'Pivotes hidráulicos de piso'),
             (4, 3, 'Acabados', 'Vidrio Satinado o Claro'),
             (5, 1, 'Clima', 'Cero condensación y sellado total'),
             (5, 2, 'Cierres', 'Cierres herméticos perimetrales'),
             (5, 3, 'Refuerzo', 'Almatensor en casos necesarios'),
             (6, 1, 'Accionamiento', 'Accionamiento suave con motor silencioso'),
             (6, 2, 'Seguridad', 'Seguridad reforzada con bloqueo automático'),
             (6, 3, 'Estructura', 'Paneles térmicos inyectados de poliuretano'),
             (7, 1, 'Aplicación', 'Para baños master & oficinas'),
             (7, 2, 'Espesor', 'Cristal templado 8mm a 10mm'),
             (7, 3, 'Herrajes', 'Acero inoxidable 304 anti-sarro'),
             (8, 1, 'Acabados', 'Diseños lisos, gota de agua y texturizados'),
             (8, 2, 'Ventajas', 'Antigolpes y peso ultra-ligero'),
             (8, 3, 'Marco', 'Aluminio anodizado blanco, natural o negro'))
         AS s (product_position, position, label, value)
         JOIN product p ON p.position = s.product_position;

INSERT INTO product_feature (product_id, position, text)
SELECT p.id, f.position, f.text
FROM (VALUES (1, 1, 'Rodamientos de alta precisión en acero inox'),
             (1, 2, 'Felpa siliconada cortaviento'),
             (1, 3, 'Seguro embutido multipunto'),
             (2, 1, 'Brazos de fricción telescópicos'),
             (2, 2, 'Sellado perimetral con empaque EPDM'),
             (2, 3, 'Manija ergonómica europea'),
             (3, 1, 'Perfil estructural serie 80'),
             (3, 2, 'Cerradura de alta seguridad'),
             (3, 3, 'Acabados anodizados o pintura electrostática'),
             (4, 1, 'Cajas hidráulicas embutidas al piso'),
             (4, 2, 'Tiradores arquitectónicos en acero inoxidable'),
             (4, 3, 'Freno a 90° con retorno controlado'),
             (5, 1, 'Perfiles multicámara europeos'),
             (5, 2, 'Vidrio laminado acústico o termopanel'),
             (5, 3, 'Ahorro de energía en climatización'),
             (6, 1, 'Control remoto y apertura por app'),
             (6, 2, 'Sensores de reversa infrarrojos'),
             (6, 3, 'Resortes de torsión de alto ciclo'),
             (7, 1, 'Sellado magnético anti-fugas de agua'),
             (7, 2, 'Tratamiento hidrófugo fácil limpieza'),
             (7, 3, 'Cortes y perforaciones exactas por láser'),
             (8, 1, 'Perfilería de aluminio de rápida instalación'),
             (8, 2, 'Placas resistentes a la humedad constante'),
             (8, 3, 'Económico y duradero'))
         AS f (product_position, position, text)
         JOIN product p ON p.position = f.product_position;

INSERT INTO glass_type (position, code, name, active, is_default)
VALUES (1, 'CRUDO', 'Crudo', TRUE, TRUE),
       (2, 'LAMINADO', 'Laminado', TRUE, FALSE),
       (3, 'TEMPLADO', 'Templado', TRUE, FALSE),
       (4, 'LAMINADO_TEMPLADO', 'Laminado templado', TRUE, FALSE),
       (5, 'ACUSTICO', 'Acústico', TRUE, FALSE),
       (6, 'TERMO_ACUSTICO', 'Termo acústico', TRUE, FALSE);

INSERT INTO glass_color (position, code, name, hex, reflective, note, active, is_default)
VALUES (1, 'TRANSPARENTE', 'Transparente', '#E0F2FE', FALSE, NULL, TRUE, TRUE),
       (2, 'BRONCE', 'Bronce', '#966969', FALSE, NULL, TRUE, FALSE),
       (3, 'AZUL_LAGO', 'Azul lago', '#ABC7E1', FALSE, NULL, TRUE, FALSE),
       (4, 'AZUL_INTENSO', 'Azul intenso', '#396DC6', FALSE, NULL, TRUE, FALSE),
       (5, 'REFLECTIVO', 'Reflectivo', '#396DC6', TRUE,
        'Mismo tono que Azul intenso, con acabado reflectivo hacia el exterior.', TRUE, FALSE),
       (6, 'VERDE_FRENCE', 'Verde Frence', '#B8FFB8', FALSE, NULL, TRUE, FALSE),
       (7, 'VERDE_INTENSO', 'Verde intenso', '#2B7133', FALSE, NULL, TRUE, FALSE);

INSERT INTO glass_thickness (position, mm, active, is_default)
VALUES (1, 4, TRUE, FALSE),
       (2, 5, TRUE, FALSE),
       (3, 6, TRUE, TRUE),
       (4, 8, TRUE, FALSE),
       (5, 10, TRUE, FALSE),
       (6, 12, TRUE, FALSE),
       (7, 15, TRUE, FALSE);
