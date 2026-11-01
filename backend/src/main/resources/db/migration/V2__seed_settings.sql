-- Contenido inicial (briefing §5). Las imágenes son provisionales y se reemplazan desde el panel.

INSERT INTO site_settings (id, hero_title, hero_subtitle, hero_image_url, hero_badge, phone, email, whatsapp_number,
                           experience_years, quoting_enabled, favicon_url, about_paragraph1, about_paragraph2,
                           about_highlight)
VALUES (1,
        'Ventanas y Aluminios SAS',
        'Fabricamos e instalamos ventanas, puertas, portones y divisiones, con más de 25 años de experiencia y acabados de excelencia.',
        'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1600&q=80',
        'VIDRIO TEMPLADO & ALUMINIO · Sistemas 2025',
        '+57 317 846 3260',
        'ventanaluminio@hotmail.com',
        '573178463260',
        25,
        TRUE,
        '/favicon.svg',
        'Somos una empresa especializada en la fabricación e instalación de soluciones en vidrio, aluminio y otros materiales, con más de 25 años de experiencia en el sector.',
        'Durante todos estos años hemos trabajado para brindar a nuestros clientes productos de alta calidad, excelentes acabados y soluciones adaptadas a sus necesidades, cuidando cada detalle desde la fabricación hasta la instalación.',
        'Contamos con un equipo de trabajo comprometido y con amplia experiencia, preparado para desarrollar proyectos residenciales, comerciales y empresariales.');

INSERT INTO featured_line (position, number, title, category_tag, description, image_url, link_text, target_category)
VALUES (1, '01', 'Ventanas', 'SISTEMAS ACÚSTICOS & TÉRMICOS',
        'Corredizas y batientes en aluminio, con vidrio sencillo, doble o templado.',
        'https://images.unsplash.com/photo-1600565193348-f74bd3c7ccdf?auto=format&fit=crop&w=1000&q=80',
        'Ver productos', 'VENTANAS'),
       (2, '02', 'Puertas', 'SEGURIDAD & ACCESO RESIDENCIAL',
        'De aluminio, vidrio y PVC, para interiores, exteriores y proyectos comerciales.',
        'https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=1000&q=80',
        'Ver productos', 'PUERTAS'),
       (3, '03', 'Portones', 'SISTEMAS AUTOMÁTICOS & MANUALES',
        'Instalación de portones tipo americano, robustos y de operación confiable.',
        'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1000&q=80',
        'Ver productos', 'PORTONES'),
       (4, '04', 'Divisiones', 'ESPACIOS INTERIORES & BAÑOS',
        'En vidrio templado y acrílico para baños y oficinas, fabricadas a la medida exacta.',
        'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=1000&q=80',
        'Ver productos', 'DIVISIONES');

INSERT INTO gallery_image (position, title, url, category)
VALUES (1, 'Fachada Minimalista Cristales Altos',
        'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1600&q=80', 'VENTANAS'),
       (2, 'Ventanal Panorámico Sala Europea',
        'https://images.unsplash.com/photo-1600565193348-f74bd3c7ccdf?auto=format&fit=crop&w=1600&q=80', 'VENTANAS'),
       (3, 'Puerta Monumental & Aluminio Negro',
        'https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=1600&q=80', 'PUERTAS'),
       (4, 'Oficina Cristal Templado & Fachada',
        'https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=1600&q=80', 'DIVISIONES'),
       (5, 'Taller de Carpintería y Ensamble',
        'https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=1600&q=80', NULL);
