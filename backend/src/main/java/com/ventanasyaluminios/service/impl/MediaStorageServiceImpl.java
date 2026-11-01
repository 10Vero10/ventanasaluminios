package com.ventanasyaluminios.service.impl;

import com.ventanasyaluminios.config.AppProperties;
import com.ventanasyaluminios.dto.response.MediaUploadResponse;
import com.ventanasyaluminios.exception.InvalidFileException;
import com.ventanasyaluminios.service.MediaStorageService;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementación de {@link MediaStorageService}. El tipo se decide por la firma de bytes del archivo, no
 * por la extensión ni el Content-Type que manda el navegador, que se pueden falsificar.
 */
@Service
public class MediaStorageServiceImpl implements MediaStorageService {

    /** Ruta pública bajo la que {@code WebConfig} sirve la carpeta de imágenes. */
    public static final String PUBLIC_PREFIX = "/media/";

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final byte[] RIFF = {'R', 'I', 'F', 'F'};
    private static final byte[] WEBP = {'W', 'E', 'B', 'P'};

    private final Path uploadDir;
    private final long maxBytes;

    public MediaStorageServiceImpl(AppProperties properties) {
        this.uploadDir = Path.of(properties.media().uploadDir()).toAbsolutePath().normalize();
        this.maxBytes = properties.media().maxFileSize().toBytes();
    }

    @Override
    public MediaUploadResponse store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("EMPTY_FILE", "No se recibió ninguna imagen.");
        }
        if (file.getSize() > maxBytes) {
            throw new InvalidFileException("FILE_TOO_LARGE", "La imagen es demasiado pesada (" + file.getSize() / 1024
                    + " KB). Usa una imagen de máximo 5 MB o pega la URL de una imagen alojada en internet.");
        }
        String extension = detectExtension(file);
        String fileName = UUID.randomUUID() + "." + extension;
        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(uploadDir);
            Files.copy(in, uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen en " + uploadDir, e);
        }
        return new MediaUploadResponse(PUBLIC_PREFIX + fileName);
    }

    private String detectExtension(MultipartFile file) {
        byte[] header;
        try (InputStream in = file.getInputStream()) {
            header = in.readNBytes(12);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la imagen subida", e);
        }
        if (startsWith(header, 0, JPEG)) {
            return "jpg";
        }
        if (startsWith(header, 0, PNG)) {
            return "png";
        }
        if (startsWith(header, 0, RIFF) && startsWith(header, 8, WEBP)) {
            return "webp";
        }
        throw new InvalidFileException("INVALID_FILE_TYPE", "El archivo seleccionado no es una imagen JPG, PNG o WebP.");
    }

    private static boolean startsWith(byte[] data, int offset, byte[] signature) {
        return data.length >= offset + signature.length
                && Arrays.equals(data, offset, offset + signature.length, signature, 0, signature.length);
    }
}
