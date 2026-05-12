package org.iesalixar.daw2.womhat.womhat.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.womhat.womhat.dtos.*;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderPriority;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderStatus;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.services.EquipmentService;
import org.iesalixar.daw2.womhat.womhat.services.MaintenanceWorkOrderService;
import org.iesalixar.daw2.womhat.womhat.services.UserRackAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Controlador MVC del módulo de órdenes de mantenimiento.
 *
 * Se centra en la gestión web del CRUD y en la preparación
 * de catálogos auxiliares para los formularios.
 */
@Controller
@RequestMapping("/maintenance/work-orders")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class MaintenanceWorkOrderController {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceWorkOrderController.class);
    private static final String ACTIVE_MENU = "maintenance";

    @Autowired
    private MaintenanceWorkOrderService maintenanceWorkOrderService;

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserRackAccessService userRackAccessService;

    /**
     * Lista órdenes de mantenimiento.
     *
     * Como el service actual no expone un listAll genérico, se utiliza
     * un criterio simple:
     * - si viene equipmentId, lista por equipo,
     * - si viene status, lista por estado,
     * - si viene priority, lista por prioridad,
     * - si no viene nada, lista las abiertas.
     *
     * @param equipmentId filtro opcional por equipo
     * @param status filtro opcional por estado
     * @param priority filtro opcional por prioridad
     * @param model modelo
     * @return vista listado
     */
    @GetMapping
    public String listWorkOrders(@RequestParam(name = "equipmentId", required = false) Long equipmentId,
                                 @RequestParam(name = "status", required = false) WorkOrderStatus status,
                                 @RequestParam(name = "priority", required = false) WorkOrderPriority priority,
                                 Model model,
                                 Principal principal,
                                 Locale locale) {

        logger.info("Listando órdenes de mantenimiento equipmentId={}, status={}, priority={}",
                equipmentId, status, priority);

        try {
            String actorEmail = principal != null ? principal.getName() : null;
            List<MaintenanceWorkOrderDTO> workOrders;

            if (equipmentId != null) {
                workOrders = maintenanceWorkOrderService.listByEquipment(equipmentId, actorEmail);
            } else if (status != null) {
                workOrders = maintenanceWorkOrderService.listByStatus(status, actorEmail);
            } else if (priority != null) {
                workOrders = maintenanceWorkOrderService.listByPriority(priority, actorEmail);
            } else {
                workOrders = maintenanceWorkOrderService.listByStatus(WorkOrderStatus.OPEN, actorEmail);
            }

            boolean adminAccess = userRackAccessService.hasGlobalAdminAccess(actorEmail);
            boolean canCreateWorkOrder = adminAccess || !userRackAccessService.listWritableRackOptions(actorEmail).isEmpty();
            Set<Long> editableWorkOrderIds = new LinkedHashSet<>();
            for (MaintenanceWorkOrderDTO workOrder : workOrders) {
                if (workOrder.getId() == null) {
                    continue;
                }

                if (adminAccess || canWriteMaintenanceForEquipment(actorEmail, workOrder.getEquipmentId())) {
                    editableWorkOrderIds.add(workOrder.getId());
                }
            }

            model.addAttribute("listWorkOrders", workOrders);
            model.addAttribute("equipmentId", equipmentId);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("selectedPriority", priority);
            model.addAttribute("statuses", WorkOrderStatus.values());
            model.addAttribute("priorities", WorkOrderPriority.values());
            model.addAttribute("canCreateWorkOrder", canCreateWorkOrder);
            model.addAttribute("canEditWorkOrders", adminAccess);
            model.addAttribute("editableWorkOrderIds", editableWorkOrderIds);
            model.addAttribute("canDeleteWorkOrders", adminAccess);
            model.addAttribute("active", ACTIVE_MENU);

        } catch (AccessDeniedException ex) {
            throw ex;

        } catch (Exception ex) {
            logger.error("Error al listar órdenes de mantenimiento: {}", ex.getMessage(), ex);
            model.addAttribute("errorMessage", msg("msg.workOrder.feedback.list.error", locale, "No se pudieron cargar las órdenes de mantenimiento."));
            model.addAttribute("active", ACTIVE_MENU);
        }

        return "views/maintenance/work-order-list";
    }

    /**
     * Muestra el formulario de alta.
     *
     * @param equipmentId equipo opcional para precargar
     * @param model modelo
     * @return vista formulario
     */
    @GetMapping("/new")
    public String showNewForm(@RequestParam(name = "equipmentId", required = false) Long equipmentId,
                              Model model,
                              Principal principal,
                              Locale locale) {

        logger.info("Mostrando formulario de alta de orden de mantenimiento.");
        String actorEmail = principal != null ? principal.getName() : null;
        boolean adminAccess = userRackAccessService.hasGlobalAdminAccess(actorEmail);

        if (!adminAccess && userRackAccessService.listWritableRackOptions(actorEmail).isEmpty()) {
            throw new AccessDeniedException(msg(
                    "msg.workOrder.feedback.create.denied",
                    locale,
                    "No tienes permisos de escritura en racks para crear órdenes de mantenimiento."
            ));
        }

        if (equipmentId != null) {
            Long rackId = equipmentService.getDetail(equipmentId, actorEmail).getRackId();
            if (!userRackAccessService.canWriteRack(actorEmail, rackId)) {
                throw new AccessDeniedException(msg(
                        "msg.workOrder.feedback.create.denied",
                        locale,
                        "No tienes permisos de escritura en racks para crear órdenes de mantenimiento."
                ));
            }
        }

        MaintenanceWorkOrderFormDTO formDTO = new MaintenanceWorkOrderFormDTO();
        formDTO.setEquipmentId(equipmentId);

        model.addAttribute("workOrder", formDTO);
        loadFormCatalogs(model, actorEmail);
        model.addAttribute("maintenanceReadOnlyMode", false);
        return "views/maintenance/work-order-form";
    }

    /**
     * Muestra el formulario de edición.
     *
     * @param id id de la orden
     * @param model modelo
     * @param redirectAttributes mensajes flash
     * @return vista o redirect
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Principal principal,
                               Locale locale) {

        logger.info("Mostrando formulario de edición de orden id={}", id);

        try {
            String actorEmail = principal != null ? principal.getName() : null;
            MaintenanceWorkOrderFormDTO formDTO = maintenanceWorkOrderService.getForm(id, actorEmail);
            boolean adminAccess = userRackAccessService.hasGlobalAdminAccess(actorEmail);

            model.addAttribute("workOrder", formDTO);
            loadFormCatalogs(model, actorEmail);
            model.addAttribute("maintenanceReadOnlyMode", !adminAccess);
            return "views/maintenance/work-order-form";

        } catch (AccessDeniedException ex) {
            throw ex;

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró la orden id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.workOrder.feedback.notFound", locale, "No se encontró la orden solicitada."));
            return "redirect:/maintenance/work-orders";

        } catch (Exception ex) {
            logger.error("Error al cargar el formulario de orden: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.workOrder.feedback.form.error", locale, "No se pudo cargar el formulario de la orden."));
            return "redirect:/maintenance/work-orders";
        }
    }

    /**
     * Inserta una orden nueva.
     *
     * @param workOrder dto del formulario
     * @param result validación
     * @param model modelo
     * @param redirectAttributes mensajes flash
     * @param principal usuario autenticado
     * @return redirect o formulario
     */
    @PostMapping("/insert")
    public String insertWorkOrder(@Valid @ModelAttribute("workOrder") MaintenanceWorkOrderFormDTO workOrder,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes,
                                  Principal principal,
                                  Locale locale) {

        logger.info("Insertando orden de mantenimiento para equipmentId={}", workOrder.getEquipmentId());

        try {
            if (result.hasErrors()) {
                String actorEmailForErrors = principal != null ? principal.getName() : null;
                loadFormCatalogs(model, actorEmailForErrors);
                return "views/maintenance/work-order-form";
            }

            String actorEmail = principal != null ? principal.getName() : null;
            maintenanceWorkOrderService.create(workOrder, actorEmail);

            redirectAttributes.addFlashAttribute("successMessage", msg("msg.workOrder.feedback.create.success", locale, "Orden de mantenimiento creada correctamente."));
            return "redirect:/maintenance/work-orders";

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró el equipo de la orden: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.workOrder.feedback.create.equipmentNotFound", locale, "No se encontró el equipo seleccionado."));
            return "redirect:/maintenance/work-orders/new";

        } catch (AccessDeniedException ex) {
            throw ex;

        } catch (Exception ex) {
            logger.error("Error al crear la orden: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.workOrder.feedback.create.error", locale, "No se pudo crear la orden de mantenimiento."));
            return "redirect:/maintenance/work-orders/new";
        }
    }

    /**
     * Actualiza una orden existente.
     *
     * @param workOrder dto del formulario
     * @param result validación
     * @param model modelo
     * @param redirectAttributes mensajes flash
     * @return redirect o formulario
     */
    @PostMapping("/update")
    public String updateWorkOrder(@Valid @ModelAttribute("workOrder") MaintenanceWorkOrderFormDTO workOrder,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes,
                                  Principal principal,
                                  Locale locale) {

        logger.info("Actualizando orden de mantenimiento id={}", workOrder.getId());

        try {
            if (result.hasErrors()) {
                String actorEmailForErrors = principal != null ? principal.getName() : null;
                loadFormCatalogs(model, actorEmailForErrors);
                return "views/maintenance/work-order-form";
            }

            String actorEmail = principal != null ? principal.getName() : null;
            maintenanceWorkOrderService.update(workOrder, actorEmail);

            redirectAttributes.addFlashAttribute("successMessage", msg("msg.workOrder.feedback.update.success", locale, "Orden de mantenimiento actualizada correctamente."));
            return "redirect:/maintenance/work-orders";

        } catch (AccessDeniedException ex) {
            throw ex;

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró la orden o el equipo: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.workOrder.feedback.update.notFound", locale, "No se encontró la orden o el equipo seleccionado."));
            return "redirect:/maintenance/work-orders";

        } catch (Exception ex) {
            logger.error("Error al actualizar la orden: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.workOrder.feedback.update.error", locale, "No se pudo actualizar la orden de mantenimiento."));
            return "redirect:/maintenance/work-orders/edit?id=" + workOrder.getId();
        }
    }

    /**
     * Elimina una orden.
     *
     * @param id id de la orden
     * @param redirectAttributes mensajes flash
     * @return redirect
     */
    @PostMapping("/delete")
    public String deleteWorkOrder(@RequestParam("id") Long id,
                                  RedirectAttributes redirectAttributes,
                                  Principal principal,
                                  Locale locale) {

        logger.info("Eliminando orden de mantenimiento id={}", id);

        try {
            String actorEmail = principal != null ? principal.getName() : null;
            maintenanceWorkOrderService.delete(id, actorEmail);
            redirectAttributes.addFlashAttribute("successMessage", msg("msg.workOrder.feedback.delete.success", locale, "Orden de mantenimiento eliminada correctamente."));

        } catch (AccessDeniedException ex) {
            throw ex;

        } catch (ResourceNotFoundException ex) {
            logger.warn("No se encontró la orden id={}", id);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.workOrder.feedback.notFound", locale, "No se encontró la orden solicitada."));

        } catch (Exception ex) {
            logger.error("Error al eliminar la orden id={}: {}", id, ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.workOrder.feedback.delete.error", locale, "No se pudo eliminar la orden de mantenimiento."));
        }

        return "redirect:/maintenance/work-orders";
    }

    /**
     * Muestra el detalle de una orden.
     *
     * @param id id de la orden
     * @param model modelo
     * @param redirectAttributes mensajes flash
     * @return vista detalle o redirect
     */
    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Principal principal,
                             Locale locale) {

        logger.info("Mostrando detalle de orden de mantenimiento id={}", id);

        try {
            String actorEmail = principal != null ? principal.getName() : null;
            MaintenanceWorkOrderDetailDTO detailDTO = maintenanceWorkOrderService.getDetail(id, actorEmail);
            boolean adminAccess = userRackAccessService.hasGlobalAdminAccess(actorEmail);

            Long equipmentRackId = equipmentService.getDetail(detailDTO.getEquipmentId(), actorEmail).getRackId();
            boolean canWriteRack = userRackAccessService.canWriteRack(actorEmail, equipmentRackId);

            model.addAttribute("workOrder", detailDTO);
            if (!model.containsAttribute("maintenanceNote")) {
                MaintenanceNoteFormDTO noteFormDTO = new MaintenanceNoteFormDTO();
                noteFormDTO.setWorkOrderId(id);
                model.addAttribute("maintenanceNote", noteFormDTO);
            }
            model.addAttribute("canEditWorkOrders", canWriteRack);
            model.addAttribute("canDeleteWorkOrders", adminAccess);
            model.addAttribute("canAddNotes", canWriteRack);
            model.addAttribute("canDeleteNotes", adminAccess);
            model.addAttribute("active", ACTIVE_MENU);
            return "views/maintenance/work-order-detail";

        } catch (AccessDeniedException ex) {
            throw ex;

        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.workOrder.feedback.notFound", locale, "No se encontró la orden solicitada."));
            return "redirect:/maintenance/work-orders";

        } catch (Exception ex) {
            logger.error("Error al cargar el detalle de la orden: {}", ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("errorMessage", msg("msg.workOrder.feedback.detail.error", locale, "No se pudo cargar el detalle de la orden."));
            return "redirect:/maintenance/work-orders";
        }
    }

    /**
     * Carga catálogos auxiliares para el formulario.
     *
     * @param model modelo
     */
    private void loadFormCatalogs(Model model, String actorEmail) {
        Pageable equipmentPageable = PageRequest.of(0, 5000, Sort.by(Sort.Direction.ASC, "name"));
        boolean adminAccess = userRackAccessService.hasGlobalAdminAccess(actorEmail);
        List<EquipmentDTO> equipmentOptions = equipmentService.list(equipmentPageable, actorEmail, true)
                .getContent()
                .stream()
                .filter(equipmentDTO -> adminAccess
                        || (equipmentDTO.getRackId() != null && userRackAccessService.canWriteRack(actorEmail, equipmentDTO.getRackId())))
                .toList();

        model.addAttribute("equipmentOptions", equipmentOptions);
        model.addAttribute("statuses", WorkOrderStatus.values());
        model.addAttribute("priorities", WorkOrderPriority.values());
        model.addAttribute("canEditWorkOrderStatus", true);
        model.addAttribute("active", ACTIVE_MENU);
    }

    private boolean canWriteMaintenanceForEquipment(String actorEmail, Long equipmentId) {
        if (equipmentId == null) {
            return false;
        }

        try {
            Long rackId = equipmentService.getDetail(equipmentId, actorEmail).getRackId();
            return userRackAccessService.canWriteRack(actorEmail, rackId);
        } catch (Exception ignored) {
            return false;
        }
    }

    private String msg(String key, Locale locale, String fallback, Object... args) {
        return messageSource.getMessage(key, args, fallback, locale);
    }
}
