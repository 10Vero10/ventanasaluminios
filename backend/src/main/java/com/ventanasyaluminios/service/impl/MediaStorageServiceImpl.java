package com.ventanasyaluminios.service.impl;

import com.ventanasyaluminios.config.AppProperties;
import com.ventanasyaluminios.dto.response.MediaUploadResponse;
import com.ventanasyaluminios.exception.ApiException;
import com.ventanasyaluminios.exception.ErrorCode;
import com.ventanasyaluminios.service.MediaStorageService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementación de {@link MediaStorageService}. El tipo se decide por la firma de bytes del archivo, no
 * por la extensión ni el Content-Type que manda el navegador, que se pueden falsificar.
 */
@Service
public class MediaStorageServiceImpl implements MediaStorageService {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final byte[] RIFF = {'R', 'I', 'F', 'F'};
    private static final byte[] WEBP = {'W', 'E', 'B', 'P'};

    private final AppProperties.Media media;
    private final Path uploadDir;

    /** Crea la carpeta al arrancar: si no se puede escribir, la aplicación no inicia. */
    public MediaStorageServiceImpl(AppProperties properties) {
        this.media = properties.media();
        this.uploadDir = media.uploadPath();
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear la carpeta de imágenes " + uploadDir, e);
        }
    }

    @Override
    public MediaUploadResponse store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ApiException(ErrorCode.EMPTY_FILE, "No se recibió ninguna imagen.");
        }
        if (file.getSize() > media.maxFileSize().toBytes()) {
            throw new ApiException(ErrorCode.FILE_TOO_LARGE, "La imagen es demasiado pesada (" + file.getSize() / 1024
                    + " KB). Usa una imagen de máximo " + media.maxFileSizeLabel()
                    + " o pega la URL de una imagen alojada en internet.");
        }
        try {
            byte[] bytes = file.getBytes();
            String extension = detectExtension(bytes).orElseThrow(() -> new ApiException(ErrorCode.INVALID_FILE_TYPE,
                    "El archivo seleccionado no es una imagen JPG, PNG o WebP."));
            String fileName = UUID.randomUUID() + "." + extension;
            Files.write(uploadDir.resolve(fileName), bytes);
            return new MediaUploadResponse(AppProperties.Media.PUBLIC_PREFIX + fileName);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen en " + uploadDir, e);
        }
    }

    /** Extensión según la firma de bytes; vacío si no es JPG, PNG ni WebP. */
    static Optional<String> detectExtension(byte[] bytes) {
        if (startsWith(bytes, 0, JPEG)) {
            return Optional.of("jpg");
        }
        if (startsWith(bytes, 0, PNG)) {
            return Optional.of("png");
        }
        if (startsWith(bytes, 0, RIFF) && startsWith(bytes, 8, WEBP)) {
            return Optional.of("webp");
        }
        return Optional.empty();
    }

    private static boolean startsWith(byte[] data, int offset, byte[] signature) {
        return data.length >= offset + signature.length
                && Arrays.equals(data, offset, offset + signature.length, signature, 0, signature.length);
    }
}
