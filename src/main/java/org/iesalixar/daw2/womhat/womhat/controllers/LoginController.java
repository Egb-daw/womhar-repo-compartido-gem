package org.iesalixar.daw2.womhat.womhat.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.iesalixar.daw2.womhat.womhat.handlers.CustomOAuth2FailureHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Locale;

/**
 * Controlador encargado de mostrar la página pública de login.
 *
 * Importante:
 * - el login real lo sigue resolviendo Spring Security;
 * - aquí solo conectamos la plantilla visual con el flujo real.
 */
@Controller
public class LoginController {

    @Autowired
    private MessageSource messageSource;

    /**
     * Muestra la vista de inicio de sesión.
     *
     * @param request request actual
     * @param model modelo para la vista
     * @return plantilla login
     */
    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/dashboard";
        }

        Locale locale = LocaleContextHolder.getLocale();

        if (request.getParameter("error") != null) {
            model.addAttribute(
                    "errorMessage",
                    messageSource.getMessage("spring.security.ui.login.error", null, locale)
            );
        }

        if (request.getParameter("logout") != null) {
            model.addAttribute(
                    "successMessage",
                    messageSource.getMessage("spring.security.ui.login.logout-success", null, locale)
            );
        }

        if (request.getParameter("accountDeactivated") != null) {
            model.addAttribute(
                    "successMessage",
                    messageSource.getMessage("spring.security.ui.login.account-deactivated", null, locale)
            );
        }

        if (request.getParameter("oauth2error") != null) {
            HttpSession session = request.getSession(false);
            String oauth2Code = null;
            if (session != null) {
                Object value = session.getAttribute(CustomOAuth2FailureHandler.OAUTH2_ERROR_CODE_SESSION_KEY);
                if (value instanceof String rawCode) {
                    oauth2Code = rawCode;
                }
                session.removeAttribute(CustomOAuth2FailureHandler.OAUTH2_ERROR_CODE_SESSION_KEY);
            }

            String messageKey = "spring.security.ui.login.oauth2-error";
            if ("github_email_missing".equalsIgnoreCase(oauth2Code)) {
                messageKey = "spring.security.ui.login.oauth2-email-missing";
            } else if ("github_user_blocked".equalsIgnoreCase(oauth2Code)) {
                messageKey = "spring.security.ui.login.oauth2-user-blocked";
            }

            model.addAttribute(
                    "errorMessage",
                    messageSource.getMessage(messageKey, null, locale)
            );
        }

        return "views/login/login";
    }
}
