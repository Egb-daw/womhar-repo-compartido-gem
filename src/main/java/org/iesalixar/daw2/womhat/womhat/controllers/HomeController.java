package org.iesalixar.daw2.womhat.womhat.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.time.OffsetDateTime;

/**
 * Controlador de entrada principal de la aplicación.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about_us";
    }

    @GetMapping("/error/403")
    public String accessDenied(HttpServletRequest request, Model model) {
        Object forwardedPath = request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);

        if (forwardedPath instanceof String originalPath && !originalPath.isBlank()) {
            model.addAttribute("path", originalPath);
        }

        model.addAttribute("timestamp", OffsetDateTime.now());
        return "error/403";
    }

    /**
     * Ruta directa para visualizar la página 404.
     *
     * <p>No sustituye el manejo automático de errores de Spring Boot; solo evita que
     * `/error/404` responda con 404 por falta de mapping explícito.</p>
     */
    @GetMapping("/error/404")
    public String notFound(HttpServletRequest request, Model model) {
        Object forwardedPath = request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);

        if (forwardedPath instanceof String originalPath && !originalPath.isBlank()) {
            model.addAttribute("path", originalPath);
        }

        model.addAttribute("timestamp", OffsetDateTime.now());
        return "error/404";
    }

    /**
     * Ruta directa para visualizar la página 500.
     *
     * <p>El manejo automático de errores reales (dispatch a `/error`) sigue en manos
     * de Spring Boot. Este endpoint solo evita un 404 cuando se abre `/error/500`
     * explícitamente.</p>
     */
    @GetMapping("/error/500")
    public String internalServerError(HttpServletRequest request, Model model) {
        Object forwardedPath = request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);

        if (forwardedPath instanceof String originalPath && !originalPath.isBlank()) {
            model.addAttribute("path", originalPath);
        }

        model.addAttribute("status", 500);
        model.addAttribute("timestamp", OffsetDateTime.now());
        return "error/500";
    }
}
