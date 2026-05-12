package org.iesalixar.daw2.womhat.womhat.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO del formulario de edición del perfil personal del usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileFormDTO {

    /** ID del usuario autenticado. */
    private Long userId;

    /** Email informativo, normalmente solo lectura. */
    private String email;

    /** Estado de la cuenta (activo/inactivo), solo lectura en la vista de perfil. */
    private Boolean accountActive;

    /** Nombre del usuario. */
    @NotBlank(message = "{msg.userProfile.firstName.notblank}")
    @Size(max = 60, message = "{msg.userProfile.firstName.size}")
    private String firstName;

    /** Apellidos del usuario. */
    @NotBlank(message = "{msg.userProfile.lastName.notblank}")
    @Size(max = 80, message = "{msg.userProfile.lastName.size}")
    private String lastName;

    /** Teléfono opcional. */
    @Size(max = 30, message = "{msg.userProfile.phoneNumber.size}")
    @Pattern(
            regexp = "^[0-9+()\\s-]*$",
            message = "{msg.userProfile.phoneNumber.invalid}"
    )
    private String phoneNumber;

    /** Imagen de perfil opcional. */
    @Size(max = 255, message = "{msg.userProfile.profileImage.size}")
    private String profileImage;

    /** Biografía opcional del usuario. */
    @Size(max = 500, message = "{msg.userProfile.bio.size}")
    private String bio;

    /** Locale o idioma preferido. */
    @Size(max = 10, message = "{msg.userProfile.locale.size}")
    private String locale;
}
