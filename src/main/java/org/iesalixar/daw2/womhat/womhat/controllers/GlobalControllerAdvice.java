package org.iesalixar.daw2.womhat.womhat.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.RequestDispatcher;
import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.dtos.UserProfileFormDTO;
import org.iesalixar.daw2.womhat.womhat.entities.User;
import org.iesalixar.daw2.womhat.womhat.enums.RackPurchaseOrderStatus;
import org.iesalixar.daw2.womhat.womhat.repositories.RackPurchaseOrderRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRackAccessRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRepository;
import org.iesalixar.daw2.womhat.womhat.services.UserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;

/**
 * Controlador global para agregar atributos comunes a todas las vistas controladas por @Controller.
 */
@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerAdvice.class);
    private static final String DEFAULT_PROFILE_IMAGE = "/assets/images/Perfil.svg";

    private final UserProfileService userProfileService;
    private final UserRepository userRepository;
    private final UserRackAccessRepository userRackAccessRepository;
    private final RackPurchaseOrderRepository rackPurchaseOrderRepository;

    @ModelAttribute
    public void addGlobalAttributes(Model model, Principal principal, HttpServletRequest request, CsrfToken csrfToken) {
        String currentPath = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (contextPath != null && !contextPath.isBlank() && currentPath.startsWith(contextPath)) {
            currentPath = currentPath.substring(contextPath.length());
        }

        if (currentPath == null || currentPath.isBlank()) {
            currentPath = "/";
        }

        boolean authenticated = principal != null;
        boolean adminAccess = authenticated && request.isUserInRole("ADMIN");
        boolean customerAccess = authenticated && !adminAccess;
        boolean showAppNav = !adminAccess;

        model.addAttribute("currentPath", currentPath);
        model.addAttribute("assetVersion", Long.toString(System.currentTimeMillis()));
        model.addAttribute("adminAccess", adminAccess);
        model.addAttribute("customerAccess", customerAccess);
        model.addAttribute("showAppNav", showAppNav);
        model.addAttribute("accessibleRackCount", 0);
        model.addAttribute("customerOrderCount", 0L);
        model.addAttribute("customerPlacedOrderCount", 0L);
        model.addAttribute("customerFulfilledOrderCount", 0L);
        model.addAttribute("userProfileImage", DEFAULT_PROFILE_IMAGE);

        // Fuerza la resolución temprana del token CSRF antes de que Thymeleaf empiece a volcar la respuesta.
        if (csrfToken != null) {
            csrfToken.getToken();
            model.addAttribute("_csrf", csrfToken);
        }

        // URLs de cambio de idioma preservando la ruta y los query params actuales.
        model.addAttribute("langEsUrl", buildLanguageUrl(request, "es"));
        model.addAttribute("langEnUrl", buildLanguageUrl(request, "en"));

        if (principal == null) {
            return;
        }

        try {
            UserProfileFormDTO profile = userProfileService.getFormByEmail(principal.getName());

            if (profile != null && profile.getProfileImage() != null && !profile.getProfileImage().isBlank()) {
                model.addAttribute("userProfileImage", profile.getProfileImage());
            }

        } catch (Exception ex) {
            logger.warn("No se pudo cargar la imagen de perfil del usuario {}: {}", principal.getName(), ex.getMessage());
        }

        try {
            User user = userRepository.findByEmailIgnoreCase(principal.getName()).orElse(null);
            if (user != null && user.getId() != null) {
                model.addAttribute("accessibleRackCount", userRackAccessRepository.findByIdUserId(user.getId()).size());
                model.addAttribute("customerOrderCount", rackPurchaseOrderRepository.countByUser_Id(user.getId()));
                model.addAttribute(
                        "customerPlacedOrderCount",
                        rackPurchaseOrderRepository.countByUser_IdAndStatus(user.getId(), RackPurchaseOrderStatus.PLACED)
                );
                model.addAttribute(
                        "customerFulfilledOrderCount",
                        rackPurchaseOrderRepository.countByUser_IdAndStatus(user.getId(), RackPurchaseOrderStatus.FULFILLED)
                );
            }
        } catch (Exception ex) {
            logger.warn("No se pudieron cargar los contadores globales del usuario {}: {}", principal.getName(), ex.getMessage());
        }
    }

    /**
     * Construye la URL para cambiar de idioma sin perder la ruta actual ni sus parámetros.
     */
    private String buildLanguageUrl(HttpServletRequest request, String lang) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(resolveLanguageBasePath(request));

        if ("GET".equalsIgnoreCase(request.getMethod())) {
            request.getParameterMap().forEach((key, values) -> {
                if ("lang".equalsIgnoreCase(key)) {
                    return;
                }

                if (values == null || values.length == 0) {
                    builder.queryParam(key);
                    return;
                }

                for (String value : values) {
                    builder.queryParam(key, value);
                }
            });
        }

        builder.queryParam("lang", lang);
        return builder.build().encode().toUriString();
    }

    /**
     * Resuelve la ruta base para el cambio de idioma.
     *
     * En dispatch de error, `request.getRequestURI()` apunta a `/error` y no a la URL original;
     * por eso se intenta recuperar `RequestDispatcher.ERROR_REQUEST_URI` para evitar redirecciones
     * incorrectas al cambiar idioma desde páginas 4xx/5xx.
     */
    private String resolveLanguageBasePath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        if (requestUri != null && requestUri.startsWith("/error")) {
            Object originalPath = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
            if (originalPath instanceof String original && !original.isBlank()) {
                return original;
            }
        }

        return (requestUri == null || requestUri.isBlank()) ? "/" : requestUri;
    }
}
