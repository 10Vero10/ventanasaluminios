package com.ventanasyaluminios.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Edición de la configuración general del sitio desde el panel. */
public record SiteSettingsRequest(
        @NotBlank(message = "El título de portada es obligatorio.")
        @Size(max = 120, message = "El título de portada admite máximo 120 caracteres.")
        String heroTitle,

        @NotBlank(message = "El subtítulo de portada es obligatorio.")
        @Size(max = 300, message = "El subtítulo de portada admite máximo 300 caracteres.")
        String heroSubtitle,

        @Size(max = 2048, message = "La URL de la imagen principal es demasiado larga.")
        @Pattern(regexp = RequestPatterns.IMAGE_URL, message = "La imagen principal debe ser una URL http(s) válida.")
        String heroImageUrl,

        @Size(max = 80, message = "La etiqueta de portada admite máximo 80 caracteres.")
        String heroBadge,

        @NotBlank(message = "El teléfono es obligatorio.")
        @Size(max = 40, message = "El teléfono admite máximo 40 caracteres.")
        @Pattern(regexp = RequestPatterns.PHONE, message = "El teléfono debe tener números y solo puede incluir espacios, +, guiones y paréntesis.")
        String phone,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 254, message = "El correo es demasiado largo.")
        String email,

        @Size(max = 20, message = "El número de WhatsApp admite máximo 20 caracteres.")
        @Pattern(regexp = RequestPatterns.WHATSAPP, message = "WhatsApp: número con indicativo de país, solo dígitos (ej. 573178463260).")
        String whatsappNumber,

        @NotNull(message = "Los años de experiencia son obligatorios.")
        @Min(value = 0, message = "Los años de experiencia no pueden ser negativos.")
        @Max(value = 150, message = "Los años de experiencia no son válidos.")
        Integer experienceYears,

        @NotNull(message = "Indica si la opción de cotización está activa.")
        Boolean quotingEnabled,

        @Size(max = 2048, message = "La URL del favicon es demasiado larga.")
        @Pattern(regexp = RequestPatterns.IMAGE_URL, message = "El favicon debe ser una URL http(s) válida.")
        String faviconUrl,

        @NotBlank(message = "El párrafo 1 de la historia es obligatorio.")
        @Size(max = 2000, message = "El párrafo 1 de la historia admite máximo 2000 caracteres.")
        String aboutParagraph1,

        @NotBlank(message = "El párrafo 2 de la historia es obligatorio.")
        @Size(max = 2000, message = "El párrafo 2 de la historia admite máximo 2000 caracteres.")
        String aboutParagraph2,

        @NotBlank(message = "El texto destacado es obligatorio.")
        @Size(max = 1000, message = "El texto destacado admite máximo 1000 caracteres.")
        String aboutHighlight) {
}
