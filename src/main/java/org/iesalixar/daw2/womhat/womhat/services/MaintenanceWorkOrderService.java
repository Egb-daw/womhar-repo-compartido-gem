package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.MaintenanceWorkOrderDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.MaintenanceWorkOrderDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.MaintenanceWorkOrderFormDTO;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderPriority;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderStatus;

import java.util.List;

/**
 * Servicio de negocio para órdenes de mantenimiento.
 */
public interface MaintenanceWorkOrderService {

    /**
     * Lista órdenes vinculadas a un equipo aplicando permisos por rack.
     */
    List<MaintenanceWorkOrderDTO> listByEquipment(Long equipmentId, String actorEmail);

    /**
     * Lista órdenes por estado aplicando visibilidad según rol/permisos.
     */
    List<MaintenanceWorkOrderDTO> listByStatus(WorkOrderStatus status, String actorEmail);

    /**
     * Lista órdenes por prioridad aplicando visibilidad según rol/permisos.
     */
    List<MaintenanceWorkOrderDTO> listByPriority(WorkOrderPriority priority, String actorEmail);

    /**
     * Devuelve el detalle completo de una orden concreta.
     */
    MaintenanceWorkOrderDetailDTO getDetail(Long id, String actorEmail);

    /**
     * Devuelve el DTO de formulario para edición de una orden existente.
     */
    MaintenanceWorkOrderFormDTO getForm(Long id, String actorEmail);

    /**
     * Crea una orden de mantenimiento y registra trazabilidad en histórico de equipo.
     */
    void create(MaintenanceWorkOrderFormDTO dto, String actorEmail);

    /**
     * Actualiza una orden y registra cambios relevantes de estado en auditoría.
     */
    void update(MaintenanceWorkOrderFormDTO dto, String actorEmail);

    /**
     * Elimina una orden (acción reservada a administración global).
     */
    void delete(Long id, String actorEmail);
}
