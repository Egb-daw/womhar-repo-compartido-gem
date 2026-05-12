package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.UserProfileFormDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio para la edición del perfil del usuario autenticado.
 */
public interface UserProfileService {

    /**
     * Obtiene el formulario de perfil a partir del email del usuario.
     */
    UserProfileFormDTO getFormByEmail(String email);

    /**
     * Actualiza el perfil del usuario.
     */
    void updateProfile(String email, UserProfileFormDTO profileDto, MultipartFile profileImageFile);

    /**
     * Elimina la imagen de perfil del usuario autenticado.
     */
    void deleteProfileImage(String email);

    /**
     * Desactiva la cuenta del usuario autenticado de forma no destructiva.
     */
    void deactivateOwnAccount(String email, String confirmationText);
}
