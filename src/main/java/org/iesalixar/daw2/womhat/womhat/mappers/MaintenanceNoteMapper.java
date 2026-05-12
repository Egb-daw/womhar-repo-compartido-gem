package org.iesalixar.daw2.womhat.womhat.mappers;

import org.iesalixar.daw2.womhat.womhat.dtos.MaintenanceNoteDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.MaintenanceNoteFormDTO;
import org.iesalixar.daw2.womhat.womhat.entities.MaintenanceNote;
import org.iesalixar.daw2.womhat.womhat.entities.MaintenanceWorkOrder;
import org.iesalixar.daw2.womhat.womhat.entities.User;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Mapper para notas de mantenimiento.
 */
public class MaintenanceNoteMapper {

    /**
     * Convierte una nota a DTO de lectura.
     */
    public static MaintenanceNoteDTO toDTO(MaintenanceNote entity) {
        if (entity == null) {
            return null;
        }

        MaintenanceNoteDTO dto = new MaintenanceNoteDTO();
        dto.setId(entity.getId());
        dto.setNote(entity.getNote());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getWorkOrder() != null) {
            dto.setWorkOrderId(entity.getWorkOrder().getId());
        }

        if (entity.getCreatedBy() != null) {
            dto.setCreatedByUserId(entity.getCreatedBy().getId());
            dto.setCreatedByEmail(entity.getCreatedBy().getEmail());
        }

        return dto;
    }

    /**
     * Convierte una colección de notas a DTOs.
     */
    public static List<MaintenanceNoteDTO> toDTOList(Collection<MaintenanceNote> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .sorted(Comparator.comparing(MaintenanceNote::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(MaintenanceNoteMapper::toDTO)
                .toList();
    }

    /**
     * Crea una nueva entidad Note a partir del formulario.
     */
    public static MaintenanceNote toEntity(MaintenanceNoteFormDTO dto, MaintenanceWorkOrder workOrder, User createdBy) {
        if (dto == null || workOrder == null) {
            return null;
        }

        MaintenanceNote entity = new MaintenanceNote();
        entity.setWorkOrder(workOrder);
        entity.setCreatedBy(createdBy);
        entity.setNote(dto.getNote());
        return entity;
    }

    /**
     * Copia el texto de la nota sobre una entidad existente.
     */
    public static void copyToExistingEntity(MaintenanceNoteFormDTO dto, MaintenanceNote entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setNote(dto.getNote());
    }
}