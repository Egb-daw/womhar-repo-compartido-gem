package org.iesalixar.daw2.womhat.womhat.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.dtos.RackDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackPurchaseOrderDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackPurchaseOrderFormDTO;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentStatus;
import org.iesalixar.daw2.womhat.womhat.enums.RackPurchaseOrderStatus;
import org.iesalixar.daw2.womhat.womhat.enums.RackStatus;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.services.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Controlador del catálogo interno de WoMHAT.
 *
 * Permite consultar racks publicados, crear pedidos internos,
 * consultar pedidos propios y gestionar pedidos desde administración.
 */
@Controller
@RequestMapping("/catalog")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class CatalogController {

    private static final Logger logger = LoggerFactory.getLogger(CatalogController.class);

    private final CatalogService catalogService;
    private final MessageSource messageSource;

    /**
     * Muestra el listado de racks disponibles en el catálogo, con opciones de búsqueda y filtrado.
     *
     * @param pageable Configuración de paginación y ordenamiento.
     * @param query Término de búsqueda para nombre o descripción.
     * @param status Filtro por estado del rack.
     * @param minCapacityU Filtro por capacidad mínima en U.
     * @param maxPrice Filtro por precio máximo.
     * @param model Modelo para la vista.
     * @return Vista del catálogo con los racks listados.
     */
    @GetMapping
    public String showCatalog(
            @PageableDefault(size = 9, sort = "catalogPrice", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "status", required = false) RackStatus status,
            @RequestParam(value = "minCapacityU", required = false) Integer minCapacityU,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            Model model,
            Locale locale
    ) {
        Page<RackDTO> page = catalogService.listCatalog(pageable, query, status, minCapacityU, maxPrice);
        String sortParam = resolveSortParam(page, "catalogPrice,asc");

        model.addAttribute("active", "catalog");
        model.addAttribute("page", page);
        model.addAttribute("query", query);
        model.addAttribute("statusFilter", status);
        model.addAttribute("minCapacityUFilter", minCapacityU);
        model.addAttribute("maxPriceFilter", maxPrice);
        model.addAttribute("sortParam", sortParam);
        model.addAttribute("sortLabel", resolveCatalogSortLabel(sortParam, locale));
        model.addAttribute("catalogStatuses", RackStatus.values());
        model.addAttribute("rackStatusLabels", buildRackStatusLabels(locale));

        return "views/catalog/catalog-list";
    }

    /**
     * Muestra el detalle de un rack específico, con opción de crear un pedido.
     *
     * @param id ID del rack a mostrar.
     * @param model Modelo para la vista.
     * @param redirectAttributes Atributos para redirecciones con mensajes.
     * @param locale Localización para mensajes.
     * @return Vista del detalle del rack o redirección en caso de error.
     */
    @GetMapping("/detail")
    public String showDetail(@RequestParam("id") Long id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {

        try {
            RackDetailDTO rack = catalogService.getCatalogDetail(id);

            RackPurchaseOrderFormDTO orderForm = new RackPurchaseOrderFormDTO();
            orderForm.setRackId(id);
            orderForm.setQuantity(1);

            model.addAttribute("active", "catalog");
            model.addAttribute("rack", rack);
            model.addAttribute("orderForm", orderForm);
            model.addAttribute("rackStatusLabels", buildRackStatusLabels(locale));
            model.addAttribute("equipmentStatusLabels", buildEquipmentStatusLabels(locale));

            return "views/catalog/catalog-detail";

        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    msg("msg.catalog.detail.notFound", locale, "No se encontró el rack solicitado.")
            );
            return "redirect:/catalog";

        } catch (Exception ex) {
            logger.error("Error cargando detalle de catálogo para rack {}: {}", id, ex.getMessage(), ex);

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    msg("msg.catalog.detail.error", locale, "No se pudo cargar la ficha del rack.")
            );
            return "redirect:/catalog";
        }
    }

    /**
     * Procesa la creación de un pedido de rack desde el detalle del catálogo.
     *
     * @param form Formulario con los datos del pedido.
     * @param result Resultado de la validación del formulario.
     * @param redirectAttributes Atributos para redirecciones con mensajes.
     * @param principal Usuario autenticado realizando el pedido.
     * @param locale Localización para mensajes.
     * @return Redirección a la lista de pedidos o al detalle en caso de error.
     */
    @PostMapping("/orders")
    public String placeOrder(@Valid @ModelAttribute("orderForm") RackPurchaseOrderFormDTO form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             Principal principal,
                             Locale locale) {
        // El catálogo interno opera con una única unidad por pedido/rack.
        form.setQuantity(1);

        if (result.hasErrors()) {
            return renderDetailWithFormErrors(form, model, locale);
        }

        try {
            catalogService.placeOrder(principal.getName(), form);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    msg("msg.catalog.orders.create.success", locale, "Pedido registrado correctamente.")
            );

            return "redirect:/catalog/orders";

        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    msg("msg.catalog.detail.notFound", locale, "No se encontró el rack solicitado.")
            );
            return "redirect:/catalog";

        } catch (IllegalStateException ex) {
            String errorMessage = ex.getMessage();
            if (errorMessage != null) {
                String normalized = errorMessage.toLowerCase(locale);
                if (normalized.contains("cantidad") || normalized.contains("unidad")) {
                    result.rejectValue(
                            "quantity",
                            "catalog.order.quantity.invalid",
                            msg("msg.catalog.order.quantity.min", locale, "La cantidad solicitada debe ser al menos 1.")
                    );
                } else if (normalized.contains("stock")) {
                    result.rejectValue(
                            "quantity",
                            "catalog.order.quantity.stock",
                            msg("msg.catalog.order.quantity.stock", locale, "La cantidad solicitada supera el stock disponible.")
                    );
                } else {
                    result.reject(
                            "catalog.order.invalidState",
                            msg("msg.catalog.orders.create.invalidState", locale, errorMessage)
                    );
                }
            }

            return renderDetailWithFormErrors(form, model, locale);

        } catch (Exception ex) {
            logger.error("Error creando pedido de rack: {}", ex.getMessage(), ex);

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    msg("msg.catalog.orders.create.error", locale, "No se pudo registrar el pedido.")
            );
            return "redirect:/catalog/detail?id=" + form.getRackId();
        }
    }

    /**
     * Muestra la lista de pedidos realizados por el usuario, con opción de cancelarlos.
     * Si el usuario tiene rol ADMIN, muestra todos los pedidos con opciones de gestión.
     *
     * @param pageable Configuración de paginación y ordenamiento.
     * @param principal Usuario autenticado para filtrar pedidos propios.
     * @param request Solicitud HTTP para verificar roles.
     * @param model Modelo para la vista.
     * @return Vista con la lista de pedidos.
     */
    @GetMapping("/orders")
    public String listOrders(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Principal principal,
            HttpServletRequest request,
            Model model,
            Locale locale
    ) {
        boolean adminAccess = request.isUserInRole("ADMIN");
        Page<RackPurchaseOrderDTO> orders = catalogService.listOrders(principal.getName(), pageable, adminAccess);
        String sortParam = resolveSortParam(orders, "createdAt,desc");

        model.addAttribute("active", "orders");
        model.addAttribute("page", orders);
        model.addAttribute("adminOrders", adminAccess);
        model.addAttribute("sortParam", sortParam);
        model.addAttribute("sortLabel", resolveOrdersSortLabel(sortParam, locale));
        model.addAttribute("orderStatusLabels", buildOrderStatusLabels(locale));

        return "views/catalog/catalog-orders";
    }

    /**
     * Permite cancelar un pedido realizado por el usuario o, si tiene rol ADMIN, cualquier pedido.
     *
     * @param id ID del pedido a cancelar.
     * @param redirectAttributes Atributos para redirecciones con mensajes.
     * @param principal Usuario autenticado para verificar permisos.
     * @param request Solicitud HTTP para verificar roles.
     * @param locale Localización para mensajes.
     * @return Redirección a la lista de pedidos con mensaje de resultado.
     */
    @PostMapping("/orders/cancel")
    public String cancelOrder(@RequestParam(value = "orderId", required = false) Long orderId,
                              @RequestParam(value = "id", required = false) Long legacyId,
                              RedirectAttributes redirectAttributes,
                              Principal principal,
                              HttpServletRequest request,
                              Locale locale) {
        Long id = orderId != null ? orderId : legacyId;
        if (id == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    msg("msg.catalog.orders.notFound", locale, "No se encontró el pedido solicitado.")
            );
            return "redirect:/catalog/orders";
        }

        boolean adminAccess = request.isUserInRole("ADMIN");

        try {
            catalogService.cancelOrder(principal.getName(), id, adminAccess);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    msg("msg.catalog.orders.cancel.success", locale, "Pedido cancelado correctamente.")
            );

        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    msg("msg.catalog.orders.notFound", locale, "No se encontró el pedido solicitado.")
            );

        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    msg("msg.catalog.orders.cancel.invalidState", locale, ex.getMessage())
            );
        }

        return "redirect:/catalog/orders";
    }

    /**
     * Permite marcar un pedido como completado desde administración.
     *
     * @param id ID del pedido a completar.
     * @param redirectAttributes Atributos para redirecciones con mensajes.
     * @param locale Localización para mensajes.
     * @return Redirección a la lista de pedidos con mensaje de resultado.
     */
    @PostMapping("/orders/fulfill")
    @PreAuthorize("hasRole('ADMIN')")
    public String fulfillOrder(@RequestParam(value = "orderId", required = false) Long orderId,
                               @RequestParam(value = "id", required = false) Long legacyId,
                               RedirectAttributes redirectAttributes,
                               Principal principal,
                               Locale locale) {
        Long id = orderId != null ? orderId : legacyId;
        if (id == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    msg("msg.catalog.orders.notFound", locale, "No se encontró el pedido solicitado.")
            );
            return "redirect:/catalog/orders";
        }

        try {
            catalogService.fulfillOrder(id, principal != null ? principal.getName() : null);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    msg("msg.catalog.orders.fulfill.success", locale, "Pedido marcado como completado.")
            );

        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    msg("msg.catalog.orders.notFound", locale, "No se encontró el pedido solicitado.")
            );

        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    msg("msg.catalog.orders.fulfill.invalidState", locale, ex.getMessage())
            );

        } catch (Exception ex) {
            logger.error("Error completando pedido {}: {}", id, ex.getMessage(), ex);

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    msg("msg.catalog.orders.fulfill.error", locale, "No se pudo completar el pedido.")
            );
        }

        return "redirect:/catalog/orders";
    }

    /**
     * Resuelve el parámetro de ordenamiento para mantenerlo en las URLs de paginación y ordenamiento.
     *
     * @param page Página actual para extraer el ordenamiento.
     * @param fallback Valor por defecto si no hay ordenamiento definido.
     * @return Cadena con el formato "campo,direccion" para usar en URLs.
     */
    private String resolveSortParam(Page<?> page, String fallback) {
        if (page == null || page.getSort() == null || page.getSort().isUnsorted()) {
            return fallback;
        }

        Sort.Order order = page.getSort().iterator().next();
        return order.getProperty() + "," + order.getDirection().name().toLowerCase();
    }

    private String resolveCatalogSortLabel(String sortParam, Locale locale) {
        return switch (sortParam) {
            case "catalogPrice,asc" -> msg("msg.catalog.list.filter.sort.priceAsc", locale, "Precio: menor a mayor");
            case "catalogPrice,desc" -> msg("msg.catalog.list.filter.sort.priceDesc", locale, "Precio: mayor a menor");
            case "capacityU,desc" -> msg("msg.catalog.list.filter.sort.capacityDesc", locale, "Capacidad: mayor a menor");
            case "catalogStock,desc" -> msg("msg.catalog.list.filter.sort.stockDesc", locale, "Stock: mayor a menor");
            case "locationLabel,asc" -> msg("msg.catalog.list.filter.sort.locationAsc", locale, "Ubicación: A-Z");
            default -> msg("msg.catalog.list.filter.sort.custom", locale, "Orden personalizado");
        };
    }

    private String resolveOrdersSortLabel(String sortParam, Locale locale) {
        return switch (sortParam) {
            case "createdAt,desc" -> msg("msg.catalog.orders.sort.createdDesc", locale, "Más recientes primero");
            case "createdAt,asc" -> msg("msg.catalog.orders.sort.createdAsc", locale, "Más antiguos primero");
            default -> msg("msg.catalog.orders.sort.custom", locale, "Orden personalizado");
        };
    }

    private Map<RackStatus, String> buildRackStatusLabels(Locale locale) {
        Map<RackStatus, String> labels = new EnumMap<>(RackStatus.class);
        for (RackStatus rackStatus : RackStatus.values()) {
            labels.put(rackStatus, msg("msg.catalog.status." + rackStatus.name().toLowerCase(), locale, rackStatus.name()));
        }
        return labels;
    }

    private Map<RackPurchaseOrderStatus, String> buildOrderStatusLabels(Locale locale) {
        Map<RackPurchaseOrderStatus, String> labels = new EnumMap<>(RackPurchaseOrderStatus.class);
        for (RackPurchaseOrderStatus orderStatus : RackPurchaseOrderStatus.values()) {
            labels.put(orderStatus, msg("msg.catalog.orders.status." + orderStatus.name().toLowerCase(), locale, orderStatus.name()));
        }
        return labels;
    }

    private Map<EquipmentStatus, String> buildEquipmentStatusLabels(Locale locale) {
        Map<EquipmentStatus, String> labels = new EnumMap<>(EquipmentStatus.class);
        for (EquipmentStatus equipmentStatus : EquipmentStatus.values()) {
            labels.put(equipmentStatus, msg("msg.equipment.status." + equipmentStatus.name().toLowerCase(), locale, equipmentStatus.name()));
        }
        return labels;
    }

    private String msg(String key, Locale locale, String fallback, Object... args) {
        return messageSource.getMessage(key, args, fallback, locale);
    }

    private String renderDetailWithFormErrors(RackPurchaseOrderFormDTO form, Model model, Locale locale) {
        try {
            form.setQuantity(1);
            RackDetailDTO rack = catalogService.getCatalogDetail(form.getRackId());
            model.addAttribute("active", "catalog");
            model.addAttribute("rack", rack);
            model.addAttribute("orderForm", form);
            model.addAttribute("rackStatusLabels", buildRackStatusLabels(locale));
            model.addAttribute("equipmentStatusLabels", buildEquipmentStatusLabels(locale));
            return "views/catalog/catalog-detail";
        } catch (ResourceNotFoundException ex) {
            model.addAttribute(
                    "errorMessage",
                    msg("msg.catalog.detail.notFound", locale, "No se encontró el rack solicitado.")
            );
            return "redirect:/catalog";
        }
    }
}
