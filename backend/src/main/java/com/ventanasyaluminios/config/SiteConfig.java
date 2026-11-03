package com.ventanasyaluminios.config;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Sirve el frontend compilado cuando {@code SITE_DIR} tiene valor, con las mismas reglas que nginx en Docker Compose:
 * {@code /assets/} con caché de un año (los nombres llevan hash) y 404 si el archivo no existe; cualquier otra ruta que
 * no sea un archivo responde {@code index.html} sin caché, para que React Router resuelva {@code /catalogo},
 * {@code /admin}, etc. {@code /api} y {@code /media} nunca caen en {@code index.html}.
 */
@Configuration
@EnableConfigurationProperties(SiteProperties.class)
public class SiteConfig implements WebMvcConfigurer {

    private final SiteProperties properties;

    public SiteConfig(SiteProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!properties.enabled()) {
            return;
        }
        String location = Path.of(properties.dir()).toAbsolutePath().toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/assets/**")
                .addResourceLocations(location + "assets/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable());
        registry.addResourceHandler("/**")
                .addResourceLocations(location)
                .setCacheControl(CacheControl.noCache())
                .resourceChain(false)
                .addResolver(new SpaFallbackResolver());
    }

    /** La raíz no pasa por el resolver (Spring descarta las rutas vacías): se reenvía a index.html. */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        if (properties.enabled()) {
            registry.addViewController("/").setViewName("forward:/index.html");
        }
    }

    /** Devuelve el archivo pedido si existe; si no, {@code index.html}, salvo en las rutas del backend. */
    static final class SpaFallbackResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource requested = super.getResource(resourcePath, location);
            if (requested != null) {
                return requested;
            }
            if (resourcePath.startsWith("api/") || resourcePath.startsWith("media/") || resourcePath.startsWith("assets/")) {
                return null;
            }
            return super.getResource("index.html", location);
        }
    }
}
