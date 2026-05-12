package org.iesalixar.daw2.womhat.womhat.mappers;

import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentEventLogDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RecentActivityDTO;
import org.iesalixar.daw2.womhat.womhat.entities.EquipmentEventLog;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Mapper para auditoría y actividad reciente de equipos.
 */
public class EquipmentEventLogMapper {

    /**
     * Convierte un evento de auditoría a DTO completo.
     */
    public static EquipmentEventLogDTO toDTO(EquipmentEventLog entity) {
        if (entity == null) {
            return null;
        }

        EquipmentEventLogDTO dto = new EquipmentEventLogDTO();
        dto.setId(entity.getId());
        dto.setAction(entity.getAction());
        dto.setEventType(entity.getEventType());
        dto.setOldRackId(entity.getOldRackId());
        dto.setNewRackId(entity.getNewRackId());
        dto.setOldStatus(entity.getOldStatus());
        dto.setNewStatus(entity.getNewStatus());
        dto.setMessage(entity.getMessage());
        dto.setChangedAt(entity.getChangedAt());

        if (entity.getEquipment() != null) {
            dto.setEquipmentId(entity.getEquipment().getId());
            dto.setEquipmentName(entity.getEquipment().getName());
        }

        if (entity.getChangedBy() != null) {
            dto.setChangedByUserId(entity.getChangedBy().getId());
            dto.setChangedByEmail(entity.getChangedBy().getEmail());
        }

        return dto;
    }

    /**
     * Convierte un evento de auditoría en el DTO ligero del dashboard.
     */
    public static RecentActivityDTO toRecentActivityDTO(EquipmentEventLog entity) {
        if (entity == null) {
            return null;
        }

        String targetLabel = entity.getEquipment() != null ? entity.getEquipment().getName() : "Equipo eliminado";
        String changedByEmail = entity.getChangedBy() != null ? entity.getChangedBy().getEmail() : "system";
        Long equipmentId = entity.getEquipment() != null ? entity.getEquipment().getId() : null;

        return new RecentActivityDTO(
                entity.getId(),
                entity.getChangedAt(),
                changedByEmail,
                equipmentId,
                targetLabel,
                entity.getAction(),
                entity.getEventType(),
                entity.getMessage()
        );
    }

    /**
     * Convierte una colección de eventos a DTOs completos.
     */
    public static List<EquipmentEventLogDTO> toDTOList(Collection<EquipmentEventLog> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .sorted(Comparator.comparing(EquipmentEventLog::getChangedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(EquipmentEventLogMapper::toDTO)
                .toList();
    }

    /**
     * Convierte una colección de eventos en lista de actividad reciente.
     */
    public static List<RecentActivityDTO> toRecentActivityList(Collection<EquipmentEventLog> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .sorted(Comparator.comparing(EquipmentEventLog::getChangedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(EquipmentEventLogMapper::toRecentActivityDTO)
                .toList();
    }
}