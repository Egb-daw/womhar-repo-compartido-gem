package org.iesalixar.daw2.womhat.womhat.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.womhat.womhat.dtos.UserProfileFormDTO;
import org.iesalixar.daw2.womhat.womhat.exceptions.InvalidFileException;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.services.UserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.security.Principal;
import java.util.Locale;

/**
 * Controlador de gestión del perfil del usuario autenticado.
 */
@Controller
@RequestMapping("/profile")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);
    private static final String PROFILE_VIEW = "views/user-profile/user-profile-form";
    private static final String ACTIVE_MENU = "profile";

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserProfileService userProfileService;

    /**
     * Muestra el formulario del perfil del usuario autenticado.
     *
     * @param model modelo para la vista
     * @param locale locale actual
     * @param principal usuario autenticado
     * @return plantilla del perfil
     */
    @GetMapping("/edit")
    public String showProfileForm(Model model, Locale locale, Principal principal) {
        String email = principal.getName();
        logger.info("Mostrando formulario de perfil para {}", email);

        model.addAttribute("active", ACTIVE_MENU);

        try {
            UserProfileFormDTO formDto = userProfileService.getFormByEmail(email);
            model.addAttribute("userProfileForm", formDto);
            model.addAttribute("accountActive", formDto.getAccountActive() == null || formDto.getAccountActive());

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el usuario para cargar el perfil: {}", ex.getMessage());
            model.addAttribute("userProfileForm", buildFallbackForm(email));
            model.addAttribute("accountActive", true);
            model.addAttribute("errorMessage", getMessage(
                    "msg.user-controller.edit.notfound",
                    locale,
                    "No se ha encontrado el usuario solicitado."
            ));

        } catch (Exception ex) {
            logger.error("Error inesperado cargando el perfil: {}", ex.getMessage(), ex);
            model.addAttribute("userProfileForm", buildFallbackForm(email));
            model.addAttribute("accountActive", true);
            model.addAttribute("errorMessage", getMessage(
                    "msg.userProfile.error",
                    locale,
                    "No se pudo cargar el perfil."
            ));
        }

        return PROFILE_VIEW;
    }

    /**
     * Actualiza el perfil del usuario autenticado.
     *
     * @param profileDto dto del formulario
     * @param result validación
     * @param profileImageFile imagen opcional
     * @param redirectAttributes mensajes flash
     * @param locale locale actual
     * @param principal usuario autenticado
     * @param model modelo para la vista en caso de error
     * @return redirect al formulario o a la misma página con mensajes de error
     */
    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("userProfileForm") UserProfileFormDTO profileDto,
                                BindingResult result,
                                @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile,
                                RedirectAttributes redirectAttributes,
                                Locale locale,
                                Principal principal,
                                Model model) {
        String email = principal.getName();
        logger.info("Actualizando perfil para {}", email);

        model.addAttribute("active", ACTIVE_MENU);
        model.addAttribute("accountActive", profileDto.getAccountActive() == null || profileDto.getAccountActive());

        if (result.hasErrors()) {
            return PROFILE_VIEW;
        }

        try {
            userProfileService.updateProfile(email, profileDto, profileImageFile);

            redirectAttributes.addFlashAttribute("successMessage", getMessage(
                    "msg.userProfile.success",
                    locale,
                    "Perfil actualizado correctamente."
            ));
            return "redirect:/profile/edit";

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se pudo actualizar el perfil: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", getMessage(
                    "msg.user-controller.edit.notfound",
                    locale,
                    "No se ha encontrado el usuario solicitado."
            ));
            return "redirect:/profile/edit";

        } catch (InvalidFileException ex) {
            logger.warn("Imagen de perfil inválida para {}: {}", email, ex.getMessage());
            String imageErrorMessage = getMessage(
                    "msg.userProfile.image.invalid",
                    locale,
                    "La imagen no es válida (tipo o tamaño)."
            );
            model.addAttribute("errorMessage", imageErrorMessage);
            model.addAttribute("profileImageError", imageErrorMessage);
            return PROFILE_VIEW;

        } catch (Exception ex) {
            logger.error("Error inesperado actualizando el perfil: {}", ex.getMessage(), ex);
            model.addAttribute("errorMessage", getMessage(
                    "msg.userProfile.error",
                    locale,
                    "No se pudo guardar el perfil."
            ));
            return PROFILE_VIEW;
        }
    }

    /**
     * Elimina la imagen de perfil del usuario autenticado.
     *
     * @param redirectAttributes mensajes flash
     * @param locale locale actual
     * @param principal usuario autenticado
     * @return redirect al formulario del perfil
     */
    @PostMapping("/delete-image")
    public String deleteProfileImage(RedirectAttributes redirectAttributes,
                                     Locale locale,
                                     Principal principal) {
        String email = principal.getName();
        logger.info("Eliminando imagen de perfil para {}", email);

        try {
            userProfileService.deleteProfileImage(email);

            redirectAttributes.addFlashAttribute("successMessage", getMessage(
                    "msg.userProfile.image.delete.success",
                    locale,
                    "La imagen de perfil se ha eliminado correctamente."
            ));

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se pudo eliminar la imagen de perfil: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", getMessage(
                    "msg.user-controller.edit.notfound",
                    locale,
                    "No se ha encontrado el usuario solicitado."
            ));

        } catch (IllegalStateException ex) {
            logger.warn("No hay imagen de perfil para eliminar para {}: {}", email, ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", getMessage(
                    "msg.userProfile.image.delete.empty",
                    locale,
                    "No hay ninguna imagen de perfil para eliminar."
            ));

        } catch (Exception ex) {
            logger.error("Error inesperado eliminando imagen de perfil: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", getMessage(
                    "msg.userProfile.image.delete.error",
                    locale,
                    "No se pudo eliminar la imagen de perfil."
            ));
        }

        return "redirect:/profile/edit";
    }

    /**
     * Desactiva la cuenta del usuario autenticado y cierra su sesión.
     */
    @PostMapping("/deactivate-account")
    public String deactivateAccount(@RequestParam("deactivationConfirmText") String deactivationConfirmText,
                                    RedirectAttributes redirectAttributes,
                                    Locale locale,
                                    Principal principal,
                                    Authentication authentication,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        String email = principal.getName();
        logger.info("Solicitando desactivación de cuenta para {}", email);

        try {
            userProfileService.deactivateOwnAccount(email, deactivationConfirmText);
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            return "redirect:/login?accountDeactivated";

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se pudo desactivar cuenta: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", getMessage(
                    "msg.user-controller.edit.notfound",
                    locale,
                    "No se ha encontrado el usuario solicitado."
            ));

        } catch (IllegalArgumentException | IllegalStateException ex) {
            logger.warn("Desactivación de cuenta rechazada para {}: {}", email, ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", getMessage(
                    ex.getMessage(),
                    locale,
                    "No se pudo desactivar la cuenta."
            ));

        } catch (Exception ex) {
            logger.error("Error inesperado desactivando cuenta para {}: {}", email, ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", getMessage(
                    "msg.userProfile.account.deactivate.error",
                    locale,
                    "No se pudo desactivar la cuenta."
            ));
        }

        return "redirect:/profile/edit";
    }

    /**
     * Crea un DTO para evitar que la vista falle si no se puede cargar el perfil.
     *
     * @param email email autenticado
     * @return dto de respaldo
     */
    private UserProfileFormDTO buildFallbackForm(String email) {
        UserProfileFormDTO dto = new UserProfileFormDTO();
        dto.setEmail(email);
        dto.setAccountActive(true);
        return dto;
    }

    /**
     * Recupera un mensaje traducido con fallback.
     *
     * @param key clave i18n
     * @param locale locale actual
     * @param defaultMessage texto por defecto
     * @return mensaje resuelto
     */
    private String getMessage(String key, Locale locale, String defaultMessage) {
        return messageSource.getMessage(key, null, defaultMessage, locale);
    }
}
