package org.iesalixar.daw2.womhat.womhat.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.womhat.womhat.dtos.PublicRegisterDTO;
import org.iesalixar.daw2.womhat.womhat.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.womhat.womhat.services.PublicRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Locale;

/**
 * Registro público real integrado con base de datos.
 */
@Controller
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @Autowired
    private PublicRegistrationService publicRegistrationService;

    @Autowired
    private MessageSource messageSource;

    /**
     * Muestra la pantalla de registro.
     *
     * @param model modelo
     * @return vista de registro
     */
    @GetMapping("/register")
    public String showRegister(Model model,
                               Principal principal) {
        if (principal != null) {
            return "redirect:/dashboard";
        }

        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new PublicRegisterDTO());
        }

        return "views/register/register";
    }

    /**
     * Maneja el envío del formulario de registro.
     *
     * @param dto                datos del formulario
     * @param result             resultado de validación
     * @param model              modelo para la vista
     * @param redirectAttributes atributos para redirección
     * @param principal          usuario autenticado (si existe)
     * @return redirección o vista de registro con errores
     */

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registerForm") PublicRegisterDTO dto,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {

        Locale locale = LocaleContextHolder.getLocale();

        if (principal != null) {
            return "redirect:/dashboard";
        }

        if (dto.getPassword() != null
                && dto.getConfirmPassword() != null
                && !dto.getPassword().equals(dto.getConfirmPassword())) {

            result.rejectValue(
                    "confirmPassword",
                    "password.mismatch",
                    messageSource.getMessage(
                            "msg.public.register.confirmPassword.mismatch",
                            null,
                            "Las contraseñas no coinciden.",
                            locale
                    )
            );
        }

        if (result.hasErrors()) {
            return "views/register/register";
        }

        try {
            publicRegistrationService.register(dto);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    messageSource.getMessage("msg.public.register.success", null, locale)
            );

            return "redirect:/login";

        } catch (DuplicateResourceException ex) {
            logger.warn("Intento de registro con email duplicado {}", dto.getEmail());

            result.rejectValue(
                    "email",
                    "duplicate",
                    messageSource.getMessage("msg.public.register.duplicate", null, locale)
            );

            return "views/register/register";

        } catch (Exception ex) {
            logger.error("Error registrando usuario público: {}", ex.getMessage(), ex);

            model.addAttribute(
                    "errorMessage",
                    messageSource.getMessage("msg.public.register.error", null, locale)
            );

            return "views/register/register";
        }
    }
}
