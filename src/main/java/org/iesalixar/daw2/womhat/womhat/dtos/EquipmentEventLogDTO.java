package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentEventType;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentLogAction;

import java.time.LocalDateTime;

/**
 * DTO de lectura para auditoría de cambios sobre equipos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentEventLogDTO {

    /** ID del evento de auditoría. */
    private Long id;

    /** ID del equipo afectado. */
    private Long equipmentId;

    /** Nombre del equipo afectado. */
    private String equipmentName;

    /** ID del usuario que hizo el cambio. */
    private Long changedByUserId;

    /** Email del usuario que hizo el cambio. */
    private String changedByEmail;

    /** Acción principal (INSERT, UPDATE, DELETE). */
    private EquipmentLogAction action;

    /** Tipo de evento más específico. */
    private EquipmentEventType eventType;

    /** Rack antiguo si el cambio fue movimiento. */
    private Long oldRackId;

    /** Rack nuevo si el cambio fue movimiento. */
    private Long newRackId;

    /** Estado anterior si cambió el estado. */
    private String oldStatus;

    /** Estado nuevo si cambió el estado. */
    private String newStatus;

    /** Mensaje descriptivo del evento. */
    private String message;

    /** Fecha y hora del cambio. */
    private LocalDateTime changedAt;
}