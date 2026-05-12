package org.iesalixar.daw2.womhat.womhat.mappers;

import org.iesalixar.daw2.womhat.womhat.dtos.*;
import org.iesalixar.daw2.womhat.womhat.entities.DataCenterRoom;
import org.iesalixar.daw2.womhat.womhat.entities.Rack;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Mapper utilitario para la entidad {@link Rack}.
 */
public class RackMapper {

    /**
     * Convierte un rack a DTO de listado.
     */
    public static RackDTO toDTO(Rack entity) {
        if (entity == null) {
            return null;
        }

        RackDTO dto = new RackDTO();
        dto.setId(entity.getId());
        dto.setLocationLabel(entity.getLocationLabel());
        dto.setCapacityU(entity.getCapacityU());
        dto.setFunctionName(entity.getFunctionName());
        dto.setGroupName(entity.getGroupName());
        dto.setDimension(entity.getDimension());
        dto.setPositionX(entity.getPositionX());
        dto.setPositionY(entity.getPositionY());
        dto.setStatus(entity.getStatus());
        dto.setEquipmentCount(entity.getEquipments() != null ? entity.getEquipments().size() : 0);
        dto.setCatalogVisible(entity.isCatalogVisible());
        dto.setCatalogPrice(entity.getCatalogPrice());
        dto.setCatalogStock(entity.getCatalogStock());
        dto.setCatalogSummary(entity.getCatalogSummary());
        dto.setCatalogDisplayName(buildCatalogDisplayName(entity));

        if (entity.getRoom() != null) {
            dto.setRoomId(entity.getRoom().getId());
            dto.setRoomName(entity.getRoom().getName());

            if (entity.getRoom().getDataCenter() != null) {
                dto.setDataCenterCode(entity.getRoom().getDataCenter().getCode());
                dto.setDataCenterName(entity.getRoom().getDataCenter().getName());
            }
        }

        return dto;
    }

    /**
     * Convierte un rack a DTO de detalle con cálculos de ocupación.
     */
    public static RackDetailDTO toDetailDTO(Rack entity) {
        if (entity == null) {
            return null;
        }

        RackDetailDTO dto = new RackDetailDTO();
        dto.setId(entity.getId());
        dto.setLocationLabel(entity.getLocationLabel());
        dto.setCapacityU(entity.getCapacityU());
        dto.setFunctionName(entity.getFunctionName());
        dto.setGroupName(entity.getGroupName());
        dto.setDimension(entity.getDimension());
        dto.setPositionX(entity.getPositionX());
        dto.setPositionY(entity.getPositionY());
        dto.setStatus(entity.getStatus());
        dto.setCatalogVisible(entity.isCatalogVisible());
        dto.setCatalogPrice(entity.getCatalogPrice());
        dto.setCatalogStock(entity.getCatalogStock());
        dto.setCatalogSummary(entity.getCatalogSummary());
        dto.setCatalogDisplayName(buildCatalogDisplayName(entity));

        int equipmentCount = entity.getEquipments() != null ? entity.getEquipments().size() : 0;
        int occupiedU = calculateOccupiedU(entity);
        int freeU = calculateFreeU(entity);
        int occupancyPercent = calculateOccupancyPercent(entity);

        dto.setEquipmentCount(equipmentCount);
        dto.setOccupiedU(occupiedU);
        dto.setFreeU(freeU);
        dto.setOccupancyPercent(occupancyPercent);

        if (entity.getRoom() != null) {
            dto.setRoomId(entity.getRoom().getId());
            dto.setRoomName(entity.getRoom().getName());
            dto.setFloor(entity.getRoom().getFloor());

            if (entity.getRoom().getDataCenter() != null) {
                dto.setDataCenterCode(entity.getRoom().getDataCenter().getCode());
                dto.setDataCenterName(entity.getRoom().getDataCenter().getName());
            }
        }

        if (entity.getEquipments() != null) {
            dto.setEquipments(
                    entity.getEquipments().stream()
                            .sorted(Comparator
                                    .comparing((org.iesalixar.daw2.womhat.womhat.entities.Equipment equipment) -> equipment.getSlotPositionU(), Comparator.nullsLast(Integer::compareTo))
                                    .thenComparing(org.iesalixar.daw2.womhat.womhat.entities.Equipment::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                            .map(EquipmentMapper::toCompactDTO)
                            .toList()
            );
        }

        if (entity.getUserAccess() != null) {
            dto.setUserAccessList(UserRackAccessMapper.toDTOList(entity.getUserAccess()));
        }

        return dto;
    }

    /**
     * Convierte un rack a DTO de opción para selects.
     */
    public static RackOptionDTO toOptionDTO(Rack entity) {
        if (entity == null) {
            return null;
        }

        RackOptionDTO dto = new RackOptionDTO();
        dto.setId(entity.getId());
        dto.setLocationLabel(entity.getLocationLabel());
        dto.setCapacityU(entity.getCapacityU());
        dto.setFunctionName(entity.getFunctionName());
        dto.setStatus(entity.getStatus());

        if (entity.getRoom() != null) {
            dto.setRoomName(entity.getRoom().getName());
        }

        return dto;
    }

    /**
     * Convierte un rack al DTO usado por el mapa del dashboard.
     */
    public static DashboardRackMapItemDTO toMapItemDTO(Rack entity) {
        if (entity == null) {
            return null;
        }

        return new DashboardRackMapItemDTO(
                entity.getId(),
                entity.getLocationLabel(),
                entity.getRoom() != null ? entity.getRoom().getName() : null,
                entity.getRoom() != null && entity.getRoom().getDataCenter() != null
                        ? entity.getRoom().getDataCenter().getName()
                        : null,
                entity.getPositionX(),
                entity.getPositionY(),
                entity.getCapacityU(),
                calculateOccupiedU(entity),
                calculateFreeU(entity),
                calculateOccupancyPercent(entity),
                entity.getEquipments() != null ? entity.getEquipments().size() : 0,
                entity.getStatus(),
                null,
                false
        );
    }

    /**
     * Convierte una colección de racks en lista de DTOs.
     */
    public static List<RackDTO> toDTOList(Collection<Rack> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .sorted(Comparator.comparing(Rack::getLocationLabel, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(RackMapper::toDTO)
                .toList();
    }

    /**
     * Convierte una colección de racks en lista de opciones para formularios.
     */
    public static List<RackOptionDTO> toOptionList(Collection<Rack> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .sorted(Comparator.comparing(Rack::getLocationLabel, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(RackMapper::toOptionDTO)
                .toList();
    }

    /**
     * Convierte una entidad rack a DTO de formulario.
     */
    public static RackFormDTO toFormDTO(Rack entity) {
        if (entity == null) {
            return null;
        }

        Long roomId = entity.getRoom() != null ? entity.getRoom().getId() : null;

        return new RackFormDTO(
                entity.getId(),
                roomId,
                entity.getLocationLabel(),
                entity.getCapacityU(),
                entity.getFunctionName(),
                entity.getGroupName(),
                entity.getDimension(),
                entity.getPositionX(),
                entity.getPositionY(),
                entity.getStatus(),
                entity.isCatalogVisible(),
                entity.getCatalogPrice(),
                entity.getCatalogStock(),
                entity.getCatalogSummary()
        );
    }

    /**
     * Crea un rack nuevo a partir del formulario y la sala ya resuelta.
     */
    public static Rack toEntity(RackFormDTO dto, DataCenterRoom room) {
        if (dto == null) {
            return null;
        }

        Rack entity = new Rack();
        entity.setRoom(room);
        entity.setLocationLabel(dto.getLocationLabel());
        entity.setCapacityU(dto.getCapacityU());
        entity.setFunctionName(dto.getFunctionName());
        entity.setGroupName(dto.getGroupName());
        entity.setDimension(dto.getDimension());
        entity.setPositionX(dto.getPositionX());
        entity.setPositionY(dto.getPositionY());
        entity.setStatus(dto.getStatus());
        entity.setCatalogVisible(dto.isCatalogVisible());
        entity.setCatalogPrice(dto.getCatalogPrice());
        entity.setCatalogStock(dto.getCatalogStock());
        entity.setCatalogSummary(dto.getCatalogSummary());
        return entity;
    }

    /**
     * Copia los campos editables del formulario sobre un rack ya cargado.
     */
    public static void copyToExistingEntity(RackFormDTO dto, Rack entity, DataCenterRoom room) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setRoom(room);
        entity.setLocationLabel(dto.getLocationLabel());
        entity.setCapacityU(dto.getCapacityU());
        entity.setFunctionName(dto.getFunctionName());
        entity.setGroupName(dto.getGroupName());
        entity.setDimension(dto.getDimension());
        entity.setPositionX(dto.getPositionX());
        entity.setPositionY(dto.getPositionY());
        entity.setStatus(dto.getStatus());
        entity.setCatalogVisible(dto.isCatalogVisible());
        entity.setCatalogPrice(dto.getCatalogPrice());
        entity.setCatalogStock(dto.getCatalogStock());
        entity.setCatalogSummary(dto.getCatalogSummary());
    }

    /**
     * Calcula el total de U ocupadas por los equipos de un rack.
     */
    public static int calculateOccupiedU(Rack entity) {
        if (entity == null || entity.getEquipments() == null || entity.getEquipments().isEmpty()) {
            return 0;
        }

        return entity.getEquipments().stream()
                .map(equipment -> equipment.getSlotHeightU() != null ? equipment.getSlotHeightU() : 0)
                .reduce(0, Integer::sum);
    }

    /**
     * Calcula las U libres del rack.
     */
    public static int calculateFreeU(Rack entity) {
        if (entity == null || entity.getCapacityU() == null) {
            return 0;
        }

        return Math.max(entity.getCapacityU() - calculateOccupiedU(entity), 0);
    }

    /**
     * Calcula el porcentaje de ocupación del rack.
     */
    public static int calculateOccupancyPercent(Rack entity) {
        if (entity == null || entity.getCapacityU() == null || entity.getCapacityU() <= 0) {
            return 0;
        }

        return (int) Math.round((calculateOccupiedU(entity) * 100.0) / entity.getCapacityU());
    }

    /**
     * Genera un nombre comercial corto para catálogo a partir del rack físico.
     * Ejemplo: "Rack Red 42U 600x1000".
     */
    public static String buildCatalogDisplayName(Rack entity) {
        if (entity == null) {
            return null;
        }

        String functionName = normalizeText(entity.getFunctionName());
        String dimension = normalizeText(entity.getDimension());
        Integer capacity = entity.getCapacityU();
        String capacityToken = (capacity != null && capacity > 0) ? (capacity + "U") : null;
        boolean capacityAlreadyIncluded = capacityToken != null
                && dimension != null
                && dimension.toUpperCase().contains(capacityToken.toUpperCase());

        StringBuilder displayName = new StringBuilder("Rack");
        if (functionName != null) {
            displayName.append(' ').append(functionName);
        }
        if (capacityToken != null && !capacityAlreadyIncluded) {
            displayName.append(' ').append(capacityToken);
        }
        if (dimension != null) {
            displayName.append(' ').append(dimension);
        }

        return displayName.toString().replaceAll("\\s+", " ").trim();
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
