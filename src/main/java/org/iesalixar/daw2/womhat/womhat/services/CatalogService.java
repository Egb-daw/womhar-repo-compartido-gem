package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.RackDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackPurchaseOrderDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackPurchaseOrderFormDTO;
import org.iesalixar.daw2.womhat.womhat.enums.RackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * Servicio del catálogo público y pedidos de usuario.
 */
public interface CatalogService {

    /**
     * Lista racks publicados en catálogo con filtros de negocio.
     */
    Page<RackDTO> listCatalog(Pageable pageable,
                              String query,
                              RackStatus status,
                              Integer minCapacityU,
                              BigDecimal maxPrice);

    /**
     * Recupera el detalle catalogable de un rack concreto.
     */
    RackDetailDTO getCatalogDetail(Long rackId);

    /**
     * Registra una solicitud de compra interna de rack para el usuario autenticado.
     */
    void placeOrder(String email, RackPurchaseOrderFormDTO form);

    /**
     * Lista pedidos. En modo admin devuelve global; en modo usuario, solo propios.
     */
    Page<RackPurchaseOrderDTO> listOrders(String email, Pageable pageable, boolean adminAccess);

    /**
     * Cancela un pedido pendiente y restaura disponibilidad cuando aplica.
     */
    void cancelOrder(String email, Long orderId, boolean adminAccess);

    /**
     * Completa un pedido pendiente y concede acceso funcional al rack comprado.
     */
    void fulfillOrder(Long orderId, String actorEmail);
}
