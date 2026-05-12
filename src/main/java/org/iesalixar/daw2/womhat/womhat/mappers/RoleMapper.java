package org.iesalixar.daw2.womhat.womhat.mappers;

import org.iesalixar.daw2.womhat.womhat.dtos.RoleDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Role;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Mapper utilitario entre la entidad {@link Role} y sus DTOs.
 *
 * Se usa principalmente en listados de usuarios y formularios donde
 * hay que mostrar los roles disponibles en selects o tablas.
 */
public class RoleMapper {

    /**
     * Convierte una entidad {@link Role} a {@link RoleDTO}.
     */
    public static RoleDTO toDTO(Role entity) {
        if (entity == null) {
            return null;
        }

        return new RoleDTO(
                entity.getId(),
                entity.getName(),
                entity.getDisplayName(),
                entity.getDescription()
        );
    }

    /**
     * Convierte una colección de roles en una lista de DTOs ordenada por displayName.
     */
    public static List<RoleDTO> toDTOList(Collection<Role> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .sorted(Comparator.comparing(Role::getDisplayName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(RoleMapper::toDTO)
                .toList();
    }
}