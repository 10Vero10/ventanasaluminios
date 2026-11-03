package com.ventanasyaluminios.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ventanasyaluminios.dto.response.ErrorResponse;
import com.ventanasyaluminios.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Tope de tamaño del cuerpo de las solicitudes, antes de que Jackson lo lea entero en memoria.
 *
 * <p>Es el único tope de tamaño (no hay proxy delante que lo ponga): 64 KB para lo público (cotización e inicio de sesión) y 6 MB para el resto (el respaldo). La subida de imágenes es
 * multipart y la limita {@code spring.servlet.multipart}.
 */
public class RequestSizeLimitFilter extends OncePerRequestFilter {

    static final long PUBLIC_LIMIT = 64 * 1024;
    static final long DEFAULT_LIMIT = 6 * 1024 * 1024;
    private static final String MESSAGE = "La solicitud es demasiado grande.";

    private final ObjectMapper objectMapper;

    public RequestSizeLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    static long limitFor(String path) {
        return path.equals("/api/v1/quotes") || path.startsWith("/api/v1/auth/") ? PUBLIC_LIMIT : DEFAULT_LIMIT;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/v1/admin/media");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long limit = limitFor(request.getRequestURI());
        if (request.getContentLengthLong() > limit) {
            response.setStatus(ErrorCode.PAYLOAD_TOO_LARGE.getStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(), new ErrorResponse(MESSAGE, ErrorCode.PAYLOAD_TOO_LARGE.name()));
            return;
        }
        // Sin Content-Length (envío por partes) se corta al leer: el cuerpo no puede pasar del límite.
        chain.doFilter(new LimitedRequest(request, limit), response);
    }

    private static final class LimitedRequest extends HttpServletRequestWrapper {

        private final long limit;
        private ServletInputStream stream;

        LimitedRequest(HttpServletRequest request, long limit) {
            super(request);
            this.limit = limit;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (stream == null) {
                stream = new LimitedInputStream(super.getInputStream(), limit);
            }
            return stream;
        }
    }

    private static final class LimitedInputStream extends ServletInputStream {

        private final ServletInputStream delegate;
        private final long limit;
        private long read;

        LimitedInputStream(ServletInputStream delegate, long limit) {
            this.delegate = delegate;
            this.limit = limit;
        }

        @Override
        public int read() throws IOException {
            int b = delegate.read();
            if (b >= 0) count(1);
            return b;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int n = delegate.read(buffer, offset, length);
            if (n > 0) count(n);
            return n;
        }

        private void count(int n) throws IOException {
            read += n;
            if (read > limit) throw new IOException(MESSAGE);
        }

        @Override
        public boolean isFinished() {
            return delegate.isFinished();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setReadListener(ReadListener listener) {
            delegate.setReadListener(listener);
        }
    }
}
