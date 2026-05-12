package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO genérico de lectura para User.
 * Se puede usar tanto para listados como para vistas de detalle simples.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /** ID del usuario. */
    private Long id;

    /** Email usado para autenticación. */
    private String email;

    /** Indica si la cuenta está activa. */
    private boolean active;

    /** Indica si la cuenta no está bloqueada. */
    private boolean accountNonLocked;

    /** Fecha del último cambio de contraseña. */
    private LocalDateTime lastPasswordChange;

    /** Fecha de caducidad de la contraseña. */
    private LocalDateTime passwordExpiresAt;

    /** Número de intentos fallidos de login. */
    private Integer failedLoginAttempts;

    /** Indica si el email del usuario está verificado. */
    private boolean emailVerified;

    /** Indica si debe cambiar la contraseña al entrar. */
    private boolean mustChangePassword;

    /** Roles asignados al usuario en formato técnico o visible. */
    private Set<String> roles;
}