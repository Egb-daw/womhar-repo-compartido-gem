package org.iesalixar.daw2.womhat.womhat.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Implementación del servicio de construcción de URLs públicas.
 *
 * La base pública se configura en application.properties y permite generar
 * enlaces correctos incluso si la aplicación está detrás de proxy o dominio real.
 */
@Service
@RequiredArgsConstructor
public class AppUrlServiceImpl implements AppUrlService {

    private static final Logger logger = LoggerFactory.getLogger(AppUrlServiceImpl.class);
    private static final String DEFAULT_LOCAL_BASE_URL = "http://localhost:8080";
    private boolean localFallbackLogged;

    /**
     * URL pública base de la aplicación.
     * Ejemplo: https://womhat.midominio.com
     */
    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

    /**
     * Ruta pública del formulario de reset de contraseña.
     */
    @Value("${app.password-reset.path:/auth/reset-password}")
    private String resetPath;

    /**
     * Construye la URL pública del reset de contraseña.
     *
     * @param rawToken token en claro
     * @return URL completa
     */
    @Override
    public String buildResetUrl(String rawToken) {
        return buildUrl(resetPath, Map.of("token", rawToken));
    }

    /**
     * Construye una URL absoluta a partir de una ruta relativa y parámetros query.
     *
     * @param path ruta relativa
     * @param queryParams parámetros query
     * @return URL absoluta
     */
    @Override
    public String buildUrl(String path, Map<String, String> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(trimTrailingSlash(publicBaseUrl))
                .path(ensureLeadingSlash(path));

        if (queryParams != null) {
            queryParams.forEach(builder::queryParam);
        }

        return builder.build().encode().toUriString();
    }

    /**
     * Elimina la barra final si existe.
     */
    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            logLocalFallbackOnce("app.public-base-url no configurada. Se usa fallback local {}");
            return DEFAULT_LOCAL_BASE_URL;
        }
        String normalized = value.trim();
        if (normalized.isBlank()) {
            logLocalFallbackOnce("app.public-base-url vacía tras trim. Se usa fallback local {}");
            return DEFAULT_LOCAL_BASE_URL;
        }
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    /**
     * Asegura que la ruta comience por '/'.
     */
    private String ensureLeadingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "/";
        }
        return value.startsWith("/") ? value : "/" + value;
    }

    private void logLocalFallbackOnce(String pattern) {
        if (localFallbackLogged) {
            return;
        }
        localFallbackLogged = true;
        logger.warn(pattern, DEFAULT_LOCAL_BASE_URL);
    }
}
