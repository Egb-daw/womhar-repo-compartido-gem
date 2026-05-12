package org.iesalixar.daw2.womhat.womhat.services;

import java.util.Map;

/**
 * Servicio utilitario para construir URLs públicas de la aplicación.
 *
 * Se usa, por ejemplo, para generar enlaces absolutos de recuperación de contraseña.
 */
public interface AppUrlService {

    /**
     * Construye la URL pública de reseteo de contraseña a partir del token en claro.
     *
     * @param rawToken token en claro
     * @return URL pública completa
     */
    String buildResetUrl(String rawToken);

    /**
     * Construye una URL pública a partir de una ruta y sus parámetros query.
     *
     * @param path ruta relativa
     * @param queryParams parámetros query
     * @return URL pública completa
     */
    String buildUrl(String path, Map<String, String> queryParams);
}