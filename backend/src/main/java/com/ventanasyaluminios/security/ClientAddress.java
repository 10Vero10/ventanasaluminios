package com.ventanasyaluminios.security;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

/**
 * IP del visitante para los límites por IP.
 *
 * <p>En Docker, nginx está en la red interna y Tomcat ya resuelve la IP real con {@code X-Forwarded-For}. En Render,
 * nginx llama al backend por su dirección pública (el plan gratis no recibe tráfico privado), así que la conexión llega
 * desde la IP de salida de nginx, igual para todos. Por eso nginx manda la IP real en {@code X-Client-Ip} junto con una
 * clave compartida en {@code X-Proxy-Secret}: solo con la clave correcta se usa esa IP. Sin clave configurada, o con
 * una clave equivocada (alguien llamando directo al backend), cuenta la IP de la conexión.
 */
public final class ClientAddress {

    static final String CLIENT_IP_HEADER = "X-Client-Ip";
    static final String PROXY_SECRET_HEADER = "X-Proxy-Secret";

    /** Solo literales IPv4 o IPv6: nunca un nombre que obligue a consultar DNS. */
    private static final Pattern IP_LITERAL = Pattern.compile("^(\\d{1,3}(\\.\\d{1,3}){3}|[0-9A-Fa-f:.]{2,45})$");

    private ClientAddress() {
    }

    public static String of(HttpServletRequest request, String trustedProxySecret) {
        if (trustedProxySecret != null && !trustedProxySecret.isBlank()) {
            String secret = request.getHeader(PROXY_SECRET_HEADER);
            String clientIp = request.getHeader(CLIENT_IP_HEADER);
            if (secret != null && clientIp != null && matches(secret, trustedProxySecret)
                    && IP_LITERAL.matcher(clientIp.trim()).matches()) {
                return clientIp.trim();
            }
        }
        return request.getRemoteAddr();
    }

    /** Comparación de tiempo constante, para no revelar la clave por tiempos de respuesta. */
    private static boolean matches(String given, String expected) {
        return MessageDigest.isEqual(given.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    }
}
