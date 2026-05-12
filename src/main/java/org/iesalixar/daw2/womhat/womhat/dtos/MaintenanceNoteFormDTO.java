package org.iesalixar.daw2.womhat.womhat.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de formulario para crear notas de mantenimiento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceNoteFormDTO {

    /** ID de la orden donde se añadirá la nota. */
    @NotNull(message = "{msg.workOrder.note.form.workOrderId.notnull}")
    private Long workOrderId;

    /** Texto de la nota. */
    @NotBlank(message = "{msg.workOrder.note.form.note.notblank}")
    @Size(max = 2000, message = "{msg.workOrder.note.form.note.size}")
    private String note;
}
