package org.iesalixar.daw2.womhat.womhat.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO para crear usuarios desde un formulario de administración.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDTO {

    /** En alta este campo vendrá null porque la BD lo autogenera. */
    private Long id;

    /** Email del nuevo usuario. */
    @Email(message = "{msg.user-form.email.invalid}")
    @NotBlank(message = "{msg.user-form.email.notblank}")
    @Size(max = 100, message = "{msg.user-form.email.size}")
    private String email;

    /** Estado activo de la cuenta. */
    private boolean active = true;

    /** Indica si la cuenta no está bloqueada. */
    private boolean accountNonLocked = true;

    /** Fecha del último cambio de contraseña. */
    @PastOrPresent(message = "{msg.user-form.lastPasswordChange.pastOrPresent}")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime lastPasswordChange;

    /** Fecha de caducidad de la contraseña. */
    @FutureOrPresent(message = "{msg.user-form.passwordExpiresAt.futureOrPresent}")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime passwordExpiresAt;

    /** Número de intentos fallidos. */
    @Min(value = 0, message = "{msg.user-form.failedLoginAttempts.min}")
    private Integer failedLoginAttempts = 0;

    /** Indica si el email ya está verificado. */
    private boolean emailVerified = false;

    /** Indica si debe cambiar contraseña al iniciar sesión. */
    private boolean mustChangePassword = false;

    /** IDs de roles seleccionados en el formulario. */
    @NotEmpty(message = "{msg.user-form.roles.notempty}")
    private Set<Long> roleIds = new HashSet<>();

    /** Contraseña inicial en texto plano. */
    @NotBlank(message = "{msg.user-form.password.notblank}")
    @Size(min = 8, max = 72, message = "{msg.user-form.password.create.size}")
    private String password;

    @AssertTrue(message = "{msg.user-form.passwordDates.order}")
    public boolean isPasswordDatesOrderValid() {
        return lastPasswordChange == null || passwordExpiresAt == null || !passwordExpiresAt.isBefore(lastPasswordChange);
    }
}
