package org.iesalixar.daw2.womhat.womhat.mappers;

import org.iesalixar.daw2.womhat.womhat.dtos.UserRackAccessDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Rack;
import org.iesalixar.daw2.womhat.womhat.entities.User;
import org.iesalixar.daw2.womhat.womhat.entities.UserRackAccess;
import org.iesalixar.daw2.womhat.womhat.entities.UserRackAccessId;
import org.iesalixar.daw2.womhat.womhat.enums.RackPermission;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Mapper para la entidad {@link UserRackAccess}.
 */
public class UserRackAccessMapper {

    /**
     * Convierte una entidad de acceso a rack a DTO de lectura.
     */
    public static UserRackAccessDTO toDTO(UserRackAccess entity) {
        if (entity == null) {
            return null;
        }

        UserRackAccessDTO dto = new UserRackAccessDTO();
        dto.setPermission(entity.getPermission());
        dto.setOriginalOwner(entity.isOriginalOwner());
        dto.setGrantedAt(entity.getGrantedAt());

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUserEmail(entity.getUser().getEmail());
        }

        if (entity.getGrantedBy() != null) {
            dto.setGrantedByEmail(entity.getGrantedBy().getEmail());
        }

        if (entity.getRack() != null) {
            dto.setRackId(entity.getRack().getId());
            dto.setRackLocationLabel(entity.getRack().getLocationLabel());
        }

        return dto;
    }

    /**
     * Convierte una colección de accesos en lista de DTOs.
     */
    public static List<UserRackAccessDTO> toDTOList(Collection<UserRackAccess> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .sorted(Comparator.comparing(access -> access.getUser() != null ? access.getUser().getEmail() : "", String::compareToIgnoreCase))
                .map(UserRackAccessMapper::toDTO)
                .toList();
    }

    /**
     * Crea una nueva entidad de acceso a rack.
     */
    public static UserRackAccess toEntity(User user, Rack rack, RackPermission permission) {
        if (user == null || rack == null) {
            return null;
        }

        UserRackAccess entity = new UserRackAccess();
        entity.setId(new UserRackAccessId(user.getId(), rack.getId()));
        entity.setUser(user);
        entity.setRack(rack);
        entity.setPermission(permission != null ? permission : RackPermission.READ);
        return entity;
    }
}
