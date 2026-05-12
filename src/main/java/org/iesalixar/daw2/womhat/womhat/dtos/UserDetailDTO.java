package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * DTO de detalle completo para usuarios.
 *
 * Además de la información base del usuario, incluye
 * datos del perfil y accesos a racks.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDTO {

    /** ID del usuario. */
    private Long id;

    /** Email del usuario. */
    private String email;

    /** Estado activo/inactivo de la cuenta. */
    private boolean active;

    /** Indica si la cuenta no está bloqueada. */
    private boolean accountNonLocked;

    /** Fecha del último cambio de contraseña. */
    private LocalDateTime lastPasswordChange;

    /** Fecha de caducidad de la contraseña. */
    private LocalDateTime passwordExpiresAt;

    /** Intentos fallidos de login. */
    private Integer failedLoginAttempts;

    /** Indica si el email está verificado. */
    private boolean emailVerified;

    /** Indica si debe cambiar la contraseña. */
    private boolean mustChangePassword;

    /** Datos del perfil del usuario. */
    private UserProfileFormDTO profile;

    /** Roles asignados al usuario. */
    private Set<String> roles;

    /** Accesos concretos del usuario a racks. */
    private List<UserRackAccessDTO> rackAccessList;
}