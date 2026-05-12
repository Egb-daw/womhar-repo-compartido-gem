package org.iesalixar.daw2.womhat.womhat.mappers;

import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterRoomDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterRoomDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterRoomFormDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RoomOptionDTO;
import org.iesalixar.daw2.womhat.womhat.entities.DataCenter;
import org.iesalixar.daw2.womhat.womhat.entities.DataCenterRoom;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Mapper utilitario para salas de CPD.
 */
public class DataCenterRoomMapper {

    /**
     * Convierte una sala a DTO de listado.
     */
    public static DataCenterRoomDTO toDTO(DataCenterRoom entity) {
        if (entity == null) {
            return null;
        }

        DataCenterRoomDTO dto = new DataCenterRoomDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setFloor(entity.getFloor());
        dto.setNotes(entity.getNotes());
        dto.setRackCount(entity.getRacks() != null ? entity.getRacks().size() : 0);

        if (entity.getDataCenter() != null) {
            dto.setDataCenterId(entity.getDataCenter().getId());
            dto.setDataCenterCode(entity.getDataCenter().getCode());
            dto.setDataCenterName(entity.getDataCenter().getName());
        }

        return dto;
    }

    /**
     * Convierte una sala a DTO de detalle con sus racks.
     */
    public static DataCenterRoomDetailDTO toDetailDTO(DataCenterRoom entity) {
        if (entity == null) {
            return null;
        }

        DataCenterRoomDetailDTO dto = new DataCenterRoomDetailDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setFloor(entity.getFloor());
        dto.setNotes(entity.getNotes());

        if (entity.getDataCenter() != null) {
            dto.setDataCenterId(entity.getDataCenter().getId());
            dto.setDataCenterCode(entity.getDataCenter().getCode());
            dto.setDataCenterName(entity.getDataCenter().getName());
        }

        if (entity.getRacks() != null) {
            dto.setRacks(
                    entity.getRacks().stream()
                            .sorted(Comparator.comparing(rack -> rack.getLocationLabel(), Comparator.nullsLast(String::compareToIgnoreCase)))
                            .map(RackMapper::toDTO)
                            .toList()
            );
        }

        return dto;
    }

    /**
     * Convierte una sala a DTO simple para selects.
     */
    public static RoomOptionDTO toOptionDTO(DataCenterRoom entity) {
        if (entity == null) {
            return null;
        }

        RoomOptionDTO dto = new RoomOptionDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setFloor(entity.getFloor());

        if (entity.getDataCenter() != null) {
            dto.setDataCenterCode(entity.getDataCenter().getCode());
            dto.setDataCenterName(entity.getDataCenter().getName());
        }

        return dto;
    }

    /**
     * Convierte una colección de salas a DTOs.
     */
    public static List<DataCenterRoomDTO> toDTOList(Collection<DataCenterRoom> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .sorted(Comparator.comparing(DataCenterRoom::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(DataCenterRoomMapper::toDTO)
                .toList();
    }

    /**
     * Convierte una colección de salas en lista de opciones para formularios.
     */
    public static List<RoomOptionDTO> toOptionList(Collection<DataCenterRoom> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .sorted(Comparator.comparing(DataCenterRoom::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(DataCenterRoomMapper::toOptionDTO)
                .toList();
    }

    /**
     * Convierte una entidad a DTO de formulario.
     */
    public static DataCenterRoomFormDTO toFormDTO(DataCenterRoom entity) {
        if (entity == null) {
            return null;
        }

        Long dataCenterId = entity.getDataCenter() != null ? entity.getDataCenter().getId() : null;

        return new DataCenterRoomFormDTO(
                entity.getId(),
                dataCenterId,
                entity.getName(),
                entity.getFloor(),
                entity.getNotes()
        );
    }

    /**
     * Crea una nueva entidad Room a partir del formulario y del CPD ya resuelto.
     */
    public static DataCenterRoom toEntity(DataCenterRoomFormDTO dto, DataCenter dataCenter) {
        if (dto == null) {
            return null;
        }

        DataCenterRoom entity = new DataCenterRoom();
        entity.setDataCenter(dataCenter);
        entity.setName(dto.getName());
        entity.setFloor(dto.getFloor());
        entity.setNotes(dto.getNotes());
        return entity;
    }

    /**
     * Copia los campos editables del formulario sobre una entidad existente.
     */
    public static void copyToExistingEntity(DataCenterRoomFormDTO dto, DataCenterRoom entity, DataCenter dataCenter) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setDataCenter(dataCenter);
        entity.setName(dto.getName());
        entity.setFloor(dto.getFloor());
        entity.setNotes(dto.getNotes());
    }
}