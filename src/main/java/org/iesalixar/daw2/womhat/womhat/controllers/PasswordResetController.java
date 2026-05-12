package org.iesalixar.daw2.womhat.womhat.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.iesalixar.daw2.womhat.womhat.dtos.PasswordResetDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.PasswordResetRequestDTO;
import org.iesalixar.daw2.womhat.womhat.services.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

/**
 * Controlador para el flujo de “Olvidé mi contraseña” y restablecimiento mediante token.
 *
 * Buenas prácticas aplicadas:
 * - no se revela si el email existe,
 * - token de un solo uso con caducidad,
 * - mensajes internacionalizados.
 */
@Controller
@RequestMapping("/auth")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private MessageSource messageSource;

    /**
     * Muestra el formulario donde el usuario introduce su email
     * para solicitar el restablecimiento de contraseña.
     *
     * @param model modelo para la vista
     * @return plantilla forgot-password
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("dto", new PasswordResetRequestDTO());
        return "views/reset-password/forgot-password";
    }

    /**
     * Procesa la solicitud de recuperación.
     *
     * La respuesta es siempre genérica para no exponer si el email existe o no.
     *
     * @param dto datos del formulario
     * @param result resultado de validación
     * @param request request actual
     * @param redirectAttributes mensajes flash
     * @return redirección al formulario
     */
    @PostMapping("/forgot")
    public String handleForgotPassword(
            @Valid @ModelAttribute("dto") PasswordResetRequestDTO dto,
            BindingResult result,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "views/reset-password/forgot-password";
        }

        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        Locale locale = LocaleContextHolder.getLocale();

        try {
            passwordResetService.requestPasswordReset(dto.getEmail(), ip, userAgent);

            String msg = messageSource.getMessage("msg.password-reset.request.sent", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", msg);

        } catch (Exception ex) {
            String msg = messageSource.getMessage(
                    "msg.password-reset.request.error",
                    null,
                    "No se pudo procesar la solicitud ahora mismo. Inténtalo de nuevo en unos segundos.",
                    locale
            );
            redirectAttributes.addFlashAttribute("errorMessage", msg);
        }

        return "redirect:/auth/forgot-password";
    }

    /**
     * Muestra el formulario final de cambio de contraseña a partir del token.
     *
     * @param token token recibido por query param
     * @param model modelo para la vista
     * @return plantilla reset-password
     */
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        PasswordResetDTO dto = new PasswordResetDTO();
        dto.setToken(token);
        model.addAttribute("dto", dto);
        return "views/reset-password/reset-password";
    }

    /**
     * Procesa el cambio de contraseña.
     *
     * @param dto token + nueva contraseña + confirmación
     * @param result resultado de validación
     * @param redirectAttributes mensajes flash
     * @return redirect a login si todo va bien
     */
    @PostMapping("/reset-password")
    public String handleResetPassword(
            @Valid @ModelAttribute("dto") PasswordResetDTO dto,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (dto.getNewPassword() != null && dto.getConfirmPassword() != null
                && !dto.getNewPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "password.mismatch");
        }

        if (result.hasErrors()) {
            return "views/reset-password/reset-password";
        }

        Locale locale = LocaleContextHolder.getLocale();

        try {
            passwordResetService.resetPassword(dto.getToken(), dto.getNewPassword());

            String msg = messageSource.getMessage("msg.password-reset.success", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", msg);
            return "redirect:/login";

        } catch (IllegalArgumentException ex) {
            String msg = messageSource.getMessage("msg.password-reset.invalid", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/auth/forgot-password";

        } catch (Exception ex) {
            String msg = messageSource.getMessage(
                    "msg.password-reset.request.error",
                    null,
                    "No se pudo completar el cambio de contraseña ahora mismo.",
                    locale
            );
            redirectAttributes.addFlashAttribute("errorMessage", msg);
            return "redirect:/auth/forgot-password";
        }
    }
}