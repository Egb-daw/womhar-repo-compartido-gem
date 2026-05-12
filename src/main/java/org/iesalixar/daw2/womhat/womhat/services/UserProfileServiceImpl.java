package org.iesalixar.daw2.womhat.womhat.services;

import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.dtos.UserProfileFormDTO;
import org.iesalixar.daw2.womhat.womhat.entities.User;
import org.iesalixar.daw2.womhat.womhat.entities.UserProfile;
import org.iesalixar.daw2.womhat.womhat.exceptions.InvalidFileException;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.mappers.UserProfileMapper;
import org.iesalixar.daw2.womhat.womhat.repositories.UserProfileRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRepository;
import org.iesalixar.daw2.womhat.womhat.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * Implementación del servicio de perfil de usuario.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileServiceImpl.class);

    private static final long MAX_IMAGE_SIZE_BYTES = 2 * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            "jpg",
            "jpeg",
            "png",
            "webp",
            "gif"
    );
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final Set<String> ACCOUNT_DEACTIVATION_CONFIRMATION_TOKENS = Set.of(
            "DESACTIVAR",
            "DEACTIVATE"
    );

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Devuelve el DTO de formulario del perfil del usuario.
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileFormDTO getFormByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("user", "email", email));

        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);

        return UserProfileMapper.toFormDto(user, profile);
    }

    /**
     * Crea o actualiza el perfil del usuario.
     */
    @Override
    public void updateProfile(String email, UserProfileFormDTO profileDto, MultipartFile profileImageFile) {
        logger.info("Actualizando perfil para email={}", email);

        User user = findUserByEmail(email);
        UserProfile existingProfile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        boolean isNew = existingProfile == null;

        /*
         * No confiamos en la ruta de imagen que venga del formulario.
         * Si no se sube una nueva imagen, mantenemos la actual almacenada en BD.
         */
        if (existingProfile != null) {
            profileDto.setProfileImage(existingProfile.getProfileImage());
        } else {
            profileDto.setProfileImage(null);
        }

        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            validateProfileImage(profileImageFile);

            String oldImagePath = existingProfile != null ? existingProfile.getProfileImage() : null;
            String newImageWebPath = fileStorageService.saveFile(profileImageFile);

            if (newImageWebPath == null || newImageWebPath.isBlank()) {
                throw new InvalidFileException(
                        "userProfile",
                        "profileImageFile",
                        profileImageFile.getOriginalFilename(),
                        "No se pudo guardar la imagen de perfil."
                );
            }

            profileDto.setProfileImage(newImageWebPath);
            deleteStoredProfileImage(oldImagePath);
        }

        UserProfile profileToSave;
        if (isNew) {
            profileToSave = UserProfileMapper.toNewEntity(profileDto, user);
        } else {
            profileToSave = existingProfile;
            UserProfileMapper.copyToExistingEntity(profileDto, profileToSave);
        }

        userProfileRepository.save(profileToSave);
    }

    /**
     * Elimina la imagen de perfil del usuario autenticado. Si no hay imagen personalizada, lanza excepción.
     */
    @Override
    public void deleteProfileImage(String email) {
        logger.info("Eliminando imagen de perfil para email={}", email);

        User user = findUserByEmail(email);
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene perfil creado."));

        String currentImagePath = profile.getProfileImage();
        if (currentImagePath == null || currentImagePath.isBlank()) {
            throw new IllegalStateException("El perfil no tiene imagen personalizada.");
        }

        deleteStoredProfileImage(currentImagePath);
        profile.setProfileImage(null);

        userProfileRepository.save(profile);
    }

    /**
     * Desactiva la cuenta propia sin borrar datos funcionales (pedidos, permisos, histórico).
     */
    @Override
    public void deactivateOwnAccount(String email, String confirmationText) {
        logger.info("Solicitud de desactivación de cuenta para email={}", email);

        if (confirmationText == null
                || !ACCOUNT_DEACTIVATION_CONFIRMATION_TOKENS.contains(confirmationText.trim().toUpperCase())) {
            throw new IllegalArgumentException("msg.userProfile.account.deactivate.confirm.invalid");
        }

        User user = findUserByEmail(email);

        if (!user.isActive()) {
            throw new IllegalStateException("msg.userProfile.account.deactivate.alreadyInactive");
        }

        if (hasRole(user, ROLE_ADMIN) && userRepository.countByRoles_Name(ROLE_ADMIN) <= 1) {
            throw new IllegalStateException("msg.userProfile.account.deactivate.lastAdmin");
        }

        user.setActive(false);
        user.setAccountNonLocked(false);
        userRepository.save(user);
        logger.info("Cuenta desactivada correctamente para email={}", email);
    }

    /**
     * Busca el usuario por email o lanza excepción si no existe.
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("user", "email", email));
    }

    /**
     * Elimina un fichero almacenado si existe una ruta válida.
     */
    private void deleteStoredProfileImage(String imagePath) {
        if (imagePath != null && !imagePath.isBlank()) {
            fileStorageService.deleteFile(imagePath);
        }
    }

    /**
     * Valida la imagen del perfil.
     */
    private void validateProfileImage(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null) {
            int lastDot = originalFilename.lastIndexOf('.');
            if (lastDot >= 0 && lastDot < originalFilename.length() - 1) {
                extension = originalFilename.substring(lastDot + 1).toLowerCase();
            }
        }

        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException(
                    "userProfile",
                    "profileImageFile",
                    contentType,
                    "Tipo de archivo no permitido."
            );
        }

        if (extension.isBlank() || !ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException(
                    "userProfile",
                    "profileImageFile",
                    originalFilename,
                    "Extensión de archivo no permitida."
            );
        }

        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new InvalidFileException(
                    "userProfile",
                    "profileImageFile",
                    file.getSize(),
                    "Archivo demasiado grande."
            );
        }
    }

    private boolean hasRole(User user, String roleName) {
        return user != null
                && user.getRoles() != null
                && user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(roleName::equalsIgnoreCase);
    }
}
