package org.iesalixar.daw2.womhat.womhat.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentEventLogDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentFormDTO;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentStatus;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentType;
import org.iesalixar.daw2.womhat.womhat.enums.NetworkConnectionType;
import org.iesalixar.daw2.womhat.womhat.enums.RackPermission;
import org.iesalixar.daw2.womhat.womhat.enums.StorageType;
import org.iesalixar.daw2.womhat.womhat.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.services.AppUrlService;
import org.iesalixar.daw2.womhat.womhat.services.EquipmentService;
import org.iesalixar.daw2.womhat.womhat.services.QrCodeService;
import org.iesalixar.daw2.womhat.womhat.services.RackService;
import org.iesalixar.daw2.womhat.womhat.services.UserRackAccessService;
import org.springframework.security.access.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;

/**
 * Controlador MVC del módulo de equipos.
 */
@Controller
@RequestMapping("/equipment")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class EquipmentController {

    private static final Logger logger = LoggerFactory.getLogger(EquipmentController.class);
    private static final String ACTIVE_MENU = "equipment";

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private RackService rackService;

    @Autowired
    private UserRackAccessService userRackAccessService;

    @Autowired
    private QrCodeService qrCodeService;

    @Autowired
    private AppUrlService appUrlService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String listEquipment(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "includeClosed", defaultValue = "false") boolean includeClosed,
            Model model,
            Principal principal,
            Locale locale) {

        logger.info("Listando equipos page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        try {
            String currentUserEmail = principal != null ? principal.getName() : null;
            Page<EquipmentDTO> page = equipmentService.list(pageable, currentUserEmail, includeClosed);

            model.addAttribute("page", page);
            model.addAttribute("sortParam", resolveSortParam(page));
            model.addAttribute("includeClosed", includeClosed);
            model.addAttribute("equipmentStatusLabels", buildEquipmentStatusLabels(locale));
            model.addAttribute("equipmentTypeLabels", buildEquipmentTypeLabels(locale));
            model.addAttribute("active", ACTIVE_MENU);

        } catch (Exception ex) {
            logger.error("Error al listar equipos: {}", ex.getMessage(), ex);
            model.addAttribute("errorMessage", msg("msg.equipment.feedback.list.error", locale, "No se pudieron cargar los equipos."));
            model.addAttribute("includeClosed", includeClosed);
            model.addAttribute("equipmentStatusLabels", buildEquipmentStatusLabels(locale));
            model.addAttribute("equipmentTypeLabels", buildEquipmentTypeLabels(locale));
            model.addAttribute("active", ACTIVE_MENU);
        }

        return "views/equipment/equipment-list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public String showNewForm(@RequestParam(value = "rackId", required = false) Long rackId,
                              Model model,
                              Principal principal,
                              Locale locale) {
        logger.info("Mostrando formulario de alta de equipo.");

        String currentUserEmail = principal != null ? principal.getName() : null;
        boolean admin = userRackAccessService.hasGlobalAdminAccess(currentUserEmail);
        if (!admin && (rackId == null || !userRackAccessService.canWriteRack(currentUserEmail, rackId))) {
            throw new AccessDeniedException(msg("msg.equipment.feedback.access.denied", locale, "No tienes permisos para gestionar equipos en este rack."));
        }

        EquipmentFormDTO form = new EquipmentFormDTO();
        form.setRackId(rackId);
        model.addAttribute("equipment", form);
        loadFormCatalogs(model, currentUserEmail, admin, locale);
        return "views/equipment/equipment-form";
    }

    @GetMapping("/edit")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public String showEditForm(@RequestParam("id") Long id,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Principal principal,
                               Locale locale) {

        logger.info("Mostrando formulario de edición de equipo id={}", id);

        try {
            EquipmentFormDTO formDTO = equipmentService.getForm(id);
            String currentUserEmail = principal != null ? principal.getName() : null;
            boolean admin = userRackAccessService.hasGlobalAdminAccess(currentUserEmail);
            if (!admin && !userRackAccessService.canWriteRack(currentUserEmail, formDTO.getRackId())) {
                throw new AccessDeniedException(msg("msg.equipment.feedback.access.denied", locale, "No tienes permisos para editar este equipo."));
            }

            model.addAttribute("equipment", formDTO);
            loadFormCatalogs(model, currentUserEmail, admin, locale);
            return "views/equipment/equipment-form";

        } catch (AccessDeniedException ex) {
            logger.warn("Acceso denegado al formulario de equipo id={}: {}", id, ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.access.denied", locale, "No tienes permisos para editar este equipo."));
            return "redirect:/equipment";

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el equipo id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.notFound", locale, "No se encontró el equipo solicitado."));
            return "redirect:/equipment";

        } catch (Exception ex) {
            logger.error("Error al cargar el formulario del equipo: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.form.error", locale, "No se pudo cargar el formulario del equipo."));
            return "redirect:/equipment";
        }
    }

    @PostMapping("/insert")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public String insertEquipment(@Valid @ModelAttribute("equipment") EquipmentFormDTO equipment,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes,
                                  Principal principal,
                                  Locale locale) {

        logger.info("Insertando equipo serialNumber={}", equipment.getSerialNumber());

        try {
            String actorEmail = principal != null ? principal.getName() : null;
            boolean admin = userRackAccessService.hasGlobalAdminAccess(actorEmail);

            if (result.hasErrors()) {
                loadFormCatalogs(model, actorEmail, admin, locale);
                return "views/equipment/equipment-form";
            }

            if (!admin && !userRackAccessService.canWriteRack(actorEmail, equipment.getRackId())) {
                throw new AccessDeniedException(msg("msg.equipment.feedback.access.denied", locale, "No tienes permisos para crear equipos en este rack."));
            }

            equipmentService.create(equipment, actorEmail);

            redirectAttributes.addFlashAttribute("successMessage", msg("msg.equipment.feedback.create.success", locale, "Equipo creado correctamente."));
            return "redirect:/equipment";

        } catch (DuplicateResourceException ex) {
            logger.warn("Número de serie duplicado: {}", equipment.getSerialNumber());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.create.duplicate", locale, "Ya existe un equipo con ese número de serie."));
            return "redirect:/equipment/new";

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el rack del equipo: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.create.rackNotFound", locale, "No se encontró el rack seleccionado."));
            return "redirect:/equipment/new";

        } catch (AccessDeniedException ex) {
            logger.warn("Acceso denegado al crear equipo: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.access.denied", locale, "No tienes permisos para crear equipos en este rack."));
            return "redirect:/equipment";

        } catch (Exception ex) {
            logger.error("Error al crear el equipo: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.create.error", locale, "No se pudo crear el equipo."));
            return "redirect:/equipment/new";
        }
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public String updateEquipment(@Valid @ModelAttribute("equipment") EquipmentFormDTO equipment,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes,
                                  Principal principal,
                                  Locale locale) {

        logger.info("Actualizando equipo id={}", equipment.getId());

        try {
            String actorEmail = principal != null ? principal.getName() : null;
            boolean admin = userRackAccessService.hasGlobalAdminAccess(actorEmail);

            if (result.hasErrors()) {
                loadFormCatalogs(model, actorEmail, admin, locale);
                return "views/equipment/equipment-form";
            }

            EquipmentFormDTO currentForm = equipmentService.getForm(equipment.getId());
            boolean canWriteCurrentRack = userRackAccessService.canWriteRack(actorEmail, currentForm.getRackId());
            boolean canWriteTargetRack = userRackAccessService.canWriteRack(actorEmail, equipment.getRackId());
            if (!admin && (!canWriteCurrentRack || !canWriteTargetRack)) {
                throw new AccessDeniedException(msg("msg.equipment.feedback.access.denied", locale, "No tienes permisos para editar este equipo."));
            }

            equipmentService.update(equipment, actorEmail);

            redirectAttributes.addFlashAttribute("successMessage", msg("msg.equipment.feedback.update.success", locale, "Equipo actualizado correctamente."));
            return "redirect:/equipment";

        } catch (DuplicateResourceException ex) {
            logger.warn("Número de serie duplicado al actualizar: {}", equipment.getSerialNumber());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.update.duplicate", locale, "Ya existe otro equipo con ese número de serie."));
            return "redirect:/equipment/edit?id=" + equipment.getId();

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el equipo o el rack asociado: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.update.notFound", locale, "No se encontró el equipo o el rack seleccionado."));
            return "redirect:/equipment";

        } catch (AccessDeniedException ex) {
            logger.warn("Acceso denegado al actualizar equipo id={}: {}", equipment.getId(), ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.access.denied", locale, "No tienes permisos para editar este equipo."));
            return "redirect:/equipment";

        } catch (Exception ex) {
            logger.error("Error al actualizar el equipo: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.update.error", locale, "No se pudo actualizar el equipo."));
            return "redirect:/equipment/edit?id=" + equipment.getId();
        }
    }

    @PostMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public String changeEquipmentStatus(@RequestParam("id") Long id,
                                        @RequestParam("status") EquipmentStatus status,
                                        RedirectAttributes redirectAttributes,
                                        Principal principal,
                                        Locale locale) {

        logger.info("Solicitado cambio de estado de equipo id={} a {}", id, status);

        try {
            String actorEmail = principal != null ? principal.getName() : null;
            equipmentService.changeStatus(id, status, actorEmail);
            redirectAttributes.addFlashAttribute("successMessage", msg("msg.equipment.lifecycle.feedback.success", locale, "Estado del equipo actualizado correctamente."));

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el equipo id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.notFound", locale, "No se encontró el equipo solicitado."));
            return "redirect:/equipment";

        } catch (AccessDeniedException ex) {
            logger.warn("Acceso denegado al cambiar estado de equipo id={}: {}", id, ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.lifecycle.feedback.denied", locale, "No tienes permisos para cambiar el estado de este equipo."));

        } catch (IllegalArgumentException ex) {
            logger.warn("Estado inválido para equipo id={}: {}", id, ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.lifecycle.feedback.invalid", locale, "Estado de equipo no válido."));

        } catch (Exception ex) {
            logger.error("Error cambiando estado de equipo id={}: {}", id, ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.lifecycle.feedback.error", locale, "No se pudo cambiar el estado del equipo."));
        }

        return "redirect:/equipment/detail?id=" + id;
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteEquipment(@RequestParam("id") Long id,
                                  RedirectAttributes redirectAttributes,
                                  Principal principal,
                                  Locale locale) {

        logger.info("Eliminando equipo id={}", id);

        try {
            String actorEmail = principal != null ? principal.getName() : null;
            equipmentService.delete(id, actorEmail);

            redirectAttributes.addFlashAttribute("successMessage", msg("msg.equipment.feedback.delete.success", locale, "Equipo eliminado correctamente."));

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el equipo id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.notFound", locale, "No se encontró el equipo solicitado."));

        } catch (Exception ex) {
            logger.error("Error al eliminar el equipo id={}: {}", id, ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.delete.error", locale, "No se pudo eliminar el equipo."));
        }

        return "redirect:/equipment";
    }

    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Principal principal,
                             Locale locale) {

        logger.info("Mostrando detalle de equipo id={}", id);

        try {
            String currentUserEmail = principal != null ? principal.getName() : null;
            EquipmentDetailDTO detailDTO = equipmentService.getDetail(id, currentUserEmail);
            List<EquipmentEventLogDTO> eventLog = equipmentService.getEventLog(id);
            boolean canWriteEquipment = userRackAccessService.canWriteRack(currentUserEmail, detailDTO.getRackId());

            model.addAttribute("equipment", detailDTO);
            model.addAttribute("eventLog", eventLog);
            model.addAttribute("equipmentHistoryLimited", false);
            model.addAttribute("equipmentMobileEntryUrl", buildEquipmentDetailUrl(id));
            model.addAttribute("currentRackPermission", userRackAccessService.resolvePermission(currentUserEmail, detailDTO.getRackId()));
            model.addAttribute("canWriteEquipment", canWriteEquipment);
            model.addAttribute("canManageEquipmentRack", userRackAccessService.canManageRackAccess(currentUserEmail, detailDTO.getRackId()));
            model.addAttribute("permissionLabels", buildPermissionLabels(locale));
            model.addAttribute("equipmentStatusLabels", buildEquipmentStatusLabels(locale));
            model.addAttribute("availableLifecycleStatuses", EquipmentStatus.values());
            model.addAttribute("equipmentTypeLabels", buildEquipmentTypeLabels(locale));
            model.addAttribute("equipmentActionLabels", buildEquipmentActionLabels(locale));
            model.addAttribute("equipmentEventTypeLabels", buildEquipmentEventTypeLabels(locale));
            model.addAttribute("active", ACTIVE_MENU);
            return "views/equipment/equipment-detail";

        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.notFound", locale, "No se encontró el equipo solicitado."));
            return "redirect:/equipment";

        } catch (AccessDeniedException ex) {
            throw ex;

        } catch (Exception ex) {
            logger.error("Error al cargar el detalle del equipo: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.detail.error", locale, "No se pudo cargar el detalle del equipo."));
            return "redirect:/equipment";
        }
    }

    @GetMapping("/qr-card")
    public String showQrCard(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Principal principal,
                             Locale locale) {

        logger.info("Mostrando tarjeta QR del equipo id={}", id);

        try {
            String currentUserEmail = principal != null ? principal.getName() : null;
            EquipmentDetailDTO detailDTO = equipmentService.getDetail(id, currentUserEmail);
            model.addAttribute("equipment", detailDTO);
            model.addAttribute("equipmentStatusLabels", buildEquipmentStatusLabels(locale));
            model.addAttribute("equipmentMobileEntryUrl", buildEquipmentDetailUrl(id));
            model.addAttribute("generatedAt", LocalDateTime.now());
            return "views/equipment/equipment-qr-card";

        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.feedback.notFound", locale, "No se encontró el equipo solicitado."));
            return "redirect:/equipment";

        } catch (AccessDeniedException ex) {
            throw ex;

        } catch (Exception ex) {
            logger.error("Error al cargar la tarjeta QR del equipo: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.equipment.detail.qr.feedback.error", locale, "No se pudo generar la tarjeta QR del equipo."));
            return "redirect:/equipment/detail?id=" + id;
        }
    }

    @GetMapping(value = "/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> showQrImage(@RequestParam("id") Long id,
                                              Principal principal,
                                              Locale locale) {

        logger.info("Generando QR del equipo id={}", id);

        if (principal == null) {
            return ResponseEntity.status(403).build();
        }

        try {
            equipmentService.getDetail(id, principal.getName());

            byte[] image = qrCodeService.generatePng(buildEquipmentDetailUrl(id), 320, 320);
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .contentType(MediaType.IMAGE_PNG)
                    .body(image);

        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.notFound().build();

        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(403).build();

        } catch (Exception ex) {
            logger.error("Error al generar el QR del equipo {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }

    private void loadFormCatalogs(Model model, String currentUserEmail, boolean admin, Locale locale) {
        model.addAttribute("racks", admin ? rackService.listOptions() : userRackAccessService.listWritableRackOptions(currentUserEmail));
        model.addAttribute("equipmentTypes", EquipmentType.values());
        model.addAttribute("equipmentStatuses", EquipmentStatus.values());
        model.addAttribute("equipmentStatusLabels", buildEquipmentStatusLabels(locale));
        model.addAttribute("networkConnectionTypes", NetworkConnectionType.values());
        model.addAttribute("storageTypes", StorageType.values());
        model.addAttribute("active", ACTIVE_MENU);
    }

    private String resolveSortParam(Page<?> page) {
        String sortParam = "name,asc";

        if (page.getSort().isSorted()) {
            Sort.Order order = page.getSort().iterator().next();
            sortParam = order.getProperty() + "," + order.getDirection().name().toLowerCase();
        }

        return sortParam;
    }

    private String buildEquipmentDetailUrl(Long equipmentId) {
        return appUrlService.buildUrl("/equipment/detail", Map.of("id", String.valueOf(equipmentId)));
    }

    private Map<RackPermission, String> buildPermissionLabels(Locale locale) {
        Map<RackPermission, String> labels = new EnumMap<>(RackPermission.class);
        for (RackPermission permission : RackPermission.values()) {
            labels.put(permission, msg("msg.user-detail.rackAccess.permission." + permission.name().toLowerCase(), locale, permission.name()));
        }
        return labels;
    }

    private Map<EquipmentStatus, String> buildEquipmentStatusLabels(Locale locale) {
        Map<EquipmentStatus, String> labels = new EnumMap<>(EquipmentStatus.class);
        for (EquipmentStatus status : EquipmentStatus.values()) {
            labels.put(status, msg("msg.equipment.status." + status.name().toLowerCase(), locale, status.name()));
        }
        return labels;
    }

    private Map<EquipmentType, String> buildEquipmentTypeLabels(Locale locale) {
        Map<EquipmentType, String> labels = new EnumMap<>(EquipmentType.class);
        for (EquipmentType type : EquipmentType.values()) {
            labels.put(type, msg("msg.equipment.type." + type.name().toLowerCase(), locale, type.name()));
        }
        return labels;
    }

    private Map<org.iesalixar.daw2.womhat.womhat.enums.EquipmentLogAction, String> buildEquipmentActionLabels(Locale locale) {
        Map<org.iesalixar.daw2.womhat.womhat.enums.EquipmentLogAction, String> labels =
                new EnumMap<>(org.iesalixar.daw2.womhat.womhat.enums.EquipmentLogAction.class);
        for (org.iesalixar.daw2.womhat.womhat.enums.EquipmentLogAction action : org.iesalixar.daw2.womhat.womhat.enums.EquipmentLogAction.values()) {
            labels.put(action, msg("msg.equipment.eventLog.action." + action.name().toLowerCase(), locale, action.name()));
        }
        return labels;
    }

    private Map<org.iesalixar.daw2.womhat.womhat.enums.EquipmentEventType, String> buildEquipmentEventTypeLabels(Locale locale) {
        Map<org.iesalixar.daw2.womhat.womhat.enums.EquipmentEventType, String> labels =
                new EnumMap<>(org.iesalixar.daw2.womhat.womhat.enums.EquipmentEventType.class);
        for (org.iesalixar.daw2.womhat.womhat.enums.EquipmentEventType eventType : org.iesalixar.daw2.womhat.womhat.enums.EquipmentEventType.values()) {
            labels.put(eventType, msg("msg.equipment.eventLog.type." + eventType.name().toLowerCase(), locale, eventType.name()));
        }
        return labels;
    }

    private String msg(String key, Locale locale, String fallback, Object... args) {
        return messageSource.getMessage(key, args, fallback, locale);
    }
}
