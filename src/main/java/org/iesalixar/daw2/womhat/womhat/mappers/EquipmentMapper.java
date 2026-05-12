package org.iesalixar.daw2.womhat.womhat.mappers;

import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentCompactDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentFormDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Equipment;
import org.iesalixar.daw2.womhat.womhat.entities.Rack;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Mapper utilitario para la entidad {@link Equipment}.
 */
public class EquipmentMapper {

    /**
     * Convierte un equipo a DTO de listado.
     */
    public static EquipmentDTO toDTO(Equipment entity) {
        if (entity == null) {
            return null;
        }

        EquipmentDTO dto = new EquipmentDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setSerialNumber(entity.getSerialNumber());
        dto.setPrimaryIp(entity.getPrimaryIp());
        dto.setManagementIp(entity.getManagementIp());
        dto.setVlanId(entity.getVlanId());
        dto.setMacAddress(entity.getMacAddress());
        dto.setSlotPositionU(entity.getSlotPositionU());
        dto.setSlotHeightU(entity.getSlotHeightU());
        dto.setStatus(entity.getStatus());
        dto.setLastUpdate(entity.getLastUpdate());

        if (entity.getRack() != null) {
            dto.setRackId(entity.getRack().getId());
            dto.setRackLocationLabel(entity.getRack().getLocationLabel());

            if (entity.getRack().getRoom() != null) {
                dto.setRoomName(entity.getRack().getRoom().getName());
                if (entity.getRack().getRoom().getDataCenter() != null) {
                    dto.setDataCenterCode(entity.getRack().getRoom().getDataCenter().getCode());
                }
            }
        }

        return dto;
    }

    /**
     * Convierte un equipo a DTO compacto para racks y mapas.
     */
    public static EquipmentCompactDTO toCompactDTO(Equipment entity) {
        if (entity == null) {
            return null;
        }

        return new EquipmentCompactDTO(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                entity.getSerialNumber(),
                entity.getSlotPositionU(),
                entity.getSlotHeightU(),
                entity.getStatus()
        );
    }

    /**
     * Convierte un equipo a DTO de detalle.
     */
    public static EquipmentDetailDTO toDetailDTO(Equipment entity) {
        if (entity == null) {
            return null;
        }

        EquipmentDetailDTO dto = new EquipmentDetailDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setSerialNumber(entity.getSerialNumber());
        dto.setPrimaryIp(entity.getPrimaryIp());
        dto.setManagementIp(entity.getManagementIp());
        dto.setVlanId(entity.getVlanId());
        dto.setMacAddress(entity.getMacAddress());
        dto.setSlotPositionU(entity.getSlotPositionU());
        dto.setSlotHeightU(entity.getSlotHeightU());
        dto.setStatus(entity.getStatus());
        dto.setLastUpdate(entity.getLastUpdate());

        if (entity.getRack() != null) {
            dto.setRackId(entity.getRack().getId());
            dto.setRackLocationLabel(entity.getRack().getLocationLabel());

            if (entity.getRack().getRoom() != null) {
                dto.setRoomName(entity.getRack().getRoom().getName());
                if (entity.getRack().getRoom().getDataCenter() != null) {
                    dto.setDataCenterCode(entity.getRack().getRoom().getDataCenter().getCode());
                    dto.setDataCenterName(entity.getRack().getRoom().getDataCenter().getName());
                }
            }
        }

        dto.setHostSpecification(HostSpecificationMapper.toDTO(entity.getHostSpecification()));
        dto.setNetworkElement(NetworkElementMapper.toDTO(entity.getNetworkElement()));
        dto.setStorageBackup(StorageBackupMapper.toDTO(entity.getStorageBackup()));

        if (entity.getWorkOrders() != null) {
            dto.setWorkOrders(
                    entity.getWorkOrders().stream()
                            .sorted(Comparator.comparing(workOrder -> workOrder.getOpenedAt(), Comparator.nullsLast(Comparator.reverseOrder())))
                            .map(MaintenanceWorkOrderMapper::toDTO)
                            .toList()
            );
        }

        return dto;
    }

    /**
     * Convierte una colección de equipos en lista de DTOs.
     */
    public static List<EquipmentDTO> toDTOList(Collection<Equipment> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .sorted(Comparator.comparing(Equipment::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(EquipmentMapper::toDTO)
                .toList();
    }

    /**
     * Convierte un equipo a DTO de formulario, incluyendo especializaciones 1:1.
     */
    public static EquipmentFormDTO toFormDTO(Equipment entity) {
        if (entity == null) {
            return null;
        }

        Long rackId = entity.getRack() != null ? entity.getRack().getId() : null;

        EquipmentFormDTO dto = new EquipmentFormDTO();
        dto.setId(entity.getId());
        dto.setRackId(rackId);
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setSerialNumber(entity.getSerialNumber());
        dto.setPrimaryIp(entity.getPrimaryIp());
        dto.setManagementIp(entity.getManagementIp());
        dto.setVlanId(entity.getVlanId());
        dto.setMacAddress(entity.getMacAddress());
        dto.setSlotPositionU(entity.getSlotPositionU());
        dto.setSlotHeightU(entity.getSlotHeightU());
        dto.setStatus(entity.getStatus());

        HostSpecificationMapper.copyToFormDTO(entity.getHostSpecification(), dto);
        NetworkElementMapper.copyToFormDTO(entity.getNetworkElement(), dto);
        StorageBackupMapper.copyToFormDTO(entity.getStorageBackup(), dto);

        return dto;
    }

    /**
     * Crea una nueva entidad base Equipment desde el formulario.
     *
     * Las especializaciones 1:1 se mapean con sus mappers específicos.
     */
    public static Equipment toEntity(EquipmentFormDTO dto, Rack rack) {
        if (dto == null) {
            return null;
        }

        Equipment entity = new Equipment();
        entity.setRack(rack);
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setSerialNumber(dto.getSerialNumber());
        entity.setPrimaryIp(dto.getPrimaryIp());
        entity.setManagementIp(dto.getManagementIp());
        entity.setVlanId(dto.getVlanId());
        entity.setMacAddress(dto.getMacAddress());
        entity.setSlotPositionU(dto.getSlotPositionU());
        entity.setSlotHeightU(dto.getSlotHeightU());
        entity.setStatus(dto.getStatus());
        return entity;
    }

    /**
     * Copia los campos base del formulario sobre una entidad existente.
     */
    public static void copyToExistingEntity(EquipmentFormDTO dto, Equipment entity, Rack rack) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setRack(rack);
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setSerialNumber(dto.getSerialNumber());
        entity.setPrimaryIp(dto.getPrimaryIp());
        entity.setManagementIp(dto.getManagementIp());
        entity.setVlanId(dto.getVlanId());
        entity.setMacAddress(dto.getMacAddress());
        entity.setSlotPositionU(dto.getSlotPositionU());
        entity.setSlotHeightU(dto.getSlotHeightU());
        entity.setStatus(dto.getStatus());
    }
}