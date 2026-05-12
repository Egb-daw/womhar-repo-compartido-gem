package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de lectura para el perfil extendido del usuario.
 *
 * Representa la información de la tabla user_profiles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    /** ID del usuario al que pertenece el perfil. */
    private Long userId;

    /** Nombre del usuario. */
    private String firstName;

    /** Apellidos del usuario. */
    private String lastName;

    /** Teléfono de contacto. */
    private String phoneNumber;

    /** Ruta o URL de la imagen de perfil. */
    private String profileImage;

    /** Biografía breve o descripción del usuario. */
    private String bio;

    /** Locale o idioma preferido. */
    private String locale;

    /** Fecha de creación del perfil. */
    private LocalDateTime createdAt;

    /** Fecha de última actualización del perfil. */
    private LocalDateTime updatedAt;
}