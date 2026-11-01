package com.ventanasyaluminios.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ventanasyaluminios.config.AppProperties;
import com.ventanasyaluminios.exception.InvalidFileException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

class MediaStorageServiceImplTest {

    /** Firma mínima de un PNG: suficiente para la detección por bytes. */
    static final byte[] PNG_BYTES = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 0x0D};

    @TempDir
    Path uploadDir;

    private MediaStorageServiceImpl service;

    @BeforeEach
    void setUp() {
        var properties = new AppProperties(new AppProperties.Cors(List.of()),
                new AppProperties.Media(uploadDir.toString(), DataSize.ofKilobytes(1)));
        service = new MediaStorageServiceImpl(properties);
    }

    @Test
    void guardaUnPngConNombreAleatorio() throws Exception {
        var response = service.store(new MockMultipartFile("file", "foto.png", "image/png", PNG_BYTES));

        assertThat(response.url()).startsWith("/media/").endsWith(".png");
        String fileName = response.url().substring("/media/".length());
        assertThat(Files.readAllBytes(uploadDir.resolve(fileName))).isEqualTo(PNG_BYTES);
    }

    @Test
    void rechazaUnArchivoQueNoEsImagenAunqueDigaSerlo() {
        var fake = new MockMultipartFile("file", "foto.jpg", "image/jpeg", "<script>".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.store(fake))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage("El archivo seleccionado no es una imagen JPG, PNG o WebP.");
    }

    @Test
    void rechazaArchivosMasGrandesQueElLimite() {
        var big = new MockMultipartFile("file", "foto.png", "image/png", new byte[2048]);

        assertThatThrownBy(() -> service.store(big))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageStartingWith("La imagen es demasiado pesada (2 KB).");
    }
}
