package com.ventanasyaluminios.config;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Configuración web: registra {@link AppProperties}, define CORS y sirve los archivos que no son la API: las imágenes
 * subidas en {@code /media/**} y, con {@code SITE_DIR}, el frontend compilado (ADR-022). Vive en un WebMvcConfigurer
 * para que también la carguen las pruebas de controllers ({@code @WebMvcTest}).
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class WebConfig implements WebMvcConfigurer {

    /** Ruta de la API sin la barra inicial, como la recibe el resolver de recursos. */
    static final String API_PATH = "api/";

    private final AppProperties properties;

    public WebConfig(AppProperties properties) {
        this.properties = properties;
    }

    /** CORS solo se activa si hay orígenes configurados; con el sitio y la API en el mismo dominio no hace falta. */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var origins = properties.cors().allowedOrigins();
        if (origins.isEmpty()) {
            return;
        }
        registry.addMapping("/api/**")
                .allowedOrigins(origins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                .allowCredentials(true);
    }

    /**
     * Imágenes subidas: solo los archivos de su carpeta (nombres UUID generados por el servidor).
     *
     * <p>Sitio: {@code /assets/} con caché de un año (los nombres llevan hash; si falta, 404) y el resto con
     * {@code index.html} sin caché cuando la ruta no es un archivo, para que React Router resuelva {@code /catalogo},
     * {@code /admin}, etc. Las rutas más específicas ({@code /media/**}, {@code /assets/**} y los controllers) ganan
     * a {@code /**}; la API se excluye a mano para que una ruta inexistente responda el 404 en JSON.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(AppProperties.Media.PUBLIC_PREFIX + "**")
                .addResourceLocations(location(properties.media().uploadPath()));

        AppProperties.Site site = properties.site();
        if (!site.enabled()) {
            return;
        }
        String root = location(site.path());
        registry.addResourceHandler("/assets/**")
                .addResourceLocations(root + "assets/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable());
        registry.addResourceHandler("/**")
                .addResourceLocations(root)
                .setCacheControl(CacheControl.noCache())
                .resourceChain(false)
                .addResolver(new SpaFallbackResolver());
    }

    /** La raíz no pasa por el resolver (Spring descarta las rutas vacías): se reenvía a index.html. */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        if (properties.site().enabled()) {
            registry.addViewController("/").setViewName("forward:/index.html");
        }
    }

    /** Carpeta como ubicación de Spring: {@code toUri()} solo agrega la barra final si la carpeta ya existe. */
    private static String location(Path dir) {
        String location = dir.toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }

    /** Devuelve el archivo pedido si existe; si no, {@code index.html}, salvo en la API. */
    static final class SpaFallbackResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource requested = super.getResource(resourcePath, location);
            if (requested != null || resourcePath.startsWith(API_PATH)) {
                return requested;
            }
            return super.getResource("index.html", location);
        }
    }
}
