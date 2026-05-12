package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentEventType;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentLogAction;

import java.time.LocalDateTime;

/**
 * DTO ligero para la tabla de "últimos cambios" del dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDTO {

    /** ID del evento. */
    private Long id;

    /** Fecha y hora del cambio. */
    private LocalDateTime changedAt;

    /** Usuario que realizó la acción. */
    private String changedByEmail;

    /** ID del equipo afectado. */
    private Long equipmentId;

    /** Etiqueta visible del elemento afectado. */
    private String targetLabel;

    /** Acción realizada. */
    private EquipmentLogAction action;

    /** Tipo de evento. */
    private EquipmentEventType eventType;

    /** Mensaje breve del cambio. */
    private String message;
}