package com.ventanasyaluminios.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ClientAddressTest {

    private static final String SECRET = "clave-compartida-con-nginx-de-32-caracteres";

    private static MockHttpServletRequest request(String secret, String clientIp) {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("44.1.1.1");
        if (secret != null) request.addHeader(ClientAddress.PROXY_SECRET_HEADER, secret);
        if (clientIp != null) request.addHeader(ClientAddress.CLIENT_IP_HEADER, clientIp);
        return request;
    }

    @Test
    void conLaClaveCorrectaUsaLaIpQueMandaElProxy() {
        assertThat(ClientAddress.of(request(SECRET, "190.1.2.3"), SECRET)).isEqualTo("190.1.2.3");
        assertThat(ClientAddress.of(request(SECRET, "2001:db8::1"), SECRET)).isEqualTo("2001:db8::1");
    }

    @Test
    void sinClaveOConClaveEquivocadaCuentaLaIpDeLaConexion() {
        assertThat(ClientAddress.of(request(null, "190.1.2.3"), SECRET)).isEqualTo("44.1.1.1");
        assertThat(ClientAddress.of(request("otra", "190.1.2.3"), SECRET)).isEqualTo("44.1.1.1");
        assertThat(ClientAddress.of(request(SECRET, "190.1.2.3"), null)).isEqualTo("44.1.1.1");
    }

    @Test
    void ignoraValoresQueNoSonUnaIp() {
        assertThat(ClientAddress.of(request(SECRET, "evil.example.com"), SECRET)).isEqualTo("44.1.1.1");
    }
}
