package org.iesalixar.daw2.womhat.womhat.mappers;

import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterFormDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DataCenterOptionDTO;
import org.iesalixar.daw2.womhat.womhat.entities.DataCenter;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Mapper utilitario para convertir entre {@link DataCenter} y sus DTOs.
 */
public class DataCenterMapper {

    /**
     * Convierte una entidad de CPD a DTO de listado.
     */
    public static DataCenterDTO toDTO(DataCenter entity) {
        if (entity == null) {
            return null;
        }

        Integer roomCount = entity.getRooms() != null ? entity.getRooms().size() : 0;

        return new DataCenterDTO(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getCity(),
                entity.getBuilding(),
                entity.getStatus(),
                roomCount
        );
    }

    /**
     * Convierte un CPD a DTO de detalle con sus salas.
     */
    public static DataCenterDetailDTO toDetailDTO(DataCenter entity) {
        if (entity == null) {
            return null;
        }

        DataCenterDetailDTO dto = new DataCenterDetailDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setCity(entity.getCity());
        dto.setBuilding(entity.getBuilding());
        dto.setStatus(entity.getStatus());

        if (entity.getRooms() != null) {
            dto.setRooms(
                    entity.getRooms().stream()
                            .sorted(Comparator.comparing(room -> room.getName(), Comparator.nullsLast(String::compareToIgnoreCase)))
                            .map(DataCenterRoomMapper::toDTO)
                            .toList()
            );
        }

        return dto;
    }

    /**
     * Convierte un CPD a DTO simple para selects.
     */
    public static DataCenterOptionDTO toOptionDTO(DataCenter entity) {
        if (entity == null) {
            return null;
        }

        return new DataCenterOptionDTO(entity.getId(), entity.getCode(), entity.getName());
    }

    /**
     * Convierte una colección de CPDs en lista de DTOs de lectura.
     */
    public static List<DataCenterDTO> toDTOList(Collection<DataCenter> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .sorted(Comparator.comparing(DataCenter::getCode, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(DataCenterMapper::toDTO)
                .toList();
    }

    /**
     * Convierte una colección de CPDs en lista de opciones para formularios.
     */
    public static List<DataCenterOptionDTO> toOptionList(Collection<DataCenter> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .sorted(Comparator.comparing(DataCenter::getCode, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(DataCenterMapper::toOptionDTO)
                .toList();
    }

    /**
     * Crea una nueva entidad a partir del formulario de CPD.
     */
    public static DataCenter toEntity(DataCenterFormDTO dto) {
        if (dto == null) {
            return null;
        }

        DataCenter entity = new DataCenter();
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setCity(dto.getCity());
        entity.setBuilding(dto.getBuilding());
        entity.setStatus(dto.getStatus());
        return entity;
    }

    /**
     * Convierte una entidad a DTO de formulario para edición.
     */
    public static DataCenterFormDTO toFormDTO(DataCenter entity) {
        if (entity == null) {
            return null;
        }

        return new DataCenterFormDTO(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getCity(),
                entity.getBuilding(),
                entity.getStatus()
        );
    }

    /**
     * Copia los campos editables del formulario sobre una entidad existente.
     */
    public static void copyToExistingEntity(DataCenterFormDTO dto, DataCenter entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setCity(dto.getCity());
        entity.setBuilding(dto.getBuilding());
        entity.setStatus(dto.getStatus());
    }
}