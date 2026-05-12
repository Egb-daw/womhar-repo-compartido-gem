package org.iesalixar.daw2.womhat.womhat.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderPriority;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderStatus;

/**
 * DTO de formulario para crear y editar órdenes de mantenimiento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceWorkOrderFormDTO {

    /** ID de la orden. En alta será null. */
    private Long id;

    /** Equipo al que pertenece la orden. */
    @NotNull(message = "{msg.workOrder.form.equipmentId.notnull}")
    private Long equipmentId;

    /** Estado actual de la orden. */
    @NotNull(message = "{msg.workOrder.form.status.notnull}")
    private WorkOrderStatus status = WorkOrderStatus.OPEN;

    /** Prioridad de la orden. */
    @NotNull(message = "{msg.workOrder.form.priority.notnull}")
    private WorkOrderPriority priority = WorkOrderPriority.MEDIUM;

    /** Resumen breve de la tarea. */
    @NotBlank(message = "{msg.workOrder.form.summary.notblank}")
    @Size(max = 180, message = "{msg.workOrder.form.summary.size}")
    private String summary;

    /** Descripción opcional ampliada. */
    @Size(max = 2000, message = "{msg.workOrder.form.details.size}")
    private String details;
}
