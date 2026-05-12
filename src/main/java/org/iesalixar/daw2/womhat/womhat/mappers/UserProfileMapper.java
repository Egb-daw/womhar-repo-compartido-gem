package org.iesalixar.daw2.womhat.womhat.mappers;

import org.iesalixar.daw2.womhat.womhat.dtos.UserProfileDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.UserProfileFormDTO;
import org.iesalixar.daw2.womhat.womhat.entities.User;
import org.iesalixar.daw2.womhat.womhat.entities.UserProfile;

/**
 * Mapper utilitario entre la entidad {@link UserProfile} y sus DTOs.
 */
public class UserProfileMapper {

    /**
     * Convierte un perfil a DTO de lectura.
     */
    public static UserProfileDTO toDTO(UserProfile entity) {
        if (entity == null) {
            return null;
        }

        return new UserProfileDTO(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhoneNumber(),
                entity.getProfileImage(),
                entity.getBio(),
                entity.getLocale(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Convierte un usuario y su perfil al DTO de formulario de "Mi perfil".
     */
    public static UserProfileFormDTO toFormDto(User user, UserProfile profile) {
        if (user == null) {
            return null;
        }

        UserProfileFormDTO dto = new UserProfileFormDTO();
        dto.setUserId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setAccountActive(user.isActive());

        if (profile != null) {
            dto.setFirstName(profile.getFirstName());
            dto.setLastName(profile.getLastName());
            dto.setPhoneNumber(profile.getPhoneNumber());
            dto.setProfileImage(profile.getProfileImage());
            dto.setBio(profile.getBio());
            dto.setLocale(profile.getLocale());
        }

        return dto;
    }

    /**
     * Crea una nueva entidad de perfil a partir del formulario.
     */
    public static UserProfile toNewEntity(UserProfileFormDTO dto, User user) {
        if (dto == null || user == null) {
            return null;
        }

        UserProfile profile = new UserProfile();
        profile.setId(null);
        profile.setUser(user);
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setProfileImage(dto.getProfileImage());
        profile.setBio(dto.getBio());
        profile.setLocale(dto.getLocale());
        return profile;
    }

    /**
     * Copia los campos editables del formulario sobre una entidad existente.
     */
    public static void copyToExistingEntity(UserProfileFormDTO dto, UserProfile profile) {
        if (dto == null || profile == null) {
            return;
        }

        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setProfileImage(dto.getProfileImage());
        profile.setBio(dto.getBio());
        profile.setLocale(dto.getLocale());
    }
}
