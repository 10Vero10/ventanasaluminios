package com.ventanasyaluminios.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ventanasyaluminios.TestImages;
import com.ventanasyaluminios.config.AppProperties;
import com.ventanasyaluminios.exception.ApiException;
import com.ventanasyaluminios.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

class MediaStorageServiceImplTest {

    private static final AppProperties.RateLimit RATE_LIMIT = new AppProperties.RateLimit(
            new AppProperties.Rule(10, Duration.ofHours(1)), new AppProperties.Rule(5, Duration.ofMinutes(1)));
    private static final AppProperties.Auth AUTH = new AppProperties.Auth("x".repeat(32), Duration.ofHours(8), true,
            null, null);

    @TempDir
    Path tempDir;

    private Path uploadDir;
    private MediaStorageServiceImpl service;

    @BeforeEach
    void setUp() {
        uploadDir = tempDir.resolve("uploads");
        var properties = new AppProperties(new AppProperties.Cors(List.of()),
                new AppProperties.Media(uploadDir.toString(), DataSize.ofKilobytes(1)), RATE_LIMIT, AUTH);
        service = new MediaStorageServiceImpl(properties);
    }

    @Test
    void creaLaCarpetaAlArrancar() {
        assertThat(uploadDir).isDirectory();
    }

    @Test
    void guardaUnPngConNombreAleatorio() throws Exception {
        var response = service.store(new MockMultipartFile("file", "foto.png", "image/png", TestImages.PNG));

        assertThat(response.url()).startsWith("/media/").endsWith(".png");
        String fileName = response.url().substring("/media/".length());
        assertThat(Files.readAllBytes(uploadDir.resolve(fileName))).isEqualTo(TestImages.PNG);
    }

    @Test
    void detectaJpegPngYWebpPorSusBytes() {
        assertThat(MediaStorageServiceImpl.detectExtension(TestImages.JPEG)).contains("jpg");
        assertThat(MediaStorageServiceImpl.detectExtension(TestImages.PNG)).contains("png");
        assertThat(MediaStorageServiceImpl.detectExtension(TestImages.WEBP)).contains("webp");
        assertThat(MediaStorageServiceImpl.detectExtension("<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8)))
                .isEmpty();
        assertThat(MediaStorageServiceImpl.detectExtension(new byte[] {'R', 'I', 'F', 'F'})).isEmpty();
    }

    @Test
    void rechazaUnArchivoQueNoEsImagenAunqueDigaSerlo() {
        var fake = new MockMultipartFile("file", "foto.jpg", "image/jpeg", "<script>".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.store(fake))
                .isInstanceOf(ApiException.class)
                .hasMessage("El archivo seleccionado no es una imagen JPG, PNG o WebP.");
    }

    @Test
    void rechazaArchivosVacios() {
        assertThatThrownBy(() -> service.store(new MockMultipartFile("file", "foto.png", "image/png", new byte[0])))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo(ErrorCode.EMPTY_FILE);
    }

    @Test
    void rechazaArchivosMasGrandesQueElLimiteConfigurado() {
        var big = new MockMultipartFile("file", "foto.png", "image/png", new byte[2048]);

        assertThatThrownBy(() -> service.store(big))
                .isInstanceOf(ApiException.class)
                .hasMessage("La imagen es demasiado pesada (2 KB). Usa una imagen de máximo 1 KB o pega la URL de una"
                        + " imagen alojada en internet.");
    }
}
