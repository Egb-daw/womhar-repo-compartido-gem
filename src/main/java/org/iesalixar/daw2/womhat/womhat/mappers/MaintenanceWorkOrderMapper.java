package org.iesalixar.daw2.womhat.womhat.mappers;

import org.iesalixar.daw2.womhat.womhat.dtos.MaintenanceWorkOrderDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.MaintenanceWorkOrderDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.MaintenanceWorkOrderFormDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Equipment;
import org.iesalixar.daw2.womhat.womhat.entities.MaintenanceWorkOrder;
import org.iesalixar.daw2.womhat.womhat.entities.User;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Mapper para órdenes de mantenimiento.
 */
public class MaintenanceWorkOrderMapper {

    /**
     * Convierte una orden a DTO de listado.
     */
    public static MaintenanceWorkOrderDTO toDTO(MaintenanceWorkOrder entity) {
        if (entity == null) {
            return null;
        }

        MaintenanceWorkOrderDTO dto = new MaintenanceWorkOrderDTO();
        dto.setId(entity.getId());
        dto.setStatus(entity.getStatus());
        dto.setPriority(entity.getPriority());
        dto.setSummary(entity.getSummary());
        dto.setOpenedAt(entity.getOpenedAt());
        dto.setClosedAt(entity.getClosedAt());
        dto.setNotesCount(entity.getNotes() != null ? entity.getNotes().size() : 0);

        if (entity.getEquipment() != null) {
            dto.setEquipmentId(entity.getEquipment().getId());
            dto.setEquipmentName(entity.getEquipment().getName());
        }

        return dto;
    }

    /**
     * Convierte una orden a DTO de detalle.
     */
    public static MaintenanceWorkOrderDetailDTO toDetailDTO(MaintenanceWorkOrder entity) {
        if (entity == null) {
            return null;
        }

        MaintenanceWorkOrderDetailDTO dto = new MaintenanceWorkOrderDetailDTO();
        dto.setId(entity.getId());
        dto.setStatus(entity.getStatus());
        dto.setPriority(entity.getPriority());
        dto.setSummary(entity.getSummary());
        dto.setDetails(entity.getDetails());
        dto.setOpenedAt(entity.getOpenedAt());
        dto.setClosedAt(entity.getClosedAt());

        if (entity.getEquipment() != null) {
            dto.setEquipmentId(entity.getEquipment().getId());
            dto.setEquipmentName(entity.getEquipment().getName());
        }

        if (entity.getCreatedBy() != null) {
            dto.setCreatedByUserId(entity.getCreatedBy().getId());
            dto.setCreatedByEmail(entity.getCreatedBy().getEmail());
        }

        if (entity.getNotes() != null) {
            dto.setNotes(
                    entity.getNotes().stream()
                            .sorted(Comparator.comparing(note -> note.getCreatedAt(), Comparator.nullsLast(Comparator.naturalOrder())))
                            .map(MaintenanceNoteMapper::toDTO)
                            .toList()
            );
        }

        return dto;
    }

    /**
     * Convierte una colección de órdenes en lista de DTOs.
     */
    public static List<MaintenanceWorkOrderDTO> toDTOList(Collection<MaintenanceWorkOrder> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .sorted(Comparator.comparing(MaintenanceWorkOrder::getOpenedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(MaintenanceWorkOrderMapper::toDTO)
                .toList();
    }

    /**
     * Convierte una entidad a DTO de formulario para edición.
     */
    public static MaintenanceWorkOrderFormDTO toFormDTO(MaintenanceWorkOrder entity) {
        if (entity == null) {
            return null;
        }

        Long equipmentId = entity.getEquipment() != null ? entity.getEquipment().getId() : null;

        return new MaintenanceWorkOrderFormDTO(
                entity.getId(),
                equipmentId,
                entity.getStatus(),
                entity.getPriority(),
                entity.getSummary(),
                entity.getDetails()
        );
    }

    /**
     * Crea una nueva orden a partir del formulario.
     */
    public static MaintenanceWorkOrder toEntity(MaintenanceWorkOrderFormDTO dto, Equipment equipment, User createdBy) {
        if (dto == null || equipment == null) {
            return null;
        }

        MaintenanceWorkOrder entity = new MaintenanceWorkOrder();
        entity.setEquipment(equipment);
        entity.setCreatedBy(createdBy);
        entity.setStatus(dto.getStatus());
        entity.setPriority(dto.getPriority());
        entity.setSummary(dto.getSummary());
        entity.setDetails(dto.getDetails());
        return entity;
    }

    /**
     * Copia los datos editables del formulario sobre una orden existente.
     */
    public static void copyToExistingEntity(MaintenanceWorkOrderFormDTO dto, MaintenanceWorkOrder entity, Equipment equipment) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setEquipment(equipment);
        entity.setStatus(dto.getStatus());
        entity.setPriority(dto.getPriority());
        entity.setSummary(dto.getSummary());
        entity.setDetails(dto.getDetails());
    }
}