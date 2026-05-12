package org.iesalixar.daw2.womhat.womhat.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentStatus;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentType;
import org.iesalixar.daw2.womhat.womhat.dtos.RackDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackFormDTO;
import org.iesalixar.daw2.womhat.womhat.enums.RackPermission;
import org.iesalixar.daw2.womhat.womhat.enums.RackStatus;
import org.iesalixar.daw2.womhat.womhat.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.services.DataCenterRoomService;
import org.iesalixar.daw2.womhat.womhat.services.RackService;
import org.iesalixar.daw2.womhat.womhat.services.UserRackAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Controlador MVC del módulo de racks.
 */
@Controller
@RequestMapping("/racks")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class RackController {

    private static final Logger logger = LoggerFactory.getLogger(RackController.class);
    private static final String ACTIVE_MENU = "racks";

    @Autowired
    private RackService rackService;

    @Autowired
    private DataCenterRoomService dataCenterRoomService;

    @Autowired
    private UserRackAccessService userRackAccessService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String listRacks(
            @PageableDefault(size = 10, sort = "locationLabel", direction = Sort.Direction.ASC) Pageable pageable,
            Model model,
            Principal principal,
            Locale locale) {

        logger.info("Listando racks page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        try {
            String currentUserEmail = principal != null ? principal.getName() : null;
            Page<RackDTO> page = rackService.list(pageable, currentUserEmail);

            model.addAttribute("page", page);
            model.addAttribute("sortParam", resolveSortParam(page));
            model.addAttribute("active", ACTIVE_MENU);

        } catch (Exception ex) {
            logger.error("Error al listar racks: {}", ex.getMessage(), ex);
            model.addAttribute("errorMessage", msg("msg.rack.feedback.list.error", locale, "No se pudieron cargar los racks."));
            model.addAttribute("active", ACTIVE_MENU);
        }

        return "views/rack/rack-list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario de alta de rack.");

        model.addAttribute("rack", new RackFormDTO());
        loadFormCatalogs(model);
        return "views/rack/rack-form";
    }

    @GetMapping("/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@RequestParam("id") Long id,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {

        logger.info("Mostrando formulario de edición de rack id={}", id);

        try {
            RackFormDTO formDTO = rackService.getForm(id);

            model.addAttribute("rack", formDTO);
            loadFormCatalogs(model);
            return "views/rack/rack-form";

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el rack id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.notFound", locale, "No se encontró el rack solicitado."));
            return "redirect:/racks";

        } catch (Exception ex) {
            logger.error("Error al cargar el formulario del rack: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.form.error", locale, "No se pudo cargar el formulario del rack."));
            return "redirect:/racks";
        }
    }

    @PostMapping("/insert")
    @PreAuthorize("hasRole('ADMIN')")
    public String insertRack(@Valid @ModelAttribute("rack") RackFormDTO rack,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        logger.info("Insertando rack locationLabel={}", rack.getLocationLabel());

        try {
            if (result.hasErrors()) {
                loadFormCatalogs(model);
                return "views/rack/rack-form";
            }

            rackService.create(rack);
            redirectAttributes.addFlashAttribute("successMessage", msg("msg.rack.feedback.create.success", locale, "Rack creado correctamente."));
            return "redirect:/racks";

        } catch (DuplicateResourceException ex) {
            logger.warn("Etiqueta de rack duplicada: {}", rack.getLocationLabel());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.create.duplicate", locale, "Ya existe un rack con esa etiqueta."));
            return "redirect:/racks/new";

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró la sala del rack: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.create.roomNotFound", locale, "No se encontró la sala seleccionada."));
            return "redirect:/racks/new";

        } catch (Exception ex) {
            logger.error("Error al crear el rack: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.create.error", locale, "No se pudo crear el rack."));
            return "redirect:/racks/new";
        }
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateRack(@Valid @ModelAttribute("rack") RackFormDTO rack,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        logger.info("Actualizando rack id={}", rack.getId());

        try {
            if (result.hasErrors()) {
                loadFormCatalogs(model);
                return "views/rack/rack-form";
            }

            rackService.update(rack);
            redirectAttributes.addFlashAttribute("successMessage", msg("msg.rack.feedback.update.success", locale, "Rack actualizado correctamente."));
            return "redirect:/racks";

        } catch (DuplicateResourceException ex) {
            logger.warn("Etiqueta duplicada al actualizar rack: {}", rack.getLocationLabel());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.update.duplicate", locale, "Ya existe otro rack con esa etiqueta."));
            return "redirect:/racks/edit?id=" + rack.getId();

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró rack o sala asociada: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.update.notFound", locale, "No se encontró el rack o la sala seleccionada."));
            return "redirect:/racks";

        } catch (Exception ex) {
            logger.error("Error al actualizar el rack: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.update.error", locale, "No se pudo actualizar el rack."));
            return "redirect:/racks/edit?id=" + rack.getId();
        }
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteRack(@RequestParam("id") Long id,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        logger.info("Eliminando rack id={}", id);

        try {
            rackService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", msg("msg.rack.feedback.delete.success", locale, "Rack eliminado correctamente."));

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el rack id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.notFound", locale, "No se encontró el rack solicitado."));

        } catch (Exception ex) {
            logger.error("Error al eliminar el rack id={}: {}", id, ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.delete.error", locale, "No se pudo eliminar el rack."));
        }

        return "redirect:/racks";
    }

    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Principal principal,
                             Locale locale) {

        logger.info("Mostrando detalle de rack id={}", id);

        try {
            String currentUserEmail = principal != null ? principal.getName() : null;
            RackDetailDTO detailDTO = rackService.getDetail(id, currentUserEmail);
            RackPermission currentRackPermission = userRackAccessService.resolvePermission(currentUserEmail, id);

            model.addAttribute("rack", detailDTO);
            model.addAttribute("permissions", RackPermission.values());
            model.addAttribute("permissionLabels", buildPermissionLabels(locale));
            model.addAttribute("currentRackPermission", currentRackPermission);
            model.addAttribute("canWriteRack", userRackAccessService.canWriteRack(currentUserEmail, id));
            model.addAttribute("canManageRackAccess", userRackAccessService.canManageRackAccess(currentUserEmail, id));
            model.addAttribute("originalRackOwner", userRackAccessService.findOriginalOwner(id));
            model.addAttribute("currentUserOriginalOwner", userRackAccessService.isOriginalOwner(currentUserEmail, id));
            model.addAttribute("rackStatusLabels", buildRackStatusLabels(locale));
            model.addAttribute("equipmentTypeLabels", buildEquipmentTypeLabels(locale));
            model.addAttribute("equipmentStatusLabels", buildEquipmentStatusLabels(locale));
            model.addAttribute("active", ACTIVE_MENU);
            return "views/rack/rack-detail";

        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.notFound", locale, "No se encontró el rack solicitado."));
            return "redirect:/racks";

        } catch (AccessDeniedException ex) {
            throw ex;

        } catch (Exception ex) {
            logger.error("Error al cargar el detalle del rack: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.detail.error", locale, "No se pudo cargar el detalle del rack."));
            return "redirect:/racks";
        }
    }

    @GetMapping("/report")
    public String showReport(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Principal principal,
                             Locale locale) {

        logger.info("Generando reporte imprimible de rack id={}", id);

        try {
            String currentUserEmail = principal != null ? principal.getName() : null;
            RackDetailDTO detailDTO = rackService.getDetail(id, currentUserEmail);

            model.addAttribute("rack", detailDTO);
            model.addAttribute("rackStatusLabels", buildRackStatusLabels(locale));
            model.addAttribute("equipmentStatusLabels", buildEquipmentStatusLabels(locale));
            model.addAttribute("generatedAt", LocalDateTime.now());
            return "views/rack/rack-report";

        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.notFound", locale, "No se encontró el rack solicitado."));
            return "redirect:/racks";

        } catch (AccessDeniedException ex) {
            throw ex;

        } catch (Exception ex) {
            logger.error("Error al generar el reporte del rack: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.report.error", locale, "No se pudo generar el reporte del rack."));
            return "redirect:/racks/detail?id=" + id;
        }
    }

    @PostMapping("/grant-access")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public String grantAccess(@RequestParam("userEmail") String userEmail,
                              @RequestParam("rackId") Long rackId,
                              @RequestParam("permission") RackPermission permission,
                              RedirectAttributes redirectAttributes,
                              Principal principal,
                              Locale locale) {

        logger.info("Asignando permiso {} del usuario {} sobre rack {}", permission, userEmail, rackId);

        try {
            String actorEmail = principal != null ? principal.getName() : null;
            userRackAccessService.grantOrUpdateByEmail(actorEmail, userEmail, rackId, permission);
            redirectAttributes.addFlashAttribute("successMessage", msg("msg.rack.feedback.permission.save.success", locale, "Permiso guardado correctamente."));

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró usuario o rack al compartir acceso: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.permission.userNotFound", locale, "No se encontró un usuario con ese correo."));

        } catch (AccessDeniedException ex) {
            logger.warn("Acceso denegado al compartir rack {}: {}", rackId, ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.permission.denied", locale, "No tienes permisos para compartir este rack."));

        } catch (Exception ex) {
            logger.error("Error asignando permiso usuario-rack: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.permission.save.error", locale, "No se pudo guardar el permiso."));
        }

        return "redirect:/racks/detail?id=" + rackId;
    }

    @PostMapping("/revoke-access")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public String revokeAccess(@RequestParam("userId") Long userId,
                               @RequestParam("rackId") Long rackId,
                               RedirectAttributes redirectAttributes,
                               Principal principal,
                               Locale locale) {

        logger.info("Revocando acceso del usuario {} sobre rack {}", userId, rackId);

        try {
            String actorEmail = principal != null ? principal.getName() : null;
            userRackAccessService.revoke(actorEmail, userId, rackId);
            redirectAttributes.addFlashAttribute("successMessage", msg("msg.rack.feedback.permission.delete.success", locale, "Permiso eliminado correctamente."));

        } catch (AccessDeniedException ex) {
            logger.warn("Acceso denegado al revocar acceso de rack {}: {}", rackId, ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.permission.denied", locale, "No tienes permisos para gestionar accesos de este rack."));

        } catch (Exception ex) {
            logger.error("Error revocando permiso usuario-rack: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.rack.feedback.permission.delete.error", locale, "No se pudo eliminar el permiso."));
        }

        return "redirect:/racks/detail?id=" + rackId;
    }

    private void loadFormCatalogs(Model model) {
        model.addAttribute("rooms", dataCenterRoomService.listOptions());
        model.addAttribute("rackStatuses", RackStatus.values());
        model.addAttribute("statuses", RackStatus.values());
        model.addAttribute("active", ACTIVE_MENU);
    }

    private String resolveSortParam(Page<?> page) {
        String sortParam = "locationLabel,asc";

        if (page.getSort().isSorted()) {
            Sort.Order order = page.getSort().iterator().next();
            sortParam = order.getProperty() + "," + order.getDirection().name().toLowerCase();
        }

        return sortParam;
    }

    private Map<RackPermission, String> buildPermissionLabels(Locale locale) {
        Map<RackPermission, String> labels = new EnumMap<>(RackPermission.class);
        for (RackPermission permission : RackPermission.values()) {
            labels.put(permission, msg("msg.user-detail.rackAccess.permission." + permission.name().toLowerCase(), locale, permission.name()));
        }
        return labels;
    }

    private Map<RackStatus, String> buildRackStatusLabels(Locale locale) {
        Map<RackStatus, String> labels = new EnumMap<>(RackStatus.class);
        for (RackStatus status : RackStatus.values()) {
            labels.put(status, msg("msg.rack.status." + status.name().toLowerCase(), locale, status.name()));
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

    private Map<EquipmentStatus, String> buildEquipmentStatusLabels(Locale locale) {
        Map<EquipmentStatus, String> labels = new EnumMap<>(EquipmentStatus.class);
        for (EquipmentStatus status : EquipmentStatus.values()) {
            labels.put(status, msg("msg.equipment.status." + status.name().toLowerCase(), locale, status.name()));
        }
        return labels;
    }

    private String msg(String key, Locale locale, String fallback, Object... args) {
        return messageSource.getMessage(key, args, fallback, locale);
    }
}
