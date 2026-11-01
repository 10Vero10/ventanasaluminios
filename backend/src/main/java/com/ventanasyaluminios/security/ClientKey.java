package com.ventanasyaluminios.security;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HexFormat;

/**
 * Clave de cliente para los límites por IP. Una IPv6 se agrupa por su bloque /64, porque una sola conexión doméstica
 * suele tener uno entero y podría rotar direcciones para evadir el límite.
 */
final class ClientKey {

    private ClientKey() {
    }

    static String of(String remoteAddress) {
        if (remoteAddress == null || remoteAddress.indexOf(':') < 0) {
            return remoteAddress;
        }
        try {
            // Solo se llega aquí con un literal IPv6, que InetAddress interpreta sin consultar DNS.
            InetAddress address = InetAddress.getByName(remoteAddress);
            if (address instanceof Inet6Address) {
                return HexFormat.of().formatHex(address.getAddress(), 0, 8) + "::/64";
            }
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            return remoteAddress;
        }
    }
}
