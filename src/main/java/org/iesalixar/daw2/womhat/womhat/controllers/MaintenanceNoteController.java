package org.iesalixar.daw2.womhat.womhat.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.womhat.womhat.dtos.MaintenanceNoteFormDTO;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.services.MaintenanceNoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Locale;

/**
 * Controlador ligero para crear y borrar notas de mantenimiento.
 *
 * Las notas no se gestionan con vistas propias, sino desde el detalle
 * de la orden de trabajo.
 */
@Controller
@RequestMapping("/maintenance-notes")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class MaintenanceNoteController {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceNoteController.class);

    @Autowired
    private MaintenanceNoteService maintenanceNoteService;

    @Autowired
    private MessageSource messageSource;

    /**
     * Inserta una nueva nota en una orden de mantenimiento.
     *
     * @param maintenanceNote dto del formulario
     * @param result validación
     * @param redirectAttributes mensajes flash
     * @param principal usuario autenticado
     * @return redirect al detalle de la orden
     */
    @PostMapping("/insert")
    public String insertNote(@Valid @ModelAttribute("maintenanceNote") MaintenanceNoteFormDTO maintenanceNote,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Principal principal,
                             Locale locale) {

        logger.info("Insertando nota de mantenimiento para workOrderId={}", maintenanceNote.getWorkOrderId());

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.maintenanceNote", result);
            redirectAttributes.addFlashAttribute("maintenanceNote", maintenanceNote);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.workOrder.note.feedback.invalid", locale, "La nota no es válida."));
            return "redirect:/maintenance/work-orders/detail?id=" + maintenanceNote.getWorkOrderId();
        }

        try {
            String actorEmail = principal != null ? principal.getName() : null;
            maintenanceNoteService.create(maintenanceNote, actorEmail);

            redirectAttributes.addFlashAttribute("successMessage", msg("msg.workOrder.note.feedback.create.success", locale, "Nota añadida correctamente."));

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró la orden de mantenimiento: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.workOrder.note.feedback.create.notFound", locale, "No se encontró la orden de mantenimiento."));

        } catch (AccessDeniedException ex) {
            throw ex;

        } catch (Exception ex) {
            logger.error("Error al crear la nota: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.workOrder.note.feedback.create.error", locale, "No se pudo guardar la nota."));
        }

        return "redirect:/maintenance/work-orders/detail?id=" + maintenanceNote.getWorkOrderId();
    }

    /**
     * Elimina una nota de mantenimiento.
     *
     * @param id id de la nota
     * @param workOrderId id de la orden a la que pertenece
     * @param redirectAttributes mensajes flash
     * @return redirect al detalle de la orden
     */
    @PostMapping("/delete")
    public String deleteNote(@RequestParam("id") Long id,
                             @RequestParam("workOrderId") Long workOrderId,
                             RedirectAttributes redirectAttributes,
                             Principal principal,
                             Locale locale) {

        logger.info("Eliminando nota de mantenimiento id={} de workOrderId={}", id, workOrderId);

        try {
            String actorEmail = principal != null ? principal.getName() : null;
            maintenanceNoteService.delete(id, actorEmail);
            redirectAttributes.addFlashAttribute("successMessage", msg("msg.workOrder.note.feedback.delete.success", locale, "Nota eliminada correctamente."));

        } catch (AccessDeniedException ex) {
            throw ex;

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró la nota id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.workOrder.note.feedback.delete.notFound", locale, "No se encontró la nota de mantenimiento."));

        } catch (Exception ex) {
            logger.error("Error al eliminar la nota id={}: {}", id, ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.workOrder.note.feedback.delete.error", locale, "No se pudo eliminar la nota."));
        }

        return "redirect:/maintenance/work-orders/detail?id=" + workOrderId;
    }

    private String msg(String key, Locale locale, String fallback, Object... args) {
        return messageSource.getMessage(key, args, fallback, locale);
    }
}
