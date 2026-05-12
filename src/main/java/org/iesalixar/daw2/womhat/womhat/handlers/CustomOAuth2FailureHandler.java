package org.iesalixar.daw2.womhat.womhat.handlers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler personalizado para fallos de autenticación OAuth2.
 *
 * Mantiene el flujo alineado con el login MVC:
 * - limpia el contexto de seguridad,
 * - invalida la sesión actual si existe,
 * - redirige al login con un parámetro simple para que
 *   LoginController construya el mensaje i18n correspondiente.
 */
@Component
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2FailureHandler.class);
    public static final String OAUTH2_ERROR_CODE_SESSION_KEY = "oauth2ErrorCode";

    /**
     * Maneja los fallos en la autenticación con OAuth2.
     * Este método se ejecuta automáticamente cuando ocurre un fallo de autenticación.
     * Realiza las siguientes acciones:
     * - Limpia el contexto de seguridad.
     * - Invalida la sesión actual si existe.
     * - Agrega un parámetro a la URL de redirección para indicar que hubo un error de autenticación OAuth2.
     * - Redirige al usuario a la página de login para que pueda intentar autenticarse nuevamente.
     *
     * @param request   El objeto {@link HttpServletRequest} que contiene la solicitud HTTP.
     * @param response  El objeto {@link HttpServletResponse} que contiene la respuesta HTTP.
     * @param exception La excepción de autenticación que indica el motivo del fallo.
     * @throws IOException      Si ocurre un error de E/S durante la redirección.
     * @throws ServletException Si ocurre un error relacionado con el manejo de la solicitud.
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        logger.warn("Fallo en autenticación OAuth2: {}", exception.getMessage());

        // Limpiar el contexto de seguridad
        SecurityContextHolder.clearContext();

        // Mantener sesión y guardar un código simple para mostrar mensaje i18n en login.
        HttpSession session = request.getSession(true);
        if (session != null) {
            session.setAttribute(OAUTH2_ERROR_CODE_SESSION_KEY, resolveOauth2ErrorCode(exception));
        }

        // Redirigir al login con un parámetro para mostrar el mensaje de error
        response.sendRedirect(request.getContextPath() + "/login?oauth2error");
    }

    private String resolveOauth2ErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Ex
                && oauth2Ex.getError() != null
                && oauth2Ex.getError().getErrorCode() != null) {
            return oauth2Ex.getError().getErrorCode();
        }
        return "oauth2_generic_error";
    }
}
