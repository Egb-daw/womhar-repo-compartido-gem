package org.iesalixar.daw2.womhat.womhat.mappers;

import org.iesalixar.daw2.womhat.womhat.dtos.UserCreateDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.UserDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.UserDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.UserUpdateDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Role;
import org.iesalixar.daw2.womhat.womhat.entities.User;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper utilitario entre la entidad {@link User} y sus DTOs.
 */
public class UserMapper {

    /**
     * Convierte una entidad {@link User} a DTO de listado.
     */
    public static UserDTO toDTO(User entity) {
        if (entity == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setActive(entity.isActive());
        dto.setAccountNonLocked(entity.isAccountNonLocked());
        dto.setLastPasswordChange(entity.getLastPasswordChange());
        dto.setPasswordExpiresAt(entity.getPasswordExpiresAt());
        dto.setFailedLoginAttempts(entity.getFailedLoginAttempts());
        dto.setEmailVerified(entity.isEmailVerified());
        dto.setMustChangePassword(entity.isMustChangePassword());
        dto.setRoles(extractRoleNames(entity));
        return dto;
    }

    /**
     * Convierte una colección de usuarios en lista de DTOs.
     */
    public static List<UserDTO> toDTOList(Collection<User> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .sorted(Comparator.comparing(User::getEmail, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(UserMapper::toDTO)
                .toList();
    }

    /**
     * Convierte un usuario a DTO de detalle.
     */
    public static UserDetailDTO toDetailDTO(User entity) {
        if (entity == null) {
            return null;
        }

        UserDetailDTO dto = new UserDetailDTO();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setActive(entity.isActive());
        dto.setAccountNonLocked(entity.isAccountNonLocked());
        dto.setLastPasswordChange(entity.getLastPasswordChange());
        dto.setPasswordExpiresAt(entity.getPasswordExpiresAt());
        dto.setFailedLoginAttempts(entity.getFailedLoginAttempts());
        dto.setEmailVerified(entity.isEmailVerified());
        dto.setMustChangePassword(entity.isMustChangePassword());
        dto.setRoles(extractRoleNames(entity));
        dto.setProfile(UserProfileMapper.toFormDto(entity, entity.getProfile()));
        dto.setRackAccessList(UserRackAccessMapper.toDTOList(entity.getRackAccess()));
        return dto;
    }

    /**
     * Convierte un usuario a DTO de formulario de edición.
     */
    public static UserUpdateDTO toUpdateDTO(User entity) {
        if (entity == null) {
            return null;
        }

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setActive(entity.isActive());
        dto.setAccountNonLocked(entity.isAccountNonLocked());
        dto.setLastPasswordChange(entity.getLastPasswordChange());
        dto.setPasswordExpiresAt(entity.getPasswordExpiresAt());
        dto.setFailedLoginAttempts(entity.getFailedLoginAttempts());
        dto.setEmailVerified(entity.isEmailVerified());
        dto.setMustChangePassword(entity.isMustChangePassword());

        if (entity.getRoles() != null) {
            dto.setRoleIds(
                    entity.getRoles().stream()
                            .map(Role::getId)
                            .collect(Collectors.toSet())
            );
        }

        return dto;
    }

    /**
     * Crea una nueva entidad User desde el DTO de alta.
     *
     * La contraseña y los roles suelen resolverse en la capa de servicio.
     */
    public static User toEntity(UserCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        User entity = new User();
        entity.setEmail(dto.getEmail());
        entity.setActive(dto.isActive());
        entity.setAccountNonLocked(dto.isAccountNonLocked());
        entity.setLastPasswordChange(dto.getLastPasswordChange());
        entity.setPasswordExpiresAt(dto.getPasswordExpiresAt());
        entity.setFailedLoginAttempts(dto.getFailedLoginAttempts());
        entity.setEmailVerified(dto.isEmailVerified());
        entity.setMustChangePassword(dto.isMustChangePassword());
        return entity;
    }

    /**
     * Variante de alta donde los roles ya llegan resueltos.
     */
    public static User toEntity(UserCreateDTO dto, Set<Role> roles) {
        User entity = toEntity(dto);
        if (entity != null) {
            entity.setRoles(roles != null ? roles : new HashSet<>());
        }
        return entity;
    }

    /**
     * Crea una nueva entidad User desde el DTO de edición.
     *
     * Útil en escenarios de reemplazo completo, aunque normalmente se recomienda
     * cargar la entidad desde BD y usar copyToExistingEntity(...).
     */
    public static User toEntity(UserUpdateDTO dto) {
        if (dto == null) {
            return null;
        }

        User entity = new User();
        entity.setId(dto.getId());
        entity.setEmail(dto.getEmail());
        entity.setActive(dto.isActive());
        entity.setAccountNonLocked(dto.isAccountNonLocked());
        entity.setLastPasswordChange(dto.getLastPasswordChange());
        entity.setPasswordExpiresAt(dto.getPasswordExpiresAt());
        entity.setFailedLoginAttempts(dto.getFailedLoginAttempts());
        entity.setEmailVerified(dto.isEmailVerified());
        entity.setMustChangePassword(dto.isMustChangePassword());
        return entity;
    }

    /**
     * Variante de edición donde los roles ya vienen resueltos.
     */
    public static User toEntity(UserUpdateDTO dto, Set<Role> roles) {
        User entity = toEntity(dto);
        if (entity != null) {
            entity.setRoles(roles != null ? roles : new HashSet<>());
        }
        return entity;
    }

    /**
     * Copia los campos editables del DTO sobre una entidad existente.
     */
    public static void copyToExistingEntity(UserUpdateDTO dto, User entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setEmail(dto.getEmail());
        entity.setActive(dto.isActive());
        entity.setAccountNonLocked(dto.isAccountNonLocked());
        entity.setLastPasswordChange(dto.getLastPasswordChange());
        entity.setPasswordExpiresAt(dto.getPasswordExpiresAt());
        entity.setFailedLoginAttempts(dto.getFailedLoginAttempts());
        entity.setEmailVerified(dto.isEmailVerified());
        entity.setMustChangePassword(dto.isMustChangePassword());
    }

    /**
     * Extrae los nombres técnicos de rol del usuario y evita nulls en la vista.
     */
    private static Set<String> extractRoleNames(User entity) {
        if (entity == null || entity.getRoles() == null || entity.getRoles().isEmpty()) {
            return new HashSet<>();
        }

        return entity.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}