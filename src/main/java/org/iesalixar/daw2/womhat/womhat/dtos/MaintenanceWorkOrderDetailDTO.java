package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderPriority;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de detalle completo para órdenes de mantenimiento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceWorkOrderDetailDTO {

    /** ID de la orden. */
    private Long id;

    /** ID del equipo afectado. */
    private Long equipmentId;

    /** Nombre del equipo afectado. */
    private String equipmentName;

    /** ID del usuario creador. */
    private Long createdByUserId;

    /** Email del usuario creador. */
    private String createdByEmail;

    /** Estado de la orden. */
    private WorkOrderStatus status;

    /** Prioridad de la orden. */
    private WorkOrderPriority priority;

    /** Resumen breve. */
    private String summary;

    /** Descripción detallada. */
    private String details;

    /** Fecha de apertura. */
    private LocalDateTime openedAt;

    /** Fecha de cierre. */
    private LocalDateTime closedAt;

    /** Notas asociadas a la orden. */
    private List<MaintenanceNoteDTO> notes = new ArrayList<>();
}