package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderPriority;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderStatus;

import java.time.LocalDateTime;

/**
 * DTO genérico de lectura para órdenes de mantenimiento.
 *
 * Pensado para listados y tablas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceWorkOrderDTO {

    /** ID de la orden de trabajo. */
    private Long id;

    /** ID del equipo afectado. */
    private Long equipmentId;

    /** Nombre del equipo afectado. */
    private String equipmentName;

    /** Estado actual de la orden. */
    private WorkOrderStatus status;

    /** Prioridad de la orden. */
    private WorkOrderPriority priority;

    /** Resumen breve de la incidencia o tarea. */
    private String summary;

    /** Fecha de apertura. */
    private LocalDateTime openedAt;

    /** Fecha de cierre, si existe. */
    private LocalDateTime closedAt;

    /** Número de notas asociadas. */
    private Integer notesCount;
}