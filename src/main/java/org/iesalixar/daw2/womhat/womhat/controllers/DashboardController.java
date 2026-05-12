package org.iesalixar.daw2.womhat.womhat.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.iesalixar.daw2.womhat.womhat.dtos.DashboardSummaryDTO;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentEventType;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentLogAction;
import org.iesalixar.daw2.womhat.womhat.enums.RackPurchaseOrderStatus;
import org.iesalixar.daw2.womhat.womhat.enums.RackStatus;
import org.iesalixar.daw2.womhat.womhat.repositories.RackPurchaseOrderRepository;
import org.iesalixar.daw2.womhat.womhat.services.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Controlador del dashboard principal.
 *
 * ADMIN mantiene el panel completo.
 * USER recibe un dashboard propio con accesos reales a catálogo,
 * pedidos, recursos permitidos y perfil.
 */
@Controller
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private RackPurchaseOrderRepository rackPurchaseOrderRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            Locale locale,
                            Principal principal,
                            HttpServletRequest request) {

        logger.info("Cargando dashboard principal.");

        boolean adminAccess = request.isUserInRole("ADMIN");
        String email = principal != null ? principal.getName() : null;

        model.addAttribute("active", "dashboard");
        model.addAttribute("adminAccess", adminAccess);
        model.addAttribute("rackStatusLabels", buildRackStatusLabels(locale));
        model.addAttribute("activityActionLabels", buildActivityActionLabels(locale));
        model.addAttribute("activityEventLabels", buildActivityEventLabels(locale));
        model.addAttribute(
                "recentMessageFallback",
                messageSource.getMessage("msg.dashboard.recent.messageFallback", null, "Sin detalle adicional registrado.", locale)
        );
        model.addAttribute("adminPlacedOrderCount", 0L);
        model.addAttribute("adminFulfilledOrderCount", 0L);

        try {
            model.addAttribute("summary", dashboardService.getSummaryForUser(email));
            model.addAttribute("rackMapItems", dashboardService.getRackMapItemsForUser(email));
            model.addAttribute("recentActivity", dashboardService.getRecentActivityForUser(email, adminAccess ? 10 : 8));

            if (adminAccess) {
                model.addAttribute(
                        "adminPlacedOrderCount",
                        rackPurchaseOrderRepository.countByStatus(RackPurchaseOrderStatus.PLACED)
                );
                model.addAttribute(
                        "adminFulfilledOrderCount",
                        rackPurchaseOrderRepository.countByStatus(RackPurchaseOrderStatus.FULFILLED)
                );
            }

        } catch (Exception ex) {
            logger.error("Error al cargar el dashboard: {}", ex.getMessage(), ex);

            model.addAttribute(
                    "errorMessage",
                    messageSource.getMessage(
                            "msg.dashboard.load.error",
                            null,
                            "No se pudo cargar el dashboard.",
                            locale
                    )
            );

            model.addAttribute("summary", new DashboardSummaryDTO(0L, 0L, 0L, 0L));
            model.addAttribute("rackMapItems", List.of());
            model.addAttribute("recentActivity", List.of());
        }

        return adminAccess
                ? "views/dashboard/dashboard"
                : "views/dashboard/dashboard-user";
    }

    private Map<RackStatus, String> buildRackStatusLabels(Locale locale) {
        Map<RackStatus, String> labels = new EnumMap<>(RackStatus.class);
        labels.put(RackStatus.ACTIVE, messageSource.getMessage("msg.rack.status.active", null, "Activo", locale));
        labels.put(
                RackStatus.MAINTENANCE,
                messageSource.getMessage("msg.rack.status.maintenance", null, "Mantenimiento", locale)
        );
        labels.put(RackStatus.INACTIVE, messageSource.getMessage("msg.rack.status.inactive", null, "Inactivo", locale));
        return labels;
    }

    private Map<EquipmentLogAction, String> buildActivityActionLabels(Locale locale) {
        Map<EquipmentLogAction, String> labels = new EnumMap<>(EquipmentLogAction.class);
        labels.put(EquipmentLogAction.INSERT, messageSource.getMessage("msg.dashboard.activity.action.insert", null, "Alta", locale));
        labels.put(
                EquipmentLogAction.UPDATE,
                messageSource.getMessage("msg.dashboard.activity.action.update", null, "Actualización", locale)
        );
        labels.put(EquipmentLogAction.DELETE, messageSource.getMessage("msg.dashboard.activity.action.delete", null, "Baja", locale));
        return labels;
    }

    private Map<EquipmentEventType, String> buildActivityEventLabels(Locale locale) {
        Map<EquipmentEventType, String> labels = new EnumMap<>(EquipmentEventType.class);
        labels.put(EquipmentEventType.CREATED, messageSource.getMessage("msg.dashboard.activity.event.created", null, "Creado", locale));
        labels.put(EquipmentEventType.UPDATED, messageSource.getMessage("msg.dashboard.activity.event.updated", null, "Actualizado", locale));
        labels.put(
                EquipmentEventType.MOVED_RACK,
                messageSource.getMessage("msg.dashboard.activity.event.movedRack", null, "Movido de rack", locale)
        );
        labels.put(
                EquipmentEventType.STATUS_CHANGED,
                messageSource.getMessage("msg.dashboard.activity.event.statusChanged", null, "Cambio de estado", locale)
        );
        labels.put(EquipmentEventType.DELETED, messageSource.getMessage("msg.dashboard.activity.event.deleted", null, "Eliminado", locale));
        return labels;
    }
}
