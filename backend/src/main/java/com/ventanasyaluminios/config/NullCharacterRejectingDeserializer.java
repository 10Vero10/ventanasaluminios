package com.ventanasyaluminios.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import java.io.IOException;
import org.springframework.boot.jackson.JsonComponent;

/**
 * Lee los textos del JSON como siempre, pero rechaza el carácter nulo (U+0000). PostgreSQL no lo admite y, sin este
 * filtro, la solicitud terminaría en un error de base de datos con un mensaje engañoso; así responde 400.
 */
@JsonComponent
public class NullCharacterRejectingDeserializer extends StringDeserializer {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = super.deserialize(parser, context);
        if (value != null && value.indexOf('\u0000') >= 0) {
            throw context.weirdStringException(value, String.class, "contiene el carácter nulo");
        }
        return value;
    }
}
