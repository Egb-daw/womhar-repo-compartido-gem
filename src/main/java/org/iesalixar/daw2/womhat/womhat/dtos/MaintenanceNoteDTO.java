package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de lectura para notas de mantenimiento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceNoteDTO {

    /** ID de la nota. */
    private Long id;

    /** ID de la orden de trabajo asociada. */
    private Long workOrderId;

    /** ID del usuario autor de la nota. */
    private Long createdByUserId;

    /** Email del autor de la nota. */
    private String createdByEmail;

    /** Contenido de la nota. */
    private String note;

    /** Fecha de creación. */
    private LocalDateTime createdAt;
}