package org.iesalixar.daw2.womhat.womhat.mappers;

import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentFormDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.NetworkElementDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Equipment;
import org.iesalixar.daw2.womhat.womhat.entities.NetworkElement;

/**
 * Mapper para la especialización 1:1 {@link NetworkElement}.
 */
public class NetworkElementMapper {

    /**
     * Convierte la entidad a DTO de lectura.
     */
    public static NetworkElementDTO toDTO(NetworkElement entity) {
        if (entity == null) {
            return null;
        }

        return new NetworkElementDTO(
                entity.getId(),
                entity.getConnection(),
                entity.getTotalPorts()
        );
    }

    /**
     * Copia los campos de red al formulario combinado del equipo.
     */
    public static void copyToFormDTO(NetworkElement entity, EquipmentFormDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        dto.setConnection(entity.getConnection());
        dto.setTotalPorts(entity.getTotalPorts());
    }

    /**
     * Crea la entidad 1:1 a partir del formulario.
     */
    public static NetworkElement toEntity(EquipmentFormDTO dto, Equipment equipment) {
        if (dto == null || equipment == null) {
            return null;
        }

        NetworkElement entity = new NetworkElement();
        entity.setEquipment(equipment);
        entity.setConnection(dto.getConnection());
        entity.setTotalPorts(dto.getTotalPorts());
        return entity;
    }

    /**
     * Copia los datos de red sobre una entidad existente.
     */
    public static void copyToExistingEntity(EquipmentFormDTO dto, NetworkElement entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setConnection(dto.getConnection());
        entity.setTotalPorts(dto.getTotalPorts());
    }
}