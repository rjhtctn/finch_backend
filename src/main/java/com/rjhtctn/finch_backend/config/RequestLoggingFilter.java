package com.rjhtctn.finch_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjhtctn.finch_backend.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.*;
import java.util.Map;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RequestLoggingFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();
        boolean isAuth = request.getRequestURI().startsWith("/api/auth/");
        HttpServletRequest wrappedReq = isAuth ? new CachedBodyHttpServletRequest(request) : request;
        CachedBodyHttpServletResponse wrappedRes = new CachedBodyHttpServletResponse(response);

        filterChain.doFilter(wrappedReq, wrappedRes);

        long duration = System.currentTimeMillis() - start;
        int status = wrappedRes.getStatus();
        String username = resolveUsername(wrappedReq);
        String color = getColor(status);
        String message = extractMessage(wrappedRes);

        String statusText = switch (status) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            case 500 -> "Server Error";
            default -> String.valueOf(status);
        };

        String logLine = String.format(
                "%s[%d %s]%s %s %s (%dms) user=%s%s",
                color,
                status,
                statusText,
                "\u001B[0m",
                request.getMethod(),
                request.getRequestURI(),
                duration,
                username,
                (message != null ? " ⚠ " + message : "")
        );

        if (status >= 400) log.warn(logLine);
        else log.info(logLine);

        wrappedRes.copyBodyToResponse();
    }

    private String getColor(int status) {
        if (status >= 500) return "\u001B[31m";
        if (status >= 400) return "\u001B[33m";
        if (status >= 200) return "\u001B[32m";
        return "\u001B[0m";
    }

    private String resolveUsername(HttpServletRequest req) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
            String header = req.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                return jwtService.extractUsername(token);
            }
            if (req instanceof CachedBodyHttpServletRequest cached) {
                String body = cached.getBody();
                if (body != null && !body.isBlank()) {
                    body = body.replaceAll("\"password\"\\s*:\\s*\".*?\"", "\"password\":\"***\"");
                    Map<String, Object> json = objectMapper.readValue(body, Map.class);
                    if (json.containsKey("username")) return String.valueOf(json.get("username"));
                    if (json.containsKey("loginIdentifier")) return String.valueOf(json.get("loginIdentifier"));
                    if (json.containsKey("email")) return String.valueOf(json.get("email"));
                }
            }
        } catch (Exception ignored) {}
        return "unauthenticated";
    }

    private String extractMessage(CachedBodyHttpServletResponse res) {
        try {
            byte[] body = res.getBody();
            if (body.length == 0) return null;

            String content = new String(body);
            if (content.startsWith("{") && content.contains("message")) {
                Map<String, Object> json = objectMapper.readValue(content, Map.class);
                Object msg = json.get("message");
                if (msg != null) return msg.toString();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;
        CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            if (request.getContentLengthLong() > 10 * 1024) {
                this.cachedBody = new byte[0];
                return;
            }
            try (InputStream in = request.getInputStream()) {
                this.cachedBody = in.readAllBytes();
            }
        }
        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(this.cachedBody);
            return new ServletInputStream() {
                @Override public int read() { return bais.read(); }
                @Override public boolean isFinished() { return bais.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener l) {}
            };
        }
        public String getBody() throws IOException {
            return new String(this.cachedBody, getCharacterEncoding() != null ? getCharacterEncoding() : "UTF-8");
        }
    }

    private static class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private final ServletOutputStream outputStream = new ServletOutputStream() {
            @Override public void write(int b) { buffer.write(b); }
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener listener) {}
        };
        private final PrintWriter writer = new PrintWriter(new OutputStreamWriter(buffer));

        CachedBodyHttpServletResponse(HttpServletResponse response) {
            super(response);
        }

        @Override public ServletOutputStream getOutputStream() { return outputStream; }
        @Override public PrintWriter getWriter() { return writer; }

        public byte[] getBody() {
            writer.flush();
            return buffer.toByteArray();
        }

        public void copyBodyToResponse() throws IOException {
            HttpServletResponse original = (HttpServletResponse) getResponse();
            original.getOutputStream().write(getBody());
        }
    }
}