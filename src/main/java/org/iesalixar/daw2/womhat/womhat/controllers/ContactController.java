package org.iesalixar.daw2.womhat.womhat.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.womhat.womhat.dtos.ContactFormDTO;
import org.iesalixar.daw2.womhat.womhat.services.ContactRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

/**
 * Controlador público de contacto.
 *
 * Gestiona un formulario real de contacto para dudas técnicas,
 * soporte, catálogo, pedidos e incidencias.
 */
@Controller
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    @Autowired
    private ContactRequestService contactRequestService;

    @Autowired
    private MessageSource messageSource;

    /**
     * Muestra el formulario de contacto.
     *
     * @param reason Motivo opcional para prellenar el campo de motivo.
     * @param model  Modelo para la vista.
     * @return Nombre de la vista del formulario de contacto.
     */
    @GetMapping("/contact")
    public String showContact(@RequestParam(value = "reason", required = false) String reason,
                              Model model) {

        if (!model.containsAttribute("contactForm")) {
            ContactFormDTO dto = new ContactFormDTO();

            if (reason != null && !reason.isBlank()) {
                dto.setMotivo(reason);
            }

            model.addAttribute("contactForm", dto);
        }

        return "contact";
    }

    /**
     * Maneja el envío del formulario de contacto.
     *
     * @param dto                DTO con los datos del formulario.
     * @param result             Resultado de la validación.
     * @param model              Modelo para la vista.
     * @param redirectAttributes Atributos para redirección.
     * @param locale             Localización para mensajes.
     * @return Redirección o nombre de la vista según el resultado.
     */
    @PostMapping("/contact")
    public String handleContact(@Valid @ModelAttribute("contactForm") ContactFormDTO dto,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes,
                                Locale locale) {

        if (result.hasErrors()) {
            return "contact";
        }

        try {
            boolean delivered = contactRequestService.sendContactMessage(dto, locale);

            if (delivered) {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        messageSource.getMessage(
                                "msg.public.contact.success",
                                null,
                                "Mensaje enviado correctamente.",
                                locale
                        )
                );
            } else {
                redirectAttributes.addFlashAttribute(
                        "infoMessage",
                        messageSource.getMessage(
                                "msg.public.contact.demo",
                                null,
                                "Solicitud registrada en modo demo. No se ha enviado un correo real porque SMTP no está configurado.",
                                locale
                        )
                );
            }

            return "redirect:/contact";

        } catch (Exception ex) {
            logger.error("Error enviando formulario de contacto: {}", ex.getMessage(), ex);

            model.addAttribute(
                    "errorMessage",
                    messageSource.getMessage(
                            "msg.public.contact.error",
                            null,
                            "No se pudo enviar el mensaje. Inténtalo de nuevo más tarde.",
                            locale
                    )
            );

            return "contact";
        }
    }
}
